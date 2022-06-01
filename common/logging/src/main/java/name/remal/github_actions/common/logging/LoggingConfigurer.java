package name.remal.github_actions.common.logging;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.github_actions.utils.Environment.ACTIONS_STEP_DEBUG;
import static org.apache.logging.log4j.Level.DEBUG;

import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.jul.Log4jBridgeHandler;
import org.fusesource.jansi.AnsiConsole;

@NoArgsConstructor(access = PRIVATE)
public abstract class LoggingConfigurer {

    private static final String REMAL_GIT_HUB_ACTIONS_LOGGER_NAME = "name.remal.github_actions";


    private static volatile boolean isConfigured;

    public static synchronized void configureLogging() {
        if (isConfigured) {
            return;
        } else {
            isConfigured = true;
        }

        Log4jBridgeHandler.install(true, null, true);

        val loggerContext = (LoggerContext) LogManager.getContext();
        configureRemalGitHubActionsLogger(loggerContext);

        AnsiConsole.systemInstall();
    }

    private static void configureRemalGitHubActionsLogger(LoggerContext loggerContext) {
        val remalGitHubActionsLogger = loggerContext.getLogger(REMAL_GIT_HUB_ACTIONS_LOGGER_NAME);
        if (ACTIONS_STEP_DEBUG) {
            remalGitHubActionsLogger.setLevel(DEBUG);
        }
    }

}
