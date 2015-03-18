package com.esen.jdbc.impl;

import java.sql.Connection;

import junit.framework.TestCase;

import com.esen.jdbc.ConnectFactoryManagerImpl;
import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.DataCopy;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;

public class DataCopyBigTest extends TestCase {
  private void initConnectionFactory(){
    ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
    /*SimpleConnectionFactory mysql = new SimpleConnectionFactory(
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://localhost/bidb2?useUnicode=true&characterEncoding=GB2312",
        "root", "dw",true);
    fm.setConnectionFactory("dw_mysql", mysql);*/
    SimpleConnectionFactory mssql = new SimpleConnectionFactory(
        "net.sourceforge.jtds.jdbc.Driver",
        "jdbc:jtds:sqlserver://192.168.1.156:1433/irpt;TDS=8.0;SendStringParametersAsUnicode=true;charset=GB2312",
        "sa", "admin",false);
    fm.setConnectionFactory("mssql_156", mssql);
    SimpleConnectionFactory oracle = new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.200:1521:orcdb", "tax", "tax",false);
    fm.setConnectionFactory("200_oracle_tax",oracle);
    DefaultConnectionFactory.set(fm);
  }
  private void dropTable(String pool,String tablename) throws Exception{
    ConnectionFactory cf = DefaultConnectionFactory.get(pool, true);
    Connection conn = cf.getConnection();
    try{
      DbDefiner dbv = cf.getDbDefiner();
      if(dbv.tableExists(conn, null, tablename))
        dbv.dropTable(conn, null, tablename);
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
  /**
   * 测试DataCopy大数据量的写入；
   * fact_sbxx 有33万数据；
   * mssql_156 是普通pc服务器
   * 200_oracle_tax 是真正服务器
   * 从200_oracle_tax写入mssql_156耗时： 648秒；
   * @throws Exception
   */
  public void testSelectInto() throws Exception{
    initConnectionFactory();
    dropTable("mssql_156","fact_sbxx");
    System.out.println("start....");
    long t = System.currentTimeMillis();
    DataCopy.createInstance().selectInto("200_oracle_tax", "select * from fact_sbxx"
        , "mssql_156", "fact_sbxx");
    System.out.println("耗时："+(System.currentTimeMillis()-t)/1000+" 秒；");
  }
  /**
   * 从mssql_156写入200_oracle_tax耗时： 167秒；
   */
  public void testSelectInto2() throws Exception{
    initConnectionFactory();
    dropTable("200_oracle_tax","fact_sbxx2");
    System.out.println("start....");
    long t = System.currentTimeMillis();
    DataCopy.createInstance().selectInto("mssql_156", "select * from fact_sbxx"
        , "200_oracle_tax", "fact_sbxx2");
    System.out.println("耗时："+(System.currentTimeMillis()-t)/1000+" 秒；");
  }
}
