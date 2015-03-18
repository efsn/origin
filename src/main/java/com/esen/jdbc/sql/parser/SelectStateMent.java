package com.esen.jdbc.sql.parser;

import java.lang.reflect.Field;
import java.util.*;

import com.esen.jdbc.sql.parser.token.SqlTokenizer;

/**
 * <p>Title: BI@Report</p>
 * <p>Description: 网络报表在线分析系统</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 武汉新连线科技有限公司</p>
 * @author daqun
 * @version 5.0
 */

public class SelectStateMent implements SQlExpresion {
  protected boolean quoted;

  protected SelectExpression select;

  protected FromExpression from;

  protected JoinExpression join;

  protected WhereExpression where;

  protected OrderByExpression orderBy;

  protected GroupByExpression groupBy;

  protected HavingExpression having;

  protected ArrayList unions;//select * from b1 UNION (select id from b)

  /*
   * select bbhid from (select bbhid, sum(b1->a1),sum(b1->a2) from b1 where bbq_ like '2003%' and sum(b1->a1)>0 and sum(b1->a2)>0)

   INTERSECT

   select bbhid from b1 where bbq_ = '200401--' and b1->a1>1000 and b1->a2>1000
   */
  protected SelectStateMent nextInterSelect;

  protected SelectStateMent nextMinus;

  public SelectStateMent getNextInterSelect() {
    return nextInterSelect;
  }

  public void addUnion(UnionExpression union) {
    if (unions == null)
      unions = new ArrayList();
    unions.add(union);
  }

  public int getUnionCount() {
    return (unions == null) ? 0 : unions.size();
  }

  public UnionExpression getUnion(int i) {
    return (UnionExpression) ((unions == null) ? null : unions.get(i));
  }

  public void setNextInterSelect(SelectStateMent nextInterSelect) {
    this.nextInterSelect = nextInterSelect;
  }

  public boolean isQuoted() {
    return quoted;
  }

  public void setQuoted(boolean quoted) {
    this.quoted = quoted;
  }

  public FromExpression getFrom() {
    return from;
  }

  public void setFrom(FromExpression from) {
    this.from = from;
  }

  public GroupByExpression getGroupBy() {
    return groupBy;
  }

  public void setGroupBy(GroupByExpression groupBy) {
    this.groupBy = groupBy;
  }

  public HavingExpression getHaving() {
    return having;
  }

  public void setHaving(HavingExpression having) {
    this.having = having;
  }

  public OrderByExpression getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(OrderByExpression orderBy) {
    this.orderBy = orderBy;
  }

  public SelectExpression getSelect() {
    return select;
  }

  public void setSelect(SelectExpression select) {
    this.select = select;
  }

  public WhereExpression getWhere() {
    return where;
  }

  public void setWhere(WhereExpression where) {
    this.where = where;
  }

  public String toString() {
    return getSql();
  }

  public void appendTo(StringBuffer sb) {
    if (quoted)
      sb.append(SqlTokenizer.TAG_LEFTQ);
    if (select != null) {
      select.appendTo(sb);
      sb.append(SqlTokenizer.TAG_CRLF);
    }

    if (from != null) {
      from.appendTo(sb);
      sb.append(SqlTokenizer.TAG_CRLF);
    }

    if (join != null) {
      join.appendTo(sb);
      sb.append(SqlTokenizer.TAG_CRLF);
    }

    if (where != null) {
      where.appendTo(sb);
      sb.append(SqlTokenizer.TAG_CRLF);
    }

    if (groupBy != null) {
      groupBy.appendTo(sb);
      sb.append(SqlTokenizer.TAG_CRLF);
    }

    if (orderBy != null) {
      orderBy.appendTo(sb);
      sb.append(SqlTokenizer.TAG_CRLF);
    }

    if (having != null) {
      having.appendTo(sb);
      sb.append(SqlTokenizer.TAG_CRLF);
    }

    for (int i = 0; i < getUnionCount(); i++) {
      getUnion(i).appendTo(sb);
      sb.append(SqlTokenizer.TAG_CRLF);
    }
    if (nextInterSelect != null) {
      sb.append(SqlTokenizer.TAG_INTERSECT);
      nextInterSelect.appendTo(sb);
      sb.append(SqlTokenizer.TAG_CRLF);
    }
    if (quoted)
      sb.append(SqlTokenizer.TAG_RIGHTQ);
  }

  public String getSql() {
    StringBuffer sb = new StringBuffer(200);
    appendTo(sb);
    return sb.toString();
  }

  public JoinExpression getJoin() {
    return join;
  }

  public void setJoin(JoinExpression join) {
    this.join = join;
  }

  public SelectStateMent getNextMinus() {
    return nextMinus;
  }

  public void setNextMinus(SelectStateMent nextMinus) {
    this.nextMinus = nextMinus;
  }

  private static final void collectSubQuerys(SQlExpresion exp, ArrayList qs) {
    if (exp instanceof SelectStateMent)
      qs.add(exp);
    Class cls = exp.getClass();
    Field fs[] = cls.getDeclaredFields();
    for (int i = 0; i < fs.length; i++) {
      try {
        Object obj = fs[i].get(exp);
        if (obj instanceof SQlExpresion)
          collectSubQuerys((SQlExpresion) obj, qs);
        else if (obj instanceof Set) {
          Iterator it = ((Set) obj).iterator();
          while (it.hasNext()) {
            Object sub = it.next();
            if ((sub != null) && (sub instanceof SQlExpresion)) {
              collectSubQuerys((SQlExpresion) sub, qs);
            }
          }
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }

    }
  }

  public SelectStateMent[] getQuerys() {
    ArrayList l = new ArrayList();
    this.collectSubQuerys(this, l);
    SelectStateMent qs[] = new SelectStateMent[l.size()];
    l.toArray(qs);
    return qs;
  }

  public void getQuerys(List l) {
    l.add(this);
    if (select != null)
      select.getQuerys(l);
    if (from != null)
      from.getQuerys(l);
    if (where != null)
      where.getQuerys(l);
    if (orderBy != null)
      orderBy.getQuerys(l);
    if (this.groupBy != null)
      groupBy.getQuerys(l);
    if (this.having != null)
      this.having.getQuerys(l);
    if (this.unions != null) {
      for (int i = 0; i < this.getUnionCount(); i++) {
        this.getUnion(i).getQuerys(l);
      }
    }
    if (this.nextInterSelect != null)
      nextInterSelect.getQuerys(l);
    if (this.nextMinus != null)
      nextMinus.getQuerys(l);
  }

  /**获得引用的全部select 子句
   * @return
   */
  public SelectStateMent[] getAllQuerys() {
    ArrayList l = new ArrayList();
    getQuerys(l);
    SelectStateMent qs[] = new SelectStateMent[l.size()];
    l.toArray(qs);
    return qs;
  }

  public void fieldtoFullName(String tableName) {
    if (this.select != null)
      select.fieldtoFullName(tableName);
    if (where != null)
      where.fieldtoFullName(tableName);
    if (this.having != null)
      having.fieldtoFullName(tableName);
  }

}
