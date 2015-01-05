package org.svip.db.anno.meta;

public @interface Foreign{
    public String name() default "";

    public String column() default "";

    public String refTable() default "";

    public String refColumn() default "";

    public String database() default "";
}
