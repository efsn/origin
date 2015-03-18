package com.esen.jdbc.sql.parser;

import java.util.ArrayList;
import java.util.List;

import com.esen.jdbc.sql.parser.token.SqlSyntaxError;
import com.esen.jdbc.sql.parser.token.SqlTokenItem;
import com.esen.jdbc.sql.parser.token.SqlTokenUtil;
import com.esen.jdbc.sql.parser.token.SqlTokenizer;
import com.esen.util.StrFunc;

/**sql 中from 表达式,可能为table列表也可能为query列表
 * <p>Title: BI@Report</p>
 * <p>Description: 网络报表在线分析系统</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 武汉新连线科技有限公司</p>
 * @author daqun
 * @version 5.0
 */

public class FromExpression implements SQlExpresion {
  private ArrayList items;

  private ArrayList getItems() {
    if (items == null)
      items = new ArrayList();
    return items;
  }

  public TableStateMent add(TableStateMent table) {
    this.getItems().add(table);
    return table;
  }
  
  public TableStateMent add(String tableName) {
    TableStateMent table=new TableStateMent(tableName);
    return this.add(table);
  }

  public boolean isEmpty() {
    return (items == null) || (items.isEmpty());
  }

  public int count() {
    return items == null ? 0 : items.size();
  }

  public TableStateMent get(int i) {
    return (TableStateMent) (items == null ? null : items.get(i));
  }
  
  public TableStateMent get(String name) {
    for(int i=0;i<this.count();i++) {
      TableStateMent table=get(i);
      if(StrFunc.compareText(name,table.getName())||StrFunc.compareText(name,table.getAlias())) {
        return table;
      }
    }
    return null;
  }

  public void appendTo(StringBuffer sb) {
    sb.append(SqlTokenizer.TAG_FROM);
    sb.append(SqlTokenizer.TAG_BLANK);
    for (int i = 0; i < count(); i++) {
      get(i).appendTo(sb);
      if (i < count() - 1)
        sb.append(SqlTokenizer.TAG_COMAR);
    }

  }

  public String toString() {
    StringBuffer sb = new StringBuffer(200);
    appendTo(sb);
    return sb.toString();
  }

  public void getQuerys(List l) {
    for (int i = 0; i < count(); i++) {
      TableStateMent table = get(i);
      if (table.isQuery())
        l.add(table.getQuery());
    }
  }

  public int findTable(String tableName) {
    for (int i = 0; i < this.count(); i++) {
      TableStateMent table = get(i);
      if (!table.isQuery()
          && (StrFunc.compareText(table.getTableName(), tableName)))
        return i;
    }
    return -1;
  }

}
