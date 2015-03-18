package com.esen.db.sql;

public abstract class BaseTable implements Cloneable {
  private StringBuffer joinOnCond;
  private StringBuffer joinWhereCond;
  private int jointype = INNER_JOIN;
  public static final int INNER_JOIN = 0;
  public static final int LEFT_JOIN = 1;
  public static final int RIGHT_JOIN = 2;
  public static final int FULL_JOIN = 3;
  //cross join 交叉连接，用户没有连接条件，返回两个表的笛卡尔乘积；
  public static final int CROSS_JOIN = 9;
  public BaseTable() { 
  }
  /**
   * 数据表，视图表别名
   * @return
   */
  public abstract String getAlias();
  /**
   * @deprecated
   * @return
   */
  public abstract String getTag();
  public void setJoinType(int jointype){
    this.jointype = jointype;
  }
  /**
   * 关联类型
   * true : left join
   * false: inner join
   * @return
   */
  public int getJoinType(){
    return jointype;
  }
  
  /**
   * 设置关联条件；
   * 可能是多个and子句；
   * @param cond
   * @return
   */
  public void addJoinOnCondition(String cond){
    if(cond==null||cond.length()==0)
      return;
    if(joinOnCond==null){
      joinOnCond = new StringBuffer(256);
    }
    if(joinOnCond.length()>0)
      joinOnCond.append(" and ");
    joinOnCond.append("(").append(cond).append(")");
  }
  public String getJoinOnCondition(){
    if(joinOnCond==null) {
      return null;
    }
    return joinOnCond.toString();
  }
  public void addJoinWhereCondition(String cond){
    if(cond==null||cond.length()==0)
      return;
    if(joinWhereCond==null){
      joinWhereCond = new StringBuffer(256);
    }
    if(joinWhereCond.length()>0)
      joinWhereCond.append(" and ");
    joinWhereCond.append("(").append(cond).append(")");
  }
  public String getJoinWhereCondition(){
    if(joinWhereCond==null) {
      return null;
    }
    return joinWhereCond.toString();
  }
  public Object clone(){
    BaseTable bt = null;
    try {
      bt = (BaseTable)super.clone();
    }
    catch (CloneNotSupportedException e) {
      return null;
    }
    bt.jointype = jointype;
    if(joinOnCond!=null){
      bt.joinOnCond = new StringBuffer(joinOnCond.length());
      bt.joinOnCond.append(joinOnCond.toString());
    }
    if(joinWhereCond!=null){
      bt.joinWhereCond = new StringBuffer(joinWhereCond.toString());
      bt.joinWhereCond.append(joinWhereCond.toString());
    }
    return bt;
  }
}
