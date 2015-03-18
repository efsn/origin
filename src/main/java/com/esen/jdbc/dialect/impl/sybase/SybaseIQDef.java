package com.esen.jdbc.dialect.impl.sybase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.dialect.Dialect;

public class SybaseIQDef extends SybaseDef {


  public SybaseIQDef(Dialect dl) {
    super(dl);
  }
  public boolean indexExists(Connection conn,String tbname, String indexname) throws SQLException {
    String sql = "select iname from sys.sysindexes " +
        "where iname='" + indexname + "'";
    //System.out.println(sql);
    Statement stmt = conn.createStatement();
    ResultSet rs = null;
    try {
      rs = stmt.executeQuery(sql);
      return ( (rs.next() ? true : false));
    }
    finally {
      rs = null;
      stmt.close();
    }
  }

}
