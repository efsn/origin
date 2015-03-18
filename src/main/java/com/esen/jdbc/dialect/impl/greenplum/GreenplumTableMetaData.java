package com.esen.jdbc.dialect.impl.greenplum;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;

import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;

public class GreenplumTableMetaData extends TableMetaDataImpl {

	public GreenplumTableMetaData(DbMetaDataImpl owner, String tablename) {
	    super(owner, tablename);
    }
	protected void initColumns() throws Exception {
	    Connection con = this.owner.getConnection();
	    try {
	      DatabaseMetaData md = con.getMetaData();
	      String[] tbs = DbDef.getTableNameForDefaultSchema(getTableName(),owner.getDataBaseInfo());
	      ResultSet rs = md.getColumns(null, tbs[0], tbs[1].toLowerCase(), null);
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
	          if(rs.getString("TYPE_NAME").equals("text")){
	        	  tp = Types.LONGVARCHAR;
	          }
	          int len = rs.getInt("COLUMN_SIZE");
	          /*
	           * ISSUE:BI-7847 导入数据库表，字段长度异常
	           * numberic类型，没有指定长度时，会返回这样的数字。处理成0
	           */
	          if(len >= 131089){
	        	  len = 0;
	          }
	          int dec = rs.getInt("DECIMAL_DIGITS");
	          String isnullable = rs.getString("IS_NULLABLE");
	          boolean nullable = isnullable == null || !isnullable.trim().equals("NO");
	          String defvalue = rs.getString("COLUMN_DEF");
	          String desc = rs.getString("REMARKS");
	          GreenplumTableColumnMetaData column = new GreenplumTableColumnMetaData(this,colname);
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
//	protected String getDefaultValue(String defvalue) {
//		//读出NULL表示没有默认值；
//		if("null".equalsIgnoreCase(defvalue)){
//			return null;
//		}
//		if (defvalue != null) {
//			/**
//			 * 这里读出的默认值，如果是字符串类型默认值，默认值都用''扩起来，读取时将''去掉；
//			 */
//			defvalue = defvalue.trim();
//			//如果是空串，会出异常；
//			if (defvalue.length() > 0 && defvalue.charAt(0) == '\'')
//				defvalue = defvalue.substring(1, defvalue.lastIndexOf('\''));
//		}
//		return defvalue;
//	}
}
