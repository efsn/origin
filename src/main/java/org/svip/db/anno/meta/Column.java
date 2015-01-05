package org.svip.db.anno.meta;

import org.svip.db.enumeration.mysql.DbType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column{
    public DbType type() default DbType.CHAR;

    public int length() default 0;

    public String comment() default "";

    public Constraint constraint() default @Constraint;

    public Foreign foreign() default @Foreign();
}
