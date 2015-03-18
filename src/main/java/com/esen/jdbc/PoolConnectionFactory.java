package com.esen.jdbc;

import com.esen.jdbc.dialect.*;
import com.esen.jdbc.dialect.impl.DbMetaDataCacheImpl;
import com.esen.jdbc.pool.*;

import java.sql.*;
import java.util.Properties;


/**
 * <p>利用BaseDataSource对象构建一个ConnectionFactory接口</p>
 * 
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: EsenSoft</p>
 * @author dw
 * @version 1.0
 */

public class PoolConnectionFactory implements ConnectionFactory {
	private BaseDataSource datasource;

	private DbMetaData dbmeta;

	/**
	 * 根据属性列表props构造连接池；
	 * @param poolname 
	 *        连接池的名字，可以传null;
	 * @param props
	 *        连接池的属性集合；
	 */
	public PoolConnectionFactory(String poolname,Properties props){
		BaseDataSource ds = new BaseDataSource(props);
		ds.setName(poolname);
		setDataSource(ds);
	}
	
	/**
	 * 创建连接池的方法现在最好不要使用BaseDataSource这个类；
	 * @deprecated
	 * @param datasource
	 */
	public PoolConnectionFactory(BaseDataSource datasource) {
		setDataSource(datasource);
	}

	/**
	 * 用于修改连接池属性；
	 * @param props
	 */
	public void setProperties(Properties props) {
		datasource.setProperties(props);
	}
	
	public Connection getConnection() throws SQLException {
		/**
		 * BI-5525 essbase服务器重启后，再创建cube主题表，提示： 
		 * com.esen.olap2j.OlapException: 会话 ID 无效。可能是因为服务器进行了重新启动。请注销，然后重新登录。
		 * 
		 * 所以从连接池获取mdx服务连接，不在使用连接池，直接创建新的连接。
		 */
		if(SqlFunc.isMdx(this)){
			return getNewConnection();
		}
		return datasource.getConnection();
	}

	public Connection getNewConnection() throws SQLException {
		return datasource.createRealPooledConnection();
	}

	public DbDefiner getDbDefiner() {
		return getDialect().createDbDefiner();
	}

	protected void setDataSource(BaseDataSource datasource) {
		this.datasource = datasource;
		this.dbmeta = new DbMetaDataCacheImpl(this);
	}

	public BaseDataSource getDataSource() {
		return this.datasource;
	}

	public Dialect getDialect() {
		return DialectFactory.createDialect(this);
	}

	public DataBaseInfo getDbType() {
		return datasource.getDbType();
	}

	public boolean compareDataBaseTo(ConnectionFactory cf) {
		if (cf instanceof PoolConnectionFactory) {
			BaseDataSource ds1 = this.getDataSource();
			BaseDataSource ds2 = ((PoolConnectionFactory) cf).getDataSource();
			String url = ds1.getUrl();
			String id = ds1.getUsername();
			/**
			 * 20091202
			 * 用户名确定，密码就确定了，不存在相同的用户名，密码却不同的数据库用户；
			 * 不需要比较密码；
			 */
			if (url != null && url.equalsIgnoreCase(ds2.getUrl()) && id != null
					&& id.equalsIgnoreCase(ds2.getUsername())) {
				return true;
			}
		}
		return false;
	}

	public String getName() {
		return datasource.getName();
	}

	public DbMetaData getDbMetaData() {
		return dbmeta;
	}

	public JdbcConnectionDebug getJdbcConnectionDebug() {
	  return datasource.getJdbcConnectionDebug();
  }
}