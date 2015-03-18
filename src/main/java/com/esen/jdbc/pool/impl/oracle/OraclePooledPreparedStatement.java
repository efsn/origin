package com.esen.jdbc.pool.impl.oracle;


import java.io.CharArrayReader;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.jdbc.ibatis.SerialClob;
import com.esen.jdbc.pool.JdbcLogger;
import com.esen.jdbc.pool.PooledConnection;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * 20090805
 * Oracle9,10g可以使用常规方法读取和写入大字段了；
 * @author dw
 *
 */
public class OraclePooledPreparedStatement extends OraclePooledStatement {

  public OraclePooledPreparedStatement(PooledConnection conn, Statement stat) {
    super(conn, stat);
  }
  public OraclePooledPreparedStatement(PooledConnection conn, PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }
  public ResultSet executeQuery() throws SQLException {
    return new OraclePooledResultSet(_pstat.executeQuery(),pconn);
  }
  
  public void setString(int parameterIndex, String x) throws SQLException {
    if (pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    x = pconn.getEncodingStr(x);
    x = getStringForORA01461(x);
    _pstat.setString(parameterIndex, x);
  }

  /**
   * 经测试，Oracle对于超过1000的varchar2类型，在写入[1001,2000]长度的字符串时，可能出现：
   * ORA-01461：仅能绑定要插入 LONG 列的 LONG 值；
   * 在Oracle9i,Oracle10.1版本下，
   * 使用oracle11.2/10.1/10.2的驱动，都有这个问题：
   * 这个长度对于中文是字符长度，但是汉字占两个定义长度，如果超过了定义的长度也也会出这个异常；
   * 比如：对于定义varchar2(4000)的字段，写入不能等于或者超过2000个汉字，否则也出现ORA-01461异常；
   * 
   * 在Oracle10.2版本下，
   * 使用oracle10.2驱动，没有出现出现ORA-01461异常；
   * 
   * 使用clob字段，除了没有长度的限制,其他情况和字符类型一样；
   * 
   * 
   * 解决办法：
   * 1）Oracle定义字符类型时，长度如果超过1000，则建议定义超过2000；
   *    原因：如果定义成2000，则写入大于1000小于2000长度的字符，必出ORA-01461异常；
   *    这个在定义表时，人为注意下，程序不自动处理；
   *    
   * 2）如果定义的字符类型字段长度超过2000，则在写入(1000,2000]长度的字符时，在后面补空格，将长度补到2001长度，再写入数据库；
   *    这个过程由jdbc程序自动处理；
   *    注意：这里长度按字符计算，即一个中文算一个长度；
   * 3）补齐后如果字节长度超过字段定义的长度，写入会出ORA-01461异常，这时的解决办法是增加字段定义长度；
   * 
   * 此方法用于对字符串补空格；
   */
  public String getStringForORA01461(String v){
    if(StrFunc.isNull(v)){
      return v;
    }
    int len = v.length();
    if(len>1000&&len<2001){
      int k = 2001-len;
      StringBuffer sbuf = new StringBuffer(2010);
      sbuf.append(v);
      for(int i=0;i<k;i++){
        sbuf.append(' ');
      }
      return sbuf.toString();
    }
    return v;
  }
  
  public void setClob(int i, Clob x) throws SQLException {
    if (pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(i + "  " + x);
    if (pconn.canLogLevel(JdbcLogger.LOG_LEVER_WARN))
//      pconn.logWarn("建议用 setCharacterStream(...) 写入Clob; ");
      pconn.logWarn(I18N.getString("com.esen.jdbc.pool.impl.oracle.oraclepooledpreparedstatement.comp1", "建议用 setCharacterStream(...) 写入Clob "));
    if(x==null){
      _pstat.setNull(i, java.sql.Types.CLOB);
      return;
    }
    Reader r = x.getCharacterStream();
    String ss = getStrFromReader(r);
    ss = getStringForORA01461(ss);
    x = new SerialClob(ss.toCharArray());
    _pstat.setClob(i, x);
  }
  
  public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
    if (pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + reader + "  " + length);
    if ( (reader == null) || (length == 0)) {
      _pstat.setNull(parameterIndex, java.sql.Types.CLOB);
      return;
    }
    if (pconn.get_ds().needEncoding()) {
      String ss = getStrFromReader(reader);
      ss = pconn.getEncodingStr(ss);
      ss = getStringForORA01461(ss);
      reader = new CharArrayReader(ss.toCharArray());
      length = ss.length();
    }else{
      String ss = getStrFromReader(reader);
      ss = getStringForORA01461(ss);
      reader = new CharArrayReader(ss.toCharArray());
      length = ss.length();
    }
    _pstat.setCharacterStream(parameterIndex, reader, length);
  }
  
  /**
   * Oracle在写入date类型字段时，调用setDate(i,x)方法,会将x中的时间信息丢失；
   * 这里转成timestamp写入，就不会丢失时间信息；
   * 20090818
   * 但是需要注意date类型只保存到秒，毫秒数会丢失；
   */
  public void setDate(int parameterIndex, Date x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    if(x==null)  {
      _pstat.setDate(parameterIndex,null);
    }else{
      long dt = x.getTime();
      _pstat.setTimestamp(parameterIndex, new Timestamp(dt));
    }
  }
  
  /**
   * 20090818
   * 先前的代码对timestamp的写入，将毫秒数去掉了；
   * 原因是Oracle8没有timestamp类型，写入问题，将毫秒数去掉；
   * 现在移到Oracle8PooledPreparedStatement类中；
   */

}
