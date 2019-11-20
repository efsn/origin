package org.svip.db.annotation.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Trigger {
    String name();

    /**
     * Before or after
     */
    boolean before() default true;

    boolean insert() default false;

    boolean update() default false;

    boolean delete() default false;
}
