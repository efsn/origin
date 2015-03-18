package com.esen.jdbc.pool;

import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.pool.impl.gbase.Gbase8tPooledConnection;
import com.esen.jdbc.pool.impl.gbase.GbasePooledConnection;
import com.esen.jdbc.pool.impl.sybase.SybasePooledConnection;
import com.esen.thread.IdleThread;
import com.esen.util.ExceptionHandler;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

//import org.apache.log4j.PropertyConfigurator;
import net.sf.log4jdbc.ConnectionSpy;

/**
 * 连接池的实现类；
 * @author dw
 *
 */
public class DataSourceImpl implements DataSource {
	private String poolname;//连接池名字；

	private boolean defaultAutoCommit = DefaultPoolParam.DEFAULT_AUTOCOMMIT;

	/**
	 * 20111108 对常用的连接池对象属性的访问，放开synchronized锁，改用volatile修饰。
	 * 原因是：BI@Report启动后，会在另一个线程同步检查连接池管理中所有连接池的可用性，如果其中有些连接池是不可用的，访问会特别慢，
	 * 这时如果访问连接池管理界面，会等待好久，直到连接池的检查完成。
	 * 查看正在运行的线程堆栈，发现问题出现在：
	 * Thread: Thread[http-8088-Processor15,5,main]
	 * 	com.esen.jdbc.pool.datasourceimpl.getDriverClassName(DataSourceImpl.java:581)
	 * 	com.esen.bi.jsp.action.sysmgr.jdbcpool.ActionJdbcPool.listJdbc(ActionJdbcPool.java:514)
	 * 	com.esen.bi.jsp.action.sysmgr.jdbcpool.ActionJdbcPool.execute(ActionJdbcPool.java:73)
	 * 总是在访问连接池的属性，这个地方等待。
	 * 所以现在对几个经常需要访问的属性放开同步锁，改用volatile保证同步。
	 * 
	 */
	private volatile String driverClassName = null;

	private volatile String url = null;

	private volatile String username = null;

	private String password = null;

	private String catalog = null;

	/**
	 * 最大连接数据库连接数
	 */
	private int maxActive = DefaultPoolParam.DEFAULT_MAX_ACTIVE;

	/**
	 * 最小等待连接中的数量
	 */
	private int minIdle = DefaultPoolParam.DEFAULT_MIN_IDLE;

	/**
	 * 最大等待时间
	 */
	private long maxWait = DefaultPoolParam.DEFAULT_MAX_WAIT;

	/**
	 * 连接最大闲置时间，默认一小时；
	 * 单位毫秒；
	 */
	private long maxIdleTime = DefaultPoolParam.DEFAULT_MAX_IDLE_TIME;

	/**
	 * 调试状态，兼容以前的参数；
	 * isDeBug=ture  表示 LOG_LEVER_WARN 级次；
	 *        =false 表示 LOG_LEVER_ERROR 级次；
	 *  @deprecated
	 */
	private boolean isdebug = false;

	/**
	 * 空闲连接池
	 */
	protected List freepool = null;

	/**
	 * 活动连接池
	 * 改为HashMap存储，提高读取效率，原来使用List，关闭某个连接时，需要遍历整个List;
	 * key是PooledConnection的getUniqueCode返回的值；
	 * value是对PooledConnection的一个弱引用；
	 * 记录此map是为了便于管理处于活动的连接，和freepool配套使用；
	 * 获取连接，将freepool中的连接取出，放入activepool，使用完毕调用close方法将连接返回freepool；
	 * 不同的是，activepool存的是连接的弱引用，用于侦测那些没有使用close方法的连接；
	 */
	protected HashMap activepool = null;

	/**
	 * 记录日志，日志分级；
	 */
	protected JdbcLogger jlog;
	
	/**
	 * 记录SQL语句日志，日志文件名
	 */
	protected String sqllogfile = "";

	/**
	 * 第三方连接池DataSource
	 */
	private DataSource ds3;

	/**
	 * 第三方DataSource Context naming
	 */
	private String datasource;

	/**
	 * 数据库类型: Oracle,Mysql,DB2,Sybase,Mssql,Other
	 */
	private volatile DataBaseInfo dbinfo;

	/**
	 * 设置数据库字符集，目前只用于Oracle
	 */
	private String characterEncoding = null;

	/**
	 * 目的字符集，将从数据库读出的字符，转成指定的字符集
	 */
	private String destCharSetEncoding = "GBK";

	/**
	 * 是否需要转换字符集
	 */
	private boolean needEncoding = false;

	/**
	 * 用于同一个线程在获取连接时，总返回同一个连接对象；
	 */
	private ThreadLocal threadLocal;

	private String initsql;

	/**
	 * 定时清除空闲池中闲置时间过长，持有时间过长的连接；
	 */
	private Runnable freePoolRemoveTimer;

	/**
	 * 表示连接池刚创建成功，还未初始化
	 */
	protected static final int STATE_UNINITED = 0;

	/**
	 * 最后一次初始化连接池失败的时间点
	 */
	private long lastInitPoolFail;

	/**
	 * 最后一次初始化连接池失败的原因
	 */
	private SQLException lastInitPoolException;

	/**
	 * 记录连接池开始初始化状态；
	 */
	protected static final int STATE_STARTING = 1;

	/**
	 * 记录连接池初始化完毕状态；
	 * 此状态还表示连接池正在运行；
	 */
	protected static final int STATE_STARTED = 2;

	/**
	 * 记录连接池开始关闭状态；
	 */
	protected static final int STATE_CLOSING = 3;

	/**
	 * 记录连接池已经关闭状态；
	 */
	protected static final int STATE_CLOSED = 4;

	/**
	 * 记录连接池的状态；
	 * 正在初始化，初始化完毕，正在关闭连接池，连接池已经关闭；
	 */
	private int state = STATE_UNINITED;

	/**
	 * 最后一次修改连接池的参数；
	 * 主要用于，当连接池的重要参数url,driverclassname,username,password等时，正在使用的链接用此参数判断是否直接物理关闭；
	 * 如果链接的创建时间小于等于这个时间参数，则链接关闭时直接物理关闭，不返回空闲池；
	 */
	private long lastModifiedPropsTime;

	/**
	 * 记录流水号，给每个获取的连接一个唯一的序号，是不断增长的；
	 */
	private long connectionIndex;

	/**
	 * 通过参数设置默认的schema值；
	 */
	private String default_schema;

	/**
	 * 无连接可用时，是否关闭使用时间最长的连接
	 */
	private boolean isCloseLongTimeConn = false;
	
	/**
	 * 可以强制关闭的连接的最小使用时间，默认一小时；
	 * 单位毫秒；
	 */
	private long minUseTime = DefaultPoolParam.DEFAULT_MIN_USE_TIME;
	
	/**
	 * 连接池描述信息
	 */
	private String desc = "";
			
	public DataSourceImpl() {
		jlog = new JdbcLogger();
		/**
		 * 默认级次设置为：Error；
		 */
		jlog.setLogLever(JdbcLogger.LOG_LEVER_ERROR);
	}

	/**
	 * 设置DataSource参数
	 * @param props Properties
	 * <pre>
	 * "defaultAutoCommit"  是否自动提交
	 * "driverClassName" 数据库驱动类名称；
	 * "url"    数据库连接url；
	 * "username"  数据库用户名；
	 * "password"  对应用户名密码；
	 * "maxActive" 最大连接数，默认15；
	 * "minIdle"   最小等待连接中的数量，默认5；
	 * "maxWait"   最大等待时间（long）默认10000；
	 * "maxIdleTime"  连接最大闲置时间,单位：毫秒
	 * "isDebug"   是否调试状态(true,false)，默认false；
	 * "datasource"   第三方连接池名称；
	 * "catalog"      数据库目录
	 * "maxIdleTime"  连接最大闲置时间,单位：毫秒
	 * "logLevel"     设置日志级别，DEBUG,INFO,WARN,ERROR,FATAL 
	 * "sqlLogFile"   设置SQL日志文件名，默认：esen_jdbc.log
	 * "characterEncoding"    写入数据库前，转换编码；
	 * "destCharSetEncoding"  读出数据库后，转换编码；
	 *                这两个编码参数，一般的数据库都不需要，其编码可以在url中设置；
	 *                主要用于Oracle的非中文字符集，比如英文字符集，写入前转换成Oracle字符集对应的characterEncoding编码（iso8859_1），
	 *                读取后，转换成destCharSetEncoding指定的编码（GBK）；
	 *                
	 * 用此方进行更改参数值，可以值设置部分属性值；
	 * 例：
	 * 创建连接池：
	 * BaseDataSource baseDataSource = new BaseDataSource();
	 * Properties props = new Properties();
	 * props.setProperty("url", "jdbc:oracle:thin:@192.168.1.102:1521:orcl");
	 * props.setProperty("driverClassName", "oracle.jdbc.driver.OracleDriver");
	 * props.setProperty("username", "test");
	 * props.setProperty("password", "test");
	 * props.setProperty("logLevel", "debug");
	 * props.setProperty("maxIdleTime", String.valueOf(10 * 1000));//设置最大闲置时间：10秒；
	 * baseDataSource.setProperties(props);
	 * 
	 * 现在希望更改用户名：
	 * props = new Properties();
	 * props.setProperty("username", "test2");
	 * props.setProperty("password", "test2");
	 * baseDataSource.setProperties(props);
	 * 
	 * </pre>
	 * 
	 * @throws SQLException
	 */
	public synchronized void setProperties(Properties props) {
		/**
		 * 此变量记录重要参数是否被修改，比如：url,driverClassName,username,password等；
		 * 如果被修改，此参数置为true；
		 * 如果reload==true 参数设置完毕后会调用relaod()方法；
		 */
		boolean reload = false;

		String v = props.getProperty(PoolPropName.PROP_DEFAULTAUTOCOMMIT);
		defaultAutoCommit = StrFunc.parseBoolean(v, DefaultPoolParam.DEFAULT_AUTOCOMMIT);

		v = props.getProperty(PoolPropName.PROP_DRIVERCLASSNAME);
		if (!StrFunc.isNull(v) && !StrFunc.compareStr(this.driverClassName, v)) {
			this.driverClassName = v;
			if (!reload)
				reload = true;
		}

		v = props.getProperty(PoolPropName.PROP_URL);
		if (!StrFunc.isNull(v) && !StrFunc.compareStr(this.url, v)) {
			this.url = v;
			if (!reload)
				reload = true;
		}

		v = props.getProperty(PoolPropName.PROP_CATALOG);
		if (!StrFunc.isNull(v) && !StrFunc.compareStr(this.catalog, v)) {
			this.catalog = v;
			if (!reload)
				reload = true;
		}

		v = props.getProperty(PoolPropName.PROP_USERNAME);
		if (!StrFunc.isNull(v) && !StrFunc.compareStr(this.username, v)) {
			this.username = v;
			if (!reload)
				reload = true;
		}

		v = props.getProperty(PoolPropName.PROP_PASSWORD);
		if (!StrFunc.isNull(v) && !StrFunc.compareStr(this.password, v)) {
			this.password = v;
			if (!reload)
				reload = true;
		}

		v = props.getProperty(PoolPropName.PROP_MAXACTIVE);
		if (!StrFunc.isNull(v)) {
			maxActive = StrFunc.str2int(v, DefaultPoolParam.DEFAULT_MAX_ACTIVE);
		}
		v = props.getProperty(PoolPropName.PROP_MINIDLE);
		if (!StrFunc.isNull(v)) {
			minIdle = StrFunc.str2int(v, DefaultPoolParam.DEFAULT_MIN_IDLE);
		}
		v = props.getProperty(PoolPropName.PROP_MAXWAIT);
		if (!StrFunc.isNull(v)) {
			maxWait = StrFunc.str2long(v, DefaultPoolParam.DEFAULT_MAX_WAIT);
		}
		/**
		 * 兼容2.0以前的参数；
		 */
		String debstr = props.getProperty(PoolPropName.PROP_ISDEBUG);
		if (!StrFunc.isNull(debstr)) {
			boolean isd = StrFunc.str2boolean(debstr);
			if (isd) {
				this.jlog.setLogLever(JdbcLogger.LOG_LEVER_WARN);
			}
			else {
				this.jlog.setLogLever(JdbcLogger.LOG_LEVER_ERROR);
			}
			this.isdebug = isd;
		}
		/**
		 * 如果设置了logLever，则以logLever为日志级次，2.1以后支持；
		 */
		String levstr = props.getProperty(PoolPropName.PROP_LOGLEVER);
		if (!StrFunc.isNull(levstr)) {
			setLogLever(levstr);
		if(!reload)
			reload = true;
		}
    
		String filename = props.getProperty(PoolPropName.PROP_SQLLOGFILE);
		if(!StrFunc.isNull(filename)){
			sqllogfile = filename;
			if(!reload)
				reload = true;
		}

		/**
		 * 对datasource和datasource3参数都支持，但是优先datasource参数；
		 */
		String ds3str = props.getProperty(PoolPropName.PROP_OTHERDATASOURCE);
		if (StrFunc.isNull(ds3str)) {
			ds3str = props.getProperty(PoolPropName.PROP_OTHERDATASOURCE3);
		}
		if (!StrFunc.isNull(ds3str) && !StrFunc.compareStr(ds3str, datasource)) {
			datasource = ds3str;
			if (!reload)
				reload = true;
		}

		/**
		 * 20090601
		 * 问题：对于Oracle的英文字符集数据库，初始化连接池时，读取判断了是否是英文字符集，
		 *      如果是则characterEncoding=iso8859_1 ,这时自动设置的，与外面传进来的参数无关；
		 *      导致，在更改连接池配置时，外面的参数characterEncoding并没有设置值，原来的程序将空值设置给characterEncoding，
		 *      所以更改后再次访问数据库，如果是英文的，由于characterEncoding=null没有做字符转换，出现乱码；
		 * 解决：重新更该配置时，如果设置空值，则保留原来的参数值；
		 */

		setCharacterEncoding(props.getProperty(PoolPropName.PROP_CHARACTERENCODING, characterEncoding));
		setDestCharSetEncoding(props.getProperty(PoolPropName.PROP_DESTCHARSETENCODING, destCharSetEncoding));
		v = props.getProperty(PoolPropName.PROP_INITSQL);
		if (!StrFunc.isNull(v)) {
			initsql = v;
		}
		/**
		 * 20091230
		 * 设置连接最大闲置时间；
		 * 配置文件中maxIdleTime参数单位是分钟；
		 * 系统里面单位是毫秒，这里转换下；
		 * 如果定义了第三方连接池，那么要尽快地将连接归还给外部的连接池
		 */
		v = props.getProperty(PoolPropName.PROP_MAXIDLETIME);
		if (!StrFunc.isNull(v) || !StrFunc.isNull(ds3str)) {
			maxIdleTime = StrFunc.str2long(v, StrFunc.isNull(ds3str) ? DefaultPoolParam.DEFAULT_MAX_IDLE_TIME
					: 1000 * 10);
		}

		v = props.getProperty(PoolPropName.PROP_DEFAULTSCHEMA);
		if (!StrFunc.isNull(v) && !StrFunc.compareStr(this.default_schema, v.trim())) {
			this.default_schema = v.trim();
			if (!reload)
				reload = true;
		}
		
		v = props.getProperty(PoolPropName.PROP_ISCLOSELONGTIMECONN);
		if (!StrFunc.isNull(v)) {
			boolean isClose = StrFunc.str2boolean(v);
			this.isCloseLongTimeConn = isClose;
		}

		v = props.getProperty(PoolPropName.PROP_MINUSETIME);
		if (!StrFunc.isNull(v)) {
			this.minUseTime = StrFunc.str2long(v, DefaultPoolParam.DEFAULT_MIN_USE_TIME);
		}
		
		/*
		 * 连接池描述信息
		 */
		v = props.getProperty(PoolPropName.PROP_DESC);
		if (v != null) { // 为 "" 是表示清空  desc 的信息，也需要重新赋值
			this.desc = v;
		}
		
		lastModifiedPropsTime = System.currentTimeMillis();

		if (reload) {
			reload();
		}
	}

	/**
	 * 20100107
	 * 重值连接池中的链接；
	 * 此方法用于在连接池使用过程中，如果关键参数url,driverclass,user,pass发生变化时，都会调用此方法；
	 * 当 loglevel 和 sqlLogFile 改变时，也调用此方法。以使用  log4jdbc 记录 SQL语句日志。
	 * 调用此方法，连接池能够直接投入使用，而不需要重起web服务；
	 * 如果一个连接已经被获取出去了并正在被使用,此时有其他线程修改了连接池的关键设置如url,classname,user,pass,那么连接池中的连接
	 * 都应该丢弃并重新初始化连接池的,对于那些正在被使用的连接池,就通过此方法标识这些链接在使用完毕后，不需要再次返回到空闲池中；
	 * 操作过程如下：
	 * 1）将正在使用的连接，在使用完毕后直接物理关闭后丢弃，不返回空闲池；
	 * 2）将空闲池中的连接，物理关闭后丢弃；
	 * 
	 * 此方法也可以手工调用，用于重启连接池；
	 */
	public void reload() {
		if (state != STATE_STARTED)
			return;
		/**
		 * 只有处于运行状态的连接池，才能重置；
		 */
		synchronized (this) {
			/**
			 * 将空闲池的连接释放,重新获取；
			 */
			releasFreePool();
			dbinfo = null;//数据库参数还原，需要重新初始化；
			/**
			 * 重要参数修改，重置最后修改参数时间；
			 * 用于活动的链接是否直接物理关闭，不返回空闲池；
			 */
			lastModifiedPropsTime = System.currentTimeMillis();
			/**
			 * 20100127
			 * 重置连接池后，状态改为STATE_UNINITED，否则dbinfo如果不调用getConnection()将无法初始化；
			 */
			state = STATE_UNINITED;
		}
	}

	public synchronized String getName() {
		return poolname;
	}

	public synchronized void setName(String poolname) {
		if (!StrFunc.compareStr(this.poolname, poolname)) {
			this.poolname = poolname;
			/**
			 * 为日志设置连接池名称；
			 */
			this.jlog.setName(poolname);
		}
	}

	private void checkIsReady() throws SQLException {
		if (state != STATE_STARTED) {
			checkClosing();
			//      throw new SQLException("连接池还未初始化");
			throw new SQLException(I18N.getString("com.esen.jdbc.pool.datasourceimpl.noinitpool",
					"连接池还未初始化"));
		}
	}

	private void checkClosing() throws SQLException {
		if (state == STATE_CLOSING) {
			//      throw new SQLException("连接池正在关闭；");
			throw new SQLException(I18N.getString("com.esen.jdbc.pool.datasourceimpl.poolclosing",
					"连接池正在关闭；"));
		}
		checkClosed();
	}

	private void checkClosed() throws SQLException {
		if (isClosed()) {
			//      throw new SQLException("连接池已被关闭；");
			throw new SQLException(I18N.getString("com.esen.jdbc.pool.datasourceimpl.poolclosed",
					"连接池已被关闭；"));
		}
	}

	protected boolean isClosed() {
		return state == STATE_CLOSED;
	}

	/**
	 * 设置日志级别，levstr可以是数值，也可以是：DEBUG,INFO,WARN,ERROR,FATAL 级别名称；
	 * @param levstr
	 */
	public synchronized void setLogLever(String levstr) {
		if (!StrFunc.compareStr(jlog.getLogLeverStr(), levstr)) {
			jlog.setLogLever(levstr);
		}
	}

	public synchronized int getLogLever() {
		return jlog.getLogLever();
	}

	/**
	 * 返回日志级别的文字串内容
	 * @return
	 */
	public synchronized String getLogLeverStr() {
		return jlog.getLogLeverStr();
	}

	/**
	 * 设置数据库的字符集，现在只用于Oracle
	 * @param encode
	 */
	protected synchronized void setCharacterEncoding(String encode) {
		characterEncoding = encode;
		if (characterEncoding != null && characterEncoding.length() > 0) {
			needEncoding = true;
		}
		else
			needEncoding = false;
	}

	/**
	 * @param destencode
	 */
	protected synchronized void setDestCharSetEncoding(String destencode) {
		if (destencode != null && destencode.trim().length() > 0)
			destCharSetEncoding = destencode;
	}

	/**
	 * 请调用setProperties(prop) 进行参数设置
	 * @deprecated
	 * @param v
	 */
	public synchronized void setInitSql(String v) {
		this.initsql = v;
	}

	public synchronized String getInitSql() {
		return this.initsql;
	}

	public synchronized String getCharacterEncoding() {
		return characterEncoding;
	}

	public synchronized String getDestCharSetEncoding() {
		return destCharSetEncoding;
	}

	public boolean needEncoding() {
		return needEncoding;
	}

	/**
	 * 获取第三方DataSource
	 * @return DataSource
	 */
	public synchronized DataSource getOtherDataSource() {
		return ds3;
	}

	/**
	 * 获取第三方DataSource Context Name
	 * 可能为空
	 * @return String
	 */
	public synchronized String getOtherDataSourceName() {
		return datasource;
	}

	/**
	 * 设置第三方DataSource
	 * 请调用setProperties(prop) 进行参数设置
	 * @deprecated
	 * @param datasourcename String
	 * @throws SQLException
	 */
	public synchronized void setOtherDataSource(String datasourcename) {
		if (!StrFunc.compareStr(datasourcename, datasource)) {
			datasource = datasourcename;
			reload();
		}
	}

	/**
	 * 获取第三方连接池
	 * @throws SQLException
	 */
	private void initDataSource() throws SQLException {
		if (datasource != null && datasource.length() > 0) {
			try {
				Context ctx = new InitialContext();
				if (ctx == null) {
					throw new Exception("No Context!");
				}
				ds3 = (DataSource) ctx.lookup(datasource);
			}
			catch (Exception e) {
				//        SQLException se = new SQLException("找不到DataSource: " + datasource);
				SQLException se = new SQLException(I18N.getString(
						"com.esen.jdbc.pool.datasourceimpl.datssourcenotfound",
						"找不到DataSource: {0}", new Object[] { datasource }));
				se.setStackTrace(e.getStackTrace());
				if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_ERROR))
//					jlog.error(StrFunc.exception2str(se, "设置第三方DataSource出错！"));
					jlog.error(StrFunc.exception2str(se, I18N.getString("com.esen.jdbc.pool.datasourceimpl.comp1", "设置第三方DataSource出错！")));
				throw se;
			}
		}
	}

	public synchronized boolean getDefaultAutoCommit() {
		return defaultAutoCommit;
	}

	/**
	 * 请调用setProperties(prop) 进行参数设置
	 * @deprecated
	 * @param value
	 */
	public synchronized void setDefaultAutoCommit(boolean value) {
		defaultAutoCommit = value;
	}

	public String getDriverClassName() {
		return this.driverClassName;
	}

	/**
	 * 请调用setProperties(prop) 进行参数设置
	 * @deprecated
	 * @param driverClassName
	 */
	public synchronized void setDriverClassName(String driverClassName) {
		if (!StrFunc.compareStr(this.driverClassName, driverClassName)) {
			this.driverClassName = driverClassName;
			reload();
		}

	}

	/**
	 * 请调用getLogLever()方法；
	 * @deprecated
	 * @return
	 */
	public synchronized boolean isDebug() {
		return this.isdebug;
	}

	/**
	 * 请调用setLogLever(lev)方法；
	 * 兼容以前的参数；
	 * isDeBug=ture  表示 LOG_LEVER_WARN 级次；
	 *        =false 表示 LOG_LEVER_ERROR 级次；
	 * @deprecated
	 * @param value
	 */
	public synchronized void setDebug(boolean value) {
		if (value) {
			this.jlog.setLogLever(JdbcLogger.LOG_LEVER_WARN);
		}
		else {
			this.jlog.setLogLever(JdbcLogger.LOG_LEVER_ERROR);
		}
		this.isdebug = value;
	}

	public String getUrl() {
		return this.url;
	}

	/**
	 * 请调用setProperties(prop) 进行参数设置
	 * @deprecated
	 * @param url
	 */
	public synchronized void setUrl(String url) {
		if (!StrFunc.compareStr(this.url, url)) {
			this.url = url;
			reload();
		}
	}

	public String getUsername() {
		return this.username;
	}

	/**
	 * 请调用setProperties(prop) 进行参数设置
	 * @deprecated
	 * @param username
	 */
	public synchronized void setUsername(String username) {
		if (!StrFunc.compareStr(this.username, username)) {
			this.username = username;
			reload();
		}

	}

	public synchronized String getPassword() {
		return this.password;
	}

	/**
	 * 请调用setProperties(prop) 进行参数设置
	 * @deprecated
	 * @param password
	 */
	public synchronized void setPassword(String password) {
		if (!StrFunc.compareStr(this.password, password)) {
			this.password = password;
			reload();
		}
	}

	public synchronized String getCatalog() {
		return catalog;
	}

	/**
	 * 请调用setProperties(prop) 进行参数设置
	 * @deprecated
	 * @param value
	 */
	public synchronized void setCatalog(String value) {
		if (!StrFunc.compareStr(this.catalog, value)) {
			catalog = value;
			reload();
		}
	}

	public synchronized String getDefaultSchema() {
		return default_schema;
	}

	public synchronized int getMaxActive() {
		return this.maxActive;
	}

	/**
	 * 请调用setProperties(prop) 进行参数设置
	 * @deprecated
	 * @param value
	 */
	public synchronized void setMaxActive(int value) {
		if (value > 0) {
			this.maxActive = value;
		}
	}

	public synchronized int getMinIdle() {
		return this.minIdle;
	}

	/**
	 * 请调用setProperties(prop) 进行参数设置
	 * @deprecated
	 * @param value
	 */
	public synchronized void setMinIdle(int value) {
		if (value >= 0) {
			this.minIdle = value;
		}
	}

	public synchronized long getMaxWait() {
		return this.maxWait;
	}

	/**
	 * 请调用setProperties(prop) 进行参数设置
	 * @deprecated
	 * @param value
	 */
	public synchronized void setMaxWait(long value) {
		if (value > 0) {
			this.maxWait = value;
		}
	}

	public synchronized long getMaxIdleTime() {
		return this.maxIdleTime;
	}

	/**
	 * 设置连接的最大闲置时间;
	 * 单位是毫秒；
	 * 请调用setProperties(prop) 进行参数设置
	 * @deprecated
	 * @param value
	 */
	public synchronized void setMaxIdleTime(long value) {
		if (value > 0) {
			this.maxIdleTime = value;
		}
	}

	/**
	 * 20091010
	 * 获取描述数据库的对象；
	 * 初始化时需要同步，初始化后，直接获取；
	 * 这样该改动的原因是：如果getConnection()需要较长时间，调用getDbType()可以直接获得，不需要同步wait；
	 * @return
	 */
	public DataBaseInfo getDbType() {
		if (dbinfo != null)
			return dbinfo;

		try {
			initpool();
		}
		catch (SQLException e) {
			ExceptionHandler.rethrowRuntimeException(e);
			return null;
		}
		return dbinfo;
	}

	/**
	 * 获得正在使用的连接数
	 * @return int
	 */
	public synchronized int getActiveConnCount() {
		int activenum = 0;
		if (activepool != null) {
			activenum = activepool.size();
		}
		return activenum;
	}

	/**
	 * 获得空闲连接数
	 * @return int
	 */
	public synchronized int getFreeConnCount() {
		int count = 0;
		if (freepool != null) {
			count = freepool.size();
		}
		return count;
	}

	/**
	 * 获得活动连接个数；
	 * 空闲数＋使用数
	 * @return int
	 */
	public int getConnCount() {
		return getActiveConnCount() + getFreeConnCount();
	}

	/**
	 * 初始化连接池
	 */
	private void initpool() throws SQLException {
		if (state == STATE_STARTED)//大部分情况下,直接return，提高效率
			return;

		synchronized (this) {
			if (state != STATE_UNINITED) {
				checkClosing();//如果连接池正在关闭或已关闭,那么给与提示
				return;
			}
			state = STATE_STARTING;
			try {
				if (System.currentTimeMillis() - lastInitPoolFail < 1000 * 2) {
					/**
					 * 如果上次初始化出现异常，那么在接下来的2秒钟只能不再尝试初始化，原因：
					 * bi在装入主题表时，会为每个主题表都获取它的数据期字段的类型，需要建立数据库连接，但如果数据库连接指定的ip不存在，那么
					 * 每次初始化都要耗费较长时间，这样装入一个主题集可能就耗费更多时间
					 * 更多原因可以参考：BI-2390
					 * @see ReportBbqInfo#getBbqFieldSqlType
					 */
					throw lastInitPoolException;
				}
				/**
				 * 20100127
				 * 此参数记录是否是初始化连接池；
				 * 加此参数是因为现在此方发可能会调用多次，当修改了连接池的关键参数比如url等，会调用reload()方法；
				 * reload方法会设置state=STATE_UNINITED，需要重新获取dbtype等，否则getDbType()返回null；
				 */
				boolean initpool = false;
				//创建空闲池
				if (freepool == null) {
					freepool = new ArrayList(minIdle);
					if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_INFO)) {
//						jlog.info("初始化连接池..........");
						jlog.info(I18N.getString("com.esen.jdbc.pool.datasourceimpl.comp2", "初始化连接池.........."));
						jlog.info(driverClassName);
					}
					initpool = true;
				}
				//创建活动池
				if (activepool == null) {
					activepool = new HashMap(maxActive);
				}
				/**
				 * 初始化时，创建一个数据库连接，初始化dbtype，字符集等；
				 * 参数minIdle现在不起作用，原因是很多开发者需要调试程序，都连同一个数据库，如果每次启动服务都创建minIdle个连接；
				 * 这样可能会超过数据库连接的最大连接数；
				 * 只初始化一个，不影响连接池的使用；
				 * 
				 * 调用此方法还有个好处就是：获取连接后关闭，返回空闲池会调用setActive(false)方法，
				 * 里面会初始化lastReturnToFreePoolTime参数，这很重要，原因是：
				 * 原来使用的方法没有初始化lastReturnToFreePoolTime参数，造成当连接池创建后，如果10秒中没有程序调用getConnection(),
				 * 监控限制连接的线程每10秒会根据lastReturnToFreePoolTime参数判断是否丢弃空闲连接，出现：
				 * 关闭闲置时间过长的连接，已空闲351271小时38分15秒781毫秒 的日志提示，这显然不对；
				 */
				getConnection().close();

				//开始监控闲置连接
				startFreePoolCheck();

				lastInitPoolFail = 0;
				lastInitPoolException = null;

				if (initpool) {//初始化连接池才记录日志；
					if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_INFO))
//						jlog.info("连接池创建成功！");
						jlog.info(I18N.getString("com.esen.jdbc.pool.datasourceimpl.comp3", "连接池创建成功！"));
				}
			}
			catch (SQLException se) {
				/**
				 * 出异常，将state值还原；
				 */
				state = STATE_UNINITED;
				activepool = null;
				if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_ERROR))
//					jlog.error(StrFunc.exception2str(se, "连接池创建失败！"));
					jlog.error(StrFunc.exception2str(se, I18N.getString("com.esen.jdbc.pool.datasourceimpl.comp4", "连接池创建失败！")));
				lastInitPoolFail = System.currentTimeMillis();
				lastInitPoolException = se;
				throw se;
			}
			state = STATE_STARTED;
		}
	}

	private void startFreePoolCheck() {
		if (freePoolRemoveTimer != null)
			return;
		freePoolRemoveTimer = new Runnable() {
			public void run() {
				checkFreePool();
			}
		};
		IdleThread.getDefault().scheduleAtFixedRate(freePoolRemoveTimer, 50, 10 * 1000);
	}

	protected void checkFreePool() {
		List dropedConnections = null;
		synchronized (this) {
			long now = System.currentTimeMillis();
			/**
			 * 20100305 freepool变量可能为null，这里要判断下，否则下面的for循环会导致空指针异常。
			 */
			if (freepool != null) {
				for (int index = freepool.size() - 1; index >= 0; index--) {
					PooledConnection pconn = (PooledConnection) freepool.get(index);
					/**
					 * 20091230
					 * 每次从空闲池获取连接时，判断连接闲置时间是否超过了允许的最大闲置时间，如果超过，则丢弃之； 
					 */
					long iddltime = now - pconn.getLastReturnToFreePoolTime();
					if (iddltime > maxIdleTime) {
						freepool.remove(index);
						if (dropedConnections == null) {
							dropedConnections = new ArrayList();
						}
						dropedConnections.add(pconn);
					}
				}
			}
		}

		//在synchronized外执行丢弃后先物理关闭，避免长时间占用锁
		if (dropedConnections != null) {
			for (int i = 0, len = dropedConnections.size(); i < len; i++) {
				closeIdleConnection((PooledConnection) dropedConnections.get(i));
			}
		}
	}

	private void initDataBaseInfo(Connection conn) throws SQLException {
		/**
		 * 用户设置的默认schema参数，需要被传递到DataBaseInfo中，获取默认schema将调用DataBaseInfo.getDefaultSchema();
		 */
		dbinfo = DataBaseInfo.createInstance(conn, default_schema);
		if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_INFO))
			jlog.info("DbType: " + dbinfo.getDatabaseProductName());

		/**
		 * 初始化字符集, 如果字符集参数没有设置，则根据数据库的字符集自动设置；
		 */
		if (dbinfo.isOracle()) {
			String charc = dbinfo.getCharacterEncoding();
			if (charc.indexOf("US7ASCII") >= 0) {
				setCharacterEncoding("iso8859_1");
			}
			else if (charc.indexOf("ZHS16GBK") >= 0) {
				setCharacterEncoding(null);
			}
		}

		/**
		 * 20100206
		 * 在初始化连接池时，检查数据库驱动版本，如果不合适，给予警告提示；
		 */
		String log = dbinfo.check();
		if (log != null && log.length() > 0) {
			if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_WARN)) {
				jlog.warn(log);
			}
		}
	}

	/**
	 * 返回当前连接池状态；
	 * @return StringBuffer
	 */
	public synchronized StringBuffer showDataSourceStatus() {
		StringBuffer info = new StringBuffer(128);
		//    info.append("连接池状态：\r\n");
		//    info.append("空闲连接数： " + getFreeConnCount() + "\r\n");
		//    info.append("正在使用的连接数： " + getActiveConnCount() + "\r\n");
		info.append(I18N.getString("com.esen.jdbc.pool.datasourceimpl.poolstate",
				"连接池状态：\r\n空闲连接数：{0}\r\n正在使用的连接数： {1}\r\n",
				new Object[] { String.valueOf(getFreeConnCount()), String.valueOf(getActiveConnCount()) }));
		return info;
	}

	/**
	 * 判断是否记录堆栈；
	 * 如果日志级次小于等于LOG_LEVER_WARN，记录连接调用堆栈;
	 * 
	 * modify by liujin 2013.10.14
	 * 修改为任何情况下都记录连接的调用堆栈，方便在连接没关闭时及时打印堆栈信息。
	 */
	public boolean isDebugStackLog() {
		//return jlog.canLogLevel(JdbcLogger.LOG_LEVER_WARN);
		return true;
	}

	/**
	 * “关闭”连接，将连接返回空闲池
	 * @param conn PooledConnection
	 * @throws SQLException
	 */
	protected void closeConnection(PooledConnection conn) throws SQLException {
		synchronized (this) {
			/**
			 * 关闭连接时，将threadLocal中的同步对象删除；
			 * 避免下次再获取连接时，不同的线程获得同一个连接对象；
			 */
			LocalUnit localUnit = (LocalUnit) threadLocal.get();
			/**
			 * i为了解决同一个线程嵌套获取连接问题，通过DbFuncs.safeGetConnection()方法，嵌套获取时，另开一个线程，在新的线程中获取连接使用；
			 * 所以在关闭这个链接时，通过当前线程获取threadLocal.get()连接对象是空的；
			 * 
			 * 解决办法：
			 * 1）这里程序上加上空判断，如果为空则输出警告；
			 * 2）i尽量避免使用DbFuncs.safeGetConnection()获取连接；
			 */
			/**
			 *  此处实际无效, 因为服务容器线程池的关系, 线程在被循环使用。即便是其他线程试图关闭连接, localUnit也不会为空, 应该是
			 *  localUnit == null || localUnit.get() != null 
			 */
			if (localUnit == null) {
				if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_WARN)) {
//					jlog.warn("在一个线程获取连接请在同一个线程中关闭；尽量避免在A线程获取连接，却在B线程中关闭连接的情况；");
					jlog.warn(I18N.getString("com.esen.jdbc.pool.datasourceimpl.comp5", "在一个线程获取连接请在同一个线程中关闭；尽量避免在A线程获取连接，却在B线程中关闭连接的情况；"));
				}
			}
			else {
				localUnit.remove();
			}
			// 这里还是不处理的好, 我们可以在真正的关闭函数中抛出WARN
			// if(conn.getCallFrequencyForSameThread() > 0) {
			//  throw new RuntimeException("非法的关闭操作,当前数据库连接仍在使用中");
			// }
			//从活动池移除此连接；
			activepool.remove(conn.getUniqueCode());
			//将此连接返回空闲池
			if (conn.getCreateTime() > this.lastModifiedPropsTime) {
				freepool.add(conn);
				conn.setActive(false);
				this.notify();
			}
			else {
				/**
				 * 将那些创建时间在lastModifiedPropsTime之前的链接真正的关闭，不再返回到freepool
				 * 因为也许此链接被获取并正在使用时，有人修改了连接池的关键信息，如url，那么此链接就是需要执行真正关闭的了。
				 */
				conn.realclose();
			}
		}
	}

	/**
	 * 空闲池没有空闲连接，并且正在使用的连接大于最大连接数；
	 * 等待获得连接
	 * @throws SQLException
	 * @return PooledConnection
	 */
	private PooledConnection waitReturnConnection(boolean throwException) throws SQLException {
		PooledConnection pconn = null;
		long waittime = this.getMaxWait();
		while ((freepool.size() == 0) && (activepool.size() >= this.getMaxActive()) && (waittime > 0)) {
			try {
				long nowtime = System.currentTimeMillis();
				this.wait(waittime);
				/**
				 * 20100105
				 * 等待的线程唤醒时，可能连接池正在关闭，或者已经关闭，这时不能再获取连接；
				 */
				checkIsReady();
				waittime -= System.currentTimeMillis() - nowtime;
				pconn = getFreeConnection();
				if (pconn != null) {
					return pconn;
				}
				if (activepool.size() < this.getMaxActive()) {
					return getNewConnection();
				}
			}
			catch (InterruptedException e) {
				if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_ERROR))
//					jlog.error(StrFunc.exception2str(e, "获得数据库连接超时；"));
					jlog.error(StrFunc.exception2str(e, I18N.getString("com.esen.jdbc.pool.datasourceimpl.comp6", "获得数据库连接超时；")));
				return null;
			}
		}
		
		if (throwException) {
			throwNoConnectException();
		}
		return null;
	}
	
	/**
	 * 抛出无法获得新连接的异常
	 * 
	 * @throws SQLException
	 */
	private void throwNoConnectException() throws SQLException {
		SQLException e = new SQLException(I18N.getString("com.esen.jdbc.pool.datasourceimpl.poolwarn",
				"连接池中目前存在{0}个活动连接，允许的最大活动连接数是{1}，无法获得新的数据库连接! ",
				new Object[] { String.valueOf(activepool.size()), String.valueOf(this.getMaxActive()) }));
		if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_ERROR))
			jlog.error(StrFunc.exception2str(e, I18N.getString("com.esen.jdbc.pool.datasourceimpl.comp7", "建议修改参数：增大最大连接数，或增大最大等待时间；")));
		throw e;
	}

	/**
	 * 从空闲池取得连接，加入活动池，并从空闲池删除
	 * @throws SQLException
	 * @return PooledConnection
	 */
	private PooledConnection getFreeConnection() throws SQLException {
		if (freepool.size() > 0) {
			PooledConnection pconn = (PooledConnection) freepool.remove(freepool.size() - 1);
			return pconn;
		}
		return null;
	}

	/**
	 * 丢弃闲置时间太长的连接pconn前，物理关闭此连接；
	 * 如果有异常，打印到控制台；
	 * @param pconn
	 */
	private void closeIdleConnection(PooledConnection pconn) {
		if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG)) {
//			jlog.debug("关闭闲置时间过长的连接，已空闲"
			jlog.debug(I18N.getString("com.esen.jdbc.pool.datasourceimpl.comp8", "关闭闲置时间过长的连接，已空闲")
					+ StrFunc.formatTime(System.currentTimeMillis() - pconn.getLastReturnToFreePoolTime()));
		}
		try {
			pconn.realclose();
		}
		catch (SQLException ex) {
			if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_WARN))
//				jlog.warn(StrFunc.exception2str(ex, "警告，关闭闲置时间过长的连接出现异常："));
				jlog.warn(StrFunc.exception2str(ex, I18N.getString("com.esen.jdbc.pool.datasourceimpl.comp9", "警告，关闭闲置时间过长的连接出现异常：")));
		}
	}

	/**
	 * 判断空闲池里的连接是否有效；
	 * 此方法不抛异常，如果链接无效，会被丢弃，重新获取；
	 * 为了解决网闸，或者数据库重启了等原因，带来的空闲池中的链接无效问题；
	 * 如果抛出异常会导致程序访问中断 ；
	 * @param pconn PooledConnection
	 * @return boolean 有效:true; 无效:false;
	 * @throws SQLException 
	 */
	private boolean checkConn(PooledConnection pconn) {
		if (pconn == null) {
			return false;
		}
		String checksql = dbinfo.getCheckSQL();
		/**
		 * 如果没有用于检查的sql，则不检测此连接是否有效；
		 */
		if (StrFunc.isNull(checksql)) {
			return true;
		}
		try {
			//链接已被关闭，直接返回无效
			if (pconn.isClosed())
				return false;
			//执行一个测试sql，不出异常，表示有效链接；
			Statement stat = pconn.getSourceConnection().createStatement();
			try {
				/**
				 * 20090922
				 * 改为直接执行一个sql，而不是执行一个查询；
				 * 原因是执行查询，会返回一个ResultSet，它会打开一个游标，原来的程序没有关闭这个ResultSet；
				 */
				stat.execute(checksql);
				return true;
			}
			finally {
				if (stat != null)
					stat.close();
			}
		}
		catch (SQLException se) {
			//不能创建Statement, 或者执行正常的查询操作出异常，表示链接无效；
			return false;
		}
	}

	/**
	 * 从连接池获取数据库连接
	 * @throws SQLException
	 * @return PooledConnection
	 */
	private PooledConnection getPooledConnection() throws SQLException {
		synchronized (this) {
			/**
			 * 处理嵌套获取连接问题；
			 * 以下两种情况，会返回当前线程正在使用的数据库连接：
			 * 1）在同一个线程中，在上一个连接没有关闭的情况下，又获取了一个连接；
			 * 2）在同一个线程中，上一个连接没有调用close关闭，且还未被回收站回收；
			 * 连接对象引用计数机制，嵌套获取时，返回的连接计数+1，关闭连接时计数-1；
			 * 直到计数为0，表示获取的连接都被“关闭”，这时才将连接返回空闲池；
			 */
			if (threadLocal == null) {
				threadLocal = new ThreadLocal();
			}
			/**
			 * 20101015
			 * 使用一个对象LocalUnit来保存连接弱对象，目的是为了在“关闭连接”时，将连接弱对象从threadLocal中删除；
			 * 原因是threadLocal的jdk1.4版本不支持remove方法；
			 * 如果连接没有被threadLocal删除，可能造成不同的线程获取到同一个连接的情况；
			 * 比如：A线程获取了一个连接T，使用完正常关闭，连接返回连接池，B线程这时调用获取连接，得到连接T，
			 * 并使用中，这时A线程再次调用获取连接，由于是在原来的线程中调用，threadLocal返回保存的链接，而这个链接正在被B线程使用；
			 * 这就造成不同的线程获取了同一个连接的情况，如果B线程刚好关闭了连接，A线程使用这个链接就会报“关闭的链接”异常，这是不被允许的；
			 */
			Object localObj = threadLocal.get();
			if (localObj == null) {
				localObj = new LocalUnit();
				threadLocal.set(localObj);
			}
			LocalUnit localUnit = (LocalUnit) localObj;
			Object tcon = localUnit.get();
			if (tcon != null) {
				PooledConnection pconn = (PooledConnection) ((WeakReference) tcon).get();
				/**
				 * 这里判断pconn是否为空，是为了防止那些没有调用"关闭"的连接，被垃圾回收后，为空连接；
				 * pconn.isActive()是判断连接是否被“关闭”，如果被关闭(放回false)，表示此连接已被返回空闲池；
				 * 这两种情况下，都需要重新获取连接；
				 */
				if (pconn != null && pconn.isSameThread()) {
					pconn.callFrequencyForSameThread();
					if (!pconn.isActive()) {
						/**
						 * 这里不同于其他几处, 其他可以通过抛出异常警告, 此处根本不能继续执行
						 * 因为迟早在checkActive中会报异常的
						 */
//						throw new RuntimeException("无效的数据库连接, 此数据库连接没有被激活");
						throw new RuntimeException(I18N.getString("com.esen.jdbc.pool.datasourceimpl.illdatasource", "无效的数据库连接, 此数据库连接没有被激活"));
					}
					return pconn;
				}
				else {
					/**
					 * 连接没有正常关闭，被垃圾回收站回收，这里将弱对象删除；
					 */
					localUnit.remove();
				}
			}

			PooledConnection pconn = getPooledConn();

			/**
			 * 使用 threadLocal 保存连接弱引用；
			 */
			localUnit.set(pconn.getWeakReference());

			/**
			 * 将获取的连接放入活动池；
			 */
			activepool.put(pconn.getUniqueCode(), pconn.getWeakReference());
			pconn.setActive(true);
			checkActivePool();

			/**
			 * 如果日志级次小于等于LOG_LEVER_WARN，记录连接调用堆栈；
			 * 用于如果获取的连接没有调用close()，java垃圾回收站回收时记录此连接的调用堆栈；
			 */
			if (isDebugStackLog()) {
				pconn.recordStacktrace();
			}
			return pconn;
		}
	}

	/**
	 * 如果获取了连接，却没有调用close()关闭，活动池将一直持有此连接的弱引用；
	 * 检查活动池，将已经被回收站回收的连接弱引用，移除活动池；
	 * 当活动池的连接个数大于设置的最大连接数maxtActive的两倍才执行；
	 */
	private void checkActivePool() {
		if (activepool.size() > this.maxActive * 2) {
			Iterator it = activepool.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry en = (Map.Entry) it.next();
				Object o = ((WeakReference) en.getValue()).get();
				if (o == null) {
					it.remove();
				}
			}
		}
	}

	private PooledConnection getPooledConn() throws SQLException {
		//从连接池中获得空闲连接
		PooledConnection con = getFreeConnection();
		if (con != null) {
			return con;
		}
		//没有空闲连接，并且正在使用的连接大于最大连接数
		if (activepool.size() >= getMaxActive()) {
			if (!isCloseLongTimeConn) {
				return waitReturnConnection(true);
			}
			
			// isCloseLongTimeConn
			con = waitReturnConnection(false);
			if (con == null) {
				synchronized (this) {
					closeLongTimeConnection();
					return getFreeConnection();
				}
			} else {
				return con;
			}
		}
		//没有超过最大连接数，创建新连接
		return getNewConnection();
	}

	/**
	 * 关闭使用的时间最长的连接
	 * @throws SQLException 
	 */
	private void closeLongTimeConnection() throws SQLException {
		PooledConnection dropedConn = null;
		synchronized (this) {
			long now = System.currentTimeMillis();
			long useTime = 0;

			if (activepool != null) {
				Iterator it = activepool.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry en = (Map.Entry) it.next();
					Object o = ((WeakReference) en.getValue()).get();
					if (o != null) {
						PooledConnection pconn = (PooledConnection) o;
						long tmpUseTime = now - pconn.getLastGetTime();
						if (tmpUseTime > useTime && tmpUseTime > minUseTime) {
							dropedConn = pconn;
						}
					}
				}
			}
		}
		//强制关闭该连接
		if (dropedConn != null) {
			dropedConn.forceClose();
			return;
		}
		
		//没有连接可强制关闭
		throwNoConnectException();
	}
	
	/**
	 * 从连接池获取数据库连接；
	 * 并记录日志
	 * @throws SQLException
	 * @return Connection
	 */
	public Connection getConnection() throws SQLException {
		initpool();
		/**
		 * 20100105
		 * 每次获取连接，检查连接的有效性，很耗时，而且在锁中运行，容易造成堵塞，改在锁外面检查；
		 * 如果是无效的连接，直接丢弃，丢弃前尝试关闭连接；
		 */
		PooledConnection r = getPooledConnection();
		/**
		 * 如果是同一个线程中，嵌套获取连接，则直接返回此连接，不用check；
		 * 这样做是为了避免死循环：如果连接不可用还调用getConnection(),则由于是用一个线程调用，总返回这个链接，造成死循环；
		 */
		synchronized (this) {
			//读取这个计数需要同步锁
			if (r.getCallFrequencyForSameThread() > 0) {
				return r;
			}
		}
		/**
		 * 如果从连接池获取的链接是可用的，则返回使用；
		 */
		if (checkConn(r)) {
			return r;
		}
		/**
		 * 获取的链接不可用，则重新从连接池获取一个；
		 * 
		 * 重新获取前，需要将不可用连接丢弃；
		 * 否则再次调用getConnection()会造成同线程获取连接，又获取此连接的问题；
		 */
		synchronized (this) {
			LocalUnit localUnit = (LocalUnit) threadLocal.get();
			localUnit.remove();
			r.setActive(false);
			activepool.remove(r.getUniqueCode());
		}
		return getConnection();
	}

	/**
	 * @deprecated
	 */
	public Connection getConnection(String username, String password) throws SQLException {
		return this.getConnection();
	}

	/**
	 * 释放连接池资源
	 */
	public void release() {
		synchronized (this) {
			if (state != STATE_STARTED)//允许多次调用release
				return;
			state = STATE_CLOSING;
			if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_INFO))
//				jlog.info("开始释放数据库连接池资源；");
				jlog.info(I18N.getString("com.esen.jdbc.pool.datasourceimpl.comp10", "开始释放数据库连接池资源；"));
			//等待活动池的连接关闭
			if (activepool != null) {
				/**
				 * 20100105
				 * 等待两分钟，直到活动池为空；
				 * 如果两分钟后，活动池还有连接，则强行关闭所有连接；
				 * 
				 * 原来的程序，没有等待2分钟，被唤醒就强行关闭所有连接，
				 * 可能造成有些正在执行的可以在2分钟内执行完毕的连接突然被关闭；
				 */
				long waitTime = 2 * 60 * 1000;
				while (activepool.size() > 0 && waitTime > 0) {
					try {
						long waitnow = System.currentTimeMillis();
						this.wait(waitTime);
						waitTime -= System.currentTimeMillis() - waitnow;
					}
					catch (InterruptedException e) {
						if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_ERROR))
//							jlog.error(StrFunc.exception2str(e, "释放连接池连接超时"));
							jlog.error(StrFunc.exception2str(e, I18N.getString("com.esen.jdbc.pool.datasourceimpl.comp11", "释放连接池连接超时")));
						break;
					}
				}
				if (activepool.size() > 0) {
					/**
					 * 20090715
					 * 释放连接池时，如果有活动的连接，比如正在执行一个查询，需要等待这个活动连接的事务执行完毕；
					 * 原来的代码是无限等待，现在改为等待2分钟后关闭所有活动的连接；
					 * BI-2154 重启BI服务器等待时间太长；
					 */
					releasActivePool();
				}
			}
			//释放空闲池

			releasFreePool();

			//释放连接池，关闭定时Timer
			if (freePoolRemoveTimer != null)
				IdleThread.getDefault().cancel(freePoolRemoveTimer);
			/**
			 * 20100128
			 * 在侦测闲置连接定时timer停止后在freepool=null;
			 * 否则可能在在timer停止前调用运行，由于freepool=null导致的空指针异常；
			 */
			freepool = null;
			activepool = null;
			if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_INFO))
//				jlog.info("已释放数据库连接池资源！");
				jlog.info(I18N.getString("com.esen.jdbc.pool.datasourceimpl.comp12", "已释放数据库连接池资源！"));
			this.jlog.close();

			state = STATE_CLOSED;
		}

	}

	/**
	 * 释放空闲池，不抛异常 ；
	 * 比如 ：在 修改了关键参数比如url等，需要将空闲池清空 ，清空前关闭链接，这时不需要抛异常；
	 * 如果出异常，记录于debug日志；
	 * @throws SQLException
	 */
	private void releasFreePool() {
		if (freepool != null) {
			Iterator freeit = freepool.iterator();
			while (freeit.hasNext()) {
				PooledConnection _conn = (PooledConnection) freeit.next();
				realCloseConnection(_conn);
				freeit.remove();
			}
		}
	}

	/**
	 * 物理关闭链接，不抛异常；
	 * 如果出异常，记录在debug日志；
	 * @param _conn
	 */
	private void realCloseConnection(PooledConnection _conn) {
		try {
			if (_conn != null && !_conn.isClosed()) {
				_conn.realclose();
			}
		}
		catch (SQLException ex) {
			if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
//				jlog.debug(StrFunc.exception2str(ex, "关闭连接出现异常"));
				jlog.debug(StrFunc.exception2str(ex, I18N.getString("com.esen.jdbc.pool.datasourceimpl.comp13", "关闭连接出现异常")));
		}
	}

	/**
	 * 释放活动池
	 * 不抛异常，防止出现异常将中断循环遍历；
	 * 如果出异常，记录于debug日志；
	 * @throws SQLException
	 */
	private void releasActivePool() {
		if (activepool != null) {
			Iterator it = activepool.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry en = (Map.Entry) it.next();
				Object o = ((WeakReference) en.getValue()).get();
				if (o != null) {
					PooledConnection _conn = (PooledConnection) o;
					realCloseConnection(_conn);
				}
				else {
					//已被回收站回收；
				}
				it.remove();
			}
		}
	}

	/**
	 * 获得新的数据库连接，并加入活动池
	 * @throws SQLException
	 * @return PooledConnection
	 */
	private PooledConnection getNewConnection() throws SQLException {
		return createPooledConnection(createNewConnection(), true);
	}

	/**
	 * 获得包装的数据库连接；
	 * @param conn
	 *        参数可能是物理连接，也可能是第三方连接池获取的连接；
	 * @param forPool 此参数为true，表示此连接是用于连接池的；
	 *                为false，表示此连接用于从连接池总是获取新连接的方法调用；
	 * @return
	 * @throws SQLException
	 */
	private PooledConnection createPooledConnection(Connection conn, boolean forPool) throws SQLException {
		/**
		 * BI-6060
		 * 增加forPool的原因是：调用ConnectionFactory.getNewConnection()获取新连接，连接池没有初始化，dbinfo对像为空，会造成空指针异常。
		 * 这时解决BI-5525时，没有考虑连接池没有初始化，这里dbinfo可能为空的情况。
		 * --20111128 dw
		 */
		if (forPool && dbinfo.isSybase()) {
			return new SybasePooledConnection(this, conn, connectionIndex++);
		}
		if (forPool && dbinfo.isGBase()) {
			return new GbasePooledConnection(this, conn, connectionIndex++);
		}
		if (forPool && dbinfo.isGBase8T()) {
			return new Gbase8tPooledConnection(this, conn, connectionIndex++);
		}
		return new PooledConnection(this, conn, connectionIndex++);
	}

	/**
	 * 20091118
	 * 获得对物理连接包装的PooledConnection对象；
	 * 如果设置了第三方连接池，则必须设置driveclassname,url,user,pass参数，否则此方法会出异常；
	 * 此连接调用close()方法，将关闭物理连接；
	 * @return
	 * @throws SQLException
	 */
	public PooledConnection createRealPooledConnection() throws SQLException {
		PooledConnection pconn = createPooledConnection(createRealConnection(), false);
		pconn.setActive(true);
		pconn.setNeedRealClose(true);
		return pconn;
	}

	/**
	 * 创建新的数据库连接
	 * 优先从第三方连接池获取连接，如果不能获取，则从driveclassname,url,user,pass参数获取物理连接；
	 * @return Connection
	 */
	private Connection createNewConnection() throws SQLException {
		Connection conn = null;
		/**
		 * 移到此处是因为在设置参数datasource时，没有必要去找第三方连接池；
		 * 在创建连接时，在初始化；
		 */
		initDataSource();
		try {
			try {
				//从第三方连接池获得连接
				if (ds3 != null) {
					conn = ds3.getConnection();
				}
			}
			catch (SQLException se) {
				//        SQLException e = new SQLException("无法从第三方连接池“"
				//            + this.getOtherDataSourceName() + "”获得连接！" + se.getMessage());
				SQLException e = new SQLException(I18N.getString(
						"com.esen.jdbc.pool.datasourceimpl.nopoolfrom3", "无法从第三方连接池“{0}”获得连接！{1}",
						new Object[] { this.getOtherDataSourceName(), se.getMessage() }));
				e.setStackTrace(se.getStackTrace());
				if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_FATAL))
//					jlog.fatal(StrFunc.exception2str(e, "无法创建连接！"));
					jlog.fatal(StrFunc.exception2str(e, I18N.getString("com.esen.jdbc.pool.datasourceimpl.comp14", "无法创建连接！")));
				throw e;
			}

			//创建物理连接
			if (conn == null)
				conn = createRealConnection();
			if (conn == null) {
				throw new SQLException();
			}
			if (conn.getAutoCommit() != defaultAutoCommit)
				conn.setAutoCommit(defaultAutoCommit);
			if (catalog != null && catalog.length() > 0) {
				conn.setCatalog(catalog);
			}
			if (dbinfo == null)
				initDataBaseInfo(conn);
			exeInitSql(conn);
		}
		catch (SQLException se) {
			StringBuffer error = new StringBuffer(1024);
			//error.append("建数据库连接出现异常:\r\n");
			error.append(I18N.getString("com.esen.jdbc.pool.datasourceimpl.poolexcr", "建数据库连接出现异常:\r\n"));
			error.append(getDriverClassName()).append("\r\n");
			error.append(getUrl()).append("\r\n");
			error.append(getUsername()).append("\r\n");
			SQLException e = new SQLException(error.toString() + se.getMessage());			
			e.setStackTrace(se.getStackTrace());
//			jlog.fatal(StrFunc.exception2str(e, "无法创建数据库连接！"));
			jlog.fatal(StrFunc.exception2str(e, I18N.getString("com.esen.jdbc.pool.datasourceimpl.comp15", "无法创建数据库连接！")));
			try {
				if (conn != null)
					conn.close();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}//关闭获取的连接
			throw e;
		}
		catch (NullPointerException ne) {
			/**
			 * BI-6416 netezza连接池不可用时，计算报空指针。
			 * Netezza的jdbc驱动在数据库连接异常的时候，没有抛出SqlException而是抛出NullPointerException，这里需要特殊处理下
			 */
			if (this.driverClassName.toLowerCase().indexOf("netezza") != -1) {
				StringBuffer error = new StringBuffer(1024);
				//error.append("建数据库连接出现异常:\r\n");
				error.append(I18N.getString("com.esen.jdbc.pool.datasourceimpl.poolexcr", "建数据库连接出现异常:\r\n"));
				error.append(getDriverClassName()).append("\r\n");
				error.append(getUrl()).append("\r\n");
				error.append(getUsername()).append("\r\n");
				SQLException e = new SQLException(error.toString() + ne.toString());		
				e.setStackTrace(ne.getStackTrace());
//				jlog.fatal(StrFunc.exception2str(e, "无法创建数据库连接！"));
				jlog.fatal(StrFunc.exception2str(e, I18N.getString("com.esen.jdbc.pool.datasourceimpl.comp15", "无法创建数据库连接！")));
				try {
					if (conn != null)
						conn.close();
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}//关闭获取的连接
				throw e;
			}
			else {
				throw ne;
			}

		}
		return conn;
	}

	private void exeInitSql(Connection conn) throws SQLException {
		if (initsql != null && initsql.length() > 0) {
			Statement stmt = conn.createStatement();
			try {
				stmt.execute(initsql);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			finally {
				stmt.close();
			}
		}
	}

	/**
	 * 创建物理；
	 * 从driveclassname,url,user,pass参数获取物理连接；
	 * 即使设置了第三方连接池，也可以通过设置driveclassname,url,user,pass来获得物理连接；
	 * @return Connection
	 **/
	private Connection createRealConnection() throws SQLException {
		if (driverClassName == null || driverClassName.equals("")) {
//			throw new SQLException("没有为 driverClassName 赋值！");
			throw new SQLException(I18N.getString("com.esen.jdbc.pool.datasourceimpl.driverClassNamenull", "没有为 driverClassName 赋值！"));
		}
		if (url == null) {
//			throw new SQLException("没有为 jdbc url 赋值！");
			throw new SQLException(I18N.getString("com.esen.jdbc.pool.datasourceimpl.jdbcurlnull", "没有为 jdbc url 赋值！"));
		}
		try {
			Class.forName(driverClassName);
		}
		catch (Exception e) {
//			throw new SQLException("无法装载JDBC驱动：" + driverClassName);
			throw new SQLException(I18N.getString("com.esen.jdbc.pool.datasourceimpl.driverinit", "无法装载JDBC驱动：{0}",new Object[]{driverClassName}));
		}

		String dburl = DefaultConnectionFactory.convertDbUrl(url);

		/**
		 * BI-5743 20111109 创建新连接同步问题
		 * 原先的代码采用DriverManager.getConnection(...)方法，查看源码这个方法是同步的静态方法，
		 * 所有连接池中创建连接都要访问这个方法，当一个连接池创建连接很慢（这个数据库没有启动或者其他什么原因），
		 * 这时如果另一个连接池要创建新的连接，就会等待。在某些特定的情况下，会让界面等待很久。
		 * 所以这里创建新连接将不再使用同步的方法。
		 */
		Driver driver = DriverManager.getDriver(dburl);
		java.util.Properties info = new java.util.Properties();
		if (username != null) {
			info.put("user", username);
			info.put("password", StrFunc.isNull(password) ? "" : password);
		}

		// log4jdbc 相关属性设置
		// 只在  loglevel 为 debug 时记录 sql 日志。
		if (jlog.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG)) {
		    //log4jdbc 的  logger 属性设置
			Properties props = new Properties();
			props.setProperty("log4j.logger.jdbc.sqlonly", "off");
			props.setProperty("log4j.additivity.jdbc.sqlonly", "false");
			props.setProperty("log4j.logger.jdbc.audit", "off");
			props.setProperty("log4j.additivity.jdbc.audit", "false");
			props.setProperty("log4j.logger.jdbc.resultset", "off");
			props.setProperty("log4j.additivity.jdbc.resultset", "false");
			
			//Log timing information about the SQL that is executed.
			//Log connection open/close events and connection number dump
			props.setProperty("log4j.logger.jdbc.sqltiming", "DEBUG,jdbc");
			props.setProperty("log4j.additivity.jdbc.sqltiming", "false");	
			props.setProperty("log4j.logger.jdbc.connection", "DEBUG,jdbc");	
			props.setProperty("log4j.additivity.jdbc.connection", "false");
			
			//the appender used for the JDBC API layer call logging above
			props.setProperty("log4j.appender.jdbc", "org.apache.log4j.RollingFileAppender");
			props.setProperty("log4j.appender.jdbc.File", StrFunc.isNull(sqllogfile)? "esen_jdbc.log" : sqllogfile);
			props.setProperty("log4j.appender.jdbc.MaxFileSize","10MB"); 
			props.setProperty("log4j.appender.jdbc.MaxBackupIndex", "50"); 
			props.setProperty("log4j.appender.jdbc.Append", "true");
			props.setProperty("log4j.appender.jdbc.layout", "org.apache.log4j.PatternLayout");
			props.setProperty("log4j.appender.jdbc.layout.ConversionPattern", "[%d{yyyy-MM-dd HH:mm:ss.SSS}][%-5p][%c] %m%n%n");
			//PropertyConfigurator.configure(props);	
			
			//log4jdbc 属性设置
			System.setProperty("log4jdbc.dump.sql.select", "true");
			System.setProperty("log4jdbc.dump.fulldebugstacktrace", "true");
			
			String strbuf = System.getProperty("log4jdbc.drivers");
			if (StrFunc.isNull(strbuf))
				strbuf = driverClassName;
			else if (strbuf.indexOf(driverClassName) < 0)
				strbuf = strbuf.concat("," + driverClassName);
			System.setProperty("log4jdbc.drivers", strbuf);
			
			return new ConnectionSpy(driver.connect(dburl, info));
	    } else
	    	return driver.connect(dburl, info);
	}

	public PrintWriter getLogWriter() throws SQLException {
		checkClosed();
		return new PrintWriter(jlog.getLogWriter());
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		/**@todo Implement this javax.sql.DataSource method*/
		throw new java.lang.UnsupportedOperationException("Method setLogWriter() not yet implemented.");
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		/**@todo Implement this javax.sql.DataSource method*/
		throw new java.lang.UnsupportedOperationException("Method setLoginTimeout() not yet implemented.");
	}

	public int getLoginTimeout() throws SQLException {
		/**@todo Implement this javax.sql.DataSource method*/
		throw new java.lang.UnsupportedOperationException("Method getLoginTimeout() not yet implemented.");
	}

	/*
	 * 将指定字符串sql（本地字符集）， 转换为数据库的字符集
	 */
	protected String getEncodingStr(String sql) {
		if (needEncoding()) {
			if (sql == null)
				return null;
			try {
				sql = new String(sql.getBytes(destCharSetEncoding), getCharacterEncoding());
			}
			catch (Exception ex) {
				ExceptionHandler.rethrowRuntimeException(ex);
			}
		}
		return sql;
	}

	/*
	 * 将数据库中取出的字符串，转换为本地字符集格式；
	 */
	protected String getGBKEncodingStr(String sql) {
		if (needEncoding()) {
			if (sql == null)
				return null;
			try {
				sql = new String(sql.getBytes(getCharacterEncoding()), destCharSetEncoding);
			}
			catch (Exception ex) {
				ExceptionHandler.rethrowRuntimeException(ex);
			}
		}
		return sql;
	}

	/**
	 * 客户并不想把数据库的连接信息打印到控制台；
	 * 现在改为返回连接池的名字，为空表示默认连接池；
	 */
	public String getDescription() {
		if (this.poolname == null || this.poolname.length() == 0)
			return I18N.getString("com.esen.jdbc.pool.datasourceimpl.pooldefault", "默认");
		return this.poolname;
		//return this.username+"@"+this.url;
	}
	
	/**
	 * 获取连接池描述信息
	 * @return
	 */
	public String getDesc() {
		return this.desc;
	}

	public String decodeExceptionMessage(String message) {
		//    if (isOracle() && needEncoding())
		//      return getDecodingStr(message);
		return message;
	}

}
