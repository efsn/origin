package com.esen.jdbc.dialect.impl.sybase;

import java.sql.Connection;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;

public class SybaseIQDbMetaData extends SybaseDbMetaData {

  public SybaseIQDbMetaData(Connection con) {
    super(con);
  }
  public SybaseIQDbMetaData(ConnectionFactory dbf) {
    super(dbf);
  }
  protected TableMetaData createTableMetaData(String tablename){
    return new SybaseIQTableMetaData(this,tablename);
  }
}
