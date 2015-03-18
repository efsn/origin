package com.esen.jdbc.sql.parser;

import com.esen.jdbc.sql.parser.token.SqlTokenizer;

/**
 * <p>Title: BI@Report</p>
 * <p>Description: 网络报表在线分析系统</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 武汉新连线科技有限公司</p>
 * @author daqun
 * @version 5.0
 */

public class SortedFieldExpression extends SqlCommonExpression implements  SQlExpresion{
  private boolean desc;

  public boolean isDesc() {
    return desc;
  }

  public void setDesc(boolean desc) {
    this.desc = desc;
  }

  public void appendTo(StringBuffer sb) {
    super.appendTo(sb);
    if (desc) {
      sb.append(SqlTokenizer.TAG_BLANK);
      sb.append(SqlTokenizer.TAG_DESC);
      sb.append(SqlTokenizer.TAG_BLANK);      
    }
  }
}
