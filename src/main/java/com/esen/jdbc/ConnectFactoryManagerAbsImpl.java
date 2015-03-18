package com.esen.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

import com.esen.jdbc.pool.BaseDataSource;
import com.esen.jdbc.pool.JdbcLogger;
import com.esen.jdbc.pool.PoolPropName;
import com.esen.util.ArrayFunc;
import com.esen.util.MiniProperties;
import com.esen.util.ObjectFactoryBuilderDefault;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * ConnectFactoryManager接口的缺省实现，其他系统最好基于此类实现ConnectFactoryManager接口。
 * 
 * 此类还实现了自身的引用计数的管理，一个此类的实例可以被多个项目引用，他们在不用时都可以执行close
 * 只有引用计数归0时才会真正的close
 * 
 * @author yukun
 */
public abstract class ConnectFactoryManagerAbsImpl implements ConnectFactoryManager {

	/**
	 * 表示此连接池被引用的次数，同一个连接池可能被多个项目引用，如wtap中i和bi
	 */
	private int refcount = 0;

	private HashMap datasources = new HashMap();

	private PoolConnectionFactory dbFactory;

	public ConnectFactoryManagerAbsImpl(MiniProperties defaultDbPoolProperties) {
		super();
		this.init(defaultDbPoolProperties);
	}

	public ConnectFactoryManagerAbsImpl() {
		super();
	}
	
	protected void init(MiniProperties defaultDbPoolProperties) {
		dbFactory = createPoolConnectionFactory(defaultDbPoolProperties, null);
		refcount++;
	}

	public ConnectionFactory getConnectionFactory(String dsname, boolean throwIfNotExists) {
		return this.getDataSource(dsname, throwIfNotExists);
	}

	public synchronized ConnectionFactory getDefaultConnectionFactory() {
		return this.dbFactory;
	}

	/**
	 * 获得一个数据库链接，获得链接后必须马上（强调必须马上）使用，由于数据库链接是希缺资源，所以
	 * 不要出现占用数据库链接资源而又不使用或是很慢的使用的情况，使用完毕后必须调用close方法，
	 * 使用过程中创建的Statement和PreparedStatement也必须关闭。
	 * @return
	 */
	public Connection getDefaultConnection() throws SQLException {
		return this.getDefaultConnectionFactory().getConnection();
	}

	/**
	 * 增加一个引用计数，只有引用计数归0后，连接池才会被真正关闭
	 * @return
	 */
	public synchronized int incRefcount() {
		return ++this.refcount;
	}

	/**
	 * 减少一个引用计数，只有引用计数归0后，连接池才会被真正关闭
	 * @return
	 */
	private synchronized int decRefcount() {
		return --this.refcount;
	}

	public synchronized int getRefcount() {
		return this.refcount;
	}

	/**
	 * 减少一个引用计数，如果引用计数归0，连接池才会被真正关闭
	 */
	public synchronized void close() {
		if (decRefcount() <= 0) {
			realclose();
		}
	}

	/**
	 * 执行真正的关闭操作，不考虑引用计数
	 * @return
	 */
	public synchronized void realclose() {
		if (dbFactory != null) {
			try {
				dbFactory.getDataSource().close();
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			dbFactory = null;
		}
		if (datasources != null) {
			closeOtherDatasources();
			datasources = null;
		}
	}

	public synchronized void closeOtherDatasources() {
		for (Iterator dbs = datasources.values().iterator(); dbs.hasNext();) {
			try {
				PoolConnectionFactory db = (PoolConnectionFactory) dbs.next();
				db.getDataSource().close();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		datasources.clear();
	}

	public synchronized void addDataSource(String dsname, PoolConnectionFactory dbf) {
		if (StrFunc.isNull(dsname) || isDefaultDataSource(dsname)) {
			//			throw new RuntimeException("数据库连接池名称不能为空");
			throw new RuntimeException(I18N.getString("com.esen.jdbc.connectfactorymanagerabsimpl.notnull4pool",
					"数据库连接池名称不能为空"));
		}
		dsname = dsname.toLowerCase();
		if (datasources.containsKey(dsname)) {
			//			throw new RuntimeException("数据库连接池已存在：" + dsname);
			throw new RuntimeException(I18N.getString("com.esen.jdbc.connectfactorymanagerabsimpl.poolexist",
					"数据库连接池已存在：{0}", new Object[] { dsname }));
		}
		datasources.put(dsname, dbf);
	}

	public void removeDataSource(String dsname) throws SQLException {
		if (isDefaultDataSource(dsname)) {
			//			throw new RuntimeException("缺省数据库连接池不能删除");
			throw new RuntimeException(I18N.getString("com.esen.jdbc.connectfactorymanagerabsimpl.deldefaultpool",
					"缺省数据库连接池不能删除"));
		}
		dsname = dsname.toLowerCase();
		PoolConnectionFactory dbf;
		synchronized (this) {
			dbf = (PoolConnectionFactory) (datasources != null ? datasources.remove(dsname) : null);
		}

		if (dbf != null) {
			dbf.getDataSource().close();
		}
	}

	/**
	 * 保存数据库连接池的配置，dsname如果是null或空那么将保存到缺省的配置中
	 */
	public void setDataSource(String dsname, MiniProperties props, boolean addit) {
		PoolConnectionFactory dbf;
		synchronized (this) {
			dbf = getDataSource(dsname, false);
			if (dbf == null) {
				if (!addit) {
					//					throw new RuntimeException("数据库连接池不存在：" + dsname);
					throw new RuntimeException(I18N.getString(
							"com.esen.jdbc.connectfactorymanagerabsimpl.poolnotexist",
							"数据库连接池不存在：{0}", new Object[] { dsname }));
				}
				if (isDefaultDataSource(dsname)) {
					//					throw new RuntimeException("请指定新添加的数据库连接池的名称");
					throw new RuntimeException(I18N.getString("com.esen.jdbc.connectfactorymanagerabsimpl.namepool",
							"请指定新添加的数据库连接池的名称"));
				}
				addDataSource(dsname, createPoolConnectionFactory(props, dsname));
				return;
			}
		}
		dbf.getDataSource().setProperties(props);
	}

	public synchronized PoolConnectionFactory getDataSource(String dsname, boolean throwIfNotExists) {
		PoolConnectionFactory r = null;
		if (isDefaultDataSource(dsname)) {
			/**
			 * 如果子类不重载ConnectFactoryManagerAbsImpl(MiniProperties defaultDbPoolProperties)函数，那么会导致
			 * dbFactory没被赋值， 
			 */
			return (PoolConnectionFactory) getDefaultConnectionFactory();
		}
		/**
		 * 写入连接池名称是小写，读取也要转成小写；
		 */
		r = (PoolConnectionFactory) (datasources != null ? datasources.get(dsname.toLowerCase()) : null);
		if (r == null && throwIfNotExists) {
			//					throw new RuntimeException("数据库连接池不存在：" + dsname);
			throw new RuntimeException(I18N.getString("com.esen.jdbc.connectfactorymanagerabsimpl.poolnotexist",
					"数据库连接池不存在：{0}", new Object[] { dsname }));
		}
		return r;
	}

	public synchronized MiniProperties getDataSourceProperties(String dsname, boolean throwIfNotExists) {
		BaseDataSource bds = getDataSource(dsname, throwIfNotExists).getDataSource();
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
		//设置描述信息
		dataSources.setString(PoolPropName.PROP_DESC, bds.getDesc());
		return dataSources;
	}

	/**
	 * 返回所有数据库连接池的名称，缺省的数据库连接池也在其中，他的名字是null
	 */
	public synchronized String[] getConnectionFactoryNames() {
		String[] r = new String[datasources.size() + 1];
		r[0] = null;
		if (r.length > 1) {
			Object[] dses = datasources.keySet().toArray();
			System.arraycopy(dses, 0, r, 1, dses.length);
		}
		//ESENFACE-1827: 将连接池 名字按字符a-z排序。by chxb
		ArrayFunc.bubbleSort(r, ComparatorAsc.instance);
		return r;
	}
	
	final static class ComparatorAsc implements Comparator {
		public static ComparatorAsc instance = new ComparatorAsc();
		public int compare(Object arg0, Object arg1) {
			if( arg0==null && arg1==null ) return 0;
			if( arg0==null && arg1!=null ) return Integer.MIN_VALUE;
			if( arg0!=null && arg1==null ) return Integer.MAX_VALUE;
			String s0 = arg0.toString();
			String s1 = arg1.toString();
			return s0.compareTo(s1);
		}
	}

	protected void encryptPassword(MiniProperties props) {
		props.setString(PoolPropName.PROP_PASSWORD,
				StrFunc.encryptPlainPassword(props.getString(PoolPropName.PROP_PASSWORD)));
	}

	public static void decryptPassword(Properties props) {
		props.setProperty(PoolPropName.PROP_PASSWORD,
				StrFunc.decryptPlainPassword((String) props.get(PoolPropName.PROP_PASSWORD)));
	}

	protected boolean isDefaultDataSource(String dsname) {
		return dsname == null || dsname.length() == 0;
	}

	protected PoolConnectionFactory createPoolConnectionFactory(Properties props, String poolname) {
		decryptPassword(props);
		BaseDataSource dataSource = new BaseDataSource(props);
		if (poolname != null)
			dataSource.setName(poolname);
		return new PoolConnectionFactory(dataSource);
	}

	/**
	 * 初始化其他的数据库连接。
	 */
	public synchronized void loadOtherDataSources(HashMap m) throws SQLException {
		closeOtherDatasources();
		if (m == null)
			return;
		Iterator it = m.entrySet().iterator();
		while (it.hasNext()) {
			Entry dsentry = (Entry) it.next();
			String dsname = (String) dsentry.getKey();
			/**
			 * 这里的连接池名肯定不为空，TODO 添加连接池时是转换为小写了加入的，而获取连接池，是直接根据dsname 获取的，这里可能会有
			 * 问题，但大部分情形是不会出现该问题。
			 */
			if (StrFunc.isNull(dsname) || datasources.containsKey(dsname.toLowerCase())) {
				continue;
			}

			MiniProperties props = (MiniProperties) dsentry.getValue();
			/**
			 * 这里不能用setDataSource(dsname, props, true),该方法是新增和修改连接池时调用的.
			 * 调用此方法会导致在启动服务器时,修改资源管理器对应的jdbc_XXX.conf文件
			 * 
			 * 这里只是将连接池加入到连接池列表中,调用addDataSource即可.
			 */
			addDataSource(dsname, createPoolConnectionFactory(props, dsname));
		}
	}

	/**
	 * 集成开发时可以在objfactory.properties文件中设定一个ObjectFactory接口的实现者类名，用于产生需要用到的连接池管理类
	 * 参考ObjectFactoryBuilderDefault类的相关说明
	 * 根据环境变量中的设置创建默认的ConnectFactoryManager接口实现类，如果没有在环境变量中设置，那么返回null
	 * 
	 * @return
	 */
	public static ConnectFactoryManager createInstance() {
		try {
			//BI.JDBCMGRFACTORY
			return (ConnectFactoryManager) ObjectFactoryBuilderDefault.getInstance().createObject("BI.JDBCMGRFACTORY");
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
