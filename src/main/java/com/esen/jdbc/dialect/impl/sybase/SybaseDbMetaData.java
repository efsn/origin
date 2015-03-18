package com.esen.jdbc.dialect.impl.sybase;

import java.sql.Connection;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;

/**
 * 20090814
 * 获取表和视图列表，使用父类方法；
 * 去除了和父类重复的代码；
 * @author dw
 *
 */
public class SybaseDbMetaData extends DbMetaDataImpl {

  public SybaseDbMetaData(Connection con) {
    super(con);
  }
  public SybaseDbMetaData(ConnectionFactory dbf) {
    super(dbf);
  }

  protected TableMetaData createTableMetaData(String tablename){
    return new SybaseTableMetaData(this,tablename);
  }
}
