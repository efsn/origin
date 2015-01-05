package org.svip.db.definer.impl;

import org.svip.db.anno.util.Parser;
import org.svip.db.definer.DbDefiner;
import org.svip.util.StrUtil;
import template.DbMgr;
import template.bean.User;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Chan
 * @version 1.0
 * Created on 2014/08/20
 */
public class MysqlDefiner implements DbDefiner{
    private static final int TIMEOUT = 0;

    @Override
    public boolean isTableExsit(Connection con, Class<?> clazz) throws SQLException{
        String beanName = StrUtil.substring(clazz.getName(), ".", true);
        if(!StrUtil.isNull(beanName)){
            return isTableExsit(con, StrUtil.getDbName(beanName));
        }
        return false;
    }

    @Override
    public boolean isTableExsit(Connection con, String tableName) throws SQLException{
        if(con.isValid(TIMEOUT)){
            DatabaseMetaData db = con.getMetaData();
            ResultSet rs = db.getTables(null, null, tableName, null);
            while(rs.next()){
                return true;
            }
        }
        return false;
    }

    @Override
    public void createTable(Connection con, Class<?> clazz, boolean isTmp) throws SQLException{
        if(con.isValid(TIMEOUT)){
            java.sql.Statement sm = con.createStatement();
            try{
                String sql = Parser.getInstance().parseBean(User.class);
                if(!StrUtil.isNull(sql)){
                    System.out.println(sql);
                    sm.executeUpdate(sql);
                }
            }finally{
                if(sm != null){
                    sm.close();
                }
                /*
                put connection back to pool
                 */
                if(con != null){
                    con.close();
                }
            }
        }
    }

    public static void main(String[] args) throws SQLException{
        MysqlDefiner definer = new MysqlDefiner();
        Connection con = DbMgr.getInstance().getConnection();
        try{
            if(!definer.isTableExsit(con, User.class)){
                definer.createTable(DbMgr.getInstance().getConnection(), User.class, false);
            }
        }finally{
            if(con != null){
                con.close();
            }
        }
    }
}
