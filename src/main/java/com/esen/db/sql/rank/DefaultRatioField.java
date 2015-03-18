package com.esen.db.sql.rank;

import com.esen.db.sql.Field;
/**
 * salary/sum(salary) over(partition by dept)as percentage
 * 
 * @author dw
 *
 */
public class DefaultRatioField extends Field {
  private String  fd ;
  public DefaultRatioField(String field, AccField af, String tag) {
    this.fd = field;
    this.tag = tag;
    //比重需要*100,除数是0，也要处理；
    StringBuffer ss = new StringBuffer();
    ss.append("case when ");
    ss.append(af.getField()).append("=0");
    ss.append(" then null else ");
    ss.append(fd).append("*100.0000/").append(af.getField());
    ss.append(" end ");
    this.field = ss.toString();
  }
  public int getType() {
    return FTYPE_ZB;
  }
  public String toString(){
    return field+ (tag==null||tag.length()==0?"":"as "+tag);
  }
}
