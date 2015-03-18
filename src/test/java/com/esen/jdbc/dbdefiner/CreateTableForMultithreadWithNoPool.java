package com.esen.jdbc.dbdefiner;

import java.sql.*;

import com.esen.util.ExceptionHandler;

public class CreateTableForMultithreadWithNoPool extends Thread{
	private int k;
	private String driver; String url; String user; String pass;
	public CreateTableForMultithreadWithNoPool(int k,String driver, String url, String user, String pass){
		this.k=k;
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.pass = pass;
	}
	
	public void run(){
		try{
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(url, user, pass);
			try{
				Statement stat = conn.createStatement();
				for(int i=k;i<k+50;i++){
					String tbname = "test"+i;
					String createsql = getCreateSql(tbname);
					//System.out.println(createsql);
					stat.executeUpdate(createsql);
					System.out.println("create table: "+tbname);
				}
				stat.close();
			}finally{
				conn.close();
			}
		}
		catch (Exception e) {
			ExceptionHandler.rethrowRuntimeException(e);
		}
	}

	private String getCreateSql(String tbname) {
		StringBuffer sql = new StringBuffer(1024);
		sql.append("create table ").append(tbname).append("(");
		sql.append("userid varchar2(20),bbq varchar2(10)");
		for(int j=0;j<500;j++){
			sql.append(",field").append(j).append(" varchar2(20)");
		}
		sql.append(",primary key (userid,bbq))");
		return sql.toString();
	}
}
