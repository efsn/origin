package com.esen.jdbc.pool;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.util.i18n.I18N;

public class DefaultResultSetMetaData implements ResultSetMetaData {
	private ResultSetMetaData rsmd;

	private DataBaseInfo dbinfo;

	/**
	 * 20090625
	 * 存储字段的序号；
	 * 格式：(大写字段名，字段序号)
	 * 大写字段名做键值；
	 * 用于通过字段名访问其他属性，比如字段类型时，能够很快定位到字段的序号；
	 */
	private HashMap metaindex;

	public DefaultResultSetMetaData(ResultSetMetaData rsmd, DataBaseInfo dbinfo) {
		this.rsmd = rsmd;
		this.dbinfo = dbinfo;
	}

	public String getCatalogName(int column) throws SQLException {
		return rsmd.getCatalogName(column);
	}

	public String getColumnClassName(int column) throws SQLException {
		return rsmd.getColumnClassName(column);
	}

	public int getColumnCount() throws SQLException {
		return rsmd.getColumnCount();
	}

	public int getColumnDisplaySize(int column) throws SQLException {
		return rsmd.getColumnDisplaySize(column);
	}

	public String getColumnLabel(int column) throws SQLException {
		return rsmd.getColumnLabel(column);
	}

	public String getColumnName(int column) throws SQLException {
		return rsmd.getColumnName(column);
	}

	public int getColumnType(int column) throws SQLException {
		return rsmd.getColumnType(column);
	}

	public int getColumnType(String fieldname) throws SQLException {
		return getColumnType(getFieldIndex(fieldname));
	}

	/**
	 * 根据字段名获得该字段的序号；
	 * @param fieldname
	 * @return
	 * @throws SQLException
	 */
	private int getFieldIndex(String fieldname) throws SQLException {
		if (metaindex == null) {
			metaindex = new HashMap(rsmd.getColumnCount());
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				String coli = rsmd.getColumnName(i);
				if (!dbinfo.isFieldCaseSensitive()) {
					//数据库不区分字段大小写的，都转换成大写
					coli = coli.toUpperCase();
				}
				metaindex.put(coli, new Integer(i));
			}
		}
		if (!dbinfo.isFieldCaseSensitive()) {
			fieldname = fieldname.toUpperCase();
		}
		Integer o = (Integer) metaindex.get(fieldname);
		if (o == null) {
			//      throw new SQLException("不存在字段："+fieldname);
			throw new SQLException(I18N.getString("com.esen.jdbc.pool.defaultresultsetmetadata.nosuchfield",
					"不存在字段：{0}", new Object[] { fieldname }));
		}
		return o.intValue();
	}

	public String getColumnTypeName(int column) throws SQLException {
		return rsmd.getColumnTypeName(column);
	}

	public int getPrecision(int column) throws SQLException {
		return rsmd.getPrecision(column);
	}

	public int getScale(int column) throws SQLException {
		return rsmd.getScale(column);
	}

	public String getSchemaName(int column) throws SQLException {
		return rsmd.getSchemaName(column);
	}

	public String getTableName(int column) throws SQLException {
		return rsmd.getTableName(column);
	}

	public boolean isAutoIncrement(int column) throws SQLException {
		return rsmd.isAutoIncrement(column);
	}

	public boolean isCaseSensitive(int column) throws SQLException {
		return rsmd.isCaseSensitive(column);
	}

	public boolean isCurrency(int column) throws SQLException {
		return rsmd.isCurrency(column);
	}

	public boolean isDefinitelyWritable(int column) throws SQLException {
		return rsmd.isDefinitelyWritable(column);
	}

	public int isNullable(int column) throws SQLException {
		return rsmd.isNullable(column);
	}

	public boolean isReadOnly(int column) throws SQLException {
		return rsmd.isReadOnly(column);
	}

	public boolean isSearchable(int column) throws SQLException {
		return rsmd.isSearchable(column);
	}

	public boolean isSigned(int column) throws SQLException {
		return rsmd.isSigned(column);
	}

	public boolean isWritable(int column) throws SQLException {
		return rsmd.isWritable(column);
	}

}
