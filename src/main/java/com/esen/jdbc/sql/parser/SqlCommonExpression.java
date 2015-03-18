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

public class SqlCommonExpression implements SQlExpresion {

  protected boolean quoted;

  private ArrayList items;

  protected ArrayList getItems() {
    if (items == null)
      items = new ArrayList();
    return items;
  }

  /**单个token的内容,可能为常量,操作符,变量(字段)
   * @param token
   */
  public void addItem(SqlTokenItem token) {
    getItems().add(token);
  }

  /**可以是函数,子查询,括号表达式等
   * @param item
   */
  public void addItem(SQlExpresion item) {
    getItems().add(item);
  }
  
  protected void addQuotedItem() {
    getItems().add(0,SqlTokenItem.getOpObj(SqlTokenizer.TAG_LEFTQ));
    getItems().add(SqlTokenItem.getOpObj(SqlTokenizer.TAG_RIGHTQ));
    this.quoted=false;
  }

  public void remove(int i) {
    if (!isEmpty()) {
      getItems().remove(i);
    }
  }

  public Object pop() {
    if (isEmpty()) {
      return null;
    }
    else {
      return items.remove(items.size() - 1);
    }
  }

  public int count() {
    return (items == null) ? 0 : items.size();
  }

  public Object get(int i) {
    return items == null ? null : items.get(i);
  }

  public boolean isEmpty() {
    return count() == 0;
  }

  public boolean isQuoted() {
    return quoted;
  }

  public void setQuoted(boolean quoted) {
    this.quoted = quoted;
  }

  public void appendTo(StringBuffer sb) {
    if (quoted)
      sb.append(SqlTokenizer.TAG_LEFTQ);
    for (int i = 0; i < this.count(); i++) {
      Object obj = this.get(i);
      if (obj instanceof SQlExpresion) {
        ((SQlExpresion) obj).appendTo(sb);
      }
      else {
        sb.append(obj.toString());
        sb.append(SqlTokenizer.TAG_BLANK);
      }
    }
    if (quoted)
      sb.append(SqlTokenizer.TAG_RIGHTQ);
  }

  public String toString() {
    StringBuffer sb = new StringBuffer(200);
    appendTo(sb);
    return sb.toString();
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
      Object obj = get(i);
      if (obj instanceof SqlTokenItem) {
        SqlTokenItem token = ((SqlTokenItem) obj).addTable(tableName);
        if (token.isVar())
          items.set(i, token);
      }
    }
  }
  
}
