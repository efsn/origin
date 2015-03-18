package com.esen.jdbc.dialect.impl.dm;

import java.sql.Connection;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;

public class DMDbMetaData extends DbMetaDataImpl {

	public DMDbMetaData(Connection con) {
		super(con);
	}
	public DMDbMetaData(ConnectionFactory conf) {
		super(conf);
	}
	
	protected TableMetaData createTableMetaData(String tablename){
	    return new DMTableMetaData(this,tablename);
	  }
}
