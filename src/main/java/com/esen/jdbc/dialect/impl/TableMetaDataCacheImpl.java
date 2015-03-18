package com.esen.jdbc.dialect.impl;

import java.sql.SQLException;
import java.util.HashMap;

import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.util.StrFunc;

/**
 * 表字段的描述实现；
 * 对字段的访问实现cache
 * 
 *	此类是线程安全的；
 * @author dw
 */
public class TableMetaDataCacheImpl implements TableMetaData {
	private DbMetaDataCacheImpl dbMetaCache;

	private String tablename;

	private TableMetaData tbmd;

	/**
	 * 存储指定字段对应在表中的值的cache
	 */
	private HashMap fieldValueMap;

	public TableMetaDataCacheImpl(DbMetaDataCacheImpl dbMetaCache, String tablename) {
		this.dbMetaCache = dbMetaCache;
		this.tablename = tablename;
	}

	private void initTableMetaData() {
		if (tbmd == null) {
			synchronized (this) {
				if (tbmd == null) {
					TableMetaData r = dbMetaCache.createDbMetaData().getTableMetaData(tablename);
					tbmd = r;
				}
			}
		}
	}

	public TableColumnMetaData getColumn(String colname) {
		initTableMetaData();
		synchronized (this) {
			return tbmd.getColumn(colname);
		}
	}

	public TableColumnMetaData getColumn(int i) {
		initTableMetaData();
		synchronized (this) {
			return tbmd.getColumn(i);
		}
	}

	public int getColumnCount() {
		initTableMetaData();
		synchronized (this) {
			return tbmd.getColumnCount();
		}
	}

	public TableColumnMetaData[] getColumns() {
		initTableMetaData();
		synchronized (this) {
			return tbmd.getColumns();
		}
	}

	public int getFieldSqlType(String field) {
		initTableMetaData();
		synchronized (this) {
			return tbmd.getFieldSqlType(field);
		}
	}

	public char getFieldType(String field) {
		initTableMetaData();
		synchronized (this) {
			return tbmd.getFieldType(field);
		}
	}

	public TableIndexMetaData[] getIndexes() {
		initTableMetaData();
		synchronized (this) {
			return tbmd.getIndexes();
		}
	}

	public String[] getPrimaryKey() {
		initTableMetaData();
		synchronized (this) {
			return tbmd.getPrimaryKey();
		}
	}

	public String getRealFieldName(String field) {
		initTableMetaData();
		synchronized (this) {
			return tbmd.getRealFieldName(field);
		}
	}

	public String getTableName() {
		return tablename;
	}

	public boolean haveField(String field) {
		initTableMetaData();
		synchronized (this) {
			return tbmd.haveField(field);
		}
	}

	public String getColumnDescription(int i) {
		initTableMetaData();
		synchronized (this) {
			return tbmd.getColumnDescription(i);
		}
	}

	public String getColumnLabel(int i) {
		initTableMetaData();
		synchronized (this) {
			return tbmd.getColumnLabel(i);
		}
	}

	public int getColumnLength(int i) {
		initTableMetaData();
		synchronized (this) {
			return tbmd.getColumnLength(i);
		}
	}

	public String getColumnName(int i) {
		initTableMetaData();
		synchronized (this) {
			return tbmd.getColumnName(i);
		}
	}

	public int getColumnScale(int i) {
		initTableMetaData();
		synchronized (this) {
			return tbmd.getColumnScale(i);
		}
	}

	public int getColumnType(int i) {
		initTableMetaData();
		synchronized (this) {
			return tbmd.getColumnType(i);
		}
	}

	public int isNullable(int i) {
		initTableMetaData();
		synchronized (this) {
			return tbmd.isNullable(i);
		}
	}

	public int isUnique(int i) {
		initTableMetaData();
		synchronized (this) {
			return tbmd.isUnique(i);
		}
	}

	public Object[] getFieldSample(String field, int howToSample) throws SQLException {
		if (StrFunc.isNull(field)) {
			return null;
		}
		if (fieldValueMap == null) {
			synchronized (this) {
				if (fieldValueMap == null) {
					fieldValueMap = new HashMap();
				}
			}
		}
		initTableMetaData();
		synchronized (this) {
			String key = field.toUpperCase() + String.valueOf(howToSample);
			Object[] vs = (Object[]) fieldValueMap.get(key);
			if (vs == null) {
				vs = tbmd.getFieldSample(field, howToSample);
				fieldValueMap.put(key, vs);
			}
			return vs;
		}

	}

}
