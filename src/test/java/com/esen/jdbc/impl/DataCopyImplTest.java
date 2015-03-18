package com.esen.jdbc.impl;

import java.sql.Connection;

import junit.framework.TestCase;

import com.esen.jdbc.ConnectFactoryManagerImpl;
import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.DataCopy;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;

public class DataCopyImplTest extends TestCase {
   String oracle_sql = "select * from ( select row$_.*, rownum rownum$_ from ( select a.QBXSSR as QBXSSR,a.YSXSSR as YSXSSR,b.SYSL_XSE_1 as SYSL_XSE_1,a.HY_DM as HY_DM,a.ID as ID "
    + "from ( "
    + "select QBXSSR as QBXSSR,YSXSSR as YSXSSR,HY_DM as HY_DM,ID as ID "
    + "from ( "
    + "select sum(a.QBXSSR) as QBXSSR,sum(a.YSXSSR) as YSXSSR, SUBSTR(a.HY_DM,1,1) as HY_DM,a.NSRDZDAH as ID,row_number() over (partition by SUBSTR(a.HY_DM,1,1) order by sum(a.QBXSSR) desc nulls last) as ID_sortn "
    + "from FACT_SBXX a "
    + "where (a.NSR_SWJG_DM like '121%') "
    + "group by SUBSTR(a.HY_DM,1,1),a.NSRDZDAH "
    + "order by SUBSTR(a.HY_DM,1,1) nulls first,ID_sortn nulls first) a "
    + "where (ID_sortn<=5)) a "
    + "left join ( "
    + "select sum(a.SYSL_XSE_1) as SYSL_XSE_1, SUBSTR(a.HY_DM,1,1) as HY_DM "
    + "from FACT_ZZS a "
    + "where (a.NSR_SWJG_DM like '121%') "
    + "group by SUBSTR(a.HY_DM,1,1)) b "
    + "on (a.HY_DM=b.HY_DM) ) row$_ ) where rownum$_ <= 1000 ";
  private void initConnectionFactory(){
    ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
    SimpleConnectionFactory mysql = new SimpleConnectionFactory(
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://localhost/bidb2?useUnicode=true&characterEncoding=GB2312",
        "root", "dw",true);
    fm.setConnectionFactory("dw_mysql", mysql);
    SimpleConnectionFactory oracle = new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.200:1521:orcdb", "tax", "tax",true);
    fm.setConnectionFactory("200_oracle_tax",oracle);
    DefaultConnectionFactory.set(fm);
  }
  public void testCreateTable() throws Exception{
    initConnectionFactory();
    dropTable("dw_mysql","test_hy");
    DataCopy.createInstance().createTableAsSelect("200_oracle_tax", 
        "select * from dim_hy", "dw_mysql", "test_hy");
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
  public void testCreatTable2() throws Exception{
    initConnectionFactory();
    dropTable("200_oracle_tax","test_hy");
    DataCopy.createInstance().createTableAsSelect("200_oracle_tax", 
        "select * from dim_hy", "200_oracle_tax", "test_hy");
  }
  public void testCreatTable3() throws Exception {
    initConnectionFactory();
    dropTable("dw_mysql", "test_olap");
    DataCopy.createInstance().createTableAsSelect("200_oracle_tax", oracle_sql,
        "dw_mysql", "test_olap");
  }
  public void testCreatTable4() throws Exception {
    initConnectionFactory();
    dropTable("200_oracle_tax", "test_olap");
    DataCopy.createInstance().createTableAsSelect("200_oracle_tax", oracle_sql,
        "200_oracle_tax", "test_olap");
  }
  public void testInsertSQL() throws Exception{
    initConnectionFactory();
    dropTable("dw_mysql","test_hy");
    DataCopy.createInstance().selectInto("200_oracle_tax", 
        "select * from dim_hy", "dw_mysql", "test_hy");
  }
  public void testInsertSQL2() throws Exception{
    initConnectionFactory();
    dropTable("200_oracle_tax","test_hy");
    DataCopy.createInstance().selectInto("200_oracle_tax", 
        "select * from dim_hy", "200_oracle_tax", "test_hy");
  }
  public void testInsertSQL3() throws Exception{
    initConnectionFactory();
    dropTable("dw_mysql","test_hy");
    DataCopy.createInstance().selectInto("200_oracle_tax", 
        "select * from dim_hy", "dw_mysql", "test_hy");
  }
  public void testInsertSQL4() throws Exception{
    initConnectionFactory();
    dropTable("200_oracle_tax","test_hy");
    DataCopy.createInstance().selectInto("200_oracle_tax", 
        "select * from dim_hy", "200_oracle_tax", "test_hy");
  }
  public void testInsertSQL5() throws Exception{
    initConnectionFactory();
    dropTable("dw_mysql","test_olap");
    DataCopy.createInstance().selectInto("200_oracle_tax", 
        oracle_sql, "dw_mysql", "test_olap");
  }
  public void testInsertSQL6() throws Exception{
    initConnectionFactory();
    dropTable("dw_mysql","test_olap");
    DataCopy.createInstance().selectInto("200_oracle_tax", 
        oracle_sql, "dw_mysql", "test_olap");
  }
  public void testInsertSQL7() throws Exception{
    initConnectionFactory();
    dropTable("200_oracle_tax","test_olap");
    DataCopy.createInstance().selectInto("200_oracle_tax", 
        oracle_sql, "200_oracle_tax", "test_olap");
  }
  public void testInsertSQL8() throws Exception{
    initConnectionFactory();
    dropTable("200_oracle_tax","test_olap");
    DataCopy.createInstance().selectInto("200_oracle_tax", 
        oracle_sql, "200_oracle_tax", "test_olap");
  }
}
