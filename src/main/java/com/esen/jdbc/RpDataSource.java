package com.esen.jdbc;

import java.sql.SQLException;
import java.util.Properties;

import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.pool.BaseDataSource;
import com.esen.util.ExceptionHandler;
import com.esen.util.StrFunc;
import com.esen.util.StringMap;
import com.esen.util.i18n.I18N;

/**
 * @author daqun
 * 简单封装BaseDataSource和ConnectionFactory对象
 *
 */
public class RpDataSource {
	private long maxWait;

	private int minIdle;

	private String username;

	private String password;

	private int maxActive;

	private String url;

	private String driverClassName;

	private boolean isDirectConn;

	/**
	 * 第三方连接池 datasource context name；
	 * datasource参数不为空，则从第三方连接池取得连接；
	 */
	private String datasource;

	private boolean autoCommit;

	private boolean debug;

	private String catalog;

	/**
	 * 标示数据库类型的字符串。
	 */
	private DataBaseInfo dbType;

	private BaseDataSource dataSource;

	private ConnectionFactory dbFactory;

	private String ds3name;

	/**
	 * 设置字符集
	 */
	private String characterEncoding = null;

	public RpDataSource() {
	}

	public RpDataSource(StringMap attrs) {
		this.loadAttrs(attrs);
	}

	public synchronized boolean open() {
		return this.getDbFactory() != null;
	}

	/**
	 * @return 此连接是否有效
	 */
	public synchronized boolean isActive() {
		return dbFactory != null;
	}

	public synchronized boolean isAutoCommit() {
		return autoCommit;
	}

	public synchronized void setAutoCommit(boolean autoCommit) {
		mustClosed();
		this.autoCommit = autoCommit;
	}

	public synchronized String getCatalog() {
		return catalog;
	}

	public synchronized void setCatalog(String catalog) {
		mustClosed();
		this.catalog = catalog;
	}

	public synchronized String getCharacterEncoding() {
		return characterEncoding;
	}

	public synchronized void setCharacterEncoding(String characterEncoding) {
		mustClosed();
		this.characterEncoding = characterEncoding;
	}

	public synchronized boolean isDebug() {
		return debug;
	}

	public synchronized void setDebug(boolean debug) {
		mustClosed();
		this.debug = debug;
	}

	public synchronized String getDriverClassName() {
		return driverClassName;
	}

	public synchronized void setDriverClassName(String driverClassName) {
		mustClosed();
		this.driverClassName = driverClassName;
	}

	public synchronized boolean isDirectConn() {
		return isDirectConn;
	}

	public synchronized void setDirectConn(boolean isDirectConn) {
		mustClosed();
		this.isDirectConn = isDirectConn;
	}

	public synchronized int getMaxActive() {
		return maxActive;
	}

	public synchronized void setMaxActive(int maxActive) {
		mustClosed();
		this.maxActive = maxActive;
	}

	public synchronized long getMaxWait() {
		return maxWait;
	}

	public synchronized void setMaxWait(long maxWait) {
		mustClosed();
		this.maxWait = maxWait;
	}

	public synchronized int getMinIdle() {
		return minIdle;
	}

	public synchronized void setMinIdle(int minIdle) {
		mustClosed();
		this.minIdle = minIdle;
	}

	public synchronized String getPassword() {
		return password;
	}

	public synchronized void setPassword(String password) {
		mustClosed();
		this.password = password;
	}

	public synchronized String getUrl() {
		return url;
	}

	public synchronized void setUrl(String url) {
		mustClosed();
		this.url = url;
	}

	public synchronized String getUsername() {
		return username;
	}

	public synchronized void setUsername(String username) {
		mustClosed();
		this.username = username;
	}

	public synchronized DataBaseInfo getDbType() {
		return dbType;
	}

	private void mustClosed() {
		if (this.isActive())
			//      throw new RuntimeException("必须先关闭连接,才能进行写操作!");
			throw new RuntimeException(I18N.getString("com.esen.jdbc.rpdatasource.mustclose",
					"必须先关闭连接,才能进行写操作!"));
	}

	public void close() throws SQLException {
		if (dataSource != null) {
			dataSource.close();
			dataSource = null;
		}
		this.dbFactory = null;
	}

	synchronized private BaseDataSource getDataSource() throws SQLException {
		if (dataSource != null) {
			return dataSource;
		}
		dataSource = new BaseDataSource();
		Properties props = new Properties();

		props.setProperty("url", url);
		props.setProperty("driverClassName", driverClassName);
		props.setProperty("username", username);
		props.setProperty("password", password);
		props.setProperty("logLevel", debug ? "debug" : "error");
		props.setProperty("catalog", catalog);
		props.setProperty("defaultAutoCommit", String.valueOf(autoCommit));
		props.setProperty("maxActive", String.valueOf(maxActive));
		props.setProperty("minIdle", String.valueOf(minIdle));
		props.setProperty("maxWait", String.valueOf(maxWait));
		props.setProperty("datasource", StrFunc.null2blank(datasource));
		props.setProperty("characterEncoding", characterEncoding);
		dataSource.setProperties(props);

		dbType = dataSource.getDbType();
		return dataSource;
	}

	public synchronized ConnectionFactory getDbFactory() {
		if (dbFactory == null) {
			try {
				BaseDataSource ds = getDataSource();
				dbFactory = new PoolConnectionFactory(ds);
			}
			catch (Exception ex) {
				try {
					this.close();
				}
				catch (Exception exe) {
					exe.printStackTrace();
				}
				ExceptionHandler.rethrowRuntimeException(ex);
			}
		}
		return dbFactory;
	}

	public synchronized void loadFromFile(String fn) throws Exception {
		StringMap conf = new StringMap();
		conf.loadFromFile(fn);
		this.loadAttrs(conf);
	}

	public synchronized void loadAttrs(StringMap props) {
		mustClosed();

		maxWait = props.getInt("maxWait", 10000);
		minIdle = props.getInt("minIdle", 5);
		username = props.getValue("username");
		password = props.getValue("password");
		maxActive = props.getInt("maxActive", 20);
		url = props.getValue("url", null);
		driverClassName = props.getValue("driverClassName");
		datasource = props.getValue("datasource", ds3name);
		autoCommit = props.getBool("defaultAutoCommit", true);
		debug = props.getBool("isDebug", false);
		catalog = props.getValue("catalog");
		isDirectConn = props.getBool("isDirectConn", ds3name == null || ds3name.length() == 0);
		characterEncoding = props.getValue("characterEncoding");
	}

	public synchronized StringMap getAttrs(StringMap props) {
		props.setValue("maxWait", this.maxWait);
		props.setValue("minIdle", this.minIdle);
		props.setValue("username", this.username);
		props.setValue("password", this.password);
		props.setValue("maxActive", this.maxActive);
		props.setValue("url", this.url);
		props.setValue("driverClassName", this.driverClassName);
		props.setValue("datasource", this.ds3name);
		props.setValue("defaultAutoCommit", this.autoCommit);
		props.setValue("isDebug", this.debug);
		props.setValue("catalog", this.catalog);
		props.setValue("characterEncoding", this.characterEncoding);
		return props;
	}

	public synchronized StringMap getAttrs() {
		return getAttrs(new StringMap());
	}

}
