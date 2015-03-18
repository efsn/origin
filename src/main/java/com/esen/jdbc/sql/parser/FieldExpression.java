package com.esen.jdbc.sql.parser;

import com.esen.jdbc.sql.parser.token.SqlTokenItem;
import com.esen.jdbc.sql.parser.token.SqlTokenizer;

/**
 * <p>Title: BI@Report</p>
 * <p>Description: 网络报表在线分析系统</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 武汉新连线科技有限公司</p>
 * @author daqun
 * @version 5.0
 */

public class FieldExpression extends SqlCommonExpression {

  private String alias;

  public FieldExpression() {

  }

  public String getAlias() {
    return alias;
  }

  /**
   * @return 是否是简单表达市,xxb.b3  或着xxb.b3 as xxx
   */
  public boolean isSingleField() {
    if (count() == 1) return (get(0) instanceof SqlTokenItem);
    else if (count() == 3){
    //xxb.b3 as xxx
      return (this.getAlias()!=null);
    } else return false;
    
  }

  /**获得字段的简单表达市
   * @return
   */
  public SqlTokenItem getSingleField() {
    return (SqlTokenItem) (isSingleField() ? get(0) : null);
  }
  
  /**除去别名的表达市
   * @return
   */
  public String getFieldExpession() {
    StringBuffer sb = new StringBuffer(200);
    super.appendTo(sb);
    return sb.toString();
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public void appendTo(StringBuffer sb) {
    super.appendTo(sb);
    if (this.alias != null) {
      sb.append(SqlTokenizer.TAG_BLANK);
      if (alias != null) {
        sb.append(SqlTokenizer.TAG_AS);
        sb.append(SqlTokenizer.TAG_BLANK);        
        sb.append(alias);
      }
    }
  }

  public String toString() {
    StringBuffer sb = new StringBuffer(200);
    appendTo(sb);
    return sb.toString();
  }

}
