package name.remal.github_actions.common.logging;

import static java.nio.charset.StandardCharsets.UTF_8;
import static name.remal.github_actions.utils.Environment.GITHUB_ACTIONS;
import static name.remal.github_actions.utils.Environment.RUNNER_DEBUG;
import static org.apache.logging.log4j.spi.StandardLevel.ERROR;
import static org.apache.logging.log4j.spi.StandardLevel.INFO;
import static org.apache.logging.log4j.spi.StandardLevel.WARN;

import java.nio.charset.Charset;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.val;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;

@Plugin(name = "GitHubActionLayout", category = Core.CATEGORY_NAME, elementType = Layout.ELEMENT_TYPE)
class GitHubActionLogsLayout extends AbstractStringLayout {

    private static final Charset CHARSET = UTF_8;
    private static final Pattern NEW_LINE = Pattern.compile("\\r\\n|\\n\\r|\\r|\\n");
    private static final String DEFAULT_PATTERN = "%-5level: %logger - %msg%n";

    private final PatternLayout defaultLayout;
    private final PatternLayout exceptionsLayout;

    public GitHubActionLogsLayout(Configuration configuration) {
        super(configuration, CHARSET, null, null);

        this.defaultLayout = PatternLayout.newBuilder()
            .withConfiguration(configuration)
            .withPattern(DEFAULT_PATTERN)
            .withCharset(CHARSET)
            .build();

        this.exceptionsLayout = PatternLayout.newBuilder()
            .withConfiguration(configuration)
            .withPattern("")
            .withCharset(CHARSET)
            .build();
    }

    @Override
    public String toSerializable(LogEvent event) {
        if (!GITHUB_ACTIONS) {
            return defaultLayout.toSerializable(event);
        }

        val intLevel = event.getLevel().getStandardLevel().intLevel();
        if (intLevel <= ERROR.intLevel()) {
            return formatGitHubActionMessage(event, "error");

        } else if (intLevel <= WARN.intLevel()) {
            return formatGitHubActionMessage(event, "warning");

        } else if (intLevel <= INFO.intLevel()) {
            return formatGitHubActionMessage(event, null);

        } else if (RUNNER_DEBUG) {
            return formatGitHubActionMessage(event, "debug");
        }

        return "";
    }

    private String formatGitHubActionMessage(LogEvent event, @Nullable String command) {
        val sb = new StringBuilder();

        if (command != null && !command.isEmpty()) {
            sb.append("::").append(command).append("::");
        }

        val fullMessage = new StringBuilder();
        fullMessage.append(event.getMessage().getFormattedMessage());
        if (event.getThrown() != null) {
            if (fullMessage.length() > 0) {
                fullMessage.append('\n');
            }
            fullMessage.append(exceptionsLayout.toSerializable(event));
        }

        if (fullMessage.length() == 0) {
            return "";
        }

        val lines = NEW_LINE.split(fullMessage.toString());
        sb.append(lines[0]);

        if (lines.length > 1) {
            for (int i = 1; i < lines.length; ++i) {
                val line = lines[i];
                if (command != null && !command.isEmpty()) {
                    sb.append("%0A");
                } else {
                    sb.append('\n');
                }
                sb.append('\t').append(line);
            }
        }

        return sb.append('\n').toString();
    }


    @PluginFactory
    public static GitHubActionLogsLayout newGitHubActionLogsLayout(
        @PluginConfiguration Configuration configuration
    ) {
        return new GitHubActionLogsLayout(configuration);
    }

}
