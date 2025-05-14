package pl.where2play.api.config;

import org.springframework.context.annotation.Profile;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Profile({"local", "dev", "sit"})
public @interface E2ETestSupport {
    String value() default "This endpoint is only for E2E test framework use and is only available in non-production environments";
}