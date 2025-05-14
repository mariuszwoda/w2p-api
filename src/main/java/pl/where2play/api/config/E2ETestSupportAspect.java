package pl.where2play.api.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class E2ETestSupportAspect {

    private final Environment environment;

    public E2ETestSupportAspect(Environment environment) {
        this.environment = environment;
    }

    @Before("@annotation(pl.where2play.api.config.E2ETestOnly)")
    public void validateE2EMethodCall(JoinPoint joinPoint) {
        // Check if we're in an appropriate environment
        boolean isTestEnvironment = environment.acceptsProfiles(Profiles.of("local", "dev", "sit"));

        if (!isTestEnvironment) {
            throw new UnsupportedOperationException("E2E test support methods are not available in this environment");
        }

        // Check caller using stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        boolean calledFromE2EController = false;

        for (int i = 1; i < stackTrace.length && i < 10; i++) {
            String className = stackTrace[i].getClassName();
            // Check if caller is an E2E controller (this is simplified)
            if (className.contains("E2ETestSupportController")) {
                calledFromE2EController = true;
                break;
            }
        }

        if (!calledFromE2EController) {
            throw new UnsupportedOperationException(
                    "E2E test support methods can only be called from designated E2E test support endpoints");
        }
    }
}
