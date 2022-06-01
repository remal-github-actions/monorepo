package name.remal.github_actions.common.logging;

import static java.nio.charset.StandardCharsets.UTF_8;
import static name.remal.github_actions.utils.Environment.ACTIONS_STEP_DEBUG;
import static name.remal.github_actions.utils.Environment.GITHUB_ACTIONS;
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
    private static final String DEFAULT_PATTERN = "%-5level: %logger - %msg%n";
    private static final Pattern NEW_LINE = Pattern.compile("\\r\\n|\\n\\r|\\r|\\n");

    private final PatternLayout defaultLayout;

    public GitHubActionLogsLayout(Configuration configuration) {
        super(configuration, CHARSET, null, null);
        this.defaultLayout = PatternLayout.newBuilder()
            .withConfiguration(configuration)
            .withPattern(DEFAULT_PATTERN)
            .withCharset(CHARSET)
            .build();
    }

    @Override
    public String toSerializable(LogEvent event) {
        if (!GITHUB_ACTIONS) {
            return defaultLayout.toSerializable(event);
        }

        val intLevel = event.getLevel().getStandardLevel().intLevel();
        val message = event.getMessage().getFormattedMessage();
        if (intLevel <= ERROR.intLevel()) {
            return formatGitHubActionMessage(message, "error");

        } else if (intLevel <= WARN.intLevel()) {
            return formatGitHubActionMessage(message, "warning");

        } else if (intLevel <= INFO.intLevel()) {
            return formatGitHubActionMessage(message, null);

        } else if (ACTIONS_STEP_DEBUG) {
            return formatGitHubActionMessage(message, "debug");
        }

        return "";
    }

    private static String formatGitHubActionMessage(String message, @Nullable String command) {
        val sb = new StringBuilder();

        if (command != null && !command.isEmpty()) {
            sb.append("::").append(command).append("::");
        }

        val lines = NEW_LINE.split(message);
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
