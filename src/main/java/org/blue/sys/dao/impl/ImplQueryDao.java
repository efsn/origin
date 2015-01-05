package com.blue.sys.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blue.sys.dao.QueryDao;

public class ImplQueryDao implements QueryDao {

    public Map<String, String> getAuthor(Connection conn) throws SQLException {
        String sql = "SELECT author_pname, author_pwd FROM TB_Author";

        Statement sm = null;
        ResultSet rs = null;
        Map<String, String> map = new HashMap<String, String>();

        try{
            sm = conn.createStatement();
            rs = sm.executeQuery(sql);

            while(rs.next()){
                map.put(rs.getString(1), rs.getString(2));
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public Map<String, String> getEditor(Connection conn) throws SQLException {
        String sql = "SELECT editor_pname, editor_pwd FROM TB_Editor";

        Statement sm = null;
        ResultSet rs = null;
        Map<String, String> map = new HashMap<String, String>();

        try{
            sm = conn.createStatement();
            rs = sm.executeQuery(sql);

            while(rs.next()){
                map.put(rs.getString(1), rs.getString(2));
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public Map<String, String> getExpert(Connection conn) throws SQLException {
        String sql = "SELECT expert_pname, expert_pwd FROM TB_Expert";

        Statement sm = null;
        ResultSet rs = null;
        Map<String, String> map = new HashMap<String, String>();

        try{
            sm = conn.createStatement();
            rs = sm.executeQuery(sql);

            while(rs.next()){
                map.put(rs.getString(1), rs.getString(2));
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public Map<String, String> getAdmin(Connection conn) throws SQLException {
        String sql = "SELECT admin_pname, admin_pwd FROM TB_Admin";

        Statement sm = null;
        ResultSet rs = null;
        Map<String, String> map = new HashMap<String, String>();

        try{
            sm = conn.createStatement();
            rs = sm.executeQuery(sql);

            while(rs.next()){
                map.put(rs.getString(1), rs.getString(2));
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public Map<String, String> getEssay(Connection conn) throws SQLException {
        String sql = "SELECT essay_name, author_pname FROM TB_Essay";

        Statement sm = null;
        ResultSet rs = null;
        Map<String, String> map = new HashMap<String, String>();

        try{
            sm = conn.createStatement();
            rs = sm.executeQuery(sql);

            while(rs.next()){
                map.put(rs.getString(1), rs.getString(2));
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public Map<String, List<String>> verifyAuthorInfo(List<String> list,
            Connection conn) throws SQLException {
        int n = 0;
        StringBuffer buffer = new StringBuffer("SELECT ");

        if(null != list.get(0) && !"".equals(list.get(0))){
            buffer.append("author_pname ");
            n++;
        }
        if(null != list.get(1) && !"".equals(list.get(1))){
            buffer.append(", author_name ");
            n++;
        }
        if(null != list.get(2) && !"".equals(list.get(2))){
            buffer.append(", author_address ");
            n++;
        }
        if(null != list.get(3) && !"".equals(list.get(3))){
            buffer.append(", author_telephone ");
            n++;
        }
        if(null != list.get(4) && !"".equals(list.get(4))){
            buffer.append(", author_email ");
            n++;
        }

        buffer.append(" FROM Author");
        String sql = buffer.toString();

        Statement sm = null;
        ResultSet rs = null;
        List<String> value = null;
        Map<String, List<String>> map = new HashMap<String, List<String>>();

        try{
            sm = conn.createStatement();
            rs = sm.executeQuery(sql);

            while(rs.next()){
                value = new ArrayList<String>();
                for(int i = 2; i <= n; i++){
                    value.add(rs.getString(i));
                }
                map.put(rs.getString(1), value);
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public Map<String, String> getMessage(Connection conn,
            String pname,
            String user) throws SQLException {
        String sql = null;
        if("author".equals(user)){
            sql = "SELECT message FROM TB_Author " + "WHERE author_pname='"
                    + pname + "'";
        }
        if("expert".equals(user)){
            sql = "SELECT message FROM TB_Expert " + "WHERE expert_pname='"
                    + pname + "'";
        }
        if("admin".equals(user)){
            sql = "SELECT message FROM TB_Admin " + "WHERE admin_pname='"
                    + pname + "'";
        }

        Statement sm = null;
        ResultSet rs = null;
        Map<String, String> map = new HashMap<String, String>();

        try{
            sm = conn.createStatement();
            rs = sm.executeQuery(sql);

            while(rs.next()){
                map.put("message", rs.getString(1));
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public List<String> getPwd(Connection conn, String pname, String user)
            throws SQLException {
        String sql = null;
        if("author".equals(user)){
            sql = "SELECT author_pwd FROM TB_Author " + "WHERE author_pname='"
                    + pname + "'";
        }

        if("editor".equals(user)){
            sql = "SELECT editor_pwd FROM TB_Editor " + "WHERE editor_pname='"
                    + pname + "'";
        }

        if("expert".equals(user)){
            sql = "SELECT expert_pwd FROM TB_Expert " + "WHERE expert_pname='"
                    + pname + "'";
        }

        if("admin".equals(user)){
            sql = "SELECT admin_pwd FROM TB_Admin " + "WHERE admin_pname='"
                    + pname + "'";
        }

        Statement sm = null;
        ResultSet rs = null;

        List<String> list = new ArrayList<String>();
        list.add(pname);

        try{
            sm = conn.createStatement();
            rs = sm.executeQuery(sql);

            while(rs.next()){
                list.add(rs.getString(1));
            }
            return list;
        }
        finally{
            sm.close();
            conn.close();
        }
    }
}
