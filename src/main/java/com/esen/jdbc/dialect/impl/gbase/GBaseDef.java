package com.esen.jdbc.dialect.impl.gbase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataHelper;
import com.esen.jdbc.dialect.impl.TableMetaDataHelper;

/**
 * @author zhaowb
 */
public class GBaseDef extends DbDef {
	public static final int MaxPrecision = 53;

	public GBaseDef(Dialect dl) {
		super(dl);
	}

	private boolean haveIncfField;

	protected String getStrFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " VARCHAR("
				+ (fi.getLen() > 0 ? fi.getLen() : 1)
				+ ") "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(), fi.getDesc(),
						DbDefiner.FIELD_TYPE_STR, isUpdate);
	}

	protected void checkKeysAndIndexesMaxLength(TableMetaDataHelper tmdh) {
		int maxKeyLen = dbinf.getMaxKeyOfFieldsLength();
		checkKeysAndIndexesMaxLength(tmdh, maxKeyLen);
	}

	protected void checkKeysAndIndexesMaxLength(TableMetaDataHelper tmdh, int maxKeyLength) {
		for (int i = 0; i < tmdh.getColumnCount(); i++) {
			TableColumnMetaDataHelper fieldi = (TableColumnMetaDataHelper) tmdh.getColumn(i);
			if (fieldi.isUnique()) {
				adjustFieldLength(new TableColumnMetaDataHelper[] { fieldi }, maxKeyLength);
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
					keys[j] = (TableColumnMetaDataHelper) tmdh.getColumn(fields[j]);
				}
				adjustFieldLength(keys, maxKeyLength);
			}
		}
	}

	private String getTransferSql(String str) {
		str = str.replaceAll("\\\\", "\\\\\\\\\\\\\\\\");
		str = str.replaceAll("%", "\\\\\\\\%");
		str = str.replaceAll("_", "\\\\\\\\_");
		str = str.replace('*', '%');
		str = str.replace('?', '_');
		return str;
	}

	public void modifyColumn(Connection conn, String tablename, String fieldname, char coltype, int len, int scale)
			throws SQLException {
		modifyColumn(conn, tablename, fieldname, fieldname, coltype, len, scale, null, false, true);
	}

	public void modifyColumn(Connection conn, String tablename, String col, String new_col, char coltype, int len,
			int dec, String defaultvalue, boolean unique, boolean nullable) throws SQLException {
		Dialect dl = SqlFunc.createDialect(conn);
		TableMetaData tmd = dl.createDbMetaData(conn).getTableMetaData(tablename);
		String newtbname = createTable(conn, dl, tmd, col, new_col, coltype, len, dec, defaultvalue, unique, nullable,
				false);
		String copysql = getCopyeSql(conn, dl, tmd, newtbname, col, new_col, coltype, false);
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(copysql);
		}
		finally {
			stmt.close();
		}
		dropTable(conn, null, tablename);
		renameTable(conn, newtbname, tablename);
	}

	private String createTable(Connection conn, Dialect dl, TableMetaData tmd, String col, String new_col,
			char coltype, int len, int dec, String defaultvalue, boolean unique, boolean nullable, boolean isDel)
			throws SQLException {
		DbDefiner dbf = dl.createDbDefiner();
		dbf.clearDefineInfo();
		String desttable = dbf.getCreateTableName(conn, tmd.getTableName(), null);
		String incName = null;
		String[] keys = tmd.getPrimaryKey();
		for (int i = 0; i < tmd.getColumnCount(); i++) {
			TableColumnMetaData colmd = tmd.getColumn(i);
			String colname = colmd.getName();
			boolean iskey = isKey(colname, keys);
			if (colname.equalsIgnoreCase(col)) {
				if (!isDel) {
					dbf.defineField(new_col == null || new_col.length() == 0 ? colname : new_col, coltype, len, dec,
							getDefineDefaultValue(defaultvalue, coltype), iskey ? false : nullable, iskey ? false
									: unique);
				}
			}
			else {
				if (colmd.isAutoInc()) {
					incName = colname;
					dbf.defineAutoIncField(colname, 1);
				}
				else {
					char tp = getFieldType(colmd.getType());
					int l = colmd.getLen();
					int cl = colmd.getScale();
					dbf.defineField(colname, tp, l, cl, getDefineDefaultValue(colmd.getDefaultValue(), tp),
							colmd.isNullable(), iskey ? false : colmd.isUnique(), colmd.getDesc());
				}
			}
		}
		if (keys != null) {
			StringBuffer ks = new StringBuffer(10 * keys.length);
			for (int i = 0; i < keys.length; i++) {
				String keyfield = keys[i];
				if (keyfield.equalsIgnoreCase(col)) {
					if (isDel)
						continue;
					if (new_col != null && new_col.length() > 0) {
						keyfield = new_col;
					}
				}
				ks.append(keyfield);
				ks.append(',');
			}
			if (ks.length() > 0) {
				ks.deleteCharAt(ks.length() - 1);
				dbf.definePrimaryKey(ks.toString());
			}
		}
		TableIndexMetaData[] indexes = tmd.getIndexes();
		if (indexes != null) {
			for (int i = 0; i < indexes.length; i++) {
				TableIndexMetaData imd = indexes[i];
				String fds[] = imd.getColumns();
				if (fds.length == 1 && incName != null && fds[0].equalsIgnoreCase(incName))
					continue;
				if (fds.length == 1 && fds[0].equalsIgnoreCase(col)) {
					if (imd.isUnique() && !unique)
						continue;
				}
				StringBuffer fields = new StringBuffer(fds.length * 20);
				for (int j = 0; j < fds.length; j++) {
					if (fields.length() > 0)
						fields.append(",");
					if (fds[j].equalsIgnoreCase(col)) {
						if (isDel) {
							continue;
						}
						fields.append(new_col == null || new_col.length() == 0 ? col : new_col);
					}
					else
						fields.append(fds[j]);
				}
				if (fields.length() > 0) {
					fields.insert(0, '(');
					fields.append(")");
					dbf.defineIndex(imd.getName(), fields.toString(), imd.isUnique());
				}
			}
		}
		dbf.createTable(conn, desttable, false, true);
		return desttable;
	}

	private String getDefineDefaultValue(String defaultValue, char coltype) {
		if (defaultValue == null)
			return null;
		if (!(coltype == DbDefiner.FIELD_TYPE_STR || coltype == DbDefiner.FIELD_TYPE_DATE || coltype == DbDefiner.FIELD_TYPE_TIMESTAMP))
			return defaultValue;
		if (defaultValue.length() == 0)
			return "''";
		if (defaultValue.length() >= 2 && defaultValue.startsWith("'") && defaultValue.endsWith("'"))
			return defaultValue;
		return "'" + defaultValue + "'";
	}

	private boolean isKey(String col, String[] keys) {
		if (keys == null)
			return false;
		for (int i = 0; i < keys.length; i++) {
			if (col.equalsIgnoreCase(keys[i]))
				return true;
		}
		return false;
	}

	private String getCopyeSql(Connection conn, Dialect dl, TableMetaData tmd, String newtbname, String col,
			String new_col, char coltype, boolean isDel) {
		StringBuffer sql = new StringBuffer(256);
		sql.append("insert into ").append(newtbname).append(" (");
		TableColumnMetaData[] cols = tmd.getColumns();
		for (int i = 0; i < cols.length; i++) {
			TableColumnMetaData colmd = cols[i];
			if (colmd.isAutoInc())
				continue;
			String colname = colmd.getName();
			if (colname.equalsIgnoreCase(col)) {
				if (!isDel) {
					sql.append(getColumnName(new_col == null || new_col.length() == 0 ? colname : new_col)).append(',');
				}
			}
			else {
				sql.append(getColumnName(colname)).append(',');
			}
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(")");
		sql.append(" select ");
		for (int i = 0; i < cols.length; i++) {
			TableColumnMetaData colmd = cols[i];
			if (colmd.isAutoInc())
				continue;
			String colname = colmd.getName();
			if (colname.equalsIgnoreCase(col)) {
				if (!isDel) {
					sql.append(dl.funcToSqlVar(getColumnName(colname), colmd.getType(), getSqlType(coltype), null)).append(
							',');
				}
			}
			else {
				sql.append(getColumnName(colname)).append(',');
			}
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(" from ").append(tmd.getTableName());
		return sql.toString();
	}

	public String getTempTableCreateDdl(String tablename, String fldDdl) {
		return "CREATE TEMPORARY TABLE IF NOT EXISTS " + tablename + "(" + fldDdl + ") ";
	}

	protected String getTailDdl(String defaultvalue, boolean nullable, boolean unique, String desc, char t,
			boolean isUpdate) {
		StringBuffer str = new StringBuffer(32);
		if (defaultvalue != null && defaultvalue.length() > 0) {
			str.append(" DEFAULT ").append(defaultvalue);
		}
		if (!nullable)
			str.append(" NOT NULL ");
		else
			str.append(" NULL ");
		if (desc != null && desc.length() > 0) {
			str.append(" COMMENT ").append('\'').append(desc).append('\'');
		}
		return str.toString();
	}

	protected String getMemoFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " TEXT "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(), fi.getDesc(),
						DbDefiner.FIELD_TYPE_memo, isUpdate);
	}

	protected String getClobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " MEDIUMTEXT "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(), fi.getDesc(),
						DbDefiner.FIELD_TYPE_CLOB, isUpdate);
	}

	protected String getBlobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " LONGBLOB "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(), fi.getDesc(),
						DbDefiner.FIELD_TYPE_BINARY, isUpdate);
	}

	public void clearDefineInfo() {
		haveIncfField = false;
		super.clearDefineInfo();
	}

	public void defineAutoIncField(String thisField, int gap) {
		super.defineAutoIncField(thisField, gap);
		definePrimaryKey(thisField);
		this.haveIncfField = true;
	}

	protected String getIdFieldDdl(String thisField, int len, int step, String desc) {
		return (thisField + " INT AUTO_INCREMENT  ");
	}

	public void definePrimaryKey(String fieldNames) {
		if (this.haveIncfField || fieldNames.indexOf(",") >= 0) {
			return;
		}
		super.definePrimaryKey(fieldNames);
	}

	public void defineIndex(String indexName, String expression, boolean unique) {
		return;
	}

	public String createIndex(Connection conn, String table, String indexName, String[] indexFields,
			boolean indexUnique, boolean ifIndexNameExistThrowException) throws SQLException {
		return indexName;
	}

	protected String getNumericFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		StringBuffer numddl = new StringBuffer();
		numddl.append(getColumnName(fi.getName()));
		if (fi.getLen() > 0) {
			numddl.append(" DOUBLE");
			int[] prcs = formatNumberPrecision(fi.getLen(), fi.getScale(), MaxPrecision);
			numddl.append('(').append(prcs[0]).append(',').append(prcs[1]).append(')');
		}
		else {
			numddl.append(" DOUBLE");
		}
		numddl.append(' ');
		numddl.append(getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(), fi.getDesc(),
				DbDefiner.FIELD_TYPE_FLOAT, isUpdate));
		return numddl.toString();

	}

	protected String getColumnName(String cname) {
		return "`" + cname + "`";
	}

	protected String getTimeStampFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName()) + " DATETIME "
				+ getTimeStampTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(), fi.getDesc());
	}

	private String getTimeStampTailDdl(String defaultvalue, boolean nullable, boolean unique, String desc) {
		StringBuffer str = new StringBuffer(32);
		if (defaultvalue != null && defaultvalue.length() > 0) {
			str.append(" DEFAULT ").append(defaultvalue);
		}
		if (nullable) {
			str.append(" NULL ");
		}
		else {
			if (defaultvalue == null) {
				str.append(" DEFAULT '1970-01-01' ");
			}
			str.append(" NOT NULL ");
		}

		if (desc != null && desc.length() > 0) {
			str.append(" COMMENT ").append('\'').append(desc).append('\'');
		}
		return str.toString();
	}

	protected String getDateFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " DATE "
				+ getDateFldDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(), fi.getDesc(),
						DbDefiner.FIELD_TYPE_DATE, isUpdate);
	}

	protected String getDateFldDdl(String defaultvalue, boolean nullable, boolean unique, String desc, char t,
			boolean isUpdate) {
		StringBuffer str = new StringBuffer(32);
		if (defaultvalue != null && defaultvalue.length() > 0) {
			str.append(" DEFAULT ").append(defaultvalue);
		}
		if (nullable) {
			str.append(" NULL ");
		}
		else {
			if (defaultvalue == null) {
				str.append(" DEFAULT '1970-01-01' ");
			}
			str.append(" NOT NULL ");
		}

		if (desc != null && desc.length() > 0) {
			str.append(" COMMENT ").append('\'').append(desc).append('\'');
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

	public void renameTable(Connection conn, String oldname, String newname) throws SQLException {
		Statement ddl = conn.createStatement();
		try {
			ddl.executeUpdate("RENAME TABLE " + oldname + " TO " + newname);
		}
		finally {
			ddl.close();
		}
	}

	protected String getIndexName(Connection conn, String tbname, String nm) throws Exception {
		return nm;
	}

	public boolean indexExists(Connection conn, String tbname, String nm) throws SQLException {
		return true;
	}

	public boolean tableOrViewExists(Connection conn, String tvname) throws SQLException {
		return tableExists(conn, null, tvname);
	}

	public boolean tableExists(Connection conn, String catalog, String tablename) throws SQLException {
		PreparedStatement pstat = conn.prepareStatement("SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME=? AND TABLE_SCHEMA=DATABASE()");
		try {
			pstat.setString(1, tablename.toLowerCase());
			ResultSet rs = pstat.executeQuery();
			try {
				return rs.next();
			}
			finally {
				rs.close();
			}

		}
		finally {
			pstat.close();
		}
	}

	public boolean viewExists(Connection conn, String viewname) throws SQLException {
		return super.viewExists(conn, viewname.trim());
	}

}
