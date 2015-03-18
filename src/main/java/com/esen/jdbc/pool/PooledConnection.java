package com.esen.jdbc.pool;

import java.lang.ref.WeakReference;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.pool.impl.db2.DB2AS400PooledPreparedStatement;
import com.esen.jdbc.pool.impl.db2.DB2PooledPreparedStatement;
import com.esen.jdbc.pool.impl.db2.DB2PooledStatement;
import com.esen.jdbc.pool.impl.dm.DMPooledPreparedStatement;
import com.esen.jdbc.pool.impl.gbase.GBasePooledPreparedStatement;
import com.esen.jdbc.pool.impl.gbase.GBasePooledStatement;
import com.esen.jdbc.pool.impl.kingbasees.KingBaseESPooledPreparedStatement;
import com.esen.jdbc.pool.impl.kingbasees.KingBaseESPooledStatement;
import com.esen.jdbc.pool.impl.mssql.MssqlPooledPreparedStatement;
import com.esen.jdbc.pool.impl.mssql.MssqlPooledStatement;
import com.esen.jdbc.pool.impl.mysql.MysqlPooledPreparedStatement;
import com.esen.jdbc.pool.impl.mysql.MysqlPooledStatement;
import com.esen.jdbc.pool.impl.netezza.NetezzaPooledPreparedStatement;
import com.esen.jdbc.pool.impl.oracle.Oracle8PooledPreparedStatement;
import com.esen.jdbc.pool.impl.oracle.Oracle8PooledStatement;
import com.esen.jdbc.pool.impl.oracle.OraclePooledPreparedStatement;
import com.esen.jdbc.pool.impl.oracle.OraclePooledStatement;
import com.esen.jdbc.pool.impl.oscar.OscarPooledPreparedStatement;
import com.esen.jdbc.pool.impl.oscar.OscarPooledStatement;
import com.esen.jdbc.pool.impl.sybase.SybaseIQPooledPreparedStatement;
import com.esen.jdbc.pool.impl.sybase.SybaseIQPooledStatement;
import com.esen.jdbc.pool.impl.sybase.SybasePooledPreparedStatement;
import com.esen.jdbc.pool.impl.sybase.SybasePooledStatement;
import com.esen.jdbc.pool.impl.teradata.TeradataPooledPreparedStatement;
import com.esen.jdbc.pool.impl.teradata.TeradataPooledStatement;
import com.esen.jdbc.pool.impl.vertica.VerticaPooledPreparedStatement;
import com.esen.jdbc.pool.impl.vertica.VerticaPooledStatement;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

public class PooledConnection
    implements Connection {
  private DataSourceImpl _ds;
  protected  Connection conn;
  /**
   * 使用Map存储此连接生产的Statement，提高访问效率；
   * Statement,PreparedStatement,CalledStatement 都存储与这个Map，原来分三个list，Statement是基类，可以使用一个存储；
   */
  private HashMap statpool;
  private StringBuffer stacktrace4Connection;
  private boolean actived;//判断是否是活动的可用连接；
  
  /**
   * 标识此连接在调用close()时，是否真实关闭；
   * =true 真实关闭；
   * =false 返回连接池；
   * ConnectionFactory.getNewConnection()返回needRealClose=true的连接；
   */
  private boolean needRealClose;
  
  /**
   * 创建此连接的时间点；
   */
  private long createTime;
  
  /**
   * 最后获得此连接的时间点
   */
  private long lastGetTime;
 
  /**
   * 最后返回空闲池的时间点；
   */
  private long lastReturnToFreePoolTime;
  
  /**
   * 此连接的弱引用，此处定义此field，是为了避免每次getConnection时都创建一个新的WeakReference对象
   * 使用此弱连接是为了实现：如果此连接使用完毕没有调用close方法，被垃圾回收时，给出信息提示，用于在debug状态，找到没有关闭此连接的地方；
   */
  private WeakReference weakref;
  
  /**
   * 此连接对象的唯一标识；
   * 用于将此对象存储于活动池Map的key,提高读取效率；
   */
  private Object uniqueCode;
  
  /**
   * 记录此连接在同一个线程中获取的次数；
   * 同一个线程中调用获取连接，计数+1，关闭连接，计数-1；
   */
  private int callFrequencyForSameThread;
  
  /**
	 * 最初调用的线程HASH值,用于比较关闭调用线程是否为同一线程
	 */
  private int callThreadHashCode;
  
  /**
   * 该连接是否由当前线程创建
   */
  private boolean createdBySameThread = true;

  public PooledConnection(DataSourceImpl ds, Connection con, long connectionIndex) throws SQLException {
    _ds = ds;
    this.conn = con;
    statpool = new HashMap();
    createTime = System.currentTimeMillis();
    weakref = new WeakReference(this);
    /**
     * 使用连续生成的序号做唯一标识代码；
     */
    uniqueCode = new Long(connectionIndex);
  }
  
  protected int getCallFrequencyForSameThread(){
	  return callFrequencyForSameThread;
  }
  
  /**
   * 同一个线程每次嵌套获取，调用此方法使计数+1
   */
  protected void callFrequencyForSameThread(){
	  callFrequencyForSameThread++;
	  lastGetTime = System.currentTimeMillis();
  }
  
  public Connection getSourceConnection(){
    return conn;
  }
  
  protected JdbcLogger getLogger(){
    return _ds.jlog;
  }
  
  /**
   * 返回连接创建时间；
   * @return
   */
  public long getCreateTime(){
    return  createTime;
  }
  
  protected void setActive(boolean active){
    this.actived = active;
    if(this.actived) {
    	callThreadHashCode =  Thread.currentThread().hashCode();
    	lastGetTime = System.currentTimeMillis();
    }
    if (!active) lastReturnToFreePoolTime = System.currentTimeMillis();
  }
  
  /**
   * 修改创建该连接的线程的信息
   */
  public void changeCallThread() {
	  createdBySameThread = false;
  }
  
  /**
   * 判断此连接是否被“关闭”，返回空闲池；
   * @return
   */
  public boolean isActive(){
    return actived;
  }
  
  protected long getLastReturnToFreePoolTime(){
    return lastReturnToFreePoolTime;
  }

  /**
   * 获取最近一次开始使用的时间
   * @return
   */
  public long getLastGetTime() {
	  return lastGetTime;
  }
  
  protected void setNeedRealClose(boolean f){
    this.needRealClose = f;
  }
  
  /**
   * 判断此连接是否已被放回空闲池；
   * @throws SQLException
   */
  protected void checkActive() throws SQLException{
    if(!actived){
    	throw new SQLException(I18N.getString("com.esen.jdbc.pool.pooledconnection.connclosed", "此连接已被关闭；"));
    }
    
    if (!isSameThread()) {
    	throw new SQLException(I18N.getString("com.esen.jdbc.pool.pooledconnection.connclosedbyotherthread", "此连接已被其他线程关闭；"));
    }
  }
  
  /**
   * 检查当前线程是否与调用该连接的线程相同
   * 
   * @return 相同返回 true，不相同返回 false
   */
  protected boolean isSameThread() {
	  if (createdBySameThread) {
		  return Thread.currentThread().hashCode() == callThreadHashCode;
	  } else {
		  return true;
	  }
  }

	/**
	 * 记录调用堆栈；
	 * 当一个线程获取了连接，却没有调用close()“关闭”，java垃圾回收站回收时，会提示信息；
	 * 如果是warn日志级别，会记录此连接的调用堆栈，并显示在提示信息中，否则会建议设置日志级别为warn，找出此连接；
	 */
	protected void recordStacktrace() {
		PooledSQLException exception = new PooledSQLException();
		if (stacktrace4Connection == null) {
			stacktrace4Connection = new StringBuffer(1024*4);
		}
//		stacktrace4Connection.append(StrFunc.exception2str(exception, "获得调用堆栈：\r\n"));
		stacktrace4Connection.append(StrFunc.exception2str(exception,I18N.getString("com.esen.jdbc.pool.pooledconnection.getstack", "获得调用堆栈：\r\n") ));
	}

  public void logWarn(String str) {
    _ds.jlog.warn(str);
  }

  public void logError(Throwable e, String msg) {
    _ds.jlog.error(StrFunc.exception2str(e, msg));
  }

  /**
   * 如果是调试状态，记录执行的Sql语句
   * @param sql String
   */
  public void logDebug(String sql) {
    /**
     *当用ibatis时，sql中有很多tab和空格，此处去掉他们
     *这个做法不是最可靠的，它也可能吧sql中的字符串常量给替换掉。 
     */
//    _ds.jlog.debug("执行SQL： " + sql.replaceAll("\\t+"," "));
    _ds.jlog.debug(I18N.getString("com.esen.jdbc.pool.pooledconnection.comp1", "执行SQL： ") + sql.replaceAll("\\t+"," "));
  }
  
  /**
   * 判断是否允许记录指定级次的日志；
   * @param lev
   * @return
   */
  public boolean canLogLevel(int lev){
    return _ds.jlog.canLogLevel(lev);
  }
  public DataBaseInfo getDbType() throws SQLException {
    return _ds.getDbType();
  }
  public Statement createStatement() throws SQLException {
    checkActive();
    PooledStatement stat = createPooledStatement(getDbType(), this, conn.createStatement());
    statpool.put(stat,stat);
    return stat;
  }
  
  private PooledStatement createPooledStatement(DataBaseInfo db, PooledConnection pconn,
      Statement stat) {
    int t = db.getDbtype();
    switch (t) {
      case SqlConst.DB_TYPE_ORACLE:{
        /**
         * 20090805 
         * 将Oracle8单独处理；原因是大字段的读取；
         * Oracle9i,10g已经可以使用常规方法读取和写入大字段了；
         * 这样更改后，第三方连接池可以任意的使用；
         */
        if(db.isOracle8i()){
          return new Oracle8PooledStatement(pconn,stat);
        }
        return new OraclePooledStatement(pconn,stat);
      }
      case SqlConst.DB_TYPE_DB2:
        return new DB2PooledStatement(pconn,stat);
      case SqlConst.DB_TYPE_MYSQL:
        return new MysqlPooledStatement(pconn,stat);
      case SqlConst.DB_TYPE_MSSQL:
        return new MssqlPooledStatement(pconn,stat);
      case SqlConst.DB_TYPE_SYBASE:
        return new SybasePooledStatement(pconn,stat);
      case SqlConst.DB_TYPE_OSCAR:
        return new OscarPooledStatement(pconn,stat);
      case SqlConst.DB_TYPE_SYBASE_IQ:
        return new SybaseIQPooledStatement(pconn,stat);
      case SqlConst.DB_TYPE_KINGBASE_ES:
        return new KingBaseESPooledStatement(pconn,stat);
      case SqlConst.DB_TYPE_GBASE:
        return new GBasePooledStatement(pconn,stat);
      case SqlConst.DB_TYPE_TERADATA:
        return new TeradataPooledStatement(pconn, stat);
      case SqlConst.DB_TYPE_VERTICA:
        return new VerticaPooledStatement(pconn, stat);
      default:
          return new PooledPreparedStatement(pconn,stat);
    }
  }

  public String getEncodingStr(String sql){
    return _ds.getEncodingStr(sql);
  }
  public String getGBKEncodingStr(String sql){
    return _ds.getGBKEncodingStr(sql);
  }
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    checkActive();
    if(canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      logDebug(sql);
    sql = getEncodingStr(sql);
    PooledPreparedStatement pstat = createPooledPreparedStatement(
        getDbType(), this, conn.prepareStatement(sql),sql);
    statpool.put(pstat,pstat);
    return pstat;
  }

  private PooledPreparedStatement createPooledPreparedStatement(DataBaseInfo db, PooledConnection pconn,
      PreparedStatement pstat, String sql) {
    int t = db.getDbtype();
    switch (t) {
      case SqlConst.DB_TYPE_ORACLE:{
        /**
         * 20090805 
         * 将Oracle8单独处理；原因是大字段的读取；
         * Oracle9i,10g已经可以使用常规方法读取和写入大字段了；
         * 这样更改后，第三方连接池可以任意的使用；
         */
        if(db.isOracle8i()){
          return new Oracle8PooledPreparedStatement(pconn,pstat,sql);
        }
        return new OraclePooledPreparedStatement(pconn,pstat,sql);
      }
      case SqlConst.DB_TYPE_DB2:
      	if(db.isDB2ForAS400()) {
      		return new DB2AS400PooledPreparedStatement(pconn,pstat,sql);
      	}
        return new DB2PooledPreparedStatement(pconn,pstat,sql);
      case SqlConst.DB_TYPE_MYSQL:
        return new MysqlPooledPreparedStatement(pconn,pstat,sql);
      case SqlConst.DB_TYPE_MSSQL:
        return new MssqlPooledPreparedStatement(pconn,pstat,sql);
      case SqlConst.DB_TYPE_SYBASE:
        return new SybasePooledPreparedStatement(pconn,pstat,sql);
      case SqlConst.DB_TYPE_SYBASE_IQ:
        return new SybaseIQPooledPreparedStatement(pconn,pstat,sql);
      case SqlConst.DB_TYPE_OSCAR:
        return new OscarPooledPreparedStatement(pconn,pstat,sql);
      case SqlConst.DB_TYPE_KINGBASE_ES:
        return new KingBaseESPooledPreparedStatement(pconn,pstat,sql);
      case SqlConst.DB_TYPE_GBASE:
        return new GBasePooledPreparedStatement(pconn,pstat,sql);
      case SqlConst.DB_TYPE_NETEZZA:
          return new NetezzaPooledPreparedStatement(pconn,pstat,sql);
      case SqlConst.DB_TYPE_DM:
          return new DMPooledPreparedStatement(pconn, pstat, sql);
      case SqlConst.DB_TYPE_TERADATA:
          return new TeradataPooledPreparedStatement(pconn,pstat,sql);
      case SqlConst.DB_TYPE_VERTICA:
          return new VerticaPooledPreparedStatement(pconn,pstat,sql);
      default:
        return new PooledPreparedStatement(pconn,pstat,sql);
    }
  }

  public CallableStatement prepareCall(String sql) throws SQLException {
    checkActive();
    if(canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      logDebug(sql);
    sql = getEncodingStr(sql);
    PooledCallableStatement cstat = new PooledCallableStatement(this,
        conn.prepareCall(sql));
    statpool.put(cstat,cstat);
    return cstat;
  }

  public String nativeSQL(String sql) throws SQLException {
    checkActive();
    sql = getEncodingStr(sql);
    return conn.nativeSQL(sql);
  }

  public void setAutoCommit(boolean autoCommit) throws SQLException {
    checkActive();
    //避免重复设置相同的值；
    if(conn.getAutoCommit()!=autoCommit)
      conn.setAutoCommit(autoCommit);
  }

  public boolean getAutoCommit() throws SQLException {
    checkActive();
    return conn.getAutoCommit();
  }

  public void commit() throws SQLException {
    checkActive();
    if(!conn.getAutoCommit()){
      conn.commit();
    }
  }

  public void rollback() throws SQLException {
    checkActive();
		try {
			if (!conn.getAutoCommit()) {
				conn.rollback();
			}
		} catch (SQLException se) {
			/**
			 * 这里将rollback异常捕获，以便在处理事务时的异常能够正常捕获；
			 * 例：
			 * try{
			 *   ...
			 * }catch(Exception ex){
			 *   conn.rollback();//如果这里出异常，则事务异常ex就捕获不到；
			 * }
			 */
			se.printStackTrace();
		}
  }

  /**
	 * 将连接放回空闲池，没有真正关闭；
	 * 
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		//checkActive();
	    if (!isSameThread()) {
	    	throw new SQLException(I18N.getString("com.esen.jdbc.pool.pooledconnection.connclosedbyotherthread", "此连接已被其他线程关闭；"));
	    }
	    
		close(false);
	}
	
	/**
	 * 关闭连接
	 * 
	 * @param isForce 是否强制关闭连接， true表示是， false 表示否
	 * @throws SQLException
	 */
	private void close(boolean isForce) throws SQLException {
		/**
		 * 其他线程试图关闭另一线程的连接,一般情况下是不会发生的, 但是一旦发生了将会产生连锁性反应
		 * 会出现大量的线程被关闭异常
		 */
		if(!isSameThread() && !isForce) {
			throw new RuntimeException(I18N.getString("com.esen.jdbc.pool.pooledconnection.closingbyother", "其他线程试图关闭另一线程的连接!"));
		}
		if (needRealClose) {
			realclose();
			return;
		}

		if (!actived)
			return;
		
		/**
		 * 对于嵌套获取的链接，需要配对关闭，关闭时计数-1
		 * 对计数的操作，需要同步；
		 */
		if (!isForce) {
			synchronized (_ds) {
				if (callFrequencyForSameThread > 0) {
					callFrequencyForSameThread--;
					return;
				}
			}
		}
		
		//如果执行有异常，finally调用这里的close方法，有些数据库jdbc此时已经关闭了链接，
		//所以判断下再commit
		if (!conn.isClosed()) {
			boolean autoCommit = _ds.getDefaultAutoCommit();
			
			if (!isForce) {
				commit();
				//还原事务状态
				setAutoCommit(autoCommit);
			} else if (this.actived) {
				if (!conn.getAutoCommit()) {
					conn.commit();
				}
				if(conn.getAutoCommit()!= autoCommit) {
					conn.setAutoCommit(autoCommit);
				}
			}
		}
		/**
		 * 旧程序中如果是debug状态，每次获取此连接都要记录获取堆栈，“关闭”时却没有清空stacktrace4Connection,
		 * 时间长了，stacktrace4Connection非常大，浪费内存；
		 * 这是2.0里面一个严重的bug；
		 * 这里必须将其清空；
		 */
		this.stacktrace4Connection = null;
		release();
		_ds.closeConnection(this);
	}
	
	/**
	 * 强制关闭连接
	 * 
	 * @throws SQLException
	 */
	protected void forceClose() throws SQLException {
		close(true);
	}

  /**
   * 释放资源：statpool,preppool,callpool
   * 将没有关闭的statement关闭，并打印调用堆栈
   */
  synchronized private void release() throws SQLException {
    release(statpool,"Statement");
    
  }
  
  private void release(HashMap map, String info) throws SQLException {
    if (map != null && !map.isEmpty()) {
      try {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry en = (Map.Entry) it.next();
          PooledStatement stat = (PooledStatement) en.getValue();
          if (stat != null) {
            stat.realclose();
            if (canLogLevel(JdbcLogger.LOG_LEVER_WARN)){
              //记录创建此stat的堆栈；
//              logWarn(info + "没有关闭： " + stat.getRecordStacktrace());
              logWarn(info + I18N.getString("com.esen.jdbc.pool.pooledconnection.comp2", "没有关闭： ") + stat.getRecordStacktrace());
              //获取调用此方法的堆栈；
              logWarn(StrFunc.exception2str(new SQLException(), null));
            }
          }
          it.remove();
        }
      }
      finally {
        map.clear();
      }
    }
  }

  /**
   * 关闭 Statement
   * @param pstat PooledStatement
   * @throws SQLException
   */
  synchronized protected void closePooledStatement(PooledStatement pstat) throws
      SQLException {
    pstat.realclose();
    statpool.remove(pstat);
  }

  /**
   * 关闭物理连接，释放资源；
   * @throws SQLException
   */
  protected void realclose() throws SQLException {
    /**
     * 20091118
     * 关闭物理连接；
     * 原来的程序没有释放相关资源；
     */
    this.stacktrace4Connection = null;
    release();
    if (conn != null) {
      conn.close();
    }
  }
  /**
   * 判断连接有没有被真正关闭；
   * @throws SQLException
   * @return boolean
   */
  public boolean isClosed() throws SQLException {
    return conn.isClosed();
  }

  public DatabaseMetaData getMetaData() throws SQLException {
    checkActive();
    return conn.getMetaData();
  }

  public void setReadOnly(boolean readOnly) throws SQLException {
    checkActive();
    conn.setReadOnly(readOnly);
  }

  public boolean isReadOnly() throws SQLException {
    checkActive();
    return conn.isReadOnly();
  }

  public void setCatalog(String catalog) throws SQLException {
    checkActive();
    conn.setCatalog(catalog);
  }

  public String getCatalog() throws SQLException {
    checkActive();
    return conn.getCatalog();
  }

  public void setTransactionIsolation(int level) throws SQLException {
    checkActive();
    conn.setTransactionIsolation(level);
  }

  public int getTransactionIsolation() throws SQLException {
    checkActive();
    return conn.getTransactionIsolation();
  }

  public SQLWarning getWarnings() throws SQLException {
    checkActive();
    return conn.getWarnings();
  }

  public void clearWarnings() throws SQLException {
    checkActive();
    conn.clearWarnings();
  }

  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws
      SQLException {
    checkActive();
    PooledStatement stat = createPooledStatement(getDbType(), this,
        conn.createStatement(resultSetType,resultSetConcurrency));
    statpool.put(stat,stat);
    return stat;
  }

  public PreparedStatement prepareStatement(String sql, int resultSetType,
                                            int resultSetConcurrency) throws
      SQLException {
    checkActive();
    if(canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      logDebug(sql);
    sql = getEncodingStr(sql);
    PooledPreparedStatement pstat = createPooledPreparedStatement(
        getDbType(), this, conn.prepareStatement(sql, resultSetType, resultSetConcurrency),sql);
    statpool.put(pstat,pstat);
    return pstat;
  }

  public CallableStatement prepareCall(String sql, int resultSetType,
                                       int resultSetConcurrency) throws
      SQLException {
    checkActive();
    if(canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      logDebug(sql);
    sql = getEncodingStr(sql);
    PooledCallableStatement cstat = new PooledCallableStatement(this,
        conn.prepareCall(sql, resultSetType, resultSetConcurrency));
    statpool.put(cstat,cstat);
    return cstat;
  }

  public Map getTypeMap() throws SQLException {
    checkActive();
    return conn.getTypeMap();
  }

  public void setTypeMap(Map map) throws SQLException {
    checkActive();
    conn.setTypeMap(map);
  }

  public void setHoldability(int holdability) throws SQLException {
    checkActive();
    conn.setHoldability(holdability);
  }

  public int getHoldability() throws SQLException {
    checkActive();
    return conn.getHoldability();
  }

  public Savepoint setSavepoint() throws SQLException {
    checkActive();
    return conn.setSavepoint();
  }

  public Savepoint setSavepoint(String name) throws SQLException {
    checkActive();
    return conn.setSavepoint(name);
  }

  public void rollback(Savepoint savepoint) throws SQLException {
    checkActive();
    conn.rollback(savepoint);
  }

  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    checkActive();
    conn.releaseSavepoint(savepoint);
  }

  public Statement createStatement(int resultSetType, int resultSetConcurrency,
                                   int resultSetHoldability) throws
      SQLException {
    checkActive();
    PooledStatement stat = createPooledStatement(getDbType(), this, 
        conn.createStatement(resultSetType,resultSetConcurrency, resultSetHoldability));
    statpool.put(stat,stat);
    return stat;
  }

  public PreparedStatement prepareStatement(String sql, int resultSetType,
                                            int resultSetConcurrency,
                                            int resultSetHoldability) throws
      SQLException {
    checkActive();
    if(canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      logDebug(sql);
    sql = getEncodingStr(sql);
    PooledPreparedStatement pstat = createPooledPreparedStatement(
        getDbType(), this, conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability),sql);
    statpool.put(pstat,pstat);
    return pstat;
  }

  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException {
    checkActive();
    if (canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      logDebug(sql);
    sql = getEncodingStr(sql);
    PooledCallableStatement cstat = new PooledCallableStatement(this, conn.prepareCall(sql, resultSetType,
        resultSetConcurrency, resultSetHoldability));
    statpool.put(cstat, cstat);
    return cstat;
  }

  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws
      SQLException {
    checkActive();
    if(canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      logDebug(sql);
    sql = getEncodingStr(sql);
    PooledPreparedStatement pstat = createPooledPreparedStatement(
        getDbType(), this, conn.prepareStatement(sql, autoGeneratedKeys),sql);
    statpool.put(pstat,pstat);
    return pstat;
  }

  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws
      SQLException {
    checkActive();
    if(canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      logDebug(sql);
    sql = getEncodingStr(sql);
    PooledPreparedStatement pstat = createPooledPreparedStatement(
        getDbType(), this, conn.prepareStatement(sql, columnIndexes),sql);
    statpool.put(pstat,pstat);
    return pstat;
  }

  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws
      SQLException {
    checkActive();
    if(canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      logDebug(sql);
    sql = getEncodingStr(sql);
    PooledPreparedStatement pstat = createPooledPreparedStatement(
        getDbType(), this, conn.prepareStatement(sql, columnNames),sql);
    statpool.put(pstat,pstat);
    return pstat;
  }

  /**
   * 重载垃圾回收站
   * 丢弃连接，并打印连接的调用堆栈
   * @throws Throwable
   */
  protected void finalize() throws Throwable {
    //已被真实关闭；
    if(isClosed()){
      return;
    }
    //如果连接池已经关闭，直接返回；
    if(_ds.isClosed())
      return;
    if(isActive()){
      /**
       * debuglog没有初始化，可能为空；
       */
      String info = stacktrace4Connection==null?null:stacktrace4Connection.toString();
      if(info==null||info.length()==0)
//        info = "请使用Warn状态调试，找出此连接；";
        info = I18N.getString("com.esen.jdbc.pool.pooledconnection.comp3", "请使用Warn状态调试，找出此连接；");
      if(canLogLevel(JdbcLogger.LOG_LEVER_FATAL))
//        _ds.jlog.fatal("连接没有关闭： " + info);
        _ds.jlog.fatal(I18N.getString("com.esen.jdbc.pool.pooledconnection.comp4", "连接没有关闭：") + info);
      close();
    }

  }
  
  public final DataSourceImpl get_ds() {
    return _ds;
  }
  
  public WeakReference getWeakReference() {
    return this.weakref;
  }
  
  public Object getUniqueCode(){
    return uniqueCode;
  }
  
  public String getCallStackTrace() {
  	return stacktrace4Connection==null ? null : stacktrace4Connection.toString();
  }
}
