package com.esen.jdbc.dialect;

public class TableSequenceMetaData {
  private String name;

  private int minvalue;

  private double maxvalue;

  private int step;

  public TableSequenceMetaData(String name) {
    this.name = name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setMinValue(int minvalue) {
    this.minvalue = minvalue;
  }

  public void setMaxValue(double maxvalue) {
    this.maxvalue = maxvalue;
  }

  public void setStep(int step) {
    this.step = step;
  }

  public String getName() {
    return this.name;
  }

  public int getMinValue() {
    return this.minvalue;
  }

  public double getMaxValue() {
    return this.maxvalue;
  }

  public int getStep() {
    return this.step;
  }
}
