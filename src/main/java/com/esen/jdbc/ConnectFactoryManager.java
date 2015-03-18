package com.esen.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import com.esen.util.MiniProperties;

/**
 * 可以访问多个连接池的接口
 * @author yk
 */
public interface ConnectFactoryManager {
	/**
	 * 获得一个连接池对象，dsname大小写不敏感，如果为空，则返回默认的ConnectionFactory
	 * @param dsname
	 * @param throwIfNotExists
	 * @return
	 */
	public ConnectionFactory getConnectionFactory(String dsname, boolean throwIfNotExists);

	public ConnectionFactory getDefaultConnectionFactory();

	/**
	 * 获得一个数据库链接，获得链接后必须马上（强调必须马上）使用，由于数据库链接是希缺资源，所以
	 * 不要出现占用数据库链接资源而又不使用或是很慢的使用的情况，使用完毕后必须调用close方法，
	 * 使用过程中创建的Statement和PreparedStatement也必须关闭。
	 * @return
	 */
	public Connection getDefaultConnection() throws SQLException;

	/**
	 * 返回所有数据库连接池的名称，缺省的数据库连接池也在其中，他的名字是null
	 */
	public String[] getConnectionFactoryNames();

	/**
	 * 关闭所有的连接池，包括缺省连接池
	 */
	public void close();

	/**
	 * 关闭缺省连接池以外的其他连接池
	 */
	public void closeOtherDatasources();

	/**
	 * 添加一个连接池
	 * @param dsname
	 * @param dbf
	 */
	public void addDataSource(String dsname, PoolConnectionFactory dbf);

	/**
	 * 装入其他连接池，不包括默认连接池，转入的Map中key是连接池的名字value是MiniProperties对象，描述jdbc的配置。
	 * m可能为空；
	 * @return
	 */
	public void loadOtherDataSources(HashMap m) throws SQLException;
	
	/**
	 * 删除一个连接池
	 * @param dsname
	 * @throws SQLException
	 */
	public void removeDataSource(String dsname) throws SQLException;

	/**
	 * 保存数据库连接池的配置，dsname如果是null或空那么将保存到缺省的配置中
	 */
	public void setDataSource(String dsname, MiniProperties props, boolean addit);

	/**
	 * @param dsname 如果为空，则返回默认的PoolConnectionFactory
	 * @param throwIfNotExists
	 * @return
	 */
	public PoolConnectionFactory getDataSource(String dsname, boolean throwIfNotExists);

	public MiniProperties getDataSourceProperties(String dsname, boolean throwIfNotExists);
}
