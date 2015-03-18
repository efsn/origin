package com.esen.jdbc.dialect.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.pool.PooledSQLException;
import com.esen.util.ArrayFunc;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * 数据定义接口抽象类
 * <p>Title: Esensoft JDBC</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Esensoft</p>
 * @author dw
 * @version 1.0
 */
public abstract class DbDef
    implements DbDefiner {
  private static final String[] TYPE_TABLE_VIEW = new String[]{"TABLE","VIEW", "SYNONYM"};
  protected static final String[] TYPE_VIEW = new String[]{"VIEW"};
  protected static final String[] TYPE_TABLE = new String[]{"TABLE"};
  protected Dialect dl;
  protected DataBaseInfo dbinf;
  protected ArrayList _indexNames;
  
  /**
   * 不同字节数的整型的长度限制
   */
  protected static int LEN_INT1 = 2;
  protected static int LEN_INT2 = 4;
  protected static int LEN_INT3 = 2;
  protected static int LEN_INT4 = 4;
  protected static int LEN_INT8 = 2;
  
  /**
   * 用于表结构定义的工具类；
   */
  protected TableMetaDataHelper tbMetaHelper;
  
  /**
   * JDBC_CREATE_MULTITHREAD是一个jdbc系统表，只有一个主键字段，存放表名；
   * 用于多线程调用自动更名创建表；
   * 原理：建表前获取的一个数据库不存在且的表名，且JDBC_CREATE_MULTITHREAD中不存在的表名，然后写入JDBC_CREATE_MULTITHREAD；
   * 创建完成后，从JDBC_CREATE_MULTITHREAD删除这个表名；
   */
  public static final String TABLENAME_CREATE_MULTITHREAD = "JDBC_MULTITHREAD";
  
  public DbDef(Dialect dl) {
    this.dl = dl;
    this.dbinf = dl.getDataBaseInfo();
    this.tbMetaHelper = new TableMetaDataHelper();
  }
  
  public void defineField(TableColumnMetaData col){
    defineField(col.getName(),SqlFunc.getSubsectionType(col.getType()),col.getLen(),col.getScale(),
        col.getDefaultValue(),col.isNullable(),col.isUnique(),col.getDesc());
  }
  
  public String formatFieldName(String fdname, boolean throwsException) {
    if (SqlFunc.isValidSymbol(fdname)) {
      if (fdname.length() > getMaxColumnLength()) {
        if (throwsException)
//          throw new RuntimeException("字段名：" + fdname + "太长；");
        	throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.dbdef.field2long", "字段名：{0}太长；", new Object[]{fdname}));
        else {
          //截取
          fdname = fdname.substring(0, getMaxColumnLength());
        }
      }
    }
    else {
      if (throwsException)
        //throw new RuntimeException("字段名：" + fdname + "不合法；");
    	  throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.dbdef.nameinvaild", "字段名：{0}不合法；", new Object[]{fdname}));
      else {
        fdname = formatName(fdname);
      }
    }
    //对关键字处理 这里不做处理，关键字对用户应该是透明的，内部处理；
    //fdname = SqlFunc.getColumnName(dl, fdname);
    //是否需要转大写
    return formatUpcaseFieldName(fdname);
  }
  protected int getMaxColumnLength() {
    return dbinf.getMaxColumnNameLength();
  }
  protected String formatUpcaseFieldName(String fdname) {
    return fdname;
  }
  /**
   * 去除非法字符；
   * @param fdname
   * @return
   */
  protected String formatName(String fdname) {
    StringBuffer fdstr = new StringBuffer(fdname.length());
    /**
     * 如果有'.'号,则表示带有前缀，这里取后面的字符串；
     * 此方法在格式化字段、索引名、表名时调用，只有表名可能有schema前缀；
     * 去掉前缀，原因是可能数据库中没有这个schema，特别是在恢复数据库时；
     */
    int indx = fdname.lastIndexOf('.');
    int start = 0;
    if(indx>0){
    	start = indx+1;
    }
    for(int i=start;i<fdname.length();i++){
      char c = fdname.charAt(i);
      if(i==start){
        if(checkFirstChar(c))
          fdstr.append(c);
        else fdstr.append('F');
      }else if(checkChar(c)){
        fdstr.append(c);
      }
    }
    return fdstr.toString();
  }
  
  /**
   * BI-5008 20110620
   * 判断表名字符，是否复合表名规则；
   * 增加'.'以免将带schema名的表名中的'.'忽略；
   * @param c
   * @return
   */
  private boolean checkChar(char c) {
    if(((c>='a') && (c<='z'))
        || (c>='A' && c<='Z')
        || (c>='0' && c<='9')
        || c=='_'||c=='$'||c=='.'){
      return true;
    }
    return false;
  }
  private boolean checkFirstChar(char c) {
    if(((c>='a') && (c<='z'))
        || (c>='A' && c<='Z')){
      return true;
    }
    return false;
  }
  protected char getFieldType(int sqlType){
    switch (sqlType) {
      case Types.INTEGER:
      case Types.SMALLINT:
      case Types.TINYINT:
      case Types.BIGINT:
        return DbDefiner.FIELD_TYPE_INT;
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.BIT:
      case Types.REAL:
      case Types.DECIMAL:
        return DbDefiner.FIELD_TYPE_FLOAT;
      case Types.VARCHAR:
      case Types.CHAR:
        return DbDefiner.FIELD_TYPE_STR;
      case Types.TIMESTAMP:
        return DbDefiner.FIELD_TYPE_TIMESTAMP;
      case Types.TIME:
        return DbDefiner.FIELD_TYPE_TIME;
      case Types.DATE:
        return DbDefiner.FIELD_TYPE_DATE;
      case Types.BLOB:
      case Types.VARBINARY:
      case Types.BINARY:
      case Types.LONGVARBINARY://sybase
        return DbDefiner.FIELD_TYPE_BINARY;
      case Types.LONGVARCHAR: //DB2,Mssql,Oracle
      case Types.CLOB:        //Mysql,Sybase
        return DbDefiner.FIELD_TYPE_CLOB;
      case Types.BOOLEAN:
        return DbDefiner.FIELD_TYPE_LOGIC;
      case Types.ARRAY:
      case Types.DATALINK:
      case Types.JAVA_OBJECT:
      case Types.NULL:
      case Types.OTHER:
      case Types.REF:
      case Types.STRUCT:
      default:
        return 0;
    }
  }
  public int getSqlType(char ft){
    switch(ft){
      case FIELD_TYPE_INT:
      case FIELD_TYPE_LOGIC:
        return Types.INTEGER;
      case FIELD_TYPE_STR:{
        return Types.VARCHAR;
      }
      case FIELD_TYPE_FLOAT:{
        return Types.DOUBLE;
      }
      case FIELD_TYPE_DATE:
        return Types.DATE;
      case FIELD_TYPE_TIME:
      case FIELD_TYPE_TIMESTAMP:
        return Types.TIMESTAMP;
      case FIELD_TYPE_CLOB:
      case FIELD_TYPE_memo:
        return Types.LONGVARCHAR;
      case FIELD_TYPE_BINARY:
        return Types.BLOB;
      default: return 0;
    }
  }
  /**
   * 创建 Catalog
   * @param conn Connection
   * @param catalog String
   * @throws Exception
   */
  public void createCatalog(Connection conn, String catalog) throws SQLException {
    if (catalog == null || catalog.equals("")) {
      return;
    }
    Statement ddl = conn.createStatement();
    try {
      ddl.executeUpdate("CREATE DATABASE " + catalog);
    }
    finally {
      ddl.close();
    }
  }
  public String[] getIndexNames(){
    if(_indexNames==null||_indexNames.size()==0)
      return null;
    String[] inds = new String[_indexNames.size()];
    _indexNames.toArray(inds);
    return inds;
  }
  /**
   * 创建表
   * @param conn Connection
   * @param catalog String
   * @param tblname String
   * @throws Exception
   */
  public String createTable(Connection conn, String catalog, String tblname) throws SQLException {
    return createTable(conn, tblname, true);
  }
  public String createTable(Connection conn, String tblname,boolean ifTableNameExistThrowException) throws SQLException {
    return createTable(conn, tblname, ifTableNameExistThrowException,false);
  }
  
  public String createTable(Connection conn, String tblname,boolean ifTableNameExistThrowException,boolean checkIndexLength) throws SQLException {
	  if (!dl.supportCreateTableInTransaction() && !conn.getAutoCommit()) {
		  try {
			  conn.commit();
			  conn.setAutoCommit(true);
			  return createTable(conn,null, tblname, false,ifTableNameExistThrowException,checkIndexLength);
		  } finally {
			  conn.setAutoCommit(false);
		  }
	  }
    return createTable(conn,null, tblname, false,ifTableNameExistThrowException,checkIndexLength);
  }
  
  private EasyDbDefiner getEasyDbDefiner(){
    return new EasyDbDefiner();
  }
  public String repairTable(Connection conn,Document xml) throws SQLException{
    return repairTable(conn,xml,null,false);
  }
  public String repairTable(Connection conn,Document xml, String tablename, boolean autoAdjustFieldLength) throws SQLException{
	  if (!dl.supportCreateTableInTransaction() && !conn.getAutoCommit()) {
		  try {
			  conn.commit();
			  conn.setAutoCommit(true);
			  return getEasyDbDefiner().repairTable(conn,xml,tablename,autoAdjustFieldLength);
		  } finally {
			  conn.setAutoCommit(false);
		  }
	  }	    
    return getEasyDbDefiner().repairTable(conn,xml,tablename,autoAdjustFieldLength);
  }
  
  public String createTable(Connection conn, Document xml, String tableName, boolean autoChangeTableName,
      boolean autoAdjustFieldLength) throws SQLException{
    return getEasyDbDefiner().createTable(conn,xml,tableName,autoChangeTableName,autoAdjustFieldLength);
  }
  public Document getTableMetaData(Connection con ,String tbname) throws SQLException{
    Dialect dl2 = dl;
    if(con!=null){
      dl2 = SqlFunc.createDialect(con);
    }
    return getEasyDbDefiner().getTableMetaData(dl2, tbname);
  }

  /**
   * 创建临时表
   * @param conn Connection
   * @param catalog String
   * @param tablename String
   * @throws Exception
   */
  public void createTempTable(Connection conn, String catalog, String tablename) throws
  SQLException {
    createTable(conn, catalog, tablename, true,true,false);
  }

  protected String getCreateTableByQueryStr(String tablename,boolean istemp) {
    if(istemp)
      return "CREATE TEMPORARY TABLE "+ tablename;
    return "CREATE TABLE "+ tablename;
  }
  public boolean tableOrViewExists(Connection conn,String tvname)throws SQLException{
    /**
     * 20090616
     * 广义的判断tvname是否存在，包括是否是表名，视图名，同义词等；
     * 这里传空更好；
     */
    return objectExists(conn, tvname, null);
  }
  /**
   * 删除表
   * @param conn Connection
   * @param catalog String
   * @param table String
   * @throws Exception
   */
  public void dropTable(Connection conn, String catalog, String table) throws
  SQLException {
    if (catalog != null) {
      conn.setCatalog(catalog);
    }
    
    /* BUG:ESENFACE-1081: modify by liujin 2014.07.21
     * 事务内不能执行 drop table 语句
     */
    boolean isAutoCommit = conn.getAutoCommit();
    if (!dl.supportCreateTableInTransaction() && !isAutoCommit) {
    	conn.commit();
    	conn.setAutoCommit(true);
    }

    Statement ddl = conn.createStatement();
    /**
     * BI-5061
     * 对于某些特殊情况下创建的表，表名包含空格，这时删除该表，表名必需加引号；
     */
    if(table.indexOf(" ")>=0){
    	table = "\""+table+"\"";
    }
    try {
    	ddl.executeUpdate("DROP TABLE " + table);
    }catch(SQLException se){
    	/**
    	 * 多线程调用删除同一个表时，会出现删除的表已经不存在的情况，即使实现判断表是否存在也没用；
    	 * 这里使用异常机制来处理，如果出新异常，则表示这个表已经被删除，直接返回，不抛出异常；
    	 */
    	return;
    }
    finally {
      ddl.close();
      
      if (!dl.supportCreateTableInTransaction() && !isAutoCommit) {
      	conn.setAutoCommit(false);
      }
   }    
  }

  /**
   * 删除临时表
   * @param conn Connection
   * @param catalog String
   * @param table String
   * @throws Exception
   */
  public void dropTempTable(Connection conn, String catalog, String table) throws
  SQLException {
    /* 
     * BUG:ESENFACE-1081: modify by liujin 2014.07.21
     * 事务内不能执行 drop table 语句
     */
    boolean isAutoCommit = conn.getAutoCommit();
    if (!dl.supportCreateTableInTransaction() && !isAutoCommit) {
    	conn.commit();
    	conn.setAutoCommit(true);
    }

    Statement ddl = conn.createStatement();
    try {
      ddl.executeUpdate("DROP TEMPORARY TABLE " + table);
    }catch(SQLException se){
    	/**
    	 * 多线程调用删除同一个表时，会出现删除的表已经不存在的情况，即使实现判断表是否存在也没用；
    	 * 这里使用异常机制来处理，如果出新异常，则表示这个表已经被删除，直接返回，不抛出异常；
    	 */
    	return;
    }
    finally {
      ddl.close();
      
      if (!dl.supportCreateTableInTransaction() && !isAutoCommit) {
      	conn.setAutoCommit(false);
      }
    }
  }

  /**
   * 删除 Catalog
   * @param conn Connection
   * @param catalog String
   * @throws Exception
   */
  public void dropCatalog(Connection conn, String catalog) throws Exception {
    Statement ddl = conn.createStatement();
    try {
      ddl.executeUpdate("DROP DATABASE " + catalog);
    }
    finally {
      ddl.close();
    }
  }

  /**
   * 更改表名
   * @param conn Connection
   * @param oldname String
   * @param newname String
   * @throws Exception
   */
  public abstract void renameTable(Connection conn, String oldname,
                                   String newname) throws SQLException;

  /**
   * 定义自动增长字段
   * 每张表只有一个自增长字段，如果重复定义，只取第一次定义的值；
   * @param thisField String
   * @param gap int
   */
  public void defineAutoIncField(String thisField, int gap) {
    defineAutoIncField(thisField, gap, null);
  }
  
  /**
   * 定义自动增长字段
   * 每张表只有一个自增长字段，如果重复定义，只取第一次定义的值；
   * @param thisField String
   * @param gap int
   */
  public void defineAutoIncField(String thisField, int gap, String desc) {
    for(int i=0;i<tbMetaHelper.getColumnCount();i++){
      TableColumnMetaData fi = tbMetaHelper.getColumn(i);
      if(fi.isAutoInc()){
        return;
      }
    }

    TableColumnMetaDataHelper fi = new TableColumnMetaDataHelper(thisField, 'I', desc);
    tbMetaHelper.addColumn(fi);
  }

  /**
   * 定义字段属性
   * @param thisField String
   * @param FieldType char
   * @param len int
   * @param dec int
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   */
  public void defineField(String thisField, char FieldType, int len, int dec,
                          String defaultvalue, boolean nullable, boolean unique,String desc) {
	/**
	 * BI-5553 定义字段，如果字段是关键字，应该在生产sql时转换；
	 * 以前的代码在这里直接转换，会导致根据字段名找定义的字段对象，可能找不到；
	 */
    TableColumnMetaDataHelper fi = new TableColumnMetaDataHelper(thisField, FieldType, len, dec,
                                 nullable, unique, defaultvalue,desc);
    tbMetaHelper.addColumn(fi);
  }
  public void defineField(String thisField, char FieldType, int len, int dec,
      String defaultvalue, boolean nullable, boolean unique) {
    defineField(thisField,FieldType,len,dec,defaultvalue,nullable,unique,null);
  }
  /**
   * 如果字段名是数据库关键字，则用引号括起来,但不能改变改字段的值；
   * @param cname
   * @return
   */
  protected String getColumnName(String cname) {
    return getColumnName(dl,cname);
  }
  protected String getColumnName(Dialect dl, String cname) {
    if(cname==null||cname.length()==0) return cname;
    return SqlFunc.getColumnName(dl,formatUpcaseFieldName(cname));
  }
  public void defineStringField(String thisField, int len, String defaultvalue,
                                boolean nullable, boolean unique) {
    defineField(thisField, FIELD_TYPE_STR, len, 0, defaultvalue, nullable,
                unique);
  }

  public void defineIntField(String thisField, int len, String defaultvalue,
                             boolean nullable, boolean unique) {
    defineField(thisField, FIELD_TYPE_INT, len, 0, defaultvalue, nullable,
                unique);
  }

  public void defineDateField(String thisField, String defaultvalue,
                              boolean nullable, boolean unique) {
    defineField(thisField, FIELD_TYPE_DATE, 0, 0, defaultvalue, nullable,
                unique);
  }

  public void defineClobField(String thisField, String defaultvalue,
                              boolean nullable, boolean unique) {
    defineField(thisField, FIELD_TYPE_CLOB, 0, 0, defaultvalue, nullable,
                unique);
  }

  public void defineMemoField(String thisField, String defaultvalue,
                              boolean nullable, boolean unique) {
    defineField(thisField, FIELD_TYPE_memo, 0, 0, defaultvalue, nullable,
                unique);
  }

  public void defineFloatField(String thisField, int len, int dec,
                               String defaultvalue,
                               boolean nullable, boolean unique) {
    defineField(thisField, FIELD_TYPE_FLOAT, len, dec, defaultvalue, nullable,
                unique);
  }

  public void defineLogicField(String thisField, String defaultvalue,
                               boolean nullable, boolean unique) {
    defineField(thisField, FIELD_TYPE_LOGIC, 1, 0, defaultvalue, nullable,
                unique);
  }

  public void defineBlobField(String thisField, String defaultvalue,
                              boolean nullable, boolean unique) {
    defineField(thisField, FIELD_TYPE_BINARY, 0, 0, defaultvalue, nullable,
                unique);
  }

  public void defineTimeField(String thisField, String defaultvalue,
                              boolean nullable, boolean unique) {
    defineField(thisField, FIELD_TYPE_TIME, 0, 0, defaultvalue, nullable,
                unique);
  }

  public void defineTimeStampField(String thisField, String defaultvalue,
                                   boolean nullable, boolean unique) {
    defineField(thisField, FIELD_TYPE_TIMESTAMP, 0, 0, defaultvalue, nullable,
                unique);
  }
  
  public boolean haveField(String field){
    TableColumnMetaData fi = tbMetaHelper.getColumn(field);
    return fi!=null;
  }
  
  /**
   * 定义主键
   * primary key (fldname1,fldname2,...)
   * @param fieldNames String
   *        如果是多个字段，则是字段以','分割的字符串；
   */
  public void definePrimaryKey(String fieldNames) {
    String[] _pkeys = fieldNames.split(",");
    tbMetaHelper.setPrimaryKey(_pkeys);
  }


  public void defineIndex(String indexName, String expression, boolean unique) {
    TableIndexMetaData ii = new TableIndexMetaDataImpl(indexName, expression, unique);
    tbMetaHelper.addIndexMeta(ii);
  }

  /**
   * 清空上一次数据定义
   */
  public void clearDefineInfo() {
    tbMetaHelper.clear();
    if(_indexNames!=null)
      _indexNames.clear();
  }

  /**
   * 判断字段是否存在
   * @param conn Connection
   * @param tablename String
   * @param colname String
   * @throws Exception
   * @return boolean
   */
  public boolean colExists(Connection conn, String tablename, String colname) throws
  SQLException {
    String sql = "SELECT * FROM " + tablename + " WHERE 1=0";
    //考虑到数据量很大的情况下，会降低效率和消耗内存，所以增加where语句使得rs无实际的数据集
    Statement stmt = conn.createStatement();
    try {
	    ResultSet rs = stmt.executeQuery(sql);	    
	    try {
	    	ResultSetMetaData rmeta = rs.getMetaData();
	      for (int i = 0; i < rmeta.getColumnCount(); i++) {
	        if (rmeta.getColumnName(i + 1).equalsIgnoreCase(colname)) {
	          return true;
	        }
	      }
	      return false;
	    } finally {
	    	rs.close();
	    }
    } finally {
      stmt.close();
    }
  }

  /**
   * 判断表是否存在
   * @param conn Connection
   * @param catalog String
   * @param tablename String
   * @throws Exception
   * @return boolean
   */
  public boolean tableExists(Connection conn, String catalog, String tablename) throws
  SQLException {
    return objectExists(conn,tablename,TYPE_TABLE);
  }

  /**
   * 判断执行sql是否有结果集；
   * @param conn
   * @param sql
   * @return
   * @throws SQLException
   */
  protected boolean excuteSql(Connection conn, String sql) throws SQLException {
    Statement stmt = conn.createStatement();
    try {
      ResultSet rs = stmt.executeQuery(sql);
      try {
        return rs.next();
      }
      finally {
        if (rs != null) {
          rs.close();
        }
      }
    }
    finally {
      if (stmt != null) {
        stmt.close();
      }
    }
  }

  /**
   * 返回一个数据库中不存在的表名；
   * 如果表名：prefix_suffix 缓存中不存在且数据库中不存在，则加入缓存并返回该值；
   * 反之，则换一个名字prefix(1,2,3....)_suffix再重新检测；
   * 如果suffix为空,去掉"_suffix";
   * @param prefix String 不能为空
   * @param suffix String 可以为空
   * @return String "prefix_suffix"
   */
  public String getCreateTableName(Connection conn, String prefix,
                                   String suffix) throws SQLException {
    String tablename = null;
    String temp = "";
    if (suffix != null && suffix.length() > 0) {
      temp = "_" + suffix;
    }
    if(prefix==null||prefix.length()==0){
//      throw new SQLException("参数prefix不能为空；");
    	 throw new SQLException(I18N.getString("com.esen.jdbc.dialect.impl.dbdef.prefixnoutnull", "参数prefix不能为空；"));
    }
    tablename = formatTableName(prefix + temp);
    int i = 1;
    /**
     * 20090512
     * 创建表时，如果需要更名，即：如果指定的表存在，则更换一个新名字，使创建成功；
     * 这里以前的代码只判断了指定的表名是否是物理表，没有判断是否是视图名，
     * 如果指定的表名是个视图名，这里会认为这个物理表不存在，创建就会报错：ORA-00955: 名称已由现有对象使用；
     * 解决办法：这里改为对表名和视图名都判断，如果是存在的视图，也要更名；
     */
    while (tableOrViewExists(conn, tablename)) {
      tablename = formatTableName(prefix + i + temp);
      i++;
    }
    return tablename;
  }
  protected String formatTableName(String tbname) {
    tbname = formatName(tbname);
    int maxlen = getMaxTableLength();
    int len = tbname.length();
    if(len>maxlen){
      return "T" + tbname.substring(len - maxlen + 1, len);
    }
    /**
     * 如果连接池指定了默认schema，则创建表都要在此schema下；
     */
    String defSchema = this.dbinf.getDefaultSchema();
    if(StrFunc.isNull(defSchema)){
    	return tbname;
    }else{
    	return defSchema+"."+tbname;
    }
  }
  public int getMaxTableLength() {
    return dbinf.getMaxTableNameLength();
  }
  public void createView(Connection conn,String viewname,String columns,String sql) throws SQLException{
    StringBuffer vsql = new StringBuffer(256);
    vsql.append("create view ").append(viewname);
    if(columns!=null&&columns.length()>0)
      vsql.append(" (").append(columns).append(")");
    vsql.append(" as ");
    vsql.append(sql);
    Statement stat = conn.createStatement();
    try{
       stat.execute(vsql.toString());
    }finally{
      if(stat!=null)
        stat.close();
    }
  }

  	/*
  	 * 创建视图，带列注释
  	 */
	public void createView(Connection conn, String viewname, String[] columns, String[] colComments, String sql) throws SQLException{
	    if (columns == null || columns.length <= 0) {
	    	createView(conn, viewname, null, sql);
	    	return;
	    }

		StringBuffer colstr = new StringBuffer(256);
    	colstr.append(columns[0]);
    	for (int i = 1; i < columns.length; i++){
    		colstr.append(", ").append(columns[i]);
    	}
	
    	if (this.dl.supportsViewColumnComment() == false) {
    		createView(conn, viewname, colstr.toString(), sql);
	    	return;
    	}

	    boolean commFlag = false;
	    if (colComments != null && colComments.length > 0) {
	    	if (colComments.length != columns.length)
	    		//throw new SQLException("视图列数和列上注释个数应一致");
	    		throw new SQLException(I18N.getString("com.esen.jdbc.dialect.impl.dbdef.columnsunmatch", "视图列数和注释个数应一致；"));
	    	else
	    		commFlag = true;
	    }

	    createView(conn, viewname, colstr.toString(), sql);

	    if (commFlag == true) {
	    	Statement stat = conn.createStatement();
	    	try {
			    for (int i = 0; i < columns.length; i++) {
			    	String commentSql = getFieldCommentSql(conn, viewname, columns[i], colComments[i]);
			    	if (StrFunc.isNull(commentSql) == false)
			    		stat.execute(commentSql);
			    }
			} catch (SQLException e) {
				dropView(conn, viewname);
				throw e;
			}
	    	finally {
	    		if (stat != null)
	    			stat.close();
	    	}
	    }
	}

	/**
	 * 目前oracle, db2, KingbaseES, DM 需要重载这个方法。
	 * 返回视图添加列注释的SQL语句
	 */
	protected String getFieldCommentSql(Connection conn, String viewname, String colname, String comment) {
		return "";
	}

  public void dropView(Connection conn,String viewname) throws SQLException{
    Statement stat = conn.createStatement();
    try{
       stat.execute("drop view "+viewname);
    }catch(SQLException se){
    	/**
    	 * 多线程调用删除同一个表时，会出现删除的表已经不存在的情况，即使实现判断表是否存在也没用；
    	 * 这里使用异常机制来处理，如果出新异常，则表示这个表已经被删除，直接返回，不抛出异常；
    	 */
    	return;
    }finally{
      if(stat!=null)
        stat.close();
    }
  }
  public boolean viewExists(Connection conn,String viewname) throws SQLException{
    return objectExists(conn,viewname,TYPE_VIEW);
  }
  
  public boolean viewValidate(Connection conn,String viewname)throws SQLException{
	  Statement stat = conn.createStatement();
	  try{
		  stat.execute("select * from "+viewname+" where 1>2");
		  return true;
	  }catch(SQLException se){
		  return false;
	  }finally{
		  stat.close();
	  }
  }
  /**
   * sqlserver的实现重载此方法；
   * 20090616
   * 更名为objectExists, 如果不更名，第三个参数传null，将会和tableExists混淆；
   */
  protected  boolean objectExists(Connection conn, String tablename, String[] types)
      throws SQLException{
    if(tablename==null||tablename.length()==0)
//      throw new SQLException("不在正确的参数："+tablename+"不能为空；");
    	 throw new SQLException(I18N.getString("com.esen.jdbc.dialect.impl.dbdef.argsnotnull", "不在正确的参数：{0}不能为空；", new Object[]{tablename}));
    DatabaseMetaData _dmd = conn.getMetaData();
    /**
     * 表名可能带有schema前缀，判断表名是否存在需要处理；
     * 带前缀，则在指定的schema中找；
     * 不带前缀，则在默认schema中找；
     */
    String[] tbs = getTableNameForDefaultSchema(tablename,dbinf);
    return objectExists(_dmd,tbs[0],tbs[1],types);
  }
  
  /**
   * 如果表名tbname带有schema前缀，返回一个数组，第一个值为schema,第二个值为去掉前缀的表名；
   * 如果表名tbname不带schema前缀，返回数组，第一个值为空，第二个值为tbname
   * @param tbname
   * @return
   */
  public static String[] getTableNameForSchema(String tbname){
	  tbname = tbname.trim();
	  int indx = tbname.lastIndexOf('.');
	  String[] tbs = new String[2];
	  if(indx<0){
		  tbs[1] = tbname;
	  }else{
		  tbs[0] = tbname.substring(0,indx);
		  tbs[1] = tbname.substring(indx+1);
	  }
	  return tbs;
  }
  
  /**
   * 返回一个数组，第一个值是schema,第二个值是去掉schema前缀的表名
   * 如果表名tbname不带前缀，则数组的第一个值为默认schema
   * @param tbname
   * @param dbinfo
   * @return
   */
  public static String[] getTableNameForDefaultSchema(String tbname,DataBaseInfo dbinfo){
	  String[] tbs = getTableNameForSchema(tbname);
	  if(StrFunc.isNull(tbs[0])){
		  tbs[0] = getTableSchema(null,dbinfo);
	  }
	  if(dbinfo.isDb2()||dbinfo.isOracle() || dbinfo.isDM_DBMS()){
		  tbs[1] = tbs[1].toUpperCase();
	  }
	  if(dbinfo.getDbtype() == SqlConst.DB_TYPE_GREENPLUM
			  || dbinfo.getDbtype() == SqlConst.DB_TYPE_GBASE_8T){
		  tbs[1] = tbs[1].toLowerCase();
	  }
	  return tbs;
  }
  
  /**
   * 获取表名的schema
   * 如果schema为空，则返回默认schema
   * @param schema 表名的schema
   * @param dbinfo
   * @return
   */
  public static String getTableSchema(String schema,DataBaseInfo dbinfo){
	  String schema2 = schema;
	  if(StrFunc.isNull(schema)){
		  schema2 = dbinfo.getDefaultSchema();
	  }
	  if(StrFunc.isNull(schema2)){
		  return null;
	  }
	  if(dbinfo.isDb2()||dbinfo.isOracle() || dbinfo.isDM_DBMS()){
		  return schema2.toUpperCase();
	  }
	  return schema2;
  }
  
  protected boolean objectExists(DatabaseMetaData _dmd, String schema, String objectname, String[] types)
      throws SQLException {
    ResultSet _rs = _dmd.getTables(null, schema, objectname, types);
    try {
      while (_rs.next()) {
        //String schamaName = _rs.getString("TABLE_SCHEM");
        String vname = _rs.getString("TABLE_NAME");
        if (vname.equalsIgnoreCase(objectname)) {
          return true;
        }
      }
    }
    finally {
      /**
       * 20090114 dw
       * 调用了DatabaseMetaData的系统函数：getTables,getIndexInfo,getColumns,getPrimaryKeys等，
       * 返回的RerultSet对象都必须显示调用close关闭；否则出现ORA-01000：超出最多允许打开的游标数 异常；
       */
      if(_rs!=null)
        _rs.close();
    }
    return false;
  }
  /*
   * 通过查询是否异常判断表或者视图是否存在，不存在出异常，效率不好；
   */
  /*protected boolean tableExists(Connection conn, String tablename) throws SQLException{
    String sql = "select * from "+tablename +" where 1>2";
    Statement stat = conn.createStatement();
    try{
      stat.executeQuery(sql);
      return true;
    }catch(SQLException se){
      return false;
    }finally{
      if(stat!=null)
        stat.close();
    }
  }*/

  /**
   * 判断索引名是否存在
   * @param conn Connection
   * @param indexname String
   * @throws Exception
   * @return boolean
   */
  public abstract boolean indexExists(Connection conn, String tablename, String indexname) throws
  SQLException;
  
	/**
	 * {@inheritDoc}
	 */
	public boolean indexExists(Connection conn, String tablename,
			String[] fields) throws SQLException {
		if (fields == null || fields.length <= 0) {
			return false;
		}

		DbMetaData dmd = dl.createDbMetaData(conn);
		if (dmd == null) {
			return false;
		}

		TableMetaData tmd = dmd.getTableMetaData(tablename);
		if (tmd == null) {
			return false;
		}

		TableIndexMetaData[] inds = tmd.getIndexes();
		if (inds == null || inds.length <= 0) {
			return false;
		}

		HashMap map = new HashMap();
		for (int m = 0; m < fields.length; m++) {
			map.put(fields[m], fields[m]);
		}

		for (int i = 0; i < inds.length; i++) {
			String[] indexFields = inds[i].getColumns();
			if (indexFields == null || indexFields.length != map.size()) {
				continue;
			}

	  		int j = 0;
	  		int length = indexFields.length;
	  		for (j = 0; indexFields != null && j < length; j++) {
	  			if (!map.containsKey(indexFields[j])) {
	  				break;
	  			}
	    		if (j == length - 1) {
	    			return true;
	    		}	
	  		}
		}

		return false;
	}
  
  public String createIndex(Connection conn,String tablename,String indexname, 
      String[] fields,boolean unique) throws SQLException{
    return createIndex(conn,tablename,indexname,fields,unique,false);
  }

  /**
   * 增加一个字段
   * @param conn Connection
   * @param tablename String
   * @param newcol String
   * @param coltype char
   * @param len int
   * @param dec int
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @throws Exception
   */
  public void addColumn(Connection conn, String tablename, String newcol,
                        char coltype, int len, int dec, String defaultvalue,
                        boolean nullable, boolean unique) throws
                        SQLException {
    StringBuffer ddl = new StringBuffer(64).append("ALTER TABLE ");
    ddl.append(tablename);
    ddl.append(" ADD ");
    ddl.append(getFldDdl(new TableColumnMetaDataHelper(newcol, coltype, len, dec,
                                       nullable, unique,defaultvalue,null),true));

    Statement stmt = conn.createStatement();
    try {
      stmt.executeUpdate(ddl.toString());
    }
    finally {
      stmt.close();
    }
  }
  public void modifyColumnForDesc(Connection conn, String tablename,String fieldname,String desc) throws SQLException{
    
  }

  protected boolean equalsFieldType(char srct,char dest){
    return srct==dest;
  }
  protected boolean checkColumnName(String col,String new_col){
    return col.equalsIgnoreCase(new_col);
  }
  /**
   * 查找指定字段的unique索引名；
   * @param tbmd
   * @param col
   * @param unique
   *        是否只查unique索引，false则查所有索引；
   * @return
   */
  protected String[] getUniqueIndexName(TableMetaData tbmd, String col,boolean unique) {
    List l = new ArrayList();
    TableIndexMetaData[] indx = tbmd.getIndexes();
    /**
     * 20090220
     * 如果该字段是主键，则不进行修改；
     */
    String[] keys = tbmd.getPrimaryKey();
    boolean iskey = keys!=null&&keys.length==1&&keys[0].equalsIgnoreCase(col);
    if(indx!=null){
    for(int i=0;i<indx.length;i++){
      TableIndexMetaData index = indx[i];
      String[] cols = index.getColumns();
      if (unique) {
        if (index.isUnique() && cols.length == 1) {
          if (cols[0].equalsIgnoreCase(col)&&!iskey) {
            l.add(index.getName());
          }
        }
      }else{
        if(findField(col,cols)){
          l.add(index.getName());
        }
      }
    }
    }
    if(l.size()>0){
      String[] uns = new String[l.size()];
      l.toArray(uns);
      return uns;
    }
    return null;
  }
  private boolean findField(String col, String[] cols) {
    for(int i=0;i<cols.length;i++){
      if(col.equalsIgnoreCase(cols[i])){
        return true;
      }
    }
    return false;
  }
  
  public abstract void modifyColumn(Connection conn, String tablename,String fieldname,char coltype, int len,int scale) throws SQLException;

  
  /**
   * 以前的旧方法，不能改空还是不空，现在默认将字段改为可空；
   */
  public void modifyColumn(Connection conn, String tablename, String col, String new_col,char coltype,
      int len, int dec, String defaultvalue, boolean unique) throws SQLException {
    modifyColumn(conn, tablename, col, new_col, coltype, len, dec, defaultvalue, unique, true);
  }
  
  /**
   * 修改字段属性
   * @param conn Connection
   * @param tablename String
   * @param coltype char
   * @param len int
   * @param dec int
   * @param defaultvalue String
   * @param unique boolean
   * @param col String
   * @throws Exception
   */
  public abstract void modifyColumn(Connection conn, String tablename, String col, String new_col,char coltype,
                           int len, int dec, String defaultvalue, boolean unique, boolean nullable) throws SQLException ;
 
  public void dropColumn(Connection conn, String tablename, String col) throws
  SQLException {
    StringBuffer ddl = new StringBuffer(64).append("ALTER TABLE ");
    ddl.append(tablename);
    ddl.append(" DROP COLUMN ").append(col);
    Statement stmt = conn.createStatement();
    try {
      stmt.execute(ddl.toString());
    }
    finally {
      stmt.close();
    }
  }

  public boolean canSetNumAsStr() {
    return true;
  }
  	
  	/**
  	 * 处理如果表名存在，则自动更名后，在创建表；
  	 * 这个过程如果多线程同时调用，就要考虑同步的问题：
  	 * 先获取一个不存在的表名，然后创建的时候可能这个表名已经被别的线程使用并创建了，
  	 * 这里在创建就会出现表名已存在的异常；
  	 * 
  	 * 解决办法：
  	 * 使用一个特殊的表，这个表只有一个字段，且是唯一的，用来存储将要创建的表名；
  	 * 流程：获取一个数据库不存在的表名，写入这个特殊的表，如果特殊的表不存在，则创建之，
  	 * 然后将表名写入特殊的表，如果这时出现违反唯一约束异常，则表示这个表正在被其他线程使用，
  	 * 然后重新生成一个表名，继续上面的判断，直到可以将表名写入特殊的表，则表示这个表名正在被本线程使用，
  	 * 然后创建表，最后从那个特殊的表将创建的表名删除；
  	 * 
  	 * 这可以解决线程同步时，创建表自动更名的问题；
  	 * 原理是利用数据库表的唯一字段，锁住将要创建的表名，完成后在删除释放；
  	 * @param conn
  	 * @param catalog
  	 * @param tablename
  	 * @param isTemp
  	 * @param checkIndexLength
  	 * @return
  	 * @throws SQLException
  	 */
	private String createTableForMultithread(Connection conn, String catalog, String tablename, boolean isTemp,
			boolean checkIndexLength) throws SQLException {
		//获取一个新表名，并写入特殊表TABLENAME_CREATE_MULTITHREAD
		String newtbname = lockNewTableName(conn, tablename);
		try {
			//创建表
			return createTableNotChangeTableName(conn, catalog, newtbname, isTemp, checkIndexLength);
		}
		finally {
			//从特殊表TABLENAME_CREATE_MULTITHREAD删除创建的表名
			unlockNewTableName(conn, newtbname);
		}
	}

	public String lockNewTableName(Connection conn, String tablename) throws SQLException {
		tablename = formatTableName(tablename);
		String oldname = tablename;
		int k = 0;
		while ((tableOrViewExists(conn, tablename)||!addTableNameToTableForMultithread(conn, tablename))&&k<2000) {
			//随机表名，增加一位A-Z随机字符，用以尽量减少这里的循环次数；
			tablename = formatTableName(oldname+(char)(Math.floor(Math.random()*26)+'A') + (int)Math.round(Math.random()*2000));
			k++;
		}
		
		return tablename;
	}

	private void createTableForMultithread(Statement stat) throws SQLException {
		TableColumnMetaDataHelper fi = new TableColumnMetaDataHelper("TABLENAME");
		fi.setType(FIELD_TYPE_STR);
		fi.setLen(100);
		fi.setNullable(false);
		StringBuffer sbuf = new StringBuffer(128);
		sbuf.append("create table ");
		sbuf.append(getTalbeNameForMultiThread()).append("(");
		sbuf.append(getFldDdl(fi)).append(",primary key (TABLENAME))");
		stat.execute(sbuf.toString());
	}
	
	private boolean addTableNameToTableForMultithread(Connection conn, String tablename) throws SQLException {
		String sql = "insert into " + getTalbeNameForMultiThread() + " (TABLENAME)values('" + tablename.toUpperCase()
				+ "')";
		Statement stat = conn.createStatement();
		boolean flag = false;
		try {
			 flag = addTableNameToTableForMultithread(sql, stat);
		}
		finally {
			stat.close();
		}
		/**
		 * 多线程调用时，可能写入TABLENAME_CREATE_MULTITHREAD表的表名已经被别的线程创建，
		 * 如果这里不判断表名是否存在，就会出现表名重复异常；
		 * 比如很多线程同时创建表test，test开始不存在，一个线程将test写入TABLENAME_CREATE_MULTITHREAD，并开始创建表，
		 * 其他线程堵塞在获取新表名并写入TABLENAME_CREATE_MULTITHREAD，前面那个线程建表成功后，从TABLENAME_CREATE_MULTITHREAD删除了表名test，
		 * 这时还有线程堵塞在这个方法，这时却可以写入test表名，而此时test表已经被创建；
		 */
		if(flag && tableOrViewExists(conn, tablename)){
			unlockNewTableName(conn,tablename);
			return false;
		}
		return flag;
	}
	
	private String getTalbeNameForMultiThread(){
		String defaultschema = dbinf.getDefaultSchema();
		if(StrFunc.isNull(defaultschema)){
			return TABLENAME_CREATE_MULTITHREAD;
		}else{
			return defaultschema+"."+TABLENAME_CREATE_MULTITHREAD;
		}
	}

	private boolean addTableNameToTableForMultithread(String sql, Statement stat) throws SQLException {
		try {
			stat.execute(sql);
			return true;
		}
		catch (PooledSQLException e) {
			//表TABLENAME_CREATE_MULTITHREAD不存在
			if (e.getErrorCode2() == PooledSQLException.JDBC_NOT_EXIST_OBJECT) {
				try {
					createTableForMultithread(stat);
				}
				catch (SQLException e1) {//创建表异常，直接抛出
					throw e1;
				}
				//创建成功，重新执行写入sql
				return addTableNameToTableForMultithread(sql,stat);
			}
			else if(e.getErrorCode2()==PooledSQLException.JDBC_UNIQUE_CONSTRAINT_VIOLATED){
				//违反唯一约定，表示表中已经有了指定的表名
				return false;
			}
			throw e;
		}
	}
	
	public void unlockNewTableName(Connection conn,String tablename) throws SQLException{
		String sql = "delete from "+getTalbeNameForMultiThread()+" where TABLENAME='"+tablename.toUpperCase()+"'";
		Statement stat = conn.createStatement();
		try{
			stat.execute(sql);
		}finally{
			stat.close();
		}
	}

	/**
	 * 创建表结构
	 * 
	 * DML(Data Manipulation Language)数据操纵语言命令使用户能够查询数据库以及操作已有数据库中的数据。
	 * 如insert,delete,update,select等都是DML.
	 *
	 * DDL语句用语定义和管理数据库中的对象，如Create,Alter和Drop.
	 *
	 * DDL操作是隐性提交的！不能rollback
	 *
	 * 因此，如果建表过程中有异常，实现回滚，需要写程序实现；
	 * @param conn Connection
	 * @param tablename String
	 * @param isTemp boolean 是否为临时表
	 * @throws Exception
	 */
	private String createTable(Connection conn, String catalog, String tablename, boolean isTemp,
			boolean ifTableNameExistThrowException, boolean checkIndexLength) throws SQLException {
		if(ifTableNameExistThrowException){
			/**
			 * 支持表名带schema前缀
			 */
			String tbname = formatTableName(tablename);
			return createTableNotChangeTableName(conn,catalog,tbname,isTemp,checkIndexLength);
		}else{
			return createTableForMultithread(conn,catalog,tablename,isTemp,checkIndexLength);
		}
	}
	
	private String createTableNotChangeTableName(Connection conn, String catalog, String tablename, boolean isTemp,
			 boolean checkIndexLength) throws SQLException {
		/**
		 * 20091023
		 * 程序有这样的处理，如果创建表出异常，会回滚，即：删除创建的表；
		 * 没有考虑：
		 * 当ifTableNameExistThrowException=true ,建表时tablename已经存在，会出异常；
		 * 这时把本来存在的表删除了，这显然是错误的；
		 * 
		 * 解决办法：
		 * 只在创建表的语句成功后，如果建索引失败等其他相关建表语句失败，出异常，才回滚时，删除创建的表；
		 * 引入一个变量：isCreated
		 */
		boolean isCreated = false;

		/**
		 * 20090902
		 * 检查主键和索引的字段定义总长度，如果超过数据库允许范围，则需要调整字段长度；
		 * 20091021
		 * 给定参数，决定是否需要检查表的索引、主键的字段总长度是否超过数据库限制，如果超过是否需要自动更改字段长度；
		 */
		if (checkIndexLength) {
			checkKeysAndIndexesMaxLength(tbMetaHelper);
		}
		/**
		 * 20100130
		 * 创建表前，处理主键，字段的唯一属性，索引是否重复；
		 * 数据库中主键，字段的唯一，都是通过索引实现的，如果有重复的设置，建表会出错；
		 * 比如Oracle:
		 * CREATE TABLE EBI_SYS22_LOGOPERDIM(ID VARCHAR2(10)  UNIQUE  NOT NULL ,OBJTYPE VARCHAR2(4)  UNIQUE ,OPER VARCHAR2(4) ,OPERDESC VARCHAR2(50) ,"DESC" VARCHAR2(255) ,LOGTYPE VARCHAR2(2) ,PRIMARY KEY (ID))
		 * ORA-02261: 表中已存在这样的唯一关键字或主键
		 */
		tbMetaHelper.formatTableMetaDataForCreateTable();
		try {
			if (catalog != null) {
				if (!catalogExists(conn, catalog)) {
					createCatalog(conn, catalog);
				}
				conn.setCatalog(catalog);
			}
			//create table
			if ((this.tbMetaHelper.getColumnCount() <= 0)) {
				throw new SQLException("cann't create table without any fields");
			}
			tablename = createTableForDefine(conn, tablename, isTemp);
			isCreated = true;//表创建成功；
			//给字段加描述信息
			addDescToField(conn, tablename);
			//创建索引
			createIndexForDefine(conn, catalog, tablename);
			//创建自动增长字段；主要针对Oracle
			createAutoIncFieldForDefine(conn, tablename);
		}
		catch (SQLException ex) {
			/**
			 * 出现异常，回滚
			 * 20090929
			 * 有些数据库没有实现objectExists方法，比如sqlserver，直接使用查系统表实现tableExist
			 * 所以改为使用tableExists
			 * 
			 * 20091023
			 * 只对创建的表回滚时，才删除；
			 */
			if (isCreated && tableExists(conn, null, tablename)) {
				dropTable(conn, null, tablename);
			}
			throw ex;
		}
		/**
		 * 20110915 dw
		 * 如果创建的表是默认schema下的，则返回表名可以不带schema前缀；
		 * 这是为了兼容以前使用了jdbc的程序，比如I@Report那边不支持带schema前缀的表名；
		 */
		String[] tbs = getTableNameForSchema(tablename);
		if(tbs[0]!=null&&tbs[0].equals(dbinf.getDefaultSchema())){
			return tbs[1];
		}
		return tablename;
	}

	private void createAutoIncFieldForDefine(Connection conn, String tablename) throws SQLException {
		for (int i = 0; i < tbMetaHelper.getColumnCount(); i++) {
			TableColumnMetaData fi = tbMetaHelper.getColumn(i);
			if (fi.isAutoInc()) {
				try {
					additionOfAutoInc(conn, tablename, getColumnName(fi.getName()));
				}
				catch (SQLException ex) {
//					throw new PooledSQLException("在表" + tablename + "上创建自增长字段:" + fi.getName() + "\r\n\r\n出现异常:", ex);
					Object[] param=new Object[]{tablename,fi.getName()};
					throw new PooledSQLException(I18N.getString("com.esen.jdbc.dialect.impl.dbdef.createfieldex", "在表{0}上创建自增长字段:{1}\r\n\r\n出现异常:", param), ex);

				}
				//每张表只有一个自增长字段；
				break;
			}
		}
	}

	private void createIndexForDefine(Connection conn, String catalog, String tablename) throws SQLException {
		TableIndexMetaData[] _index = tbMetaHelper.getIndexes();
		for (int i = 0; _index != null && i < _index.length; i++) {
			//对索引名如果存在或者超出长度范围，自动更名，使创建成功；
			TableIndexMetaData indi = _index[i];
			createIndex(conn, tablename, indi.getName(),indi.getColumns(),indi.isUnique(), false);
		}
	}

	private String createTableForDefine(Connection conn, String tablename,boolean isTemp) throws SQLException {
		StringBuffer fldddl = new StringBuffer(256);
		for (int i = 0; i < tbMetaHelper.getColumnCount(); i++) {
			TableColumnMetaDataHelper fi = (TableColumnMetaDataHelper) tbMetaHelper.getColumn(i);
			if (fi.isAutoInc()) {
				fldddl.append(getIdFieldDdl(fi.getName(), fi.getLen(), fi.getStep(), fi.getDesc()));
				fldddl.append(",");
			}
			else {
				fldddl.append(getFldDdl(fi) + ",");
			}
		}
		String[] _pkeys = tbMetaHelper.getPrimaryKey();
		if (_pkeys != null && _pkeys.length > 0) {
			//create primary key
			fldddl.append("PRIMARY KEY (");
			fldddl.append(ArrayFunc.array2Str(_pkeys, ','));
			fldddl.append(')');
		}
		else {
			fldddl.deleteCharAt(fldddl.length() - 1);
		}
		
		String ddl = null;
		if (isTemp) {
			ddl = getTempTableCreateDdl(tablename, fldddl.toString());
		}
		else {
			ddl = getTableCreateDdl(tablename, fldddl.toString());
		}

		Statement stmt = conn.createStatement();
		try {
			stmt.executeUpdate(ddl);
		}
		catch (SQLException ex) {
//			throw new PooledSQLException("创建表:" + tablename + "\r\n\r\n" + ddl + "\r\n\r\n出现异常:" ,ex);
			Object[] param=new Object[]{tablename,ddl};
			throw new PooledSQLException(I18N.getString("com.esen.jdbc.dialect.impl.dbdef.createtableex", "创建表:{0}\r\n\r\n{1}\r\n\r\n出现异常:", param) ,ex);
		}
		finally {
			stmt.close();
		}
		return tablename;
	}
  
  /**
   * 20091102
   * 修改字段长度时，如果字段是主键之一，则根据主键长度限制，调整指定的修改长度；
   * 
   * @param tbmd  原表结构
   * @param colname  修改字段名
   * @param len     修改字段colname的长度；
   * @return
   */
  protected int adjustFieldLengthForModify(TableMetaData tbmd, String colname, int len) {
    int maxKeysLen = this.dbinf.getMaxKeyOfFieldsLength();
    if(maxKeysLen<0) return len;
    TableColumnMetaData col = getColumn(tbmd,colname);
    //检查主键
    int adjustlen = adjustFieldLengthByPrimaryKey(tbmd, colname, len, maxKeysLen, col);
    if(adjustlen!=len){
      return adjustlen;
    }
    //检查索引
    adjustlen = adjustFieldLengthByIndex(tbmd, colname, len, maxKeysLen, col);
    if(adjustlen!=len){
      return adjustlen;
    }
    
    //检查唯一属性字段
    TableColumnMetaData[] cols = tbmd.getColumns();
    for(int i=0;i<cols.length;i++){
      TableColumnMetaData coli = cols[i];
      if(coli.isUnique()){
        if(len>maxKeysLen){
          return maxKeysLen;
        }else {
          return len;
        }
      }
    }
    
    return len;
  }
  
  protected int adjustFieldLengthByIndex(TableMetaData tbmd, String colname, int len, int maxKeysLen,
      TableColumnMetaData col) {
    TableIndexMetaData[] indx = tbmd.getIndexes();
    if(indx!=null&&indx.length>0){
      int newlen = len;
      for(int i=0;i<indx.length;i++){
        TableIndexMetaData indxi = indx[i];
        if(indxi.isUnique()&&isIndexColumn(indxi,colname)){
          String[] indxcols = indxi.getColumns();
          int keylen = 0;
          for(int j=0;j<indxcols.length;j++){
            TableColumnMetaData colj = getColumn(tbmd,indxcols[j]);
            keylen += colj.getLen();
          }
          int maxlen = maxKeysLen - (keylen-col.getLen());
          if(newlen>maxlen){
            newlen =  maxlen;
          }
        }
      }
      return newlen;
    }
    return len;
  }
  private int adjustFieldLengthByPrimaryKey(TableMetaData tbmd, String colname, int len, int maxKeysLen,
      TableColumnMetaData col) {
    String keys[] = tbmd.getPrimaryKey();
    if(keys!=null&&keys.length>0){
      boolean iskey = false;
      for(int i=0;i<keys.length;i++){
        if(keys[i].equals(colname)){
          //修改字段是主键之一
          iskey = true;
          break;
        }
      }
      if(iskey){
        int keylen = 0;
        for(int i=0;i<keys.length;i++){
          TableColumnMetaData coli = getColumn(tbmd,keys[i]);
          keylen += coli.getLen();
        }
        int maxlen = maxKeysLen - (keylen-col.getLen());
        if(len>maxlen){
          return maxlen;
        }else{
          return len;
        }
      }
    }
    return len;
  }
  
  protected boolean isIndexColumn(TableIndexMetaData indxi, String colname) {
    String[] cols = indxi.getColumns();
    for(int i=0;i<cols.length;i++){
      if(colname.equals(cols[i])){
        return true;
      }
    }
    return false;
  }
  
  protected TableColumnMetaData getColumn(TableMetaData tbmd, String colname) {
    TableColumnMetaData[] cols = tbmd.getColumns();
    for(int i=0;i<cols.length;i++){
      String coln = cols[i].getName();
      if(coln.equals(colname)){
        return cols[i];
      }
    }
    return null;
  }
  /**
   * 20090902
   * 建表前，检查主键和索引的字段定义总长度，如果超过数据库允许范围，则需要调整字段长度；
   * 由子类实现；
   * @param conn 
   */
  protected void checkKeysAndIndexesMaxLength(TableMetaDataHelper tmdh) {
    
  }
  
  /**
   * 20090902
   * 根据索引最大允许长度，调整相关字段的定义长度；
   * @param maxKeyLength
   */
  protected void checkKeysAndIndexesMaxLength(TableMetaDataHelper tmdh,int maxKeyLength) {
    //对单个字段，如果定义了唯一属性，是通过索引实现的，也要限制长度；
    for(int i=0;i<tmdh.getColumnCount();i++){
      TableColumnMetaDataHelper fieldi = (TableColumnMetaDataHelper)tmdh.getColumn(i);
      if(fieldi.isUnique()){
        adjustFieldLength(new TableColumnMetaDataHelper[]{fieldi},maxKeyLength);
      }
    }
    //检查主键
    String[] _pkeys = tmdh.getPrimaryKey();
    if(_pkeys!=null&&_pkeys.length>0){
      TableColumnMetaDataHelper[] keys = new TableColumnMetaDataHelper[_pkeys.length];
      for(int i=0;i<_pkeys.length;i++){
        keys[i] = (TableColumnMetaDataHelper)tmdh.getColumn(_pkeys[i]);
      }
      adjustFieldLength(keys,maxKeyLength);
    }
    //检查索引；
    TableIndexMetaData[] _index = tmdh.getIndexes();
    if(_index!=null&&_index.length>0){
      for (int i = 0; i < _index.length; i++) {
        TableIndexMetaData indx =  _index[i];
        String[] fields = indx.getColumns();
        TableColumnMetaDataHelper[] keys = new TableColumnMetaDataHelper[fields.length];
        for(int j=0;j<fields.length;j++){
          keys[j] = (TableColumnMetaDataHelper)tmdh.getColumn(fields[j]);
        }
        adjustFieldLength(keys,maxKeyLength);
      }
    }
  }
  
  protected void adjustFieldLength(TableColumnMetaDataHelper[] keys, int maxKeyLength) {
    int tlen = 0;
    for(int i=0;i<keys.length;i++){
      TableColumnMetaDataHelper fi = keys[i];
      if(fi==null) continue;
      int flen =  fi.getLen();
      char tp = (char)fi.getType();
      /**
       * 对于非字符类型的字段类型，比如数值，日期都当作8个字节长度处理；
       */
      if(tp!='C'){
        flen = 8;
      }
      tlen += flen;
    }
    if(tlen>maxKeyLength){
      for(int i=0;i<keys.length;i++){
        TableColumnMetaDataHelper fi = keys[i];
        if(fi==null) continue;
        //只需要字符类型的字段需要调整；
        if((char)fi.getType()=='C'){
          /**
           * 20090903
           * 这里对每个字段的长度做等比缩小;
           * 总长度缩小到最长允许范围的0.95 ,是为了在导入数据时，可以有空间扩容；
           */
          int len = (int)Math.round(fi.getLen()*1.0000/tlen * maxKeyLength*0.95);
          fi.setLen(len);
        }
      }
    }
  }
  

  /**
   * 目前oracle,db2, vertica需要实现这个方法；
   * @param conn
   */
  protected void addDescToField(Connection conn,String tbname) throws SQLException{
    
  }
  
  

  public String getTableCreateDdl(String tablename, String fldDdl) {
    return "CREATE TABLE " + tablename + "(" + fldDdl + ")";
  }

  public String getTempTableCreateDdl(String tablename, String fldDdl) {
    return "CREATE TEMPORARY TABLE " + tablename + "(" + fldDdl +
        ") ";
  }

  /**
   * 判断Catalog是否存在
   * @param conn Connection
   * @param catalog String
   * @throws Exception
   * @return boolean
   */
  protected boolean catalogExists(Connection conn, String catalog) throws
  SQLException {
    DatabaseMetaData dm = conn.getMetaData();
    if (dm.getCatalogTerm().length() == 0) {
      return true;
    }
    //如果该数据库不支持catalog，则总是认为catalog存在
    ResultSet rs = dm.getCatalogs();
    try {
      while (rs.next()) {
        if (rs.getString(1).equalsIgnoreCase(catalog)) {
          return true;
        }
      }
    }
    finally {
      rs.close();
    }
    return false;
  }


  public String createIndex(Connection conn, String table, String indexName,String[] indexFields,boolean indexUnique,boolean ifIndexNameExistThrowException) throws SQLException {
    StringBuffer s = new StringBuffer(32);
    try {
      if (indexUnique) { //unique
        s.append("CREATE UNIQUE INDEX ");
      }
      else {
        s.append("CREATE INDEX ");
      }
      
      indexName = checkIndexName(table,indexName,ifIndexNameExistThrowException);
      if(_indexNames==null)
        _indexNames = new ArrayList();
      _indexNames.add(indexName);
      s.append(indexName).append(" ON ").append(table);
      s.append("(");
      for(int i=0;i<indexFields.length;i++){
        s.append(indexFields[i]);
        if(i<indexFields.length-1)
          s.append(',');
      }
      s.append(")");
      Statement ddl = conn.createStatement();
      try {
        ddl.executeUpdate(s.toString());
      }
      finally {
        ddl.close();
      }
      return indexName;
    }
    catch (PooledSQLException ex) {
    	if(!ifIndexNameExistThrowException && ex.getErrorCode2()==PooledSQLException.JDBC_EXISTING_OBJECT){
    		/**
    		 * 通过异常机制处理索引名重名，此过程是线程同步的；
    		 */
    		return createIndex(conn,table,getRandomIndexName("I"+table),indexFields,indexUnique,false);
    	}
//    	throw new PooledSQLException("创建表索引:" + table + "\r\n" + s +"\r\n出现异常:" ,ex);
    	Object[] param=new Object[]{table,s};
    	throw new PooledSQLException( I18N.getString("com.esen.jdbc.dialect.impl.dbdef.createindexex","创建表索引:{0}\r\n{1}\r\n出现异常:", param),ex);
    	
    }
    
  }
  
  protected String checkIndexName(String tablename,String indexname,boolean ifIndexNameExistThrowException){
	  if(StrFunc.isNull(indexname)){
		  /**
		   * 没有指定索引名，则以表名为前缀随机生成一个的；
		   */
		  return getRandomIndexName("I"+tablename);
	  }
	  if(ifIndexNameExistThrowException)
		  return indexname;
	  //去除非法字符；
	  indexname = this.formatName(indexname);
	  
	  /**
	   * WTAP-773
	   * 对给定的索引名，也在末尾加上随机字符串，以减少重名的机率；
	   * 如果重名，这里为了集群同步，使用数据库异常机制处理，但是这会在多线程同步执行时，可能出现oracle内部错误；
	   * 加上随机数后，同步测试用例没有出现异常；
	   * --20101119
	   */
	  return getRandomIndexName(indexname);
  }

  protected String getRandomIndexName(String indexname){
	  int idx = indexname.lastIndexOf(".");
	  String idxname = indexname;
	  if(idx>=0){
		  idxname = indexname.substring(idx+1);
	  }
	  int maxlen = dbinf.getMaxIndexNameLength();
	  if(idxname.length()>maxlen-6){
		  idxname = idxname.substring(0,maxlen-6);
	  }
	  
	  //后五位随机生成；
	  return idxname+(char)(Math.floor(Math.random()*26)+'A')
	  	+(char)(Math.floor(Math.random()*26)+'A')+(int)Math.floor(Math.random()*10000);
  }

  protected int getMaxIndexLength() {
    return dbinf.getMaxIndexNameLength();
  }
  /**
   * 定义自动增长字段类型
   * @param thisField String
   * @param step int
   */
  protected String getIdFieldDdl(String thisField, int step) {
	  return getIdFieldDdl(thisField, LEN_INT4, step, null);
  }
  
  /**
   * 定义自动增长字段类型
   * 对于可以定义字段  comment 的数据库，只有  MySql 在建表时指定 comment, 其他数据库在建表完成以后添加 comment
   * @param thisField String
   * @param step int
   * @return String comment
   */
  protected String getIdFieldDdl(String thisField, int step, String desc) {
	  return getIdFieldDdl(thisField, LEN_INT4, step, desc); //不指定长度时，使用 INT 的长度
  }
  
  /**
   * 定义自动增长字段类型
   * 可以指定长度，在 Sybase12 中，可以使用 NUMERIC(len, 0) 类型
   * @param thisField
   * @param len
   * @param step
   * @param desc
   * @return
   */
  protected abstract String getIdFieldDdl(String thisField, int len, int step, String desc);

  /**
   * 定义各种数据字段类型，返回生成字段的定义语句；
   * 这里要区别建表和修改字段；
   * @param fi FieldInfo
   * @return String
   */
  protected String getFldDdl(TableColumnMetaDataHelper fi) throws SQLException {
    return getFldDdl(fi,false);
  }
  /**
   * 返回生成字段的定义语句；
   * 这里要区别建表和修改字段；
   * 要区分的原因是：有些数据库，比如DB2，sqlserver ：
   * ALTER TABLE 只允许添加可包含空值或指定了 DEFAULT 定义的列。因为列 'field4' 不能包含空值且未指定 DEFAULT 定义，
   * 所以无法将该列添加到表 'test_1' 中。 SQL如下：
   * ALTER TABLE test_1 ADD field4 DATETIME  UNIQUE  NOT NULL 
   * 建表时却允许；
   * @param fi
   * @param isUpdate
   *           =true  表示修改字段属性的定义语句；
   *           =false 表示创建表的字段的定义语句；
   * @return
   * @throws Exception
   */
  protected String getFldDdl(TableColumnMetaData fi,boolean isUpdate) throws SQLException {
    switch ((char)fi.getType()) {
      case DbDefiner.FIELD_TYPE_STR:
        return getStrFldDdl(fi, isUpdate);
      case DbDefiner.FIELD_TYPE_INT:
        return getIntFldDdl(fi, isUpdate);
      case DbDefiner.FIELD_TYPE_FLOAT:
        return getNumericFldDdl(fi, isUpdate);
      case DbDefiner.FIELD_TYPE_DATE:
        return getDateFldDdl(fi, isUpdate);
      case DbDefiner.FIELD_TYPE_TIME:
        return getTimeFldDdl(fi, isUpdate);
      case DbDefiner.FIELD_TYPE_TIMESTAMP:
        return getTimeStampFldDdl(fi, isUpdate);
      case DbDefiner.FIELD_TYPE_LOGIC:
        return getLogicFldDdl(fi, isUpdate);
      case DbDefiner.FIELD_TYPE_memo:
        return getMemoFldDdl(fi, isUpdate);
      case DbDefiner.FIELD_TYPE_CLOB:
        return getClobFldDdl(fi, isUpdate);
      case DbDefiner.FIELD_TYPE_BINARY:
        return getBlobFldDdl(fi, isUpdate);
      default:
        throw new SQLException(
            "database not support to define this type of field,type:" +
            (char)fi.getType());
    }
  }
/**
 * default 值 需要在 unique (not) null 前面，也可以在后面；
 * default值 现在不加',如果需要在在defaultvalue参数中构建好
 * @param defaultvalue
 * @param nullable
 * @param unique
 * @param desc
 * @param isUpdate 
 *         这里不管isUpdate是true还是false，都返回同样的值；
 * @return
 */
  protected String getTailDdl(String defaultvalue, boolean nullable,
                              boolean unique,String desc,char t, boolean isUpdate) {
    StringBuffer str = new StringBuffer(16);
    if (defaultvalue != null && defaultvalue.length()>0) {
      str.append(" DEFAULT ").append(defaultvalue);
    }
    if(unique) str.append(" UNIQUE ");
    if(!nullable) str.append(" NOT NULL ");
    return str.toString();
  }
  /**
   * 设置默认值，如果没有指定默认值，给个系统默认值；
   * @param defaultvalue
   * @param t
   * @return
   */
  protected String getDefualtSql(String defaultvalue, char t) {
    if (defaultvalue != null&& defaultvalue.length()>0) {
      return " DEFAULT "+defaultvalue;
    }
    switch(t){
      case DbDefiner.FIELD_TYPE_INT:
      case DbDefiner.FIELD_TYPE_FLOAT:
        return "DEFAULT 0";
      case DbDefiner.FIELD_TYPE_STR:
        return "DEFAULT ''";
      case DbDefiner.FIELD_TYPE_DATE:
        return "DEFAULT "+dl.funcToday();
      case DbDefiner.FIELD_TYPE_TIME:
      case DbDefiner.FIELD_TYPE_TIMESTAMP:
        return "DEFAULT "+dl.funcNow();
    }
    return null;
  }
  /**
   * 定义整型字段
   * @param isUpdate TODO
   * @param isUpdate 
   * @param fldname String
   * @param len int
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected String getIntFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " INT " +
        getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_INT, isUpdate);
  }

  /**
   * 定义浮点型字段
   * 20090722 
   * 每种数据库都实现了，这里改为抽象的方法；
   * @param isUpdate TODO
   * @param fldname String
   * @param len int
   * @param dec int
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected abstract String getNumericFldDdl(TableColumnMetaData fi, boolean isUpdate);
  
  /**
   * 获取数据库允许的精度范围；
   * 如果总长度小于小数位数，则总长度=总长度+小数位数；
   * 如果总长度大于38，取38；
   * 如果小数位数大于等于38，取12；小于0则取0；
   * @param len 
   *           输入的总长度，包含小数位数
   * @param scale
   *           小数位数
   * @param maxLen
   *           最大精度
   * @return
   */
  protected int[] formatNumberPrecision(int len,int scale,int maxLen){
    if(len<=0){
      len = 0;
      scale = 0;
    }
    if(len<scale){
      len = len+scale;
    }
    if(len>maxLen){
      len = maxLen;
    }
    if(scale>=maxLen){
      scale = 12;
    }
    if(scale<0){
      scale = 0;
    }
    return new int[]{len,scale};
  }

  /**
   * 定义字符串类型字段
   * @param isUpdate TODO
   * @param fldname String
   * @param len int
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected String getStrFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " VARCHAR(" + (fi.getLen() > 0 ? fi.getLen() : 1) + ") " +
        getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_STR, isUpdate);
  }

  /**
   * 定义日期类型字段
   * @param isUpdate TODO
   * @param fldname String
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected String getDateFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " DATE " +
        getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_DATE, isUpdate);
  }

  /**
   * 定义时间类型字段
   * @param isUpdate TODO
   * @param fldname String
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected String getTimeFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " TIME " +
        getTailDdl(fi.getDefaultValue(), fi.isNullable(),fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_TIME, isUpdate);
  }

  /**
   * 定义 TIMESTAMP 型字段
   * @param isUpdate TODO
   * @param fldname String
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected String getTimeStampFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " TIMESTAMP " +
        getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_TIMESTAMP, isUpdate);
  }

  /**
   * 定义logic类型字段
   * @param isUpdate TODO
   * @param fldname String
   * @param len int
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected String getLogicFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " VARCHAR(1) " +
        getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_LOGIC, isUpdate);
  }

  /**
   * 定义Memo类型字段
   * @param isUpdate TODO
   * @param fldname String
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected abstract String getMemoFldDdl(TableColumnMetaData fi, boolean isUpdate);

  /**
   * 定义Clob类型字段
   * @param isUpdate TODO
   * @param fldname String
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected abstract String getClobFldDdl(TableColumnMetaData fi, boolean isUpdate);

  /**
   * 定义Blob类型字段
   * @param isUpdate TODO
   * @param fldname String
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected abstract String getBlobFldDdl(TableColumnMetaData fi, boolean isUpdate);

  /**
   * 表创建以后，创建自动增长字段；
   * 主要针对Oracle
   * @param conn Connection
   * @param tablename String
   * @param fldname String
   * @throws Exception
   */
  protected void additionOfAutoInc(Connection conn, String tablename,
                                   String fldname) throws SQLException {
  }
  /**
   * 为已有表增加自动增长字段；
   * 方法：创建一个包含自动增长字段的新表，copy原表数据到新表，删除原表，更改新表表名为原表表名；
   * @param conn
   * @param tmd
   * @param incname
   * @throws SQLException 
   */
  protected void addAutoIncField(Connection conn,TableMetaData tmd,String incname) throws SQLException{
    
  }
  
}
/*
*//**
 * 20090902 
 * 将自增长字段也用FieldInfo类表示；
 * 统一处理；
 * @author dw
 *
 *//*
class FieldInfo {
  private String _name;
  private char _type;
  private int _len;
  private int _dec;
  private String _defaultvalue;
  private boolean _null;
  private boolean _unique;
  private String _desc;
  private int _step;//自增长字段，步长；
  private boolean isAutoField;//记录是否是自增长字段；
  
  *//**
   * 定义自增长字段；
   * @param nm
   * @param step
   *//*
  public FieldInfo(String nm,int step){
    _name = nm;
    _step = step;
    isAutoField = true;
  }

  public FieldInfo(String nm, char tp, int len, int dec, String defaultvalue,
                   boolean nullable, boolean unique,String desc) {
    _name = nm;
    _type = tp;
    _len = len;
    _dec = dec;
    _defaultvalue = defaultvalue;
    _null = nullable;
    _unique = unique;
    _desc = desc;
  }
  public FieldInfo(String nm, char tp, int len, int dec, String defaultvalue,
      boolean nullable, boolean unique) {
    this(nm,tp,len,dec,defaultvalue,nullable,unique,null);
  }
  public String getName() {
    return _name;
  }
  *//**
   * 增长字段的步长；
   * @return
   *//*
  public int getStep(){
    return _step;
  }
  *//**
   * 是否是增长字段；
   * @return
   *//*
  public boolean isAutoField(){
    return isAutoField;
  }
  public String getFieldName() {
    return _name;
  }

  public int getFieldLen() {
    return _len;
  }
  *//**
   * 调整字段的定义长度；
   * @param len
   *//*
  public void setFieldLen(int len){
    _len = len;
  }

  public int getDecLen() {
    return _dec;
  }

  public String getDefaultValue() {
    return _defaultvalue;
  }

  *//**
   * 得到字段的数据类型
   * （C|N|M|I|L|D|T|P）
   * @return char
   *//*
  public char getFieldType() {
    return _type;
  }

  public boolean nullable() {
    return _null;
  }
  
  public void setNullable(boolean nullable){
    _null = nullable;
  }

  public boolean isUnique() {
    return _unique;
  }
  public String getDesc(){
    return _desc;
  }
}*/

/*class IndexInfo {

  private boolean _unique = false; //common index or unique index
  private String _fields;
  private String _name;

  IndexInfo(String name, String fields, boolean unique) {
    _unique = unique;
    _fields = fields;
    _name = name;
  }

  public boolean isUnique() {
    return _unique;
  }

  public String getName() {
    return _name;
  }

  void setUnique(boolean b) {
    _unique = b;
  }

  public String getFields() {
    return _fields;
  }
  *//**
   * 将定义索引的字段分解成字段数组；
   * _fields 格式：(field1,field2,...) ,带括号的；
   * @return
   *//*
  public String[] getFieldArray() {
    return _fields.substring(1, _fields.length()-1).split(",");
  }
}*/


