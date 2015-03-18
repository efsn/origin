package com.esen.jdbc.orm.impl;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import com.esen.jdbc.orm.Entity;
import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.EntityInfoManager;
import com.esen.jdbc.orm.EntityInfoManagerFactory;
import com.esen.jdbc.orm.Executer;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.Query;
import com.esen.jdbc.orm.QueryResult;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.TestFunc;
import com.esen.jdbc.orm.helper.EntityAdvFunc;
import com.esen.util.exp.Expression;

/**
 * 测试 UpdateExecuter
 * 
 * @author liujin
 */
public class TestExecuter extends TestCase {
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
				
	public TestExecuter() {

	}

	/**
	 * 查询所有记录
	 */
	private Entity[] getAll() {
		try {
			Query queryExe = session.createQuery(entityInfo.getClass(), entityName);		
			QueryResult result = queryExe.query(null, "id=false", null);

			int rows = result.calcTotalCount();
			List<Entity> rowset = result.list(-1, 0);
			if (rowset == null) {
				return null;
			}
			assertEquals(rows, rowset.size());

			Entity[] entitys = new Entity[rows];
			for (int i = 0; i < rows; i++) {
				entitys[i] = rowset.get(i);
			}
			return entitys;
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();
		}

		return null;
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
	 * 测试 add
	 */
	public void testAdd() {
		initData(); 
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
			Executer updateExe = session.createExecuter(entityInfo.getClass(), entityInfo.getEntityName());
			
			//插入数据
			Entity[] addObjs = {
					new Entity(1000001, "apple", 6.00, new Date()),
					new Entity(1000002, "banana", 10.00, null),
					new Entity(1000003, "苹果", 5.00, new Date())
			};
			
			int i = 0;
			for (i = 0; i < addObjs.length; i++) {
				updateExe.add(addObjs[i]);
			}
			session.commit();
			
			//检查结果
			Entity[] rowset = getAll();
			assertNotNull(rowset);
			assertEquals(entitys.length + addObjs.length, rowset.length);
			
			for (i = 0; i < entitys.length; i++) {
				assertEquals(entitys[i].getId(), rowset[i].getId());
				assertEquals(entitys[i].getName(), rowset[i].getName());
				assertEquals(entitys[i].getPrice(), rowset[i].getPrice());
			}
			for (; i < entitys.length + addObjs.length; i++) {
				assertEquals(addObjs[i - entitys.length].getId(), rowset[i].getId());
				assertEquals(addObjs[i - entitys.length].getName(), rowset[i].getName());
				assertEquals(addObjs[i - entitys.length].getPrice(), rowset[i].getPrice());
			}
			
			//插入同样Id的数据，报错
			try {
				for (i = 0; i < addObjs.length; i++) {
					updateExe.add(addObjs[i]);
					fail();
				}
			} catch(ORMException e) {
				;
			}
			session.rollback();
			
		} catch (ORMException e) {
			e.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试  delete
	 * delete(idValue)
	 */
	public void testDeleteByID() {
		initData(); 
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
			Executer updateExe = session.createExecuter(entityInfo.getClass(), entityInfo.getEntityName());
			
			int i = 0;
			for (i = 10; i < entitys.length; i++) {
				assertEquals(1, updateExe.delete(entitys[i].getId()));
			}
			session.commit();
			
			//检查结果
			Entity[] rowset = getAll();
			assertNotNull(rowset);
			assertEquals(10, rowset.length);
			
			for (i = 0; i < 10; i++) {
				assertEquals(entitys[i].getId(), rowset[i].getId());
				assertEquals(entitys[i].getName(), rowset[i].getName());
				assertEquals(entitys[i].getPrice(), rowset[i].getPrice());
			}
			
			//删除不存在的一行数据
			assertEquals(0, updateExe.delete(entitys[10].getId()));

		} catch (ORMException e) {
			e.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试  delete
	 * delete(condition, params)
	 */
	public void testDeleteByCondition() {
		initData(); 
		
		//测试带删除的条件的情况
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
			Executer updateExe = session.createExecuter(entityInfo.getClass(), entityInfo.getEntityName());
			
			int i = 0;
			Expression exp = new Expression("name = 'apple'");
			int deleteRows = updateExe.delete(exp, null);
			session.commit();
			
			//检查结果
			Entity[] rowset = getAll();
			assertNotNull(rowset);
			assertEquals(entitys.length - deleteRows, rowset.length);
			
			//不存在 Name 为 apple 的数据
			for (i = 0; i < rowset.length; i++) {
				assertFalse("apple".equals(rowset[i].getName()));
			}
		} catch (ORMException e) {
			e.printStackTrace();
			fail();
		} finally {
			session.close();
		}
		
		initData(); 
		
		//测试带删除的条件和参数的情况
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
			Executer updateExe = session.createExecuter(entityInfo.getClass(), entityInfo.getEntityName());		
			//带条件和参数
			Expression exp = new Expression("left(name, 1) != ? or id > ?");
			int deleteRows = updateExe.delete(exp, new Object[]{"a", 100});
			session.commit();
			
			//检查结果
			Entity[] rowset = getAll();
			assertNotNull(rowset);
			assertEquals(entitys.length - deleteRows, rowset.length);
			
			//只存在 name 第一个字母为  a 和 id < 100 的数据
			for (int i = 0; i < rowset.length; i++) {
				assertEquals('a', rowset[i].getName().charAt(0));
				assertTrue(rowset[i].getId() < 100);
			}
		} catch (ORMException e) {
			e.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}
	
	//public void update(Object oldId, Object object, String...properties) {
	public void testUpdate() {
		initData(); 
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
			Executer updateExe = session.createExecuter(entityInfo.getClass(), entityInfo.getEntityName());
			
			Entity newObj = new Entity(1, "appleApple-----------", 100.00, new Date());
			//更新所有字段
			updateExe.update(1, newObj);

			Entity[] rowset = getAll();
			assertNotNull(rowset);
			assertEquals(entitys.length, rowset.length);
			//比较第一行的数据即可
			assertEquals(newObj.getId(), rowset[0].getId());
			assertEquals(newObj.getName(), rowset[0].getName());
			assertEquals(newObj.getPrice(), rowset[0].getPrice());

			session.rollback();

			//更新指定字段
			newObj.setSaleDay(null);
			updateExe.update(3, newObj, "name", "saleDay");
			rowset = getAll();
			assertNotNull(rowset);
			assertEquals(entitys.length, rowset.length);
			//比较相应行的数据即可
			assertFalse(newObj.getId() == rowset[2].getId());
			assertEquals(newObj.getName(), rowset[2].getName());
			assertFalse(newObj.getPrice() == rowset[2].getPrice());
			assertNull(rowset[2].getSaleDay());
			
			session.rollback();
			
			//id也更新
			newObj.setId(10000000);
			updateExe.update(1004, newObj);
			rowset = getAll();
			assertNotNull(rowset);
			assertEquals(entitys.length, rowset.length);
			//比较相应行的数据即可
			assertEquals(newObj.getId(), rowset[13].getId());
			assertFalse(entitys[13].getId() == rowset[13].getId());
			assertEquals(newObj.getName(), rowset[13].getName());
			assertEquals(newObj.getPrice(), rowset[13].getPrice());
			
			session.rollback();
			
			rowset = getAll();
			assertNotNull(rowset);
			assertEquals(entitys.length, rowset.length);
			
			for (int i = 0; i < entitys.length; i++) {
				assertEquals(entitys[i].getId(), rowset[i].getId());
				assertEquals(entitys[i].getName(), rowset[i].getName());
				assertEquals(entitys[i].getPrice(), rowset[i].getPrice());
			}
		} catch (ORMException e) {
			e.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}
	
}