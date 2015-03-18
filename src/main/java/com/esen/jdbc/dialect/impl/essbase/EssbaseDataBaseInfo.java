package com.esen.jdbc.dialect.impl.essbase;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.AbstractDataBaseInfo;

public class EssbaseDataBaseInfo extends AbstractDataBaseInfo {

	public EssbaseDataBaseInfo(Connection conn,String defualtSchema) throws SQLException {
		super(conn,defualtSchema);
		this.dbtype = SqlConst.DB_TYPE_ESSBASE;
	}
	
	protected void initDefaultSchema(Connection conn, String defaultSchema) {
		
	}
	
	protected void initNCharByteLength(Connection conn) throws SQLException {
		
	}
	protected void initNCharLength(Connection conn) throws SQLException {
		
	}
	protected String getNCharLengthSQL() {
		return null;
	}

	protected String getNCharByteLengthSQL() {
		return null;
	}

	public int getMaxKeyOfFieldsLength() {
		return 0;
	}

	public int getMaxIndexOfFieldsLength() {
		return 0;
	}

	public int getMaxVarcharFieldLength() {
		return 0;
	}

}
