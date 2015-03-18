package com.esen.jdbc.dialect.impl.oscar;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.impl.DbDef;

public class OscarDef extends DbDef {


  public OscarDef(Dialect dl) {
    super(dl);
  }

  protected String getBlobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName())+" BLOB";
  }

  protected String getClobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName())+" CLOB";
  }

  protected String getIdFieldDdl(String thisField, int len, int step, String desc) {
	  if (len <= LEN_INT4) {
		  return thisField + " SERIAL";
	  } else {
		  return thisField + " SERIAL8";
	  }
  }
  protected String getNumericFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " NUMERIC(" + fi.getLen() + "," + fi.getScale() + ") "
        + getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_FLOAT, isUpdate);
  }
  protected String getMemoFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName())+" TEXT "+ getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_memo, isUpdate);
  }
  protected String getTimeFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " TIME " + getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_TIME, isUpdate);
  }
  public boolean indexExists(Connection conn, String tbname, String indexname)
      throws SQLException {
    String sql = "select relname from sys_class " + "where relkind='i' and relname='" + indexname+ "'";
    //System.out.println(sql);
    Statement stmt = conn.createStatement();
    ResultSet rs = null;
    try {
      rs = stmt.executeQuery(sql);
      return ((rs.next() ? true : false));
    }
    finally {
      rs = null;
      stmt.close();
    }
  }
  /**
   * Oscar数据库不支持更改字段属性，但可以更改字段名；
   * alter table testdb rename column userid to userid_ 
   */
  public void modifyColumn(Connection conn, String tablename, String col,String new_col,
      char coltype, int len, int dec, String defaultvalue,
      boolean unique ,boolean nullable) throws SQLException {
    if(col!=null&&!col.equalsIgnoreCase(new_col)){
      Statement ddl = conn.createStatement();
      try {
        ddl.executeUpdate("alter table " + tablename+" rename column "+col+" to "+new_col);
      }
      finally {
        ddl.close();
      }
    }
  }
  public void renameTable(Connection conn, String oldname, String newname)
      throws SQLException {
    Statement ddl = conn.createStatement();
    try {
      ddl.executeUpdate("alter table " + oldname+" rename to "+ newname);
    }
    finally {
      ddl.close();
    }

  }

  public void modifyColumn(Connection conn, String tablename, String fieldname, char coltype, int len, int scale)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }



}
