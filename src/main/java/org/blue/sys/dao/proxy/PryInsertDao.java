package org.blue.sys.dao.proxy;

import org.blue.sys.dao.InsertDao;
import org.blue.sys.dao.impl.ImplInsertDao;
import org.blue.sys.vo.Author;
import org.blue.sys.vo.CheckEssay;
import org.blue.sys.vo.Editor;
import org.blue.sys.vo.Essay;
import org.blue.sys.vo.Expert;
import org.svip.pool.db.ConnMgr;

public class PryInsertDao {
    
    private InsertDao impl;
    
    public PryInsertDao() {
        impl = new ImplInsertDao();
    }

    public boolean doAuthor(Author author) throws Exception {
        return impl.doAuthor(ConnMgr.getConnection(), author);
    }

    public boolean doEditor(Editor editor) throws Exception {
        return impl.doEditor(ConnMgr.getConnection(), editor);
    }

    public boolean doExpert(Expert expert) throws Exception {
        return impl.doExpert(ConnMgr.getConnection(), expert);
    }

    public boolean doEssay(Essay essay) throws Exception {
        return impl.doEssay(ConnMgr.getConnection(), essay);
    }

    public boolean doCheckEssay(CheckEssay checkEssay) throws Exception {
        return impl.doCheckEssay(ConnMgr.getConnection(), checkEssay);
    }

}
