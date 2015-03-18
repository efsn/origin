package com.esen.jdbc.dialect.impl.gbase;

import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;


/**
 * 实现南通数据库的DbMetaData接口；
 * 暂时只支持用户的默认模式：PUBLIC
 * @author dw
 *
 */
public class GBaseTableMetaData extends TableMetaDataImpl {


  public GBaseTableMetaData(DbMetaDataImpl owner, String tablename) {
    super(owner, tablename);
  }
  
  /**
   * GBASE 上不允许获取默认值，获取默认值会导致创建维表时修复数据表结构
   */
	protected String getDefaultValue(String defvalue) {
	  return null;
  }
}
