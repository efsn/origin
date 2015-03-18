package com.esen.jdbc.dbdefiner;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.ResultMetaData;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.EasyDbDefiner;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataHelper;
import com.esen.jdbc.dialect.impl.TableMetaDataHelper;
import com.esen.jdbc.pool.PooledResultSet;
import com.esen.jdbc.pool.PooledSQLException;
import com.esen.util.ExceptionHandler;
import com.esen.util.StmFunc;
import com.esen.util.StrFunc;
import com.esen.util.XmlFunc;

import junit.framework.TestCase;

public abstract class DbDefinerTest extends TestCase {
  private ConnectionFactory dbf = null;
  protected String tablename = "T_TEST";
  public static String CLOB_CONTACT = "新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段"+
  		"新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段" +
  		"新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段" ;
  		//"新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段新连线科技测试大字段" ;
  protected  int[][] dates = new int[][]{
      {20070201,200702,2007},
      {20070812,200708,2007},
      {20060319,200603,2006},
      {20060223,200602,2006},
      {20051112,200511,2005}
  };
  public ConnectionFactory getConnectionFactory(){
    if (dbf==null){
      dbf = createConnectionFactory();
    }
    return dbf;
  }
  public abstract ConnectionFactory createConnectionFactory();
  
  protected void tearDown() throws java.lang.Exception{
    if(dbf!=null){
      SimpleConnectionFactory sf = (SimpleConnectionFactory)dbf;
      sf.close();
    }
  } 
  
	/**
	 * 测试在多线程环境下，删除表是否会出异常；
	 */
	public void testDropTableForMutiThread() {
		ConnectionFactory conf = getConnectionFactory();
		List tabList = Collections.synchronizedList(new ArrayList(50));
		try {
			Connection con = conf.getConnection();
			DbDefiner dbf = conf.getDbDefiner();
			try {
				for (int i = 0; i < 20; i++) {
					dbf.clearDefineInfo();
					dbf.defineStringField("field", 20, null, true, false);
					tabList.add(dbf.createTable(con, "TEST", false));
				}
			}
			finally {
				con.close();
			}
		}
		catch (Exception e) {
			ExceptionHandler.rethrowRuntimeException(e);
		}

		List errorList = Collections.synchronizedList(new ArrayList(32));

		try {
			//启用多线程删除表
			DropTableForMultithread[] cms = new DropTableForMultithread[5];
			for (int i = 0; i < 3; i++) {
				cms[i] = new DropTableForMultithread(errorList, tabList, conf);
				cms[i].start();

			}
			for (int i = 0; i < 3; i++) {
				cms[i].join();
			}
			if (errorList.size() > 0) {
				fail((String) errorList.get(0));
			}
			//验证表是否都被删除
			Connection conn = conf.getConnection();
			DbDefiner dbf = conf.getDbDefiner();
			try{
				for(int i=0;i<tabList.size();i++){
					String tbi = (String)tabList.get(i);
					assertEquals(false, dbf.tableExists(conn, null, tbi));
				}
			}finally{
				conn.close();
			}
		}
		catch (Exception e) {
			ExceptionHandler.rethrowRuntimeException(e);
		}finally {
			try {
				List tblist = conf.getDbMetaData().getTableNames();
				Connection con = conf.getConnection();
				DbDefiner dbv = conf.getDbDefiner();
				try {
					for (int i = 0; i < tblist.size(); i++) {
						String tbi = (String) tblist.get(i);
						if(tbi.startsWith("TEST"))
						  dbv.dropTable(con, null, tbi);
					}
				}
				finally {
					con.close();
				}
			}
			catch (Exception e) {
				ExceptionHandler.rethrowRuntimeException(e);
			}
		}

	}
  
  /**
   * 测试常见的执行sql的异常代码，是否能转换成统一的异常编码；
   * @throws SQLException
   */
	public void testSQLException() throws SQLException {
		ConnectionFactory conf = getConnectionFactory();
		DbDefiner def = conf.getDbDefiner();
		Connection conn = conf.getConnection();
		try {
			if (def.tableExists(conn, null, tablename)) {
				def.dropTable(conn, null, tablename);
			}
			
			Statement stat = conn.createStatement();
			try{
				//查询不存在的表
				stat.executeQuery("select field from "+tablename);
			}catch(PooledSQLException se){
				assertEquals(PooledSQLException.JDBC_NOT_EXIST_OBJECT, se.getErrorCode2());
			}finally{
				stat.close();
			}
			
			stat = conn.createStatement();
			try{
				//写入不存在的表
				stat.execute("insert into "+tablename+" (field)values('aa')");
			}catch(PooledSQLException se){
				assertEquals(PooledSQLException.JDBC_NOT_EXIST_OBJECT, se.getErrorCode2());
			}finally{
				stat.close();
			}
			
			stat = conn.createStatement();
			try{
				//删除不存在的表
				stat.executeUpdate("DROP TABLE " + tablename);
			}catch(PooledSQLException se){
				assertEquals(PooledSQLException.JDBC_NOT_EXIST_OBJECT, se.getErrorCode2());
			}finally{
				stat.close();
			}
			def.defineStringField("id", 20, null, false, false);
			def.defineStringField("field", 20, null, true, false);
			def.defineStringField("field2", 20, null, true, false);
			def.definePrimaryKey("id");
			def.createTable(conn, null, tablename);
			
			try{
				//重名
				def.createTable(conn, null, tablename);
			}catch(PooledSQLException se){
				assertEquals(PooledSQLException.JDBC_EXISTING_OBJECT, se.getErrorCode2());
			}
			//创建唯一索引
			def.createIndex(conn, tablename, "I"+tablename+99, new String[]{"field"}, true,true);
			
			stat = conn.createStatement();
			try{
				//创建一个重复索引名的索引； CREATE INDEX IT_TEST99 ON T_TEST(field2)
				if (conf.getDialect().getDataBaseInfo().isTeradata()) {
					// Teradata 语法不一样
					stat.execute("CREATE INDEX IT_TEST99(field2) ON T_TEST");
				} else {
					stat.execute("CREATE INDEX IT_TEST99 ON T_TEST(field2)");
				}
			}catch(PooledSQLException se){
				assertEquals(PooledSQLException.JDBC_EXISTING_OBJECT, se.getErrorCode2());
			}finally{
				stat.close();
			}
			
			stat = conn.createStatement();
			try{
				stat.execute("insert into "+tablename+" (id,field)values('1','aa')");
			}finally{
				stat.close();
			}
			
			stat = conn.createStatement();
			try{
				//写入违反唯一索引约束
				stat.execute("insert into "+tablename+" (id,field)values('2','aa')");
			}catch(PooledSQLException se){
				assertEquals(PooledSQLException.JDBC_UNIQUE_CONSTRAINT_VIOLATED, se.getErrorCode2());
			}finally{
				stat.close();
			}
			stat = conn.createStatement();
			try{
				//写入违反主键约束
				stat.execute("insert into "+tablename+" (id,field)values('1','bb')");
			}catch(PooledSQLException se){
				assertEquals(PooledSQLException.JDBC_UNIQUE_CONSTRAINT_VIOLATED, se.getErrorCode2());
			}finally{
				stat.close();
			}
			
			stat = conn.createStatement();
			try{
				//查询不存在的字段
				stat.executeQuery("select field3 from "+tablename);
			}catch(PooledSQLException se){
				assertEquals(PooledSQLException.JDBC_INVALID_COLUMN, se.getErrorCode2());
			}finally{
				stat.close();
			}
			
			
			def.dropTable(conn, null, tablename);
			
			
		}
		finally {
			conn.close();
		}
	}
  
  /**
   * 测试多线程下，创建表，指定表名存在，自动更名是否出现对象存在无法创建的问题；
   */
	public void testCreateTableForMultithread() {
		ConnectionFactory conf = getConnectionFactory();
		List errorList = Collections.synchronizedList(new ArrayList(32));
		List tabList = Collections.synchronizedList(new ArrayList(250));
		try {
			CreateTableForMultithread[] cms = new CreateTableForMultithread[5];
			for (int i = 0; i < 3; i++) {
				cms[i] = new CreateTableForMultithread(errorList,tabList,conf);
				cms[i].start();
				
			}
			for (int i = 0; i < 3; i++) {
				cms[i].join();
			}
			if(errorList.size()>0){
				this.fail((String)errorList.get(0));
			}
			this.assertEquals(3 * 50, tabList.size());
		}
		catch (Exception e) {
			ExceptionHandler.rethrowRuntimeException(e);
		}
		finally {
			try {
				Connection con = conf.getConnection();
				DbDefiner dbv = conf.getDbDefiner();
				try {
					for (int i = 0; i < tabList.size(); i++) {
						String tbi = (String) tabList.get(i);
						dbv.dropTable(con, null, tbi);
					}
				}
				finally {
					con.close();
				}
			}
			catch (Exception e) {
				ExceptionHandler.rethrowRuntimeException(e);
			}
		}
	}
  
  /**
   * 测试DbMetaData，分别测试有cache功能的实现和无cache功能的实现；
 * @throws Exception 
   */
  public void testDbMetaData() throws Exception{
	  ConnectionFactory conf = getConnectionFactory();
	  Dialect dl = conf.getDialect();
	  DbMetaData dbMeta = dl.createDbMetaData();//无cache功能的实现
	  DbMetaData dbCacheMeta = conf.getDbMetaData();//有cache功能的实现
	  
	  createTable();
	  addData(100);
	  
	  testDbMetaData(dbMeta);
	  testDbMetaData(dbCacheMeta);
	  
  }
  
  private void testDbMetaData(DbMetaData dbMeta) throws SQLException{
	  TableMetaData tbmt = dbMeta.getTableMetaData(tablename);
	  assertEquals(true,tbmt.haveField("str_"));
	  TableColumnMetaData col = tbmt.getColumn("STR_");
	  assertEquals("STR_", col.getName().toUpperCase());
	  assertEquals('C', SqlFunc.getType(col.getType()));
	  assertEquals('C', tbmt.getFieldType("str_"));
	  assertEquals(col.getType(),tbmt.getFieldSqlType("str_"));
	  assertEquals("STR_",tbmt.getRealFieldName("STR_").toUpperCase());
	  
	  Object[] bbqs = (Object[])tbmt.getFieldSample("STRBBQ_", TableMetaData.FIELD_SAMPLE_SELECTRANDOM);
	  assertEquals("yyyymmdd",StrFunc.guessDateFormat((String)bbqs[0]));
	  assertEquals("yyyymmdd",StrFunc.guessDateFormat((String)bbqs[2]));
	  
	  bbqs = (Object[])tbmt.getFieldSample("STRBBQ_", TableMetaData.FIELD_SAMPLE_DISTINCTALL);
	  assertEquals("yyyymmdd",StrFunc.guessDateFormat((String)bbqs[0]));
	  assertEquals("yyyymmdd",StrFunc.guessDateFormat((String)bbqs[2]));
	  
	  bbqs = (Object[])tbmt.getFieldSample("INTBBQYM", TableMetaData.FIELD_SAMPLE_DISTINCTALL);
	  assertEquals("yyyymm",StrFunc.guessDateFormat(String.valueOf(bbqs[0])));
	  assertEquals("yyyymm",StrFunc.guessDateFormat(String.valueOf(bbqs[2])));
	  
	  bbqs = (Object[])tbmt.getFieldSample("STRBBQ_", TableMetaData.FIELD_SAMPLE_MAX);
	  assertEquals(1,bbqs.length);
	  assertEquals("20070812",bbqs[0]);
  }
  
  /**
   * 测试DbDefiner.defineField(TableColumnMetaData col);
   * @throws Exception 
   */
  public void testDefColumnFromColumnMeta() throws Exception{
    createTable();
    
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = getConnectionFactory().getConnection();
    try{
      TableMetaData tmd = getConnectionFactory().getDialect().createDbMetaData(conn).getTableMetaData(tablename);
      TableColumnMetaData[] cols = tmd.getColumns();
      for(int i=0;i<cols.length;i++){
        TableColumnMetaData coli = cols[i];
        dbv.defineField(coli);
      }
      String tbname = dbv.createTable(conn, tablename,false);
      this.assertEquals(true, dbv.tableExists(conn, null, tbname));
      dbv.dropTable(conn, null, tbname);
    }finally{
      conn.close();
    }
  }
  
  /**
   * KingBaseES数据库
   * select substring(a.field,1,4) from tbname a 这个sql读取的字段值，通过rs.getMetaData()获取的字段类型是Types.LONGVARCHAR
   * 这是一种clob类型，而不是Types.VARCHAR ,这和Oracle等数据库的jdbc实现不同；
   *  造成BI中根据字段类型构造表元出现问题；
   * @throws SQLException 
   * 
   */
  public void testSubStrReturnType() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    DataBaseInfo dbinfo = getConnectionFactory().getDbType();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineIntField("INT_", 30, null, true, false);
      dbv.defineStringField("STR_",  20, null, true, false);
      dbv.defineFloatField("NUM_",  18, 2, null, true, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.defineTimeField("TIME_", null, true, false);
      dbv.defineTimeStampField("TIMESTAMP_",  null, true,false);
      String tbname = dbv.createTable(conn,null,tablename);
      Statement stat = conn.createStatement();
      String insertsql = "insert into T_TEST (INT_,STR_,NUM_)values(56,'abcd',8923434.95)";
      stat.executeUpdate(insertsql);
      ResultSet rs = stat.executeQuery("select "+dl.funcLeft("STR_", "2")+","+dl.funcMid("STR_","0", "2")+" from "+tbname);
      //db2返回的是Types.CHAR
      assertEquals('C',SqlFunc.getType(rs.getMetaData().getColumnType(1)));
      assertEquals('C',SqlFunc.getType(rs.getMetaData().getColumnType(2)));
      while(rs.next()){
        assertEquals("ab",rs.getString(1));
        assertEquals("ab",rs.getString(2));
      }
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
  

  /**
   * 有些数据库比如SybaseIQ
   * 从sql中获取case when 语句的指端值时，出现异常：
   * java.lang.ArrayIndexOutOfBoundsException: 147
  at com.sybase.jdbc3.tds.TdsNumeric.numericValue(Unknown Source)
  at com.sybase.jdbc3.tds.TdsDataObject.readNUMERIC(Unknown Source)
  at com.sybase.jdbc3.tds.TdsJdbcInputStream.getDouble(Unknown Source)
  at com.sybase.jdbc3.jdbc.SybResultSet.getDouble(Unknown Source)
  at com.esen.jdbc.pool.impl.PooledResultSet.getDouble(PooledResultSet.java:106)
  at com.esen.irpt.rpdb.db.top.OlapRowData.<init>(OlapRowData.java:78)
  
   * 测试KingBaseES数据库在结果集中通过rs.getObject()读取值时，可能出现的:
   * [KingbaseES JDBC Driver]非法BigDecimal值
 at com.kingbase.jdbc2.AbstractJdbc2ResultSet.toBigDecimal(Unknown Source)
 at com.kingbase.jdbc2.AbstractJdbc2ResultSet.getBigDecimal(Unknown Source)
 at com.kingbase.jdbc3.AbstractJdbc3ResultSet.getObject(Unknown Source)
 at com.esen.jdbc.pool.impl.PooledResultSet.getObject(PooledResultSet.java:250)
   * @throws Exception
   */
  public void testGetObjectFromResultSet() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    DataBaseInfo dbinfo = getConnectionFactory().getDbType();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineIntField("INT_", 30, null, true, false);
      dbv.defineStringField("STR_",  20, null, true, false);
      dbv.defineFloatField("NUM_",  18, 2, null, true, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.defineTimeField("TIME_", null, true, false);
      dbv.defineTimeStampField("TIMESTAMP_",  null, true,false);
      String tbname = dbv.createTable(conn,null,tablename);
      
      Statement stat = conn.createStatement();
      String insertsql = "insert into T_TEST (INT_,STR_,NUM_)values(56,'aa',8923434.95)";
      String insertsql2 = "insert into T_TEST (INT_,STR_,NUM_)values(25,'aa',3656784.48)";
      String insertsql3 = "insert into T_TEST (INT_,STR_,NUM_)values(25,'aa',7834259.33)";
      stat.executeUpdate(insertsql);
      stat.executeUpdate(insertsql2);
      stat.executeUpdate(insertsql3);
      
      String sql = "select avg(NUM_) from "+tbname;
      ResultSet rs = stat.executeQuery(sql);
      while(rs.next()){
        Object o = rs.getObject(1);
        double d = rs.getDouble(1);
        String s = rs.getString(1);
      }
      
      String sql2 = "select case when b.zb2=0 then null else a.zb1/b.zb2 end as zb ".toUpperCase() +
          " from (select case when num_>10 then num_/int_ else num_-int_ end as zb1,str_ from ".toUpperCase()+tbname+") A " +
              "left join (" +
              "select case when num_>10 then num_*2 else int_*2 end as zb2,str_ from ".toUpperCase()+tbname+") B " +
                  "on a.str_=b.str_ ".toUpperCase() ;
      
      ResultSet rs2 = stat.executeQuery(sql2);
      while(rs2.next()){
        Object o = rs2.getObject(1);
        double d = rs2.getDouble(1);
        String s = rs2.getString(1);
      }
      
      stat.close();
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
  
  public void testSetValueForPrepareStatement() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    DataBaseInfo dbinfo = getConnectionFactory().getDbType();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineIntField("INT_", 30, null, true, false);
      dbv.defineStringField("STR_",  20, null, true, false);
      dbv.defineFloatField("NUM_",  18, 2, null, true, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.defineTimeField("TIME_", null, true, false);
      dbv.defineTimeStampField("TIMESTAMP_",  null, true,false);
      dbv.defineBlobField("BINARY_", null, true, false);
      dbv.defineClobField("CHARACTER_",  null, true, false);
      String tbname = dbv.createTable(conn,null,tablename);
      String tbname2 = dbv.createTable(conn,tablename,false);
      
      Statement stat = conn.createStatement();
      stat.executeUpdate("insert into "+tbname2+" (STR_,TIMESTAMP_,INT_)values('B82834',"+dl.funcNow()+",23)");
      stat.close();
      
      //Teradata 中，该语句不能带括号
      String sql = "insert into  "+tbname+"(STR_,TIMESTAMP_,INT_) select  ?,"
        + "?,2  from "+tbname2+" where STR_=?  ";
      Timestamp time = new Timestamp(System.currentTimeMillis());
      PreparedStatement ps = conn.prepareStatement(sql);
      try {
        int col = 1;
        ps.setString(col++, "B82834");
        ps.setTimestamp(col++, time);
        ps.setString(col++, "B82834");
        ps.execute();
      }
      finally {
        ps.close();
      }

    }finally{
      if(conn!=null)
        conn.close();
    }
  }
  
  public void testSetNull() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    DataBaseInfo dbinfo = getConnectionFactory().getDbType();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineIntField("INT_", 30, null, true, false);
      dbv.defineStringField("STR_",  20, null, true, false);
      dbv.defineFloatField("NUM_",  18, 2, null, true, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.defineTimeField("TIME_", null, true, false);
      dbv.defineTimeStampField("TIMESTAMP_",  null, true,false);
      dbv.defineBlobField("BINARY_", null, true, false);
      dbv.defineClobField("CHARACTER_",  null, true, false);
      String tbname = dbv.createTable(conn,null,tablename);
      
      PreparedStatement pstat = conn.prepareStatement("insert into "+tablename+" (INT_,STR_,NUM_,DATE_,TIME_,TIMESTAMP_,BINARY_,CHARACTER_)values(?,?,?,?,?,?,?,?)");
      pstat.setNull(1, Types.NULL);
      pstat.setNull(2, Types.NULL);
      pstat.setNull(3, Types.NULL);
      pstat.setNull(4, Types.NULL);
      pstat.setNull(5, Types.NULL);
      pstat.setNull(6, Types.NULL);
      pstat.setNull(7, Types.NULL);
      pstat.setNull(8, Types.NULL);
      pstat.addBatch();
      pstat.executeBatch();
      pstat.close();
      
      Statement stat = conn.createStatement();
      ResultSet rs = stat.executeQuery("select INT_,STR_,NUM_,DATE_,TIME_,TIMESTAMP_,BINARY_,CHARACTER_ from "+tablename);
      assertEquals(true, rs.next());
      rs.getInt(1);
      assertEquals(true,rs.wasNull());
      rs.getString(2);
      assertEquals(true,rs.wasNull());
      rs.getDouble(3);
      assertEquals(true,rs.wasNull());
      rs.getDate(4);
      assertEquals(true,rs.wasNull());
      rs.getTime(5);
      assertEquals(true,rs.wasNull());
      rs.getTimestamp(6);
      assertEquals(true,rs.wasNull());
      rs.getBinaryStream(7);
      assertEquals(true,rs.wasNull());
      rs.getString(8);
      assertEquals(true,rs.wasNull());
      
      assertEquals(false, rs.next());
      rs.close();
      stat.close();
    }finally{
      if(conn!=null)
        conn.close();
    }
    
  }
  
  /**
   * 测试在事务中，修改字段属性，多线程下，能否立即获取新的数据库连接；
   * 重点测试SybaesAse,db2,sqlserver
   * @throws Exception
   */
  public void testTranslationForAlter() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = null;
    long modifyTimestamp;
    try{
      conn = getConnectionFactory().getConnection();
      
      //测试定义超长字段；
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineIntField("INT_", 30, null, true, false);
      dbv.defineStringField("STR_",  100, null, true, true);
      dbv.defineStringField("STR2_",  200, null, true, false);
      dbv.defineStringField("STR3_",  300, null, true, true);
      dbv.defineFloatField("NUM_",  18, 2, null, true, false);
      dbv.defineDateField("DATE_", null, true, false);
      //表名会变
      tablename = dbv.createTable(conn,tablename,false,true);
      
      conn.setAutoCommit(false);
      dbv.modifyColumn(conn, tablename, "STR_", 'C', 200, 0);
      modifyTimestamp = System.currentTimeMillis();
      executeTimestamp = 0;
      /**
       * 下面这个线程，执行一个sql，应该是可以立即执行的；
       */
      ThreadForAlter ta = new ThreadForAlter();
      ta.start();
      
      Thread.sleep(10000);
    }finally{
      if(conn!=null)
        conn.close();
    }
    /**
     * 测试线程中的sql是否是立即执行的；
     * SybaseAse 如果在一个事务中执行alter，则事务提交前，是无法获取新连接的；
     */
    long t = executeTimestamp-modifyTimestamp;
    assertEquals(true, t>0&&t<10000);
  }
  
  private long executeTimestamp;
  class ThreadForAlter extends Thread{
    public void run(){
      try {
        Connection conn = getConnectionFactory().getConnection();
        try {
          Statement stat = conn.createStatement();
          try{
            ResultSet rs = stat.executeQuery("select STR_ from "+tablename);
            executeTimestamp = System.currentTimeMillis();
            rs.close();
          }finally{
            stat.close();
          }
        }
        finally {
          conn.close();
        }
      }
      catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
  }
  
  
  /**
   * 测试字段，索引，主键的长度超过数据库允许范围时的处理；
   * @throws Exception
   */
  public void testKeyAndIndexMaxLength() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      
      //测试定义超长字段；
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineIntField("INT_", 30, null, true, false);
      dbv.defineStringField("STR_",  1000, null, true, true);//唯一属性
      dbv.defineStringField("STR2_",  2000, null, true, false);
      dbv.defineStringField("STR3_",  3000, null, true, true);//测试超大唯一属性
      dbv.defineFloatField("NUM_",  18, 2, null, true, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.createTable(conn,tablename,false,true);
      
      //测试定义超长主键
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineIntField("INT_", 30, null, true, false);
      dbv.defineStringField("STR_",  1000, null, true, false);
      dbv.defineStringField("STR2_",  2000, null, true, false);
      dbv.defineStringField("STR3_",  3000, null, true, false);
      dbv.defineFloatField("NUM_",  18, 2, null, true, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.definePrimaryKey("STR_,NUM_");
      dbv.createTable(conn,tablename,false,true);
      
      //测试定义超长主键
      
      //定义超长索引
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineIntField("INT_", 30, null, true, false);
      dbv.defineStringField("STR_",  1000, null, true, false);
      dbv.defineStringField("STR2_",  2000, null, true, false);
      dbv.defineStringField("STR3_",  3000, null, true, false);
      dbv.defineFloatField("NUM_",  18, 2, null, true, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.defineIndex("I_STR_STR2_DATE", "(STR_,STR2_,DATE_)", false);
      dbv.createTable(conn,tablename,false,true);
      
      //测试定义超长主键
      
      //定义唯一索引
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineIntField("INT_", 30, null, true, false);
      dbv.defineStringField("STR_",  3000, null, true, false);
      dbv.defineStringField("STR2_",  3000, null, true, false);
      dbv.defineStringField("STR3_",  3000, null, true, false);
      dbv.defineFloatField("NUM_",  18, 2, null, true, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.defineIndex("I_STR_STR2_DATE", "(STR_,STR2_,STR3_)", true);
      dbv.createTable(conn,tablename,false,true);
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
  
  /**
   * 测试获取表结构信息；
   * 接口增加了获取是否为空的信息；
   * @throws Exception 
   */
  public void testResultSetMetaData() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineIntField("INT_", 30, null, true, false);
      dbv.defineStringField("STR_",  20, null, false, true);
      dbv.defineFloatField("NUM_",  18, 2, null, true, true);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.createTable(conn,null,tablename);
    }finally{
      if(conn!=null)
        conn.close();
    }
    Dialect dl = getConnectionFactory().getDialect();
    ResultMetaData rsmd = dl.getQueryResultMetaData("select INT_,STR_,NUM_,DATE_ from "+tablename);
    assertEquals("INT_", rsmd.getColumnName(0).toUpperCase());
    assertEquals(1, rsmd.isNullable(0));
    assertEquals("STR_", rsmd.getColumnName(1).toUpperCase());
    assertEquals(0, rsmd.isNullable(1));
    assertEquals("NUM_", rsmd.getColumnName(2).toUpperCase());
    /**
     * db2 和 Teradata 如果字段设置了唯一，则必须不为空；
     */
    if(dl.getDataBaseInfo().isDb2() || dl.getDataBaseInfo().isTeradata()){
      assertEquals(0, rsmd.isNullable(2));
    }else{
      assertEquals(1, rsmd.isNullable(2));
    }
    assertEquals("DATE_", rsmd.getColumnName(3).toUpperCase());
    assertEquals(1, rsmd.isNullable(3));
  }
  
  public void testLargeVarcharField() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = null;
    String tbname = "t_largefield";
    String clob = this.CLOB_CONTACT;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tbname)){
        dbv.dropTable(conn,null,tbname);
      }
      dbv.clearDefineInfo();
      dbv.defineStringField("STR_",  1024, null, false, false);
      //Tearadata 中不能同时插入两个完全一样的行，所以此处增加一列
      dbv.defineIntField("ID_",  10, null, true, false);
      dbv.createTable(conn,null,tbname);
      
      conn.setAutoCommit(false);
      
      PreparedStatement pstat = conn.prepareStatement("insert into "+tbname+" (STR_, ID_)values(?, ?)");
      pstat.clearBatch();
      pstat.setString(1, clob);
      pstat.setInt(2, 1);
      pstat.executeUpdate();
      
      /**
       * 20090806
       * 在SybaseAse12.5, 如果定义的字符串长度太大，
       * 比如这里的varchar(1024), 使用sql查询的meta.getColumnType()获得的类型却是clob类型；
       * 这里调用setCharacterStream()写入，会报：
       * com.sybase.jdbc3.jdbc.SybSQLException: Implicit conversion from datatype 'TEXT' to 'VARCHAR' is not allowed.  Use the CONVERT function to run this query.
       * 对于clob类型，所有数据库都可以使用setString()
       * 这里改为使用setObject()内部实现改为setString()写入；见SybasePooledPreparedStatement类；
       * 
       * 连接SybaseAse 时driverClassName最好使用：
       * com.sybase.jdbc3.jdbc.SybDriver
       * 而不是使用：com.sybase.jdbc2.jdbc.SybDriver
       * 原因是：com.sybase.jdbc2.jdbc.SybDriver会导致写入varchar类型最大长度是256，如果超过会截取，却不报错；
       * com.sybase.jdbc3.jdbc.SybDriver没有这个问题；
       */
      pstat.setObject(1, clob);
      pstat.setInt(2, 2);
      pstat.executeUpdate();
      pstat.close();
      
      conn.commit();
      
      
    }finally{
      if(conn!=null)
        conn.close();
    }
    conn = getConnectionFactory().getConnection();
    try{
      Statement stat = conn.createStatement();
      ResultSet rs = stat.executeQuery("select STR_ from "+tbname);
      assertEquals(true, rs.next());
      String v = rs.getString(1);
      assertEquals(clob, v);
      assertEquals(true, rs.next());
      String v2 = rs.getString(1);
      assertEquals(clob, v2);
      stat.close();
    }finally{
      if(dbv.tableExists(conn,null,tbname)){
        dbv.dropTable(conn,null,tbname);
      }
      if(conn!=null)
        conn.close();
    }
  }
  

  
  public void testNumFieldCreateAndModify() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Dialect dl = getConnectionFactory().getDialect();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.defineStringField("STR_",  30, null, true, false);
      //创建不知道精度的数值字段
      dbv.defineFloatField("num_", 0, 0, null, true, false);
      //创建超过最大精度的字段
      dbv.defineFloatField("num2_", 126, 38, null, true, false);
      
      dbv.defineFloatField("num3_", 18, 2, null, true, false);
      dbv.createTable(conn,null,tablename);
      
      TableColumnMetaData col = dl.getTableColumnMetaData(conn, tablename, "num2_");
      assertEquals(true, col.getLen()>=31);
      assertEquals(12,col.getScale());
      
      /*col = dl.getTableColumnMetaData(conn, tablename, "num_");
      assertEquals(0, col.getLen());
      assertEquals(0,col.getScale());*/
      
      //修改为无精度的字段；
      dbv.modifyColumn(conn, tablename, "num3_", 'N', 0, 0);
      /*col = dl.getTableColumnMetaData(conn, tablename, "num3_");
      assertEquals(0, col.getLen());
      assertEquals(0,col.getScale());*/
      
      //修改为超大精度字段
      dbv.modifyColumn(conn, tablename, "num3_", 'N', 127, 38);
      col = dl.getTableColumnMetaData(conn, tablename, "num3_");
      assertEquals(true, col.getLen()>=18);//db2最大精度18
      assertEquals(12,col.getScale());
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
  
  public void testNewModify() throws Exception{
    createTable();
    //Oracle 数值型改小精度，字段列需要为空；
    //addData(10);
    ConnectionFactory conf = getConnectionFactory();
    Dialect dl= conf.getDialect();
    DbDefiner dbfv = conf.getDbDefiner();
    Connection conn = conf.getConnection();
    try{
      //字符型长度由30改为100
      dbfv.modifyColumn(conn, tablename, "STR_", 'C', 100, 0);
      TableColumnMetaData col = dl.getTableColumnMetaData(conn, tablename, "STR_");
      assertEquals(100, col.getLen());
      //数值型由(18,2)改为(16,4)
      dbfv.modifyColumn(conn, tablename, "NUM_", 'N', 16, 4);
      col = dl.getTableColumnMetaData(conn, tablename, "NUM_");
      assertEquals(16, col.getLen());
      assertEquals(4, col.getScale());
      //只改精度，数值型由(16,4)改为(17,3)
      dbfv.modifyColumn(conn, tablename, "NUM_", 'N', 17, 3);
      col = dl.getTableColumnMetaData(conn, tablename, "NUM_");
      assertEquals(17, col.getLen());
      assertEquals(3, col.getScale());
      
      //date转timestamp类型
      dbfv.modifyColumn(conn, tablename, "DATE_", 'P', 0, 0);
      col = dl.getTableColumnMetaData(conn, tablename, "DATE_");
      assertEquals('P',SqlFunc.getSubsectionType(col.getType()));
      
      //将int型转换成int型，修改属性和原属性相同，是否出异常；
      dbfv.addColumn(conn, tablename, "INT3_", 'I', 20, 0, null, true, false);
      dbfv.modifyColumn(conn, tablename, "INT3_", 'I', 20, 0);
      col = dl.getTableColumnMetaData(conn, tablename, "INT3_");
      if(conf.getDbType().isOracle()){//oracle没有int类型
        assertEquals('N',SqlFunc.getSubsectionType(col.getType()));
      }else{
        assertEquals('I',SqlFunc.getSubsectionType(col.getType()));
      }
      
      //字符型转clob型，不能做这样的修改，数据库不允许；
      /*dbfv.modifyColumn(conn, tablename, "STR_", 'M', 0, 0);
      col = dl.getTableColumnMetaData(conn, tablename, "STR_");
      assertEquals('M',SqlFunc.getSubsectionType(col.getType()));*/
    }finally{
      if(conn!=null) conn.close();
    }
    dropTable(tablename);
  }
  
  public void testCreateTableAndView() throws Exception{
    createTable();
    addData(10);
    ConnectionFactory conf = getConnectionFactory();
    DbDefiner dbfv = conf.getDbDefiner();
    Connection conn = conf.getConnection();
    try{
      //表是否存在
      assertEquals(true,dbfv.tableExists(conn, null, tablename));
      //表或视图是否存在
      assertEquals(true,dbfv.tableOrViewExists(conn, tablename));
      //已存在tablename的表,对其判断是否是视图
      assertEquals(false,dbfv.viewExists(conn, tablename));
      //创建一个视图
      String viewname = "v_"+tablename;
      if(dbfv.viewExists(conn, viewname))
        dbfv.dropView(conn, viewname);
      dbfv.createView(conn, viewname, null, "select * from "+tablename);
      assertEquals(true,dbfv.viewExists(conn, viewname));
      
      //创建同视图名的表，看是否出异常
      try{
        dbfv.clearDefineInfo();
        dbfv.defineStringField("fieldname", 20, null, true, false);
        //存在同名的视图，出异常
        dbfv.createTable(conn, viewname,true);
        assertEquals(true, false);
      }catch(Exception ex){
        assertEquals(true, true);
      }
      try{
      try{
        dbfv.clearDefineInfo();
        dbfv.defineStringField("fieldname", 20, null, true, false);
        //存在同名的视图，创建更名，不出异常；
        String vname = dbfv.createTable(conn, viewname,false);
        assertEquals(true, true);
        assertEquals(true,dbfv.tableExists(conn, null, vname));
      }catch(Exception ex){
        assertEquals(true, false);
      }
      }finally{
        dbfv.dropView(conn, viewname);
      }
    }finally{
      if(conn!=null) conn.close();
    }
    dropTable(tablename);
  }
  
  public void testBigVarchar() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Dialect dl = getConnectionFactory().getDialect();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.defineStringField("STR_",  1000, null, true, false);
      dbv.createTable(conn,null,tablename);
    }finally{
      if(conn!=null)
        conn.close();
    }
    TableMetaData tbm = dl.createDbMetaData().getTableMetaData(tablename);
    assertEquals(1000,tbm.getColumnLength(0));
    
    dropTable(tablename);
  }
  public void testTableMetaDoc() throws Exception{
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
      dbv.defineStringField("STR_",  50, null, true, false);
      dbv.defineFloatField("NUM_",  30, 2, null, true, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.defineMemoField("MEMO_",  null, true, false);
      dbv.defineBlobField("BINARY_", null, true, false);
      dbv.definePrimaryKey("ID_");
      dbv.defineIndex("I" + tablename+"AA", "(STR_,DATE_)", false);

      dbv.createTable(conn,null,tablename);
      
      Document doc = dbv.getTableMetaData(conn, tablename);
      checkTableMetaDoc(dl,doc);
      
    }finally{
      if(conn!=null)
        conn.close();
    }
    Document doc = dbv.getTableMetaData(null, tablename);
    checkTableMetaDoc(dl,doc);
    
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      Document doc2 = dbv.getTableMetaData(conn, tablename);
      assertEquals(null,doc2);
    }finally{
      if(conn!=null)
        conn.close();
    }
    Document doc2 = dbv.getTableMetaData(null, tablename);
    assertEquals(null,doc2);
    
    dropTable(tablename);
  }
  private void checkTableMetaDoc(Dialect dl, Document doc) {
    Element ss = doc.getDocumentElement();
    String tbname = ss.getAttribute("tablename");
    assertEquals(tbname,tablename);
    String pkeys = ss.getAttribute("primarykey");
    assertEquals("ID_",pkeys.toUpperCase());
    Element fields = (Element)ss.getElementsByTagName("fields").item(0);
    NodeList fds = fields.getElementsByTagName("field");
    assertEquals(7,fds.getLength());
    for(int i=0;i<fds.getLength();i++){
      Element fd = (Element)fds.item(i);
      String fdname = fd.getAttribute("fieldname");
      if(i==0){
        assertEquals("ID_",fdname.toUpperCase());
      }
    }
    Element indexes = (Element)ss.getElementsByTagName("indexes").item(0);
    NodeList inds = indexes.getElementsByTagName("index");
    assertEquals(2,inds.getLength());
    if(indexes!=null&&inds.getLength()>0){
      for(int i = 0;i<inds.getLength();i++){
        Element el = (Element)inds.item(i);
        String indexname = el.getAttribute("indexname");
        
        int unique = StrFunc.str2int(el.getAttribute("unique"),0);
        String fdss = el.getAttribute("fields").toUpperCase();
        
        if(fdss.equals("ID_")){
          assertEquals(1,unique);
          assertEquals("ID_",fdss);
        }
        else{
          assertEquals(0,unique);
          assertEquals("STR_,DATE_",fdss);
        }
      }
    }
  }
  

  public void testSetMemoField() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = getConnectionFactory().getConnection();
    try{
      if(dbv.tableExists(conn, null, "testdb"))
        dbv.dropTable(conn, null, "testdb");
      dbv.clearDefineInfo();
      dbv.defineStringField("name_", 1000, null, true, false);
      dbv.defineMemoField("memo_", null, true, false);
      dbv.createTable(conn, null, "testdb");
      
      PreparedStatement pstat = conn.prepareStatement("insert into testdb (name_,memo_)values(?,?)");
      pstat.setString(1, "abc");
      pstat.setString(2, "我的大学1");
      pstat.executeUpdate();
      pstat.close();
      
      pstat = conn.prepareStatement("select name_,memo_ from testdb where memo_ like ?");
      pstat.setString(1, "我的大学1");
      ResultSet rs = pstat.executeQuery();
      rs.next();
      assertEquals("abc",rs.getString(1));
      assertEquals("我的大学1",rs.getString(2));
      rs.close();
      pstat.close();
      
      dbv.dropTable(conn, null, "testdb");
    }finally{
      conn.close();
    }
  }
  /**
   * 使用repairTable修改表的第一个字段名字，Oracle 上 有问题；
   * 原因是EasyDbDefiner类中的checkModify方法，具体原因看代码注释；
   * @throws Exception
   */
  public void testRepairTableRenameField() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Dialect dl = getConnectionFactory().getDialect();
    Connection conn = getConnectionFactory().getConnection();
    try{
      if(dbv.tableExists(conn, null, "testdb"))
        dbv.dropTable(conn, null, "testdb");
      dbv.clearDefineInfo();
      dbv.defineStringField("name_", 1000, null, true, false);
      dbv.defineMemoField("memo_", null, true, false);
      dbv.createTable(conn, null, "testdb");
      
      Document doc = dbv.getTableMetaData(conn, "testdb");
      Element root = doc.getDocumentElement();
      Element fields = (Element)root.getElementsByTagName("fields").item(0);
      NodeList fds = fields.getElementsByTagName("field");
      for(int i=fds.getLength()-1;i>=0;i--){
        Element fd = (Element)fds.item(i);
        String fname = fd.getAttribute("fieldname");
        if(fname!=null&&fname.equalsIgnoreCase("memo_")){
          fd.setAttribute("newfieldname","name2_");
        }
        if(fname!=null&&fname.equalsIgnoreCase("name_")){
          fd.setAttribute("defaultvalue","'aa'");
        }
      }
      dbv.repairTable(conn, doc);
      TableColumnMetaData col = dl.getTableColumnMetaData("testdb", "name2_");
      assertEquals(false,col==null);
      TableColumnMetaData col2 = dl.getTableColumnMetaData("testdb", "name_");
      assertEquals("aa",col2.getDefaultValue());
      dbv.dropTable(conn, null, "testdb");
    }finally{
      conn.close();
    }
  }
  
  /**
   * 大表的修改，比如目的表有700个字段，而源表也有700个字段，但是字段名不同的有600个；
   * 原来的实现是先在目的表上增加600个字段，修改相同的100个字段属性，再删除原有的600个字段；
   * 这会造成表字段数超过1000个的情况，修改失败；
   * 现在改为：先删除那些无用的字段；
  * @throws SQLException 
   */
  /*public void testRepairTableForBigTable() throws Exception {
    TableMetaDataHelper srcMeta = new TableMetaDataHelper("test_1");
    for(int i=0;i<500;i++){
      srcMeta.addColumn(new TableColumnMetaDataHelper("newfield"+i,Types.VARCHAR,20,0,true,false,null,null));
    }
    Document xml = srcMeta.getXml();
    
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = getConnectionFactory().getConnection();
    try{
      if(dbv.tableExists(conn, null, "test_1"))
            dbv.dropTable(conn, null, "test_1");
      dbv.clearDefineInfo();
      for(int i=0;i<700;i++){
        dbv.defineStringField("field"+i, 20, null, true, false);
      }
      dbv.createTable(conn, null, "test_1");
      
      dbv.repairTable(conn, xml);
      
      dbv.dropTable(conn, null, "test_1");
    }finally{
      conn.close();
    }
  }*/
  
  /**
   * 测试新改动的repairTable方法；
   * @throws Exception
   */
  public void testRepairTable2() throws Exception{
    InputStream in = getClass().getResourceAsStream("table2.xml");//文件中的tablename=t_test_1
    BufferedReader reader = new BufferedReader(new InputStreamReader(in,"utf-8"));
    String xmlstr=StmFunc.reader2str(reader);
    Document doc = XmlFunc.getDocument(xmlstr);
    
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = getConnectionFactory().getConnection();
    try{
      Dialect dl = SqlFunc.createDialect(conn);
      DataBaseInfo db = dl.getDataBaseInfo();
      
      //不指定表名
      if(dbv.tableExists(conn, null, "t_test_1"))
        dbv.dropTable(conn, null, "t_test_1");
      String tn = dbv.repairTable(conn, doc, null, true);
      assertEquals(getTableNameNoSchema(tn),"t_test_1");
      assertEquals(true,dbv.tableExists(conn, null, "t_test_1"));
      
      //再次调用，修改结构；
      dbv.repairTable(conn, doc, null, true);
      
      dbv.dropTable(conn, null, "t_test_1");
      
      //指定表名
      if(dbv.tableExists(conn, null, "t_test_2"))
        dbv.dropTable(conn, null, "t_test_2");
      tn = dbv.repairTable(conn, doc, "t_test_2", true);
      assertEquals(getTableNameNoSchema(tn),"t_test_2");
      assertEquals(true,dbv.tableExists(conn, null, "t_test_2"));
      dbv.dropTable(conn, null, "t_test_2");
      
      tn = dbv.createTable(conn, doc, null,true, true);
      assertEquals(getTableNameNoSchema(tn),"t_test_1");
      assertEquals(true,dbv.tableExists(conn, null, "t_test_1"));
      
      //不自动更名，t_test_1 已存在，创建出异常；
      try{
      tn = dbv.createTable(conn, doc, null,false, true);
      assertEquals(false,true);
      }catch(Exception ex){
        assertEquals(true,true);
      }
      //设置自动更名
      tn = dbv.createTable(conn, doc, null,true, true);
      assertEquals(false, "t_test_1".equalsIgnoreCase(tn));
      assertEquals(true,dbv.tableExists(conn, null, tn));
      dbv.dropTable(conn, null, tn);
    }finally{
      conn.close();
    }
  }
  
  protected String getTableNameNoSchema(String tbname){
	  int idx = tbname.lastIndexOf(".");
	  if(idx<0){
		  return tbname;
	  }
	  return tbname.substring(idx+1);
  }
  
  /**
   * 测试主键，字段的唯一属性，索引设置有重复的情况下建表，修改表；
   * 比如：
   * 主键字段，设置唯一；
   * 主键字段，设置索引；
   * 唯一字段，设置索引；
   * @throws Exception 
   */
  public void testRepairTable3() throws Exception{
    InputStream in = getClass().getResourceAsStream("table3.xml");//文件中的tablename=t_test_1
    BufferedReader reader = new BufferedReader(new InputStreamReader(in,"utf-8"));
    String xmlstr=StmFunc.reader2str(reader);
    Document doc = XmlFunc.getDocument(xmlstr);
    String tbname = "t_test_3";
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = getConnectionFactory().getConnection();
    try{
      Dialect dl = SqlFunc.createDialect(conn);
      DataBaseInfo db = dl.getDataBaseInfo();
      
      //不指定表名
      if(dbv.tableExists(conn, null, tbname))
        dbv.dropTable(conn, null, tbname);
      String tn = dbv.repairTable(conn, doc, null, true);
      assertEquals(getTableNameNoSchema(tn),tbname);
      assertEquals(true,dbv.tableExists(conn, null, tbname));
      
      TableMetaDataHelper tmd = (TableMetaDataHelper)dl.createDbMetaData(conn).getTableMetaData(tbname);
      String[] keys = tmd.getPrimaryKey();
      assertEquals(1,keys.length);
      assertEquals("parentdir",keys[0].toLowerCase());
      tmd.getColumns();
      assertEquals(true,tmd.getColumn("parentdir").isUnique());
      assertEquals(true,tmd.getColumn("filename").isUnique());
      assertEquals(true,tmd.getColumn("filename2").isUnique());
    }finally{
      conn.close();
    }
  }
  

	/**
	 * 大表的修改，比如目的表有700个字段，而源表也有700个字段，但是字段名不同的有600个；
	 * 原来的实现是先在目的表上增加600个字段，修改相同的100个字段属性，再删除原有的600个字段；
	 * 这会造成表字段数超过1000个的情况，修改失败；
	 * 现在改为：先删除那些无用的字段；
	* @throws SQLException 
	 */
	public void testRepairTableForBigTable() throws Exception {
		/*TableMetaDataHelper srcMeta = new TableMetaDataHelper("test_1");
		for(int i=0;i<500;i++){
			srcMeta.addColumn(new TableColumnMetaDataHelper("newfield"+i,Types.VARCHAR,20,0,true,false,null,null));
		}
		Document xml = srcMeta.getXml();
		
		DbDefiner dbv = getConnectionFactory().getDbDefiner();
		Connection conn = getConnectionFactory().getConnection();
		try{
			if(dbv.tableExists(conn, null, "test_1"))
		        dbv.dropTable(conn, null, "test_1");
			dbv.clearDefineInfo();
			for(int i=0;i<700;i++){
				dbv.defineStringField("field"+i, 20, null, true, false);
			}
			dbv.createTable(conn, null, "test_1");
			
			dbv.repairTable(conn, xml);
			
			dbv.dropTable(conn, null, "test_1");
		}finally{
			conn.close();
		}*/
	}
  

  public void testRepairTable() throws Exception{
    InputStream in = getClass().getResourceAsStream("table.txt");
    BufferedReader reader = new BufferedReader(new InputStreamReader(in,"utf-8"));
    String xmlstr=StmFunc.reader2str(reader);
    Document doc = XmlFunc.getDocument(xmlstr);
    Document doc2 = XmlFunc.getDocument(xmlstr);
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = getConnectionFactory().getConnection();
    Dialect dl = SqlFunc.createDialect(conn);
    DataBaseInfo db = dl.getDataBaseInfo();
    try{
      //删除表
      if(dbv.tableExists(conn, null, "test_1"))
        dbv.dropTable(conn, null, "test_1");
      //创建表；
      dbv.repairTable(conn, doc);
      /**
       * 对已存在的表，再次创建，产生新的表名；
       */
      String newtbname = dbv.createTable(conn, doc, null,true, true);
      assertEquals(false, "test_1".equalsIgnoreCase(newtbname));
      assertEquals(true,dbv.tableExists(conn, null, newtbname));
      dbv.dropTable(conn, null, newtbname);
      //修改表结构
      Element root = doc2.getDocumentElement();
      Element fields = (Element)root.getElementsByTagName("fields").item(0);
      //增加字段
      Element field = doc2.createElement("field");
      field.setAttribute("fieldname", "field1");
      field.setAttribute("sqltype", String.valueOf('C'));
      field.setAttribute("len", String.valueOf(20));
      field.setAttribute("nullable", "1");
      field.setAttribute("unique", "1");
      fields.appendChild(field);
      
      Element field2 = doc2.createElement("field");
      field2.setAttribute("fieldname", "field2");
      field2.setAttribute("sqltype", String.valueOf('I'));
      field2.setAttribute("len", String.valueOf(20));
      field2.setAttribute("nullable", "1");
      fields.appendChild(field2);
      
      Element field3 = doc2.createElement("field");
      field3.setAttribute("fieldname", "field3");
      field3.setAttribute("sqltype", String.valueOf('N'));
      field3.setAttribute("len", String.valueOf(20));
      field3.setAttribute("scale", String.valueOf(2));
      field3.setAttribute("nullable", "0");
      field3.setAttribute("defaultvalue", "1");
      fields.appendChild(field3);
      
      Element field4 = doc2.createElement("field");
      field4.setAttribute("fieldname", "field4");
      field4.setAttribute("sqltype", String.valueOf('D'));
      field4.setAttribute("nullable", "0");
      field4.setAttribute("unique", "1");
      fields.appendChild(field4);
      
      dbv.repairTable(conn, doc2);
      assertEquals(true,dbv.colExists(conn, "test_1", "field1"));
      assertEquals(true,dbv.colExists(conn, "test_1", "field2"));
      assertEquals(true,dbv.colExists(conn, "test_1", "field3"));
      assertEquals(true,dbv.colExists(conn, "test_1", "field4"));
      TableColumnMetaData[] cols = dl.createDbMetaData().getTableMetaData("test_1").getColumns();
      for(int i=0;i<cols.length;i++){
        TableColumnMetaData col = cols[i];
        if (col.getName().equalsIgnoreCase("field1")) {
          assertEquals('C', SqlFunc.getSubsectionType(col.getType()));
          assertEquals(20, col.getLen());
          if(db.isDb2() || db.isTeradata()){
            //db2唯一建和主键不允许为空值；
            assertEquals(false, col.isNullable());
          }else
          assertEquals(true, col.isNullable());
          assertEquals(true, col.isUnique());
        }
        if (col.getName().equalsIgnoreCase("field2")) {
          char t = SqlFunc.getSubsectionType(col.getType());
          assertEquals(true, t=='I'||t=='N');
          //assertEquals(20, col.getLen()); int没有长度
          assertEquals(true, col.isNullable());
          assertEquals(false, col.isUnique());
        }
        if (col.getName().equalsIgnoreCase("field3")) {
          assertEquals('N', SqlFunc.getSubsectionType(col.getType()));
          assertEquals(20, col.getLen());
          assertEquals(2, col.getScale());
          assertEquals(false, col.isNullable());
          assertEquals(false, col.isUnique());
          assertEquals(1,Double.parseDouble(col.getDefaultValue().trim()),0);
        }
        if (col.getName().equalsIgnoreCase("field4")) {
          assertEquals('D', SqlFunc.getType(col.getType()));
          assertEquals(false, col.isNullable());
          assertEquals(true, col.isUnique());
        }
      }
      
      //修改，删除字段
      NodeList fds = fields.getElementsByTagName("field");
      for(int i=fds.getLength()-1;i>=0;i--){
        Element fd = (Element)fds.item(i);
        String fname = fd.getAttribute("fieldname");
        if(fname!=null&&fname.equalsIgnoreCase("field4")){
          fields.removeChild(fd);
        }
        if(fname!=null&&fname.equalsIgnoreCase("field3")){
          fields.removeChild(fd);
        }
        if(fname!=null&&fname.equalsIgnoreCase("field2")){
          //将可以为空，变成不为空
          fd.setAttribute("nullable", "0");
          //将不唯一，变成唯一
          fd.setAttribute("unique", "1");
        }
        if(fname!=null&&fname.equalsIgnoreCase("field1")){
          //将唯一，改为不唯一
          fd.setAttribute("unique", "0");
        }
        if(fname!=null&&fname.equalsIgnoreCase("zb1")){
          //修改字段名
          fd.setAttribute("newfieldname","zb2");
          //修改默认值
          fd.setAttribute("defaultvalue", "20");
        }
        if(fname!=null&&fname.equalsIgnoreCase("clob_")){
          //修改字段名,大小写
          fd.setAttribute("fieldname", "Clob_");
          fd.setAttribute("srcfieldname","clob_");
        }
        if(fname!=null&&fname.equalsIgnoreCase("userid_")){
          //第一个字段被改名，同时修改字段描述
          /**
           * userid_是主键之一，DB2在修改时，需要创建表，建主键时需要注意；
           */
          fd.setAttribute("newfieldname","userid2_");
          fd.setAttribute("fielddesc", "单位代码2");
        }
        //将已有的字段描述设置为空；
        if(fname!=null&&fname.equalsIgnoreCase("bbq_")){
          //修改字段名同时修改字段描述
          fd.setAttribute("newfieldname","bbq2_");
          fd.setAttribute("fielddesc", "");
        }
      }
      
      dbv.repairTable(conn, doc2);
      if(!dl.getDataBaseInfo().isDb2()){//db2不支持删除字段
      assertEquals(false,dbv.colExists(conn, "test_1", "field3"));
      assertEquals(false,dbv.colExists(conn, "test_1", "field4"));
      assertEquals(false,dbv.colExists(conn, "test_1", "zb1"));
      }
      assertEquals(true,dbv.colExists(conn, "test_1", "zb2"));
      assertEquals(true,dbv.colExists(conn, "test_1", "Clob_"));
      cols = dl.createDbMetaData().getTableMetaData("test_1").getColumns();
      for(int i=0;i<cols.length;i++){
        TableColumnMetaData col = cols[i];
        if (col.getName().equalsIgnoreCase("field1")) {
          assertEquals('C', SqlFunc.getSubsectionType(col.getType()));
          assertEquals(20, col.getLen());

          assertEquals(true, col.isNullable());

          assertEquals(false, col.isUnique());
        }
        if (col.getName().equalsIgnoreCase("field2")) {
          char t = SqlFunc.getSubsectionType(col.getType());
          assertEquals(true, t=='I'||t=='N');
          //assertEquals(20, col.getLen());数值不比较长度
          assertEquals(false, col.isNullable());
          assertEquals(true, col.isUnique());
        }
        if (col.getName().equalsIgnoreCase("field1")) {
          assertEquals(false, col.isUnique());
        }
        if (col.getName().equalsIgnoreCase("zb2")) {
          assertEquals(20,Double.parseDouble(col.getDefaultValue()),0);
        }
        //if (!db.isMssql()&&!db.isSybase()&&!db.isKingBaseES()&&!db.isDM_DBMS()) {//sqlserver不支持字段注释
        if (dl.supportsTableColumnComment()) {
          if (col.getName().equalsIgnoreCase("userid2_")) {
            assertEquals("单位代码2", col.getDesc());
          }
          if (col.getName().equalsIgnoreCase("bbq2_")) {
            assertEquals(true, StrFunc.isNull(col.getDesc()));
          }
        }
      }
      dbv.dropTable(conn, null, "test_1");
    }finally{
      conn.close();
    }
    
  }
  
  public void testSetObjectChinaChar() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = getConnectionFactory().getConnection();
    try{
      if(dbv.tableExists(conn, null, "testdb"))
        dbv.dropTable(conn, null, "testdb");
      dbv.clearDefineInfo();
      dbv.defineStringField("name_", 10, null, true, false);
      dbv.defineFloatField("num_", 30, 2, null, true, false);
      dbv.createTable(conn, null, "testdb");
      
      Statement stat = conn.createStatement();
      stat.executeUpdate("insert into testdb (name_,num_)values('四川',0.23)");
      
      ResultSet rs = stat.executeQuery("select name_,num_ from testdb order by name_");
      assertEquals(true,rs.next());
      
      stat.close();
      
      PreparedStatement pstat = conn.prepareStatement("delete from testdb where name_=?");
      pstat.setString(1, "四川");
      assertEquals(1,pstat.executeUpdate());
      
      stat = conn.createStatement();
      stat.executeUpdate("insert into testdb (name_,num_)values('四川',0.23)");
      stat.close();
      
      pstat = conn.prepareStatement("select name_,num_ from testdb where name_=?");
      pstat.setObject(1, "四川");
      rs = pstat.executeQuery();
      assertEquals(true,rs.next());
      /**
       * Oracle英文数据库，删除时用 setObject 中文参数有问题；
       */
      pstat = conn.prepareStatement("delete from testdb where name_=?");
      pstat.setObject(1, "四川");
      assertEquals(1,pstat.executeUpdate());
      
      dbv.dropTable(conn, null, "testdb");
    }finally{
      conn.close();
    }
  }
  
  
  /**
   * 在oracle中，rs.getString(1)在获取number字段的值时有问题，比如“0.23”，getString会返回“.23”
   * 其他数据库没这个问题；
   * 对超大数值读取的测试
   * @throws Exception
   */
  public void testResuletGetString() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = getConnectionFactory().getConnection();
    try{
      if(dbv.tableExists(conn, null, "testdb"))
        dbv.dropTable(conn, null, "testdb");
      dbv.clearDefineInfo();
      dbv.defineStringField("name_", 10, null, true, false);
      dbv.defineFloatField("num_", 30, 2, null, true, false);
      dbv.createTable(conn, null, "testdb");
      
      Statement stat = conn.createStatement();
      stat.executeUpdate("insert into testdb (name_,num_)values('aa',0.23)");
      stat.executeUpdate("insert into testdb (name_,num_)values('bb',null)");
      stat.executeUpdate("insert into testdb (name_,num_)values('cc',210005000004723196)");
      ResultSet rs = stat.executeQuery("select name_,num_ from testdb order by name_");
      rs.next();
      //通过连接池获取值
      assertEquals("0.23",rs.getString(2));
      assertEquals("0.23",rs.getString("num_"));
      assertEquals("0.23",rs.getObject(2).toString());
      assertEquals("0.23",rs.getObject("num_").toString());
      
      Dialect dl = getConnectionFactory().getDialect();
      DataBaseInfo dbinf = dl.getDataBaseInfo();
      //通过原始的jdbc接口获取值
      PooledResultSet prs = (PooledResultSet)rs;
      ResultSet _rs = prs.getResultSet();
      
      assertEquals(dbinf.isOracle()?".23":"0.23",_rs.getString(2));
      assertEquals(dbinf.isOracle()?".23":"0.23",_rs.getString("num_"));
      assertEquals("0.23",_rs.getObject(2).toString());
      assertEquals("0.23",_rs.getObject("num_").toString());
      
      rs.next();
      assertEquals(null,rs.getString(2));
      assertEquals(null,rs.getString("num_"));
      
      rs.next();
      assertEquals("210005000004723196",rs.getString(2).substring(0,18));
      assertEquals("210005000004723196",rs.getString("num_").substring(0,18));
      assertEquals("210005000004723196",rs.getObject(2).toString().substring(0,18));
      assertEquals("210005000004723196",rs.getObject("num_").toString().substring(0,18));
      stat.close();
      
      dbv.dropTable(conn, null, "testdb");
    }finally{
      conn.close();
    }
  }
  /**
   * 判断表是否存在，getTables(,tablename,) 匹配表名类似like；
例：数据库中存在表：testaa,   这时判断表名test_a是否存在，却返回true
   * @throws Exception
   */
  public void testTableExist() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = getConnectionFactory().getConnection();
    try{
      if(dbv.tableExists(conn, null, "testaa"))
        dbv.dropTable(conn, null, "testaa");
      dbv.clearDefineInfo();
      dbv.defineStringField("name_", 10, null, true, false);
      dbv.createTable(conn, null, "testaa");
      
      assertEquals(true,dbv.tableExists(conn, null, "testaa"));
      
      if(dbv.tableExists(conn, null, "test_a"))
        dbv.dropTable(conn, null, "test_a");
      //存在testaa,  test_a不存在
      assertEquals(false,dbv.tableExists(conn, null, "test_a"));
      //创建test_a
      dbv.clearDefineInfo();
      dbv.defineStringField("name_", 10, null, true, false);
      dbv.createTable(conn, null, "test_a");
      
      assertEquals(true,dbv.tableExists(conn, null, "test_a"));
      dbv.dropTable(conn, null, "test_a");
      assertEquals(false,dbv.tableExists(conn, null, "test_a"));
      dbv.dropTable(conn, null, "testaa");
      
      //测试表名大小写
      if(dbv.tableExists(conn, null, "tesTaa"))
        dbv.dropTable(conn, null, "tesTaa");
      dbv.clearDefineInfo();
      dbv.defineStringField("name_", 10, null, true, false);
      dbv.createTable(conn, null, "tesTaa");
      
      assertEquals(true,dbv.tableExists(conn, null, "tesTaa"));
      Dialect dl = getConnectionFactory().getDialect();
      DataBaseInfo db = dl.getDataBaseInfo();
      if(db.isFieldCaseSensitive())
        assertEquals(false,dbv.tableExists(conn, null, "testaa"));
      else assertEquals(true,dbv.tableExists(conn, null, "testaa"));
      dbv.dropTable(conn, null, "tesTaa");
    }finally{
      conn.close();
    }
  }
  public void testInsertIntoSepecail() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = getConnectionFactory().getConnection();
    try{
      DatabaseMetaData meta = conn.getMetaData();
      System.out.println(meta.getSearchStringEscape());
      if(dbv.tableExists(conn, null, "testaa"))
        dbv.dropTable(conn, null, "testaa");
      dbv.clearDefineInfo();
      dbv.defineStringField("name_", 100, null, true, false);
      dbv.createTable(conn, null, "testaa");
      
      assertEquals(true,dbv.tableExists(conn, null, "testaa"));
      
      Statement stat = conn.createStatement();
      try{
      String value = "/a\\c\"`\t%lkjl_a?a\r\n&bb'";
      Dialect dl = SqlFunc.createDialect(conn);
      DataBaseInfo dbinf = dl.getDataBaseInfo();
      String insertsql = "insert into testaa (name_)values('"+dl.formatConstStr(value)+"')";
      //System.out.println(value);
      //System.out.println(insertsql);
      stat.executeUpdate(insertsql);
      
      ResultSet rs = stat.executeQuery("select name_ from testaa");
      if(rs.next()){
        assertEquals(value,rs.getString(1));
      }
      
      PreparedStatement pstat = conn.prepareStatement("insert into testaa (name_)values(?)");
      pstat.setString(1,"a$$_b%\\//c's");
      pstat.addBatch();
      pstat.setString(1,"a$$1b2\\//c's");
      pstat.addBatch();
      pstat.setString(1,"'a%bc's");
      pstat.addBatch();
      pstat.setString(1,"'1a%*?ed'r");
      pstat.addBatch();
      pstat.executeBatch();
      pstat.close();
      //stat.executeUpdate("insert into testaa (name_)values('a$$_b%\\//c''s')");
      //stat.executeUpdate("insert into testaa (name_)values('a$$1b2\\//c''s')");
      
      String v = "a$$_b%\\//c's";
      String constv = dl.formatConstStr(v);
      String cond = dl.formatLikeCondition(null,v,"%");
      if(dbinf.isMysql()){
        assertEquals("'a$$$$$_b$%\\\\//c''s%' escape '$'",cond);
        assertEquals("a$$_b%\\\\//c''s",constv);
      }else{
        assertEquals("'a$$$$$_b$%\\//c''s%' escape '$'",cond);
        assertEquals("a$$_b%\\//c''s",constv);
      }
      rs = stat.executeQuery("select name_ from testaa where name_ like "+cond);
      assertEquals(true,rs.next());//第一行有数据
      assertEquals(v,rs.getString(1));
      assertEquals(false,rs.next());//没有第二行
      rs.close();
      
      testFormartLikeConditions("'","a%bc",  "'%", '@',true,stat,dl,"'a%bc's",true);
      testFormartLikeConditions("'?","%bc*",  "'*", '@',true,stat,dl,"'a%bc's",true);
      testFormartLikeConditions("'*","bc",  "'*", '@',true,stat,dl,"'a%bc's",true);
      testFormartLikeConditions("'?","%bc*",  "'*", '@',false,stat,dl,"'a%bc's",false);
      
      testFormartLikeConditions(null,"'1a*r",  null, '@',true,stat,dl,"'1a%*?ed'r",true);
      testFormartLikeConditions(null,"'1a%*r",  null, '@',true,stat,dl,"'1a%*?ed'r",true);
      testFormartLikeConditions(null,"'1a%*r",  null, '@',false,stat,dl,"'1a%*?ed'r",false);
      testFormartLikeConditions("'1%",null,  null, '@',true,stat,dl,"'1a%*?ed'r",true);
      testFormartLikeConditions("'1%",null,  null, '@',false,stat,dl,"'1a%*?ed'r",true);
      testFormartLikeConditions("'?a%r",null,  null, '@',true,stat,dl,"'1a%*?ed'r",true);
      testFormartLikeConditions("'?a%r",null,  null, '@',false,stat,dl,"'1a%*?ed'r",true);
      testFormartLikeConditions(null,null,  "'?a%r", '@',true,stat,dl,"'1a%*?ed'r",true);
      testFormartLikeConditions(null,null,  "'?a%r", '@',false,stat,dl,"'1a%*?ed'r",true);
      
      testFormartLikeConditions("'","1a*r",  null, '@',true,stat,dl,"'1a%*?ed'r",true);
      testFormartLikeConditions("'","1a*",  "r", '@',true,stat,dl,"'1a%*?ed'r",true);
      testFormartLikeConditions("'","1a*",  "r", '@',false,stat,dl,"'1a%*?ed'r",false);
      testFormartLikeConditions("'","1a%*?",  "%'r", '@',false,stat,dl,"'1a%*?ed'r",true);
      }finally{
        stat.close();
      }
      
    }finally{
      conn.close();
    }
    dropTable("testaa");
  }
  
  private void testFormartLikeConditions(String prefix,String likecond,String sufix,char escape,
		  boolean escapeWildcard,Statement stat,Dialect dl,String value,boolean haveData) throws SQLException{
	  String cond = dl.formatLikeCondition(prefix,likecond,  sufix, escape,escapeWildcard);
      ResultSet rs = stat.executeQuery("select name_ from testaa where name_ like "+cond); 
      if(haveData){
      assertEquals(true,rs.next());//第一行有数据
      assertEquals(value,rs.getString(1));
      assertEquals(false,rs.next());//没有第二行
      }else{
    	  assertEquals(false,rs.next());
      }
      rs.close();
  }
  
  
  public void testCreateTableMaxField() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Dialect dl = getConnectionFactory().getDialect();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }

      for(int i=0;i<1000;i++){
        //dbv.defineStringField("field"+i, 100, null, true, false);
        dbv.defineFloatField("field"+i, 10, 2, null, true, false);
      }

      String tbname = dbv.createTable(conn,null,tablename);
      assertEquals(this.getTableNameNoSchema(tbname), tablename);
      assertEquals(true, dbv.tableExists(conn,null,tablename));
    }finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(tablename);
  }
  /**
   * 测试正确参数创建表
   * @throws Exception
   */
  public void testCreateTable() throws Exception{
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
      dbv.defineIntField("INT_2", 30, "0", true, false);
      dbv.defineIntField("INT_3", 30, null, false, true);
      dbv.defineIntField("INT_4", 30, "0", false, false);
      dbv.defineStringField("STR_",  50, null, true, false);
      dbv.defineStringField("STR_2",  50, null, false, false);
      dbv.defineStringField("STR_3",  50, "'aa'", true, false);
      dbv.defineStringField("STR_4",  50, "'aa'", false, false);
      dbv.defineFloatField("NUM_",  30, 2, null, true, false);
      dbv.defineFloatField("NUM_2",  30, 2, null, false, false);
      dbv.defineFloatField("NUM_3",  30, 2, "2.3", true, false);
      dbv.defineFloatField("NUM_4",  30, 2, "23.9", false, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.defineDateField("DATE_2", null, false, false);
      dbv.defineDateField("DATE_3", dl.funcToday(), true, false);
      dbv.defineDateField("DATE_4", dl.funcToday(), false, false);
      dbv.defineTimeField("TIME_", null, true, false);
      dbv.defineTimeField("TIME_2", null, false, false);
      dbv.defineTimeStampField("TIMESTAMP_",  null, true,false);
      dbv.defineTimeStampField("TIMESTAMP_2",  null, false,false);
      dbv.defineTimeStampField("TIMESTAMP_3",  dl.funcNow(), true,false);
      dbv.defineTimeStampField("TIMESTAMP_4",  dl.funcToDateTime("20080101 00:01:01"), false,false);
      dbv.defineLogicField("LOGIC_", null, true, false);
      dbv.defineLogicField("LOGIC_2", null, false, false);
      dbv.defineLogicField("LOGIC_3", "'0'", true, false);
      dbv.defineLogicField("LOGIC_4", "'0'", false, false);
      dbv.defineMemoField("MEMO_",  null, true, false);
      dbv.defineBlobField("BINARY_", null, true, false);
      dbv.definePrimaryKey("ID_");
      dbv.defineIndex("I" + tablename+"aa", "(STR_,DATE_)", false);

      String tbname = dbv.createTable(conn,null,tablename);
      assertEquals(getTableNameNoSchema(tbname), tablename);
      assertEquals(true, dbv.tableExists(conn,null,tablename));
      String[] indexs = dbv.getIndexNames();
    }finally{
      if(conn!=null)
        conn.close();
    }
    TableMetaData tmd = dl.createDbMetaData().getTableMetaData(tablename);
    assertEquals(tmd.getColumnCount(),29);
    TableColumnMetaData id_ = dl.getTableColumnMetaData(tablename, "ID_");
    assertEquals(true,id_.isAutoInc());
    TableColumnMetaData int_ = dl.getTableColumnMetaData(tablename, "INT_");
    assertEquals(false,int_.isAutoInc());
    assertEquals(true,int_.isNullable());
    assertEquals(false,int_.isUnique());
    TableColumnMetaData int_3 = dl.getTableColumnMetaData(tablename, "INT_3");
    assertEquals(false,int_3.isAutoInc());
    assertEquals(false,int_3.isNullable());
    assertEquals(true,int_3.isUnique());
    assertEquals(null,int_3.getDefaultValue());
    TableColumnMetaData int_4 = dl.getTableColumnMetaData(tablename, "INT_4");
    assertEquals(false,int_4.isAutoInc());
    assertEquals(false,int_4.isNullable());
    assertEquals(false,int_4.isUnique());
    assertEquals("0",int_4.getDefaultValue().trim());//oracle测试时，取出来是"0 "
    
    TableColumnMetaData date_ = dl.getTableColumnMetaData(tablename, "DATE_");
    assertEquals('D',SqlFunc.getType(date_.getType()));
    TableColumnMetaData TIMESTAMP_ = dl.getTableColumnMetaData(tablename, "TIMESTAMP_");
    assertEquals('D',SqlFunc.getType(TIMESTAMP_.getType()));
    
    dropTable(tablename);
  }
  /**
   * 测试创建表的异常情况
   * @throws Exception 
   */
  public void testCreateTable2() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Dialect dl = getConnectionFactory().getDialect();
    DataBaseInfo dbinf = getConnectionFactory().getDbType();
    Connection conn = null;
    String createtbname = null;
    //测试是否能创建大小写不同，却是相同字符的字段；
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineStringField("str_", 20, null, true, false);
      dbv.defineStringField("Str_", 20, null, true, false);
      createtbname = dbv.createTable(conn, tablename, true);
      if(dbinf.isSybase())
        assertEquals(true,true);
      else  assertEquals(true,false);
    }catch(Exception ex){
      if(dbinf.isSybase())
        assertEquals(true,false);
      else assertEquals(true,true);
    }
    finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(createtbname);
    
    //测试创建超长字段名，应该出异常；
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineStringField("str_aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 20, null, true, false);
      createtbname = dbv.createTable(conn, tablename, true);
      assertEquals(true,false);
    }catch(Exception ex){
      assertEquals(true,true);
    }
    finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(createtbname);
    
    //测试创建超长索引名，不出异常；
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineStringField("str_", 20, null, true, false);
      dbv.defineStringField("str_2", 20, null, true, false);
      dbv.defineIndex("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "(str_,str_2)", true);
      createtbname = dbv.createTable(conn, tablename, true);
      assertEquals(true,true);
    }catch(Exception ex){
      assertEquals(true,false);
    }
    finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(createtbname);
    
    //测试创建超长索引名，不出出异常；
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineStringField("str_", 20, null, true, false);
      dbv.defineStringField("str_2", 20, null, true, false);
      dbv.defineIndex("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "(str_,str_2)", true);
      createtbname = dbv.createTable(conn, tablename, false);
      assertEquals(true,true);
    }catch(Exception ex){
      assertEquals(true,false);
    }
    finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(createtbname);
    
  //测试创建已存在的索引名，不出异常；
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineStringField("str_", 20, null, true, false);
      dbv.defineStringField("str_2", 20, null, true, false);
      dbv.defineIndex("aaa", "(str_,str_2)", true);
      dbv.defineIndex("aaa", "(str_)", false);
      createtbname = dbv.createTable(conn, tablename, true);
      assertEquals(true,true);
    }catch(Exception ex){
      assertEquals(true,false);
    }
    finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(createtbname);
  //测试创建已存在的索引名，不出异常；
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineStringField("str_", 20, null, true, false);
      dbv.defineStringField("str_2", 20, null, true, false);
      dbv.defineIndex("aaa", "(str_,str_2)", true);
      dbv.defineIndex("aaa", "(str_)", false);
      createtbname = dbv.createTable(conn, tablename, false);
      assertEquals(true,true);
    }catch(Exception ex){
      assertEquals(true,false);
    }
    finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(createtbname);
    
    //测试创建不合法的索引名，不出异常；
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineStringField("str_", 20, null, true, false);
      dbv.defineStringField("str_2", 20, null, true, false);
      dbv.defineIndex("1aaa", "(str_,str_2)", true);
      createtbname = dbv.createTable(conn, tablename, true);
      assertEquals(true,true);
    }catch(Exception ex){
      assertEquals(true,false);
    }
    finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(createtbname);
    
    //测试创建不合法的索引名，不出异常；
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineStringField("str_", 20, null, true, false);
      dbv.defineStringField("str_2", 20, null, true, false);
      dbv.defineIndex("1aaa", "(str_,str_2)", true);
      createtbname = dbv.createTable(conn, tablename, false);
      assertEquals(true,true);
    }catch(Exception ex){
      assertEquals(true,false);
    }
    finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(createtbname);
    
    //测试创建不合法的索引名，不出异常；
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineStringField("str_", 20, null, true, false);
      dbv.defineStringField("str_2", 20, null, true, false);
      dbv.defineIndex(null, "(str_,str_2)", true);
      createtbname = dbv.createTable(conn, tablename, false);
      assertEquals(true,true);
    }catch(Exception ex){
      assertEquals(true,false);
    }
    finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(createtbname);
    
  //测试创建不合法的索引名，不出异常；
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineStringField("str_", 20, null, true, false);
      dbv.defineStringField("str_2", 20, null, true, false);
      dbv.defineIndex("Iindexnameaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "(str_,str_2)", true);
      createtbname = dbv.createTable(conn, tablename, false);
      assertEquals(true,true);
    }catch(Exception ex){
      assertEquals(true,false);
    }
    finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(createtbname);
    
    //测试创建不合法的表名，出异常；
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,"2aaf")){
        dbv.dropTable(conn,null,"2aaf");
      }
      dbv.clearDefineInfo();
      dbv.defineStringField("str_", 20, null, true, false);
      dbv.defineStringField("str_2", 20, null, true, false);
      createtbname = dbv.createTable(conn, "2aaf", true);
      if(dbinf.isMysql())//mysql可以创建数值开头的表名...
        assertEquals(true,true);
      else assertEquals(true,false);
    }catch(Exception ex){
      if(dbinf.isMysql())
        assertEquals(true,false);
      else assertEquals(true,true);
    }
    finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(createtbname);
    
  //测试创建不合法的表名，不出异常；
    
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,"2aaf")){
        dbv.dropTable(conn,null,"2aaf");
      }
      dbv.clearDefineInfo();
      dbv.defineStringField("str_", 20, null, true, false);
      dbv.defineStringField("str_2", 20, null, true, false);
      createtbname = dbv.createTable(conn, "2aaf", false);
      assertEquals(true,true);
    }catch(Exception ex){
      assertEquals(true,false);
    }
    finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(createtbname);
    
  //测试创建超长的表名，出异常；
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,"taaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaafsssasssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss")){
        dbv.dropTable(conn,null,"taaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaafsssasssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss");
      }
      dbv.clearDefineInfo();
      dbv.defineStringField("str_", 20, null, true, false);
      dbv.defineStringField("str_2", 20, null, true, false);
      dbv.createTable(conn, "taaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaafsssasssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss", true);
      assertEquals(true,false);
    }catch(Exception ex){
      assertEquals(true,true);
    }
    finally{
      if(conn!=null)
        conn.close();
    }
    
  //测试创建超长的表名，不出异常；
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,"taaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaf")){
        dbv.dropTable(conn,null,"taaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaf");
      }
      dbv.clearDefineInfo();
      dbv.defineStringField("str_", 20, null, true, false);
      dbv.defineStringField("str_2", 20, null, true, false);
      createtbname = dbv.createTable(conn, "taaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaf", false);
      assertEquals(true,true);
    }catch(Exception ex){
      assertEquals(true,false);
    }
    finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(createtbname);
    
    
  }
  /**
   * 创建表时，如果中间出异常，比如表创建了，创建索引出异常了，测试是否回滚；
   * @throws Exception 
   */
  public void testCreateTable3() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Dialect dl = getConnectionFactory().getDialect();
    DataBaseInfo dbinf = getConnectionFactory().getDbType();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineStringField("str_", 20, null, true, false);
      dbv.defineStringField("str_2", 20, null, true, false);
      dbv.defineIndex("1aaa", "(str_,str_2)a", true);
      dbv.createTable(conn, tablename, true);
      assertEquals(true,false);
    }catch(Exception ex){
      assertEquals(true,true);
      assertEquals(false,dbv.tableExists(conn,null,tablename));
    }
    finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(tablename);
  }
  public void testformatFieldName() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    assertEquals("abc123_",dbv.formatFieldName("abc123_", false));
    assertEquals("Fabc123_",dbv.formatFieldName("2abc123_", false));
    assertEquals("abc$123_",dbv.formatFieldName("abc$*123,_", false));   
    
    try{
      assertEquals("abc123_",dbv.formatFieldName("abc123_", true));
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
        assertEquals(true,true);
      }finally{
        stat.close();
      }
      
    }finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(tbname);
  }
  
  //测试创建关键字做字段，并修改该字段长度；
  //PASSWORD 和  OPTION 都是关键字
  public void testKeyName() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Dialect dl = getConnectionFactory().getDialect();
    DataBaseInfo dbinf = dl.getDataBaseInfo();
    Connection conn = null;
    String tbname = "TbName";
    try{
      conn = getConnectionFactory().getConnection();
      Dialect dl2 = SqlFunc.createDialect(conn);
      DatabaseMetaData md = conn.getMetaData();
      assertEquals(true,md.supportsTransactions());
      conn.setAutoCommit(false);
      if(dbv.tableExists(conn,null,tbname)){
        TableMetaData tmd = dl2.createDbMetaData().getTableMetaData(tbname);
        tmd.getColumns();
        dbv.dropTable(conn,null,tbname);
      }
      dbv.defineStringField("OPTION", 30, null, true, false);
      dbv.defineStringField("PASSWORD", 30, null, true, false);
      tbname = dbv.createTable(conn,null,tbname);
      
      Statement stat = conn.createStatement();
      try{
        stat.executeQuery("select "+SqlFunc.getColumnName(dl, dbv.formatFieldName("OPTION", false))
        		+", " + SqlFunc.getColumnName(dl, dbv.formatFieldName("PASSWORD", false))
        		+ " from "+tbname);
      }finally{
        stat.close();
      }
      //对关键字字段修改长度：改大，支持；改小报错；
      dbv.modifyColumn(conn, tbname, "OPTION", "OPTION", 'C', 50, 0, null, false, true);
      //把关键字段改为非关键字字段
      dbv.modifyColumn(conn, tbname, "OPTION", "OPTION2", 'C', 40, 0, null, false, true);
      //将非关键字字段改为关键字字段
      if(dbinf.isSybase()){
        try{
        dbv.modifyColumn(conn, tbname, "OPTION2", "OPTION", 'C', 40, 0, null, false, true);
        assertEquals(true,false);
        }catch(Exception ex){
          assertEquals("OPTION为关键字，Sybase不支持修改字段为关键字；",ex.getMessage());
        }
      }
      else dbv.modifyColumn(conn, tbname, "OPTION2", "OPTION", 'C', 40, 0, null, false, true);
      
      TableMetaData tmd = dl2.createDbMetaData().getTableMetaData(tbname);
      if(dbinf.isSybase()){
        assertEquals("OPTION2",tmd.getColumnName(0));
      }else assertEquals("OPTION",tmd.getColumnName(0).toUpperCase());
      assertEquals(40,tmd.getColumnLength(0));
      conn.commit();
      conn.setAutoCommit(true);
    }finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(tbname);
  }
  
  public void testCreateTalbeAsQureySQL() throws Exception{
    createTable();
    addData(10);
    
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Dialect dl =  getConnectionFactory().getDialect();
    String sql = dl.getCreateTableByQureySql("temptb", "select STR_,INT_,NUM_,STRBBQ_ FROM "+tablename, false);
    createTableAsSql(dbv, sql);
    
    checkData(dl,4,false);
    
    sql = dl.getCreateTableByQureySql("temptb", " SELECT STR_,INT_,NUM_,STRBBQ_ FROM "+tablename, true);
    createTableAsSql(dbv, sql);
    checkData(dl,4,true);
    
    sql = dl.getCreateTableByQureySql("temptb", tablename, false);
    createTableAsSql(dbv, sql);
    checkData(dl,20,false);
    
    sql = dl.getCreateTableByQureySql("temptb", tablename, true);
    createTableAsSql(dbv, sql);
    checkData(dl,20,true);
    
    dropTable(tablename);
    dropTable("temptb");
 }
  private void checkData(Dialect dl,int n,boolean temp) throws Exception {
    DbMetaData dbmd = dl.createDbMetaData();
    TableMetaData tmd = dbmd.getTableMetaData("temptb");
    assertEquals(tmd.getColumnCount(),n);
    if(!temp&&(!dl.getDataBaseInfo().isOracle()&&!dl.getDataBaseInfo().isDb2()))
      assertEquals(10,getCount("temptb"));
  }
  private int getCount(String tbname) throws Exception {
    Connection conn = getConnectionFactory().getConnection();
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
 private void createTableAsSql(DbDefiner dbv, String sql) throws Exception, SQLException {
   Connection conn =  getConnectionFactory().getConnection();
   try{
     if(dbv.tableExists(conn,null,"temptb")){
       dbv.dropTable(conn,null,"temptb");
     }
     Statement stat = conn.createStatement();
     try {
    	 stat.executeUpdate(sql);
    	 assertEquals(true,dbv.tableExists(conn, null, "temptb"));
     } finally {
    	 stat.close();
     }
   }finally{
     conn.close();
   }
   
 }
  public void testDescToField() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = null;
    String tbname = "testdb2";
    try{
      conn = getConnectionFactory().getConnection();
      try{
        dbv.dropTable(conn,null,tbname);
      }catch(Exception ex){}
      assertEquals(false,dbv.tableExists(conn,null,tbname));
      
      dbv.clearDefineInfo();
      dbv.defineAutoIncField("ID_", 1);
      dbv.defineField("INT_", DbDefiner.FIELD_TYPE_INT, 0, 0, null, true, false, "整型");
      dbv.defineField("STR_", DbDefiner.FIELD_TYPE_STR, 10, 0, null, true, false, "字符");
      dbv.definePrimaryKey("ID_");
      String tbname2 = dbv.createTable(conn,null,tbname);
      assertEquals(true,dbv.tableExists(conn,null,tbname2));
    }finally{
      if(conn!=null)
        conn.close();
    }
    Dialect dl =  getConnectionFactory().getDialect();
    DbMetaData dbmd = dl.createDbMetaData();
    TableMetaData tmd = dbmd.getTableMetaData(tbname);
    TableColumnMetaData[] cols = tmd.getColumns();
    if (dl.supportsTableColumnComment()) {
	    for(int i=0;i<cols.length;i++){
	      TableColumnMetaData col = cols[i];
	      if(col.getName().equalsIgnoreCase("INT_"))
	        assertEquals("整型",col.getDesc());
	      if(col.getName().equalsIgnoreCase("STR_"))
	        assertEquals("字符",col.getDesc());
	    }
    }
    dropTable(tbname);
  }
  public void testModefiyDescToField() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = null;
    String tbname = "testdb2";
    try{
      conn = getConnectionFactory().getConnection();
      try{
        dbv.dropTable(conn,null,tbname);
      }catch(Exception ex){}
      assertEquals(false,dbv.tableExists(conn,null,tbname));
      
      dbv.clearDefineInfo();
      dbv.defineAutoIncField("ID_", 1);
      dbv.defineField("INT_", DbDefiner.FIELD_TYPE_INT, 0, 0, null, true, false, "整型");
      dbv.defineField("STR_", DbDefiner.FIELD_TYPE_STR, 10, 0, null, true, false, "字符");
      dbv.definePrimaryKey("ID_");
      String tbname2 = dbv.createTable(conn,null,tbname);
      assertEquals(true,dbv.tableExists(conn,null,tbname2));
    }finally{
      if(conn!=null)
        conn.close();
    }
    
    Dialect dl =  getConnectionFactory().getDialect();
    DbMetaData dbmd = dl.createDbMetaData();
    TableMetaData tmd = dbmd.getTableMetaData(tbname);
    TableColumnMetaData[] cols = tmd.getColumns();
    if (dl.supportsTableColumnComment()) {
	    for(int i=0;i<cols.length;i++){
	      TableColumnMetaData col = cols[i];
	      if(col.getName().equalsIgnoreCase("INT_"))
	        assertEquals("整型",col.getDesc());
	      if(col.getName().equalsIgnoreCase("STR_"))
	        assertEquals("字符",col.getDesc());
	    }
    }
    conn = getConnectionFactory().getConnection();
    try{
      dbv.modifyColumnForDesc(conn, tbname, "INT_", "整型22");
    }finally{
      conn.close();
    }
    
    DbMetaData dbmd2 = dl.createDbMetaData();
    TableMetaData tmd2 = dbmd2.getTableMetaData(tbname);
    TableColumnMetaData[] cols2 = tmd2.getColumns();
    if (dl.supportsTableColumnComment()) {
	    for(int i=0;i<cols2.length;i++){
	      TableColumnMetaData col = cols2[i];
	      if(col.getName().equalsIgnoreCase("INT_"))
	        assertEquals("整型22",col.getDesc());
	      if(col.getName().equalsIgnoreCase("STR_"))
	        assertEquals("字符",col.getDesc());
	    }
    }
    dropTable(tbname);
  }
  public void testFieldLength() throws Exception{
    createTable();
    Dialect dl =  getConnectionFactory().getDialect();
    DbMetaData dbmd = dl.createDbMetaData();
    TableMetaData tmd = dbmd.getTableMetaData(tablename);
    TableColumnMetaData[] cols = tmd.getColumns();
    for(int i=0;i<cols.length;i++){
      TableColumnMetaData col = cols[i];
      if(col.getName().equalsIgnoreCase("NUM_")){
        assertEquals(18,col.getLen());
        assertEquals(2,col.getScale());
      }
      if(col.getName().equalsIgnoreCase("STR_"))
        assertEquals(20,col.getLen());
      if(col.getName().equalsIgnoreCase("ID_"))
        assertEquals(true,col.isAutoInc());
    }
    dropTable(tablename);
  }
  public void test_init() throws Exception{
    createTable();
    addData(10);
    updateDateByTimestamp();
    dropTable(tablename);
  }
  public void test_bigBlob() throws Exception{
    createTable();
    String sql = "insert into "+tablename+" (INT_,STR_,DATE_,BINARY_)values(?,?,?,?)";
    PreparedStatement pstat = null;
    Connection conn = null;
    try {
      conn = getConnectionFactory().getConnection();
      pstat = conn.prepareStatement(sql);
      pstat.setInt(1, -1);
      pstat.setString(2, "test中文");
      pstat.setDate(3, java.sql.Date.valueOf("2005-08-10"));
      InputStream bigin = getClass().getResourceAsStream("test.txt");
      
      try{
        pstat.setBinaryStream(4, bigin, -1);
      }finally{
        bigin.close();
      }
      pstat.executeUpdate();
    }finally{
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
    dropTable(tablename);
  }

  /**
   * 对setObject进行测试
   * @throws Exception 
   */
  public void test_setObjectNull() throws Exception {
    createTable();
    Connection conn = null;
    PreparedStatement pstat = null;
    String sql = "insert into "
        + tablename
        + " ("+"int_,str_,num_,date_,time_,timestamp_,logic_,memo_,binary_,character_,strbbq_,int2_".toUpperCase()+")values(?,?,?,?,?,?,?,?,?,?,?,?)";
    try {
      conn = getConnectionFactory().getConnection();
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
        // 对于形如"11.0"的字符串，插入int类型字段，测试是否出异常；
        pstat.setObject(12, "11.0");
        pstat.addBatch();
        pstat.executeBatch();
        
      //插入一行空置数据
        pstat.setInt(1, 0);
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
        pstat.setObject(12, null);
        pstat.addBatch();
        pstat.executeBatch();
        
        //测试对int类型字段插入空串
        pstat.setInt(1, 1);
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
        pstat.setObject(12, "");
        pstat.addBatch();
      pstat.executeBatch();
    }catch (Exception ex) {
      throw ex;
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
    dropTable(tablename);
  }
  /**
   * date类型，使用timestamp去set
   * @throws Exception 
   */
  private void updateDateByTimestamp() throws Exception {
    Connection conn = null;
    PreparedStatement pstat = null;
    String sql = "update "
        + tablename
        + " set DATE_=?";
    try {
      conn = getConnectionFactory().getConnection();
      pstat = conn.prepareStatement(sql);
      pstat.setTimestamp(1, java.sql.Timestamp
          .valueOf("2006-08-10 13:30:14.234"));
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
   * 测试setObject
   * @throws Exception
   */
  public void test_setObject() throws Exception{
    createTable();
    Connection conn = null;
    PreparedStatement pstat = null;
    String sql = "insert into "
      + tablename
      + " ("+"str_,date_,strbbq_,character_,num_,int_".toUpperCase()+")values(?,?,?,?,?,?)";
    try {
      conn = getConnectionFactory().getConnection();
      pstat = conn.prepareStatement(sql);
      //字符字段set数字
      pstat.setObject(1, new Double(23.4));
      //日期字段set日期字符串
      pstat.setObject(2, "20050201");
      //字符字段set日期类型
      pstat.setObject(3,  java.sql.Date.valueOf("2005-08-10"));
      //clob字段set字符串
      pstat.setObject(4, CLOB_CONTACT);
      //num字段set数字字符串
      pstat.setObject(5,"1234");
      pstat.setObject(6,"123434");
      pstat.addBatch();
      pstat.executeBatch();
      
      pstat.setObject(1, new Integer(13));
      pstat.setObject(2, "2007-02-01");
      pstat.setObject(3,  new java.sql.Timestamp(System.currentTimeMillis()));
      //clob字段set数字
      pstat.setObject(4, new Double(2345));
      //对数值类型插入空串，相当于插入空；
      pstat.setObject(5,"");
      pstat.setObject(6,new Double(5124));
      pstat.addBatch();
      pstat.executeBatch();
      
      pstat.setObject(1, new Integer(123));
      pstat.setObject(2, new java.util.Date());
      pstat.setObject(3,  new java.sql.Timestamp(System.currentTimeMillis()));
      //clob字段set日期
      pstat.setObject(4, new java.sql.Date(System.currentTimeMillis()));
      pstat.setObject(5,new Double(124));
      pstat.setObject(6,new Double(1244));
      pstat.addBatch();
      pstat.executeBatch();
      
      pstat.setObject(1, new Long("13235364764577"));
      pstat.setObject(2, "200702--");
      pstat.setObject(3,  new java.sql.Timestamp(System.currentTimeMillis()));
      pstat.setObject(4, CLOB_CONTACT);
      pstat.setObject(5,new Double(124));
      pstat.setObject(6,new Double(1524));
      pstat.addBatch();
      pstat.executeBatch();
      
      pstat.setObject(1, new Float("13235364764577.23"));
      pstat.setObject(2, "2007----");
      pstat.setObject(3,  new java.sql.Timestamp(System.currentTimeMillis()));
      pstat.setObject(4, CLOB_CONTACT);
      pstat.setObject(5,new Double(124));
      pstat.setObject(6,new Double(1264));
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
    dropTable(tablename);
  }
  
  public void test_getObject() throws Exception{
    createTable();
    addData(1);
    String sql = "select "+"int_,str_,num_,date_,time_,timestamp_,logic_,memo_,binary_,character_,strbbq_".toUpperCase()+" from "+tablename;
    Connection conn = null;
    PreparedStatement pstat = null;
    try {
      conn = getConnectionFactory().getConnection();
      pstat = conn.prepareStatement(sql);
      ResultSet rs = pstat.executeQuery();
      rs.next();
      assertEquals(rs.getObject(10), CLOB_CONTACT);
      assertEquals(rs.getObject("character_".toUpperCase()), CLOB_CONTACT);
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
    dropTable(tablename);
  }
  /**
   * 测试set大字段
   * setCharacterStream(i,x,len) x的长度往往不好确定，长度len给个最大值，看能否set成功
   * setBinaryStream(i,x,len) 同上
   * @throws Exception 
   */
  public void test_setSteam() throws Exception{
    createTable();
    
    Connection conn = null;
    String sql = "insert into "
        + tablename
        + " ("+"str_,binary_,character_,int_".toUpperCase()+")values(?,?,?,?)";
    try {
      conn = getConnectionFactory().getConnection();
      PreparedStatement pstat = conn.prepareStatement(sql);
      pstat.setString(1, "测试set大字段");
      pstat.setBinaryStream(2, getTestInputStream(), Integer.MAX_VALUE);
      pstat.setString(3,CLOB_CONTACT);
      pstat.setInt(4, 4567);
      pstat.addBatch();
      pstat.executeBatch();
      
      pstat.setString(1, "测试set大字段");
      pstat.setBinaryStream(2, getTestInputStream(), -1);
      pstat.setCharacterStream(3, new CharArrayReader(CLOB_CONTACT.toCharArray()), -1);
      pstat.setInt(4, 4568);
      pstat.addBatch();
      pstat.executeBatch();
      
      pstat.setString(1, "测试set大字段");
      pstat.setBinaryStream(2, getTestInputStream(), 100);
      BufferedReader r = new BufferedReader(new StringReader(CLOB_CONTACT));
      pstat.setCharacterStream(3, r, Integer.MAX_VALUE);
      pstat.setInt(4, 4569);
      pstat.addBatch();
      
      pstat.executeBatch();
      pstat.close();
      Statement stat = conn.createStatement();
      ResultSet rs = stat.executeQuery("select "+"str_,binary_,character_".toUpperCase()+" from "+tablename);
      while(rs.next()){
      assertEquals("测试set大字段",rs.getString(1));//测试中文
      InputStream in = rs.getBinaryStream(2);
      in = StmFunc.getUnGZIPStm(in);
      byte[] bys = StmFunc.stm2bytes(in);
      String str = new String(bys);
      assertEquals(str,getTestInputStreamStr());
      assertEquals(rs.getString(3),CLOB_CONTACT);
      }
      stat.close();
    }
    finally {
      try {
        if (conn != null) {
          conn.close();
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    dropTable(tablename);
  }
  
  /**
   * 测试大字段的读写；
   * 使用jdbc的通用方法；
   * clob字段分别使用setString和流写入的方法写入；
   * clob读取全部使用getString；
   * Oracle9i,10g测试通过；
   * @throws Exception
   */
  public void testReadClobBlobData() throws Exception {
    createTable();
    addBlobClobData();
    Connection conn = getConnectionFactory().getConnection();
    InputStream in = null;
    Statement stat = null;
    try{
      stat = conn.createStatement();
      ResultSet rs = stat.executeQuery("select "+"binary_,character_".toUpperCase()+" from "+tablename);
      while(rs.next()){
      in = rs.getBinaryStream(1);
      //in = rs.getBlob(1).getBinaryStream();
      in = StmFunc.getUnGZIPStm(in);
      
      byte[] bys = StmFunc.stm2bytes(in);
      String str = new String(bys);
      assertEquals(str,getTestInputStreamStr());
      
      assertEquals(rs.getString(2),CLOB_CONTACT);
      }
      rs.close();
      
    }finally{
      stat.close();
      conn.close();
    }
    dropTable(tablename);
  }
  
  protected void addBlobClobData() throws Exception {
    Connection conn = null;
    PreparedStatement pstat = null;
    String sql = "insert into "
        + tablename
        + " (INT_,MEMO_,BINARY_,CHARACTER_)" +
            "values(?,?,?,?)";
    try {
      conn = getConnectionFactory().getConnection();
      pstat = conn.prepareStatement(sql);
      int n = 0;
      for (int i = 0; i < 100; i++) {
        System.out.println(i+"_________________________________");
        pstat.setInt(1, i);
        pstat.setString(2, "阿斗发机adskfager lkjgerg;");
        InputStream fin = getTestInputStream();
        
        //写入clob
        String clob = CLOB_CONTACT;
        if(i%2==0){
        	//使用流写入blob
        	pstat.setBinaryStream(3, fin, fin.available());
          //使用setString
          pstat.setString(4, clob);
        }else{
        	//使用setObject写入blob
        	pstat.setObject(3, fin);
          //使用流写入
          BufferedReader r = new BufferedReader(new StringReader(clob));
          pstat.setCharacterStream(4,r,clob.length());
        }
        pstat.addBatch();
        n++;
        if(n>=1){
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
  public void testModifyColumn() throws Exception{
    createTable();
    addData(10);
    ConnectionFactory conf = getConnectionFactory();
    DbDefiner dbfv = conf.getDbDefiner();
    Connection conn = conf.getConnection();
    try {
      conn.setAutoCommit(true);
      //将date转换为timestamp
      dbfv.modifyColumn(conn, tablename, "DATE_", "DATE_", DbDefiner.FIELD_TYPE_TIMESTAMP, 0, 0, null, false, true);

      //增大字符字段长度；
      dbfv.modifyColumn(conn, tablename, "STR_", "STR_", DbDefiner.FIELD_TYPE_STR, 100, 0, null, false, true);
      Dialect dl = SqlFunc.createDialect(conn);
      TableColumnMetaData tcol = dl.getTableColumnMetaData(tablename, "STR_");
      assertEquals(100, tcol.getLen());
      //只改名：
      dbfv.modifyColumn(conn, tablename, "STR_", "STR2_", DbDefiner.FIELD_TYPE_STR, 100, 0, null, false, true);
      tcol = dl.getTableColumnMetaData(tablename, "STR_");
      assertEquals(null, tcol);
      tcol = dl.getTableColumnMetaData(tablename, "STR2_");
      assertEquals(100, tcol.getLen());

      dbfv.modifyColumn(conn, tablename, "NUM_", "NUL2_", DbDefiner.FIELD_TYPE_FLOAT, 30, 2, null, false, true);
      tcol = dl.getTableColumnMetaData(tablename, "NUM_");
      assertEquals(null, tcol);
      tcol = dl.getTableColumnMetaData(tablename, "NUL2_");
      assertEquals(30, tcol.getLen());
      
      /**
       * 主键不能修改成 不唯一
       */
      dbfv.modifyColumn(conn, tablename, "INT_", null, DbDefiner.FIELD_TYPE_INT, 30, 0, null, false, false);
      tcol = dl.getTableColumnMetaData(tablename, "INT_");
      assertEquals(true, conf.getDbType().isMysql()?true:tcol.isUnique());//mysql如果有自动增长字段，必须是该增长字段为主键；
    }
    catch (Exception ex) {
      conn.rollback();
      throw ex;
    }
    finally {
      if (conn != null)
        conn.close();
    }
    Dialect dl = getConnectionFactory().getDialect();
    TableColumnMetaData tcol = null;
    //改为unique

    modifyColumn(tablename, "STR2_", DbDefiner.FIELD_TYPE_STR, 100, null, true, true);
    tcol = dl.getTableColumnMetaData(tablename, "STR2_");
    assertEquals(null, tcol.getDefaultValue());
    if (dl.getDataBaseInfo().isDb2() || dl.getDataBaseInfo().isTeradata())
      assertEquals(false, tcol.isNullable());
    else
      assertEquals(true, tcol.isNullable());
    assertEquals(true, tcol.isUnique());

    //改为非空
    //sqlserver当字段有unique约束，将为空转为不空，会报有对象依赖该列，不能修改的提示；反过来将不空转为空可以；
    //if (!dl.getDataBaseInfo().isMssql()) {
      modifyColumn(tablename, "STR2_", DbDefiner.FIELD_TYPE_STR, 100, null, false, true);
      tcol = dl.getTableColumnMetaData(tablename, "STR2_");
      assertEquals(true, StrFunc.isNull(tcol.getDefaultValue()));
      assertEquals(false, tcol.isNullable());
      assertEquals(true, tcol.isUnique());
    //}
    //改默认值
    modifyColumn(tablename, "STR2_", DbDefiner.FIELD_TYPE_STR, 100, "'aa'", false, true);
    tcol = dl.getTableColumnMetaData(tablename, "STR2_");
    assertEquals("aa", tcol.getDefaultValue());
    assertEquals(false, tcol.isNullable());
    assertEquals(true, tcol.isUnique());

    //改非unique,可空，无默认值
    modifyColumn(tablename, "STR2_", DbDefiner.FIELD_TYPE_STR, 100, null, true, false);
    tcol = dl.getTableColumnMetaData(tablename, "STR2_");
    assertEquals(true, StrFunc.isNull(tcol.getDefaultValue()));
    assertEquals(true, tcol.isNullable());
    assertEquals(false, tcol.isUnique());

    //改nuique,非空，无默认值
    modifyColumn(tablename, "STR2_", DbDefiner.FIELD_TYPE_STR, 100, null, false, true);
    tcol = dl.getTableColumnMetaData(tablename, "STR2_");
    assertEquals(true, StrFunc.isNull(tcol.getDefaultValue()));
    assertEquals(false, tcol.isNullable());
    assertEquals(true, tcol.isUnique());

    //改nuique,可空，有默认值
    modifyColumn(tablename, "STR2_", DbDefiner.FIELD_TYPE_STR, 100, "'bb'", true, true);
    tcol = dl.getTableColumnMetaData(tablename, "STR2_");
    assertEquals("bb", tcol.getDefaultValue());
    if (dl.getDataBaseInfo().isDb2() || dl.getDataBaseInfo().isTeradata())
      assertEquals(false, tcol.isNullable());
    else
      assertEquals(true, tcol.isNullable());
    assertEquals(true, tcol.isUnique());

    //改非nuique,非空，无默认值
    modifyColumn(tablename, "STR2_", DbDefiner.FIELD_TYPE_STR, 100, null, false, false);
    tcol = dl.getTableColumnMetaData(tablename, "STR2_");
    assertEquals(true, StrFunc.isNull(tcol.getDefaultValue()));
    assertEquals(false, tcol.isNullable());
    assertEquals(false, tcol.isUnique());

    dropTable(tablename);
  }
  private void modifyColumn(String tbname,String colname,char t,int len,String defv,boolean nullable,boolean unique) throws Exception{
    ConnectionFactory conf = getConnectionFactory();
    DbDefiner dbfv = conf.getDbDefiner();
    Connection conn = conf.getConnection();
    try {
      dbfv.modifyColumn(conn, tablename, colname, null, t, len, 0, defv, unique, nullable);
    }finally{
      conn.close();
    }
  }

  /**
   * 测试判断字段是否存在
   * @throws Exception 
   */
  public void testColumnExist() throws Exception{
    createTable();
    addData(10);
    ConnectionFactory conf = getConnectionFactory();
    DbDefiner dbfv = conf.getDbDefiner();
    Connection conn = conf.getConnection();
    try{
      assertEquals(true,dbfv.colExists(conn, tablename, "INT_"));
      assertEquals(true,dbfv.colExists(conn, tablename, "int_"));
    }finally{
      conn.close();
    }
    dropTable(tablename);
  }
  /**
   * public void dropColumn(Connection conn,String tablename,String col) throws Exception;
   * 删除指定表的指定字段；
   * 使用  public boolean colExists(Connection conn, String tablename,
                           String colname) throws Exception;
     检验是否删除成功；
   * @throws Exception 
   */
  public void testDropColumn() throws Exception{
    createTable();
    addData(10);
    ConnectionFactory conf = getConnectionFactory();
    DbDefiner dbfv = conf.getDbDefiner();
    Connection conn = conf.getConnection();
    conn.setAutoCommit(true);
    try{
      dbfv.dropColumn(conn, tablename, "LOGIC_");
      assertEquals(false, dbfv.colExists(conn, tablename, "LOGIC_"));
      
      /*dbfv.dropColumn(conn, tablename, "str_");
      assertEquals(false, dbfv.colExists(conn, tablename, "str_"));
      */
      dbfv.dropColumn(conn, tablename, "NUM_");
      assertEquals(false, dbfv.colExists(conn, tablename, "NUM_"));
      
      /*dbfv.dropColumn(conn, tablename, "date_");
      assertEquals(false, dbfv.colExists(conn, tablename, "date_"));
      */
      dbfv.dropColumn(conn, tablename, "TIME_");
      assertEquals(false, dbfv.colExists(conn, tablename, "TIME_"));
      
      dbfv.dropColumn(conn, tablename, "TIMESTAMP_");
      assertEquals(false, dbfv.colExists(conn, tablename, "TIMESTAMP_"));
      
      
      dbfv.dropColumn(conn, tablename, "MEMO_");
      assertEquals(false, dbfv.colExists(conn, tablename, "MEMO_"));
      
      dbfv.dropColumn(conn, tablename, "BINARY_");
      assertEquals(false, dbfv.colExists(conn, tablename, "BINARY_"));
      
      dbfv.dropColumn(conn, tablename, "CHARACTER_");
      assertEquals(false, dbfv.colExists(conn, tablename, "CHARACTER_"));
      conn.commit();
      conn.setAutoCommit(true);
    }catch(Exception ex){
      conn.rollback();
      throw ex;
    }
    finally{
      if(conn!=null) conn.close();
    }
    dropTable(tablename);
  }
  
  /**
   * 测试增加字段
   * @throws Exception 
   */
  public void testAddColunum() throws Exception{
    createTable();
    ConnectionFactory conf = getConnectionFactory();
    DbDefiner dbfv = conf.getDbDefiner();
    Connection conn = conf.getConnection();
    conn.setAutoCommit(true);
    try{
      dbfv.addColumn(conn, tablename, "add_int_", DbDefiner.FIELD_TYPE_INT, 10, 0, null, true, false);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_int_"));
      
      dbfv.addColumn(conn, tablename, "add_str_", DbDefiner.FIELD_TYPE_STR, 10, 0, null, true, false);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_str_"));
      
      dbfv.addColumn(conn, tablename, "add_str2_", DbDefiner.FIELD_TYPE_STR, 10, 0, "'aa'", false, true);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_str2_"));
      
      dbfv.addColumn(conn, tablename, "add_float_", DbDefiner.FIELD_TYPE_FLOAT, 10, 1, null, true, false);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_float_"));
      
      dbfv.addColumn(conn, tablename, "add_date_", DbDefiner.FIELD_TYPE_DATE, 0, 0, null, true, false);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_date_"));
      
      dbfv.addColumn(conn, tablename, "add_time_", DbDefiner.FIELD_TYPE_TIME, 0, 0, null, true, false);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_time_"));
      
      dbfv.addColumn(conn, tablename, "add_timestamp_", DbDefiner.FIELD_TYPE_TIMESTAMP, 0, 0, null, true, false);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_timestamp_"));
      
      dbfv.addColumn(conn, tablename, "add_logic_", DbDefiner.FIELD_TYPE_LOGIC, 1, 0, null, true, false);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_logic_"));
      
      dbfv.addColumn(conn, tablename, "add_memo_", DbDefiner.FIELD_TYPE_memo, 0, 0, null, true, false);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_memo_"));
      
      dbfv.addColumn(conn, tablename, "add_binary_", DbDefiner.FIELD_TYPE_BINARY, 0, 0, null, true, false);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_binary_"));
      
      //增加unique字段，可空，无默认值
      dbfv.addColumn(conn, tablename, "add_str_2", DbDefiner.FIELD_TYPE_STR, 10, 0, null, true, true);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_str_2"));
      Dialect dl = SqlFunc.createDialect(conn);
      TableColumnMetaData tcol = dl.getTableColumnMetaData(tablename, "add_str_2");
      if(dl.getDataBaseInfo().isDb2() || dl.getDataBaseInfo().isTeradata()){
        //增加字段时 unique 必须 not null default ''
        //not null 必须  default ''
        assertEquals("",tcol.getDefaultValue());
        assertEquals(false,tcol.isNullable());
      }else{
        assertEquals(null,tcol.getDefaultValue());
        assertEquals(true,tcol.isNullable());
      }
      assertEquals(true,tcol.isUnique());
      
      //增加unique字段，可空，有默认值
      dbfv.addColumn(conn, tablename, "add_str_3", DbDefiner.FIELD_TYPE_STR, 10, 0, "'a'", true, true);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_str_3"));
      tcol = dl.getTableColumnMetaData(tablename, "add_str_3");
      if(dl.getDataBaseInfo().isDb2() || dl.getDataBaseInfo().isTeradata()){
        //增加字段时 unique 必须 not null default ''
        //not null 必须  default ''
        assertEquals(false,tcol.isNullable());
      }else{
        assertEquals("a",tcol.getDefaultValue());
        assertEquals(true,tcol.isNullable());
      }
      assertEquals(true,tcol.isUnique());
      
      //增加unique字段，不可空，无默认值
      dbfv.addColumn(conn, tablename, "add_str_4", DbDefiner.FIELD_TYPE_STR, 10, 0, null, false, true);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_str_4"));
      tcol = dl.getTableColumnMetaData(tablename, "add_str_4");
      if(dl.getDataBaseInfo().isDb2() || dl.getDataBaseInfo().isTeradata()){
        //增加字段时 unique 必须 not null default ''
        //not null 必须  default ''
        assertEquals("",tcol.getDefaultValue());
      }else
        assertEquals(true,StrFunc.isNull(tcol.getDefaultValue()));
      assertEquals(false,tcol.isNullable());
      assertEquals(true,tcol.isUnique());
      
      //增加unique字段，不可空，有默认值
      dbfv.addColumn(conn, tablename, "add_str_5", DbDefiner.FIELD_TYPE_STR, 10, 0, "'a'", false, true);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_str_5"));
      tcol = dl.getTableColumnMetaData(tablename, "add_str_5");
      assertEquals("a",tcol.getDefaultValue());
      assertEquals(false,tcol.isNullable());
      assertEquals(true,tcol.isUnique());
      
      //增加非unique字段，可空，有默认值
      dbfv.addColumn(conn, tablename, "add_str_6", DbDefiner.FIELD_TYPE_STR, 10, 0, "'a'", true, false);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_str_6"));
      tcol = dl.getTableColumnMetaData(tablename, "add_str_6");
      assertEquals("a",tcol.getDefaultValue());
      assertEquals(true,tcol.isNullable());
      assertEquals(false,tcol.isUnique());
      
      //增加非unique字段，不可空，无默认值
      dbfv.addColumn(conn, tablename, "add_str_7", DbDefiner.FIELD_TYPE_STR, 10, 0, null, false, false);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_str_7"));
      tcol = dl.getTableColumnMetaData(tablename, "add_str_7");
      if(dl.getDataBaseInfo().isDb2()){
        assertEquals("",tcol.getDefaultValue());
      }else assertEquals(true,StrFunc.isNull(tcol.getDefaultValue()));
      assertEquals(false,tcol.isNullable());
      assertEquals(false,tcol.isUnique());
      
      //增加非unique字段，不可空，有默认值
      dbfv.addColumn(conn, tablename, "add_str_8", DbDefiner.FIELD_TYPE_STR, 10, 0, "'a'", false, false);
      assertEquals(true, dbfv.colExists(conn, tablename, "add_str_8"));
      tcol = dl.getTableColumnMetaData(tablename, "add_str_8");
      assertEquals("a",tcol.getDefaultValue());
      assertEquals(false,tcol.isNullable());
      assertEquals(false,tcol.isUnique());
      
    }catch(Exception ex){
      conn.rollback();
      throw ex;
    }
    finally{
      if(conn!=null) conn.close();
    }
    dropTable(tablename);
  }
  
  /**
   * 测试是否能够获得表的索引
   * @throws Exception 
   */
  public void testIndexExist() throws Exception{
    createTable();
    addData(10);
    ConnectionFactory conf = getConnectionFactory();
    Dialect dl = conf.getDialect();
    DbMetaData dbmd = dl.createDbMetaData();
    TableMetaData tmd = dbmd.getTableMetaData(tablename);
    /** BIDEV-760
    * 读取表结构时，有些方法是访问才初始化，一开始就访问索引，最近增加了判断索引字段是不是表字段的代码，
    * 这个判断需要读取字段列表，但是还没初始化，字段列表的初始化代码却要访问索引，所以出现死循环； 
    * 测试：直接读取表索引，是否出现死循环；
    */
    TableIndexMetaData[] timd = tmd.getIndexes();
    /**
     * Mysql的自动增长字段，默认建了索引；所以比一般的数据库多一条索引；
     */
    assertEquals(dl.getDataBaseInfo().isMysql()?3:2,timd==null?0:timd.length);
    
    //索引名在创建表时，可能会更名；
    /*DbDefiner dbdef = conf.getDbDefiner();
    Connection conn = conf.getConnection();
    try{
      assertEquals(true,dbdef.indexExists(conn, tablename, "I"+tablename));
    }finally{
      conn.close();
    }*/
    dropTable(tablename);
  }
  
  /**
   * 测试
   * @throws Exception 
   */
  public void testRenameTable() throws Exception{
    createTable();
    addData(10);
    ConnectionFactory conf = getConnectionFactory();
    DbDefiner dbfv = conf.getDbDefiner();
    Connection conn = conf.getConnection();
    try{
      if(dbfv.tableExists(conn,null,"test_rename_table_")){
        dbfv.dropTable(conn,null,"test_rename_table_");
      }
      dbfv.renameTable(conn, tablename, "test_rename_table_");
      assertEquals(false, dbfv.tableExists(conn, null, tablename));
      assertEquals(true, dbfv.tableExists(conn, null, "test_rename_table_"));
      dbfv.dropTable(conn, null, "test_rename_table_");
    }
    finally{
      if(conn!=null) conn.close();
    }
    dropTable(tablename);
  }
  
  public void testTableList() throws Exception{
    ConnectionFactory conf = getConnectionFactory();
    Dialect dl = conf.getDialect();
    DbMetaData dbmd = dl.createDbMetaData();
    List l = dbmd.getTableNames();
    List vl = dbmd.getViewNames();
  }
  public void testView() throws Exception{
    createTable();
    addData(10);
    ConnectionFactory conf = getConnectionFactory();
    DbDefiner dbfv = conf.getDbDefiner();
    Connection conn = conf.getConnection();
    try{
      assertEquals(true,dbfv.tableOrViewExists(conn, tablename));
      //已存在tablename的表,对其判断是否是视图
      assertEquals(false,dbfv.viewExists(conn, tablename));
      String viewname = "v_"+tablename;
      if(dbfv.viewExists(conn, viewname))
        dbfv.dropView(conn, viewname);
      dbfv.createView(conn, viewname, null, "select * from "+tablename);
      assertEquals(true,dbfv.viewExists(conn, viewname));
      dbfv.dropView(conn, viewname);
      assertEquals(false,dbfv.viewExists(conn, viewname));
      if(dbfv.viewExists(conn, "v_tb2"))
        dbfv.dropView(conn, "v_tb2");
      dbfv.createView(conn, "v_tb2", "int_2,str_2", "select INT_,STR_ from "+tablename);
      assertEquals(true,dbfv.tableOrViewExists(conn, "v_tb2"));
      dbfv.dropView(conn, "v_tb2");
      
      /**
       * 大小写敏感的测试
       */
      if(dbfv.viewExists(conn, "v_Tb3"))
        dbfv.dropView(conn, "v_Tb3");
      dbfv.createView(conn, "v_Tb3", null, "select * from "+tablename);
      assertEquals(true,dbfv.viewExists(conn, "v_Tb3"));
      dbfv.dropView(conn, "v_Tb3");
    }finally{
      if(conn!=null) conn.close();
    }
    dropTable(tablename);
  }
  
  public void testReadView() throws Exception{
    createTable();
    addData(10);
    ConnectionFactory conf = getConnectionFactory();
    DbDefiner dbfv = conf.getDbDefiner();
    Connection conn = conf.getConnection();
    try{
      assertEquals(true,dbfv.tableOrViewExists(conn, tablename));
      //已存在tablename的表,对其判断是否是视图
      assertEquals(false,dbfv.viewExists(conn, tablename));
      String viewname = "v_"+tablename;
      if(dbfv.viewExists(conn, viewname))
        dbfv.dropView(conn, viewname);
      dbfv.createView(conn, viewname, null, "select * from "+tablename);
      assertEquals(true,dbfv.viewExists(conn, viewname));
      
      TableMetaData vtmd = conf.getDialect().createDbMetaData(conn).getTableMetaData(viewname);
      assertEquals(20,vtmd.getColumns().length);
      assertEquals(null,vtmd.getIndexes());
      assertEquals(null,vtmd.getPrimaryKey());
      dbfv.dropView(conn, viewname);
      
    }finally{
      if(conn!=null) conn.close();
    }
    dropTable(tablename);
  }
  /**
   * 创建测试用表
   * @throws Exception
   */
  protected void createTable() throws Exception {
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineAutoIncField("ID_", 1);
      dbv.defineIntField("INT_", 30, null, false, false);
      dbv.defineIntField("INT2_", 30, null, true, false);
      dbv.defineStringField("STR_",  20, null, true, false);
      dbv.defineFloatField("NUM_",  18, 2, null, true, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.defineTimeField("TIME_", null, true, false);
      dbv.defineTimeStampField("TIMESTAMP_",  null, true,false);
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
      dbv.definePrimaryKey("INT_");
      dbv.defineIndex("I" + tablename, "(INT_,DATE_)", false);

      String tbname = dbv.createTable(conn,null,tablename);
      //TableMetaData tmd = getConnectionFactory().getDialect().createDbMetaData().getTableMetaData(tbname);
      //TableIndexMetaData[] inds = tmd.getIndexes();
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
  protected void dropTable(String tbname) throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = getConnectionFactory().getConnection();
    try{
      if(tbname!=null&&dbv.tableExists(conn,null,tbname)){
        dbv.dropTable(conn,null,tbname);
      }
    }finally{
      conn.close();
    }
  }
  
  
  
  /**
   * 插入指定行数据；
   * @throws Exception 
   */
  protected void addData(int num) throws Exception {
    Connection conn = null;
    PreparedStatement pstat = null;
    String sql = "insert into "
        + tablename
        + " (INT_,STR_,NUM_,DATE_,TIME_,TIMESTAMP_,LOGIC_,MEMO_,BINARY_,CHARACTER_,STRBBQ_" +
            ",INTBBQYMD,INTBBQYM,INTBBQY,CHARBBQYM,CHARBBQY,CHARBBQYM_,CHARBBQY_)" +
            "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    try {
      conn = getConnectionFactory().getConnection();
      pstat = conn.prepareStatement(sql);
      int n = 0;
      for (int i = 0; i < num; i++) {
        System.out.println(i+"_________________________________");
        pstat.setLong(1, 2000+i);
        pstat.setString(2, String.valueOf(Math.round((Math.random() * 10000))));
        pstat.setDouble(3, (Math.random() * 1000) / 10);
        pstat.setDate(4, java.sql.Date.valueOf("2005-08-10"));//java.sql.Date.valueOf("2005-08-10"));
        pstat.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));// java.sql.Time.valueOf("13:30:14"));
        pstat.setTimestamp(6, java.sql.Timestamp
            .valueOf("1954-01-01 00:00:00.0"));//java.sql.Timestamp.valueOf("2005-08-10 13:30:14.234"));
        pstat.setString(7, "1");
        pstat.setString(8, "阿斗发机adskfager lkjgerg;");
        InputStream fin = getTestInputStream();
        pstat.setBinaryStream(9, fin, fin.available());
        String clob = CLOB_CONTACT;
        //BufferedReader r = new BufferedReader(new StringReader(clob));
        //pstat.setCharacterStream(10,r,clob.length());
        pstat.setString(10, clob);
        //pstat.setCharacterStream(10, new CharArrayReader(clob.toCharArray()),clob.length());
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
        if(n>=1){
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
  
  protected InputStream getTestInputStream() throws IOException{
    //保存压缩流
    return new ByteArrayInputStream(StmFunc.gzipBytes(getTestInputStreamStr().getBytes()));
  }
  protected String getTestInputStreamStr(){
    return "长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，" +
    		"长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，" +
    		"长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，" +
    		"长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，" +
    		"长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，" +
    		"长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，长江长城，黄山黄河，";
  }
  
  /**
   * 检验一个sql的正确性
   * @param sql
   * @return
   * @throws Exception
   */
  protected boolean excuteQuery(String sql) throws Exception  {
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      Statement stat = conn.createStatement();
      stat.executeQuery(sql);
      stat.close();
      return true;
    }
    finally{
      if(conn!=null)
        try {
          conn.close();
        }
        catch (SQLException e) {
          e.printStackTrace();
        }
    }
  }
  protected String getSql(String str) {
    return "select "+str+" from "+tablename;
  }
}
