package org.blue.sys.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface QueryAllDao {
    List<String> getAuthor(Connection conn, String user) throws SQLException;

    List<String> getEditor(Connection conn, String user) throws SQLException;

    List<String> getExpert(Connection conn, String user) throws SQLException;

    Map<Integer, List<String>> getAuthorAll(Connection conn)
            throws SQLException;

    Map<Integer, List<String>> getEditorAll(Connection conn)
            throws SQLException;

    Map<Integer, List<String>> getExpertAll(Connection conn)
            throws SQLException;

    Map<Integer, List<String>> getEssayAll(Connection conn) throws SQLException;

    Map<Integer, List<String>> getEssayNotCheck(Connection conn,
                                                String essayName,
                                                String authorpName) throws SQLException;

    Map<Integer, List<String>> getEssayCheckedByEditor(Connection conn,
                                                       String user,
                                                       String essayName) throws SQLException;

    Map<Integer, List<String>> getEssayCheckedByExpert(Connection conn,
                                                       String essayName,
                                                       String expertpName) throws SQLException;

    Map<Integer, List<String>> getEssayType(Connection conn)
            throws SQLException;

    Map<Integer, List<String>> getEssayCheckedByAdmin(Connection conn)
            throws SQLException;

    Map<Integer, List<String>> getMessageToEditor(Connection conn, String user)
            throws SQLException;

    Map<Integer, List<String>> getEssayToAuthor(Connection conn,
                                                String authorPname) throws SQLException;

}
