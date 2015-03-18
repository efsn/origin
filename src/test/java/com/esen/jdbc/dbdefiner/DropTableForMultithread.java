package com.esen.jdbc.dbdefiner;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.util.ExceptionHandler;

public class DropTableForMultithread extends Thread{
	private ConnectionFactory conf;
	private List errorList;
	private List tblist;
	public DropTableForMultithread(List errorList,List tblist, ConnectionFactory conf){
		this.errorList = errorList;
		this.tblist = tblist;
		this.conf = conf;
	}
	public void run(){
		try {
			Connection con = conf.getConnection();
			//alter session set sql_trace=true
			//openSqlTrace(con);
			DbDefiner dbf = conf.getDbDefiner();
			try{
				for(int i=0;i<tblist.size();i++){
					try{
						dbf.dropTable(con, null,(String)tblist.get(i));
						con.commit();
					}catch (SQLException e) {
						errorList.add(e.toString());
						break;
					}
				}
			}finally{
				con.close();
			}
		}
		catch (Exception e) {
			ExceptionHandler.rethrowRuntimeException(e);
		}
	}
	private void openSqlTrace(Connection con) throws SQLException {
		Statement stat = con.createStatement();
		try{
			stat.execute("alter session set sql_trace=true");
		}finally{
			stat.close();
		}
	}
}
