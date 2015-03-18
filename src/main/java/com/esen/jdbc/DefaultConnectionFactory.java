package com.esen.jdbc;

/**
 * <p>Title: Esensoft JDBC</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Esensoft</p>
 * @author dw
 * @version 1.0
 */

public final class DefaultConnectionFactory {
  private static final String DEFAULT_POOL_ADDRESS = "default-pool-address";
  private DefaultConnectionFactory() {
  }

  private static ConnectFactoryManager cfm;
  public  final static ConnectionFactory get(){
    return cfm.getDefaultConnectionFactory();
  }

  public  final static ConnectionFactory get(String dsname, boolean throwIfNotExists){
    return cfm.getConnectionFactory(dsname, throwIfNotExists);
  }

  public final static void set(ConnectFactoryManager f){
    cfm = f;
  }
  
  /**
   * 假设A机器的地址是192.168.1.200，它上面有mysql和oracle，A上也部署了bi，bi的默认数据库连接池是本机的mysql，bi中还配置了一个连接池连接的是本机的oracle
   * B机器也部署了bi，但它想将自己的默认数据库也连接A机器上的mysql，B可以配置jdbc连接192.168.1.200上的mysql，但当系统启动后bi内部还有一个连接池是连接A本机的oracle的
   * 而且写的地址是localhost，这就造成了B无法使用这个连接池，因为在B机器上解析“localhost”就会认为是B机器自己，也许你想把localhost改为固定ip192.168.1.200不就ok了，是的
   * 但是那样会有一个问题：如果A机器的ip是动态分配的或A的服务器备份恢复到另外的地方后，都必须再次修改那个连接池的地址，这样很不方便
   * 现在我们可以在连接池的url中将localhost改写为default-pool-address，这样在B机器上和A机器上都能使用那个连接池了
   */
  public final static String convertDbUrl(String url){
    int i = url.toLowerCase().indexOf(DEFAULT_POOL_ADDRESS);
    if (i!=-1){
      ConnectionFactory dcf = DefaultConnectionFactory.get();
      url = url.substring(0,i)+dcf.getDbType().getDatabaseAddress()+url.substring(i+DEFAULT_POOL_ADDRESS.length());
    }
    return url;
  }

}