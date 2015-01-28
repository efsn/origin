package org.blue.sys.vo;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.svip.db.anno.meta.Column;
import org.svip.db.anno.meta.Constraint;
import org.svip.db.anno.meta.Index;
import org.svip.db.anno.meta.Table;
import org.svip.db.enumeration.mysql.DbType;

@Table(index = {@Index(name = "expertIdIdx", column = {"expertId"})})
public class Expert{
    
    @Column(type = DbType.INT,
            length = 11,
            constraint = @Constraint(primary = true, autoIncrement = true))
    private int expertId;
    
    @Column(type = DbType.VARCHAR, length=20)
    private String expertPwd;
    
    @Column(type = DbType.VARCHAR, length=30)
    private String expertPname;
    
    @Column(type = DbType.VARCHAR, length=100)
    private String expertName;
    
    @Column(type = DbType.VARCHAR, length=300)
    private String expertTitle;
    
    @Column(type = DbType.TEXT)
    private String expertRemark;
    
    @Column(type = DbType.VARCHAR, length=11)
    private String expertTelephone;
    
    @Column(type = DbType.VARCHAR, length=100)
    private String expertEmail;
    
    @Column(type = DbType.TEXT)
    private String message;
    
    @Column(type = DbType.TEXT)
    private String messagee;
    
    public void setIdStatementValue(PreparedStatement pstm) throws SQLException {
        pstm.setInt(1, expertId);
    }

    public void setStatementValue(PreparedStatement pstm) throws SQLException {
        pstm.setString(1, expertPname);
        pstm.setString(2, expertPwd);
        pstm.setString(3, expertName);
        pstm.setString(4, expertEmail);
        pstm.setString(5, expertTelephone);
        pstm.setString(6, expertTitle);
        pstm.setString(7, expertRemark);
    }

    public int getExpertId() {
        return expertId;
    }

    public void setExpertId(int expertId) {
        this.expertId = expertId;
    }

    public String getExpertPwd() {
        return expertPwd;
    }

    public void setExpertPwd(String expertPwd) {
        this.expertPwd = expertPwd;
    }

    public String getExpertPname() {
        return expertPname;
    }

    public void setExpertPname(String expertPname) {
        this.expertPname = expertPname;
    }

    public String getExpertName() {
        return expertName;
    }

    public void setExpertName(String expertName) {
        this.expertName = expertName;
    }

    public String getExpertTitle() {
        return expertTitle;
    }

    public void setExpertTitle(String expertTitle) {
        this.expertTitle = expertTitle;
    }

    public String getExpertRemark() {
        return expertRemark;
    }

    public void setExpertRemark(String expertRemark) {
        this.expertRemark = expertRemark;
    }

    public String getExpertTelephone() {
        return expertTelephone;
    }

    public void setExpertTelephone(String expertTelephone) {
        this.expertTelephone = expertTelephone;
    }

    public String getExpertEmail() {
        return expertEmail;
    }

    public void setExpertEmail(String expertEmail) {
        this.expertEmail = expertEmail;
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
