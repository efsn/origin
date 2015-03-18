package com.esen.db.sql.rank;

import com.esen.db.sql.Field;
import com.esen.db.sql.OrderByInfo;
import com.esen.jdbc.dialect.Dialect;
/**
 * rank() over (partition by olap2,olap3 order by zb1 desc) sortn
 * @author dw
 *
 */
public class SortNField extends Field {
  public static final int SORT_RANK = 0;              //1 2 2 4
  public static final int SORT_DENSE_RANK = 1;        //1 2 2 3
  public static final int SORT_ROW_NUMBER = 2;        //1 2 3 4
  public static final int SORT_TYPE_ASC = 1;
  private StringBuffer field;
  private String tag;
  private String[] partitions;
  private boolean desc = false;
	private String zb;//判断统计方法的名字
  public SortNField(Dialect dialect,int sorttype,String[] partitions,String orderby,int sortway, String tag) {
    super();
    this.zb = orderby;
    this.partitions = partitions;
    field = new StringBuffer(256);
    switch(sorttype){
      case SORT_DENSE_RANK:
      	/*
      	 * BUG:BI-8606: add by liujin 2013.06.27
      	 * Teradata 数据库不支持 dense_rank 函数，用  rank 代替。
      	 */
      	if (dialect != null && !dialect.supportsDenseRank()) {
      		field.append("rank()");
      	} else {
      		field.append("dense_rank()");
      	}
        break;
      case SORT_ROW_NUMBER:
        field.append("row_number()");
        break;
      case SORT_RANK:
      default:
          field.append("rank()");
    }
    field.append(" over (");
    if(partitions!=null&&partitions.length>0){
      field.append("partition by ");
      for(int i=0;i<partitions.length;i++){
        if(i>0) field.append(",");
        field.append(partitions[i]);
      }
    }
    boolean f = OrderByInfo.isProcNullSort(dialect);
    field.append(" order by ");
    field.append(orderby);
    switch(sortway){
      case SORT_TYPE_ASC:
        break;
      default:
        desc = true;
        field.append(" desc");
        if(f)
          field.append(" nulls last");
    }
    field.append(")");
    this.tag = tag;
  }
  public boolean isDesc(){
    return desc;
  }
  public String[] getPartitionby(){
    return partitions;
  }
  public String getField(){
    return field.toString();
  }
  public String getAlias(){
    return tag;
  }
  public int getType(){
    return FTYPE_ZB;
  }
  public String toString(){
    if(tag==null||tag.length()==0)
      return field.toString();
    return field.toString() + " as " + tag;
  }
  
  /**
   * 获取统计方法名称
   * @return
   */
  public String getZbName(){
  	return this.zb;
  }
}
