package com.esen.jdbc.dialect.impl.gbase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataHelper;
import com.esen.jdbc.dialect.impl.TableMetaDataHelper;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * @author liujin
 */
public class GBase8tDef extends DbDef {
	public static final int MaxPrecision = 32;

	public GBase8tDef(Dialect dl) {
		super(dl);
	}

	protected String getStrFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		String type = " VARCHAR ";
		if (fi.getLen() > 255) {
			type = " LVARCHAR ";
		}
		return getColumnName(fi.getName())
				+ type
				+ "("
				+ (fi.getLen() > 0 ? fi.getLen() : 1)
				+ ") "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_STR,
						isUpdate);
	}

	protected void checkKeysAndIndexesMaxLength(TableMetaDataHelper tmdh) {
		int maxKeyLen = dbinf.getMaxKeyOfFieldsLength();
		checkKeysAndIndexesMaxLength(tmdh, maxKeyLen);
	}

	protected void checkKeysAndIndexesMaxLength(TableMetaDataHelper tmdh,
			int maxKeyLength) {
		for (int i = 0; i < tmdh.getColumnCount(); i++) {
			TableColumnMetaDataHelper fieldi = (TableColumnMetaDataHelper) tmdh
					.getColumn(i);
			if (fieldi.isUnique()) {
				adjustFieldLength(new TableColumnMetaDataHelper[] { fieldi },
						maxKeyLength);
			}
		}
		String[] _pkeys = tmdh.getPrimaryKey();
		if (_pkeys != null && _pkeys.length > 0) {
			TableColumnMetaDataHelper[] keys = new TableColumnMetaDataHelper[_pkeys.length];
			for (int i = 0; i < _pkeys.length; i++) {
				keys[i] = (TableColumnMetaDataHelper) tmdh.getColumn(_pkeys[i]);
			}
			adjustFieldLength(keys, maxKeyLength);
		}
		TableIndexMetaData[] _index = tmdh.getIndexes();
		if (_index != null && _index.length > 0) {
			for (int i = 0; i < _index.length; i++) {
				TableIndexMetaData indx = _index[i];
				String[] fields = indx.getColumns();
				TableColumnMetaDataHelper[] keys = new TableColumnMetaDataHelper[fields.length];
				for (int j = 0; j < fields.length; j++) {
					keys[j] = (TableColumnMetaDataHelper) tmdh
							.getColumn(fields[j]);
				}
				adjustFieldLength(keys, maxKeyLength);
			}
		}
	}

	protected String formatUpcaseFieldName(String fdname) {
		return fdname.toLowerCase();
	}

	public void modifyColumn(Connection conn, String tablename,
			String fieldname, char coltype, int len, int scale)
			throws SQLException {
		StringBuffer ddl = new StringBuffer(32).append("ALTER TABLE ");
		ddl.append(tablename);
		ddl.append(" MODIFY ");
		ddl.append(getFieldDefine(coltype, fieldname, len, scale));
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(ddl.toString());
		} finally {
			stmt.close();
		}
	}

	private String getFieldDefine(char coltype, String fieldname, int len,
			int scale) throws SQLException {
		switch (coltype) {
		case DbDefiner.FIELD_TYPE_STR:
			return fieldname + " VARCHAR(" + len + ") ";
		case DbDefiner.FIELD_TYPE_INT:
			return fieldname + " INT";
		case DbDefiner.FIELD_TYPE_FLOAT:
			int[] prcs = formatNumberPrecision(len, scale, MaxPrecision);
			if (prcs[0] == 0) {
				return fieldname + " DOUBLE PRECISION";
			}
			return fieldname + " DECIMAL(" + prcs[0] + ", " + prcs[1] + ")";
		case DbDefiner.FIELD_TYPE_DATE:
			return fieldname + " DATE ";
		case DbDefiner.FIELD_TYPE_TIME:
			return fieldname + " DATETIME HOUR TO SECOND ";
		case DbDefiner.FIELD_TYPE_TIMESTAMP:
			return fieldname + " DATETIME YEAR TO SECOND ";
		case DbDefiner.FIELD_TYPE_LOGIC:
			return fieldname + " VARCHAR(1)";
		case DbDefiner.FIELD_TYPE_memo:
			return fieldname + " LVARCHAR(8000) ";
		case DbDefiner.FIELD_TYPE_CLOB:
			return fieldname + " CLOB ";
		case DbDefiner.FIELD_TYPE_BINARY:
			return fieldname + " BLOB ";
		default:
			throw new SQLException(
					"database not support to define this type of field,type:"
							+ coltype);
		}
	}

	public void modifyColumn(Connection conn, String tablename, String col,
			String new_col, char coltype, int len, int dec,
			String defaultvalue, boolean unique, boolean nullable)
			throws SQLException {
		if (col == null || col.length() == 0) {
			// throw new SQLException("修改列名不能为空！");
			throw new SQLException(I18N.getString(
					"JDBC.COMMON.COLUMNNAMECANNOTBEEMPTY", "修改列名不能为空！"));
		}
		String col2 = getColumnName(dl, col);
		String new_col2 = getColumnName(dl, new_col);

		List ddls = new ArrayList();

		// 字段名发生改变时，单独用 语句进行修改，不能合并到修改其他属性一起修改
		if (!StrFunc.isNull(new_col2) && !col2.equals(new_col2)) {
			ddls.add("RENAME COLUMN " + tablename + "." + col2 + " TO "
					+ new_col2);
			col2 = new_col2;
		}

		StringBuffer ddl = new StringBuffer("ALTER TABLE ");
		ddl.append(tablename);

		Dialect dl = SqlFunc.createDialect(conn);
		TableMetaData tbmd = dl.createDbMetaData().getTableMetaData(tablename);
		TableColumnMetaData tcol = tbmd.getColumn(col);
		tcol.isUnique();

		ddl.append(" MODIFY ");

		if (!tcol.isAutoInc()) {
			// 修改字段长度时，自动根据数据库限制，比如主键组合长度等限制，调整字段长度；
			len = adjustFieldLengthForModify(tbmd, col2, len);
			ddl.append(getFldDdl(new TableColumnMetaDataHelper(col2, coltype,
					len, dec, nullable, unique, defaultvalue, null), true));

			ddls.add(ddl.toString());
		}

		// 如果字段是主键，修改属性时，会丢失主键定义，需要加上
		String[] keys = tbmd.getPrimaryKey();
		boolean defkey = false;
		if (keys != null && keys.length == 1) {
			if (keys[0].equalsIgnoreCase(col)) {
				defkey = true;
				// 将原信息中的主键的字段名改为新的字段名，字段名可能发生修改
				keys[0] = col2;
			}
		}

		if (!tcol.isAutoInc() && defkey && !unique) {
			StringBuffer str = new StringBuffer("ALTER TABLE ");
			str.append(tablename);
			str.append(" ADD CONSTRAINT PRIMARY KEY(");
			str.append(keys[0]);
			for (int i = 1; i < keys.length; i++) {
				str.append(", ");
				str.append(keys[i]);
			}
			str.append(")");
			ddls.add(str.toString());
		}

		Statement stmt = conn.createStatement();
		try {
			for (int i = 0; i < ddls.size(); i++)
				stmt.execute((String) ddls.get(i));
		} finally {
			stmt.close();
		}
	}

	public String getTableCreateDdl(String tablename, String fldDdl) {
		return "CREATE TABLE " + tablename + "(" + fldDdl + ") ";
	}

	public String getTempTableCreateDdl(String tablename, String fldDdl) {
		return "CREATE TEMPORARY TABLE IF NOT EXISTS " + tablename + "("
				+ fldDdl + ") ";
	}

	protected String getTailDdl(String defaultvalue, boolean nullable,
			boolean unique, String desc, char t, boolean isUpdate) {
		StringBuffer str = new StringBuffer(32);
		if (defaultvalue != null && defaultvalue.length() > 0) {
			str.append(" DEFAULT ").append(defaultvalue);
		}

		if (unique) {
			str.append(" UNIQUE ");
		}

		if (!nullable) {
			str.append(" NOT NULL ");
		}

		return str.toString();
	}

	protected String getMemoFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " LVARCHAR(8000) "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_memo,
						isUpdate);
	}

	protected String getClobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " CLOB "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_CLOB,
						isUpdate);
	}

	protected String getBlobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " BLOB "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(),
						DbDefiner.FIELD_TYPE_BINARY, isUpdate);
	}

	protected String getIdFieldDdl(String thisField, int len, int step, String desc) {
		StringBuffer idddl = new StringBuffer();
		idddl.append(thisField);

		String type = "";
		if (len <= LEN_INT2) {
			type = "SERIAL";
		} else {
			type = "SERIAL8";
		}
		idddl.append(' ').append(type);

		return idddl.toString();
	}
	
	protected String getIntFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		if (isLenValid(fi.getLen())) {
			String type = getIntType(fi.getLen());
			return getColumnName(fi.getName())
					+ " " + type + ""
					+ getTailDdl(fi.getDefaultValue(), fi.isNullable(),
							fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_INT,
							isUpdate);
		} else {
			return getNumericFldDdl(fi, isUpdate);
		}
	}
	
	private boolean isLenValid(int len) {
		if (len <= 0 || len > LEN_INT8) {
			return false;
		}
		
		return true;
	}

	private String getIntType(int len) {
		if (!isLenValid(len)) {
			return null;
		}

		if (len <= LEN_INT2) {
			return "SMALLINT";
		}
		if (len <= LEN_INT4) {
			return "INTEGER";
		}
		if (len <= LEN_INT8) {
			return "BIGINT";
		}
		return null;
	}

	protected String getNumericFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		StringBuffer numddl = new StringBuffer();
		numddl.append(getColumnName(fi.getName()));
		if (fi.getLen() > 0) {
			numddl.append(" NUMERIC");
			int[] prcs = formatNumberPrecision(fi.getLen(), fi.getScale(), MaxPrecision);
			numddl.append('(').append(prcs[0]).append(',').append(prcs[1]).append(')');
		} else {
			numddl.append(" DOUBLE PRECISION");
		}
		numddl.append(' ');
		numddl.append(getTailDdl(fi.getDefaultValue(), fi.isNullable(),
				fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_FLOAT,
				isUpdate));
		return numddl.toString();
	}

	protected String getColumnName(String cname) {
		// return "`" + cname + "`";
		return cname;
	}

	protected String getTimeStampFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " DATETIME YEAR TO SECOND "
				+ getTimeStampTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc());
	}

	private String getTimeStampTailDdl(String defaultvalue, boolean nullable,
			boolean unique, String desc) {
		StringBuffer str = new StringBuffer(32);
		if (defaultvalue != null && defaultvalue.length() > 0) {
			str.append(" DEFAULT ");

			if (dl.funcToday().equalsIgnoreCase(defaultvalue)
					|| dl.funcNow().equalsIgnoreCase(defaultvalue)) {
				str.append(" CURRENT YEAR TO SECOND ");
			} else {
				str.append(" DATETIME(" + defaultvalue + ") YEAR TO SECOND");
				// str.append(dl.funcDateToChar(defaultvalue, "YYYY-MM-DD HH:MM:SS"));
			}
		}
		if (!nullable) {
			str.append(" NOT NULL ");
		}

		if (unique) {
			str.append(" UNIQUE ");
		}
		return str.toString();
	}

	protected String getTimeFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " DATETIME HOUR TO SECOND "
				+ getTimeTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_TIME,
						isUpdate);
	}

	protected String getDateFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " DATE "
				+ getDateTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_DATE,
						isUpdate);
	}

	protected String getTimeTailDdl(String defaultvalue, boolean nullable,
			boolean unique, String desc, char t, boolean isUpdate) {
		StringBuffer str = new StringBuffer(32);
		if (defaultvalue != null && defaultvalue.length() > 0) {
			str.append(" DEFAULT");

			if (dl.funcNow().equalsIgnoreCase(defaultvalue)) {
				str.append(" CURRENT HOUR TO SECOND ");
			} else {
				str.append(" DATETIME(" + defaultvalue + ") HOUR TO SECOND");
			}
		}
		if (!nullable) {
			str.append(" NOT NULL ");
		}

		if (unique) {
			str.append(" UNIQUE ");
		}
		return str.toString();
	}

	protected String getDateTailDdl(String defaultvalue, boolean nullable,
			boolean unique, String desc, char t, boolean isUpdate) {
		StringBuffer str = new StringBuffer(32);
		if (defaultvalue != null && defaultvalue.length() > 0) {
			str.append(" DEFAULT");

			if (dl.funcToday().equalsIgnoreCase(defaultvalue)) {
				str.append(" TODAY ");
			} else {
				str.append(" DATETIME(" + defaultvalue + ") YEAR TO DAY");
			}
		}
		if (!nullable) {
			str.append(" NOT NULL ");
		}

		if (unique) {
			str.append(" UNIQUE ");
		}
		return str.toString();
	}

	protected int[] formatNumberPrecision(int len, int scale, int maxLen) {
		if (len <= 0) {
			len = 0;
			scale = 0;
		}
		if (len < scale) {
			len = len + scale;
		}
		if (len > maxLen) {
			len = maxLen;
		}
		if (scale >= 15) {
			scale = 12;
		}
		if (scale < 0) {
			scale = 0;
		}
		return new int[] { len, scale };
	}

	public void renameTable(Connection conn, String oldname, String newname)
			throws SQLException {
		Statement ddl = conn.createStatement();
		try {
			ddl.executeUpdate("RENAME TABLE " + oldname + " TO " + newname);
		} finally {
			ddl.close();
		}
	}

	public void dropColumn(Connection conn, String tablename, String col)
			throws SQLException {
		StringBuffer ddl = new StringBuffer(64).append("ALTER TABLE ");
		ddl.append(tablename);
		ddl.append(" DROP ").append(col);
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(ddl.toString());
		} finally {
			stmt.close();
		}
	}

	protected String getIndexName(Connection conn, String tbname, String nm)
			throws Exception {
		return nm;
	}

	public boolean indexExists(Connection conn, String tbname, String nm)
			throws SQLException {
		return true;
	}

	public boolean tableOrViewExists(Connection conn, String tvname)
			throws SQLException {
		return tableExists(conn, null, tvname);
	}

	public boolean tableExists(Connection conn, String catalog, String tablename)
			throws SQLException {
		PreparedStatement pstat = conn.prepareStatement("SELECT 1 FROM SYSTABLES WHERE TABNAME=? ");
		try {
			pstat.setString(1, tablename.toLowerCase());
			ResultSet rs = pstat.executeQuery();
			try {
				return rs.next();
			} finally {
				rs.close();
			}

		} finally {
			pstat.close();
		}
	}
}
