package com.blue.sys.dao.proxy;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.blue.sys.dbc.MySQLConnection;

import com.blue.sys.dao.TypeDao;
import com.blue.sys.dao.impl.ImplTypeDao;
import com.blue.sys.vo.EssayType;

public class PryTypeDao {
    public PryTypeDao() {
        impl = new ImplTypeDao();
    }

    public boolean typeInsert(EssayType essayType) throws SQLException,
            IOException, ClassNotFoundException {
        return impl.typeInsert(MySQLConnection.getConnection(), essayType);
    }

    public boolean typeDelete(EssayType essayType) throws SQLException,
            IOException, ClassNotFoundException {
        return impl.typeDelete(MySQLConnection.getConnection(), essayType);
    }

    public Map<Integer, String[]> getAllType() throws SQLException,
            IOException, ClassNotFoundException {
        return impl.getAllType(MySQLConnection.getConnection());
    }

    private TypeDao impl;
}
