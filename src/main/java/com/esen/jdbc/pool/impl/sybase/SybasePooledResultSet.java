package com.esen.jdbc.pool.impl.sybase;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledResultSet;

/**
 * hibernate在读取大字段时必需调用getBlob(),getClob()，而这两个方法Sybase提供的jdbc驱动没有实现；
 * 驱动不支持ResultSet.getClob(...)和ResultSet.getBlob(...)读取大字段；
 * 即使使用ResultSet.getCharacterStream(...)和ResultSet.getBinaryStream(...)来读取大字段流，一但ResultSet关闭，就会出现流已关闭异常；
 * 
 * 解决办法：
 * 自定义实现Sybase读取Blob,Clob字段的方法；
 * 为了支持Hibernate，并实现二级缓存，目前采用blob,clob字段内容读取到内存中的方法实现；
 * 这样会耗费一些内存，暂时这样实现，以后有好办法再改进；
 *
 * @author dw
 */
public class SybasePooledResultSet extends PooledResultSet {

  public SybasePooledResultSet(ResultSet rs, PooledConnection pconn) throws SQLException {
    super(rs, pconn);
    
  }
  
	public Blob getBlob(int i) throws SQLException {
		InputStream in = getBinaryStream(i);
		return new EsenBlob(in);
	}

	public Clob getClob(int i) throws SQLException {
		Reader rr = getCharacterStream(i);
		return new EsenClob(rr);
	}

	public Blob getBlob(String colName) throws SQLException {
		InputStream in = getBinaryStream(colName);
		return new EsenBlob(in);
	}

	public Clob getClob(String colName) throws SQLException {
		Reader rr = getCharacterStream(colName);
		return new EsenClob(rr);
	}

  /**
   * 20100128
   * SybaseAse在读取date字段时，通过rs.getDate(..)读取的只有年月日，
   * 如果字段值包含时间信息，就会丢失；
   * 这里做个转换，读取时按timestamp读取，返回包含时间的Date类型值；
   */
  public Date getDate(int columnIndex) throws SQLException {
    Timestamp tsp = _rs.getTimestamp(columnIndex);
    if(tsp==null)return null;
    return new Date(tsp.getTime());
  }
  public Date getDate(String columnName) throws SQLException {
    Timestamp tsp = _rs.getTimestamp(columnName);
    if(tsp==null)return null;
    return new Date(tsp.getTime());
  }
}
