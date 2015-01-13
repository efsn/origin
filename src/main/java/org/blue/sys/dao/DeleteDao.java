package org.blue.sys.dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.blue.sys.vo.Author;
import org.blue.sys.vo.Editor;
import org.blue.sys.vo.Expert;

public interface DeleteDao {
    
    boolean deleteAuthor(Connection conn, Author author) throws SQLException;

    boolean deleteExpert(Connection conn, Expert expert) throws SQLException;

    boolean deleteEditor(Connection conn, Editor editor) throws SQLException;

    boolean deleteEssay(Connection conn, int id) throws SQLException;

}
