package com.blue.sys.dao.proxy;

import com.blue.sys.dao.QueryAllDao;
import com.blue.sys.dao.impl.ImplQueryAllDao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PryQueryAllDao {
    Connection con;
    public PryQueryAllDao() {
        impl = new ImplQueryAllDao();
        try{
            con = con;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public List<String> getAuthor(String user) throws Exception,
            IOException, ClassNotFoundException {
        return impl.getAuthor(con, user);
    }

    public List<String> getEditor(String user) throws Exception,
            IOException, ClassNotFoundException {
        return impl.getEditor(con, user);
    }

    public List<String> getExpert(String user) throws Exception,
            IOException, ClassNotFoundException {
        return impl.getExpert(con, user);
    }

    public Map<Integer, List<String>> getAuthorAll() throws Exception,
            IOException, ClassNotFoundException {
        return impl.getAuthorAll(con);
    }

    public Map<Integer, List<String>> getEditorAll() throws Exception,
            IOException, ClassNotFoundException {
        return impl.getEditorAll(con);
    }

    public Map<Integer, List<String>> getExpertAll() throws Exception,
            IOException, ClassNotFoundException {
        return impl.getExpertAll(con);
    }

    public Map<Integer, List<String>> getEssayAll() throws Exception,
            IOException, ClassNotFoundException {
        return impl.getEssayAll(con);
    }

    public Map<Integer, List<String>> getEssayNotCheck(String essayName,
            String authorpName) throws SQLException, IOException,
            ClassNotFoundException {
        return impl.getEssayNotCheck(con,
                essayName, authorpName);
    }

    public Map<Integer, List<String>> getEssayCheckedByEditor(String user,
            String essayName) throws SQLException, IOException,
            ClassNotFoundException {
        return impl.getEssayCheckedByEditor(con,
                user, essayName);
    }

    public Map<Integer, List<String>> getEssayCheckedByExpert(String essayName,
            String expertpName) throws SQLException, IOException,
            ClassNotFoundException {
        return impl.getEssayCheckedByExpert(con,
                essayName, expertpName);
    }

    public Map<Integer, List<String>> getEssayType() throws SQLException,
            IOException, ClassNotFoundException {
        return impl.getEssayType(con);
    }

    public Map<Integer, List<String>> getEssayCheckedByAdmin()
            throws SQLException, IOException, ClassNotFoundException {
        return impl.getEssayCheckedByAdmin(con);
    }

    public Map<Integer, List<String>> getMessageToEditor(String user)
            throws SQLException, IOException, ClassNotFoundException {
        return impl.getMessageToEditor(con, user);
    }

    public Map<Integer, List<String>> getEssayToAuthor(String authorPname)
            throws SQLException, IOException, ClassNotFoundException {
        return impl.getEssayToAuthor(con,
                authorPname);
    }

    private QueryAllDao impl;
}
