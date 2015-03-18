package com.esen.jdbc.dbdefiner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableMetaData;

public class DbDefinerSybaseTest extends DbDefinerTest {

  /*public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.sybase.jdbc3.jdbc.SybDriver",
        "jdbc:sybase:Tds:192.168.1.51:5000/test?charset=cp936",
        "sa", "123456","debug");
  }*/
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.sybase.jdbc3.jdbc.SybDriver",
        "jdbc:sybase:Tds:192.168.1.212:5000/testdb?charset=cp936",
        "test", "123456","debug");
  }
  /*public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.sybase.jdbc2.jdbc.SybDriver",
        "jdbc:sybase:Tds:192.168.1.2:5000/shltestdb",
        "shltest", "shltest",true);
  }*/
  //12.5.1 ebf11522
  private Connection getConnection() throws Exception{
    Class.forName("com.sybase.jdbc2.jdbc.SybDriver");
    return DriverManager.getConnection("jdbc:sybase:Tds:192.168.1.2:5000/shltestdb","shltest","shltest");
  }
  //12.5.2 ebf11948
  private Connection getConnection2() throws Exception{
    Class.forName("com.sybase.jdbc2.jdbc.SybDriver");
    return DriverManager.getConnection("jdbc:sybase:Tds:192.168.1.42:5000/bidb?charset=utf8","test","111111");
  }
  
  /**
   * Sybase修改字段结构不支持事务；
   * 进行修改前，如果已经进入了一个事务，先调用commit将事务关闭，在设置AutoCommit为true；
   * 这样可以避免在事务内设置AutoCommit参数是报：
   * SET CHAINED command not allowed within multi-statement transaction.
   * 的异常；
   * 这里的改动同 dropColumn方法；
   * 上面的办法在12.5.2 ebf11948 版本没有问题；
   * 但是在12.5.1 ebf11522 版本有问题：
   * 事务开始时 调用 conn.setAutCommit(false);
   * 由于修改字段结构不支持事务，需要将其conn.setAutCommit(true);
   * setAutCommit()的实现掉用getAutoCommit()获取当前状态进行判断设置（不一致就设置）；
   * 这时此版本的Sybase报SET CHAINED command not allowed within multi-statement transaction.异常；
   * 
   * @throws Exception
   */
  /*public void testTranslate() throws Exception{
    Connection con = getConnection();
    try{
      
      con.setAutoCommit(false);
      boolean tbexist = false;
      Statement stat = con.createStatement();
      try{
        stat.executeQuery("select * from t_test22");
        tbexist = true;
      }catch(Exception ex){
      }finally{
        stat.close();
      }
      if(tbexist){
      stat = con.createStatement();
      try{
        stat.executeUpdate("drop table t_test22");
      }finally{
        stat.close();
      }
      }
      stat = con.createStatement();
      try{
        stat.executeUpdate("create table t_test22 (id_ varchar(20) null,name_ varchar(20) null,date_ date null)");
        stat.executeUpdate("insert into t_test22 (id_,name_)values('111','aaa')");
      }finally{
        stat.close();
      }
      
      stat = con.createStatement();
      try{
        con.commit();
        //boolean f = con.getAutoCommit();
        //if(!f)
          con.setAutoCommit(true);
        stat.executeUpdate("alter table t_test22 modify date_  datetime null");
        con.setAutoCommit(false);
        
        stat.executeUpdate("insert into t_test22 (id_,name_)values('222','bb')");
        con.commit();
        con.setAutoCommit(true);
      }finally{
        stat.close();
      }
      
      
    }finally{
      con.close();
    }
  }*/
  /*
   * 测试字段名表名是否大小写敏感
   */
  public void testCreateTableFieldName() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Dialect dl = getConnectionFactory().getDialect();
    Connection conn = null;
    String tbname = "TbName";
    String fieldname = dbv.formatFieldName("INTtest_",false);
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tbname)){
        dbv.dropTable(conn,null,tbname);
      }
      dbv.defineIntField(fieldname, 30, null, true, false);
      assertEquals(tbname,getTableNameNoSchema(dbv.createTable(conn,null,tbname)));
      
    }finally{
      if(conn!=null)
        conn.close();
    }
    TableMetaData tmd = dl.createDbMetaData().getTableMetaData(tbname);
    assertEquals(fieldname,tmd.getColumnName(0));
    
    //表名大小写不一致，是否能正常查询；
    try{
      conn = getConnectionFactory().getConnection();
      Statement stat = conn.createStatement();
      try{
        stat.executeQuery("select * from tbname");
        assertEquals(true,false);
      }catch(SQLException se){
        assertEquals(true,true);
      }finally{
        stat.close();
      }
      
    }finally{
      if(conn!=null)
        conn.close();
    }
    //测试字段名是否可以大小写不敏感；
    try{
      conn = getConnectionFactory().getConnection();
      Statement stat = conn.createStatement();
      try{
        stat.executeQuery("select inttest_ from "+tbname);
        assertEquals(true,false);
      }catch(SQLException se){
        assertEquals(true,true);
      }finally{
        stat.close();
      }
      
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
  /**
   * 测试sybase插入空串''，读出来是' ',显然不对，jdbc层转换下：当作null插入
   * @throws Exception 
   */
  public void testInsertNull() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = getConnectionFactory().getConnection();
    try{
      if(dbv.tableExists(conn, null, "testdb"))
        dbv.dropTable(conn, null, "testdb");
      dbv.clearDefineInfo();
      dbv.defineStringField("name_", 10, null, true, false);
      dbv.defineFloatField("num_", 30, 2, null, true, false);
      dbv.createTable(conn, null, "testdb");
      
     PreparedStatement pstat = conn.prepareStatement("insert into testdb (name_,num_)values(?,?)");
     pstat.setString(1, "");
     pstat.setInt(2, 1);
     pstat.addBatch();
     pstat.setString(1, null);
     pstat.setInt(2, 2);
     pstat.addBatch();
     pstat.executeBatch();
     pstat.close();
     
     Statement stat = conn.createStatement();
     ResultSet rs = stat.executeQuery("select name_,num_ from testdb order by num_");
     rs.next();
     assertEquals(null,rs.getString(1));
     rs.next();
     assertEquals(null,rs.getString(1));
     stat.close();
    }finally{
      conn.close();
    }
  }
}
