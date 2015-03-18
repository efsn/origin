package com.esen.jdbc.pool;

public class PoolPropName {
  private PoolPropName() {
  }
  public final static String PROP_DEFAULTAUTOCOMMIT = "defaultAutoCommit";
  public final static String PROP_DRIVERCLASSNAME = "driverClassName";
  public final static String PROP_MAXACTIVE = "maxActive";
  public final static String PROP_MINIDLE = "minIdle";
  public final static String PROP_MAXWAIT = "maxWait";
  public final static String PROP_USERNAME = "username";
  public final static String PROP_PASSWORD = "password";
  public final static String PROP_URL = "url";
  public final static String PROP_ISDEBUG = "isDebug";
  public final static String PROP_MAXIDLETIME = "maxIdleTime";
  /**
   * 第三方连接池名称；
   * 同PROP_OTHERDATASOURCE
   */
  public final static String PROP_OTHERDATASOURCE3 = "datasource3";
  /**
   * 第三方连接池名称；
   */
  public final static String PROP_OTHERDATASOURCE = "datasource";
  public final static String PROP_CATALOG = "catalog";
  public final static String PROP_CHARACTERENCODING = "characterEncoding";
  public final static String PROP_DESTCHARSETENCODING = "destCharSetEncoding";
  public final static String PROP_INITSQL = "initsql";
  
  public final static String PROP_LOGLEVER = "logLevel";
  public final static String PROP_SQLLOGFILE= "sqlLogFile";
  
  /**
   * 通过参数指定默认的schema名；
   */
  public final static String PROP_DEFAULTSCHEMA = "default_schema";
  
  /**
   * 在连接池中无连接可用时，是否关闭最长时间没用的连接
   */
  public final static String PROP_ISCLOSELONGTIMECONN = "isCloseLongTimeConn";
  
  /**
   * 强制关闭连接时，该连接的最短使用时间，单位为分钟
   */
  public final static String PROP_MINUSETIME = "minUseTime";
  
  /**
   * 连接池描述信息
   */
  public final static String PROP_DESC = "desc";
}
