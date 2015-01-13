package org.blue.sys.dao.proxy;

import org.blue.sys.dao.DeleteDao;
import org.blue.sys.dao.impl.ImplDeleteDao;
import org.blue.sys.vo.Author;
import org.blue.sys.vo.Editor;
import org.blue.sys.vo.Expert;
import org.svip.pool.db.ConnMgr;

public class PryDeleteDao {
    
    private DeleteDao impl;
    
    public PryDeleteDao() {
        impl = new ImplDeleteDao();
    }

    public boolean deleteAuthor(Author author) throws Exception {
        return impl.deleteAuthor(ConnMgr.getConnection(), author);
    }

    public boolean deleteEditor(Editor editor) throws Exception {
        return impl.deleteEditor(ConnMgr.getConnection(), editor);
    }

    public boolean deleteExpert(Expert expert) throws Exception {
        return impl.deleteExpert(ConnMgr.getConnection(), expert);
    }

    public boolean deleteEssay(int id) throws Exception {
        return impl.deleteEssay(ConnMgr.getConnection(), id);
    }

}
