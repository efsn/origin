package com.esen.jdbc.impl;

import java.sql.SQLException;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SqlExecute;

import func.FuncTestCase;
import func.jdbc.FuncConnectionFactory;

public class TestOracleDbMetaData extends TestDbMetaDataAbstract {

	public ConnectionFactory getConnectionFactory() {
		return FuncConnectionFactory.getOracleCustomConnectionFactory();
	}

	public void createProcedure() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		String createSql1 = "create or replace procedure output_date is begin dbms_output.put_line(sysdate); end output_date;";//有效的存储过程
		String createSql2 = "create or replace procedure output_date1 is begin ; end output_date;";//无效的存储过程

		executeSql(createSql1);
		executeSql(createSql2);
	}

	public void createTrigger() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		//触发器一定有效,无法创建无效的触发器.删除触发器所在的表时,触发器也会被删除
		String createSql = "create or replace trigger TEBI_SYS21_LOG before insert on EBI_SYS21_LOG for each row begin select SEQ_ESENSOFT_IREPORT5.nextval into :new.index_ from dual;end;";
		executeSql(createSql);
	}

	public void createSynonym() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		String createSql1 = "CREATE OR REPLACE SYNONYM \"TEST\".\"TT\" FOR \"ZCX\".\"EBI_SYS22_VFS123\";";//有效的同义词
		String createSql2 = "CREATE OR REPLACE SYNONYM \"TEST\".\"TT\" FOR \"ZCX\".\"EBI_SYS22_VFS123\";";//无效的同义词

		executeSql(createSql1);
		executeSql(createSql2);
	}

	public void testDbLink() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		String createSql1 = "CREATE DATABASE LINK l3 CONNECT TO test IDENTIFIED BY test USING 'jdbc:oracle:thin:@192.168.1.100:1521:esenbi'";//有效的数据库连接
		String createSql2 = "CREATE DATABASE LINK l4 CONNECT TO test IDENTIFIED BY testa USING 'jdbc:oracle:thin:@192.168.1.100:1521:esenbi'";//无效的数据库连接

		executeSql(createSql1);
		executeSql(createSql2);
	}

	public void testSequences() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		//有效的序列,无法创建无效的序列
		String createSql = "CREATE SEQUENCE  \"TEST\".\"SEQ_ESENSOFT_IREPORT6\"  MINVALUE 1 MAXVALUE 1000 INCREMENT BY 1 START WITH 20 CACHE 20 NOORDER  NOCYCLE ;";

		executeSql(createSql);
	}

	private void executeSql(String sql) throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		SqlExecute se = SqlExecute.getInstance(fct);
		se.executeUpdate(sql);

	}
}
