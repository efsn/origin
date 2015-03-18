package com.esen.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.SqlExecuter;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.TableColumnMetaData;

import junit.framework.TestCase;

public class SqlExcuterTest extends TestCase {

  public void testExcuteSql() throws Exception {
    SimpleConnectionFactory conf = new SimpleConnectionFactory("oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.200:1521:dbdev1", "test", "test", "debug");
    try {
      String tbname = "t_test";
      DbDefiner def = conf.getDbDefiner();
      Connection conn = conf.getConnection();
      try {
        if (def.tableExists(conn, null, tbname)) {
          def.dropTable(conn, null, tbname);
        }
      }
      finally {
        conn.close();
      }
      SqlExecuter sqlExe = SqlExecuter.getInstance(conf);
      try {
        //测试建表
        sqlExe.executeSql("create table " + tbname + " (id_ varchar2(20),name_ varchar2(20))");
        //测试insert into
        sqlExe.executeSql("insert into " + tbname + " (id_,name_)values('001','a')");
        sqlExe.executeSql("insert into " + tbname + " (id_,name_)values('002','b')");
        sqlExe.executeSql("insert into " + tbname + " (id_,name_)values('003','c')");

        //测试sql查询；
        ResultSet rs = (ResultSet) sqlExe.executeSql("select id_,name_ from " + tbname + " order by id_");
        int cnt = rs.getMetaData().getColumnCount();
        assertEquals(2, cnt);
        assertEquals(true, rs.next());
        assertEquals("001", rs.getString(1));
        assertEquals("a", rs.getString(2));
        assertEquals(true, rs.next());
        assertEquals("002", rs.getString(1));
        assertEquals("b", rs.getString(2));
        assertEquals(true, rs.next());
        assertEquals("003", rs.getString(1));
        assertEquals("c", rs.getString(2));
        assertEquals(false, rs.next());
        rs.close();
        
        //测试分页；
        rs = (ResultSet) sqlExe.executeSql("select id_,name_ from " + tbname + " order by id_",0,2);
        assertEquals(true, rs.next());
        assertEquals("001", rs.getString(1));
        assertEquals("a", rs.getString(2));
        assertEquals(true, rs.next());
        assertEquals("002", rs.getString(1));
        assertEquals(false, rs.next());
        
        rs = (ResultSet) sqlExe.executeSql("select id_,name_ from " + tbname + " order by id_",2,2);
        assertEquals(true, rs.next());
        assertEquals("003", rs.getString(1));
        assertEquals("c", rs.getString(2));
        assertEquals(false, rs.next());
        
        //测试update
        Object o2 = sqlExe.executeSql("update " + tbname + " set name_='aa' where id_='001'");
        Integer i2 = (Integer) o2;
        assertEquals(1, i2.intValue());

        //测试alter
        sqlExe.executeSql("alter table " + tbname + " modify id_ varchar2(100)");
        TableColumnMetaData col = conf.getDialect().getTableColumnMetaData(sqlExe.getConnection(),tbname, "id_");
        assertEquals(100, col.getLen());

        //创建存储过程
        sqlExe.executeSql("CREATE OR REPLACE PROCEDURE TEST_PPP as\n begin\ninsert into " + tbname + " (id_,name_)values('004','d');\ncommit;\nend;");
        //调用存储过程
        sqlExe.executeSql("{call TEST_PPP}");
        //测试存储过程结果
        ResultSet rs2 = (ResultSet) sqlExe.executeSql("select id_,name_ from " + tbname + " where id_='004'");
        assertEquals(true,rs2.next());
        assertEquals("004", rs2.getString(1));
        assertEquals("d", rs2.getString(2));

        //删除存储过程；
        sqlExe.executeSql("DROP PROCEDURE TEST_PPP");
        
        //删除测试表
        sqlExe.executeSql("drop table " + tbname);
      }
      finally {
        sqlExe.close();
      }
      Connection conn2 = conf.getConnection();
      try {
        assertEquals(false, def.tableExists(conn2, null, tbname));
      }
      finally {
        conn2.close();
      }

    }
    finally {
      conf.close();
    }
  }
  
  /**
   * 测试执行存储过程；
   * 测试可能出现的ORA-04068: 已丢弃程序包  的当前状态
   * @throws Exception
   */
  public void testExcuteSqlForProcedure() throws Exception{
    SimpleConnectionFactory oracleconf = new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.200:1521:dbdev1",
        "test", "test","debug");
    try{
    //建表用于存储过程测试
    createTableForProcedure(oracleconf);
    //创建存储过程包
    createPkg(oracleconf);
    //创建存储过程包体
    createpkgbody(oracleconf);
    //使用创建存储过程的连接，执行
    excuteProcedure(oracleconf,"{call pkg_test1.test_proc}");
    
    String pkgbody = "CREATE OR REPLACE PACKAGE BODY PKG_TEST1 IS\n"
      +"GV_ID VARCHAR2(12);\n"
      +"PROCEDURE TEST_PROC as\n"
      +"BEGIN\n"
      +"--Dbms_output.Put_line('hello world!');\n"
      +"--insert into t_for_proc (id_)values('aa');\n"
      +"insert into t_for_proc (id_)values('bb');\n"
      +"commit;\n"
      +"END test_proc;\n"
      +"END PKG_TEST1;";
    //新建连接，修改存储过程；
    modifypkgbody(oracleconf,pkgbody);
    
    //使用创建存储过程的连接，执行
    excuteProcedure2(oracleconf,"{call pkg_test1.test_proc}");
    
    String pkgbody2 = "CREATE OR REPLACE PACKAGE BODY PKG_TEST1 IS\n"
      +"GV_ID VARCHAR2(12);\n"
      +"PROCEDURE TEST_PROC as\n"
      +"BEGIN\n"
      +"--Dbms_output.Put_line('hello world!');\n"
      +"insert into t_for_proc (id_)values('aa');\n"
      +"insert into t_for_proc (id_)values('bb');\n"
      +"commit;\n"
      +"END test_proc;\n"
      +"END PKG_TEST1;";
    //再次修改存储过程
    modifypkgbody(oracleconf,pkgbody2);
    
    //新建连接执行
    SqlExecuter.getInstance(oracleconf).executeSql("{call pkg_test1.test_proc}");
    
    }finally{
      oracleconf.close();
    }
  }
  
  private void excuteProcedure(SimpleConnectionFactory oracleconf,String sql) throws SQLException  {
    Connection conn = oracleconf.getConnection();
    try{
       callProcedure(sql, conn);
    }
    finally{
      if(conn!=null){
        conn.close();
      }
    }
    
  }
  private void excuteProcedure2(SimpleConnectionFactory oracleconf,String sql) throws SQLException  {
    Connection conn = oracleconf.getConnection();
    try{
       callProcedure(sql, conn);
       fail("此方法应该抛出：ORA-04068: 已丢弃程序包  的当前状态");
    }
    catch (SQLException e) {
      e.getErrorCode();
      if(e.getSQLState().equals("72000")
          && e.getErrorCode()==4068){
        //ORA-04068: 已丢弃程序包  的当前状态
        
      }else{
        throw e;
      }
    }
    finally{
      if(conn!=null){
        conn.close();
      }
    }
    
  }
  
  private static Object callProcedure(String sql, Connection conn) throws SQLException {
    CallableStatement cstat = conn.prepareCall(sql);
    try{
      boolean f = cstat.execute();
      return Boolean.valueOf(f);
    }finally{
      if(cstat!=null)
        cstat.close();
    }
  }

  private void modifypkgbody(ConnectionFactory conf, String pkgbody) throws SQLException {
    Connection conn = conf.getNewConnection();
    try {
      Statement stat = conn.createStatement();
      stat.executeUpdate(pkgbody);
      stat.close();
    }
    finally {
      if (conn != null) {
        conn.close();
      }
    }
  }
  
  private void createpkgbody(ConnectionFactory conf) throws SQLException {
    String pkgbody = "CREATE OR REPLACE PACKAGE BODY PKG_TEST1 IS\n"
      +"GV_ID VARCHAR2(12);\n"
      +"PROCEDURE TEST_PROC as\n"
      +"BEGIN\n"
      +"Dbms_output.Put_line('hello world!');\n"
      +"insert into t_for_proc (id_)values('aa');\n"
      +"--insert into t_for_proc (id_)values('bb');\n"
      +"commit;\n"
      +"END test_proc;\n"
      +"END PKG_TEST1;";
    executeSql(conf, pkgbody);
    
  }


  private void createPkg(ConnectionFactory conf) throws SQLException {
    String pkg = "CREATE OR REPLACE PACKAGE PKG_TEST1 IS\n PROCEDURE TEST_PROC;\n END PKG_TEST1;";
    executeSql(conf, pkg);

  }


  private void createTableForProcedure(ConnectionFactory conf) throws Exception {
    DbDefiner def = conf.getDbDefiner();
    Connection conn = conf.getConnection();
    try {
      if(def.tableExists(conn, null, "t_for_proc")){
        def.dropTable(conn, null, "t_for_proc");
      }
      def.defineStringField("id_", 20, null, true, false);
      def.createTable(conn, null, "t_for_proc");
    }
    finally {
      conn.close();
    }

  }
  
  private void executeSql(ConnectionFactory conf,String sql) throws SQLException{
    Connection conn = conf.getConnection();
    try{
      Statement stat = conn.createStatement();
      stat.execute(sql);
      stat.close();
    }finally{
      conn.close();
    }
  }
}
