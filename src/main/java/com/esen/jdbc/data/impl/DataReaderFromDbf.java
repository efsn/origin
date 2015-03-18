package com.esen.jdbc.data.impl;

import java.io.File;
import java.sql.Types;

import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.data.DataReader;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataImpl;
import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.ExceptionHandler;
import com.esen.dbf.DBFReader;
import com.esen.dbf.field.DBFField;
import com.esen.util.i18n.I18N;
/**
 * 读取dbf文件；
 * @author Administrator
 *
 */
public class DataReaderFromDbf implements DataReader {
  private String filepath;

  private DBFReader dbf;

  private AbstractMetaData meta;

  public DataReaderFromDbf(String filepath) {
    this.filepath = filepath;
  }

  public AbstractMetaData getMeta() throws Exception {
    if (meta == null)
      init();
    return meta;
  }

  public int getRecordCount() throws Exception {
    return getReader().getDbfRecordSet().getCount();
  }

  public boolean next() throws Exception {
    return getReader().getDbfRecordSet().hasNext();
  }

  public Object getValue(int i) throws Exception {
    //需要测试判断两种方法的效率问题
    //如果仅仅读取某些字段可以使用,
    return getReader().getDbfRecordSet().getObject(i);
    //如果需要读取全部的字段，则最好使用
    //    return getReader().getRecord()[i];
  }

  public void close() throws Exception {
    getReader().close();
  }

  /**
   * 初始化结构信息
   */
  private void init() {
    String[] primaryKeys = null;//还未实现
    TableIndexMetaData[] indexes = null;//还未实现
    TableColumnMetaData[] columns = getDbfColumns();
    this.meta = new TableMetaDataStm(primaryKeys, columns, indexes);
  }

  /**
   * 初始化字段结构
   * @return
   */
  private TableColumnMetaData[] getDbfColumns() {
    DBFReader r = getReader();
    TableColumnMetaDataImpl[] columns = null;
    try {
      int count = r.getDbfFieldCount();
      columns = new TableColumnMetaDataImpl[count];
      for (int i = 0; i < count; i++) {
        DBFField field = r.getDbfField(i);
        TableColumnMetaDataImpl column = new TableColumnMetaDataImpl(field
            .getName());
        column.setLength(field.getLength());
        column.setScale(field.getDecimal());
        column.setType(getSqlType(field.getType()));
        columns[i] = column;
      }
    }
    catch (Exception e) {
      ExceptionHandler.rethrowRuntimeException(e);
    }
    return columns;
  }

  private DBFReader getReader() {
    if (this.dbf == null) {
      try {
        this.dbf = new DBFReader(new File(this.filepath));
        this.dbf.open();
      }
      catch (Exception e) {
        ExceptionHandler.rethrowRuntimeException(e);
      }
    }
    return this.dbf;
  }

  /**
   * 转换字段类型
   * @param dbftype
   * @return
   */
  private int getSqlType(char dbftype) {
    switch (dbftype) {
      case DBFField.FIELD_TYPE_DATE://date
        return Types.TIMESTAMP;
      case DBFField.FIELD_TYPE_CHAR://char
        return Types.VARCHAR;
      case DBFField.FIELD_TYPE_LOGICAL://logic
        return Types.BOOLEAN;
      case DBFField.FIELD_TYPE_DOUBLE://number
        return Types.NUMERIC;
      case DBFField.FIELD_TYPE_FLOAT://float
        return Types.FLOAT;
      case DBFField.FIELD_TYPE_MEMO://clob
        return Types.CLOB;
      case DBFField.FIELD_TYPE_INTEGER://integer
        return Types.INTEGER;
      case DBFField.FIELD_TYPE_GENERAL:
      case DBFField.FIELD_TYPE_PICTURE:
      case DBFField.FIELD_TYPE_DATETIME:
      case DBFField.FIELD_TYPE_CURRENCY:
      default:
//        throw new RuntimeException("不支持的数据类型");
    	  throw new RuntimeException(I18N.getString("com.esen.jdbc.data.impl.datareaderfromdbf.unsupportkind", "不支持的数据类型\""));
    }
  }
}
