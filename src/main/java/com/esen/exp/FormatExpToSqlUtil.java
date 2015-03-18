package com.esen.exp;

import com.esen.jdbc.FormatExpToSqlExp;
import com.esen.jdbc.dialect.Dialect;
import com.esen.util.exp.ExpressionNode;
import com.esen.util.exp.ExpVar;

/**
 * <pre>
 * 20091229
 * 用于将Ibatis配置文件中的关于sql表达式的部分翻译成适用指定数据库的表达式；
 * 输入的表达式是BI系统的表达式；
 * 例：
 *    left(xxb.field,2)
 * Oracle --> substr(xxb.field,1,2)
 * Mysql  --> left(xxb.field,2)
 * 
 * 
 * </pre>
 * @author dw
 *
 */
public class FormatExpToSqlUtil extends FormatExpToSqlExp{

  private boolean removeQuotesForConst;
  
  public FormatExpToSqlUtil(Dialect dialect) {
    super(dialect);
  }
  
  /**
   * 对于字符串常量，是否去掉外围的引号；
   * 例：func1("select field from tbname ") --> func1(select field from tbname)
   * @param removeQuotesForConst
   */
  public void setRemoveQuotesForConst(boolean removeQuotesForConst){
    this.removeQuotesForConst = removeQuotesForConst;
  }

  public String formatZz(ExpVar var) {
    if(removeQuotesForConst && var.isConstExp()){
      return var.toStr(null);
    }
    
    return super.formatZz(var);
  }
  
  public String formatNode(ExpressionNode pnode, ExpressionNode nd) {
    if(removeQuotesForConst && (nd.isConst())){
      return nd.evaluateString(null);
    }
    return super.formatNode(pnode, nd);
  }
  
  /**
   * 对于型如：
   * len('#parentDir#') 不能当常量计算=11
   * 需要翻译成sql ： length(#parentDir#)
   */
  public String formatConstExp(ExpressionNode nd){
    if(nd.isConstExp()){
      return null; 
    }
    return super.formatConstExp(nd);
  }
}
