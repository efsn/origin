package com.esen.jdbc.etl.impl;

import java.io.Serializable;

import com.esen.jdbc.etl.EtlDataFieldMeta;
import com.esen.jdbc.etl.EtlDataMeta;
import com.esen.util.StrFunc;
import com.esen.util.StringMap;
import com.esen.util.exp.ExpCompilerHelper;
import com.esen.util.exp.ExpException;
import com.esen.util.exp.ExpFuncOp;
import com.esen.util.exp.ExpVar;
import com.esen.util.exp.ExpVarImpl;

public class EtlExpCompilerHelper implements ExpCompilerHelper, Serializable {
	private static final long serialVersionUID = 2498516367589551055L;

private StringMap fieldinfo;

  private EtlDataMeta srcMeta;

  public EtlExpCompilerHelper(EtlDataMeta srcMeta, String fieldinfostr) {
    this.srcMeta = srcMeta;
    //fieldinfo 字段对应关系，如：bbq:XXB.BBQ;...
    fieldinfo = new StringMap(fieldinfostr, ";", ":");
  }

  public ExpVar getExpVar(String var) throws ExpException {
    if (StrFunc.isNull(var)) {
      return null;
    }
    var = var.toUpperCase();
    EtlDataFieldMeta field = this.srcMeta.getField(var);
    if (field == null) {
      String fieldname = fieldinfo.getValue(var);
      field = this.srcMeta.getField(fieldname);
    }
    return field == null ? null : new ExpVarImpl(field.getFieldName(), field
        .getDataType());
  }

  public ExpFuncOp getFuncInfo(String funcName) {
    return null;
  }
}
