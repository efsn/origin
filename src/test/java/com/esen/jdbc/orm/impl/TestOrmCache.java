package com.esen.jdbc.orm.impl;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import com.esen.jdbc.orm.CacheEntity;
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
import com.esen.jdbc.orm.Update;
import com.esen.jdbc.orm.helper.EntityAdvFunc;
import com.esen.util.exp.Expression;

public class TestOrmCache extends TestCase {
	private Session session;

	private String entityName = "orm.CacheEntity"; 
	private String entityName2 = "orm.Entity"; 
	
	private EntityInfoManager entityManager 
				= EntityInfoManagerFactory.buildFromFile(
						"/com/esen/jdbc/orm/cache_mapping.xml", 
						"/com/esen/jdbc/orm/entity_mapping.xml");;
	
	private EntityInfo entityInfo = entityManager.getEntity(entityName);

	private	CacheEntity[] entitys = {
			new CacheEntity(1, "apple", 6.00, new Date()),
			new CacheEntity(2, "banana", 10.00, null),
			new CacheEntity(3, "苹果", 5.00, new Date()),
			new CacheEntity(4, "香蕉", 3.00, new Date()),
			new CacheEntity(5, "梨子", 3,  null),
			new CacheEntity(6, "pear", 8.5, new Date()),
			new CacheEntity(7, "peach", 15, new Date()),
			new CacheEntity(8, "西瓜", 20, new Date()),
			new CacheEntity(9, "葡萄", 10,  null),
			new CacheEntity(10, "水果", 10, new Date()),
			new CacheEntity(1001, "banana", 5, new Date()),
			new CacheEntity(1002, "柑橘", 8, new Date()),
			new CacheEntity(1003, "apple", 0, new Date()),
			new CacheEntity(1004, "banana", 0,  null),
	};
	
	private Entity[] entitys2 = {
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
				
	public TestOrmCache() {

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
	 * 初始化数据
	 */
	private void initData2() {
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			
			EntityAdvFunc.repairTable(session, entityName2, null);
			session.beginTransaction();
			session.delete(entityName2);
			session.commit();
			
			for (int i = 0; i < entitys2.length; i++) {
				session.add(entityName2, entitys2[i]);
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
	 * 测试缓存有效的情况
	 */
	public void testQuery() {
		initData(); 
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
			
			Query queryExe = session.createQuery(entityInfo.getClass(), entityName);

			System.out.println("日志中有查询的  sql 语句");
			CacheEntity data1 = (CacheEntity) queryExe.get(1);
			
			System.out.println("使用了缓存，两个对象的值应该相同，日志中没有再次发送 sql语句");
			CacheEntity data2 = (CacheEntity) queryExe.get(1);
			
			//两个对象的值相同，但两个对象不应该相同
			assertFalse(data1 == data2);
			
			System.out.println("sql语句不同，参数相同，对象不同，日志中可以看到 sql语句");
			CacheEntity data3 = (CacheEntity) queryExe.get(1, "name", "price");
			
			System.out.println("sql语句相同，但参数不同，对象不同，日志中可以看到 sql语句");
			CacheEntity data4 = (CacheEntity) queryExe.get(2, "name", "price");
			
			System.out.println("sql语句相同，参数相同，对象相同，日志中没有再次发送 sql 语句");
			CacheEntity data5 = (CacheEntity) queryExe.get(2, "name", "price");

			System.out.println("日志中有查询的  sql 语句");
			QueryResult result = queryExe.list();
			List list = result.list(-1, -1);			
			
			System.out.println("sql语句相同，limit 和 offset 相同，参数都为空, 日志中没有sql语句");
			QueryResult result2 = queryExe.list();
			List list2 = result2.list(-1, -1);
			
			System.out.println("sql语句相同， limit 和  offset 不相同，参数都为空，日志中有sql语句");
			QueryResult result3 = queryExe.list();
			List list3 = result3.list(0, 2);
			
			System.out.println("sql语句相同， limit 和  offset 相同，参数都为空，日志中没有sql语句");
			QueryResult result4= queryExe.list();
			List list4 = result4.list(0, 2);
		} catch (ORMException e) {
			e.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 Executer 的操作以后 cache 失效
	 */
	public void testExecuter() {
		initData(); 
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);

			Executer executer = session.createExecuter(entityInfo.getClass(), entityName);			
			Query queryExe = session.createQuery(entityInfo.getClass(), entityName);
			
			System.out.println("发送查询的  sql 语句");
			CacheEntity data = (CacheEntity) queryExe.get(1);
			
			System.out.println("使用缓存，不发送查询的 sql 语句");
			CacheEntity data2 = (CacheEntity) queryExe.get(1);
	
			//执行插入操作以后，缓存清除了，两个对象不相同
			CacheEntity addObj = new CacheEntity(1000001, "apple", 6.00, new Date());
			executer.add(addObj);
			
			System.out.println("执行插入操作以后，缓存清除了，重新发送查询的  sql 语句");
			CacheEntity data3 = (CacheEntity) queryExe.get(1);
			
			//执行删除操作以后，缓存清除了，两个对象不相同
			executer.delete(1000001);
			
			System.out.println("执行删除操作以后，缓存清除了，重新发送查询的  sql 语句");
			CacheEntity data4 = (CacheEntity) queryExe.get(1);

			
			executer.delete(new Expression("price>10"), null);
			
			System.out.println("执行删除操作以后，缓存清除了，重新发送查询的  sql 语句");
			CacheEntity data5 = (CacheEntity) queryExe.get(1);
			
			CacheEntity updateObj = new CacheEntity(1000001, "apple", 6.00, new Date());
			executer.update(1001, updateObj, "name");
			
			System.out.println("执行更新操作以后，缓存清除了，重新发送查询的  sql 语句");
			CacheEntity data6 = (CacheEntity) queryExe.get(1);
		} catch (ORMException e) {
			e.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试  Update 的操作以后 cache 失效
	 */
	public void testUpdate() {
		initData(); 
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
			Update updateExe = session.createUpdate(entityInfo.getClass(), entityInfo.getEntityName());
			
			Query queryExe = session.createQuery(entityInfo.getClass(), entityName);
			
			System.out.println("发送查询的  sql 语句");
			CacheEntity data = (CacheEntity) queryExe.get(1);
			
			System.out.println("使用缓存，不发送查询的 sql 语句");
			CacheEntity data2 = (CacheEntity) queryExe.get(1);
			
			updateExe.setPropertyValue("name", "abcd");
			updateExe.executeUpdate();
			
			System.out.println("执行更新操作以后，缓存清除了，重新发送查询的  sql 语句");
			CacheEntity data3 = (CacheEntity) queryExe.get(1);
			assertEquals(data3.getName(), "abcd");		
		} catch (ORMException e) {
			e.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试 根据参数配置，缓存失效的情况
	 */
	public void testCacheInvalid() {
		initData(); 
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
			
			Query queryExe = session.createQuery(entityInfo.getClass(), entityName);
			
			System.out.println("发送查询的  sql 语句");
			CacheEntity data1 = (CacheEntity) queryExe.get(1);
			
			System.out.println("使用缓存，不发送查询的 sql 语句");
			CacheEntity data2 = (CacheEntity) queryExe.get(1);
			
			//配置的 maxSize 为 8，缓存策略为 LRU，缓存失效
			System.out.println("测试缓存失效的情景（缓存个数超过配置的最大数）");
			CacheEntity[] data = new CacheEntity[10];
			for (int i = 0; i < 10; i++) {
				data[i] = (CacheEntity) queryExe.get(i + 1);
				assertEquals(data[i].getId(), i + 1);
			}
			
			System.out.println("缓存失效了，重新发送查询的  sql 语句");
			CacheEntity data3 = (CacheEntity) queryExe.get(1);
			assertEquals(data3.getId(), data1.getId());
			assertEquals(data3.getName(), data1.getName());
			assertEquals(data3.getPrice(), data1.getPrice());		

			CacheEntity data4 = (CacheEntity) queryExe.get(1);
			
			System.out.println("测试缓存失效的情景（缓存时间超过配置的失效时间）");
			//配置的失效时间为 20s，超时以后缓存失效
			try {
				Thread.sleep(30 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("缓存失效了，重新发送查询的  sql 语句");
			CacheEntity data5 = (CacheEntity) queryExe.get(1);
			assertEquals(data4.getId(), data5.getId());
			assertEquals(data4.getName(), data5.getName());
			assertEquals(data4.getPrice(), data5.getPrice());		
		} catch (ORMException e) {
			e.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试使用缓存和不使用缓存的查询时间
	 * 两个实体结构相同，数据相同
	 * 各完成 10000 次全表查询
	 */
	public void testTime() {
		initData(); 
		initData2();
		
		int TIMES = 10000;
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
		
			//使用缓存的情况
			System.out.println("测试使用缓存的情景");
			Query queryExe = session.createQuery(entityInfo.getClass(), entityName);
			long start = System.currentTimeMillis();
			for (int i = 0; i < TIMES; i++) {
				QueryResult result = queryExe.list();
				List list = result.list(-1, -1);
				for (int j = 0; j < list.size(); j++) {
					CacheEntity entity = (CacheEntity) list.get(j);
				}
			}
			long finish = System.currentTimeMillis();
			long time = finish - start;
			
			System.out.println("测试不使用缓存的情景");
			Query queryExe2 = session.createQuery(entityInfo.getClass(), entityName2);
			
			start = System.currentTimeMillis();
			for (int i = 0; i < TIMES; i++) {
				QueryResult result = queryExe2.list();
				List list = result.list(-1, -1);
				for (int j = 0; j < list.size(); j++) {
					Entity entity = (Entity) list.get(j);
				}
			}
			finish = System.currentTimeMillis();
			long time2 = finish - start;
			
			System.out.println("使用缓存花费的时间：" + time * 1.0 / 1000 + " s");
			System.out.println("不使用缓存花费的时间：" + time2 * 1.0 / 1000 + " s");

		} catch (ORMException e) {
			e.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}
	
	/**
	 * 在使用缓存时，扩展属性的值可能会丢失
	 */
	public void testExtProp() {
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			
			EntityAdvFunc.repairTable(session, entityName, null);
			session.beginTransaction();
			session.delete(entityName);
			session.commit();
			
			for (int i = 0; i < entitys.length; i++) {
				entitys[i].setExtValue("ext_c1", "aaa" + entitys[i].getId());
				entitys[i].setExtValue("ext_c2", 100 + entitys[i].getId());
				session.add(entityName, entitys[i]);
			}
			session.commit();
			
			//查询
			Update updateExe = session.createUpdate(entityInfo.getClass(), entityInfo.getEntityName());
			
			Query queryExe = session.createQuery(entityInfo.getClass(), entityName);
			
			System.out.println("发送查询的  sql 语句");
			CacheEntity data = (CacheEntity) queryExe.get(1);

			assertEquals("aaa1", (String) data.getExtValue("ext_c1"));
			assertEquals(101.0, data.getExtValue("ext_c2"));
			
		} catch (ORMException e) {
			e.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}
}