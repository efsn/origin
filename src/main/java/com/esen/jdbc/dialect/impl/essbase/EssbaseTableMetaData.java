package com.esen.jdbc.dialect.impl.essbase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataProvider;
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;

public class EssbaseTableMetaData extends TableMetaDataImpl {

	public EssbaseTableMetaData(DbMetaDataImpl owner, String tablename) {
		super(owner, tablename);
	}
	
	public TableIndexMetaData[] getIndexes() {
		return null;
	}
	public  String[] getPrimaryKey() {
		return null;
	}
	
	protected void initColumns() throws Exception {
	    Connection con = this.owner.getConnection();
	    try {
	      DatabaseMetaData md = con.getMetaData();
	      String table = getTableName();
	      ResultSet rs = md.getColumns(null, getSchemaName(), table, null);
	      try {
	        while (rs.next()) {
	          //String tbname = rs.getString("TABLE_NAME");
	          String colname = rs.getString("COLUMN_NAME");
	          int tp = rs.getInt("DATA_TYPE");
	          int len = rs.getInt("COLUMN_SIZE");
	          int dec = rs.getInt("DECIMAL_DIGITS");
	          String isnullable = rs.getString("IS_NULLABLE");
	          boolean nullable = isnullable == null || !isnullable.trim().equals("NO");
	          String defvalue = rs.getString("COLUMN_DEF");
	          String desc = rs.getString("REMARKS");
	          TableColumnMetaDataProvider column = new TableColumnMetaDataProvider(this,colname);
	          column.setLable(colname);
	          column.setType(tp);
	          column.setLength(len);
	          column.setScale(dec);
	          column.setNullable(nullable);
	         
	          column.setDefaultValue(defvalue);
	          column.setDesc(desc);
	          addColumn(column);
	        }
	      }
	      finally {
	        rs.close();
	      }
	    }
	    finally {
	      this.owner.closeConnection(con);
	    }

	  }
	
	/**
	 * select {} on columns,{[数据期].levels(0).members} on rows from BIAPP.XXB
	 */
	protected String getSampleSql(String field, int howToSample) {
		StringBuffer sql = new StringBuffer(64);
		sql.append("select {} on columns,{[").append(field).append("].levels(0).members} on rows from ").append(tablename);
		return sql.toString();
	}
}
