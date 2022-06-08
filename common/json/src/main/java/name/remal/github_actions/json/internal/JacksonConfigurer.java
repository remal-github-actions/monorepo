package name.remal.github_actions.json.internal;

import static com.fasterxml.jackson.core.JsonFactory.Feature.INTERN_FIELD_NAMES;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_JAVA_COMMENTS;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_TRAILING_COMMA;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS;
import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.core.TSFBuilder;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class JacksonConfigurer {

    @SuppressWarnings("unchecked")
    public static <T extends TSFBuilder<?, ?>> T configureFactoryBuilderWithDefaultFeatures(T builder) {
        return (T) builder
            .disable(INTERN_FIELD_NAMES)
            .enable(ALLOW_JAVA_COMMENTS)
            .enable(ALLOW_SINGLE_QUOTES)
            .enable(ALLOW_UNQUOTED_FIELD_NAMES)
            .enable(ALLOW_TRAILING_COMMA);
    }

    @SuppressWarnings("unchecked")
    public static <T extends MapperBuilder<?, ?>> T configureMapperBuilderWithDefaultFeatures(T builder) {
        return (T) builder
            .findAndAddModules()
            .disable(WRITE_DATES_AS_TIMESTAMPS)
            .disable(WRITE_DATE_KEYS_AS_TIMESTAMPS)
            .enable(FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(SORT_PROPERTIES_ALPHABETICALLY);
    }

}
