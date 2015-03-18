package com.esen.jdbc.dialect.impl.greenplum;

import java.sql.Connection;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;

public class GreenplumDbMetaData extends DbMetaDataImpl {

	public GreenplumDbMetaData(Connection con) {
		super(con);
	}

	public GreenplumDbMetaData(ConnectionFactory dbf) {
		super(dbf);
	}

	protected TableMetaData createTableMetaData(String tablename) {
		return new GreenplumTableMetaData(this, tablename);
	}
	

}
