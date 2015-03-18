package com.esen.jdbc.dialect.impl.vertica;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;
import com.esen.util.StrFunc;

/**
 * Vertica 表元信息
 * 
 * @author liujin
 *
 */
public class VerticaTableMetaData extends TableMetaDataImpl {

	/**
	 * 构造方法
	 * @param owner 数据库结构对象
	 * @param tablename 表名
	 */
	public VerticaTableMetaData(DbMetaDataImpl owner, String tablename) {
		super(owner, tablename);
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void initColumns() throws Exception {
		super.initColumns();

		//unique, nullable 可能 不准确，再查约束信息
		
		//主键唯一
		String[] keys = getPrimaryKey();
		if (keys != null && keys.length == 1) {
			TableColumnMetaDataImpl col = (TableColumnMetaDataImpl) getColumn(keys[0]);
			if (col != null) {
				col.setUnique(true);
			}
		}
		
		/* 
		 * 查询约束信息
			•c — check is reserved, but not supported
			•f — foreign
			•n — not null
			•p — primary
			•u — unique
			•d — determines
		*/		
		String[] tbs = DbDef.getTableNameForDefaultSchema(getTableName(), owner.getDataBaseInfo());
		String sql = "select column_name, constraint_type from constraint_columns where table_name='" 
				+ tbs[1] + "'";
		if (!StrFunc.isNull(tbs[0])) {
			sql = sql + " and table_schema='" + tbs[0] + "'";
		}
		
	    try {
	        Connection conn = owner.getConnection();
	        try {
	          Statement stmt = conn.createStatement();
	          try {
	        	  ResultSet rs = stmt.executeQuery(sql);
	        	  try {
	        		  while(rs.next()){
	        			  String colname = rs.getString("COLUMN_NAME");
	        			  String con_type = rs.getString("CONSTRAINT_TYPE");
	        			  TableColumnMetaDataImpl col = (TableColumnMetaDataImpl)getColumn(colname);
	        				if (col != null) {
	        					if (StrFunc.compareStr(con_type, "u")) {
	        						col.setUnique(true);
	        					} else if (StrFunc.compareStr(con_type, "n")) {
	        						col.setNullable(false);
	        					}
	        				}
	        		  }
	        	  } finally {
	        		  rs.close();
	        	  }
	          } finally {
	        	  stmt.close();
	          }
	        } finally {
	        	owner.closeConnection(conn);
	        }
	      } catch (Exception e) {
	        ; //不抛出异常，有错误时不获取信息
	      }
	    
		// 查询列上的 comment 信息，系统表 commnets
	    // TODO
	}

}
