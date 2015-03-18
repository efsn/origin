package com.esen.jdbc.impl;

import java.sql.SQLException;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SqlExecute;

import func.FuncTestCase;
import func.jdbc.FuncConnectionFactory;

public class TestMssql2005DbMetaData extends TestDbMetaDataAbstract {

	public ConnectionFactory getConnectionFactory() {
		return FuncConnectionFactory.getMssql2005CustomConnectionFactory();
	}

	public void createProcedure() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		String createSql1 = "CREATE PROCEDURE PVFS AS select * from EBI_SYS21_VFS GO";//有效的存储过程
		String createSql2 = "CREATE PROCEDURE PVFS AS select * from EBI_SYS21_VFS_11 GO";//无效的存储过程

		executeSql(createSql1);
		executeSql(createSql2);
	}

	public void createTrigger() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		//触发器一定有效,无法创建无效的触发器.删除触发器所在的表时,触发器也会被删除
		String createSql = "Create Trigger tEBI_SYS21_VFS "
				+ " On EBI_SYS21_VFS                        --在Student表中创建触发器"
				+ " for Update                         --为什么事件触发"
				+ " As                                       --事件触发后所要做的事情" + " if Update(PARENTDIR)           "
				+ " begin" + " Update EBI_SYS21_VFS" + " Set PARENTDIR=i.PARENTDIR"
				+ " From BorrowRecord br , Deleted  d ,Inserted i     --Deleted和Inserted临时表"
				+ " Where br.PARENTDIR=d.PARENTDIR" + " end";
		executeSql(createSql);
	}

	public void createSynonym() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		String createSql1 = "CREATE SYNONYM TT FOR EBI_SYS21_VFS";//有效的同义词
		String createSql2 = "CREATE SYNONYM TT1 FOR EBI_SYS21_VFS_11";//无效的同义词

		executeSql(createSql1);
		executeSql(createSql2);
	}

	public void testDbLink() throws SQLException {
		ConnectionFactory fct = getConnectionFactory();
		String createSql1 = "EXEC sp_addlinkedserver @server='S1_instance1',@srvproduct='',@provider='SQLNCLI',@datasrc='S1\\instance1'";//有效的数据库连接
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
