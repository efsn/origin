package org.svip.db.annotation.meta;

import org.svip.db.enumeration.mysql.DbType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    DbType type() default DbType.CHAR;

    int length() default 0;

    String comment() default "";

    Constraint constraint() default @Constraint;

    Foreign foreign() default @Foreign();
}
