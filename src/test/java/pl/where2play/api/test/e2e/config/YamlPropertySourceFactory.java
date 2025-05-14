package pl.where2play.api.test.e2e.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Factory for creating a PropertySource from a YAML file.
 * This is needed because Spring's @PropertySource annotation doesn't support YAML files by default.
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) throws IOException {
        Resource resource = encodedResource.getResource();

        // Create a YamlPropertiesFactoryBean to convert the YAML file to Properties
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource);
        factory.afterPropertiesSet();

        Properties properties = factory.getObject();

        // Use the resource name as the property source name if not provided
        String sourceName = (name != null) ? name : resource.getFilename();

        return new PropertiesPropertySource(sourceName, properties);
    }
}