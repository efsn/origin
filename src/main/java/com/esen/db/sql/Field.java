package com.esen.db.sql;

public class Field {
  protected String field;
  protected String tag;
  protected int type;
  public static final int FTYPE_ZB = 1;//ZB
  public static final int FTYPE_DIM = 2;//OLAP
  public static final int FTYPE_SELECTFIELD= 3;//把一个子查询作为字段 SelectField
  public static final int FTYPE_ZBCHARACTOR = 4;//文字指标；
  public static final int FTYPE_OTHER = 5;
  public Field(){
  }
  public Field(String field){
    this(field,null);
  }
  public Field(String field ,String tag) {
    this(field,tag,FTYPE_ZB);
  }
  public Field(String field ,String tag,int type) {
    this.field = field;
    this.tag = formatAlies(tag);
    this.type = type;
  }

  /**
   * 20091013 BI-2594 
   * 字段别名是根据指标名来取的，但是有些指标名太长，超过了数据库的允许范围；
   * 比如Oracle的别名不能超过31， 否则报ORA-00972：identifier is too long的异常；
   * 这里对太长的字段别名做处理，生成一个较短的别名，供使用；
   * 测试用例：
   * 开发测试专用/开发测试/多维分析/OLAP07298 测试长字段别名
   * 
   * 20091125 BI-2670
   * 固定维分组，自动分析指标和维，其名字可能超过别名长度限制；
   * 
   * 新的测试用例：
   * 开发测试专用/开发测试/报表模板/别名/B45677 测试超长别名 
   * 
   **/
  private String formatAlies(String alies){
    if(alies==null||alies.length()==0){
      return alies;
    }
    StringBuffer fn = new StringBuffer(20);
    /**
     * 20091125 BUG
     * 由于此方法的存在，造成在创建Field类是，给定的别名，可能不是创建后Field类的别名；
     * 对解析嵌套算子时，造成影响，嵌套算子会生成随机11位的别名:INNERZBxxxx ， xxxx是随机数；
     * 造成生成sql别名早不到的错误；
     * 
     * 解决办法：原来的程序大于10位就进行了限制，现在改为超过20位再限制；
     */
    if(alies.length()>20){
      /**
       * 20091016 BI-2602
       * 兼容jdk1.4
       * 原来的代码alies.subSequence(0, 10) 返回 CharSequence
       * fn.append(CharSequence c) 是jdk1.5的方法；
       * 这里应该使用alies.substring(0, 10)；
       */
      fn.append(alies.substring(0, 15));
      fn.append(Math.round(Math.random()*10000));
    }else {
      fn.append(alies);
    }
    return fn.toString();
  }
  
  public String getField(){
    return field;
  }
  public String getAlias(){
    return tag;
  }
  /**
   * @deprecated
   * @return
   */
  public String getTag(){
    return tag;
  }
  public int getType(){
    return type;
  }
  public String toString(){
    if(tag==null||tag.length()==0)
      return field;
    return ((field==null||field.trim().length()==0)?"''":field )+ " as " + tag;
  }
  public static Field createNullField(String tag){
    //用''不用null是因为有些数据库如：db2不支持null
    return new Field("''",tag,FTYPE_OTHER);
  }
}
