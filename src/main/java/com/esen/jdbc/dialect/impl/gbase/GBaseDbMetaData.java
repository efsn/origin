package com.esen.jdbc.dialect.impl.gbase;

import java.sql.Connection;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;

public class GBaseDbMetaData extends DbMetaDataImpl {

  public GBaseDbMetaData(ConnectionFactory dbf){
    super(dbf);
  }
  
  public GBaseDbMetaData(Connection con) {
    super(con);
  }
  
  protected TableMetaData createTableMetaData(String tablename){
    return new GBaseTableMetaData(this,tablename);
  }
  
   
}
