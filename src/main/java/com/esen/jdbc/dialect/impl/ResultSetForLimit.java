package com.esen.jdbc.dialect.impl;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * 为Dialect.queryLimit(...)提供返回值；
 * @author dw
 *
 */
public class ResultSetForLimit implements ResultSet {
  private ResultSet _rs;
  private int limit;
  private int start;
  /**
   * 记录遍历行数；
   */
  private int n; 
  
  /**
   * start<=0 and limit<=0 表示遍历所有记录
   * @param rs
   * @param start
   * @param limit
   * @throws SQLException
   */
  public ResultSetForLimit(ResultSet rs,int start, int limit) throws SQLException{
    this._rs = rs;
    this.start = start<0?0:start;
    this.limit = limit<0?0:limit;
    if (start > 0) {
      if (_rs.getType() == ResultSet.TYPE_FORWARD_ONLY) {
        int n = 0;
        while(n<start&&_rs.next()){
          n++;
        }
      }
      else {
        _rs.absolute(start);
      }
    }
    
  }

  public boolean absolute(int row) throws SQLException {
	  if(start>0){
		  return _rs.absolute(start+row);
	  }else return _rs.absolute(row);
  }

  public void afterLast() throws SQLException {
    n = limit;
    _rs.absolute(start+limit);

  }

  public void beforeFirst() throws SQLException {
    n = 0;
    _rs.absolute(start);
  }

  public void cancelRowUpdates() throws SQLException {
    _rs.cancelRowUpdates();
  }

  public void clearWarnings() throws SQLException {
    _rs.clearWarnings();

  }

  public void close() throws SQLException {
    _rs.close();

  }

  public void deleteRow() throws SQLException {
    _rs.deleteRow();

  }

  public int findColumn(String columnName) throws SQLException {
    return _rs.findColumn(columnName);
  }

  public boolean first() throws SQLException {
    n = 1;
    _rs.absolute(start);
    return _rs.next();
  }

  public Array getArray(int i) throws SQLException {
    return _rs.getArray(i);
  }

  public Array getArray(String colName) throws SQLException {
    return _rs.getArray(colName);
  }

  public InputStream getAsciiStream(int columnIndex) throws SQLException {
    return _rs.getAsciiStream(columnIndex);
  }

  public InputStream getAsciiStream(String columnName) throws SQLException {
    return _rs.getAsciiStream(columnName);
  }

  public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
    return _rs.getBigDecimal(columnIndex);
  }

  public BigDecimal getBigDecimal(String columnName) throws SQLException {
    return _rs.getBigDecimal(columnName);
  }
  /**
   * @deprecated
   */
  public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
    return _rs.getBigDecimal(columnIndex, scale);
  }
  /**
   * @deprecated
   */
  public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
    return _rs.getBigDecimal(columnName, scale);
  }

  public InputStream getBinaryStream(int columnIndex) throws SQLException {
    return _rs.getBinaryStream(columnIndex);
  }

  public InputStream getBinaryStream(String columnName) throws SQLException {
    return _rs.getBinaryStream(columnName);
  }

  public Blob getBlob(int i) throws SQLException {
    return _rs.getBlob(i);
  }

  public Blob getBlob(String colName) throws SQLException {
    return _rs.getBlob(colName);
  }

  public boolean getBoolean(int columnIndex) throws SQLException {
    return _rs.getBoolean(columnIndex);
  }

  public boolean getBoolean(String columnName) throws SQLException {
    return _rs.getBoolean(columnName);
  }

  public byte getByte(int columnIndex) throws SQLException {
    return _rs.getByte(columnIndex);
  }

  public byte getByte(String columnName) throws SQLException {
    return _rs.getByte(columnName);
  }

  public byte[] getBytes(int columnIndex) throws SQLException {
    return _rs.getBytes(columnIndex);
  }

  public byte[] getBytes(String columnName) throws SQLException {
    return _rs.getBytes(columnName);
  }

  public Reader getCharacterStream(int columnIndex) throws SQLException {
    return _rs.getCharacterStream(columnIndex);
  }

  public Reader getCharacterStream(String columnName) throws SQLException {
    return _rs.getCharacterStream(columnName);
  }

  public Clob getClob(int i) throws SQLException {
    return _rs.getClob(i);
  }

  public Clob getClob(String colName) throws SQLException {
    return _rs.getClob(colName);
  }

  public int getConcurrency() throws SQLException {
    return _rs.getConcurrency();
  }

  public String getCursorName() throws SQLException {
    return _rs.getCursorName();
  }

  public Date getDate(int columnIndex) throws SQLException {
    return _rs.getDate(columnIndex);
  }

  public Date getDate(String columnName) throws SQLException {
    return _rs.getDate(columnName);
  }

  public Date getDate(int columnIndex, Calendar cal) throws SQLException {
    return _rs.getDate(columnIndex, cal);
  }

  public Date getDate(String columnName, Calendar cal) throws SQLException {
    return _rs.getDate(columnName, cal);
  }

  public double getDouble(int columnIndex) throws SQLException {
    return _rs.getDouble(columnIndex);
  }

  public double getDouble(String columnName) throws SQLException {
    return _rs.getDouble(columnName);
  }

  public int getFetchDirection() throws SQLException {
    return _rs.getFetchDirection();
  }

  public int getFetchSize() throws SQLException {
    return _rs.getFetchSize();
  }

  public float getFloat(int columnIndex) throws SQLException {
    return _rs.getFloat(columnIndex);
  }

  public float getFloat(String columnName) throws SQLException {
    return _rs.getFloat(columnName);
  }

  public int getInt(int columnIndex) throws SQLException {
    return _rs.getInt(columnIndex);
  }

  public int getInt(String columnName) throws SQLException {
    return _rs.getInt(columnName);
  }

  public long getLong(int columnIndex) throws SQLException {
    return _rs.getLong(columnIndex);
  }

  public long getLong(String columnName) throws SQLException {
    return _rs.getLong(columnName);
  }

  public ResultSetMetaData getMetaData() throws SQLException {
    return _rs.getMetaData();
  }

  public Object getObject(int columnIndex) throws SQLException {
    return _rs.getObject(columnIndex);
  }

  public Object getObject(String columnName) throws SQLException {
    return _rs.getObject(columnName);
  }

  public Object getObject(int arg0, Map arg1) throws SQLException {
    return _rs.getObject(arg0, arg1);
  }

  public Object getObject(String arg0, Map arg1) throws SQLException {
    return _rs.getObject(arg0, arg1);
  }

  public Ref getRef(int i) throws SQLException {
    return _rs.getRef(i);
  }

  public Ref getRef(String colName) throws SQLException {
    return _rs.getRef(colName);
  }

  public int getRow() throws SQLException {
    return _rs.getRow();
  }

  public short getShort(int columnIndex) throws SQLException {
    return _rs.getShort(columnIndex);
  }

  public short getShort(String columnName) throws SQLException {
    return _rs.getShort(columnName);
  }

  public Statement getStatement() throws SQLException {
    return _rs.getStatement();
  }

  public String getString(int columnIndex) throws SQLException {
    return _rs.getString(columnIndex);
  }

  public String getString(String columnName) throws SQLException {
    return _rs.getString(columnName);
  }

  public Time getTime(int columnIndex) throws SQLException {
    return _rs.getTime(columnIndex);
  }

  public Time getTime(String columnName) throws SQLException {
    return _rs.getTime(columnName);
  }

  public Time getTime(int columnIndex, Calendar cal) throws SQLException {
    return _rs.getTime(columnIndex, cal);
  }

  public Time getTime(String columnName, Calendar cal) throws SQLException {
    return _rs.getTime(columnName, cal);
  }

  public Timestamp getTimestamp(int columnIndex) throws SQLException {
    return _rs.getTimestamp(columnIndex);
  }

  public Timestamp getTimestamp(String columnName) throws SQLException {
    return _rs.getTimestamp(columnName);
  }

  public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
    return _rs.getTimestamp(columnIndex, cal);
  }

  public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
    return _rs.getTimestamp(columnName, cal);
  }

  public int getType() throws SQLException {
    return _rs.getType();
  }

  public URL getURL(int columnIndex) throws SQLException {
    return _rs.getURL(columnIndex);
  }

  public URL getURL(String columnName) throws SQLException {
    return _rs.getURL(columnName);
  }
  /**
   * @deprecated
   */
  public InputStream getUnicodeStream(int columnIndex) throws SQLException {
    return _rs.getUnicodeStream(columnIndex);
  }
  /**
   * @deprecated
   */
  public InputStream getUnicodeStream(String columnName) throws SQLException {
    return _rs.getUnicodeStream(columnName);
  }

  public SQLWarning getWarnings() throws SQLException {
    return _rs.getWarnings();
  }

  public void insertRow() throws SQLException {
    _rs.insertRow();
  }

  public boolean isAfterLast() throws SQLException {
    return n==limit;
  }

  public boolean isBeforeFirst() throws SQLException {
    return n==0;
  }

  public boolean isFirst() throws SQLException {
    return n==1;
  }

  public boolean isLast() throws SQLException {
    return n==limit-1;
  }

  public boolean last() throws SQLException {
    n = limit-1;
    return _rs.absolute(start+limit-1);
  }

  public void moveToCurrentRow() throws SQLException {
    _rs.moveToCurrentRow();
  }

  public void moveToInsertRow() throws SQLException {
    _rs.moveToInsertRow();
  }

  public boolean next() throws SQLException {
  	//limit<=0 表示取所有数据
    if((n<limit||limit<=0)&&_rs.next()){
      n++;
      return true;
    }
    return false;
  }

  public boolean previous() throws SQLException {
    return _rs.previous();
  }

  public void refreshRow() throws SQLException {
    _rs.refreshRow();
  }

  public boolean relative(int rows) throws SQLException {
    return _rs.relative(rows);
  }

  public boolean rowDeleted() throws SQLException {
    return _rs.rowDeleted();
  }

  public boolean rowInserted() throws SQLException {
    return _rs.rowInserted();
  }

  public boolean rowUpdated() throws SQLException {
    return _rs.rowUpdated();
  }

  public void setFetchDirection(int direction) throws SQLException {
    _rs.setFetchDirection(direction);

  }

  public void setFetchSize(int rows) throws SQLException {
    _rs.setFetchSize(rows);

  }

  public void updateArray(int columnIndex, Array x) throws SQLException {

  }

  public void updateArray(String columnName, Array x) throws SQLException {

  }

  public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {

  }

  public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {

  }

  public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

  }

  public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {

  }

  public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {

  }

  public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {

  }

  public void updateBlob(int columnIndex, Blob x) throws SQLException {

  }

  public void updateBlob(String columnName, Blob x) throws SQLException {

  }

  public void updateBoolean(int columnIndex, boolean x) throws SQLException {

  }

  public void updateBoolean(String columnName, boolean x) throws SQLException {

  }

  public void updateByte(int columnIndex, byte x) throws SQLException {

  }

  public void updateByte(String columnName, byte x) throws SQLException {

  }

  public void updateBytes(int columnIndex, byte[] x) throws SQLException {

  }

  public void updateBytes(String columnName, byte[] x) throws SQLException {

  }

  public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {

  }

  public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {

  }

  public void updateClob(int columnIndex, Clob x) throws SQLException {

  }

  public void updateClob(String columnName, Clob x) throws SQLException {
  }

  public void updateDate(int columnIndex, Date x) throws SQLException {

  }

  public void updateDate(String columnName, Date x) throws SQLException {

  }

  public void updateDouble(int columnIndex, double x) throws SQLException {

  }

  public void updateDouble(String columnName, double x) throws SQLException {

  }

  public void updateFloat(int columnIndex, float x) throws SQLException {

  }

  public void updateFloat(String columnName, float x) throws SQLException {

  }

  public void updateInt(int columnIndex, int x) throws SQLException {

  }

  public void updateInt(String columnName, int x) throws SQLException {

  }

  public void updateLong(int columnIndex, long x) throws SQLException {

  }

  public void updateLong(String columnName, long x) throws SQLException {

  }

  public void updateNull(int columnIndex) throws SQLException {

  }

  public void updateNull(String columnName) throws SQLException {
  }

  public void updateObject(int columnIndex, Object x) throws SQLException {

  }

  public void updateObject(String columnName, Object x) throws SQLException {

  }

  public void updateObject(int columnIndex, Object x, int scale) throws SQLException {

  }

  public void updateObject(String columnName, Object x, int scale) throws SQLException {

  }

  public void updateRef(int columnIndex, Ref x) throws SQLException {

  }

  public void updateRef(String columnName, Ref x) throws SQLException {

  }

  public void updateRow() throws SQLException {

  }

  public void updateShort(int columnIndex, short x) throws SQLException {

  }

  public void updateShort(String columnName, short x) throws SQLException {

  }

  public void updateString(int columnIndex, String x) throws SQLException {

  }

  public void updateString(String columnName, String x) throws SQLException {

  }

  public void updateTime(int columnIndex, Time x) throws SQLException {

  }

  public void updateTime(String columnName, Time x) throws SQLException {
  }

  public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {

  }

  public void updateTimestamp(String columnName, Timestamp x) throws SQLException {

  }

  public boolean wasNull() throws SQLException {
    return _rs.wasNull();
  }

}
