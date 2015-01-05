package com.blue.sys.dao.proxy;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.blue.sys.dbc.MySQLConnection;

import com.blue.sys.dao.impl.ImplQueryDao;

public class PryQueryDao {
    public PryQueryDao() {
        impl = new ImplQueryDao();
    }

    public Map<String, String> getAuthor() throws SQLException, IOException,
            ClassNotFoundException {
        return impl.getAuthor(MySQLConnection.getConnection());
    }

    public Map<String, String> getEditor() throws SQLException, IOException,
            ClassNotFoundException {
        return impl.getEditor(MySQLConnection.getConnection());
    }

    public Map<String, String> getExpert() throws SQLException, IOException,
            ClassNotFoundException {
        return impl.getExpert(MySQLConnection.getConnection());
    }

    public Map<String, String> getAdmin() throws SQLException, IOException,
            ClassNotFoundException {
        return impl.getAdmin(MySQLConnection.getConnection());
    }

    public Map<String, String> getEssay() throws SQLException, IOException,
            ClassNotFoundException {
        return impl.getEssay(MySQLConnection.getConnection());
    }

    public Map<String, List<String>> verifyAuthorInfo(List<String> list)
            throws SQLException, IOException, ClassNotFoundException {
        return impl.verifyAuthorInfo(list, MySQLConnection.getConnection());
    }

    public Map<String, String> getMessage(String pname, String user)
            throws SQLException, IOException, ClassNotFoundException {
        return impl.getMessage(MySQLConnection.getConnection(), pname, user);
    }

    public List<String> getPwd(String pname, String user) throws SQLException,
            IOException, ClassNotFoundException {
        return impl.getPwd(MySQLConnection.getConnection(), pname, user);
    }

    private ImplQueryDao impl;
}
