package com.esen.jdbc.dialect.impl;

/**
 * 兼容旧的类；
 */
public class TableColumnMetaDataImpl extends TableColumnMetaDataHelper {

  public TableColumnMetaDataImpl(String name) {
    super(name);
  }

  public TableColumnMetaDataImpl(String name, String lable, int type,
      int length, int scale,String defaultValue,String desc) {
    super(name,lable,type,length,scale,true,false,defaultValue,desc);
  }
  public TableColumnMetaDataImpl(String name, String label, int type,
      int length, int scale) {
    this(name,label,type,length,scale,null,null);
  }


}
