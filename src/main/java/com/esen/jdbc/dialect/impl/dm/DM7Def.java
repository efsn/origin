package com.esen.jdbc.dialect.impl.dm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

public class DM7Def extends DMDef {

	public DM7Def(Dialect dl) {
		super(dl);
	}

	public boolean indexExists(Connection conn, String tbname, String indexname)
			throws SQLException {
		String sql = "select ID from SYSINDEXES "
				+ " inner join SYSOBJECTS on SYSOBJECTS.id = SYSINDEXES.id"
				+ " and SYSOBJECTS.name='" + indexname + "'";

		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery(sql);
			try {
				return ((rs.next() ? true : false));
			} finally {
				if (rs != null)
					rs.close();
			}
		} finally {
			if (stmt != null)
				stmt.close();
		}

	}

	protected void addDescToField(Connection conn, String tbname)
			throws SQLException {
		Statement stat = conn.createStatement();
		try {
			for (int i = 0; i < tbMetaHelper.getColumnCount(); i++) {
				TableColumnMetaData fi = tbMetaHelper.getColumn(i);
				if (fi.getDesc() != null && fi.getDesc().length() > 0) {
					String csql = getDescToFieldSql(tbname, fi);
					stat.executeUpdate(csql);
				}
			}
		} finally {
			stat.close();
		}
	}

	private String getDescToFieldSql(String tbname, TableColumnMetaData fi) {
		return getDescToFieldSql(tbname, fi.getName(), fi.getDesc());
	}

	private String getDescToFieldSql(String tbname, String fieldname,
			String desc) {
		// COMMENT ON COLUMN table_name.field_name IS 'desc'
		StringBuffer sql = new StringBuffer(64);
		sql.append("COMMENT ON COLUMN ");
		sql.append(tbname);
		sql.append(".");
		sql.append(getColumnName(fieldname));
		sql.append(" IS '");
		if (desc != null)
			sql.append(desc);
		sql.append('\'');
		return sql.toString();
	}

	public void modifyColumnForDesc(Connection conn, String tablename,
			String fieldname, String desc) throws SQLException {
		String sql = getDescToFieldSql(tablename, fieldname, desc);
		Statement stat = conn.createStatement();
		try {
			stat.executeUpdate(sql);
		} finally {
			if (stat != null)
				stat.close();
		}
	}

	public void modifyColumn(Connection conn, String tablename, String col,
			String new_col, char coltype, int len, int dec,
			String defaultvalue, boolean unique, boolean nullable)
			throws SQLException {
		if (col == null || col.length() == 0) {
//			throw new SQLException("修改列名不能为空！");
			throw new SQLException(I18N.getString("JDBC.COMMON.COLUMNNAMECANNOTBEEMPTY", "修改列名不能为空！"));
		}
		String col2 = getColumnName(dl, col);
		String new_col2 = getColumnName(dl, new_col);
		List ddls = new ArrayList();

		StringBuffer renddl = null;
		if (coltype == FIELD_TYPE_FLOAT) {
			if (len > 38)
				len = 38;
			if (dec < 0)
				dec = 4;
		}
		if (new_col != null && new_col.length() > 0 && !col.equals(new_col)) {
			// 修改字段名；
			renddl = new StringBuffer(32).append("ALTER TABLE ");
			renddl.append(tablename);
			renddl.append(" ALTER COLUMN ");
			renddl.append(col2).append(" RENAME TO ").append(new_col2);
			ddls.add(renddl.toString());
			col2 = new_col2;
		}
		Dialect dl = SqlFunc.createDialect(conn);
		TableMetaData tbmd = dl.createDbMetaData(conn).getTableMetaData(
				tablename);
		TableColumnMetaData[] clmds = tbmd.getColumns();
		TableColumnMetaData tcol = null;
		for (int i = 0; i < clmds.length; i++) {
			TableColumnMetaData clmd = clmds[i];
			if (clmd.getName().equalsIgnoreCase(col))
				tcol = clmd;
		}
		if (tcol == null) {
//			throw new SQLException("表" + tablename + "不存在字段：" + col);
			Object[] param=new Object[]{tablename,col};
			throw new SQLException(I18N.getString("JDBC.COMMON.NOFIELD", "表{0}不存在字段：{1}", param));
		}
		// 修改属性：类型，长度
		/*
		 * BUG: IRPT-11085: modify by liujin 2013.08.22
		 * 判断数据类型是否一致的方法有误，进行修改
		 */
		if ((coltype != tcol.getType())
				|| len != tcol.getLen()
				|| (coltype == 'N' && (len != tcol.getLen() || dec != tcol.getScale()))) {
			/**
			 * 20091102
			 * 修改字段长度时，自动根据数据库限制，比如主键组合长度等限制，调整字段长度；
			 */

			len = this.adjustFieldLengthForModify(tbmd, col2, len);
			StringBuffer ddl = new StringBuffer(32).append("ALTER TABLE ");
			ddl.append(tablename);
			ddl.append(" MODIFY ");
			ddl.append(getFieldDefine(coltype, col2, len, dec));
			ddls.add(ddl.toString());
		}

		// 判断是否需要修改unique属性
		if (tcol.isUnique() != unique) {
			if (unique) {
				// 增加unique属性
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" ADD UNIQUE(").append(col2).append(")");
				ddls.add(sql.toString());
			} else {
				/*
				 * 删除unique属性
				 * unique有可能是主键，这里主键不能被删除；
				 */
				TableMetaData tmd = dl.createDbMetaData().getTableMetaData(
						tablename);
				String[] keys = tmd.getPrimaryKey();
				if (keys != null && keys.length == 1
						&& keys[0].equalsIgnoreCase(col2)) {
					// 判断是主键，不进行修改；
				} else {
					// String[] indexname = getUniqueIndexName(tbmd, col, true);
					// analyzeUniqueName(conn, tbmd, tablename, indexname);
					String[] indexname = analyzeUniqueName(conn,
							getUniqueIndexName(tbmd, col, true));
					if (indexname != null) {
						for (int i = 0; i < indexname.length; i++) {
							String sql = "alter table " + tablename
									+ " drop constraint " + indexname[i];
							ddls.add(sql);
						}
					}
				}
			}
		}
		/**
		 * 判断是否需要修改default值 ''和null 是一致的，所以在都非空且值不一致，或者一个空且一个非空的情况下才需要修改默认值；
		 * 这里非空是指既不是''也不是null；
		 */
		boolean isNullDefaltValue = StrFunc.isNull(defaultvalue);
		boolean isNullSrcDefaultValue = StrFunc.isNull(tcol.getDefaultValue());
		if ((!isNullDefaltValue && !isNullSrcDefaultValue && !StrFunc
				.compareStr(defaultvalue, tcol.getDefaultValue()))
				|| (isNullDefaltValue && !isNullSrcDefaultValue)
				|| (!isNullDefaltValue && isNullSrcDefaultValue)) {
			if (defaultvalue == null) {
				// 删除原有default值
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" ALTER ").append(col2)
						.append(" DROP DEFAULT ");
				ddls.add(sql.toString());
			} else {
				if (defaultvalue.length() == 0)
					defaultvalue = "''";
				// 修改defualt值
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" ALTER ").append(col2)
						.append(" SET DEFAULT ").append(defaultvalue);
				ddls.add(sql.toString());
			}
		}
		// 判断是否需要修改是否允许空值
		if (tcol.isNullable() != nullable) {
			if (nullable) {
				// 设置允许为空
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" MODIFY ")
						.append(getFieldDefine(coltype, col2, len, dec));
				sql.append(" NULL");
				ddls.add(sql.toString());
				/**
				 * 设置为空或者不为空时，执行的sql： ALTER TABLE T_TEST MODIFY STR2_
				 * VARCHAR(100) NULL 如果str2_有唯一属性，则会去除唯一，所以后面跟一个sql： ALTER TABLE
				 * T_TEST ADD UNIQUE(STR2_) 把唯一加上；
				 */
				if (unique) {
					StringBuffer sql2 = new StringBuffer(32)
							.append("ALTER TABLE ");
					sql2.append(tablename).append(" ADD UNIQUE(").append(col2)
							.append(")");
					ddls.add(sql2.toString());
				}
			} else {
				// 设置不允许空值
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" MODIFY ")
						.append(getFieldDefine(coltype, col2, len, dec));
				sql.append(" NOT NULL");
				ddls.add(sql.toString());
				if (unique) {
					StringBuffer sql2 = new StringBuffer(32)
							.append("ALTER TABLE ");
					sql2.append(tablename).append(" ADD UNIQUE(").append(col2)
							.append(")");
					ddls.add(sql2.toString());
				}
			}
		}
		Statement stmt = conn.createStatement();
		try {
			for (int i = 0; i < ddls.size(); i++) {
				stmt.execute((String) ddls.get(i));
			}
		} finally {
			stmt.close();
		}
	}

	/*
	 * DM7 中，定义唯一约束时，系统会创建唯一约束，并且同时创建唯一索引。 该索引属于系统索引，不能使用sql语句删除。
	 * 在删除唯一约束时，该索引会被同时删除。
	 */
	private String[] analyzeUniqueName(Connection conn, String[] indexname)
			throws SQLException {
		if (indexname == null || indexname.length == 0) {
			return null;
		}

		/*
		 * select O.NAME, FLAG from "SYSINDEXES" I, SYSOBJECTS O where O.ID =
		 * I.ID AND O."NAME" = 'INDEX_NAMEWE6968'
		 */
		String sql1 = "select O.NAME, FLAG from SYSINDEXES I, SYSOBJECTS O "
				+ " where O.ID = I.ID AND O.NAME = "; // + 索引名
		String sql2 = "SELECT O2.NAME AS NAME FROM SYSOBJECTS O1, SYSOBJECTS O2, SYSCONS CONS"
				+ " WHERE CONS.INDEXID=O1.ID AND O2.ID =CONS.ID AND O1.NAME="; // +
																				// 索引名
		Statement stmt = conn.createStatement();
		try {
			for (int i = 0; i < indexname.length; i++) {
				ResultSet rs = stmt.executeQuery(sql1 + "'" + indexname[i]
						+ "'");
				int flag = 0;
				if (rs.next()) {
					flag = rs.getInt("FLAG");
				}
				rs.close();

				if (flag == 1) { // 系统索引
					rs = stmt.executeQuery(sql2 + "'" + indexname[i] + "'");
					if (rs.next()) {
						indexname[i] = rs.getString("NAME");
					}
					rs.close();
				}
			}
		} finally {
			stmt.close();
		}

		return indexname;
	}

	/**
	 * {@inheritDoc}
	 */
	public void renameTable(Connection conn, String oldname, String newname) throws SQLException {
		/*
		 * BUG:BI-9555: modify by liujin 2013.09.29
		 * DM7 不支持修改表名的语句中新表名带 schema 名的语法，需要去掉 schema 名
		 * 如果新表名带 schema 名，且为默认的 schema 名，就只使用表名。
		 */
		String[] newtbs = getTableNameForDefaultSchema(newname, dbinf);
		String schema = dbinf.getDefaultSchema();

		if (StrFunc.compareStr(schema, newtbs[0])) {
			super.renameTable(conn, oldname, newtbs[1]);
		} else {
			super.renameTable(conn, oldname, newname);
		}
	}
}
