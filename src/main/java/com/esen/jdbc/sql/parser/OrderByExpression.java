package com.esen.jdbc.sql.parser;

import java.util.ArrayList;
import java.util.List;

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

public class OrderByExpression implements  SQlExpresion{
  private ArrayList fields;

  private ArrayList getFields() {
    if (fields == null)
      fields = new ArrayList();
    return fields;
  }

  public SortedFieldExpression add(SortedFieldExpression field) {
    getFields().add(field);
    return field;
  }

  public boolean isEmpty() {
    return fields == null ? true : fields.isEmpty();
  }

  public int count() {
    return fields == null ? 0 : fields.size();
  }

  public SortedFieldExpression get(int i) {
    return (SortedFieldExpression) (fields == null ? null : fields.get(i));
  }

  public void appendTo(StringBuffer sb) {
    //字段列表
    sb.append(SqlTokenizer.TAG_ORDERBY);
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
    // do nothing
    
  }

}
