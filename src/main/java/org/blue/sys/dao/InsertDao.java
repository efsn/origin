package org.blue.sys.dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.blue.sys.vo.Author;
import org.blue.sys.vo.CheckEssay;
import org.blue.sys.vo.Editor;
import org.blue.sys.vo.Essay;
import org.blue.sys.vo.Expert;

public interface InsertDao {
    boolean doAuthor(Connection conn, Author author) throws SQLException;

    boolean doEditor(Connection conn, Editor editor) throws SQLException;

    boolean doExpert(Connection conn, Expert expert) throws SQLException;

    boolean doEssay(Connection conn, Essay essay) throws SQLException;

    boolean doCheckEssay(Connection conn, CheckEssay checkEssay)
            throws SQLException;

}
