package com.esen.jdbc.dialect.impl;

import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.esen.db.sql.analyse.SQLAnalyse;
import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.ResultMetaData;
import com.esen.jdbc.ResultMetaDataImpl;
import com.esen.jdbc.SqlExecuter;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.util.StmFunc;
import com.esen.util.StrFunc;
import com.esen.util.exp.ExpException;
import com.esen.util.i18n.I18N;

/**
 * <p>Title: Esensoft JDBC</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Esensoft</p>
 * @author dw
 * @version 1.0
 */

public abstract class DialectImpl implements Dialect {

  protected ConnectionFactory connectionFactory;
  protected Connection con;
  protected DbMetaData dbmd;
  
  public DialectImpl(Object con_OR_conf) {
    if (con_OR_conf instanceof Connection){
      this.con = (Connection) con_OR_conf;
    }else{
      this.connectionFactory = (ConnectionFactory) con_OR_conf;
    }
  }
  
  public DataBaseInfo getDataBaseInfo(){
    if (connectionFactory!=null){
      return connectionFactory.getDbType();
    }
    return DataBaseInfo.createInstance(con);
  }
  
  public Connection getConnection() throws SQLException{
    if (con!=null)
      return con;
    return connectionFactory.getConnection();
  }
  
  public void closeConnection(Connection con) throws SQLException{
    if (con!=this.con)
      con.close();
  }

  public boolean canSetNumAsStr() {
    return true;
  }
  
  public int getMaxVarCharLength() {
    return this.getDataBaseInfo().getMaxVarcharFieldLength();
  }
  
  public TableColumnMetaData getTableColumnMetaData(String tbname,String columnname) throws SQLException{
    return getTableColumnMetaData(null,tbname,columnname);
  }
  
  public TableColumnMetaData getTableColumnMetaData(Connection conn,String tbname,String columnname) throws SQLException{
    DbMetaData dbmd2;
    if(conn==null){
      dbmd2 = createDbMetaData();
    }else{
      dbmd2 = createDbMetaData(conn);
    }
    TableMetaData tbmd = dbmd2.getTableMetaData(tbname);
    TableColumnMetaData[] clmds = tbmd.getColumns();
    for(int i=0;i<clmds.length;i++){
      TableColumnMetaData clmd = clmds[i];
      if(clmd.getName().equalsIgnoreCase(columnname))
        return clmd;
    }
    return null;
  }
  /**
   * 将一个字符串变为小写,其中连续的空格只保留一个
   * @param sString String
   * @return String
   */
  protected String getLowNoSpaceString(String s) {
    if (s == null || s.length() == 0) {
      return "";
    }
    char c;
    s = s.toLowerCase().trim();
    int len = s.length();
    StringBuffer sBuf = new StringBuffer(s.length());
    for (int i = 0; i < len;) {
      c = s.charAt(i);
      if (c != 32) {
        sBuf.append(c);
        i++;
      }
      else {
        sBuf.append(c);
        c = s.charAt(++i);
        while (c == 32) {
          c = s.charAt(++i);
        }
      }
    }
    return sBuf.toString();
  }

  public boolean resultEndWithIndex() {
    return false;
  }

  public boolean supportsLimit() {
    return false;
  }

  public boolean supportsLimitOffset() {
    return false;
  }

  public boolean supportsViewColumnComment() {
	return false; 
  }
  
  public boolean supportsTableComment() {
	return false; 
  }
  
  public boolean supportsTableColumnComment() {
	return false; 
  }

  public String getLimitString(String querySelect, int offset, int limit) {
    return querySelect;//对两者都不支持时
  }

  /**
   * 默认的实现方式,一直回滚到指定的行
   * @param stmt Statement
   * @param querySelect String
   * @param offset int
   * @param limit int
   * @return ResultSet
   */
  public ResultSet queryLimit(Statement stmt, String querySelect, int offset,
      int limit) throws SQLException {
    try{
      /**
       * 先执行分页sql，如果有异常，转到直接执行该sql；
       */
      String limitsql = getLimitString(querySelect, offset, limit);
      return stmt.executeQuery(limitsql);
    }catch(SQLException ex){
      ResultSet rst = stmt.executeQuery(querySelect);
      return new ResultSetForLimit(rst,offset,limit);
    }
  }

  public int getMaxColumn() {
    return 1000;
  }

  public String getInsertSelectSql(String selClause, String fromClause,
      String whereClause, String intoClause) {
    return "INSERT " + intoClause + " SELECT " + selClause + " FROM "
        + fromClause + " " + whereClause;
  }

  private void t(String msg) throws ExpException {
    throw new ExpException(msg);
  }

  protected void tno(String msg) throws ExpException {
   // t("no " + msg + " 不能实现转化为sql语句");
	  t(I18N.getString("com.esen.jdbc.dialect.impl.dialectimpl.nosql1", "no  {0} 不能实现转化为sql语句", new Object[]{msg}));
  }

  protected void todo(String msg) throws ExpException {
//    t("todo " + msg + " 暂且不支持转化为sql语句");
	  t(I18N.getString("com.esen.jdbc.dialect.impl.dialectimpl.nosql2", "todo  {0} 暂且不支持转化为sql语句", new Object[]{msg}));
  }

  public DbMetaData createDbMetaData() throws SQLException {
    if(dbmd==null){
      dbmd = connectionFactory!=null?new DbMetaDataImpl(connectionFactory):new DbMetaDataImpl(con);
    }
    return dbmd;
  }
  
  public DbMetaData createDbMetaData(Connection conn) throws SQLException {
    return new DbMetaDataImpl(conn);
  }
  /**
   * querySelect的字段最好有别名
   * SELECT COUNT(*) FROM (select sum(NUM_) as num_ from TESTDB group by  LEFT(STR_,1)) x_
   * SELECT COUNT(*) FROM (select sum(NUM_) from TESTDB group by  LEFT(STR_,1)) x_
   * 第二个sql中sum(NUM_)没有别名，在sqlserver2000中报错；
   */
  public String getCountString(String querySelect) {
    SQLAnalyse sa = new SQLAnalyse(querySelect);
    querySelect  = sa.getNoOrderBySQl();
    //AS400数据库sql不支持$符号；
    return "SELECT COUNT(*) FROM(" + querySelect + ") x_";
  }
  
  /**
   * 获取指定查询sql的结果集结构；
   * 如果connectionFactory不为空，则执行的sql可以是返回结果集的存储过程；
   * @param sql
   * @return
   * @throws SQLException
   */
	public ResultMetaData getResultMetaData(String sql) throws SQLException {
		String sqlstr = getResultMetaData_Sql(sql);
		if (connectionFactory != null) {
			return getResultMetaData(connectionFactory, sqlstr);
		}
		Connection conn = this.getConnection();
		ResultMetaDataImpl rmd = null;
		try {
			Statement stat = conn.createStatement();
			try {
				ResultSet rs = stat.executeQuery(sqlstr);
				try {
					rmd = new ResultMetaDataImpl(rs.getMetaData());
				}
				finally {
					rs.close();
				}
			}
			finally {
				stat.close();
			}
		}
		finally {
			this.closeConnection(conn);
		}
		return rmd;
	}

	private ResultMetaData getResultMetaData(ConnectionFactory connectionFactory2, String sqlstr) throws SQLException {
		SqlExecuter se = SqlExecuter.getInstance(connectionFactory2);
		ResultMetaDataImpl rmd = null;
		try {
			ResultSet rs = se.executeQuery(sqlstr, 0, 0);//st.executeQuery(sqlstr);
			try {
				rmd = new ResultMetaDataImpl(rs.getMetaData());
			}
			finally {
				rs.close();
			}
		}
		finally {
			se.close();
		}
		return rmd;
	}

	protected String getResultMetaData_Sql(String sql) {
		//mdx数据源，直接返回mdx
		if(SqlFunc.isMdx(getDataBaseInfo().getJdbcurl())){
			return sql;
		}
		if (SqlFunc.isCallalbe(sql)) {
			return sql;
		}
		/**
		 * 20090727 BI-2240
		 * 查询获取sql的字段结构时，最好将sql中的order by ...排序的字符都去掉；
		 * 否字在sqlserver中 会报“除非同时指定了 TOP，否则 ORDER BY 子句在视图、内嵌函数、派生表和子查询中无效。” 的异常；
		 */
		SQLAnalyse sqla = new SQLAnalyse(sql);
		sql = sqla.getNoOrderBySQl();
		StringBuffer sqlstr = new StringBuffer(sql.length() + 40);
		sqlstr.append("select * from( ").append(sql.replaceAll("\\?", "''"));
		//AS400数据库sql不支持$符号；
		sqlstr.append(") x_ where 1=0");
		return sqlstr.toString();
	}

  public ResultMetaData getQueryResultMetaData(String sql) throws SQLException {
    return getResultMetaData(sql);
  }
  public String funcDate(String yy,String mm,String dd){
    if(yy==null||yy.length()==0)
      return funcToDate(null);
    if(mm==null||mm.length()==0){
      mm = "--";
      dd = "--";
    }else{
      if(dd==null||dd.length()==0)
        dd = "--";
    }
    StringBuffer date = new StringBuffer(10);
    date.append(yy);
    if(mm.length()<2)
      date.append(0);
    date.append(mm);
    if(dd.length()<2)
      date.append(0);
    date.append(dd);
    return funcToDate(date.toString());
  }
  public String funcStrCat(String field1, String field2) {
    boolean f1 = field1==null||field1.equalsIgnoreCase("null");
    boolean f2 = field2==null||field2.equalsIgnoreCase("null");
    if(f1&&f2) return null;
    if(f1) return field2;
    if(f2) return field1;
    boolean c1 = field1.startsWith("'")&&field1.endsWith("'");
    boolean c2 = field2.startsWith("'")&&field2.endsWith("'");
    if(!c1) field1 = ifNull(field1, "''");
    if(!c2) field2 = ifNull(field2, "''");
    StringBuffer sBuf = new StringBuffer(50)
        .append(" CONCAT(")
        .append(field1)
        .append(',')
        .append(field2)
        .append(')');
    return sBuf.toString();
  }
  /**
   * 除Oracle外，数据库都用ifnull函数；
   */
  public String ifNull(String str,String str2){
    return "ifnull("+str+","+str2+")";
  }
  /**
   * case when qbxssr > 10000 then qbxssr else a.msxse end
   */
  public String funcIf(String b, String t, String f) {
    StringBuffer str = new StringBuffer(128);
    str.append("case when ").append(b);
    str.append(" then ").append(t);
    str.append(" else ").append(f);
    str.append(" end");
    return str.toString();
  }
  public boolean supportRegExp(){
    return false;
  }
  public String regExp(String field,String regexp){
    return null;
  }

  
  public String getTransferSql(String str,int type) {
    return str.replaceAll("'", "''");
  }
  public String funcDays(String datefield,String datefield2){
    return "abs(trunc("+datefield+"-"+datefield2+"))+1";
  }
  /**
   * 数据库的标准差函数；
   * Oracle,DB2,SybaseASE,SybaseIQ,Mysql  都是stddev;
   * SQL SERVER   是stdev
   * BI公式系统的标准差函数是stdev
   */
  public String funcStdev(String field){
    return "stddev("+field+")";
  }
  
  /**
   * 20090625 
   * 取整，改为使用trunc函数实现；
   * 原来的实现没有取整；
   */
  public String funcAsInt(String v){
    return funcTrunc(v,"0");
  }
  
  public String funcAsNum(String v){
    return v; 
  }
  public String funcAsStr(String v){
    return v;
  }
  public String funcToSqlConst(Object o, int destsqltype ){
    if(o==null){
      return funcCastNull(destsqltype);
    }
    switch (destsqltype) {
      case Types.INTEGER:
      case Types.SMALLINT:
      case Types.TINYINT:
      case Types.BIGINT:{
        return funcToSqlIntConst(o);
      }
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.BIT:
      case Types.REAL:
      case Types.DECIMAL:{
        return funcToSqlDoubleConst(o);
      }
      case Types.VARCHAR:
      case Types.CHAR:{
        return funcToSqlCharConst(o);
      }
      case Types.TIMESTAMP:
      case Types.TIME:{
        return funcToSqlTimestampConst(o);
      }
      case Types.DATE:{
        return funcToSqlDateConst(o);
      }
      case Types.BLOB:
      case Types.VARBINARY:
      case Types.BINARY:
      case Types.LONGVARBINARY://sybase
      case Types.LONGVARCHAR: 
        //DB2,Mssql,Oracle
      case Types.CLOB:        //Mysql,Sybase
      case Types.BOOLEAN:
      case Types.ARRAY:
      case Types.DATALINK:
      case Types.JAVA_OBJECT:
      case Types.NULL:
      case Types.OTHER:
      case Types.REF:
      case Types.STRUCT:
      default:
//        throw new RuntimeException("不支持的转换；");
    	  throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.dialectimpl.unsupportc", "不支持的转换；"));
    }
  }
  /**
   * 将空值转换成指定类型；
   * @param type
   * @return
   */
  protected String funcCastNull(int type){
    switch (type) {
      case Types.INTEGER:
      case Types.SMALLINT:
      case Types.TINYINT:
      case Types.BIGINT:{
        return funcAsInt(null);
      }
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.BIT:
      case Types.REAL:
      case Types.DECIMAL:{
        return funcAsNum(null);
      }
      case Types.VARCHAR:
      case Types.CHAR:{
        return funcAsStr(null);
      }
      case Types.TIMESTAMP:
      case Types.TIME:{
        return funcToDateTime(null);
      }
      case Types.DATE:{
        return funcToDate(null);
      }
      case Types.BLOB:
      case Types.VARBINARY:
      case Types.BINARY:
      case Types.LONGVARBINARY://sybase
      case Types.LONGVARCHAR: 
        //DB2,Mssql,Oracle
      case Types.CLOB:        //Mysql,Sybase
      case Types.BOOLEAN:
      case Types.ARRAY:
      case Types.DATALINK:
      case Types.JAVA_OBJECT:
      case Types.NULL:
      case Types.OTHER:
      case Types.REF:
      case Types.STRUCT:
      default: return null;
    }
  }
  private String funcToSqlDateConst(Object o) {
    if(o==null)
      return funcToDate(null);
    if(o instanceof String){
      //20070201,2007-02-01,200701--,200701,2007----,2007
      String v = (String)o;
      return funcToDate(v);
    }else if(o instanceof Number){
      //20070201,200701,2007
      Number num = (Number)o;
      return funcToDate(String.valueOf(num.intValue()));
    }else if(o instanceof java.util.Date){
      java.util.Date d = (java.util.Date)o;
      Calendar c = Calendar.getInstance();
      c.setTimeInMillis(d.getTime());
      String v = StrFunc.date2str(c, null);
      return funcToDate(v);
    }else if(o instanceof java.util.Calendar){
      Calendar c = (Calendar)o;
      String v = StrFunc.date2str(c, null);
      return funcToDate(v);
    }else{
      //throw new RuntimeException("不支持的转换；");
    	 throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.dialectimpl.unsupportc", "不支持的转换；"));
    }
  }


  private String funcToSqlTimestampConst(Object o) {
    if(o==null)
      return funcToDateTime(null);
    if(o instanceof String){
      //20070201,2007-02-01,200701--,200701,2007----,2007
      //20070201 00:00:00
      //2007-02-01 00:00:00
      String v = (String)o;
      return funcToDateTime(v);
    }else if(o instanceof Number){
      //20070201,200701,2007
      Number num = (Number)o;
      return funcToDateTime(String.valueOf(num.intValue()));
    }else if(o instanceof java.util.Date){
      java.util.Date d = (java.util.Date)o;
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String v = df.format(d);
      return funcToDateTime(v);
    }else if(o instanceof java.util.Calendar){
      Calendar c = (Calendar)o;
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String v = df.format(c.getTime());
      return funcToDateTime(v);
    }else {
      //throw new RuntimeException("不支持的转换；");
    	 throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.dialectimpl.unsupportc", "不支持的转换；"));
    }
  }

  private String funcToSqlCharConst(Object o) {
    if(o==null)
      return funcAsStr(null);
    if(o instanceof String){
      String v = (String)o;
      return v;
    }else if(o instanceof Number){
      Number num = (Number)o;
      return String.valueOf(num);
    }else if(o instanceof java.util.Date){
      java.util.Date d = (java.util.Date)o;
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String v = df.format(d);
      return v;
    }else if(o instanceof java.util.Calendar){
      Calendar c = (Calendar)o;
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String v = df.format(c.getTime());
      return v;
    } if(o instanceof Reader) {
      Reader r = (Reader)o;
      try {
        return StmFunc.reader2str(r);
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    else{
      //throw new RuntimeException("不支持的转换；");
    	 throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.dialectimpl.unsupportc", "不支持的转换；"));
    }
  }

  private String funcToSqlDoubleConst(Object o) {
    if(o==null)
      return funcAsNum(null);
    if(o instanceof String){
      String v = (String)o;
      if(StrFunc.isNumber(v))
        return v;
     // else throw  new RuntimeException("非数字类型转换；");
      else throw  new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.dialectimpl.notnumch", "非数字类型转换；"));
    }else if(o instanceof Number){
      Number num = (Number)o;
      return String.valueOf(num);
    }else{
      //throw new RuntimeException("不支持的转换；");
    	 throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.dialectimpl.unsupportc", "不支持的转换；"));
    }
  }

  private String funcToSqlIntConst(Object o) {
    if(o==null)
      return funcAsInt(null);
    if(o instanceof String){
      String v = (String)o;
      if(StrFunc.isNumber(v)){
        double d = Double.parseDouble(v);
        return String.valueOf(Math.round(d));
      }
      //else throw  new RuntimeException("非数字类型转换；");
      else throw  new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.dialectimpl.notnumch", "非数字类型转换；"));
    }else if(o instanceof Number){
      Number num = (Number)o;
      return String.valueOf(num.intValue());
    }else{
      //throw new RuntimeException("不支持的转换；");
    	throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.dialectimpl.unsupportc", "不支持的转换；"));
    }
  }

  public String funcToSqlVar(String var ,int srcSqlType, int destsqltype ,String style){
    switch (destsqltype) {
      case Types.INTEGER:
      case Types.SMALLINT:
      case Types.TINYINT:
      case Types.BIGINT:{
        return funcToSqlVarInt(var,srcSqlType);
      }
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.BIT:
      case Types.REAL:
      case Types.DECIMAL:{
        return funcToSqlVarNum(var,srcSqlType);
      }
      case Types.VARCHAR:
      case Types.CHAR:{
        return funcToSqlVarStr(var,srcSqlType,style);
      }
      case Types.TIMESTAMP:
      case Types.TIME:{
        return funcToSqlVarTimeStamp(var,srcSqlType,style);
      }
      case Types.DATE:{
        return funcToSqlVarDate(var,srcSqlType,style);
      }
      case Types.BLOB:
      case Types.VARBINARY:
      case Types.BINARY:
      case Types.LONGVARBINARY://sybase
      {
        return var;
        //todo；
      }
      case Types.LONGVARCHAR: //DB2,Mssql,Oracle
      case Types.CLOB:        //Mysql,Sybase
      {
        return var;
        //todo;
      }
      case Types.BOOLEAN:
      case Types.ARRAY:
      case Types.DATALINK:
      case Types.JAVA_OBJECT:
      case Types.NULL:
      case Types.OTHER:
      case Types.REF:
      case Types.STRUCT:
      default:
        return funcToSqlConst(null,destsqltype);
    }
  }

  private String funcToSqlVarTimeStamp(String var, int srcSqlType, String style) {
    switch (srcSqlType) {
      case Types.INTEGER:
      case Types.SMALLINT:
      case Types.TINYINT:
      case Types.BIGINT:{
        return date2timestamp(funcCharToDate(funcAsStr(var),style));
      }
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.BIT:
      case Types.REAL:
      case Types.DECIMAL:{
        return date2timestamp(funcCharToDate(funcAsStr(funcAsInt(var)),style));
      }
      case Types.VARCHAR:
      case Types.CHAR:{
        return date2timestamp(funcCharToDate(var,style));
      }
      case Types.TIMESTAMP:
      case Types.TIME:{
        return var;
      }
      case Types.DATE:{
        return date2timestamp(var);
      }
      case Types.BLOB:
      case Types.VARBINARY:
      case Types.BINARY:
      case Types.LONGVARBINARY://sybase
      {
        //todo；
      }
      case Types.LONGVARCHAR: //DB2,Mssql,Oracle
      case Types.CLOB:        //Mysql,Sybase
      {
        //todo;
      }
      case Types.BOOLEAN:
      case Types.ARRAY:
      case Types.DATALINK:
      case Types.JAVA_OBJECT:
      case Types.NULL:
      case Types.OTHER:
      case Types.REF:
      case Types.STRUCT:
      default:
        return funcToDateTime(null);
    }
  }

  protected String date2timestamp(String var) {
    return var;
  }

  private String funcToSqlVarDate(String var, int srcSqlType, String style) {
    switch (srcSqlType) {
      case Types.INTEGER:
      case Types.SMALLINT:
      case Types.TINYINT:
      case Types.BIGINT:{
        return funcCharToDate(funcAsStr(var),style);
      }
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.BIT:
      case Types.REAL:
      case Types.DECIMAL:{
        return funcCharToDate(funcAsStr(funcAsInt(var)),style);
      }
      case Types.VARCHAR:
      case Types.CHAR:{
        return funcCharToDate(var,style);
      }
      case Types.TIMESTAMP:
      case Types.TIME:{
        return timestamp2date(var);
      }
      case Types.DATE:{
        return var;
      }
      case Types.BLOB:
      case Types.VARBINARY:
      case Types.BINARY:
      case Types.LONGVARBINARY://sybase
      {
        //todo；
      }
      case Types.LONGVARCHAR: //DB2,Mssql,Oracle
      case Types.CLOB:        //Mysql,Sybase
      {
        //todo;
      }
      case Types.BOOLEAN:
      case Types.ARRAY:
      case Types.DATALINK:
      case Types.JAVA_OBJECT:
      case Types.NULL:
      case Types.OTHER:
      case Types.REF:
      case Types.STRUCT:
      default:
        return funcToDate(null);
    }
  }

  protected String timestamp2date(String var) {
    return var;
  }

  private String funcToSqlVarStr(String var, int srcSqlType, String style) {
    switch (srcSqlType) {
      case Types.INTEGER:
      case Types.SMALLINT:
      case Types.TINYINT:
      case Types.BIGINT:{
        return funcAsStr(var);
      }
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.BIT:
      case Types.REAL:
      case Types.DECIMAL:{
        return funcAsStr(var);
      }
      case Types.VARCHAR:
      case Types.CHAR:{
        return var;
      }
      case Types.TIMESTAMP:
      case Types.TIME:
      case Types.DATE:{
        return funcDateToChar(var,style);
      }
      case Types.BLOB:
      case Types.VARBINARY:
      case Types.BINARY:
      case Types.LONGVARBINARY://sybase
      {
        //todo；
      }
      case Types.LONGVARCHAR: //DB2,Mssql,Oracle
      case Types.CLOB:        //Mysql,Sybase
      {
        //todo;
      }
      case Types.BOOLEAN:
      case Types.ARRAY:
      case Types.DATALINK:
      case Types.JAVA_OBJECT:
      case Types.NULL:
      case Types.OTHER:
      case Types.REF:
      case Types.STRUCT:
      default:
        return funcAsStr(null);
    }
  }

  private String funcToSqlVarNum(String var, int srcSqlType) {
    switch (srcSqlType) {
      case Types.INTEGER:
      case Types.SMALLINT:
      case Types.TINYINT:
      case Types.BIGINT:{
        return var;
      }
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.BIT:
      case Types.REAL:
      case Types.DECIMAL:{
        return var;
      }
      case Types.VARCHAR:
      case Types.CHAR:{
        return funcAsNum(var);
      }
      case Types.TIMESTAMP:
      case Types.TIME:{
      }
      case Types.DATE:{
      }
      case Types.BLOB:
      case Types.VARBINARY:
      case Types.BINARY:
      case Types.LONGVARBINARY://sybase
      {
        //todo；
      }
      case Types.LONGVARCHAR: //DB2,Mssql,Oracle
      case Types.CLOB:        //Mysql,Sybase
      {
        //todo;
      }
      case Types.BOOLEAN:
      case Types.ARRAY:
      case Types.DATALINK:
      case Types.JAVA_OBJECT:
      case Types.NULL:
      case Types.OTHER:
      case Types.REF:
      case Types.STRUCT:
      default:
        return funcAsNum(null);
    }
  }

  private String funcToSqlVarInt(String var, int srcSqlType) {
    switch (srcSqlType) {
      case Types.INTEGER:
      case Types.SMALLINT:
      case Types.TINYINT:
      case Types.BIGINT:{
        return var;
      }
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.BIT:
      case Types.REAL:
      case Types.DECIMAL:{
        return funcAsInt(var);
      }
      case Types.VARCHAR:
      case Types.CHAR:{
        return funcAsInt(var);
      }
      case Types.TIMESTAMP:
      case Types.TIME:{
      }
      case Types.DATE:{
      }
      case Types.BLOB:
      case Types.VARBINARY:
      case Types.BINARY:
      case Types.LONGVARBINARY://sybase
      {
        //todo；
      }
      case Types.LONGVARCHAR: //DB2,Mssql,Oracle
      case Types.CLOB:        //Mysql,Sybase
      {
        //todo;
      }
      case Types.BOOLEAN:
      case Types.ARRAY:
      case Types.DATALINK:
      case Types.JAVA_OBJECT:
      case Types.NULL:
      case Types.OTHER:
      case Types.REF:
      case Types.STRUCT:
      default:
        return funcAsInt(null);
    }
  }
  
  /**
   * 返回top n sql
   * 用于sybase,mssql 分页sql
   * @param querySelect
   * @param topnum
   * @return
   */
  public static String getTopPageSQl(String querySelect,int topnum){
    Pattern p = Pattern.compile("\\s*select\\s+(distinct\\s)?",Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
    Matcher mtc = p.matcher(querySelect);
    if(mtc.find()){
      StringBuffer qs = new StringBuffer(256).append("select");
      String dstr = mtc.group(1);
      if(dstr!=null)
        qs.append(" ").append(dstr);
      else qs.append(" ");
      qs.append("top ").append(topnum).append(" ");
      return mtc.replaceFirst(qs.toString());
    }
    return querySelect;
  }
  public int getStrLength(String str) throws SQLException {
    if(str==null) return -1;
    String sqlstr = getStrLengthSql(str);
    Connection con = getConnection();
    try {
      Statement st = con.createStatement();
      try {
        ResultSet rs = st.executeQuery(sqlstr);
        try {
          if(rs.next())
            return rs.getInt(1);
        }
        finally {
          rs.close();
        }
      }
      finally {
        st.close();
      }
    }
    finally {
      closeConnection(con);
    }
    return str.length();
  }

  protected abstract String getStrLengthSql(String str) ;
  
	public String formatLikeCondition(String value) {
		return formatLikeCondition(null, value, null,Dialect.ESCAPECHAR,true);
	}
	
	public String formatLikeCondition(String value,char escape,boolean escapeWildcard){
		return formatLikeCondition(null, value, null,escape,escapeWildcard);
	}
	
	public String formatLikeCondition(String prefix,String value,  String sufix) {
		return formatLikeCondition( prefix,value, sufix, Dialect.ESCAPECHAR,true);
	}

	public String formatLikeCondition(String prefix, String value, String sufix, char escape,boolean escapeWildcard) {
		if(escape=='\''){
//			throw new RuntimeException("转义字符不支持单引号；");
			throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.dialectimpl.notsingle", "转义字符不支持单引号；"));
		}
		String value2 = value;
		if (value2 != null && value2.length() > 0) {
			//转义特殊字符单引号
			value2 = formatConstStr(value2);
			//转义转义字符
			value2 = value2.replaceAll("\\" + escape, "\\" + escape + "\\" + escape);
			//转义通配符
			value2 = value2.replaceAll("%", "\\"+escape+"%");
			value2 = value2.replaceAll("_", "\\"+escape+"_");
			if(escapeWildcard){
			  value2 = value2.replace('*', '%');
			  value2 = value2.replace('?', '_');
			}
		}
		StringBuffer sql = new StringBuffer(32);
		sql.append("'");
		if (prefix != null && prefix.length() > 0){
			String ss = formatConstStr(prefix);
			ss = ss.replace('*', '%');
		    ss = ss.replace('?', '_');
			sql.append(ss);
		}
		if (value2 != null && value2.length() > 0) {
			sql.append(value2);
		}
		if (sufix != null && sufix.length() > 0){
			String ss = formatConstStr(sufix);
			ss = ss.replace('*', '%');
		    ss = ss.replace('?', '_');
			sql.append(ss);
		}
		sql.append("'");

		if (sql.indexOf(String.valueOf(escape)) >= 0){
			sql.append(" escape '").append(escape).append("'");
		}
		return sql.toString();
	}
	  
	public String formatConstStr(String value) {
		if (value != null)
			return value.replaceAll("'", "''");
		return value;
	}
	
	public String addQuote(String fieldname){
		return "\""+fieldname+"\"";
	}
	
  /**
   * 这里是Oracle,db2 语法；
   */
  public String getDeleteSql(String tbname,String tbname2 ,String[] keys, String[] keys2){
    StringBuffer delsql = new StringBuffer(64);
    delsql.append("delete from ").append(tbname).append(" a \n");
    delsql.append("where exists (select 1 from ").append(tbname2).append(" b ");
    delsql.append(" where ");
    for(int i=0;i<keys.length;i++){
      if(i>0) delsql.append(" and ");
      delsql.append("a.").append(keys[i]).append("=b.").append(keys2[i]);
    }
    delsql.append(')');
    return delsql.toString();
  }
  public String getCreateTableByQureySql(String tablename,String querysql,boolean istemp) {
    String sql = "CREATE TABLE "+ tablename;
    if(!SqlFunc.isSelect(querysql))
      querysql = "select * from "+ querysql;
    sql = sql+" AS "+querysql;
    return sql;
  }
  
	public String funcLenB(String f) {
		return funcLen(f);
	}

	public String funcMidB(String source, String i, String n) {
		return funcMid(source, i, n);
	}

	public String funcLeftB(String source, String len) {
		return funcLeft(source, len);
	}

	public String funcRightB(String source, String len) {
		return funcRight(source, len);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean supportsDenseRank() {
		/*
		 * BUG:BI-8606: add by liujin 2013.06.27
		 * Teradata 数据库不支持 dense_rank 函数。
		 */
		return true;
	}

	/**
	 * 是否支持full join连接方式
	 * 
	 * @return boolean 是否支持full join
	 */
	public boolean supportsFullJoin() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcCoalesce(String[] expressions) {
		if (expressions == null || expressions.length == 0) {
			return "null";
		}

		if (expressions.length == 1) {
			return expressions[0];
		}

		StringBuffer funcsql = new StringBuffer(64);
		funcsql.append(" COALESCE(");
		for (int i = 0; i < expressions.length; i++) {
			if (i != 0) {
				funcsql.append(",");
			}
			funcsql.append(expressions[i]);
		}
		funcsql.append(") ");

		return funcsql.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTruncateTableSql(String tablename) {
		return "TRUNCATE TABLE " + tablename;
	}
	
	/**
	 * {@inheritDoc}
	 */	
	public boolean supportCreateTableInTransaction() {
		return true;
	}
}
