package name.remal.github_actions.common.lifecycle;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(METHOD)
@Retention(CLASS)
public @interface PostMainLifecycle {

    int order() default 0;

}
