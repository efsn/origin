package org.svip.db.annotation.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Trigger{
    public String name();

    /**
     * Before or after
     */
    public boolean before() default true;

    public boolean insert() default false;

    public boolean update() default false;

    public boolean delete() default false;
}
