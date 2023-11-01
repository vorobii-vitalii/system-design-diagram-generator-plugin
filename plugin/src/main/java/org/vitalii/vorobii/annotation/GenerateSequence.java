package org.vitalii.vorobii.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GenerateSequence {
    String sequenceName();

    String sequenceDescription() default "";
}
