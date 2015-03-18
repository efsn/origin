package com.esen.vfs2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import junit.framework.Test;

import org.w3c.dom.Document;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.ibatis.EsenSqlMapClient;
import com.esen.jdbc.ibatis.SqlMapClientFactory;
import com.esen.jdbc.ibatis.SqlMapConfigParser_For_Esen;
import com.esen.util.ArrayFunc;
import com.esen.util.FileFunc;
import com.esen.util.StmFunc;
import com.esen.util.StrFunc;
import com.esen.util.XmlFunc;
import com.esen.util.chardet.Chardet;
import com.esen.vfs2.impl.AbstractVfs2;
import com.esen.vfs2.impl.VfsFile2Impl;
import com.esen.vfs2.impl.VfsOperatorImpl;

import func.FuncFile;
import func.FuncTestCase;
import func.TestSuiteExtend;
import func.jdbc.FuncConnectionFactory;

public class TestVfsFile2 extends FuncTestCase {

	private SimpleConnectionFactory dbfct;

	public TestVfsFile2() {
	}

	public TestVfsFile2(SimpleConnectionFactory dbfct) {
		this.dbfct = dbfct;
		//		System.out.println(dbfct.getDbType().getDbName());
	}

	private SimpleConnectionFactory getConnectionFactory() {
		if (dbfct == null) {
			dbfct = FuncConnectionFactory.getOracleInstance().getCustomConnectionFactory(true, true);
			//			dbfct = FuncConnectionFactory.getOracleCustomConnectionFactory();
			//      dbfct = FuncConnectionFactory.getMysqlCustomConnectionFactory();
			//      dbfct = FuncConnectionFactory.getDb2CustomConnectionFactory();
			//      dbfct = FuncConnectionFactory.getMssqlCustomConnectionFactory();
			//      dbfct = FuncConnectionFactory.getMssql2005CustomConnectionFactory();
			//      dbfct = FuncConnectionFactory.getSybaseCustomConnectionFactory();
		}
		return dbfct;
	}

	private static String TABLENAME = "TEST_VFS";

	private static String TXTFILE = "test好.txt";

	//  private static String TESTCONTENT = "abc123你好";
	private static String TESTCONTENT = "abc123你好啊什么时候才能";

	private static String TESTCONTENTXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><sqlMap namespace=\"Vfs\">abc123你好</sqlMap>";

	private static String TESTCONTENTXML_GBK = "<?xml version=\"1.0\" encoding=\"GBK\" ?><sqlMap namespace=\"Vfs\">abc123你好</sqlMap>";

	private Vfs2 vfs;

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

	private VfsOperator getOperator() {
		return admin;
	}

	private VfsFile2 getVfsFile(String path) {
		return getVfs().getVfsFile(path, getOperator());
	}

	private VfsFile2 getVfsFile(VfsFile2 parent, String name) {
		return getVfsFile(parent.getAbsolutePath() + "/" + name);
	}

	private VfsFile2 getNewVfsFile2(VfsFile2 file) {
		return getVfsFile(file.getAbsolutePath());
	}

	private VfsFile2 getTestVfsFile() {
		return getVfsFile(TXTFILE);
	}

	/**
	 * 创建一个VfsListener2的实例,方便测试
	 */
	class VfsListenerImpl implements VfsListener2 {
		private VfsFile2 file;

		private int chgflag;

		public void onBeforeChange(VfsFile2 f, int chgflag) {
		}

		public void onAfterChange(VfsFile2 f, int chgflag) {
			this.file = f;
			this.chgflag = chgflag;
		}

		public VfsFile2 getFile() {
			return file;
		}

		public int getChgFlag() {
			return chgflag;
		}
	}

	private VfsListenerImpl createListener() {
		return new VfsListenerImpl();
	}

	public void setUp() throws Exception {
		getVfs().getVfsFile(null, getOperator()).remove();
	}

	public void tearDown() throws Exception {
		getConnectionFactory().close();
	}

	public static void main(String[] args) throws Exception {
		TestVfsFile2 t = new TestVfsFile2();
		t.setUp();
		t.testRenameTo2();
	}

	public static Test suite() {
		boolean testOne = false;
		if (testOne) {
			TestSuiteExtend suite = new TestSuiteExtend("test");
			ConnectionFactory fct = FuncConnectionFactory.getDb2CustomConnectionFactory();
			//			suite.addTestSuite(TestVfsFile2.class, new Object[] { fct });
			suite.addTestSuite(TestVfsFile2.class, new Object[] { fct }, new String[] { "testListFiles" });
			return suite;
		}
		else {
			TestSuiteExtend suite = new TestSuiteExtend("test");
			ConnectionFactory[] fcts = FuncConnectionFactory.getCustomConnectionFactoryArray();
			int len = fcts == null ? 0 : fcts.length;
			for (int i = 0; i < len; i++) {
				ConnectionFactory fct = fcts[i];
				//				suite.addTestSuite(TestVfsFile2.class, new Object[] { fct },new String[] { "testImportZipInputStream" });
				suite.addTestSuite(TestVfsFile2.class, new Object[] { fct });
			}
			return suite;
		}
	}

	/**
	 * 检查fs的文件名是否在namestr对应的文件名列表中,如果存在文件名不在列表中,则执行结果不正确
	 * @param fs
	 * @param namestr
	 */
	private void assertEquals(VfsFile2[] fs, String namestr) {
		int fslen = fs == null ? 0 : fs.length;
		String[] names = StrFunc.isNull(namestr) ? null : namestr.split(",");
		int namelen = names == null ? 0 : names.length;
		if (fslen != namelen) {
			fail("列出的文件个数不正确");
		}
		for (int i = 0; i < fslen; i++) {
			String name = fs[i].getName();
			if (ArrayFunc.find(names, name, true, -1) == -1) {
				fail("列出的文件存在文件名在文件名列表中不存在:" + name);
			}
		}
	}

	private void assertEquals(VfsFile2[] fs1, VfsFile2[] fs2) {
		int len1 = fs1 == null ? 0 : fs1.length;
		int len2 = fs2 == null ? 0 : fs2.length;
		if (len1 != len2) {
			fail("文件个数不相等");
		}
		for (int i = 0; i < len1; i++) {
			VfsFile2 f1 = fs1[i];
			if (find(fs2, f1) == -1) {
				fail("文件列表不相等:" + f1.getAbsolutePath());
			}
		}
	}

	private void assertEquals(VfsFile2 f1, VfsFile2 f2) {
		assertEquals(f1.getAbsolutePath(), f2.getAbsolutePath());
	}

	private int find(VfsFile2[] fs, VfsFile2 f) {
		if (f == null) {
			return -1;
		}
		int len = fs == null ? 0 : fs.length;
		if (len == 0) {
			return -1;
		}
		for (int i = 0; i < len; i++) {
			if (StrFunc.compareText(fs[i].getAbsolutePath(), f.getAbsolutePath())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * =================== 开始测试 ===================
	 */
	public void test() {
	}

	/**
	 * 测试文件的parentDir和fileName
	 * @throws Exception 
	 */
	public void testVfsFilePath() throws Exception {
		{//测试正常的文件名
			assertEquals(getVfsFile(null).getAbsolutePath(), "/");
			assertEquals(getVfsFile("").getAbsolutePath(), "/");
			assertEquals(getVfsFile("//////////////////").getAbsolutePath(), "/");
			assertEquals(getVfsFile("\\\\\\\\\\\\\\").getAbsolutePath(), "/");
			assertEquals(getVfsFile("\\\\\\\\\\\\\\//////////////").getAbsolutePath(), "/");
			assertEquals(getVfsFile("//////////////\\\\\\\\\\\\\\").getAbsolutePath(), "/");
			assertEquals(
					getVfsFile("\\\\\\\\\\\\\\//////////////\\\\\\\\\\\\\\\\\\\\\\//////////////").getAbsolutePath(),
					"/");
			assertEquals(getVfsFile("a/b/c.txt").getAbsolutePath(), "/a/b/c.txt");
			assertEquals(getVfsFile("/a/b/c.txt").getAbsolutePath(), "/a/b/c.txt");
			assertEquals(getVfsFile("\\a\\b\\c.txt").getAbsolutePath(), "/a/b/c.txt");
			assertEquals(getVfsFile("\\a/b\\c.txt").getAbsolutePath(), "/a/b/c.txt");
			assertEquals(getVfsFile("\\a\\\\\\/\\\\\\/////\\\\\\\\/b\\c.txt").getAbsolutePath(), "/a/b/c.txt");
			//测试数据库中的特殊字段作为文件名:_,%
			assertEquals(getVfsFile("_").getAbsolutePath(), "/_");
			assertEquals(getVfsFile("%").getAbsolutePath(), "/%");

		}
		{//测试不正确的文件名
			{
				VfsFile2 file = getVfsFile("/:*?\"<>|\r\n\t\b\f/c.txt");
				try {
					file.ensureExists(false);
					fail("不合法的文件名,包含非法字符:*?\"<>|\r\n\t\b\f");
				}
				catch (Exception e) {
				}
			}

			class CheckFileName {
				public void check() {
					String invalidChars = "*?\"<>|\r\n\t\b\f";
					for (int i = 0, len = invalidChars.length(); i < len; i++) {
						VfsFile2 file = getVfsFile(String.valueOf(invalidChars.charAt(i)));
						try {
							file.ensureExists(false);
							fail("不合法的文件名,包含非法字符:*?\"<>|\r\n\t\b\f");
						}
						catch (Exception e) {
						}
					}
				}
			}

			new CheckFileName().check();

			{
				VfsFile2 file = getVfsFile(".");
				try {
					file.ensureExists(false);
					fail("不合法的文件名,文件名不能为'.'");
				}
				catch (Exception e) {
				}
			}
			{
				VfsFile2 file = getVfsFile("..");
				try {
					file.ensureExists(false);
					fail("不合法的文件名,文件名不能为'.'");
				}
				catch (Exception e) {
				}
			}

			{
				String str = "abcdefghijklmnopqrstuvwxyz1234567890";
				String fileName = "";
				for (int i = 0; i < 10; i++) {
					fileName += str;
				}
				String fileName1 = fileName.substring(0, 256);
				String fileName2 = fileName.substring(0, 257);

				if (getConnectionFactory().getDbType().isDb2()) {
					/**
					 * 如果是db2,那么在PrepareStatement中设置的参数不能超过对应字段的长度
					 * 在db2中vfs的FILENAME_字段的长度为236,所以256的长度超过了236,执行
					 * 
					 * select * from TEST_VFS where    (PARENTDIR_=? and FILENAME_=?)
					 * 1  /
					 * 2  abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcd
					 * 
					 * 会抛出异常:
					 *   Cause: com.ibm.db2.jcc.a.in: DB2 SQL Error: SQLCODE=-302, SQLSTATE=22001, SQLERRMC=null, DRIVER=3.52.95
					 */
					try {
						VfsFile2 file = getVfsFile(fileName1);//这里去数据库中查询时就会出异常
						fail("文件名长度超过了字段的长度,会抛出异常");
					}
					catch (Exception e) {
					}
				}
				else {
					/**
					 * ORA-12899: 列 "TESTCASE"."TEST_VFS"."FILENAME_" 的值太大 (实际值: 256, 最大值: 255)
					 */
					{
						VfsFile2 file = getVfsFile(fileName1);
						try {
							file.ensureExists(false);
							fail("文件名长度超过了字段的长度,会抛出异常");
						}
						catch (Exception e) {
						}
					}

					{
						VfsFile2 file = getVfsFile(fileName2);
						try {
							file.ensureExists(false);
							fail("文件名长度超过限制,长度不能超过256");
						}
						catch (Exception e) {
						}
					}
				}
			}
		}
	}

	/**
	 * 测试VfsFile2 getVfsFile(String fn)
	 */
	public void testGetVfsFile() {
		//正确的路径
		assertEquals(getVfsFile("/abc").getVfsFile(""), getVfsFile("abc"));
		assertEquals(getVfsFile("/abc").getVfsFile(null), getVfsFile("abc"));
		assertEquals(getVfsFile("/abc").getVfsFile("./"), getVfsFile("abc"));
		assertEquals(getVfsFile("/abc").getVfsFile("../"), getVfsFile(""));
		assertEquals(getVfsFile("/abc").getVfsFile("../../"), getVfsFile(""));
		assertEquals(getVfsFile("/abc").getVfsFile("../.."), getVfsFile(""));
		assertEquals(getVfsFile("/abc").getVfsFile("d"), getVfsFile("abc/d"));
		assertEquals(getVfsFile("/abc").getVfsFile("/d"), getVfsFile("d"));
		assertEquals(getVfsFile("/abc").getVfsFile("./d"), getVfsFile("abc/d"));
		assertEquals(getVfsFile("/abc").getVfsFile("../d"), getVfsFile("d"));
		assertEquals(getVfsFile("/abc").getVfsFile(".././d"), getVfsFile("d"));
		assertEquals(getVfsFile("/abc").getVfsFile("../../d"), getVfsFile("d"));
		assertEquals(getVfsFile("/abc").getVfsFile("../.././d"), getVfsFile("d"));
		assertEquals(getVfsFile("/abc").getVfsFile("../../../../d"), getVfsFile("d"));
		assertEquals(getVfsFile("/a/b/c/d/e/f/g").getVfsFile("../../d"), getVfsFile("/a/b/c/d/e/d"));

		//传入参数不正确,获得正确的路径,vfs有部分情况下可以获得正确的结果
		assertEquals(getVfsFile("/abc").getVfsFile(".//d"), getVfsFile("/abc/d"));
		//传入参数不正确,获得不正确的路径
		assertEquals(getVfsFile("/abc").getVfsFile(".././../../d"), getVfsFile("/../../d"));
		assertEquals(getVfsFile("/abc").getVfsFile("././d"), getVfsFile("/abc/./d"));
	}

	public void testGetOutputStream() throws Exception {
		{//测试本身的父目录都不存在,调用getOutputStream会自动创建
			VfsFile2 dir = getVfsFile("/a/b");
			assertFalse(dir.exists());
			VfsFile2 file = getVfsFile(dir, "c.txt");
			assertFalse(file.exists());
			OutputStream out = file.getOutputStream();
			try {
			}
			finally {
				out.close();
			}
			assertTrue(getVfsFile(dir.getAbsolutePath()).exists());
			assertTrue(file.exists());
		}
		{//测试当文件是目录时会抛出异常
			VfsFile2 dir = getVfsFile("/aa");
			assertFalse(dir.exists());
			dir.ensureExists(true);
			try {
				OutputStream out = dir.getOutputStream();
				fail("向目录写入文件会出现异常");
			}
			catch (Exception e) {
			}
		}
		{//测试文件正常写入
			VfsFile2 file = getTestVfsFile();
			OutputStream out = file.getOutputStream();
			try {
				out.write(TESTCONTENT.getBytes());
			}
			finally {
				out.close();
			}
			assertEquals(TESTCONTENT, file.getAsString());
		}
	}

	public void testGetInputStream() throws Exception {
		{//文件不存在,会抛出异常
			VfsFile2 file = getVfsFile("/a/a.txt");
			assertFalse(file.exists());
			try {
				file.getInputStream();
				fail("不存在的文件不能获得内容");
			}
			catch (Exception e) {
			}
		}
		{//文件是目录,会抛出异常
			VfsFile2 file = getVfsFile("/a/b");
			assertFalse(file.exists());
			file.ensureExists(true);
			try {
				file.getInputStream();
				fail("不存在的文件不能获得内容");
			}
			catch (Exception e) {
			}
		}
		{//正常读取
			VfsFile2 file = getTestVfsFile();
			assertFalse(file.exists());
			file.ensureExists(false);
			assertTrue(file.isFile());
			//没有写入内容前,内容为空
			InputStream in = file.getInputStream();
			try {
				byte[] bs = in == null ? null : StmFunc.stm2bytes(in);
				assertEquals(bs == null ? 0 : bs.length, 0);
			}
			finally {
				if (in != null)
					in.close();
			}
			//写入内容
			OutputStream out = file.getOutputStream();
			try {
				out.write(TESTCONTENT.getBytes());
			}
			finally {
				out.close();
			}
			//再次读取内容,并与写入的内容比较
			in = file.getInputStream();
			try {
				byte[] bs = in == null ? null : StmFunc.stm2bytes(in);
				assertEquals(bs, TESTCONTENT.getBytes());
			}
			finally {
				if (in != null)
					in.close();
			}
		}
	}

	public void testGetGzipInputStream() throws Exception {
		//只测试文件的正常读取,其它情况与getInputStream是一样的
		VfsFile2 file = getTestVfsFile();
		byte[] content = TESTCONTENT.getBytes();
		OutputStream out = file.getOutputStream();
		try {
			out.write(content);
		}
		finally {
			out.close();
		}
		InputStream in = file.getGzipInputStream();
		try {
			byte[] bs = StmFunc.stm2bytes(in);
			assertEquals(bs, StmFunc.gzipBytes(content));
		}
		finally {
			in.close();
		}
	}

	public void testGetAsBytes() throws Exception {
		{//文件不存在,返回内容为空
			VfsFile2 file = getVfsFile("/a/b");
			assertFalse(file.exists());
			assertEquals(file.getAsBytes(), 0);
		}
		{//文件是一个目录,会抛出异常
			VfsFile2 file = getVfsFile("/a/b");
			assertFalse(file.exists());
			file.ensureExists(true);
			assertTrue(file.exists());
			try {
				file.getAsBytes();
				fail("目录调用获得内容的方法,会出现异常");
			}
			catch (Exception e) {
			}
		}
		{//正常获得文件的内容
			VfsFile2 file = getVfsFile("/a/b.txt");
			assertFalse(file.exists());
			file.ensureExists(false);
			assertTrue(file.exists());

			//读取内容,此时内容为空
			assertEquals(file.getAsBytes(), 0);

			//设置内容
			file.setAsBytes(TESTCONTENT.getBytes());

			//再次读取内容,与设置的内容比较
			assertEquals(file.getAsBytes(), TESTCONTENT.getBytes());
		}
	}

	public void testSetAsBytes() throws Exception {
		{//如果文件是一个目录,会出现异常
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());
			file.ensureExists(true);
			assertTrue(file.exists());

			try {
				file.setAsBytes(null);
				fail("向目录写入文件会出现异常");
			}
			catch (Exception e) {
			}
		}
		{//文件不存在,会自动创建,父目录不存在也会自动创建
			VfsFile2 dir = getVfsFile("/2/a/b");
			VfsFile2 file = getVfsFile(dir, "c.txt");
			assertFalse(dir.exists());
			assertFalse(file.exists());

			file.setAsBytes(TESTCONTENT.getBytes());

			assertTrue(getNewVfsFile2(dir).exists());
			assertTrue(getNewVfsFile2(file).exists());

			assertEquals(TESTCONTENT.getBytes(), file.getAsBytes());
		}
		{//文件已经存在,会覆盖当前文件的内容
			VfsFile2 file = getVfsFile("/3/a/b/c/d.txt");
			assertFalse(file.exists());
			file.ensureExists(false);
			assertTrue(file.exists());

			assertEquals(file.getAsBytes(), 0);

			file.setAsBytes(TESTCONTENT.getBytes());

			assertEquals(file.getAsBytes(), TESTCONTENT.getBytes());
		}
	}

	public void testGetAsString() {
		{//文件不存在,返回null
			VfsFile2 file = getVfsFile("/a/b.txt");
			assertFalse(file.exists());

			assertNull(file.getAsString());
		}
		{//文件存在,内容为空,返回""
			VfsFile2 file = getVfsFile("/a/b.txt");
			assertFalse(file.exists());
			file.ensureExists(false);
			assertTrue(file.exists());

			assertEquals(file.getAsString(), "");

		}
		{//此文件是一个目录,会出现异常
			VfsFile2 file = getVfsFile("/aa/b");
			assertFalse(file.exists());
			file.ensureExists(true);
			assertTrue(file.exists());

			try {
				file.getAsString();
				fail("从目录获得内容,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//不是文本文件,会抛出异常
			VfsFile2 file = getVfsFile("/a/c.abc");//abc为未知后缀,不是文本文件
			assertFalse(file.exists());
			assertTrue(file.createFile());
			assertTrue(file.exists());

			try {
				file.getAsString();
				fail("不是文本文件将获得的内容转换为字串,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//正常获得文件内容
			VfsFile2 file = getVfsFile("/a/d.txt");
			assertFalse(file.exists());
			assertTrue(file.createFile());
			assertTrue(file.exists());

			assertEquals(file.getAsString(), "");
			file.saveAsString(TESTCONTENT);
			assertEquals(file.getAsString(), TESTCONTENT);
		}
	}

	public void testSaveAsStringString() {
		//此方法等于saveAsString(String v, null)
	}

	public void testSaveAsStringStringString() throws Exception {
		{//文件不存在,会自动创建,父目录不存在,也会自动创建
			VfsFile2 dir = getVfsFile("/1/a/b");
			VfsFile2 file = getVfsFile(dir, "c.txt");
			assertFalse(dir.exists());
			assertFalse(file.exists());

			file.saveAsString(TESTCONTENT, null);
			assertTrue(file.exists());
			assertTrue(getNewVfsFile2(dir).exists());
			assertEquals(file.getAsString(), TESTCONTENT);
		}
		{//此文件是一个目录,保存内容会抛出异常
			VfsFile2 file = getVfsFile("/2/阿/b");
			assertFalse(file.exists());
			file.ensureExists(true);
			assertTrue(file.exists());

			try {
				file.saveAsString(TESTCONTENT, null);
				fail("向目录写入内容,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//正常保存文件内容
			VfsFile2 file = getVfsFile("/3/a/b/c/d.txt");
			assertFalse(file.exists());
			file.ensureExists(false);
			assertTrue(file.isFile());

			file.saveAsString(TESTCONTENT, null);
			assertEquals(TESTCONTENT, file.getAsString());

			//测试传入编码的情况
			file.saveAsString(TESTCONTENT, StrFunc.GBK);
			assertEquals(TESTCONTENT, file.getAsString());

			file.saveAsString(TESTCONTENT, StrFunc.UTF8);
			assertEquals(TESTCONTENT, file.getAsString());

			//设置为StrFunc.ISO8859_1后无法还原
			//    file.saveAsString(TESTCONTENT, StrFunc.ISO8859_1);
			//    assertEquals(TESTCONTENT, file.getAsString());

			//测试没有传入编码时自动获得编码
			{//从getCharset()中获得编码
				file.setCharset(StrFunc.GBK);
				file.saveAsString(TESTCONTENT, null);
				assertEquals(TESTCONTENT, new String(file.getAsBytes(), file.getCharset()));

				file.setCharset(StrFunc.UTF8);
				file.saveAsString(TESTCONTENT, null);
				assertEquals(TESTCONTENT, new String(file.getAsBytes(), file.getCharset()));
			}
			{//xml或html内容中获得编码
				file.setCharset(null);
				file.saveAsString(TESTCONTENTXML, null);
				String charset = Chardet.extractCharset(TESTCONTENTXML);
				assertEquals(charset, StrFunc.UTF8);
				assertEquals(TESTCONTENTXML, new String(file.getAsBytes(), charset));

				file.saveAsString(TESTCONTENTXML_GBK, null);
				charset = Chardet.extractCharset(TESTCONTENTXML_GBK);
				assertEquals(charset, StrFunc.GBK);
				assertEquals(TESTCONTENTXML_GBK, new String(file.getAsBytes(), charset));
			}
			{//vfs默认的编码
				file.setCharset(null);
				file.saveAsString(TESTCONTENT, null);
				assertEquals(TESTCONTENT, new String(file.getAsBytes(), AbstractVfs2.DEFAULT_ENCODING));
			}
			{//特定的后缀,直接获得设置的编码,如cod,ncd的编码是GBK
				file = getVfsFile("i.cod");
				assertFalse(file.exists());
				assertTrue(file.createFile());
				assertTrue(file.exists());

				file.saveAsString(TESTCONTENT, null);
				assertEquals(TESTCONTENT, new String(file.getAsBytes(), StrFunc.GBK));
			}
		}
	}

	public void testGetAsXml() throws Exception {
		{//文件不存在,返回null
			VfsFile2 file = getVfsFile("/1/a/1.txt");
			assertFalse(file.exists());

			assertNull(file.getAsXml());
		}
		{//文件内容为空,返回null
			VfsFile2 file = getVfsFile("/2/a/2.txt");
			assertFalse(file.exists());
			file.ensureExists(false);
			assertTrue(file.exists());

			assertEquals(file.getAsBytes(), 0);

			assertNull(file.getAsXml());
		}
		{//此文件是目录,抛出异常
			VfsFile2 file = getVfsFile("/3/a/b");
			file.ensureExists(true);
			assertTrue(file.isDirectory());

			try {
				file.getAsXml();
				fail("从目录获得内容,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//正常读取内容
			VfsFile2 file = getVfsFile("/4/a/3.xml");
			file.ensureExists(false);
			assertTrue(file.isFile());

			file.saveAsString(TESTCONTENTXML, StrFunc.UTF8);
			assertEquals(file.getAsXml(), XmlFunc.getDocument(TESTCONTENTXML));

			file.saveAsString(TESTCONTENTXML_GBK, StrFunc.GBK);
			assertEquals(file.getAsXml(), XmlFunc.getDocument(TESTCONTENTXML_GBK));
		}
	}

	public void testSaveAsXml() throws Exception {
		{//此文件是目录,会抛出异常
			VfsFile2 file = getVfsFile("/1/a/b");
			file.ensureExists(true);
			assertTrue(file.isDirectory());

			Document doc = XmlFunc.getDocument(TESTCONTENTXML);
			try {
				file.saveAsXml(doc, null);
				fail("向目录保存内容,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//如果文件不存在,会自动创建,父目录不存在,也会自动创建
			VfsFile2 dir = getVfsFile("/2/a/b/");
			VfsFile2 file = getVfsFile(dir, "c.xml");
			assertFalse(dir.exists());
			assertFalse(file.exists());

			Document doc = XmlFunc.getDocument(TESTCONTENTXML);
			file.saveAsXml(doc, null);

			assertTrue(file.exists());
			dir = getNewVfsFile2(dir);
			assertTrue(dir.exists());

			assertEquals(doc, file.getAsXml());
		}
		{//如果传入的Document为空,会抛出异常
			VfsFile2 file = getVfsFile("/3/a/b/d.xml");
			try {
				file.saveAsXml(null, null);
				fail("传入的Document参数为空,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//正常保存文件内容
			VfsFile2 file = getVfsFile("/4/a/b/e.xml");

			//不设置编码
			Document doc = XmlFunc.getDocument(TESTCONTENTXML);
			file.saveAsXml(doc, null);
			assertEquals(doc, file.getAsXml());

			//设置编码为UTF-8
			file.saveAsXml(doc, StrFunc.UTF8);
			assertEquals(doc, file.getAsXml());

			//设置编码为GBK
			file.saveAsXml(doc, StrFunc.GBK);
			assertEquals(doc, file.getAsXml());

			//xml本身的编码为GBK
			doc = XmlFunc.getDocument(TESTCONTENTXML_GBK);
			file.saveAsXml(doc, null);
			assertEquals(doc, file.getAsXml());
		}
	}

	public void testIsFile() throws Exception {
		{//此文件是目录,返回false
			VfsFile2 file = getVfsFile("isdir");
			assertFalse(file.exists());
			assertTrue(file.createDir());
			assertTrue(file.isDirectory());

			assertFalse(file.isFile());
		}
		{//此文件存在并且是文件,返回true
			VfsFile2 file = getVfsFile("isfile");
			assertFalse(file.exists());

			assertFalse(file.isFile());

			assertTrue(file.createFile());
			assertTrue(file.exists());

			assertTrue(file.isFile());
		}
	}

	public void testIsDirectory() {
		{//此文件存在并且是目录,返回false
			VfsFile2 file = getVfsFile("isdir");
			assertFalse(file.exists());

			assertFalse(file.isDirectory());

			assertTrue(file.createDir());
			assertTrue(file.exists());

			assertTrue(file.isDirectory());
		}
		{//此文件是文件,返回false
			VfsFile2 file = getVfsFile("isfile");
			assertFalse(file.exists());
			assertTrue(file.createFile());
			assertTrue(file.exists());

			assertFalse(file.isDirectory());
		}
	}

	public void testExists() throws Exception {
		{//测试文件是否存在
			VfsFile2 file = getVfsFile("isfile");
			assertFalse(file.exists());
			assertTrue(file.createFile());
			assertTrue(file.exists());
		}
		{//测试目录是否存在
			VfsFile2 file = getVfsFile("isdir");
			assertFalse(file.exists());
			assertTrue(file.createDir());
			assertTrue(file.exists());
		}
	}

	public void testMkdirs() throws Exception {
		{//创建目录,自动创建,父目录不存在,也会自动创建
			VfsFile2 dir = getVfsFile("/a/b/c/d/e");
			VfsFile2 file = getVfsFile(dir, "f");
			assertFalse(dir.exists());
			assertFalse(file.exists());

			assertTrue(file.mkdirs());
			assertTrue(getNewVfsFile2(dir).exists());
			assertTrue(file.exists());
			assertTrue(file.isDirectory());
		}
		{//如果目录已经存在,直接返回true
			VfsFile2 dir = getVfsFile("/1/b/c/d/e");
			VfsFile2 file = getVfsFile(dir, "f");

			assertFalse(dir.exists());
			assertFalse(file.exists());

			file.ensureExists(true);

			dir = getNewVfsFile2(dir);
			assertTrue(dir.exists());
			assertTrue(file.exists());
			assertTrue(file.isDirectory());
		}
		{//如果本文件是文件而不是目录,会抛出异常
			VfsFile2 dir = getVfsFile("/2/b/c/d/e");
			VfsFile2 file = getVfsFile(dir, "f.txt");

			assertFalse(dir.exists());
			assertFalse(file.exists());

			file.ensureExists(false);

			dir = getNewVfsFile2(dir);
			assertTrue(dir.exists());
			assertTrue(file.exists());
			assertTrue(file.isFile());

			try {
				file.mkdirs();
				fail("已经存在的文件不能调用mkdirs,否则会出现异常");
			}
			catch (Exception e) {
			}

		}
	}

	public void testCreateFile() throws Exception {
		{//如果文件不存在,父目录存在,创建成功则返回true
			VfsFile2 dir = getVfsFile("/1/b/");
			VfsFile2 file = getVfsFile(dir, "c.txt");
			assertFalse(dir.exists());
			assertFalse(file.exists());

			assertTrue(dir.mkdirs());

			assertTrue(dir.isDirectory());
			assertFalse(file.exists());

			assertTrue(file.createFile());

			assertTrue(file.isFile());

		}
		{//如果父目录不存在,不会自动创建,返回false
			VfsFile2 dir = getVfsFile("/2/b/");
			VfsFile2 file = getVfsFile(dir, "c.txt");
			assertFalse(dir.exists());
			assertFalse(file.exists());

			assertFalse(file.createFile());

			assertFalse(file.exists());
		}
		{//如果文件已经存在,返回false
			VfsFile2 dir = getVfsFile("/3/b/");
			VfsFile2 file = getVfsFile(dir, "c.txt");
			assertFalse(dir.exists());
			assertFalse(file.exists());

			assertTrue(dir.mkdirs());

			assertTrue(dir.isDirectory());
			assertFalse(file.exists());

			assertTrue(file.createFile());

			assertTrue(file.isFile());

			//再次调用createFile会返回false
			assertFalse(file.createFile());
		}
		{//如果此文件是一个目录,会抛出异常
			VfsFile2 dir = getVfsFile("/3/b/");
			VfsFile2 file = getVfsFile(dir, "c");
			assertFalse(file.exists());

			assertTrue(file.mkdirs());
			assertTrue(file.isDirectory());

			try {
				file.createFile();
				fail("目录本身不能再创建为一个文件");
			}
			catch (Exception e) {
			}
		}
	}

	public void testCreateDir() throws Exception {
		{//如果父目录存在,目录本身不存在,创建成功会返回true
			VfsFile2 dir = getVfsFile("/1/b/");
			VfsFile2 file = getVfsFile(dir, "c");
			assertFalse(dir.exists());
			assertFalse(file.exists());

			assertTrue(dir.mkdirs());

			assertTrue(dir.isDirectory());
			assertFalse(file.exists());

			assertTrue(file.createDir());

			assertTrue(file.isDirectory());
		}
		{//如果父目录不存在,创建目录不会成功,会返回false
			VfsFile2 dir = getVfsFile("/2/b/");
			VfsFile2 file = getVfsFile(dir, "c");
			assertFalse(dir.exists());
			assertFalse(file.exists());

			assertFalse(file.createDir());

			assertFalse(file.exists());
		}
		{//如果目录已经存在,返回false
			VfsFile2 dir = getVfsFile("/3/b/");
			VfsFile2 file = getVfsFile(dir, "c");
			assertFalse(dir.exists());
			assertFalse(file.exists());

			assertTrue(dir.mkdirs());

			assertTrue(dir.isDirectory());
			assertFalse(file.exists());

			assertTrue(file.createDir());

			assertTrue(file.isDirectory());

			//再次调用createFile会返回false
			assertFalse(file.createDir());
		}
		{//如果目录已经是一个文件,会抛出异常
			VfsFile2 dir = getVfsFile("/4/b/");
			VfsFile2 file = getVfsFile(dir, "c");
			assertFalse(file.exists());

			file.ensureExists(false);
			assertTrue(file.isFile());

			try {
				file.createDir();
				fail("文件本身不能再创建为一个目录");
			}
			catch (Exception e) {
			}
		}
	}

	public void testListFiles() throws Exception {
		VfsFile2 dir = getVfsFile("testdir");
		{//创建测试的环境
			/**
			 * 生成的结构为:
			 * /a
			 *   /b
			 *     /d.txt
			 *   /c.txt 
			 */
			VfsFile2 aFile = getVfsFile(dir, "a");
			VfsFile2 bFile = getVfsFile(aFile, "b");
			VfsFile2 cFile = getVfsFile(aFile, "c.txt");
			VfsFile2 dFile = getVfsFile(bFile, "d.txt");
			assertFalse(aFile.exists());

			cFile.ensureExists(false);
			dFile.ensureExists(false);

			cFile.saveAsString(TESTCONTENTXML, StrFunc.UTF8);
			dFile.saveAsString(TESTCONTENTXML_GBK, StrFunc.GBK);
		}

		{//如果文件不存在,返回null,不会出现异常
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());
			assertNull(file.listFiles());
		}
		{//如果不是目录,返回null,不会出现异常
			VfsFile2 file = getVfsFile("/2/a/b");
			file.ensureExists(true);
			assertNull(file.listFiles());
		}
		{//列出a的子文件
			VfsFile2 file = getVfsFile(dir, "a");
			assertEquals(file.listFiles(), "b,c.txt");
			assertEquals(getVfsFile(file, "c.txt").getAsBytes(), TESTCONTENTXML.getBytes(StrFunc.UTF8));
		}
		{//列出b的子文件
			VfsFile2 file = getVfsFile(dir, "a/b");
			assertEquals(file.listFiles(), "d.txt");
			assertEquals(getVfsFile(file, "d.txt").getAsBytes(), TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));
		}
		{//如果是文件,调用此方法,返回的为null
			VfsFile2 file = getVfsFile(dir, "a/c.txt");
			assertEquals(file.listFiles(), 0);

			file = getVfsFile(dir, "a/b/d.txt");
			assertEquals(file.listFiles(), 0);
		}
	}

	public void testListFilesStringIntBoolean() throws Exception {
		VfsFile2 dir = getVfsFile("testdir/a");
		{//构建测试环境
			/**
			 * 目录结构如下:
			 * /a
			 *   /b
			 *     /d.txt
			 *     /b.pdf
			 *     /e.tx
			 *     /c
			 *   /c.txt
			 *   /_
			 *   /%
			 */
			VfsFile2 aFile = dir;
			VfsFile2 bFile = getVfsFile(aFile, "b");
			VfsFile2 cFile = getVfsFile(aFile, "c.txt");
			VfsFile2 dFile = getVfsFile(bFile, "d.txt");
			VfsFile2 bbFile = getVfsFile(bFile, "b.pdf");
			VfsFile2 eFile = getVfsFile(bFile, "e.tx");
			VfsFile2 bcFile = getVfsFile(bFile, "c");
			VfsFile2 uFile = getVfsFile(aFile, "_");
			VfsFile2 pFile = getVfsFile(aFile, "%");

			aFile.ensureExists(true);
			bFile.ensureExists(true);
			cFile.ensureExists(false);
			dFile.ensureExists(false);
			eFile.ensureExists(false);
			bbFile.ensureExists(false);
			bcFile.ensureExists(true);
			uFile.ensureExists(false);
			pFile.ensureExists(false);

			cFile.saveAsString(TESTCONTENTXML, StrFunc.UTF8);
			dFile.saveAsString(TESTCONTENTXML_GBK, StrFunc.GBK);
		}

		{//文件不存在,返回null,不会抛出异常
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());
			assertNull(file.listFiles(null, VfsFile2.RESERVEFILE | VfsFile2.RESERVEFOLDER, true));
		}
		{//如果是文件,返回null,不会抛出异常
			VfsFile2 file = getVfsFile(dir, "c.txt");
			assertTrue(file.isFile());
			assertNull(file.listFiles(null, VfsFile2.RESERVEFILE | VfsFile2.RESERVEFOLDER, true));
		}
		{//测试不递归
			//保留文件
			assertEquals(dir.listFiles(null, VfsFile2.RESERVEFILE, false), "c.txt,_,%");
			//保留目录
			assertEquals(dir.listFiles(null, VfsFile2.RESERVEFOLDER, false), "b");
			//过滤文件
			assertEquals(dir.listFiles(null, VfsFile2.FILTERFILE, false), 0);
			assertEquals(dir.listFiles("*.txt", VfsFile2.FILTERFILE, false), "c.txt");
			assertEquals(dir.listFiles("*", VfsFile2.FILTERFILE, false), "c.txt,_,%");
			assertEquals(dir.listFiles("_", VfsFile2.FILTERFILE, false), "_");
			assertEquals(dir.listFiles("%", VfsFile2.FILTERFILE, false), "%");
			//过滤目录
			assertEquals(dir.listFiles(null, VfsFile2.FILTERFOLDER, false), 0);
			assertEquals(dir.listFiles("?", VfsFile2.FILTERFOLDER, false), "b");
			assertEquals(dir.listFiles("*", VfsFile2.FILTERFOLDER, false), "b");
			//测试合并情况
			assertEquals(dir.listFiles(null, VfsFile2.FILTERFILE | VfsFile2.RESERVEFILE, false), dir.listFiles(null,
					VfsFile2.RESERVEFILE, false));
			assertEquals(dir.listFiles(null, VfsFile2.FILTERFOLDER | VfsFile2.RESERVEFOLDER, false), dir.listFiles(
					null, VfsFile2.RESERVEFOLDER, false));
			assertEquals(dir.listFiles("*", VfsFile2.FILTERFILE | VfsFile2.FILTERFOLDER, false), "b,c.txt,_,%");
		}
		{//测试递归
			//保留文件
			assertEquals(dir.listFiles(null, VfsFile2.RESERVEFILE, true), "c.txt,b.pdf,e.tx,d.txt,_,%");
			//保留目录
			assertEquals(dir.listFiles(null, VfsFile2.RESERVEFOLDER, true), "b,c");
			//过滤文件
			assertEquals(dir.listFiles(null, VfsFile2.FILTERFILE, true), 0);
			assertEquals(dir.listFiles("*.txt", VfsFile2.FILTERFILE, true), "c.txt,d.txt");
			assertEquals(dir.listFiles("*", VfsFile2.FILTERFILE, true), "c.txt,d.txt,e.tx,b.pdf,_,%");
			assertEquals(dir.listFiles("*.t?", VfsFile2.FILTERFILE, true), "e.tx");
			assertEquals(dir.listFiles("_", VfsFile2.FILTERFILE, true), "_");
			assertEquals(dir.listFiles("%", VfsFile2.FILTERFILE, true), "%");
			//过滤目录
			assertEquals(dir.listFiles(null, VfsFile2.FILTERFOLDER, true), 0);
			assertEquals(dir.listFiles("?", VfsFile2.FILTERFOLDER, true), "b,c");
			assertEquals(dir.listFiles("*", VfsFile2.FILTERFOLDER, true), "b,c");
			//测试合并情况
			assertEquals(dir.listFiles(null, VfsFile2.FILTERFILE | VfsFile2.RESERVEFILE, true), dir.listFiles(null,
					VfsFile2.RESERVEFILE, true));
			assertEquals(dir.listFiles(null, VfsFile2.FILTERFOLDER | VfsFile2.RESERVEFOLDER, true), dir.listFiles(null,
					VfsFile2.RESERVEFOLDER, true));
			assertEquals(dir.listFiles("*", VfsFile2.FILTERFILE | VfsFile2.FILTERFOLDER, true),
					"b,c.txt,d.txt,e.tx,c,b.pdf,_,%");
			assertEquals(dir.listFiles("*d*", VfsFile2.FILTERFILE | VfsFile2.FILTERFOLDER, true), "d.txt,b.pdf");

			//assertEquals(getVfsFile(dir, "%").listFiles("*", VfsFile2.FILTERFILE | VfsFile2.FILTERFOLDER, true), 0);
		}
		{//测试WITHCONTENT参数
			{//查询根目录
				{//不带内容
					VfsFile2[] list = getVfsFile(null).listFiles(null, VfsFile2.RESERVEFILE | VfsFile2.RESERVEFOLDER,
							true);
					assertEquals(list, "testdir,a,b,c.txt,_,%,d.txt,b.pdf,e.tx,c");
					int len = list == null ? 0 : list.length;
					for (int i = 0; i < len; i++) {
						VfsFile2Impl file = (VfsFile2Impl) list[i];
						assertFalse(file.getNode().getContainContent());
					}
				}
				{//带内容
					VfsFile2[] list = getVfsFile(null).listFiles(null,
							VfsFile2.RESERVEFILE | VfsFile2.RESERVEFOLDER | VfsFile2.WITHCONTENT, true);
					assertEquals(list, "testdir,a,b,c.txt,_,%,d.txt,b.pdf,e.tx,c");
					int len = list == null ? 0 : list.length;
					for (int i = 0; i < len; i++) {
						VfsFile2Impl file = (VfsFile2Impl) list[i];
						assertTrue(file.getNode().getContainContent());
						File content = file.getNode().getContent();
						byte[] bs = content == null ? null : StmFunc.ungzipBytes(FileFunc.file2buf(content));
						if (file.getName().equalsIgnoreCase("c.txt")) {
							assertEquals(bs, TESTCONTENTXML.getBytes(StrFunc.UTF8));
						}
						else if (file.getName().equalsIgnoreCase("d.txt")) {
							assertEquals(bs, TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));
						}
						else {
							assertEquals(bs, 0);
						}
					}
				}
			}
			{//查询testdir/a目录
				{//不带内容
					VfsFile2[] list = dir.listFiles(null, VfsFile2.RESERVEFILE | VfsFile2.RESERVEFOLDER, true);
					assertEquals(list, "b,c.txt,_,%,d.txt,b.pdf,e.tx,c");
					int len = list == null ? 0 : list.length;
					for (int i = 0; i < len; i++) {
						VfsFile2Impl file = (VfsFile2Impl) list[i];
						assertFalse(file.getNode().getContainContent());
					}
				}
				{//带内容
					VfsFile2[] list = dir.listFiles(null, VfsFile2.RESERVEFILE | VfsFile2.RESERVEFOLDER
							| VfsFile2.WITHCONTENT, true);
					assertEquals(list, "b,c.txt,_,%,d.txt,b.pdf,e.tx,c");
					int len = list == null ? 0 : list.length;
					for (int i = 0; i < len; i++) {
						VfsFile2Impl file = (VfsFile2Impl) list[i];
						assertTrue(file.getNode().getContainContent());
						File content = file.getNode().getContent();
						byte[] bs = content == null ? null : StmFunc.ungzipBytes(FileFunc.file2buf(content));
						if (file.getName().equalsIgnoreCase("c.txt")) {
							assertEquals(bs, TESTCONTENTXML.getBytes(StrFunc.UTF8));
						}
						else if (file.getName().equalsIgnoreCase("d.txt")) {
							assertEquals(bs, TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));
						}
						else {
							assertEquals(bs, 0);
						}
					}
				}
			}
			{//查询testdir/a目录
				//以下两个测试只有调试时才能看到结果,带通配符的查询sql是用的like,不带通配符的使用的是=
				{//查询条件中带通配符
					VfsFile2[] list = dir.listFiles("*.t?*", VfsFile2.FILTERFILE | VfsFile2.FILTERFOLDER, true);
					assertEquals(list, "c.txt,d.txt,e.tx");

				}
				{//查询条件中不带通配符
					VfsFile2[] list = dir.listFiles("%", VfsFile2.FILTERFILE | VfsFile2.FILTERFOLDER, true);
					assertEquals(list, "%");
				}
			}
		}
	}

	public void testGetParent() {
		getTestVfsFile().getParent();
		assertNull(getVfsFile(null).getParent());

		{//父目录和子文件都不存在时,子文件能够获得父目录
			VfsFile2 dir = getVfsFile("/1/a/b/c");
			VfsFile2 file = getVfsFile(dir, "d");

			assertFalse(dir.exists());
			assertFalse(file.exists());

			assertEquals(dir.getAbsolutePath(), file.getParent().getAbsolutePath());
		}
		{//父目录存在,子文件不存在,能够获得父目录
			VfsFile2 dir = getVfsFile("/2/a/b/c");
			VfsFile2 file = getVfsFile(dir, "d");

			assertFalse(dir.exists());
			assertFalse(file.exists());

			dir.ensureExists(true);

			assertTrue(dir.exists());
			assertFalse(file.exists());

			assertEquals(dir.getAbsolutePath(), file.getParent().getAbsolutePath());
		}
		{//文件存在时,是否能够获得父目录
			VfsFile2 dir = getVfsFile("/3/a/b/c");
			assertFalse(dir.exists());
			//测试文件
			VfsFile2 file1 = getVfsFile(dir, "d1");

			assertFalse(file1.exists());

			file1.ensureExists(false);

			dir = getNewVfsFile2(dir);
			assertTrue(dir.exists());
			assertTrue(file1.exists());

			assertEquals(dir.getAbsolutePath(), file1.getParent().getAbsolutePath());
			//测试目录
			VfsFile2 file2 = getVfsFile(dir, "d2");

			assertFalse(file2.exists());

			file2.ensureExists(true);

			assertTrue(dir.exists());
			assertTrue(file2.exists());

			assertEquals(dir.getAbsolutePath(), file2.getParent().getAbsolutePath());

		}
		{//根的父目录为null
			VfsFile2 root = getVfsFile(null);
			assertEquals(root.getAbsolutePath(), FileFunc.separator);
			assertNull(root.getParent());
		}
	}

	public void testGetName() {
		{//文件或目录不存在,获得名称
			VfsFile2 file = getVfsFile("/1/a/b/c");
			assertFalse(file.exists());

			assertEquals(file.getName(), "c");
		}
		{//文件存在,获得名称
			VfsFile2 file = getVfsFile("/2/a/b/c");
			assertFalse(file.exists());

			file.ensureExists(false);
			assertTrue(file.isFile());

			assertEquals(file.getName(), "c");
		}
		{//目录存在,获得名称
			VfsFile2 file = getVfsFile("/3/a/b/c");
			assertFalse(file.exists());

			file.ensureExists(true);
			assertTrue(file.isDirectory());

			assertEquals(file.getName(), "c");
		}
		{//根目录名称
			VfsFile2 file = getVfsFile("/");
			assertEquals(file.getName(), "");
		}
	}

	public void testGetAbsolutePath() {
		{//文件不存在,获得文件路径
			VfsFile2 file = getVfsFile("/1/a/b/c");
			assertFalse(file.exists());

			assertEquals(file.getAbsolutePath(), "/1/a/b/c");
		}
		{//文件存在,获得文件路径
			VfsFile2 file = getVfsFile("/2/a/b/c");
			assertFalse(file.exists());

			file.ensureExists(false);
			assertTrue(file.isFile());

			assertEquals(file.getAbsolutePath(), "/2/a/b/c");
		}
		{//目录存在,获得文件路径
			VfsFile2 file = getVfsFile("/3/a/b/c");
			assertFalse(file.exists());

			file.ensureExists(true);
			assertTrue(file.isDirectory());

			assertEquals(file.getAbsolutePath(), "/3/a/b/c");
		}
		{//获得根目录的路径
			VfsFile2 file = getVfsFile("/");
			assertEquals(file.getAbsolutePath(), FileFunc.separator);
		}
	}

	public void testGetOwner() throws Exception {
		{//文件不存在,获得owner会抛出异常
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());

			try {
				file.getOwner();
				fail("不存在的文件获得owner,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//文件存在,获得owner
			VfsFile2 file = getVfsFile("/2/a/b");
			assertFalse(file.exists());

			file.ensureExists(false);
			assertTrue(file.isFile());

			assertEquals(file.getOwner(), getOperator().getId());
		}
		{//目录存在,获得owner
			VfsFile2 file = getVfsFile("/3/a/b");
			assertFalse(file.exists());

			file.ensureExists(true);
			assertTrue(file.isDirectory());

			assertEquals(file.getOwner(), getOperator().getId());
		}
	}

	public void testSetOwner() throws Exception {
		{//文件或目录不存在,设置owner会抛出异常
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());

			try {
				file.setOwner("test");
				fail("文件不存在,设置owner会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//设置文件的owner
			VfsFile2 file = getVfsFile("/2/a/b");

			file.ensureExists(false);
			assertTrue(file.isFile());

			String owner = "test";
			file.setOwner(owner);
			assertEquals(file.getOwner(), owner);
		}
		{//设置目录的owner
			VfsFile2 file = getVfsFile("/3/a/b");

			file.ensureExists(true);
			assertTrue(file.isDirectory());

			String owner = "test";
			file.setOwner(owner);
			assertEquals(file.getOwner(), owner);
		}
	}

	public void testSetLastModified() throws Exception {
		{//文件不存在,设置最后修改时间会出现异常
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());

			try {
				file.setLastModified(System.currentTimeMillis());
				fail("文件不存在,设置最后修改时间会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//文件存在时,设置最后修改时间
			VfsFile2 file = getVfsFile("/2/a/b");

			file.ensureExists(false);
			assertTrue(file.isFile());

			long time = System.currentTimeMillis();
			file.setLastModified(time);

			/**
			 * 在mssql中设置时间后与取出来的时间几乎总是不相等,原因不明
			 */
			if (getConnectionFactory().getDbType().isMssql()) {
				assertEquals(file.getLastModified(), time, 10);
			}
			else {
				assertEquals(file.getLastModified(), time);
			}
		}
		{//目录存在时设置最后修改时间
			VfsFile2 file = getVfsFile("/3/a/b");

			file.ensureExists(true);
			assertTrue(file.isDirectory());

			long time = System.currentTimeMillis();
			file.setLastModified(time);

			/**
			 * 在mssql中设置时间后与取出来的时间几乎总是不相等,原因不明
			 */
			if (getConnectionFactory().getDbType().isMssql()) {
				assertEquals(file.getLastModified(), time, 10);
			}
			else {
				assertEquals(file.getLastModified(), time);
			}
		}
	}

	public void testGetLastModified() throws Exception {
		{//文件不存在时,获得最后修改时间会抛出异常
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());

			try {
				file.getLastModified();
				fail("文件不存在,获得最后修改时间会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//文件存在时,获得最后修改时间
			//与setLastModified测试相同
		}
		{//目录存在时,获得最后修改时间
			//与setLastModified测试相同
		}
	}

	public void testGetOperator() throws Exception {
		{//文件不存在时,获得owner
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());

			assertEquals(file.getOperator().getId(), getOperator().getId());
		}
		{//文件存在时,获得owner
			VfsFile2 file = getVfsFile("/2/a/b");
			assertFalse(file.exists());

			file.ensureExists(false);
			assertTrue(file.isFile());

			assertEquals(file.getOperator().getId(), getOperator().getId());
		}
		{//目录存在时,获得owner
			VfsFile2 file = getVfsFile("/3/a/b");
			assertFalse(file.exists());

			file.ensureExists(true);
			assertTrue(file.isDirectory());

			assertEquals(file.getOperator().getId(), getOperator().getId());
		}
	}

	public void testLength() throws Exception {
		{//文件不存在时,获得大小会抛出异常
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());

			try {
				file.length();
				fail("文件不存在,获得文件的大小会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//文件存在时,获得文件的大小 
			VfsFile2 file = getVfsFile("/2/a/b");
			assertFalse(file.exists());

			file.ensureExists(false);
			assertTrue(file.isFile());

			assertEquals(file.length(), 0);

			file.saveAsString(TESTCONTENT, StrFunc.UTF8);
			assertEquals(file.length(), TESTCONTENT.getBytes(StrFunc.UTF8).length);

			file.saveAsString(TESTCONTENT, StrFunc.GBK);
			assertEquals(file.length(), TESTCONTENT.getBytes(StrFunc.GBK).length);
		}
		{//目录存在时,获得的大小始终为0
			VfsFile2 file = getVfsFile("/3/a/b");
			assertFalse(file.exists());

			file.ensureExists(true);
			assertTrue(file.isDirectory());

			assertEquals(file.length(), 0);
		}
	}

	public void testRemove() throws Exception {
		{//如果文件不存在,直接返回
			VfsFile2 file = getVfsFile("/1/a/b/c");
			assertFalse(file.exists());

			file.remove();
		}
		{//删除文件本身
			VfsFile2 file = getVfsFile("/2/a/b/c");
			assertFalse(file.exists());

			file.ensureExists(false);
			assertTrue(file.isFile());

			file.remove();

			assertFalse(file.exists());
		}
		{//删除目录,目录下没有子文件
			VfsFile2 file = getVfsFile("/3/a/b/c");
			assertFalse(file.exists());

			file.ensureExists(true);
			assertTrue(file.isDirectory());

			file.remove();

			assertFalse(file.exists());
		}
		{//删除目录,目录下的所有文件都会被删除
			VfsFile2 dir = getVfsFile("/4/");
			VfsFile2 file1 = getVfsFile(dir, "a");
			VfsFile2 file2 = getVfsFile(dir, "b");
			assertFalse(dir.exists());
			assertFalse(file1.exists());
			assertFalse(file2.exists());

			dir.ensureExists(true);
			file1.ensureExists(true);
			file2.ensureExists(false);
			assertTrue(dir.isDirectory());
			assertTrue(file1.isDirectory());
			assertTrue(file2.isFile());

			dir.remove();

			file1 = getNewVfsFile2(file1);
			file2 = getNewVfsFile2(file2);
			assertFalse(dir.exists());
			assertFalse(file1.exists());
			assertFalse(file2.exists());
		}
		{//根目录是无法删除的
			VfsFile2 file = getVfsFile(null);
			assertTrue(file.isDirectory());

			file.remove();

			assertTrue(file.isDirectory());
		}
		{//测试特殊字段_和%为名称
			{//测试_
				VfsFile2 dir = getVfsFile("/5/a/b");
				VfsFile2 uFile = getVfsFile(dir, "_");
				VfsFile2 aFile = getVfsFile(dir, "a");
				VfsFile2 bFile = getVfsFile(uFile, "b");
				VfsFile2 cFile = getVfsFile(aFile, "c");

				dir.ensureExists(true);
				uFile.ensureExists(true);
				aFile.ensureExists(true);
				bFile.ensureExists(false);
				cFile.ensureExists(false);

				assertEquals(dir.listFiles(), "_,a");
				assertEquals(aFile.listFiles(), "c");

				uFile.remove();

				assertEquals(dir.listFiles(), "a");
				//TODO
				//assertEquals(aFile.listFiles(), "c");
			}
			{//测试%
				VfsFile2 dir = getVfsFile("/5/a/b");
				VfsFile2 uFile = getVfsFile(dir, "%");
				VfsFile2 aFile = getVfsFile(dir, "a");
				VfsFile2 bFile = getVfsFile(uFile, "b");
				VfsFile2 cFile = getVfsFile(aFile, "c");

				dir.ensureExists(true);
				uFile.ensureExists(true);
				aFile.ensureExists(true);
				bFile.ensureExists(false);
				cFile.ensureExists(false);

				assertEquals(dir.listFiles(), "%,a");
				//TODO
				//assertEquals(aFile.listFiles(), "c");

				uFile.remove();

				assertEquals(dir.listFiles(), "a");
				//TODO
				//assertEquals(aFile.listFiles(), "c");
			}
		}
	}

	public void testRenameTo() throws Exception {
		{//新的文件名不包含/和\\
			{//文件
				VfsFile2 file = getVfsFile("/1/a/b");
				file.ensureExists(false);
				assertTrue(file.isFile());
				file.saveAsString(TESTCONTENT, StrFunc.UTF8);

				assertEquals(file.getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));

				VfsFile2 dest = getVfsFile(file.getParent(), "c");
				assertFalse(dest.exists());

				assertTrue(file.renameTo(dest.getName()));
				assertFalse(file.exists());

				dest = getNewVfsFile2(dest);
				assertTrue(dest.isFile());
				assertEquals(dest.getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));

				assertEquals(file.getParent().getAbsolutePath(), dest.getParent().getAbsolutePath());
			}
			{//目录
				{//目录下没有子文件
					VfsFile2 file = getVfsFile("/11/a/b");
					file.ensureExists(true);
					assertTrue(file.isDirectory());
					assertEquals(file.listFiles(), 0);

					VfsFile2 dest = getVfsFile(file.getParent(), "c");
					assertFalse(dest.exists());

					assertTrue(file.renameTo(dest.getName()));

					dest = getNewVfsFile2(dest);
					assertFalse(file.exists());
					assertTrue(dest.isDirectory());

				}
				{//目录下有子文件
					VfsFile2 file = getVfsFile("/12/a/b");
					file.ensureExists(true);
					assertTrue(file.isDirectory());

					VfsFile2 aFile = getVfsFile(file, "a");
					VfsFile2 bFile = getVfsFile(file, "b.txt");
					aFile.ensureExists(true);
					bFile.ensureExists(false);

					assertEquals(file.listFiles(), "a,b.txt");

					VfsFile2 dest = getVfsFile(file.getParent(), "c");
					assertFalse(dest.exists());

					assertTrue(file.renameTo(dest.getName()));

					dest = getNewVfsFile2(dest);
					assertFalse(file.exists());
					assertTrue(dest.isDirectory());

					assertEquals(dest.listFiles(), "a,b.txt");
				}
			}
		}
		{//新的文件名包含/或\\
			{//重命名文件,如果目的目录不存在,会自动创建
				VfsFile2 file = getVfsFile("/2/a/b");
				VfsFile2 dest = getVfsFile("/3/a/b");
				file.ensureExists(false);
				assertTrue(file.isFile());
				assertFalse(dest.exists());
				assertFalse(dest.getParent().exists());

				assertTrue(file.renameTo(dest.getAbsolutePath()));

				dest = getNewVfsFile2(dest);
				assertFalse(file.exists());
				assertTrue(dest.isFile());
				assertTrue(dest.getParent().isDirectory());
			}
			{//重命名目录,如果目的目录不存在,会自动创建
				{//目录下没有子文件
					VfsFile2 file = getVfsFile("/21/a/b");
					VfsFile2 dest = getVfsFile("/31/a/b");
					assertFalse(file.exists());
					file.ensureExists(true);
					assertTrue(file.isDirectory());
					assertFalse(dest.exists());
					assertFalse(dest.getParent().exists());

					assertTrue(file.renameTo(dest.getAbsolutePath()));

					dest = getNewVfsFile2(dest);
					assertFalse(file.exists());
					assertTrue(dest.isDirectory());
					assertTrue(dest.getParent().isDirectory());
				}
				{//目录下有子文件
					VfsFile2 file = getVfsFile("/22/a/b");
					VfsFile2 dest = getVfsFile("/32/a/b");
					assertFalse(file.exists());
					file.ensureExists(true);
					assertTrue(file.isDirectory());
					assertFalse(dest.exists());
					assertFalse(dest.getParent().exists());

					VfsFile2 aFile = getVfsFile(file, "a");
					VfsFile2 bFile = getVfsFile(file, "b.txt");
					aFile.ensureExists(true);
					bFile.ensureExists(false);

					assertTrue(file.renameTo(dest.getAbsolutePath()));

					dest = getNewVfsFile2(dest);
					assertFalse(file.exists());
					assertTrue(dest.isDirectory());
					assertTrue(dest.getParent().isDirectory());
					assertEquals(dest.listFiles(), "a,b.txt");
				}
			}
		}
		{//文件名不合法,会抛出异常
			{//文件名为空,会抛出异常
				{//文件
					try {
						VfsFile2 file = getVfsFile("/4/a/b");
						file.ensureExists(false);
						file.renameTo(null);
						fail("重命名的名称为null,会抛出异常");
					}
					catch (Exception e) {
					}
					try {
						VfsFile2 file = getVfsFile("/44/a/b");
						file.ensureExists(false);
						file.renameTo("");
						fail("重命名的名称为空,会抛出异常");
					}
					catch (Exception e) {
					}
				}
				{//目录
					try {
						VfsFile2 file = getVfsFile("/4/a/b");
						file.ensureExists(true);
						file.renameTo(null);
						fail("重命名的名称为null,会抛出异常");
					}
					catch (Exception e) {
					}
					try {
						VfsFile2 file = getVfsFile("/44/a/b");
						file.ensureExists(true);
						file.renameTo("");
						fail("重命名的名称为空,会抛出异常");
					}
					catch (Exception e) {
					}
				}
			}
			{//文件名中有不合法的字段,会出现异常
				class CheckFileName {
					public void check(boolean isdir, boolean haschild) {
						if (!isdir && haschild)
							return;
						String invalidChars = "/\\*?\"<>|\r\n\t\b\f";
						for (int i = 0, len = invalidChars.length(); i < len; i++) {
							try {
								VfsFile2 file = getVfsFile("/45/a/b/" + i);
								file.ensureExists(isdir);

								if (isdir && haschild) {
									getVfsFile(file, "a").ensureExists(true);
									getVfsFile(file, "b.txt").ensureExists(false);
								}

								file.renameTo(String.valueOf(invalidChars.charAt(i)));
								fail("文件名不合法,不能重命名");
							}
							catch (Exception e) {
							}
						}
					}
				}
				new CheckFileName().check(false, false);
				new CheckFileName().check(true, false);
				new CheckFileName().check(false, true);
				new CheckFileName().check(true, true);
			}
		}
		{//文件不存在,会抛出异常
			{//新名称没有/或\\
				VfsFile2 file = getVfsFile("/5/a/b");
				assertFalse(file.exists());

				try {
					file.renameTo("c");
					fail("重命名时文件不存在,会抛出异常");
				}
				catch (Exception e) {
				}
			}
			{//新名称有/或\\
				VfsFile2 file = getVfsFile("/5/a/b");
				assertFalse(file.exists());

				try {
					file.renameTo("/55/a/b/c");
					fail("重命名时文件不存在,会抛出异常");
				}
				catch (Exception e) {
				}
			}
		}
		{//重命名后的文件已经存在
			{//新名称没有/或\\
				{//文件,已经存在同名的文件
					VfsFile2 file = getVfsFile("/6/a/b");
					assertFalse(file.exists());
					file.ensureExists(false);

					VfsFile2 dest = getVfsFile(file.getParent(), "c");
					dest.ensureExists(false);

					try {
						file.renameTo(dest.getName());
						fail("文件已经存在,重命名会出现异常");
					}
					catch (Exception e) {
					}
				}
				{//目录,已经存在同名的目录
					VfsFile2 file = getVfsFile("/61/a/b");
					assertFalse(file.exists());
					file.ensureExists(true);

					VfsFile2 dest = getVfsFile(file.getParent(), "c");
					dest.ensureExists(true);

					try {
						file.renameTo(dest.getName());
						fail("文件已经存在,重命名会出现异常");
					}
					catch (Exception e) {
					}
				}
				{//文件重命名,已经存在同名的目录
					VfsFile2 file = getVfsFile("/62/a/b");
					assertFalse(file.exists());
					file.ensureExists(false);

					VfsFile2 dest = getVfsFile(file.getParent(), "c");
					dest.ensureExists(true);

					try {
						file.renameTo(dest.getName());
						fail("文件已经存在,重命名会出现异常");
					}
					catch (Exception e) {
					}
				}
				{//目录重命名,已经存在同名的文件
					VfsFile2 file = getVfsFile("/63/a/b");
					assertFalse(file.exists());
					file.ensureExists(true);

					VfsFile2 dest = getVfsFile(file.getParent(), "c");
					dest.ensureExists(false);

					try {
						file.renameTo(dest.getName());
						fail("文件已经存在,重命名会出现异常");
					}
					catch (Exception e) {
					}
				}
			}
			{//新名称有/或\\
				{//文件,已经存在同名的文件
					VfsFile2 file = getVfsFile("/64/a/b");
					assertFalse(file.exists());
					file.ensureExists(false);

					VfsFile2 dest = getVfsFile("/641/a/b1");
					dest.ensureExists(false);

					try {
						file.renameTo(dest.getAbsolutePath());
						fail("文件已经存在,重命名会出现异常");
					}
					catch (Exception e) {
					}
				}
				{//目录,已经存在同名的目录
					VfsFile2 file = getVfsFile("/65/a/b");
					assertFalse(file.exists());
					file.ensureExists(true);

					VfsFile2 dest = getVfsFile("/651/a/b1");
					dest.ensureExists(true);

					try {
						file.renameTo(dest.getAbsolutePath());
						fail("文件已经存在,重命名会出现异常");
					}
					catch (Exception e) {
					}
				}
				{//文件重命名,已经存在同名的目录
					VfsFile2 file = getVfsFile("/66/a/b");
					assertFalse(file.exists());
					file.ensureExists(false);

					VfsFile2 dest = getVfsFile("/661/a/b1");
					dest.ensureExists(true);

					try {
						file.renameTo(dest.getAbsolutePath());
						fail("文件已经存在,重命名会出现异常");
					}
					catch (Exception e) {
					}
				}
				{//目录重命名,已经存在同名的文件
					VfsFile2 file = getVfsFile("/67/a/b");
					assertFalse(file.exists());
					file.ensureExists(true);

					VfsFile2 dest = getVfsFile("/671/a/b1");
					dest.ensureExists(false);

					try {
						file.renameTo(dest.getAbsolutePath());
						fail("文件已经存在,重命名会出现异常");
					}
					catch (Exception e) {
					}
				}
			}
		}
		{//重命名后的文件是当前文件的子文件
			VfsFile2 file = getVfsFile("/68/a/b");
			assertFalse(file.exists());
			file.ensureExists(true);

			try {
				file.renameTo(file.getAbsolutePath() + "/c");
				fail("重命名为当前目录的子文件,会出现异常");
			}
			catch (Exception e) {
			}
		}
	}

	public void testRenameTo2() throws Exception {
		{//重命名后的文件已经存在
			{//新名称没有/或\\
				{//文件,已经存在同名的文件
					VfsFile2 file = getVfsFile("/6/a/b");
					assertFalse(file.exists());
					file.ensureExists(false);
					file.saveAsString(TESTCONTENT, StrFunc.UTF8);

					VfsFile2 dest = getVfsFile(file.getParent(), "c");
					dest.ensureExists(false);

					assertTrue(file.renameTo(dest.getName(), true));
					dest = getNewVfsFile2(dest);
					assertEquals(dest.getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));
				}
				{//目录,已经存在同名的目录
					VfsFile2 file = getVfsFile("/61/a/b");
					assertFalse(file.exists());
					file.ensureExists(true);

					VfsFile2 dest = getVfsFile(file.getParent(), "c");
					dest.ensureExists(true);

					assertTrue(file.renameTo(dest.getName(), true));
				}
				{//文件重命名,已经存在同名的目录
					VfsFile2 file = getVfsFile("/62/a/b");
					assertFalse(file.exists());
					file.ensureExists(false);
					file.saveAsString(TESTCONTENT, StrFunc.UTF8);

					VfsFile2 dest = getVfsFile(file.getParent(), "c");
					dest.ensureExists(true);

					assertTrue(file.renameTo(dest.getName(), true));
					dest = getNewVfsFile2(dest);
					assertTrue(dest.isFile());
					assertEquals(dest.getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));
				}
				{//目录重命名,已经存在同名的文件
					VfsFile2 file = getVfsFile("/63/a/b");
					assertFalse(file.exists());
					file.ensureExists(true);

					VfsFile2 dest = getVfsFile(file.getParent(), "c");
					dest.ensureExists(false);

					assertTrue(file.renameTo(dest.getName(), true));
					assertTrue(getNewVfsFile2(dest).isDirectory());
				}
			}
			{//新名称有/或\\
				{//文件,已经存在同名的文件
					VfsFile2 file = getVfsFile("/64/a/b");
					assertFalse(file.exists());
					file.ensureExists(false);
					file.saveAsString(TESTCONTENT, StrFunc.UTF8);

					VfsFile2 dest = getVfsFile("/641/a/b1");
					dest.ensureExists(false);

					assertTrue(file.renameTo(dest.getAbsolutePath(), true));
					dest = getNewVfsFile2(dest);
					assertEquals(dest.getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));
				}
				{//目录,已经存在同名的目录
					VfsFile2 file = getVfsFile("/65/a/b");
					assertFalse(file.exists());
					file.ensureExists(true);

					VfsFile2 dest = getVfsFile("/651/a/b1");
					dest.ensureExists(true);

					assertTrue(file.renameTo(dest.getAbsolutePath(), true));
					assertTrue(getNewVfsFile2(dest).isDirectory());
				}
				{//文件重命名,已经存在同名的目录
					VfsFile2 file = getVfsFile("/66/a/b");
					assertFalse(file.exists());
					file.ensureExists(false);
					file.saveAsString(TESTCONTENT, StrFunc.UTF8);

					VfsFile2 dest = getVfsFile("/661/a/b1");
					dest.ensureExists(true);

					assertTrue(file.renameTo(dest.getAbsolutePath(), true));
					dest = getNewVfsFile2(dest);
					assertTrue(dest.isFile());
					assertEquals(dest.getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));
				}
				{//目录重命名,已经存在同名的文件
					VfsFile2 file = getVfsFile("/67/a/b");
					assertFalse(file.exists());
					file.ensureExists(true);

					VfsFile2 dest = getVfsFile("/671/a/b1");
					dest.ensureExists(false);

					assertTrue(file.renameTo(dest.getAbsolutePath(), true));
					assertTrue(getNewVfsFile2(dest).isDirectory());
				}
			}
		}
	}

	public void testMoveTo() throws Exception {
		{//目标目录不存在,会自动创建
			{//文件
				VfsFile2 file = getVfsFile("/1/a/b");
				file.ensureExists(false);

				VfsFile2 dest = getVfsFile("/11/a/b");
				assertFalse(dest.exists());

				VfsFile2 destFile = getVfsFile(dest, file.getName());
				assertFalse(destFile.exists());

				assertTrue(file.moveTo(dest));

				dest = getNewVfsFile2(dest);
				destFile = getNewVfsFile2(destFile);
				assertFalse(file.exists());
				assertTrue(dest.isDirectory());
				assertTrue(destFile.isFile());
			}
			{//目录
				{//没有子目录
					VfsFile2 file = getVfsFile("/12/a/b");
					file.ensureExists(true);

					VfsFile2 dest = getVfsFile("/121/a/b");
					assertFalse(dest.exists());

					VfsFile2 destFile = getVfsFile(dest, file.getName());
					assertFalse(destFile.exists());

					assertTrue(file.moveTo(dest));

					destFile = getNewVfsFile2(destFile);
					assertFalse(file.exists());
					assertTrue(dest.isDirectory());
					assertTrue(destFile.isDirectory());
				}
				{//有子目录
					VfsFile2 file = getVfsFile("/13/a/b");
					file.ensureExists(true);

					VfsFile2 dest = getVfsFile("/131/a/b");
					assertFalse(dest.exists());

					getVfsFile(file, "a").ensureExists(true);//目录
					getVfsFile(file, "b.txt").ensureExists(false);//文件

					VfsFile2 destFile = getVfsFile(dest, file.getName());
					assertFalse(destFile.exists());

					assertTrue(file.moveTo(dest));

					assertFalse(file.exists());
					assertTrue(dest.isDirectory());
					destFile = getNewVfsFile2(destFile);
					assertTrue(destFile.isDirectory());

					assertEquals(destFile.listFiles(), "a,b.txt");
				}
			}
		}
		{//文件不存在,会抛出异常
			VfsFile2 file = getVfsFile("/2/a/b");
			assertFalse(file.exists());

			VfsFile2 dest = getVfsFile("/22/a/b");
			dest.ensureExists(true);

			try {
				file.moveTo(dest);
				fail("移动不存在的文件,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//目标目录不是目录
			VfsFile2 file = getVfsFile("/3/a/b");
			file.ensureExists(false);

			VfsFile2 dest = getVfsFile("/33/a/b");
			dest.ensureExists(false);

			try {
				file.moveTo(dest);
				fail("目标目录不是目录,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//目标目录是本文件的子目录
			VfsFile2 file = getVfsFile("/4/a/b");
			file.ensureExists(true);

			VfsFile2 dest = getVfsFile(file, "c");
			dest.ensureExists(true);

			try {
				file.moveTo(dest);
				fail("目标目录是本文件的子目录,会抛出异常");
			}
			catch (Exception e) {
			}

		}
		{//目标目录下存在与本文件名相同的文件或目录
			{//存在同名文件
				VfsFile2 file = getVfsFile("/5/a/b");
				file.ensureExists(true);

				VfsFile2 dest = getVfsFile("/55/a/b");
				VfsFile2 destFile = getVfsFile(dest, file.getName());
				destFile.ensureExists(false);
				assertTrue(destFile.isFile());

				try {
					file.moveTo(dest);
					fail("目标目录下已经存在同名的文件,会抛出异常");
				}
				catch (Exception e) {
				}
			}
			{//存在同名目录
				VfsFile2 file = getVfsFile("/51/a/b");
				file.ensureExists(true);

				VfsFile2 dest = getVfsFile("/551/a/b");
				VfsFile2 destFile = getVfsFile(dest, file.getName());
				destFile.ensureExists(true);
				assertTrue(destFile.isDirectory());

				try {
					file.moveTo(dest);
					fail("目标目录下已经存在同名的目录,会抛出异常");
				}
				catch (Exception e) {
				}
			}
		}
	}

	public void testCopyTo() throws Exception {
		{//如果name为空,仍然使用原名称
			VfsFile2 file = getVfsFile("/1/a/b");
			file.ensureExists(false);

			VfsFile2 dest = getVfsFile("/11/a/b");
			dest.ensureExists(true);

			VfsFile2 destFile = getVfsFile(dest, file.getName());
			assertFalse(destFile.exists());

			assertTrue(file.copyTo(dest, null, false));

			destFile = getNewVfsFile2(destFile);
			assertTrue(file.isFile());
			assertTrue(destFile.isFile());
		}
		{//name不为空,使用指定名称
			{//文件
				VfsFile2 file = getVfsFile("/2/a/b");
				file.ensureExists(false);

				VfsFile2 dest = getVfsFile("/21/a/b");
				dest.ensureExists(true);

				VfsFile2 destFile = getVfsFile(dest, "c");
				assertFalse(destFile.exists());

				assertTrue(file.copyTo(dest, destFile.getName(), false));

				destFile = getNewVfsFile2(destFile);
				assertTrue(file.isFile());
				assertTrue(destFile.isFile());
			}
			{//目录,没有子文件
				VfsFile2 file = getVfsFile("/22/a/b");
				file.ensureExists(true);

				VfsFile2 dest = getVfsFile("/221/a/b");
				dest.ensureExists(true);

				VfsFile2 destFile = getVfsFile(dest, "c");
				assertFalse(destFile.exists());

				assertTrue(file.copyTo(dest, destFile.getName(), false));

				destFile = getNewVfsFile2(destFile);
				assertTrue(file.isDirectory());
				assertTrue(destFile.isDirectory());
			}
			{//目录,有子文件
				VfsFile2 file = getVfsFile("/23/a/b");
				file.ensureExists(true);

				VfsFile2 aFile = getVfsFile(file, "a");
				VfsFile2 bFile = getVfsFile(file, "b");
				VfsFile2 cFile = getVfsFile(file, "c.txt");
				VfsFile2 dFile = getVfsFile(bFile, "d.txt");
				VfsFile2 eFile = getVfsFile(bFile, "e");
				aFile.ensureExists(true);
				bFile.ensureExists(true);
				cFile.ensureExists(false);
				dFile.ensureExists(false);
				eFile.ensureExists(true);

				VfsFile2 dest = getVfsFile("/231/a/b");
				dest.ensureExists(true);

				VfsFile2 destFile = getVfsFile(dest, "c");
				assertFalse(destFile.exists());

				assertFalse(getVfsFile(destFile, aFile.getName()).isDirectory());
				assertFalse(getVfsFile(destFile, bFile.getName()).isDirectory());
				assertFalse(getVfsFile(destFile, cFile.getName()).isFile());
				assertFalse(getVfsFile(getVfsFile(destFile, bFile.getName()), dFile.getName()).isFile());
				assertFalse(getVfsFile(getVfsFile(destFile, bFile.getName()), eFile.getName()).isDirectory());

				assertTrue(file.copyTo(dest, destFile.getName(), false));

				assertTrue(file.isDirectory());
				destFile = getNewVfsFile2(destFile);
				assertTrue(destFile.isDirectory());

				assertTrue(getVfsFile(destFile, aFile.getName()).isDirectory());
				assertTrue(getVfsFile(destFile, bFile.getName()).isDirectory());
				assertTrue(getVfsFile(destFile, cFile.getName()).isFile());
				assertTrue(getVfsFile(getVfsFile(destFile, bFile.getName()), dFile.getName()).isFile());
				assertTrue(getVfsFile(getVfsFile(destFile, bFile.getName()), eFile.getName()).isDirectory());
				assertEquals(destFile.listFiles(), "a,b,c.txt");
				assertEquals(getVfsFile(destFile, bFile.getName()).listFiles(), "e,d.txt");
			}
		}
		{//name不合法,是否会检测文件名,并抛出异常
			VfsFile2 file = getVfsFile("/3/a/b");
			file.ensureExists(false);

			VfsFile2 dest = getVfsFile("/33/a/b");
			dest.ensureExists(true);

			try {
				String name = FileFunc.invalidFnChar;
				file.copyTo(dest, name, true);
				fail("文件名不合法,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//目标目录不存在,会自动创建
			VfsFile2 file = getVfsFile("/4/a/b");
			file.ensureExists(false);

			VfsFile2 dest = getVfsFile("/44/a/b");
			assertFalse(dest.exists());

			assertTrue(file.copyTo(dest, null, true));

			assertTrue(dest.isDirectory());
			assertTrue(getVfsFile(dest, file.getName()).isFile());
		}
		{//如果目标目录中已经存在同名文件,并且isoverwrite=false,会抛出异常
			VfsFile2 file = getVfsFile("/5/a/b");
			file.ensureExists(false);

			VfsFile2 dest = getVfsFile("/55/a/b");
			VfsFile2 destFile = getVfsFile(dest, file.getName());
			destFile.ensureExists(false);

			try {
				file.copyTo(dest, null, false);
				fail("目标目录中已经存在同名文件,并且不覆盖,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//如果目标目录已经存在同名文件,并且isoverwrite=true,会先删除原文件,再复制
			{//文件内容为空
				VfsFile2 file = getVfsFile("/61/a/b");
				file.ensureExists(false);

				VfsFile2 dest = getVfsFile("/616/a/b");
				VfsFile2 destFile = getVfsFile(dest, file.getName());
				destFile.ensureExists(false);

				dest = getNewVfsFile2(dest);
				assertTrue(dest.isDirectory());
				destFile.saveAsString(TESTCONTENT, StrFunc.UTF8);
				assertEquals(destFile.getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));

				assertTrue(file.copyTo(dest, null, true));
				destFile = getNewVfsFile2(destFile);
				assertEquals(destFile.getAsBytes(), 0);
			}
			{//文件内容不为空
				VfsFile2 file = getVfsFile("/62/a/b");
				file.ensureExists(false);
				file.saveAsString(TESTCONTENTXML, StrFunc.UTF8);

				VfsFile2 dest = getVfsFile("/626/a/b");
				VfsFile2 destFile = getVfsFile(dest, file.getName());
				destFile.ensureExists(false);

				dest = getNewVfsFile2(dest);
				assertTrue(dest.isDirectory());
				destFile.saveAsString(TESTCONTENT, StrFunc.UTF8);
				assertEquals(destFile.getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));

				assertTrue(file.copyTo(dest, null, true));
				destFile = getNewVfsFile2(destFile);
				assertEquals(destFile.getAsBytes(), TESTCONTENTXML.getBytes(StrFunc.UTF8));

			}
			{//文件内容为空,存在同名目录,会删除目录,再复制文件
				{//目录没有子文件
					VfsFile2 file = getVfsFile("/63/a/b");
					file.ensureExists(false);

					VfsFile2 dest = getVfsFile("/636/a/b");
					VfsFile2 destFile = getVfsFile(dest, file.getName());
					destFile.ensureExists(true);

					dest = getNewVfsFile2(dest);
					assertTrue(dest.isDirectory());
					assertTrue(destFile.isDirectory());

					assertTrue(file.copyTo(dest, null, true));
					destFile = getNewVfsFile2(destFile);
					assertTrue(destFile.isFile());
				}
				{//目录有子文件
					VfsFile2 file = getVfsFile("/64/a/b");
					file.ensureExists(false);

					VfsFile2 dest = getVfsFile("/646/a/b");
					VfsFile2 destFile = getVfsFile(dest, file.getName());
					VfsFile2 aFile = getVfsFile(destFile, "a");
					VfsFile2 bFile = getVfsFile(destFile, "b.txt");
					destFile.ensureExists(true);
					aFile.ensureExists(true);
					bFile.ensureExists(false);

					dest = getNewVfsFile2(dest);
					assertTrue(dest.isDirectory());
					assertTrue(destFile.isDirectory());
					assertTrue(aFile.isDirectory());
					assertTrue(bFile.isFile());

					assertTrue(file.copyTo(dest, null, true));

					destFile = getNewVfsFile2(destFile);
					aFile = getNewVfsFile2(aFile);
					bFile = getNewVfsFile2(bFile);
					assertTrue(destFile.isFile());
					assertFalse(aFile.exists());
					assertFalse(bFile.exists());
				}
			}
			{//当前文件为目录,存在同名文件,会删除文件,再复制目录
				{//目录没有子文件
					VfsFile2 file = getVfsFile("/65/a/b");
					file.ensureExists(true);

					VfsFile2 dest = getVfsFile("/656/a/b");
					VfsFile2 destFile = getVfsFile(dest, file.getName());
					destFile.ensureExists(false);

					dest = getNewVfsFile2(dest);
					assertTrue(dest.isDirectory());
					assertTrue(destFile.isFile());

					assertTrue(file.copyTo(dest, null, true));
					destFile = getNewVfsFile2(destFile);
					assertTrue(destFile.isDirectory());
				}
				{//目录有子文件
					VfsFile2 file = getVfsFile("/66/a/b");
					VfsFile2 aFile = getVfsFile(file, "a");
					VfsFile2 bFile = getVfsFile(file, "b.txt");
					file.ensureExists(true);
					aFile.ensureExists(true);
					bFile.ensureExists(false);
					bFile.saveAsString(TESTCONTENT, StrFunc.UTF8);

					VfsFile2 dest = getVfsFile("/666/a/b");
					VfsFile2 destFile = getVfsFile(dest, file.getName());
					destFile.ensureExists(true);

					dest = getNewVfsFile2(dest);
					assertTrue(dest.isDirectory());
					assertTrue(destFile.isDirectory());

					assertTrue(file.copyTo(dest, null, true));

					destFile = getNewVfsFile2(destFile);
					assertTrue(destFile.isDirectory());
					assertTrue(getVfsFile(destFile, aFile.getName()).isDirectory());
					assertTrue(getVfsFile(destFile, bFile.getName()).isFile());
					assertEquals(getVfsFile(destFile, bFile.getName()).getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));
				}
			}
		}
		{//此文件不存在,会抛出异常
			VfsFile2 file = getVfsFile("/7/a/b");
			assertFalse(file.exists());

			VfsFile2 dest = getVfsFile("/77/a/b");
			dest.ensureExists(true);

			try {
				file.copyTo(dest, null, true);
				fail("文件不存在,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//目标目录不是目录,会抛出异常
			VfsFile2 file = getVfsFile("/8/a/b");
			file.ensureExists(true);

			VfsFile2 dest = getVfsFile("/88/a/b");
			dest.ensureExists(false);

			try {
				file.copyTo(dest, null, true);
				fail("目标目录不是目录,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//此文件与目标目录相同,会抛出异常
			VfsFile2 file = getVfsFile("/8/a/b");
			file.ensureExists(true);

			VfsFile2 dest = getVfsFile("/88/a/b");
			dest.ensureExists(false);

			try {
				file.copyTo(dest, null, true);
				fail("目标目录不是目录,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//目标目录是本目录的子目录,会抛出异常
			VfsFile2 file = getVfsFile("/9/a/b");
			file.ensureExists(true);

			VfsFile2 dest = getVfsFile(file, "c");
			dest.ensureExists(true);

			try {
				file.copyTo(dest, null, true);
				fail("目标目录是本目录的子目录,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//本文件已经在目标目录下,会抛出异常
			VfsFile2 file = getVfsFile("/1a/a/b");
			file.ensureExists(true);

			VfsFile2 dest = file.getParent();

			try {
				file.copyTo(dest, null, true);
				fail("本文件已经在目标目录下,会抛出异常");
			}
			catch (Exception e) {
			}
		}
	}

	public void testCheckExists() {
		{//文件不存在,检测是否存在会抛出异常
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());

			try {
				file.checkExists(false);
				fail("文件不存在,检测是否是文件会抛出异常");
			}
			catch (Exception e) {
			}

			try {
				file.checkExists(true);
				fail("文件不存在,检测是否是目录会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//文件存在时检测
			VfsFile2 file = getVfsFile("/2/a/b");
			assertFalse(file.exists());

			file.ensureExists(false);
			assertTrue(file.isFile());

			file.checkExists(false);

			try {
				file.checkExists(true);
				fail("检查文件是否是目录,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//目录存在时检测
			VfsFile2 file = getVfsFile("/3/a/b");
			assertFalse(file.exists());

			file.ensureExists(true);
			assertTrue(file.isDirectory());

			file.checkExists(true);

			try {
				file.checkExists(false);
				fail("检查目录是否是文件,会抛出异常");
			}
			catch (Exception e) {
			}
		}
	}

	public void testEnsureExists() throws Exception {
		{//父目录不存在,文件不存在,会自动创建文件和父目录
			VfsFile2 dir = getVfsFile("/1/a/b/c");
			VfsFile2 file = getVfsFile(dir, "d");
			assertFalse(dir.exists());
			assertFalse(file.exists());

			file.ensureExists(false);

			dir = getNewVfsFile2(dir);
			assertTrue(dir.isDirectory());
			assertTrue(file.isFile());
		}
		{//父目录不存在,目录不存在,会自动创建目录和父目录
			VfsFile2 dir = getVfsFile("/2/a/b/c");
			VfsFile2 file = getVfsFile(dir, "d");
			assertFalse(dir.exists());
			assertFalse(file.exists());

			file.ensureExists(true);

			dir = getNewVfsFile2(dir);
			assertTrue(dir.isDirectory());
			assertTrue(file.isDirectory());

		}
		{//父目录存在,文件不存在,会自动创建文件
			VfsFile2 dir = getVfsFile("/3/a/b/c");
			VfsFile2 file = getVfsFile(dir, "d");
			assertFalse(dir.exists());
			assertFalse(file.exists());

			dir.ensureExists(true);
			assertTrue(dir.isDirectory());
			assertFalse(file.exists());

			file.ensureExists(false);

			assertTrue(dir.isDirectory());
			assertTrue(file.isFile());
		}
		{//父目录存在,目录 不存在,会自动创建目录
			VfsFile2 dir = getVfsFile("/4/a/b/c");
			VfsFile2 file = getVfsFile(dir, "d");
			assertFalse(dir.exists());
			assertFalse(file.exists());

			dir.ensureExists(true);
			assertTrue(dir.isDirectory());
			assertFalse(file.exists());

			file.ensureExists(true);

			assertTrue(dir.isDirectory());
			assertTrue(file.isDirectory());
		}
		{//文件存在
			VfsFile2 dir = getVfsFile("/5/a/b/c");
			VfsFile2 file = getVfsFile(dir, "d");
			assertFalse(dir.exists());
			assertFalse(file.exists());

			dir.ensureExists(true);
			file.ensureExists(false);
			assertTrue(dir.isDirectory());
			assertTrue(file.isFile());

			//文件可能再次调用
			file.ensureExists(false);
		}
		{//目录存在
			VfsFile2 dir = getVfsFile("/6/a/b/c");
			VfsFile2 file = getVfsFile(dir, "d");
			assertFalse(dir.exists());
			assertFalse(file.exists());

			dir.ensureExists(true);
			file.ensureExists(true);
			assertTrue(dir.isDirectory());
			assertTrue(file.isDirectory());

			//文件可能再次调用
			file.ensureExists(true);
		}
		{//文件存在,但是调用ensureExists(true)会抛出异常
			VfsFile2 file = getVfsFile("/7/a/b/c/d");
			assertFalse(file.exists());

			file.ensureExists(false);

			assertTrue(file.isFile());

			try {
				file.ensureExists(true);
				fail("文件已经存在,调用ensureExists(true)会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//目录存在,但是调用ensureExists(false)会抛出异常
			VfsFile2 file = getVfsFile("/8/a/b/c/d");
			assertFalse(file.exists());

			file.ensureExists(true);

			assertTrue(file.isDirectory());

			try {
				file.ensureExists(false);
				fail("目录已经存在,调用ensureExists(true)会抛出异常");
			}
			catch (Exception e) {
			}
		}
	}

	public void testCreateTempFile() {
		{//没有传入文件名,会自动生成,并且与目录下的文件不重名
			VfsFile2 dir = getVfsFile("/1/a/b/c");
			dir.ensureExists(true);

			VfsFile2 file = dir.createTempFile(null, false);
			assertTrue(file.exists());

			assertEquals(dir.getAbsolutePath(), file.getParent().getAbsolutePath());
		}
		{//传入文件名,返回的文件名称与目录下的文件不重名
			VfsFile2 dir = getVfsFile("/2/a/b/c");
			VfsFile2 file = getVfsFile(dir, "d");
			file.ensureExists(false);

			dir = getNewVfsFile2(dir);
			VfsFile2 temp = dir.createTempFile(file.getName(), false);
			assertTrue(temp.exists());

			assertEquals(dir.getAbsolutePath(), temp.getParent().getAbsolutePath());
		}
		{//参数createit=true时,会创建文件或目录,父目录不存在,也会自动创建
			VfsFile2 dir = getVfsFile("/3/a/b/c");
			assertFalse(dir.exists());

			VfsFile2 file1 = dir.createTempFile(null, false);//文件
			VfsFile2 file2 = dir.createTempFile(null, true);//目录

			assertTrue(dir.isDirectory());
			assertTrue(file1.isFile());
			assertTrue(file2.isDirectory());
		}
		{//参数createit=false时,不会创建文件或目录,仅返回一个不存在的文件对象
			VfsFile2 dir = getVfsFile("/4/a/b/c");
			assertFalse(dir.exists());

			VfsFile2 file1 = dir.createTempFile(null, false);//文件
			VfsFile2 file2 = dir.createTempFile(null, true);//目录

			assertTrue(dir.exists());
			assertTrue(file1.isFile());
			assertTrue(file2.isDirectory());
		}
		{//当前文件不存在,返回一个当前文件下的文件对象
			VfsFile2 dir = getVfsFile("/5/a/b/c");
			assertFalse(dir.exists());

			VfsFile2 file = dir.createTempFile(null, false);

			assertTrue(dir.isDirectory());
			assertEquals(dir.getAbsolutePath(), file.getParent().getAbsolutePath());
		}
		{//当前文件存在,并且是目录,则返回当前目录下的一个文件对象
			VfsFile2 dir = getVfsFile("/6/a/b/c");
			dir.ensureExists(true);
			assertTrue(dir.isDirectory());

			VfsFile2 file = dir.createTempFile(null, false);

			assertTrue(file.isFile());
			assertEquals(dir.getAbsolutePath(), file.getParent().getAbsolutePath());
		}
		{//当前文件存在,并且是文件,则返回文件所在目录下的一个文件
			VfsFile2 file = getVfsFile("/7/a/b/c/d");
			file.ensureExists(false);
			assertTrue(file.isFile());

			VfsFile2 temp = file.createTempFile(null, false);

			assertTrue(temp.isFile());
			assertEquals(temp.getParent().getAbsolutePath(), file.getParent().getAbsolutePath());
		}
	}

	public void testSetChildrenOrder() throws Exception {
		VfsFile2 dir = getVfsFile("/1/a/b");
		{//构建测试环境
			/**
			 * 测试环境的目录结构如下:
			 * /a
			 *   /b
			 *     /d.txt
			 *   /c.txt
			 *   /e
			 *   /f
			 *   /g.pdf
			 */
			VfsFile2 aFile = getVfsFile(dir, "a");
			VfsFile2 bFile = getVfsFile(aFile, "b");
			VfsFile2 cFile = getVfsFile(aFile, "c.txt");
			VfsFile2 dFile = getVfsFile(bFile, "d.txt");
			VfsFile2 eFile = getVfsFile(aFile, "e");
			VfsFile2 fFile = getVfsFile(aFile, "f");
			VfsFile2 gFile = getVfsFile(aFile, "g.pdf");

			aFile.ensureExists(true);
			bFile.ensureExists(true);
			dFile.ensureExists(false);
			cFile.ensureExists(false);
			eFile.ensureExists(true);
			fFile.ensureExists(true);
			gFile.ensureExists(false);

			assertTrue(aFile.isDirectory());
			assertTrue(bFile.isDirectory());
			assertTrue(cFile.isFile());
			assertTrue(dFile.isFile());
			assertTrue(eFile.isDirectory());
			assertTrue(fFile.isDirectory());
			assertTrue(gFile.isFile());

			cFile.saveAsString(TESTCONTENTXML, StrFunc.UTF8);
			dFile.saveAsString(TESTCONTENTXML_GBK, StrFunc.GBK);
		}

		//获得默认的顺序
		VfsFile2 pfile = getVfsFile(dir, "a");
		VfsFile2[] dfs = pfile.listFiles();

		ArrayList dlist = new ArrayList();
		for (int i = 0, len = dfs.length; i < len; i++) {
			dlist.add(dfs[i].getName());
		}
		assertEquals(dfs, 5);
		{//此文件是文件,设置无效,会直接返回
			VfsFile2 file = getVfsFile(pfile, "c.txt");
			assertEquals(file.getAsBytes(), TESTCONTENTXML.getBytes(StrFunc.UTF8));

			//设置为空,内容不会改变
			file.setChildrenOrder(null);
			assertEquals(file.getAsBytes(), TESTCONTENTXML.getBytes(StrFunc.UTF8));

			//设置的内容不为空,内容仍然不会改变
			ArrayList list = new ArrayList(dlist);
			list.remove(0);
			file.setChildrenOrder(list);
			assertEquals(file.getAsBytes(), TESTCONTENTXML.getBytes(StrFunc.UTF8));
		}
		{//修改了列出顺序,能够正常获得
			//修改顺序
			ArrayList list = new ArrayList(dlist);
			list.add(list.remove(0));//移动位置
			list.add(list.remove(0));//移动位置
			String expFile1 = (String) list.remove(0);//不在列表中的文件
			String expFile2 = (String) list.remove(0);//不在列表中的文件

			pfile.setChildrenOrder(list);

			VfsFile2[] fs = pfile.listFiles();
			for (int i = 0, len = fs.length - 2; i < len; i++) {//这里len=fs.length-2是因为有两个文件不在列表中
				if (!StrFunc.compareText((String) list.get(i), fs[i].getName())) {
					fail("获得的文件顺序不正确");
				}
			}
			//还原顺序
			pfile.setChildrenOrder(dlist);
			fs = pfile.listFiles();
			for (int i = 0, len = fs.length; i < len; i++) {
				if (!StrFunc.compareText((String) dlist.get(i), fs[i].getName())) {
					fail("获得的文件顺序不正确");
				}
			}
		}
		{//不是直接子文件,即使设置了顺序也没有作用
			VfsFile2[] fs = pfile.listFiles(null, VfsFile2.RESERVEFILE + VfsFile2.RESERVEFOLDER, true);
			assertEquals(fs, dlist.size() + 1);
			for (int i = 0, len = dlist.size(); i < len; i++) {
				if (!StrFunc.compareText((String) dlist.get(i), fs[i].getName())) {
					fail("获得的文件顺序不正确");
				}
			}
			VfsFile2 lastFile = fs[fs.length - 1];
			if (!StrFunc.compareText(lastFile.getName(), getVfsFile(pfile, "/b/d.txt").getName())) {
				fail("获得的文件顺序不正确");
			}

			//设置d.txt为第一个,但是因为它不是/a的直接子文件,所以还是最后一个
			ArrayList list = new ArrayList(dlist.size() + 1);
			list.add(lastFile.getName());
			list.addAll(dlist);

			pfile.setChildrenOrder(list);

			fs = pfile.listFiles(null, VfsFile2.RESERVEFILE + VfsFile2.RESERVEFOLDER, true);
			assertEquals(fs, dlist.size() + 1);
			for (int i = 0, len = dlist.size(); i < len; i++) {
				if (!StrFunc.compareText((String) dlist.get(i), fs[i].getName())) {
					fail("获得的文件顺序不正确");
				}
			}
			lastFile = fs[fs.length - 1];
			if (!StrFunc.compareText(lastFile.getName(), getVfsFile(pfile, "/b/d.txt").getName())) {
				fail("获得的文件顺序不正确");
			}
		}
		{//测试文件重命名后依然有效
			ArrayList list = new ArrayList(dlist);
			String rename1before = (String) list.get(0);
			String rename2before = (String) list.get(3);
			String rename1after = rename1before + "1";
			String rename2after = rename2before + "2";

			list.set(0, rename1after);
			list.set(3, rename2after);

			//重命名
			getVfsFile(pfile, rename1before).renameTo(rename1after);
			getVfsFile(pfile, rename2before).renameTo(rename2after);

			pfile = getNewVfsFile2(pfile);
			VfsFile2[] fs = pfile.listFiles();
			for (int i = 0, len = fs.length; i < len; i++) {
				if (!StrFunc.compareText((String) list.get(i), fs[i].getName())) {
					fail("获得的文件顺序不正确");
				}
			}
			//还原顺序
			getVfsFile(pfile, rename1after).renameTo(rename1before);
			getVfsFile(pfile, rename2after).renameTo(rename2before);

			pfile = getNewVfsFile2(pfile);
			fs = pfile.listFiles();
			for (int i = 0, len = fs.length; i < len; i++) {
				if (!StrFunc.compareText((String) dlist.get(i), fs[i].getName())) {
					fail("获得的文件顺序不正确");
				}
			}

		}
	}

	public void testSetCharset() throws Exception {
		{//文件不存在,会抛出异常
			VfsFile2 file = getVfsFile("/1/a/b/c");
			assertFalse(file.exists());

			try {
				file.setCharset(StrFunc.UTF8);
				fail("文件不存在,不能设置编码");
			}
			catch (Exception e) {
			}
		}
		{//此文件是目录,会抛出异常
			VfsFile2 file = getVfsFile("/2/a/b/");
			file.ensureExists(true);
			assertTrue(file.isDirectory());

			try {
				file.setCharset(StrFunc.UTF8);
				fail("目录不能设置编码");
			}
			catch (Exception e) {
			}
		}
		{//此文件对象是文件,能够正常设置
			VfsFile2 file = getVfsFile("/3/a/b");
			file.ensureExists(false);
			assertTrue(file.isFile());

			file.setCharset(StrFunc.UTF8);
			assertEquals(StrFunc.UTF8, file.getCharset());

			file.setCharset(StrFunc.GBK);
			assertEquals(StrFunc.GBK, file.getCharset());
		}
		{//测试保存的内容是否用此编码保存
			VfsFile2 file = getVfsFile("/4/a/b");
			file.ensureExists(false);
			assertTrue(file.isFile());

			file.setCharset(StrFunc.UTF8);
			assertEquals(StrFunc.UTF8, file.getCharset());
			file.saveAsString(TESTCONTENT);
			assertEquals(TESTCONTENT.getBytes(StrFunc.UTF8), file.getAsBytes());

			file.setCharset(StrFunc.GBK);
			assertEquals(StrFunc.GBK, file.getCharset());
			try {
				assertEquals(TESTCONTENT.getBytes(StrFunc.GBK), file.getAsBytes());
				fail("编码应该不相等");
			}
			catch (Exception e) {
			}
			file.saveAsString(TESTCONTENT);
			assertEquals(TESTCONTENT.getBytes(StrFunc.GBK), file.getAsBytes());
		}
	}

	public void testGetCharset() throws Exception {
		{//文件不存在,会抛出异常
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());

			try {
				file.getCharset();
				fail("文件不存在,获得编码会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//文件可以获得编码
			VfsFile2 file = getVfsFile("/2/a/b");
			assertFalse(file.exists());
			file.ensureExists(false);
			assertTrue(file.isFile());

			assertNull(file.getCharset());

			file.setCharset(StrFunc.UTF8);
			assertEquals(file.getCharset(), StrFunc.UTF8);

			file.setCharset(StrFunc.GBK);
			assertEquals(file.getCharset(), StrFunc.GBK);
		}
		{//目录的编码始终为空
			VfsFile2 file = getVfsFile("/3/a/b");
			assertFalse(file.exists());
			file.ensureExists(true);
			assertTrue(file.isDirectory());

			assertNull(file.getCharset());
		}
	}

	public void testSetMimeType() throws Exception {
		{//文件不存在,会抛出异常
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());

			try {
				file.setMimeType("text/plain");
				fail("文件不存在,不能设置MimeType");
			}
			catch (Exception e) {
			}
		}
		{//如果是目录,会抛出异常
			VfsFile2 file = getVfsFile("/2/a/b");
			assertFalse(file.exists());
			file.ensureExists(true);

			try {
				file.setMimeType("text/plain");
				fail("目录不能设置MimeTYpe");
			}
			catch (Exception e) {
			}
		}
		{//正常设置文件的MimeType
			VfsFile2 file = getVfsFile("/3/a/b");
			assertFalse(file.exists());
			file.ensureExists(false);

			file.setMimeType("text/plain");
			file.setMimeType("application/pdf");
		}
	}

	public void testGetMimeType() throws Exception {
		{//如果文件不存在,会抛出异常
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());

			try {
				file.setMimeType("text/plain");
				fail("文件不存在,不能设置MimeType");
			}
			catch (Exception e) {
			}
		}
		{//如果是目录,返回null
			VfsFile2 file = getVfsFile("/2/a/b");
			assertFalse(file.exists());
			file.ensureExists(true);

			assertNull(file.getMimeType());
		}
		{//如果是文件,返回文件的MimeType
			VfsFile2 file = getVfsFile("/3/a/b");
			assertFalse(file.exists());
			file.ensureExists(false);

			assertNull(file.getMimeType());

			file.setMimeType("text/plain");
			assertEquals(file.getMimeType(), "text/plain");

			file.setMimeType("application/pdf");
			assertEquals(file.getMimeType(), "application/pdf");
		}
	}

	public void testGuessMimeType() {
		{//如果文件不存在,会抛出异常
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());

			try {
				file.guessMimeType();
				fail("文件不存在,不能获得MimeType");
			}
			catch (Exception e) {
			}
		}
		{//如果是目录,返回null
			VfsFile2 file = getVfsFile("/2/a/b");
			assertFalse(file.exists());
			file.ensureExists(true);
			assertTrue(file.isDirectory());

			assertNull(file.guessMimeType());
		}
		{//根据文件后缀获得文件的MimeType
			{//没有后缀的文件
				VfsFile2 file = getVfsFile("/3/a/b");
				assertFalse(file.exists());
				file.ensureExists(false);
				assertTrue(file.isFile());
				assertEquals(file.guessMimeType(), StrFunc.getContentType(null));
				//根据MimeType字段的值获得
				file.setMimeType("text/plain");
				assertEquals(file.guessMimeType(), file.getMimeType());
			}

			{//根据文件名后缀获得
				VfsFile2 file = getVfsFile("/3/a/c.cod");
				assertFalse(file.exists());
				file.ensureExists(false);
				assertTrue(file.isFile());

				assertEquals(file.guessMimeType(), "text/plain");
			}
			{//从bi系统定义的ContentType中获得
				VfsFile2 file = getVfsFile("/3/a/d.pdf");
				assertFalse(file.exists());
				file.ensureExists(false);
				assertTrue(file.isFile());

				assertEquals(file.guessMimeType(), "application/pdf");
			}
		}
	}

	public void testGetPropertyNames() {
		{//不存在的文件,获得属性名
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());

			try {
				assertEquals(file.getPropertyNames(), 0);
				fail("文件不存在,获得属性会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//文件存在时,获得属性名
			VfsFile2 file = getVfsFile("/2/a/b");
			file.ensureExists(false);

			assertEquals(file.getPropertyNames(), 0);
		}
		{//目录存在时,获得属性名
			VfsFile2 file = getVfsFile("/3/a/b");
			file.ensureExists(true);

			assertEquals(file.getPropertyNames(), 0);
		}

	}

	public void testGetProperty() {
		//TODO
	}

	public void testSetProperty() {
		{//文件不存在,会抛出异常
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());

			HashMap props = new HashMap();
			props.put("mender", getOperator().getId());

			try {
				file.setProperty(props);
				fail("文件不存在,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//设置文件的属性
			VfsFile2 file = getVfsFile("/2/a/b");
			file.ensureExists(false);
			assertTrue(file.isFile());

			long time = System.currentTimeMillis() + 1000;
			String newowner = "abc";
			String newcharset = StrFunc.UTF8;
			String newmimetype = "text/plain";
			assertFalse(time == file.getLastModified());
			assertFalse(StrFunc.compareText(file.getOwner(), newowner));
			assertFalse(StrFunc.compareText(file.getCharset(), newcharset));
			assertFalse(StrFunc.compareText(file.getMimeType(), newmimetype));

			HashMap props = new HashMap();
			props.put("lastModifyTime", new Timestamp(time));
			props.put("owner", newowner);
			props.put("charset", newcharset);
			props.put("mimeType", newmimetype);

			file.setProperty(props);
			assertEquals(file.getLastModified(), time, 100);
			assertEquals(file.getOwner(), newowner);
			assertEquals(file.getCharset(), newcharset);
			assertEquals(file.getMimeType(), newmimetype);
		}
		{//设置目录的属性
			VfsFile2 file = getVfsFile("/3/a/b");
			file.ensureExists(true);
			assertTrue(file.isDirectory());

			long time = System.currentTimeMillis() + 1000;
			String newowner = "abc";
			String newcharset = StrFunc.UTF8;
			String newmimetype = "text/plain";
			assertFalse(time == file.getLastModified());
			assertFalse(StrFunc.compareText(file.getOwner(), newowner));
			assertFalse(StrFunc.compareText(file.getCharset(), newcharset));
			assertFalse(StrFunc.compareText(file.getMimeType(), newmimetype));

			HashMap props = new HashMap();
			props.put("lastModifyTime", new Timestamp(time));
			props.put("owner", newowner);
			props.put("charset", newcharset);
			props.put("mimeType", newmimetype);

			file.setProperty(props);
			assertEquals(file.getLastModified(), time, 100);
			assertEquals(file.getOwner(), newowner);
			assertEquals(file.getCharset(), null);//目录设置charset无效
			assertEquals(file.getMimeType(), null);//目录设置mimetype无效
		}
		{//不支持的属性设置后是无效的
			VfsFile2 file = getVfsFile("/4/a/b");
			file.ensureExists(false);
			assertTrue(file.isFile());

			String newprop = "newprop";
			assertEquals(file.getProperty(newprop), null);

			HashMap props = new HashMap();
			props.put(newprop, newprop);

			file.setProperty(props);
			assertEquals(file.getProperty(newprop), null);
		}
	}

	public void testImportFiles() throws Exception {
		String temp = FileFunc.createTempFile(null, null, true, true, true, true);
		{//生成测试环境
			/**
			 * 生成的结构为:
			 * /a
			 *   /b
			 *     /d.txt
			 *   /c.txt 
			 */
			File aFile = new File(temp + "/a");
			aFile.mkdirs();

			File bFile = new File(temp + "/a/b");
			bFile.mkdirs();

			File cFile = new File(temp + "/a/c.txt");
			cFile.createNewFile();
			FileFunc.writeStrToFile(cFile.getAbsolutePath(), TESTCONTENTXML, false, StrFunc.UTF8);

			File dFile = new File(temp + "/a/b/d.txt");
			dFile.createNewFile();
			FileFunc.writeStrToFile(dFile.getAbsolutePath(), TESTCONTENTXML_GBK, false, StrFunc.GBK);
		}
		{//文件导入一个不存在的文件,直接返回
			VfsFile2 file = getVfsFile("/1/a/b/1.txt");
			file.ensureExists(false);

			File f = new File(temp + "/c");
			assertFalse(f.exists());

			//测试内容为空的情况
			assertEquals(file.getAsBytes(), 0);
			file.importFile(f);
			assertEquals(file.getAsBytes(), 0);

			//测试内容不为空的情况
			file.saveAsString(TESTCONTENT, StrFunc.UTF8);
			assertEquals(file.getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));
			file.importFile(f);
			assertEquals(file.getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));
		}
		{//目录导入一个不存在的文件,直接返回
			VfsFile2 file = getVfsFile("/2/a/b/c");
			assertFalse(file.exists());
			file.ensureExists(true);
			assertTrue(file.isDirectory());

			assertEquals(file.listFiles(), 0);

			File f = new File(temp + "/c");
			assertFalse(f.exists());

			file.importFile(f);
			assertEquals(file.listFiles(), 0);
		}
		{//当此文件是文件时,导入一个文件
			VfsFile2 file = getVfsFile("/3/a/b/1.txt");
			assertFalse(file.exists());
			file.ensureExists(false);
			assertTrue(file.isFile());

			assertEquals(file.getAsBytes(), 0);
			//导入c.txt
			file.importFile(new File(temp + "/a/c.txt"));
			assertEquals(file.getAsBytes(), TESTCONTENTXML.getBytes(StrFunc.UTF8));
			//导入d.txt
			file.importFile(new File(temp + "/a/b/d.txt"));
			assertEquals(file.getAsBytes(), TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));
		}
		{//当此文件是文件时,导入一个目录会抛出异常
			VfsFile2 file = getVfsFile("/4/a/b/1.txt");
			file.ensureExists(false);

			try {
				file.importFile(new File(temp + "/a"));
				fail("导入目录到文件会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//当此文件是目录时,导入一个文件
			VfsFile2 file = getVfsFile("/5/a/b");
			assertFalse(file.exists());
			file.ensureExists(true);
			assertTrue(file.isDirectory());
			assertEquals(file.listFiles(), 0);

			{//导入c.txt
				File f = new File(temp + "/a/c.txt");
				VfsFile2 cFile = getVfsFile(file, f.getName());
				assertFalse(cFile.exists());

				file.importFile(f);
				assertEquals(file.listFiles(), 1);

				cFile = getNewVfsFile2(cFile);
				assertTrue(cFile.exists());
				assertEquals(cFile.getAsBytes(), FileFunc.file2buf(f));
			}
			{//导入d.txt
				File f = new File(temp + "/a/b/d.txt");
				VfsFile2 dFile = getVfsFile(file, f.getName());
				assertFalse(dFile.exists());

				file.importFile(f);
				assertEquals(file.listFiles(), 2);//已经存在c.txt,又上传了d.txt,所以是2

				dFile = getNewVfsFile2(dFile);
				assertTrue(dFile.exists());
				assertEquals(dFile.getAsBytes(), FileFunc.file2buf(f));
			}
		}
		{//此目录不存在,会自动创建
			VfsFile2 file = getVfsFile("/6/a/b");
			assertFalse(file.exists());

			File f = new File(temp + "/a");
			file.importFile(f);

			assertTrue(file.isDirectory());
		}
		{//当此文件是目录时,导入一个目录
			VfsFile2 file = getVfsFile("/7/a/b");
			file.ensureExists(true);
			assertTrue(file.isDirectory());
			assertEquals(file.listFiles(), 0);

			File f = new File(temp + "/a");
			file.importFile(f);//只会导入子文件,所以会有三个文件导入
			assertEquals(file.listFiles(), "b,c.txt");//只有两个在file目录下,还有一个在/b目录下

			VfsFile2 bFile = getVfsFile(file, "/b");
			assertTrue(bFile.isDirectory());

			VfsFile2 cFile = getVfsFile(file, "/c.txt");
			assertTrue(cFile.isFile());
			assertEquals(cFile.getAsBytes(), TESTCONTENTXML.getBytes(StrFunc.UTF8));

			VfsFile2 dFile = getVfsFile(file, "/b/d.txt");
			assertTrue(dFile.isFile());
			assertEquals(dFile.getAsBytes(), TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));
		}
		{//当此文件是目录时,导入一个文件,同时此目录下存在文件与导入文件的名称相同,会用导入文件的内容覆盖目录下的文件内容
			VfsFile2 dir = getVfsFile("/8/a/b");
			dir.ensureExists(true);
			assertTrue(dir.isDirectory());

			VfsFile2 file = getVfsFile(dir, "c.txt");
			file.ensureExists(false);
			assertTrue(file.isFile());
			assertEquals(file.getAsBytes(), 0);

			File f = new File(temp + "/a/c.txt");
			dir.importFile(f);

			file = getNewVfsFile2(file);
			assertEquals(file.getAsString(), TESTCONTENTXML);
		}
		{//当此文件是目录时,导入一个目录,如果存在同名目录,会删除同名目录,再导入
			VfsFile2 dir = getVfsFile("/9/a/b");
			dir.ensureExists(true);

			VfsFile2 file = getVfsFile(dir, "/b/e.txt");
			file.ensureExists(false);
			assertTrue(file.isFile());

			File f = new File(temp + "/a");
			dir.importFile(f);//会删除/b目录,所以/b/e.txt也会被删除

			file = getNewVfsFile2(file);
			assertFalse(file.exists());
		}
	}

	/**
	 * void importFile(File file, boolean deleteFirst)
	 */
	public void testImportFileBoolean() throws Exception {
		String temp = FileFunc.createTempFile(null, null, true, true, true, true);
		{//生成测试环境
			/**
			 * 生成的结构为:
			 * /a
			 *   /b
			 *     /d.txt
			 *   /c.txt 
			 */
			File aFile = new File(temp + "/a");
			aFile.mkdirs();

			File bFile = new File(temp + "/a/b");
			bFile.mkdirs();

			File cFile = new File(temp + "/a/c.txt");
			cFile.createNewFile();
			FileFunc.writeStrToFile(cFile.getAbsolutePath(), TESTCONTENTXML, false, StrFunc.UTF8);

			File dFile = new File(temp + "/a/b/d.txt");
			dFile.createNewFile();
			FileFunc.writeStrToFile(dFile.getAbsolutePath(), TESTCONTENTXML_GBK, false, StrFunc.GBK);
		}
		{//文件导入一个不存在的文件,直接返回
			VfsFile2 file = getVfsFile("/1/a/b/1.txt");
			file.ensureExists(false);

			File f = new File(temp + "/c");
			assertFalse(f.exists());

			//测试内容为空的情况
			assertEquals(file.getAsBytes(), 0);
			file.importFile(f, true);
			assertEquals(file.getAsBytes(), 0);

			//测试内容不为空的情况
			file.saveAsString(TESTCONTENT, StrFunc.UTF8);
			assertEquals(file.getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));
			file.importFile(f, true);
			assertEquals(file.getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));
		}
		{//目录导入一个不存在的文件,直接返回
			VfsFile2 file = getVfsFile("/2/a/b/c");
			assertFalse(file.exists());
			file.ensureExists(true);
			assertTrue(file.isDirectory());

			assertEquals(file.listFiles(), 0);

			File f = new File(temp + "/c");
			assertFalse(f.exists());

			file.importFile(f, true);
			assertEquals(file.listFiles(), 0);
		}
		{//当此文件是文件时,导入一个文件
			VfsFile2 file = getVfsFile("/3/a/b/1.txt");
			assertFalse(file.exists());
			file.ensureExists(false);
			assertTrue(file.isFile());

			assertEquals(file.getAsBytes(), 0);
			//导入c.txt
			file.importFile(new File(temp + "/a/c.txt"), true);
			assertEquals(file.getAsBytes(), TESTCONTENTXML.getBytes(StrFunc.UTF8));
			//导入d.txt
			file.importFile(new File(temp + "/a/b/d.txt"));
			assertEquals(file.getAsBytes(), TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));
		}
		{//当此文件是文件时,导入一个目录会抛出异常
			VfsFile2 file = getVfsFile("/4/a/b/1.txt");
			file.ensureExists(false);

			try {
				file.importFile(new File(temp + "/a"), true);
				fail("导入目录到文件会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//当此文件是目录时,导入一个文件
			VfsFile2 file = getVfsFile("/5/a/b");
			assertFalse(file.exists());
			file.ensureExists(true);
			assertTrue(file.isDirectory());
			assertEquals(file.listFiles(), 0);

			{//导入c.txt
				File f = new File(temp + "/a/c.txt");
				VfsFile2 cFile = getVfsFile(file, f.getName());
				assertFalse(cFile.exists());

				file.importFile(f, true);
				assertEquals(file.listFiles(), 1);

				cFile = getNewVfsFile2(cFile);
				assertTrue(cFile.exists());
				assertEquals(cFile.getAsBytes(), FileFunc.file2buf(f));
			}
			{//导入d.txt
				File f = new File(temp + "/a/b/d.txt");
				VfsFile2 dFile = getVfsFile(file, f.getName());
				assertFalse(dFile.exists());

				file.importFile(f, true);
				assertEquals(file.listFiles(), 2);//已经存在c.txt,又上传了d.txt,所以是2

				dFile = getNewVfsFile2(dFile);
				assertTrue(dFile.exists());
				assertEquals(dFile.getAsBytes(), FileFunc.file2buf(f));
			}
		}
		{//此目录不存在,会自动创建
			VfsFile2 file = getVfsFile("/6/a/b");
			assertFalse(file.exists());

			File f = new File(temp + "/a");
			file.importFile(f, true);

			assertTrue(file.isDirectory());
		}
		{//当此文件是目录时,导入一个目录
			VfsFile2 file = getVfsFile("/7/a/b");
			file.ensureExists(true);
			assertTrue(file.isDirectory());
			assertEquals(file.listFiles(), 0);

			File f = new File(temp + "/a");
			file.importFile(f, true);//只会导入子文件,所以会有三个文件导入
			assertEquals(file.listFiles(), "b,c.txt");//只有两个在file目录下,还有一个在/b目录下

			VfsFile2 bFile = getVfsFile(file, "/b");
			assertTrue(bFile.isDirectory());

			VfsFile2 cFile = getVfsFile(file, "/c.txt");
			assertTrue(cFile.isFile());
			assertEquals(cFile.getAsBytes(), TESTCONTENTXML.getBytes(StrFunc.UTF8));

			VfsFile2 dFile = getVfsFile(file, "/b/d.txt");
			assertTrue(dFile.isFile());
			assertEquals(dFile.getAsBytes(), TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));
		}
		{//当此文件是目录时,导入一个文件,同时此目录下存在文件与导入文件的名称相同,会用导入文件的内容覆盖目录下的文件内容
			VfsFile2 dir = getVfsFile("/8/a/b");
			dir.ensureExists(true);
			assertTrue(dir.isDirectory());

			VfsFile2 file = getVfsFile(dir, "c.txt");
			file.ensureExists(false);
			assertTrue(file.isFile());
			assertEquals(file.getAsBytes(), 0);

			File f = new File(temp + "/a/c.txt");
			dir.importFile(f, true);

			file = getNewVfsFile2(file);
			assertEquals(file.getAsString(), TESTCONTENTXML);
		}
		{//当此文件是目录时,导入一个目录,如果存在同名目录,会删除同名目录,再导入
			VfsFile2 dir = getVfsFile("/9/a/b");
			dir.ensureExists(true);

			VfsFile2 file = getVfsFile(dir, "/b/e.txt");
			file.ensureExists(false);
			assertTrue(file.isFile());

			File f = new File(temp + "/a");
			dir.importFile(f, true);//会删除/b目录,所以/b/e.txt也会被删除

			file = getNewVfsFile2(file);
			assertFalse(file.exists());
		}
		{//测试deleteFirst=false时的情况
			{//目录同名.当此文件是目录时,导入一个目录,如果存在同名目录,会删除同名目录,再导入,同名目录下的文件会保留
				VfsFile2 dir = getVfsFile("/1a/a/b");
				dir.ensureExists(true);

				VfsFile2 file = getVfsFile(dir, "/b/e.txt");
				file.ensureExists(false);
				assertTrue(file.isFile());

				File f = new File(temp + "/a");
				dir.importFile(f, false);//会删除/b目录,但是/b/e.txt不会被删除

				file = getNewVfsFile2(file);
				assertTrue(file.exists());
			}
			{//文件与目录同名.当此文件是目录时,导入一个目录,如果在导入的目录中存在一个文件与此文件中的一个目录同名,则会删除此文件下目录中所有的内容
				VfsFile2 dir = getVfsFile("/1b/a/b");
				dir.ensureExists(true);

				VfsFile2 file = getVfsFile(dir, "/c.txt");
				VfsFile2 eFile = getVfsFile(file, "e.txt");
				file.ensureExists(true);//目录
				eFile.ensureExists(false);
				assertTrue(file.isDirectory());
				assertTrue(eFile.isFile());

				File f = new File(temp + "/a");
				dir.importFile(f, false);//会删除/c.txt和/c.txt/e.txt

				file = getNewVfsFile2(file);
				assertTrue(file.isFile());
				eFile = getNewVfsFile2(eFile);
				assertFalse(eFile.exists());
			}
			{//目录与文件同名.当此文件是目录时,导入一个目录,如果在导入的目录中存在一个目录与此文件中的一个文件同名,会删除此文件中的文件
				VfsFile2 dir = getVfsFile("/1c/a/b");
				dir.ensureExists(true);

				VfsFile2 file = getVfsFile(dir, "/b");
				VfsFile2 dFile = getVfsFile(file, "d.txt");
				file.ensureExists(false);//
				assertTrue(file.isFile());
				assertFalse(dFile.exists());

				File f = new File(temp + "/a");
				dir.importFile(f, false);//会删除/c.txt和/c.txt/e.txt

				file = getNewVfsFile2(file);
				assertTrue(file.isDirectory());
				dFile = getNewVfsFile2(dFile);
				assertTrue(dFile.isFile());
			}
		}
	}

	public void testImportStmInputStreamString() throws Exception {
		{//测试不合法的文件名,会抛出异常
			VfsFile2 dir = getVfsFile("/1/a/b/");
			//测试传入的文件名为空
			try {
				dir.importStm(null, null);
				fail("文件名为空,会抛出异常");
			}
			catch (Exception e) {
			}
			//测试传入的文件名有非法字段
			try {
				dir.importStm(null, "a/\\:*?\"<>|\r\n\t\b\fb");
				fail("非法的文件名,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//此文件是文件,不是目录,会抛出异常
			VfsFile2 file = getVfsFile("/2/a/b");
			file.ensureExists(false);
			assertTrue(file.isFile());

			try {
				file.importStm(null, "c.txt");
				fail("此文件是文件,目录,导入文件会异常");
			}
			catch (Exception e) {
			}
		}
		{//目录下已经存在名称为name的目录,会抛出异常
			VfsFile2 file = getVfsFile("/3/a/b/test");
			file.ensureExists(true);
			assertTrue(file.isDirectory());

			try {
				file.getParent().importStm(null, file.getName());
				fail("已经存在同名的目录,会抛出异常");
			}
			catch (Exception e) {
			}
		}
		{//目录下已经存在名称为name的文件,会覆盖以前的内容
			{//导入内容为空
				VfsFile2 file = getVfsFile("/4/a/b/");
				VfsFile2 destFile = getVfsFile(file, "test.txt");
				file.ensureExists(true);
				assertTrue(file.isDirectory());

				destFile.ensureExists(false);
				destFile.saveAsString(TESTCONTENT, StrFunc.UTF8);
				assertEquals(destFile.getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));

				file.importStm(null, destFile.getName());

				destFile = getNewVfsFile2(destFile);
				assertEquals(destFile.getAsBytes(), 0);
			}
			{//导入内容不为空
				VfsFile2 file = getVfsFile("/44/a/b/");
				file.ensureExists(true);
				assertTrue(file.isDirectory());

				VfsFile2 destFile = getVfsFile(file, "test.txt");
				destFile.saveAsString(TESTCONTENT, StrFunc.UTF8);
				assertEquals(destFile.getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));

				file.importStm(new ByteArrayInputStream(TESTCONTENTXML.getBytes(StrFunc.UTF8)), destFile.getName());

				destFile = getNewVfsFile2(destFile);
				assertEquals(destFile.getAsBytes(), TESTCONTENTXML.getBytes(StrFunc.UTF8));
			}
		}
		{//此目录不存在,会自动创建
			VfsFile2 file = getVfsFile("/5/a/b/test");
			assertFalse(file.getParent().exists());

			file.getParent().importStm(new ByteArrayInputStream(TESTCONTENT.getBytes(StrFunc.UTF8)), file.getName());

			assertTrue(file.getParent().isDirectory());

			file = getNewVfsFile2(file);
			assertEquals(file.getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));
		}
		{//父目录存在,文件不存在,能够导入内容
			VfsFile2 file = getVfsFile("/6/a/b/c");
			VfsFile2 dir = file.getParent();
			assertFalse(file.exists());
			assertFalse(dir.exists());
			dir.ensureExists(true);
			assertTrue(dir.isDirectory());

			assertFalse(file.exists());

			dir.importStm(null, file.getName());

			file = getNewVfsFile2(file);
			assertTrue(file.isFile());

			file.remove();
			assertFalse(file.exists());
			dir.importStm(new ByteArrayInputStream(TESTCONTENT.getBytes(StrFunc.UTF8)), file.getName());

			file = getNewVfsFile2(file);
			assertTrue(file.isFile());
			assertEquals(file.getAsBytes(), TESTCONTENT.getBytes(StrFunc.UTF8));
		}
	}

	public void testImportZipInputStream() throws Exception {
		String zipfile = null;
		{//创建测试的zip包
			/**
			 * 测试环境的目录结构如下:
			 * /a
			 *   /b
			 *     /d.txt
			 *   /c.txt
			 */
			String temp = FileFunc.createTempFile(null, null, true, true, true, true);
			File aFile = new File(temp + "/a");
			aFile.mkdirs();

			File bFile = new File(temp + "/a/b");
			bFile.mkdirs();

			File cFile = new File(temp + "/a/c.txt");
			cFile.createNewFile();
			FileFunc.writeStrToFile(cFile.getAbsolutePath(), TESTCONTENTXML, false, StrFunc.UTF8);

			File dFile = new File(temp + "/a/b/d.txt");
			dFile.createNewFile();
			FileFunc.writeStrToFile(dFile.getAbsolutePath(), TESTCONTENTXML_GBK, false, StrFunc.GBK);

			zipfile = FuncFile.zipFile(aFile);
		}
		{//如果此文件是文件,会抛出异常
			VfsFile2 file = getVfsFile("/1/a/b");
			file.ensureExists(false);
			assertTrue(file.isFile());

			FileInputStream in = new FileInputStream(zipfile);
			try {
				try {
					file.importZip(in);
					fail("文件导入zip包,会出现异常");
				}
				catch (Exception e) {
				}
			}
			finally {
				in.close();
			}
		}
		{//此文件是目录,能够正常导入
			VfsFile2 file = getVfsFile("/2/a/b");
			file.ensureExists(true);
			assertTrue(file.isDirectory());

			FileInputStream in = new FileInputStream(zipfile);
			try {
				file.importZip(in);
			}
			finally {
				in.close();
			}
			//测试导入的结果是否正确
			VfsFile2 aFile = getVfsFile(file, "a");
			VfsFile2 bFile = getVfsFile(aFile, "b");
			VfsFile2 cFile = getVfsFile(aFile, "c.txt");
			VfsFile2 dFile = getVfsFile(bFile, "d.txt");

			assertTrue(aFile.isDirectory());
			assertTrue(bFile.isDirectory());
			assertTrue(cFile.isFile());
			assertTrue(dFile.isFile());

			assertEquals(file.listFiles(), "a");//a目录
			assertEquals(aFile.listFiles(), "b,c.txt");
			assertEquals(bFile.listFiles(), "d.txt");

			assertEquals(cFile.getAsBytes(), TESTCONTENTXML.getBytes(StrFunc.UTF8));
			assertEquals(dFile.getAsBytes(), TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));

		}
		{//如果此目录不存在,会自动创建
			VfsFile2 file = getVfsFile("/3/a/b");
			assertFalse(file.exists());

			FileInputStream in = new FileInputStream(zipfile);
			try {
				file.importZip(in);
			}
			finally {
				in.close();
			}

			//测试不存在的目录是否已经自动创建
			assertTrue(file.isDirectory());
			//测试导入的结果是否正确
			VfsFile2 aFile = getVfsFile(file, "a");
			VfsFile2 bFile = getVfsFile(aFile, "b");
			VfsFile2 cFile = getVfsFile(aFile, "c.txt");
			VfsFile2 dFile = getVfsFile(bFile, "d.txt");

			assertEquals(file.listFiles(), 1);//a目录
			assertEquals(aFile.listFiles(), 2);
			assertEquals(bFile.listFiles(), 1);

			assertTrue(aFile.isDirectory());
			assertTrue(bFile.isDirectory());
			assertTrue(cFile.isFile());
			assertTrue(dFile.isFile());

			assertEquals(cFile.getAsBytes(), TESTCONTENTXML.getBytes(StrFunc.UTF8));
			assertEquals(dFile.getAsBytes(), TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));
		}
	}

	public void testExportZipPackageOutputStream() throws Exception {
		VfsFile2 dir = getVfsFile("/testexportzip");
		{//构建测试环境
			/**
			 * 测试环境的目录结构如下:
			 * /a
			 *   /b
			 *     /d.txt
			 *   /c.txt
			 */
			VfsFile2 aFile = getVfsFile(dir, "a");
			VfsFile2 bFile = getVfsFile(aFile, "b");
			VfsFile2 cFile = getVfsFile(aFile, "c.txt");
			VfsFile2 dFile = getVfsFile(bFile, "d.txt");

			cFile.saveAsString(TESTCONTENTXML, StrFunc.UTF8);
			dFile.saveAsString(TESTCONTENTXML_GBK, StrFunc.GBK);
		}
		{//导出的文件或目录不存在,会抛出异常
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				try {
					file.exportZipPackage(out);
					fail("导出不存在的文件,会抛出异常");
				}
				catch (Exception e) {
				}
			}
			finally {
				out.close();
			}
		}
		{//导出目录,传入的OutputStream不是ZipOutputStream
			VfsFile2 file = getVfsFile(dir, "a");
			assertTrue(file.isDirectory());

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				file.exportZipPackage(out);
			}
			finally {
				out.close();
			}

			//测试导出的zip文件是否正确
			String temp = FileFunc.createTempFile(null, null, true, true, true, true);
			FileFunc.unzip(new ByteArrayInputStream(out.toByteArray()), temp);
			File tempFile = new File(temp);

			File aFile = new File(temp + "/a");
			File bFile = new File(temp + "/a/b");
			File cFile = new File(temp + "/a/c.txt");
			File dFile = new File(temp + "/a/b/d.txt");

			assertEquals(tempFile.list(), 1);
			assertEquals(aFile.list(), 2);
			assertEquals(bFile.list(), 1);

			assertTrue(aFile.isDirectory());
			assertTrue(bFile.isDirectory());
			assertTrue(cFile.isFile());
			assertTrue(dFile.isFile());

			assertEquals(FileFunc.file2buf(cFile), TESTCONTENTXML.getBytes(StrFunc.UTF8));
			assertEquals(FileFunc.file2buf(dFile), TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));
		}
		{//导出文件,传入的OutputStream不是ZipOutputStream
			{//导出c.txt
				VfsFile2 file = getVfsFile(dir, "/a/c.txt");
				assertTrue(file.isFile());

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				try {
					file.exportZipPackage(out);
				}
				finally {
					out.close();
				}

				//测试导出的zip文件是否正确
				String temp = FileFunc.createTempFile(null, null, true, true, true, true);
				FileFunc.unzip(new ByteArrayInputStream(out.toByteArray()), temp);
				File tempFile = new File(temp);

				File cFile = new File(temp + "/c.txt");

				assertEquals(tempFile.list(), 1);
				assertTrue(cFile.isFile());
				assertEquals(FileFunc.file2buf(cFile), TESTCONTENTXML.getBytes(StrFunc.UTF8));
			}
			{//导出d.txt
				VfsFile2 file = getVfsFile(dir, "/a/b/d.txt");
				assertTrue(file.isFile());

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				try {
					file.exportZipPackage(out);
				}
				finally {
					out.close();
				}

				//测试导出的zip文件是否正确
				String temp = FileFunc.createTempFile(null, null, true, true, true, true);
				FileFunc.unzip(new ByteArrayInputStream(out.toByteArray()), temp);
				File tempFile = new File(temp);

				File dFile = new File(temp + "/d.txt");

				assertEquals(tempFile.list(), 1);
				assertTrue(dFile.isFile());
				assertEquals(FileFunc.file2buf(dFile), TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));
			}

		}
		{//导出目录,传入的OutputStream是ZipOutputStream
			VfsFile2 file = getVfsFile(dir, "a");
			assertTrue(file.isDirectory());

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				file.exportZipPackage(out);
			}
			finally {
				out.close();
			}

			//测试导出的zip文件是否正确
			String temp = FileFunc.createTempFile(null, null, true, true, true, true);
			FileFunc.unzip(new ByteArrayInputStream(out.toByteArray()), temp);
			File tempFile = new File(temp);

			File aFile = new File(temp + "/a");
			File bFile = new File(temp + "/a/b");
			File cFile = new File(temp + "/a/c.txt");
			File dFile = new File(temp + "/a/b/d.txt");

			assertEquals(tempFile.list(), 1);
			assertEquals(aFile.list(), 2);
			assertEquals(bFile.list(), 1);

			assertTrue(aFile.isDirectory());
			assertTrue(bFile.isDirectory());
			assertTrue(cFile.isFile());
			assertTrue(dFile.isFile());

			assertEquals(FileFunc.file2buf(cFile), TESTCONTENTXML.getBytes(StrFunc.UTF8));
			assertEquals(FileFunc.file2buf(dFile), TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));
		}
		{//导出文件,传入的OutputStream是ZipOutputStream
			{//导出c.txt
				VfsFile2 file = getVfsFile(dir, "/a/c.txt");
				assertTrue(file.isFile());

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				try {
					file.exportZipPackage(out);
				}
				finally {
					out.close();
				}

				//测试导出的zip文件是否正确
				String temp = FileFunc.createTempFile(null, null, true, true, true, true);
				FileFunc.unzip(new ByteArrayInputStream(out.toByteArray()), temp);
				File tempFile = new File(temp);

				File cFile = new File(temp + "/c.txt");

				assertEquals(tempFile.list(), 1);
				assertTrue(cFile.isFile());
				assertEquals(FileFunc.file2buf(cFile), TESTCONTENTXML.getBytes(StrFunc.UTF8));
			}
			{//导出d.txt
				VfsFile2 file = getVfsFile(dir, "/a/b/d.txt");
				assertTrue(file.isFile());

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				try {
					file.exportZipPackage(out);
				}
				finally {
					out.close();
				}

				//测试导出的zip文件是否正确
				String temp = FileFunc.createTempFile(null, null, true, true, true, true);
				FileFunc.unzip(new ByteArrayInputStream(out.toByteArray()), temp);
				File tempFile = new File(temp);

				File dFile = new File(temp + "/d.txt");

				assertEquals(tempFile.list(), 1);
				assertTrue(dFile.isFile());
				assertEquals(FileFunc.file2buf(dFile), TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));
			}
		}
	}

	/**
	 * 测试void writeContentTo(OutputStream out)
	 * @throws Exception 
	 */
	public void testWriteContentTo() throws Exception {
		{//文件不存在,不会输出任何内容
			VfsFile2 file = getVfsFile("/1/a/b");
			assertFalse(file.exists());

			//文件不存在时,如果out为空,调用该方法不会抛出异常
			file.writeContentTo(null, false);
			//文件不存在时,传入的out不会空,获得的结果为空
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			file.writeContentTo(out, false);
			assertEquals(out.toByteArray(), 0);
		}
		{//文件是目录,不会输出任何内容
			VfsFile2 file = getVfsFile("/2/a/b");
			file.ensureExists(true);
			assertTrue(file.isDirectory());

			//文件是目录时,如果out为空,调用该方法不会抛出异常
			file.writeContentTo(null, false);
			//文件是目录时,传入的out不会空,获得的结果为空
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			file.writeContentTo(out, false);
			assertEquals(out.toByteArray(), 0);
		}
		{//文件是文件,会输出内容到out中
			{//out为空会出现异常
				VfsFile2 file = getVfsFile("/3/a/b");
				file.ensureExists(false);
				file.saveAsString(TESTCONTENTXML, StrFunc.UTF8);

				//文件是文件时,如果out为空,调用该方法会抛出异常
				try {
					file.writeContentTo(null, false);
					fail("out为空,应该抛出异常");
				}
				catch (Exception e) {
				}
				try {
					file.writeContentTo(null, true);
					fail("out为空,应该抛出异常");
				}
				catch (Exception e) {
				}
			}
			{//正常输出内容到out中
				{//在输出前没有调用过获得内容的方法
					VfsFile2 file = getVfsFile("/4/a/b");
					file.ensureExists(false);
					file.saveAsString(TESTCONTENTXML, StrFunc.UTF8);

					{//输出没有压缩的内容
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						file.writeContentTo(out, false);
						assertEquals(out.toByteArray(), TESTCONTENTXML.getBytes(StrFunc.UTF8));
					}
					{//输出压缩后的内容
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						file.writeContentTo(out, true);
						assertEquals(out.toByteArray(), StmFunc.gzipBytes(TESTCONTENTXML.getBytes(StrFunc.UTF8)));
					}
				}
				{//在输出前调用过获得内容的方法
					VfsFile2 file = getVfsFile("/5/a/b");
					file.ensureExists(false);
					file.saveAsString(TESTCONTENTXML_GBK, StrFunc.GBK);
					assertEquals(file.getAsBytes(), TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));

					{//输出没有压缩的内容
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						file.writeContentTo(out, false);
						assertEquals(out.toByteArray(), TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));
					}
					{//输出压缩后的内容
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						file.writeContentTo(out, true);
						assertEquals(out.toByteArray(), StmFunc.gzipBytes(TESTCONTENTXML_GBK.getBytes(StrFunc.GBK)));
					}
				}
			}
		}
	}

	/**
	 * 测试VfsListener对文件和监控
	 * @throws Exception 
	 * @throws  
	 */
	public void testListener() throws Exception {
		/*
		{//监控文件和目录的创建
			{//文件的创建
				VfsFile2 file = getVfsFile("/1/a/b");
				assertFalse(file.exists());

				VfsListenerImpl listener = createListener();
				getVfs().addListener(file.getAbsolutePath(), listener);
				file.ensureExists(false);
				getVfs().removeListener(listener);

				assertEquals(file, listener.getFile());
				assertEquals(VfsListener2.VFSFILE_NOTIFY_NEW, listener.getChgFlag());

				assertTrue(file.isFile());
				assertTrue(listener.getFile().isFile());
			}
			{//目录的创建
				VfsFile2 file = getVfsFile("/2/a/b");
				assertFalse(file.exists());

				VfsListenerImpl listener = createListener();
				getVfs().addListener(file.getAbsolutePath(), listener);
				file.ensureExists(true);
				getVfs().removeListener(listener);

				assertEquals(file, listener.getFile());
				assertEquals(VfsListener2.VFSFILE_NOTIFY_NEW, listener.getChgFlag());

				assertTrue(file.isDirectory());
				assertTrue(listener.getFile().isDirectory());
			}
		}
		{//监控复制文件
			VfsFile2 file = getVfsFile("/3/a/b");
			file.ensureExists(true);
			VfsFile2 cFile = getVfsFile(file, "c.txt");
			cFile.ensureExists(false);
			cFile.saveAsString(TESTCONTENTXML, StrFunc.UTF8);

			VfsFile2 destDir = getVfsFile("/33/a/b");

			VfsFile2 destFile = getVfsFile(destDir, file.getName());
			VfsFile2 destCFile = getVfsFile(destFile, cFile.getName());
			assertFalse(destFile.exists());
			assertFalse(destCFile.exists());

			//加入监听器
			VfsListenerImpl listener1 = new VfsListenerImpl();
			VfsListenerImpl listener2 = new VfsListenerImpl();
			getVfs().addListener(destFile.getAbsolutePath(), listener1);
			getVfs().addListener(destCFile.getAbsolutePath(), listener2);

			//复制并删除监听器
			file.copyTo(destDir, null, true);
			getVfs().removeListener(listener1);
			getVfs().removeListener(listener2);

			//验证
			destFile = getNewVfsFile2(destFile);
			assertEquals(destFile, listener1.getFile());
			assertEquals(VfsListener2.VFSFILE_NOTIFY_NEW, listener1.getChgFlag());
			assertTrue(listener1.getFile().isDirectory());

			destCFile = getNewVfsFile2(destCFile);
			assertEquals(destFile, listener1.getFile());
			assertEquals(VfsListener2.VFSFILE_NOTIFY_NEW, listener2.getChgFlag());
			assertTrue(listener2.getFile().isFile());
			assertEquals(listener2.getFile().getAsBytes(), TESTCONTENTXML.getBytes(StrFunc.UTF8));
		}
		{//监控导入文件
			//创建导入文件
			String temp = FileFunc.createTempFile(null, null, true, true, true, true);
			File dir = new File(temp);
			File aFile = new File(dir.getAbsolutePath() + FileFunc.separator + "a");
			assertTrue(aFile.mkdir());
			File bFile = new File(aFile.getAbsolutePath() + FileFunc.separator + "b.txt");
			assertTrue(bFile.createNewFile());
			FileFunc.str2file(bFile.getAbsolutePath(), TESTCONTENTXML, StrFunc.UTF8);
			File cFile = new File(dir.getAbsolutePath() + FileFunc.separator + "c.txt");
			assertTrue(cFile.createNewFile());
			FileFunc.str2file(cFile.getAbsolutePath(), TESTCONTENTXML_GBK, StrFunc.GBK);

			VfsFile2 file = getVfsFile("/4/a/b");
			file.ensureExists(true);
			VfsFile2 aVFile = getVfsFile(file, aFile.getName());
			VfsFile2 bVFile = getVfsFile(aVFile, bFile.getName());
			VfsFile2 cVFile = getVfsFile(file, cFile.getName());
			cVFile.ensureExists(false);
			cVFile.saveAsString(TESTCONTENT, StrFunc.UTF8);

			//开始测试
			VfsListenerImpl listener1 = createListener();
			VfsListenerImpl listener2 = createListener();
			VfsListenerImpl listener3 = createListener();
			getVfs().addListener(aVFile.getAbsolutePath(), listener1);
			getVfs().addListener(bVFile.getAbsolutePath(), listener2);
			getVfs().addListener(cVFile.getAbsolutePath(), listener3);
			file.importFile(dir);
			getVfs().removeListener(listener1);
			getVfs().removeListener(listener2);
			getVfs().removeListener(listener3);

			//验证
			aVFile = getNewVfsFile2(aVFile);
			assertEquals(aVFile, listener1.getFile());
			assertEquals(VfsListener2.VFSFILE_NOTIFY_NEW, listener1.getChgFlag());
			assertTrue(listener1.getFile().isDirectory());

			bVFile = getNewVfsFile2(bVFile);
			assertEquals(bVFile, listener2.getFile());
			assertEquals(VfsListener2.VFSFILE_NOTIFY_NEW, listener2.getChgFlag());
			assertTrue(listener2.getFile().isFile());
			assertEquals(listener2.getFile().getAsBytes(), TESTCONTENTXML.getBytes(StrFunc.UTF8));

			cVFile = getNewVfsFile2(cVFile);
			assertEquals(cVFile, listener3.getFile());
			assertEquals(VfsListener2.VFSFILE_NOTIFY_WRITE, listener3.getChgFlag());
			assertTrue(listener3.getFile().isFile());
			assertEquals(listener3.getFile().getAsBytes(), TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));
		}
		{//监控剪切
			VfsFile2 file = getVfsFile("/5/a/b");
			file.ensureExists(true);
			VfsFile2 cFile = getVfsFile(file, "c.txt");
			cFile.ensureExists(false);
			cFile.saveAsString(TESTCONTENTXML, StrFunc.UTF8);

			VfsFile2 destDir = getVfsFile("/55/a/b");

			VfsFile2 destFile = getVfsFile(destDir, file.getName());
			VfsFile2 destCFile = getVfsFile(destFile, cFile.getName());
			assertFalse(destFile.exists());
			assertFalse(destCFile.exists());

			//加入监听器
			VfsListenerImpl listener1 = new VfsListenerImpl();
			VfsListenerImpl listener2 = new VfsListenerImpl();
			VfsListenerImpl listener3 = new VfsListenerImpl();
			VfsListenerImpl listener4 = new VfsListenerImpl();
			getVfs().addListener(destFile.getAbsolutePath(), listener1);
			getVfs().addListener(destCFile.getAbsolutePath(), listener2);
			getVfs().addListener(file.getAbsolutePath(), listener3);
			getVfs().addListener(cFile.getAbsolutePath(), listener4);

			//复制并删除监听器
			file.moveTo(destDir);
			getVfs().removeListener(listener1);
			getVfs().removeListener(listener2);
			getVfs().removeListener(listener3);
			getVfs().removeListener(listener4);

			//验证
			destFile = getNewVfsFile2(destFile);
			assertEquals(destFile, listener1.getFile());
			assertEquals(VfsListener2.VFSFILE_NOTIFY_NEW, listener1.getChgFlag());
			assertTrue(listener1.getFile().isDirectory());

			destCFile = getNewVfsFile2(destCFile);
			assertEquals(destFile, listener1.getFile());
			assertEquals(VfsListener2.VFSFILE_NOTIFY_NEW, listener2.getChgFlag());
			assertTrue(listener2.getFile().isFile());
			assertEquals(listener2.getFile().getAsBytes(), TESTCONTENTXML.getBytes(StrFunc.UTF8));

			file = getNewVfsFile2(file);
			assertEquals(file, listener3.getFile());
			assertEquals(VfsListener2.VFSFILE_NOTIFY_REMOVE, listener3.getChgFlag());
			assertFalse(listener3.getFile().exists());

			cFile = getNewVfsFile2(cFile);
			assertEquals(cFile, listener4.getFile());
			assertEquals(VfsListener2.VFSFILE_NOTIFY_REMOVE, listener4.getChgFlag());
			assertFalse(listener4.getFile().exists());
		}
		{//监控删除
			VfsFile2 file = getVfsFile("/6/a/b");
			file.ensureExists(true);
			VfsFile2 cFile = getVfsFile(file, "c.txt");
			cFile.ensureExists(false);
			cFile.saveAsString(TESTCONTENTXML, StrFunc.UTF8);

			//加入监听器
			VfsListenerImpl listener1 = new VfsListenerImpl();
			VfsListenerImpl listener2 = new VfsListenerImpl();

			getVfs().addListener(file.getAbsolutePath(), listener1);
			getVfs().addListener(cFile.getAbsolutePath(), listener2);
			file.remove();
			getVfs().removeListener(listener1);
			getVfs().removeListener(listener2);

			//验证
			assertEquals(file, listener1.getFile());
			assertEquals(VfsListener2.VFSFILE_NOTIFY_REMOVE, listener1.getChgFlag());
			assertFalse(listener1.getFile().exists());

			cFile = getNewVfsFile2(cFile);
			assertEquals(cFile, listener2.getFile());
			assertEquals(VfsListener2.VFSFILE_NOTIFY_REMOVE, listener2.getChgFlag());
			assertFalse(listener2.getFile().exists());

		}
		{//监控重命名
			VfsFile2 file = getVfsFile("/7/a/b");
			file.ensureExists(true);
			VfsFile2 cFile = getVfsFile(file, "c.txt");
			cFile.ensureExists(false);
			cFile.saveAsString(TESTCONTENTXML, StrFunc.UTF8);

			VfsFile2 destFile = getVfsFile(file.getParent(), "bb");
			VfsFile2 destCFile = getVfsFile(destFile, cFile.getName());
			assertFalse(destFile.exists());
			assertFalse(destCFile.exists());

			//加入监听器
			VfsListenerImpl listener1 = new VfsListenerImpl();
			VfsListenerImpl listener2 = new VfsListenerImpl();
			VfsListenerImpl listener3 = new VfsListenerImpl();
			VfsListenerImpl listener4 = new VfsListenerImpl();
			getVfs().addListener(destFile.getAbsolutePath(), listener1);
			getVfs().addListener(destCFile.getAbsolutePath(), listener2);
			getVfs().addListener(file.getAbsolutePath(), listener3);
			getVfs().addListener(cFile.getAbsolutePath(), listener4);

			//复制并删除监听器
			file.renameTo("bb");
			getVfs().removeListener(listener1);
			getVfs().removeListener(listener2);
			getVfs().removeListener(listener3);
			getVfs().removeListener(listener4);

			//验证
			destFile = getNewVfsFile2(destFile);
			assertEquals(destFile, listener1.getFile());
			assertEquals(VfsListener2.VFSFILE_NOTIFY_NEW, listener1.getChgFlag());
			assertTrue(listener1.getFile().isDirectory());

			destCFile = getNewVfsFile2(destCFile);
			assertEquals(destFile, listener1.getFile());
			assertEquals(VfsListener2.VFSFILE_NOTIFY_NEW, listener2.getChgFlag());
			assertTrue(listener2.getFile().isFile());
			assertEquals(listener2.getFile().getAsBytes(), TESTCONTENTXML.getBytes(StrFunc.UTF8));

			file = getNewVfsFile2(file);
			assertEquals(file, listener3.getFile());
			assertEquals(VfsListener2.VFSFILE_NOTIFY_REMOVE, listener3.getChgFlag());
			assertFalse(listener3.getFile().exists());

			cFile = getNewVfsFile2(cFile);
			assertEquals(cFile, listener4.getFile());
			assertEquals(VfsListener2.VFSFILE_NOTIFY_REMOVE, listener4.getChgFlag());
			assertFalse(listener4.getFile().exists());
		}
		{//监控修改
			{//修改内容
				{//文件内容修改
					VfsFile2 file = getVfsFile("/8/a/b");
					file.ensureExists(false);
					file.saveAsString(TESTCONTENTXML, StrFunc.UTF8);

					VfsListenerImpl listener = createListener();
					getVfs().addListener(file.getAbsolutePath(), listener);
					file.saveAsString(TESTCONTENTXML_GBK, StrFunc.GBK);
					getVfs().removeListener(listener);

					assertEquals(file, listener.getFile());
					assertEquals(VfsListener2.VFSFILE_NOTIFY_WRITE, listener.getChgFlag());
					assertEquals(listener.getFile().getAsBytes(), TESTCONTENTXML_GBK.getBytes(StrFunc.GBK));
				}
				{//目录内容修改(子文件顺序)
					VfsFile2 file = getVfsFile("/9/a/b");
					file.ensureExists(true);

					VfsListenerImpl listener = createListener();
					getVfs().addListener(file.getAbsolutePath(), listener);
					ArrayList list = new ArrayList();
					list.add("a");
					list.add("b");
					file.setChildrenOrder(list);
					getVfs().removeListener(listener);

					assertEquals(file, listener.getFile());
					assertEquals(VfsListener2.VFSFILE_NOTIFY_WRITE, listener.getChgFlag());
				}
			}
			{//charset
				VfsFile2 file = getVfsFile("/1a/a/b");
				file.ensureExists(false);

				VfsListenerImpl listener = createListener();
				getVfs().addListener(file.getAbsolutePath(), listener);
				file.setCharset(StrFunc.UTF8);
				getVfs().removeListener(listener);

				assertEquals(file, listener.getFile());
				assertEquals(VfsListener2.VFSFILE_NOTIFY_WRITE, listener.getChgFlag());
				assertEquals(listener.getFile().getCharset(), StrFunc.UTF8);
			}
			{//最后修改时间
				VfsFile2 file = getVfsFile("/1b/a/b");
				file.ensureExists(false);

				VfsListenerImpl listener = createListener();
				getVfs().addListener(file.getAbsolutePath(), listener);
				file.setLastModified(System.currentTimeMillis());
				getVfs().removeListener(listener);

				assertEquals(file, listener.getFile());
				assertEquals(VfsListener2.VFSFILE_NOTIFY_WRITE, listener.getChgFlag());
				assertEquals(listener.getFile().getLastModified(), file.getLastModified());
			}
			{//mimetype
				VfsFile2 file = getVfsFile("/1c/a/b");
				file.ensureExists(false);

				VfsListenerImpl listener = createListener();
				getVfs().addListener(file.getAbsolutePath(), listener);
				file.setMimeType("text/plain");
				getVfs().removeListener(listener);

				assertEquals(file, listener.getFile());
				assertEquals(VfsListener2.VFSFILE_NOTIFY_WRITE, listener.getChgFlag());
				assertEquals(listener.getFile().getMimeType(), "text/plain");
			}
			{//owner
				VfsFile2 file = getVfsFile("/1c/a/b");
				file.ensureExists(false);

				VfsListenerImpl listener = createListener();
				getVfs().addListener(file.getAbsolutePath(), listener);
				file.setOwner("abc");
				getVfs().removeListener(listener);

				assertEquals(file, listener.getFile());
				assertEquals(VfsListener2.VFSFILE_NOTIFY_WRITE, listener.getChgFlag());
				assertEquals(listener.getFile().getOwner(), "abc");
			}
			{//属性
				VfsFile2 file = getVfsFile("/1c/a/b");
				file.ensureExists(false);

				VfsListenerImpl listener = createListener();
				getVfs().addListener(file.getAbsolutePath(), listener);
				HashMap props = new HashMap();
				props.put("charset", StrFunc.GBK);
				props.put("mimeType", "text/plain");
				file.setProperty(props);
				getVfs().removeListener(listener);

				assertEquals(file, listener.getFile());
				assertEquals(VfsListener2.VFSFILE_NOTIFY_WRITE, listener.getChgFlag());
				assertEquals(listener.getFile().getCharset(), StrFunc.GBK);
				assertEquals(listener.getFile().getMimeType(), "text/plain");
			}
		}
		*/
	}

	/**
	 * 测试缓存
	 * 
	 * 以前加入缓存的方式如下:
	 * VfsNode oldNode = (VfsNode) nodesmap.get(key);
		if (oldNode == null) {
			nodesmap.put(key, node);
		}
		else {
			oldNode.delete();
			if (oldNode.getLastModifyTime().getTime() != node.getLastModifyTime().getTime()) {
				//对象已经改变
				nodesmap.put(key, node);
			}
			else if (node.getContainContent()) {
				nodesmap.put(key, node);
			}
		}
	 * 
	 * 存在以下问题(此问题存在的前提是ibatis的缓存没有关闭):
	 * String fn = ...
	 * VfsFile2 file = getVfsFile(fn);//把内容也读取出来了
	 * VfsFile[] fs = file.getParent().listFile();//再次读取了fn,但是是没有内容的.会调用oldNode.delete()将缓存文件删除了,但是缓存的对象仍然是oldNode
	 * byte[] bs = getVfsFile(fn).getAsBytes();//因为 oldNode的缓存文件被删除了,获得文件内容时会再次从ibatis中获取,ibatis返回的仍然是oldNode,所以获得的内容为空
	 * @throws Exception 
	 */
	public void testCache() throws Exception {
		VfsFile2 file = getVfsFile("/1/a/b/c.txt");
		file.saveAsString(TESTCONTENTXML, StrFunc.UTF8);
		file.getParent().listFiles();
		byte[] bs = getNewVfsFile2(file).getAsBytes();
		assertEquals(TESTCONTENTXML.getBytes(StrFunc.UTF8), bs);
	}
}
