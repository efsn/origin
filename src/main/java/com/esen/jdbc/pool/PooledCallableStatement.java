package com.esen.jdbc.pool;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.Time;
import java.sql.Timestamp;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Calendar;


public class PooledCallableStatement extends PooledPreparedStatement implements CallableStatement {
  private CallableStatement _cstat;
  public PooledCallableStatement(PooledConnection conn,CallableStatement cstat) {
    super(conn,cstat,null);
    _cstat = cstat;
  }

  public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
    _cstat.registerOutParameter(parameterIndex,sqlType);
  }
  public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
    _cstat.registerOutParameter(parameterIndex,sqlType,scale);
  }
  public boolean wasNull() throws SQLException {
    return _cstat.wasNull();
  }
  public String getString(int parameterIndex) throws SQLException {
    String value = _cstat.getString(parameterIndex);
    value = pconn.getGBKEncodingStr(value);
    return value;
  }
  public boolean getBoolean(int parameterIndex) throws SQLException {
    return _cstat.getBoolean(parameterIndex);
  }
  public byte getByte(int parameterIndex) throws SQLException {
    return _cstat.getByte(parameterIndex);
  }
  public short getShort(int parameterIndex) throws SQLException {
    return _cstat.getShort(parameterIndex);
  }
  public int getInt(int parameterIndex) throws SQLException {
    return _cstat.getInt(parameterIndex);
  }
  public long getLong(int parameterIndex) throws SQLException {
    return _cstat.getLong(parameterIndex);
  }
  public float getFloat(int parameterIndex) throws SQLException {
    return _cstat.getFloat(parameterIndex);
  }
  public double getDouble(int parameterIndex) throws SQLException {
    return _cstat.getDouble(parameterIndex);
  }
  public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
    throw new java.lang.UnsupportedOperationException("Method getBigDecimal() not yet implemented.");
  }
  public byte[] getBytes(int parameterIndex) throws SQLException {
    return _cstat.getBytes(parameterIndex);
  }
  public Date getDate(int parameterIndex) throws SQLException {
    return _cstat.getDate(parameterIndex);
  }
  public Time getTime(int parameterIndex) throws SQLException {
    return _cstat.getTime(parameterIndex);
  }
  public Timestamp getTimestamp(int parameterIndex) throws SQLException {
    return _cstat.getTimestamp(parameterIndex);
  }
  public Object getObject(int parameterIndex) throws SQLException {
    return _cstat.getObject(parameterIndex);
  }
  public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
    return _cstat.getBigDecimal(parameterIndex);
  }
  public Object getObject(int i, Map map) throws SQLException {
    return _cstat.getObject(i,map);
  }
  public Ref getRef(int i) throws SQLException {
    return _cstat.getRef(i);
  }
  public Blob getBlob(int i) throws SQLException {
    return _cstat.getBlob(i);
  }
  public Clob getClob(int i) throws SQLException {
    return _cstat.getClob(i);
  }
  public Array getArray(int i) throws SQLException {
    return _cstat.getArray(i);
  }
  public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
    return _cstat.getDate(parameterIndex,cal);
  }
  public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
    return _cstat.getTime(parameterIndex,cal);
  }
  public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
    return _cstat.getTimestamp(parameterIndex,cal);
  }
  public void registerOutParameter(int paramIndex, int sqlType, String typeName) throws SQLException {
    _cstat.registerOutParameter(paramIndex,sqlType,typeName);
  }
  public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
    _cstat.registerOutParameter(parameterName,sqlType);
  }
  public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
    _cstat.registerOutParameter(parameterName,sqlType,scale);
  }
  public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
    _cstat.registerOutParameter(parameterName,sqlType,typeName);
  }
  public URL getURL(int parameterIndex) throws SQLException {
    return _cstat.getURL(parameterIndex);
  }
  public void setURL(String parameterName, URL val) throws SQLException {
    _cstat.setURL(parameterName,val);
  }
  public void setNull(String parameterName, int sqlType) throws SQLException {
    _cstat.setNull(parameterName,sqlType);
  }
  public void setBoolean(String parameterName, boolean x) throws SQLException {
    _cstat.setBoolean(parameterName,x);
  }
  public void setByte(String parameterName, byte x) throws SQLException {
    _cstat.setByte(parameterName,x);
  }
  public void setShort(String parameterName, short x) throws SQLException {
    _cstat.setShort(parameterName,x);
  }
  public void setInt(String parameterName, int x) throws SQLException {
    _cstat.setInt(parameterName,x);
  }
  public void setLong(String parameterName, long x) throws SQLException {
    _cstat.setLong(parameterName,x);
  }
  public void setFloat(String parameterName, float x) throws SQLException {
    _cstat.setFloat(parameterName,x);
  }
  public void setDouble(String parameterName, double x) throws SQLException {
    _cstat.setDouble(parameterName,x);
  }
  public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
    _cstat.setBigDecimal(parameterName,x);
  }
  public void setString(String parameterName, String x) throws SQLException {
    x = pconn.getEncodingStr(x);
    _cstat.setString(parameterName,x);
  }
  public void setBytes(String parameterName, byte[] x) throws SQLException {
    _cstat.setBytes(parameterName,x);
  }
  public void setDate(String parameterName, Date x) throws SQLException {
    _cstat.setDate(parameterName,x);
  }
  public void setTime(String parameterName, Time x) throws SQLException {
    _cstat.setTime(parameterName,x);
  }
  public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
    _cstat.setTimestamp(parameterName,x);
  }
  public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
    _cstat.setAsciiStream(parameterName,x,length);
  }
  public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
    _cstat.setBinaryStream(parameterName,x,length);
  }
  public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
    _cstat.setObject(parameterName,x,targetSqlType,scale);
  }
  public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
    _cstat.setObject(parameterName,x,targetSqlType);
  }
  public void setObject(String parameterName, Object x) throws SQLException {
    _cstat.setObject(parameterName,x);
  }
  public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
    _cstat.setCharacterStream(parameterName,reader,length);
  }
  public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
    _cstat.setDate(parameterName,x,cal);
  }
  public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
    _cstat.setTime(parameterName,x,cal);
  }
  public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
    _cstat.setTimestamp(parameterName,x,cal);
  }
  public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
    _cstat.setNull(parameterName,sqlType,typeName);
  }
  public String getString(String parameterName) throws SQLException {
    String value = _cstat.getString(parameterName);
      value = pconn.getGBKEncodingStr(value);
    return value;
  }
  public boolean getBoolean(String parameterName) throws SQLException {
    return _cstat.getBoolean(parameterName);
  }
  public byte getByte(String parameterName) throws SQLException {
    return _cstat.getByte(parameterName);
  }
  public short getShort(String parameterName) throws SQLException {
    return _cstat.getShort(parameterName);
  }
  public int getInt(String parameterName) throws SQLException {
    return _cstat.getInt(parameterName);
  }
  public long getLong(String parameterName) throws SQLException {
    return _cstat.getLong(parameterName);
  }
  public float getFloat(String parameterName) throws SQLException {
    return _cstat.getFloat(parameterName);
  }
  public double getDouble(String parameterName) throws SQLException {
    return _cstat.getDouble(parameterName);
  }
  public byte[] getBytes(String parameterName) throws SQLException {
    return _cstat.getBytes(parameterName);
  }
  public Date getDate(String parameterName) throws SQLException {
    return _cstat.getDate(parameterName);
  }
  public Time getTime(String parameterName) throws SQLException {
    return _cstat.getTime(parameterName);
  }
  public Timestamp getTimestamp(String parameterName) throws SQLException {
    return _cstat.getTimestamp(parameterName);
  }
  public Object getObject(String parameterName) throws SQLException {
    return _cstat.getObject(parameterName);
  }
  public BigDecimal getBigDecimal(String parameterName) throws SQLException {
    return _cstat.getBigDecimal(parameterName);
  }
  public Object getObject(String parameterName, Map map) throws SQLException {
    return _cstat.getObject(parameterName,map);
  }
  public Ref getRef(String parameterName) throws SQLException {
    return _cstat.getRef(parameterName);
  }
  public Blob getBlob(String parameterName) throws SQLException {
    return _cstat.getBlob(parameterName);
  }
  public Clob getClob(String parameterName) throws SQLException {
    return _cstat.getClob(parameterName);
  }
  public Array getArray(String parameterName) throws SQLException {
    return _cstat.getArray(parameterName);
  }
  public Date getDate(String parameterName, Calendar cal) throws SQLException {
    return _cstat.getDate(parameterName,cal);
  }
  public Time getTime(String parameterName, Calendar cal) throws SQLException {
    return _cstat.getTime(parameterName,cal);
  }
  public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
    return _cstat.getTimestamp(parameterName,cal);
  }
  public URL getURL(String parameterName) throws SQLException {
    return _cstat.getURL(parameterName);
  }
  public void close() throws SQLException {
    super.close();
  }
}
