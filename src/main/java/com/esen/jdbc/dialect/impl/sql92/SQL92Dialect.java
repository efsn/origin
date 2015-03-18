package com.esen.jdbc.dialect.impl.sql92;

import java.sql.SQLException;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.jdbc.dialect.impl.DialectImpl;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * 实现SQL92标准的Dialect接口；
 * 有待完善；
 * @author dw
 *
 */
public class SQL92Dialect extends DialectImpl {

  public SQL92Dialect(Object conORConf) {
    super(conORConf);
    
  }
  
  public DbMetaData createDbMetaData() throws SQLException {
    if(dbmd==null){
      dbmd = connectionFactory!=null?new DbMetaDataImpl(connectionFactory):new DbMetaDataImpl(con);
    }
    return dbmd;
  }

  protected String getStrLengthSql(String str) {
    StringBuffer sql = new StringBuffer(32);
    sql.append("select ").append(funcLen("'"+str+"'"));
    return sql.toString();
  }

  public DbDefiner createDbDefiner() {
    return new SQL92Def(this);
  }

  public String formatOffsetDate(String datefield, int offset, char t) {
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

  public String funcAbs(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" ABS(").append(d).append(')');
    return sBuf.toString();
  }
  
  /**
   * 不支持组合函数
   */
  public String funcC(String d) {
    return null;
  }

  public String funcChar(String ascii) {
    StringBuffer sBuf = new StringBuffer(20).append(" CHR(").append(ascii).append(')');
    return sBuf.toString();
  }

  public String funcCharToDate(String charfield, String style) {
    return null;
  }

  public String funcCode(String sourece) {
    StringBuffer sBuf = new StringBuffer(50).append(" ASCII(").append(sourece).append(')');
    return sBuf.toString();
  }

  public String funcCos(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" COS(").append(d).append(')');
    return sBuf.toString();
  }

  public String funcDateToChar(String datefield, String style) {
    if(style==null||style.length()==0){
      style = "YYYYMMDD";
    }
    style = style.toUpperCase();
    
    String mystyle = "%Y%m%d";
    if(style.matches("[yY]{4}[mM]{2}[dD]{2}")){
    }
    if(style.matches("[yY]{4}[mM]{2}[dD]{2} HH24:MI:SS")){
      mystyle = "%Y%m%d %H:%i:%s";
    }
    if(style.matches("[yY]{4}[mM]{2}--")){
      mystyle = "%Y%m--";
    }
    if(style.matches("[yY]{4}[mM]{2}")){
      mystyle = "%Y%m";
    }
    if(style.matches("[yY]{4}----")){
      mystyle = "%Y----";
    }
    if(style.matches("[yY]{4}")){
      return "%Y";
    }
    if(style.matches("[mM]{2}")){
      return "%m";
    }
    if(style.matches("[dD]{2}")){
      return "%d";
    }
    
    StringBuffer sql = new StringBuffer(32);
    sql.append("date_format(");
    sql.append(datefield);
    sql.append(",'").append(mystyle).append("')");
    return sql.toString();
  }

  /**
   * select date_format(current_date,'%d')
   */
  public String funcDay(String datefield) {
    StringBuffer sql = new StringBuffer(32);
    sql.append("date_format(").append(datefield).append(",'%d')");
    return sql.toString();
  }

  /**
   * 不支持的函数
   */
  public String funcEven(String d) {
    return null;
  }

  
  public String funcExp(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" EXP(").append(d).append(')');
    return sBuf.toString();
  }

  /**
   * 不支持的函数
   */
  public String funcFact(String d) {
    return null;
  }

  /**
   * 返回查找c1在c2第一次出现的位置，0表示第一个位置；
   * 此函数区分大小写；
   */
  public String funcFind(String c1, String c2) {
    StringBuffer sBuf = new StringBuffer(100).append("(INSTR(").append(c2).append(',').append(c1).append(")-1)"); //数据库是从1开始计算位置
    return sBuf.toString();
  }

  public String funcInt(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" TRUNC(").append(d).append(",0)");//截整
    return sBuf.toString();
  }

  public String funcLeft(String source, String len) {
    StringBuffer sBuf = new StringBuffer(50).append(" LEFT(").append(source).append(",").append(len).append(')');
    return sBuf.toString();
  }

  public String funcLen(String field) {
    StringBuffer sBuf = new StringBuffer(50).append(" LENGTH(").append(field).append(')');
    return sBuf.toString();
  }

  public String funcLn(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" LN(").append(d).append(')');
    return sBuf.toString();
  }

  public String funcLog(String d, String dValue) {
    StringBuffer sBuf = new StringBuffer(40).append(" LOG(").append(d).append(',').append(dValue).append(')');
    return sBuf.toString();
  }

  public String funcLower(String field) {
    StringBuffer sBuf = new StringBuffer(50).append(" LOWER(").append(field).append(')');
    return sBuf.toString();
  }

  public String funcMid(String field, String iFrom, String len) {
    StringBuffer sb = new StringBuffer(50).append("SUBSTRING(").append(field).append(","); //系统函数从0开始
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

  public String funcMod(String iValue, String i) {
    if(iValue==null||iValue.trim().length()==0){
      return "cast(null as int)";
    }
    if(i==null||i.trim().length()==0){
      return "cast(null as double)";
    }
    StringBuffer sBuf = new StringBuffer(40).append(" MOD(").append(iValue).append(',').append(i).append(')');
    return sBuf.toString();
  }

  public String funcMonth(String datefield) {
    StringBuffer sql = new StringBuffer(32);
    sql.append("date_format(").append(datefield).append(",'%m')");
    return sql.toString();
  }

  public String funcNow() {
    return "now()";
  }

  public String funcPi() {
    return "pi()";
  }

  public String funcPower(String dValue, String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" POWER(").append(dValue).append(',').append(d).append(')');
    return sBuf.toString();
  }

  public String funcRand() {
    return "RANDOM()";
  }

  /**
   * SELECT REPEAT('123',3) AS RESULT;
   * 输出结果如下：                  
   *   RESULT
   * -----------
   * 123123123
   */
  public String funcRepeat(String field, String count) {
    StringBuffer sql = new StringBuffer(32);
    sql.append("REPEAT(").append(field).append(",").append(count).append(")");
    return sql.toString();
  }

  public String funcRight(String source, String len) {
    StringBuffer sBuf = new StringBuffer(50).append(" RIGHT(").append(source).append(",").append(len).append(')');
    return sBuf.toString();
  }

  public String funcRound(String d, String i) {
    StringBuffer sBuf = new StringBuffer(40).append(" ROUND(").append(d).append(',').append(i).append(')');
    return sBuf.toString();
  }

  /**
   * 和find的区别是，search不区分大小写；
   */
  public String funcSearch(String sub, String toSearch) {
    StringBuffer sBuf = new StringBuffer(100)//转成大小
        .append(" ( INSTR(UPPER(").append(toSearch).append(") , UPPER(").append(sub).append(") ) -1)"); //数据库是从1开始计算位置
    return sBuf.toString();
  }

  public String funcSeconds(String datefield, String datefield2) {
    StringBuffer sql = new StringBuffer(32);
    sql.append("datediff(ss,").append(datefield2).append(',').append(datefield).append(')');
    return sql.toString();
  }
  
  public String funcDays(String datefield,String datefield2){
    StringBuffer sql = new StringBuffer(32);
    sql.append("datediff(day,").append(datefield2).append(',').append(datefield).append(")+1");
    return sql.toString();
  }

  public String funcSign(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" SIGN(").append(d).append(')');
    return sBuf.toString();
  }

  public String funcSin(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" SIN(").append(d).append(')');
    return sBuf.toString();
  }

  public String funcSqrt(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" SQRT(").append(d).append(')');
    return sBuf.toString();
  }

  public String funcTan(String d) {
    StringBuffer sBuf = new StringBuffer(40).append(" TAN(").append(d).append(')');
    return sBuf.toString();
  }

  public String funcToDate(String date) {
    return null;
  }

  public String funcToDateTime(String dtstr) {
    return null;
  }

  public String funcToday() {
    return "current_date";
  }

  public String funcTrim(String field) {
    StringBuffer sBuf = new StringBuffer(40).append(" TRIM(").append(field).append(')');
    return sBuf.toString();
  }

  public String funcTrunc(String d, String i) {
    if(d==null||d.trim().length()==0){
      return "cast(null as double)";
    }
    if(i==null||i.trim().length()==0){
      return "cast(null as double)";
    }
    StringBuffer sBuf = new StringBuffer(40).append(" TRUNC(").append(d).append(',').append(i).append(')');
    return sBuf.toString();
  }

  public String funcUpper(String field) {
    StringBuffer sBuf = new StringBuffer(40).append(" UPPER(").append(field).append(')');
    return sBuf.toString();
  }

  public String funcWholeReplace(String source, String oldSub, String newSub) {
    StringBuffer sBuf = new StringBuffer(50).append(" REPLACE(").append(source).append(',').append(oldSub).append(',')
        .append(newSub).append(')');
    return sBuf.toString();
  }

  public String funcYear(String datefield) {
    StringBuffer sql = new StringBuffer(32);
    sql.append("date_format(").append(datefield).append(",'%Y')");
    return sql.toString();
  }

	/**
	 * {@inheritDoc}
	 */
	public String getTruncateTableSql(String tablename) {
		return "DELETE FROM TABLE " + tablename;
	}
}
