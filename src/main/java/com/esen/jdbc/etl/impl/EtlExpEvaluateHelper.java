package com.esen.jdbc.etl.impl;

import java.util.HashMap;

import com.esen.jdbc.etl.EtlDataFieldMeta;
import com.esen.jdbc.etl.EtlDataMeta;
import com.esen.jdbc.etl.EtlDataSrc;
import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.StrFunc;
import com.esen.util.StringMap;
import com.esen.util.exp.ExpEvaluateHelper;
import com.esen.util.exp.ExpVar;
import com.esen.util.exp.ExpVarArray;
import com.esen.util.exp.ExpressionNode;
import com.esen.util.exp.util.ExpEvaluateHelper_AbstractImpl;
import com.esen.util.i18n.I18N;

public class EtlExpEvaluateHelper extends ExpEvaluateHelper_AbstractImpl {
  private EtlDataMeta srcMeta;

  private HashMap fields = new HashMap();

  private EtlDataSrc src;//数据源

  private StringMap fieldinfo;

  public EtlExpEvaluateHelper(EtlDataMeta srcMeta, String fieldinfostr) {
    this.srcMeta = srcMeta;
    int count = this.srcMeta.getFieldCount();
    for (int i = 0; i < count; i++) {
      EtlDataFieldMeta field = this.srcMeta.getField(i);
      fields.put(field.getFieldName(), new Integer(i));
    }
    fieldinfo = new StringMap(fieldinfostr, ";", ":");
  }

  public void setDataSrc(EtlDataSrc src) {
    this.src = src;
  }

  public long getInt(ExpVar var, ExpEvaluateHelper h) {
    Object obj = getObject(var, h);
    return (long) StrFunc.parseDouble(obj, 0);
  }

  public double getDouble(ExpVar var, ExpEvaluateHelper h) {
    Object obj = getObject(var, h);
    return StrFunc.parseDouble(obj, Double.NaN);
  }

  public boolean getBoolean(ExpVar var, ExpEvaluateHelper h) {
    Object obj = getObject(var, h);
    return StrFunc.parseBoolean(obj, false);
  }

  public String getString(ExpVar var, ExpEvaluateHelper h) {
    Object obj = getObject(var, h);
    return obj == null ? null : obj.toString();
  }

  public Object getObject(ExpVar var, ExpEvaluateHelper h) {
    String name = var.getName().toUpperCase();
    int i = getFieldIndex(name);
    if (i < 0) {
      String fieldname = this.fieldinfo.getValue(name);
      i = getFieldIndex(fieldname);
    }
    if (i >= 0) {
      return this.src.getObject(i);
    }
//    throw new RuntimeException("计算时无法获得变量" + name + "的值");
    throw new RuntimeException(I18N.getString("com.esen.jdbc.etl.impl.etlexpevaluatehelper.valuenotexist", "计算时无法获得变量{0}的值", new Object[]{name}));
  }

  public ExpVar getVar(ExpVar var, ExpEvaluateHelper h) {
    return null;
  }

  public ExpVarArray getArray(ExpVar var, ExpEvaluateHelper h) {
    return null;
  }

  public long calcFuncInt(ExpressionNode node, ExpEvaluateHelper h) {
    return 0;
  }

  public String calcFuncStr(ExpressionNode node, ExpEvaluateHelper h) {
    return null;
  }

  public double calcFuncDouble(ExpressionNode node, ExpEvaluateHelper h) {
    return 0;
  }

  public boolean calcFuncBool(ExpressionNode node, ExpEvaluateHelper h) {
    return false;
  }

  public Object calcFuncObject(ExpressionNode node, ExpEvaluateHelper h) {
    return null;
  }

  private int getFieldIndex(String fieldname) {
    synchronized (fields) {
      Integer i = (Integer) fields.get(fieldname);
      return i == null ? -1 : i.intValue();
    }
  }
}
