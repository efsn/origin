package org.blue.sys.vo;

import org.svip.db.anno.meta.Column;
import org.svip.db.anno.meta.Constraint;
import org.svip.db.anno.meta.Index;
import org.svip.db.anno.meta.Table;
import org.svip.db.enumeration.mysql.DbType;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Table(index = {@Index(name = "author_id_idx", column = {"authorId"})})
public class Author{

    @Column(type = DbType.INT,
            length = 11,
            constraint = @Constraint(primary = true, autoIncrement = true))
    private int authorId;

    @Column(type = DbType.VARCHAR, length = 20)
    private String authorPname;

    @Column(type = DbType.VARCHAR, length = 20)
    private String authorName;

    @Column(type = DbType.VARCHAR, length = 20)
    private String authorPwd;

    @Column(type = DbType.TEXT)
    private String authorAddress;

    @Column(type = DbType.VARCHAR, length = 50)
    private String authorEmail;

    @Column(type = DbType.CHAR, length = 11)
    private String authorTelephone;

    @Column(type = DbType.TEXT)
    private String authorMark;

    @Column(type = DbType.TEXT)
    private String message;

    @Column(type = DbType.TEXT)
    private String messagee;

    public void setStatementValue(PreparedStatement pstm) throws SQLException {
        pstm.setString(1, authorPname);
        pstm.setString(2, authorPwd);
        pstm.setString(3, authorName);
        pstm.setString(4, authorEmail);
        pstm.setString(5, authorTelephone);
        pstm.setString(6, authorAddress);
        pstm.setString(7, authorMark);
    }

    public void setIdStatementValue(PreparedStatement pstm) throws SQLException {
        pstm.setInt(1, authorId);
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getAuthorPname() {
        return authorPname;
    }

    public void setAuthorPname(String authorPname) {
        this.authorPname = authorPname;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorPwd() {
        return authorPwd;
    }

    public void setAuthorPwd(String authorPwd) {
        this.authorPwd = authorPwd;
    }

    public String getAuthorAddress() {
        return authorAddress;
    }

    public void setAuthorAddress(String authorAddress) {
        this.authorAddress = authorAddress;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getAuthorTelephone() {
        return authorTelephone;
    }

    public void setAuthorTelephone(String authorTelephone) {
        this.authorTelephone = authorTelephone;
    }

    public String getAuthorMark() {
        return authorMark;
    }

    public void setAuthorMark(String authorMark) {
        this.authorMark = authorMark;
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
