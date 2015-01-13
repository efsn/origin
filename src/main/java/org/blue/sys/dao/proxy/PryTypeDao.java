package org.blue.sys.dao.proxy;

import java.util.Map;

import org.blue.sys.dao.TypeDao;
import org.blue.sys.dao.impl.ImplTypeDao;
import org.blue.sys.vo.EssayType;
import org.svip.pool.db.ConnMgr;

public class PryTypeDao {
    
    private TypeDao impl;
    
    public PryTypeDao() {
        impl = new ImplTypeDao();
    }

    public boolean typeInsert(EssayType essayType) throws Exception {
        return impl.typeInsert(ConnMgr.getConnection(), essayType);
    }

    public boolean typeDelete(EssayType essayType) throws Exception {
        return impl.typeDelete(ConnMgr.getConnection(), essayType);
    }

    public Map<Integer, String[]> getAllType() throws Exception {
        return impl.getAllType(ConnMgr.getConnection());
    }

}
