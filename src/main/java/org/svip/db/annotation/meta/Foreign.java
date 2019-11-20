package org.svip.db.annotation.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Foreign {
    String name() default "";

    String column() default "";

    String refTable() default "";

    String refColumn() default "";

    String database() default "";
}
