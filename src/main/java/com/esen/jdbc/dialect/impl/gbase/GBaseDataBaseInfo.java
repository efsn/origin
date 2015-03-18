package com.esen.jdbc.dialect.impl.gbase;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.AbstractDataBaseInfo;

/**
 * @author zhaowb
 */
public class GBaseDataBaseInfo extends AbstractDataBaseInfo {

	public GBaseDataBaseInfo(Connection conn, String defaultSchema) throws SQLException {
		super(conn, defaultSchema);
		this.dbtype = SqlConst.DB_TYPE_GBASE;
		this.testSql = "select 1";
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
		return "select length('中')";
	}

	protected String getNCharLengthSQL() {
		return "select char_length('中')";
	}

	public int getMaxKeyOfFieldsLength() {
		return 1024;
	}

	public int getMaxIndexOfFieldsLength() {
		return 1024;
	}

	public int getMaxVarcharFieldLength() {
		return 8000;
	}

	public boolean isEmptyStringEqualsNull() {
	  return false;
  }
}
