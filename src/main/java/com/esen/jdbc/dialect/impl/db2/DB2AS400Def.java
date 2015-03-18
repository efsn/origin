package com.esen.jdbc.dialect.impl.db2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.util.StrFunc;

/**
 * AS400,i5OS系统，db2数据库的DbDefiner实现；
 * 
 * @author dw
 * 
 */
public class DB2AS400Def extends DB2Def {

	public DB2AS400Def(Dialect dl) {
		super(dl);
	}

	/**
	 * Memo类型定义为long varchar时，执行增加字段：
	 * ALTER TABLE T_TEST ADD add_str_ VARCHAR(10) 
	 * 会出现DB2 SQL error: SQLCODE: -101, SQLSTATE: 54001, SQLERRMC: 3的异常；
	 * 说的是表定义的列长度超过了限制；
	 * 
	 * 解决办法：改为定义为clob类型；
	 */
	protected String getMemoFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
		    + " CLOB(1M) "
		    + getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_memo,
		        isUpdate);
	}
	
	/**
	 * BI-6770, AS400生成的 DEFAULT 2099-12-31 NOT NULL 上无法执行
	 * 如果默认值没有被引号引起来，且不是数据库关键字, 则要加上引号
	 */
	protected String getDateFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		String default_value = fi.getDefaultValue();
		if(!StrFunc.isNull(default_value) && !SqlFunc.isKeyWord(default_value) && default_value.length()> 2 
				&& default_value.charAt(0)!='\'' && default_value.charAt( default_value.length() -1 )!='\'') {
			default_value = "'" + default_value + "'";
		}
		return getColumnName(fi.getName())
				+ " DATE "
				+ getTailDdl(default_value, fi.isNullable(), fi.isUnique(), fi.getDesc(),	DbDefiner.FIELD_TYPE_DATE, 
						isUpdate);
	}

	/**
	 * DB2 A400数据库下查询表是否存在使用系统表SYSIBM.TABLES, 不再依赖于驱动程序提供的方法
	 * 这样就可以使用最新版本驱动
	 */
	public boolean tableExists(Connection conn, String catalog, String tablename) throws SQLException {
		String[] tbs = getTableNameForDefaultSchema(tablename, dbinf);
		String sql = "select TABLE_NAME from SYSIBM.TABLES where TABLE_SCHEMA='" + tbs[0] + "' " + "and TABLE_NAME='"
		    + tbs[1].toUpperCase() + "' and (TABLE_TYPE='BASE TABLE' or TABLE_TYPE='ALIAS')";
		return excuteSql(conn, sql);
	}

	public boolean viewExists(Connection conn, String viewname) throws SQLException {
		String[] tbs = getTableNameForDefaultSchema(viewname, dbinf);
		String sql = "select TABLE_NAME from SYSIBM.TABLES where TABLE_SCHEMA='" + tbs[0] + "' " + "and TABLE_NAME='"
		    + tbs[1].toUpperCase() + "' and (TABLE_TYPE='VIEW')";
		return excuteSql(conn, sql);
	}

	public boolean tableOrViewExists(Connection conn, String tvname) throws SQLException {
		String[] tbs = getTableNameForDefaultSchema(tvname, dbinf);
		String sql = "select TABLE_NAME from SYSIBM.TABLES where TABLE_SCHEMA='" + tbs[0] + "' " + "and TABLE_NAME='"
		    + tbs[1].toUpperCase() + "'";
		return excuteSql(conn, sql);
	}

	public boolean indexExists(Connection conn, String tbname, String indexname) throws SQLException {
		String sql = "select index_name from sysibm.sqlstatistics " + "where index_name='" + indexname.toUpperCase() + "'";
		Statement stmt = conn.createStatement();
		ResultSet rs = null;
		try {
			rs = stmt.executeQuery(sql);
			return ((rs.next() ? true : false));
		}
		finally {
			rs = null;
			stmt.close();
		}
	}

	/**
	 * DB2 A400数据库下查询物理表关联的视图列表使用系统表QSYS2.SYSVIEWDEP
	 */
	protected String[] getViewFromTable(Statement stat, String tbname) throws SQLException {
		String[] tbs = getTableNameForDefaultSchema(tbname, dbinf);
		String sql = "select VIEW_NAME from QSYS2.SYSVIEWDEP where VIEW_SCHEMA='" + tbs[0] + "' and TABLE_NAME='"
		    + tbs[1].toUpperCase() + "'";
		ResultSet rs = stat.executeQuery(sql);
		List ll = new ArrayList();
		while (rs.next()) {
			ll.add(rs.getString(1));
		}
		if (ll.size() > 0) {
			String[] tbs2 = new String[ll.size()];
			ll.toArray(tbs2);
			return tbs2;
		}
		return null;
	}

	/**
	 * DB2 A400数据库下查询视图定义DDL语句使用系统表QSYS2.VIEWS取字段VIEW_DEFINITION
	 * 这个VIEW_DEFINITION和DB2下的SYSCAT.VIEWS.TEXT还不太一样, 
	 * VIEW_DEFINITION 获取出来的值诸如 : select * from table, 不能直接用来执行创建视图
	 */
	protected String getViewText(Statement stat, String vname) throws SQLException {
		String[] tbs = getTableNameForDefaultSchema(vname, dbinf);
		String sql = "select VIEW_DEFINITION from QSYS2.VIEWS where TABLE_SCHEMA='" + tbs[0] + "' and TABLE_NAME='"
		    + tbs[1].toUpperCase() + "'";
		ResultSet rs = stat.executeQuery(sql);
		while (rs.next()) {
			return "CREATE VIEW " + tbs[1] + " AS " + rs.getString(1);
		}
		return null;
	}
}
