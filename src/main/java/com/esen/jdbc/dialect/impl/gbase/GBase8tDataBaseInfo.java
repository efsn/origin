package com.esen.jdbc.dialect.impl.gbase;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.AbstractDataBaseInfo;

/**
 * @author liujin
 */
public class GBase8tDataBaseInfo extends AbstractDataBaseInfo {

	public GBase8tDataBaseInfo(Connection conn, String defaultSchema) throws SQLException {
		super(conn, defaultSchema);
		this.dbtype = SqlConst.DB_TYPE_GBASE_8T;
		this.testSql = "SELECT 1 FROM SYSMASTER:SYSSHMVALS";
	}

	protected void initDefaultSchema(Connection conn, String defaultSchema) {
		if (defaultSchema != null && defaultSchema.length() > 0) {
			default_schema = defaultSchema;
		}
		else {
			String v = getDefaultSchema(conn);
			if (v != null && v.trim().length() > 0) {
				default_schema = v;
			}
		}
	}

	protected String getDefaultSchema(Connection conn) {
		return null;
	}

	protected String getNCharByteLengthSQL() {
		return "SELECT length('中') FROM SYSMASTER:SYSSHMVALS";
	}

	protected String getNCharLengthSQL() {
		return "SELECT char_length('中') FROM SYSMASTER:SYSSHMVALS";
	}

	public int getMaxKeyOfFieldsLength() {
		return 387;
	}

	public int getMaxIndexOfFieldsLength() {
		return 387;
	}

	public int getMaxVarcharFieldLength() {
		return 32739;
	}

	public boolean isEmptyStringEqualsNull() {
	  return false;
  }
}
