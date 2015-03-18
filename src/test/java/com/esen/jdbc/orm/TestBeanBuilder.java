package com.esen.jdbc.orm;

import java.util.Date;

import junit.framework.TestCase;

import com.esen.jdbc.orm.helper.EntityAdvFunc;

/**
 * xml解析单元测试
 *
 * @author liujin
 */
public class TestBeanBuilder extends TestCase {

	private Session session;

	private String entityName = "orm.Entity"; 
	
	private EntityInfoManager entityManager 
				= EntityInfoManagerFactory.buildFromFile("/test/com/esen/jdbc/orm/entitybeanbuilder_mapping.xml");
	
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
			assertEquals(entitys[0].getName() + entitys[0].getName(), entity.getName());
			assertEquals(entitys[0].getPrice(), entity.getPrice()); //使用 BeanBuilder 定义的方法创建对象，该值为 0
			
			entity = (Entity) queryExe.get(entitys[10].getId());
			assertNotNull(entity);
			assertEquals(entitys[10].getId(), entity.getId());
			assertEquals(entitys[10].getName() + entitys[10].getName(), entity.getName());
			assertEquals(entitys[10].getPrice(), entity.getPrice());
			
			// idValue 不存在
			entity = (Entity) queryExe.get(12334);
			assertNull(entity);			
		} catch (ORMException e) {
			e.printStackTrace();
			fail();
		} finally {
			session.close();
		}
	}
	
}
