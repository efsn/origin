package com.esen.jdbc;

import java.util.Calendar;

import com.esen.util.StrFunc;

/**
 * 描述存储过程参数
 * @author dw
 *
 */
public class CallableParam{
  public static final int CALLABLE_TYPE_IN = 0;
  public static final int CALLABLE_TYPE_OUT = 1;
  public static final int CALLABLE_TYPE_INOUT = 2;
  
  public static final char SQLTYPE_NUMBER = 'N';
  public static final char SQLTYPE_CHAE = 'C';
  public static final char SQLTYPE_DATE = 'D';
  public static final char SQLTYPE_RESULTSET = 'X';
  
  /**
   * 记录参数在存储过程中的类型，输入还是输出；
   * 0:输入,1:输出,2:输入输出
   */
  private int t;
  /**
   * 记录参数的sql类型，支持：
   * N:数值型,C:字符型,D:日期型,X:结果集类型
   */
  private char sqltype;
  /**
   * 参数值；
   */
  private Object v;
  
  public CallableParam(int t,char sqltype,Object v){
    this.t = t;
    this.sqltype = sqltype;
    this.v = v;
  }
  
  public int getType(){
    return t;
  }
  
  public char getSqlType(){
    return sqltype;
  }
  
  public Object getValue(){
    return v;
  }
  
  public String toString(){
    switch(t){
      case CALLABLE_TYPE_IN:{
        return inParamToString();
      }
      case CALLABLE_TYPE_OUT:{
        return outParamToString();
      }
      case CALLABLE_TYPE_INOUT:{
        return inoutParamToString();
      }
    }
    return null;
  }

  private String inoutParamToString() {
    switch(sqltype){
      case SQLTYPE_NUMBER:{
        return "$"+v.toString();
      }
      case SQLTYPE_CHAE:{
        String vs = (String)v;
        vs = vs.replaceAll("\\'", "''");
        return "$'"+vs+"'";
        
      }
      case SQLTYPE_DATE:{
        return "$#"+StrFunc.date2str((Calendar)v, "yyyymmdd")+"#";
      }
    }
    return null;
  }

  private String outParamToString() {
    switch(sqltype){
      case SQLTYPE_NUMBER:{
        return "$n";
      }
      case SQLTYPE_CHAE:{
        return "$c";
      }
      case SQLTYPE_DATE:{
        return "$d";
      }
      case SQLTYPE_RESULTSET:{
        return "$cursor";
      }
    }
    return null;
  }

  private String inParamToString() {
    switch(sqltype){
      case SQLTYPE_NUMBER:{
        return v.toString();
      }
      case SQLTYPE_CHAE:{
        return v.toString();
      }
      case SQLTYPE_DATE:{
        return StrFunc.date2str((Calendar)v, null); 
      }
    }
    return null;
  }
}
