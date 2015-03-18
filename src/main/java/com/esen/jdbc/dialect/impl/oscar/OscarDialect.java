package com.esen.jdbc.dialect.impl.oscar;

import java.sql.SQLException;
import java.util.ArrayList;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.impl.DialectImpl;
import com.esen.util.i18n.I18N;

public class OscarDialect extends DialectImpl {

  public OscarDialect(Object f) {
    super(f);
  }
  
  public DbDefiner createDbDefiner() {
    return new OscarDef(this);
  }

  public String funcAbs(String d) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcC(String d) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcChar(String ascii) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcCode(String sourece) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcCos(String d) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcDateToChar(String datefield, String style) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcDay(String datefield) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcEven(String d) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcExp(String d) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcFact(String d) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcFind(String sub, String toFind) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcInt(String d) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcLeft(String source, String len) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcLen(String field) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcLn(String d) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcLog(String d, String dValue) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcLower(String field) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcMid(String field, String iFrom, String len) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcMod(String iValue, String i) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcMonth(String datefield) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcNullvalue(String field, String defaultValue) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcPi() {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcPower(String dValue, String d) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcRand() {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcRepeat(String field, String count) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcRight(String source, String len) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcRound(String d, String i) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcSearch(String sub, String toSearch) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcSign(String d) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcSin(String d) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcSqrt(String d) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcStrCat(String field1, String field2) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcTan(String d) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcToDate(String date) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcToDateTime(String dtstr) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcToSql(String LocalFunc, ArrayList params)
      throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcToday() {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcTrim(String field) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcTrunc(String d, String i) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcUpper(String field) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcWholeReplace(String source, String oldSub, String newSub) {
    // TODO Auto-generated method stub
    return null;
  }

  public String funcYear(String datefield) {
    // TODO Auto-generated method stub
    return null;
  }


  public String formatOffsetDate(String datefield,int offset,char ymd){
//    throw  new RuntimeException("暂不支持；");
	  throw  new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.oscar.oscardialect.unsupport1", "暂不支持；"));
  }
  public String funcCharToDate(String charfield,String style){
    return null;
  }

  protected String getStrLengthSql(String str) {
    return str;
  }

  public String funcNow() {
    // TODO Auto-generated method stub
    return null;
  }
  public String getDeleteSql(String tbname,String tbname2 ,String[] keys, String[] keys2){
    return null;
  }

  public String funcSeconds(String datefield, String datefield2) {
    // TODO Auto-generated method stub
    return null;
  }
}
