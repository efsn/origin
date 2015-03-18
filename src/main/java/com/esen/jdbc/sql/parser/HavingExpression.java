package com.esen.jdbc.sql.parser;

import com.esen.jdbc.sql.parser.token.SqlSyntaxError;
import com.esen.jdbc.sql.parser.token.SqlTokenItem;
import com.esen.jdbc.sql.parser.token.SqlTokenUtil;
import com.esen.jdbc.sql.parser.token.SqlTokenizer;

/**
 * <p>Title: BI@Report</p>
 * <p>Description: 网络报表在线分析系统</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 武汉新连线科技有限公司</p>
 * @author daqun
 * @version 5.0
 */

public class HavingExpression extends SqlConditionExpression implements  SQlExpresion{

  public void appendTo(StringBuffer sb) {
    sb.append(SqlTokenizer.TAG_HAVING);
    sb.append(SqlTokenizer.TAG_BLANK);
    super.appendTo(sb);
  }

}
