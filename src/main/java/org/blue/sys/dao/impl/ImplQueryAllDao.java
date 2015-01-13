package org.blue.sys.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.blue.sys.dao.QueryAllDao;

public class ImplQueryAllDao implements QueryAllDao {

    public List<String> getAuthor(Connection conn, String user)
            throws SQLException {
        String sql = "SELECT " + "author_name, " + "author_address, "
                + "author_email, " + "author_telephone, " + "author_mark "
                + "FROM TB_Author " + "WHERE author_pname= '" + user + "'";

        Statement sm = null;
        ResultSet rs = null;
        List<String> list = new ArrayList<String>();

        try{
            sm = conn.createStatement();

            rs = sm.executeQuery(sql);

            while(rs.next()){
                for(int i = 1; i <= 5; i++){
                    list.add(rs.getString(i));
                }
            }
            return list;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public Map<Integer, List<String>> getAuthorAll(Connection conn)
            throws SQLException {
        String sql = "SELECT * FROM TB_Author";

        Statement sm = null;
        ResultSet rs = null;

        Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();

        try{
            sm = conn.createStatement();

            rs = sm.executeQuery(sql);

            while(rs.next()){
                List<String> list = new ArrayList<String>();
                for(int i = 2; i <= 8; i++){
                    list.add(rs.getString(i));
                }
                map.put(rs.getInt(1), list);
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }

    }

    public List<String> getEditor(Connection conn, String user)
            throws SQLException {
        String sql = "SELECT " + "editor_name, " + "editor_telephone, "
                + "editor_email " + "FROM TB_Editor " + "WHERE editor_pname= '"
                + user + "'";

        Statement sm = null;
        ResultSet rs = null;
        List<String> list = new ArrayList<String>();

        try{
            sm = conn.createStatement();
            rs = sm.executeQuery(sql);

            while(rs.next()){
                for(int i = 1; i <= 3; i++){
                    list.add(rs.getString(i));
                }
            }
            return list;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public Map<Integer, List<String>> getEditorAll(Connection conn)
            throws SQLException {
        String sql = "SELECT * FROM TB_Editor";

        Statement sm = null;
        ResultSet rs = null;

        Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();

        try{
            sm = conn.createStatement();

            rs = sm.executeQuery(sql);

            while(rs.next()){
                List<String> list = new ArrayList<String>();
                for(int i = 2; i <= 6; i++){
                    list.add(rs.getString(i));
                }
                map.put(rs.getInt(1), list);
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }

    }

    public List<String> getExpert(Connection conn, String user)
            throws SQLException {
        String sql = "SELECT " + "expert_name, " + "expert_title, "
                + "expert_email, " + "expert_telephone, " + "expert_remark "
                + "FROM TB_Expert " + "WHERE expert_pname= '" + user + "'";

        Statement sm = null;
        ResultSet rs = null;
        List<String> list = new ArrayList<String>();

        try{
            sm = conn.createStatement();

            rs = sm.executeQuery(sql);

            while(rs.next()){
                for(int i = 1; i <= 5; i++){
                    list.add(rs.getString(i));
                }
            }
            return list;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public Map<Integer, List<String>> getExpertAll(Connection conn)
            throws SQLException {
        String sql = "SELECT * FROM TB_Expert";

        Statement sm = null;
        ResultSet rs = null;

        Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();

        try{
            sm = conn.createStatement();

            rs = sm.executeQuery(sql);

            while(rs.next()){
                List<String> list = new ArrayList<String>();
                for(int i = 2; i <= 9; i++){
                    list.add(rs.getString(i));
                }
                map.put(rs.getInt(1), list);
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public Map<Integer, List<String>> getEssayAll(Connection conn)
            throws SQLException {
        String sql = "SELECT * FROM TB_Essay";

        Statement sm = null;
        ResultSet rs = null;

        Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();

        try{
            sm = conn.createStatement();

            rs = sm.executeQuery(sql);

            while(rs.next()){
                List<String> list = new ArrayList<String>();
                for(int i = 2; i <= 13; i++){
                    list.add(rs.getString(i));
                }
                map.put(rs.getInt(1), list);
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public Map<Integer, List<String>> getEssayNotCheck(Connection conn,
            String essayName,
            String authorpName) throws SQLException {
        StringBuffer sql = new StringBuffer(
                "SELECT * FROM Essay WHERE checkMark = 'not checked' ");
        if(null != essayName && !"".equals(essayName)){
            sql.append(" AND essay_name='");
            sql.append(essayName);
            sql.append("' ");
        }
        if(null != authorpName && !"".equals(authorpName)){
            sql.append(" AND author_pname='");
            sql.append(authorpName);
            sql.append("' ");
        }

        Statement sm = null;
        ResultSet rs = null;

        Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();

        try{
            sm = conn.createStatement();
            rs = sm.executeQuery(sql.toString());

            while(rs.next()){
                List<String> list = new ArrayList<String>();
                for(int i = 2; i <= 13; i++){
                    list.add(rs.getString(i));
                }
                map.put(rs.getInt(1), list);
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public Map<Integer, List<String>> getEssayCheckedByEditor(Connection conn,
            String user,
            String essayName) throws SQLException {
        String sql = "SELECT TB_Essay.essay_id, " + "TB_Essay.essay_name, "
                + "TB_EssayType.type_name, " + "TB_Essay.checkMark, "
                + "TB_Essay.author_pname "
                + "FROM TB_Essay, TB_EssayType, TB_Expert "
                + "WHERE TB_Essay.type_id = TB_Expert.type_id "
                + "and TB_Expert.type_id = TB_EssayType.type_id "
                + "and TB_Expert.expert_pname= '" + user + "' ";
        if(null != essayName && !"".equals(essayName)){
            sql += " and Essay.essay_name='" + essayName + "' ";
        }

        sql += " and TB_Essay.checkMark LIKE '%editorCheckPass%' ";

        Statement sm = null;
        ResultSet rs = null;

        Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();

        try{
            sm = conn.createStatement();
            rs = sm.executeQuery(sql);

            while(rs.next()){
                List<String> list = new ArrayList<String>();
                for(int i = 2; i <= 5; i++){
                    list.add(rs.getString(i));
                }
                map.put(rs.getInt(1), list);
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public Map<Integer, List<String>> getEssayCheckedByExpert(Connection conn,
            String essayName,
            String expertpName) throws SQLException {
        String sql = "SELECT "
                + "TB_Essay.essay_id, "
                + "TB_CheckEssay.expert_name, "
                + "TB_CheckEssay.check_date, "
                + "TB_CheckEssay.check_content, "
                + "TB_Essay.essay_name, "
                + "TB_EssayType.type_name, "
                +
                // "Essay.useMark, " +
                "TB_Essay.author_pname "
                + "FROM TB_CheckEssay, TB_Essay , TB_EssayType "
                + "WHERE TB_CheckEssay.essay_id = TB_Essay.essay_id "
                + "AND TB_Essay.type_id = TB_EssayType.type_id "
                + "AND TB_Essay.useMark = 'not used' "
                + "AND TB_CheckEssay.check_content LIKE '%ExpertCheckPass%' ";
        if(null != essayName && !"".equals(essayName)){
            sql += " AND Essay.essay_name='" + essayName + "'";
        }
        if(null != expertpName && !"".equals(expertpName)){
            sql += " AND CheckEssay.expert_name='" + expertpName + "'";
        }

        Statement sm = null;
        ResultSet rs = null;

        Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();

        try{
            sm = conn.createStatement();

            rs = sm.executeQuery(sql);

            while(rs.next()){
                List<String> list = new ArrayList<String>();
                for(int i = 2; i <= 7; i++){
                    list.add(rs.getString(i));
                }
                map.put(rs.getInt(1), list);
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }

    }

    public Map<Integer, List<String>> getEssayType(Connection conn)
            throws SQLException {
        String sql = "SELECT * FROM TB_EssayType";

        Statement sm = null;
        ResultSet rs = null;

        Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();

        try{
            sm = conn.createStatement();

            rs = sm.executeQuery(sql);

            while(rs.next()){
                List<String> list = new ArrayList<String>();
                list.add(rs.getString(2));
                list.add(rs.getString(3));
                map.put(rs.getInt(1), list);
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public Map<Integer, List<String>> getEssayCheckedByAdmin(Connection conn)
            throws SQLException {
        String sql = "SELECT " + "essay_id, " + "essay_name, "
                + "author_pname, " + "publish_time, " + "publish_money, "
                + "ispay, " + "TB_EssayType.type_name "
                + "FROM TB_Essay, TB_EssayType "
                + "WHERE useMark <> 'not used' AND useMark NOT LIKE '%not%' "
                + "AND TB_Essay.type_id = TB_EssayType.type_id ";

        Statement sm = null;
        ResultSet rs = null;

        Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();

        try{
            sm = conn.createStatement();

            rs = sm.executeQuery(sql);

            while(rs.next()){
                List<String> list = new ArrayList<String>();
                for(int i = 2; i <= 7; i++){
                    list.add(rs.getString(i));
                }
                map.put(rs.getInt(1), list);
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public Map<Integer, List<String>> getEssayToAuthor(Connection conn,
            String authorPname) throws SQLException {
        String sql = "SELECT " + "essay_id, " + "essay_name, "
                + "publish_time, " + "publish_money, " + "ispay, "
                + "TB_EssayType.type_name " + "FROM TB_Essay, TB_EssayType "
                + "WHERE author_pname='" + authorPname + "' "
                + "AND TB_EssayType.type_id = TB_Essay.type_id ";

        Statement sm = null;
        ResultSet rs = null;

        Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();

        try{
            sm = conn.createStatement();

            rs = sm.executeQuery(sql);

            while(rs.next()){
                List<String> list = new ArrayList<String>();
                for(int i = 2; i <= 6; i++){
                    list.add(rs.getString(i));
                }
                map.put(rs.getInt(1), list);
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

    public Map<Integer, List<String>> getMessageToEditor(Connection conn,
            String user) throws SQLException {
        String sql = null;
        if("author".equals(user)){
            sql = "SELECT " + "author_id, " + "author_pname, " + "messagee "
                    + "FROM TB_Author " + "WHERE messagee IS NOT NULL ";
        }
        if("expert".equals(user)){
            sql = "SELECT " + "expert_id, " + "expert_pname, " + "messagee "
                    + "FROM TB_Expert " + "WHERE messagee IS NOT NULL ";
        }
        if("admin".equals(user)){
            sql = "SELECT " + "admin_id, " + "admin_pname, " + "messagee "
                    + "FROM TB_Admin " + "WHERE messagee IS NOT NULL ";
        }

        Statement sm = null;
        ResultSet rs = null;

        Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();

        try{
            sm = conn.createStatement();

            rs = sm.executeQuery(sql);

            while(rs.next()){
                List<String> list = new ArrayList<String>();
                for(int i = 2; i <= 3; i++){
                    list.add(rs.getString(i));
                }
                map.put(rs.getInt(1), list);
            }
            return map;
        }
        finally{
            sm.close();
            conn.close();
        }
    }

}
