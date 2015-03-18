package com.esen.jdbc.orm.impl;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import com.esen.jdbc.orm.Entity;
import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.EntityInfoManager;
import com.esen.jdbc.orm.EntityInfoManagerFactory;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.Query;
import com.esen.jdbc.orm.QueryResult;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.TestFunc;
import com.esen.jdbc.orm.helper.EntityAdvFunc;
import com.esen.util.exp.Expression;

/**
 * 测试 Query
 * 
 * @author liujin
 */
public class TestQuery extends TestCase {
	private Session session;

	private String entityName = "orm.Entity"; 
	
	private EntityInfoManager entityManager 
				= EntityInfoManagerFactory.buildFromFile("test/com/esen/jdbc/orm/entity_mapping.xml");;
	
	private EntityInfo entityInfo = entityManager.getEntity(entityName);

	private	Entity[] entitys = {
			new Entity(1, "apple", 6.00, new Date()),
			new Entity(2, "banana", 10.00, null),
			new Entity(3, "苹果", 5.00, new Date()),
			new Entity(4, "香蕉", 3.00, new Date()),
			new Entity(5, "梨子", 3,  null),
			new Entity(6, "pear", 8.5, new Date()),
			new Entity(7, "peach", 15, new Date()),
			new Entity(8, "西瓜", 20, new Date()),
			new Entity(9, "葡萄", 10,  null),
			new Entity(10, "水果", 10, new Date()),
			new Entity(1001, "banana", 5, new Date()),
			new Entity(1002, "柑橘", 8, new Date()),
			new Entity(1003, "apple", 0, new Date()),
			new Entity(1004, "banana", 0,  null),
	};
				
	public TestQuery() {

	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			
			EntityAdvFunc.repairTable(session, entityName, null);
			session.beginTransaction();
			session.delete(entityName);
			session.commit();
			
			for (int i = 0; i < entitys.length; i++) {
				session.add(entityName, entitys[i]);
			}
			session.commit();
		} catch (ORMException e) {
			e.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}

	/**
	 * 测试 get
	 * 包括：
	 *	get(idValue);
	 *  get(idValue, propertyNames);
	 */
	public void testGet() {
		initData(); 
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			Query queryExe = session.createQuery(entityInfo.getClass(), entityName);
			
			//测试 get(idValue)
			Entity entity = (Entity) queryExe.get(entitys[0].getId());
			assertNotNull(entity);
			assertEquals(entitys[0].getId(), entity.getId());
			assertEquals(entitys[0].getName(), entity.getName());
			assertEquals(entitys[0].getPrice(), entity.getPrice());
			
			entity = (Entity) queryExe.get(entitys[10].getId());
			assertNotNull(entity);
			assertEquals(entitys[10].getId(), entity.getId());
			assertEquals(entitys[10].getName(), entity.getName());
			assertEquals(entitys[10].getPrice(), entity.getPrice());
			
			// idValue 不存在
			entity = (Entity) queryExe.get(12334);
			assertNull(entity);
			
			//测试 get(idValue, propertyNames);
			entity = (Entity) queryExe.get(entitys[0].getId(), new String[]{"name", "price"});
			assertNotNull(entity);
			assertEquals(0, entity.getId()); //没有查询的属性的值
			assertEquals(entitys[0].getName(), entity.getName());
			assertEquals(entitys[0].getPrice(), entity.getPrice());
			assertNull(entity.getSaleDay());
			
		} catch (ORMException e) {
			e.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}
	

	/**
	 * 测试 exist
	 */
	public void testExist() {
		initData(); 
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			Query queryExe = session.createQuery(entityInfo.getClass(), entityName);
			
			assertTrue(queryExe.exist(entitys[0].getId()));
			assertFalse(queryExe.exist(12345678));			
		} catch (ORMException e) {
			e.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 list
	 */
	public void testList() {
		initData(); 
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			Query queryExe = session.createQuery(entityInfo.getClass(), entityName);
			
			QueryResult<Entity> results = queryExe.list();
			assertEquals(entitys.length, results.calcTotalCount());
			List<Entity> resultPart;

			int pageIndex = 0;
			int pageSize = 5;
			int totalPage = (int) Math.ceil(entitys.length * 1.0 / pageSize);
			for (; pageIndex < totalPage; pageIndex++) {
				resultPart = results.list(pageIndex, pageSize);
				if (pageIndex < totalPage - 1) {
					assertEquals(pageSize, resultPart.size());
				} else {
					assertEquals(4, resultPart.size());
				}
			
				for (int i = 0; i < resultPart.size(); i++) {
					assertEquals(entitys[i + pageIndex * pageSize].getId(), resultPart.get(i).getId());
					assertEquals(entitys[i + pageIndex * pageSize].getName(), resultPart.get(i).getName());
					assertEquals(entitys[i + pageIndex * pageSize].getPrice(), resultPart.get(i).getPrice());
				}
			}
		} catch (ORMException e) {
			e.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}
	
	public void testQueryOrderBy() {
		initData();
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);

			Query queryExe = session.createQuery(entityInfo.getClass(), entityName);
			
			//带排序
			//降序
			QueryResult result = queryExe.query(null, "id=true", null);

			int rows = result.calcTotalCount();
			assertEquals(entitys.length, rows);
			
			List<Entity> rowset = result.list(-1, -1);
			assertNotNull(rowset);
			
			for (int i = 0; i < rows; i++) {
				assertEquals(entitys[rows - i - 1].getId(), rowset.get(i).getId());
				assertEquals(entitys[rows - i - 1].getName(), rowset.get(i).getName());
				assertEquals(entitys[rows - i - 1].getPrice(), rowset.get(i).getPrice());
			}
			
			//多个排序条件
			result = queryExe.query(null, "name=true,id=false", null);

			rows = result.calcTotalCount();
			assertEquals(entitys.length, rows);
			
			rowset = result.list(-1, -1);
			assertNotNull(rowset);
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}
	
	/**
	 * 带表达式的查询
	 */
	public void testQueryExpression() {
		initData();
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);

			Query queryExe = session.createQuery(entityInfo.getClass(), entityName);;
			
			//不带参数
			Expression exp = new Expression("id > 100 and name='banana'");
			QueryResult result = queryExe.query(exp, "id=false", null);
			int rows = result.calcTotalCount();
			assertEquals(2, rows);
			
			List<Entity> rowset = result.list(-1, -1);
			assertNotNull(rowset);

			//1001
			assertEquals(entitys[10].getId(), rowset.get(0).getId());
			assertEquals(entitys[10].getName(), rowset.get(0).getName());
			assertEquals(entitys[10].getPrice(), rowset.get(0).getPrice());

			//1004
			assertEquals(entitys[13].getId(), rowset.get(1).getId());
			assertEquals(entitys[13].getName(), rowset.get(1).getName());
			assertEquals(entitys[13].getPrice(), rowset.get(1).getPrice());
			
			//带参数
			exp = new Expression("id = ? and name=?");
			result = queryExe.query(exp, "id=false", null, 1001, "banana");
			rows = result.calcTotalCount();
			assertEquals(1, rows);
			
			rowset = result.list(-1, -1);
			assertNotNull(rowset);

			//1001
			assertEquals(entitys[10].getId(), rowset.get(0).getId());
			assertEquals(entitys[10].getName(), rowset.get(0).getName());
			assertEquals(entitys[10].getPrice(), rowset.get(0).getPrice());

		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}
	
	/**
	 * 查询指定属性
	 */
	public void testQueryProperty() {
		initData();
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);

			Query queryExe = session.createQuery(entityInfo.getClass(), entityName);
			
			QueryResult result = queryExe.query(null, "", new String[]{"name"});
			int rows = result.calcTotalCount();
			assertEquals(entitys.length, rows);
			
			List<Entity> rowset = result.list(-1, -1);
			assertNotNull(rowset);
			
			//只查询到了 name 属性的值
			for (int i = 0; i < rows; i++) {
				assertEquals(0, rowset.get(i).getId());
				assertEquals(entitys[i].getName(), rowset.get(i).getName());
				assertEquals(0.0, rowset.get(i).getPrice());
				assertNull(rowset.get(i).getSaleDay());
			}
			
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}

}
