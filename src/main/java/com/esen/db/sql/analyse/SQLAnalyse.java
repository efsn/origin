package com.esen.db.sql.analyse;

import java.util.ArrayList;
import java.util.List;

/**
 * 简单的分析sql
 * 获取sql中的排序字段信息；
 */
public class SQLAnalyse {
  private String sql;
  private StringBuffer noOrderSql;
  private OrderByInfo[] orders;
  private OrderByInfo lastorder;//在末尾order by
  public SQLAnalyse(String sql){
    this.sql = sql;
    analyse();
  }
  public String getSourceSQL(){
    return sql;
  }
  public String getNoOrderBySQl(){
    return noOrderSql.toString().trim();
  }
  /**
   * 返回sql中的所有order by 字段的集合
   * @return
   */
  public OrderByInfo[] getOrderBys(){
    return orders;
  }
  public OrderByInfo getLastOrderBy(){
    return lastorder;
  }
  private void analyse() {
    List orderlist = new ArrayList();
    noOrderSql = new StringBuffer(sql.length());
    StringBuffer temp = new StringBuffer();
    for(int i=0;i<sql.length();i++){
      char c = sql.charAt(i);
      if(isNotViewChar(c)){
        temp.append(c);
      }else{
        if(temp.length()==5&&Character.toLowerCase(temp.charAt(1))=='r'&&temp.toString().equalsIgnoreCase("order")){
          int pos = haveBy(i);
          if(pos>0){//后面有 " by "
            String orderstr = getOrderByStr(pos+1);
            OrderByInfo obi = new OrderByInfo(orderstr);
            if((pos+1+orderstr.length())==sql.length())
              lastorder = obi;
            orderlist.add(obi);
            noOrderSql.delete(noOrderSql.length()-temp.length(), noOrderSql.length());
            int len = pos-i+orderstr.length();
            while(len>0){
              i++;
              len--;
            }
          }
        }else
        /**
         * 20091104
         * 解析sql中的 rank() over(... order by ...) 
         * 里面的order by 不能去掉；
         */
        if(temp.length()==4&&Character.toLowerCase(temp.charAt(1))=='v'&&temp.toString().equalsIgnoreCase("over")){
          if(isOver(i)){
            int overlen = getOverLength(i);
            noOrderSql.append(sql.substring(i,i+overlen+1));
            i+=overlen;
            continue;
          }
        }
        temp.setLength(0);
      }
      noOrderSql.append(c);
    }
    orders = new OrderByInfo[orderlist.size()];
    orderlist.toArray(orders);
  }
  
  
  private int getOverLength(int k) {
    int t=0;
    for(int i=k;i<sql.length();i++){
      char c = sql.charAt(i);
      if(c=='('){
        t++;
      }
      if(c==')'){
        t--;//括号配对
        if(t==0){
          return i-k;
        }
      }
      
    }
    return 0;
  }
  
  /**
   * 判断是不是rank() over(PARTITION BY ...  ORDER BY ...) 语法
   * 如果是, 里面的 order by 不能去掉；
   * @param i
   * @return
   */
  private boolean isOver(int k) {
    for(int i=k;i<sql.length();i++){
      char c = sql.charAt(i);
      if(c==' '){
        continue;
      }else if(c=='('){
        return true;
      }else{
        return false;
      }
    }
    return false;
  }
  
  private boolean isNotViewChar(char c){
    return c!=' '&&c!='\t' && c!='\r' && c!='\n'&& c!='('&&c!=')';
  }
  /**
   * select sum(case when nvl(a.BALANORIENT,0)=1 then nvl(a.LOCALDEBITAMOUNT,0) else nvl(a.LOCALCREDITAMOUNT,0) end) as C3, SUBSTR(a.SUBJCODE,1,6) as A3 
from FACT_BALANCE a 
where (( SUBSTR(a.PERIOD,1,6)='200803') AND ( SUBSTR(a.SUBJCODE,1,4)='5502')) 
group by SUBSTR(a.SUBJCODE,1,6) 
order by SUBSTR(a.SUBJCODE,1,6) nulls first 

order by 后面的字符串有 () 配对，需要处理
   * @param k
   * @return
   */
  private String getOrderByStr(int k) {
    StringBuffer orderstr = new StringBuffer(64);
    int t = 0;
    for(int i=k;i<sql.length();i++){
      char c = sql.charAt(i);
      if(c=='('){
        t++;
      }
      if(c==')'){
        t--;//括号配对
      }
      if(t>=0){
        orderstr.append(c);
      }else{
        break;
      }
    }
    return orderstr.toString();
  }
  private int haveBy(int k) {
    StringBuffer temp = new StringBuffer(32);
    for(int i=k;i<sql.length();i++){
      char c = sql.charAt(i);
      if(isNotViewChar(c)){
        temp.append(c);
        if(temp.toString().equalsIgnoreCase("by")&&sql.charAt(i+1)==' ')
          return i;
      }
    }
    return -1;
  }

}
