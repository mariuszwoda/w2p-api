package pl.where2play.api.config;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentValidator {
    private final Environment environment;

    public EnvironmentValidator(Environment environment) {
        this.environment = environment;
    }

    public boolean isTestEnvironment() {
        return environment.acceptsProfiles(Profiles.of("local", "dev", "sit"));
    }
}
