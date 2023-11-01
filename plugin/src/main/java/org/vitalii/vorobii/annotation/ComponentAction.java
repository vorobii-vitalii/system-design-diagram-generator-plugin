package org.vitalii.vorobii.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ComponentAction {
    String requestDescription();
    String responseDescription() default "";
}
