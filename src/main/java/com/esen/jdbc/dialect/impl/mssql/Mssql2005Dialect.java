package com.esen.jdbc.dialect.impl.mssql;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;

public class Mssql2005Dialect extends MssqlDialect {

  public Mssql2005Dialect(Object f) {
    super(f);
  }
  public DbDefiner createDbDefiner() {
    return new Mssql2005Def(this);
  }
  public DbMetaData createDbMetaData() throws SQLException {
    if(dbmd==null){
      dbmd = connectionFactory!=null?new Mssql2005DbMetaData(connectionFactory):new Mssql2005DbMetaData(con);
    }
    return dbmd;
  }
  
  public DbMetaData createDbMetaData(Connection conn) throws SQLException {
    return new Mssql2005DbMetaData(conn);
  }
}
