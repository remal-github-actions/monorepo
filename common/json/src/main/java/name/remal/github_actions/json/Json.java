package name.remal.github_actions.json;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.github_actions.json.internal.JacksonConfigurer.configureFactoryBuilderWithDefaultFeatures;
import static name.remal.github_actions.json.internal.JacksonConfigurer.configureMapperBuilderWithDefaultFeatures;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class Json {

    private static final JsonFactory JSON_FACTORY = configureFactoryBuilderWithDefaultFeatures(
        JsonFactory.builder()
    )
        .build();

    public static final JsonMapper JSON_MAPPER = configureMapperBuilderWithDefaultFeatures(
        JsonMapper.builder(JSON_FACTORY)
    )
        .defaultPrettyPrinter(new JsonPrettyPrinter())
        .build();


}
