package name.remal.github_actions.utils;

import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class AnnotationProperties implements CommandProperties {

    @Nullable
    String title;

    @Nullable
    String file;

    @Nullable
    Integer startLine;

    @Nullable
    Integer endLine;

    @Nullable
    Integer startColumn;

    @Nullable
    Integer endColumn;

}
