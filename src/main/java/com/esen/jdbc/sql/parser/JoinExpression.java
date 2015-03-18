package com.esen.jdbc.sql.parser;

import java.util.List;

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

public class JoinExpression implements  SQlExpresion{
  protected SqlTokenItem type;//inner left right join

  protected SqlTokenItem table;//left join tablename on xxx=xxxx

  protected SqlCommonExpression exp;

  public SqlCommonExpression getExp() {
    return exp;
  }

  public void setExp(SqlCommonExpression exp) {
    this.exp = exp;
  }

  public SqlTokenItem getTable() {
    return table;
  }

  public void setTable(SqlTokenItem table) throws SqlSyntaxError {
    if (!table.isVar())
//      throw new SqlSyntaxError("Join 表达式表名不合法!");
    	 throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.joinexpression.illegaljoin", "Join 表达式表名不合法!"));
    this.table = table;
  }

  public SqlTokenItem getType() {
    return type;
  }

  public void setType(SqlTokenItem type) {
    this.type = type;
  }

  public JoinExpression(SqlTokenItem type) {
    this.type = type;
  }

  public void appendTo(StringBuffer sb) {
    sb.append(type.toString());
    sb.append(SqlTokenizer.TAG_BLANK);    
    sb.append(SqlTokenizer.TAG_JOIN);
    sb.append(SqlTokenizer.TAG_BLANK);
    sb.append(table.toString());
    sb.append(SqlTokenizer.TAG_BLANK);    
    if (exp != null) {
      sb.append(SqlTokenizer.TAG_ON);
      sb.append(SqlTokenizer.TAG_BLANK);
      exp.appendTo(sb);
    }
  }

  public void getQuerys(List l) {
    //DO nothing
    
  }

}
