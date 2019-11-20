package org.svip.db.annotation.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    Index[] index() default {};

    Foreign[] foreign() default {};

    Trigger[] trigger() default {};

    String engine() default "InnoDB";

    String charset() default "UTF8";
}
