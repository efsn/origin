package org.svip.db.annotation.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Constraint {
    boolean primary() default false;

    String unique() default "";

    boolean nullAble() default true;

    boolean autoIncrement() default false;
}
