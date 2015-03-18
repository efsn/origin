package com.esen.db.sql.oracle;

import com.esen.db.sql.Field;
/**
 * ratio_to_report(qbxssr) over(partition by pid_) pct
 * ratio_to_report(qbxssr) over() pct
 * @author dw
 *
 */
public class RatioField extends Field {
  private StringBuffer field;
  private String tag;
	private String zb;//统计的方法或者zb
  public RatioField(String zb,String[] partitions,String tag){
    super(); 
    this.zb = zb;
    field = new StringBuffer(256);
    field.append("ratio_to_report(").append(zb).append(")");
    field.append(" over ( ");
    if(partitions!=null&&partitions.length>0){
      field.append("partition by ");
      for(int i=0;i<partitions.length;i++){
        if(i>0) field.append(",");
        field.append(partitions[i]);
      }
    }
    field.append(")*100");
    this.tag = tag;
  }
  public String getField(){
    return field.toString();
  }
  public String getAlias(){
    return tag;
  }
  
  /**
   * 获取统计的方法名字
   * @return
   */
  public String getZbName(){
  	return this.zb;
  }
  
  public int getType(){
    return FTYPE_ZB;
  }
  public String toString(){
    if(tag==null||tag.length()==0)
      return field.toString();
    return field.toString() + " as " + tag;
  }
}
