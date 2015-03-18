package com.esen.jdbc.sql.parser.token;

import com.esen.util.StrFunc;

/**
 * <p>
 * Title: BI@Report
 * </p>
 * <p>
 * Description: 网络报表在线分析系统
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company: 武汉新连线科技有限公司
 * </p>
 * 
 * @author daqun
 * @version 5.0
 */

public class SqlTokenItem {
  protected String item;

  private boolean isConst;// 是否是常量表达式

  private boolean isOp;// 是否是操作符号,>,<,=,

  private boolean isVar;// 是否是变量,字段,函数等

  private boolean isKey;// 是否是关键字,如select, group by,等

  private SqlTokenItem(String item, boolean isConst, boolean isOp,
      boolean isVar, boolean isKey) {
    this.item = item;
    this.isConst = isConst;
    this.isOp = isOp;
    this.isVar = isVar;
    this.isKey = isKey;
  }

  public static final SqlTokenItem getConstObj(String item) {
    return new SqlTokenItem(item, true, false, false, false);
  }

  public static final SqlTokenItem getOpObj(String item) {
    return new SqlTokenItem(item, false, true, false, false);
  }

  public static final SqlTokenItem getVarObj(String item) {
    return new SqlTokenItem(item, false, false, true, false);
  }

  public static final SqlTokenItem getKeyObj(String keyWord) {
    return new SqlTokenItem(keyWord.toUpperCase(), false, false, false, true);
  }

  public String getItem() {
    return item;
  }

  public boolean isConst() {
    return isConst;
  }

  public boolean isKey() {
    return isKey;
  }

  public boolean isItem(String item) {
    return StrFunc.compareText(item, this.item);
  }

  public boolean isOp() {
    return isOp;
  }

  public boolean isVar() {
    return isVar;
  }

  public String toString() {
    return this.getItem();
  }
  
  public SqlTokenItem addTable(String table) {
    return (isVar&&(item.indexOf('.')<0))?getVarObj(table+'.'+item):this;
  }

}
