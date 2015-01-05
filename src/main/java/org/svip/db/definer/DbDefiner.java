package org.svip.db.definer;

import java.sql.Connection;
import java.sql.SQLException;

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
     * @throws SQLException
     */
    boolean isTableExsit(Connection con, Class<?> clazz) throws SQLException;

    /**
     * check whether the table exsit
     *
     * @param con database connection
     * @param tableName table name
     * @return whether the table name specified table exsit
     * @throws SQLException
     */
    boolean isTableExsit(Connection con, String tableName) throws SQLException;

    /**
     *
     * @param con database connection
     * @param clazz bean class
     * @param isTmp is temporary table or not
     * @throws SQLException
     */
    void createTable(Connection con, Class<?> clazz, boolean isTmp) throws SQLException;
}
