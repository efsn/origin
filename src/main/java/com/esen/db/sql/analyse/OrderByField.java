package com.esen.db.sql.analyse;

public class OrderByField {
  private String tag;
  private String field;
  private String order;
  private boolean desc;//降序 为 true
  public OrderByField(String orderfield){
    String[] fd = orderfield.trim().split(" ");
    String[] ff = fd[0].split("\\.");
    if(ff.length>1){
      tag = ff[0];
      field = ff[1];
    }else field = fd[0];
    desc = false;
    if(fd.length>1){
      order = fd[1];
      desc = order.equalsIgnoreCase("desc");
    }
  }
  public String getTag(){
    return tag;
  }
  public String getField(){
    return field;
  }
  public String getOrder(){
    return order;
  }
  public boolean isDesc(){
    return desc;
  }
}
