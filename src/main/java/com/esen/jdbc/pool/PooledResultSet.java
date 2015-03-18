package com.esen.jdbc.pool;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
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

import com.esen.jdbc.dialect.DataBaseInfo;



/**
 * <p>Title: Esensoft JDBC</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Esensoft</p>
 * @author dw
 * @version 1.0
 */

public class PooledResultSet
    implements ResultSet {
  protected ResultSet _rs;
  protected PooledConnection _pconn;
  protected DefaultResultSetMetaData rsmeta;
  
  public PooledResultSet(ResultSet rs, PooledConnection pconn) throws SQLException{
    _rs = rs;
    _pconn = pconn;
    /**
     * 20110905
     * 结果集每次从数据库中读取的行数，jdbc默认值是0, 此参数设置合适的值决定了读取效率；
     * 经测试，设置30-50是比较合理的值，Oracle的驱动默认是10。
     * 
     * 设置后装载维表数据的时间缩短了一半；
     * 载入维【SBQK_税务机关】82979条数据花费时间：4262
     * 设置前需要8秒多；
     */
    _rs.setFetchSize(50);
    
  }
  /**
   * 返回驱动接口实现
   * @return
   */
  public ResultSet getResultSet(){
    return _rs;
  }


  public boolean next() throws SQLException {
    return _rs.next();
  }

  public void close() throws SQLException {
    _rs.close();
  }

  public boolean wasNull() throws SQLException {
    return _rs.wasNull();
  }
  /**
   * 读取指定字段的字符串值；
   * 对于clob字段，可以直接读取；
   * Oracle9i及以上版本都支持，其他数据库也测试通过；
   * @param columnIndex int
   * @throws SQLException
   * @return String
   */
  public String getString(int columnIndex) throws SQLException {
    return _pconn.getGBKEncodingStr(_rs.getString(columnIndex));
  }

  public boolean getBoolean(int columnIndex) throws SQLException {
    return _rs.getBoolean(columnIndex);
  }

  public byte getByte(int columnIndex) throws SQLException {
    return _rs.getByte(columnIndex);
  }

  public short getShort(int columnIndex) throws SQLException {
    return _rs.getShort(columnIndex);
  }

  public int getInt(int columnIndex) throws SQLException {
    return _rs.getInt(columnIndex);
  }

  public long getLong(int columnIndex) throws SQLException {
    return _rs.getLong(columnIndex);
  }

  public float getFloat(int columnIndex) throws SQLException {
    return _rs.getFloat(columnIndex);
  }

  public double getDouble(int columnIndex) throws SQLException {
    return _rs.getDouble(columnIndex);
  }
  /**
   * @deprecated
   */
  public BigDecimal getBigDecimal(int columnIndex, int scale) throws
      SQLException {
    return _rs.getBigDecimal(columnIndex, scale);
  }

  public byte[] getBytes(int columnIndex) throws SQLException {
    return _rs.getBytes(columnIndex);
  }

  public Date getDate(int columnIndex) throws SQLException {
    return _rs.getDate(columnIndex);
  }

  public Time getTime(int columnIndex) throws SQLException {
    return _rs.getTime(columnIndex);
  }

  public Timestamp getTimestamp(int columnIndex) throws SQLException {
    return _rs.getTimestamp(columnIndex);
  }

  public InputStream getAsciiStream(int columnIndex) throws SQLException {
    return _rs.getAsciiStream(columnIndex);
  }
  /**
   * @deprecated
   */
  public InputStream getUnicodeStream(int columnIndex) throws SQLException {
    return _rs.getUnicodeStream(columnIndex);
  }

  public InputStream getBinaryStream(int columnIndex) throws SQLException {
    return _rs.getBinaryStream(columnIndex);
  }

  /**
   * 同getSring(int columnIndex)
   */
  public String getString(String columnName) throws SQLException {
    return _pconn.getGBKEncodingStr(_rs.getString(columnName));
  }
  
  protected int getColumnType(String columnName) throws SQLException{
    getMetaData();
    return rsmeta.getColumnType(columnName);
  }

  protected int getSQLType(int index)throws SQLException {
    return getMetaData().getColumnType(index);
  }
  public boolean getBoolean(String columnName) throws SQLException {
    return _rs.getBoolean(columnName);
  }

  public byte getByte(String columnName) throws SQLException {
    return _rs.getByte(columnName);
  }

  public short getShort(String columnName) throws SQLException {
    return _rs.getShort(columnName);
  }

  public int getInt(String columnName) throws SQLException {
    return _rs.getInt(columnName);
  }

  public long getLong(String columnName) throws SQLException {
    return _rs.getLong(columnName);
  }

  public float getFloat(String columnName) throws SQLException {
    return _rs.getFloat(columnName);
  }

  public double getDouble(String columnName) throws SQLException {
    return _rs.getDouble(columnName);
  }
  /**
   * @deprecated
   */
  public BigDecimal getBigDecimal(String columnName, int scale) throws
      SQLException {
    return _rs.getBigDecimal(columnName, scale);
  }

  public byte[] getBytes(String columnName) throws SQLException {
    return _rs.getBytes(columnName);
  }

  public Date getDate(String columnName) throws SQLException {
    return _rs.getDate(columnName);
  }

  public Time getTime(String columnName) throws SQLException {
    return _rs.getTime(columnName);
  }

  public Timestamp getTimestamp(String columnName) throws SQLException {
    return _rs.getTimestamp(columnName);
  }

  public InputStream getAsciiStream(String columnName) throws SQLException {
    return _rs.getAsciiStream(columnName);
  }

  public InputStream getUnicodeStream(String columnName) throws SQLException {
    /**@todo Implement this java.sql.ResultSet method*/
    throw new java.lang.UnsupportedOperationException(
        "Method getUnicodeStream() not yet implemented.");
  }
 
  public InputStream getBinaryStream(String columnName) throws SQLException {
    return _rs.getBinaryStream(columnName);
  }

  public SQLWarning getWarnings() throws SQLException {
    return _rs.getWarnings();
  }

  public void clearWarnings() throws SQLException {
    _rs.clearWarnings();
  }

  public String getCursorName() throws SQLException {
    return _rs.getCursorName();
  }

  public ResultSetMetaData getMetaData() throws SQLException {
    if(rsmeta==null)
      rsmeta = new DefaultResultSetMetaData(_rs.getMetaData(),_pconn.getDbType());
    return rsmeta;
  }
  /**
   * 如果是clob字段，则返回字符串
   */
  public Object getObject(int columnIndex) throws SQLException {
    switch(getSQLType(columnIndex)){
      case java.sql.Types.CLOB ://DB2,Mssql,Oracle
      case java.sql.Types.LONGVARCHAR: //Mysql,Sybase
        return getString(columnIndex);
      default: {
        return getObjectOther(columnIndex);
      }
    }
  }
  protected Object getObjectOther(int columnIndex) throws SQLException {
    Object o = _rs.getObject(columnIndex);
    if (o instanceof String){
      return _pconn.getGBKEncodingStr(o.toString());
    }
    return o;
  }

  public Object getObject(String columnName) throws SQLException {
    int sqltype = getColumnType(columnName);
    switch(sqltype){
      case java.sql.Types.CLOB ://DB2,Mssql,Oracle
      case java.sql.Types.LONGVARCHAR: //Mysql,Sybase
        return getString(columnName);
      default: return getObjectOther(columnName);
    }
    
  }
  private Object getObjectOther(String columnName) throws SQLException {
    Object o = _rs.getObject(columnName);
    if (o instanceof String){
      return _pconn.getGBKEncodingStr(o.toString());
    }
    return o;
  }

  public int findColumn(String columnName) throws SQLException {
    return _rs.findColumn(columnName);
  }
  public Reader getCharacterStream(int columnIndex) throws SQLException {
    DataBaseInfo db = _pconn.getDbType();
    if (db != null && (db.isOscar())){
      return getCharacterStreamOscar(columnIndex);
    }
    if(_pconn.get_ds().needEncoding()){
      /**
       * 20100301
       * 对于clob类型的值可以直接通过getString读取，转换编码后转成StringReader返回；
       * 原来的代码从Reader转String编码再转reader，每次调用都产生中间变量，浪费内存；
       */
      String ss = getString(columnIndex);
      if(ss==null) return null;
      return new StringReader(ss);
    }
    return _rs.getCharacterStream(columnIndex);
  }
  
  private Reader getCharacterStreamOscar(int columnIndex) throws SQLException {
    Clob clob = _rs.getClob(columnIndex);
    if(clob==null) return null;
    return clob.getCharacterStream();
  }
 

  public Reader getCharacterStream(String columnName) throws SQLException {
     DataBaseInfo db = _pconn.getDbType();
    if (db != null && (db.isOscar())){
      return getCharacterStreamOscar(columnName);
    }
    if(_pconn.get_ds().needEncoding()){
      String ss = getString(columnName);
      if(ss==null) return null;
      return new StringReader(ss);
    }
    return _rs.getCharacterStream(columnName);
  }

  private Reader getCharacterStreamOscar(String columnName) throws SQLException {
    Clob clob = _rs.getClob(columnName);
    if(clob==null) return null;
    return clob.getCharacterStream();
  }
  public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
    return _rs.getBigDecimal(columnIndex);
  }

  public BigDecimal getBigDecimal(String columnName) throws SQLException {
    return _rs.getBigDecimal(columnName);
  }

  public boolean isBeforeFirst() throws SQLException {
    return _rs.isBeforeFirst();
  }

  public boolean isAfterLast() throws SQLException {
    return _rs.isAfterLast();
  }

  public boolean isFirst() throws SQLException {
    return _rs.isFirst();
  }

  public boolean isLast() throws SQLException {
    return _rs.isLast();
  }

  public void beforeFirst() throws SQLException {
    _rs.beforeFirst();
  }

  public void afterLast() throws SQLException {
    _rs.afterLast();
  }

  public boolean first() throws SQLException {
    return _rs.first();
  }

  public boolean last() throws SQLException {
    return _rs.last();
  }

  public int getRow() throws SQLException {
    return _rs.getRow();
  }

  public boolean absolute(int row) throws SQLException {
    return _rs.absolute(row);
  }

  public boolean relative(int rows) throws SQLException {
    return _rs.relative(rows);
  }

  public boolean previous() throws SQLException {
    return _rs.previous();
  }

  public void setFetchDirection(int direction) throws SQLException {
    _rs.setFetchDirection(direction);
  }

  public int getFetchDirection() throws SQLException {
    return _rs.getFetchDirection();
  }

  public void setFetchSize(int rows) throws SQLException {
    _rs.setFetchSize(rows);
  }

  public int getFetchSize() throws SQLException {
    return _rs.getFetchSize();
  }

  public int getType() throws SQLException {
    return _rs.getType();
  }

  public int getConcurrency() throws SQLException {
    return _rs.getConcurrency();
  }

  public boolean rowUpdated() throws SQLException {
    return _rs.rowUpdated();
  }

  public boolean rowInserted() throws SQLException {
    return _rs.rowInserted();
  }

  public boolean rowDeleted() throws SQLException {
    return _rs.rowDeleted();
  }

  public void updateNull(int columnIndex) throws SQLException {
    _rs.updateNull(columnIndex);
  }

  public void updateBoolean(int columnIndex, boolean x) throws SQLException {
    _rs.updateBoolean(columnIndex, x);
  }

  public void updateByte(int columnIndex, byte x) throws SQLException {
    _rs.updateByte(columnIndex, x);
  }

  public void updateShort(int columnIndex, short x) throws SQLException {
    _rs.updateShort(columnIndex, x);
  }

  public void updateInt(int columnIndex, int x) throws SQLException {
    _rs.updateInt(columnIndex, x);
  }

  public void updateLong(int columnIndex, long x) throws SQLException {
    _rs.updateLong(columnIndex, x);
  }

  public void updateFloat(int columnIndex, float x) throws SQLException {
    _rs.updateFloat(columnIndex, x);
  }

  public void updateDouble(int columnIndex, double x) throws SQLException {
    _rs.updateDouble(columnIndex, x);
  }

  public void updateBigDecimal(int columnIndex, BigDecimal x) throws
      SQLException {
    _rs.updateBigDecimal(columnIndex, x);
  }

  public void updateString(int columnIndex, String x) throws SQLException {
    _rs.updateString(columnIndex, x);
  }

  public void updateBytes(int columnIndex, byte[] x) throws SQLException {
    _rs.updateBytes(columnIndex, x);
  }

  public void updateDate(int columnIndex, Date x) throws SQLException {
    _rs.updateDate(columnIndex, x);
  }

  public void updateTime(int columnIndex, Time x) throws SQLException {
    _rs.updateTime(columnIndex, x);
  }

  public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
    _rs.updateTimestamp(columnIndex, x);
  }

  public void updateAsciiStream(int columnIndex, InputStream x, int length) throws
      SQLException {
    _rs.updateAsciiStream(columnIndex, x, length);
  }

  public void updateBinaryStream(int columnIndex, InputStream x, int length) throws
      SQLException {
    _rs.updateBinaryStream(columnIndex, x, length);
  }

  public void updateCharacterStream(int columnIndex, Reader x, int length) throws
      SQLException {
    _rs.updateCharacterStream(columnIndex, x, length);
  }

  public void updateObject(int columnIndex, Object x, int scale) throws
      SQLException {
    _rs.updateObject(columnIndex, x, scale);
  }

  public void updateObject(int columnIndex, Object x) throws SQLException {
    _rs.updateObject(columnIndex, x);
  }

  public void updateNull(String columnName) throws SQLException {
    _rs.updateNull(columnName);
  }

  public void updateBoolean(String columnName, boolean x) throws SQLException {
    _rs.updateBoolean(columnName, x);
  }

  public void updateByte(String columnName, byte x) throws SQLException {
    _rs.updateByte(columnName, x);
  }

  public void updateShort(String columnName, short x) throws SQLException {
    _rs.updateShort(columnName, x);
  }

  public void updateInt(String columnName, int x) throws SQLException {
    _rs.updateInt(columnName, x);
  }

  public void updateLong(String columnName, long x) throws SQLException {
    _rs.updateLong(columnName, x);
  }

  public void updateFloat(String columnName, float x) throws SQLException {
    _rs.updateFloat(columnName, x);
  }

  public void updateDouble(String columnName, double x) throws SQLException {
    _rs.updateDouble(columnName, x);
  }

  public void updateBigDecimal(String columnName, BigDecimal x) throws
      SQLException {
    _rs.updateBigDecimal(columnName, x);
  }

  public void updateString(String columnName, String x) throws SQLException {
    x = _pconn.getEncodingStr(x);
    _rs.updateString(columnName, x);
  }

  public void updateBytes(String columnName, byte[] x) throws SQLException {
    _rs.updateBytes(columnName, x);
  }

  public void updateDate(String columnName, Date x) throws SQLException {
    _rs.updateDate(columnName, x);
  }

  public void updateTime(String columnName, Time x) throws SQLException {
    _rs.updateTime(columnName, x);
  }

  public void updateTimestamp(String columnName, Timestamp x) throws
      SQLException {
    _rs.updateTimestamp(columnName, x);
  }

  public void updateAsciiStream(String columnName, InputStream x, int length) throws
      SQLException {
    _rs.updateAsciiStream(columnName, x, length);
  }

  public void updateBinaryStream(String columnName, InputStream x, int length) throws
      SQLException {
    _rs.updateBinaryStream(columnName, x, length);
  }

  public void updateCharacterStream(String columnName, Reader reader,
                                    int length) throws SQLException {
    _rs.updateCharacterStream(columnName, reader, length);
  }

  public void updateObject(String columnName, Object x, int scale) throws
      SQLException {
    _rs.updateObject(columnName, x, scale);
  }

  public void updateObject(String columnName, Object x) throws SQLException {
    _rs.updateObject(columnName, x);
  }

  public void insertRow() throws SQLException {
    _rs.insertRow();
  }

  public void updateRow() throws SQLException {
    _rs.updateRow();
  }

  public void deleteRow() throws SQLException {
    _rs.deleteRow();
  }

  public void refreshRow() throws SQLException {
    _rs.refreshRow();
  }

  public void cancelRowUpdates() throws SQLException {
    _rs.cancelRowUpdates();
  }

  public void moveToInsertRow() throws SQLException {
    _rs.moveToInsertRow();
  }

  public void moveToCurrentRow() throws SQLException {
    _rs.moveToCurrentRow();
  }

  public Statement getStatement() throws SQLException {
    return _rs.getStatement();
  }

  public Object getObject(int i, Map map) throws SQLException {
    return _rs.getObject(i, map);
  }

  public Ref getRef(int i) throws SQLException {
    return _rs.getRef(i);
  }

  public Blob getBlob(int i) throws SQLException {
    return _rs.getBlob(i);
  }

  public Clob getClob(int i) throws SQLException {
    return _rs.getClob(i);
  }

  public Array getArray(int i) throws SQLException {
    return _rs.getArray(i);
  }

  public Object getObject(String colName, Map map) throws SQLException {
    return _rs.getObject(colName, map);
  }

  public Ref getRef(String colName) throws SQLException {
    return _rs.getRef(colName);
  }

  public Blob getBlob(String colName) throws SQLException {
    return _rs.getBlob(colName);
  }

  public Clob getClob(String colName) throws SQLException {
    return _rs.getClob(colName);
  }

  public Array getArray(String colName) throws SQLException {
    return _rs.getArray(colName);
  }

  public Date getDate(int columnIndex, Calendar cal) throws SQLException {
    return _rs.getDate(columnIndex, cal);
  }

  public Date getDate(String columnName, Calendar cal) throws SQLException {
    return _rs.getDate(columnName, cal);
  }

  public Time getTime(int columnIndex, Calendar cal) throws SQLException {
    return _rs.getTime(columnIndex, cal);
  }

  public Time getTime(String columnName, Calendar cal) throws SQLException {
    return _rs.getTime(columnName, cal);
  }

  public Timestamp getTimestamp(int columnIndex, Calendar cal) throws
      SQLException {
    return _rs.getTimestamp(columnIndex, cal);
  }

  public Timestamp getTimestamp(String columnName, Calendar cal) throws
      SQLException {
    return _rs.getTimestamp(columnName, cal);
  }

  public URL getURL(int columnIndex) throws SQLException {
    return _rs.getURL(columnIndex);
  }

  public URL getURL(String columnName) throws SQLException {
    return _rs.getURL(columnName);
  }

  public void updateRef(int columnIndex, Ref x) throws SQLException {
    _rs.updateRef(columnIndex, x);
  }

  public void updateRef(String columnName, Ref x) throws SQLException {
    _rs.updateRef(columnName, x);
  }

  public void updateBlob(int columnIndex, Blob x) throws SQLException {
    _rs.updateBlob(columnIndex, x);
  }

  public void updateBlob(String columnName, Blob x) throws SQLException {
    _rs.updateBlob(columnName, x);
  }

  public void updateClob(int columnIndex, Clob x) throws SQLException {
    _rs.updateClob(columnIndex, x);
  }

  public void updateClob(String columnName, Clob x) throws SQLException {
    _rs.updateClob(columnName, x);
  }

  public void updateArray(int columnIndex, Array x) throws SQLException {
    _rs.updateArray(columnIndex, x);
  }

  public void updateArray(String columnName, Array x) throws SQLException {
    _rs.updateArray(columnName, x);
  }

}
