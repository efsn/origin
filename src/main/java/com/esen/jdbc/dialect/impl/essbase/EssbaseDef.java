package com.esen.jdbc.dialect.impl.essbase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.util.i18n.I18N;

public class EssbaseDef extends DbDef {

	public EssbaseDef(Dialect dl) {
		super(dl);
	}

	public boolean tableExists(Connection conn, String catalog, String tablename) throws SQLException {
		return objectExists(conn, tablename, new String[] { "CUBE" });
	}

	protected boolean objectExists(Connection conn, String tablename, String[] types) throws SQLException {
		if (tablename == null || tablename.length() == 0)
//			throw new SQLException("不在正确的参数：" + tablename + "不能为空；");
			throw new SQLException(I18N.getString("com.esen.jdbc.dialect.impl.essbase.essbasedef.wrongargs", "不在正确的参数{0}不能为空；", new Object[]{tablename}));
		DatabaseMetaData _dmd = conn.getMetaData();
		String[] cub = tablename.split("\\.");
		String schema = null;
		if (cub.length == 2) {
			schema = getNoBracketsName(cub[0]);
			tablename = getNoBracketsName(cub[1]);
		}
		else {
			tablename = getNoBracketsName(tablename);
		}
		return objectExists(_dmd, schema, tablename, types);
	}

	/**
	 * 将名称去除方括号；
	 * 例：[biapp] --> biapp
	 * @param nm
	 * @return
	 */
	private String getNoBracketsName(String nm) {
		Pattern pt = Pattern.compile("\\[(.*)\\]");
		Matcher m = pt.matcher(nm);
		if (m.matches()) {
			return m.group(1);
		}
		return nm;
	}

	public void renameTable(Connection conn, String oldname, String newname) throws SQLException {

	}

	public boolean indexExists(Connection conn, String tablename, String indexname) throws SQLException {
		return false;
	}

	public void modifyColumn(Connection conn, String tablename, String fieldname, char coltype, int len, int scale)
			throws SQLException {

	}

	public void modifyColumn(Connection conn, String tablename, String col, String new_col, char coltype, int len,
			int dec, String defaultvalue, boolean unique, boolean nullable) throws SQLException {

	}

	protected String getIdFieldDdl(String thisField, int len, int step, String desc) {
		return null;
	}

	protected String getNumericFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return null;
	}

	protected String getMemoFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return null;
	}

	protected String getClobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return null;
	}

	protected String getBlobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return null;
	}

}
