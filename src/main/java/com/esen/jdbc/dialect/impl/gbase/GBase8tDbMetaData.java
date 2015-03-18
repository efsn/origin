package com.esen.jdbc.dialect.impl.gbase;

import java.sql.Connection;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;

public class GBase8tDbMetaData extends DbMetaDataImpl {

  public GBase8tDbMetaData(ConnectionFactory dbf){
    super(dbf);
  }
  
  public GBase8tDbMetaData(Connection con) {
    super(con);
  }
  
  protected TableMetaData createTableMetaData(String tablename){
    return new GBase8tTableMetaData(this,tablename);
  }
  
   
}
