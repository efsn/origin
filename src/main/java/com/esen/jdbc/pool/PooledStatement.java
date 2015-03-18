package com.esen.jdbc.pool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

public class PooledStatement implements Statement {
  protected Statement _stat;
  protected PooledConnection pconn;
  private StringBuffer callStack4getConnection;
  
  protected PooledStatement(PooledConnection conn,Statement stat) {
    _stat = stat;
    pconn = conn;
    recordStacktrace();
  }

	/**
	 * 记录调用堆栈
	 */
	private void recordStacktrace() {
		if (pconn.get_ds().isDebugStackLog()) {
			Exception e = new Exception();
			if (callStack4getConnection == null) {
				callStack4getConnection = new StringBuffer(1024*4);
			}
//			callStack4getConnection.append(StrFunc.exception2str(e, "获得调用堆栈：\r\n"));
			callStack4getConnection.append(StrFunc.exception2str(e,I18N.getString("com.esen.jdbc.pool.pooledstatement.getstack", "获得调用堆栈：\r\n") ));
		}
	}
  
  protected StringBuffer getRecordStacktrace(){
    return callStack4getConnection;
  }
  
  public ResultSet executeQuery(String sql) throws SQLException {
    ResultSet rs = getQureyResultSet(sql);
    return new PooledResultSet(rs,pconn);
  }
  
  protected ResultSet getQureyResultSet(String sql) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(sql);
    sql = pconn.getEncodingStr(sql);
    ResultSet rs = null;
    try{
      rs = _stat.executeQuery(sql);
    }catch(SQLException ex){
      throwSQLException(ex);
    }
    return rs;
  }

	protected void throwSQLException(SQLException ex) throws SQLException {
		throw PooledSQLExceptionFactory.getInstance(pconn.getDbType(), ex);
		/**
		 * 20090807
		 * 执行sql出异常，直接抛出数据库原始异常；
		 * 原来的程序包装了，内容太多；
		 * 
		 * 执行sql的异常有外部调用程序处理，jdbc里面就不记录了；
		 * 原因是有些程序需要通过异常去控制，这时将异常记录就不合适；
		 */
		//if (pconn.canLogLevel(JdbcLogger.LOG_LEVER_ERROR))
		//	pconn.logError(pse, null);
	}
  
  protected  ResultSet getLimitQeury(String sql, ResultSet rs) throws SQLException {
    int startindex = getStartIndex(sql);
    if(startindex>0){
      if(rs.getType()==ResultSet.TYPE_FORWARD_ONLY){
        while(startindex-->0){
          rs.next();
        }
      }else{
        rs.absolute(startindex);
      }
    }
    return rs;
  }
  /**
   * sybaseiq,mssql都不支持分页，但是可以支持求前多少行；
   * 现在在分页sql前加sql注释，来记录分页从哪里开始
   * @param sql
   * @return
   */
  public static int getStartIndex(String sql){
    // sql="/*STARTINDEX:20*/set rowcount 30 select ..."
    // 表示从20行开始取后面的行数
  	/**
  	 * 这里的正则表达式很明显是错误的, 原来为^[\\s]*打头
  	 * 没有搞懂为何要进行取反操作, 经过测试这个表达式不能匹配任意合法分页语句
  	 * 参见 IRPT-8387
  	 */
    String regx = "[\\s]*(/\\*STARTINDEX:\\d+\\*/)";
    Matcher mat = Pattern.compile(regx).matcher(sql);
    if(mat.find()){
      String str = mat.group(1);
      int p = str.indexOf(':');
      return Integer.parseInt(str.substring(p+1,str.length()-2));
    }
    return -1;
  }
  public int executeUpdate(String sql) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(sql);
    sql = pconn.getEncodingStr(sql);
    try{
    return _stat.executeUpdate(sql);
    }catch(SQLException ex){
      throwSQLException(ex);
      return -1;
    }
  }

  protected void realclose() throws SQLException {
    _stat.close();
  }
  public void close() throws SQLException {
    pconn.closePooledStatement(this);
  }
  public int getMaxFieldSize() throws SQLException {
    return _stat.getMaxFieldSize();
  }
  public void setMaxFieldSize(int max) throws SQLException {
    _stat.setMaxFieldSize(max);
  }
  public int getMaxRows() throws SQLException {
    return _stat.getMaxRows();
  }
  public void setMaxRows(int max) throws SQLException {
    _stat.setMaxRows(max);
  }
  public void setEscapeProcessing(boolean enable) throws SQLException {
    _stat.setEscapeProcessing(enable);
  }
  public int getQueryTimeout() throws SQLException {
    return _stat.getQueryTimeout();
  }
  public void setQueryTimeout(int seconds) throws SQLException {
    _stat.setQueryTimeout(seconds);
  }
  public void cancel() throws SQLException {
    _stat.cancel();
  }
  public SQLWarning getWarnings() throws SQLException {
    return _stat.getWarnings();
  }
  public void clearWarnings() throws SQLException {
    _stat.clearWarnings();
  }
  public void setCursorName(String name) throws SQLException {
    _stat.setCursorName(name);
  }
  public boolean execute(String sql) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(sql);
    sql = pconn.getEncodingStr(sql);
    try{
      return _stat.execute(sql);
    }catch(SQLException ex){
      throwSQLException(ex);
      return false;
    }
  }
  public ResultSet getResultSet() throws SQLException {
	  ResultSet _rs = _stat.getResultSet();
	  if(_rs==null){
		  return null;
	  }
    return new PooledResultSet(_rs,pconn);
  }
  public int getUpdateCount() throws SQLException {
    return _stat.getUpdateCount();
  }
  public boolean getMoreResults() throws SQLException {
    return _stat.getMoreResults();
  }
  public void setFetchDirection(int direction) throws SQLException {
    _stat.setFetchDirection(direction);
  }
  public int getFetchDirection() throws SQLException {
    return _stat.getFetchDirection();
  }
  public void setFetchSize(int rows) throws SQLException {
    _stat.setFetchSize(rows);
  }
  public int getFetchSize() throws SQLException {
    return _stat.getFetchSize();
  }
  public int getResultSetConcurrency() throws SQLException {
    return _stat.getResultSetConcurrency();
  }
  public int getResultSetType() throws SQLException {
    return _stat.getResultSetType();
  }
  public void addBatch(String sql) throws SQLException {
    sql = pconn.getEncodingStr(sql);
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(sql);
    _stat.addBatch(sql);
  }
  public void clearBatch() throws SQLException {
    _stat.clearBatch();
  }

	public int[] executeBatch() throws SQLException {
		try {
			return _stat.executeBatch();
		}
		catch (SQLException ex) {
			throwSQLException(ex);
			return null;
		}
	}
	
  public Connection getConnection() throws SQLException {
    return pconn;
  }
  public boolean getMoreResults(int current) throws SQLException {
    return _stat.getMoreResults(current);
  }
  public ResultSet getGeneratedKeys() throws SQLException {
    return new PooledResultSet(_stat.getGeneratedKeys(),pconn);
  }
  public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(sql);
    sql = pconn.getEncodingStr(sql);
    return _stat.executeUpdate(sql,autoGeneratedKeys);
  }
  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(sql);
    sql = pconn.getEncodingStr(sql);
    return _stat.executeUpdate(sql,columnIndexes);
  }
  public int executeUpdate(String sql, String[] columnNames) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(sql);
    sql = pconn.getEncodingStr(sql);
    return _stat.executeUpdate(sql,columnNames);
  }
  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(sql);
    sql = pconn.getEncodingStr(sql);
    return _stat.execute(sql,autoGeneratedKeys);
  }
  public boolean execute(String sql, int[] columnIndexes) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(sql);
    sql = pconn.getEncodingStr(sql);
    return _stat.execute(sql,columnIndexes);
  }
  public boolean execute(String sql, String[] columnNames) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(sql);
    sql = pconn.getEncodingStr(sql);
    return _stat.execute(sql,columnNames);
  }
  public int getResultSetHoldability() throws SQLException {
    return _stat.getResultSetHoldability();
  }
}
