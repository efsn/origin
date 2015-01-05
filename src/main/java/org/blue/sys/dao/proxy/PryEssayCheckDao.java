package com.blue.sys.dao.proxy;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import com.blue.sys.dao.EssayCheckDao;
import com.blue.sys.dao.impl.ImplEssayCheckDao;

public class PryEssayCheckDao {
    public PryEssayCheckDao() {
        impl = new ImplEssayCheckDao();
    }

    public boolean EditorCheckFirst(Map<String, String> value)
            throws SQLException, IOException, ClassNotFoundException {
        return impl.EditorCheckFirst(MySQLConnection.getConnection(), value);
    }

    public boolean checkMarkFromExpert(int essayId, String checkMark)
            throws SQLException, IOException, ClassNotFoundException {
        return impl.checkMarkFromExpert(MySQLConnection.getConnection(),
                essayId, checkMark);
    }

    public boolean checkMarkFromAdmin(int essayId, String useMark)
            throws SQLException, IOException, ClassNotFoundException {
        return impl.checkMarkFromAdmin(MySQLConnection.getConnection(),
                essayId, useMark);
    }

    private EssayCheckDao impl;
}
