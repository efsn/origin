package com.esen.jdbc.orm.helper.impl;

import java.util.List;

import junit.framework.TestCase;

import com.esen.jdbc.orm.City;
import com.esen.jdbc.orm.EntityInfoManager;
import com.esen.jdbc.orm.EntityInfoManagerFactory;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.Query;
import com.esen.jdbc.orm.QueryResult;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.TestFunc;
import com.esen.jdbc.orm.helper.EntityAdvFunc;
import com.esen.jdbc.orm.helper.HelperFactory;
import com.esen.jdbc.orm.helper.TreeEntityInfo;
import com.esen.jdbc.orm.helper.TreeHelper;

public class TestTreeHelperImpl extends TestCase {
	private static Session session;
	
	private String entityName = "orm.City";
		
	private static EntityInfoManager entityManager = 
			EntityInfoManagerFactory.buildFromFile("test/com/esen/jdbc/orm/city_mapping.xml");

	public TestTreeHelperImpl() {
		
	}
		
	public void testDropTable() {
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();		
			EntityAdvFunc.dropTable(session, entityName);
			session.commit();
		} catch (Exception sqlex) {
			session.rollback();
			fail();
		} finally {
			session.close();
		}
	}
	
	public void testReCreateTable() {
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();		
			EntityAdvFunc.dropTable(session, entityName);
			EntityAdvFunc.createTable(session, entityManager.getEntity(entityName));
			session.commit();
		} catch (Exception sqlex) {
			session.rollback();
			fail();
		} finally {
			session.close();
		}
	}
	
	public void testAddData() {
		testReCreateTable();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
			TreeEntityInfo treeEntity = (TreeEntityInfo) entityManager.getEntity(entityName);
			
			City obj1 = new City("000000", "中国", "--", false);
			City obj2 = new City("100000", "北京市", "000000", false);
			obj2.setUpids("000000");
			
			City obj3 = new City("101000", "海淀区", "100000", true);
			obj3.setUpids("000000", "100000");
			
			City obj4 = new City("102000", "东城区", "100000", true);
			obj4.setUpids("000000", "100000");
			
			City obj5 = new City("420000", "湖北", "000000", false);
			obj5.setUpids("000000");
			
			City obj6 = new City("421000", "武汉", "420000", false);
			obj6.setUpids("000000", "420000");
			
			City obj7 = new City("421010", "江汉区", "421000", true);
			obj7.setUpids("000000", "420000", "421000");
			
			City obj8 = new City("421020", "洪山区", "421000", true);
			obj8.setUpids("000000", "420000", "421000");
			
			City obj9 = new City("422000", "黄石市", "420000", true);
			obj9.setUpids("000000", "420000");
			
			TreeHelper treeHelper = HelperFactory.getTreeHelper(session, treeEntity);
			
			treeHelper.add(obj1);
			treeHelper.add(obj2);
			treeHelper.add(obj3);
			treeHelper.add(obj4);
			treeHelper.add(obj5);
			treeHelper.add(obj6);
			treeHelper.add(obj7);
			treeHelper.add(obj8);
			treeHelper.add(obj9);
			
			session.commit();
			
			System.out.println("期望9条数据");
			printAllData(session, entityName);
			
			obj9.setCityId("423000");
			obj9.setName("宜昌市");
			treeHelper.add(obj9);
			
			City obj10 = new City("422010", "黄石市XX区", "422000", true);
			obj10.setUpids("000000", "420000", "422000");
			treeHelper.add(obj10);
			
			//插入同样id的节点，报错
			try {
				treeHelper.add(obj10);
				fail();
			} catch (ORMException e) {
				;
			}
			
			session.commit();		
			
			System.out.println("期望11条数据");
			printAllData(session, entityName);

		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			session.rollback();
			fail();
		} finally {
			session.close();
		}
	}
	
	public void testDeleteData() {
		testAddData();

		try {
			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
						
			TreeHelper treeHelper = HelperFactory.getTreeHelper(session, (TreeEntityInfo) entityManager.getEntity(entityName));
			System.out.println("期望 11  行数据");
			printAllData(session, entityName);
			
			treeHelper.delete("422010");
			System.out.println("期望 10 行数据, 且黄石市btype为 true");
			printAllData(session, entityName);
			
			treeHelper.delete("421020");
			System.out.println("期望 9 行数据，且武汉市 btype 为  false");
			printAllData(session, entityName);
			
			session.commit();
			
			//不允许删除非叶子节点1
			try {
				treeHelper.delete("000000");
				fail();
			} catch (Exception e) {
				;
			}
			
			//不允许删除非叶子节点2
			try {
				treeHelper.delete("000000", false);
				fail();
			} catch (Exception e) {
				;
			}
			
			//允许递归删除非叶子节点
			treeHelper.delete("420000", true);
			System.out.println("期望 4 行数据");
			printAllData(session, entityName);

		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			session.rollback();
			fail();
		} finally {
			session.close();
		}
	}
	
	public static void printAllData(Session session, String entityName) {
		try {
			Query query = session.createQuery(City.class, entityName);
			QueryResult result = query.list();

			int rows = result.calcTotalCount();
			System.out.println("共有 " + rows + " 行数据");

			List rowset = result.list(-1, 0);
			if (rowset.size() != rows) {
				System.out.println("数据查询错误，行数不相同。count = " + rows + "  size=" + rowset.size());
			}

			for (int i = 0; i < rows; i++) {
				City obj = (City) rowset.get(i);
				System.out.print("id=" + obj.getCityId() + " name=" + obj.getName() + " parent=" + obj.getParent());
				System.out.print(" btype=" + obj.isBtype() + " upids=");
				Object[] upids = obj.getUpids();
				for (int j = 0; upids != null && j < upids.length; j++) {
					System.out.print(upids[j] + " ");
				}
				
				System.out.println(" ");
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			throw new RuntimeException("查询记录失败", sqlex);
		}
	}
	
	public void testGetRoots() {
		System.out.println("\n=====================testGetRoots=========================");
		
		testAddData();
		
		session = TestFunc.buildSessionFactory(entityManager);
		TreeHelper treeHelper = HelperFactory.getTreeHelper(session, (TreeEntityInfo) entityManager.getEntity(entityName));
		QueryResult res = treeHelper.getRoots();
		int row = res.calcTotalCount();
		assertEquals(1, row);
		
		City obj = new City("AAAAAAAA", "XX国", "--", false);
		treeHelper.add(obj);
		row = treeHelper.getRoots().calcTotalCount();
		assertEquals(2, row);

		session.close();
	}
	
	public void testListChildren() {
		System.out.println("\n=====================testListChildren=========================");
		
		testAddData();
		
		session = TestFunc.buildSessionFactory(entityManager);
		TreeHelper treeHelper = HelperFactory.getTreeHelper(session, (TreeEntityInfo) entityManager.getEntity(entityName));
		
		//不递归
		QueryResult res = treeHelper.listChildren("420000", false);
		int row = res.calcTotalCount();
		assertEquals(3, row);

		res = treeHelper.listChildren("420000", true);
		row = res.calcTotalCount();
		assertEquals(6, row);

		//
		res = treeHelper.listChildren("--", true);
		row = res.calcTotalCount();
		assertEquals(11, row);
		
		session.close();
	}

	public void testUpdate() {
		System.out.println("\n=====================testUpdate=========================");
		
		testAddData();
		
		session = TestFunc.buildSessionFactory(entityManager);
		TreeEntityInfo treeEntity = (TreeEntityInfo) entityManager.getEntity(entityName);
		TreeHelper treeHelper = HelperFactory.getTreeHelper(session, treeEntity);
		
		//更新全部字段
		//id 不变
		City obj = new City("420000", "湖北湖北", "000000", false);
		obj.setUpids("000000");		
		treeHelper.update("420000", obj);

		//更新全部字段
		//id 改变
		City obj2 = new City("430000", "湖北湖北湖北", "000000", false);
		obj2.setUpids("000000");
		treeHelper.update("420000", obj);
		
		//更改部分字段
		City obj3 = new City("420000", "湖北湖北湖北", "000000", false);
		treeHelper.update("430000", obj3, treeEntity.getIdPropertyName());
		session.close();
	}
	
}
