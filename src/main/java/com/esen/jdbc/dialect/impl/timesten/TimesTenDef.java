package com.esen.jdbc.dialect.impl.timesten;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.util.i18n.I18N;

public class TimesTenDef extends DbDef {

  public TimesTenDef(Dialect dl) {
    super(dl);
  }
  public boolean indexExists(Connection conn, String tbname, String indexname)
      throws SQLException {
    String dbuser = conn.getMetaData().getUserName();

    String sql = "select IXNAME from indexes " + "where IXNAME='"
        + indexname.toUpperCase() + "' and IXOWNER='"
        + dbuser.toUpperCase() + "'";
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
  protected boolean tableExists(Connection conn, String catalog, String table,
      boolean temp) throws Exception {
    String dbuser = conn.getMetaData().getUserName();

    String sql = "select TBLNAME from tables " + "where TBLNAME='"
        + table.toUpperCase() + "' and TBLOWNER='"
        + dbuser.toUpperCase() + "'";
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
  protected String getNumericFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    StringBuffer numddl = new StringBuffer();
    numddl.append(getColumnName(fi.getName()));
    numddl.append(" NUMBER");
    //如果长度为0，则不指定长度；
    if(fi.getLen()>0){
      numddl.append('(').append(fi.getLen()).append(',').append(fi.getScale()<0?0:fi.getScale()).append(')');
    }
    numddl.append(' ');
    numddl.append(getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_FLOAT, isUpdate));
    return numddl.toString();

  }
  protected String getTimeFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " TIME " + getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_TIME, isUpdate);
  }
  protected String getBlobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
//    throw new RuntimeException("TimesTen 不支持Blob类型;");
	  throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.timesten.timestendef.unsupportblob", "TimesTen 不支持Blob类型;"));
  }

  protected String getClobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
//    throw new RuntimeException("TimesTen 不支持Clob类型;");
	  throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.timesten.timestendef.unsupportclob", "TimesTen 不支持Clob类型;"));
  }

  protected String getIdFieldDdl(String thisField, int len, int step, String desc) {
//    throw new RuntimeException("TimesTen 不支持自动增长字段;");
	  throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.timesten.timestendef.unsupportselfup", "TimesTen 不支持自动增长字段;"));
  }

  protected String getMemoFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " VARCHAR(4000) " + getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_STR, isUpdate);
  }

  public void renameTable(Connection conn, String oldname, String newname)
      throws SQLException {
//    throw new RuntimeException("TimesTen 不支持修改表名;");
	  throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.timesten.timestendef.unsupportcht", "TimesTen 不支持修改表名;"));
  }
  
  public void modifyColumn(Connection conn, String tablename, String fieldname, char coltype, int len, int scale)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }
  public void modifyColumn(Connection conn, String tablename, String col, String newCol, char coltype, int len,
      int dec, String defaultvalue, boolean unique, boolean nullable) throws SQLException {
    // TODO Auto-generated method stub
    
  }

}
