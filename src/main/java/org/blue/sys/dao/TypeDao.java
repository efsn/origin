package org.blue.sys.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.blue.sys.vo.EssayType;

public interface TypeDao {
    // insert type
    boolean typeInsert(Connection conn, EssayType essayType)
            throws SQLException;

    // delete type
    boolean typeDelete(Connection conn, EssayType essayType)
            throws SQLException;

    // query type
    Map<Integer, String[]> getAllType(Connection conn) throws SQLException;

}
