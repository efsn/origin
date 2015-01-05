package com.blue.sys.dao;

import com.blue.sys.vo.Author;
import com.blue.sys.vo.Editor;
import com.blue.sys.vo.Expert;

import java.sql.Connection;
import java.sql.SQLException;

public interface DeleteDao {
    
    boolean deleteAuthor(Connection conn, Author author) throws SQLException;

    boolean deleteExpert(Connection conn, Expert expert) throws SQLException;

    boolean deleteEditor(Connection conn, Editor editor) throws SQLException;

    boolean deleteEssay(Connection conn, int id) throws SQLException;

}
