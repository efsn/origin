package com.blue.sys.dao.proxy;

import java.sql.Connection;

import org.svip.pool.db.ConnPoolMgr;

import com.blue.sys.dao.DeleteDao;
import com.blue.sys.dao.impl.ImplDeleteDao;
import com.blue.sys.vo.Author;
import com.blue.sys.vo.Editor;
import com.blue.sys.vo.Expert;

public class PryDeleteDao {
    
    private DeleteDao impl;
    private Connection con;
    
    public PryDeleteDao() {
        impl = new ImplDeleteDao();
    }

    public boolean deleteAuthor(Author author) throws Exception {
        return impl.deleteAuthor(this.getConnection(), author);
    }

    public boolean deleteEditor(Editor editor) throws Exception {
        return impl.deleteEditor(this.getConnection(), editor);
    }

    public boolean deleteExpert(Expert expert) throws Exception {
        return impl.deleteExpert(this.getConnection(), expert);
    }

    public boolean deleteEssay(int id) throws Exception {
        return impl.deleteEssay(this.getConnection(), id);
    }

    private Connection getConnection() throws Exception{
        if(con == null || !con.isValid(0)){
            return ConnPoolMgr.getInstance().getPool().getConnection();
        }
        return con;
    }
    
}
