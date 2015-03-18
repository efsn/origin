package com.esen.jdbc;

import com.esen.jdbc.pool.*;

import java.sql.*;
import java.util.Properties;

/**
 * 此类是简便的创建ConnectionFactory接口实例的类；
 * 常用于测试类里面，快速的创建连接池；
 * @author dw
 * @version 1.0
 */

public class SimpleConnectionFactory extends PoolConnectionFactory {
  private BaseDataSource baseDataSource;
  public SimpleConnectionFactory(String driverClassName, String url,
      String userName, String password) {
    this(driverClassName,url,userName,password,"error");
  }
  public SimpleConnectionFactory(String driverClassName, String url,
	      String userName, String password, String logLever) {
	  this(driverClassName,url,userName,password,logLever,null);
  }
  
  public SimpleConnectionFactory(String driverClassName, String url,
      String userName, String password, String logLever,String defaulSchema) {
    super(null);
    Properties props = new Properties();
    props.setProperty("url", url);
    props.setProperty("driverClassName", driverClassName);
    props.setProperty("username", userName);
    props.setProperty("password", password);
    props.setProperty("logLevel", logLever);
    if(defaulSchema!=null)
      props.setProperty("default_schema", defaulSchema);
    baseDataSource = new BaseDataSource();
    baseDataSource.setProperties(props);

    setDataSource(baseDataSource);
    
  }
  /**
   * @deprecated
   * @param driverClassName
   * @param url
   * @param userName
   * @param password
   * @param debug
   */
  public SimpleConnectionFactory(String driverClassName, String url,
      String userName, String password, boolean debug) {
    this(driverClassName,url,userName,password,debug?"debug":"error");
  }
  /**
   * @deprecated
   * @param datasource
   * @param debug
   */
  public SimpleConnectionFactory(String datasource,boolean debug){
    super(null);
    baseDataSource = new BaseDataSource();
    baseDataSource.setOtherDataSource(datasource);
    baseDataSource.setDebug(true);
    setDataSource(baseDataSource);
  }

  public void close() throws SQLException {
    if(baseDataSource!=null)
      baseDataSource.close();
  }
}
