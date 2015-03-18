package com.esen.jdbc.data;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.esen.jdbc.ConnectFactoryManagerImpl;
import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.DataCopy;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.util.StmFunc;
import com.esen.util.StrFunc;

public class TestDataCopy2 extends TestCase {
  
  public void testCopy() throws Exception{
    DbPool[] pools = new DbPool[]{new DbPool("oracle","oracle.jdbc.driver.OracleDriver","jdbc:oracle:thin:@192.168.1.42:1521:orcl","dw","dw"),
        //new DbPool("db2","com.ibm.db2.jcc.DB2Driver","jdbc:db2://192.168.1.42:50000/dwdb","db2admin","db2admin"),
        new DbPool("mysql","com.mysql.jdbc.Driver","jdbc:mysql://192.168.1.42/bidb2?useUnicode=true&characterEncoding=utf8","root","dw"),
        //new DbPool("mssql2000","net.sourceforge.jtds.jdbc.Driver","jdbc:jtds:sqlserver://192.168.1.244:1433/testdb","sa","admin"),
        /*new DbPool("sybase12.5","com.sybase.jdbc2.jdbc.SybDriver","jdbc:sybase:Tds:192.168.1.42:5000/bidb?charset=cp936","sa","")*/};
    initpool(pools);
    tryCopy(pools);
    closePool(pools);
  }
  private void closePool(DbPool[] pools) throws SQLException {
    for(int i=0;i<pools.length;i++){
      ConnectionFactory m5f = DefaultConnectionFactory.get(pools[i].getName(), false);
      if(m5f!=null){
        SimpleConnectionFactory fs = (SimpleConnectionFactory)m5f;
        fs.close();
      }
    }
    
  }

  private void tryCopy(DbPool[] pools) throws Exception {
    for(int i=0;i<pools.length;i++){
      String pnamei = pools[i].getName();
      //自身内部copy
      //tryCopy(pnamei,pnamei);
      for(int j=i+1;j<pools.length;j++){
        String pnamej = pools[j].getName();
        //不同源互相copy
        tryCopy(pnamei,pnamej);
        tryCopy(pnamej,pnamei);
      }
    }
    
  }

  private void tryCopy(String srcpool, String destpool) throws Exception {
    createTable(srcpool);
    addData(srcpool, 100);
    addNullRow(srcpool);
    
    ConnectionFactory srcconf = DefaultConnectionFactory.get(srcpool, true);
    ConnectionFactory destconf = DefaultConnectionFactory.get(destpool, true);
    dropTable(destconf,"testdb2");
    //根据表名复制表结构，不copy数据
    DataCopy.createInstance().createTableAsSelect(srcpool, tablename, destpool, "testdb2");
    //检查新旧表的 自动增长字段和索引是否一致；
    checkTable(srcconf,tablename, destconf,"testdb2");
    
    //根据 sql 创建表
    dropTable(destconf,"testdb2");
    DataCopy.createInstance().createTableAsSelect(srcpool, "select * from "+tablename, destpool, "testdb2");
    assertEquals(true,tableExists(destconf,"testdb2"));
    
    //根据表名，copy表数据
    dropTable(destconf,"testdb2");
    DataCopy.createInstance().selectInto(srcpool, tablename, destpool, "testdb2");
    //检查表结构
    checkTable(srcconf,tablename, destconf,"testdb2");
    //检查数据
		chechData(srcconf, destconf);
    //检查目的表大字段数据
    checkClobBlobData(destconf,"testdb2");
    
    //根据 sql 创建表并copy表数据
    dropTable(destconf,"testdb2");
    DataCopy.createInstance().selectInto(srcpool, "select STR_,DATE_,NUM_,BINARY_,CHARACTER_ from "+tablename, destpool, "testdb2");
    int cn1 = getDataCount(srcconf,"select count(*) from (select STR_,DATE_,NUM_,BINARY_,CHARACTER_ from "+tablename+") x");
    int cn2 = getDataCount(destconf,"select count(*) from testdb2");
    assertEquals(cn1,cn2);
    //检查目的表大字段数据
    checkClobBlobData(destconf,"testdb2");
    
    //测试select * from tbname做 srcsql
    dropTable(destconf,"testdb2");
    DataCopy.createInstance().selectInto(srcpool, "select * from "+tablename, destpool, "testdb2");
    cn1 = getDataCount(srcconf,"select count(*) from (select * from "+tablename+") x");
    cn2 = getDataCount(destconf,"select count(*) from testdb2");
    assertEquals(cn1,cn2);
    //检查目的表大字段数据
    checkClobBlobData(destconf,"testdb2");
    //测试导入导出
    testExportAndImport(srcpool,destpool);
  }
	private void chechData(ConnectionFactory srcconf, ConnectionFactory destconf)
			throws Exception {
		int cn1 = getDataCount(srcconf,"select count(*) from "+tablename);
    int cn2 = getDataCount(destconf,"select count(*) from testdb2");
    assertEquals(cn1,cn2);
    //检查日期字段值
    
	}

  private void initpool(DbPool[] pools) {
    ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
    for(int i=0;i<pools.length;i++){
      fm.setConnectionFactory(pools[i].getName(),createConnectionFactory(pools[i]));
    }
    DefaultConnectionFactory.set(fm);
  }
  private ConnectionFactory createConnectionFactory(DbPool pool){
    return new SimpleConnectionFactory(
        pool.getDriverclass(),
        pool.getUrl(),
        pool.getUser(), pool.getPassword(),true);
  }
  private void testExportAndImport(String srcpool, String destpool) throws Exception {
    File f = new File("c:\\testdata.db");
    FileOutputStream out = new FileOutputStream(f);
    try{
      DataCopy.createInstance().exportData(srcpool, tablename, out);
    }finally{
      out.close();
    }
    FileInputStream fin = new FileInputStream(f);
    String tbname = null;
    try{
      tbname = DataCopy.createInstance().importData(fin, destpool, tablename, DataCopy.OPT_CREATENEWTABLE);
    }finally{
      fin.close();
    }
    ConnectionFactory srcconf = DefaultConnectionFactory.get(srcpool, true);
    ConnectionFactory destconf = DefaultConnectionFactory.get(destpool, true);
    //检查表结构
    checkTable(srcconf,tablename, destconf,tbname);
    //检查数据
    int cn1 = getDataCount(srcconf,"select count(*) from "+tablename);
    int cn2 = getDataCount(destconf,"select count(*) from "+tbname);
    assertEquals(cn1,cn2);
    //检查目的表大字段数据
    checkClobBlobData(destconf,tbname);
  }
  protected void checkClobBlobData(ConnectionFactory conf, String tbname) throws Exception {
    Connection conn = conf.getConnection();
    InputStream in = null;
    try{
      Statement stat = conn.createStatement();
      ResultSet rs = stat.executeQuery("select BINARY_,CHARACTER_ from "+tbname);
      rs.next();
      in = rs.getBinaryStream(1);
      in = StmFunc.getUnGZIPStm(new ByteArrayInputStream(StmFunc.stm2bytes(in)));
      
      assertEquals(rs.getString(2),CLOB_CONTACT);
      rs.close();
      stat.close();
    }finally{
      conn.close();
    }
    byte[] bys = StmFunc.stm2bytes(in);
    String str = new String(bys);
    assertEquals(str,getTestInputStreamStr());
  }
  private int getDataCount(ConnectionFactory conf, String sql) throws Exception {
    Connection conn = conf.getConnection();
    try{
      Statement stat = conn.createStatement();
      try{
        ResultSet rs = stat.executeQuery(sql);
        rs.next();
        return rs.getInt(1);
      }finally{
        stat.close();
      }
    }finally{
      conn.close();
    }
  }
  private void checkTable(ConnectionFactory srcconf,String srctbname, ConnectionFactory destconf,String desttbname)
      throws Exception {
    TableMetaData tmd = srcconf.getDialect().createDbMetaData().getTableMetaData(srctbname);
    String incName = getIncName(tmd);
    TableMetaData tmd2 = destconf.getDialect().createDbMetaData().getTableMetaData(desttbname);
    String incName2 = getIncName(tmd2);
    //比较自动增长字段
    assertEquals(true, incName!=null&&StrFunc.compareStr(incName, incName2));
    //比较字段个数
    assertEquals(tmd.getColumnCount(),tmd2.getColumnCount());
    //比较日期字段类型是否一致
    for(int i=0;i<tmd.getColumnCount();i++){
    	int t1 = tmd.getColumnType(i);
    	int t2 = tmd2.getColumnType(i);
    	if(SqlFunc.getType(t1)=='D'){
    		assertEquals(t1,t2);
    	}
    }
    
    //比较索引个数是否一致；
    TableIndexMetaData[] inds = getIndexesNoInc(tmd.getIndexes(),incName);
    TableIndexMetaData[] inds2 = getIndexesNoInc(tmd2.getIndexes(),incName2);
    assertEquals(true,inds.length==inds2.length);
    
  }
  
  private TableIndexMetaData[] getIndexesNoInc(TableIndexMetaData[] indexes,
      String incName) {
    List inds = new ArrayList();
    for(int i=0;i<indexes.length;i++){
      String[] cols = indexes[i].getColumns();
      if(cols.length!=1||!cols[0].equalsIgnoreCase(incName)){
       inds.add(indexes[i]); 
      }
    }
    TableIndexMetaData tims[] = new TableIndexMetaData[inds.size()];
    inds.toArray(tims);
    return tims;
  }
  private boolean tableExists(ConnectionFactory destconf,String tbname) throws Exception,
      SQLException {
    Connection destconn = destconf.getConnection();
    try{
      return destconf.getDbDefiner().tableExists(destconn, null, tbname);
    }finally{
      destconn.close();
    }
  }
  private String getIncName(TableMetaData tmd) {
    TableColumnMetaData[] cols = tmd.getColumns();
    for(int i=0;i<cols.length;i++){
      if(cols[i].isAutoInc())
        return cols[i].getName().toUpperCase();
    }
    return null;
  }
  private void dropTable(ConnectionFactory destconf,String tbname) throws Exception,
      SQLException {
    Connection destconn = destconf.getConnection();
    try{
      if(destconf.getDbDefiner().tableExists(destconn, null, tbname))
        destconf.getDbDefiner().dropTable(destconn, null, tbname);
    }finally{
      destconn.close();
    }
  }
  private String tablename = "TESTDB";
  private static String CLOB_CONTACT = "新连线科技";
  private  int[][] dates = new int[][]{
      {20070201,200702,2007},
      {20070812,200708,2007},
      {20060319,200603,2006},
      {20060223,200602,2006},
      {20051112,200511,2005}
  };
  /**
   * 创建测试用表
   * @throws Exception
   */
  private void createTable(String poolname) throws Exception {
    ConnectionFactory conf = DefaultConnectionFactory.get(poolname, true);
    DbDefiner dbv = conf.getDbDefiner();
    Connection conn = null;
    try{
      conn = conf.getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.defineAutoIncField("ID_", 1);
      dbv.defineIntField("INT_", 30, null, true, false);
      dbv.defineStringField("STR_",  50, null, true, false);
      dbv.defineFloatField("NUM_",  30, 2, null, true, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.defineTimeField("TIME_", null, true, false);
      dbv.defineTimeStampField("TIMESTAMP_",  null, true, false);
      dbv.defineLogicField("LOGIC_", null, true, false);
      dbv.defineMemoField("MEMO_",  null, true, false);
      dbv.defineStringField("STRBBQ_",100,null,true,false);//yyyymmdd
      dbv.defineBlobField("BINARY_", null, true, false);
      dbv.defineClobField("CHARACTER_",  null, true, false);
      dbv.defineIntField("INTBBQYMD", 8, null, true, false);//yyyymmdd
      dbv.defineIntField("INTBBQYM", 8, null, true, false);//yyyymm
      dbv.defineIntField("INTBBQY", 8, null, true, false);//yyyy
      dbv.defineStringField("CHARBBQYM", 8, null, true, false);//yyyymm
      dbv.defineStringField("CHARBBQY", 8, null, true, false);//yyyy
      dbv.defineStringField("CHARBBQYM_", 8, null, true, false);//yyyymm--
      dbv.defineStringField("CHARBBQY_", 8, null, true, false);//yyyy----
      dbv.defineStringField("OPTION", 8, null, true, false);//测试数据库关键字做字段名
      dbv.definePrimaryKey("ID_");
      dbv.defineIndex("I" + tablename, "(STR_,DATE_)", false);

      dbv.createTable(conn,null,tablename);
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
  public void addNullRow(String poolname) throws Exception {
    ConnectionFactory conf = DefaultConnectionFactory.get(poolname, true);
    Connection conn = null;
    PreparedStatement pstat = null;
    String sql = "insert into "
        + tablename
        + " (INT_,STR_,NUM_,DATE_,TIME_,TIMESTAMP_,LOGIC_,MEMO_,BINARY_,CHARACTER_,STRBBQ_)values(?,?,?,?,?,?,?,?,?,?,?)";
    try {
      conn = conf.getConnection();
      pstat = conn.prepareStatement(sql);
      //插入非空的一行数据
        pstat.setObject(1, new Long(Math.round((Math.random() * 1000))));
        pstat.setObject(2, "大学" + Math.round((Math.random() * 1000)));
        pstat.setObject(3, new Double((Math.round((Math.random() * 1000))) / 100));
        pstat.setObject(4, java.sql.Date.valueOf("2005-08-10"));//java.sql.Date.valueOf("2005-08-10"));
        pstat.setObject(5, new java.sql.Timestamp(System.currentTimeMillis()));// java.sql.Time.valueOf("13:30:14"));
        pstat.setObject(6, java.sql.Timestamp
            .valueOf("2005-08-10 13:30:14.234"));//java.sql.Timestamp.valueOf("2005-08-10 13:30:14.234"));
        pstat.setObject(7, "1");
        pstat.setObject(8, "阿斗发机adskfager lkjgerg;");
        InputStream fin = getTestInputStream();
        pstat.setObject(9, fin);
        String clob = CLOB_CONTACT;
        pstat.setObject(10, new CharArrayReader(clob.toCharArray()));
        //pstat.setCharacterStream(10, new CharArrayReader(clob.toCharArray()),clob.length());
        pstat.setObject(11, "200503--");
        pstat.addBatch();
      //插入一行空置数据
        pstat.setObject(1, null);
        pstat.setObject(2, "大学" + Math.round((Math.random() * 1000)));
        pstat.setObject(3, null);
        pstat.setObject(4, null);
        pstat.setObject(5, null);
        pstat.setObject(6, null);
        pstat.setObject(7, null);
        pstat.setObject(8, null);
        pstat.setObject(9, null);
        pstat.setObject(10, null);
        pstat.setObject(11, null);
        pstat.addBatch();
      pstat.executeBatch();
    }
    finally {
      try {
        if (pstat != null)
          pstat.close();
        if (conn != null) {
          conn.close();
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  /**
   * 插入指定行数据；
   * @throws Exception 
   */
  private void addData(String poolname,int num) throws Exception {
    ConnectionFactory conf = DefaultConnectionFactory.get(poolname, true);
    Connection conn = null;
    PreparedStatement pstat = null;
    String sql = "insert into "
        + tablename
        + " (INT_,STR_,NUM_,DATE_,TIME_,TIMESTAMP_,LOGIC_,MEMO_,BINARY_,CHARACTER_,STRBBQ_" +
            ",INTBBQYMD,INTBBQYM,INTBBQY,CHARBBQYM,CHARBBQY,CHARBBQYM_,CHARBBQY_)" +
            "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    try {
      conn = conf.getConnection();
      pstat = conn.prepareStatement(sql);
      int n = 0;
      for (int i = 0; i < num; i++) {
        System.out.println(i+"_________________________________");
        pstat.setLong(1, i);
        pstat.setString(2, String.valueOf(Math.round((Math.random() * 10000))));
        pstat.setDouble(3, (Math.random() * 1000) / 10);
        pstat.setDate(4, java.sql.Date.valueOf("2005-08-10"));//java.sql.Date.valueOf("2005-08-10"));
        pstat.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));// java.sql.Time.valueOf("13:30:14"));
        pstat.setTimestamp(6, java.sql.Timestamp
            .valueOf("2005-08-10 13:30:14.234"));//java.sql.Timestamp.valueOf("2005-08-10 13:30:14.234"));
        pstat.setString(7, "1");
        pstat.setString(8, "阿斗发机adskfager lkjgerg;");
        InputStream fin = getTestInputStream();
        pstat.setBinaryStream(9, fin, fin.available());
        String clob = CLOB_CONTACT;
        pstat.setCharacterStream(10, new CharArrayReader(clob.toCharArray()),
            clob.length());
        int[] dd = dates[(int)Math.round(Math.random()*4)];
        pstat.setString(11, String.valueOf(dd[0]));
        pstat.setInt(12, dd[0]);
        pstat.setInt(13, dd[1]);
        pstat.setInt(14, dd[2]);
        pstat.setString(15, String.valueOf(dd[1]));
        pstat.setString(16, String.valueOf(dd[2]));
        pstat.setString(17, String.valueOf(dd[1])+"--");
        pstat.setString(18, String.valueOf(dd[2])+"----");
        pstat.addBatch();
        n++;
        if(n>=100){
          pstat.executeBatch();
          n=0;
        }
      }
      if(n>0)
       pstat.executeBatch();
    }
    finally {
      try {
        if (pstat != null)
          pstat.close();
        if (conn != null) {
          conn.close();
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  private InputStream getTestInputStream() throws IOException{
    //保存压缩流
    return new ByteArrayInputStream(StmFunc.gzipBytes(getTestInputStreamStr().getBytes()));
  }
  private String getTestInputStreamStr(){
    return "长江长城，黄山黄河";
  }
}
