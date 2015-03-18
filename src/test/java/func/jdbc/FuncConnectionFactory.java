package func.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;

import com.esen.jdbc.ConnectFactoryManager;
import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.PoolConnectionFactory;
import com.esen.jdbc.pool.BaseDataSource;
import com.esen.jdbc.pool.JdbcLogger;
import com.esen.jdbc.pool.PoolPropName;
import com.esen.util.ArrayFunc;
import com.esen.util.MiniProperties;
import com.esen.util.StrFunc;

import func.FuncSys;

public abstract class FuncConnectionFactory {

	public static final String DB_ORACLE = "oracle";

	public static final String DB_MYSQL = "mysql";

	public static final String DB_MSSQL = "mssql";

	public static final String DB_MSSQL2005 = "mssql2005";

	public static final String DB_DB2 = "db2";

	public static final String DB_SYBASE = "sybase";

	public static final String DB_SYBASEIQ = "sybaseiq";

	public static final String DB_DM = "dm";

	/**
	 * {ip,database,user,pw,port}
	 */
	protected static final String[][] CUSTOMJDBC = { //
	{ null, "test", "testcase", "testcase", null },//oracle
			{ null, "testcase", "testcase", "testcase", "3307" },//mysql
			{ "192.168.1.21", "test", "testcase", "testcase", null },//mssql
			{ "192.168.1.51", "test", "testcase", "testcase", "1434" },//mssql2005
			{ "192.168.1.51", "testdb", "testcase", "testcase", null },//db2
			{ "192.168.1.51", "test", "testcase", "testcase", null },//sybase
			{ "192.168.1.224", "testdb", "testcase", "testcase", "2638" }, //sybaseiq
			{ "192.168.1.224", "testdb", "testcase", "testcase", "12345" }, //达梦
	};

	public static final String DRIVER_DB2 = "com.ibm.db2.jcc.DB2Driver";

	public static final String DRIVER_MSSQL = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

	public static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";

	public static final String DRIVER_ORALCE = "oracle.jdbc.driver.OracleDriver";

	public static final String DRIVER_SYBASE = "com.sybase.jdbc3.jdbc.SybDriver";

	public static final String DRIVER_DM = "dm.jdbc.driver.DmDriver";

	public static FuncConnectionFactory getInstance(String dbname) {
		if (DB_ORACLE.equalsIgnoreCase(dbname)) {
			return getOracleInstance();
		}
		else if (DB_MYSQL.equalsIgnoreCase(dbname)) {
			return getMysqlInstance();
		}
		else if (DB_MSSQL.equalsIgnoreCase(dbname)) {
			return getMssqlInstance();
		}
		else if (DB_MSSQL2005.equalsIgnoreCase(dbname)) {
			return getMssql2005Instance();
		}
		else if (DB_DB2.equalsIgnoreCase(dbname)) {
			return getDb2Instance();
		}
		else if (DB_SYBASE.equalsIgnoreCase(dbname)) {
			return getSybaseInstance();
		}
		else if (DB_SYBASEIQ.equalsIgnoreCase(dbname)) {
			return getSybaseiqInstance();
		}
		else if (DB_DM.equalsIgnoreCase(dbname)) {
			return getDMInstance();
		}
		throw new RuntimeException("不支持的数据库类型:" + dbname);
	}

	public static FuncConnectionFactory getOracleInstance() {
		return new FuncConnectionFactoryOracle();
	}

	public static FuncConnectionFactory getMysqlInstance() {
		return new FuncConnectionFactoryMysql();
	}

	public static FuncConnectionFactory getMssqlInstance() {
		return new FuncConnectionFactoryMssql();
	}

	public static FuncConnectionFactory getMssql2005Instance() {
		return new FuncConnectionFactoryMssql2005();
	}

	public static FuncConnectionFactory getDb2Instance() {
		return new FuncConnectionFactoryDb2();
	}

	public static FuncConnectionFactory getSybaseInstance() {
		return new FuncConnectionFactorySybase();
	}

	public static FuncConnectionFactory getSybaseiqInstance() {
		return new FuncConnectionFactorySybaseiq();
	}

	public static FuncConnectionFactory getDMInstance() {
		return new FuncConnectionFactoryDM();
	}

	public static FuncSimpleConnectionFactory getConnectionFactory(String driver, String url, String user, String pw,
			boolean debug, boolean asDefault) {
		return getConnectionFactory(null, driver, url, user, pw, debug, asDefault);
	}

	public static FuncSimpleConnectionFactory getConnectionFactory(String name, String driver, String url, String user,
			String pw, boolean debug, boolean asDefault) {
		if (StrFunc.isNull(url) || StrFunc.isNull(user) || StrFunc.isNull(pw))
			return null;
		FuncSimpleConnectionFactory fct = null;
		if (debug) {
			fct = new FuncSimpleConnectionFactory(name, driver, url, user, pw, "debug");
		}
		else {
			fct = new FuncSimpleConnectionFactory(name, driver, url, user, pw);
		}
		FuncConnectFactoryManager fm = getConnectionFactoryManager();
		fm.setConnectionFactory(name, fct);
		if (asDefault) {
			setDefault(fct);
		}
		return fct;
	}

	public static void setDefault(ConnectionFactory fct) {
		FuncConnectFactoryManager fm = getConnectionFactoryManager();
		fm.setConnectionFactory("*", fct);
		fm.setConnectionFactory(null, fct);
		fm.setConnectionFactory("", fct);
	}

	private static FuncConnectFactoryManager getConnectionFactoryManager() {
		ConnectFactoryManager fm = (ConnectFactoryManager) FuncSys.getDeepDeclaredFieldValue(
				DefaultConnectionFactory.class, "cfm");
		if (fm == null)
			fm = new FuncConnectFactoryManager();
		if (!(fm instanceof FuncConnectFactoryManager)) {
			fm = new FuncConnectFactoryManager(fm);
		}
		DefaultConnectionFactory.set(fm);
		return (FuncConnectFactoryManager) fm;
	}

	public abstract String getName();

	public abstract String getDriver();

	protected abstract String[] getCustom();

	/**
	 * 获得连接池，连接池的驱动根据url来判断时什么数据库
	 */
	public static FuncSimpleConnectionFactory getConnectionFactoryDetectType(String url, String user, String pw,
			boolean debug, boolean asDefault) {
		FuncConnectionFactory ins = null;
		if (url.startsWith("jdbc:db2:")) {
			ins = getDb2Instance();
		}
		else if (url.startsWith("jdbc:oracle:")) {
			ins = getOracleInstance();
		}
		else if (url.startsWith("jdbc:jtds:sqlserver:")) {
			ins = getMssqlInstance();
		}
		else if (url.startsWith("jdbc:mysql:")) {
			ins = getMysqlInstance();
		}
		else if (url.startsWith("jdbc:sybase:Tds:")) {
			ins = getSybaseInstance();
		}
		return ins.getConnectionFactory(url, user, pw, debug, asDefault);
	}

	public FuncSimpleConnectionFactory getConnectionFactory(String url, String user, String pw, boolean debug,
			boolean asDefault) {
		return getConnectionFactory(this.getName(), getDriver(), url, user, pw, debug, asDefault);
	}

	public FuncSimpleConnectionFactory getCustomConnectionFactory(boolean debug, boolean asDefault) {
		return getCustomConnectionFactory(getCustom(), debug, asDefault);
	}

	protected FuncSimpleConnectionFactory getCustomConnectionFactory(String[] jdbc, boolean debug, boolean asDefault) {
		String url = getUrl(jdbc[0], jdbc[1], jdbc[4]);
		String user = jdbc[2];
		String pw = jdbc[3];
		return getConnectionFactory(url, user, pw, debug, asDefault);
	}

	public abstract String getUrl(String ip, String sid, String port);

	protected String getDefaultIp(String ip) {
		return getDefaultValue(ip, "localhost");
	}

	protected String getDefaultPort(String port, String def) {
		return getDefaultValue(port, def);
	}

	private String getDefaultValue(String value, String def) {
		return StrFunc.isNull(value) ? def : value;
	}

	public FuncSimpleConnectionFactory getConnectionFactory(String url, String user, String pw) {
		return getConnectionFactory(url, user, pw, true, true);
	}

	public FuncSimpleConnectionFactory getCustomConnectionFactory() {
		return getCustomConnectionFactory(true, true);
	}

	public static FuncSimpleConnectionFactory getCustomConnectionFactory(String dbname) {
		FuncConnectionFactory cf = getInstance(dbname);
		return cf.getCustomConnectionFactory();
	}

	public static FuncSimpleConnectionFactory getOracleCustomConnectionFactory() {
		FuncConnectionFactory cf = getOracleInstance();
		return cf.getCustomConnectionFactory();
	}

	public static FuncSimpleConnectionFactory getMysqlCustomConnectionFactory() {
		FuncConnectionFactory cf = getMysqlInstance();
		return cf.getCustomConnectionFactory();
	}

	public static FuncSimpleConnectionFactory getMssqlCustomConnectionFactory() {
		FuncConnectionFactory cf = getMssqlInstance();
		return cf.getCustomConnectionFactory();
	}

	public static FuncSimpleConnectionFactory getMssql2005CustomConnectionFactory() {
		FuncConnectionFactory cf = getMssql2005Instance();
		return cf.getCustomConnectionFactory();
	}

	public static FuncSimpleConnectionFactory getDb2CustomConnectionFactory() {
		FuncConnectionFactory cf = getDb2Instance();
		return cf.getCustomConnectionFactory();
	}

	public static FuncSimpleConnectionFactory getSybaseCustomConnectionFactory() {
		FuncConnectionFactory cf = getSybaseInstance();
		return cf.getCustomConnectionFactory();
	}

	public static FuncSimpleConnectionFactory getSybaseIQCustomConnectionFactory() {
		FuncConnectionFactory cf = getSybaseiqInstance();
		return cf.getCustomConnectionFactory();
	}

	public static FuncSimpleConnectionFactory getDMCustomConnectionFactory() {
		FuncConnectionFactory cf = getDMInstance();
		return cf.getCustomConnectionFactory();
	}

	public static FuncSimpleConnectionFactory[] getCustomConnectionFactoryArray() {
		ArrayList list = new ArrayList();
		addCanConnectFactory(getOracleInstance().getCustomConnectionFactory(), list);
		addCanConnectFactory(getMysqlInstance().getCustomConnectionFactory(), list);
		addCanConnectFactory(getMssqlInstance().getCustomConnectionFactory(), list);
		addCanConnectFactory(getMssql2005Instance().getCustomConnectionFactory(), list);
		addCanConnectFactory(getDb2Instance().getCustomConnectionFactory(), list);
		addCanConnectFactory(getSybaseInstance().getCustomConnectionFactory(), list);
		//    addCanConnectFactory(getSybaseiqInstance().getCustomConnectionFactory(), list);
		addCanConnectFactory(getDMInstance().getCustomConnectionFactory(), list);
		return (FuncSimpleConnectionFactory[]) ArrayFunc.list2array(list);
	}

	private static void addCanConnectFactory(ConnectionFactory fct, ArrayList list) {
		if (FuncJdbc.canConnection(fct)) {
			list.add(fct);
		}
	}

	/**
	 * 不使用bi的连接池配置,直接使用原始的Connection
	 */
	public static Connection getOriCustomConnectionFactory(String dbname) throws Exception {
		FuncConnectionFactory fcf = FuncConnectionFactory.getInstance(dbname);
		Class.forName(fcf.getDriver());
		String[] jdbc = fcf.getCustom();
		String url = fcf.getUrl(jdbc[0], jdbc[1], jdbc[4]);
		String user = jdbc[2];
		String pw = jdbc[3];
		return DriverManager.getConnection(url, user, pw);
	}

	public static MiniProperties getProperties(ConnectionFactory fct) {
		if (fct instanceof PoolConnectionFactory) {
			PoolConnectionFactory pfct = (PoolConnectionFactory) fct;
			BaseDataSource bds = pfct.getDataSource();
			MiniProperties dataSources = new MiniProperties();
			dataSources.setString(PoolPropName.PROP_CATALOG, bds.getCatalog());
			dataSources.setString(PoolPropName.PROP_CHARACTERENCODING, bds.getCharacterEncoding());
			dataSources.setString(PoolPropName.PROP_DRIVERCLASSNAME, bds.getDriverClassName());
			dataSources.setString(PoolPropName.PROP_PASSWORD, bds.getPassword());
			dataSources.setString(PoolPropName.PROP_USERNAME, bds.getUsername());
			dataSources.setString(PoolPropName.PROP_URL, bds.getUrl());
			dataSources.setBoolean(PoolPropName.PROP_DEFAULTAUTOCOMMIT, bds.getDefaultAutoCommit());
			dataSources.setString(PoolPropName.PROP_LOGLEVER, JdbcLogger.LOGLEVERSTR[bds.getLogLever()]);
			dataSources.setInt(PoolPropName.PROP_MAXACTIVE, bds.getMaxActive());
			dataSources.setLong(PoolPropName.PROP_MAXWAIT, bds.getMaxWait());
			dataSources.setInt(PoolPropName.PROP_MINIDLE, bds.getMinIdle());
			dataSources.setString(PoolPropName.PROP_DESTCHARSETENCODING, bds.getDestCharSetEncoding());
			dataSources.setString(PoolPropName.PROP_INITSQL, bds.getInitSql());
			return dataSources;
		}
		return null;
	}
}
