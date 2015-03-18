package com.esen.jdbc.dialect.impl.teradata;

import java.sql.*;
import java.util.ArrayList;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataHelper;
import com.esen.jdbc.dialect.impl.TableMetaDataHelper;
import com.esen.jdbc.pool.PooledSQLException;
import com.esen.util.ArrayFunc;
import com.esen.util.i18n.I18N;

/**
 * Teradata 数据库数据定义类
 *
 * @author liujin
 */
public class TeradataDef extends DbDef {

	/**
	 * 定义数值类型最大精度；
	 */
	public static final int MaxPrecision = 38;

	/**
	 * 构造方法
	 * @param dl 方言
	 */
	public TeradataDef(Dialect dl) {
		super(dl);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void checkKeysAndIndexesMaxLength(TableMetaDataHelper tmdh) {
		int maxKeyLength = dbinf.getMaxKeyOfFieldsLength();

		// 检查字段
		for (int i = 0; i < tmdh.getColumnCount(); i++) {
			TableColumnMetaDataHelper fieldi = (TableColumnMetaDataHelper) tmdh.getColumn(i);
			
			// varchar 类型最大长度
			adjustFieldLength(new TableColumnMetaDataHelper[] { fieldi },
					dbinf.getMaxVarcharFieldLength());

			// 唯一属性的字段，必须为  not null
			if (fieldi.isUnique()) {
				fieldi.setNullable(false);
				adjustFieldLength(new TableColumnMetaDataHelper[] { fieldi },
						maxKeyLength);
			}
		}

		// 检查主键
		String[] _pkeys = tmdh.getPrimaryKey();
		if (_pkeys != null && _pkeys.length > 0) {
			TableColumnMetaDataHelper[] keys = new TableColumnMetaDataHelper[_pkeys.length];
			for (int i = 0; i < _pkeys.length; i++) {
				keys[i] = (TableColumnMetaDataHelper) tmdh.getColumn(_pkeys[i]);
				TableColumnMetaDataHelper keyfield = keys[i];
				
				// 主键字段不允许为空，否则报异常：
				if (keyfield.isNullable()) {
					keyfield.setNullable(false);
				}
			}
			adjustFieldLength(keys, maxKeyLength);
		}

		// 检查索引
		TableIndexMetaData[] _index = tmdh.getIndexes();
		if (_index != null && _index.length > 0) {
			for (int i = 0; i < _index.length; i++) {
				TableIndexMetaData indx = _index[i];
				String[] fields = indx.getColumns();
				TableColumnMetaDataHelper[] keys = new TableColumnMetaDataHelper[fields.length];
				for (int j = 0; j < fields.length; j++) {
					keys[j] = (TableColumnMetaDataHelper) tmdh.getColumn(fields[j]);
					
					// 索引字段不允许为空，否则报异常：
					if (keys[j].isNullable()) {
						keys[j].setNullable(false);
					}
				}
				adjustFieldLength(keys, maxKeyLength);
			}
		}
	}

	/**
	 * 获取指定字段左边指定个数的字符的函数
	 * 
	 * @param fldname 字段名
	 * @param cnt 字符个数
	 * @return sql语句
	 */
	public String funcLeftSql(String fldname, int cnt) {
		return " SUBSTR(" + fldname + ", 1, " + cnt + ") ";
	}

	/**
	 * {@inheritDoc}
	 */
	public void modifyColumnForDesc(Connection conn, String tablename,
			String fieldname, String desc) throws SQLException {
		String sql = getDescToFieldSql(tablename, fieldname, desc);
		Statement stat = conn.createStatement();
		try {
			stat.executeUpdate(sql);
			conn.commit();
		} finally {
			if (stat != null)
				stat.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void addDescToField(Connection conn, String tbname)
			throws SQLException {
		Statement stat = conn.createStatement();
		try {
			for (int i = 0; i < tbMetaHelper.getColumnCount(); i++) {
				TableColumnMetaData fi = tbMetaHelper.getColumn(i);
				if (fi.getDesc() != null && fi.getDesc().length() > 0) {
					String csql = getDescToFieldSql(tbname, fi);
					stat.executeUpdate(csql);
					conn.commit();
				}
			}
		} finally {
			stat.close();
		}
	}

	/**
	 * 获取对指定的表的列添加描述信息的sql语句
	 * 
	 * @param tbname 表名
	 * @param fi 字段元信息对象
	 * @return sql语句
	 */
	private String getDescToFieldSql(String tbname, TableColumnMetaData fi) {
		return getDescToFieldSql(tbname, fi.getName(), fi.getDesc());
	}

	/**
	 * 获取对指定的表的列添加描述信息的sql语句
	 * 
	 * @param tbname 表名
	 * @param fieldname 字段名
	 * @param desc 描述信息
	 * @return sql语句
	 */
	private String getDescToFieldSql(String tbname, String fieldname,
			String desc) {
		StringBuffer sql = new StringBuffer(64);
		sql.append("COMMENT ON COLUMN ");
		sql.append(tbname).append(".").append(getColumnName(fieldname));
		sql.append(" IS '");
		if (desc != null) {
			sql.append(desc);
		}
		sql.append('\'');
		return sql.toString();
	}

	/**
	 * 定义Memo字段 
	 * 
	 * @param fldname 字段名
	 * @param defaultvalue 默认值
	 * @param nullable 是否可为空
	 * @param unique 是否唯一
	 * @return 字段定义的 sql 语句片段
	 */
	protected String getMemoFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		/*
		 * BUG:BI-8594: modify by liujin 2013.07.10
		 * 字符类型区分大小写。
		 */
		return getColumnName(fi.getName())
				+ " LONG VARCHAR CASESPECIFIC "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_memo,
						isUpdate);
	}

	/**
	 * 获得创建临时表的 sql 语句 
	 * 
	 * @param tablename 表名
	 * @param fldDdl 表结构的子查询的语句
	 * @return 创建临时表的 sql 语句
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
			return "CREATE GLOBAL TEMPORARY TABLE " + tablename
					+ " ON COMMIT DELETE ROWS ";
		}
		return "CREATE TABLE " + tablename;
	}

	/**
	 * {@inheritDoc}
	 */
	public void modifyColumn(Connection conn, String tablename,
			String fieldname, char coltype, int len, int scale)
			throws SQLException {
		StringBuffer ddl = new StringBuffer(32).append("ALTER TABLE ");
		ddl.append(tablename);
		ddl.append(" ADD ");
		ddl.append(getFieldDefine(coltype, fieldname, len, scale));
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(ddl.toString());
			conn.commit();
		} finally {
			stmt.close();
		}
	}

	/**
	 * 获取字段定义的语句
	 * 
	 * @param coltype 数据类型
	 * @param fieldname 字段名
	 * @param len 字段长度
	 * @param scale 字段标度
	 * @return 字段定义语句
	 * @throws SQLException 对于不支持的数据类型抛出异常
	 */
	private String getFieldDefine(char coltype, String fieldname, int len,
			int scale) throws SQLException {
		switch (coltype) {
		case DbDefiner.FIELD_TYPE_STR:
			/*
			 * BUG:BI-8594: modify by liujin 2013.07.10
			 * 字符类型区分大小写。
			 */
			return fieldname + " VARCHAR(" + len + ") CASESPECIFIC ";
		case DbDefiner.FIELD_TYPE_INT: {
			return fieldname + " INTEGER ";
		}
		case DbDefiner.FIELD_TYPE_FLOAT: {
			return formatModifyNumberDefiner(fieldname, len, scale);
		}
		case DbDefiner.FIELD_TYPE_DATE:
			return fieldname + " DATE ";
		case DbDefiner.FIELD_TYPE_TIME:
			return fieldname + " TIME ";
		case DbDefiner.FIELD_TYPE_TIMESTAMP:
			return fieldname + " TIMESTAMP ";
		case DbDefiner.FIELD_TYPE_LOGIC:
			return fieldname + " VARCHAR(1)";
		case DbDefiner.FIELD_TYPE_memo:
			/*
			 * BUG:BI-8594: modify by liujin 2013.07.10
			 * 字符类型区分大小写。
			 */
			return fieldname + " LONG VARCHAR CASESPECIFIC ";
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

	/**
	 * 获取 number 类型字段定义语句
	 * 
	 * @param fieldname 字段名
	 * @param len 精度
	 * @param scale 标度
	 * @return 字段定义语句
	 */
	private String formatModifyNumberDefiner(String fieldname, int len,
			int scale) {
		int prcs[] = formatNumberPrecision(len, scale, MaxPrecision);
		/*
		 * BUG:BI-8611： modify by liujin 2013.06.26
		 * 将 NUMBER 类型改为 NUMERIC 类型
		 */
		if (prcs[0] == 0) {
			return fieldname + " NUMERIC";
		}
		return fieldname + " NUMERIC(" + prcs[0] + "," + prcs[1] + ")";
	}

	/**
	 * 更改表结构
	 * 不直接支持表字段的更名，类型的转换，非字符类型长度的变化等；
	 * 这里提供一个修改的方法：
	 * 根据要修改的字段建新表，copy原表数据到新表，删除原表，更名新表为原表名；
	 * 注意：在进行写入表数据过程中，不能调用此方法修改字段信息；
	 *      比如DataCopy的写入功能；需要跟据值长度，来修改表的字段长度；
	 * 
	 * @param conn 数据库连接
	 * @param tablename 表名
	 * @param col 列名
	 * @param new_col 新的列名
	 * @param coltype 列的类型
	 * @param len 长度
	 * @param dec 标度
	 * @param defaultvalue 默认值
	 * @param unique 列是否唯一
	 * @param nullable 列是否可为空
	 * @throws SQLException 修改表结构出现错误时抛出异常
	 */
	public void modifyColumn(Connection conn, String tablename, String col, String new_col, char coltype, int len,
			int dec, String defaultvalue, boolean unique, boolean nullable) throws SQLException {
		/**
		 * 这里不用系统变量dl，是因为此方法给了conn, 通过系统变量dl获取表结构时，会造成嵌套获取连接；
		 */
		Dialect dl = SqlFunc.createDialect(conn);
		/**
		 * 字段可能不是只有长度发生变化，DB2字段修改通过建新表实现；
		 * 下面注释的代码是多余的；
		 */

		//先根据新类型创建表，在copy数据；
		//但是数据目前只支持如下数据的转换，其他转换不copy数据；
		//int -> number,str
		//number -> int,str
		//str -> int,num 如果不是数字值，报异常
		//date -> str,timestamp
		//timestamp -> date
		TableMetaData tmd = dl.createDbMetaData(conn).getTableMetaData(tablename);
		String newtbname = createTable(conn, dl, tmd, col, new_col, coltype, len, dec, defaultvalue, unique, nullable,
				false);
		String copysql = getCopyeSql(conn, dl, tmd, newtbname, col, new_col, coltype, false);
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(copysql);
			conn.commit();
		} finally {
			stmt.close();
		}
		dropTable(conn, null, tablename);
		renameTable(conn, newtbname, tablename);
	}

	/**
	 * 为修改字段和删除字段后，创建的新表 copy数据；
	 * @param conn 数据库连接
	 * @param dl 方言
	 * @param tmd 表元信息
	 * @param newtbname 新表名
	 * @param col 字段名
	 * @param new_col 新的字段名
	 * @param coltype 数据类型
	 * @param isDel 
	 *        是否删除字段col的标识；
	 *        =true , 则 new_col,coltype参数无效；
	 * @return copy 数据的 sql语句
	 */
	private String getCopyeSql(Connection conn, Dialect dl, TableMetaData tmd, String newtbname, String col,
			String new_col, char coltype, boolean isDel) {
		StringBuffer sql = new StringBuffer(256);
		sql.append("insert into ").append(newtbname).append(" (");
		TableColumnMetaData[] cols = tmd.getColumns();
		for (int i = 0; i < cols.length; i++) {
			TableColumnMetaData colmd = cols[i];
			if (colmd.isAutoInc()) {
				continue;
			}
			String colname = colmd.getName();
			if (colname.equalsIgnoreCase(col)) {
				if (!isDel) {
					sql.append(getColumnName(new_col == null || new_col.length() == 0 ? colname : new_col)).append(',');
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
					sql.append(dl.funcToSqlVar(getColumnName(colname), colmd.getType(), getSqlType(coltype), null)).append(',');
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
	 * @param conn 数据库连接
	 * @param dl 方言
	 * @param tmd 表元信息
	 * @param col 列名
	 * @param new_col 新的列名
	 * @param coltype 数据类型
	 * @param len 长度
	 * @param dec 标度
	 * @param defaultvalue 默认值
	 * @param unique 是否唯一
	 * @param nullable 是否可为空
	 * @param isDel
	 *        是否删除字段col的标识；
	 *        =true , 则 new_col,coltype,len,dec,defualtvalue,unique,nullable参数无效；
	 * @return 新建的表名
	 * @throws Exception
	 */
	private String createTable(Connection conn, Dialect dl, TableMetaData tmd, String col, String new_col, char coltype,
			int len, int dec, String defaultvalue, boolean unique, boolean nullable, boolean isDel) throws SQLException {
		DbDefiner dbf = dl.createDbDefiner();
		dbf.clearDefineInfo();
		String desttable = dbf.getCreateTableName(conn, tmd.getTableName(), null);
		String incName = null;//自动增长字段名；
		String[] keys = tmd.getPrimaryKey();
		for (int i = 0; i < tmd.getColumnCount(); i++) {
			TableColumnMetaData colmd = tmd.getColumn(i);
			String colname = colmd.getName();
			boolean iskey = isKey(colname, keys);
			if (colname.equalsIgnoreCase(col)) {
				//如果是主键，不能为空；
				if (!isDel) {
					//定义修改字段；
					//如果是主键，则不能定义为 unique，否则建表出错；
					dbf.defineField(new_col == null || new_col.length() == 0 ? colname : new_col, coltype, len, dec,
							getDefineDefaultValue(defaultvalue, coltype), iskey ? false : nullable, iskey ? false : unique);
				}
			} else {
				if (colmd.isAutoInc()) {
					incName = colname;
					dbf.defineAutoIncField(colname, 1);
				} else {
					char tp = getFieldType(colmd.getType());
					int l = colmd.getLen();
					int cl = colmd.getScale();
					//不用修改的字段，使用原来的属性
					dbf.defineField(colname, tp, l, cl, getDefineDefaultValue(colmd.getDefaultValue(), tp), colmd.isNullable(),
							iskey ? false : colmd.isUnique(), colmd.getDesc());
				}
			}
		}
		//创建主键
		if (keys != null) {
			StringBuffer ks = new StringBuffer(10 * keys.length);
			for (int i = 0; i < keys.length; i++) {
				/**
				 * 主键中的字段可能改名，创建新表是用新的字段建主键；
				 * 否则建表失败，主键字段不是表的字段；
				 */
				String keyfield = keys[i];
				if (keyfield.equalsIgnoreCase(col)) {
					if (isDel)
						continue;//删除的字段是主键之一；
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
		//创建原表的索引
		TableIndexMetaData[] indexes = tmd.getIndexes();
		if (indexes != null) {
			for (int i = 0; i < indexes.length; i++) {
				TableIndexMetaData imd = indexes[i];
				String fds[] = imd.getColumns();
				if (fds.length == 1 && incName != null && fds[0].equalsIgnoreCase(incName)) {
					continue;//过滤自动增长字段的索引；
				}
				if (fds.length == 1 && fds[0].equalsIgnoreCase(col)) {
					//要修改的字段是unique，现在需要将其修改为!unique,这里的唯一索引就不要再建了；
					if (imd.isUnique() && !unique)
						continue;
				}
				StringBuffer fields = new StringBuffer(fds.length * 20);
				for (int j = 0; j < fds.length; j++) {
					if (fields.length() > 0)
						fields.append(",");
					if (fds[j].equalsIgnoreCase(col)) {
						if (isDel) {
							continue; //需要删除的字段，就不用再建索引了；
						}
						fields.append(new_col == null || new_col.length() == 0 ? col : new_col);
					} else
						fields.append(fds[j]);
				}
				if (fields.length() > 0) {//加了删除标识，对删除字段的单索引，就不用建了；
					fields.insert(0, '(');
					fields.append(")");
					dbf.defineIndex(imd.getName(), fields.toString(), imd.isUnique());
				}
			}
		}
		/**
		 * 修改字段自动调整字段长度，依据数据库限制
		 */
		dbf.createTable(conn, desttable, false, true);
		conn.commit();
		return desttable;
	}

	/**
	 * 建表时，给的默认值需要用'引起来；
	 * @param defaultValue 默认值
	 * @param coltype 数据类型
	 * @return
	 */
	private String getDefineDefaultValue(String defaultValue, char coltype) {
		if (defaultValue == null)
			return null;
		if (coltype != DbDefiner.FIELD_TYPE_STR) {
			return defaultValue;
		}
		if (defaultValue.length() == 0) {
			return "''";
		}
		if (defaultValue.length() >= 2 && defaultValue.startsWith("'") && defaultValue.endsWith("'")) {
			return defaultValue;
		}
		return "'" + defaultValue + "'";
	}

	private boolean isKey(String col, String[] keys) {
		if (keys == null) {
			return false;
		}
		for (int i = 0; i < keys.length; i++) {
			if (col.equalsIgnoreCase(keys[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void dropTempTable(Connection conn, String catalog, String table)
			throws SQLException {
		dropTable(conn, catalog, table);
	}

	/**
	 * 修改 Index 名
	 * 
	 * @param conn 数据库连接
	 * @param oldname 原有的索引名
	 * @param newname 新的索引名
	 * @throws Exception 修改索引名错误时抛出异常
	 */
	protected void renameIndex(Connection conn, String oldname, String newname)
			throws Exception {
		Statement ddl = conn.createStatement();
		try {
			ddl.executeUpdate("ALTER INDEX " + oldname + " RENAME TO "
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
				+ " BLOB "
				+ getTailDdl(null, fi.isNullable(), fi.isUnique(),
						fi.getDesc(), DbDefiner.FIELD_TYPE_BINARY, isUpdate);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean indexExists(Connection conn, String tbname, String indexname)
			throws SQLException {
		String dbuser = conn.getMetaData().getUserName();
		String sql = "select indexname from dbc.IndicesV "
				+ "where indexname='" + indexname.toUpperCase()
				+ "' and tablename='" + dbuser.toUpperCase() + "'";
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery(sql);
			try {
				return rs.next();
			} finally {
					rs.close();
			}
		} finally {
				stmt.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getIdFieldDdl(String thisField, int len, int step, String desc) {
		return thisField + " INT GENERATED ALWAYS AS IDENTITY NOT NULL";
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getClobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " CLOB "
				+ getTailDdl(null, fi.isNullable(), fi.isUnique(),
						fi.getDesc(), DbDefiner.FIELD_TYPE_CLOB, isUpdate);
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getIntFldDdl(TableColumnMetaData fi, boolean isUpdate) {
	    return getColumnName(fi.getName()) + " INTEGER " +
	    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_INT, isUpdate);
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
		/*
		 * ISSUE:BI-8611 add by liujin 2013.06.24
		 * NUMBER 类型较为特殊，改为使用 NUMERIC。
		 */
		numddl.append(" NUMERIC");
		
		// 如果长度为0，则不指定长度；
		if (fi.getLen() > 0) {
			int[] prcs = formatNumberPrecision(fi.getLen(), fi.getScale(), MaxPrecision);
			numddl.append('(').append(prcs[0]).append(',').append(prcs[1]).append(')');
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
		/*
		 * BUG:BI-8594: modify by liujin 2013.07.10
		 * 字符类型区分大小写。
		 */
		return getColumnName(fi.getName())
				+ " VARCHAR(" + (fi.getLen() > 0 ? fi.getLen() : 1) + ") CASESPECIFIC "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_STR,
						isUpdate);
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getTailDdl(String defaultvalue, boolean nullable, boolean unique, String desc, char t,
			boolean isUpdate) {
		if (isUpdate) {
			//增加字段
			StringBuffer str = new StringBuffer(32);
			if (unique) {
				str.append(" UNIQUE NOT NULL ");
				String def = getDefualtSql(defaultvalue, t);
				if (def != null) {
					str.append(def);
				}
				return str.toString();
			} else if (!nullable) {
				str.append(" NOT NULL ");
				String def = getDefualtSql(defaultvalue, t);
				if (def != null) {
					str.append(def);
				}
				return str.toString();
			} else if (defaultvalue != null && defaultvalue.length() > 0) {
				str.append(" DEFAULT ").append(defaultvalue);
			}

			return str.toString();
		} else {
			//建表
			StringBuffer str = new StringBuffer(16);
			if (defaultvalue != null && defaultvalue.length() > 0) {
				str.append(" DEFAULT ").append(defaultvalue);
			}
			if (unique) {
				/*
				 * 建表时，设置unique，必须为not null;
				 * 否则报：42831 主键或唯一键列不允许空值，异常；
				 */
				str.append(" UNIQUE NOT NULL");
			} else {
				if (!nullable) {
					str.append(" NOT NULL ");
				}
			}
			return str.toString();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String createIndex(Connection conn, String table, String indexName,
			String[] indexFields, boolean indexUnique,
			boolean ifIndexNameExistThrowException) throws SQLException {
		StringBuffer s = new StringBuffer(32);
		try {
			if (indexUnique) { // unique
				s.append("CREATE UNIQUE INDEX ");
			} else {
				s.append("CREATE INDEX ");
			}

			indexName = checkIndexName(table, indexName, ifIndexNameExistThrowException);
			if (_indexNames == null) {
				_indexNames = new ArrayList();
			}
			_indexNames.add(indexName);

			s.append(indexName);
			s.append(" (");
			for (int i = 0; i < indexFields.length; i++) {
				s.append(indexFields[i]);
				if (i < indexFields.length - 1) {
					s.append(',');
				}
			}
			s.append(")");
			s.append(" ON ").append(table);
			Statement ddl = conn.createStatement();
			try {
				ddl.executeUpdate(s.toString());
				conn.commit();
			} catch (SQLException e) {
				/* 
				 * 检查此错误是否是因为该列上的索引已存在，如果是，不报错
				 * 3534 Another index already exists, using the same columns and the same ordering.
				 */			
				if (e.getErrorCode() != 3534) {
					throw e;
				}
			} finally {
				ddl.close();
			}
			return indexName;
		} catch (PooledSQLException ex) {
			if (!ifIndexNameExistThrowException
					&& ex.getErrorCode2() == PooledSQLException.JDBC_EXISTING_OBJECT) {
				//通过异常机制处理索引名重名，此过程是线程同步的；
				return createIndex(conn, table,
						getRandomIndexName("I" + table), indexFields,
						indexUnique, false);
			}
			throw new PooledSQLException(I18N.getString("com.esen.jdbc.dialect.impl.teradata.teradatadef.1", "创建表索引:{0}\r\n{1}\r\n出现异常:",I18N.getDefaultLocale(),new Object[]{table,s}),ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dropColumn(Connection conn, String tablename, String col) throws SQLException {
		StringBuffer ddl = new StringBuffer(64).append("ALTER TABLE ");
		ddl.append(tablename);
		ddl.append(" DROP ").append(col);
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(ddl.toString());
			conn.commit();
		} catch (SQLException e) {
			/*
			 * 删除列出异常时，
			 * 如果是因为列上有约束不允许删除，采用变通的方法
			 */
			dropConstraintColumn(conn, tablename, col);
		} finally {
			stmt.close();
		}
	}
	    
	/**
	 * 在列上有约束时不能直接删除字段
	 * 现在使用下面方法实现：
	 * 创建新表，copy数据，删除原表，更名新表名为原表名；
	 */
	private void dropConstraintColumn(Connection conn, String tablename, String col) throws SQLException {
		TableMetaData tmd = dl.createDbMetaData(conn).getTableMetaData(tablename);
		String newtbname = createTable(conn, dl, tmd, col, null, 'C', -1, -1, null, false, true, true);
		String copysql = getCopyeSql(conn, dl, tmd, newtbname, col, null, 'C', true);
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(copysql);
			conn.commit();
		} finally {
			stmt.close();
		}
		dropTable(conn, null, tablename);
		renameTable(conn, newtbname, tablename);
	}

	/**
	 * {@inheritDoc}
	 */
	public void renameTable(Connection conn, String oldname, String newname) throws SQLException {
    Statement ddl = conn.createStatement();
    try {
      ddl.executeUpdate("RENAME TABLE " + oldname + " TO " + newname);
      conn.commit();
    }
    finally {
      ddl.close();
    }	
	}

	/**
	 * 为指定的表增加列
	 * 
	 * @param conn 数据库连接
	 * @param tablename 表名
	 * @param newcol 新增的列名
	 * @param coltype 新增列的数据类型
	 * @param len 新增列的长度
	 * @param dec 新增列的标度
	 * @param defaultvalue 新增列的默认值
	 * @param nullable 新增列是否可为空
	 * @param unique 新增列是否唯一
	 */
	public void addColumn(Connection conn, String tablename, String newcol, char coltype, int len, int dec,
			String defaultvalue, boolean nullable, boolean unique) throws SQLException {
		StringBuffer ddl = new StringBuffer(64).append("ALTER TABLE ");
		ddl.append(tablename);
		ddl.append(" ADD ");
		ddl.append(getFldDdl(
				new TableColumnMetaDataHelper(newcol, coltype, len, dec, unique? false : nullable, false, defaultvalue, null), true));

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
	 * 检查对象是否存在
	 * 
	 * 重写该方法是因为 DbDef 中的实现对于对象名的大小写没有准确处理
	 * 直接修改担心影响其他数据库，所以此处重写
	 */
	protected boolean objectExists(DatabaseMetaData _dmd, String schema, String objectname, String[] types)
			throws SQLException {
		ResultSet _rs = _dmd.getTables(null, schema, null, types);
		try {
			while (_rs.next()) {
				String vname = _rs.getString("TABLE_NAME");
				if (vname.equalsIgnoreCase(objectname)) {
					return true;
				}
			}
		} finally {
			_rs.close();
		}
		return false;
  }
	
	/**
	 * {@inheritDoc}
	 */
	public void dropTable(Connection conn, String catalog, String table)
			throws SQLException {
		if (catalog != null) {
			conn.setCatalog(catalog);
		}
		Statement ddl = conn.createStatement();
		/**
		 * BI-5061 对于某些特殊情况下创建的表，表名包含空格，这时删除该表，表名必需加引号；
		 */
		if (table.indexOf(" ") >= 0) {
			table = "\"" + table + "\"";
		}
		try {
			ddl.executeUpdate("DROP TABLE " + table);
			/*
			 * 一个事务中，不能执行多个DDL语句 所以执行完 Drop table 以后需要先提交
			 */
			conn.commit();
		} catch (SQLException se) {
			/**
			 * 多线程调用删除同一个表时，会出现删除的表已经不存在的情况，即使实现判断表是否存在也没用；
			 * 这里使用异常机制来处理，如果出新异常，则表示这个表已经被删除，直接返回，不抛出异常；
			 */
			return;
		} finally {
			ddl.close();
		}
	}

	protected String createTableForDefine(Connection conn, String tablename,boolean isTemp) throws SQLException {
		StringBuffer fldddl = new StringBuffer(256);
		for (int i = 0; i < tbMetaHelper.getColumnCount(); i++) {
			TableColumnMetaDataHelper fi = (TableColumnMetaDataHelper) tbMetaHelper.getColumn(i);
			if (fi.isAutoInc()) {
				fldddl.append(getIdFieldDdl(fi.getName(), fi.getLen(), fi.getStep(), fi.getDesc()));
				fldddl.append(",");
			} else {
				fldddl.append(getFldDdl(fi) + ",");
			}
		}
		String[] _pkeys = tbMetaHelper.getPrimaryKey();
		if (_pkeys != null && _pkeys.length > 0) {
			//create primary key
			fldddl.append("PRIMARY KEY (");
			fldddl.append(ArrayFunc.array2Str(_pkeys, ','));
			fldddl.append(')');
		}
		else {
			fldddl.deleteCharAt(fldddl.length() - 1);
		}
		
		String ddl = null;
		if (isTemp) {
			ddl = getTempTableCreateDdl(tablename, fldddl.toString());
		}
		else {
			ddl = getTableCreateDdl(tablename, fldddl.toString());
		}

		Statement stmt = conn.createStatement();
		try {
			stmt.executeUpdate(ddl);
			conn.commit();
		}
		catch (SQLException ex) {
//			throw new PooledSQLException("创建表:" + tablename + "\r\n\r\n" + ddl + "\r\n\r\n出现异常:" ,ex);
			Object[] param=new Object[]{tablename,ddl};
			throw new PooledSQLException(I18N.getString("com.esen.jdbc.dialect.impl.teradata.teradatadef.createtableex", "创建表:{0}\r\n\r\n{1}\r\n\r\n出现异常:", param) ,ex);
		}
		finally {
			stmt.close();
		}
		return tablename;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getTableCreateDdl(String tablename, String fldDdl) {
		/*
		 * 在默认情况下创建的表是不允许有相同的记录的，修改为允许有相同记录存在
		 */
		return "CREATE MULTISET TABLE " + tablename + "(" + fldDdl + ")";
	}
}
