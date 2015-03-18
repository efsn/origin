package com.esen.jdbc.data.impl;


import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.impl.TableMetaDataHelper;
/**
 * 根据给定的主键，字段，索引生成表结构对象；
 */
public class TableMetaDataStm extends TableMetaDataHelper {

  public TableMetaDataStm(String[] keys,TableColumnMetaData[] meta,TableIndexMetaData[] indexes){
    super(null,keys,meta,indexes);
  }
  

}
