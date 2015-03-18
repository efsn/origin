package com.esen.jdbc.etl.impl;

import java.io.Serializable;

import com.esen.jdbc.etl.EtlFieldDefine;

public class EtlFieldDefineImpl implements EtlFieldDefine, Serializable {
	private static final long serialVersionUID = -1612656302631473399L;

private String name;

  private String caption;

  private char type;

  private int length;

  private int scale;

  private String exp;

  public EtlFieldDefineImpl(String name) {
    this.name = name == null ? null : name.toUpperCase();
  }

  public String getFieldName() {
    return this.name;
  }

  public String getCaption() {
    return this.caption;
  }

  public String getSrcExp() {
    return this.exp;
  }

  public char getDataType() {
    return this.type;
  }

  public int getLength() {
    return this.length;
  }

  public int getScale() {
    return this.scale;
  }

  public void setFieldName(String name) {
    this.name = name;
  }

  public void setCaption(String caption) {
    this.caption = caption;
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

  public void setSrcExp(String exp) {
    this.exp = exp;
  }
}
