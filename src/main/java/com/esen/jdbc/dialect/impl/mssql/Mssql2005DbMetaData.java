package com.esen.jdbc.dialect.impl.mssql;

import java.sql.Connection;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;

public class Mssql2005DbMetaData extends MssqlDbMetaData {

  public Mssql2005DbMetaData(Connection con) {
    super(con);
  }
  public Mssql2005DbMetaData(ConnectionFactory dbf) {
    super(dbf);
  }
  
  protected TableMetaData createTableMetaData(String tablename){
    return new Mssql2005TableMetaData(this,tablename);
  }
  
}
