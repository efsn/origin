package com.esen.jdbc.dialect.impl.dm;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.AbstractDataBaseInfo;

public class DMDataBaseInfo extends AbstractDataBaseInfo {

	public DMDataBaseInfo(Connection conn, String defaultSchema) throws SQLException {
		super(conn,defaultSchema);
		this.dbtype = SqlConst.DB_TYPE_DM;
	    this.testSql = "select 1";
	}

	protected String getDefaultSchema(Connection conn) {
		return null;
	}
	  
	protected String getNCharByteLengthSQL() {
		return "select length('中')";
	}

	protected String getNCharLengthSQL() {
		return "select length('中')";
	}

	/**
	 * 索引各字段值相加得到的总数据值长度不得超过1020。
	 */
	public int getMaxIndexOfFieldsLength() {
		return 1020;
	}

	public int getMaxKeyOfFieldsLength() {
		return 1020;
	}

	/**
	 * varchar类型最大长度和页大小有关：
	 * 4k --> 1900
	 * 8k --> 3900
	 * 16k --> 8000
	 * 
   * 创建表时可能会调用这个方法，获取字符字段允许的定义长度；
   * 这里给一个一般情况下，比较大的字符字段定义长度，而不是数据库允许的最大长度；
   * 原因是如果这个值设置的很大，尽管数据允许，但是和表的其他字段长度加起来可能超过页大小限制；
   */
	public int getMaxVarcharFieldLength() {
		return 1900;
	}

}
