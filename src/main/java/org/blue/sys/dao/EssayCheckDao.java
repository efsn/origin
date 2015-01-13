package org.blue.sys.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public interface EssayCheckDao {
    boolean EditorCheckFirst(Connection conn, Map<String, String> value)
            throws SQLException;

    boolean checkMarkFromExpert(Connection conn, int essayId, String checkMark)
            throws SQLException;

    boolean checkMarkFromAdmin(Connection conn, int essayId, String useMark)
            throws SQLException;
}
