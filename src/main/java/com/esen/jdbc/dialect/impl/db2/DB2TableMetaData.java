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
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;

/**
 * db2的表结构实现类；
 * 获取表字段，索引单独实现；
 * @author dw
 *
 */
public class DB2TableMetaData extends TableMetaDataImpl  {

  public DB2TableMetaData(DbMetaDataImpl owner, String tablename) {
    super(owner, tablename);
  }

  //db2的系统表里面，表名，字段名等，都是大写；
  protected String getUpcaseTableName() {
    return getTableName().toUpperCase();
  }

  /**
   * 20091113
   * 原来的程序通过meta.getColumns()获取字段结构，广州财政局项目，执行这个操作非常缓慢；
   * 现在改为亚查询：select * from tbname where 1>2 来获取表结构；
   * 20091117
   * 金利来客户项目也发现这个问题，使用db2数据库，只有BI访问，但是BI启动过段时间（一天）反应很慢；
   * 这种方法不能获取字段默认值和字段描述.
   * 
   * 20110913
   * 通过读取系统表，获取表字段的信息；
   * select TABSCHEMA,TABNAME,COLNAME,TYPENAME,LENGTH,SCALE,DEFAULT,NULLS,IDENTITY,REMARKS from SYSCAT.COLUMNS
   */
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
		String sql = "select TABSCHEMA,TABNAME,COLNAME,TYPENAME,LENGTH,SCALE,DEFAULT,NULLS,IDENTITY,REMARKS from SYSCAT.COLUMNS "
				+ "where TABNAME='" + tbs[1].toUpperCase() + "' and TABSCHEMA='" + tbs[0] + "'";
		Statement stmt = con.createStatement();
		ResultSet rs = null;
		try {
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String fieldname = rs.getString("COLNAME");
				DB2TableColumnMetaDataProvider col = (DB2TableColumnMetaDataProvider)getColumn(fieldname);
				int len = rs.getInt("LENGTH");
				int scale = rs.getInt("SCALE");
				String defaultValue = rs.getString("DEFAULT");
				String nulls = rs.getString("NULLS");//N,Y
				String identity = rs.getString("IDENTITY");//N,Y
				String remarks = rs.getString("REMARKS");
				col.setLen(len);
				col.setScale(scale);
				col.setDefaultValue(getDefaultValue(defaultValue));
				col.setNullable(nulls!=null&&nulls.equals("Y"));
				col.setAutoInc(identity!=null&&identity.equals("Y"));
				col.setDesc(remarks);
			}
		}
		finally {
			if(rs!=null)
			  rs.close();
			stmt.close();
		}

	}

protected void initIndexes() throws Exception{
    Connection conn = owner.getConnection();
    String[] tbs = DbDef.getTableNameForDefaultSchema(tablename,owner.getDataBaseInfo());
    String sql = "select INDNAME,COLNAMES,UNIQUERULE,COLCOUNT from SYSCAT.INDEXES " +
        "where TABNAME='"+tbs[1].toUpperCase()+"' and TABSCHEMA='"+tbs[0]+"'";
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sql);
        while (rs.next()) {
          String indexname = rs.getString(1);
          String colnms = rs.getString(2);//+id_+str_
          if(colnms.length()>0)
            colnms = colnms.substring(1);
          String uniq = rs.getString(3);
          TableIndexMetaDataImpl inmd = new TableIndexMetaDataImpl(indexname,colnms.split("\\+")
              ,uniq.equals("U") || uniq.equals("P"));
          this.addIndexMeta(inmd);
        }
      }
      finally {
        rs = null;
        stmt.close();
      }
    }
    finally {
      owner.closeConnection(conn);
    }
  }
}
