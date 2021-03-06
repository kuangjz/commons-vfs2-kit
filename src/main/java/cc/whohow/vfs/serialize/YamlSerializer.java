package cc.whohow.vfs.serialize;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public class YamlSerializer<T> extends JsonSerializer<T> {
    private static final YAMLMapper YAML_MAPPER = new YAMLMapper();
    private static final YamlSerializer<JsonNode> INSTANCE = new YamlSerializer<>(JsonNode.class);

    public YamlSerializer(Class<T> type) {
        super(YAML_MAPPER, type);
    }

    public YamlSerializer(TypeReference<T> type) {
        super(YAML_MAPPER, type);
    }

    public YamlSerializer(String type) {
        super(YAML_MAPPER, type);
    }

    public YamlSerializer(JavaType type) {
        super(YAML_MAPPER, type);
    }

    public YamlSerializer(ObjectMapper objectMapper, Class<T> type) {
        super(objectMapper, type);
    }

    public YamlSerializer(ObjectMapper objectMapper, TypeReference<T> type) {
        super(objectMapper, type);
    }

    public YamlSerializer(ObjectMapper objectMapper, String type) {
        super(objectMapper, type);
    }

    public YamlSerializer(ObjectMapper objectMapper, JavaType type) {
        super(objectMapper, type);
    }

    public static YamlSerializer<JsonNode> get() {
        return INSTANCE;
    }
}
