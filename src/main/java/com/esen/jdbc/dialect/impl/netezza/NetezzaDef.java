package com.esen.jdbc.dialect.impl.netezza;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.impl.sql92.SQL92Def;
import com.esen.util.i18n.I18N;

public class NetezzaDef extends SQL92Def {
	/**
	 * 定义数值最大精度；
	 */
	public static final int MaxPrecision = 38;

	protected String getFieldDefine(char coltype, String fieldname, int len,
			int scale) throws SQLException {
		if(DbDefiner.FIELD_TYPE_STR==coltype){
			return fieldname + " NVARCHAR("+len+")";
		}else{
//			throw new RuntimeException("Netezza数据库不支持修改非varchar字段长度或修改字段类型");
			throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.netezza.netezzadef.unsupportchf", "Netezza数据库不支持修改非varchar字段长度或修改字段类型"));
		}
		
	}
	protected String getTailDdl(String defaultvalue, boolean nullable,
			boolean unique, String desc, char t, boolean isUpdate) {
		StringBuffer str = new StringBuffer(16);
	    if (defaultvalue != null && defaultvalue.length()>0) {
	      str.append(" DEFAULT ").append(defaultvalue);
	    }
	    if(unique) str.append(" UNIQUE ");
	    if(!nullable){ 
	    	str.append(" NOT NULL ");
	    }else{
	    	str.append(" NULL ");
	    }
	    return str.toString();
	}

	public NetezzaDef(Dialect dl) {
		super(dl);
	}


	public void renameTable(Connection conn, String oldname, String newname)
			throws SQLException {
		Statement ddl = conn.createStatement();
		try {
			ddl.executeUpdate("ALTER TABLE " + oldname + " RENAME TO "
					+ newname);
		} finally {
			ddl.close();
		}

	}

	public boolean indexExists(Connection conn, String tablename,
			String indexname) throws SQLException {
		return false;
	}

	public void modifyColumn(Connection conn, String tablename,
			String fieldname, char coltype, int len, int scale)
			throws SQLException {
		StringBuffer ddl = new StringBuffer(32).append("ALTER TABLE ");
		ddl.append(tablename);
		ddl.append(" MODIFY COLUMN (");
		ddl.append(this.getFieldDefine(coltype, fieldname, len, scale)).append(")");
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(ddl.toString());
		} finally {
			stmt.close();
		}
	}

	/**
	 * BI-6448 主题表同步到数据库表的问题
	 * 修改列类型及长度的变更时的问题
	 * 修改默认值、是否唯一、是否为空的处理
	 * modify by baochl
	 * 2012.4.18
	 */
	public void modifyColumn(Connection conn, String tablename, String col,
			String new_col, char coltype, int len, int dec,
			String defaultvalue, boolean unique, boolean nullable)
			throws SQLException {
		TableColumnMetaData colMeta = this.dl.getTableColumnMetaData(tablename, col);
		if( colMeta.isUnique()!=unique || colMeta.isNullable()!=nullable){
//			throw new RuntimeException("Netezza数据库不支持修改字段的唯一或是否为空");
			throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.netezza.netezzadef.unsupportchfnull", "Netezza数据库不支持修改字段的唯一或是否为空"));
		}
		if(col.equals(new_col)){//不涉及列改名
			if((colMeta.getType()!=Types.VARCHAR)){
//				throw new RuntimeException("Netezza数据库不支持修改非varchar字段长度或修改字段类型");
				throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.netezza.netezzadef.unsupportchfnvarc", "Netezza数据库不支持修改非varchar字段长度或修改字段类型"));
			}else{
				if(colMeta.getDefaultValue()!=null&&!colMeta.getDefaultValue().equals(defaultvalue)){
					StringBuffer ddl = new StringBuffer(32).append("ALTER TABLE ");
					ddl.append(tablename);
					ddl.append(" ALTER ");
					ddl.append(col);
					ddl.append(" SET DEFAULT");
					ddl.append(defaultvalue);
					Statement stmt = conn.createStatement();
					try {
						stmt.execute(ddl.toString());
					} finally {
						stmt.close();
					}
				}
				this.modifyColumn(conn, tablename, col, coltype, len, dec);
			}
		}else{//列需要改名
			if(SqlFunc.getType(colMeta.getType())!=coltype){
//				throw new RuntimeException("Netezza数据库不支持修改字段类型");
				throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.netezza.netezzadef.unsupportchfkind", "Netezza数据库不支持修改字段类型"));
			}else if(colMeta.getLen()!=len && colMeta.getType()!=Types.VARCHAR){
//				throw new RuntimeException("Netezza数据库不支持修改非varchar字段长度或修改字段类型");
				throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.netezza.netezzadef.unsupportchfnvarc", "Netezza数据库不支持修改非varchar字段长度或修改字段类型"));
			}else{
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
				if(colMeta.getDefaultValue()!=null&&!colMeta.getDefaultValue().equals(defaultvalue)){
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
				if(colMeta.getLen()!=len){
					this.modifyColumn(conn, tablename, new_col, coltype, len, dec);
				}
			}
		}
	}
	/**
	 * BI-6448 主题表同步到数据库表的问题
	 * 修改删除列的问题
	 * modify by baochl
	 * 2012.4.18
	 */
	public void dropColumn(Connection conn, String tablename, String col)
			throws SQLException {
		StringBuffer ddl = new StringBuffer(64).append("ALTER TABLE ");
	    ddl.append(tablename);
	    ddl.append(" DROP ").append(col).append(" CASCADE");
	    Statement stmt = conn.createStatement();
	    try {
	      stmt.execute(ddl.toString());
	    }
	    finally {
	      stmt.close();
	    }
	}
	protected String getIdFieldDdl(String thisField, int step) {
		return thisField + " numeric(30) NOT NULL ";
	}

	protected String getNumericFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		StringBuffer numddl = new StringBuffer(64);// 初始长度由16改为64，如果表的数值型字段很多，还是有影响的；
		numddl.append(getColumnName(fi.getName()));
		numddl.append(" numeric");
		// 如果长度为0，则不指定长度；
		if (fi.getLen() > 0) {
			int[] prcs = formatNumberPrecision(fi.getLen(), fi.getScale(),
					MaxPrecision);
			numddl.append('(').append(prcs[0]).append(',').append(prcs[1])
					.append(')');
		}
		numddl.append(' ');
		numddl.append(getTailDdl(fi.getDefaultValue(), fi.isNullable(),
				fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_FLOAT,
				isUpdate));
		return numddl.toString();
	}

	protected String getMemoFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " NVARCHAR(4000) "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_CLOB,
						isUpdate);
	}

	protected String getClobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return null;
	}

	protected String getBlobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return null;
	}

	/**
	 * 字符串类型，netezza使用nvarchar支持汉字
	 * 
	 * @param fldname
	 *            String
	 * @param len
	 *            int
	 * @param defaultvalue
	 *            String
	 * @param nullable
	 *            boolean
	 * @param unique
	 *            boolean
	 * @return String
	 */
	protected String getStrFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " NVARCHAR("
				+ (fi.getLen() > 0 ? fi.getLen() : 1)
				+ ") "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_STR,
						isUpdate);
	}
}
