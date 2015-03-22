package org.svip.db.annotation.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.svip.db.enumeration.mysql.DbType;

@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Index{
    public String name();

    public String[] column();

    public DbType mode() default DbType.HASH;

    public DbType type() default DbType.IDX_NORMAL;
}
