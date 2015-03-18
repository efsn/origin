package com.esen.jdbc.dialect.impl.db2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableIndexMetaDataImpl;
import com.esen.util.ArrayFunc;

/**
 * AS400数据库构造表结构；
 * 读取索引，主键另外实现；
 * @author dw
 *
 */
public class DB2AS400TableMetaData extends DB2TableMetaData {
 
	private static final String[] AS400_FIELD_TYPES = {
		"VARCHAR", "CHAR", "SMALLINT","DECIMAL","DOUBLE", "REAL", "INTEGER",
		"VARG","TIMESTMP","NUMERIC","DATE","TIME","BINARY","BIGINT", 
		"GRAPHIC", "CLOB", "DBCLOB","BLOB","FLOAT","VARBIN"
	};
	
	private int getColumnType(String type) {
		if(type == null || type.length() == 0) {
			return -1;
		}
		int index = ArrayFunc.find(AS400_FIELD_TYPES,type);
		switch(index) {
			case 0: return java.sql.Types.VARCHAR;
			case 1: return java.sql.Types.CHAR;
			case 2: return java.sql.Types.SMALLINT;
			case 3: return java.sql.Types.DECIMAL;
			case 4: return java.sql.Types.DOUBLE;
			case 5: return java.sql.Types.REAL;
			case 6: return java.sql.Types.INTEGER;
			// "VARG","TIMESTMP","NUMERIC","DATE","TIME","BINARY","BIGINT", 
			case 7: return java.sql.Types.BLOB;
			case 8: return java.sql.Types.TIMESTAMP;
			case 9: return java.sql.Types.NUMERIC;
			case 10: return java.sql.Types.DATE;
			case 11: return java.sql.Types.TIME;
			case 12: return java.sql.Types.BINARY;
			case 13: return java.sql.Types.BIGINT;
			// "GRAPHIC", "CLOB", "DBCLOB","BLOB","FLOAT","VARBIN"
			case 14: return java.sql.Types.BLOB;
			case 15: return java.sql.Types.CLOB;
			case 16: return java.sql.Types.BLOB;
			case 17: return java.sql.Types.BLOB;
			case 18: return java.sql.Types.FLOAT;
			case 19: return java.sql.Types.VARBINARY;
			default: return -1;
		}
	}
	
	public DB2AS400TableMetaData(DbMetaDataImpl owner, String tablename) {
		super(owner, tablename);
	}

	protected void initColumns() throws Exception {
    Connection con = this.owner.getConnection();
    try {
      Statement sm = con.createStatement();
      try {
      	/*
      	 * BUG:IRPT-11257：2013/09/26 edit by xuxg
      	 * DB2不支持查询时有重复列的写法，当有多个表时候，分多次查询来获取拆分表的列元信息
      	 */
      	String[] tables = this.getTableName().split(",");
		for (int k = 0; k < tables.length; k++) {
			ResultSet rs = sm.executeQuery(SQL_COLUMN + tables[k] + " where 1>2");
	        try {
	          ResultSetMetaData meta = rs.getMetaData();
	          int count = meta.getColumnCount();
	          for (int i = 1; i <= count; i++) {
	            String colname = meta.getColumnName(i);
	            //使用统一的基类构造表结构；
	            DB2TableColumnMetaDataProvider column = new DB2TableColumnMetaDataProvider(this,colname);
	            column.setLable(colname);
	            column.setAutoInc(meta.isAutoIncrement(i));
	            column.setType(meta.getColumnType(i));
	            char tc = SqlFunc.getType(meta.getColumnType(i));
	            if (tc == 'C') {
	              column.setLength(meta.getColumnDisplaySize(i));
	            }
	            if (tc == 'N' || tc == 'I') {
	              column.setLength(meta.getPrecision(i));
	              if (tc == 'N') {
	                column.setScale(meta.getScale(i));
	              }
	            }
	            column.setNullable(meta.isNullable(i) == ResultSetMetaData.columnNullable);
	            addColumn(column);
	          }
	        }
	        finally {
	          rs.close();
	        }
	      }
      }
      finally {
        sm.close();
      }
      queryOtherFieldInfo(con);
    }
    finally {
      this.owner.closeConnection(con);
    }
  }
 
  
	private void queryOtherFieldInfo(Connection con) throws SQLException {
		String[] tbs = DbDef.getTableNameForDefaultSchema(tablename, owner.getDataBaseInfo());
		String sql = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, LENGTH, NUMERIC_SCALE, IS_NULLABLE, COLUMN_DEFAULT, IS_IDENTITY from QSYS2.SYSCOLUMNS "
				+ "where TABLE_NAME='" + tbs[1].toUpperCase() + "' and TABLE_SCHEMA='" + tbs[0] + "'";
		Statement stmt = con.createStatement();
		ResultSet rs = null;
		try {
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String fieldname = rs.getString("COLUMN_NAME");
				DB2TableColumnMetaDataProvider col = (DB2TableColumnMetaDataProvider)getColumn(fieldname);
				int len = rs.getInt("LENGTH");
				int scale = rs.getInt("NUMERIC_SCALE");
				String defaultValue = rs.getString("COLUMN_DEFAULT");
				String nulls = rs.getString("IS_NULLABLE");//N,Y
				String identity = rs.getString("IS_IDENTITY");//N,Y
				// String remarks = rs.getString("REMARKS");
				col.setType(getColumnType(rs.getString("DATA_TYPE")));
				col.setLen(len);
				col.setScale(scale);
				col.setDefaultValue(getDefaultValue(defaultValue));
				col.setNullable(nulls!=null&&nulls.equals("Y"));
				col.setAutoInc(identity!=null&&identity.equals("YES"));
				// col.setDesc(remarks);
			}
		}
		finally {
			if(rs!=null)
			  rs.close();
			stmt.close();
		}

	}
 
	protected void initPrimaryKey() {
		String sql = "select TABLE_NAME,INDEX_NAME,COLUMN_NAMES from QSYS2.SYSTABLEINDEXSTAT where TABLE_SCHEMA='"
				+ getSchemaName()
				+ "' AND TABLE_NAME='"
				+ getUpcaseTableName()
				+ "' AND INDEX_TYPE='PRIMARY KEY'";
		try {
			Connection con = this.owner.getConnection();
			try {
				Statement stat = con.createStatement();
				ResultSet rs = stat.executeQuery(sql);
				try {
					if (rs.next()) {
						String cols = rs.getString("COLUMN_NAMES");
						setPrimaryKey( cols.split(", "));
					}
				} finally {
					if (rs != null)
						rs.close();
					if (stat != null) {
						stat.close();
					}
				}
			} finally {
				this.owner.closeConnection(con);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	protected void initIndexes() throws Exception {
		Connection conn = owner.getConnection();
		String sql = "select TABLE_NAME,INDEX_NAME,INDEX_TYPE,COLUMN_NAMES from QSYS2.SYSTABLEINDEXSTAT where TABLE_SCHEMA='"
				+ getSchemaName()
				+ "' AND TABLE_NAME='"
				+ getUpcaseTableName()
				+ "'";
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = null;
			try {
				rs = stmt.executeQuery(sql);
				while (rs.next()) {
					String indexname = rs.getString("INDEX_NAME");
					String colnms = rs.getString("COLUMN_NAMES");
					String uniq = rs.getString("INDEX_TYPE");
					if (uniq != null) {
						uniq = uniq.trim();
					}
					TableIndexMetaDataImpl inmd = new TableIndexMetaDataImpl(
							indexname,colnms.split(", "),uniq != null
							&& (uniq.equals("UNIQUE") || uniq
									.equals("PRIMARY KEY")));
					addIndexMeta(inmd);
				}

			} finally {
				rs = null;
				stmt.close();
			}
		} finally {
			owner.closeConnection(conn);
		}
	}
}
