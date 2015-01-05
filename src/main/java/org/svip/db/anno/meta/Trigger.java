package org.svip.db.anno.meta;

public @interface Trigger{
    public String name();

    /**
     * Before or after
     */
    public boolean before() default true;

    public boolean insert() default false;

    public boolean update() default false;

    public boolean delete() default false;
}
