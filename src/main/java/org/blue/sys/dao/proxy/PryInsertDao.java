package com.blue.sys.dao.proxy;

import java.io.IOException;
import java.sql.SQLException;

import org.blue.sys.dbc.MySQLConnection;

import com.blue.sys.dao.InsertDao;
import com.blue.sys.dao.impl.ImplInsertDao;
import com.blue.sys.vo.*;

public class PryInsertDao {
    public PryInsertDao() {
        impl = new ImplInsertDao();
    }

    // ����DBʵ��Authorע�����Ĳ���
    public boolean doAuthor(Author author) throws SQLException, IOException,
            ClassNotFoundException {
        return impl.doAuthor(MySQLConnection.getConnection(), author);
    }

    // ����DBʵ��Editorע�����Ĳ���
    public boolean doEditor(Editor editor) throws SQLException, IOException,
            ClassNotFoundException {
        return impl.doEditor(MySQLConnection.getConnection(), editor);
    }

    // ����DBʵ��Expertע�����Ĳ���
    public boolean doExpert(Expert expert) throws SQLException, IOException,
            ClassNotFoundException {
        return impl.doExpert(MySQLConnection.getConnection(), expert);
    }

    // ����DBʵ��Essayע�����Ĳ���
    public boolean doEssay(Essay essay) throws SQLException, IOException,
            ClassNotFoundException {
        return impl.doEssay(MySQLConnection.getConnection(), essay);
    }

    public boolean doCheckEssay(CheckEssay checkEssay) throws SQLException,
            IOException, ClassNotFoundException {
        return impl.doCheckEssay(MySQLConnection.getConnection(), checkEssay);
    }

    private InsertDao impl;

}
