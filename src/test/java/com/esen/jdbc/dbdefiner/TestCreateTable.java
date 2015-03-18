package com.esen.jdbc.dbdefiner;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.util.ExceptionHandler;
import com.esen.util.StrFunc;

import junit.framework.TestCase;

public class TestCreateTable  extends TestCase {
	
	public ConnectionFactory createConnectionFactory(){
	    return new SimpleConnectionFactory(
	        "oracle.jdbc.driver.OracleDriver",
	        "jdbc:oracle:thin:@192.168.1.100:1521:esenbi",
	        "test", "test","error");
	  }
	
	public void testCreateTable() throws Exception {
		ConnectionFactory conf = createConnectionFactory();
		DbDefiner dbf = conf.getDbDefiner();
		Connection conn = conf.getConnection();
		try {
			for(int i=0;i<100;i++){
				String tb = "test_a"+i;
				if(dbf.tableExists(conn, null, tb)){
					dbf.dropTable(conn, null, tb);
				}
			}
			long l = System.currentTimeMillis();
			for (int i = 0; i < 100; i++) {
				dbf.clearDefineInfo();
				String tb = "test_a"+i;
				for (int j = 0; j < 500; j++) {
					dbf.defineStringField("field" + j, 20, null, true, false);
				}
				String tbname = dbf.createTable(conn, null, tb);
				System.out.println(tbname);
			}
			System.out.println("耗时："+StrFunc.formatTime(System.currentTimeMillis()-l));
		}
		finally {
			conn.close();
		}
	}
	
	  
	  /**
	   * 测试多线程下，创建表，指定表名存在，自动更名是否出现对象存在无法创建的问题；
	   */
		public void testCreateTableForMultithread() {
			ConnectionFactory conf = createConnectionFactory();
			List errorList = Collections.synchronizedList(new ArrayList(32));
			List tabList = Collections.synchronizedList(new ArrayList(250));
			try {
				int k = 15;
				CreateTableForMultithread[] cms = new CreateTableForMultithread[k];
				for (int i = 0; i < k; i++) {
					cms[i] = new CreateTableForMultithread(errorList,tabList,conf);
					cms[i].start();
					
				}
				for (int i = 0; i < k; i++) {
					cms[i].join();
				}
				if(errorList.size()>0){
					this.fail((String)errorList.get(0));
				}
				this.assertEquals(k*50, tabList.size());
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
							System.out.println(tbi);
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
		 * 在不使用连接池的情况下，测试Oracle同步创建表是否有异常；
		 * @throws SQLException 
		 * 
		 */
		public void testCreateTableForMultithreadWithNoPool() throws SQLException{
			//使用连接池的功能删除test开头的表；
			String driver = "oracle.jdbc.driver.OracleDriver";
			String url = "jdbc:oracle:thin:@192.168.1.100:1521:esenbi";
			String user = "test";
			String pass = "test";
			dropTestTable(driver,url,user,pass);
			try{
				int k = 15;
				CreateTableForMultithreadWithNoPool[] cms = new CreateTableForMultithreadWithNoPool[k];
				for(int i=0;i<k;i++){
					cms[i] = new CreateTableForMultithreadWithNoPool(i*50,driver,url,user,pass);
					cms[i].start();
				}
				for (int i = 0; i < k; i++) {
					cms[i].join();
				}
				
			}
			catch (Exception e) {
				ExceptionHandler.rethrowRuntimeException(e);
			
			}finally{
				dropTestTable(driver,url,user,pass);
			}
		}

		private void dropTestTable(String driver, String url, String user, String pass) throws SQLException {
			ConnectionFactory conf = new SimpleConnectionFactory(
			        driver,url,user,pass,"debug");
			try{
			DbDefiner dbf = conf.getDbDefiner();
			Dialect dl = conf.getDialect();
			Connection conn = conf.getConnection();
			try {
				List alltblist = dl.createDbMetaData(conn).getTableNames();
				for(int i=0;i<alltblist.size();i++){
					String tb = (String)alltblist.get(i);
					if(tb.toUpperCase().startsWith("TEST")){
						dbf.dropTable(conn, null, tb);
					}
				}
			}
			finally {
				conn.close();
			}
			}finally{
				((SimpleConnectionFactory)conf).close();
			}
		}
}
