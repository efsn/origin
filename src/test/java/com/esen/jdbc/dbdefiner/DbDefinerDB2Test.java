package com.esen.jdbc.dbdefiner;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.Dialect;

public class DbDefinerDB2Test extends DbDefinerTest {

/*  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.ibm.db2.jcc.DB2Driver",
        "jdbc:db2://192.168.1.51:50000/testdb",//8.1
        "testcase", "testcase","debug");
  } */
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.ibm.db2.jcc.DB2Driver",
        "jdbc:db2://192.168.1.212:50000/dbsvr",//9.5.2
        "db2admin", "db2admin","debug","dw");
  }
  /*public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.ibm.db2.jcc.DB2Driver",
        "jdbc:db2://192.168.1.92:50000/centerdb",
        "zkr", "zkr","debug");
  } */
  
  /**
   * 测试更改表名，更改视图名；
   */
  public void testRenameTable() throws Exception{
    DbDefiner dbf = getConnectionFactory().getDbDefiner();
    Connection conn = getConnectionFactory().getConnection();
    try{
      String tbname = "t_db";
      String newname = "t_db2";
      String vname = "v_t_db";
      String vname2 = "v_t_db2";
      if(dbf.viewExists(conn, vname)){
        dbf.dropView(conn, vname);
      }
      if(dbf.viewExists(conn, vname2)){
        dbf.dropView(conn, vname2);
      }
      if(dbf.tableExists(conn, null, newname)){
        dbf.dropTable(conn, null, newname);
      }
      if(dbf.tableExists(conn, null, tbname)){
        dbf.dropTable(conn, null, tbname);
      }
      dbf.defineStringField("id_", 20, null, false, false);
      dbf.createTable(conn, null, tbname);
      
      dbf.createView(conn, "v_t_db", null, "select * from "+tbname);
      
      dbf.renameTable(conn, tbname, newname);
      
      dbf.renameTable(conn, vname, vname2);
    }finally{
      conn.close();
    }
  }

  
  
  /**
   * 20091020
   * 测试读取用户的所有表和视图；
   * 测试能否将物化视图列出来；
   */
  public void testTableList() throws Exception{
    ConnectionFactory conf = getConnectionFactory();
    Dialect dl = conf.getDialect();
    DbDefiner dbf = conf.getDbDefiner();
    //创建物化视图
    Connection conn = conf.getConnection();
    try{
      //删除物化视图mqt_emp
      if(dbf.tableExists(conn, null, "mqt_emp")){
        dbf.dropTable(conn, null, "mqt_emp");
      }
      //删除mqt_emp使用的表t_emp
      if(dbf.tableExists(conn, null, "t_emp")){
        dbf.dropTable(conn, null, "t_emp");
      }
      if(dbf.viewExists(conn, "v_emp")){
        dbf.dropView(conn, "v_emp");
      }
      
      Statement stat = conn.createStatement();
      stat.execute("create table t_emp (" +
      		"empno varchar(10) unique not null," +
      		"empname varchar(20)," +
      		"address varchar(100))");
      stat.execute("insert into t_emp (empno,empname,address)values('001','张三','武汉')");
      stat.execute("insert into t_emp (empno,empname,address)values('002','李四','北京')");
      stat.execute("insert into t_emp (empno,empname,address)values('003','Tom','New York')");
      assertEquals(true, dbf.tableExists(conn, null, "t_emp"));
      
      //创建物化视图
      stat.execute("create table mqt_emp as (select e.empno,e.empname,e.address  from t_emp e)"
                      +" data initially deferred refresh immediate");
      stat.execute("set integrity for mqt_emp immediate checked not incremental");
      
      assertEquals(true, dbf.tableExists(conn, null, "mqt_emp"));
      
      stat.execute("create view v_emp as select * from t_emp");
      
      assertEquals(true, dbf.viewExists(conn, "v_emp"));
      
      stat.close();
    }finally{
      conn.close();
    }
    
    //检查表列表中是否有mqt_emp和t_emp
    DbMetaData dbmd = dl.createDbMetaData();
    List l = dbmd.getTableNames();
    assertEquals(true,checkTableName(l,"t_emp"));
    assertEquals(true,checkTableName(l,"mqt_emp"));
    //检查视图列表中是否有v_emp
    List vl = dbmd.getViewNames();
    assertEquals(true,checkTableName(vl,"v_emp"));
  }
  
  private boolean checkTableName(List l, String tbname) {
    for(int i=0;i<l.size();i++){
      String tn = (String)l.get(i);
      if(tn.equalsIgnoreCase(tbname)){
        return true;
      }
    }
    return false;
  }
  public void testformatFieldName() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    assertEquals("ABC123_",dbv.formatFieldName("abc123_", false));
    assertEquals("FABC123_",dbv.formatFieldName("2abc123_", false));
    assertEquals("ABC$123_",dbv.formatFieldName("abc$*123,_", false));
    DataBaseInfo dbinf = getConnectionFactory().getDbType();
    if(dbinf.isDb29()){
      assertEquals("ABC123_1242434234AAAAAAAAAAAAAAAA123",dbv.formatFieldName("abc123_1242434234aaaaaaaaaaaaaaaa123", false));
    }else{
      assertEquals("ABC123_1242434234AAAAAAAAAAAAA",dbv.formatFieldName("abc123_1242434234aaaaaaaaaaaaaaaa123", false));
    }
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
  /*public void testDropColumn() throws Exception{
    DataBaseInfo dbinf = getConnectionFactory().getDbType();
    if(dbinf.isDb29()){
      testDropColumnFor9();
    }
  }
  private void testDropColumnFor9() throws Exception{
    createTable();
    addData(10);
    ConnectionFactory conf = getConnectionFactory();
    DbDefiner dbfv = conf.getDbDefiner();
    Connection conn = conf.getConnection();
    conn.setAutoCommit(true);
    try{
      dbfv.dropColumn(conn, tablename, "LOGIC_");
      assertEquals(false, dbfv.colExists(conn, tablename, "LOGIC_"));
      

      dbfv.dropColumn(conn, tablename, "NUM_");
      assertEquals(false, dbfv.colExists(conn, tablename, "NUM_"));
      
      dbfv.dropColumn(conn, tablename, "TIME_");
      assertEquals(false, dbfv.colExists(conn, tablename, "TIME_"));
      
      //对一张表删除3个字段，后报：表 "JLL.T_TEST2" 所处的状态对该操作无效。原因码 = "23"。的异常；
      //原因不明；
      dbfv.dropColumn(conn, tablename, "TIMESTAMP_");
      assertEquals(false, dbfv.colExists(conn, tablename, "TIMESTAMP_"));
      
    }catch(Exception ex){
      conn.rollback();
      throw ex;
    }
    finally{
      if(conn!=null) conn.close();
    }
    dropTable(tablename);
  }*/
  
  /**
   * db2 不支持这种selelct ?,? from tbname where field=?的预处理语句
   */
  public void testSetValueForPrepareStatement() throws Exception{
	  
  }
}