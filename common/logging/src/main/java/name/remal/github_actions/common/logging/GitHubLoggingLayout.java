package name.remal.github_actions.common.logging;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.logging.log4j.spi.StandardLevel.ERROR;

import lombok.val;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.fusesource.jansi.AnsiConsole;

class GitHubLoggingLayout extends AbstractStringLayout {

    private static final int ERROR_INT_LEVEL = ERROR.intLevel();

    public GitHubLoggingLayout(Configuration configuration) {
        super(configuration, UTF_8, null, null);
        AnsiConsole.systemInstall();
    }

    @Override
    public String toSerializable(LogEvent event) {
        val intLevel = event.getLevel().getStandardLevel().intLevel();
        if (intLevel <= ERROR_INT_LEVEL) {
            return "";
        }
        return event.getMessage().getFormattedMessage();
    }

}
