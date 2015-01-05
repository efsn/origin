package com.blue.sys.dao;

import java.sql.Connection;
import java.sql.SQLException;

import com.blue.sys.vo.*;
import com.blue.sys.vo.CheckEssay;

public interface InsertDao {
    // ʵ��author�û�ע����Ϣ�Ĳ���
    boolean doAuthor(Connection conn, Author author) throws SQLException;

    // ʵ��editor�û���ע����Ϣ�Ĳ���
    boolean doEditor(Connection conn, Editor editor) throws SQLException;

    // ʵ��expert�û�ע����Ϣ�Ĳ���
    boolean doExpert(Connection conn, Expert expert) throws SQLException;

    // ʵ��expert�û�ע����Ϣ�Ĳ���
    boolean doEssay(Connection conn, Essay essay) throws SQLException;

    // ʵ��checkEssay��Ϣ�Ĳ���
    boolean doCheckEssay(Connection conn, CheckEssay checkEssay)
            throws SQLException;

}
