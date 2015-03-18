package com.esen.jdbc.sql.parser;

import java.util.List;

import com.esen.jdbc.sql.parser.token.SqlTokenizer;

/**
 * <p>Title: BI@Report</p>
 * <p>Description: 网络报表在线分析系统</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 武汉新连线科技有限公司</p>
 * @author daqun
 * @version 5.0
 */

public class UnionExpression implements SQlExpresion {
  private boolean all = false;

  private SelectStateMent select;

  public boolean isAll() {
    return all;
  }

  public void setAll(boolean all) {
    this.all = all;
  }

  public SelectStateMent getSelect() {
    return select;
  }

  public void setSelect(SelectStateMent select) {
    this.select = select;
  }

  public void appendTo(StringBuffer sb) {
    sb.append(SqlTokenizer.TAG_UNION);
    sb.append(SqlTokenizer.TAG_CRLF);
    if (isAll()) {
      sb.append(SqlTokenizer.TAG_ALL);
      sb.append(SqlTokenizer.TAG_BLANK);
    }
    select.appendTo(sb);
  }

  public void getQuerys(List l) {
    l.add(select);
  }

}
