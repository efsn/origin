package com.esen.jdbc.dialect.impl.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.AbstractDataBaseInfo;

public class MysqlDataBaseInfo extends AbstractDataBaseInfo {
  /**
   * varchar类型最大长度；
   */
  private int maxVarcharLength;
  
	/*
	 * IRPT-8814: MySQL 主键长度限制
	 */
	private int maxKeyLength; // 主键最大字节数
	private Properties props; // MySQL 的系统变量
	
	static final int MAX_KEY_LENGTH_NORMAL = 767;
	static final int MAX_KEY_LENGTH_LARGE = 3072;
  
  public MysqlDataBaseInfo(Connection conn, String defaultSchema) throws SQLException {
    super(conn,defaultSchema);
    this.dbtype = SqlConst.DB_TYPE_MYSQL;
    this.testSql = "select 1";
    
    initVariables(conn);

	initMysql(conn);
  }
  
  /**
   * 获取默认的schema名；
   * mysql的schema名是数据库名，不是用户名，如果没有指定，则返回空；
   * @param conn
   * @param defaultSchema 
   */
  protected void initDefaultSchema(Connection conn, String defaultSchema) {
    if(defaultSchema!=null&&defaultSchema.length()>0){
      default_schema = defaultSchema;
    }else{
      String v = getDefaultSchema(conn);//通过查询sql获取默认schema名
      if(v!=null && v.trim().length()>0){
    	  default_schema = v;
      }
    }
  }
  protected String getDefaultSchema(Connection conn) {
	  return null;
  }

	private void initMysql(Connection conn) throws SQLException {
		DatabaseMetaData dbmd = conn.getMetaData();
		int majorVersion = dbmd.getDatabaseMajorVersion();
		if (majorVersion < 5) {
			maxVarcharLength = 255;
		} else {
			maxVarcharLength = 8000;
		}

		/*
		 * Innodb类型MySQL的varchar主键只支持不超过768个字节
		 */
		maxKeyLength = MysqlDataBaseInfo.MAX_KEY_LENGTH_NORMAL;
		int minorVersion = dbmd.getDatabaseMinorVersion();
		int littleVersion = 0;

		String[] productVersion = dbmd.getDatabaseProductVersion().split("\\.");
		if (productVersion != null && productVersion.length >= 3) {
			// MySQL 的小版本号中可能带有字母
			String[] ver = productVersion[2].split("\\D");
			if (ver != null)
				littleVersion = Integer.parseInt(ver[0]);
		}

		/* 
		 * IRPT-8814: MySQL 的主键长度限制
		 * 从 5.5.14 版本开始，在同时满足以下条件时，最大长度为 3072 bytes
		 * innodb_file_format=barracuda
		 * innodb_large_prefix=on
		 * innodb_file_per_table =on
		 */
		if (majorVersion > 5
				|| (majorVersion == 5 && minorVersion > 5)
				|| (majorVersion == 5 && minorVersion == 5 && littleVersion >= 14)) {
			if (getVariable("innodb_file_format").compareToIgnoreCase("Barracuda") == 0
					&& getVariable("innodb_large_prefix").compareToIgnoreCase("ON") == 0
					&& getVariable("innodb_file_per_table").compareToIgnoreCase("ON") == 0) {
				maxKeyLength = MysqlDataBaseInfo.MAX_KEY_LENGTH_LARGE;
			}
		}
	}

	/*
	 * 获取所有系统变量的值
	 */
	private void initVariables(Connection conn) throws SQLException {
		String sql = "show variables";
		props = new Properties();
		Statement stmt = null;
		try {			
			stmt = conn.createStatement();
			ResultSet rs = null;
			try {
				rs = stmt.executeQuery(sql);
				while (rs.next()) {
					props.setProperty(rs.getString(1), rs.getString(2));
				}
			} finally {
				rs.close();
			}
		} catch (SQLException ex) {
			;
		} finally {
			stmt.close();
		}
	}

	/*
	 * 表定义中 row_format 是否采用 compressed
	 */
	public boolean useCompressedTable() {
		return maxKeyLength == MysqlDataBaseInfo.MAX_KEY_LENGTH_LARGE;
	}

	protected String getVariable(String variable) {
		if (props == null)
			return "";
		String value = props.getProperty(variable);
		return value == null ? "" : value;
	}

  protected String getNCharByteLengthSQL() {
    /**
     * mysql utf8,gbk字符集，获取中文的字节长度；
     */
    return "select length('中')";
  }
  
  protected String getNCharLengthSQL() {
    /**
     * mysql utf8,gbk字符集，定义varchar(n) 类型，一个中文占一个长度；
     */
    return "select char_length('中')";
  }

  /**
   * Innodb类型MySQL的varchar主键只支持不超过768个字节 或者 768/2=384个双字节 或者 768/3=256个三字节的字段；
   * 而GBK是双字节的，UTF-8是三字节的；
   */
  public int getMaxKeyOfFieldsLength() {
	  return maxKeyLength / getNCharByteLength();
  }

  public int getMaxIndexOfFieldsLength() {
    return -1;
  }

  /**
   * BIDEV-1046
   * mysql5可以定义最大65535字节长度的字符类型，很多建表语句直接调用这个方法获取最大可定义的字符类型长度；
   * 这个值太大，直接使用会报异常：Column length too big for column 'RESOURCEID' (max = 21845); use BLOB or TEXT instead
   * 现在改为8000；
   * 
   * 创建表时可能会调用这个方法，获取字符字段允许的定义长度；
   * 这里给一个一般情况下，比较大的字符字段定义长度，而不是数据库允许的最大长度；
   * 原因是如果这个值设置的很大，尽管数据允许，但是和表的其他字段长度加起来可能超过页大小限制；
   */
  public int getMaxVarcharFieldLength() {
    return maxVarcharLength;
  }
  
}
