package org.svip.db.annotation.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Constraint{
    public boolean primary() default false;

    public String unique() default "";

    public boolean nullAble() default true;

    public boolean autoIncrement() default false;
}
