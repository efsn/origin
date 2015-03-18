package com.esen.jdbc.dialect.impl.db2;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.util.StrFunc;

public class DB29Dialect extends DB2Dialect {

  public DB29Dialect(Object f) {
    super(f);
  }
  public DbDefiner createDbDefiner() {
    return new DB29Def(this);
  }
  
  /**
   * 20090820 BI-2397
   * Db2的字符串处理函数： substr(field,1,4) 截取的长度都是按字节计算的；
   * 以前的代码加了VARGRAPHIC(field)函数是为了将其转换成unicode字符在截取，实现按字符长度截取；
   * 但是这样在条件中，比如：substr(VARGRAPHIC(bbq),1,4)='2009' 执行sql报错：
   * DB2 SQL error: SQLCODE: -401, SQLSTATE: 42818, SQLERRMC: =
   * 解决办法：现在还原，不使用VARGRAPHIC函数；同下面的len函数
   */
  public String funcMid(String field, String iFrom, String len) {
    if(field==null||field.trim().length()==0)
      return "cast(null as char)";
    if(iFrom==null||iFrom.trim().length()==0)
      return "cast(null as char)";
    StringBuffer sBuf = new StringBuffer(50).append(" SUBSTR(").append(field)
        .append(" , ");
    if(StrFunc.isNumber(iFrom)){
      int start = Integer.parseInt(iFrom);
      sBuf.append(start+1);
    }else
      sBuf.append("(").append(iFrom).append(")+ 1"); //本系统从0开始
    if(len==null||len.trim().length()==0){
      sBuf.append(')');
    }else{
      sBuf.append(',').append(len).append(')');
    }
    return sBuf.toString();
  }
  public String funcLen(String field) {
    if(field==null||field.trim().length()==0)
      return "cast(null as int)";
    StringBuffer sBuf = new StringBuffer(50).append(" LENGTH(").append(field)
        .append(")");
    return sBuf.toString();
  }
  
  /**
   * 20100421
   * Db2 9 支持trim函数了
   */
  public String funcTrim(String field) {
    if(field==null)
      return "cast(null as char)";
    return "trim("+field+")";
  }
  
}
