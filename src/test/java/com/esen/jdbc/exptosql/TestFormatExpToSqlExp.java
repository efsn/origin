package com.esen.jdbc.exptosql;


import com.esen.jdbc.FormatExpToSqlExp;
import com.esen.jdbc.dialect.Dialect;
import com.esen.util.exp.ExpressionNode;
import com.esen.util.exp.ExpVar;

public class TestFormatExpToSqlExp extends FormatExpToSqlExp {

  public TestFormatExpToSqlExp(Dialect dialect) {
    super(dialect);
  }
  public int getDataSqlType(ExpressionNode nd){
    ExpVar vc = nd.getVar();
    if(vc!=null&&vc instanceof TestVarCell){
      TestVarCell tvc = (TestVarCell)vc;
      return tvc.getSqlType();
    }
    return super.getDataSqlType(nd);
  }
  public String getDateStrStyle(ExpressionNode nd){
    ExpVar vc = nd.getVar();
    if(vc!=null&&vc instanceof TestVarCell){
      TestVarCell tvc = (TestVarCell)vc;
      String nm = tvc.getName();
      if(nm.equalsIgnoreCase("strbbq_")){
        return "yyyymmdd";
      }
      if(nm.equalsIgnoreCase("intbbqymd")){
        return "yyyymmdd";
      }
      if(nm.equalsIgnoreCase("intbbqym")){
        return "yyyymm";
      }
      if(nm.equalsIgnoreCase("intbbqy")){
        return "yyyy";
      }
      if(nm.equalsIgnoreCase("charbbqym")){
        return "yyyymm";
      }
      if(nm.equalsIgnoreCase("charbbqy")){
        return "yyyy";
      }
      if(nm.equalsIgnoreCase("charbbqym_")){
        return "yyyymm--";
      }
      if(nm.equalsIgnoreCase("charbbqym00")){
        return "yyyymm00";
      }
      if(nm.equalsIgnoreCase("charbbqy_")){
        return "yyyy----";
      }
      if(nm.equalsIgnoreCase("charbbqy0000")){
        return "yyyy0000";
      }
    }
    return null;
  }
}
