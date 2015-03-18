package com.esen.jdbc.dialect.impl.sybase;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.impl.DialectImpl;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * <p>Title: Esensoft JDBC</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Esensoft</p>
 * @author dw
 * @version 1.0
 */

public class SybaseDialect extends DialectImpl {
  private static final int VARCHAR_LEN = 255;

  public SybaseDialect(Object f) {
    super(f);
  }

  public DbDefiner createDbDefiner() {
    return new SybaseDef(this);
  }
  public DbMetaData createDbMetaData() throws SQLException {
    if(dbmd==null){
      dbmd = connectionFactory!=null?new SybaseDbMetaData(connectionFactory):new SybaseDbMetaData(con);
    }
    return dbmd;
  }
  
  public DbMetaData createDbMetaData(Connection conn) throws SQLException {
    return new SybaseDbMetaData(conn);
  }
  
  public boolean canSetNumAsStr() {
    return false;
  }

  public int getMaxColumn() {
    return 1024;
  }

  /**
   * supportsLimit
   *
   * @return boolean
   */
  public boolean supportsLimit() {
    return true;
  }

  /**
   * supportsLimitOffset
   *
   * @return boolean
   */
  public boolean supportsLimitOffset() {
    return true;
  }

  /**
   * getLimitString
   * Sybase 不支持top语法；
   * @param querySelect String
   * @param offset int
   * @param limit int
   * @return String
   */
  public String getLimitString(String querySelect, int offset, int limit) {
    if(offset<0||limit<=0)
      return querySelect;
    StringBuffer sql = new StringBuffer(querySelect.length() + 50);
    if(offset>0){
      sql.append("/*STARTINDEX:").append(offset).append("*/");
    }
    sql.append("set rowcount ").append(offset+limit).append("\r\n");
    sql.append(querySelect);
    sql.append("\r\nset rowcount 0");
    return sql.toString();
  }

  /**
   * resultEndWithIndex
   *
   * @return boolean
   */
  public boolean resultEndWithIndex() {
    return false;
  }

  /**
   * funcToSql
   *
   * @param LocalFunc String
   * @param params ArrayList
   * @return String
   */
  public String funcToSql(String LocalFunc, ArrayList params) {
    return "";
  }

  /**
   * funcChar
   *
   * @param ascii String
   * @return String
   */
  public String funcChar(String ascii) {
    StringBuffer sBuf = new StringBuffer(20).append(" CHAR(").append(ascii)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcCode
   *
   * @param sourece String
   * @return String
   */
  public String funcCode(String sourece) {
    StringBuffer sBuf = new StringBuffer(50).append(" ASCII(").append(sourece)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcFind
   * 20090907
   * 原来的代码参数调用反了；
   * @param sub String
   * @param toFind String
   * @return String
   */
  public String funcFind(String sub, String toFind) {
    if(sub==null||toFind==null)
      return sub;
    StringBuffer sBuf = new StringBuffer(100).append("(CHARINDEX(").append(
        sub).append(',').append(toFind).append(")-1)"); //数据库是从1开始计算位置
    return sBuf.toString();
  }

  /**
   * funcLeft
   *
   * @param source String
   * @param len String
   * @return String
   */
  public String funcLeft(String source, String len) {
    StringBuffer sBuf = new StringBuffer(50).append(" LEFT(").append(source)
        .append(",").append(len).append(')');
    return sBuf.toString();
  }

  /**
   * funcRight
   *
   * @param source String
   * @param len String
   * @return String
   */
  public String funcRight(String source, String len) {
    StringBuffer sBuf = new StringBuffer(50).append(" RIGHT(").append(source)
        .append(",").append(len).append(')');
    return sBuf.toString();
  }

  /**
   * funcMid
   *
   * @param field String
   * @param iFrom String
   * @param len String
   * @return String
   */
  public String funcMid(String field, String iFrom, String len) {
    if(iFrom==null||len==null) return field;
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

  /**
   * funcLen
   * 对于cp936中文字符集：
   * len('中国a')=5 
   * char_length('中国a')=3 
   * @param field String
   * @return String
   */
  public String funcLen(String field) {
    StringBuffer sBuf = new StringBuffer(50).append(" char_length(").append(field)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcLower
   *
   * @param field String
   * @return String
   */
  public String funcLower(String field) {
    StringBuffer sBuf = new StringBuffer(50).append(" LOWER(").append(field)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcUpper
   *
   * @param field String
   * @return String
   */
  public String funcUpper(String field) {
    StringBuffer sBuf = new StringBuffer(50).append(" UPPER(").append(field)
        .append(')');
    return sBuf.toString();
  }

  /**
   * 20090824
   * SybaseAse使用charindex函数实现；
   * 区分大小写;
   * 例：charindex('a','abc')=1
   *     charindex('中','a无b中c')=4 //中文字符按字符算；
   *     charindex('ac','abc')=0  //找不到返回0
   *     charindex(null,'abc')=0
   *     charindex('a',null)=0
   * 两个参数允许空值；
   * @param sub String
   * @param toSearch String
   * @return String
   */
  public String funcSearch(String sub, String toSearch) {
    StringBuffer sBuf = new StringBuffer(32);
    sBuf.append(" ( CHARINDEX(UPPER(").append(sub).append(") , UPPER(");
    sBuf.append(toSearch).append(") ) -1)"); //数据库是从1开始计算位置
    return sBuf.toString();
  }

  /**
   * funcRepeat
   *
   * @param field String
   * @param count String
   * @return String
   */
  public String funcRepeat(String field, String count) {
    return field;
  }

  /**
   * funcStrCat
   *
   * @param field1 String
   * @param field2 String
   * @return String
   */
  public String funcStrCat(String field1, String field2) {
    return field1+"+"+field2;
  }

  /**
   * funcWholeReplace
   *
   * @param source String
   * @param oldSub String
   * @param newSub String
   * @return String
   */
  public String funcWholeReplace(String source, String oldSub, String newSub) {
    StringBuffer sBuf = new StringBuffer(50).append(" REPLACE(").append(source)
        .append(',').append(oldSub).append(',').append(newSub).append(')');
    return sBuf.toString();
  }

  /**
   * funcTrim
   * SybaseASE 不支持trim，支持ltrim,rtrim
   * @param field String
   * @return String
   */
  public String funcTrim(String field) {
    StringBuffer sBuf = new StringBuffer(40).append(" ltrim(rtrim(").append(field)
        .append("))");
    return sBuf.toString();
  }

  /**
   * funcNullvalue
   *
   * @param field String
   * @param defaultValue String
   * @return String
   */
  public String funcNullvalue(String field, String defaultValue) {
    return "ISNULL(" + field + "," + defaultValue+ ")";
  }
  public String ifNull(String str,String str2){
    return "ISNULL(" + str + "," + str2+ ")";
  }
  /**
   * funcToday
   *
   * @return String
   */
  public String funcToday() {
    return "getdate()";
  }
  public String funcNow(){
    return "getdate()";
  }
  /**
   * funcAbs
   *
   * @param d String
   * @return String
   */
  public String funcAbs(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" ABS(").append(d).append(
        ')');
    return sBuf.toString();
  }

  /**
   * funcC
   *
   * @param d String
   * @return String
   */
  public String funcC(String d) {
    return "";
  }

  /**
   * funcCos
   *
   * @param d String
   * @return String
   */
  public String funcCos(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" COS(").append(d).append(
        ')');
    return sBuf.toString();
  }

  /**
   * funcSin
   *
   * @param d String
   * @return String
   */
  public String funcSin(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" SIN(").append(d).append(
        ')');
    return sBuf.toString();
  }

  /**
   * funcTan
   *
   * @param d String
   * @return String
   */
  public String funcTan(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" TAN(").append(d).append(
        ')');
    return sBuf.toString();
  }

  /**
   * funcEven
   *
   * @param d String
   * @return String
   */
  public String funcEven(String d) {
    return "";
  }

  /**
   * funcExp
   *
   * @param d String
   * @return String
   */
  public String funcExp(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" EXP(").append(d).append(
        ')');
    return sBuf.toString();
  }

  /**
   * funcSqrt
   *
   * @param d String
   * @return String
   */
  public String funcSqrt(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" SQRT(").append(d).append(
        ')');
    return sBuf.toString();
  }

  /**
   * funcFact
   *
   * @param d String
   * @return String
   */
  public String funcFact(String d) {
    return "";
  }

  /**
   * funcInt
   *
   * @param d String
   * @return String
   */
  public String funcInt(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" cast(").append(d)
        .append(" as int)");//截整
    return sBuf.toString();
  }

  /**
   * funcSign
   *
   * @param d String
   * @return String
   */
  public String funcSign(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" SIGN(").append(d).append(
        ')');
    return sBuf.toString();
  }

  /**
   * funcLn
   * 自然对数
   * @param d String
   * @return String
   */
  public String funcLn(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" LOG(").append(d).append(
        ')');
    return sBuf.toString();
  }

  /**
   * funcLog
   *
   * @param d String
   * @param dValue String
   * @return String
   */
  public String funcLog(String d, String dValue) {
    return "";//不支持
  }

  /**
   * funcMod
   *
   * @param iValue String
   * @param i String
   * @return String
   */
  public String funcMod(String iValue, String i) {
    StringBuffer sBuf = new StringBuffer(40).append(iValue)
        .append("%").append(i);
    return sBuf.toString();
  }

  /**
   * funcPi
   *
   * @return String
   */
  public String funcPi() {
    return "PI()";
  }

  /**
   * funcPower
   *
   * @param dValue String
   * @param d String
   * @return String
   */
  public String funcPower(String dValue, String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" POWER(").append(dValue)
        .append(',').append(d).append(')');
    return sBuf.toString();
  }

  /**
   * funcRand
   *
   * @return String
   */
  public String funcRand() {
    return "RAND()";
  }

  /**
   * funcRound
   *
   * @param d String
   * @param i String
   * @return String
   */
  public String funcRound(String d, String i) {
    StringBuffer sBuf = new StringBuffer(40).append(" ROUND(").append(d)
        .append(',').append(i).append(')');
    return sBuf.toString();
  }

  /**
   * funcTrunc
   *convert(numeric(38,2),2345.233) -- 2345.23
   * @param d String
   * @param i String
   * @return String
   */
  public String funcTrunc(String d, String i) {
    if(d==null||i==null) return null;
    StringBuffer sBuf = new StringBuffer(40).append(" convert(numeric(38,")
       .append(i).append("),").append(d).append(')');
    return sBuf.toString();
  }

  /**
   * select convert(date,'20070809',112)
      select convert(date,'2007-08-09')
      select convert(date,'20070809')
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
    StringBuffer sql = new StringBuffer(32);
    sql.append("convert(date,'").append(date).append("')");
    return sql.toString();
  }

  /**
   * select convert(char,getdate(),111) --'2003/09/27'
select convert(char,getdate(),112) --'20030927'
select rtrim(convert(char,getdate(),112))+' '+(convert(char,getdate(),108)) -- '20031112 11:03:41'
select datepart(yy,getdate()) --year
select datepart(mm,getdate()) --month
select datepart(dd,getdate()) --day  1-31
select datepart(hh,getdate()) --hour
select datepart(mi,getdate()) --min
select datepart(ss,getdate()) --sec
select datepart(ms,getdate()) --Millisecond 0 – 999

select datepart(wk,getdate())  week 1 - 54
select datepart(qq,getdate())  季度 1-4
select datepart(dy,getdate()) days 1-356
select datepart(dw,getdate()) weekday  1-7
   */
  public String funcDateToChar(String datefield, String style) {
    if(style==null||style.length()==0)
      style = "YYYYMMDD";
    style = style.trim().toUpperCase();
    StringBuffer sql = new StringBuffer(32);
    if(style.equals("YYYYMMDD")){
      //YYYYMMDD
      sql.append("convert(char,").append(datefield).append(",112)");
    }else if(style.equals("YYYY-MM-DD")){
      //YYYY-MM-DD
      String datestr = "convert(char,"+datefield+",112)";
      sql.append(funcLeft(datestr, "4"));
      sql.append("||'-'||").append(funcMid(datestr,"4","2"));
      sql.append("||'-'||").append(funcMid(datestr,"6","2"));
    }else if(style.equals("YYYYMM")){
    //YYYYMM
      String datestr = "convert(char,"+datefield+",112)";
      sql.append(funcLeft(datestr, "4"));
      sql.append("||").append(funcMid(datestr,"4","2"));
    }else if(style.equals("YYYYMM--")){
    //YYYYMM--
      String datestr = "convert(char,"+datefield+",112)";
      sql.append(funcLeft(datestr, "4"));
      sql.append("||").append(funcMid(datestr,"4","2"));
      sql.append("||'--'");
    }else if(style.equals("YYYY")){
    //YYYY
    // datepart 的返回值是 int, 需要转成字符类型
      sql.append("CONVERT(VARCHAR, datepart(yy,").append(datefield).append("))");
    }else if(style.equals("YYYY----")){//YYYY----
      String datestr = "convert(char,"+datefield+",112)";
      sql.append(funcLeft(datestr, "4"));
      sql.append("||'----'");
    }else if(style.equals("MM")){//MM
      sql.append("CONVERT(VARCHAR, datepart(mm,").append(datefield).append("))");
    }else if(style.equals("DD")){//DD
      sql.append("CONVERT(VARCHAR, datepart(dd,").append(datefield).append("))");
    }else if(style.equals("WW")||style.equals("WK")){//week
      sql.append("CONVERT(VARCHAR, datepart(wk,").append(datefield).append("))");
    }else if(style.equals("Q")||style.equals("QQ")){//季度
      sql.append("CONVERT(VARCHAR, datepart(qq,").append(datefield).append("))");
    } else if (style.equals("YYYY-MM")) {
    	sql.append("CONVERT(VARCHAR, datepart(yy,").append(datefield).append("))");
    	sql.append(" || '-' || ");
    	sql.append("CONVERT(VARCHAR, datepart(mm,").append(datefield).append("))");
    }
    return sql.toString();
  }
  /**
   * select convert(date,'20070809',112)
      select convert(date,'2007-08-09')
      select convert(date,'20070809')
   */
  public String funcCharToDate(String charfield,String style){
    if(style==null||style.length()==0)
      return "convert(date,"+charfield+")";
    if(style.matches("[yY]{4}[mM]{2}[dD]{2}"))
      return "convert(date,"+charfield+",112)";
    if(style.matches("[yY]{4}[mM]{2}-{0,2}"))
      return "convert(date,"+funcLeft(charfield,String.valueOf(6))+"||'01')";
    if(style.matches("[yY]{4}-{0,4}"))
      return "convert(date,"+funcLeft(charfield,String.valueOf(4))+"||'0101')";
    return "convert(date,"+charfield+")";
  }
  /**
   * years(datefield,offset)
   * months(datefield,offset)
   * days(datefield,offset)
   * 或者：
   * DATEADD( date-part, numeric-expression, date-expr )
 使用DATEADD函数，可以对日期进行不同时间单位的加减操作：
1 select dateadd(day,-5,datefield)        as hd_minus_5D,
2        dateadd(day,5,datefield)                  as hd_plus_5D,
3        dateadd(month,-5,datefield)         as hd_minus_5M,
4        dateadd(month,5,datefield)                as hd_plus_5M,
5        dateadd(year,-5,datefield)           as hd_minus_5Y,
6        dateadd(year,5,datefield)            as hd_plus_5Y
7   from emp
8  where deptno = 10
   */
  public String formatOffsetDate(String datefield,int offset,char t){
    switch(t){
      case 'y':
      case 'Y':
        return "dateadd(year,"+offset+","+datefield+")";
      case 'm':
      case 'M':
        return "dateadd(month,"+offset+","+datefield+")";
      case 'd':
      case 'D':
        return "dateadd(day,"+offset+","+datefield+")";
    }
//    throw new RuntimeException("不支持的类型（y[y],m[M],d[D]）："+t);
    throw new RuntimeException(I18N.getString("JDBC.COMMON.UNSUPPORTEDTYPE", "不支持的类型（y[y],m[M],d[D]）：")+t);
  }
  /**
   * select convert(datetime,'2007-08-09 12:20:45')
   * select convert(datetime,'20070809 12:20:45')
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
    return "convert(datetime,'" + dtstr + "')";
  }

  public String funcDay(String datefield) {
    return "day(" + datefield + ")";
  }

  public String funcMonth(String datefield) {
    return "month(" + datefield + ")";
  }

  public String funcYear(String datefield) {
    return "year(" + datefield + ")";
  }


  public String funcAsInt(String v){
    return "cast("+v+" as int)";
  }
  public String funcAsNum(String v){
    return "cast("+v+" as NUMERIC(15,2))";
  }
  /**
   * BI-5557
   * 将数据值型转换成字符型，如果是char，会按数值类型的最大长度去转，前后有很多空格；
   * 转换成varchar就没有这个问题；
   */
  public String funcAsStr(String v){
    return "cast("+v+" as varchar)";
  }
  public String funcDays(String datefield,String datefield2){
    return "abs(datediff(dd,"+datefield+","+datefield2+"))+1";
  }
  protected String getStrLengthSql(String str) {
    StringBuffer sql = new StringBuffer(32);
    sql.append("select ").append(funcLen("'"+str+"'"));
    return sql.toString();
  }
  /**
   * select * into temp_12345 from ( select * from test_b0) qx$_   一般的物理表
   * select * into temp_12345 from test_b0
   * select * into #temp_12345 from ( select * from test_b0) qx$_  局部临时表
   * select * into ##temp_12345 from ( select * from test_b0) qx$_ 全局临时表
   */
  public String getCreateTableByQureySql(String tablename,String querysql,boolean istemp) {
    if(SqlFunc.isSelect(querysql))
      return "select * into "+tablename+" from ("+querysql+") qx$_ ";
    return "select * into "+tablename+" from "+querysql;
  }
  /**
   * delete tbname from tbname as t1,tbname2 as t2 where t1.key1=t2.key1 and t1.key2=t2.key2
   */
  public String getDeleteSql(String tbname,String tbname2 ,String[] keys, String[] keys2){
    StringBuffer delsql = new StringBuffer(64);
    delsql.append("delete ").append(tbname).append(" from ").append(tbname).append(" as t1,");
    delsql.append(tbname2).append(" as t2 \n");
    delsql.append("where ");
    for(int i=0;i<keys.length;i++){
      if(i>0) delsql.append(" and ");
      delsql.append("t1.").append(keys[i]).append("=t2.").append(keys2[i]);
    }
    return delsql.toString();
  }
  
  /**
   * SybaseASE： datediff(ss, date1, date2)
   * SybaseIQ：datediff(ss, date1, date2)
   * SqlServer： datediff(ss, date1, date2)
   */
  public String funcSeconds(String datefield, String datefield2) {
    StringBuffer sql = new StringBuffer(32);
    sql.append("datediff(ss,").append(datefield2).append(',').append(datefield).append(')');
    return sql.toString();
  }
  
	/**
	 * {@inheritDoc}
	 * 
	 * Sybase 不支持在事务中创建表
	 */	
	public boolean supportCreateTableInTransaction() {
		return false;
	}
}
