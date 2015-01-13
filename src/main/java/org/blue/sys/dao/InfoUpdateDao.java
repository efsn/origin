package org.blue.sys.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public interface InfoUpdateDao {
    // author info update
    boolean doAuthorUpdate(Connection conn, Map<String, String> value)
            throws SQLException;

    // editor info update
    boolean doEditorUpdate(Connection conn, Map<String, String> value)
            throws SQLException;

    // expert info update
    boolean doExpertUpdate(Connection conn, Map<String, String> value)
            throws SQLException;

    // admin info update
    boolean doAdminUpdate(Connection conn, Map<String, String> value)
            throws SQLException;

    // publish info of essay update
    boolean doPublishUpdate(Connection conn, Map<String, String> value)
            throws SQLException;

    boolean doEssayUpload(Connection conn, Map<String, String> value)
            throws SQLException;

}
