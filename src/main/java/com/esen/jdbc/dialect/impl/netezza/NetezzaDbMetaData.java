package com.esen.jdbc.dialect.impl.netezza;

import java.sql.Connection;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;

public class NetezzaDbMetaData extends DbMetaDataImpl {

	public NetezzaDbMetaData(Connection con) {
		super(con);
	}
	public NetezzaDbMetaData(ConnectionFactory dbf) {
	    super(dbf);
	}
	protected TableMetaData createTableMetaData(String tablename){
	   return new NetezzaTableMetaData(this, tablename);
	}
}
