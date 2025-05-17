package pl.where2play.w2papi.e2e.framework.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for handling JSON files.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JsonUtils {

    private final ObjectMapper objectMapper;

    /**
     * Loads an object from a JSON file in the classpath.
     *
     * @param resourcePath the path to the JSON file
     * @param valueType the type of the value to deserialize
     * @param <T> the type of the value
     * @return the deserialized value
     */
    public <T> T loadFromClasspath(String resourcePath, Class<T> valueType) {
        try {
            Resource resource = new ClassPathResource(resourcePath);
            try (InputStream inputStream = resource.getInputStream()) {
                return objectMapper.readValue(inputStream, valueType);
            }
        } catch (IOException e) {
            log.error("Failed to load JSON from classpath: {}", resourcePath, e);
            throw new RuntimeException("Failed to load JSON from classpath: " + resourcePath, e);
        }
    }

    /**
     * Loads an object from a JSON file in the file system.
     *
     * @param filePath the path to the JSON file
     * @param valueType the type of the value to deserialize
     * @param <T> the type of the value
     * @return the deserialized value
     */
    public <T> T loadFromFileSystem(String filePath, Class<T> valueType) {
        try {
            Path path = Paths.get(filePath);
            byte[] jsonData = Files.readAllBytes(path);
            return objectMapper.readValue(jsonData, valueType);
        } catch (IOException e) {
            log.error("Failed to load JSON from file system: {}", filePath, e);
            throw new RuntimeException("Failed to load JSON from file system: " + filePath, e);
        }
    }

    /**
     * Saves an object to a JSON file in the file system.
     *
     * @param filePath the path to the JSON file
     * @param value the value to serialize
     * @param <T> the type of the value
     */
    public <T> void saveToFileSystem(String filePath, T value) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            byte[] jsonData = objectMapper.writeValueAsBytes(value);
            Files.write(path, jsonData);
        } catch (IOException e) {
            log.error("Failed to save JSON to file system: {}", filePath, e);
            throw new RuntimeException("Failed to save JSON to file system: " + filePath, e);
        }
    }

    /**
     * Converts an object to a JSON string.
     *
     * @param value the value to serialize
     * @param <T> the type of the value
     * @return the JSON string
     */
    public <T> String toJson(T value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            log.error("Failed to convert object to JSON string", e);
            throw new RuntimeException("Failed to convert object to JSON string", e);
        }
    }

    /**
     * Converts a JSON string to an object.
     *
     * @param json the JSON string
     * @param valueType the type of the value to deserialize
     * @param <T> the type of the value
     * @return the deserialized value
     */
    public <T> T fromJson(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (IOException e) {
            log.error("Failed to convert JSON string to object", e);
            throw new RuntimeException("Failed to convert JSON string to object", e);
        }
    }
}