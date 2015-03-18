package com.esen.vfs2;

import java.util.Properties;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.ibatis.EsenSqlMapClient;
import com.esen.jdbc.ibatis.SqlMapClientFactory;
import com.esen.jdbc.ibatis.SqlMapConfigParser_For_Esen;
import com.esen.vfs2.impl.VfsOperatorImpl;

import func.jdbc.FuncConnectionFactory;

/**
 * 测试vfs创建临时文件
 *
 * @author zhucx
 */
public class TestVfsCreateTempFile {

	private static final Logger log = LoggerFactory.getLogger(TestVfsCreateTempFile.class);

	private Vfs2 vfs;

	private String TABLENAME = "TEST_VFS_TMPFILE";

	private SimpleConnectionFactory dbfct;

	private VfsOperator admin = new VfsOperatorImpl("admin", true);

	private Vfs2 getVfs() {
		if (vfs == null) {
			Properties sqlMapProps = new Properties();
			sqlMapProps.setProperty("VFSTABLENAME", TABLENAME);

			Properties conf = new Properties();
			conf.setProperty(SqlMapConfigParser_For_Esen.CACHEENABLE, "false");

			SqlMapClientFactory.setFacoty(new SqlMapClientFactory());
			EsenSqlMapClient smc = SqlMapClientFactory.getInstance().createSqlMapClient(getConnectionFactory(),
					sqlMapProps, "com/esen/vfs2/sqlmapconfig-vfs.xml", conf);
			vfs = new Vfs2DB(getConnectionFactory(), TABLENAME, null, smc);
		}
		return vfs;
	}

	private SimpleConnectionFactory getConnectionFactory() {
		if (dbfct == null) {
			dbfct = FuncConnectionFactory.getOracleInstance().getCustomConnectionFactory(false, true);
			//			dbfct = FuncConnectionFactory.getOracleCustomConnectionFactory();
			//      dbfct = FuncConnectionFactory.getMysqlCustomConnectionFactory();
			//      dbfct = FuncConnectionFactory.getDb2CustomConnectionFactory();
			//      dbfct = FuncConnectionFactory.getMssqlCustomConnectionFactory();
			//      dbfct = FuncConnectionFactory.getMssql2005CustomConnectionFactory();
			//      dbfct = FuncConnectionFactory.getSybaseCustomConnectionFactory();
		}
		return dbfct;
	}

	private VfsOperator getOperator() {
		return admin;
	}

	private VfsFile2 getVfsFile(String path) {
		return getVfs().getVfsFile(path, getOperator());
	}

	public void setUp() throws Exception {
		getVfs().getVfsFile(null, getOperator()).remove();
	}

	public void tearDown() throws Exception {
		getConnectionFactory().close();
	}

	public static void main(String[] args) throws Exception {
		TestVfsCreateTempFile t = new TestVfsCreateTempFile();
		t.prepare();

		int threadcount = 5;
		int retryTimes = 5;

		Thread[] ts = new Thread[threadcount];
		for (int i = 0; i < threadcount; i++) {
			ts[i] = new TestVfsCreateTmpFileThread(t, i, retryTimes);
		}
		for (int i = 0; i < threadcount; i++) {
			ts[i].start();
		}
	}

	private void prepare() {
		VfsFile2 file = getVfsFile("tmpDir");
		file.ensureExists(true);
		Assert.assertTrue(file.isDirectory());

		file = getVfsFile("tmpFile");
		file.ensureExists(false);
		Assert.assertTrue(file.isFile());
	}

	public void testCreateTempFile() {
		{//测试在目录下创建临时文件
			VfsFile2 file = getVfsFile("tmpDir");
			//创建临时文件
			VfsFile2 tmp = file.createTempFile("tmpfile", true);
			Assert.assertTrue(tmp.isDirectory());
		}
		{//测试在文件下创建临时文件，此时会在此文件的父目录下创建
			VfsFile2 file = getVfsFile("tmpFile");
			//创建临时文件
			VfsFile2 tmp = file.createTempFile("file", false);
			Assert.assertTrue(tmp.isFile());
		}
	}
}

class TestVfsCreateTmpFileThread extends Thread {
	private TestVfsCreateTempFile t;

	private int index;

	private int retryTimes;

	public TestVfsCreateTmpFileThread(TestVfsCreateTempFile t, int index, int retryTimes) {
		this.t = t;
		this.index = index;
		this.retryTimes = retryTimes;
	}

	public void run() {
		for (int i = 0; i < retryTimes; i++) {
			t.testCreateTempFile();
		}
	}
}
