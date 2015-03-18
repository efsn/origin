package com.esen.jdbc.exptosql;

import java.sql.Types;

import com.esen.util.exp.ExpException;
import com.esen.util.exp.ExpVar;
import com.esen.util.exp.util.ExpSuperCompilerHelper;

public class TestExpSuperCompilerHelper extends ExpSuperCompilerHelper {
  public ExpVar getExpVar(String var) throws ExpException {
    if(var!=null&&var.equalsIgnoreCase("str_")){
      return new TestVarCell("str_".toUpperCase(),'C',Types.VARCHAR);
    }
    if(var!=null&&var.equalsIgnoreCase("int_")){
      return new TestVarCell("int_".toUpperCase(),'I',Types.INTEGER);
    }
    if(var!=null&&var.equalsIgnoreCase("num_")){
      return new TestVarCell("num_".toUpperCase(),'N',Types.DOUBLE);
    }
    if(var!=null&&var.equalsIgnoreCase("date_")){
      return new TestVarCell("date_".toUpperCase(),'D',Types.DATE);
    }
    if(var!=null&&var.equalsIgnoreCase("timestamp_")){
      return new TestVarCell("timestamp_".toUpperCase(),'D',Types.TIMESTAMP);
    }
    if(var!=null&&var.equalsIgnoreCase("strbbq_")){//yyyymmdd
      return new TestVarCell("strbbq_".toUpperCase(),'D',Types.VARCHAR);
    }
    if(var!=null&&var.equalsIgnoreCase("intbbqymd")){//yyyymmdd
      return new TestVarCell("intbbqymd".toUpperCase(),'D',Types.INTEGER);
    }
    if(var!=null&&var.equalsIgnoreCase("intbbqym")){//yyyymm
      return new TestVarCell("intbbqym".toUpperCase(),'D',Types.INTEGER);
    }
    if(var!=null&&var.equalsIgnoreCase("intbbqy")){//yyyy
      return new TestVarCell("intbbqy".toUpperCase(),'D',Types.INTEGER);
    }
    if(var!=null&&var.equalsIgnoreCase("charbbqym")){//yyyymm
      return new TestVarCell("charbbqym".toUpperCase(),'D',Types.VARCHAR);
    }
    if(var!=null&&var.equalsIgnoreCase("charbbqy")){//yyyy
      return new TestVarCell("charbbqy".toUpperCase(),'D',Types.VARCHAR);
    }
    if(var!=null&&var.equalsIgnoreCase("charbbqym_")){//yyyymm--
      return new TestVarCell("charbbqym_".toUpperCase(),'D',Types.VARCHAR);
    }
    if(var!=null&&var.equalsIgnoreCase("charbbqym00")){//yyyymm00
      return new TestVarCell("charbbqym00".toUpperCase(),'D',Types.VARCHAR);
    }
    if(var!=null&&var.equalsIgnoreCase("charbbqy_")){//yyyy----
      return new TestVarCell("charbbqy_".toUpperCase(),'D',Types.VARCHAR);
    }
    if(var!=null&&var.equalsIgnoreCase("charbbqy0000")){//yyyy0000
      return new TestVarCell("charbbqy0000".toUpperCase(),'D',Types.VARCHAR);
    }
    return super.getExpVar(var);
  }
}
