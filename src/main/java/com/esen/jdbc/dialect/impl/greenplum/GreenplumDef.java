package com.esen.jdbc.dialect.impl.greenplum;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.impl.sql92.SQL92Def;
import com.esen.util.i18n.I18N;

public class GreenplumDef extends SQL92Def {

	public GreenplumDef(Dialect dl) {
		super(dl);
	}

	public String createIndex(Connection conn, String table, String indexName, String[] indexFields,
	        boolean indexUnique, boolean ifIndexNameExistThrowException) throws SQLException {
		return super.createIndex(conn, table, indexName, indexFields, false, ifIndexNameExistThrowException);
	}

	protected String getTailDdl(String defaultvalue, boolean nullable, boolean unique, String desc, char t,
	        boolean isUpdate) {
		return super.getTailDdl(defaultvalue, nullable, false, desc, t, isUpdate);
	}

	protected String getIdFieldDdl(String thisField, int step) {
		return thisField + " serial";
	}

	protected String getBlobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
		        + " bytea "
		        + getTailDdl(fi.getDefaultValue(), fi.isNullable(), false, fi.getDesc(), DbDefiner.FIELD_TYPE_BINARY,
		                isUpdate);
	}

	protected String getClobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
		        + " text "
		        + getTailDdl(fi.getDefaultValue(), fi.isNullable(), false, fi.getDesc(), DbDefiner.FIELD_TYPE_BINARY,
		                isUpdate);

	}

	protected String getStrFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		// if(fi.getLen()>)
		return getColumnName(fi.getName())
		        + " VARCHAR("
		        + (fi.getLen() > 0 ? fi.getLen() : 1)
		        + ") "
		        + getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(), fi.getDesc(),
		                DbDefiner.FIELD_TYPE_STR, isUpdate);
	}

	protected String getNumericFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		StringBuffer numddl = new StringBuffer();
		numddl.append(getColumnName(fi.getName()));
		// 如果长度为0，则不指定长度；
		if (fi.getLen() > 0) {
			numddl.append(" DECIMAL");
			int[] prcs = formatNumberPrecision(fi.getLen(), fi.getScale(), 38);
			numddl.append('(').append(prcs[0]).append(',').append(prcs[1]).append(')');
		} else {
			numddl.append(" DECIMAL");
		}
		numddl.append(' ');
		numddl.append(getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(), fi.getDesc(),
		        DbDefiner.FIELD_TYPE_FLOAT, isUpdate));
		return numddl.toString();
	}

	protected String getFieldDefine(char coltype, String fieldname, int len, int scale) throws SQLException {
		switch (coltype) {
		case DbDefiner.FIELD_TYPE_FLOAT:
			int[] prcs = formatNumberPrecision(len, scale, 38);
			if (prcs[0] == 0) {
				return fieldname + " TYPE DECIMAL";
			}
			return fieldname + " TYPE DECIMAL(" + prcs[0] + "," + prcs[1] + ")";
		}
		return super.getFieldDefine(coltype, fieldname, len, scale);
	}

	public void modifyColumn(Connection conn, String tablename, String col, String new_col, char coltype, int len,
	        int dec, String defaultvalue, boolean unique, boolean nullable) throws SQLException {
		if (col == null || col.length() == 0) {
			// throw new SQLException("修改列名不能为空！");
			throw new SQLException(I18N.getString("JDBC.COMMON.COLUMNNAMECANNOTBEEMPTY",
			        "修改列名不能为空！"));
		}
		TableColumnMetaData colMeta = this.dl.getTableColumnMetaData(tablename, col);
		if (col.equals(new_col)||new_col==null||new_col.equals("")) {// 不涉及列改名
			if (defaultvalue!=null&&!defaultvalue.equals("")) {
				StringBuffer ddl = new StringBuffer(32).append("ALTER TABLE ");
				ddl.append(tablename);
				ddl.append(" ALTER ");
				ddl.append(col);
				ddl.append(" SET DEFAULT ");
				ddl.append(defaultvalue);
				Statement stmt = conn.createStatement();
				try {
					stmt.execute(ddl.toString());
				} finally {
					stmt.close();
				}
			}
			this.modifyColumn(conn, tablename, col, coltype, len, dec);
		} else {// 列需要改名
			StringBuffer ddl = new StringBuffer(32).append("ALTER TABLE ");
			ddl.append(tablename);
			ddl.append(" RENAME COLUMN ");
			ddl.append(col);
			ddl.append(" TO ");
			ddl.append(new_col);
			Statement stmt = conn.createStatement();
			try {
				stmt.execute(ddl.toString());
			} finally {
				stmt.close();
			}
			if (colMeta.getDefaultValue() != null && !colMeta.getDefaultValue().equals(defaultvalue)) {
				ddl = new StringBuffer(32).append("ALTER TABLE ");
				ddl.append(tablename);
				ddl.append(" ALTER ");
				ddl.append(col);
				ddl.append(" SET DEFAULT");
				ddl.append(defaultvalue);
				stmt = conn.createStatement();
				try {
					stmt.execute(ddl.toString());
				} finally {
					stmt.close();
				}
			}
			if (colMeta.getLen() != len) {
				this.modifyColumn(conn, tablename, new_col, coltype, len, dec);
			}
		}
	}
	public void dropTable(Connection conn, String catalog, String table) throws
	  SQLException {
	    if (catalog != null) {
	      conn.setCatalog(catalog);
	    }
	    Statement ddl = conn.createStatement();
	    /**
	     * BI-5061
	     * 对于某些特殊情况下创建的表，表名包含空格，这时删除该表，表名必需加引号；
	     */
	    if(table.indexOf(" ")>=0){
	    	table = "\""+table+"\"";
	    }
	    try {
	    	ddl.executeUpdate("DROP TABLE " + table.toLowerCase());
	    }catch(SQLException se){
	    	/**
	    	 * 多线程调用删除同一个表时，会出现删除的表已经不存在的情况，即使实现判断表是否存在也没用；
	    	 * 这里使用异常机制来处理，如果出新异常，则表示这个表已经被删除，直接返回，不抛出异常；
	    	 */
	    	return;
	    }
	    finally {
	      ddl.close();
	    }
	  }
}
