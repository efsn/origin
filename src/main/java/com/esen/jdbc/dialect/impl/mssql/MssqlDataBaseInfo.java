package com.esen.jdbc.dialect.impl.mssql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.AbstractDataBaseInfo;

public class MssqlDataBaseInfo extends AbstractDataBaseInfo {

	/**
	 * 数据库的排序规则
	 */
	private String collation;

  public MssqlDataBaseInfo(Connection conn, String defaultSchema) throws SQLException {
    super(conn, defaultSchema);
    this.dbtype = SqlConst.DB_TYPE_MSSQL;
    this.testSql = "select 1";
    
    /*
     * ISSUE:BI-6480:added by liujin 2013.04.08
     * MSSQL 在默认情况下，列的排序规则与数据库一致。
     * 修改为当数据库不区分大小写时，列区分大小写。避免从其他数据库导入数据时主键冲突。
     */
    initCollection(conn);
  }
  
	/*
	 * 使用内置 函数查询 Schema
	 */
	protected String getDefaultSchema(Connection conn) {
		try {
			Statement stat = conn.createStatement();
			try {
				ResultSet rs = stat.executeQuery("SELECT SCHEMA_NAME()");
				if (rs.next()) {
					return rs.getString(1);
				}
				rs.close();
			} finally {
				stat.close();
			}
		} catch (SQLException se) {
			//出错时，使用数据库的默认schema
			return "dbo";
		}
		return "dbo";
	}

  
  protected String getNCharByteLengthSQL() {
    return "select datalength('中')";
  }
  
  protected String getNCharLengthSQL() {
    return "select datalength('中')";
  }

  /**
   * sqlserver 没有限制，可以定义很长的索引；例：

CREATE TABLE T_TEST(INT_ INT ,STR_ VARCHAR(3000) ,STR2_ VARCHAR(4000) ,STR3_ VARCHAR(5000) ,NUM_ NUMERIC(18,2) ,DATE_ DATETIME );

CREATE UNIQUE INDEX I_STR_STR2_DATE ON T_TEST(STR_,STR2_,STR3_);

可成功创建；

   */
  public int getMaxKeyOfFieldsLength() {
    return -1;
  }

  public int getMaxIndexOfFieldsLength() {
    return -1;
  }

  /**
   * 创建表时可能会调用这个方法，获取字符字段允许的定义长度；
   * 这里给一个一般情况下，比较大的字符字段定义长度，而不是数据库允许的最大长度；
   * 原因是如果这个值设置的很大，尽管数据允许，但是和表的其他字段长度加起来可能超过页大小限制；
   */
  public int getMaxVarcharFieldLength() {
    return 4000;
  }
  
	/**
	 * 获取数据库的排序规则
	 * @return 排序规则
	 */
	public String getCollation() {
		return collation;
	}

	/**
	 * 初始化数据库的排序规则
	 * 
	 * @param conn 数据库连接
	 */
	private void initCollection(Connection conn) {
		/*
		 * databasepropertyex 函数的返回值是 variant
		 * 此类型 jdbc 无法处理，会抛出异常，所以转换成 varchar 类型
		 */
		try {
			Statement stmt = null;
			ResultSet rs = null;

			try {
				String sql = "SELECT CAST(databasepropertyex('" 
							+ conn.getCatalog() + "', 'collation') as varchar)";

				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				if (rs.next()) {
					this.collation = rs.getString(1);
				} else {
					this.collation = "";
				}
			} finally {
				if (rs != null) {
					rs.close();
				}

				if (stmt != null) {
					stmt.close();
				}
			}
		} catch (SQLException e) {
			this.collation = "";
		}
	}
}
