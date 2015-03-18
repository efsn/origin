package com.esen.jdbc.dbdefiner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;

public class DbDefinerOracleTest extends DbDefinerTest {

 public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.100:1521:esenbi",
        "test", "test","debug");
  }

/* public ConnectionFactory createConnectionFactory(){

   return new SimpleConnectionFactory(
       "oracle.jdbc.driver.OracleDriver",
       "jdbc:oracle:thin:@192.168.1.225:1521:testdb",
       "testcase", "testcase","debug");
 }*/
 /*public ConnectionFactory createConnectionFactory(){
   return new SimpleConnectionFactory(
       "oracle.jdbc.driver.OracleDriver",
       "jdbc:oracle:thin:@192.168.1.102:1521:orcl",
       "test", "test","debug");
 }*/
 
 public static void main(String[] args) throws Exception{
	 DbDefinerOracleTest dt = new DbDefinerOracleTest();
	 dt.testSQLException();
 }
 
 /**
  * 测试oracle中对timestamp类型的写入，有没有丢失毫秒数；
  * 注意：date类型只到秒；
  * @throws Exception
  */
 public void testOralceSettimeStamp() throws Exception{
   DbDefiner dbv = getConnectionFactory().getDbDefiner();
   Connection conn = null;
   try{
     conn = getConnectionFactory().getConnection();
     if(dbv.tableExists(conn,null,tablename)){
       dbv.dropTable(conn,null,tablename);
     }
     dbv.clearDefineInfo();
     dbv.defineStringField("STR_",  20, null, false, true);
     dbv.defineDateField("DATE_", null, true, false);
     dbv.defineTimeStampField("TIMESTAMP_", null, true, false);
     dbv.createTable(conn,null,tablename);
     
     PreparedStatement pstat = conn.prepareStatement("insert into "+tablename+" (STR_,DATE_,TIMESTAMP_)values(?,?,?)");
     pstat.setString(1, "a");
     long t = System.currentTimeMillis();
     pstat.setDate(2, new java.sql.Date(t));
     pstat.setTimestamp(3, new java.sql.Timestamp(t));
     pstat.addBatch();
     pstat.executeBatch();
     pstat.close();
     
     Statement stat = conn.createStatement();
     ResultSet rs = stat.executeQuery("select STR_,DATE_,TIMESTAMP_ from "+tablename);
     assertEquals(true,rs.next());
     assertEquals(t,rs.getTimestamp(3).getTime());
     /**
      * Oracle的date类型只存储到秒；没有毫秒数 t-t%1000
      */
     assertEquals(t-t%1000,rs.getDate(2).getTime());
     stat.close();
   }finally{
     if(conn!=null)
       conn.close();
   }
 }
 
 /**
  * 读取Oracle 的物化视图表结构；
  * 读取索引，其索引字段可能不是表字段；
 * @throws Exception 
  */
 public void testOracleMaterialized() throws Exception{
   this.createTable();
   this.addData(10);
   DbDefiner dbv = getConnectionFactory().getDbDefiner();
   Dialect dl = getConnectionFactory().getDialect();
   Connection conn = null;
   try{
     conn = getConnectionFactory().getConnection();
     Statement stat = conn.createStatement();
     /**
      * 创建一个没有主键的物化视图，使用with rowid 关键字；
      * 此物化视图的索引是 rowid 不在物化视图的表字段中；
      * 读取索引应该忽略，不报错；
      */
     stat.execute("create materialized view mv_test refresh force on commit with rowid  as select STR_,NUM_ from "+tablename);
     stat.close();
     DbMetaData dbmd = dl.createDbMetaData(conn);
     TableMetaData tmd = dbmd.getTableMetaData("mv_test");
     TableIndexMetaData[] indexes = tmd.getIndexes();
     this.assertEquals(0, indexes==null?0:indexes.length);
     
     stat = conn.createStatement();
     stat.execute("drop materialized view mv_test ");
     stat.close();
   }finally{
     if(conn!=null)
       conn.close();
   }
 }

 /**
  * 测试nvarchar,nvarchar2类型的字段，能否正确的读取表结构；
 * @throws Exception 
  */
 public void testNvarchar2() throws Exception{
   DbDefiner dbv = getConnectionFactory().getDbDefiner();
   Connection conn = null;
   try{
     conn = getConnectionFactory().getConnection();
     if(dbv.tableExists(conn,null,"t_str")){
       dbv.dropTable(conn,null,"t_str");
     }
     Statement stat = conn.createStatement();
     try{
       stat.execute("create table t_str (nstr_ varchar2(10),nstr2_ nvarchar2(10))");
     }finally{
       stat.close();
     }
     assertEquals(true, dbv.tableExists(conn,null,"t_str"));
     Dialect dl = SqlFunc.createDialect(conn);
     TableMetaData tmd = dl.createDbMetaData().getTableMetaData("t_str");
     for(int i=0;i<tmd.getColumnCount();i++){
       String coli = tmd.getColumnName(i);
       if(coli.equalsIgnoreCase("nstr_")){
         int nt = tmd.getColumnType(i);
         char ct = SqlFunc.getType(nt);
         assertEquals('C',ct);
       }
       if(coli.equalsIgnoreCase("nstr2_")){
         int nt = tmd.getColumnType(i);
         char ct = SqlFunc.getType(nt);
         assertEquals('C',ct);
       }
     }
     dbv.dropTable(conn,null,"t_str");
   }finally{
     if(conn!=null)
       conn.close();
   }
 }
 
 /**
  * 测试Oracle同义词，
 * @throws Exception 
  */
 public void testOracleSYNONYM() throws Exception{
   createTable();
   addData(10);
   ConnectionFactory conf = getConnectionFactory();
   DbDefiner dbfv = conf.getDbDefiner();
   Connection conn = conf.getConnection();
   try{
     assertEquals(true,dbfv.tableOrViewExists(conn, tablename));
     //已存在tablename的表,对其判断是否是视图
     assertEquals(false,dbfv.viewExists(conn, tablename));
     
     //创建一个同义词
     String sname = "sy_"+tablename;
     dropSYNONYM(conn,sname);
     Statement stat = conn.createStatement();
     try{
       stat.execute("create SYNONYM  "+sname+" for "+tablename);
     }finally{
       stat.close();
     }
     
     Dialect dl = SqlFunc.createDialect(conn);
     TableMetaData vtmd = dl.createDbMetaData().getTableMetaData(sname);
     assertEquals(20,vtmd.getColumns().length);
     assertEquals(null,vtmd.getIndexes());
     assertEquals(null,vtmd.getPrimaryKey());
     
     //删除同义词
     dropSYNONYM(conn, sname);
   }finally{
     if(conn!=null) conn.close();
   }
   dropTable(tablename);
 }

private void dropSYNONYM(Connection conn, String sname) throws SQLException {
   Statement stat = conn.createStatement();
   try{
     stat.execute("drop SYNONYM "+sname);
   }catch(SQLException se){
   }finally{
     stat.close();
   }
}

  public void testformatFieldName() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    assertEquals("ABC123_",dbv.formatFieldName("abc123_", false));
    assertEquals("FABC123_",dbv.formatFieldName("2abc123_", false));
    assertEquals("ABC$123_",dbv.formatFieldName("abc$*123,_", false));
    assertEquals("ABC123_1242434234AAAAAAAAAAAAA",dbv.formatFieldName("abc123_1242434234aaaaaaaaaaaaaaaa123", false));
    assertEquals("SELECT",dbv.formatFieldName("select", false));
    
    try{
      assertEquals("ABC123_",dbv.formatFieldName("abc123_", true));
    }catch(Exception ex){}
    
    try{
      dbv.formatFieldName("2abc123_", true);
    }catch(Exception ex){
      assertEquals(ex.getMessage(),"字段名：2abc123_不合法；");
    }
    
    try{
      dbv.formatFieldName("abc$*123,_", true);
    }catch(Exception ex){
      assertEquals(ex.getMessage(),"字段名：abc$*123,_不合法；");
    }
    
    try{
      dbv.formatFieldName("abc123_1242434234aaaaaaaaaaaaaaaa123", true);
    }catch(Exception ex){
      assertEquals(ex.getMessage(),"字段名：abc123_1242434234aaaaaaaaaaaaaaaa123太长；");
    }
  }
  
  /**
   * 对Oracle异常：
   * ORA-01461: 仅能绑定要插入 LONG 列的 LONG 值 
   * @throws SQLException 
   */
  public void testOra01461ForVarchar() throws Exception{
    ConnectionFactory conf = getConnectionFactory();
    DbDefiner dbfv = conf.getDbDefiner();
    String tbname = "t_test_ora01461";
    
    
    /**
     * 定义4000长度的字符类型字段，插入1500长度左右的的字符串；
     * 出现ORA-01461 异常；
     */
     Connection conn = conf.getConnection();
    try{
      System.out.println(conn.getMetaData().getDatabaseProductVersion());
      if(dbfv.tableExists(conn, null, tbname)){
        dbfv.dropTable(conn, null, tbname);
      }
      dbfv.clearDefineInfo();
      dbfv.defineStringField("field1", 200, null, true, false);
      dbfv.defineStringField("field2",4000, null, true, false);
      dbfv.defineStringField("field3", 200, null, true, false);
      dbfv.createTable(conn, null, tbname);
      
      /**
       * 经测试，Oracle对于超过1000的varchar2类型，在写入[1001,2000]长度的字符串时，可能出现：
       * ORA-01461：仅能绑定要插入 LONG 列的 LONG 值；
       * 在Oracle9i,Oracle10.1版本下，
       * 使用oracle11.2/10.1/10.2的驱动，都有这个问题：
       * 这个长度对于中文是字符长度，但是汉字占两个定义长度，如果超过了定义的长度也也会出这个异常；
       * 比如：对于定义varchar2(4000)的字段，写入不能等于或者超过2000个汉字，否则也出现ORA-01461异常；
       * 
       * 在Oracle10.2版本下，
       * 使用oracle10.2驱动，没有出现出现ORA-01461异常；
       * 
       */
      String longstr = "123";
      for(int i=0;i<180;i++){
        longstr+="湖北武汉市abcde";
      }
      PreparedStatement pstat = conn.prepareStatement("insert into "+tbname+" (field1,field2,field3)values(?,?,?)");
      try{
      pstat.setString(1, "001");
      pstat.setString(2, longstr);
      pstat.setString(3, "abcd");
      pstat.executeUpdate();
      assertEquals(true,true);
      }finally{
        pstat.close();
      }
    }catch(SQLException se){
      /**
       * ORA-01461: 仅能绑定要插入 LONG 列的 LONG 值
       */
      assertEquals(1461,se.getErrorCode());
      fail(conn.getMetaData().getDatabaseProductVersion()+"出现ORA-01461异常；");
    }
    finally{
      conn.close();
    }
  }
  
  public void testOra01461ForClob() throws Exception{
    ConnectionFactory conf = getConnectionFactory();
    DbDefiner dbfv = conf.getDbDefiner();
    String tbname = "t_test_ora01461";
    Connection conn = conf.getConnection();
    try{
      System.out.println(conn.getMetaData().getDatabaseProductVersion());
      if(dbfv.tableExists(conn, null, tbname)){
        dbfv.dropTable(conn, null, tbname);
      }
      dbfv.clearDefineInfo();
      dbfv.defineStringField("field1", 200, null, true, false);
      dbfv.defineClobField("field2", null, true, false);
      dbfv.defineStringField("field3", 200, null, true, false);
      dbfv.createTable(conn, null, tbname);
      
      /**
       * 使用clob字段，除了没有长度的限制,其他情况和字符类型一样；
       * 
       * 
       */
      String longstr = "12";
      for(int i=0;i<170;i++){
        longstr+="湖北武汉市54321";
      }
      PreparedStatement pstat = conn.prepareStatement("insert into "+tbname+" (field1,field2,field3)values(?,?,?)");
      try{
      pstat.setString(1, "001");
      pstat.setString(2, longstr);
      pstat.setString(3, "abcd");
      pstat.executeUpdate();
      assertEquals(true,true);
      }finally{
        pstat.close();
      }
    }catch(SQLException se){
      /**
       * ORA-01461: 仅能绑定要插入 LONG 列的 LONG 值
       */
      assertEquals(1461,se.getErrorCode());
      fail(conn.getMetaData().getDatabaseProductVersion()+"出现ORA-01461异常；");
    }
    finally{
      conn.close();
    }
  }

}
