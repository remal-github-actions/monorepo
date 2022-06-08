package name.remal.github_actions.core;

import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedWriter;
import static java.util.Collections.emptyMap;
import static java.util.UUID.randomUUID;
import static javax.annotation.meta.When.UNKNOWN;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.github_actions.core.Environment.GITHUB_ENV;
import static name.remal.github_actions.core.Environment.GITHUB_PATH;
import static name.remal.github_actions.core.Environment.GITHUB_STEP_SUMMARY;
import static name.remal.github_actions.json.Json.JSON_MAPPER;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.intellij.lang.annotations.Language;

@RequiredArgsConstructor(access = PRIVATE)
public abstract class Commands {

    @SuppressWarnings("java:S106")
    public static synchronized void logInfo(@Nullable Object value) {
        val valueString = toCommandValue(value);
        System.out.println(toSystemNewLines(valueString));
    }


    @Nullable
    public static synchronized String getInput(String name) {
        return getInput(name, true);
    }

    @Nullable
    public static synchronized String getInput(String name, boolean trimWhitespace) {
        name = name.replace(' ', '_').toUpperCase();
        val endVar = "INPUT_" + name;
        var value = System.getenv(endVar);

        if (trimWhitespace && value != null) {
            value = value.trim();
        }

        return value;
    }


    @Nullable
    public static synchronized String getState(String name) {
        name = name.replace(' ', '_').toUpperCase();
        return System.getenv("INPUT_" + name);
    }

    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#sending-values-to-the-pre-and-post-actions">Sending values to the pre and post actions</a>.
     */
    public static synchronized void saveState(String name, @Nullable Object value) {
        name = name.replace(' ', '_').toUpperCase();
        issueCommand("set-output", value, CommandProperties.fromMap("name", name));
    }


    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#setting-an-environment-variable">Setting an environment variable</a>.
     */
    public static synchronized void exportEnvVar(String name, @Nullable Object value) {
        val message = new StringBuilder();
        val delimiter = "__GitHubActionsFileCommandDelimiter_" + randomUUID() + "__";
        message.append(name).append("<<").append(delimiter).append(lineSeparator())
            .append(toCommandValue(value)).append(lineSeparator())
            .append(delimiter);

        issueFileCommand(GITHUB_ENV, message);
    }


    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#masking-a-value-in-log">Masking a value in log</a>.
     */
    public static synchronized void addMask(String secret) {
        issueCommand("add-mask", secret);
    }


    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#adding-a-system-path">Adding a system path</a>.
     */
    public static synchronized void addToPathEnvVar(File path) {
        addToPathEnvVar(path.toPath());
    }

    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#adding-a-system-path">Adding a system path</a>.
     */
    public static synchronized void addToPathEnvVar(Path path) {
        issueFileCommand(GITHUB_PATH, path);
    }


    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#adding-a-system-path">Adding a system path</a>.
     */
    public static synchronized void addStepSummary(@Nullable @Language("Markdown") String summary) {
        addStepSummary((Object) summary);
    }

    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#adding-a-system-path">Adding a system path</a>.
     */
    public static synchronized void addStepSummary(@Nullable Object summary) {
        issueFileCommand(GITHUB_STEP_SUMMARY, summary);
    }


    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#setting-an-output-parameter">Setting an output parameter</a>.
     */
    public static synchronized void setOutput(String name, @Nullable Object value) {
        issueCommand("set-output", value, CommandProperties.fromMap("name", name));
    }

    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#setting-a-debug-message">Setting a debug message</a>.
     */
    public static synchronized void logDebug(@Nullable Object value) {
        issueCommand("debug", value);
    }


    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#setting-a-notice-message">Setting a notice message</a>.
     */
    public static synchronized void logNotice(@Nullable Object value) {
        logNotice(value, null);
    }

    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#setting-a-notice-message">Setting a notice message</a>.
     */
    public static synchronized void logNotice(
        @Nullable Object value,
        @Nullable AnnotationProperties properties
    ) {
        issueCommand("notice", value, properties);
    }


    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#setting-a-warning-message">Setting a warning message</a>.
     */
    public static synchronized void logWarning(@Nullable Object value) {
        logWarning(value, null);
    }

    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#setting-a-warning-message">Setting a warning message</a>.
     */
    public static synchronized void logWarning(
        @Nullable Object value,
        @Nullable AnnotationProperties properties
    ) {
        issueCommand("warning", value, properties);
    }


    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#setting-an-error-message">Setting an error message</a>.
     */
    public static synchronized void logError(@Nullable Object value) {
        logError(value, null);
    }

    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#setting-an-error-message">Setting an error message</a>.
     */
    public static synchronized void logError(
        @Nullable Object value,
        @Nullable AnnotationProperties properties
    ) {
        issueCommand("error", value, properties);
    }


    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#grouping-log-lines">Grouping log lines</a>.
     */
    public static synchronized void forLogGroup(@Nullable Object title, VoidAction action) {
        forLogGroup(title, (Action<Void>) () -> {
            action.execute();
            return null;
        });
    }

    private static final AtomicBoolean IS_IN_GROUP = new AtomicBoolean();

    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#grouping-log-lines">Grouping log lines</a>.
     */
    @Nonnull(when = UNKNOWN)
    @SneakyThrows
    public static synchronized <T> T forLogGroup(@Nullable Object title, Action<T> action) {
        val titleString = toCommandValue(title);
        if (titleString.isEmpty()) {
            return action.execute();
        }

        if (!IS_IN_GROUP.compareAndSet(false, true)) {
            logInfo(titleString + ": start");
            try {
                return action.execute();
            } finally {
                logInfo(titleString + ": end");
            }
        }

        issueCommand("group", titleString);
        try {
            return action.execute();
        } finally {
            issueCommand("endgroup");
            IS_IN_GROUP.set(false);
        }
    }


    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#stopping-and-starting-workflow-commands">Stopping and starting workflow commands</a>.
     */
    public static synchronized void forDisabledWorkflowCommands(VoidAction action) {
        forDisabledWorkflowCommands((Action<Void>) () -> {
            action.execute();
            return null;
        });
    }

    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#stopping-and-starting-workflow-commands">Stopping and starting workflow commands</a>.
     */
    @Nonnull(when = UNKNOWN)
    @SneakyThrows
    public static synchronized <T> T forDisabledWorkflowCommands(Action<T> action) {
        val token = randomUUID().toString();
        issueCommand("stop-commands", token);
        try {
            return action.execute();
        } finally {
            issueCommand(token);
        }
    }


    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#echoing-command-outputs">Echoing command outputs</a>.
     */
    public static synchronized void forEchoingWorkflowCommands(VoidAction action) {
        forEchoingWorkflowCommands((Action<Void>) () -> {
            action.execute();
            return null;
        });
    }

    /**
     * See <a href="https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#echoing-command-outputs">Echoing command outputs</a>.
     */
    @Nonnull(when = UNKNOWN)
    @SneakyThrows
    public static synchronized <T> T forEchoingWorkflowCommands(Action<T> action) {
        issueCommand("echo", "on");
        try {
            return action.execute();
        } finally {
            issueCommand("echo", "off");
        }
    }


    @SneakyThrows
    public static synchronized String toCommandValue(@Nullable Object object) {
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
            return object.toString().trim();
        }

        val tree = JSON_MAPPER.valueToTree(object);
        if (tree.isValueNode()) {
            return tree.asText().trim();
        } else {
            return tree.toString();
        }
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

    private static synchronized void issueCommand(String command) {
        issueCommand(command, null, null);
    }

    private static synchronized void issueCommand(String command, @Nullable Object message) {
        issueCommand(command, message, null);
    }

    @SuppressWarnings("java:S106")
    private static synchronized void issueCommand(
        String command,
        @Nullable Object message,
        @Nullable CommandProperties properties
    ) {
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

        sb.append("::");

        var messageString = toCommandValue(message);
        if (!messageString.isEmpty()) {
            messageString = toSystemNewLines(messageString);
            messageString = escapeData(messageString);
            sb.append(messageString);
        }

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


    private static final Pattern NEW_LINE = Pattern.compile("\\r\\n|\\n\\r|\\r|\\n");
    private static final String UNIX_NEW_LINE = "\n";

    private static String toUnixNewLines(String string) {
        return NEW_LINE.matcher(string).replaceAll(UNIX_NEW_LINE);
    }

    private static String toSystemNewLines(String string) {
        return toUnixNewLines(string)
            .replace(UNIX_NEW_LINE, lineSeparator());
    }

}
