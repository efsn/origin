package com.esen.jdbc.orm.helper.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import com.esen.jdbc.orm.EntityInfoManager;
import com.esen.jdbc.orm.EntityInfoManagerFactory;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.Org;
import com.esen.jdbc.orm.Query;
import com.esen.jdbc.orm.QueryResult;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.TestFunc;
import com.esen.jdbc.orm.helper.EntityAdvFunc;
import com.esen.jdbc.orm.helper.HelperFactory;
import com.esen.jdbc.orm.helper.SCEntityInfo;
import com.esen.jdbc.orm.helper.SCHelper;

public class TestSCHelperImpl extends TestCase {
	private static Session session;
	
	private String entityName = "orm.Org";
		
	private static EntityInfoManager entityManager = 
			EntityInfoManagerFactory.buildFromFile("test/com/esen/jdbc/orm/org_mapping.xml");

	public TestSCHelperImpl() {
		
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
			
			Calendar createDate = Calendar.getInstance();
			createDate.set(2010, 1, 1);
			Calendar maxDate = Calendar.getInstance();
			maxDate.set(2099, 12, 31, 23, 59, 59);
			
			Org obj1 = new Org("10001", "xx市第一医院", createDate, createDate, maxDate);
			Org obj2 = new Org("10002", "xx市第二医院", createDate, createDate, maxDate);
			
			SCHelper scHelper = HelperFactory.getSCHelper(session, (SCEntityInfo) entityManager.getEntity(entityName));
			scHelper.add(obj1);
			scHelper.add(obj2);
			
			try {
				createDate.set(2010, 2, 1);
				Org obj3 = new Org("10002", "xx市第二医院", createDate, createDate, maxDate);
				scHelper.add(obj3);
				fail();
			} catch (ORMException e) {
				;
			}
			
			session.commit();
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
						
			SCHelper scHelper = HelperFactory.getSCHelper(session, (SCEntityInfo) entityManager.getEntity(entityName));
			System.out.println("测试开始前");
			assertEquals(2, printAllData(session, entityName));
			
			scHelper.delete("10002", false);
			System.out.println("删除以后");
			assertEquals(1, printAllData(session, entityName));
			
			scHelper.delete("10001", false);
			System.out.println("删除以后");
			assertEquals(0, printAllData(session, entityName));
			
			session.commit();
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			session.rollback();
			fail();
		} finally {
			session.close();
		}
	}
	
	/*
	 * http://192.168.1.200/wiki/pages/viewpage.action?pageId=176259189
	 * 对应的几种情况的测试
	 */
	
	/*
	 * A，B
	 * 时间1-->时间2
	 * 
	 * 时间1-->时间3
	 * 时间3-->时间2
	 */
	public void testUpdateAB() {
		System.out.println("\n=====================testUpdateAB=========================");
		
		testReCreateTable();
		
		Calendar createDate = Calendar.getInstance();
		Calendar maxDate = Calendar.getInstance();
		
		session = TestFunc.buildSessionFactory(entityManager);
		SCHelper scHelper = HelperFactory.getSCHelper(session, (SCEntityInfo) entityManager.getEntity(entityName));

		createDate.set(2010, 0, 1);
		maxDate.set(2099, 11, 31);
		Org obj1 = new Org("10001", "xx市第一医院", createDate, createDate, maxDate);		
		scHelper.add(obj1);
		
		System.out.println("测试开始前：");
		assertEquals(1, printAllData(session, entityName));

		obj1.setName("aaaaaaaa");
		createDate.set(2010, 9, 1);
		obj1.setFromDate(createDate);
		scHelper.update(obj1);
		
		System.out.println("测试完成后：");
		assertEquals(2, printAllData(session, entityName));

		session.close();
	}

	/*
	 * C
	 * 时间1-->时间2
	 * 
	 * 时间1-->时间3
	 * 时间3-->时间4
	 * 时间4-->时间2
	 */
	public void testUpdateC() {
		System.out.println("\n=====================testUpdateC=========================");
		
		testReCreateTable();

		Calendar createDate = Calendar.getInstance();
		Calendar maxDate = Calendar.getInstance();
		
		session = TestFunc.buildSessionFactory(entityManager);
		SCHelper scHelper = HelperFactory.getSCHelper(session, (SCEntityInfo) entityManager.getEntity(entityName));
	
		createDate.set(2010, 1, 1);
		maxDate.set(2099, 12, 31);
		Org obj2 = new Org("10001", "xx市第一医院", createDate, createDate, maxDate);		
		scHelper.add(obj2);

		System.out.println("测试开始前：");
		assertEquals(1, printAllData(session, entityName));

		obj2.setName("aaaaaaaa");
		createDate.set(2010, 5, 1);
		obj2.setFromDate(createDate);
		maxDate.set(2010, 10, 1);
		obj2.setToDate(maxDate);	
		scHelper.update(obj2);
		
		System.out.println("测试完成后：(期望 3 行数据)");
		assertEquals(3, printAllData(session, entityName));

		session.close();
	}
	
	/*
	 * D
	 * 时间1-->时间2
	 * 时间2-->时间3
	 * 时间3-->时间4
	 * 时间4-->时间5
	 * 
	 * 时间1-->时间2
	 * 时间2-->时间5
	 */
	public void testUpdateD() {
		System.out.println("\n=====================testUpdateD=========================");
		
		testReCreateTable();

		Calendar createDate = Calendar.getInstance();
		Calendar maxDate = Calendar.getInstance();
		
		session = TestFunc.buildSessionFactory(entityManager);
		SCHelper scHelper = HelperFactory.getSCHelper(session, (SCEntityInfo) entityManager.getEntity(entityName));
		
		createDate.set(2010, 1, 1);
		maxDate.set(2099, 12, 31);
		Org obj3 = new Org("10001", "xx市第一医院1", createDate, createDate, maxDate);
		scHelper.add(obj3);
		
		//利用情形 A 创建4条记录
		obj3.setName("xx市第一医院2");
		createDate.set(2010, 3, 2);
		obj3.setFromDate(createDate);
		scHelper.update(obj3);
		
		obj3.setName("xx市第一医院3");
		createDate.set(2010, 5, 2);
		obj3.setFromDate(createDate);
		scHelper.update(obj3);
		
		obj3.setName("xx市第一医院4");
		createDate.set(2010, 10, 2);
		obj3.setFromDate(createDate);
		scHelper.update(obj3);
		
		System.out.println("测试开始前：(期望 4 行数据)");
		assertEquals(4, printAllData(session, entityName));

		//更新
		createDate.set(2010, 3, 2);
		obj3.setName("aaaaaaaa");
		obj3.setFromDate(createDate);
		scHelper.update(obj3);

		System.out.println("测试完成后：(期望 2 行数据)");
		assertEquals(2, printAllData(session, entityName));

		session.close();
	}
	
	/*
	 * E
	 * 时间1-->时间2
	 * 时间2-->时间3
	 * 时间3-->时间4
	 * 时间4-->时间5
	 * 
	 * 时间1-->时间2
	 * 时间2-->时间4
	 * 时间4-->时间5
	 */
	public void testUpdateE() {
		System.out.println("\n=====================testUpdateE=========================");
		
		testReCreateTable();

		Calendar createDate = Calendar.getInstance();
		Calendar maxDate = Calendar.getInstance();
		
		session = TestFunc.buildSessionFactory(entityManager);
		SCHelper scHelper = HelperFactory.getSCHelper(session, (SCEntityInfo) entityManager.getEntity(entityName));

		createDate.set(2010, 1, 1);
		maxDate.set(2099, 12, 31);
		Org obj4 = new Org("10001", "xx市第一医院1", createDate, createDate, maxDate);
		scHelper.add(obj4);
		
		//利用情形 A 创建4条记录
		obj4.setName("xx市第一医院2");
		createDate.set(2010, 3, 2);
		obj4.setFromDate(createDate);
		scHelper.update(obj4);
		
		obj4.setName("xx市第一医院3");
		createDate.set(2010, 5, 2);
		obj4.setFromDate(createDate);
		scHelper.update(obj4);
		
		obj4.setName("xx市第一医院4");
		createDate.set(2010, 10, 2);
		obj4.setFromDate(createDate);
		scHelper.update(obj4);
		
		System.out.println("测试开始前：(期望 4 行数据)");
		assertEquals(4, printAllData(session, entityName));

		//更新
		obj4.setName("aaaaaaaa");
		createDate.set(2010, 3, 2);
		obj4.setFromDate(createDate);
		maxDate.set(2010, 10, 2);
		obj4.setToDate(maxDate);
		scHelper.update(obj4);
		
		System.out.println("测试完成后：(期望 3 行数据)");
		assertEquals(3, printAllData(session, entityName));

		session.close();
	}
		
	/*
	 * F
	 * 时间1-->时间2
	 * 时间2-->时间3
	 * 时间3-->时间4
	 * 时间4-->时间5
	 * 
	 * 时间1-->时间2
	 * 时间2-->时间2+
	 * 时间2+-->时间4
	 * 时间4-->时间5
	 */
	public void testUpdateF() {
		System.out.println("\n=====================testUpdateF=========================");
		
		testReCreateTable();

		Calendar createDate = Calendar.getInstance();
		Calendar maxDate = Calendar.getInstance();
		
		session = TestFunc.buildSessionFactory(entityManager);
		SCHelper scHelper = HelperFactory.getSCHelper(session, (SCEntityInfo) entityManager.getEntity(entityName));

		createDate.set(2010, 1, 1);
		maxDate.set(2099, 12, 31);
		Org obj5 = new Org("10001", "xx市第一医院1", createDate, createDate, maxDate);
		scHelper.add(obj5);
		
		//利用情形 A 创建4条记录
		obj5.setName("xx市第一医院2");
		createDate.set(2010, 3, 2);
		obj5.setFromDate(createDate);
		scHelper.update(obj5);
		
		obj5.setName("xx市第一医院3");
		createDate.set(2010, 5, 2);
		obj5.setFromDate(createDate);
		scHelper.update(obj5);
		
		obj5.setName("xx市第一医院4");
		createDate.set(2010, 10, 2);
		obj5.setFromDate(createDate);
		scHelper.update(obj5);
		
		System.out.println("测试开始前：");
		assertEquals(4, printAllData(session, entityName));

		//更新
		obj5.setName("aaaaaaaa");
		createDate.set(2010, 3, 2);
		obj5.setFromDate(createDate);
		maxDate.set(2010, 6, 2);
		obj5.setToDate(maxDate);
		scHelper.update(obj5);
		
		System.out.println("测试完成后：");
		assertEquals(4, printAllData(session, entityName));
		session.close();
	}
	
	
	public int printAllData(Session session, String entityName) {
		try {
			Query query = session.createQuery(Org.class, entityName);
			QueryResult result = query.list();

			int rows = result.calcTotalCount();
			List rowset = result.list(-1, 0);

			System.out.println("数据查询行数。rows = " + rows + "  size=" + rowset.size());
			assertEquals(rows, rowset.size());
			
			for (int i = 0; i < rows; i++) {
				Org obj = (Org) rowset.get(i);
				System.out.print("id=" + obj.getOrgId() + " name=" + obj.getName());
				System.out.print(" fromDate=" + calendar2Str(obj.getFromDate()));
				System.out.print(" toDate=" + calendar2Str(obj.getToDate()));
				
				System.out.println(" ");
			}
			return rows;
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			throw new RuntimeException("查询记录失败", sqlex);
		}
	}
	
	//测试更新时需要更新 id 的情况
	public void testUpdateId() {
		System.out.println("\n=====================testUpdateID=========================");
		
		testReCreateTable();

		Calendar createDate = Calendar.getInstance();
		Calendar maxDate = Calendar.getInstance();
		
		session = TestFunc.buildSessionFactory(entityManager);
		SCHelper scHelper = HelperFactory.getSCHelper(session, (SCEntityInfo) entityManager.getEntity(entityName));
	
		createDate.set(2010, 1, 1);
		maxDate.set(2099, 12, 31);
		Org obj2 = new Org("10001", "xx市第一医院", createDate, createDate, maxDate);		
		scHelper.add(obj2);

		System.out.println("测试开始前：(期望 1 行数据)");
		assertEquals(1, printAllData(session, entityName));

		createDate.set(2010, 5, 1);
		maxDate.set(2010, 10, 1);
		Org obj = new Org("100000001", "xx市第一医院修改信息和id", createDate, createDate, maxDate);		
		scHelper.update("10001", obj);
		
		System.out.println("测试完成后：(期望 3 行数据)");
		assertEquals(3, printAllData(session, entityName));

		session.close();
	}
	
	public void testGet() {
		System.out.println("\n=====================testGet=========================");
		
		testReCreateTable();

		Calendar createDate = Calendar.getInstance();
		Calendar maxDate = Calendar.getInstance();
		
		session = TestFunc.buildSessionFactory(entityManager);
		SCHelper scHelper = HelperFactory.getSCHelper(session, (SCEntityInfo) entityManager.getEntity(entityName));

		createDate.set(2010, 1, 1);
		maxDate.set(2099, 12, 31);
		Org obj5 = new Org("10001", "xx市第一医院1", createDate, createDate, maxDate);
		scHelper.add(obj5);
		
		obj5.setName("xx市第一医院2");
		createDate.set(2010, 3, 2);
		obj5.setFromDate(createDate);
		scHelper.update(obj5);
		
		obj5.setName("xx市第一医院3");
		createDate.set(2010, 5, 2);
		obj5.setFromDate(createDate);
		scHelper.update(obj5);
		
		obj5.setName("xx市第一医院4");
		createDate.set(2010, 10, 2);
		obj5.setFromDate(createDate);
		scHelper.update(obj5);
		
		System.out.println("期望 4 行数据");
		assertEquals(4, printAllData(session, entityName));

		//测试获取全部
		QueryResult result = scHelper.getAll(new Date().getTime());

		int rows = result.calcTotalCount();
		System.out.println("共有 " + rows + " 行数据" + "(期望最后一行)");

		List rowset = result.list(-1, 0);
		
		System.out.println("数据查询行数。rows = " + rows + "  size=" + rowset.size());
		assertEquals(rows, rowset.size());
		
		for (int i = 0; i < rows; i++) {
			Org obj = (Org) rowset.get(i);
			System.out.print("id=" + obj.getOrgId() + " name=" + obj.getName());
			System.out.print(" fromDate=" + calendar2Str(obj.getFromDate()));
			System.out.print(" toDate=" + calendar2Str(obj.getToDate()));
			
			System.out.println(" ");
		}
		
		System.out.println("测试获取指定 id 的数据, 不存在");
		Org obj = (Org) scHelper.get("10002", new Date().getTime());
		if (obj != null) {
			System.out.println("错误, 期望不存在");
			fail();
		} else {
			System.out.println("正确");
		}
		
		System.out.println("测试获取指定 id 的数据， 存在");
		obj = (Org) scHelper.get("10001", new Date().getTime());
		if (obj == null) {
			System.out.println("错误, 期望为 null");
			fail();
		}
		System.out.print("id=" + obj.getOrgId() + " name=" + obj.getName());
		System.out.print(" fromDate=" + calendar2Str(obj.getFromDate()));
		System.out.print(" toDate=" + calendar2Str(obj.getToDate()));
	
		
		result = scHelper.get("10001");
		System.out.println(result.calcTotalCount());
		session.close();
	}
	
	private String calendar2Str(Calendar date) {
		if (date == null) {
			return "null";
		}
		return date.get(Calendar.YEAR) + "." + (date.get(Calendar.MONTH) + 1) + "." + date.get(Calendar.DATE) 
				+ " " + date.get(Calendar.HOUR) + ":" + date.get(Calendar.MINUTE) + ":" + date.get(Calendar.SECOND);
	}
	
	public void testFromDateNull() {
		System.out.println("\n=====================testFromDateNull=========================");
		
		testReCreateTable();
		
		Calendar createDate = Calendar.getInstance();
		Calendar maxDate = Calendar.getInstance();
		
		session = TestFunc.buildSessionFactory(entityManager);
		SCHelper scHelper = HelperFactory.getSCHelper(session, (SCEntityInfo) entityManager.getEntity(entityName));

		createDate.set(2010, 0, 1);
		maxDate.set(2099, 11, 31);
		Org obj = new Org("10001", "xx市第一医院", createDate, createDate, null);		
		scHelper.add(obj);
		
		System.out.println("测试开始前：(期望 1 行数据)");
		assertEquals(1, printAllData(session, entityName));

		Org obj1 = new Org("10001", "aaaaaaa", Calendar.getInstance(), null, null);
		scHelper.update(obj1);
		
		System.out.println("测试完成后：(期望 2 行数据)");
		assertEquals(2, printAllData(session, entityName));

		session.close();
	}
	
}
