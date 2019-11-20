package org.svip.db.annotation.meta;

import org.svip.db.enumeration.mysql.DbType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Index {
    String name();

    String[] column();

    DbType mode() default DbType.HASH;

    DbType type() default DbType.IDX_NORMAL;
}
