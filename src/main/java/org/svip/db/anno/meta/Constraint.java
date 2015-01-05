package org.svip.db.anno.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Constraint{
    public boolean primary() default false;

    public String unique() default "";

    public boolean nullAble() default true;

    public boolean autoIncrement() default false;
}
