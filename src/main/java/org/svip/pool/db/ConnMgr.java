package org.svip.pool.db;

import java.sql.Connection;

public class ConnMgr{
    
    public static Connection getConnection() throws Exception{
        return ConnPoolMgr.getInstance().getPool().getConnection();
    }
    
}
