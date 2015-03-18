package com.esen.jdbc.dialect.impl.sybase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.util.StrFunc;

public class SybaseIQDialect extends SybaseDialect {

  public SybaseIQDialect(Object f) {
    super(f);
  }
  
  public DbDefiner createDbDefiner() {
    return new SybaseIQDef(this);
  }
  public DbMetaData createDbMetaData() throws SQLException {
    if(dbmd==null){
      dbmd = connectionFactory!=null?new SybaseIQDbMetaData(connectionFactory):new SybaseIQDbMetaData(con);
    }
    return dbmd;
  }
  public DbMetaData createDbMetaData(Connection conn) throws SQLException {
    return new SybaseIQDbMetaData(conn);
  }
  /**
   * SybsseIQ中支持trim,ltrim,rtrim
   */
  public String funcTrim(String field) {
    StringBuffer sBuf = new StringBuffer(40).append(" trim(").append(field)
        .append(')');
    return sBuf.toString();
  }
  
  /**
   * sybaseiq 支持top语法
   */
  public String getLimitString(String querySelect, int offset, int limit) {
    if(offset<0||limit<=0)
      return querySelect;
    StringBuffer sql = new StringBuffer(querySelect.length() + 50);
    if(offset>0){
      sql.append("/*STARTINDEX:").append(offset).append("*/");
    }
    sql.append(getTopPageSQl(querySelect, offset+limit));
    //不使用set rowcount是因为它影响了一个链接中的所有事务；
    /*sql.append("set rowcount ").append(offset+limit).append(" ");
    sql.append(querySelect);*/
    return sql.toString();
  }
  /**
   * date('20050202')
   * date('2005-02-02')
   */
  public String funcToDate(String date) {
    if (date == null || date.length() == 0)
      return "cast(null as date)";
    if (Pattern.matches("[0-9]{4}----", date)) {
      date = date.substring(0,4)+"0101";
    }
    if (Pattern.matches("[0-9]{4}", date)) {
      date = date+"0101";
    }
    if (Pattern.matches("[0-9]{4}[0-9]{2}--", date)) {
      date = date.substring(0,6)+"01";
    }
    if (Pattern.matches("[0-9]{4}[0-9]{2}", date)) {
      date = date+"01";
    }
    StringBuffer sql = new StringBuffer(20);
    sql.append("date('").append(date).append("')");
    return sql.toString();
  }
  /**
   * datetime('2006-01-01 21:34:02')
   */
  public String funcToDateTime(String dtstr) {
    if(dtstr==null||dtstr.length()==0)
      return "cast(null as datetime)";
    if (Pattern.matches("[0-9]{4}----", dtstr)) {
      dtstr = dtstr.substring(0,4)+"0101";
    }
    if (Pattern.matches("[0-9]{4}", dtstr)) {
      dtstr = dtstr+"0101";
    }
    if (Pattern.matches("[0-9]{4}[0-9]{2}--", dtstr)) {
      dtstr = dtstr.substring(0,6)+"01";
    }
    if (Pattern.matches("[0-9]{4}[0-9]{2}", dtstr)) {
      dtstr = dtstr+"01";
    }
    return "datetime('" + dtstr + "')";
  }
  /**
   * dateformat(today(),'yyyymmdd')
   */
  public String funcDateToChar(String datefield, String style) {
    if(style==null||style.length()==0)
      style = "yyyymmdd";
    if(style.matches("[Qq]{1,2}")){
      return "QUARTER("+datefield+")";
    }
    StringBuffer sql = new StringBuffer(20);
    sql.append("dateformat(");
    sql.append(datefield);
    sql.append(",'");
    sql.append(style);
    sql.append("')");
    return sql.toString();
  }
  public String funcCharToDate(String charfield,String style){
    if(style==null||style.length()==0)
      return "date("+charfield+")";
    if(style.matches("[yY]{4}[mM]{2}[dD]{2}"))
      return "date("+charfield+")";
    if(style.matches("[yY]{4}[mM]{2}"))
      return "date("+charfield+"||'01')";
    if(style.matches("[yY]{4}[mM]{2}.*"))
      return "date("+funcLeft(charfield,String.valueOf(6))+"||'01')";
    if(style.matches("[yY]{4}.*"))
      return "date("+funcLeft(charfield,String.valueOf(4))+"||'0101')";
    return "date("+charfield+")";
  }
  protected String getResultMetaData_Sql(String sql) {
  	/*
  	 * ISSUE:BI-8226 add by jzp 2013.04.02
  	 * sybase IQ 执行sql时，没有判断是存储过程还是sql,导致错误
  	 */
		if (SqlFunc.isCallalbe(sql)) {
			return sql;
		}
    sql = sql.replaceAll("\\?", "''");
    sql = Pattern.compile("order\\s+by\\s+(\\w|,)+(\\sdesc|\\sasc){0,1}",Pattern.CASE_INSENSITIVE|Pattern.DOTALL).matcher(sql).replaceAll("");
    StringBuffer sqlstr = new StringBuffer(sql.length()+40);
    sqlstr.append("select * from( ").append(sql);
    sqlstr.append(") x$_ where 1=0");
    return sqlstr.toString();
  }
  public String funcLen(String field) {
    StringBuffer sBuf = new StringBuffer(50).append(" LENGTH(").append(field)
        .append(')');
    return sBuf.toString();
  }
  public String funcDays(String datefield,String datefield2){
    return "abs(days("+datefield+","+datefield2+"))+1";
  }
  public String funcInt(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" TRUNC(").append(d)
        .append(",0)");//截整
    return sBuf.toString();
  }
  public String funcMod(String iValue, String i) {
    StringBuffer sBuf = new StringBuffer(40).append(" MOD(").append(iValue)
        .append(',').append(i).append(')');
    return sBuf.toString();
  }
  public String funcTrunc(String d, String i) {
    StringBuffer sBuf = new StringBuffer(40).append(" TRUNCNUM(").append(d)
        .append(',').append(i).append(')');
    return sBuf.toString();
  }
  
  public String funcMid(String field, String iFrom, String len) {
    StringBuffer sb = new StringBuffer(50).append("SUBSTRING(").append(field)
        .append(","); //系统函数从0开始
    if(iFrom==null) iFrom = "0";
    int ifrm = StrFunc.str2int(iFrom, Integer.MIN_VALUE);
    if (ifrm == Integer.MIN_VALUE) {
      sb.append(iFrom).append("+1");
    }
    else {
      sb.append(ifrm + 1);
    }
    if (len != null && len.length() > 0) {
      sb.append(",").append(len);
    }
    sb.append(')');
    return sb.toString();
  }
  
  public String funcToday() {
    return "today()";
  }
  public static void main(String[] args){
    String sql = "select * from aaa where aadd>34 \r\n order  by \r\n  ddd  ";
    System.out.println(Pattern.compile("order\\s+by\\s+\\w+",Pattern.CASE_INSENSITIVE|Pattern.DOTALL).matcher(sql).replaceAll(""));
    
    sql = "select * from aaa where aadd>34 orDer by ddd,aaa desc";
    System.out.println(Pattern.compile("order\\s+by\\s+(\\w|,)+(\\sdesc|\\sasc){0,1}",Pattern.CASE_INSENSITIVE|Pattern.DOTALL).matcher(sql).replaceAll(""));
  }
}
