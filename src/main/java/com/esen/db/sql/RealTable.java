package com.esen.db.sql;


public class RealTable extends BaseTable  {
  private String tablename;//表名
  private String tag;//别名

  public RealTable(String tablename,String tag) {
    this.tablename = tablename;
    this.tag = tag;
  }
  public String getTable(){
    return tablename;
  }
  public String getAlias(){
    return tag;
  }
  /**
   * @deprecated
   * @return
   */
  public String getTag(){
    return tag;
  }
  public String toString(){
    return tablename+" "+tag;
  }
  public Object clone() {
    RealTable rt = (RealTable)super.clone();
    rt.tablename = tablename;
    rt.tag = tag;
    return rt;

  }
}
