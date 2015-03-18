package com.esen.db.sql.analyse;

public class OrderByInfo {
  private OrderByField[] ofs;
  private String fds;
  /**
   * 排序字段对象

   * @param fds 排序字段字符串 "id_ desc,_name"
   */
  public OrderByInfo(String fds){
    this.fds = fds;
    String[] fields = fds.trim().split(",");
    ofs = new OrderByField[fields.length];
    for(int i=0;i<fields.length;i++){
      ofs[i] = new OrderByField(fields[i]);
    }
  }

  public OrderByField[] getFields(){
    return ofs;
  }
  public String toString(){
    return fds;
  }
}
