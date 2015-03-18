package com.esen.jdbc.dialect;

public class TableTriggerMetaData {
  private String name;

  private String[] column;//触发器作用的列名

  private String body;

  private String desc;

  public TableTriggerMetaData(String name) {
    this.name = name;
  }

  public void setColumn(String[] column) {
    this.column = column;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public String getName() {
    return this.name;
  }

  public String[] getColumn() {
    return this.column;
  }

  public String getBody() {
    return this.body;
  }

  public String getDesc() {
    return this.desc;
  }
}
