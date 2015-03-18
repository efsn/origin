package com.esen.jdbc;

import java.sql.*;

import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.Dialect;

/**
 * <p>定义一个获得数据库链接和数据定义接口的接口</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Esensoft</p>
 * @author dw
 * @version 1.0
 */

public interface ConnectionFactory {
  /**
   * 返回此连接池的名称
   */
  public String getName();
  
  /**
   * 20091118
   * 总是获得一个新建立的连接，此连接不进入连接池；
   * 使用完毕，必须关闭此连接；
   * 返回的是PooledConnection，拥有此连接池中数据库连接的所有功能；
   * 不同的是close()方法，将直接关闭物理连接；
   * 
   * 如果配置了第三方连接池，要获得此连接，必须设置driverclassname,url,username,password参数，否则会出异常；
   * 
   * 增加这个方法原因是：Oralce在调用存储过程包时，如果存储过程包中有全局变量，且包中的存储过程被另一个会话修改，并编译，
   * 那么连接池中先前创建的连接再调用此存储过程包，就会出现ORA-04068: existing state of packages has been discarded.异常；
   * 解决办法是：每次调用存储过程都创建一个新的数据库连接来执行；
   * @return
   * @throws SQLException
   */
  public Connection getNewConnection() throws SQLException;
  
  /**
   * 获得一个数据库链接，在一个线程类连续多次调用此函数将返回同一个结果，这样可以避免嵌套获取数据库连接形成死锁。
   * 返回的Connection是有引用计数的，执行了多少次getConnection()方法，就应该执行多少次close方法，否则连接无法回收。
   * @return
   */
  public Connection getConnection()throws SQLException;

  /**
   * 获得一个数据定义接口
   * @return
   */
  public DbDefiner getDbDefiner();

  /**
   * 获得一个数据库特征功能的对象。
   * @return
   */
  public Dialect getDialect();
  
  /**
   * 数据库类型: Oracle,Oracle817,Mysql,DB2,Sybase,Mssql,Other
   * 返回DefaultPoolParam里面的字符串常量
   */
  public DataBaseInfo getDbType();
  
  /**
   * 获取一个缓存的DbMetaData实现类，主要缓存数据表结构，
   * 对于多次访问同一个表的结构接口TableMetaData，将返回同一个TableMetaData实例；
   * 有缓存超时机制，并且可以设置缓存的超时时间；
   * 返回的实现类是一个唯一的实例。
   * @return
   */
  public DbMetaData getDbMetaData();
  
  
  /**
   * 比较是否是同一用户连接的同一数据库
   * @param cf
   * @return
   */
  public boolean compareDataBaseTo(ConnectionFactory cf);
  
  /**
   * 获取线程池调试接口, 如果日志级次小于等于LOG_LEVER_WARN, 开启调试
   * 返回结果可能为空
   * @return
   */
  public JdbcConnectionDebug getJdbcConnectionDebug();
}