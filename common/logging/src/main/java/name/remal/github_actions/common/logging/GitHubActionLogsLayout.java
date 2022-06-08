package name.remal.github_actions.common.logging;

import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Predicate.not;
import static name.remal.github_actions.core.Commands.logDebug;
import static name.remal.github_actions.core.Commands.logError;
import static name.remal.github_actions.core.Commands.logInfo;
import static name.remal.github_actions.core.Commands.logWarning;
import static name.remal.github_actions.core.Commands.toCommandValue;
import static name.remal.github_actions.core.Environment.GITHUB_ACTIONS;
import static name.remal.github_actions.core.Environment.RUNNER_DEBUG;
import static org.apache.logging.log4j.spi.StandardLevel.ERROR;
import static org.apache.logging.log4j.spi.StandardLevel.INFO;
import static org.apache.logging.log4j.spi.StandardLevel.WARN;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.ExtendedThrowablePatternConverter;

@Plugin(name = "GitHubActionLayout", category = Core.CATEGORY_NAME, elementType = Layout.ELEMENT_TYPE)
class GitHubActionLogsLayout extends AbstractStringLayout {

    private static final Charset CHARSET = UTF_8;
    private static final String DEFAULT_PATTERN = "%-5level: %logger - %msg%n";

    private final PatternLayout defaultLayout;
    private final ExtendedThrowablePatternConverter throwablePatternConverter;

    public GitHubActionLogsLayout(Configuration configuration) {
        super(configuration, CHARSET, null, null);

        this.defaultLayout = PatternLayout.newBuilder()
            .withConfiguration(configuration)
            .withPattern(DEFAULT_PATTERN)
            .withCharset(CHARSET)
            .build();

        this.throwablePatternConverter = ExtendedThrowablePatternConverter.newInstance(configuration, new String[]{
            "full",
            "filters(" + join(",", STACK_TRACE_EXCLUSIONS) + ")"
        });
    }

    @Override
    public String toSerializable(LogEvent event) {
        event = event.toImmutable();

        if (!GITHUB_ACTIONS) {
            return defaultLayout.toSerializable(event);
        }

        val intLevel = event.getLevel().getStandardLevel().intLevel();
        if (intLevel <= ERROR.intLevel()) {
            logError(formatMessage(event));

        } else if (intLevel <= WARN.intLevel()) {
            logWarning(formatMessage(event));

        } else if (intLevel <= INFO.intLevel()) {
            logInfo(formatMessage(event));

        } else if (RUNNER_DEBUG) {
            logDebug(formatMessage(event));
        }

        return "";
    }

    @SuppressWarnings("java:S3776")
    private String formatMessage(LogEvent event) {
        val sb = new StringBuilder();

        val markerName = Optional.ofNullable(event.getMarker())
            .map(Marker::getName)
            .filter(not(String::isEmpty))
            .orElse(null);
        if (markerName != null) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append('[').append(markerName).append(']');
        }

        event.getContextData().forEach((name, valueObject) -> {
            val value = toCommandValue(valueObject);
            if (!name.isEmpty() && !value.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb
                    .append('[')
                    .append(name.replace('\r', ' ').replace('\n', ' '))
                    .append('=')
                    .append(value.replace('\r', ' ').replace('\n', ' '))
                    .append(']');
            }
        });

        val message = event.getMessage().getFormattedMessage();
        if (!message.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(message);
        }

        if (event.getThrown() != null) {
            if (sb.length() > 0) {
                if (!message.isEmpty()) {
                    sb.append(lineSeparator());
                } else {
                    sb.append(' ');
                }
            }
            val exceptionMessage = new StringBuilder();
            throwablePatternConverter.format(event, exceptionMessage);
            sb.append(exceptionMessage.toString()
                .replace("\t", "  ")
            );
        }

        return sb.toString();
    }


    @PluginFactory
    public static GitHubActionLogsLayout newGitHubActionLogsLayout(
        @PluginConfiguration Configuration configuration
    ) {
        return new GitHubActionLogsLayout(configuration);
    }


    private static final List<String> STACK_TRACE_EXCLUSIONS = List.of(
        "java.",
        "javax.",
        "kotlin.",
        "groovy.",
        "scala.",
        "sun.",
        "jdk."
    );

}
