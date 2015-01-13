package org.blue.sys.dao.proxy;

import java.util.Map;

import org.blue.sys.dao.InfoUpdateDao;
import org.blue.sys.dao.impl.ImplInfoUpdateDao;
import org.svip.pool.db.ConnMgr;

public class PryInfoUpdateDao {
    
    private InfoUpdateDao impl;
    
    public PryInfoUpdateDao() {
        impl = new ImplInfoUpdateDao();
    }

    public boolean doAuthorUpdate(Map<String, String> value) throws Exception {
        return impl.doAuthorUpdate(ConnMgr.getConnection(), value);
    }

    public boolean doEditorUpdate(Map<String, String> value) throws Exception {
        return impl.doEditorUpdate(ConnMgr.getConnection(), value);
    }

    public boolean doExpertUpdate(Map<String, String> value) throws Exception {
        return impl.doExpertUpdate(ConnMgr.getConnection(), value);
    }

    public boolean doAdminUpdate(Map<String, String> value) throws Exception {
        return impl.doAdminUpdate(ConnMgr.getConnection(), value);
    }

    public boolean doPublishUpdate(Map<String, String> value) throws Exception {
        return impl.doPublishUpdate(ConnMgr.getConnection(), value);
    }

    public boolean doEssayUpload(Map<String, String> value) throws Exception {
        return impl.doEssayUpload(ConnMgr.getConnection(), value);
    }

}
