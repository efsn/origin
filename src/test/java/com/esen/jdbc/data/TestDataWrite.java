package com.esen.jdbc.data;

import java.sql.*;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.util.StrFunc;

import junit.framework.TestCase;

public class TestDataWrite extends TestCase {
	private boolean batchCommit;
	private void setBatchCommit(boolean v){
		this.batchCommit = v;
	}
	private void write(ConnectionFactory cf,int t) throws Exception{
		
		Connection conn = cf.getConnection();
		try{
			DbDefiner def = cf.getDbDefiner();
			if(def.tableExists(conn, null, "t_test"))
				def.dropTable(conn, null, "t_test");
			def.clearDefineInfo();
			for(int i=0;i<5;i++){
				def.defineStringField("field"+i, 20, null, true, false);	
			}
			for(int i=5;i<50;i++){
				def.defineFloatField("field"+i, 20, 2, null, true, false);
			}
			def.createTable(conn, null, "t_test");
			
			StringBuffer insertsql = new StringBuffer();
			insertsql.append("insert into t_test values(");
			for(int i=0;i<50;i++){
				if(i>0) insertsql.append(",");
				insertsql.append("?");
			}
			insertsql.append(")");
			long l = System.currentTimeMillis();
			if(batchCommit)
			  conn.setAutoCommit(false);
			PreparedStatement pstat = conn.prepareStatement(insertsql.toString());
			for(int i=0;i<10000;i++){
				int k=1;
				for(int j=0;j<5;j++){
					pstat.setString(k++, "saelwl;njglw");
				}
				for(int j=5;j<50;j++){
					pstat.setDouble(k++,Math.round( Math.random()*10000)/100.00);
				}
				System.out.println(i);
				if(batchCommit){
				  pstat.addBatch();
				  if(i%1000==0)
					  pstat.executeBatch();
				}else
					pstat.executeUpdate();
			}
			if(batchCommit){
			  pstat.executeBatch();
			  conn.commit();
			}
			if(batchCommit)
			  conn.setAutoCommit(true);
			l = System.currentTimeMillis()-l;
			System.out.println("耗时："+StrFunc.formatTime(l) );
			assertEquals(true, l<t*1000);
		}finally{
			conn.close();
		}
	}
	/*public void testMysql5Write() throws Exception{
		SimpleConnectionFactory cf = new SimpleConnectionFactory("com.mysql.jdbc.Driver"
				,"jdbc:mysql://192.168.1.42/bidb2?useUnicode=true&characterEncoding=utf8","root","dw");
		write(cf,10);
	}*/
	
	/*public void testMysql4Write() throws Exception{
		SimpleConnectionFactory cf = new SimpleConnectionFactory("com.mysql.jdbc.Driver"
				,"jdbc:mysql://192.168.1.200/bidb?useUnicode=true&characterEncoding=utf8","bitest","bitest");
		write(cf,10);
	}*/
	
	/*public void testOracleWrite() throws Exception{
		SimpleConnectionFactory cf = new SimpleConnectionFactory("oracle.jdbc.driver.OracleDriver"
				,"jdbc:oracle:thin:@192.168.1.42:1521:orcl","dw","dw");
		write(cf,5);
	}*/
	
/*	public void testDb2Write(){
		SimpleConnectionFactory cf = new SimpleConnectionFactory("com.ibm.db2.jcc.DB2Driver"
				,"jdbc:db2://192.168.1.42:50000/dwdb","db2admin","db2admin");
		try{
		write(cf,10);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}*/
	
/*	public void testMssql2000Write(){
		SimpleConnectionFactory cf = new SimpleConnectionFactory("net.sourceforge.jtds.jdbc.Driver"
				,"jdbc:jtds:sqlserver://192.168.1.244:1433/testdb","sa","admin");
		this.setBatchCommit(true);
		try{
		write(cf,10);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}*/
	public void testSybase12Write(){
		SimpleConnectionFactory cf = new SimpleConnectionFactory("com.sybase.jdbc2.jdbc.SybDriver"
				,"jdbc:sybase:Tds:192.168.1.42:5000/bidb?charset=cp936","sa","");
		this.setBatchCommit(false);
		try{
		write(cf,25);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
