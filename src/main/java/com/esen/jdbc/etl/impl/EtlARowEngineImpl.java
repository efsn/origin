package com.esen.jdbc.etl.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.sql.*;

import com.esen.jdbc.etl.EtlARowEngine;
import com.esen.jdbc.etl.EtlDataDest;
import com.esen.jdbc.etl.EtlDataFieldMeta;
import com.esen.jdbc.etl.EtlDataMeta;
import com.esen.jdbc.etl.EtlDataSrc;
import com.esen.jdbc.etl.EtlDefine;
import com.esen.jdbc.etl.EtlFieldDefine;
import com.esen.util.StrFunc;
import com.esen.util.StringMap;
import com.esen.util.exp.ExpUtil;
import com.esen.util.exp.Expression;

public class EtlARowEngineImpl extends EtlARowEngine {
  private EtlDefine def;

  private EtlDataMeta srcMeta;

  private EtlDataMeta destMeta;

  private HashMap exps = new HashMap();

  private EtlExpCompilerHelper compiler;

  private EtlExpEvaluateHelper eval;

  private Expression allcondition;

  private Expression condition;

  public EtlARowEngineImpl(EtlDefine def, EtlDataMeta srcMeta,
      EtlDataMeta destMeta) {
    this.def = def;
    this.srcMeta = srcMeta;
    this.destMeta = destMeta;
  }

  /**
   * 编译表达式
   */
  public void compile() {
    String fieldinfo = def.getOptions().getValue("fieldinfo");
    compiler = new EtlExpCompilerHelper(this.srcMeta, fieldinfo);
    eval = new EtlExpEvaluateHelper(srcMeta, fieldinfo);

    //字段的表达式
    int count = this.destMeta.getFieldCount();
    for (int i = 0; i < count; i++) {
      EtlDataFieldMeta field = this.destMeta.getField(i);
      EtlFieldDefine fdef = this.def.getDestFieldDefine(field.getFieldName());
      String srcexp = fdef.getSrcExp();
      Expression exp = new Expression(srcexp);

      exp.compile(compiler);

      addExp(field, exp);
    }

    //本表的导入条件
    StringMap options = def.getOptions();
    String con = def.getImportCondition();
    if (!StrFunc.isNull(con)) {
      condition = new Expression(con);
      condition.compile(compiler);
    }

    //所有表的导入条件
    String allcon = options.getValue("allcondition");
    if (!StrFunc.isNull(allcon)) {
      allcondition = new Expression(allcon);
      allcondition.compile(compiler);
    }
  }

  /**
   * 抽取一行数据
   */
  public boolean etlARow(EtlDataSrc src, EtlDataDest dest) {
    this.eval.setDataSrc(src);
    if (!canEtl()) {
      return false;
    }
    int count = this.destMeta.getFieldCount();
    for (int i = 0; i < this.destMeta.getFieldCount(); i++) {
      EtlDataFieldMeta field = this.destMeta.getField(i);
      Expression exp = getExp(field);
      Object obj = null;
      //      Object obj = exp.evaluateObject(this.eval);
      if (exp.getRootNode().isData()) {
        obj = this.eval.getObject(exp.getRootNode().getVar(), this.eval);//源数据已经转换为相应的类型，这里才能这样用
      }
      else {
        //A1+A2  Double类型 如果两个中有一个为null则结果为null
        //Int 类型， 如果都为null，返回0
        obj = exp.evaluateObject(this.eval);
      }
      if (obj instanceof Double && ((Double) obj).isNaN())
        obj = null;
      if (exp.getReturnType() == ExpUtil.TODAT) {//可能为字符串，需要处理
        obj = StrFunc.parseCalendar(obj, null);
      }
      if (obj instanceof Calendar) {//date特殊处理
        Calendar c = (Calendar) obj;
        obj = new Date(c.getTimeInMillis());
      }
      if (obj instanceof Boolean) {//boolean用字串表示
        obj = Boolean.TRUE.equals(obj) ? "T" : "F";
      }
      //checkValue(field, obj);
      dest.setValue(i, obj);
    }
    return true;
  }

//  private void checkValue(EtlDataFieldMeta field, Object obj) {
//    if (obj == null) {
//      return;
//    }
//    switch (field.getDataType()) {
//      case 'I':
//        if (obj instanceof Integer) {
//
//          //          throw new RuntimeException("数值:" + obj + "超出精度，最大长度为"
//          //              + field.getLength() + "小数位数为" + field.getScale());
//          return;
//        }
//      case 'N':
//        if (obj instanceof Float || obj instanceof Double) {
//          //          throw new RuntimeException("数值:" + obj + "超出精度，最大长度为"
//          //              + field.getLength() + "小数位数为" + field.getScale());
//          return;
//        }
//      case 'C':
//        if (obj instanceof String) {
//          if (obj.toString().length() > field.getLength()) {
//            throw new RuntimeException("数据:" + obj + "的长度超过了字段"
//                + field.getFieldName() + "的长度" + field.getLength());
//          }
//          return;
//        }
//      case 'D':
//        if (obj instanceof Date || obj instanceof Time
//            || obj instanceof Timestamp) {
//          return;
//        }
//      case 'L':
//        if (obj instanceof Boolean) {
//          return;
//        }
//      case 'M':
//        if (obj instanceof String) {
//          return;
//        }
//    }
//    throw new RuntimeException("字段" + field.getFieldName() + "数据类型不正确:"
//        + obj.getClass().getName() + "(字段类型为" + field.getDataType() + ")");
//  }

  /**
   * 判断是否符合导入条件
   * @return
   */
  private boolean canEtl() {
    return (this.allcondition == null ? true : this.allcondition
        .evaluateBoolean(this.eval))
        && (this.condition == null ? true : this.condition
            .evaluateBoolean(this.eval));
  }

  public boolean canDataEtl(EtlDataSrc src) {
    this.eval.setDataSrc(src);
    return this.allcondition == null ? true : this.allcondition
        .evaluateBoolean(this.eval);
  }

  private void addExp(EtlDataFieldMeta field, Expression exp) {
    synchronized (exps) {
      this.exps.put(field, exp);
    }
  }

  private Expression getExp(EtlDataFieldMeta field) {
    synchronized (exps) {
      return (Expression) this.exps.get(field);
    }
  }
}
