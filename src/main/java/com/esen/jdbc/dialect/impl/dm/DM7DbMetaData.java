package com.esen.jdbc.dialect.impl.dm;

import java.sql.Connection;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;

public class DM7DbMetaData extends DMDbMetaData {

	public DM7DbMetaData(Connection con) {
		super(con);
	}

	public DM7DbMetaData(ConnectionFactory dbf) {
		super(dbf);
	}

	protected TableMetaData createTableMetaData(String tablename) {
		return new DM7TableMetaData(this, tablename);
	}
}
