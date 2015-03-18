package com.esen.jdbc.sql.parser;

import java.util.ArrayList;
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

public class InExpression implements  SQlExpresion{
  private ArrayList items;

  protected ArrayList getItems() {
    if (items == null)
      items = new ArrayList();
    return items;
  }

  public int count() {
    return items == null ? 0 : items.size();
  }

  public void add(SqlCommonExpression exp) {
    this.getItems().add(exp);
  }

  public boolean isEmpty() {
    return count() == 0;
  }

  public void appendTo(StringBuffer sb) {
    sb.append(SqlTokenizer.TAG_IN);
    sb.append(SqlTokenizer.TAG_BLANK);
    sb.append(SqlTokenizer.TAG_LEFTQ);
    for (int i = 0; i < this.count(); i++) {
      get(i).appendTo(sb);
      if (i < count() - 1)
        sb.append(SqlTokenizer.TAG_COMAR);
    }
    sb.append(SqlTokenizer.TAG_RIGHTQ);
  }

  public SqlCommonExpression get(int i) {
    return (SqlCommonExpression) (isEmpty() ? null : items.get(i));
  }

  public void getQuerys(List l) {
    //DO nothing
    
  }

}
