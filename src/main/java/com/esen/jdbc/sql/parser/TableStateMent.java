package com.esen.jdbc.sql.parser;

import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.jdbc.sql.parser.token.SqlSyntaxError;
import com.esen.jdbc.sql.parser.token.SqlTokenItem;
import com.esen.jdbc.sql.parser.token.SqlTokenizer;
import com.esen.util.i18n.I18N;

/**
 * <p>Title: BI@Report</p>
 * <p>Description: 网络报表在线分析系统</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 武汉新连线科技有限公司</p>
 * @author daqun
 * @version 5.0
 */

public class TableStateMent {
  private String tableName;

  private String alias;

  private boolean isQuery;

  private SelectStateMent query;

  public TableStateMent(String tableName) {
    this.tableName = tableName;
  }

  public TableStateMent() {

  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public TableStateMent(SqlTokenItem table) throws SqlSyntaxError {
    if (!table.isVar())
//      throw new SqlSyntaxError("报表表达式不合法!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.tablestatement.illegalbbexp", "报表表达式不合法!"));
    this.tableName = table.getItem();
  }

  public void setQuery(SelectStateMent query) {
    query.quoted = true;
    this.isQuery = true;
    this.query = query;
  }

  public TableStateMent(SelectStateMent query) {
    this.setQuery(query);
  }

  public boolean isQuery() {
    return isQuery;
  }

  public SelectStateMent getQuery() {
    return query;
  }

  public boolean validate() throws SqlSyntaxError {
    if (isQuery) {
      if (getQuery() == null)
//        throw new SqlSyntaxError("表定义不合法,找不到Query!");
    	  throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.tablestatement.noquery4table", "表定义不合法,找不到Query!"));
      //if (alias==null) throw new SqlSyntaxError("子查询找不到别名!"); 
    } else {
      if (getTableName()==null)
//        throw new SqlSyntaxError("表定义不合法,找不到报表名!");
    	  throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.tablestatement.nobbname", "表定义不合法,找不到报表名!"));
    }
    return true;
  }

  public String getTableName() {
    return tableName;
  }
  
  public String getName() {
    return getTableName();
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public void setAlias(SqlTokenItem alias) throws SqlSyntaxError {
    if (!alias.isVar())
//      throw new SqlSyntaxError("报表表达式不合法!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.tablestatement.illbbexp", "报表表达式不合法!"));
    this.alias = alias.getItem();
  }

  public void appendTo(StringBuffer sb) {
    if (this.isQuery)
      getQuery().appendTo(sb);
    else {
      sb.append(tableName);
    }
    if (alias != null) {
      sb.append(SqlTokenizer.TAG_BLANK);
      sb.append(alias);
    }
  }
  
  public String toString(){
    StringBuffer sb=new StringBuffer(100);
    this.appendTo(sb);
    return sb.toString();
  }

}
