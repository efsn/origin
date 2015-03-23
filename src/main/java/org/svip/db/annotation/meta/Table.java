package org.svip.db.annotation.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table{
    public Index[] index() default {};

    public Foreign[] foreign() default {};

    public Trigger[] trigger() default {};

    public String engine() default "InnoDB";

    public String charset() default "UTF8";
}
