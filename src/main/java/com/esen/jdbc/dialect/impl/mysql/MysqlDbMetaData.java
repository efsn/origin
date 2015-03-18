package com.esen.jdbc.dialect.impl.mysql;

import java.sql.Connection;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;

public class MysqlDbMetaData extends DbMetaDataImpl {

	public MysqlDbMetaData(Connection con) {
		super(con);
	}
	
	public MysqlDbMetaData(ConnectionFactory fct) {
		super(fct);
	}

	protected TableMetaData createTableMetaData(String tablename) {
		return new MysqlTableMetaData(this, tablename);
	}
}
