package com.esen.jdbc.dialect.impl.timesten;

import java.sql.SQLException;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.jdbc.dialect.impl.oracle.OracleDialect;

public class TimesTenDialect extends OracleDialect {

  public TimesTenDialect(Object con_OR_conf) {
    super(con_OR_conf);
  }
  public DbDefiner createDbDefiner() {
    return new TimesTenDef(this);
  }
  public DbMetaData createDbMetaData() throws SQLException {
    if(dbmd==null){
      dbmd = connectionFactory!=null?new DbMetaDataImpl(connectionFactory):new DbMetaDataImpl(con);
    }
    return dbmd;
  }
  
  public boolean supportsViewColumnComment() {
	return false; 
  }  

  public String funcAsStr(String v){
    if(v==null)
      return "null";
    try{
      Double.parseDouble(v);
    }catch(NumberFormatException ne){
      return "to_char("+v+")";
    }
    return super.funcAsStr(v);
  }
  public String funcSqrt(String d) {
    if(d==null)
      return "null";
    return super.funcSqrt(d);
  }
  public String funcInt(String d) {
    if(d==null)
      return "null";
    return super.funcInt(d);
  }
  public String funcSign(String d) {
    if(d==null)
      return "null";
    return super.funcSign(d);
  }
  public String funcMod(String iValue, String i) {
    if(iValue==null||i==null)
      return "null";
    return super.funcMod(iValue, i);
  }
  public String funcPower(String dValue, String d) {
    if(dValue==null||d==null)
      return "null";
    return super.funcPower(dValue, d);
  }
  public String funcRound(String d, String i) {
    if(d==null||i==null)
      return "null";
    return super.funcRound(d, i);
  }
  public String funcTrunc(String d, String i) {
    if(d==null||i==null)
      return "null";
    return super.funcTrunc(d, i);
  }
  public String funcDays(String datefield,String datefield2){
    //此方法不能垮年计算;
    return "abs( to_number(to_char("+datefield+",'ddd'))-to_number(to_char("+datefield2+",'ddd')))+1";
  }
  public String funcChar(String ascii) {
    if(ascii==null)
      return "null";
    return super.funcChar(ascii);
  }
  public String funcCode(String sourece) {
    if(sourece==null)
      return "null";
    StringBuffer sBuf = new StringBuffer(50).append(" ASCIISTR(").append(sourece)
        .append(')');
    return sBuf.toString();
  }
  public String funcFind(String sub, String toFind) {
    if(sub==null||toFind==null)
      return "null";
    return super.funcFind(sub, toFind);
  }
  public String funcLeft(String source, String len) {
    if(source==null||len==null)
      return "null";
    return super.funcLeft(source, len);
  }
  public String funcRight(String source, String len) {
    if(source==null||len==null)
      return "null";
    return super.funcRight(source, len);
  }
  public String funcMid(String field, String iFrom, String len) {
    if(field==null||iFrom==null||len==null)
      return "null";
    return super.funcMid(field, iFrom, len);
  }
  public String funcLen(String field) {
    if(field==null)
      return "null";
    return super.funcLen(field);
  }
  public String funcLower(String field) {
    if(field==null)
      return "null";
    return super.funcLower(field);
  }
  public String funcUpper(String field) {
    if(field==null)
      return "null";
    return super.funcUpper(field);
  }
  public String funcStrCat(String field1, String field2) {
    if(field1==null)
      field1="''";
    if(field2==null)
      field2="''";
    return super.funcStrCat(field1, field2);
  }
  public String funcTrim(String field) {
    if(field==null)
      return "null";
    return super.funcTrim(field);
  }
  
	/**
	 * {@inheritDoc}
	 */
	public String getTruncateTableSql(String tablename) {
		if (tablename == null) {
			return "null";
		}
		return super.getTruncateTableSql(tablename);
	}
}
