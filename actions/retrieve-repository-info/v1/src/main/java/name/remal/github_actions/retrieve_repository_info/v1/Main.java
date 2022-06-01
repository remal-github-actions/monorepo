package name.remal.github_actions.retrieve_repository_info.v1;

import lombok.extern.log4j.Log4j2;
import name.remal.github_actions.common.lifecycle.MainLifecycle;

@Log4j2
public class Main {

    @MainLifecycle
    public static void main() {
        log.debug("debug\n1\n2");
        log.info("info\n1\n2");
        log.warn("warn\n1\n2");
        log.error("error\n1\n2");
    }

}
