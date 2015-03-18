package com.esen.jdbc.dbdefiner;

import java.sql.*;
import java.util.List;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.util.ExceptionHandler;

public class CreateTableForMultithread extends Thread{
	private ConnectionFactory conf;
	private List l;
	private List tblist;
	public CreateTableForMultithread(List l,List tblist, ConnectionFactory conf){
		this.l = l;
		this.tblist = tblist;
		this.conf = conf;
	}
	public void run(){
		try {
			Connection con = conf.getConnection();
			DbDefiner dbf = conf.getDbDefiner();
			/*Statement stat = con.createStatement();
			try{
				stat.execute("alter session set sql_trace=true");
			}finally{
				stat.close();
			}*/
			try{
				for(int i=0;i<50;i++){
					dbf.clearDefineInfo();
					dbf.defineStringField("userid_", 50, null, true, false);
					dbf.defineStringField("bbq_", 10, null, true, false);
					dbf.defineStringField("btype_", 2, null, true, false);
					for(int j=0;j<300;j++){
						dbf.defineFloatField("zb"+j, 18, 4, null, true, false);
					}
					//dbf.definePrimaryKey("userid_,bbq_,btype_");
					dbf.defineIndex("ITest", "userid_,bbq_,btype_", true);
					try{
						String tbname = dbf.createTable(con, "TEST", false);
						tblist.add(tbname);
						System.out.println("create table: "+tbname);
					}catch (Exception e) {
						e.printStackTrace();
						l.add(e.toString());
						break;
					}
				}
			}finally{
				/*Statement stat2 = con.createStatement();
				try{
					stat2.execute("alter session set sql_trace=false");
				}finally{
					stat2.close();
				}*/
				con.close();
			}
		}
		catch (Exception e) {
			ExceptionHandler.rethrowRuntimeException(e);
		}
	}
}
