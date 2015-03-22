package org.svip.db.definer.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import org.svip.db.annotation.util.Parser;
import org.svip.db.definer.DbDefiner;
import org.svip.pool.db.ConnMgr;
import org.svip.util.StrUtil;

import template.DbMgr;
import template.bean.User;

/**
 * @author Arthur
 * @version 1.0
 * Created on 2014/08/20
 */
public class MysqlDefiner implements DbDefiner{
    private static final int TIMEOUT = 0;
    
    private Connection con;
    
    public MysqlDefiner(){
        try {
            this.con = ConnMgr.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static DbDefiner getDefiner(){
        return Singleton.definer;
    }
    
    private static class Singleton{
        private static MysqlDefiner definer = new MysqlDefiner();
    }
    
    @Override
    public boolean isTableExsit(Connection con, Class<?> clazz) throws Exception{
        String beanName = StrUtil.substring(clazz.getName(), ".", true);
        if(!StrUtil.isNull(beanName)){
            return isTableExsit(con, StrUtil.getDbName(beanName));
        }
        return false;
    }

    @Override
    public boolean isTableExsit(Connection con, String tableName) throws Exception{
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
    public void createTable(Connection con, Class<?> clazz, boolean isTmp) throws Exception{
        if(con.isValid(TIMEOUT)){
            java.sql.Statement sm = con.createStatement();
            try{
                String sql = Parser.getInstance().parseBean(clazz);
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
                    this.close();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception{
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

    @Override
    public boolean isTableExsit(Class<?> clazz) throws Exception{
        return this.isTableExsit(con, clazz);
    }

    @Override
    public boolean isTableExsit(String tableName) throws Exception{
        return this.isTableExsit(con, tableName);
    }

    @Override
    public void createTable(Class<?> clazz, boolean isTmp) throws Exception{
        this.createTable(con, clazz, isTmp);
    }
    
    private void close() throws Exception{
        ConnMgr.close(con);
    }
}
