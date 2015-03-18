package com.esen.db.sql;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.Dialect;

/**
 * 将自定义分组的多个sql组合成一个sql
 * @author dw
 *
 */
public class SelectUnionTable extends SelectTable {
  public static final int UNION = 1; //去除相同的项，并集
  public static final int UNION_ALL = 2;//包含相同的项，全集
  private SelectTable[] sts;
  private int uniontype;
  public SelectUnionTable(SelectTable[] sts){
    this(sts,UNION_ALL,true);
  }
  /**
   * 
   * @param sts
   * @param uniontype
   * @param autoAddField  是否需要自动加上第一个sql的字段
   */
  public SelectUnionTable(SelectTable[] sts,int uniontype,boolean autoAddField){
    super();
    this.sts = sts;
    this.uniontype = uniontype;
    if (autoAddField) {
      SelectTable st0 = sts[0];
      for (int i = 0; i < st0.getFieldCount(); i++) {
        Field f = st0.getField(i);
        this.addField(new Field(f.getAlias(), f.getAlias(), f.getType()));
      }
    }
  }
  public SelectTable[] getSelectTables(){
    return sts;
  }
  public SelectTable removeSelectTable(int i){
    SelectTable rmvst = sts[i];
    SelectTable newsts[] = new SelectTable[sts.length-1];
    if(i>0)
      System.arraycopy(sts, 0, newsts, 0, i);
    System.arraycopy(sts, i+1, newsts, i, sts.length-i-1);
    sts = newsts;
    return rmvst;
  }
  
  public String toString(){
    return getSql(null);
  }
  
  /**
   * BI-4852 20110517
   * 解决BI-4795时父类SelectTable方法做了调整，加了参数，这里也要做相应的改动，否则出现异常；
   */
  public String getSql(Dialect dl,int oracleJoinType){
    if(sts.length==1)
      return sts[0].getSql(dl,oracleJoinType);
    StringBuffer sql = new StringBuffer(512);
    sql.append("select ");
    for(int i=0;i<this.getFieldCount();i++){
      if(i>0) sql.append(",");
      sql.append(this.getField(i).toString());
    }
    sql.append("\r\n from (\r\n");
    for(int i=0;i<sts.length;i++){
      if(i>0) {
        if(uniontype==UNION_ALL)
          sql.append(" \r\n union all \r\n");
        else sql.append(" \r\n union \r\n");
      }
      sql.append(sts[i].getSql(dl,oracleJoinType));
    }
    sql.append("\r\n) un_");//这里给别名，是因为适应mysql
    if (getGroupByCount() > 0) {
      sql.append("\r\n");
      sql.append("group by ");
    }
    for (int i = 0; i < getGroupByCount(); i++) {
      if (i > 0)
        sql.append(",");
      sql.append(getGroupBy(i));
    }
    if (getOrderByFieldCount() > 0) {
      sql.append("\r\n");
      sql.append("order by ");
    }
    for (int i = 0; i < getOrderByFieldCount(); i++) {
      if (i > 0)
        sql.append(",");
      sql.append(getOrderByField(i).toString());
    }
    return sql.toString();
  }
  
  public Object clone(){
    SelectUnionTable ust = (SelectUnionTable)super.clone();
    ust.uniontype = uniontype;
    ust.sts = new SelectTable[sts.length];
    for(int i=0;i<sts.length;i++){
      ust.sts[i] = (SelectTable)sts[i].clone();
    }
    return ust;
  }
}
