package com.esen.jdbc.sql.parser;

import com.esen.jdbc.sql.parser.token.SqlTokenItem;

/**
 * <p>Title: BI@Report</p>
 * <p>Description: 网络报表在线分析系统</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 武汉新连线科技有限公司</p>
 * @author daqun
 * @version 5.0
 */

public class UnkownNextTokenExp extends UnKownTokenItem {

  private SqlCommonExpression exp;

  public UnkownNextTokenExp(SqlTokenItem token, SqlCommonExpression exp) {
    super(token);
    this.exp = exp;
  }

  public SqlCommonExpression getExp() {
    return exp;
  }

}
