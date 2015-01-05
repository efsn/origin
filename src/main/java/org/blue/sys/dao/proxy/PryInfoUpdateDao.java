package com.blue.sys.dao.proxy;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.blue.sys.dbc.MySQLConnection;

import com.blue.sys.dao.InfoUpdateDao;
import com.blue.sys.dao.impl.ImplInfoUpdateDao;

public class PryInfoUpdateDao {
    public PryInfoUpdateDao() {
        impl = new ImplInfoUpdateDao();
    }

    public boolean doAuthorUpdate(Map<String, String> value)
            throws SQLException, IOException, ClassNotFoundException {
        return impl.doAuthorUpdate(MySQLConnection.getConnection(), value);
    }

    public boolean doEditorUpdate(Map<String, String> value)
            throws SQLException, IOException, ClassNotFoundException {
        return impl.doEditorUpdate(MySQLConnection.getConnection(), value);
    }

    public boolean doExpertUpdate(Map<String, String> value)
            throws SQLException, IOException, ClassNotFoundException {
        return impl.doExpertUpdate(MySQLConnection.getConnection(), value);
    }

    public boolean doAdminUpdate(Map<String, String> value)
            throws SQLException, IOException, ClassNotFoundException {
        return impl.doAdminUpdate(MySQLConnection.getConnection(), value);
    }

    public boolean doPublishUpdate(Map<String, String> value)
            throws SQLException, IOException, ClassNotFoundException {
        return impl.doPublishUpdate(MySQLConnection.getConnection(), value);
    }

    public boolean doEssayUpload(Map<String, String> value)
            throws SQLException, IOException, ClassNotFoundException {
        return impl.doEssayUpload(MySQLConnection.getConnection(), value);
    }

    private InfoUpdateDao impl;
}
