package name.remal.github_actions.common.logging;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static name.remal.github_actions.utils.Environment.GITHUB_ACTIONS;
import static org.apache.logging.log4j.core.Core.CATEGORY_NAME;
import static org.apache.logging.log4j.core.Layout.ELEMENT_TYPE;
import static org.apache.logging.log4j.spi.StandardLevel.ERROR;
import static org.apache.logging.log4j.spi.StandardLevel.INFO;
import static org.apache.logging.log4j.spi.StandardLevel.WARN;

import java.nio.charset.Charset;
import java.util.regex.Pattern;
import lombok.val;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;

@Plugin(name = "GitHubActionLayout", category = CATEGORY_NAME, elementType = ELEMENT_TYPE, printObject = true)
class GitHubActionLayout extends AbstractStringLayout {

    private static final Charset CHARSET = UTF_8;
    private static final String DEFAULT_PATTERN = "%-5level: %logger - %msg%n";
    private static final Pattern NEW_LINE = Pattern.compile("\\r\\n|\\n\\r|\\r|\\n");

    private final PatternLayout defaultLayout;

    public GitHubActionLayout(Configuration configuration) {
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
            return NEW_LINE.splitAsStream(message)
                .map(line -> "::error::" + line)
                .collect(joining("\n")) + '\n';

        } else if (intLevel <= WARN.intLevel()) {
            return NEW_LINE.splitAsStream(message)
                .map(line -> "::warning::" + line)
                .collect(joining("\n")) + '\n';

        } else if (intLevel <= INFO.intLevel()) {
            return message + '\n';

        } else {
            return NEW_LINE.splitAsStream(message)
                .map(line -> "::debug::" + line)
                .collect(joining("\n")) + '\n';
        }
    }


    @PluginFactory
    public static GitHubActionLayout newGitHubActionLayout(
        @PluginConfiguration Configuration configuration
    ) {
        return new GitHubActionLayout(configuration);
    }

}
