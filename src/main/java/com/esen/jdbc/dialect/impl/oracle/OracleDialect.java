package com.esen.jdbc.dialect.impl.oracle;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.impl.DialectImpl;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.Pattern;

/**
 * <p>Title: Esensoft JDBC</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Esensoft</p>
 * @author dw
 * @version 1.0
 */

public class OracleDialect extends DialectImpl {
  private static final int VARCHAR_LEN=4000;

  public OracleDialect(Object f) {
    super(f);
  }

  public DbDefiner createDbDefiner() {
    return new OracleDef(this);
  }

  public int getMaxColumn() {
    return 1000;
  }
  /**
   * Oracle 如果创建临时表，也不能插入数据；
   */
  public String getCreateTableByQureySql(String tablename,String querysql,boolean istemp) {
    String sql = getCreateTableByQueryStr(tablename,istemp);
    if(!SqlFunc.isSelect(querysql))
      querysql = "select * from "+ querysql;
    sql = sql+" AS "+querysql;
    return sql;
  }

  private String getCreateTableByQueryStr(String tablename,boolean istemp) {
    if(istemp)
      return "CREATE GLOBAL TEMPORARY TABLE "+ tablename;
    return "CREATE TABLE "+ tablename;
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
  
  public boolean supportsViewColumnComment() {
	return true; 
  }
  
	public boolean supportsTableComment() {
		return true;
	}

	public boolean supportsTableColumnComment() {
		return true;
	}

  /**
   * Oracle中获得字符字节的长度；
   */
  protected String getStrLengthSql(String str) {
    StringBuffer sql = new StringBuffer(32);
    sql.append("select ").append("lengthb('").append(str).append("')");
    sql.append(" from dual");
    return sql.toString();
  }
  
  /**
   * <pre>
   * 20111101 dw
   * Oracle 分页sql优化：
   * 第一页：
   * select * from ( select INT_,STR_ from TESTDB order by INT_ ) where rownum <= 3
   * 第二页：
   * select * from ( select row_.*, rownum rownum_ from ( 
   * select INT_,STR_ from TESTDB order by INT_ 
   * ) row_ where rownum <= 6) where rownum_ > 3 
   * 
   * 原来的sql：
   * select * from (select row$_.*, rownum rownum$_
   * from (select INT_,STR_ from TESTDB order by INT_ 
   * ) row$_) where rownum$_ > 60 and rownum$_ <= 80
   * 
   * 这两种分页方法效率相差n个数量级。
   * <pre>
   * @param sql String
   * @param offset int
   * @param limit int
   * @return String
   */
  public String getLimitString(String sql, int offset, int limit) {
    if(offset<0||limit<=0)
      return sql;
    sql = sql.trim();
	String forUpdateClause = null;
	boolean isForUpdate = false;
	final int forUpdateIndex = sql.toLowerCase().lastIndexOf( "for update") ;
	if ( forUpdateIndex > -1 ) {
		forUpdateClause = sql.substring( forUpdateIndex );
		sql = sql.substring( 0, forUpdateIndex-1 );
		isForUpdate = true;
	}
	boolean hasOffset = offset>0;
    StringBuffer pagingSelect = new StringBuffer( sql.length()+100 );
	if (hasOffset) {
		pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
	}
	else {
		/**
		 * BI-5904 该表计算的时候，利用BI的一个漏洞，FloatFxqInnerCalc.collectResult_float_autoolap_processdata_acceptRow
		 *   恰好访问到了rownum对应的值，如果没有值就会出现异常。对于复杂的交叉分析表构造分析对象有BUG
		 */
		pagingSelect.append("select row_.*,rownum from ( ");
	}
	pagingSelect.append(sql);
	if (hasOffset) {
		pagingSelect.append(" ) row_ where rownum <= ").append(offset + limit);
		pagingSelect.append(") where rownum_ > ").append(offset);
	}
	else {
		pagingSelect.append(" )row_ where rownum <= ").append(limit);
	}
 
    if ( isForUpdate ) {
		pagingSelect.append( " " );
		pagingSelect.append( forUpdateClause );
	}
    return pagingSelect.toString();
  }

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
/*  public ResultSet queryLimit(Statement stmt, String querySelect, int offset,
      int limit) throws SQLException {
    super.checkParam(querySelect, offset, limit);
    querySelect = getLimitString(querySelect, offset, limit);
    ResultSet rst = null;
    rst = stmt.executeQuery(querySelect);
    return rst;

  }*/

  /**
   * funcChar
   *
   * @param ascii int
   * @return String
   */
  public String funcChar(String ascii) {
    StringBuffer sBuf = new StringBuffer(20).append(" CHR(").append(ascii)
        .append(')');
    return sBuf.toString();
  }

  /**
   * funcFind
   *
   * @param sub String
   * @param toFind String
   * @return String
   */
  public String funcFind(String sub, String toFind) {
    StringBuffer sBuf = new StringBuffer(100).append("(INSTR(").append(toFind)
        .append(',').append(sub).append(" ,1,1)-1)"); //数据库是从1开始计算位置
    return sBuf.toString();

  }

	public String funcLeft(String source, String len) {
		return _funcLeft(source, len, false);
	}

	 /**
	   * funcRight
	   *
	   * @param source String
	   * @param len int
	   * @return String
	   */
	  public String funcRight(String source, String len) {
	    return _funcRight(source, len, false);
	  }

	  /**
	   * funcMid
	   * len=0，返回空
	   * @param field String
	   * @param iFrom int
	   * @param len int
	   * @return String
	   */
	  public String funcMid(String field, String iFrom, String len) {
	    return _funcMid(field, iFrom, len, false);
	  }

	  /**
	   * funcLen
	   *
	   * @param field String
	   * @return String
	   */
	  public String funcLen(String field) {
	    return _funcLen(field, false);
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
   * funcSearch
   *
   * @param sub String
   * @param toSearch String
   * @return String
   */
  public String funcSearch(String sub, String toSearch) {
    StringBuffer sBuf = new StringBuffer(100)//转成大小
        .append(" ( INSTR(UPPER(").append(toSearch).append(") , UPPER(")
        .append(sub).append(") ,1,1) -1)"); //数据库是从1开始计算位置
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
    StringBuffer sBuf = new StringBuffer(50).append(" CONCAT(").append(field1)
        .append(',').append(field2).append(')');
    return sBuf.toString();
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
   *
   * @param field String
   * @return String
   */
  public String funcTrim(String field) {
    StringBuffer sBuf = new StringBuffer(50).append(" TRIM(").append(field)
        .append(')');
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
    StringBuffer sBuf = new StringBuffer(50).append(" NVL(").append(field)
        .append(',').append(defaultValue).append(')');
    return sBuf.toString();
  }

  /**
   * funcToday
   *
   * @return String
   */
  public String funcToday() {
    //return " TO_CHAR(SYSDATE, '#YYYY-MM-DD#')";
    return " SYSDATE ";
  }
  public String funcNow(){
    return " SYSDATE ";
    //return "TO_CHAR(SYSDATE, 'HH24:MI:SS')";
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
   * funcAbs
   *
   * @param d double
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
    StringBuffer sBuf = new StringBuffer(40).append(" COS(").append(d).append(
        ')');
    return sBuf.toString();
  }

  /**
   * funcSin
   *
   * @param d double
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
   * @param d double
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
    StringBuffer sBuf = new StringBuffer(40).append(" EXP(").append(d).append(
        ')');
    return sBuf.toString();
  }

  /**
   * funcSqrt
   *
   * @param d double
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
   * @param d double
   * @return String
   */
  public String funcFact(String d) {
    return ""; //没有本地函数
  }

  /**
   * funcInt
   *
   * @param d double
   * @return String
   */
  public String funcInt(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" TRUNC(").append(d)
        .append(",0)");//截整
    return sBuf.toString();

  }

  /**
   * funcSign
   *
   * @param d double
   * @return String
   */
  public String funcSign(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" SIGN(").append(d).append(
        ')');
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
    StringBuffer sBuf = new StringBuffer(40).append(" LOG(").append(d).append(
        ',').append(dValue).append(')');
    return sBuf.toString();
  }

  /**
   * funcMod
   *
   * @param iValue int
   * @param i int
   * @return String
   */
  public String funcMod(String iValue, String i) {
    StringBuffer sBuf = new StringBuffer(40).append(" MOD(").append(iValue)
        .append(',').append(i).append(')');
    return sBuf.toString();
  }

  /**
   * funcPi
   *
   * @return String
   */
  public String funcPi() {
    return " 3.14159266359 ";
  }

  /**
   * funcPower
   *
   * @param dValue double
   * @param d double
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
    return " DBMS_RANDOM.VALUE "; //(a,b)
  }

  /**
   * funcRound
   *
   * @param d double
   * @param i int
   * @return String
   */
  public String funcRound(String d, String i) {
    StringBuffer sBuf = new StringBuffer(40).append(" ROUND(").append(d)
        .append(',').append(i).append(')');
    return sBuf.toString();
  }

  /**
   * funcTrunc
   *
   * @param d double
   * @param i int
   * @return String
   */
  public String funcTrunc(String d, String i) {
    StringBuffer sBuf = new StringBuffer(40).append(" TRUNC(").append(d)
        .append(',').append(i).append(')');
    return sBuf.toString();
  }

  /**
   * funcLn
   *
   * @param d double
   * @return String
   */
  public String funcLn(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" LN(").append(d).append(
        ')');
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
    /*
     if(count<=1) return "field";
     StringBuffer sBuf=new StringBuffer(100);
     for(int i=0;i<count-1;i++){
     sBuf.append("CONCAT(");
     }
     sBuf.append(field);
     for(int i=0;i<count-1;i++){
     sBuf.append(',')
     .append(field)
     .append(')');
     }
     return sBuf.toString();
     */
    return field; //不支持
  }

  public DbMetaData createDbMetaData() throws SQLException {
    if(dbmd==null)
      dbmd = connectionFactory!=null?new OracleDbMetaData(connectionFactory):new OracleDbMetaData(con);
    return dbmd;
  }
  
  public DbMetaData createDbMetaData(Connection conn) throws SQLException {
    return new OracleDbMetaData(conn);
  }
  
  protected String timestamp2date(String var) {
    return "to_date(to_char("+var+",'yyyymmdd'),'yyyymmdd')";
  }
  public String funcToDate(String date) {
    if(date==null||date.length()==0)
      return "to_date(null)";
    StringBuffer sql = new StringBuffer(32);
    sql.append("to_date('");
    String style = "YYYYMMDD";
    if (Pattern.matches("[0-9]{8}", date)) {
    }
    if (Pattern.matches("[0-9]{8} [0-9]{2}:[0-9]{2}:[0-9]{2}", date)) {
      style="YYYYMMDD HH24:MI:SS";
    }
    if (Pattern.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}", date)) {
      style = "YYYY-MM-DD";
    }
    if (Pattern.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2}", date)) {
      style = "YYYY-MM-DD  HH24:MI:SS";
    }
    if (Pattern.matches("[0-9]{4}----", date)) {
      date = date.substring(0,4)+"0101";
      style = "YYYYMMDD";
    }
    if (Pattern.matches("[0-9]{4}", date)) {
      date = date+"0101";
      style = "YYYYMMDD";
    }
    if (Pattern.matches("[0-9]{4}[0-9]{2}--", date)) {
      style = "YYYYMM--";
    }
    if (Pattern.matches("[0-9]{4}[0-9]{2}", date)) {
      style = "YYYYMM";
    }
    sql.append(date);
    sql.append("','").append(style).append("')");
    return sql.toString();
  }

  public String funcDateToChar(String datefield,String style) {
    StringBuffer sql = new StringBuffer(20);
    sql.append("to_char(");
    sql.append(datefield);
    sql.append(",'");
    sql.append(style);
    sql.append("')");
    return sql.toString();
  }
  public String funcCharToDate(String charfield,String style){
    StringBuffer sql = new StringBuffer(20);
    sql.append("to_date(");
    sql.append(charfield);
    sql.append(",'");
    sql.append(style);
    sql.append("')");
    return sql.toString();
  }
  public String ifNull(String str,String str2){
    return "nvl("+str+","+str2+")";
  }
  public boolean supportRegExp(){
    return true;
  }
  public String regExp(String field,String regexp){
    return "REGEXP_LIKE("+field+",'"+regexp+"')";
  }

  public String funcToDateTime(String dtstr) {
    if(dtstr==null||dtstr.length()==0)
      return "to_date(null)";
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
        style = "YYYY-MM-DD HH24:MI:SS";
    else if(dtstr.matches("[0-9]{4}[0-9]{2}[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"))
      style = "YYYYMMDD HH24:MI:SS";
    else
//      throw new RuntimeException("格式不对："+dtstr);
    	throw new RuntimeException(I18N.getString("JDBC.COMMON.FORMATISWRONG", "格式不对：")+dtstr);
    StringBuffer sql = new StringBuffer(20);
    sql.append("to_date('");
    sql.append(dtstr);
    sql.append("','").append(style).append("')");
    return sql.toString();
  }

  public String funcDay(String datefield) {
    return "to_char("+datefield+",'DD')";
  }

  public String funcMonth(String datefield) {
    return "to_char("+datefield+",'MM')";
  }

  public String funcYear(String datefield) {
    return "to_char("+datefield+",'YYYY')";
  }
  /**
   * 对天数采用标准加减，而使用ADD_MONTHS 函数加减月数和年数：
1 select hiredate-5                 as hd_minus_5D,
2        hiredate+5                    as hd_plus_5D,
3        add_months(hiredate,-5)          as hd_minus_5M,
4        add_months(hiredate,5)          as hd_plus_5M,
5        add_months(hiredate,-5*12)       as hd_minus_5Y,
6        add_months(hiredate,5*12)       as hd_plus_5Y
7   from emp
   */
  public String formatOffsetDate(String datefield,int offset,char t){
    switch(t){
      case 'y':
      case 'Y':
        return "add_months("+datefield+","+(offset*12)+")";
      case 'm':
      case 'M':
        return "add_months("+datefield+","+offset+")";
      case 'd':
      case 'D':
        return datefield+(offset<0?"":"+")+offset;
    }
//    throw new RuntimeException("不支持的类型（y[y],m[M],d[D]）："+t);
    throw new RuntimeException(I18N.getString("JDBC.COMMON.UNSUPPORTEDTYPE", "不支持的类型（y[y],m[M],d[D]）：")+t);
  }


  public String funcDays(String datefield,String datefield2){
    //包含两头的天数；
    return "abs(trunc("+datefield+"-"+datefield2+"))+1";
  }
  public String funcStdev(String field){
    return "stddev("+field+")";
  }

  /**
   * 20090623
   * Oracle 取整使用trunc，直接舍位；
   * trunc(1.56,0)=1
   * 原来的代码只是转换成数值，没有取整；
   */
  public String funcAsInt(String v){
    return "trunc("+v+",0)";
  }
  
  public String funcAsNum(String v){
    return "to_number("+v+")";
  }
  public String funcAsStr(String v){
    if(v==null)
      return "null";
    return "to_char("+v+")";
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
  /**
   * Oracle：(date1-date2)*3600*24
   */
  public String funcSeconds(String datefield, String datefield2) {
    StringBuffer sql = new StringBuffer(36);
    
    /*
     * 之前的 sql 语句返回值是  interval 类型。
     * 当为 interval day to second 类型时，无法直接转换为数值类型
     * 修改为返回值为数值类型的形式
     */
    sql.append(" EXTRACT(DAY FROM");
    sql.append('(').append(datefield).append('-').append(datefield2);
    sql.append(")*86400");
    sql.append(')');    
    
    return sql.toString();
  }
  
	public String funcLenB(String f) {
		return _funcLen(f, true);
	}

	private String _funcLen(String field, boolean isByte) {
		return new StringBuffer(50).append(isByte ? " LENGTHB(" : " LENGTH(").append(field).append(')').toString();
	}

	public String funcMidB(String source, String i, String n) {
		return _funcMid(source, i, n, true);
	}

	private String _funcMid(String field, String iFrom, String len, boolean isByte) {
		StringBuffer sb = new StringBuffer(50).append(isByte ? " SUBSTRB(" : " SUBSTR(").append(field).append(","); //系统函数从0开始
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

	public String funcLeftB(String source, String len) {
		return _funcLeft(source, len, true);
	}

	public String funcRightB(String source, String len) {
		return _funcRight(source, len, true);
	}

	private String _funcRight(String source, String len, boolean isByte) {
		StringBuffer sb = new StringBuffer(50).append(isByte ? " SUBSTRB(" : " SUBSTR(").append(source);
		int l = StrFunc.str2int(len, Integer.MIN_VALUE);
		if (l == Integer.MIN_VALUE) {
			/**
			 * 20090711
			 * 如果len是个表达式，如果其值=0,原来的代码会返回整个字符串；
			 * 现在改为通过substr三个参数，计算出起位置，来获取需要的值；
			 */
			String ifrom = _funcLen(source, isByte) + "-" + len + "+1";
			sb.append(",").append(ifrom).append(",").append(len).append(")");
		}
		else {
			if (l > 0) {
				//len=0，返回空
				sb.append(",-").append(len).append(')');
			}
			else if (l == 0) {
				sb.append(",1,0)");
			}
			else {
				/**
				 * 20090711
				 * len为负数，则相当于left
				 * 原来的代码返回的是-len开始后面所有的字符串；
				 */
				l = -l;
				sb.append(",1,").append(l).append(')');
			}
		}
		return sb.toString();

	}
	
	/**
	 * funcLeft
	 *
	 * @param source String
	 * @param len int
	 * @return String
	 */
	public String _funcLeft(String source, String len, boolean isByte) {
		StringBuffer sBuf = new StringBuffer(50).append(" ").append(isByte ? "SUBSTRB(" : "SUBSTR(").append(source);
		int l = StrFunc.str2int(len, Integer.MIN_VALUE);
		if (l == Integer.MIN_VALUE) {
			sBuf.append(",1,").append(len).append(')');
		}
		else {
			if (l >= 0) {
				sBuf.append(",1,").append(len).append(')');
			}
			else {
				/**
				 * 20090711
				 * len<0 则返回right(source,-len)
				 * 原来的代码返回null；
				 */
				sBuf.append(",").append(len).append(')');
			}
		}
		return sBuf.toString();
	}
}
