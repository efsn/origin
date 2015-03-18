package com.esen.jdbc.sql.parser.token;

import java.util.ArrayList;

/**
 * <p>Title: BI@Report</p>
 * <p>Description: 网络报表在线分析系统</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 武汉新连线科技有限公司</p>
 * @author daqun
 * @version 5.0
 */

public class SqlTokens {
  private ArrayList l;

  public boolean add(Object arg0) {
    return l.add(arg0);
  }

  public SqlTokenItem get(int index) {
    return (SqlTokenItem) l.get(index);
  }

  public SqlTokenItem getLast() {
    return (SqlTokenItem) (isEmpty() ? null : l.get(l.size() - 1));
  }

  public boolean isEmpty() {
    return l.isEmpty();
  }

  public int size() {
    return l.size();
  }

}
