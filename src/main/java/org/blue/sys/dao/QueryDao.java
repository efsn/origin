package com.blue.sys.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface QueryDao {
    // Verify author info while login
    Map<String, String> getAuthor(Connection conn) throws SQLException;

    // Verify editor info while login
    Map<String, String> getEditor(Connection conn) throws SQLException;

    // Verify expert info while login
    Map<String, String> getExpert(Connection conn) throws SQLException;

    // Verify admin info while login
    Map<String, String> getAdmin(Connection conn) throws SQLException;

    // Verify essay info while register
    Map<String, String> getEssay(Connection conn) throws SQLException;

    // Verify author info while insert essay info
    Map<String, List<String>> verifyAuthorInfo(List<String> list,
                                               Connection conn) throws SQLException;

    Map<String, String> getMessage(Connection conn, String pname, String user)
            throws SQLException;

    List<String> getPwd(Connection conn, String pname, String user)
            throws SQLException;

}
