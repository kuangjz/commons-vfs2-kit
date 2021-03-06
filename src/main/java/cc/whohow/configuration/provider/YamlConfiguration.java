package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.vfs.FileObjectX;
import cc.whohow.vfs.serialize.FileValue;
import cc.whohow.vfs.serialize.YamlSerializer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class YamlConfiguration<T> extends FileValue.Cache<T> implements Configuration<T> {
    public YamlConfiguration(FileObjectX fileObject, Class<T> type) {
        super(fileObject, new YamlSerializer<>(type));
    }

    public YamlConfiguration(FileObjectX fileObject, TypeReference<T> type) {
        super(fileObject, new YamlSerializer<>(type));
    }

    public YamlConfiguration(FileObjectX fileObject, String type) {
        super(fileObject, new YamlSerializer<>(type));
    }

    public YamlConfiguration(FileObjectX fileObject, JavaType type) {
        super(fileObject, new YamlSerializer<>(type));
    }

    public YamlConfiguration(FileObjectX fileObject, Class<T> type, ObjectMapper objectMapper) {
        super(fileObject, new YamlSerializer<>(objectMapper, type));
    }

    public YamlConfiguration(FileObjectX fileObject, TypeReference<T> type, ObjectMapper objectMapper) {
        super(fileObject, new YamlSerializer<>(objectMapper, type));
    }

    public YamlConfiguration(FileObjectX fileObject, String type, ObjectMapper objectMapper) {
        super(fileObject, new YamlSerializer<>(objectMapper, type));
    }

    public YamlConfiguration(FileObjectX fileObject, JavaType type, ObjectMapper objectMapper) {
        super(fileObject, new YamlSerializer<>(objectMapper, type));
    }
}
