package com.esen.jdbc.dialect.impl.vertica;

import java.sql.Connection;
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
import com.esen.jdbc.pool.impl.oracle.OraclePooledPreparedStatement;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * Vertica 数据库数据定义类
 * 
 * @author liujin
 */
public class VerticaDef extends DbDef {
	/**
	 * 定义数值最大精度；
	 */
	public static final int MaxPrecision = 38;

	/**
	 * 构造方法
	 * @param dl
	 */
	public VerticaDef(Dialect dl) {
		super(dl);
	}

	/**
	 * {@inheritDoc}
	 */
	protected int adjustFieldLengthByIndex(TableMetaData tbmd, String colname,
			int len, int maxKeysLen, TableColumnMetaData col) {
		//没有索引，无限制
		return len;
	}

	/**
	 * {@inheritDoc}
	 */
	public void modifyColumnForDesc(Connection conn, String tablename,
			String fieldname, String desc) throws SQLException {
		String sql = getDescToFieldSql(conn, tablename, fieldname, desc);
		if (!StrFunc.isNull(sql)) {
			Statement stat = conn.createStatement();
			try {
				stat.executeUpdate(sql);
			} finally {
				if (stat != null)
					stat.close();
			}
		}
	}

	protected void addDescToField(Connection conn, String tbname)
			throws SQLException {
		Statement stat = conn.createStatement();
		try {
			for (int i = 0; i < tbMetaHelper.getColumnCount(); i++) {
				TableColumnMetaData fi = tbMetaHelper.getColumn(i);
				if (fi.getDesc() != null && fi.getDesc().length() > 0) {
					String csql = getDescToFieldSql(conn, tbname, fi.getName(), fi.getDesc());
					if (!StrFunc.isNull(csql)) {
						stat.executeUpdate(csql);
					}
				}
			}
		} finally {
			stat.close();
		}
	}

	private String getDescToFieldSql(Connection conn, String tbname, String fieldname,
			String desc) {
		// COMMENT ON COLUMN [[db-name.]schema.]proj_name.column_name IS {'comment' | NULL}
		StringBuffer sql = new StringBuffer(64);
		
		//tbname 可能带 schema名
		String tbs[] = getTableNameForSchema(tbname);
		String querysql = "select distinct(projection_name) from projections where anchor_table_name = '" + tbs[1] + "'";
		if (!StrFunc.isNull(tbs[0])) {
			querysql = querysql + " and projection_schema='" + tbs[0] + "'";
		}
		try {
			Statement stmt = conn.createStatement();
			try {
				ResultSet rs = stmt.executeQuery(querysql);
				try {
					while (rs.next()) {
						sql.append("COMMENT ON COLUMN ");
						sql.append(rs.getString(1));
						sql.append(".");
						sql.append(getColumnName(fieldname));
						sql.append(" IS '");
						if (desc != null) {
							sql.append(desc);
						}
						sql.append("\';");
					}
				} finally {
					rs.close();
				}
			} finally {
				stmt.close();
			}
		} catch (SQLException e) {
			//出错以后不添加 comment
			return null;	
		}
		
		return sql.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getMemoFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " VARCHAR(4000) "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_memo,
						isUpdate);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTempTableCreateDdl(String tablename, String fldDdl) {
		return "CREATE GLOBAL TEMPORARY TABLE " + tablename + "(" + fldDdl
				+ ") ON COMMIT DELETE ROWS ";
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getCreateTableByQueryStr(String tablename, boolean istemp) {
		if (istemp) {
			return "CREATE GLOBAL TEMPORARY TABLE " + tablename;
		}
		return "CREATE TABLE " + tablename;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean tableExists(Connection conn, String catalog, String tablename)
			throws SQLException {
		// 临时表用 "TABLE" 查不出来
		return objectExists(conn, tablename, new String[] { "TABLE",
				"GLOBAL TEMPORARY", "LOCAL TEMPORARY" });
	}

	/**
	 * 无法直接修改数据类型或者长度
	 */
	public void modifyColumn(Connection conn, String tablename,
			String fieldname, char coltype, int len, int scale)
			throws SQLException {
		 if (fieldname == null || fieldname.length() == 0) {
			 // throw new SQLException("修改列名不能为空！");
			 throw new SQLException(I18N.getString("JDBC.COMMON.COLUMNNAMECANNOTBEEMPTY", "修改列名不能为空！"));
		 }

		TableMetaData tmd = dl.createDbMetaData(conn).getTableMetaData(tablename);
		TableColumnMetaData cmd = tmd.getColumn(fieldname);
		if (cmd == null) {
			 // throw new SQLException("修改列名不存在！");
			 throw new SQLException(I18N.getString("JDBC.COMMON.NOFIELD", "表{0}不存在字段：{1}", new Object[]{tablename, fieldname}));
		}
		
		modifyColumn(conn, tablename, fieldname, fieldname, coltype, len, scale, 
				cmd.getDefaultValue(), cmd.isUnique(), cmd.isNullable());
	}

	public void dropTempTable(Connection conn, String catalog, String table)
			throws SQLException {
		dropTable(conn, catalog, table);
	}

	/**
	 * 为指定的表增加列
	 * 
	 * @param conn
	 *            数据库连接
	 * @param tablename
	 *            表名
	 * @param newcol
	 *            新增的列名
	 * @param coltype
	 *            新增列的数据类型
	 * @param len
	 *            新增列的长度
	 * @param dec
	 *            新增列的标度
	 * @param defaultvalue
	 *            新增列的默认值
	 * @param nullable
	 *            新增列是否可为空
	 * @param unique
	 *            新增列是否唯一
	 */
	public void addColumn(Connection conn, String tablename, String newcol,
			char coltype, int len, int dec, String defaultvalue,
			boolean nullable, boolean unique) throws SQLException {
		StringBuffer ddl = new StringBuffer(64).append("ALTER TABLE ");
		ddl.append(tablename);
		ddl.append(" ADD COLUMN ");
		ddl.append(getFldDdl(
				new TableColumnMetaDataHelper(newcol, coltype, len, dec,
						unique ? false : nullable, false, defaultvalue, null),
				true));

		StringBuffer ddl2 = new StringBuffer(64);
		if (unique) {
			ddl2.append("ALTER TABLE ");
			ddl2.append(tablename);
			ddl2.append(" ADD UNIQUE(");
			ddl2.append(newcol);
			ddl2.append(")");
		}

		Statement stmt = conn.createStatement();
		System.out.println(ddl.toString());
		try {
			stmt.executeUpdate(ddl.toString());
			conn.commit();

			if (unique) {
				stmt.executeUpdate(ddl2.toString());
				conn.commit();
			}
		} finally {
			stmt.close();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void dropColumn(Connection conn, String tablename, String col)
			throws SQLException {
		StringBuffer ddl = new StringBuffer(64).append("ALTER TABLE ");
		ddl.append(tablename);
		ddl.append(" DROP COLUMN ").append(col).append(" CASCADE");
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(ddl.toString());
		} finally {
			stmt.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void renameTable(Connection conn, String oldname, String newname)
			throws SQLException {
		Statement ddl = conn.createStatement();
		try {
			ddl.executeUpdate("ALTER TABLE " + oldname + " RENAME TO "
					+ newname);
			conn.commit();
		} finally {
			ddl.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getBlobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " LONG VARBINARY(10000000) "
				+ getTailDdl(null, fi.isNullable(), fi.isUnique(),
						fi.getDesc(), DbDefiner.FIELD_TYPE_BINARY, isUpdate);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean indexExists(Connection conn, String tbname, String indexname)
			throws SQLException { 
		//Vertica 不支持索引
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getIdFieldDdl(String thisField, int len, int step, String desc) {
		return thisField + " AUTO_INCREMENT ";
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getClobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " LONG VARCHAR(10000000) "
				+ getTailDdl(null, fi.isNullable(), fi.isUnique(),
						fi.getDesc(), DbDefiner.FIELD_TYPE_CLOB, isUpdate);
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getIntFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		int len = fi.getLen();
		if (len <= 0 || len > 18) {
			return getNumericFldDdl(fi, isUpdate);
		}
		
		String type = "INTEGER";
		if (len < 3) {
			type = "TINYINT";
		} else if (len < 5) {
			type = "SMALLINT";
		} else if (len < 10) {
			type = "INTEGER";
		} else {
			type = "BIGINT";
		}

		return getColumnName(fi.getName())
				+ " " + type + " "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_INT,
						isUpdate);
	}

	/**
	 * 用于存放生成数值型字段定义的容器，由于可能一张表有很多数值字段，所以这里使用一个StringBuffer对象；
	 */
	private StringBuffer numddl;

	/**
	 * {@inheritDoc}
	 */
	protected String getNumericFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		if (numddl == null) {
			numddl = new StringBuffer(64);
		} else {
			numddl.setLength(0);
		}
		
		numddl.append(getColumnName(fi.getName()));
		numddl.append(" NUMERIC");
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

	/**
	 * {@inheritDoc}
	 */
	protected String getTimeFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " TIME "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_TIME,
						isUpdate);
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getTimeStampFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " TIMESTAMP "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(),
						DbDefiner.FIELD_TYPE_TIMESTAMP, isUpdate);
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getStrFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " VARCHAR("
				+ (fi.getLen() > 0 ? fi.getLen() : 1)
				+ ") "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_STR,
						isUpdate);
	}

	protected String getFieldCommentSql(Connection conn, String viewname, String colname,
			String comment) {
		if (StrFunc.isNull(viewname) || StrFunc.isNull(colname)
				|| StrFunc.isNull(comment))
			return "";

		return getDescToFieldSql(conn, viewname, colname, comment);
	}

	/**
	 * Vertica 不支持索引
	 */
	public String createIndex(Connection conn, String table, String indexName,
			String[] indexFields, boolean indexUnique,
			boolean ifIndexNameExistThrowException) throws SQLException {
		return null;
	}

	/**
	 * 更改表结构 不直接支持表字段的更名，类型的转换，类型长度的变化等
	 * 根据要修改的字段建新表，copy原表数据到新表，删除原表，更名新表为原表名； 注意：在进行写入表数据过程中，不能调用此方法修改字段信息；
	 * 比如DataCopy的写入功能；需要跟据值长度，来修改表的字段长度；
	 * 
	 * @param conn 		Connection
	 * @param tablename tring
	 * @param coltype 	char
	 * @param len 		int
	 * @param dec 		int
	 * @param col 		String
	 * @throws Exception
	 */
	public void modifyColumn(Connection conn, String tablename, String col,
			String new_col, char coltype, int len, int dec,
			String defaultvalue, boolean unique, boolean nullable)
			throws SQLException {
		if (col == null || col.length() == 0) {
			throw new SQLException(I18N.getString("JDBC.COMMON.COLUMNNAMECANNOTBEEMPTY", "修改列名不能为空！"));
		}

		//这里不用系统变量dl，是因为此方法给了conn, 通过系统变量dl获取表结构时，会造成嵌套获取连接；
		Dialect dl = SqlFunc.createDialect(conn);
		
		TableMetaData tmd = dl.createDbMetaData(conn).getTableMetaData(tablename);
		TableColumnMetaData cmd = tmd.getColumn(col);
		
		/*
		 * datatype, len, scale 发生改变时，需要通过建新表实现
		 * 其他修改不需要建新表
		 */
		if (isDataTypeChange(SqlFunc.getType(cmd.getType()), cmd.getLen(), cmd.getScale(), coltype, len, dec)) {
			String newtbname = createTable(conn, dl, tmd, col, new_col, coltype,
					len, dec, defaultvalue, unique, nullable, false);
			String copysql = getCopyeSql(conn, dl, tmd, newtbname, col, new_col,
					coltype, false);
			Statement stmt = conn.createStatement();
			try {
				stmt.execute(copysql);
			} finally {
				stmt.close();
			}
			dropTable(conn, null, tablename);
			renameTable(conn, newtbname, tablename);
			return;
		}
		
		/* 处理不需要重建表可以完成的列定义修改
		 * 1.列名
		 * 2.默认值
		 * 3.唯一性
		 * 4.是否可以为空
		 * 5.注释
		 */
		String col2 = getColumnName(dl, col);
		String new_col2 = getColumnName(dl, new_col);
		List ddls = new ArrayList();

		StringBuffer renddl = null;
		if (new_col != null && new_col.length() > 0 && !col.equals(new_col)) {
			// 修改字段名；
			renddl = new StringBuffer(32).append("ALTER TABLE ");
			renddl.append(tablename);
			renddl.append(" RENAME COLUMN ");
			renddl.append(col2).append(" TO ").append(new_col2);
			ddls.add(renddl.toString());
			col2 = new_col2;
		}

		// 判断是否需要修改unique属性
		if (cmd.isUnique() != unique) {
			if (unique) {
				// 增加unique属性
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" ADD UNIQUE(").append(col2).append(")");
				ddls.add(sql.toString());
			} else {
				// 删除unique属性
				// unique有可能是主键，这里主键不能被删除
				String[] keys = tmd.getPrimaryKey();
				if (keys != null && keys.length == 1
						&& keys[0].equalsIgnoreCase(col2)) {
					// 判断是主键，不进行修改；
				} else {
					//需要使用约束名删除，先要获取约束名
					String uniqueConstaintName = "";
					String[] tbs = DbDef.getTableNameForDefaultSchema(tablename, dl.getDataBaseInfo());
					String querySql = "select constraint_name from constraint_columns where constraint_type='u' and table_name='" 
							+ tbs[1] + "'";
					if (!StrFunc.isNull(tbs[0])) {
						querySql = querySql + " and table_schema='" + tbs[0] + "'";
					}

					try {
						Statement stmt = conn.createStatement();
						try {
							ResultSet rs = stmt.executeQuery(querySql);
							try {
								if (rs.next()) {
									uniqueConstaintName = rs.getString(1);
								}
							} finally {
								rs.close();
							}
						} finally {
							stmt.close();
						}
						
						StringBuffer sql = new StringBuffer(32)
						.append("ALTER TABLE ");
						sql.append(tablename).append(" DROP CONSTRAINT ").append(uniqueConstaintName);
						ddls.add(sql.toString());
					} catch (SQLException e) {
						;//没有查询到约束名
					}
				}
			}
		}
		
		//判断是否需要修改default值 
		boolean isNullDefaltValue = StrFunc.isNull(defaultvalue);
		boolean isNullSrcDefaultValue = StrFunc.isNull(cmd.getDefaultValue());
		if ((!isNullDefaltValue && !isNullSrcDefaultValue && !StrFunc
				.compareStr(defaultvalue, cmd.getDefaultValue()))
				|| (isNullDefaltValue && !isNullSrcDefaultValue)
				|| (!isNullDefaltValue && isNullSrcDefaultValue)) {
			if (defaultvalue == null) {
				// 删除原有default值
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" ALTER COLUMN ").append(col2)
						.append(" DROP DEFAULT ");
				ddls.add(sql.toString());
			} else {
				if (defaultvalue.length() == 0)
					defaultvalue = "''";
				// 修改default值
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" ALTER COLUMN ").append(col2)
						.append(" SET DEFAULT ").append(defaultvalue);
				ddls.add(sql.toString());
			}
		}
		// 判断是否需要修改是否允许空值
		if (cmd.isNullable() != nullable) {
			if (nullable) {
				// 设置允许为空
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" ALTER COLUMN ").append(col2)
						.append(" DROP NOT NULL");
				ddls.add(sql.toString());
			} else {
				// 设置不允许空值
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" ALTER COLUMN ").append(col2)
						.append(" SET NOT NULL");
				ddls.add(sql.toString());
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
	
	  /**
	   * 如果原类型srcType和目标类型coltype不一致，返回true；

	   * @param srcType
	   * @param coltype
	   * @return
	   */
	  private boolean isDataTypeChange(char srcType, int srclen, int srcscale, char destType, int destlen, int destscale) {
	    if (srcType == 'P' && destType == 'T') {
	      return false;
	    }
	    
	    /*
		 * 整形字段，当目标字段是整形，且长度和原字段相同，小数位数都是0,则认为字段类型是相同的，不需要修改；
	     */
	    if (((srcType=='N' && destType=='I') || (srcType == 'I' && destType=='N')))
	    	if (destlen > 38 || destlen < 0) {
	    		destlen = 38;
	    	}
	    
	    	if (srclen == destlen 
	    		&& srcscale == destscale
	    		&& destscale == 0) {
	      return false;
	    }
	    
	    //逻辑型
	    if (srcType == DbDefiner.FIELD_TYPE_STR 
	    		&& srclen == 1
	    		&& destType == DbDefiner.FIELD_TYPE_LOGIC) {
	    	return false;
	    }
	 
	    //字符类型长度改变
	    if (destType == 'C' && destlen != srclen){
	    	return true;
	    }
	    
	    if (destType == 'N' && (srclen != destlen || srcscale != destscale)){
	    	return true;
	    }
	    
	    return srcType != destType;
	  }

	/**
	 * 为修改字段和删除字段后，创建的新表 copy数据；
	 * 
	 * @param conn
	 * @param dl
	 * @param tmd
	 * @param newtbname
	 * @param col
	 * @param new_col
	 * @param coltype
	 * @param isDel
	 *            是否删除字段col的标识； =true , 则 new_col,coltype参数无效；
	 * @return
	 */
	private String getCopyeSql(Connection conn, Dialect dl, TableMetaData tmd,
			String newtbname, String col, String new_col, char coltype,
			boolean isDel) {
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
					sql.append(
							getColumnName(new_col == null
									|| new_col.length() == 0 ? colname
									: new_col)).append(',');
				}
			} else {
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
					sql.append(
							dl.funcToSqlVar(getColumnName(colname),
									colmd.getType(), getSqlType(coltype), null))
							.append(',');
				}
			} else {
				sql.append(getColumnName(colname)).append(',');
			}
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(" from ").append(tmd.getTableName());
		return sql.toString();
	}

	/**
	 * 为修改字段和删除字段创建新的表；
	 * 
	 * @param conn
	 * @param dl
	 * @param tmd
	 * @param col
	 * @param new_col
	 * @param coltype
	 * @param len
	 * @param dec
	 * @param defaultvalue
	 * @param unique
	 * @param nullable
	 * @param isDel 是否删除字段col的标识； =true , 则
	 *            new_col,coltype,len,dec,defualtvalue,unique,nullable参数无效；
	 * @return
	 * @throws Exception
	 */
	private String createTable(Connection conn, Dialect dl, TableMetaData tmd,
			String col, String new_col, char coltype, int len, int dec,
			String defaultvalue, boolean unique, boolean nullable, boolean isDel)
			throws SQLException {
		DbDefiner dbf = dl.createDbDefiner();
		dbf.clearDefineInfo();
		String desttable = dbf.getCreateTableName(conn, tmd.getTableName(),
				null);
		String incName = null;// 自动增长字段名；
		String[] keys = tmd.getPrimaryKey();
		for (int i = 0; i < tmd.getColumnCount(); i++) {
			TableColumnMetaData colmd = tmd.getColumn(i);
			String colname = colmd.getName();
			boolean iskey = isKey(colname, keys);
			if (colname.equalsIgnoreCase(col)) {
				// 如果是主键，不能为空；
				if (!isDel) {
					// 定义修改字段；
					dbf.defineField(
							new_col == null || new_col.length() == 0 ? colname
									: new_col, coltype, len, dec,
							getDefineDefaultValue(defaultvalue, coltype),
							iskey ? false : nullable, iskey ? false : unique);
				}
			} else {
				if (colmd.isAutoInc()) {
					incName = colname;
					dbf.defineAutoIncField(colname, 1);
				} else {
					char tp = getFieldType(colmd.getType());
					int l = colmd.getLen();
					int cl = colmd.getScale();
					// 不用修改的字段，使用原来的属性
					dbf.defineField(colname, tp, l, cl,
							getDefineDefaultValue(colmd.getDefaultValue(), tp),
							colmd.isNullable(),
							iskey ? false : colmd.isUnique(), colmd.getDesc());
				}
			}
		}
		// 创建主键
		if (keys != null) {
			StringBuffer ks = new StringBuffer(10 * keys.length);
			for (int i = 0; i < keys.length; i++) {
				/**
				 * 主键中的字段可能改名，创建新表是用新的字段建主键； 否则建表失败，主键字段不是表的字段；
				 */
				String keyfield = keys[i];
				if (keyfield.equalsIgnoreCase(col)) {
					if (isDel)
						continue;// 删除的字段是主键之一；
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

		/**
		 *  修改字段自动调整字段长度
		 */
		dbf.createTable(conn, desttable, false, true);
		return desttable;
	}

	/**
	 * 建表时，给的默认值需要用'引起来；
	 * 
	 * @param defaultValue
	 * @param coltype
	 * @return
	 */
	private String getDefineDefaultValue(String defaultValue, char coltype) {
		if (defaultValue == null) {
			return null;
		}
		
		if (coltype != DbDefiner.FIELD_TYPE_STR) {
			return defaultValue;
		}
		
		if (defaultValue.length() == 0) {
			return "''";
		}
		
		if (defaultValue.length() >= 2 && defaultValue.startsWith("'")
				&& defaultValue.endsWith("'")) {
			return defaultValue;
		}
		
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
}
