package com.esen.jdbc.pool;

import com.esen.jdbc.SqlConst;

public class DefaultPoolParam {
  public DefaultPoolParam() {
  }
  public static final boolean DEFAULT_AUTOCOMMIT = true;
  public static final int DEFAULT_MAX_ACTIVE = 20;  //最大连接数据库连接数
  public static final int DEFAULT_MIN_IDLE = 5;    //最小等待连接中的数量
  public static final long DEFAULT_MAX_WAIT = 15000;  //最大等待时间
  public static final long DEFAULT_MAX_IDLE_TIME = 3600000;//连接最大闲置时间，默认一小时；
  public static final long DEFAULT_MIN_USE_TIME = 3600000;//需要强制关闭的连接的最小使用时间，默认一小时；

  public static final String DB_ORACLE    = SqlConst.DB_ORACLE    ;
  public static final String DB_ORACLE817 = SqlConst.DB_ORACLE8i ;
  public static final String DB_MYSQL     = SqlConst.DB_MYSQL     ;
  public static final String DB_SYBASE    = SqlConst.DB_SYBASE    ;
  public static final String DB_SYBASE_IQ = SqlConst.DB_SYBASE_IQ ;
  public static final String DB_DB2       = SqlConst.DB_DB2       ;
  public static final String DB_MSSQL     = SqlConst.DB_MSSQL     ;
  public static final String DB_OTHER     = SqlConst.DB_OTHER     ;
  public static final String DB_OSCAR     = SqlConst.DB_OSCAR     ;//神舟飞船用的数据库；
}
