package com.esen.jdbc.pool.impl.oracle;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledResultSet;
import com.esen.util.StrFunc;


/**
 * 20090805
 * Oracle9,10g可以使用常规方法读取和写入大字段了；
 * @author Administrator
 *
 */
public class OraclePooledResultSet extends PooledResultSet {

  public OraclePooledResultSet(ResultSet rs, PooledConnection pconn)
      throws SQLException {
    super(rs, pconn);
  }
  
  public ResultSetMetaData getMetaData() throws SQLException {
    if(rsmeta==null)
      rsmeta = new OracleResultSetMetaData(_rs.getMetaData(),_pconn.getDbType());
    return rsmeta;
  }
  /**
   * Oracle在读取date字段时，通过rs.getDate(..)读取的只有年月日，
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
  
  /**
   * rs.getString(1)在获取number字段的值时有问题，比如“0.23”，getString会返回“.23”
   * 对于超大数：210005000004723196 不能getDouble
   */
  public String getString(int columnIndex) throws SQLException {
    switch(getSQLType(columnIndex)){
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.BIT:
      case Types.REAL:
      case Types.DECIMAL:{
        String v = _rs.getString(columnIndex);
        return getNumberStr(v);
      }
      case java.sql.Types.CLOB :{
        /**
         * 20090625
         * Oracle只有定义的clob类型才需要通过流来读取；
         * 但是经过测试clob,long 类型的字段，可以直接getString()来读取；
         * 在Oracle9,Oracle10上测试通过；
         * 注：使用的是10g的jdbc驱动；
         * 对于Oracle8 需要测试，TODO
         */
        //return getStrFromReader(getCharacterStream(columnIndex));
      }
    }
    return trimForORA01461(super.getString(columnIndex));
  }

  protected String getNumberStr(String v) throws SQLException {
    if(_rs.wasNull())
      return null;
    if(v!=null&&v.length()>0&&v.charAt(0)=='.'){
      return "0"+v;
    }
    else return v;//210005000004723196
  }
  
  public String getString(String columnName) throws SQLException {
    switch(getColumnType(columnName)){
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.BIT:
      case Types.REAL:
      case Types.DECIMAL:{
        String v = _rs.getString(columnName);
        return getNumberStr(v);
      }
    }
    return trimForORA01461(super.getString(columnName));
  }
  
  /**
   * 经测试，Oracle对于超过1000的varchar2类型，在写入[1001,2000]长度的字符串时，可能出现：
   * ORA-01461：仅能绑定要插入 LONG 列的 LONG 值；在{@link OraclePooledPreparedStatement#setString(int, String)}
   * 中我们对字符串添加了空格，在这里需要去掉。
   * @param str
   * @return
   */
	private String trimForORA01461(String str) {
		if (StrFunc.isNull(str)) {
			return str;
		}
		int len = str.length();
		if (len == 2001) {
			return rightTrim(str);
		}
		
		return str;
	}
	
	/**
	 * 去掉字符串后面添加的空格
	 * @param str
	 * @return
	 */
	private static String rightTrim(String str){
		if(StrFunc.isNull(str)){
			return str;
		}
		int pos = 0;
		for(int i = str.length()-1;i>0;i--){
			if(str.charAt(i)!=' '){
				pos = i;
				break;
			}
		}
		return str.substring(0,pos+1);
	}

}
