package com.esen.jdbc.etl.impl;

import java.io.Serializable;

import com.esen.jdbc.etl.EtlDefine;
import com.esen.jdbc.etl.EtlFieldDefine;
import com.esen.util.HashMapList;
import com.esen.util.StrFunc;
import com.esen.util.StringMap;

public class EtlDefineImpl implements EtlDefine, Serializable {
	private static final long serialVersionUID = 7166932475933382007L;

private String name;

  private String caption;

  private StringMap options;

  private String condition;

  private boolean isabsolute;

  protected HashMapList fields;

  public EtlDefineImpl(String name) {
    this.name = name == null ? null : name.toUpperCase();
    fields = new HashMapList();
  }

  public String getName() {
    return this.name;
  }

  public String getCaption() {
    return this.caption;
  }

  public int getDestFieldCount() {
    synchronized (fields) {
      return this.fields.size();
    }
  }

  public EtlFieldDefine getDestFieldDefine(int i) {
    synchronized (fields) {
      return (EtlFieldDefine) this.fields.get(i);
    }
  }

  public EtlFieldDefine getDestFieldDefine(String name) {
    synchronized (fields) {
      return (EtlFieldDefine) this.fields.get(name == null ? null : name
          .toUpperCase());
    }
  }

  public StringMap getOptions() {
    return this.options;
  }

  public String getImportCondition() {
    return this.condition;
  }

  public boolean isAbsolute() {
    return this.isabsolute;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setOptions(StringMap options) {
    this.options = options;
  }

  public void setImportCondition(String condition) {
    this.condition = condition;
  }

  public void setIsAbsolute(boolean isabsolute) {
    this.isabsolute = isabsolute;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public void addFieldDefine(EtlFieldDefine field) {
    synchronized (fields) {
      this.fields.put(field.getFieldName(), field);
    }
  }
}
