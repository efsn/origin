package com.esen.jdbc.dialect.impl.netezza;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.AbstractDataBaseInfo;

public class NetezzaDataBaseInfo extends AbstractDataBaseInfo {

	public NetezzaDataBaseInfo(Connection conn, String defaultSchema)
			throws SQLException {
		super(conn, defaultSchema);
		
		this.dbtype = SqlConst.DB_TYPE_NETEZZA;
	    this.testSql = "select 1";
	}

	

	protected void initDefaultSchema(Connection conn, String defaultSchema) {
		
	}



	protected String getNCharLengthSQL() {
		return "select length('中')";
	}

	protected String getNCharByteLengthSQL() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getMaxKeyOfFieldsLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxIndexOfFieldsLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxVarcharFieldLength() {
		// TODO Auto-generated method stub
		return 64000;
	}

}
