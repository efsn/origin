package com.esen.vfs2;

import java.util.Properties;

import junit.framework.TestCase;

import com.esen.jdbc.ConnectFactoryManagerImpl;
import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.ibatis.EsenSqlMapClient;
import com.esen.jdbc.ibatis.SqlMapClientFactory;
import com.esen.vfs2.impl.VfsOperatorImpl;

public class TestVfs2 extends TestCase {

	private static ConnectionFactory getConnectionFactory() {
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
		return fct;
	}

	private VfsOperator admin = new VfsOperatorImpl("admin", true);

	private static String TABLENAME = "TEST_VFS1";

	private Vfs2 vfs = new Vfs2DB(getConnectionFactory(), TABLENAME, null, getClient());

	private EsenSqlMapClient getClient() {
		Properties props = new Properties();
		props.setProperty("VFSTABLENAME", TABLENAME);

		Properties conf = new Properties();

		SqlMapClientFactory.setFacoty(new SqlMapClientFactory());
		EsenSqlMapClient scf = SqlMapClientFactory.getInstance().createSqlMapClient(getConnectionFactory(), props,
				"com/esen/vfs2/sqlmapconfig-vfs.xml", null);
		return scf;
	}

	private Vfs2 getVfs() {
		return vfs;
	}

	public static void main(String[] args) {
		TestVfs2 t = new TestVfs2();
	}

	/**
	 * =================== 开始测试 ===================
	 */

	public void testGetVfsFile() {
		fail("Not yet implemented");
	}

	public void testGetRoot() {
		fail("Not yet implemented");
	}

	public void testGetFileAsString() {
		fail("Not yet implemented");
	}

	public void testSetFileAsString() {
		fail("Not yet implemented");
	}

	public void testGetFileAsBytes() {
		fail("Not yet implemented");
	}

	public void testSetFileAsBytes() {
		fail("Not yet implemented");
	}

	public void testRemoveFile() {
		fail("Not yet implemented");
	}

	public void testStartTransaction() {
		fail("Not yet implemented");
	}

	public void testCommitTransaction() {
		fail("Not yet implemented");
	}

	public void testEndTransaction() {
		fail("Not yet implemented");
	}

	public void testGetContentType() {
		fail("Not yet implemented");
	}

	public void testGetNamespace() {
		fail("Not yet implemented");
	}

	public void testSetNamespace() {
		fail("Not yet implemented");
	}

	public void testMount() {
		fail("Not yet implemented");
	}

	public void testUnmount() {
		fail("Not yet implemented");
	}

	public void testAddListener() {
		fail("Not yet implemented");
	}

	public void testRemoveListener() {
		fail("Not yet implemented");
	}

	public void testSetSecurityMgr() {
		fail("Not yet implemented");
	}

	public void testLock() {
		fail("Not yet implemented");
	}

	public void testUnlock() {
		fail("Not yet implemented");
	}

}
