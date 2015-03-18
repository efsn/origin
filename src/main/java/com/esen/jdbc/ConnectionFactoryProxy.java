package com.esen.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.Dialect;


/**
 * ConnectionFactory接口的代理类；
 * <p>Title: Esensoft JDBC</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: EsenSoft</p>
 * @author dw
 * @version 1.0
 */

public class ConnectionFactoryProxy
    implements ConnectionFactory {

  private ConnectionFactory factory;

  public Connection getNewConnection() throws SQLException{
    return factory.getNewConnection();
  }
  
  public Connection getConnection() throws SQLException{
    return factory.getConnection();
  }

  public DbDefiner getDbDefiner(){
    return factory.getDbDefiner();
  }

  public void setFactory(ConnectionFactory factory) {
    this.factory = factory;
  }

  public Dialect getDialect(){
    return factory.getDialect();
  }

  public DataBaseInfo getDbType()  {
    return factory.getDbType();
  }

  public boolean compareDataBaseTo(ConnectionFactory cf) {
    return factory.compareDataBaseTo(cf);
  }

  public String getName() {
    return factory.getName();
  }

public DbMetaData getDbMetaData() {
	return factory.getDbMetaData();
}

public JdbcConnectionDebug getJdbcConnectionDebug() {
	return factory.getJdbcConnectionDebug();
}


}