package com.esen.jdbc.dialect.impl.sql92;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbDef;

/**
 * 实现SQL92标准的DbDefiner接口；
 * 有待完善；
 * @author dw
 *
 */
public class SQL92Def extends DbDef {

  public SQL92Def(Dialect dl) {
    super(dl);
  }

  protected String getBlobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " BLOB " +getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_BINARY, isUpdate);
  }

  protected String getClobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " CLOB " +getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_BINARY, isUpdate);

  }

  protected String getIdFieldDdl(String thisField, int len, int step, String desc) {
    return  thisField + " int identity(1," + step +")";
  }

  protected String getMemoFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " TEXT " +getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_BINARY, isUpdate);

  }

  protected String getNumericFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    StringBuffer numddl = new StringBuffer();
    numddl.append(getColumnName(fi.getName()));
    //如果长度为0，则不指定长度；
    if(fi.getLen()>0){
      numddl.append(" DECIMAL");
      int[] prcs = formatNumberPrecision(fi.getLen(),fi.getScale(),38);
      numddl.append('(').append(prcs[0]).append(',').append(prcs[1]).append(')');
    }else{
      //无法指导精度的使用double类型；
      numddl.append(" DOUBLE");
    }
    numddl.append(' ');
    numddl.append(getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_FLOAT, isUpdate));
    return numddl.toString();
  }

  public boolean indexExists(Connection conn, String tablename, String indexname) throws SQLException {
    Dialect dl = SqlFunc.createDialect(conn);
    TableMetaData tmd = dl.createDbMetaData().getTableMetaData(tablename);
    if(tmd==null) return false;//表名不存在
    TableIndexMetaData[] inds = tmd.getIndexes();
    if(inds!=null){
    for(int i=0;i<inds.length;i++){
      if(inds[i].getName().equalsIgnoreCase(indexname))
        return true;
    }
    }
    return false;
  }

  public void modifyColumn(Connection conn, String tablename, String fieldname, char coltype, int len, int scale)
      throws SQLException {
    StringBuffer ddl = new StringBuffer(32).append("ALTER TABLE ");
    ddl.append(tablename);
    ddl.append(" ALTER ");
    ddl.append(getFieldDefine(coltype,fieldname,len,scale));
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
        return fieldname + " TYPE VARCHAR("+len+")";
      case DbDefiner.FIELD_TYPE_INT:
        return fieldname + " TYPE INT";
      case DbDefiner.FIELD_TYPE_FLOAT:
        int[] prcs = formatNumberPrecision(len,scale,38);
        if(prcs[0]==0){
          return fieldname + " TYPE DOUBLE";
        }
        return fieldname + " TYPE DECIMAL("+prcs[0]+","+prcs[1]+")";
      case DbDefiner.FIELD_TYPE_DATE:
        return fieldname + " TYPE DATE ";
      case DbDefiner.FIELD_TYPE_TIME:
        return fieldname + " TYPE TIME ";
      case DbDefiner.FIELD_TYPE_TIMESTAMP:
        return fieldname + " TYPE TIMESTAMP ";
      case DbDefiner.FIELD_TYPE_LOGIC:
        return fieldname + " TYPE VARCHAR(1)";
      case DbDefiner.FIELD_TYPE_memo:
        return fieldname + " TYPE TEXT";
      case DbDefiner.FIELD_TYPE_CLOB:
        return fieldname + " TYPE CLOB";
      case DbDefiner.FIELD_TYPE_BINARY:
        return fieldname + " TYPE BLOB";
      default:
        throw new SQLException(
            "database not support to define this type of field,type:" +coltype);
    }
  }

  public void renameTable(Connection conn, String oldname, String newname) throws SQLException {
    Statement ddl = conn.createStatement();
    try {
      ddl.executeUpdate("ALTER TABLE " + oldname+" RENAME TO "+ newname);
    }
    finally {
      ddl.close();
    }

  }

  public void modifyColumn(Connection conn, String tablename, String col, String new_col, char coltype, int len,
      int dec, String defaultvalue, boolean unique, boolean nullable) throws SQLException {
    
    
  }

}
