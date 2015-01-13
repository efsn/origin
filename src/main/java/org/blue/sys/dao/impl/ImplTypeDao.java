package org.blue.sys.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.blue.sys.dao.TypeDao;
import org.blue.sys.vo.EssayType;

public class ImplTypeDao implements TypeDao {

    public boolean typeInsert(Connection conn, EssayType essayType)
            throws SQLException {
        String sql = "INSERT INTO TB_EssayType " + "( " + "type_name,"
                + "deadline " + ") " + "VALUES " + "(?,?)";

        PreparedStatement pstm = null;

        try{
            pstm = conn.prepareStatement(sql);
            essayType.setNameStatementValue(pstm);
            pstm.executeUpdate();
        }
        finally{
            pstm.close();
            conn.close();
        }

        return true;

    }

    public boolean typeDelete(Connection conn, EssayType essayType)
            throws SQLException {
        String sql = "DELETE FROM TB_EssayType " + "WHERE type_id = ?";

        PreparedStatement pstm = null;

        try{
            pstm = conn.prepareStatement(sql);
            essayType.setIdStatementValue(pstm);
            pstm.executeUpdate();
        }
        finally{
            pstm.close();
            conn.close();
        }

        return true;
    }

    public Map<Integer, String[]> getAllType(Connection conn)
            throws SQLException {

        String sql = "SELECT type_id, type_name, deadline FROM TB_EssayType ";

        Statement sm = null;
        ResultSet rs = null;
        Map<Integer, String[]> value = new HashMap<Integer, String[]>();

        try{
            sm = conn.createStatement();

            rs = sm.executeQuery(sql);

            while(rs.next()){
                String[] str = new String[2];
                str[0] = rs.getString(2);
                str[1] = rs.getString(3);
                value.put(rs.getInt(1), str);
            }

            return value;

        }
        finally{
            sm.close();
            conn.close();
        }
    }

}
