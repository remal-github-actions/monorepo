package name.remal.github_actions.common.logging;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import org.apache.logging.log4j.jul.Log4jBridgeHandler;

@NoArgsConstructor(access = PRIVATE)
public abstract class LoggingConfigurer {

    private static volatile boolean isConfigured;

    public static synchronized void configureLogging() {
        if (isConfigured) {
            return;
        } else {
            isConfigured = true;
        }

        Log4jBridgeHandler.install(true, null, true);
    }

}
