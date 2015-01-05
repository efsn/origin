package com.blue.sys.vo;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Expert{
    public void setIdStatementValue(PreparedStatement pstm) throws SQLException {
        pstm.setInt(1, expert_id);
    }

    public void setStatementValue(PreparedStatement pstm) throws SQLException {
        pstm.setString(1, expert_pname);
        pstm.setString(2, expert_pwd);
        pstm.setString(3, expert_name);
        pstm.setString(4, expert_email);
        pstm.setString(5, expert_telephone);
        pstm.setString(6, expert_title);
        pstm.setString(7, expert_remark);
    }

    public int getExpert_id() {
        return expert_id;
    }

    public void setExpert_id(int expertId) {
        expert_id = expertId;
    }

    public String getExpert_pwd() {
        return expert_pwd;
    }

    public void setExpert_pwd(String expertPwd) {
        expert_pwd = expertPwd;
    }

    public String getExpert_pname() {
        return expert_pname;
    }

    public void setExpert_pname(String expertPname) {
        expert_pname = expertPname;
    }

    public String getExpert_name() {
        return expert_name;
    }

    public void setExpert_name(String expertName) {
        expert_name = expertName;
    }

    public String getExpert_title() {
        return expert_title;
    }

    public void setExpert_title(String expertTitle) {
        expert_title = expertTitle;
    }

    public String getExpert_remark() {
        return expert_remark;
    }

    public void setExpert_remark(String expertRemark) {
        expert_remark = expertRemark;
    }

    public String getExpert_telephone() {
        return expert_telephone;
    }

    public void setExpert_telephone(String expertTelephone) {
        expert_telephone = expertTelephone;
    }

    public String getExpert_email() {
        return expert_email;
    }

    public void setExpert_email(String expertEmail) {
        expert_email = expertEmail;
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

    private int expert_id;
    private String expert_pwd;
    private String expert_pname;
    private String expert_name;
    private String expert_title;
    private String expert_remark;
    private String expert_telephone;
    private String expert_email;
    private String message;
    private String messagee;
}
