package pl.where2play.api.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@PreAuthorize("@environmentValidator.isTestEnvironment()") //security to be available
public @interface E2ETestOnly {
    String value() default "This method should only be called from E2E test support endpoints";
}
