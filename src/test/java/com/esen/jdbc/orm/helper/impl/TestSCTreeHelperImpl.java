package com.esen.jdbc.orm.helper.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import com.esen.jdbc.orm.Area;
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
import com.esen.jdbc.orm.helper.SCTreeEntityInfo;
import com.esen.jdbc.orm.helper.SCTreeHelper;

public class TestSCTreeHelperImpl extends TestCase {
	private Session session;
	
	private String entityName = "orm.Area";
		
	private static EntityInfoManager entityManager = 
			EntityInfoManagerFactory.buildFromFile("test/com/esen/jdbc/orm/area_mapping.xml");

	public TestSCTreeHelperImpl() {
		
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
	
//scTreeHelper.add(T);
//scTreeHelper.update(T);
//scTreeHelper.update(oldIdValue, T);
	public void testAddData() {
		try {
			testReCreateTable();
	
			try {
				session = TestFunc.buildSessionFactory(entityManager);
				session.beginTransaction();
				SCTreeEntityInfo treeEntity = (SCTreeEntityInfo) entityManager.getEntity(entityName);
				
				Calendar createDateTmp = Calendar.getInstance();
				createDateTmp.set(2010, 1, 1);
				Date createDate = createDateTmp.getTime();
				
				Calendar maxDate = Calendar.getInstance();
				maxDate.set(2099, 12, 31, 23, 59, 59);
				Date toDate = maxDate.getTime();
	
				Area obj1 = new Area("000000", "中国", createDate, toDate, "--", false);
				Area obj2 = new Area("100000", "北京市", createDate, toDate, "000000", false);
				obj2.setUpids("000000");
				
				Area obj3 = new Area("101000", "海淀区", createDate, toDate, "100000", true);
				obj3.setUpids("000000", "100000");
				
				Area obj4 = new Area("102000", "东城区", createDate, toDate, "100000", true);
				obj4.setUpids("000000", "100000");
				
				Area obj5 = new Area("420000", "湖北", createDate, toDate, "000000", false);
				obj5.setUpids(obj5, treeEntity, "000000");
				
				Area obj6 = new Area("421000", "武汉", createDate, toDate, "420000", false);
				obj6.setUpids("000000", "420000");
				
				Area obj7 = new Area("421010", "江汉区", createDate, toDate, "421000", true);
				obj7.setUpids("000000", "420000", "421000");
				
				Area obj8 = new Area("421020", "洪山区", createDate, toDate, "421000", true);
				obj8.setUpids("000000", "420000", "421000");
				
				Area obj9 = new Area("422000", "黄石市", createDate, toDate, "420000", true);
				obj9.setUpids("000000", "420000");
				
				session.add(entityName, obj1);
				session.add(entityName, obj2);
				session.add(entityName, obj3);
				session.add(entityName, obj4);
				session.add(entityName, obj5);
				session.add(entityName, obj6);
				session.add(entityName, obj7);
				session.add(entityName, obj8);
				session.add(entityName, obj9);
				
				session.commit();
				
				assertEquals(9, printAllData(session, entityName));
				
				SCTreeHelper scTreeHelper = HelperFactory.getSCTreeHelper(session, treeEntity);
				obj9.setAreaId("423000");
				obj9.setName("宜昌市");
				scTreeHelper.add(obj9);
				
				createDateTmp.set(2012, 9, 1);
				createDate = createDateTmp.getTime();
				Area obj10 = new Area("422010", "黄石市XX区", createDate, toDate, "422000", true);
				obj10.setUpids("000000", "420000", "422000");
				scTreeHelper.add(obj10);
				
				session.commit();		
	
				//重复插入同样id的节点，报错
				try {
					scTreeHelper.add(obj10);
					fail();
				} catch(ORMException e) {
					;
				}
				
				//更新湖北这个节点
				//该节点一条记录会成为三条记录
				obj5.setName("湖北湖北修改");
				createDateTmp.set(2010, 9, 1);
				createDate = createDateTmp.getTime();
				obj5.setCreateDate(createDate);
				maxDate.set(2013, 1, 1);
				toDate = maxDate.getTime();
				obj5.setExpiredDate(toDate);
				scTreeHelper.update(obj5);			
				assertEquals(13, printAllData(session, entityName));
				
				//更新海淀这个节点
				//该节点一条记录会成为两条记录
				obj3.setName("海淀海淀修改");
				createDateTmp.set(2010, 9, 1);
				createDate = createDateTmp.getTime();
				obj3.setCreateDate(createDate);
				scTreeHelper.update(obj3);			
				assertEquals(14, printAllData(session, entityName));
				
				//修改对象id
				//目前暂不支持（）
				try {
					scTreeHelper.update("111111", obj3);
					fail();
				} catch(Exception e) {
					;
				}
			} finally {
				session.close();
			}
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			session.rollback();
			fail();
		}
	}
	
	public int printAllData(Session session, String entityName) {
		try {
			Query query = session.createQuery(City.class, entityName);
			QueryResult result = query.list();

			int rows = result.calcTotalCount();
			System.out.println("共有 " + rows + " 行数据");

			List rowset = result.list(-1, 0);
			System.out.println("数据查询行数。count = " + rows + "  size=" + rowset.size());
			assertEquals(rowset.size(), rows);

			for (int i = 0; i < rows; i++) {
				Area obj = (Area) rowset.get(i);
				System.out.print("id=" + obj.getAreaId() + " name=" + obj.getName() + " parent=" + obj.getParent());
				System.out.print(" btype=" + obj.getBtype() + " upids=");
				Object[] upids = obj.getUpids();
				for (int j = 0; upids != null && j < upids.length; j++) {
					System.out.print(upids[j] + " ");
				}
				System.out.print(" fromDate=" + date2Str(obj.getCreateDate()));
				System.out.print(" toDate=" + date2Str(obj.getExpiredDate()));
				System.out.println(" ");
			}
			return rows;
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			throw new RuntimeException("查询记录失败", sqlex);
		}
	}
		
//scTreeHelper.delete(idValue);
//scTreeHelper.delete(idValue, stretchUp);
	public void testDelete() {
		System.out.println("\n=====================testDelete=========================");
		
		try {
			testAddData();
			
			session = TestFunc.buildSessionFactory(entityManager);
			SCTreeHelper scTreeHelper = HelperFactory.getSCTreeHelper(session, (SCTreeEntityInfo) entityManager.getEntity(entityName));
	
			//删除非叶子结点，报错
			try {
				scTreeHelper.delete("420000");
				fail();
			} catch (ORMException e) {
				;
			}
			
			try {
				scTreeHelper.delete("420000", false);
				fail();
			} catch (ORMException e) {
				;
			}
			
			//删除叶子结点
			scTreeHelper.delete("422010");
			assertEquals(13, printAllData(session, entityName));
			
			//递归删除非叶子节点
			scTreeHelper.delete("420000", true);
			assertEquals(5, printAllData(session, entityName));
			
		} catch (Exception ee) {
			fail();
		} finally {
			session.close();
		}
		
	}

	public void testGetAll() {
		System.out.println("\n=====================testGet=========================");
		
		try {
			testAddData();
			
			session = TestFunc.buildSessionFactory(entityManager);
			SCTreeHelper scTreeHelper = HelperFactory.getSCTreeHelper(session, (SCTreeEntityInfo) entityManager.getEntity(entityName));
	
			Calendar cal = Calendar.getInstance();
			
			//无该时间点节点
			cal.set(1990, 10, 1);
			QueryResult res = scTreeHelper.getAll(cal.getTimeInMillis());
			assertEquals(0, res.calcTotalCount());
			
			//无黄石市XX区这个节点
			cal.set(2010, 10, 20);
			res = scTreeHelper.getAll(cal.getTimeInMillis());
			assertEquals(10, res.calcTotalCount());

			//所有节点
			cal.set(2015, 10, 20);
			res = scTreeHelper.getAll(cal.getTimeInMillis());
			assertEquals(11, res.calcTotalCount());

		} catch (Exception ee) {
			fail();
		} finally {
			session.close();
		}
	}
	
	public void testGet() {
		System.out.println("\n=====================testGet=========================");
		
		try {
			testAddData();
			
			session = TestFunc.buildSessionFactory(entityManager);
			SCTreeHelper scTreeHelper = HelperFactory.getSCTreeHelper(session, (SCTreeEntityInfo) entityManager.getEntity(entityName));
	
			Calendar cal = Calendar.getInstance();
			
			//无该时间点节点
			cal.set(1990, 10, 1);
			Area obj = (Area) scTreeHelper.get("420000", cal.getTimeInMillis());
			assertEquals(null, obj);
			
			//无id为"12345"这个节点
			cal.set(2010, 10, 20);
			obj = (Area) scTreeHelper.get("12345", cal.getTimeInMillis());
			assertEquals(null, obj);
			
			//存在一个节点
			obj = (Area) scTreeHelper.get("420000", cal.getTimeInMillis());
			assertEquals("湖北湖北修改", obj.getName());

		} catch (Exception ee) {
			fail();
		} finally {
			session.close();
		}
	}
	
	public void testGetRoots() {
		System.out.println("\n=====================testGetRoots=========================");
		
		try {
			testAddData();
			
			session = TestFunc.buildSessionFactory(entityManager);
			SCTreeHelper scTreeHelper = HelperFactory.getSCTreeHelper(session, (SCTreeEntityInfo) entityManager.getEntity(entityName));
	
			Calendar cal = Calendar.getInstance();
			
			//无该时间点根节点
			cal.set(1990, 10, 1);
			QueryResult res = scTreeHelper.getRoots(cal.getTimeInMillis());
			assertEquals(0, res.calcTotalCount());
			
			//一个根节点
			cal.set(2010, 10, 20);
			res = scTreeHelper.getRoots(cal.getTimeInMillis());
			assertEquals(1, res.calcTotalCount());
			Area area = (Area) res.list(-1, -1).get(0);
			assertEquals("中国", area.getName());
			
			//新增一个根节点
			area.setAreaId("AAAAAAA");
			area.setName("XXXX国");
			area.setBtype(true);
			scTreeHelper.add(area);
			
			//两个根节点
			res = scTreeHelper.getRoots(cal.getTimeInMillis());
			assertEquals(2, res.calcTotalCount());
			List rows = res.list(-1, -1);
			area = (Area) rows.get(0);
			assertEquals("中国", area.getName());
			area = (Area) rows.get(1);
			assertEquals("XXXX国", area.getName());
			
		} catch (Exception ee) {
			fail();
		} finally {
			session.close();
		}
	}
	
//scTreeHelper.listChildren(idValue, recursive, date);
	public void testListChildren() {
		System.out.println("\n=====================testListChildren=========================");
		
		try {
			testAddData();
			
			session = TestFunc.buildSessionFactory(entityManager);
			SCTreeHelper scTreeHelper = HelperFactory.getSCTreeHelper(session, (SCTreeEntityInfo) entityManager.getEntity(entityName));
	
			Calendar cal = Calendar.getInstance();
			
			//无该时间点节点
			cal.set(1990, 10, 1);
			QueryResult res = scTreeHelper.listChildren("420000", false, cal.getTimeInMillis());
			assertEquals(0, res.calcTotalCount());
			
			//不递归
			cal.set(2010, 10, 20);
			res = scTreeHelper.listChildren("420000", false, cal.getTimeInMillis());
			assertEquals(3, res.calcTotalCount());
			
			//递归
			res = scTreeHelper.listChildren("420000", true, cal.getTimeInMillis());
			assertEquals(5, res.calcTotalCount());

			//递归
			cal.set(2013, 10, 20);
			res = scTreeHelper.listChildren("420000", true, cal.getTimeInMillis());
			assertEquals(6, res.calcTotalCount());
			
			//递归所有节点
			res = scTreeHelper.listChildren("--", true, cal.getTimeInMillis());
			assertEquals(10, res.calcTotalCount());
			
		} catch (Exception ee) {
			fail();
		} finally {
			session.close();
		}	
	}
	
	private String date2Str(Date date) {
		if (date == null) {
			return "null";
		}
		return date.toGMTString();
//		return (date.getYear() + 1900) + "." + date.getMonth() + "." + date.getDate()
//				+ " " + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
	}

}