package name.remal.github_actions.common.logging;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.logging.log4j.spi.StandardLevel.FATAL;
import static org.fusesource.jansi.Ansi.ansi;

import lombok.val;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.fusesource.jansi.AnsiConsole;

class GitHubLoggingLayout extends AbstractStringLayout {

    private static final int FATAL_INT_LEVEL = FATAL.intLevel();

    public GitHubLoggingLayout(Configuration configuration) {
        super(configuration, UTF_8, null, null);
        AnsiConsole.systemInstall();
    }

    @Override
    public String toSerializable(LogEvent event) {
        val intLevel = event.getLevel().getStandardLevel().intLevel();
        if (intLevel <= FATAL_INT_LEVEL) {
return ansi().fg
        }
        return event.getMessage().getFormattedMessage();
    }

}
