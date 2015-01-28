package org.blue.sys.vo;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.svip.db.anno.meta.Column;
import org.svip.db.anno.meta.Constraint;
import org.svip.db.anno.meta.Index;
import org.svip.db.anno.meta.Table;
import org.svip.db.enumeration.mysql.DbType;

@Table(index = {@Index(name = "adminIdIdx", column = {"adminId"})})
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
        this.adminId = adminId;
    }

    public String getadminPname() {
        return adminPname;
    }

    public void setAdmin_login(String adminPname) {
        this.adminPname = adminPname;
    }

    public String getAdmin_name() {
        return adminName;
    }

    public void setAdmin_name(String adminName) {
        this.adminName = adminName;
    }

    public String getAdmin_pwd() {
        return adminPwd;
    }

    public void setAdmin_pwd(String adminPwd) {
        this.adminPwd = adminPwd;
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
