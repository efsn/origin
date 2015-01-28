package org.blue.sys.bean;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.blue.sys.vo.Admin;
import org.svip.db.definer.impl.MysqlDefiner;
import org.svip.pool.db.ConnPoolMgr;

public class AdminRegisterBean {
    public AdminRegisterBean(String pname, String pwd, String name) throws Exception {
        Connection conn = ConnPoolMgr.getInstance().getPool().getConnection();
        
        if(!MysqlDefiner.getDefiner().isTableExsit(conn, Admin.class)){
            MysqlDefiner.getDefiner().createTable(conn, Admin.class, false);
        }
        
        String sql = "INSERT INTO TB_Admin(" + "admin_pname," + "admin_pwd,"
                + "admin_name)" + "VALUES(" + "?, ?, ?)";

        PreparedStatement pstm = null;

        pstm = conn.prepareStatement(sql);

        pstm.setString(1, pname);
        pstm.setString(2, pwd);
        pstm.setString(3, name);

        pstm.executeUpdate();

    }

    public static void main(String[] args) {
        try{
            new AdminRegisterBean("roota", "roota", "binge");
        } catch(Exception e){
            e.printStackTrace();
        } 
    }
}
