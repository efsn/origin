package com.esen.jdbc.dialect.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.util.StrFunc;

public abstract class AbstractDataBaseInfo extends DataBaseInfo {

  protected int dbtype;

  protected int databaseMajorVersion;

  protected int databaseMinorVersion;

  protected String databaseProductVersion;

  protected String databaseProductName;

  protected String characterEncoding;
  
  protected int maxColumnNameLength;
  
  protected int maxTableNameLength; 
  
  protected int maxIndexNameLength;
  
  protected int maxColumnsInTable;//数据库表支持的最大字段数；
  
  /**
   * 20090813
   * 记录数据库的一个中文字符在字符类型字段varchar(n)定义的长度中占几个长度；
   * 主要用于DataCopy在导入数据时，判断插入值的长度是否超过字段定义的长度；
   */
  private int nCharLength;
  
  /**
   * 20090902
   * 记录一个中文字符在数据库中的byte长度；
   * 通常gbk字符集占2个长度，utf8字符集占3个长度；
   */
  private int nCharByteLength;

  /**
   * 记录此链接的url字符串
   */
  private String jdbcurl;

  private String databaseAddress;

  /**
   * 记录此链接的数据库用户名；
   */
  private String username;
  
  /**
   * 用于测试数据库连接的sql；
   */
  protected String testSql;
  
  /**
   * jdbc驱动的版本；
   */
  protected String driverVersion;
  
  /**
   * jdbc驱动的主版本号；
   */
  protected int driverMajorVersion;
  
  /**
   * 用户默认的schema名；
   */
  protected String default_schema;
  
  public AbstractDataBaseInfo(Connection conn, String defaultSchema) throws SQLException {
    DatabaseMetaData dbmd = conn.getMetaData();
    jdbcurl = dbmd.getURL();
    username = dbmd.getUserName();
    databaseAddress = parseDatabaseAddress();

    databaseProductVersion = dbmd.getDatabaseProductVersion();
    databaseProductName = dbmd.getDatabaseProductName();
    driverVersion = dbmd.getDriverVersion();
    driverMajorVersion = dbmd.getDriverMajorVersion();
    try {
      //有些jdbc不支持这两个方法
      databaseMajorVersion = dbmd.getDatabaseMajorVersion();
      databaseMinorVersion = dbmd.getDatabaseMinorVersion();
    }
    catch (Throwable ex) {
      if (isOracle()) {
        if (databaseProductVersion.indexOf("8.1") >= 0)
          databaseMajorVersion = 8;
      }
    }
    maxColumnNameLength = dbmd.getMaxColumnNameLength();
    maxTableNameLength = dbmd.getMaxTableNameLength();
    maxIndexNameLength = maxTableNameLength;

    maxColumnsInTable = dbmd.getMaxColumnsInTable();

    initNCharByteLength(conn);
    initNCharLength(conn);
    
    initDefaultSchema(conn, defaultSchema);
  }

  /**
   * 获取默认的schema名；
   * @param conn
   * @param defaultSchema 
   */
  protected void initDefaultSchema(Connection conn, String defaultSchema) {
		if (!StrFunc.isNull(defaultSchema)) {
			default_schema = defaultSchema;
		} else {
			String v = getDefaultSchema(conn);// 通过查询sql获取默认schema名
			if (v == null || v.trim().length() == 0) {
				default_schema = username;
			} else {
				default_schema = v;
			}
		}
  }
  
  /**
   * 通过sql查询获取默认schema名；
   * @param conn
   * @return
   */
	/*
	 * BUG:ESENBI-3591: modify by liujin 2015.01.04 
	 * 对于只读权限的数据库，使用该方式获取 schema 名会出错
	 * 对于支持其他方法获取 schema 名的数据库需要重写该方法
	 */
	protected String getDefaultSchema(Connection conn) {
		String tablename = String.valueOf((char) (Math.floor(Math.random() * 26) + 'A'))
				+ String.valueOf((int) Math.round(Math.random() * 2000));
		try{
			conn.setAutoCommit(true);
			Statement stat = conn.createStatement();
			stat.executeUpdate("create table "+tablename+" (field0 char(1))");
			try{
				DatabaseMetaData dbmeta = conn.getMetaData();
				ResultSet rs = dbmeta.getTables(null, null, tablename, new String[]{"TABLE"});
				while(rs.next()){
					String tbname = rs.getString("TABLE_NAME");
					if(tbname.equalsIgnoreCase(tablename)){
						return rs.getString("TABLE_SCHEM");
					}
				}
			}finally{
				stat.execute("drop table "+tablename);
			}
		}catch(SQLException sx){
			throw new RuntimeException(sx);
		}
		return null;
	}

  
  public String getDefaultSchema(){
    return default_schema;
  }

  public String getDriverVersion(){
    return driverVersion;
  }
  public int getDriverMajorVersion(){
    return driverMajorVersion;
  }

  /**
   * 获得数据库支持的字段名最大长度；
   * @return
   */
  public int getMaxColumnNameLength() {
    return maxColumnNameLength;
  }
  /**
   * 表名最大长度；
   * @return
   */
  public int getMaxTableNameLength() {
    return maxTableNameLength;
  }
  /**
   * 索引名最大长度
   * @return
   */
  public int getMaxIndexNameLength() {
    return maxIndexNameLength;
  }
  
  /**
   * 一个中文字，在数据库的字符类型定义varchar(n)中的长度；
   * 
   * @return
   */
  public int getNCharLength(){
    return nCharLength;
  }
  
  /**
   * 20090902
   * 返回一个中文字符在数据库中的byte长度；
   * 通常gbk字符集占2个长度，utf8字符集占3个长度；
   * @return
   */
  public int getNCharByteLength(){
    return nCharByteLength;
  }
  

  /**
   * 20090902
   * 初始化获取数据库中中文的字节长度；
   * @param conn
   * @throws SQLException
   */
  protected void initNCharByteLength(Connection conn) throws SQLException {
    String sql = getNCharByteLengthSQL();
    initNCharByteLength(conn,sql);
  }
  private void initNCharByteLength(Connection conn, String sql) throws SQLException {
    if (sql == null) {
      /**
       * 其他未知数据库，默认一个中文字，占2个字符长度；
       */
      nCharByteLength = 2;
      return;
    }
    Statement stat = conn.createStatement();
    try {
      ResultSet rs = stat.executeQuery(sql);
      try {
        if(rs.next()){
          nCharByteLength = rs.getInt(1);
        }else {
          nCharByteLength = 2;
        }
      }
      finally {
        rs.close();
      }
    }
    finally {
      stat.close();
    }
  }
  
  /**
   * 20090813
   * 初始化参数nCharLength ，获取一个中文字符在数据库中的字符长度；
   * 字符类型字段，varchar(n)，oracle是varchar2(n),长度 n :
   * DB2,SybaseAse,SqlServer 都按照字节来计算长度的，gbk字符集一个中文占两个长度，utf8字符集占3个长度；
   * mysql是按字符长度的，gbk和utf8字符集，一个中文都占一个长度；
   * oracle的英文字符集，一个汉字占两个长度；
   * @param conn
   * @throws SQLException 
   */
  protected void initNCharLength(Connection conn) throws SQLException {
    String sql = getNCharLengthSQL();
    initNCharLength(conn,sql);
  }
  
  private void initNCharLength(Connection conn, String sql) throws SQLException {
    if (sql == null) {
      /**
       * 其他未知数据库，默认一个中文字，占2个字符长度；
       */
      nCharLength = 2;
      return;
    }
    Statement stat = conn.createStatement();
    try {
      ResultSet rs = stat.executeQuery(sql);
      try {
        if(rs.next()){
          nCharLength = rs.getInt(1);
        }else {
          nCharLength = 2;
        }
      }
      finally {
        rs.close();
      }
    }
    finally {
      stat.close();
    }
  }
  
  protected abstract String getNCharLengthSQL();
  
  /**
   * 通过sql查询获取中文的字节长度；
   * @return
   */
  protected abstract String getNCharByteLengthSQL();
  

  
  private String parseDatabaseAddress() {
    String r= SqlFunc.getIpAddressFromUrl(jdbcurl);
    if (r==null)
      return r;
    if (r.equalsIgnoreCase("127.0.0.1")||r.equalsIgnoreCase("localhost")){
      try {
        return InetAddress.getLocalHost().getHostAddress();
      }
      catch (UnknownHostException ex) {
        ex.printStackTrace();
        return null;
      }
    }
    return r;
  }

  /**
   * 返回数据库连接的url字符串；
   * @return
   */
  public final String getJdbcurl() {
    return jdbcurl;
  }
  
  /**
   * 返回数据库连接的用户名；
   * @return
   */
  public final String getUserName(){
    return username;
  }
  
  /**
   * 返回数据库的ip地址或机器名，如果无法获取数据库的地址或机器名，那么返回值可能是null
   * 返回值不会是127.0.0.1或localhost，本地地址会转换为外部地址
   */
  public final String getDatabaseAddress() {
    return databaseAddress;
  }
  public final boolean isOracle() {
    return dbtype == SqlConst.DB_TYPE_ORACLE;
  }
  public final boolean isOracle8i(){
    return dbtype == SqlConst.DB_TYPE_ORACLE && databaseMajorVersion==8;
  }

  public final boolean isDb2() {
    return dbtype == SqlConst.DB_TYPE_DB2;
  }
  public final boolean isDb29() {
    return dbtype == SqlConst.DB_TYPE_DB2 && databaseMajorVersion>=9;
  }
  
  public final boolean isDB2ForAS400(){
	  return dbtype == SqlConst.DB_TYPE_DB2 
	  && databaseProductName!=null && databaseProductName.indexOf("AS")==0;
  }
  public final boolean isMysql() {
    return dbtype == SqlConst.DB_TYPE_MYSQL;
  }

  public final boolean isMssql() {
    return dbtype == SqlConst.DB_TYPE_MSSQL;
  }
  public final boolean isMssql2005() {
    if(isMssql()&&databaseMajorVersion>8){
      return true;
    }
    return false;
  }
  public final boolean isSybase() {
    return dbtype == SqlConst.DB_TYPE_SYBASE;
  }

  public final boolean isSybaseIQ() {
    return dbtype == SqlConst.DB_TYPE_SYBASE_IQ;
  }
  public final boolean isOscar(){
    return dbtype == SqlConst.DB_TYPE_OSCAR;
  }
  public final boolean isTimesTen(){
    return dbtype == SqlConst.DB_TYPE_TIMESTEN;
  }
  public final boolean isKingBaseES(){
    return dbtype == SqlConst.DB_TYPE_KINGBASE_ES;
  }
  
  public final boolean isDM_DBMS() {
	return dbtype == SqlConst.DB_TYPE_DM;
  }
	
  public final boolean isDM7() {
    if (isDM_DBMS() && databaseMajorVersion >= 7) {
      return true;
    }
    return false;
  }
  
  public final boolean isEssbase(){
	return dbtype == SqlConst.DB_TYPE_ESSBASE;
  }
  
  public final boolean isGBase(){
  	return dbtype == SqlConst.DB_TYPE_GBASE;
  }
  
  public final boolean isGBase8T(){
	return dbtype == SqlConst.DB_TYPE_GBASE_8T;
  }
  
  public final boolean isNetezza(){
  	return dbtype == SqlConst.DB_TYPE_NETEZZA;
  }
  
   public final boolean isGreenplum(){
  	return dbtype == SqlConst.DB_TYPE_GREENPLUM;
  }
  
  /**
   * 判断是否是 Teradata 数据库
   * 
   * @return 是 Teradata 数据库，返回  True；否则，返回  False
   */
  public final boolean isTeradata(){
	  return dbtype == SqlConst.DB_TYPE_TERADATA;
  }
  
  /**
   * 判断是否是 PeteBase 数据库
   * 该分支不支持 PetaBase 数据库，应该直接返回false
   * 为避免不同分支代码冲突，使用统一形式的代码。
   * 
   * @return
   */
  public final boolean isPetaBase(){
	  return dbtype == SqlConst.DB_TYPE_PETABASE;
  }
  
  /**
   * 判断是否是 Vertica 数据库
   * 
   * @return 是 Vertica 数据库，返回  True；否则，返回  False
   */
  public final boolean isVertica(){
	  return dbtype == SqlConst.DB_TYPE_VERTICA;
  }
  
  /**
   * 表名，字段名是否区分大小写；
   * @return
   */
  public boolean isFieldCaseSensitive(){
    return isSybase() || isMssql();
  }
  
  public final int getDbtype() {
    return dbtype;
  }
  public final String getDbName() {
    switch (dbtype) {
      case SqlConst.DB_TYPE_ORACLE:
        if (databaseMajorVersion > 8)
          return SqlConst.DB_ORACLE;
        else
          return SqlConst.DB_ORACLE8i;
      case SqlConst.DB_TYPE_DB2:
        return SqlConst.DB_DB2;
      case SqlConst.DB_TYPE_MYSQL:
        return SqlConst.DB_MYSQL;
      case SqlConst.DB_TYPE_MSSQL:
        return SqlConst.DB_MSSQL;
      case SqlConst.DB_TYPE_SYBASE:
        return SqlConst.DB_SYBASE;
      case SqlConst.DB_TYPE_SYBASE_IQ:
        return SqlConst.DB_SYBASE_IQ;
      case SqlConst.DB_TYPE_OSCAR:
        return SqlConst.DB_OSCAR;
      case SqlConst.DB_TYPE_TIMESTEN:
        return SqlConst.DB_TIMESTEN;
      case SqlConst.DB_TYPE_DM:
		return SqlConst.DB_DM;
      case SqlConst.DB_TYPE_ESSBASE:
    	  return SqlConst.DB_ESSBASE;
      case SqlConst.DB_TYPE_NETEZZA:
    	  return SqlConst.DB_NETEZZA;  
      case SqlConst.DB_TYPE_GBASE:
    	  return SqlConst.DB_GBASE; 
      case SqlConst.DB_TYPE_GBASE_8T:
    	  return SqlConst.DB_GBASE_8T; 
      default:
        return SqlConst.DB_OTHER;
    }
  }
  public final int getDatabaseMajorVersion() {
    return databaseMajorVersion;
  }

  public final int getDatabaseMinorVersion() {
    return databaseMinorVersion;
  }

  public final String getDatabaseProductVersion() {
    return databaseProductVersion;
  }

  public final String getDatabaseProductName() {
    return databaseProductName;
  }
  public final int getMaxColumnsInTable(){
    return maxColumnsInTable;
  }
  /**
   * 如果是oracle的话，此函数返回oracle对于的编码， 如：
   * US7ASCII  ZHS16GBK……
   * 其它数据库返回null
   */
  public final String getCharacterEncoding() {
    return characterEncoding;
  }
  /**
   * 为获取链接前提供检查链接正确性的sql语句；
   * @return
   */
  public String getCheckSQL(){
    return testSql;
  }
  
  /**
   * 需要检查的子类继承
   */
  public String check(){
    return null;
  }
  
  /**
   * 判断数据库对空字符串的处理, 是否同NULL值等价
   * 默认我们认为等价
   * @return
   */
  public boolean isEmptyStringEqualsNull() {
  	return true;
  }
  
  /**
   * {@inheritDoc}
   */
  public int getMaxRowsInTrans() {
  	return -1;
  }
}
