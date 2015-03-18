package com.esen.jdbc.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.DataCopy;
import com.esen.jdbc.DataCopyForUpdate;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.util.ProgressDefault;

import junit.framework.TestCase;

public class DataCopyForUpdateTest extends TestCase {

	private SimpleConnectionFactory getOraclePool() {
		return new SimpleConnectionFactory("oracle.jdbc.driver.OracleDriver",
				"jdbc:oracle:thin:@192.168.1.100:1521:esenbi", "test", "test", "debug");
	}

	public void testOracleUpdateForKeys() throws Exception {
		SimpleConnectionFactory conf = getOraclePool();
		try {
			testUpdateForKeys(conf, conf);
		}
		finally {
			conf.close();
		}
	}

	public void testOracleAppendForKeys() throws Exception {
		SimpleConnectionFactory conf = getOraclePool();
		try {
			testAppendForKeys(conf, conf);
		}
		finally {
			conf.close();
		}
	}

	public void testOracleInsertIntoNoKeys() throws Exception {
		SimpleConnectionFactory conf = getOraclePool();
		try {
			testInsertIntoNoKeys(conf, conf);
		}
		finally {
			conf.close();
		}
	}

	/**
	 * 测试没有主键的时候，直接写入目的表数据；
	 * 目的表不存在；
	 * @param srcpool
	 * @param targetpool
	* @throws Exception 
	 */
	private void testInsertIntoNoKeys(SimpleConnectionFactory srcpool, SimpleConnectionFactory targetpool)
			throws Exception {
		initSrcData(srcpool);
		try {
			Connection scon = srcpool.getConnection();
			try {
				Connection tcon = targetpool.getConnection();
				try {
					DataCopy.createInstance().selectInto(scon, "t_test_src", tcon, "t_test_target", DataCopy.OPT_APPEND);
				}
				finally {
					tcon.close();
				}
			}
			finally {
				scon.close();
			}
			checkInsertData(targetpool);

		}
		finally {
			dropTable(srcpool, "t_test_src");
			dropTable(targetpool, "t_test_target");
		}
	}

	private void checkInsertData(SimpleConnectionFactory targetpool) throws SQLException {
		Connection conn = targetpool.getConnection();
		try {
			Statement stat = conn.createStatement();
			try {
				ResultSet rs = stat.executeQuery("select bbq_,userid_,hy_dm,zb1,zb2,zb3,zb4,zb5,zb6 from t_test_target order by bbq_,userid_");
				assertEquals(true, rs.next());
				assertEquals("200901--", rs.getString(1));
				assertEquals("A001", rs.getString(2));
				assertEquals("A0000", rs.getString(3));
				assertEquals(11, rs.getDouble(4), 0);
				assertEquals(12, rs.getDouble(5), 0);
				assertEquals(13, rs.getDouble(6), 0);
				assertEquals(14, rs.getDouble(7), 0);
				assertEquals(15, rs.getDouble(8), 0);
				assertEquals(16, rs.getDouble(9), 0);

				assertEquals(true, rs.next());
				assertEquals("200901--", rs.getString(1));
				assertEquals("B001", rs.getString(2));
				assertEquals("B0000", rs.getString(3));
				assertEquals(31, rs.getDouble(4), 0);
				assertEquals(32, rs.getDouble(5), 0);
				assertEquals(33, rs.getDouble(6), 0);
				assertEquals(34, rs.getDouble(7), 0);
				assertEquals(35, rs.getDouble(8), 0);
				assertEquals(36, rs.getDouble(9), 0);

				assertEquals(true, rs.next());
				assertEquals("200901--", rs.getString(1));
				assertEquals("C001", rs.getString(2));
				assertEquals("C0000", rs.getString(3));
				assertEquals(61, rs.getDouble(4), 0);
				assertEquals(62, rs.getDouble(5), 0);
				assertEquals(63, rs.getDouble(6), 0);
				assertEquals(64, rs.getDouble(7), 0);
				assertEquals(65, rs.getDouble(8), 0);
				assertEquals(66, rs.getDouble(9), 0);

				assertEquals(true, rs.next());
				assertEquals("200902--", rs.getString(1));
				assertEquals("A001", rs.getString(2));
				assertEquals("A0000", rs.getString(3));
				assertEquals(21, rs.getDouble(4), 0);
				assertEquals(22, rs.getDouble(5), 0);
				assertEquals(23, rs.getDouble(6), 0);
				assertEquals(24, rs.getDouble(7), 0);
				assertEquals(25, rs.getDouble(8), 0);
				assertEquals(26, rs.getDouble(9), 0);

				assertEquals(true, rs.next());
				assertEquals("200902--", rs.getString(1));
				assertEquals("B001", rs.getString(2));
				assertEquals("B0000", rs.getString(3));
				assertEquals(41, rs.getDouble(4), 0);
				assertEquals(42, rs.getDouble(5), 0);
				assertEquals(43, rs.getDouble(6), 0);
				assertEquals(44, rs.getDouble(7), 0);
				assertEquals(45, rs.getDouble(8), 0);
				assertEquals(46, rs.getDouble(9), 0);

				assertEquals(true, rs.next());
				assertEquals("200902--", rs.getString(1));
				assertEquals("C001", rs.getString(2));
				assertEquals("C0000", rs.getString(3));
				assertEquals(71, rs.getDouble(4), 0);
				assertEquals(72, rs.getDouble(5), 0);
				assertEquals(73, rs.getDouble(6), 0);
				assertEquals(74, rs.getDouble(7), 0);
				assertEquals(75, rs.getDouble(8), 0);
				assertEquals(76, rs.getDouble(9), 0);

				assertEquals(true, rs.next());
				assertEquals("200903--", rs.getString(1));
				assertEquals("B001", rs.getString(2));
				assertEquals("B0000", rs.getString(3));
				assertEquals(51, rs.getDouble(4), 0);
				assertEquals(52, rs.getDouble(5), 0);
				assertEquals(53, rs.getDouble(6), 0);
				assertEquals(54, rs.getDouble(7), 0);
				assertEquals(55, rs.getDouble(8), 0);
				assertEquals(56, rs.getDouble(9), 0);

				assertEquals(false, rs.next());
			}
			finally {
				stat.close();
			}
		}
		finally {
			conn.close();
		}

	}

	/**
	 * 测试直接添加数据，目的表有主键；
	 * 不更新已有数据；
	 * @param srcpool
	 * @param targetpool
	 * @throws Exception
	 */
	private void testAppendForKeys(SimpleConnectionFactory srcpool, SimpleConnectionFactory targetpool)
			throws Exception {
		initSrcData(srcpool);
		initTargetData(targetpool);
		try {
			DataCopyForUpdate dcu = DataCopyForUpdate.createInstance();
			dcu.setIprogress(new ProgressDefault());
			dcu.setSourceDataPool(srcpool);
			dcu.setTargetDataPool(targetpool);
			dcu.setTargetTable("t_test_target");
			dcu.isOnlyInsertNewRecord(true);

			dcu.setPrimaryKeys(new String[] { "bbq_", "userid_" });
			dcu.addSourceSql("select bbq_,userid_,hy_dm,zb1,zb2,zb3 from t_test_src where bbq_='200901--'");
			dcu.addSourceSql("select bbq_,userid_,hy_dm,zb4,zb5,zb6 from t_test_src where bbq_='200901--'");
			//测试多次提交；
			dcu.executeUpdate();
			dcu.addSourceSql("select bbq_,userid_,hy_dm,zb1,zb2,zb3,zb4,zb5,zb6 from t_test_src where bbq_='200902--'");
			//测试多次提交；
			dcu.executeUpdate();

			checkAppendData(targetpool);
		}
		finally {
			dropTable(srcpool, "t_test_src");
			dropTable(targetpool, "t_test_target");
		}
	}

	private void checkAppendData(SimpleConnectionFactory targetpool) throws SQLException {
		Connection conn = targetpool.getConnection();
		try {
			Statement stat = conn.createStatement();
			try {
				ResultSet rs = stat.executeQuery("select bbq_,userid_,hy_dm,zb1,zb2,zb3,zb4,zb5,zb6,zb7 from t_test_target order by bbq_,userid_");
				assertEquals(true, rs.next());
				assertEquals("200901--", rs.getString(1));
				assertEquals("A001", rs.getString(2));
				assertEquals("A0000", rs.getString(3));
				assertEquals(1, rs.getDouble(4), 0);
				assertEquals(2, rs.getDouble(5), 0);
				assertEquals(3, rs.getDouble(6), 0);
				assertEquals(4, rs.getDouble(7), 0);
				assertEquals(5, rs.getDouble(8), 0);
				assertEquals(6, rs.getDouble(9), 0);
				assertEquals(7, rs.getDouble(10), 0);

				assertEquals(true, rs.next());
				assertEquals("200901--", rs.getString(1));
				assertEquals("B001", rs.getString(2));
				assertEquals("B0000", rs.getString(3));
				assertEquals(31, rs.getDouble(4), 0);
				assertEquals(32, rs.getDouble(5), 0);
				assertEquals(33, rs.getDouble(6), 0);
				assertEquals(34, rs.getDouble(7), 0);
				assertEquals(35, rs.getDouble(8), 0);
				assertEquals(0, rs.getDouble(9), 0);
				assertEquals(0, rs.getDouble(10), 0);

				assertEquals(true, rs.next());
				assertEquals("200901--", rs.getString(1));
				assertEquals("C001", rs.getString(2));
				assertEquals("C0000", rs.getString(3));
				assertEquals(61, rs.getDouble(4), 0);
				assertEquals(62, rs.getDouble(5), 0);
				assertEquals(63, rs.getDouble(6), 0);
				assertEquals(64, rs.getDouble(7), 0);
				assertEquals(65, rs.getDouble(8), 0);
				assertEquals(0, rs.getDouble(9), 0);
				assertEquals(0, rs.getDouble(10), 0);

				assertEquals(true, rs.next());
				assertEquals("200902--", rs.getString(1));
				assertEquals("A001", rs.getString(2));
				assertEquals("A0000", rs.getString(3));
				assertEquals(21, rs.getDouble(4), 0);
				assertEquals(22, rs.getDouble(5), 0);
				assertEquals(23, rs.getDouble(6), 0);
				assertEquals(24, rs.getDouble(7), 0);
				assertEquals(25, rs.getDouble(8), 0);
				assertEquals(0, rs.getDouble(9), 0);
				assertEquals(0, rs.getDouble(10), 0);

				assertEquals(true, rs.next());
				assertEquals("200902--", rs.getString(1));
				assertEquals("B001", rs.getString(2));
				assertEquals("B0000", rs.getString(3));
				assertEquals(21, rs.getDouble(4), 0);
				assertEquals(22, rs.getDouble(5), 0);
				assertEquals(23, rs.getDouble(6), 0);
				assertEquals(24, rs.getDouble(7), 0);
				assertEquals(25, rs.getDouble(8), 0);
				assertEquals(26, rs.getDouble(9), 0);
				assertEquals(27, rs.getDouble(10), 0);

				assertEquals(true, rs.next());
				assertEquals("200902--", rs.getString(1));
				assertEquals("C001", rs.getString(2));
				assertEquals("C0000", rs.getString(3));
				assertEquals(71, rs.getDouble(4), 0);
				assertEquals(72, rs.getDouble(5), 0);
				assertEquals(73, rs.getDouble(6), 0);
				assertEquals(74, rs.getDouble(7), 0);
				assertEquals(75, rs.getDouble(8), 0);
				assertEquals(0, rs.getDouble(9), 0);
				assertEquals(0, rs.getDouble(10), 0);

				assertEquals(true, rs.next());
				assertEquals("200903--", rs.getString(1));
				assertEquals("A001", rs.getString(2));
				assertEquals("A0000", rs.getString(3));
				assertEquals(31, rs.getDouble(4), 0);
				assertEquals(32, rs.getDouble(5), 0);
				assertEquals(33, rs.getDouble(6), 0);
				assertEquals(34, rs.getDouble(7), 0);
				assertEquals(35, rs.getDouble(8), 0);
				assertEquals(36, rs.getDouble(9), 0);
				assertEquals(37, rs.getDouble(10), 0);

				assertEquals(false, rs.next());
			}
			finally {
				stat.close();
			}
		}
		finally {
			conn.close();
		}

	}

	/**
	 * 测试更新数据，目的表有主键；
	 * 已有数据根据主键更新；
	 * 新的数据直接写入；
	 * @param srcpool
	 * @param targetpool
	 * @throws Exception
	 */
	private void testUpdateForKeys(ConnectionFactory srcpool, ConnectionFactory targetpool) throws Exception {
		initSrcData(srcpool);
		initTargetData(targetpool);
		try {
			DataCopyForUpdate dcu = DataCopyForUpdate.createInstance();
			dcu.setIprogress(new ProgressDefault());
			dcu.setSourceDataPool(srcpool);
			dcu.setTargetDataPool(targetpool);
			dcu.setTargetTable("t_test_target");
			dcu.isOnlyInsertNewRecord(false);

			dcu.setPrimaryKeys(new String[] { "bbq_", "userid_" });
			dcu.addSourceSql("select bbq_,userid_,hy_dm,zb1,zb2,zb3 from t_test_src where bbq_='200901--'");
			dcu.addSourceSql("select bbq_,userid_,hy_dm,zb4,zb5,zb6 from t_test_src where bbq_='200901--'");
			//测试多次提交；
			dcu.executeUpdate();
			dcu.addSourceSql("select bbq_,userid_,hy_dm,zb1,zb2,zb3 from t_test_src where bbq_='200902--'");
			//测试多次提交；
			dcu.executeUpdate();
			dcu.addSourceSql("select bbq_,userid_,hy_dm,zb4,zb5,zb6 from t_test_src where bbq_='200902--'");
			dcu.executeUpdate();

			checkUpdateData(targetpool);
		}
		finally {
			dropTable(srcpool, "t_test_src");
			dropTable(targetpool, "t_test_target");
		}
	}

	private void checkUpdateData(ConnectionFactory targetpool) throws Exception {
		Connection conn = targetpool.getConnection();
		try {
			Statement stat = conn.createStatement();
			try {
				ResultSet rs = stat.executeQuery("select bbq_,userid_,hy_dm,zb1,zb2,zb3,zb4,zb5,zb6,zb7 from t_test_target order by bbq_,userid_");
				assertEquals(true, rs.next());
				assertEquals("200901--", rs.getString(1));
				assertEquals("A001", rs.getString(2));
				assertEquals("A0000", rs.getString(3));
				assertEquals(11, rs.getDouble(4), 0);
				assertEquals(12, rs.getDouble(5), 0);
				assertEquals(13, rs.getDouble(6), 0);
				assertEquals(14, rs.getDouble(7), 0);
				assertEquals(15, rs.getDouble(8), 0);
				assertEquals(6, rs.getDouble(9), 0);
				assertEquals(7, rs.getDouble(10), 0);

				assertEquals(true, rs.next());
				assertEquals("200901--", rs.getString(1));
				assertEquals("B001", rs.getString(2));
				assertEquals("B0000", rs.getString(3));
				assertEquals(31, rs.getDouble(4), 0);
				assertEquals(32, rs.getDouble(5), 0);
				assertEquals(33, rs.getDouble(6), 0);
				assertEquals(34, rs.getDouble(7), 0);
				assertEquals(35, rs.getDouble(8), 0);
				assertEquals(0, rs.getDouble(9), 0);
				assertEquals(0, rs.getDouble(10), 0);

				assertEquals(true, rs.next());
				assertEquals("200901--", rs.getString(1));
				assertEquals("C001", rs.getString(2));
				assertEquals("C0000", rs.getString(3));
				assertEquals(61, rs.getDouble(4), 0);
				assertEquals(62, rs.getDouble(5), 0);
				assertEquals(63, rs.getDouble(6), 0);
				assertEquals(64, rs.getDouble(7), 0);
				assertEquals(65, rs.getDouble(8), 0);
				assertEquals(0, rs.getDouble(9), 0);
				assertEquals(0, rs.getDouble(10), 0);

				assertEquals(true, rs.next());
				assertEquals("200902--", rs.getString(1));
				assertEquals("A001", rs.getString(2));
				assertEquals("A0000", rs.getString(3));
				assertEquals(21, rs.getDouble(4), 0);
				assertEquals(22, rs.getDouble(5), 0);
				assertEquals(23, rs.getDouble(6), 0);
				assertEquals(24, rs.getDouble(7), 0);
				assertEquals(25, rs.getDouble(8), 0);
				assertEquals(0, rs.getDouble(9), 0);
				assertEquals(0, rs.getDouble(10), 0);

				assertEquals(true, rs.next());
				assertEquals("200902--", rs.getString(1));
				assertEquals("B001", rs.getString(2));
				assertEquals("B0000", rs.getString(3));
				assertEquals(41, rs.getDouble(4), 0);
				assertEquals(42, rs.getDouble(5), 0);
				assertEquals(43, rs.getDouble(6), 0);
				assertEquals(44, rs.getDouble(7), 0);
				assertEquals(45, rs.getDouble(8), 0);
				assertEquals(26, rs.getDouble(9), 0);
				assertEquals(27, rs.getDouble(10), 0);

				assertEquals(true, rs.next());
				assertEquals("200902--", rs.getString(1));
				assertEquals("C001", rs.getString(2));
				assertEquals("C0000", rs.getString(3));
				assertEquals(71, rs.getDouble(4), 0);
				assertEquals(72, rs.getDouble(5), 0);
				assertEquals(73, rs.getDouble(6), 0);
				assertEquals(74, rs.getDouble(7), 0);
				assertEquals(75, rs.getDouble(8), 0);
				assertEquals(0, rs.getDouble(9), 0);
				assertEquals(0, rs.getDouble(10), 0);

				assertEquals(true, rs.next());
				assertEquals("200903--", rs.getString(1));
				assertEquals("A001", rs.getString(2));
				assertEquals("A0000", rs.getString(3));
				assertEquals(31, rs.getDouble(4), 0);
				assertEquals(32, rs.getDouble(5), 0);
				assertEquals(33, rs.getDouble(6), 0);
				assertEquals(34, rs.getDouble(7), 0);
				assertEquals(35, rs.getDouble(8), 0);
				assertEquals(36, rs.getDouble(9), 0);
				assertEquals(37, rs.getDouble(10), 0);

				assertEquals(false, rs.next());
			}
			finally {
				stat.close();
			}
		}
		finally {
			conn.close();
		}
	}

	private void dropTable(ConnectionFactory srcpool, String tbname) throws Exception {
		Connection conn = srcpool.getConnection();
		try {
			DbDefiner dbf = srcpool.getDbDefiner();
			if (dbf.tableExists(conn, null, tbname))
				dbf.dropTable(conn, null, tbname);
		}
		finally {
			conn.close();
		}
	}

	private void initTargetData(ConnectionFactory targetpool) throws Exception {
		Connection conn = targetpool.getConnection();
		DbDefiner dbv = targetpool.getDbDefiner();
		String targettable = "t_test_target";
		try {
			//创建目的表；
			if (dbv.tableExists(conn, null, targettable)) {
				dbv.dropTable(conn, null, targettable);
			}
			dbv.clearDefineInfo();
			dbv.defineStringField("bbq_", 10, null, false, false);
			dbv.defineStringField("userid_", 20, null, false, false);
			dbv.defineStringField("hy_dm", 10, null, true, false);
			dbv.defineFloatField("zb1", 18, 4, null, true, false);
			dbv.defineFloatField("zb2", 18, 4, null, true, false);
			dbv.defineFloatField("zb3", 18, 4, null, true, false);
			dbv.defineFloatField("zb4", 18, 4, null, true, false);
			dbv.defineFloatField("zb5", 18, 4, null, true, false);
			dbv.defineFloatField("zb6", 18, 4, null, true, false);// 数值类型；
			dbv.defineFloatField("zb7", 18, 4, null, true, false);
			dbv.createTable(conn, null, targettable);

			Statement stat = conn.createStatement();
			stat.executeUpdate("insert into "
					+ targettable
					+ " (bbq_,userid_,hy_dm,zb1,zb2,zb3,zb4,zb5,zb6,zb7)values('200901--','A001','A0000',1,2,3,4,5,6,7)");
			stat.executeUpdate("insert into "
					+ targettable
					+ " (bbq_,userid_,hy_dm,zb1,zb2,zb3,zb4,zb5,zb6,zb7)values('200903--','A001','A0000',31,32,33,34,35,36,37)");
			stat.executeUpdate("insert into "
					+ targettable
					+ " (bbq_,userid_,hy_dm,zb1,zb2,zb3,zb4,zb5,zb6,zb7)values('200902--','B001','B0000',21,22,23,24,25,26,27)");
			stat.close();
		}
		finally {
			conn.close();
		}

	}

	private void initSrcData(ConnectionFactory srcpool) throws Exception {
		Connection conn = srcpool.getConnection();
		DbDefiner dbv = srcpool.getDbDefiner();
		String tbname = "t_test_src";
		try {
			//创建源表；
			if (dbv.tableExists(conn, null, tbname)) {
				dbv.dropTable(conn, null, tbname);
			}
			dbv.clearDefineInfo();
			dbv.defineStringField("bbq_", 10, null, false, false);
			dbv.defineStringField("userid_", 20, null, false, false);
			dbv.defineStringField("hy_dm", 10, null, true, false);
			dbv.defineFloatField("zb1", 18, 4, null, true, false);
			dbv.defineFloatField("zb2", 18, 4, null, true, false);
			dbv.defineFloatField("zb3", 18, 4, null, true, false);
			dbv.defineFloatField("zb4", 18, 4, null, true, false);
			dbv.defineFloatField("zb5", 18, 4, null, true, false);
			dbv.defineStringField("zb6", 18, null, true, false);//字符类型
			dbv.createTable(conn, null, tbname);

			Statement stat = conn.createStatement();
			stat.executeUpdate("insert into "
					+ tbname
					+ " (bbq_,userid_,hy_dm,zb1,zb2,zb3,zb4,zb5,zb6)values('200901--','A001','A0000',11,12,13,14,15,'16')");
			stat.executeUpdate("insert into "
					+ tbname
					+ " (bbq_,userid_,hy_dm,zb1,zb2,zb3,zb4,zb5,zb6)values('200902--','A001','A0000',21,22,23,24,25,'26')");
			stat.executeUpdate("insert into "
					+ tbname
					+ " (bbq_,userid_,hy_dm,zb1,zb2,zb3,zb4,zb5,zb6)values('200901--','B001','B0000',31,32,33,34,35,'36')");
			stat.executeUpdate("insert into "
					+ tbname
					+ " (bbq_,userid_,hy_dm,zb1,zb2,zb3,zb4,zb5,zb6)values('200902--','B001','B0000',41,42,43,44,45,'46')");
			stat.executeUpdate("insert into "
					+ tbname
					+ " (bbq_,userid_,hy_dm,zb1,zb2,zb3,zb4,zb5,zb6)values('200903--','B001','B0000',51,52,53,54,55,'56')");
			stat.executeUpdate("insert into "
					+ tbname
					+ " (bbq_,userid_,hy_dm,zb1,zb2,zb3,zb4,zb5,zb6)values('200901--','C001','C0000',61,62,63,64,65,'66')");
			stat.executeUpdate("insert into "
					+ tbname
					+ " (bbq_,userid_,hy_dm,zb1,zb2,zb3,zb4,zb5,zb6)values('200902--','C001','C0000',71,72,73,74,75,'76')");

			stat.close();
		}
		finally {
			conn.close();
		}

	}
}
