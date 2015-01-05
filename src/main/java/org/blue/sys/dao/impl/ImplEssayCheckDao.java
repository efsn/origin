package com.blue.sys.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import com.blue.sys.dao.EssayCheckDao;

public class ImplEssayCheckDao implements EssayCheckDao {

    public boolean EditorCheckFirst(Connection conn, Map<String, String> value)
            throws SQLException {
        StringBuffer sql = new StringBuffer("UPDATE Essay SET ");

        if(!value.containsKey("not pass")){
            if(null != value.get("checkMark")){
                sql.append(" checkMark = 'editorCheckPass---"
                        + value.get("checkMark") + "'");
            }
        }
        else{
            sql.append(" checkMark = 'editorCheckNotPass---"
                    + value.get("checkMark") + "'");
        }

        sql.append("WHERE essay_name = '" + value.get("essayName") + "'");

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

    public boolean checkMarkFromExpert(Connection conn,
            int essayId,
            String checkMark) throws SQLException {
        String sql = "UPDATE TB_Essay SET checkMark = '" + checkMark
                + "' WHERE essay_id='" + essayId + "'";

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

    public boolean checkMarkFromAdmin(Connection conn,
            int essayId,
            String useMark) throws SQLException {
        String sql = "UPDATE TB_Essay SET useMark = '" + useMark
                + "' WHERE essay_id='" + essayId + "'";

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
