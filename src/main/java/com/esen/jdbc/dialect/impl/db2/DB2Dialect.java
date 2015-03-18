package com.esen.jdbc.dialect.impl.db2;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.impl.DialectImpl;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

import java.sql.*;
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

public class DB2Dialect extends DialectImpl {
  
  public DB2Dialect(Object f) {
    super(f);
  }

  public DbDefiner createDbDefiner() {
    return new DB2Def(this);
  }
  public DbMetaData createDbMetaData() throws SQLException {
    if(dbmd==null){
      dbmd = connectionFactory!=null?new DB2DbMetaData(connectionFactory):new DB2DbMetaData(con); 
    }
    return dbmd;
  }
  
  public DbMetaData createDbMetaData(Connection conn) throws SQLException {
    return new DB2DbMetaData(conn);
  }
  
  public String getInsertSelectSql(String selClause, String fromClause,
      String whereClause, String intoClause) {
    return "INSERT " + intoClause + " SELECT " + selClause + " FROM "
        + fromClause + " " + whereClause;
  }

  public boolean supportsLimit() {
    return true;
  }

  public boolean supportsLimitOffset() {
    return true;
  }
  
  public boolean supportsViewColumnComment() {
	return true; 
  }
  
  public boolean supportsTableComment() {
	return true; 
  }
  
  public boolean supportsTableColumnComment() {
	return true; 
  }

  public String funcStdev(String field){
    return "stddev("+field+")";
  }
/*  private static boolean hasDistinct(String sql) {
    return sql.toLowerCase().indexOf("select distinct") >= 0;
  }*/

  public boolean canSetNumAsStr() {
    return false;
  }
/*  private String getRowNumber(String sql) {
    StringBuffer rownumber = new StringBuffer(50).append("rownumber() over(");
    int orderByIndex = sql.toLowerCase().indexOf("order by");
    if (orderByIndex > 0 && !hasDistinct(sql)) {
      rownumber.append(sql.substring(orderByIndex));
    }
    rownumber.append(") as rownumber-,");
    return rownumber.toString();
  }*/

  //这个相对于 getLimitString_01 没有进一步优化 order by
  public String getLimitString(String querySelect, int offset, int limit) {
    if(offset<0||limit<=0)
      return querySelect;
    int startOfSelect = querySelect.toLowerCase().indexOf("select");
    if (startOfSelect > 0) {
      querySelect = querySelect.substring(startOfSelect); // 去掉/*commaen*/
    }
    StringBuffer pagingSelect = new StringBuffer(querySelect.length() + 120);
    pagingSelect
        .append("select * from ( select row_.*, rownumber() over() as rownum_ from ( ");

    pagingSelect.append(querySelect);
    if (offset > 0) {
      pagingSelect.append(" ) row_ ) row0_ where rownum_<= "
          + (offset + limit) + " and rownum_ > " + offset);
    }
    else {
      pagingSelect.append(" ) row_ ) row0_  where rownum_<= " + limit);
    }
    return pagingSelect.toString();
  }

  /*
   //注意,该方式对 select * from tableName
   //子查询中有 order by 的查询没有处理,且第一行为序号行
   public String getLimitString_01(String querySelect, int offset, int limit) {
   querySelect = getLowNoSpaceString(querySelect);
   int startOfSelect = querySelect.indexOf("select"); commaen* select
   StringBuffer pagingSelect = new StringBuffer(querySelect.length() + 100)
   .append(querySelect.substring(0, startOfSelect)) //add the comment
   .append("select * from ( select ") //nest the main query in an outer select
   .append(getRowNumber(querySelect)); //add the rownnumber bit into the outer query select list
   if (hasDistinct(querySelect)) {
   pagingSelect.append(" row_.* from ( ") //add another (inner) nested select
   .append(querySelect.substring(startOfSelect)) //add the main query
   .append(" ) as row_"); //close off the inner nested select
   }
   else {
   pagingSelect.append(querySelect.substring(startOfSelect + 6)); //add the main query
   }
   pagingSelect.append(" ) as temp_ where rownumber_ ");
   if (offset > 0) {
   pagingSelect.append("between " + (offset + 1) + " and  " +
   (offset + limit));
   }
   else {
   pagingSelect.append("<= " + limit);
   }
   return pagingSelect.toString();
   }
   */
  /**
   * resultEndWithIndex
   *
   * @return boolean
   */
  public boolean resultEndWithIndex() {
    return true;
  }

  /**
   * queryLimit
   *
   * @param stmt Statement
   * @param querySelect String
   * @param offset int
   * @param limit int
   * @return ResultSet
   */
  /*public ResultSet queryLimit(Statement stmt, String querySelect, int offset,
      int limit) throws SQLException {
    super.checkParam(querySelect, offset, limit);
    querySelect = getLimitString(querySelect, offset, limit);
    ResultSet rst = null;
    rst = stmt.executeQuery(querySelect);
    return rst;
  }*/

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
    super.tno("C");
    return ""; //不能实现
  }

  /**
   * funcChar
   *
   * @param ascii String
   * @return String
   */
  public String funcChar(String ascii) {
    if(ascii==null||ascii.trim().length()==0)
      return "cast(null as char)";
    StringBuffer sBuf = new StringBuffer(20).append(" CHR(").append(ascii)
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
    if(sourece==null||sourece.trim().length()==0)
      return "cast(null as int)";
    StringBuffer sBuf = new StringBuffer(20).append(" ASCII(").append(sourece)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcCos
   *
   * @param d String
   * @return String
   */
  public String funcCos(String d) {
    if(d==null||d.trim().length()==0){
      return "cast(null as double)";
    }
    StringBuffer sBuf = new StringBuffer(40).append(" COS(").append(d).append(
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
    super.tno("Even");
    return ""; //没有对应函数
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
   * funcFact
   *
   * @param d String
   * @return String
   */
  public String funcFact(String d) {
    super.tno("Fact");
    return "";//no local
  }

  /**
   * funcFind
   * 与Search不同，FIND区分大小写
   * @param sub String
   * @param toFind String
   * @return String
   */
  public String funcFind(String sub, String toFind) {
    StringBuffer sBuf = new StringBuffer(32);
    sBuf.append(" ( LOCATE(").append(sub).append(" , ");
    sBuf.append(toFind).append(") -1)"); //数据库是从1开始计算位置
    return sBuf.toString();
  }

  /**
   * funcInt
   *
   * @param d String
   * @return String
   */
  public String funcInt(String d) {
    if(d==null||d.trim().length()==0){
      return "cast(null as int)";
    }
    //TRUNC(-125.1115,-1)=-120.0000 即会用0填充来保持精度
    StringBuffer sBuf = new StringBuffer(40).append(" INT(")//或者INTEGER
        .append(d).append(')');//去尾
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
    if(len==null||len.trim().length()==0)
      return "cast(null as char)";
    return funcMid(source, String.valueOf(0), len);
  }

  /**
   * funcLen
   * length('中国a') =5 
   * length(vargraphic('中国a')) =3 
   * @param field String
   * @return String
   */
  public String funcLen(String field) {
    if(field==null||field.trim().length()==0)
      return "cast(null as int)";
    StringBuffer sBuf = new StringBuffer(50).append(" LENGTH(").append(field)
        .append(")");
    return sBuf.toString();
  }

  /**
   * funcLn
   *
   * @param d String
   * @return String
   */
  public String funcLn(String d) {
    if(d==null||d.trim().length()==0){
      return "cast(null as double)";
    }
    StringBuffer sBuf = new StringBuffer(50).append(" LN(").append(d).append(
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
    if(d==null||d.trim().length()==0){
      return "cast(null as double)";
    }
    StringBuffer sBuf = new StringBuffer(50).append(" LOG(").append(d).append(
        ')');
    return sBuf.toString();
  }

  /**
   * funcLower
   *
   * @param field String
   * @return String
   */
  public String funcLower(String field) {
    if(field==null)
      return "cast(null as char)";
    StringBuffer sBuf = new StringBuffer(50).append(" LOWER(").append(field)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcMid
   * substr('中国a',1,2) ='中' 
   * substr(vargraphic('中国a'),1,2)='中国' 
   * substr(vargraphic('中国a'),2)='国a' 
   * @param field String
   * @param iFrom String
   * @param len String
   * @return String
   */
  public String funcMid(String field, String iFrom, String len) {
    if(field==null||field.trim().length()==0)
      return "cast(null as char)";
    if(iFrom==null||iFrom.trim().length()==0)
      return "cast(null as char)";
    StringBuffer sBuf = new StringBuffer(50).append(" SUBSTR(").append(field)
        .append(" , ");
    if(StrFunc.isNumber(iFrom)){
      int start = Integer.parseInt(iFrom);
      sBuf.append(start+1);
    }else
      sBuf.append("(").append(iFrom).append(")+ 1"); //本系统从0开始
    if(len==null||len.trim().length()==0){
      sBuf.append(')');
    }else{
      sBuf.append(',').append(len).append(')');
    }
    return sBuf.toString();
  }

  /**
   * funcMod
   *
   * @param iValue String
   * @param i String
   * @return String
   */
  public String funcMod(String iValue, String i) {
    if(iValue==null||iValue.trim().length()==0){
      return "cast(null as int)";
    }
    if(i==null||i.trim().length()==0){
      return "cast(null as double)";
    }
    StringBuffer sBuf = new StringBuffer(40)
    .append(" MOD(")
    .append(iValue)
    .append(',')
    .append(i)
    .append(')');
return sBuf.toString();
  }

  /**
   * value(zb,0)    zb 为数字
   * value(hy_dm,'aaa') hy_dm 为字符
   * 同COALESCE func
   */
  public String ifNull(String str, String str2) {
    return "value(" + str + "," + str2 + ")";
  }

  /**
   * funcPi
   *
   * @return String
   */
  public String funcPi() {
    return String.valueOf(Math.PI);
  }

  /**
   * funcPower
   *
   * @param dValue String
   * @param d String
   * @return String
   */
  public String funcPower(String dValue, String d) {
    if(dValue==null||dValue.trim().length()==0){
      return "cast(null as double)";
    }
    if(d==null||d.trim().length()==0){
      return "cast(null as double)";
    }
    StringBuffer sBuf = new StringBuffer(40)
    .append(" POWER(")
    .append(dValue)
    .append(',')
    .append(d)
    .append(')');
return sBuf.toString();
  }

  /**
   * funcRand
   *
   * @return String
   */
  public String funcRand() {
    return "rand()";
  }

  /**
   * funcRepeat
   *
   * @param field String
   * @param count String
   * @return String
   */
  public String funcRepeat(String field, String count) {
    if(field==null||field.trim().length()==0)
      return "cast(null as char)";
    if(count==null||count.trim().length()==0)
      return "cast(null as char)";
    StringBuffer sBuf = new StringBuffer(40)
    .append(" repeat(")
    .append(field)
    .append(',')
    .append(count)
    .append(')');
return sBuf.toString();
  }

  /**
   * funcRight
   * right('中文',2)='中'
   * right(vargraphic('中文'),2)='中文'
   * @param source String
   * @param len String
   * @return String
   */
  public String funcRight(String source, String len) {
    if(len==null||len.length()==0)
      return "cast(null as char)"; 
    /**
     * IRPT-7496 原来right函数的实现方式是调用funcMid采用SUBSTR函数,
     * 如公式right(title,2)会翻译为SUBSTR(TITLE,(LENGTH(TITLE)-2) + 1) 
     * 如果字段TITLE存在为空的值, (LENGTH(TITLE)-2)+1是一个小于0的负值, 此时DB2数据库会报错
     * DB2数据支持RIGHT函数,所以改用其代替实现
     */
    StringBuffer sBuf = new StringBuffer(50).append(" RIGHT(").append(source)
  		.append(',').append(len).append(")");
    return sBuf.toString();
  }

  /**
   * funcRound
   *
   * @param d String
   * @param i String
   * @return String
   */
  public String funcRound(String d, String i) {
    if(d==null||d.trim().length()==0){
      return "cast(null as double)";
    }
    if(i==null||i.trim().length()==0){
      return "cast(null as double)";
    }
    StringBuffer sBuf = new StringBuffer(40)
    .append(" ROUND(")
    .append(d)
    .append(',')
    .append(i)
    .append(')');
return sBuf.toString();//返回 bigInt
  }

  /**
   * 20090823
   * DB2使用locate函数，但是返回值和字符集有关；
   * 以gbk为例：
   * locate('a','abc')=1 //第一个位置返回1
   * locate('中','a无b中c')=5
   * locate('ac','abc')=0  //找不到返回0
   * 注：参数sub和toSearch参数，如果是常量，则不能为null
   *     如果是字段变量，则字段值也不能为空，否则：
   * 报："NULL" 在使用它的上下文中无效。的异常；
   * @param sub String
   * @param toSearch String
   * @return String
   */
  public String funcSearch(String sub, String toSearch) {
    StringBuffer sBuf = new StringBuffer(32);
    sBuf.append(" ( LOCATE(UPPER(").append(sub).append(") , UPPER(");
    sBuf.append(toSearch).append(") ) -1)"); //数据库是从1开始计算位置
    return sBuf.toString();
  }

  /**
   * funcSign
   *
   * @param d String
   * @return String
   */
  public String funcSign(String d) {
    if(d==null||d.trim().length()==0){
      return "cast(null as double)";
    }
    StringBuffer sBuf = new StringBuffer(40)
    .append(" SIGN(")
    .append(d)
    .append(')');
return sBuf.toString();
  }

  /**
   * funcSin
   *
   * @param d String
   * @return String
   */
  public String funcSin(String d) {
    if(d==null||d.trim().length()==0){
      return "cast(null as double)";
    }
    StringBuffer sBuf = new StringBuffer(40)
    .append(" SIN(")
    .append(d)
    .append(')');
return sBuf.toString();
  }

  /**
   * funcSqrt
   *
   * @param d String
   * @return String
   */
  public String funcSqrt(String d) {
    if(d==null||d.trim().length()==0){
      return "cast(null as double)";
    }
    StringBuffer sBuf = new StringBuffer(40)
    .append(" sqrt(")
    .append(d)
    .append(')');
return sBuf.toString();
  }

  /**
   * funcStrCat
   *
   * @param field1 String
   * @param field2 String
   * @return String
   */
  public String funcStrCat(String field1, String field2) {
    boolean f1 = field1==null||field1.equalsIgnoreCase("null");
    boolean f2 = field2==null||field2.equalsIgnoreCase("null");
    if(f1&&f2) return "cast(null as char)";
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
   * funcTan
   *
   * @param d String
   * @return String
   */
  public String funcTan(String d) {
    if(d==null||d.trim().length()==0){
      return "cast(null as double)";
    }
    StringBuffer sBuf = new StringBuffer(40)
    .append(" tan(")
    .append(d)
    .append(')');
return sBuf.toString();
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
   * funcToday
   *
   * @return String
   */
  public String funcToday() {
    return "current date";
  }

  /**
   * funcTrim
   * Db2 8 不支持trim
   * 使用ltrim(rtrim(...))代替
   * @param field String
   * @return String
   */
  public String funcTrim(String field) {
    if(field==null)
      return "cast(null as char)";
    return "ltrim(rtrim("+field+"))";
  }

  /**
   * funcTrunc
   *
   * @param d String
   * @param i String
   * @return String
   */
  public String funcTrunc(String d, String i) {
    if(d==null||d.trim().length()==0){
      return "cast(null as double)";
    }
    if(i==null||i.trim().length()==0){
      return "cast(null as double)";
    }
    StringBuffer sBuf = new StringBuffer(40)
    .append(" TRUNCATE(")
    .append(d)
    .append(',')
    .append(i)
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
    if(field==null)
      return "cast(null as char)";
    StringBuffer sBuf = new StringBuffer(50)
    .append(" UPPER(")
    .append(field)
    .append(')');
return sBuf.toString();
  }
  public String funcDays(String datefield,String datefield2){
    return "abs(days("+datefield+")-days("+datefield2+"))+1";
  }
  public String funcNow(){
    return "current timestamp";
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
    if(source==null||oldSub==null||newSub==null)
      return "cast(null as char)";
    StringBuffer sql = new StringBuffer(32);
    sql.append("replace(").append(source).append(",");
    sql.append(oldSub).append(",").append(newSub).append(")");
    return sql.toString();
  }

  /**
   * TIMESTAMP ('2002-10-20-12.00.00.000000')
   TIMESTAMP ('2002-10-20 12:00:00')
   DATE ('2002-10-20')
   DATE ('10/20/2002')
   TIME ('12:00:00')
   TIME ('12.00.00')
   */
  public String funcToDate(String date) {
    if (date == null || date.length() == 0)
      return "cast(null as date)";
    StringBuffer sql = new StringBuffer(32);
    sql.append("date('");
    if (Pattern.matches("[0-9]{8}.*", date)) {
      sql.append(date.substring(0, 4)).append("-");
      sql.append(date.substring(4, 6)).append("-");
      sql.append(date.substring(6, 8));
    }else if (Pattern.matches("[0-9]{6}--", date)
        || Pattern.matches("[0-9]{6}", date) ){
      sql.append(date.substring(0, 4)).append("-");
      sql.append(date.substring(4, 6)).append("-01");
    }else if (Pattern.matches("[0-9]{4}----", date)
        || Pattern.matches("[0-9]{4}", date) ){
      sql.append(date.substring(0, 4)).append("-01-01");
    }
    else if (Pattern.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}.*", date)) {
      sql.append(date.substring(0, 10));
    }
    else{
      sql.append(date);
    }
    sql.append("')");
    return sql.toString();
  }

  /**
   * select TIMESTAMP_FORMAT('1999-12-31 23:59:59','YYYY-MM-DD HH24:MI:SS') from aaa
   * select VARCHAR_FORMAT(char(current date)||' 00:00:00','YYYY-MM-DD HH24:MI:SS') from aaa
   */
  public String funcDateToChar(String datefield, String style) {
    String date = "char(" + datefield + ")";//返回 yyyy-mm-dd 的格式字符串
    if(style==null||style.length()==0)
      return date;
    //这里需要返回字符串类型值
    String y = funcLeft(date,String.valueOf(4));
    String m = funcMid(date,String.valueOf(5),String.valueOf(2));
    String d = funcMid(date,String.valueOf(8),String.valueOf(2));
    if(style.matches("[yY]{4}[mM]{2}[dD]{2}")){
      StringBuffer sql = new StringBuffer(10);
      sql.append(y).append("||");
      sql.append(m).append("||");
      sql.append(d);
      return sql.toString();
    }
    if(style.matches("[yY]{4}[mM]{2}--")){
      StringBuffer sql = new StringBuffer(50);
      sql.append(y).append("||");
      sql.append(m).append("||'--'");
      return sql.toString();
    }
    if(style.matches("[yY]{4}[mM]{2}")){
      StringBuffer sql = new StringBuffer(10);
      sql.append(y).append("||");
      sql.append(m);
      return sql.toString();
    }
    if(style.matches("[yY]{4}----")){
      StringBuffer sql = new StringBuffer(10);
      sql.append(y).append("||'----'");
      return sql.toString();
    }
    if(style.matches("[yY]{4}")){
      return y;
    }
    if(style.matches("[mM]{2}")){
      return m;
    }
    if(style.matches("[dD]{2}")){
      return d;
    }
    if(style.matches("[qQ]{1,2}")){//季
      return "("+funcMonth(datefield)+"-1)/3 +1";
    }
    if(style.matches("[wW]{1,2}")){//周
      return "week("+datefield+")";
    }
    return date;
  }
  public String funcCharToDate(String charfield,String style){
    if(style==null||style.length()==0)
      return "date("+charfield+")";
    if(style.matches("[yY]{4}[mM]{2}[dD]{2}")){
      StringBuffer sql = new StringBuffer(20);
      sql.append("date(");
      sql.append(funcLeft(charfield,String.valueOf(4))).append("||'-'||");
      sql.append(funcMid(charfield,String.valueOf(4),String.valueOf(2))).append("||'-'||");
      sql.append(funcMid(charfield,String.valueOf(6),String.valueOf(2)));
      sql.append(")");
      return sql.toString();
    } 
    if(style.matches("[yY]{4}[mM]{2}.*")){
      StringBuffer sql = new StringBuffer(20);
      sql.append("date(");
      sql.append(funcLeft(charfield,String.valueOf(4))).append("||'-'||");
      sql.append(funcMid(charfield,String.valueOf(4),String.valueOf(2))).append("||'-");
      sql.append("01'");
      sql.append(")");
      return sql.toString();
    }
    if(style.matches("[yY]{4}.*")){
      StringBuffer sql = new StringBuffer(20);
      sql.append("date(");
      sql.append(funcLeft(charfield,String.valueOf(4))).append("||'-");
      sql.append("01-01'");
      sql.append(")");
      return sql.toString();
    }
    return "date("+charfield+")";
  }
  /**
   * TIMESTAMP ('2002-10-20-12.00.00.000000')
   TIMESTAMP ('2002-10-20 12:00:00')
   */
  public String funcToDateTime(String date) {
    if (date == null || date.length() == 0)
      return "cast(null as timestamp)";
    StringBuffer sql = new StringBuffer(32);
    sql.append("timestamp('");
    if (Pattern.matches("[0-9]{8}", date)) {
      sql.append(date.substring(0, 4)).append("-");
      sql.append(date.substring(4, 6)).append("-");
      sql.append(date.substring(6, 8));
      sql.append(" 00:00:00");
    }else if (Pattern.matches("[0-9]{6}--", date)
        || Pattern.matches("[0-9]{6}", date) ){
      sql.append(date.substring(0, 4)).append("-");
      sql.append(date.substring(4, 6)).append("-01");
      sql.append(" 00:00:00");
    }else if (Pattern.matches("[0-9]{4}----", date)
        || Pattern.matches("[0-9]{4}", date) ){
      sql.append(date.substring(0, 4)).append("-01-01");
      sql.append(" 00:00:00");
    }
    else if (Pattern.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}", date)) {
      sql.append(date);
      sql.append(" 00:00:00");
    }
    else if (Pattern.matches("[0-9]{8} [0-9]{2}:[0-9]{2}:[0-9]{2}", date)) {
      sql.append(date.substring(0, 4)).append("-");
      sql.append(date.substring(4, 6)).append("-");
      sql.append(date.substring(6, 8));
      sql.append(date.substring(8));
    }
    else if (Pattern.matches(
        "[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2}", date)) {
      sql.append(date);
    }else{
      sql.append(date);
    }
    sql.append("')");
    return sql.toString();
  }
  /**
   * 对日期值，允许进行标准的加、减操作，但是，如果对日期进行加减操作，后面一定要给出它所表示的时间单位：
1 select hiredate -5 day       as hd_minus_5D,
2        hiredate +5 day        as hd_plus_5D,
3        hiredate -5 month       as hd_minus_5M,
4        hiredate +5 month       as hd_plus_5M,
5        hiredate -5 year  as hd_minus_5Y,
6        hiredate +5 year         as hd_plus_5Y
7   from emp
8  where deptno = 10
   */
  public String formatOffsetDate(String datefield,int offset,char t){
    switch(t){
      case 'y':
      case 'Y':
        return datefield+" "+(offset<0?"":"+")+offset+" year";
      case 'm':
      case 'M':
        return datefield+" "+(offset<0?"":"+")+offset+" month";
      case 'd':
      case 'D':
        return datefield+" "+(offset<0?"":"+")+offset+" day";
    }
   // throw new RuntimeException("不支持的类型（y[y],m[M],d[D]）："+t);
    throw new RuntimeException(I18N.getString("JDBC.COMMON.UNSUPPORTEDTYPE", "不支持的类型（y[y],m[M],d[D]）：")+t);
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

  /**
   * 20090820 
   * 使asint,asnum,asstr 传入"null"字符串，能正确的解析；
   */
  public String funcAsInt(String v){
    if(v==null||v.equalsIgnoreCase("null")){
      return "cast(null as int)";
    }
    return "int("+v+")";
  }
  public String funcAsNum(String v){
    if(v==null||v.equalsIgnoreCase("null")){
      return "cast(null as double)";
    }
    return "double("+v+")"; 
  }
  public String funcAsStr(String v){
    if(v==null||v.equalsIgnoreCase("null")){
      return "cast(null as char)";
    }
    return "char("+v+")";
  }
  protected String timestamp2date(String var) {
    return "date("+var+")";
  }
  protected String date2timestamp(String var) {
    return "timestamp(char("+var+")||' 00:00:00')";
  }
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
      case Types.BLOB:{
          return "cast(null as blob)";
      }
      case Types.VARBINARY:{
          return "cast(null as varbinary)";
      }
      case Types.BINARY:{
          return "cast(null as binary)";
      }
      case Types.LONGVARBINARY://sybase
      {
          return "cast(null as longvarbinary)";
      }
      case Types.LONGVARCHAR: {
        //DB2,Mssql,Oracle
          return "cast(null as longvarchar)";
      }
      case Types.CLOB:        //Mysql,Sybase
      {
          return "cast(null as clob)";
      }
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
  /**
   * select length('我的大学；.') from sysibm.sysdummy1
   */
  protected String getStrLengthSql(String str) {
    StringBuffer sql = new StringBuffer(50);
    sql.append("select ").append(funcLen("'"+str+"'"));
    sql.append(" from sysibm.sysdummy1");
    return sql.toString();
  }
  /**
   * db2这里的语句只能创建表，数据没有到新表；
   * 需要另外处理；
   */
  public String getCreateTableByQureySql(String tablename,String querysql,boolean istemp) {
    String sql = "CREATE TABLE "+ tablename;
    if(!SqlFunc.isSelect(querysql))
      querysql = "select * from "+ querysql;
    sql = sql+" AS ("+querysql+") definition only";
    return sql;
  }

  /**
   * DB2： (DAYS(t1) - DAYS(t2)) * 86400 + (MIDNIGHT_SECONDS(t1) - MIDNIGHT_SECONDS(t2))
   * 注：DB2中t1,t2必须是timestamp类型，如果是date类型需要转换：timestamp(char(t1)||' 00:00:00')
   */
  public String funcSeconds(String datefield, String datefield2) {
    StringBuffer sql = new StringBuffer(100);
    sql.append("(DAYS(").append(datefield).append(")-DAYS(").append(datefield2).append("))*86400");
    sql.append("+(MIDNIGHT_SECONDS(").append(datefield).append(")-MIDNIGHT_SECONDS(").append(datefield2).append("))");
    return sql.toString();
  }
  
  /**
   * IRPT-7520 :DB2下修改字段,从浮点型修改为字符型字段存在长度溢出
   * 比如浮点型(12,0)使用char()函数转义后实际长度为13位,包括了一位小数点
   * 目前处理办法是对其默认转义长度进行截取掉一位, 这种处理办法不好, 正式的办法应修改Dialect接口传递
   * 源字段具体的精度参数通过cast a as int/decimal(12,0)等办法实现
   */
  public String funcToSqlVar(String var ,int srcSqlType, int destsqltype ,String style) {
	  String sql = super.funcToSqlVar(var, srcSqlType, destsqltype, style);
	  if( (srcSqlType== Types.FLOAT || srcSqlType==Types.DOUBLE || srcSqlType==Types.DECIMAL)
			  && (destsqltype==Types.VARCHAR || destsqltype==Types.CHAR)) {
		  return this.funcLeft(sql, this.funcLen(sql)+"-2");
	  }
	  return sql;
  }
  
	/**
	 * {@inheritDoc}
	 */
	public String getTruncateTableSql(String tablename) {
		/*
		 * BUG:BI-10950: modify by liujin 2915.01.19
		 * 修改 DB2 中清空表数据的方法
		 */
		return "TRUNCATE TABLE " + tablename + " IMMEDIATE";
	}
}
