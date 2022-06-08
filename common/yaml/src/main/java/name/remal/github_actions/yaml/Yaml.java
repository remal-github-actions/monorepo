package name.remal.github_actions.yaml;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.github_actions.json.internal.JacksonConfigurer.configureFactoryBuilderWithDefaultFeatures;
import static name.remal.github_actions.json.internal.JacksonConfigurer.configureMapperBuilderWithDefaultFeatures;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class Yaml {

    private static final YAMLFactory YAML_FACTORY = configureFactoryBuilderWithDefaultFeatures(
        YAMLFactory.builder()
    )
        .disable(WRITE_DOC_START_MARKER)
        .enable(MINIMIZE_QUOTES)
        .build();

    public static final YAMLMapper YAML_MAPPER = configureMapperBuilderWithDefaultFeatures(
        YAMLMapper.builder(YAML_FACTORY)
    )
        .build();

}
