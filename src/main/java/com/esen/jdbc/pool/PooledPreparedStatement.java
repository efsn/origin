package com.esen.jdbc.pool;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.esen.io.MyByteArrayInputStream;
import com.esen.jdbc.SqlFunc;
import com.esen.util.StmFunc;
import com.esen.util.i18n.I18N;

/**
 * 实现PreparedStatement接口的实现类；
 * 各个数据库都有自己的实现；
 * Statement也继承该抽象类；
 * @author dw
 *
 */
public class PooledPreparedStatement extends PooledStatement
    implements PreparedStatement {
  protected PreparedStatement _pstat;
  protected String sql;
  
  private ParameterMetaData parameterMetaData;
  
  /**
   * 为各个数据库实现Statement接口
   * @param conn
   * @param stat
   */
  protected PooledPreparedStatement(PooledConnection conn,Statement stat) {
    super(conn,stat);
  }
  
  /**
   * 为各个数据库实现PreparedStatement接口
   * @param conn
   * @param pstat
   * @param sql
   */
  protected PooledPreparedStatement(PooledConnection conn, PreparedStatement pstat,String sql) {
    super(conn,pstat);
    _pstat = (PreparedStatement)_stat;
    this.sql = sql;
    
  }
  /**
   * 返回驱动接口实现
   * @return
   */
  public PreparedStatement getPreparedStatement(){
    return _pstat;
  }

	public ResultSet executeQuery() throws SQLException {
		try {
			return new PooledResultSet(_pstat.executeQuery(), pconn);
		}
		catch (SQLException ex) {
			throwSQLException(ex);
			return null;
		}
	}

	public int executeUpdate() throws SQLException {
		try {
			return _pstat.executeUpdate();
		}
		catch (SQLException ex) {
			throwSQLException(ex);
			return -1;
		}
	}

  public void setNull(int parameterIndex, int sqlType) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug("setNull: " + parameterIndex + "  " + sqlType);
    _pstat.setNull(parameterIndex, sqlType);
  }

  public void setBoolean(int parameterIndex, boolean x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    _pstat.setBoolean(parameterIndex, x);
  }

  public void setByte(int parameterIndex, byte x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
       pconn.logDebug(parameterIndex + "  " + x);
    _pstat.setByte(parameterIndex, x);
  }

  public void setShort(int parameterIndex, short x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    _pstat.setShort(parameterIndex, x);
  }

  public void setInt(int parameterIndex, int x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    _pstat.setInt(parameterIndex, x);
  }

  public void setLong(int parameterIndex, long x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    _pstat.setLong(parameterIndex, x);
  }

  public void setFloat(int parameterIndex, float x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    _pstat.setFloat(parameterIndex, x);
  }

  public void setDouble(int parameterIndex, double x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    _pstat.setDouble(parameterIndex, x);
  }

  public void setBigDecimal(int parameterIndex, BigDecimal x) throws
      SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    _pstat.setBigDecimal(parameterIndex, x);
  }

  public void setString(int parameterIndex, String x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    x = pconn.getEncodingStr(x);
    _pstat.setString(parameterIndex, x);
  }

  public void setBytes(int parameterIndex, byte[] x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  byte[...]");
    _pstat.setBytes(parameterIndex, x);
  }

  public void setDate(int parameterIndex, Date x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    _pstat.setDate(parameterIndex, x);
  }

  public void setTime(int parameterIndex, Time x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    _pstat.setTime(parameterIndex, x);
  }

  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    _pstat.setTimestamp(parameterIndex, x);
  }

  public void setAsciiStream(int parameterIndex, InputStream x, int length) throws
      SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x + "  " + length);
    _pstat.setAsciiStream(parameterIndex, x, length);
  }

  public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws
      SQLException {
    throw new java.lang.UnsupportedOperationException(
        "Method setUnicodeStream() not yet implemented.");
  }

  protected void setBinaryStream2(int parameterIndex, InputStream x,
                                      int length) throws SQLException {
    if ( (x == null) || (length == 0)) {
      _pstat.setNull(parameterIndex, java.sql.Types.BLOB);
      return;
    }
    _pstat.setBinaryStream(parameterIndex, x, length);
  }



  public static final int MAX_BINARY =  1024*1024;

  //磁盘流的缓存
  private List cbs;
  
  //关闭磁盘内的缓存
  private void closeCachedDiskFiles(){
    if(cbs==null) return;
    for(int i=0;i<cbs.size();i++){
      DiskBinaryInfo info=(DiskBinaryInfo) cbs.get(i);
      info.close();
    }
    if(cbs!=null) cbs.clear();
  }

  /**内存流
   * @param x
   * @param length 
   * @param length
   * @return
   * @throws IOException 
   */
  private InputStream makeMemoryBinaryStream(InputStream x, int length) throws IOException {
    if(length>0){
      //读取指定长度
      byte[] bb = new byte[length];
      int rlen = StmFunc.stmTryRead(x, bb);//实际流长度
      if(rlen<length){
        //指定长度超过流总长度
        byte[] rbb = new byte[rlen];
        System.arraycopy(bb, 0, rbb, 0, rlen);
        return new ByteArrayInputStream(rbb);
      }
      return new ByteArrayInputStream(bb);
    }
    //length<0 则读取全部流
    byte[] bb = StmFunc.stm2bytes(x);
    return new ByteArrayInputStream(bb);
  }

  class DiskBinaryInfo {
    File f;
    InputStream in;
    long len;//in流长度
    void close() {
      if (in != null) {
        try {
          in.close();
          in=null;
        }
        catch (IOException e) {
        }
      }
      if (f != null)
        f.delete();
      f=null;
    }
  }

  /**文件流
   * @param x
   * @param length
   * @return
   * @throws IOException
   */
  private DiskBinaryInfo makeDiskBinaryFile(InputStream x, int length) throws Exception {
    DiskBinaryInfo info=new DiskBinaryInfo();
    info.f = File.createTempFile("psmt", null);
    info.f.getParentFile().mkdirs();
    /**
     * 写入磁盘文件
     * 原来的代码使用压缩流写入临时文件，读取时解压，然后写入数据库；
     * 现在改为对临时文件的存储不使用压缩流；
     * 原因是：Oracle对一定长度的流，如果这个流本身就是压缩流，再通过压缩，写临时文件，读取，写入数据库，就会出现写入的流与原来的流不一致问题；
     * 原因不明，可能是oracle jdbc的bug；
     */
    OutputStream out = new FileOutputStream(info.f);
    try {
    	if(length>0){
    		info.len = StmFunc.stmCopyFrom(x, out,length);
    	}else{//length<0表示不知道流的长度，写入整个流；
    		info.len = StmFunc.stmTryCopyFrom(x, out);
    	}
    }
    finally {
      out.close();
    }
    info.in = new FileInputStream(info.f);
    return info;
  }

  /**构造输入流的缓冲器,如果较小的数据就用内存流,大数据就用文件缓冲
   * length>0  指定流长度，有可能小于或者大于流x的长度
   * length=0  返回null
   * length<0  返回整个流x
   * @param x
   * @param length
   * @return
   * @throws SQLException
   */
  private DiskBinaryInfo makeCachedBinaryStream(InputStream x, int length) throws Exception {
    if((x==null)||(length==0)) return null;
    if (length>0&&length <= MAX_BINARY) {
      /**
       * 小于缓冲阀值用内存流；
       */
      DiskBinaryInfo info = new DiskBinaryInfo();
      info.in =  makeMemoryBinaryStream(x,length);
      info.len = info.in.available();
      return info;
    }
    else {
    	/**
    	 * 如果length<0表示不知道流长度，通过写入临时文件，读取整个流，再写入数据库；
    	 */
      if(cbs==null) cbs=new ArrayList(20);
      DiskBinaryInfo info = makeDiskBinaryFile(x,length);
      cbs.add(info);
      return info;
    }
  }

  public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
    int len;
    try {
      //设置缓存流避免分配大内存
      DiskBinaryInfo info = makeCachedBinaryStream(x,length);
      x = info==null?null:info.in;
      len = info==null?0:(int)info.len;
    }
    catch (Exception e) {
//    	SQLException se = new SQLException("设置大字段流出错:"+e.getMessage());
    	SQLException se = new SQLException(I18N.getString("com.esen.jdbc.pool.pooledpreparedstatement.setloberr", "设置大字段流出错:{0}", new Object[]{e.getMessage()}));
    	se.setStackTrace(e.getStackTrace());
    	throw se;
    }
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x + "  " + len);
    setBinaryStream2(parameterIndex, x, len);
  }


  public void clearParameters() throws SQLException {
    _pstat.clearParameters();
  }

  public void setObject(int parameterIndex, Object x, int targetSqlType,
                        int scale) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x + "  " + targetSqlType + "  " +
                      scale);
    _pstat.setObject(parameterIndex, x, targetSqlType, scale);
  }

  public void setObject(int parameterIndex, Object x, int targetSqlType) throws
      SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x + "  " + targetSqlType);
    _pstat.setObject(parameterIndex, x, targetSqlType);
  }

  /**
   * 支持任意类型的数据值；
   * 但是对于clob字段，支持String,Reader,Clob对象；
   * 对于blob字段，支持Blob,InputStream,btye[]对象；
   */
  public void setObject(int parameterIndex, Object x) throws SQLException {
    ParameterMetaData pmd = getParameterMetaData();
    if(pmd==null){
      setObjectDefault(parameterIndex,x);
      return;
    }
    int t = pmd.getParameterType(parameterIndex);
    if(x==null){
      setNull(parameterIndex, t);
      return;
    }
    switch(t){
      case Types.LONGVARCHAR:
      case Types.CLOB:{
        mySetClob(parameterIndex,x);
        return;
      }
      case Types.BLOB:
      case Types.VARBINARY:
      case Types.BINARY:
      case Types.LONGVARBINARY:{
        mySetBlob(parameterIndex, x);
        return;
      }
      case Types.DATE:{
        setDate(parameterIndex ,SqlFunc.toSqlDate(x));
        return;
      }
      case Types.TIME:
      case Types.TIMESTAMP:{
        setTimestamp(parameterIndex , SqlFunc.toSqlTimeStamp(x));
        return;
      }
      case Types.VARCHAR:
      case Types.CHAR:{
        setString(parameterIndex,SqlFunc.toSqlVarchar(x));
        return;
      }
      case Types.NUMERIC:
      /**
       * 20090710
       * 对Mysql 调用setObject(1,"");
       * 原来的代码没有把DECIMAL类型加到这里，对数值类型使用setObject插入空串，报错；
       */
      case Types.DECIMAL:
      case Types.DOUBLE:
      case Types.FLOAT:
      case Types.REAL:{
        //超大数字，转换double有精度损失；
        if(x instanceof BigDecimal){
          BigDecimal bd = (BigDecimal)x;
          setBigDecimal(parameterIndex, bd);
          return;
        }
        if(x instanceof String){
          /**
           * 20090430
           * 如果是空字符串，这里对于数值插入空；
           */
          String xstr = (String)x;
          xstr = xstr.trim();//增强：先trim，如果是"  "也相当于插入空；
          if(xstr.length()==0){
            setNull(parameterIndex, t);
            return;
          }
          /**
           * BI-3869
           * 目标数据库字段类型是数值，但是要写入的却是true/false,发生这种情况，
           * 可能是有些数据库的bool类型，转到目标数据库变成了整型。
           * 比如这个帖子就是sqlserver到oracle，有个字段是bit类型，在sqlserver是用true/false存储，
           * 但是oracle中bit是数值型存储，所以这里做了转换。
           * 20120224 dw
           */
          if("true".equalsIgnoreCase(xstr)){
        	  setInt(parameterIndex,1);
        	  return;
          }
          if("false".equalsIgnoreCase(xstr)){
        	  setInt(parameterIndex,0);
        	  return;
          }
          /**
           * BI-6254 这里对数值类型的写入，却碰到了字符'~';
           * 产生的原因是：Oracle环境，该字段值通过jdbc的getString()读取是'~',通过getDouble()读取是Double.POSITIVE_INFINITY，
           * 系统备份是通过jdbc的getString()读取字段值的，所以恢复写入数据库读取.db文件碰到这个'~'。
           * 解决办法：Double.POSITIVE_INFINITY是一个非法的数值，恢复写入直接将该字段值写为空。
           */
          if("~".equals(xstr)||"-~".equals(xstr)){
        	  setNull(parameterIndex, t);
        	  return;
          }
          //将字符串插入数值，sybase数据库用setString报错；这里改用无精度损失的BigDecimal来处理；
          BigDecimal bd = new BigDecimal(xstr);
          setBigDecimal(parameterIndex, bd);
          //setDouble(parameterIndex,Double.parseDouble(xstr));
          //setString(parameterIndex,(String)x); sybase 对num类型，setString出错；
          return;
        }
        setDouble(parameterIndex,Double.parseDouble(x.toString()));
        return;
      }
      /**
       * 20100107
       * 这里没有加上下面这两种数据库里面的整型类型；
       * 造成setObject()出错 ；
       * 例 ：DB2中创建了smallint类型的字段，值x可能是个字符串类型的对象，直接调用
       * _pstat.setObject(parameterIndex, x)会出现：
       *  Invalid data conversion: Parameter instance  is invalid for requested conversion.
        at com.ibm.db2.jcc.b.r.a(r.java:690)
        at com.ibm.db2.jcc.b.tf.b(tf.java:927)
        at com.ibm.db2.jcc.b.tf.setString(tf.java:910)
        at com.ibm.db2.jcc.b.tf.setObject(tf.java:1190)
        at com.esen.jdbc.pool.impl.PooledPreparedStatement.setObject(PooledPreparedStatement.java:463)
        at com.esen.jdbc.data.impl.DataWriterToDb.setObject(DataWriterToDb.java:1365)
       * 异常；
       */
      case Types.SMALLINT:
      case Types.TINYINT:
      case Types.INTEGER:{
        /**
         * 将浮点数转整型，比如：12.0 调用Integer.parseInt("12.0")会出错；
         * 这里转换下一处理；
         */
        if(x instanceof Number){
          Number nx = (Number)x;
          setInt(parameterIndex,nx.intValue());
          return;
        }
        if(x instanceof String){
          /**
           * 20091026
           * setObject时，将空串插入int类型字段，出异常；
           * 解决办法：将空串当作null插入int类型字段；
           */
          String sv = (String)x;
          if(sv.trim().length()==0){
            setNull(parameterIndex, t);
            return;
          }
        }
        /**
         * 20091026
         * 对于形如"11.0"的字符串，插入int类型字段，出异常；
         * 解决办法：将其转换成int类型，在插入；
         */
        Number nx = new Double(Double.parseDouble(x.toString()));
        setInt(parameterIndex,nx.intValue());
        return;
      }
      case Types.BIGINT: {
        //同setint
        if (x instanceof String) {
          String sv = (String) x;
          if (sv.trim().length() == 0) {
            _pstat.setNull(parameterIndex, t);
            return;
          }
        }
        setLong(parameterIndex, Long.parseLong(x.toString()));
        return;
      }
    }
    pconn.logDebug(parameterIndex + "  " + x);
    _pstat.setObject(parameterIndex, x);
  }
  public void setObjectDefault(int parameterIndex, Object x) throws SQLException {
    //int t = _pstat.getParameterMetaData().getParameterType(parameterIndex);
      if(x==null){
        if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
          pconn.logDebug(parameterIndex + "  " + x);
        _pstat.setObject(parameterIndex,null);
        return;
      }
      if(x instanceof Reader){
        Reader r = (Reader)x;
        setCharacterStream(parameterIndex,r,Integer.MAX_VALUE);
        return;
      }else
      if(x instanceof Clob){
        Clob clob = (Clob)x;
        Reader r = clob.getCharacterStream();
        if(r!=null){
          setCharacterStream(parameterIndex,r,Integer.MAX_VALUE);
          return;
        }else {
          setClob(parameterIndex, clob);
          return;
        }
      }else
      if (x instanceof Blob) {
        Blob blob = (Blob) x;
        InputStream in = blob.getBinaryStream();
        if (in != null) {
          setBinaryStream(parameterIndex, in,Integer.MAX_VALUE);
          return;
        }
        else {
          setBlob(parameterIndex, blob);
          return;
        }
      }else if(x instanceof InputStream){
        InputStream in = (InputStream)x;
        setBinaryStream(parameterIndex, in,Integer.MAX_VALUE);
        return;
      }else if(x instanceof String){
        //删除数据时，英文Oracle数据库，设置参数setObject(i,value) value 如果有中文，删除不成功；
        //原因是英文数据库，没有转码；
        setString(parameterIndex, (String)x);
        return;
      }
      pconn.logDebug(parameterIndex + "  " + x);
      _pstat.setObject(parameterIndex, x);
    }
  protected void mySetClob(int parameterIndex, Object x) throws SQLException{
    if(x instanceof Reader){
      Reader r = (Reader)x;
      setCharacterStream(parameterIndex,r,Integer.MAX_VALUE);
      return;
    }else if(x instanceof String){
      String str = (String)x;
      CharArrayReader r = new CharArrayReader(str.toCharArray());
      setCharacterStream(parameterIndex,r,str.length());
      return;
    }else
    if(x instanceof Clob){
      Clob clob = (Clob)x;
      Reader r = clob.getCharacterStream();
      if(r!=null){
        setCharacterStream(parameterIndex,r,Integer.MAX_VALUE);
        return;
      }else {
        setClob(parameterIndex, clob);
        return;
      }
    }
    if(x instanceof Integer||
        x instanceof Long ||
        x instanceof Float ||
        x instanceof Double ||
        x instanceof Date ||
        x instanceof java.util.Date ||
        x instanceof Time ||
        x instanceof Timestamp){
      setString(parameterIndex,x.toString());
      return;
    }
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    _pstat.setObject(parameterIndex, x);
  }
  protected void mySetBlob(int parameterIndex, Object x) throws SQLException{
    if (x instanceof Blob) {
      Blob blob = (Blob) x;
      InputStream in = blob.getBinaryStream();
      if (in != null) {
        setBinaryStream(parameterIndex, in, -1);
        return;
      }
      else {
        setBlob(parameterIndex, blob);
        return;
      }
    }else if(x instanceof InputStream){
      InputStream in = (InputStream)x;
      //不知道流的长度，传-1
      setBinaryStream(parameterIndex, in, -1);
      return;
    }else if(x.getClass().isArray() && x.getClass().getComponentType()==byte.class){
      byte[] bb = (byte[]) x;
      setBinaryStream(parameterIndex, new MyByteArrayInputStream(bb),bb.length);
      return;
    }
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    _pstat.setObject(parameterIndex, x);
  }

	public boolean execute() throws SQLException {
		try {
			return _pstat.execute();
		}
		catch (SQLException ex) {
			throwSQLException(ex);
			return false;
		}
	}

  public void addBatch() throws SQLException {
    _pstat.addBatch();
  }

  protected void setCharacterStream2(int parameterIndex, Reader reader,
                                         int length) throws SQLException {
    if ( (reader == null) || (length == 0)) {
      _pstat.setNull(parameterIndex, java.sql.Types.CLOB);
      return;
    }
    //length长度可能是很大一个值
    String str = getStrFromReader(reader);
    CharArrayReader r = new CharArrayReader(str.toCharArray());
    _pstat.setCharacterStream(parameterIndex, r, str.length());
  }

  protected String getStrFromReader(Reader rr) throws SQLException {
    return SqlFunc.reader2str(rr);
  }
  public void setCharacterStream(int parameterIndex, Reader reader, int length) throws
      SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + reader + "  " + length);
    if(reader!=null&&pconn.get_ds().needEncoding()){
      String ss = getStrFromReader(reader);
      ss = pconn.getEncodingStr(ss);
      reader = new CharArrayReader(ss.toCharArray());
      length = ss.length();
    }
    setCharacterStream2(parameterIndex, reader, length);
  }


  public void setRef(int i, Ref x) throws SQLException {
    _pstat.setRef(i, x);
  }

  public void setBlob(int i, Blob x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(i + "  " + x);
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_WARN))
//      pconn.logWarn("建议用 setBinaryStream(...) 写入Blob; ");
      pconn.logWarn(I18N.getString("com.esen.jdbc.pool.pooledpreparedstatement.comp1", "建议用 setBinaryStream(...) 写入Blob;"));
    _pstat.setBlob(i, x);
  }

  public void setClob(int i, Clob x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(i + "  " + x);
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_WARN))
//      pconn.logWarn("建议用 setCharacterStream(...) 写入Clob; ");
      pconn.logWarn(I18N.getString("com.esen.jdbc.pool.pooledpreparedstatement.comp2", "建议用 setCharacterStream(...) 写入Clob;"));
    _pstat.setClob(i, x);
  }

  public void setArray(int i, Array x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(i + "  " + x);
    _pstat.setArray(i, x);
  }

  public ResultSetMetaData getMetaData() throws SQLException {
    return _pstat.getMetaData();
  }

  public void setDate(int parameterIndex, Date x, Calendar cal) throws
      SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x + "  " + cal);
    _pstat.setDate(parameterIndex, x, cal);
  }

  public void setTime(int parameterIndex, Time x, Calendar cal) throws
      SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x + "  " + cal);
    _pstat.setTime(parameterIndex, x, cal);
  }

  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws
      SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x + "  " + cal);
    _pstat.setTimestamp(parameterIndex, x, cal);
  }

  public void setNull(int paramIndex, int sqlType, String typeName) throws
      SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug("setNull: " + paramIndex + "  " + sqlType + "  " +
                      typeName);
    _pstat.setNull(paramIndex, sqlType, typeName);
  }

  public void setURL(int parameterIndex, URL x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    _pstat.setURL(parameterIndex, x);
  }

  /**
   * 实现获取insert,update参数的类型
   */
  public ParameterMetaData getParameterMetaData() throws SQLException {
    if(parameterMetaData==null){
      parameterMetaData = createParameterMetaData();
    }
    return parameterMetaData;
  }
  /**
   * insert into tbname (field1,field2)valuse(?,?)
   * insert into tbname values(?,?)
   * update tbname set field1=?,field2=? where field3=? and field4=?
   * @return
   * @throws SQLException 
   */
  private ParameterMetaData createParameterMetaData() throws SQLException {
    if(sql==null||sql.length()==0)
      return null;
    if(!SqlFunc.isInsertInto(sql)&&!SqlFunc.isUpdate(sql)) {
        /*
         * BUG:ESENFACE-1028: modify by liujin 2014.06.16
         * 添加根据  ParameterMetaData 获取所需的参数元信息的方法。
         * 避免 select 等类型的 SQL 语句无法获取参数元信息
         */
    	if (_pstat == null) {
    		return null;
    	}
    	try {
	    	ParameterMetaData pmd = _pstat.getParameterMetaData();
	    	if (pmd == null) {
	    	  return null;
	    	}
	    	MyParameterMetaData mpmd = new MyParameterMetaData(pmd);
	    	return mpmd;
    	} catch (SQLException e) {
    		//有的数据库不支持，不向外抛出异常
    		return null;
    	}
    }
    String ss = SqlFunc.getSelectSql(sql)+" where 1=2";
    Connection conn = pconn.getSourceConnection();
    Statement stat = null;
    try{
      stat = conn.createStatement();
      ResultSet rs = stat.executeQuery(ss);
      try{
        ResultSetMetaData md = rs.getMetaData();
        MyParameterMetaData pmd = new MyParameterMetaData(md);
        return pmd;
      }finally{
        rs.close();
      }
      
    }finally{
      if(stat!=null)
        stat.close();
    }
  }
 
  public void close() throws SQLException {
    //如果有大字段,关闭大字段的缓存流
    closeCachedDiskFiles();
    /**
     * 20100128
     * 父类方法会调用此方法，避免重复调用，这里注释掉；
     * 原因是：Sybase数据库会出现异常："java.sql.SQLException: JZ0S2: Statement 对象已经关闭。" 
     */
    //pconn.closePooledStatement(this);
    /**
     * 20090623
     * 由于所有的statemen实现类都继承preparedStatement;因此Statement实现类的close方法被重载；
     * 这里也调用下父类方法，来关闭statemen接口；
     */
    super.close();
  }


  class MyParameterMetaData implements ParameterMetaData{
    private int pcount;
    /**
      * 20090710
      * 对于定长的int类型集合，采用int数组比较好；
      */
    private int[] pmds;
    public MyParameterMetaData(ResultSetMetaData md) throws SQLException{
      pcount = md.getColumnCount();
      pmds = new int[pcount];
      for(int i=0;i<pcount;i++){
        int t = md.getColumnType(i+1);
        pmds[i]=t;
      }
    }
    
    /*
     * BUG:ESENFACE-1028: modify by liujin 2014.06.16
     * 添加根据  ParameterMetaData 获取所需的参数元信息的方法。
     */
    public MyParameterMetaData(ParameterMetaData pmd) throws SQLException{
        pcount = pmd.getParameterCount();
        pmds = new int[pcount];
        for(int i=0;i<pcount;i++){
          int t = pmd.getParameterType(i+1);
          pmds[i]=t;
        }
      }
    
    public String getParameterClassName(int param) throws SQLException {
      return null;
    }

    public int getParameterCount() throws SQLException {
      return pcount;
    }

    public int getParameterMode(int param) throws SQLException {
      return 0;
    }
    /**
     * 目前只需要实现这个方法；
     * param从1开始
     */
    public int getParameterType(int param) throws SQLException {
      return  pmds[param-1];
    }

    public String getParameterTypeName(int param) throws SQLException {
      return null;
    }

    public int getPrecision(int param) throws SQLException {
      return 0;
    }

    public int getScale(int param) throws SQLException {
      return 0;
    }

    public int isNullable(int param) throws SQLException {
      return 0;
    }

    public boolean isSigned(int param) throws SQLException {
      return false;
    }
    
  }
}
