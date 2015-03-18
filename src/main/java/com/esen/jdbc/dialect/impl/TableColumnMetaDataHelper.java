package com.esen.jdbc.dialect.impl;

import com.esen.jdbc.dialect.TableColumnMetaData;

/**
 * 描述一个字段信息的类；
 * 这是字段接口的基类；
 * @author dw
 *
 */
public class TableColumnMetaDataHelper implements TableColumnMetaData {
  protected String fdname;
  protected String lable;
  protected String fddesc;
  /**
   * 字段类型，可以是系统定义的char类型：'C','N',...
   * 也可以是数据库的类型：Types.VARCHAR ...等；
   * 这里根据需要存储，读取时注意转换；
   */
  protected int sqltype;
  protected int len;
  protected int scale;
  protected boolean autoinc;
  int step;//自动增长步长；
  protected boolean nullable;
  protected boolean unique;
  protected String defaultvalue;
  String newfdname;
  
  public TableColumnMetaDataHelper(String name){
    this(name,null,-1,-1,-1,-1,1,-1,null,null,null);
  }
  
  /**
   * 构造一个字段，增加了newfdname属性，是新字段名，用于更字段名；
   * @param fdname
   * @param lable
   * @param sqltype
   * @param len
   * @param scale
   * @param autoinc
   * @param nullable
   * @param unique
   * @param defaultvalue
   * @param fddesc
   * @param newfdname
   */
  public TableColumnMetaDataHelper(String fdname,String lable,  int sqltype, int len, int scale, int autoinc,
      int nullable, int unique, String defaultvalue,String fddesc, String newfdname) {
    this.fdname = fdname;
    this.lable = lable;
    this.sqltype = sqltype;
    this.len = len;
    this.scale = scale;
    this.autoinc = autoinc==1;
    this.nullable = nullable==1;
    this.unique = unique==1;
    this.defaultvalue = defaultvalue;
    this.fddesc = fddesc;
    this.newfdname = newfdname;
    
  }
  /**
   * 构造一个字段
   * @param fdname
   * @param sqltype
   * @param len
   * @param scale
   * @param autoinc
   * @param nullable
   * @param unique
   * @param defaultvalue
   * @param fddesc
   */
  public TableColumnMetaDataHelper(String fdname,  int sqltype, int len, int scale, int autoinc,
      int nullable, int unique, String defaultvalue,String fddesc) {
    this(fdname,fdname,sqltype,len,scale,autoinc,nullable,unique,defaultvalue,fddesc,null);
  }
  
  public TableColumnMetaDataHelper(String fdname,  int sqltype, int len, int scale,
      boolean nullable, boolean unique, String defaultvalue,String fddesc) {
    this(fdname,fdname,sqltype,len,scale,0,nullable?1:0,unique?1:0,defaultvalue,fddesc,null);
  }
  public TableColumnMetaDataHelper(String fdname,String lable,  int sqltype, int len, int scale,
      boolean nullable, boolean unique, String defaultvalue,String fddesc) {
    this(fdname,lable,sqltype,len,scale,0,nullable?1:0,unique?1:0,defaultvalue,fddesc,null);
  }
  /**
   * 构造自动增长字段；
   * @param fdname
   * @param step
   */
  public TableColumnMetaDataHelper(String fdname, int step){
	  this(fdname, step, null);
  }
  
  /**
   * 构造自动增长字段；
   * @param fdname
   * @param step
   * @param desc
   */
  public TableColumnMetaDataHelper(String fdname, int step, String desc){
    this(fdname,'I',30,0,1,0,0,null, desc);
    this.step = step;
  }
  
  public int getStep(){
    return step;
  }

  public String getDefaultValue() {
    return defaultvalue;
  }

  public String getDesc() {
    return fddesc;
  }

  public String getLabel() {
    return lable;
  }

  public int getLen() {
    return len;
  }
  
  /**
   * 调整字段的长度；
   * 在根据数据库主键列组合长度限制，自动调整时用到；
   * @param len
   */
  public void setLen(int len){
    this.len = len;
  }

  public String getName() {
    return fdname;
  }

  public int getScale() {
    return scale;
  }

  public int getType() {
    return sqltype;
  }

  public boolean isAutoInc() {
    return autoinc;
  }

  public boolean isNullable() {
    return nullable;
  }
  
  public void setNullable(boolean nullable){
    this.nullable = nullable;
  }

  public boolean isUnique() {
    return unique;
  }
  /**
   * 返回新的字段名，用于更改字段名；
   * @return
   */
  public String getNewColumnName(){
    return newfdname;
  }
  
  public void setName(String name) {
    this.fdname = name;
  }

  public void setLable(String lable) {
    this.lable = lable;
  }

  public void setLength(int length) {
    this.len = length;
  }

  public void setScale(int scale) {
    this.scale = scale;
  }

  public void setType(int type) {
    this.sqltype = type;
  }

  public void setAutoInc(boolean autoinc) {
    this.autoinc = autoinc;
  }

  public void setUnique(boolean isunique) {
    this.unique = isunique;
  }

  public void setDesc(String desc) {
    this.fddesc = desc;
  }
  
  public void setDefaultValue(String v){
    this.defaultvalue = v;
  }
}
