package org.blue.sys.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.blue.sys.dao.InfoUpdateDao;

public class ImplInfoUpdateDao implements InfoUpdateDao {

    public boolean doAuthorUpdate(Connection conn, Map<String, String> value)
            throws SQLException {
        int n = 0;
        StringBuffer sql = new StringBuffer("UPDATE Author SET ");
        if(null != value.get("userName")){
            sql.append(" author_name = '" + value.get("userName") + "'");
            n++;
        }
        if(null != value.get("authorPW1")){
            if(1 == n){
                sql.append(", author_pwd = '" + value.get("authorPW1") + "'");
                n++;
            }
            else{
                sql.append(" author_pwd = '" + value.get("authorPW1") + "'");
                n++;
            }
        }
        if(null != value.get("address")){
            if(1 == n){
                sql.append(", author_address = '" + value.get("address") + "'");
                n++;
            }
            else{
                sql.append(" author_address = '" + value.get("address") + "'");
                n++;
            }
        }
        if(null != value.get("email")){
            if(n > 0){
                sql.append(", author_email = '" + value.get("email") + "'");
                n++;
            }
            else{
                sql.append(" author_email = '" + value.get("email") + "'");
                n++;
            }
        }
        if(null != value.get("phone")){
            if(n > 0){
                sql.append(", author_telephone = '" + value.get("phone") + "'");
                n++;
            }
            else{
                sql.append(" author_telephone = '" + value.get("phone") + "'");
                n++;
            }
        }
        if(null != value.get("mark")){
            if(n > 0){
                sql.append(", author_mark = '" + value.get("mark") + "'");
                n++;
            }
            else{
                sql.append(" author_mark = '" + value.get("mark") + "'");
                n++;
            }
        }
        if(null != value.get("message")){
            if(n > 0){
                sql.append(", message = '" + value.get("message") + "'");
                n++;
            }
            else{
                sql.append(" message = '" + value.get("message") + "'");
                n++;
            }
        }
        if(null != value.get("messagee")){
            if(n > 0){
                sql.append(", messagee = '" + value.get("messagee") + "'");
                n++;
            }
            else{
                sql.append(" messagee = '" + value.get("messagee") + "'");
                n++;
            }
        }
        sql.append("WHERE author_pname = '" + value.get("user") + "'");

        if(0 == n){
            return false;
        }

        PreparedStatement pstm = null;

        try{
            pstm = conn.prepareStatement(sql.toString());
            pstm.executeUpdate();
        }
        finally{
            pstm.close();
            conn.close();
        }

        return true;

    }

    public boolean doEditorUpdate(Connection conn, Map<String, String> value)
            throws SQLException {
        int n = 0;
        StringBuffer sql = new StringBuffer("UPDATE Editor SET ");
        if(null != value.get("userName")){
            sql.append(" editor_name = '" + value.get("userName") + "'");
            n++;
        }
        if(null != value.get("editorPW1")){
            if(1 == n){
                sql.append(", editor_pwd = '" + value.get("editorPW1") + "'");
                n++;
            }
            else{
                sql.append(" editor_pwd = '" + value.get("editorPW1") + "'");
                n++;
            }
        }
        if(null != value.get("email")){
            if(n > 0){
                sql.append(", editor_email = '" + value.get("email") + "'");
                n++;
            }
            else{
                sql.append(" editor_email = '" + value.get("email") + "'");
                n++;
            }
        }
        if(null != value.get("phone")){
            if(n > 0){
                sql.append(", editor_telephone = '" + value.get("phone") + "'");
                n++;
            }
            else{
                sql.append(" editor_telephone = '" + value.get("phone") + "'");
                n++;
            }
        }
        sql.append("WHERE editor_pname = '" + value.get("user") + "'");

        if(0 == n){
            return false;
        }

        PreparedStatement pstm = null;

        try{
            pstm = conn.prepareStatement(sql.toString());
            pstm.executeUpdate();
        }
        finally{
            pstm.close();
            conn.close();
        }

        return true;
    }

    public boolean doExpertUpdate(Connection conn, Map<String, String> value)
            throws SQLException {
        int n = 0;
        StringBuffer sql = new StringBuffer("UPDATE Expert SET ");
        if(null != value.get("userName")){
            sql.append(" expert_name = '" + value.get("userName") + "'");
            n++;
        }
        if(null != value.get("expertPW1")){
            if(1 == n){
                sql.append(", expert_pwd = '" + value.get("expertPW1") + "'");
                n++;
            }
            else{
                sql.append(" expert_pwd = '" + value.get("expertPW1") + "'");
                n++;
            }
        }
        if(null != value.get("email")){
            if(n > 0){
                sql.append(", expert_email = '" + value.get("email") + "'");
                n++;
            }
            else{
                sql.append(" expert_email = '" + value.get("email") + "'");
                n++;
            }
        }
        if(null != value.get("phone")){
            if(n > 0){
                sql.append(", expert_telephone = '" + value.get("phone") + "'");
                n++;
            }
            else{
                sql.append(" expert_telephone = '" + value.get("phone") + "'");
                n++;
            }
        }
        if(null != value.get("mark")){
            if(n > 0){
                sql.append(", expert_remark = '" + value.get("mark") + "'");
                n++;
            }
            else{
                sql.append(" expert_remark = '" + value.get("mark") + "'");
                n++;
            }
        }
        if(null != value.get("message")){
            if(n > 0){
                sql.append(", message = '" + value.get("message") + "'");
                n++;
            }
            else{
                sql.append(" message = '" + value.get("message") + "'");
                n++;
            }
        }
        if(null != value.get("messagee")){
            if(n > 0){
                sql.append(", messagee = '" + value.get("messagee") + "'");
                n++;
            }
            else{
                sql.append(" messagee = '" + value.get("messagee") + "'");
                n++;
            }
        }
        sql.append("WHERE expert_pname = '" + value.get("user") + "'");

        if(0 == n){
            return false;
        }

        PreparedStatement pstm = null;

        try{
            pstm = conn.prepareStatement(sql.toString());
            pstm.executeUpdate();
        }
        finally{
            pstm.close();
            conn.close();
        }

        return true;
    }

    public boolean doAdminUpdate(Connection conn, Map<String, String> value)
            throws SQLException {
        StringBuffer sql = new StringBuffer("UPDATE TB_Admin SET ");

        int n = 0;
        if(null != value.get("adminPW1")){
            sql.append(" admin_pwd = '" + value.get("adminPW1") + "'");
            n++;
        }

        if(null != value.get("message")){
            if(n > 0){
                sql.append(", message = '" + value.get("message") + "'");
                n++;
            }
            else{
                sql.append(" message = '" + value.get("message") + "'");
                n++;
            }
        }
        if(null != value.get("messagee")){
            if(n > 0){
                sql.append(", messagee = '" + value.get("messagee") + "'");
                n++;
            }
            else{
                sql.append(" messagee = '" + value.get("messagee") + "'");
                n++;
            }
        }
        sql.append("WHERE admin_pname = '" + value.get("user") + "'");

        PreparedStatement pstm = null;

        try{
            pstm = conn.prepareStatement(sql.toString());
            pstm.executeUpdate();
        }
        finally{
            pstm.close();
            conn.close();
        }

        return true;
    }

    public boolean doPublishUpdate(Connection conn, Map<String, String> value)
            throws SQLException {
        StringBuffer sql = new StringBuffer("UPDATE Essay SET ");

        sql.append(" publish_time = '" + value.get("publish_time") + "' ");
        sql.append(", publish_money = '" + value.get("publish_money") + "' ");
        sql.append(", ispay = '" + value.get("ispay") + "' ");

        sql.append(" WHERE essay_id = '" + value.get("essayId") + "'");

        PreparedStatement pstm = null;

        try{
            pstm = conn.prepareStatement(sql.toString());
            pstm.executeUpdate();
        }
        finally{
            pstm.close();
            conn.close();
        }

        return true;
    }

    public boolean doEssayUpload(Connection conn, Map<String, String> value)
            throws SQLException {
        String sql = "UPDATE TB_Essay SET essay_content1='" + value.get("file")
                + "' " + "WHERE essay_name=' " + value.get("essayName") + "' "
                + "AND author_pname='" + value.get("user") + "'";

        PreparedStatement pstm = null;

        try{
            pstm = conn.prepareStatement(sql);
            pstm.executeUpdate();
        }
        finally{
            pstm.close();
            conn.close();
        }

        return true;
    }

}
