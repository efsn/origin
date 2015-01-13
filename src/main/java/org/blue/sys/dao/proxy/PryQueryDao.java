package org.blue.sys.dao.proxy;

import java.util.List;
import java.util.Map;

import org.blue.sys.dao.impl.ImplQueryDao;
import org.svip.pool.db.ConnMgr;

public class PryQueryDao {
    
    private ImplQueryDao impl;
    
    public PryQueryDao() {
        impl = new ImplQueryDao();
    }

    public Map<String, String> getAuthor() throws Exception {
        return impl.getAuthor(ConnMgr.getConnection());
    }

    public Map<String, String> getEditor() throws Exception {
        return impl.getEditor(ConnMgr.getConnection());
    }

    public Map<String, String> getExpert() throws Exception {
        return impl.getExpert(ConnMgr.getConnection());
    }

    public Map<String, String> getAdmin() throws Exception {
        return impl.getAdmin(ConnMgr.getConnection());
    }

    public Map<String, String> getEssay() throws Exception {
        return impl.getEssay(ConnMgr.getConnection());
    }

    public Map<String, List<String>> verifyAuthorInfo(List<String> list) throws Exception {
        return impl.verifyAuthorInfo(list, ConnMgr.getConnection());
    }

    public Map<String, String> getMessage(String pname, String user) throws Exception {
        return impl.getMessage(ConnMgr.getConnection(), pname, user);
    }

    public List<String> getPwd(String pname, String user) throws Exception {
        return impl.getPwd(ConnMgr.getConnection(), pname, user);
    }

}
