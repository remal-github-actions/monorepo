package build;

import static com.google.common.primitives.Primitives.unwrap;
import static com.google.common.primitives.Primitives.wrap;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@Builder
@EqualsAndHashCode(of = "name")
public class GitHubActionsEnvVar implements Comparable<GitHubActionsEnvVar> {

    String name;

    String descHtml;

    @Default
    Class<?> optionalType = String.class;

    @Default
    boolean optional = false;

    public Class<?> getType() {
        if (isOptional() && getOptionalType() != Boolean.class) {
            return wrap(getOptionalType());
        } else {
            return unwrap(getOptionalType());
        }
    }


    @Override
    public int compareTo(GitHubActionsEnvVar other) {
        return getName().compareTo(other.getName());
    }

}
