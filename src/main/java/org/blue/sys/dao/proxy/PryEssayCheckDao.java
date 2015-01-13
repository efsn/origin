package org.blue.sys.dao.proxy;

import java.util.Map;

import org.blue.sys.dao.EssayCheckDao;
import org.blue.sys.dao.impl.ImplEssayCheckDao;
import org.svip.pool.db.ConnMgr;

public class PryEssayCheckDao {
    
    private EssayCheckDao impl;
    
    public PryEssayCheckDao() {
        impl = new ImplEssayCheckDao();
    }

    public boolean EditorCheckFirst(Map<String, String> value) throws Exception {
        return impl.EditorCheckFirst(ConnMgr.getConnection(), value);
    }

    public boolean checkMarkFromExpert(int essayId, String checkMark) throws Exception {
        return impl.checkMarkFromExpert(ConnMgr.getConnection(), essayId, checkMark);
    }

    public boolean checkMarkFromAdmin(int essayId, String useMark) throws Exception {
        return impl.checkMarkFromAdmin(ConnMgr.getConnection(), essayId, useMark);
    }

}
