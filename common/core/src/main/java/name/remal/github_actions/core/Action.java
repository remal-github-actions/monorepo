package name.remal.github_actions.core;

import static javax.annotation.meta.When.UNKNOWN;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface Action<T> {

    @Nonnull(when = UNKNOWN)
    T execute() throws Throwable;

}
