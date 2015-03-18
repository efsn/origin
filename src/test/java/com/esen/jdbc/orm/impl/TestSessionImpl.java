package com.esen.jdbc.orm.impl;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.EntityInfoManager;
import com.esen.jdbc.orm.EntityInfoManagerFactory;
import com.esen.jdbc.orm.Entity;
import com.esen.jdbc.orm.Query;
import com.esen.jdbc.orm.QueryResult;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.Iterator;
import com.esen.jdbc.orm.Batch;
import com.esen.jdbc.orm.TestFunc;
import com.esen.jdbc.orm.Update;
import com.esen.jdbc.orm.helper.EntityAdvFunc;
import com.esen.util.exp.Expression;

public class TestSessionImpl extends TestCase {
	private Session session;

	private static String entityName = "orm.Entity"; //这里对应用户自己的entityName
	
	private static EntityInfoManager entityManager 
	= EntityInfoManagerFactory.buildFromFile("test/com/esen/jdbc/orm/entity_mapping.xml");;
	
	private static EntityInfo entityInfo = entityManager.getEntity(entityName);
	
	public TestSessionImpl() {

	}

	private void init() {
		session = TestFunc.buildSessionFactory(entityManager);
		//删除
		EntityAdvFunc.dropTable(session, entityName);		
		//创建实体名对应的表
		EntityAdvFunc.createTable(session, entityManager.getEntity(entityName));
		session.close();
	}
	
	/**
	 * 增加、更新、删除记录
	 */
	public void testSimple() {
		init();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//增加
			Entity obj = new Entity(10001, "apple", 6.00, new Date());
			session.add(entityName, obj);
			Entity obj2 = new Entity(10002, "banana", 10.00, new Date());
			session.add(entityName, obj2);

			session.commit();
			
			checkResult(obj, obj2);
			
			//更新
			Entity obj3 = new Entity(10002, "apple", 8.00, new Date());//这里对应用户自己要更新的对象。
			session.update(entityName, obj3);
			session.commit();
			
			checkResult(obj, obj3);

			//删除
			long idValue = 10002;//要删除的记录的id值
			session.delete(entityName, idValue);
			session.commit();
			
			checkResult(obj);
			session.close();
		} catch (Exception sqlex) {
			fail();		
		} finally {
			session.close();
		}
	}

	/**
	 * 查询所有记录
	 */
	private Entity[] getAll() {
		try {
			Query queryExe = session.createQuery(entityInfo.getClass(), entityName);		
			QueryResult result = queryExe.query(null, "id=false", null);

			int rows = result.calcTotalCount();
			List rowset = result.list(-1, 0);
			if (rowset == null) {
				return null;
			}
			assertEquals(rows, rowset.size());

			Entity[] entitys = new Entity[rows];
			for (int i = 0; i < rows; i++) {
				entitys[i] = (Entity) rowset.get(i);
			}
			return entitys;
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			fail();
		}

		return null;
	}

	/**
	 * 批量增加记录
	 */
	public void testBatchAdd() {
		init();
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
			
			Class<Entity> clazz = Entity.class;

			Batch<Entity> batch = session.createInsertBatch(clazz, entityName, "id", "name", "price");
			
			Entity fruit1 = new Entity(11001, "apple", 5.00, new Date());//这里对应用户自己要增加的对象1。
			batch.addBatch(fruit1);
			
			Entity fruit2 = new Entity(11002, "banana", 3.00, new Date());//这里对应用户自己要增加的对象2。
			batch.addBatch(fruit2);
			
			fruit2.setId(11003);
			batch.addBatch(fruit2);
			
			fruit2.setId(11004);
			fruit2.setName("pear");
			batch.addBatch(fruit2);
			
			batch.exectue();
			batch.close();
			
			session.commit();
			
			Entity[] expects = new Entity[4];
			expects[0] = new Entity(11001, "apple", 5.00, null);
			expects[1] = new Entity(11002, "banana", 3.00, null);
			expects[2] = new Entity(11003, "banana", 3.00, null);
			expects[3] = new Entity(11004, "pear", 3.00, null);			
			checkResult(expects);
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			session.rollback();
			fail();
		} finally {
			session.close();
		}
	}

	/**
	 * 批量更新记录
	 */	/**
	 * 根据条件批量删除记录
	 * 示例中将产地为w开头的记录删除
	 */
	public void testDelete2() {
		testBatchAdd();
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			Expression condition = new Expression("left(name,1) = ? ");//条件表达式
			String param1 = "a";

			session.delete(entityName, condition, param1);
			session.commit();
			
			Entity[] expects = new Entity[3];
			expects[0] = new Entity(11002, "banana", 3.00, null);
			expects[1] = new Entity(11003, "banana", 3.00, null);
			expects[2] = new Entity(11004, "pear", 3.00, null);
			checkResult(expects);
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			session.rollback();
			fail();
		} finally {
			session.close();
		}
	}

	//删除全部记录
	public void testDeleteAll() {
		testBatchAdd();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			session.delete(entityName);
			session.commit();
			
			checkResult();
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			session.rollback();
			fail();
		} finally {
			session.close();
		}
	}
	
	public void testBatchUpdate1() {
		testBatchAdd();
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
			Class<Entity> clazz = Entity.class;

			String[] propertyNames = {"name", "price"};
			Batch<Entity> batch = session.createUpdateBatch(clazz, entityName, propertyNames);
			
			Entity fruit1 = new Entity(11001, "apple002", 9.00, new Date());//这里对应用户自己要更新的对象1。
			batch.addBatch(fruit1);
			
			Entity fruit2 = new Entity(11002, "banana002", 6.00, new Date());//这里对应用户自己要更新的对象2。
			batch.addBatch(fruit2);
			
			batch.exectue();
			batch.close();
			
			session.commit();
			
			//只更新了两个字段
			Entity[] expects = new Entity[4];
			expects[0] = new Entity(11001, "apple002", 9.00, null);
			expects[1] = new Entity(11002, "banana002", 6.00, null);
			expects[2] = new Entity(11003, "banana", 3.00, null);
			expects[3] = new Entity(11004, "pear", 3.00, null);
			checkResult(expects);
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			session.rollback();
			fail();
		} finally {
			session.close();
		}
	}

	/**
	 * 批量更新记录的另一种实现
	 */
	public void testBatchUpdate2() {
		testBatchAdd();
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
			Class<Entity> clazz = Entity.class;

			Update<Entity> update = session.createUpdate(clazz, entityName);
			Expression condition = new Expression("name=?");//条件表达式
			String param1 = "pear";

			update= update.setCondition(condition, param1);
			Expression setExp = new Expression("'100'");
			update = update.setPropertyExp("price", setExp);
			
			update.executeUpdate();
			session.commit();
			
			//只更新了一条记录的一个字段
			Entity[] expects = new Entity[4];
			expects[0] = new Entity(11001, "apple", 5.00, null);
			expects[1] = new Entity(11002, "banana", 3.00, null);
			expects[2] = new Entity(11003, "banana", 3.00, null);
			expects[3] = new Entity(11004, "pear", 100.00, null);
			checkResult(expects);
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			session.rollback();
			fail();
		} finally {
			session.close();
		}
	}
	
	/**
	 * 测试批量删除
	 */
	public void testBatchDelete() {
		testBatchAdd();
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
			Class<Entity> clazz = Entity.class;

			String[] propertyNames = {"name", "price"};
			Batch<Entity> batch = session.createDeleteBatch(clazz, entityName, propertyNames);
			
			Entity fruit1 = new Entity(0, "banana", 3.00, null);//这里对应用户自己要删除的 2 个对象。
			batch.addBatch(fruit1);
			
			Entity fruit2 = new Entity(0, "apple", 4.00, null);//这里对应用户自己要删除的 0 个对象。
			batch.addBatch(fruit2);
			
			batch.exectue();
			batch.close();
			
			session.commit();
			
			//只剩下两个对象
			Entity[] expects = new Entity[2];
			expects[0] = new Entity(11001, "apple", 5.00, null);
			expects[1] = new Entity(11004, "pear", 3.00, null);
			checkResult(expects);
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			session.rollback();
			fail();
		} finally {
			session.close();
		}
	}
	
	/**
	 * 分页查询结果集
	 * 示例中查询出以w开头的产地的Fruit的分页结果
	 */
	public void testSelectPage() {
		QueryResult<Entity> queryResult = null;

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			Class<Entity> clazz = Entity.class;

			Query<Entity> query = session.createQuery(clazz, entityName);

			Expression condition = new Expression("left(name, 1) = ?");
			String oderbyProperties = "name=false, price=true";
			String[] propertyNames = {"name", "price"};

			queryResult = query.query(condition, oderbyProperties, propertyNames, "b");
			
			Iterator<Entity> iterator = queryResult.iterator(0, 10);//分页结果集，这里可是自定义开始的Index，和每页显示条数。
			while (iterator.hasNext()) {
				Entity fruit = iterator.next();
				if (fruit != null) {
					System.out.println(fruit.getId() + " " + fruit.getName() + " " + fruit.getPrice());
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
	 * 检查结果集与期望值是否一致
	 * @param obj
	 */
	private void checkResult(Entity ... entitys){
		Entity[] actuals = getAll();
		assertEquals(entitys == null, actuals == null);
		
		if (entitys == null) {
			return;
		}
		
		assertEquals(entitys.length, actuals.length);
		
		for (int i = 0; i < entitys.length; i++) {
			assertEquals(entitys[i].getId(), actuals[i].getId());
			assertEquals(entitys[i].getName(), actuals[i].getName());
			assertEquals(entitys[i].getPrice(), actuals[i].getPrice());
			
			//两个时间差别小于1秒视为正确
			assertEquals(entitys[i].getSaleDay() == null, actuals[i].getSaleDay() == null);
			if (entitys[i].getSaleDay() != null) {
				assertTrue(Math.abs(entitys[i].getSaleDay().getTime() - actuals[i].getSaleDay().getTime()) <= 1000);
			}
		}
	}
}
