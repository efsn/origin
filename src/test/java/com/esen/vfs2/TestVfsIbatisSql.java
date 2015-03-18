package com.esen.vfs2;

import java.sql.SQLException;
import java.util.Properties;

import com.esen.jdbc.ConnectFactoryManagerImpl;
import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.ibatis.EsenSqlMapClient;
import com.esen.jdbc.ibatis.SqlMapClientFactory;
import com.esen.util.StringMap;
import com.esen.vfs2.impl.VfsCacheImpl;
import com.esen.vfs2.impl.VfsNode;

public class TestVfsIbatisSql {
	public static void main(String[] args) throws Exception {
		TestVfsIbatisSql t = new TestVfsIbatisSql();
		t.test();
	}

	private ConnectionFactory dbfct;

	private ConnectionFactory getConnectionFactory() {
		if (dbfct == null) {
			String driver = "com.mysql.jdbc.Driver";
			String url = "jdbc:mysql://localhost:3306/vfs?useUnicode=true&characterEncoding=utf8";
			//    String driver = "oracle.jdbc.driver.OracleDriver";
			//    String url = "jdbc:oracle:thin:@192.168.1.200:1521:dbdev1";
			String user = "test";
			String pw = "test";
			SimpleConnectionFactory fct = new SimpleConnectionFactory(driver, url, user, pw, "debug");
			ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
			fm.setConnectionFactory("*", fct);
			DefaultConnectionFactory.set(fm);
			dbfct = fct;
		}
		return dbfct;
	}

	private static String TABLENAME = "TEST_VFS";

	private EsenSqlMapClient sqlMapClient;

	private EsenSqlMapClient getClient() {
		if (sqlMapClient == null) {
			Properties props = new Properties();
			props.setProperty("VFSTABLENAME", TABLENAME);

			Properties conf = new Properties();

			SqlMapClientFactory.setFacoty(new SqlMapClientFactory());
			sqlMapClient = SqlMapClientFactory.getInstance().createSqlMapClient(getConnectionFactory(), props,
					"com/esen/vfs2/SqlMapConfig.xml", conf);
		}
		return sqlMapClient;
	}

	private VfsNodeOperDB operDb;

	private VfsNodeOperDB getOperDb() {
		if (operDb == null) {
			StringMap options = new StringMap();
			options.put("tablename", TABLENAME);
			operDb = new VfsNodeOperDB(getConnectionFactory(), getClient(), new VfsCacheImpl(), options);
		}
		return operDb;
	}

	private VfsNode getTestNodeDirParent() {
		return getOperDb().createNode("/", "p", false, "admin");
	}

	private VfsNode getTestNodeDir() {
		return getOperDb().createNode("/p/", "d", false, "admin");
	}

	private VfsNode getTestNodeDirSubFile() {
		return getOperDb().createNode("/p/d/", "f", false, "admin");
	}

	private VfsNode getTestNodeFile() {
		return getOperDb().createNode("/p/", "f", true, "admin");
	}

	private VfsNode getTestNodeRoot() {
		return getOperDb().createNode("/", "/", true, "admin");
	}

	/**
	 * =============== 开始测试 ===============
	 */

	private void test() throws Exception {
		testDelete();
	}

	private void testAll() throws Exception {
		testInsert();
		testSelect();
		testUpdate();
		testRename();
		testCopy();
		testList();
		testDelete();
	}

	private void testInsert() throws SQLException {
		VfsNodeOperDB oper = getOperDb();
		oper.remove(getTestNodeRoot());
		oper.createFile(getTestNodeDir());
		oper.createFile(getTestNodeFile());
	}

	private void testSelect() throws SQLException {
		VfsNodeOperDB oper = getOperDb();
		oper.remove(getTestNodeRoot());
		oper.createFile(getTestNodeDir());
		oper.createFile(getTestNodeFile());

		VfsNode node = getTestNodeDir();
		VfsNode dir = oper.getNode(node.getParentDir(), node.getFileName());
		oper.getNode(node.getParentDir(), node.getFileName());
		node = getTestNodeFile();
		VfsNode file = oper.getNode(node.getParentDir(), node.getFileName());
		oper.getNode(node.getParentDir(), node.getFileName());
	}

	private void testUpdate() throws SQLException {
		VfsNodeOperDB oper = getOperDb();
		oper.remove(getTestNodeRoot());
		oper.createFile(getTestNodeDir());
		oper.createFile(getTestNodeFile());

		oper.setProperties(getTestNodeDir());
		oper.setProperties(getTestNodeFile());
		oper.writeContent(getTestNodeDir());
		oper.writeContent(getTestNodeFile());
	}

	private void testRename() throws SQLException {
		VfsNodeOperDB oper = getOperDb();
		oper.remove(getTestNodeRoot());
		oper.remove(getTestNodeRoot());
		oper.createFile(getTestNodeDir());
		oper.createFile(getTestNodeFile());
		oper.createFile(getTestNodeDirSubFile());

		VfsNode node = getTestNodeDir();
		oper.renameTo(node, "/r/", "td", false);
		node = getTestNodeFile();
		oper.renameTo(node, "/r/", "tf", false);
	}

	private void testCopy() throws SQLException {
		VfsNodeOperDB oper = getOperDb();
		oper.remove(getTestNodeRoot());
		oper.createFile(getTestNodeDir());
		oper.createFile(getTestNodeFile());
		oper.createFile(getTestNodeDirSubFile());

		VfsNode node = getTestNodeDir();
		oper.copyTo(node, "/r/", "td");
		node = getTestNodeFile();
		oper.copyTo(node, "/r/", "tf");
	}

	private void testList() throws SQLException {
		VfsNodeOperDB oper = getOperDb();
		oper.remove(getTestNodeRoot());
		oper.createFile(getTestNodeDir());
		oper.createFile(getTestNodeFile());
		oper.createFile(getTestNodeDirSubFile());

		oper.listFiles(getTestNodeDirParent());
	}

	private void testDelete() throws SQLException {
		VfsNodeOperDB oper = getOperDb();
		oper.remove(getTestNodeDir());
		oper.remove(getTestNodeFile());
		oper.remove(getTestNodeRoot());
	}

}
