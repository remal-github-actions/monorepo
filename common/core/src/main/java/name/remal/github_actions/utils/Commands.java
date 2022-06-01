package name.remal.github_actions.utils;

import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedWriter;
import static java.util.Collections.emptyMap;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.github_actions.utils.Environment.GITHUB_ENV;
import static name.remal.github_actions.utils.Environment.GITHUB_PATH;
import static name.remal.github_actions.utils.Json.JSON_MAPPER;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@RequiredArgsConstructor(access = PRIVATE)
public abstract class Commands {

    public static void exportVariable(String name, @Nullable Object value) {
        val message = new StringBuilder();
        val delimiter = "_GitHubActionsFileCommandDelimeter_";
        message.append(name).append("<<").append(delimiter).append(lineSeparator())
            .append(toCommandValue(value)).append(lineSeparator())
            .append(delimiter);

        issueFileCommand(GITHUB_ENV, message);
    }

    /**
     * See
     * <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#masking-a-value-in-log">Masking a value in log</a>.
     */
    public static void addMask(String secret) {
        issueCommand("add-mask", secret);
    }

    public static void addPath(File path) {
        addPath(path.toPath());
    }

    public static void addPath(Path path) {
        issueFileCommand(GITHUB_PATH, path);
    }

    @Nullable
    public static String getInput(String name) {
        return getInput(name, true);
    }

    @Nullable
    public static String getInput(String name, boolean trimWhitespace) {
        val endVar = "INPUT_" + name.replace(' ', '_').toUpperCase();
        var value = System.getenv(endVar);

        if (trimWhitespace && value != null) {
            value = value.trim();
        }

        return value;
    }

    public static void setOutput(String name, @Nullable Object value) {
        issueCommand("set-output", value, CommandProperties.fromMap("name", name));
    }

    public static void debug(@Nullable Object value) {
        issueCommand("debug", value);
    }

    public static void notice(@Nullable Object value) {
        issueCommand("notice", value);
    }


    @SneakyThrows
    private static void issueFileCommand(Path commandFile, @Nullable Object message) {
        val stringMessage = toCommandValue(message);
        if (stringMessage.isEmpty()) {
            return;
        }

        try (val writer = newBufferedWriter(commandFile, UTF_8)) {
            writer.write(stringMessage);
            writer.newLine();
        }
    }


    private static void issueCommand(String command) {
        issueCommand(command, null, null);
    }

    private static void issueCommand(String command, @Nullable Object message) {
        issueCommand(command, message, null);
    }

    private static void issueCommand(String command, CommandProperties properties) {
        issueCommand(command, null, properties);
    }

    @SuppressWarnings("java:S106")
    private static void issueCommand(String command, @Nullable Object message, @Nullable CommandProperties properties) {
        val sb = new StringBuilder();

        sb.append("::").append(command);

        val propertiesMap = Optional.ofNullable(properties)
            .map(CommandProperties::asMap)
            .orElse(emptyMap());
        if (!propertiesMap.isEmpty()) {
            sb.append(' ');
            var isFirst = true;
            for (val entry : propertiesMap.entrySet()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append(',');
                }
                sb.append(escapeProperty(entry.getKey())).append('=').append(escapeProperty(entry.getValue()));
            }
        }

        sb.append("::").append(escapeData(message));

        System.out.println(sb);
    }


    private static String escapeData(@Nullable Object object) {
        return toCommandValue(object)
            .replace("%", "%25")
            .replace("\r", "%0D")
            .replace("\n", "%0A");
    }

    private static String escapeProperty(@Nullable Object object) {
        return escapeData(object)
            .replace(":", "%3A")
            .replace(",", "%2C")
            .replace("=", "%3D");
    }

    @SneakyThrows
    private static String toCommandValue(@Nullable Object object) {
        if (object instanceof Supplier<?> supplier) {
            object = supplier.get();
        } else if (object instanceof Callable<?> callable) {
            object = callable.call();
        } else if (object instanceof Future<?> future) {
            object = future.get();
        }

        if (object == null) {
            return "";
        }

        if (object instanceof File file) {
            object = file.toPath();
        }
        if (object instanceof Path path) {
            object = path.toAbsolutePath().normalize().toString();
        }

        if (object instanceof CharSequence
            || object instanceof Number
            || object instanceof Boolean
            || object instanceof Character
            || object instanceof UUID
            || object instanceof Throwable
        ) {
            return object.toString();
        }

        val tree = JSON_MAPPER.valueToTree(object);
        if (tree.isValueNode()) {
            return tree.asText();
        } else {
            return tree.toString();
        }
    }

}
