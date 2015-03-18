package com.esen.jdbc.orm.impl;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import org.w3c.dom.Document;

import junit.framework.TestCase;

import com.esen.jdbc.orm.Blob;
import com.esen.jdbc.orm.DataType;
import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.EntityInfoManager;
import com.esen.jdbc.orm.EntityInfoManagerFactory;
import com.esen.jdbc.orm.FileBlob;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.Query;
import com.esen.jdbc.orm.QueryResult;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.TestFunc;
import com.esen.jdbc.orm.helper.EntityAdvFunc;
import com.esen.util.XmlFunc;

/**
 * 测试所有支持的数据类型
 * 
 * 数据按 空值，一般值，边界值 进行测试
 * 空值单独处理，注意 null 和  默认值
 * 
 * @author liujin
 */
public class TestDataTypeValue extends TestCase {
	private Session session;

	private static String entityName = "orm.DataType"; 
	
	private static EntityInfoManager entityManager 
	= EntityInfoManagerFactory.buildFromFile("test/com/esen/jdbc/orm/datatype_mapping.xml");;
	
	private static EntityInfo entityInfo = entityManager.getEntity(entityName);
	
	public TestDataTypeValue() {

	}

	private void init() {
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			
			EntityAdvFunc.repairTable(session, entityName, null);
			session.beginTransaction();
			session.delete(entityName);
			session.commit();
		} catch (ORMException e) {
			fail();
		} finally {
			session.close();
		}
	}
	
	/**
	 * 查询所有记录
	 */
	private DataType[] getAll() {
		try {
			Query queryExe = session.createQuery(entityInfo.getClass(), entityName);		
			QueryResult result = queryExe.query(null, "id=false", null);

			int rows = result.calcTotalCount();
			List rowset = result.list(-1, 0);
			if (rowset == null) {
				return null;
			}
			assertEquals(rows, rowset.size());

			DataType[] dataTypes = new DataType[rows];
			for (int i = 0; i < rows; i++) {
				dataTypes[i] = (DataType) rowset.get(i);
			}
			return dataTypes;
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();
		}

		return null;
	}

	/**
	 * 测试 int
	 */
	public void testCint1() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			int[] values = new int[] {0, 1, Integer.MAX_VALUE, Integer.MIN_VALUE};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCint1(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].getCint1());
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 Integer
	 */
	public void testCint2() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			Integer[] values = new Integer[] {null, 1, Integer.MAX_VALUE, Integer.MIN_VALUE};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCint2(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].getCint2());
			}
		} catch (Exception sqlex) {
			fail();		
		} finally {
			session.close();
		}
	}

	/**
	 * 测试 long
	 */
	public void testClong1() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			long[] values = new long[] {0, 1, Integer.MAX_VALUE, Integer.MIN_VALUE};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setClong1(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].getClong1());
			}
		} catch (Exception sqlex) {
			fail();		
		} finally {
			session.close();
		}
	}

	/**
	 * 测试 Long
	 */
	public void testClong2() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			Long[] values = new Long[] {null, 1L, (long) Integer.MAX_VALUE, (long) Integer.MIN_VALUE};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setClong2(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].getClong2());
			}
		} catch (Exception sqlex) {
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 byte
	 */
	public void testCbyte1() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			byte[] values = new byte[] {0, 1, Byte.MAX_VALUE, Byte.MIN_VALUE};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCbyte1(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].getCbyte1());
			}
		} catch (Exception sqlex) {
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 Byte
	 */
	public void testCbyte2() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			Byte[] values = new Byte[] {null, 1, Byte.MAX_VALUE, Byte.MIN_VALUE};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCbyte2(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].getCbyte2());
			}
		} catch (Exception sqlex) {
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 double
	 * 38,10
	 */
	public void testCdouble1() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			//double[] values = new double[] {0, 1, Double.MAX_VALUE, Double.MIN_VALUE};
			double[] values = new double[] {0, 1, 1234567890.1234567, -1234567890.1234567};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCdouble1(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].getCdouble1());
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 Double 
	 * 10,2
	 */
	public void testCdouble2() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			//double[] values = new double[] {0, 1, Double.MAX_VALUE, Double.MIN_VALUE};
			Double[] values = new Double[] {null, (double) 1L, 12345678.12, -12345678.12};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCdouble2(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].getCdouble2());
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 float
	 * 38,10
	 */
	public void testCfloat1() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			float[] values = new float[] {0, 1, (float) 1234567890.12345, (float) -1234567890.12345};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCfloat1(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].getCfloat1());
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 Float 
	 * 10,2
	 */
	public void testCfloat2() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			Float[] values = new Float[] {null, (float) 1, (float) 12345678.12, (float) -12345678.12};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCfloat2(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].getCfloat2());
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 String 
	 * 10
	 */
	public void testCstr() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			String[] values = new String[] {null, "", "abcdefg", "一二三四五"};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCstr(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//超过最大长度，报错
			try {
				obj.setId(100);
				obj.setCstr("abcdefghijklmn");
				session.add(entityName, obj);
				fail();
			} catch (ORMException e) {
				;
			} finally {
				session.rollback();
			}
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].getCstr());
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 StringBuffer 
	 * 20
	 */
	public void testCstrbuf() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			StringBuffer[] values = new StringBuffer[] 
					{null, new StringBuffer(""), new StringBuffer("abcdefg"), new StringBuffer("一二三四五")};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCstrbuf(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);
			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				if (i == 0) {
					assertNull(results[i].getCstrbuf());
				} else {
					assertEquals(values[i].toString(), results[i].getCstrbuf().toString());
				}
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 StringBuilder 
	 * len = 30
	 */
	public void testCstrbuilder() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			StringBuilder[] values = new StringBuilder[] 
					{null, new StringBuilder(""), new StringBuilder("abcdefg"), new StringBuilder("一二三四五")};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCstrbuilder(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);
			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				if (i == 0) {
					assertNull(results[i].getCstrbuilder());
				} else {
					assertEquals(values[i].toString(), results[i].getCstrbuilder().toString());
				}
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 char 
	 */
	public void testCchar1() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			char[] values = new char[]{0, '0', 'A', 'z'};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCchar1(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);
			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].getCchar1());
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 Character 
	 */
	public void testCchar2() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			Character[] values = new Character[]{null, '0', 'A', 'z'};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCchar2(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);
			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].getCchar2());
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 boolean 
	 */
	public void testCbool1() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			boolean[] values = new boolean[]{false, false, true};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCbool1(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);
			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].isCbool1());
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 Boolean
	 */
	public void testCbool2() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			Boolean[] values = new Boolean[]{null, false, true};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCbool2(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);
			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].getCbool2());
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 int 作为 布尔型
	 */
	public void testCbool3() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			int[] values = new int[]{0, 0, 1, -12345, 12345};
			int[] valuesExpected = new int[]{0, 0, 1, 0, 1};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCbool3(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);
			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(valuesExpected[i], results[i].getCbool3());
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 Integer 作为 布尔型
	 */
	public void testCbool4() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			Integer[] values = new Integer[]{null, 0, 1, -12345, 12345};
			Integer[] valuesExpected = new Integer[]{null, 0, 1, 0, 1};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCbool4(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);
			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(valuesExpected[i], results[i].getCbool4());
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 Calendar
	 */
	public void testCcal() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			Calendar[] values = new Calendar[2];
			values[1] = Calendar.getInstance();
			
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCcal(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);
			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				
				if (i == 0) {				
					assertEquals(values[i], results[i].getCcal());
				} else {
					assertEquals(values[i].get(Calendar.YEAR), results[i].getCcal().get(Calendar.YEAR));
					assertEquals(values[i].get(Calendar.MONTH), results[i].getCcal().get(Calendar.MONTH));
					assertEquals(values[i].get(Calendar.DAY_OF_MONTH), results[i].getCcal().get(Calendar.DAY_OF_MONTH));
					assertEquals(values[i].get(Calendar.HOUR_OF_DAY), results[i].getCcal().get(Calendar.HOUR_OF_DAY));
					assertEquals(values[i].get(Calendar.MINUTE), results[i].getCcal().get(Calendar.MINUTE));
					assertEquals(values[i].get(Calendar.SECOND), results[i].getCcal().get(Calendar.SECOND));
				}
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试  java.sql.Date
	 */
	public void testCsqlDate() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			java.sql.Date[] values = new java.sql.Date[2];
			values[1] = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
			
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCsqlDate(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);
			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				if (i == 0) {
					assertNull(results[i].getCsqlDate());
				} else {
					assertEquals(values[i].getYear(), results[i].getCsqlDate().getYear());
					assertEquals(values[i].getMonth(), results[i].getCsqlDate().getMonth());
					assertEquals(values[i].getDate() , results[i].getCsqlDate().getDate());
//					assertEquals(values[i].getHours() , results[i].getCsqlDate().getHours());
//					assertEquals(values[i].getMinutes(), results[i].getCsqlDate().getMinutes());
//					assertEquals(values[i].getSeconds(), results[i].getCsqlDate().getSeconds());
				}
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试  java.util.Date
	 */
	public void testCutilDate() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			java.util.Date[] values = new java.util.Date[2];
			values[1] = new java.util.Date(Calendar.getInstance().getTimeInMillis());
			
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCutilDate(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);
			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				if (i == 0) {
					assertNull(results[i].getCutilDate());
				} else {
					assertEquals(values[i].getYear(), results[i].getCutilDate().getYear());
					assertEquals(values[i].getMonth(), results[i].getCutilDate().getMonth());
					assertEquals(values[i].getDate() , results[i].getCutilDate().getDate());
				}
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试  java.sql.Timestamp
	 */
	public void testCsqltimestamp() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			java.sql.Timestamp[] values = new java.sql.Timestamp[2];
			values[1] = new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis());
			
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCtimestamp(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);
			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				if (i == 0) {
					assertNull(results[i].getCtimestamp());
				} else {
					assertEquals(values[i].getYear(), results[i].getCtimestamp().getYear());
					assertEquals(values[i].getMonth(), results[i].getCtimestamp().getMonth());
					assertEquals(values[i].getDate() , results[i].getCtimestamp().getDate());
					assertEquals(values[i].getHours() , results[i].getCtimestamp().getHours());
					assertEquals(values[i].getMinutes(), results[i].getCtimestamp().getMinutes());
					assertEquals(values[i].getSeconds(), results[i].getCtimestamp().getSeconds());
				}
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 clob
	 */
	public void testCclob() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			String[] values = new String[] {null, "", "abcdefg", "一二三四五"};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCclob(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].getCclob());
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 document, clob
	 */
	public void testCdocument1() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			Document[] values = {
					null,
					XmlFunc.getDocument("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><mapping>aaaaa</mapping>"),
			};
			
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCdocument1(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				if (values[i] == null) {
					assertNull(results[i].getCdocument1());
				} else {
					assertEquals(XmlFunc.document2str(values[i], "utf-8"), 
							XmlFunc.document2str(results[i].getCdocument1(), "utf-8"));
				}
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 document, blob
	 */
	public void testCdocument2() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			Document[] values = {
					null,
					XmlFunc.getDocument("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><mapping>aaaaa</mapping>"),
			};
			
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCdocument2(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				if (values[i] == null) {
					assertNull(results[i].getCdocument2());
				} else {
					assertEquals(XmlFunc. document2str(values[i], "utf-8"), 
							XmlFunc.document2str(results[i].getCdocument2(), "utf-8"));
				}			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试  blob
	 */
	public void testCblob() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			DataType obj = new DataType();
			obj.setId(1);
			session.add(entityName, obj);
			
			File aa = new File("test/com/esen/jdbc/orm/datatype_mapping.xml");
			FileBlob[] values = {
					null,
					new FileBlob(aa),
			};
			
			int i = 1;
			for (; i < values.length; i++) {
				obj.setId(i + 1);
				obj.setCblob(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			DataType[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);			
			for (i = 0; i < values.length; i++) {
				assertEquals(i + 1, results[i].getId());
				if (values[i] == null) {
					assertNull(results[i].getCblob());
				} else {
					Blob fblob = results[i].getCblob();
					assertEquals(fblob.length(), aa.length());
					fblob.free();
				}
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
}
