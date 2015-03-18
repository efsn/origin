package com.esen.jdbc.dialect.impl.netezza;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;

public class NetezzaTableMetaData extends TableMetaDataImpl {

	public NetezzaTableMetaData(DbMetaDataImpl owner, String tablename) {
		super(owner, tablename);
	}

	protected void initColumns() throws Exception {
		Connection con = this.owner.getConnection();
	    try {
	      DatabaseMetaData md = con.getMetaData();
	      String[] tbs = DbDef.getTableNameForDefaultSchema(getTableName(),owner.getDataBaseInfo());
	      ResultSet rs = md.getColumns(null, tbs[0], tbs[1], null);
	      try {
	        while (rs.next()) {
	          String tbname = rs.getString("TABLE_NAME");
	          /**
	           * 20090218
	           * 可能会有重复的表，比如t_hy将会把tahy表（如果存在）的字段读进来；
	           */
	          if(!tbname.equalsIgnoreCase(tbs[1])){
	            continue;
	          }
	          String colname = rs.getString("COLUMN_NAME");
	          int tp = rs.getInt("DATA_TYPE");
	          int len = rs.getInt("COLUMN_SIZE");
	          int dec = rs.getInt("DECIMAL_DIGITS");
	          String isnullable = rs.getString("IS_NULLABLE");
	          boolean nullable = isnullable == null || !isnullable.trim().equals("NO");
	          String defvalue = rs.getString("COLUMN_DEF");
	          String desc = rs.getString("REMARKS");
	          NetezzaTableColumnMetaData column = new NetezzaTableColumnMetaData(this,colname);
	          column.setLable(colname);
	          column.setType(tp);
	          column.setLength(len);
	          column.setScale(dec);
	          column.setNullable(nullable);
	          
	          column.setDefaultValue(getDefaultValue(defvalue));
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
	

}
