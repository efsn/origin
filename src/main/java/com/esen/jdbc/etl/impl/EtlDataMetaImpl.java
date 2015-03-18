package com.esen.jdbc.etl.impl;

import java.io.Serializable;

import com.esen.jdbc.etl.EtlDataFieldMeta;
import com.esen.jdbc.etl.EtlDataMeta;
import com.esen.util.HashMapList;

public class EtlDataMetaImpl implements EtlDataMeta, Serializable {
	private static final long serialVersionUID = 8189537259309835574L;

private String name;

  private String caption;

  private HashMapList fields = new HashMapList();

  public EtlDataMetaImpl(String name) {
    this.name = name;
  }

  public String getCaption() {
    return this.caption;
  }

  public EtlDataFieldMeta getField(int i) {
    synchronized (this.fields) {
      return (EtlDataFieldMeta) this.fields.get(i);
    }
  }

  public int getFieldCount() {
    synchronized (this.fields) {
      return this.fields.size();
    }
  }

  public EtlDataFieldMeta getField(String fldname) {
    synchronized (this.fields) {
      return (EtlDataFieldMeta) this.fields.get(fldname);
    }
  }

  public String getName() {
    return this.name;
  }

  public void addField(EtlDataFieldMeta field) {
    synchronized (this.fields) {
      this.fields.put(field.getFieldName(), field);
    }
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public String toString() {
    return this.name;
  }
}
