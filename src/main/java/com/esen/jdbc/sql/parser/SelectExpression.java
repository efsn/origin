package com.esen.jdbc.sql.parser;

import java.util.ArrayList;
import java.util.List;

import com.esen.jdbc.sql.parser.token.SqlSyntaxError;
import com.esen.jdbc.sql.parser.token.SqlTokenItem;
import com.esen.jdbc.sql.parser.token.SqlTokenUtil;
import com.esen.jdbc.sql.parser.token.SqlTokenizer;
import com.esen.util.StrFunc;

/**
 * <p>Title: BI@Report</p>
 * <p>Description: 网络报表在线分析系统</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 武汉新连线科技有限公司</p>
 * @author daqun
 * @version 5.0
 * 
 * select sum(d2+c2) , (select count(*) from `ireport50`.`irpt_log`) as logcnt  from `ireport50`.`ssxb_b0`
 */

public class SelectExpression implements SQlExpresion {

  protected boolean distinct;

  protected boolean allFields;

  private ArrayList fields;

  private ArrayList getFields() {
    if (fields == null)
      fields = new ArrayList();
    return fields;
  }

  public boolean isAllFields() {
    return allFields;
  }

  public void setAllFields(boolean allFields) {
    this.allFields = allFields;
  }

  public boolean isDistinct() {
    return distinct;
  }

  public void setDistinct(boolean distinct) {
    this.distinct = distinct;
  }

  public boolean isEmpty() {
    return (!allFields) && ((fields == null) || (fields.isEmpty()));
  }

  public FieldExpression add(FieldExpression field) {
    getFields().add(field);
    return field;
  }

  public int count() {
    return fields == null ? 0 : fields.size();
  }

  public FieldExpression get(int i) {
    return (FieldExpression) (fields == null ? null : fields.get(i));
  }

  public FieldExpression find(String name) {
    for (int i = 0; i < count(); i++) {
      FieldExpression f = get(i);
      String fname = null;
      if (f.isSingleField()) {
        fname = f.getSingleField().getItem();
        if (StrFunc.compareText(fname, name))
          return f;
      }
      if (StrFunc.compareText(f.getAlias(), name))
        return f;
    }
    return null;
  }

  public void appendTo(StringBuffer sb) {
    sb.append(SqlTokenizer.TAG_SELECT);
    sb.append(SqlTokenizer.TAG_BLANK);

    if (distinct) {
      sb.append(SqlTokenizer.TAG_DISTINCT);
      sb.append(SqlTokenizer.TAG_BLANK);
    }
    if (isAllFields()) {
      sb.append(SqlTokenizer.TAG_STAR);
      sb.append(SqlTokenizer.TAG_BLANK);

    }
    else {
      //字段列表
      for (int i = 0; i < count(); i++) {
        get(i).appendTo(sb);
        if (i < count() - 1)
          sb.append(SqlTokenizer.TAG_COMAR);
      }
    }
  }

  public String toStirng() {
    StringBuffer sb = new StringBuffer(200);
    appendTo(sb);
    return sb.toString();
  }

  public void getQuerys(List l) {
    if (isAllFields())
      return;
    for (int i = 0; i < this.count(); i++) {
      get(i).getQuerys(l);
    }
  }

  public void addFields(String[] fieldNames) {
    for (int i = 0; i < fieldNames.length; i++) {
      FieldExpression field = new FieldExpression();
      field.addItem(SqlTokenItem.getVarObj(fieldNames[i]));
      this.add(field);
    }
  }

  public void fieldtoFullName(String tableName) {
    for (int i = 0; i < this.count(); i++) {
      get(i).fieldtoFullName(tableName);
    }
  }

}
