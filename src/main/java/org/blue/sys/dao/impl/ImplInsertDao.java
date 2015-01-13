package org.blue.sys.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.blue.sys.dao.InsertDao;
import org.blue.sys.vo.Author;
import org.blue.sys.vo.CheckEssay;
import org.blue.sys.vo.Editor;
import org.blue.sys.vo.Essay;
import org.blue.sys.vo.Expert;

public class ImplInsertDao implements InsertDao {
    public boolean doAuthor(Connection conn, Author author)
            throws SQLException {
        String sql = "INSERT INTO TB_Author " + "( " + "author_pname, "
                + "author_pwd, " + "author_name, " + "author_email, "
                + "author_telephone, " + "author_address, " + "author_mark "
                + ") " + "VALUES " + "(?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pstm = null;

        try{
            pstm = conn.prepareStatement(sql);
            author.setStatementValue(pstm);
            pstm.executeUpdate();
        }
        finally{
            this.close(pstm);
            conn.close();
        }

        return true;
    }

    public boolean doEditor(Connection conn, Editor editor)
            throws SQLException {
        String sql = "INSERT INTO TB_Editor " + "( " + "editor_pname, "
                + "editor_pwd, " + "editor_name, " + "editor_email, "
                + "editor_telephone " + ") " + "VALUES " + "(?, ?, ?, ?, ?)";

        PreparedStatement pstm = null;

        try{
            pstm = conn.prepareStatement(sql);
            editor.setStatementValue(pstm);
            pstm.executeUpdate();
        }
        finally{
            this.close(pstm);
            conn.close();
        }

        return true;
    }

    public boolean doExpert(Connection conn, Expert expert)
            throws SQLException {
        String sql = "INSERT INTO TB_Expert " + "( " + "expert_pname, "
                + "expert_pwd, " + "expert_name, " + "expert_email, "
                + "expert_telephone, " + "expert_title, " + "expert_remark "
                + ") " + "VALUES " + "(?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pstm = null;

        try{
            pstm = conn.prepareStatement(sql);
            expert.setStatementValue(pstm);
            pstm.executeUpdate();
        }
        finally{
            this.close(pstm);
            conn.close();
        }

        return true;
    }

    public boolean doEssay(Connection conn, Essay essay) throws SQLException {
        String sql = "INSERT INTO TB_Essay " + "( " + "type_id, "
                + "essay_name, " + "essay_content1, " + "essay_content2, "
                + "publish_time, " + "useMark, " + "checkMark, "
                + "author_pname, " + "essay_nums, " + "essay_keywords, "
                + "author_info, " + "register_date " + ") " + "VALUES "
                + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pstm = null;

        try{
            pstm = conn.prepareStatement(sql);
            essay.setStatementValue(pstm);
            pstm.executeUpdate();
        }
        finally{
            this.close(pstm);
            conn.close();
        }

        return true;
    }

    private void close(PreparedStatement pstm) throws SQLException {
        pstm.close();
    }

    public boolean doCheckEssay(Connection conn, CheckEssay checkEssay)
            throws SQLException {
        String sql = "INSERT INTO TB_CheckEssay " + "( " + "essay_id, "
                + "expert_name, " + "check_content, " + "check_date " + ") "
                + "VALUES " + "(?, ?, ?, ?)";

        PreparedStatement pstm = null;

        try{
            pstm = conn.prepareStatement(sql);
            checkEssay.setStatementValue(pstm);
            pstm.executeUpdate();
        }
        finally{
            this.close(pstm);
            conn.close();
        }

        return true;
    }

}
