package name.remal.github_actions.core;

@FunctionalInterface
public interface VoidAction {

    void execute() throws Throwable;

}
