package org.svip.db.definer;

import java.sql.Connection;

/**
 * @author Chan
 * @version 1.0
 * Created on 2014/08/20
 */
public interface DbDefiner{

    /**
     * check whether the bean specified table exsit
     *
     * @param con database connection
     * @param clazz bean class
     * @return whether the bean class specified table exsit
     * @throws Exception
     */
    boolean isTableExsit(Connection con, Class<?> clazz) throws Exception;
    boolean isTableExsit(Class<?> clazz) throws Exception;
    
    /**
     * check whether the table exsit
     *
     * @param con database connection
     * @param tableName table name
     * @return whether the table name specified table exsit
     * @throws Exception
     */
    boolean isTableExsit(Connection con, String tableName) throws Exception;
    boolean isTableExsit(String tableName) throws Exception;
    
    /**
     *
     * @param con database connection
     * @param clazz bean class
     * @param isTmp is temporary table or not
     * @throws Exception
     */
    void createTable(Connection con, Class<?> clazz, boolean isTmp) throws Exception;
    void createTable(Class<?> clazz, boolean isTmp) throws Exception;
}
