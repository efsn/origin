package com.esen.jdbc.dialect.impl.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.impl.DialectImpl;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * <p>Title: Esensoft JDBC</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Esensoft</p>
 * @author dw
 * @version 1.0
 */

public class MysqlDialect
    extends DialectImpl {
  private static final int VARCHAR_LEN=250;
  public MysqlDialect(Object f) {
    super(f);
  }

  public DbDefiner createDbDefiner() {
    return new MysqlDef(this);
  }
  
	public DbMetaData createDbMetaData() throws SQLException {
		if (dbmd == null) {
			dbmd = connectionFactory != null ? new MysqlDbMetaData(
					connectionFactory) : new MysqlDbMetaData(con);
		}
		return dbmd;
	}

	public DbMetaData createDbMetaData(Connection conn) throws SQLException {
		return new MysqlDbMetaData(conn);
	}
	  
  public int getMaxColumn() {
    return 1000;
  }

  protected String getStrLengthSql(String str) {
    StringBuffer sql = new StringBuffer(32);
    sql.append("select ").append(funcLen("'"+str+"'"));
    return sql.toString();
  }
  
  public String addQuote(String fieldname){
		return "`"+fieldname+"`";
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
  
	public boolean supportsTableComment() {
		return true;
	}

	public boolean supportsTableColumnComment() {
		return true;
	}

  /**
   * mysql分页sql实现；
   * 对于嵌套使用limit有一定的规则：
   * ( select * from ebi_sys23_vfs limit 2,2 )limit 0,101  
   * －－查出的结果大于2条，原因不明；
   * select * from ( select * from ebi_sys23_vfs limit 2,2 ) as tmp limit 0,101
   * －－查询结果正确；
   *
   * @param querySelect String
   * @param offset int
   * @param limit int
   * @return String
   */
  public String getLimitString(String querySelect, int offset, int limit) {
    if(offset<0||limit<=0)
      return querySelect;
    return new StringBuffer(querySelect.length() + 20)
        .append("select * from ( ")
        .append(querySelect + " ) as temp_ limit ") //添加优先级
        .append(offset > 0 ? String.valueOf(offset) + ',' + limit :
                String.valueOf(limit))
        .toString();
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
   *""->0 null->null
   * @param ascii int
   * @return String
   */
  public String funcChar(String ascii) {
    StringBuffer sBuf = new StringBuffer(20)
        .append(" CHAR(")
        .append(ascii)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcFind
   * 20090907
   * 使用binary区分大小写；
   * instr('abc', binary 'Bc' )=0
   * @param sub String
   * @param toFind String
   * @return String
   */
  public String funcFind(String sub, String toFind) {
    StringBuffer sBuf = new StringBuffer(100)
        .append("(INSTR(")
        .append(toFind)
        .append(", binary ")//区分大小写
        .append(sub)
        .append(")-1)"); //数据库是从1开始计算位置
    return sBuf.toString();
  }

  /**
   * funcLeft
   *
   * @param source String
   * @param len int
   * @return String
   */
  public String funcLeft(String source, String len) {
    StringBuffer sBuf = new StringBuffer(50)
        .append(" LEFT(")
        .append(source)
        .append(',')
        .append(len)
        .append(')');
    return sBuf.toString();

  }

  /**
   * funcRight
   *
   * @param source String
   * @param len int
   * @return String
   */
  public String funcRight(String source, String len) {
    StringBuffer sBuf = new StringBuffer(50)
        .append(" RIGHT(")
        .append(source)
        .append(',')
        .append(len)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcMid
   *
   * @param field String
   * @param iFrom int
   * @param len int
   * @return String
   */
  public String funcMid(String field, String iFrom, String len) {
    StringBuffer sBuf = new StringBuffer(50)
        .append(" SUBSTRING(")
        .append(field)
        .append(" ,");
    if(StrFunc.isNumber(iFrom)){
      int start = Integer.parseInt(iFrom);
      sBuf.append(start+1);
    }else
      sBuf.append("(").append(iFrom).append(")+ 1"); //本系统从0开始
    sBuf.append(',').append(len).append(')');
    return sBuf.toString();

  }
  /**
   * funcLen
   * CHAR_LENGTH('中国a')=3
   * LENGTH('中国a')=7 这个长度与字符集有关，=7是因为该数据库字符集为utf8
   * @param field String
   * @return String
   */
  
  public String funcLen(String field) {
    StringBuffer sBuf = new StringBuffer(50)
        .append(" CHAR_LENGTH(")
        .append(field)
        .append(')');
    return sBuf.toString();
  }
  
  /* 
   * ESENBI-3456: modify by liujin 2014.12.15
   * 增加  MySQL 的函数
   * MySQL 的 concat_ws 函数的返回值存在为 blob 类型的情况，需要进行数据类型转换
   */
	public String funcStrCat(String field1, String field2) {
		/*
		 * BUG:ESENBI-4266：modify by liujin 2015.01.19
		 * 将 CAST 函数改为使用 CONVERT 函数，
		 * 避免与  MyBatis 的配置文件中对数据类型转换时使用 CAST 做标识引起的冲突。
		 */
		StringBuffer sBuf = new StringBuffer(128).append(" CONVERT(CONCAT_WS('', ")
				.append(field1).append(", ").append(field2).append("), CHAR) ");
		return sBuf.toString();
	}

  /**
   * funcLower
   *
   * @param field String
   * @return String
   */
  public String funcLower(String field) {
    StringBuffer sBuf = new StringBuffer(50)
        .append(" LOWER(")
        .append(field)
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
    StringBuffer sBuf = new StringBuffer(50)
        .append(" UPPER(")
        .append(field)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcSearch
   * 20090907
   * instr函数本省就不区分大小写，不需要使用upper函数；
   * @param sub String
   * @param toSearch String
   * @return String
   */
  public String funcSearch(String sub, String toSearch) {
    StringBuffer sBuf = new StringBuffer(100)
        .append("(INSTR(")
        .append(toSearch)
        .append(",")
        .append(sub)
        .append(")-1) "); //数据库是从1开始计算位置
    return sBuf.toString();
  }

  /**
   * funcStrCat
   *
   * @param field1 String
   * @param field2 String
   * @return String
   */
  

  /**
   * funcWholeReplace
   *
   * @param source String
   * @param oldSub String
   * @param newSub String
   * @return String
   */
  public String funcWholeReplace(String source, String oldSub, String newSub) {
    StringBuffer sBuf = new StringBuffer(50)
        .append(" REPLACE(")
        .append(source)
        .append(',')
        .append(oldSub)
        .append(',')
        .append(newSub)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcTrim
   *
   * @param field String
   * @return String
   */
  public String funcTrim(String field) {
    StringBuffer sBuf = new StringBuffer(50)
        .append(" TRIM(")
        .append(field)
        .append(')');
    return sBuf.toString();
  }

  public String funcNow(){
    return "CURRENT_TIMESTAMP()";
  }
  /**
   * funcNullvalue
   *
   * @param field String
   * @param defaultValue String
   * @return String
   */
  public String funcNullvalue(String field, String defaultValue) {
    StringBuffer sBuf = new StringBuffer(50)
        .append(" IFNULL(")
        .append(field)
        .append(',')
        .append(defaultValue)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcToday
   *
   * @return String
   */
  public String funcToday() {
    return " CURDATE() ";
  }

  /**
   * funcCode
   *
   * @param sourece String
   * @return String
   */
  public String funcCode(String sourece) {
    StringBuffer sBuf = new StringBuffer(20)
        .append(" ASCII(")
        .append(sourece)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcAbs
   *
   * @param d double
   * @return String
   */
  public String funcAbs(String d) {
    StringBuffer sBuf = new StringBuffer(40)
        .append(" ABS(")
        .append(d)
        .append(')');
    return sBuf.toString();

  }

  /**
   * funcC
   *
   * @param d double
   * @return String
   */
  public String funcC(String d) {
    return ""; //不能实现
  }

  /**
   * funcCos
   *
   * @param d double
   * @return String
   */
  public String funcCos(String d) {
    StringBuffer sBuf = new StringBuffer(40)
        .append(" COS(")
        .append(d)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcSin
   *
   * @param d double
   * @return String
   */
  public String funcSin(String d) {
    StringBuffer sBuf = new StringBuffer(40)
        .append(" SIN(")
        .append(d)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcTan
   *
   * @param d double
   * @return String
   */
  public String funcTan(String d) {
    StringBuffer sBuf = new StringBuffer(40)
        .append(" TAN(")
        .append(d)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcEven
   *
   * @param d double
   * @return String
   */
  public String funcEven(String d) {
    return ""; //没有对应函数
  }

  /**
   * funcExp
   *
   * @param d double
   * @return String
   */
  public String funcExp(String d) {
    StringBuffer sBuf = new StringBuffer(40)
        .append(" EXP(")
        .append(d)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcSqrt
   *
   * @param d double
   * @return String
   */
  public String funcSqrt(String d) {
    StringBuffer sBuf = new StringBuffer(40)
        .append(" SQRT(")
        .append(d)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcFact
   *
   * @param d double
   * @return String
   */
  public String funcFact(String d) {
    return "";//no local
  }

  /**
   * funcInt
   *
   * @param d double
   * @return String
   */
  public String funcInt(String d) {
    StringBuffer sBuf = new StringBuffer(40)
        .append(" TRUNCATE(")
        .append(d)
        .append(",0)");//去尾
    return sBuf.toString();
  }

  /**
   * funcSign
   *
   * @param d double
   * @return String
   */
  public String funcSign(String d) {
    StringBuffer sBuf = new StringBuffer(40)
        .append(" SIGN(")
        .append(d)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcLog
   *
   * @param d double
   * @param dValue double
   * @return String
   */
  public String funcLog(String d, String dValue) {
    StringBuffer sBuf = new StringBuffer(40)
        .append("(LOG(")
        .append(dValue)
        .append(")/LOG(")
        .append(d)
        .append(')')
        .append(')');
    return sBuf.toString();//log(100)/log(10)

  }

  /**
   * funcMod
   *
   * @param iValue int
   * @param i int
   * @return String
   */
  public String funcMod(String iValue, String i) {
    StringBuffer sBuf = new StringBuffer(40)
        .append(" MOD(")
        .append(iValue)
        .append(',')
        .append(i)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcPi
   *
   * @return String
   */
  public String funcPi() {
    return " PI() ";
  }

  /**
   * funcPower
   *
   * @param dValue double
   * @param d double
   * @return String
   */
  public String funcPower(String dValue, String d) {
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
    return " RAND() ";
  }

  /**
   * funcRound
   *
   * @param d double
   * @param i int
   * @return String
   */
  public String funcRound(String d, String i) {
    StringBuffer sBuf = new StringBuffer(40)
        .append(" ROUND(")
        .append(d)
        .append(',')
        .append(i)
        .append(')');
    return sBuf.toString();//返回 bigInt
  }

  /**
   * funcTrunc
   *
   * @param d double
   * @param i int
   * @return String
   */
  public String funcTrunc(String d, String i) {
    StringBuffer sBuf = new StringBuffer(40)
        .append(" TRUNCATE(")
        .append(d)
        .append(',')
        .append(i)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcLn
   *
   * @param d double
   * @return String
   */
  public String funcLn(String d) {
    StringBuffer sBuf=new StringBuffer(40)
        .append("(log(")
        .append(d)
        .append(")/LOG(EXP(1)))");
    return sBuf.toString();
  }

  /**
   * funcRepeat
   *
   * @param field String
   * @param count int
   * @return String
   */
  public String funcRepeat(String field, String count) {
    StringBuffer sBuf=new StringBuffer(100)
        .append(" REPEAT(")
        .append(field)
        .append(',')
        .append(count)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcIf
   *
   * @param b String
   * @param t String
   * @param f String
   * @return String
   */
  public String funcIf(String b, String t, String f) {
    StringBuffer sBuf=new StringBuffer(100)
        .append(" if(")
        .append(b)
        .append(',')
        .append(t)
        .append(',')
        .append(f)
        .append(')');
    return sBuf.toString();
  }

  public String funcToDate(String date) {
    if(date==null||date.length()==0)
      return "cast(null as date)";
    StringBuffer sql = new StringBuffer(32);
    sql.append("str_to_date('");
    if (Pattern.matches("[0-9]{8}.*", date)) {
      sql.append(date.substring(0,8));
    }else if (Pattern.matches("[0-9]{6}--", date)
        || Pattern.matches("[0-9]{6}", date) ){
      sql.append(date.substring(0,6)).append("01");
    }else if (Pattern.matches("[0-9]{4}----", date)
        || Pattern.matches("[0-9]{4}", date) ){
      sql.append(date.substring(0, 4)).append("0101");
    }else if (Pattern.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}.*", date)) {
      sql.append(date.substring(0, 4));
      sql.append(date.substring(5, 7));
      sql.append(date.substring(8, 10));
    }else{
      sql.append(date);
    }
    sql.append("','%Y%m%d')");
    return sql.toString();
  }
  public String funcCharToDate(String charfield,String style){
    if(style==null||style.length()==0)
      return "str_to_date("+charfield+",'%Y%m%d')";
    if(style.matches("[yY]{4}[mM]{2}[dD]{2}")){
      return "str_to_date("+charfield+",'%Y%m%d')";
    }
    if(style.matches("[yY]{4}[mM]{2}.*")){
      StringBuffer sql = new StringBuffer(32);
      sql.append("str_to_date(concat(");
      sql.append(funcLeft(charfield,String.valueOf(6)));
      sql.append(",'01')");
      sql.append(",'%Y%m%d')");
      return sql.toString();
    }
    if(style.matches("[yY]{4}.*")){
      StringBuffer sql = new StringBuffer(32);
      sql.append("str_to_date(concat(");
      sql.append(funcLeft(charfield,String.valueOf(4)));
      sql.append(",'0101')");
      sql.append(",'%Y%m%d')");
      return sql.toString();
    }
    return "str_to_date("+charfield+",'%Y%m%d')";
  }
  /**
   * date_format
   * Specifier Description 
%a Abbreviated weekday name (Sun..Sat) 
%b Abbreviated month name (Jan..Dec) 
%c Month, numeric (0..12) 
%D Day of the month with English suffix (0th, 1st, 2nd, 3rd, ...) 
%d Day of the month, numeric (00..31) 
%e Day of the month, numeric (0..31) 
%f Microseconds (000000..999999) 
%H Hour (00..23) 
%h Hour (01..12) 
%I Hour (01..12) 
%i Minutes, numeric (00..59) 
%j Day of year (001..366) 
%k Hour (0..23) 
%l Hour (1..12) 
%M Month name (January..December) 
%m Month, numeric (00..12) 
%p AM or PM 
%r Time, 12-hour (hh:mm:ss followed by AM or PM) 
%S Seconds (00..59) 
%s Seconds (00..59) 
%T Time, 24-hour (hh:mm:ss) 
%U Week (00..53), where Sunday is the first day of the week 
%u Week (00..53), where Monday is the first day of the week 
%V Week (01..53), where Sunday is the first day of the week; used with %X 
%v Week (01..53), where Monday is the first day of the week; used with %x 
%W Weekday name (Sunday..Saturday) 
%w Day of the week (0=Sunday..6=Saturday) 
%X Year for the week where Sunday is the first day of the week, numeric, four digits; used with %V 
%x Year for the week, where Monday is the first day of the week, numeric, four digits; used with %v 
%Y Year, numeric, four digits 
%y Year, numeric (two digits) 
%% A literal ‘%’ character 

   */
  public String funcDateToChar(String datefield,String style) {
    if(StrFunc.isNull(style)){
      style = "YYYYMMDD";
      return "date_format(" + datefield + ",'%Y%m%d')";
    }

	if (style.matches("[yY]{4}")) {
		return "year(" + datefield + ")";
	}
	if (style.matches("[mM]{2}")) {
		return "month(" + datefield + ")";
	}
	if (style.matches("[dD]{2}")) {
		return "day(" + datefield + ")";
	}
	if (style.matches("[qQ]{1,2}")) {// 季
		// funcMonth求得是0-11的值，0表示一月份
		return "TRUNCATE(" + funcMonth(datefield) + "/3,0) +1";
	}
	if (style.matches("[wW]{1,2}")) {// 周
		return "week(" + datefield + ")";
	}

	/**
	 * 20100401 原来的实现方法： CONCAT(year(str_to_date(20100101,
	 * '%Y%m%d')),TRUNCATE((month(str_to_date(20100101,'%Y%m%d'))-1)/3,0)
	 * +1) 在从ResultSet读取时，其字段类型读取出来的数据类型是Types.VARBINARY 这是blob类型，造成读取数据不对；
	 * 现在改成：年*10+季， 读取出来是数值；
	 */
	if (style.matches("[yY]{4}[qQ]{1,2}")) {// 年季
		return "year(" + datefield + ")*10+TRUNCATE((" + funcMonth(datefield) + "-1)/3,0) +1";
	}
      
	/*
	 * ESENBI-3317; modify by liujin 2014.12.15
	 * 修改实现方法，不适用匹配整个格式串的方式，改为分段匹配，然后拼成目的格式串
	 * 方便处理格式串中带中文(年、月、日)、连接符(空格、_、/ 等)的情况
	 */
    style = style.toUpperCase();
    StringBuffer mystyle = new StringBuffer();
    String style_tmp = style;
    
    //格式串，需要时继续添加，注意先后顺序
    String[][] trans = {{"YYYY", "%Y"},
    		{"MM", "%m"},
    		{"DD", "%d"},
    		{"HH24", "%H"},
    		{"HH", "%h"},
    		{"MI", "%i"},
    		{"SS", "%s"},
    };
    
    // 格式串中的中文，需要时继续添加
    String[] format = {"年", "月", "日"};
    
    while (!StrFunc.isNull(style_tmp)) {
    	boolean find = false; //表示是否找到匹配的格式串
    	for (int i = 0; i < trans.length && !find; i++) {
    	    if (style_tmp.startsWith(trans[i][0])) {
    	    	mystyle.append(trans[i][1]);
    	    	style_tmp = style_tmp.substring(trans[i][0].length());
    	    	find = true;
    	    }
    	}
    	
    	for (int j = 0; j < format.length && !find; j++) {
    	    if (style_tmp.startsWith(format[j])) {
    	    	mystyle.append(format[j]);
    	    	style_tmp = style_tmp.substring(format[j].length());
    	    	find = true;
    	    }
    	}
    	
    	if (!find) {
    		mystyle.append(style_tmp.charAt(0));
    		style_tmp = style_tmp.substring(1);
    	}
    }

    StringBuffer sql = new StringBuffer(32);
    sql.append("date_format(");
    sql.append(datefield);
    sql.append(",'").append(mystyle).append("')");
    return sql.toString();
  }
  public boolean supportRegExp(){
    return true;
  }
  public String regExp(String field,String regexp){
    return field+" regexp '"+regexp+"'";
  }

  public String funcToDateTime(String dtstr) {
    if(dtstr==null||dtstr.length()==0)
      return "cast(null as datetime)";
    if (Pattern.matches("[0-9]{8}", dtstr)) {
      dtstr = dtstr+" 00:00:00";
    }else
    if (Pattern.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}", dtstr)) {
      dtstr = dtstr+" 00:00:00";
    }else 
    if (Pattern.matches("[0-9]{4}----", dtstr)) {
      dtstr = dtstr.substring(0,4)+"0101 00:00:00";
    }else
    if (Pattern.matches("[0-9]{4}", dtstr)) {
      dtstr = dtstr+"0101 00:00:00";
    }else
    if (Pattern.matches("[0-9]{4}[0-9]{2}--", dtstr)) {
      dtstr = dtstr.substring(0,6)+"01 00:00:00";
    }else
    if (Pattern.matches("[0-9]{4}[0-9]{2}", dtstr)) {
      dtstr = dtstr+"01 00:00:00";
    }
    String style = null;
    //  匹配日期时间 "2001-01-01 00:00:00"
    if(dtstr.matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"))
        style = "%Y-%m-%d %H:%i:%s";
    else if(dtstr.matches("[0-9]{4}[0-9]{2}[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"))
      style = "%Y%m%d %H:%i:%s";
    else
//      throw new RuntimeException("格式不对："+dtstr);
    	throw new RuntimeException(I18N.getString("JDBC.COMMON.FORMATISWRONG", "格式不对：")+dtstr);
    StringBuffer sql = new StringBuffer(32);
    sql.append("str_to_date('");
    
    sql.append(dtstr);
    sql.append("','").append(style).append("')");
    return sql.toString();
  }
  /**
   * 使用DATE_ADD函数，如下所示：
1 select date_add(hiredate,interval -5 day)        as hd_minus_5D,
2        date_add(hiredate,interval  5 day)          as hd_plus_5D,
3        date_add(hiredate,interval -5 month) as hd_minus_5M,
4        date_add(hiredate,interval  5 month)       as hd_plus_5M,
5        date_add(hiredate,interval -5 year)    as hd_minus_5Y,
6        date_add(hiredate,interval  5 year)  as hd_plus_5DY
7  from emp
8  where deptno=10
   */
  public String formatOffsetDate(String datefield,int offset,char t){
    switch(t){
      case 'y':
      case 'Y':
        return "date_add("+datefield+",interval "+offset+" year)";
      case 'm':
      case 'M':
        return "date_add("+datefield+",interval "+offset+" month)";
      case 'd':
      case 'D':
        return "date_add("+datefield+",interval "+offset+" day)";
    }
//    throw new RuntimeException("不支持的类型（y[y],m[M],d[D]）："+t);
    throw new RuntimeException(I18N.getString("JDBC.COMMON.UNSUPPORTEDTYPE", "不支持的类型（y[y],m[M],d[D]）：")+t);
  }
  public String funcDay(String datefield) {
    return "day("+datefield+")";
  }

  public String funcMonth(String datefield) {
    return "month("+datefield+")";
  }

  public String funcYear(String datefield) {
    return "year("+datefield+")";
  }


  public String funcDays(String datefield,String datefield2){
    return "abs(to_days("+datefield+")-to_days("+datefield2+"))+1";
  }

  public String formatConstStr(String value) {
    if(value!=null)
      value = value.replaceAll("\\\\", "\\\\\\\\");
    return super.formatConstStr(value);
  }
  
  /**
   * delete t1 from tbname as t1,tbname2 as t2 where t1.key1=t2.key1 and t1.key2=t2.key2
   */
  public String getDeleteSql(String tbname,String tbname2 ,String[] keys, String[] keys2){
    StringBuffer delsql = new StringBuffer(64);
    delsql.append("delete t1 from ").append(tbname).append(" as t1,");
    delsql.append(tbname2).append(" as t2 \n");
    delsql.append("where ");
    for(int i=0;i<keys.length;i++){
      if(i>0) delsql.append(" and ");
      delsql.append("t1.").append(keys[i]).append("=t2.").append(keys2[i]);
    }
    return delsql.toString();
  }
  
  /**
   * Mysql：UNIX_TIMESTAMP(date1)-UNIX_TIMESTAMP(date2)
   */
  public String funcSeconds(String datefield, String datefield2) {
    StringBuffer sql = new StringBuffer(64);
    sql.append("UNIX_TIMESTAMP(").append(datefield).append(")-");
    sql.append("UNIX_TIMESTAMP(").append(datefield2).append(")");
    return sql.toString();
  }
}
