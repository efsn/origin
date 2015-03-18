package com.esen.jdbc.pool.impl.sybase;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledResultSet;


public class SbaseIQPooledResultSet extends PooledResultSet {

  public SbaseIQPooledResultSet(ResultSet rs, PooledConnection pconn) throws SQLException {
    super(rs, pconn);
  }
  public ResultSetMetaData getMetaData() throws SQLException {
    if(rsmeta==null)
      rsmeta = new SybaseIQResultSetMetaData(_rs.getMetaData(),_pconn.getDbType());
    return rsmeta;
  }

}
