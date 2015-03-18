package com.esen.db.sql;

import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.Dialect;

public class OrderByInfo {
  private String orderby;

  private boolean desc;// true降序，其他升序
  private boolean desc_nullsfirst;
  private boolean asc_nullslast;
  private boolean needProcNullSort;
  private Dialect dialect;
/**
 * 数据库中空值做无穷大处理的，排序时降序空值总在最前，使用 nulls last 将空值挪到最后；
 * desc_nullsfirst=true; //降序时NULL排序到最前
     asc_nullslast=true;   //升序时NULL排序到最后
     这种空值的处理只有Oracle，DB2是这样；
 * @param orderby
 * @param desc
 */
  public OrderByInfo(Dialect dialect,String orderby, boolean desc,boolean desc_nullsfirst,boolean asc_nullslast) {
    this.orderby = orderby;
    this.desc = desc;
    this.desc_nullsfirst = desc_nullsfirst;
    this.asc_nullslast = asc_nullslast;
    this.dialect = dialect;
    this.needProcNullSort = isProcNullSort(dialect);
  }

  public boolean isDesc_nullsfirst() {
    return desc_nullsfirst;
  }

  public boolean isAsc_nullslast() {
    return asc_nullslast;
  }

  public Dialect getDialect() {
    return dialect;
  }

  public static boolean isProcNullSort(Dialect dialect) {
    if(dialect==null) return false;
    DataBaseInfo db = dialect.getDataBaseInfo();
    if(db.isOracle()){
      return true;
    }
    return false;
  }

  public String getOderbySrt() {
    return orderby;
  }

  public boolean isSortDesc() {
    return desc;
  }

  public String toString() {
    StringBuffer str = new StringBuffer(64);
    str.append(getOderbySrt());
    if (desc){
      str.append(" desc ");
      if(needProcNullSort&&!desc_nullsfirst)
        str.append(" nulls last");
    }else{
      if(needProcNullSort&&!asc_nullslast){
        str.append(" nulls first");
      }
    }
    return str.toString();
  }
}
