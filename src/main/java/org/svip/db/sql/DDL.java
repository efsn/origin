package org.svip.db.sql;

import java.sql.SQLException;

/**
 * MySqlDDL, Data Definition Language
 * Create, Drop, Alter
 *
 * @author Chan
 * @version 1.0
 * Created on 2014/08/20
 */
public interface DDL extends SQL {

    /**
     * Create table according to bean clazz
     *
     * @param clazz bean Class object
     * @throws SQLException
     */
    String getCreateSql(Class<?> clazz) throws SQLException;

}
