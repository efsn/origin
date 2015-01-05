package org.blue.sys.bean;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.svip.pool.db.ConnPoolMgr;

public class AdminRegisterBean {
    public AdminRegisterBean(String pname, String pwd, String name) throws Exception {
        String sql = "INSERT INTO TB_Admin(" + "admin_pname," + "admin_pwd,"
                + "admin_name)" + "VALUES(" + "?, ?, ?)";

        PreparedStatement pstm = null;
        Connection conn = ConnPoolMgr.getInstance().getPool().getConnection();

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
