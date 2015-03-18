package com.esen.jdbc.dialect.impl.dm;

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
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataHelper;
import com.esen.jdbc.dialect.impl.TableMetaDataHelper;
import com.esen.jdbc.dialect.impl.sql92.SQL92Def;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

public class DMDef extends SQL92Def {

	public DMDef(Dialect dl) {
		super(dl);
	}
	
	/**
   * 创建表时根据参数控制是否溶解索引，字符定义长度；
   */
	protected void checkKeysAndIndexesMaxLength(TableMetaDataHelper tmdh) {
    for(int i=0;i<tmdh.getColumnCount();i++){
      TableColumnMetaDataHelper fieldi = (TableColumnMetaDataHelper)tmdh.getColumn(i);
      adjustFieldLength(new TableColumnMetaDataHelper[]{fieldi},dbinf.getMaxVarcharFieldLength());
    }
    checkKeysAndIndexesMaxLength(tmdh,dbinf.getMaxKeyOfFieldsLength());
  }

	public void modifyColumn(Connection conn, String tablename, String fieldname, char coltype, int len, int scale)
			throws SQLException {
		StringBuffer ddl = new StringBuffer(32).append("ALTER TABLE ");
		ddl.append(tablename);
		ddl.append(" MODIFY ");
		ddl.append(getFieldDefine(coltype, fieldname, len, scale));
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(ddl.toString());
		}
		finally {
			stmt.close();
		}

	}
	
	protected String getFieldDefine(char coltype, String fieldname, int len, int scale) throws SQLException {
	    switch (coltype) {
	      case DbDefiner.FIELD_TYPE_STR:
	        return fieldname + " VARCHAR("+len+")";
	      case DbDefiner.FIELD_TYPE_INT:
			/*
			 * BUG:BI-9558: modify by liujin 2013.10.16 对于整型，如果长度超过 10，需要使用
			 * BIGINT，而不是 INTEGER
			 */
	      	if (len > 10) {
	      		return fieldname + " BIGINT";
	      	}
	        return fieldname + " INT";
	      case DbDefiner.FIELD_TYPE_FLOAT:
	        int[] prcs = formatNumberPrecision(len,scale,38);
	        if(prcs[0]==0){
	          return fieldname + " DOUBLE";
	        }
	        return fieldname + " DECIMAL("+prcs[0]+","+prcs[1]+")";
	      case DbDefiner.FIELD_TYPE_DATE:
	        return fieldname + " DATE ";
	      case DbDefiner.FIELD_TYPE_TIME:
	        return fieldname + " TIME ";
	      case DbDefiner.FIELD_TYPE_TIMESTAMP:
	        return fieldname + " TIMESTAMP ";
	      case DbDefiner.FIELD_TYPE_LOGIC:
	        return fieldname + " VARCHAR(1)";
	      case DbDefiner.FIELD_TYPE_memo:
	        return fieldname + " TEXT";
	      case DbDefiner.FIELD_TYPE_CLOB:
	        return fieldname + " CLOB";
	      case DbDefiner.FIELD_TYPE_BINARY:
	        return fieldname + " BLOB";
	      default:
	        throw new SQLException(
	            "database not support to define this type of field,type:" +coltype);
	    }
	  }
	
	/**
	 * ALTER TABLE T_TEST ADD add_str2_ VARCHAR(10)  UNIQUE  NOT NULL  DEFAULT 'aa'
	 * 定义default值，必须放在unique 和是否为空 之后； 
	 */
	protected String getTailDdl(String defaultvalue, boolean nullable, boolean unique, String desc, char t,
			boolean isUpdate) {
		StringBuffer str = new StringBuffer(16);
		if (unique)
			str.append(" UNIQUE ");
		if (!nullable)
			str.append(" NOT NULL ");
		if (defaultvalue != null && defaultvalue.length() > 0) {
			str.append(" DEFAULT ").append(defaultvalue);
		}
		return str.toString();
	}
	
	public void modifyColumn(Connection conn, String tablename, String col, String new_col, char coltype, int len,
			int dec, String defaultvalue, boolean unique, boolean nullable) throws SQLException {
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
			//修改字段名；
			renddl = new StringBuffer(32).append("ALTER TABLE ");
			renddl.append(tablename);
			renddl.append(" ALTER COLUMN ");
			renddl.append(col2).append(" RENAME TO ").append(new_col2);
			ddls.add(renddl.toString());
			col2 = new_col2;
		}
		Dialect dl = SqlFunc.createDialect(conn);
		TableMetaData tbmd = dl.createDbMetaData(conn).getTableMetaData(tablename);
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
		//修改属性：类型，长度
		if ((coltype == 'C' && len != tcol.getLen())
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

		//判断是否需要修改unique属性
		if (tcol.isUnique() != unique) {
			if (unique) {
				//增加unique属性
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" ADD UNIQUE(").append(col2).append(")");
				ddls.add(sql.toString());
			}
			else {
				//删除unique属性
				/**
				 * 20090220
				 * unique有可能是主键，这里主键不能被删除；
				 */
				TableMetaData tmd = dl.createDbMetaData().getTableMetaData(tablename);
				String[] keys = tmd.getPrimaryKey();
				if (keys != null && keys.length == 1 && keys[0].equalsIgnoreCase(col2)) {
					//判断是主键，不进行修改；
				}
				else {

					String[] indexname = getUniqueIndexName(tbmd, col, true);
					if (indexname != null) {
						for (int i = 0; i < indexname.length; i++) {
							String sql = "alter table " + tablename + " drop constraint " + indexname[i];
							ddls.add(sql);
						}
					}
				}
			}
		}
		/**
		 * 判断是否需要修改default值
		 * ''和null 是一致的，所以在都非空且值不一致，或者一个空且一个非空的情况下才需要修改默认值；
		 * 这里非空是指既不是''也不是null；
		 */
		boolean isNullDefaltValue = StrFunc.isNull(defaultvalue);
		boolean isNullSrcDefaultValue = StrFunc.isNull(tcol.getDefaultValue());
		if ((!isNullDefaltValue && !isNullSrcDefaultValue && !StrFunc.compareStr(defaultvalue, tcol.getDefaultValue()))
				|| (isNullDefaltValue && !isNullSrcDefaultValue) || (!isNullDefaltValue && isNullSrcDefaultValue)) {
			if (defaultvalue == null) {
				//删除原有default值
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" ALTER ").append(col2).append(" DROP DEFAULT ");
				ddls.add(sql.toString());
			}
			else {
				if (defaultvalue.length() == 0)
					defaultvalue = "''";
				//修改defualt值
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" ALTER ").append(col2).append(" SET DEFAULT ").append(defaultvalue);
				ddls.add(sql.toString());
			}
		}
		//判断是否需要修改是否允许空值
		if (tcol.isNullable() != nullable) {
			if (nullable) {
				//设置允许为空
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" MODIFY ").append(getFieldDefine(coltype, col2, len, dec));
				sql.append(" NULL");
				ddls.add(sql.toString());
				/**
				 * 设置为空或者不为空时，执行的sql：
				 * ALTER TABLE T_TEST MODIFY STR2_ VARCHAR(100) NULL
				 * 如果str2_有唯一属性，则会去除唯一，所以后面跟一个sql：
				 * ALTER TABLE T_TEST ADD UNIQUE(STR2_)
				 * 把唯一加上；
				 */
				if(unique){
					StringBuffer sql2 = new StringBuffer(32).append("ALTER TABLE ");
					sql2.append(tablename).append(" ADD UNIQUE(").append(col2).append(")");
					ddls.add(sql2.toString());
				}
			}
			else {
				//设置不允许空值
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" MODIFY ").append(getFieldDefine(coltype, col2, len, dec));
				sql.append(" NOT NULL");
				ddls.add(sql.toString());
				if(unique){
					StringBuffer sql2 = new StringBuffer(32).append("ALTER TABLE ");
					sql2.append(tablename).append(" ADD UNIQUE(").append(col2).append(")");
					ddls.add(sql2.toString());
				}
			}
		}
		Statement stmt = conn.createStatement();
		try {
			for (int i = 0; i < ddls.size(); i++) {
				stmt.execute((String) ddls.get(i));
			}
		}
		finally {
			stmt.close();
		}
	}
	
	
	public boolean indexExists(Connection conn, String tbname, String indexname) throws SQLException {
	    String sql = "select name from SYSDBA.SYSINDEXES " + "where name='" + indexname+"'";
	    Statement stmt = conn.createStatement();
	    try {
	      ResultSet rs = stmt.executeQuery(sql);
	      try {
	        return ((rs.next() ? true : false));
	      }
	      finally {
	        if (rs != null)
	          rs.close();
	      }
	    }
	    finally {
	      if (stmt != null)
	        stmt.close();
	    }

	  }
		
  	protected String getFieldCommentSql(String viewname, String colname, String comment) {
		if (StrFunc.isNull(viewname) || StrFunc.isNull(colname) || StrFunc.isNull(comment))
			return "";
  			
		StringBuffer sql = new StringBuffer(50);
	    sql.append("COMMENT ON COLUMN ");
	    sql.append(viewname);
	    sql.append(".");
	    sql.append(getColumnName(colname));
	    sql.append(" IS '");
	    if(comment != null)
	      sql.append(comment);
	    sql.append("\'");
	    return sql.toString();
	}
  	
	/**
	 * {@inheritDoc}
	 */
	protected String getIntFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		/*
		 * BUG:BI-9558: modify by liujin 2013.10.16
		 * 对于整型，如果长度超过 10，需要使用 BIGINT，而不是 INTEGER
		 */
		String colType = " INTEGER ";
		if (fi.getLen() > 10) { //BIGINT
			colType = " BIGINT ";
		}
		return getColumnName(fi.getName()) + colType
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_INT,
				isUpdate);
	}
}
