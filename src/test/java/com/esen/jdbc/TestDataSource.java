package com.esen.jdbc;

import java.sql.*;
import java.util.Properties;

import junit.framework.TestCase;

import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.pool.BaseDataSource;

public class TestDataSource  extends TestCase  {
  
  private BaseDataSource getDataSource(){
    BaseDataSource baseDataSource = new BaseDataSource();
    Properties props = new Properties();
    props.setProperty("url", "jdbc:oracle:thin:@192.168.1.200:1521:esenbi");
    props.setProperty("driverClassName", "oracle.jdbc.driver.OracleDriver");
    props.setProperty("username", "test");
    props.setProperty("password", "test");
    props.setProperty("logLevel", "debug");
    baseDataSource.setProperties(props);
    return baseDataSource;
  }
  
  /**
   * 测试reload连接池；
   * @throws Exception
   */
  public void testDataSourceReload() throws Exception {
    BaseDataSource ds = getDataSource();
    try{
    Connection conn = ds.getConnection();
    try {
      Thread.sleep(1000);
    }
    finally {
      conn.close();
    }
    ds.reload();
    DataBaseInfo dbinf = ds.getDbType();
    assertEquals(true,dbinf!=null);
    
    conn = ds.getConnection();
    try {
      Thread.sleep(1000);
    }
    finally {
      conn.close();
    }
    }finally{
      ds.close();
    }
  }
  
  /**
   * 测试连接没有关闭，连接池是否能侦测到；
   * @throws Exception
   */
  public void testCloseConnection() throws Exception{
    BaseDataSource ds = getDataSource();
    try{
      Connection conn = ds.getConnection();
      try {
        Thread.sleep(1000);
      }
      finally {
        //conn.close();
      }

      Thread.sleep(60000);
      System.gc();
      Thread.sleep(2000);
    }finally{
      ds.close();
    }
  }
  
  /**
   * 测试更关键参数，连接池需要reload();
   * @throws Exception
   */
  public void testReload() throws Exception{
    BaseDataSource baseDataSource = getDataSource();
    try{
      //获取连接一次
      Connection conn = baseDataSource.getConnection();
      try {
        Thread.sleep(1000);
      }
      finally {
        conn.close();
      }
      assertEquals(true,baseDataSource.getDbType()!=null);
      //更改用户和密码
      Properties props = new Properties();
      props.setProperty("username", "dev1");
      props.setProperty("password", "yf,one^");
      baseDataSource.setProperties(props);
      //获取链接，这时和前面的链接应该是不是一个对象；
      Connection conn2 = baseDataSource.getConnection();
      try {
        assertEquals(false, conn.equals(conn2));
        Thread.yield();
      }
      finally {
        conn2.close();
      }
      assertEquals(true,baseDataSource.getDbType()!=null);
    }finally{
      baseDataSource.close();
    }
  }
  
  /**
   * 测试新增参数maxIdleTime
   * <pre>
   * 测试原理：
   * 设置最大闲置时间为10秒；
   * 连接池初始化时，只获取一个连接到空闲池；
   * 获取一次连接，关闭；
   * 等9秒后，再次获取连接，这时获取的连接和前一次获取的连接，应该是同一个对象，关闭；
   * 等11秒后，再次获取连接，这时空闲池的连接超过了最大闲置时间，应该被抛弃，同时产生新的连接返回，
   * 获取的连接应该和前两次获取的连接对象不同，同时debug状态应该打印："关闭闲置时间过长的连接；"信息；
   * </pre>
   * @throws InterruptedException 
   */
  public void testIdleTime() throws Exception {
    BaseDataSource baseDataSource = getDataSource();
    Properties props = new Properties();
    props.setProperty("maxIdleTime", String.valueOf(10 * 1000));//设置最大闲置时间：10秒；
    baseDataSource.setProperties(props);
    try {
      //获取连接一次
      Connection conn = baseDataSource.getConnection();
      try {
        Thread.sleep(1000);
      }
      finally {
        conn.close();
      }
      //过9秒后再次获取连接；
      Thread.sleep(9000);
      Connection conn2 = baseDataSource.getConnection();
      try {
        assertEquals(true, conn.equals(conn2));
        Thread.sleep(1);
      }
      finally {
        conn2.close();
      }
      //等待11秒后再次获取连接；
      Thread.sleep(11000);
      Connection conn3 = baseDataSource.getConnection();
      try {
        assertEquals(false, conn.equals(conn3));
        Thread.sleep(1000);
      }
      finally {
        conn3.close();
      }
    }
    finally {
      baseDataSource.close();
    }
  }
  
  /**
   * 测试连接池并发；
   * 同时开启50个线程，每个线程获取10次连接，每次获取持有1-2秒不等时间 ；
   * @throws Exception
   */
  public void testDataSource() throws Exception {
     BaseDataSource baseDataSource = getDataSource();
    try{
      System.out.println("begin init.............");
      /**
       * 并发测试获取连接；
       */
      int len = 30;
      GetOneConnection[] gcons = new GetOneConnection[len];
      for (int i = 0; i < len; i++) {
        gcons[i] = new GetOneConnection(baseDataSource,i);
        Thread.yield();
      }
      //等待所有线程执行完毕；
      for (int i = 0; i < len; i++) {
        gcons[i].join();
        
      }

    }finally{
      //最后关闭连接池；
      baseDataSource.close();
    }
  }

  class GetOneConnection extends Thread{
    private BaseDataSource dbs = null;
    private int i;
    public GetOneConnection(BaseDataSource ds, int i){
      dbs = ds;
      this.i = i;
      start();
    }
    /**
     * 每个线程获取10次连接；
     */
    public void run(){
      Connection conn = null;
      System.out.println("开始执行第"+(i+1)+"个线程；");
      for(int i=0;i<10;i++){
        conn = null;
        try {
          conn = dbs.getConnection();
          Statement stat = conn.createStatement();
          try{
          /**
           * 每个连接获取后等待1-2秒
           */
          sleep(Math.round(Math.random() * 1000+1500));
          }finally{
            stat.close();
          }
        }
        catch (Exception se) {
          se.printStackTrace();
        }
        finally {
          try {
            if (conn != null)
              conn.close();
          }
          catch (SQLException se) {
            se.printStackTrace();
          }
        }
      }
      System.out.println("第"+(i+1)+"个线程执行完毕；");
    }
  }
}
