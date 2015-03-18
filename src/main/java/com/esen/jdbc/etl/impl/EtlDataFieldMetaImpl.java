package com.esen.jdbc.etl.impl;

import java.io.Serializable;

import com.esen.jdbc.etl.EtlDataFieldMeta;

public class EtlDataFieldMetaImpl implements EtlDataFieldMeta, Serializable {
	/**
	 * BI-5462 对象序列化异常
	 * 原因：没有实现Serializable接口
	 */
	private static final long serialVersionUID = -7545148706924421359L;

private String name;

  private String description;

  private char type;

  private int length;

  private int scale;

  private int fieldindex;

  private boolean isdim;

  private String dimname;

  private int uniquedindex;

  private String dbfield;

  public EtlDataFieldMetaImpl(String name) {
    this.name = name == null ? null : name.toUpperCase();
  }

  public EtlDataFieldMetaImpl(int fieldindex) {
    this.fieldindex = fieldindex;
  }

  public String getFieldName() {
    return this.name;
  }

  public char getDataType() {
    return this.type;
  }

  public String getDescription() {
    return this.description;
  }

  public int getLength() {
    return this.length;
  }

  public int getScale() {
    return this.scale;
  }

  public int getFiledIndex() {
    return this.uniquedindex;
  }

  public boolean isDimField() {
    return this.isdim;
  }

  public String getDimName() {
    return this.dimname;
  }

  public int getUniquedIndex() {
    return this.uniquedindex;
  }

  public String getDbField() {
    return this.dbfield;
  }

  public void setFieldName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setFieldIndex(int fieldindex) {
    this.fieldindex = fieldindex;
  }

  public void setDataType(char type) {
    this.type = type;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public void setScale(int scale) {
    this.scale = scale;
  }

  public void setUniquedIndex(int uniquedindex) {
    this.uniquedindex = uniquedindex;
  }

  public void setIsDimField(boolean isdimfield) {
    this.isdim = isdimfield;
  }

  public void setDimName(String dimname) {
    this.dimname = dimname;
  }

  public void setDbField(String dbfield) {
    this.dbfield = dbfield;
  }
}
