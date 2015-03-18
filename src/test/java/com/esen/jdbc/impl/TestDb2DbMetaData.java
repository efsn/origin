package com.esen.jdbc.impl;

import java.sql.SQLException;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SqlExecute;

import func.FuncTestCase;
import func.jdbc.FuncConnectionFactory;

public class TestDb2DbMetaData extends TestDbMetaDataAbstract {

	public static void main(String[] args) throws SQLException {
		TestDb2DbMetaData t = new TestDb2DbMetaData();
		t.createProcedure();
	}

	public ConnectionFactory getConnectionFactory() {
		return FuncConnectionFactory.getDb2CustomConnectionFactory();
	}

	public void createProcedure() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		String createSql1 = "CREATE PROCEDURE org123() ---存储过程可以设定输入参数和输出参数"
				+ " LANGUAGE SQL                   ----DB2可以用多种语言编写存储过程，这里用的是纯SQL"
				+ " BEGIN                          ---开始"
				+ " SELECT index_ FROM ebi_sys22_org   ---for循环 tmp_brnd_cd预先创建好" + " END";//有效的存储过程
		//无法创建无效的存储过程

		executeSql(createSql1);
	}

	public void createTrigger() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		//触发器一定有效,无法创建无效的触发器.删除触发器所在的表时,触发器也会被删除
		String createSql = "CREATE TRIGGER TESTCASE.TT NO CASCADE BEFORE  INSERT  ON TESTCASE.EBI_SYS21_VFS  FOR EACH ROW  MODE DB2SQL select * from EBI_SYS21_VFS";
		executeSql(createSql);
	}

	public void createSynonym() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		String createSql1 = "";//有效的同义词
		//不能创建无效的同义词

		executeSql(createSql1);
	}

	public void testDbLink() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		String createSql1 = "";//有效的数据库连接
		String createSql2 = "";//无效的数据库连接

		executeSql(createSql1);
		executeSql(createSql2);
	}

	public void testSequences() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		//有效的序列,无法创建无效的序列
		String createSql = "";

		executeSql(createSql);
	}

	private void executeSql(String sql) throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		SqlExecute se = SqlExecute.getInstance(fct);
		se.executeUpdate(sql);
	}
}
