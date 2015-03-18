package com.esen.jdbc.sql.parser;

import java.util.ArrayList;
import java.util.List;

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

public class SqlFunc implements SQlExpresion {
  private String name;

  private ArrayList params;

  protected ArrayList getParams() {
    if (params == null)
      params = new ArrayList();
    return params;
  }

  public SqlFunc(String name) {
    this.name = name;
  }

  public void addParam(SqlCommonExpression param) {
    getParams().add(param);
  }

  public int count() {
    return params == null ? 0 : params.size();
  }

  public SqlCommonExpression get(int i) {
    return (SqlCommonExpression) getParams().get(i);
  }

  public void appendTo(StringBuffer sb) {
    sb.append(name);
    sb.append(SqlTokenizer.TAG_LEFTQ);
    for (int i = 0; i < count(); i++) {
      get(i).appendTo(sb);
      if (i < count() - 1)
        sb.append(SqlTokenizer.TAG_COMAR);
    }
    sb.append(SqlTokenizer.TAG_RIGHTQ);
  }

  public void getQuerys(List l) {
    for (int i = 0; i < this.count(); i++) {
      Object obj = get(i);
      if (obj instanceof SQlExpresion) {
        ((SQlExpresion) obj).getQuerys(l);
      }
    }

  }

  public void fieldtoFullName(String tableName) {
    for (int i = 0; i < this.count(); i++) {
      get(i).fieldtoFullName(tableName);
    }
  }

}
