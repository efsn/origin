package com.esen.jdbc.data.impl;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Types;

import com.esen.io.MyByteArrayOutputStream;
import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.data.DataReader;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableIndexMetaDataImpl;
import com.esen.util.StmFunc;
import com.esen.util.StrFunc;

/**
 * 读取旧格式的*.db文件；
 * @author Administrator
 *
 */
public class DataReaderFromStm implements DataReader {
  private InputStream in ;
  private AbstractMetaData meta;
  private int count;
  private int totalcount;
  public DataReaderFromStm(InputStream in ){
    this.in = in;
  }

  public AbstractMetaData getMeta() throws Exception {
    if(meta==null)
     init();
    return meta;
  }

  private void init() throws Exception {
    String[] primaryKeys = readPrimaryKeys();
    TableColumnMetaData[] columns = readColumns();
    TableIndexMetaData[] indexes = readIndexes();
    meta = new TableMetaDataStm(primaryKeys,columns,indexes);
    count = readInt();
    totalcount = count;
  }

  private TableIndexMetaData[] readIndexes() throws Exception {
    int count = readInt();
    if (count > 0) {
      TableIndexMetaData[] indexes = new TableIndexMetaData[count];
      String name;
      for (int i = 0; i < count; i++) {
        name = readString();
        TableIndexMetaDataImpl index = new TableIndexMetaDataImpl(name,readString(),readInt() == 1);
        indexes[i] = index;
      }
      return indexes;
    }
    return null;
  }

  private TableColumnMetaData[] readColumns() throws Exception {
    int count = readInt();
    TableColumnMetaData[] columns = new TableColumnMetaData[count];
    TableColumnMetaDataImpl column;
    String name;
    for (int i = 0; i < count; i++) {
      name = readString();
      column = new TableColumnMetaDataImpl(name);
      column.setLength(readInt());
      column.setScale(readInt());
      column.setType(readInt());
      column.setAutoInc(readInt() == 1);
      column.setNullable(readInt() == 1);
      column.setUnique(readInt() == 1);
      columns[i] = column;
    }
    return columns;
  }

  private String[] readPrimaryKeys() throws Exception {
     String key = readString();
     String[] keys = null;
     if(key!=null&&key.length()>0){
       keys = key.split(",");
     }
     return keys;
  }
  private String readString() throws Exception {
    int len = readInt();
    if (len != 0) {
      return StmFunc.readFix(in, len);
    }
    return null;
  }
  private int readInt() throws Exception {
    return StmFunc.readInt(in);
  }
  public Object getValue(int i) throws Exception {
    if(meta==null)
      init();
    if(count<0) return null;
    switch(meta.getColumnType(i)){
      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.BIGINT:
      case Types.INTEGER:
        return readIntValue();
      case Types.FLOAT:
        return readFloatValue();
      case Types.DECIMAL:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.REAL:
        return readDoubleValue();
      case Types.DATE:
        return readDateValue();
      case Types.TIME:
        return readTimeValue();
      case Types.TIMESTAMP:
        return readTimestampValue();
      case Types.BLOB:
      case Types.LONGVARBINARY:
      case Types.VARBINARY:
      case Types.BINARY:
        return readBlobValue();
      case Types.CLOB:
      case Types.LONGVARCHAR:
        return readClobValue();
      default:
        return readDefaultValue();
    }
  }

  private Object readDefaultValue() throws Exception {
    return readString();
  }

  private Reader readClobValue() throws Exception {
    int len = readInt();
    if(len>0){
      MyByteArrayOutputStream out = new MyByteArrayOutputStream(len);
      StmFunc.stmCopyFrom(in, out, len);
      CharArrayWriter w = new CharArrayWriter();
      InputStreamReader r = new InputStreamReader(out.asInputStream());
      len = StmFunc.reader2writer(r, w);
      return new CharArrayReader(w.toCharArray());
    }
    return null;
  }

  private InputStream readBlobValue() throws Exception {
    int len = readInt();
    if(len>0){
      MyByteArrayOutputStream out = new MyByteArrayOutputStream(len);
      StmFunc.stmCopyFrom(in, out, len);
      return out.asInputStream();
    }
    return null;
  }

  private java.sql.Timestamp readTimestampValue() throws Exception {
    String s = readString();
    if(s!=null){
      return new java.sql.Timestamp(StrFunc.str2long(s, 0));
    }
    return null;
  }

  private java.sql.Time readTimeValue() throws Exception {
    String s = readString();
    if(s!=null){
      return new java.sql.Time(StrFunc.str2long(s, 0));
    }
    return null;
  }

  private java.sql.Date readDateValue() throws Exception {
    String s = readString();
    if(s!=null){
      return new java.sql.Date(StrFunc.str2long(s, 0));
    }
    return null;
  }

  private Object readDoubleValue() throws Exception {
    String s = readString();
    /*if(s!=null){
      return new Double(StrFunc.str2double(s, 0));
    }*/
    return s;
  }

  private Float readFloatValue() throws Exception {
    String s = readString();
    if(s!=null){
      return new Float(StrFunc.str2float(s, 0));
    }
    return null;
  }

  private Long readIntValue() throws Exception {
    String v = readString();
    if(v!=null)
      return new Long(StrFunc.str2long(v, 0));
    return null;
  }

  public boolean next() throws Exception {
    if(meta==null)
      init();
    return count-->0;
  }

	public int getRecordCount() throws Exception {
	  if(meta==null)
      init();
		return totalcount;
	}

  public void close() throws Exception {

  }
}

