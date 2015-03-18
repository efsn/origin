package com.esen.jdbc.pool;

import javax.naming.spi.ObjectFactory;
import javax.naming.Name;
import javax.naming.Context;
import java.util.Hashtable;
import java.util.Properties;
import java.sql.SQLException;
import javax.naming.RefAddr;
import javax.naming.Reference;


public class DataSourceFactory
    implements ObjectFactory {
  private final static String PROP_DEFAULTAUTOCOMMIT = PoolPropName.PROP_DEFAULTAUTOCOMMIT;
  private final static String PROP_DRIVERCLASSNAME = PoolPropName.PROP_DRIVERCLASSNAME;
  private final static String PROP_MAXACTIVE = PoolPropName.PROP_MAXACTIVE;
  private final static String PROP_MINIDLE = PoolPropName.PROP_MINIDLE;
  private final static String PROP_MAXWAIT = PoolPropName.PROP_MAXWAIT;
  private final static String PROP_USERNAME = PoolPropName.PROP_USERNAME;
  private final static String PROP_PASSWORD = PoolPropName.PROP_PASSWORD;
  private final static String PROP_URL = PoolPropName.PROP_URL;
  private final static String PROP_ISDEBUG = PoolPropName.PROP_ISDEBUG;
  private final static String PROP_CATALOG = PoolPropName.PROP_CATALOG;
  private final static String PROP_CHARACTERENCODING = PoolPropName.PROP_CHARACTERENCODING;
  private final static String[] ALL_PROPERTIES = {
      PROP_DEFAULTAUTOCOMMIT,
      PROP_DRIVERCLASSNAME,
      PROP_MAXACTIVE,
      PROP_MINIDLE,
      PROP_MAXWAIT,
      PROP_PASSWORD,
      PROP_URL,
      PROP_USERNAME,
      PROP_ISDEBUG,
      PROP_CATALOG,
      PROP_CHARACTERENCODING
  };
  /**
   * 实现DataSource Context Naming配置接口
   * @param obj Object
   * @param name Name
   * @param nameCtx Context
   * @param environment Hashtable
   * @throws Exception
   * @return Object
   */
  public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                  Hashtable environment) throws Exception {
    if ( (obj == null) || ! (obj instanceof Reference)) {
      return null;
    }
    Reference ref = (Reference) obj;
    if (!"com.esen.jdbc.pool.BaseDataSource".equals(ref.getClassName())) {
      return null;
    }

    Properties properties = new Properties();
    for (int i = 0; i < ALL_PROPERTIES.length; i++) {
      String propertyName = ALL_PROPERTIES[i];
      RefAddr ra = ref.get(propertyName);
      if (ra != null) {
        String propertyValue = ra.getContent().toString();
        properties.setProperty(propertyName, propertyValue);
      }
    }

    return createDataSource(properties);
  }

  /**
   * 创建DataSource
   * @param props Properties
   * "defaultAutoCommit" 是否自动Commit
   * "driverClassName" 数据库驱动类名称；
   * "url"    数据库连接url；
   * "username"  数据库用户名；
   * "password"  对应用户名密码；
   * "maxActive" 最大连接数，默认15；
   * "minIdle"   最小等待连接中的数量，默认5；
   * "maxWait"   最大等待时间（long）默认10000；
   * "isDebug"   是否调试状态(true,fasle)，默认fasle；
   * "catalog"      数据库目录
   * @throws SQLException
   * @return DataSource
   */
  private BaseDataSource createDataSource(Properties props) throws SQLException {
    BaseDataSource _ds = new BaseDataSource(props);
    return _ds;
  }

}
