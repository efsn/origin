package com.esen.jdbc.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;

import junit.framework.TestCase;

import com.esen.jdbc.ConnectFactoryManagerImpl;
import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.DataCopy;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.util.StmFunc;

public abstract class DataCopyTest extends TestCase {
  
  public abstract ConnectionFactory createConnectionFactory();
  
  private ConnectionFactory dbf = null;
  public ConnectionFactory getConnectionFactory(){
    if (dbf==null){
      dbf = createConnectionFactory();
    }
    return dbf;
  }
  protected void tearDown() throws java.lang.Exception{
    if(dbf!=null){
      SimpleConnectionFactory sf = (SimpleConnectionFactory)dbf;
      sf.close();
    }
  }
  public ConnectionFactory getConnectionFactoryOracle(){
    return new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.223:1521:testdb",
        "testcase", "testcase","debug");
  }
  public ConnectionFactory getConnectionFactoryDB2(){
    return new SimpleConnectionFactory(
        "com.ibm.db2.jcc.DB2Driver",
        "jdbc:db2://192.168.1.222:50000/testdb",//9.5.2
        "testcase", "testcase","debug");
  }
  public ConnectionFactory getConnectionFactorySybase12(){
    return new SimpleConnectionFactory(
        "com.sybase.jdbc3.jdbc.SybDriver",
        "jdbc:sybase:Tds:192.168.1.222:5000/testdb?charset=cp936",
        "testcase", "testcase","debug");
  }

  public ConnectionFactory getConnectionFactoryMysql5(){
    return new SimpleConnectionFactory(
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://192.168.1.223/testdb?useUnicode=true&characterEncoding=utf8",
        "testcase", "testcase","debug");
  }
  public ConnectionFactory getConnectionFactoryMssql2000(){
    return new SimpleConnectionFactory(
        "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        "jdbc:sqlserver://192.168.1.223:1433;databaseName=testdb;",
        "testcase", "testcase","debug");
  }
  
  public ConnectionFactory getConnectionFactoryMssql2005(){
    return new SimpleConnectionFactory(
        "net.sourceforge.jtds.jdbc.Driver",
        "jdbc:jtds:sqlserver://192.168.1.222:1433/testdb",
        "testcase", "testcase","debug");
  }

 public void testExportImportInOwnDb() throws Exception {
    testExportImport("default_pool",getConnectionFactory());
  }
   /*public void testExportImportToOracle() throws Exception {
    testExportImport("oracle",getConnectionFactoryOracle());
  }*/
   /* public void testExportImportToSybase() throws Exception {
    testExportImport("sybase12",getConnectionFactorySybase12());
  }*/
  /*public void testExportImportToMssql2000() throws Exception {
    testExportImport("mssql2000",getConnectionFactoryMssql2000());
  }*/
  /*public void testExportImportToMssql2005() throws Exception {
    testExportImport("mssql2000",getConnectionFactoryMssql2005());
  }*/
  /*public void testExportImportToMysql5() throws Exception {
    testExportImport("mysql5",getConnectionFactoryMysql5());
  }
  public void testExportImportToDB2() throws Exception {
    testExportImport("db2",getConnectionFactoryDB2());
  }*/
  
  private void testExportImport(String pname,ConnectionFactory conf) throws Exception {
    ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
    fm.setConnectionFactory("default_pool", getConnectionFactory());
    if(!pname.equals("default_pool"))
      fm.setConnectionFactory(pname, conf);
    DefaultConnectionFactory.set(fm);
    //导入时创建新表；
    testExpImpTable(pname,DataCopy.OPT_CREATENEWTABLE);
    //导入前覆盖
    testExpImpTable(pname,DataCopy.OPT_OVERWRITER);
    //导入前清空
    testExpImpTable(pname,DataCopy.OPT_CLEARTABLE);
    
    //追加记录，源表与目的表，表结构一致；
    testExpImpTableAppend(pname,0,null,null);//源表没有主键
    testExpImpTableAppend(pname,1,null,null);//源表有主键
    testExpImpTableAppend(pname,2,null,null);//源表有唯一索引
    
    //追加记录，源表与目的表，表结构不一致；
    String[] srcfields = {"ZB3","ZB4"};
    String[] destfields = {"ZB5","ZB6"};
    testExpImpTableAppend(pname,0,srcfields,destfields);//源表没有主键
    testExpImpTableAppend(pname,1,srcfields,destfields);//源表有主键
    testExpImpTableAppend(pname,2,srcfields,destfields);//源表有唯一索引
    
    //增量更新记录，源表与目的表，表结构一致；
    testExpImpTableUpdate(pname,0,null,null);//源表没有主键
    testExpImpTableUpdate(pname,1,null,null);//源表有主键
    testExpImpTableUpdate(pname,2,null,null);//源表有唯一索引
    
    
    //增量更新记录，源表与目的表，表结构不一致；
    testExpImpTableUpdate(pname,0,srcfields,destfields);//源表没有主键
    testExpImpTableUpdate(pname,1,srcfields,destfields);//源表有主键
    testExpImpTableUpdate(pname,2,srcfields,destfields);//源表有唯一索引
    
  }
  
  private void testExpImpTableUpdate(String pname,int t,String[] srcfields,String[] destfields) throws Exception {
  //准备数据
    createTable("default_pool","t_data",t,srcfields);
    writeData2("default_pool","t_data",srcfields);
    //导出数据
    try {
      File f = File.createTempFile("test", "db");
      try {
        //导出
        BufferedOutputStream out = new BufferedOutputStream(
            new FileOutputStream(f));
        try {
          DataCopy.createInstance().exportData("default_pool", "t_data", out);
        }
        finally {
          out.close();
        }
        //导入没有主键的表；
        importDataNoKey(pname, f,DataCopy.OPT_UPDATE,destfields);
        //导入有主键的表；
        importDataUpdateHaveKey(pname, f,destfields);
        //导入有唯一约束索引的表；
        importDataUpdateHaveIndex(pname, f,destfields);
        if(t>0){//源表有主键或者唯一索引
          //主键不一致，出异常
          importDataHaveOtherKey(pname, f,DataCopy.OPT_UPDATE,destfields);
          //索引不一致，出异常
          importDataHaveOtherIndex(pname, f,DataCopy.OPT_UPDATE,destfields);
        }
      }finally{
        f.delete();
      }
    }finally{
      dropTable("default_pool","t_data");
    }
    
  }

  private void importDataUpdateHaveIndex(String pname, File f,String[] otherfields) throws Exception {
    //在目的池建结构相同的表：
    createTable(pname,"t_data2",2,otherfields);//有唯一索引
    writeData(pname,"t_data2",otherfields);//三条记录，有两条与源表的主键相同
    try{
      //导入刚建的表；
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
      try {
        DataCopy.createInstance().importData(in, pname,
            "t_data2", DataCopy.OPT_UPDATE);
      }finally {
        in.close();
      }
      //由于目的表有主键，主键冲突的记录需要修改，写入后应该有4条记录；
      int n = getCount(pname,"t_data2");
      assertEquals(4, n);
      //检查数据，交集数据应该不变，增加了一条记录
      checkUpdateData(pname,"t_data2",otherfields);
    }finally{
      dropTable(pname,"t_data2");
    }
    
    
  }
  private void importDataUpdateHaveKey(String pname, File f, String[] otherfields) throws Exception {
    //在目的池建结构相同的表：
    createTable(pname,"t_data2",1,otherfields);//有主键
    writeData(pname,"t_data2",otherfields);//三条记录，有两条与源表的主键相同
    try{
      //导入刚建的表；
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
      try {
        DataCopy.createInstance().importData(in, pname,
            "t_data2", DataCopy.OPT_UPDATE);
      }finally {
        in.close();
      }
      //由于目的表有主键，主键冲突的记录需要修改，写入后应该有4条记录；
      int n = getCount(pname,"t_data2");
      assertEquals(4, n);
      //检查数据，交集数据应该不变，增加了一条记录
      checkUpdateData(pname,"t_data2",otherfields);
    }finally{
      dropTable(pname,"t_data2");
    }
    
  }
  private void checkUpdateData(String poolname, String tbname, String[] otherfields) throws Exception {
    ConnectionFactory conf2 = DefaultConnectionFactory.get(poolname, true);
    Connection conn = conf2.getConnection();
    try{
      Statement stat = conn.createStatement();
      StringBuffer sql = new StringBuffer(64);
      sql.append("select USERID_,BTYPE_,BBQ_,ZB1,ZB2");
      if(otherfields!=null){
        for(int i=0;i<otherfields.length;i++){
          sql.append(",").append(otherfields[i]);
        }
      }
      sql.append(" from ");
      sql.append(tbname).append(" order by USERID_");
      ResultSet rs = stat.executeQuery(sql.toString());
      rs.next();
      assertEquals("A100",rs.getString(1));
      assertEquals("0",rs.getString(2));
      assertEquals("200801--",rs.getString(3));
      assertEquals(123.45,rs.getDouble(4),0);
      assertEquals(7457.32,rs.getDouble(5),0);
      if(otherfields!=null){
        for(int i=0;i<otherfields.length;i++){
          assertEquals("A1_"+i,rs.getString(6+i));
        }
      }
      
      rs.next();
      assertEquals("A200",rs.getString(1));
      assertEquals("0",rs.getString(2));
      assertEquals("200801--",rs.getString(3));
      assertEquals(1233.45,rs.getDouble(4),0);
      assertEquals(757.2,rs.getDouble(5),0);
      if(otherfields!=null){
        for(int i=0;i<otherfields.length;i++){
          assertEquals("A2_"+i,rs.getString(6+i));
        }
      }
      
      rs.next();
      assertEquals("A300",rs.getString(1));
      assertEquals("0",rs.getString(2));
      assertEquals("200801--",rs.getString(3));
      assertEquals(12322.5,rs.getDouble(4),0);
      assertEquals(45373.2,rs.getDouble(5),0);
      if(otherfields!=null){
        for(int i=0;i<otherfields.length;i++){
          assertEquals("A3_"+i,rs.getString(6+i));
        }
      }
      
      rs.next();
      assertEquals("A400",rs.getString(1));
      assertEquals("0",rs.getString(2));
      assertEquals("200801--",rs.getString(3));
      assertEquals(1167.42,rs.getDouble(4),0);
      assertEquals(645.43,rs.getDouble(5),0);
      if(otherfields!=null){
        for(int i=0;i<otherfields.length;i++){
          assertEquals(null,rs.getString(6+i));
        }
      }
      
      rs.close();
      stat.close();
    }finally{
      conn.close();
    }
    
  }
  private void testExpImpTableAppend(String pname,int t,String[] srcfields,String[] destfields) throws Exception {
    //准备数据
    createTable("default_pool","t_data",t,srcfields);
    writeData2("default_pool","t_data",srcfields);
    //导出数据
    try {
      File f = File.createTempFile("test", "db");
      try {
        //导出
        BufferedOutputStream out = new BufferedOutputStream(
            new FileOutputStream(f));
        try {
          DataCopy.createInstance().exportData("default_pool", "t_data", out);
        }
        finally {
          out.close();
        }
        //导入没有主键的表；
        importDataNoKey(pname, f,DataCopy.OPT_APPEND,destfields);
        //导入有主键的表；
        importDataHaveKey(pname, f,destfields);
        //导入有唯一约束索引的表；
        importDataHaveIndex(pname, f,destfields);
        if(t>0){//源表有主键或者唯一索引
          //主键不一致，出异常
          importDataHaveOtherKey(pname, f,DataCopy.OPT_APPEND,destfields);
          //索引不一致，出异常
          importDataHaveOtherIndex(pname, f,DataCopy.OPT_APPEND,destfields);
        }
      }finally{
        f.delete();
      }
    }finally{
      dropTable("default_pool","t_data");
    }
  }
  private void importDataHaveOtherIndex(String pname, File f, int p, String[] otherfields) throws Exception {
    //在目的池建结构相同的表：
    createTable(pname,"t_data2",4,otherfields);//其他唯一索引
    writeData(pname,"t_data2",otherfields);//三条记录，有两条与源表的主键相同
    try{
      //导入刚建的表；
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
      try {
        DataCopy.createInstance().importData(in, pname,
            "t_data2", p);
        assertEquals(false,true);//应该出异常，不会到这里；
      }catch(Exception ex){
        assertEquals("目的表主键和源表主键不一致；",ex.getMessage());
      }finally {
        in.close();
      }
    }finally{
      dropTable(pname,"t_data2");
    }
    
  }
  private void importDataHaveOtherKey(String pname, File f, int p,String[] otherfields) throws Exception {
    //在目的池建结构相同的表：
    createTable(pname,"t_data2",3,otherfields);//其他主键
    writeData(pname,"t_data2",otherfields);//三条记录，有两条与源表的主键相同
    try{
      //导入刚建的表；
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
      try {
        DataCopy.createInstance().importData(in, pname,
            "t_data2", p);
        assertEquals(false,true);//应该出异常，不会到这里；
      }catch(Exception ex){
        assertEquals("目的表主键和源表主键不一致；",ex.getMessage());
      }finally {
        in.close();
      }
    }finally{
      dropTable(pname,"t_data2");
    }
    
  }
  private void importDataHaveIndex(String pname, File f,String[] otherfields) throws Exception {
    //在目的池建结构相同的表：
    createTable(pname,"t_data2",2,otherfields);//有唯一索引
    writeData(pname,"t_data2",otherfields);//三条记录，有两条与源表的主键相同
    try{
      //导入刚建的表；
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
      try {
        DataCopy.createInstance().importData(in, pname,
            "t_data2", DataCopy.OPT_APPEND);
      }finally {
        in.close();
      }
      //由于目的表有主键，忽略主键冲突的记录，写入后应该有4条记录；
      int n = getCount(pname,"t_data2");
      assertEquals(4, n);
      //检查数据，交集数据应该不变，增加了一条记录
      checkAppendData(pname,"t_data2",otherfields);
    }finally{
      dropTable(pname,"t_data2");
    }
  }
  private void importDataHaveKey(String pname, File f,String[] otherfields) throws Exception {
    //在目的池建结构相同的表：
    createTable(pname,"t_data2",1,otherfields);//有主键
    writeData(pname,"t_data2",otherfields);//三条记录，有两条与源表的主键相同
    try{
      //导入刚建的表；
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
      try {
        DataCopy.createInstance().importData(in, pname,
            "t_data2", DataCopy.OPT_APPEND);
      }finally {
        in.close();
      }
      //由于目的表有主键，忽略主键冲突的记录，写入后应该有4条记录；
      int n = getCount(pname,"t_data2");
      assertEquals(4, n);
    //检查数据，交集数据应该不变，增加了一条记录
      checkAppendData(pname,"t_data2",otherfields);
    }finally{
      dropTable(pname,"t_data2");
    }
  }
  /**
   * 检查数据，交集数据应该不变，增加了一条记录
   * @param otherfields 
   * @param pname
   * @param string
   * @throws Exception 
   */
  private void checkAppendData(String poolname, String tbname, String[] otherfields) throws Exception {
    ConnectionFactory conf2 = DefaultConnectionFactory.get(poolname, true);
    Connection conn = conf2.getConnection();
    try{
      Statement stat = conn.createStatement();
      StringBuffer sql = new StringBuffer(64);
      sql.append("select USERID_,BTYPE_,BBQ_,ZB1,ZB2");
      if(otherfields!=null){
        for(int i=0;i<otherfields.length;i++){
          sql.append(",").append(otherfields[i]);
        }
      }
      sql.append(" from ");
      sql.append(tbname).append(" order by USERID_");
      ResultSet rs = stat.executeQuery(sql.toString());
      rs.next();
      assertEquals("A100",rs.getString(1));
      assertEquals("0",rs.getString(2));
      assertEquals("200801--",rs.getString(3));
      assertEquals(123.45,rs.getDouble(4),0);
      assertEquals(7457.32,rs.getDouble(5),0);
      if(otherfields!=null){
        for(int i=0;i<otherfields.length;i++){
          assertEquals("A1_"+i,rs.getString(6+i));
        }
      }
      
      rs.next();
      assertEquals("A200",rs.getString(1));
      assertEquals("0",rs.getString(2));
      assertEquals("200801--",rs.getString(3));
      assertEquals(1232.5,rs.getDouble(4),0);
      assertEquals(4537.2,rs.getDouble(5),0);
      if(otherfields!=null){
        for(int i=0;i<otherfields.length;i++){
          assertEquals("A2_"+i,rs.getString(6+i));
        }
      }
      
      rs.next();
      assertEquals("A300",rs.getString(1));
      assertEquals("0",rs.getString(2));
      assertEquals("200801--",rs.getString(3));
      assertEquals(167.4,rs.getDouble(4),0);
      assertEquals(45.33,rs.getDouble(5),0);
      if(otherfields!=null){
        for(int i=0;i<otherfields.length;i++){
          assertEquals("A3_"+i,rs.getString(6+i));
        }
      }
      
      rs.next();
      assertEquals("A400",rs.getString(1));
      assertEquals("0",rs.getString(2));
      assertEquals("200801--",rs.getString(3));
      assertEquals(1167.42,rs.getDouble(4),0);
      assertEquals(645.43,rs.getDouble(5),0);
      if(otherfields!=null){
        for(int i=0;i<otherfields.length;i++){
          assertEquals(null,rs.getString(6+i));
        }
      }
      
      rs.close();
      stat.close();
    }finally{
      conn.close();
    }
    
  }
  private void importDataNoKey(String pname, File f, int p,String[] otherfields) throws Exception, FileNotFoundException, IOException {
    //在目的池建结构相同的表：
    createTable(pname,"t_data2",0,otherfields);//没有主键
    writeData(pname,"t_data2",otherfields);//三条记录，有两条与源表的主键相同
    try{
      //导入刚建的表；
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
      try {
        DataCopy.createInstance().importData(in, pname,
            "t_data2", p);
      }finally {
        in.close();
      }
      //由于目的表没有主键，则直接写入，不考虑主键冲突，写入后应该有6条记录；
      int n = getCount(pname,"t_data2");
      assertEquals(6, n);
    }finally{
      dropTable(pname,"t_data2");
    }
  }
  /**
   * 获取指定表的记录条数；
   * @param pname
   * @param tbname
   * @return
   * @throws Exception 
   */
  private int getCount(String poolname, String tbname) throws Exception {
    ConnectionFactory conf2 = DefaultConnectionFactory.get(poolname, true);
    Connection conn = conf2.getConnection();
    Statement stat =null;
    try{
      stat = conn.createStatement();
      ResultSet rs = stat.executeQuery("select count(*) from "+tbname);
      if(rs.next())
        return rs.getInt(1);
      return 0;
    }finally{
      if(stat!=null)
      stat.close();
      if(conn!=null)
      conn.close();
    }
  }
  /**
   * 创建一个有主键的表；
   * type=0  无主键和索引
   * type=1  建主键
   * type=2  建唯一索引
   * type=3  其他主键
   * @param tablename
   * @return
   * @throws Exception
   */
  private String createTable(String poolname,String tablename,int type,String[] otherfields) throws Exception {
    ConnectionFactory conf2 = DefaultConnectionFactory.get(poolname, true);
    DbDefiner dbv = conf2.getDbDefiner();
    Connection conn = null;
    try{
      conn = conf2.getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.defineStringField("USERID_",  20, null, false, false);
      dbv.defineStringField("BTYPE_",  20, null, false, false);
      dbv.defineStringField("BBQ_",  20, null, false, false);
      dbv.defineFloatField("ZB1",  20,2, null, false, false);
      dbv.defineFloatField("ZB2",  20,2, null, true, false);
      if(otherfields!=null){
        for(int i=0;i<otherfields.length;i++){
          dbv.defineStringField(otherfields[i],  20, null, true, false);
        }
      }
      if(type==0){
      }else if(type==1){
        dbv.definePrimaryKey("USERID_,BTYPE_,BBQ_");
      }else if(type==2){
        dbv.defineIndex("I" + tablename, "(USERID_,BTYPE_,BBQ_)", true);
      }else if(type==3){
        dbv.definePrimaryKey("USERID_,BTYPE_,ZB1");
      }else if(type==4){
        dbv.defineIndex("I" + tablename, "(USERID_,BTYPE_,ZB1)", true);
      }
      String tbname = dbv.createTable(conn,null,tablename);
      return tbname;
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
  /**
   * 用于写入源表
   * @param poolname
   * @param tablename
   * @param otherfields 
   * @throws Exception
   */
  private void writeData(String poolname,String tablename, String[] otherfields) throws Exception{
    ConnectionFactory conf2 = DefaultConnectionFactory.get(poolname, true);
    Connection conn = null;
    try{
      conn = conf2.getConnection();
      StringBuffer sql = new StringBuffer(64);
      sql.append("insert into ").append(tablename);
      sql.append(" (USERID_,BTYPE_,BBQ_,ZB1,ZB2");
      if(otherfields!=null){
        for(int i=0;i<otherfields.length;i++){
          sql.append(",").append(otherfields[i]);
        }
      }
      sql.append(")values(?,?,?,?,?");
      if(otherfields!=null){
        for(int i=0;i<otherfields.length;i++){
          sql.append(",?");
        }
      }
      sql.append(")");
      PreparedStatement stat = conn.prepareStatement(sql.toString());
      stat.setString(1, "A100");
      stat.setString(2, "0");
      stat.setString(3, "200801--");
      stat.setDouble(4, 123.45);
      stat.setDouble(5, 7457.32);
      if(otherfields!=null){
        for(int i=0;i<otherfields.length;i++){
          stat.setString(6+i, "A1_"+i);
        }
      }
      stat.addBatch();
      
      stat.setString(1, "A200");
      stat.setString(2, "0");
      stat.setString(3, "200801--");
      stat.setDouble(4, 1232.5);
      stat.setDouble(5, 4537.2);
      if(otherfields!=null){
        for(int i=0;i<otherfields.length;i++){
          stat.setString(6+i, "A2_"+i);
        }
      }
      stat.addBatch();
      
      stat.setString(1, "A300");
      stat.setString(2, "0");
      stat.setString(3, "200801--");
      stat.setDouble(4, 167.4);
      stat.setDouble(5, 45.33);
      if(otherfields!=null){
        for(int i=0;i<otherfields.length;i++){
          stat.setString(6+i, "A3_"+i);
        }
      }
      stat.addBatch();
      stat.executeBatch();
      stat.close();
      
      conn.commit();
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
  /**
   * 用于写入目的表
   * 三条记录，有两条与源表的主键相同
   * @param poolname
   * @param tablename
   * @param srcfields 
   * @throws Exception
   */
  private void writeData2(String poolname,String tablename, String[] srcfields) throws Exception{
    ConnectionFactory conf2 = DefaultConnectionFactory.get(poolname, true);
    Connection conn = null;
    try{
      conn = conf2.getConnection();
      StringBuffer sql = new StringBuffer(64);
      sql.append("insert into ").append(tablename);
      sql.append(" (USERID_,BTYPE_,BBQ_,ZB1,ZB2");
      if(srcfields!=null){
        for(int i=0;i<srcfields.length;i++){
          sql.append(",").append(srcfields[i]);
        }
      }
      sql.append(")values(?,?,?,?,?");
      if(srcfields!=null){
        for(int i=0;i<srcfields.length;i++){
          sql.append(",?");
        }
      }
      sql.append(")");
      PreparedStatement stat = conn.prepareStatement(sql.toString());
      stat.setString(1, "A200");
      stat.setString(2, "0");
      stat.setString(3, "200801--");
      stat.setDouble(4, 1233.45);
      stat.setDouble(5, 757.2);
      if(srcfields!=null){
        for(int i=0;i<srcfields.length;i++){
          stat.setString(6+i, "A2__"+i);
        }
      }
      stat.addBatch();
      
      stat.setString(1, "A300");
      stat.setString(2, "0");
      stat.setString(3, "200801--");
      stat.setDouble(4, 12322.5);
      stat.setDouble(5, 45373.2);
      if(srcfields!=null){
        for(int i=0;i<srcfields.length;i++){
          stat.setString(6+i, "A3__"+i);
        }
      }
      stat.addBatch();
      
      stat.setString(1, "A400");
      stat.setString(2, "0");
      stat.setString(3, "200801--");
      stat.setDouble(4, 1167.42);
      stat.setDouble(5, 645.43);
      if(srcfields!=null){
        for(int i=0;i<srcfields.length;i++){
          stat.setString(6+i, "A4__"+i);
        }
      }
      stat.addBatch();
      stat.executeBatch();
      stat.close();
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
  /**
   * 创建表，插入数据，导出数据到临时文件；
   * 从临时文件导入指定表；
   * 测试整个流程，建表时包含所有字段类型；
   * 检查导入的新表结构和源表是否一致，数据是否正确；
   * @param pname
   * @throws Exception
   * @throws IOException
   * @throws FileNotFoundException
   */
  private void testExpImpTable(String pname,int opt) throws Exception, IOException, FileNotFoundException {
    String tbname = "test_1";
    createTable(tbname);
    insertData(tbname);

    String tbname2 = null;
    try {
      File f = File.createTempFile("test", "db");
      try {
        //导出
        BufferedOutputStream out = new BufferedOutputStream(
            new FileOutputStream(f));
        try {
          DataCopy.createInstance().exportData("default_pool", tbname, out);
        }
        finally {
          out.close();
        }
        //导入
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
        try {
          tbname2 = DataCopy.createInstance().importData(in, pname,
              tbname, opt);
        }catch(Exception ex){
          ex.printStackTrace();
        }
        finally {
          in.close();
        }
      }
      finally {
        f.delete();
      }
      //比较表结构，数据
      checkTable(tbname, tbname2,pname);
    }
    finally {
      deleteTable(tbname, tbname2,pname);
    }
  }
  

/*  public void testCopyDataInOwnDb() throws Exception{
    testCopyData("default_pool",getConnectionFactory());
  }
/*  public void testCopyDataToOracle() throws Exception{
    testCopyData("oracle",getConnectionFactoryOracle());
  }
  public void testCopyDataToSybase() throws Exception{
    testCopyData("sybase12",getConnectionFactorySybase12());
  }*/
  private void testCopyData(String pname,ConnectionFactory conf) throws Exception{
    ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
    fm.setConnectionFactory("default_pool", getConnectionFactory());
    if(!pname.equals("default_pool"))
      fm.setConnectionFactory(pname, conf);
    DefaultConnectionFactory.set(fm);
    String tbname = "test_01";
    createTable(tbname);
    insertData(tbname);
    
    String tbname2 = "test_02";
    try {
      dropTable("default_pool",tbname2);
      DataCopy.createInstance().selectInto("default_pool", tbname, pname, tbname2);
      checkTable(tbname, tbname2,pname);
    }
    finally {
      deleteTable(tbname, tbname2,pname);
    }
  }
/*  public void testCopyDataFromDbf() throws Exception{
   ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
    fm.setConnectionFactory("default_pool", getConnectionFactory());
    DefaultConnectionFactory.set(fm);
    DataCopy.createInstance().importDataFromDbf("D:\\temp\\dbf\\B1.dbf", "default_pool", "test_b1", DataCopy.OPT_CREATENEWTABLE);

  }*/
  
  private void deleteTable(String tbname, String tbname2,String dpname) throws Exception {
    ConnectionFactory conf2 = DefaultConnectionFactory.get(dpname, true);
    Connection conn = getConnectionFactory().getConnection();
    try{
      DbDefiner dbv = getConnectionFactory().getDbDefiner();
      if(dbv.tableExists(conn, null, tbname))
        dbv.dropTable(conn, null, tbname);
    }finally{
      conn.close();
    }
    Connection conn2 = conf2.getConnection();
    try{
      DbDefiner dbv2 = conf2.getDbDefiner();
      if(dbv2.tableExists(conn2, null, tbname2))
        dbv2.dropTable(conn2, null, tbname2);
    }finally{
      conn2.close();
    }
  }
  private void dropTable(String poolname, String tbname2) throws Exception {
    ConnectionFactory conf2 = DefaultConnectionFactory.get(poolname, true);
    Connection conn = conf2.getConnection();
    try{
      DbDefiner dbv = conf2.getDbDefiner();
      if(dbv.tableExists(conn,null,tbname2))
        dbv.dropTable(conn, null, tbname2);
    }finally{
      conn.close();
    }
  }
  private void checkTable(String tbname, String tbname2,String dpname) throws Exception {
    ConnectionFactory conf2 = DefaultConnectionFactory.get(dpname, true);
    DbMetaData dmd = getConnectionFactory().getDbMetaData();
    DbMetaData dmd2 = conf2.getDbMetaData();
    TableMetaData tmd = dmd.getTableMetaData(tbname);
    TableMetaData tmd2 = dmd2.getTableMetaData(tbname2);
    //检查表结构
    checkTableMetaData(tmd,tmd2,conf2.getDbType());
    //检查数据；
    assertEquals(getCount("default_pool", tbname),
       getCount(dpname, tbname2));
    checkData(tbname,tbname2,conf2);
  }
  
  private void checkData(String tbname, String tbname2,ConnectionFactory conf2) throws Exception {
    Connection conn = getConnectionFactory().getConnection();
    Connection conn2 = conf2.getConnection();
    DataBaseInfo db = getConnectionFactory().getDbType();
    DataBaseInfo db2 = conf2.getDbType();
    try{
      Statement stat = conn.createStatement();
      Statement stat2 = conn2.createStatement();
      ResultSet rs = stat.executeQuery("select INT_,STR_,NUM_,DATE_,TIME_,TIMESTAMP_,BINARY_,CHARACTER_,NUM2_,BINARY2_,CHARACTER2_ from "+tbname+" order by INT_");
      ResultSet rs2 = stat2.executeQuery("select INT_,STR_,NUM_,DATE_,TIME_,TIMESTAMP_,BINARY_,CHARACTER_,NUM2_,BINARY2_,CHARACTER2_ from "+tbname2+" order by INT_");
      rs.next();
      rs2.next();
      //测试基本数据
      assertEquals(rs.getInt(1),rs2.getInt(1));
      assertEquals(rs.getString(2),rs2.getString(2));
      assertEquals(rs.getDouble(3),rs2.getDouble(3),0);
      //判断写入的日期类型字段，是否包含时间信息；

      
        long tt = Timestamp.valueOf("2008-01-01 22:25:01").getTime();
        assertEquals(tt,rs.getTimestamp(4).getTime());
        assertEquals(tt,rs2.getTimestamp(4).getTime());
     
      
      assertEquals(rs.getTime(5),rs2.getTime(5));
      assertEquals(rs.getTimestamp(6),rs2.getTimestamp(6));
      assertEquals(rs.getDouble(9),rs2.getDouble(9),0);
      
      //测试大数值
      rs.next();rs2.next();
      assertEquals("210005000004723196",rs.getString(3).substring(0,18));
      assertEquals("210005000004723196",rs2.getString(3).substring(0,18));
      assertEquals(rs.getDouble(9),rs2.getDouble(9),0.00000001);
      
      //测试小blob,clob
      rs.next();rs2.next();
      InputStream in = rs.getBinaryStream(7);
      String str = getStrValue(in);
      assertEquals("四川汶川大地震3",str);
      InputStream in2 = rs2.getBinaryStream(7);
      String str2 = getStrValue(in2);
      assertEquals(str,str2);
      
      String v = rs.getString(8);
      assertEquals("四川汶川大地震3",v);
      String v2 = rs2.getString(8);
      assertEquals(v,v2);
      
      InputStream in3 = rs.getBinaryStream(10);
      String str3 = getStrValue(in3);
      assertEquals("四川汶川大地震3333",str3);
      InputStream in4 = rs2.getBinaryStream(10);
      String str4 = getStrValue(in4);
      assertEquals(str3,str4);
      
      String v3 = rs.getString(11);
      assertEquals("四川汶川大地震33334",v3);
      String v4 = rs2.getString(11);
      assertEquals(v3,v4);
      
      assertEquals(rs.getDouble(9),rs2.getDouble(9),0);
      
      
      //测试大blob,clob
      if(rs.next()&&rs2.next()){
      checkBigBlob(rs.getBinaryStream(7),rs2.getBinaryStream(7));
      checkBigBlob(rs.getBinaryStream(10),rs2.getBinaryStream(10));
      
      String bigstr = this.getBigColgValue();
      assertEquals(bigstr,rs.getString(8));
      assertEquals(bigstr,rs2.getString(8));
      assertEquals(bigstr,rs.getString(11));
      assertEquals(bigstr,rs2.getString(11));
      
      assertEquals(rs.getDouble(9),rs2.getDouble(9),0);
      }
      stat.close();
      stat2.close();
    }finally{
      conn.close();
      conn2.close();
    }
  }
  
  private void checkBigBlob(InputStream in, InputStream in2) throws Exception {
    //比较流的长度
    int len = readInputStream(in);
    int len2 = readInputStream(in2);
    assertEquals(len,len2);
  }
  private int readInputStream(InputStream in) throws IOException,
      FileNotFoundException {
    int len = 0;
    File f = File.createTempFile("test", "db");
    try {
      BufferedOutputStream w = new BufferedOutputStream(new FileOutputStream(f));
      try {
        len = StmFunc.stmTryCopyFrom(in, w);
        w.flush();
      }
      finally {
        w.close();
      }
    }
    finally {
      f.delete();
    }
    return len;
  }
  private void checkTableMetaData(TableMetaData tmd, TableMetaData tmd2, DataBaseInfo dbinfo) {
    boolean isMysql = dbinfo.isMysql();
    String[] key = tmd.getPrimaryKey();
    String[] key2 = tmd2.getPrimaryKey();
    if(isMysql){
      /**
       * mysql的自增长字段必须是主键；
       */
      assertEquals(key[0], "STR_");
      assertEquals(key2[0], "ID_");
    }else{
      assertEquals(key[0], key2[0]);
    }
    
    assertEquals(tmd.getColumnCount(),tmd2.getColumnCount());
    
    for(int i=0;i<tmd.getColumnCount();i++){
      TableColumnMetaData coli = tmd.getColumns()[i];
      TableColumnMetaData coli2 = tmd2.getColumns()[i];
      assertEquals(coli.getName().toUpperCase(),coli2.getName().toUpperCase());
      if(SqlFunc.getType(coli.getType())=='I'||SqlFunc.getType(coli.getType())=='N'){
        
      }else
        assertEquals(SqlFunc.getType(coli.getType()),SqlFunc.getType(coli2.getType()));
      if(SqlFunc.getType(coli.getType())=='C'&&SqlFunc.getType(coli2.getType())=='C'){
        assertEquals(true,coli.getLen()<=coli2.getLen());
      }
      assertEquals(coli.isAutoInc(),coli2.isAutoInc());
      assertEquals(coli.isNullable(),coli2.isNullable());
      if(!coli.isAutoInc())//自动增长字段，可能没有设置成唯一
        assertEquals(coli.isUnique(),coli2.isUnique());
    }
    
    TableIndexMetaData[] indx = tmd.getIndexes();
    TableIndexMetaData[] indx2 = tmd2.getIndexes();
    
    if(isMysql){
      /**
       * Mysql中，ID_是自动增长字段，但是定义了STR_为主键，则STR_变成唯一索引；
       */
      assertEquals(indx.length,2);
      assertEquals(indx2.length,3);
      boolean haveStrIndex =false;
      for(int i=0;i<indx2.length;i++){
        TableIndexMetaData ix = indx2[i];
        if(ix.getName().equals("ind_auto_STR_")){
          //STR_变成唯一索引；
          assertEquals(ix.getColumns()[0],"STR_");
          assertEquals(ix.isUnique(),true);
          haveStrIndex = true;
        }
      }
      assertEquals(true,haveStrIndex);
    }else{
      assertEquals(indx.length,indx2.length);
    }
    

  }
  private void insertData(String tbname) throws Exception{
    String insertsql = "insert into "+tbname+" (INT_,STR_,NUM_,DATE_,TIME_,TIMESTAMP_,BINARY_,CHARACTER_,NUM2_,BINARY2_,CHARACTER2_)values(?,?,?,?,?,?,?,?,?,?,?)";
    Connection conn = getConnectionFactory().getConnection();
    try{
      //测试一般的int，char，double，date，time，timestamp数值
      PreparedStatement pstat = conn.prepareStatement(insertsql);
      pstat.setInt(1, 1);
      pstat.setString(2, "四川汶川大地震");
      pstat.setDouble(3, 123456789);
      pstat.setDate(4, new Date(Timestamp.valueOf("2008-01-01 22:25:01").getTime()));
      pstat.setTime(5, Time.valueOf("10:56:12"));
      pstat.setTimestamp(6, Timestamp.valueOf("1954-01-01 21:25:15"));
      pstat.setBinaryStream(7, null, 0);
      pstat.setCharacterStream(8, null, 0);
      pstat.setDouble(9, 123.01);
      pstat.setBinaryStream(10, null, 0);
      pstat.setCharacterStream(11, null, 0);
      //pstat.addBatch();
      //pstat.executeBatch();
      pstat.executeUpdate();
      
      //测试double字段插入大数据
      pstat.setInt(1, 2);
      pstat.setString(2, "四川汶川大地震2");
      pstat.setObject(3, "210005000004723196");
      pstat.setDate(4, null);
      pstat.setTime(5, null);
      pstat.setTimestamp(6, null);
      pstat.setBinaryStream(7, null, 0);
      pstat.setCharacterStream(8, null, 0);
      pstat.setDouble(9, 0.987654321987654321);
      pstat.setBinaryStream(10, null, 0);
      pstat.setCharacterStream(11, null, 0);
      //pstat.addBatch();
      //pstat.executeBatch();
      pstat.executeUpdate();
      
      //测试小的blob,clob值
      pstat.setInt(1, 3);
      String str = "四川汶川大地震3";
      pstat.setString(2, str);
      pstat.setDouble(3, 234.34);
      pstat.setDate(4, null);
      pstat.setTime(5, null);
      pstat.setTimestamp(6, null);
      pstat.setBinaryStream(7, getStrInputStream(str), -1);
      pstat.setCharacterStream(8, new CharArrayReader(str.toCharArray()), str.length());
      pstat.setObject(9, "123456789123456789");
      String str2 = "四川汶川大地震3333";
      pstat.setBinaryStream(10, getStrInputStream(str2), -1);
      String str3 = "四川汶川大地震33334";
      pstat.setCharacterStream(11, new CharArrayReader(str3.toCharArray()), str3.length());
      //pstat.addBatch();
      //pstat.executeBatch();
      pstat.executeUpdate();
      
      
      //测试大的blob,clob值
      pstat.setInt(1, 4);
      pstat.setString(2, "四川汶川大地震4");
      pstat.setDouble(3, 234.34);
      pstat.setDate(4, null);
      pstat.setTime(5, null);
      pstat.setTimestamp(6, null);
      InputStream bigin = getClass().getResourceAsStream("impl\\big.txt");
      try{
        pstat.setBinaryStream(7, bigin, -1);
        
      }finally{
        bigin.close();
      }
      InputStream bigin2 = getClass().getResourceAsStream("impl\\big.txt");
      try{
        pstat.setBinaryStream(10, bigin2, -1);
        
      }finally{
        bigin2.close();
      }
      String bigstr = getBigColgValue();
      pstat.setCharacterStream(8, new CharArrayReader(bigstr.toCharArray()), bigstr.length());
      pstat.setDouble(9, 123.0123);
      pstat.setCharacterStream(11, new CharArrayReader(bigstr.toCharArray()), bigstr.length());
      //pstat.addBatch();
      //pstat.executeBatch();
      pstat.executeUpdate();
      
      
      //写入大量数据
      //writeBigData(pstat);
      
      pstat.close();
      conn.commit();
    }finally{
      conn.close();
    }
  }
  private void writeBigData(PreparedStatement pstat) throws Exception {
    int k=5;
    for(int i=0;i<1000;i++){
    //测试小的blob,clob值
    pstat.setInt(1, k++);
    String str = "四川汶川大地震a"+k;
    pstat.setString(2, str);
    pstat.setDouble(3, Math.round(Math.random()*10000)/100);
    pstat.setDate(4, null);
    pstat.setTime(5, null);
    pstat.setTimestamp(6, null);
    pstat.setBinaryStream(7, getStrInputStream(str), -1);
    pstat.setCharacterStream(8, new CharArrayReader(str.toCharArray()), str.length());
    //pstat.addBatch();
    //pstat.executeBatch();
    pstat.executeUpdate();
    
    
    //测试大的blob,clob值
    pstat.setInt(1, k++);
    pstat.setString(2, "四川汶川大地震"+k);
    pstat.setDouble(3, Math.round(Math.random()*10000)/100);
    pstat.setDate(4, null);
    pstat.setTime(5, null);
    pstat.setTimestamp(6, null);
    InputStream bigin = getClass().getResourceAsStream("impl\\big.txt");
    try{
      pstat.setBinaryStream(7, bigin, -1);
    }finally{
      bigin.close();
    }
    String bigstr = getBigColgValue();
    pstat.setCharacterStream(8, new CharArrayReader(bigstr.toCharArray()), bigstr.length());
    //pstat.addBatch();
    //pstat.executeBatch();
    pstat.executeUpdate();
    System.out.println(k);
    }
  }
  private InputStream getStrInputStream(String str) throws UnsupportedEncodingException{
    return new ByteArrayInputStream(str.getBytes("utf-8"));
  }
  public String getStrValue(InputStream in) throws IOException{
    byte[] bys = StmFunc.stm2bytes(in);
    return new String(bys,"utf-8");
  }
  public String getBigColgValue() throws IOException{
    InputStream bigin = getClass().getResourceAsStream("san.txt");
    try{
      return new String(StmFunc.stm2bytes(bigin),"utf-8");
    }finally{
      bigin.close();
    }
  }
  protected String createTable(String tablename) throws Exception {
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Dialect dl = getConnectionFactory().getDialect();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.defineAutoIncField("ID_", 1);
      dbv.defineIntField("INT_", 30, null, true, false);
      dbv.defineStringField("STR_",  15, null, true, false);
      dbv.defineFloatField("NUM_",  120, 2, null, true, false);
      dbv.defineFloatField("NUM2_",  30, 2, null, true, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.defineTimeField("TIME_", null, true, false);
      dbv.defineTimeStampField("TIMESTAMP_",  null, true,false);
      dbv.defineLogicField("LOGIC_", null, true, false);
      dbv.defineMemoField("MEMO_",  null, true, false);
      dbv.defineStringField("STRBBQ_",100,null,true,false);//yyyymmdd
      dbv.defineBlobField("BINARY_", null, true, false);
      dbv.defineBlobField("BINARY2_", null, true, false);
      dbv.defineClobField("CHARACTER_",  null, true, false);
      dbv.defineClobField("CHARACTER2_",  null, true, false);
      dbv.defineStringField("name",  20, null, true, false);
      dbv.definePrimaryKey("STR_");
      dbv.defineIndex("I" + tablename, "(STR_,DATE_)", false);
      dbv.defineIndex("I" + tablename, "("+dl.funcLeft("STR_", "4")+")", false);
      
      String tbname = dbv.createTable(conn,null,tablename);
      return tbname;
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
}
