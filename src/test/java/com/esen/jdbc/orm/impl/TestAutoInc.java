package com.esen.jdbc.orm.impl;

import java.util.List;

import junit.framework.TestCase;

import com.esen.jdbc.orm.AutoInc;
import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.EntityInfoManager;
import com.esen.jdbc.orm.EntityInfoManagerFactory;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.Query;
import com.esen.jdbc.orm.QueryResult;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.TestFunc;
import com.esen.jdbc.orm.helper.EntityAdvFunc;

/**
 * 测试自增类型
 * 
 * @author liujin
 */
public class TestAutoInc extends TestCase {
	private Session session;

	private static String entityName = "orm.AutoInc"; 
	
	private static EntityInfoManager entityManager 
	= EntityInfoManagerFactory.buildFromFile("test/com/esen/jdbc/orm/autoinc_mapping.xml");
	
	private static EntityInfo entityInfo = entityManager.getEntity(entityName);
	
	public TestAutoInc() {

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
	private AutoInc[] getAll() {
		try {
			Query queryExe = session.createQuery(entityInfo.getClass(), entityName);		
			QueryResult result = queryExe.query(null, "id=false", null);

			int rows = result.calcTotalCount();
			List rowset = result.list(-1, 0);
			if (rowset == null) {
				return null;
			}
			assertEquals(rows, rowset.size());

			AutoInc[] dataTypes = new AutoInc[rows];
			for (int i = 0; i < rows; i++) {
				dataTypes[i] = (AutoInc) rowset.get(i);
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
	public void testAutoInc1() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//插入数据
			AutoInc obj = new AutoInc();
			session.add(entityName, obj);
			
			String[] values = new String[] {null, "", "abcdefg", "一二三四"};
			int i = 1;
			for (; i < values.length; i++) {
				obj.setValue(values[i]);
				session.add(entityName, obj);
			}
			session.commit();
			
			//查询结果
			AutoInc[] results = getAll();
			assertNotNull(results);
			assertEquals(values.length, results.length);			
			for (i = 0; i < values.length; i++) {
		//		assertEquals(i + 1, results[i].getId());
				assertEquals(values[i], results[i].getValue());
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();		
		} finally {
			session.close();
		}
	}
}