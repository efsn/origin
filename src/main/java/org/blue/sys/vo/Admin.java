package com.blue.sys.vo;

import org.svip.db.anno.meta.*;
import org.svip.db.enumeration.mysql.DbType;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Table(index = {@Index(name = "adminIdIdx", column = "adminId")})
public class Admin {

    @Column(type = DbType.INT,
            length = 11,
            constraint = @Constraint(primary = true, autoIncrement = true))
    private int adminId;

    @Column(type = DbType.VARCHAR, length = 20)
    private String adminPname;

    @Column(type = DbType.VARCHAR, length = 20)
    private String adminName;

    @Column(type = DbType.VARCHAR, length = 20)
    private String adminPwd;

    @Column(type = DbType.TEXT)
    private String message;

    @Column(type = DbType.TEXT)
    private String messagee;

    public void setLogValue(PreparedStatement pstm) throws SQLException {
        pstm.setString(1, adminPname);
        pstm.setString(2, adminPwd);
    }

    public int getAdmin_id() {
        return adminId;
    }

    public void setAdmin_id(int adminId) {
        adminId = adminId;
    }

    public String getadminPname() {
        return adminPname;
    }

    public void setAdmin_login(String adminPname) {
        adminPname = adminPname;
    }

    public String getAdmin_name() {
        return adminName;
    }

    public void setAdmin_name(String adminName) {
        adminName = adminName;
    }

    public String getAdmin_pwd() {
        return adminPwd;
    }

    public void setAdmin_pwd(String adminPwd) {
        adminPwd = adminPwd;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessagee() {
        return messagee;
    }

    public void setMessagee(String messagee) {
        this.messagee = messagee;
    }

}
