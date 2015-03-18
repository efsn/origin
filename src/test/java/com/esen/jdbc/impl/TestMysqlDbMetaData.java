package com.esen.jdbc.impl;

import java.sql.SQLException;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SqlExecute;

import func.FuncTestCase;
import func.jdbc.FuncConnectionFactory;

public class TestMysqlDbMetaData extends TestDbMetaDataAbstract {

	public ConnectionFactory getConnectionFactory() {
		return FuncConnectionFactory.getMysqlCustomConnectionFactory();
	}

	public void createProcedure() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		String createSql1 = "CREATE PROCEDURE vv() BEGIN END;";//有效的存储过程
		String createSql2 = "";//无效的存储过程

		executeSql(createSql1);
		executeSql(createSql2);
	}

	public void createTrigger() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		//触发器一定有效,无法创建无效的触发器.删除触发器所在的表时,触发器也会被删除
		String createSql = "CREATE " + " TRIGGER cc2 AFTER INSERT" + " ON EBI_SYS21_USER" + " FOR EACH ROW BEGIN"
				+ " END";
		executeSql(createSql);
	}

	public void createSynonym() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		String createSql1 = "";//有效的同义词
		String createSql2 = "";//无效的同义词

		executeSql(createSql1);
		executeSql(createSql2);
	}

	public void testDbLink() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		String createSql1 = "";//有效的数据库连接
		String createSql2 = "";//无效的数据库连接

		executeSql(createSql1);
		executeSql(createSql2);
	}

	private void executeSql(String sql) throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		SqlExecute se = SqlExecute.getInstance(fct);
		se.executeUpdate(sql);
		
	}
}
