package com.esen.vfs2;

import java.util.Properties;

import junit.framework.TestCase;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.ibatis.EsenSqlMapClient;
import com.esen.jdbc.ibatis.SqlMapClientFactory;
import com.esen.jdbc.ibatis.SqlMapConfigParser_For_Esen;
import com.esen.util.cluster.Cluster;
import com.esen.vfs2.impl.VfsOperatorImpl;

import func.jdbc.FuncConnectionFactory;

/**
 * vfs集群测试.主要测试vfs缓存对集群的支持
 * 
 *
 * @author zhuchx
 */
public class TestVfsCacheCluster extends TestCase {
	private static String TABLENAME = "TEST_VFS";

	private ConnectionFactory dbfct;

	private Vfs2 createVfs2(Cluster ctrl) {
		Properties sqlMapProps = new Properties();
		sqlMapProps.setProperty("VFSTABLENAME", TABLENAME);

		Properties config = new Properties();
		config.setProperty(SqlMapConfigParser_For_Esen.CACHEENABLE, String.valueOf(false));

		Properties props = new Properties();
		props.setProperty(Vfs2DB.VFS_CACHE, "com.esen.vfs2.TestVfsCache");

		TestVfsCache.setClusterCtrlAbs(ctrl);

		SqlMapClientFactory.setFacoty(new SqlMapClientFactory());
		EsenSqlMapClient smc = SqlMapClientFactory.getInstance().createSqlMapClient(getConnectionFactory(),
				sqlMapProps, "com/esen/vfs2/sqlmapconfig-vfs.xml", config);
		Vfs2DB vfs = new Vfs2DB(getConnectionFactory(), TABLENAME, props, smc);
		return vfs;
	}

	private ConnectionFactory getConnectionFactory() {
		if (dbfct == null) {
			dbfct = FuncConnectionFactory.getOracleCustomConnectionFactory();
		}
		return dbfct;
	}

	public VfsFile2 getVfsFile(Vfs2 vfs, String filename) {
		return vfs.getVfsFile(filename, admin);
	}

	private VfsOperator admin = new VfsOperatorImpl("admin", true);

	/**
	 * ==========================================================================
	 * 测试
	 * ==========================================================================
	 */

	/**
	 * 测试集群发送和获得信息 
	 * 
	 * 此测试用例的测试结果是在控制台中会输出两个++++++++++++++++++++++++++++++/,两个++++++++++++++++++++++++++++++/abc123
	 * 如果有上面两个输出则是正确的
	 */
	public void test() throws Exception {/*
		Properties props = ClusterMgr.loadClusterProperties();
		ClusterCtrlJGroupsImpl node1 = new ClusterCtrlJGroupsImpl(props);
		ClusterCtrlJGroupsImpl node2 = new ClusterCtrlJGroupsImpl(props);
		node1.start();
		node2.start();

		Vfs2 vfs1 = createVfs2(node1);
		Vfs2 vfs2 = createVfs2(node2);//会自动注册监听器

		VfsFile2 file = getVfsFile(vfs1, "/abc123");
		if (file.exists()) {
			file.remove();
		}
		file.ensureExists(false);
		*//**
		 * 这里要睡眠一段时间,否则传到其它node2的信息还没有处理,整个测试就已经结束了
		 *//*
		Thread.currentThread().sleep(5000);
	*/}
}
