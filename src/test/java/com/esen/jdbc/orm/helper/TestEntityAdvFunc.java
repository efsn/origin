package com.esen.jdbc.orm.helper;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.EntityInfoManager;
import com.esen.jdbc.orm.EntityInfoManagerFactory;
import com.esen.jdbc.orm.Fruit;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.TestFunc;

import junit.framework.TestCase;

/**
 * 测试EntityAdvFunc类
 * 
 * @author liujin
 */
public class TestEntityAdvFunc extends TestCase {
	private Session session;
	
	private String filePath = "test/com/esen/jdbc/orm/fruit_mapping_right.xml";
	private String filePath2 = "test/com/esen/jdbc/orm/fruit_mapping_modify_forrepair.xml";
	private String filePath3 = "test/com/esen/jdbc/orm/fruit_mapping_modify_forupgrade.xml";
	
	private String entityName = "orm.Fruit";
	private String entityName2 = "orm.FruitModify";
	
	public TestEntityAdvFunc() {

	}

	/**
	 * 初始化测试环境，没有该表
	 */
	private void init() {
		try {
			EntityInfoManager entityManager = EntityInfoManagerFactory.buildFromFile(filePath, filePath3);

			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();		

			//删除
			EntityAdvFunc.dropTable(session, entityName);
			session.commit();
			
			EntityAdvFunc.dropTable(session, entityName2);
			session.commit();
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			session.rollback();
			fail();
		} finally {
			session.close();
		}
	}

	public void testDropTable() {
		init();
		
		try {
			EntityInfoManager entityManager = EntityInfoManagerFactory.buildFromFile(filePath);
			EntityInfo entityInfo = entityManager.getEntity(entityName);

			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();		

			//表不存在，删除成功
			EntityAdvFunc.dropTable(session, entityName);
			session.commit();
			
			//表存在，删除成功
			EntityAdvFunc.createTable(session, entityInfo);
			session.commit();
			EntityAdvFunc.dropTable(session, entityName);
			session.commit();
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			session.rollback();
			fail();
		} finally {
			session.close();
		}
	}

	public void testCreateTable() {
		init();

		try {
			EntityInfoManager entityManager = EntityInfoManagerFactory.buildFromFile(filePath);
			EntityInfo entityInfo = entityManager.getEntity(entityName);

			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();

			//表不存在，创建成功
			EntityAdvFunc.createTable(session, entityInfo);
			session.commit();
			
			//表存在，创建失败
			try {
				EntityAdvFunc.createTable(session, entityInfo);
				fail();
			} catch (ORMException e) {
				session.rollback();
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
	
	public void testRepairTable() {
		init();

		try {
			EntityInfoManager entityManager = EntityInfoManagerFactory.buildFromFile(filePath);

			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
			
			// 表不存在，可以创建表
			EntityAdvFunc.repairTable(session, entityName, null);
			session.commit();
			
			//表已经存在，结构相同
			EntityAdvFunc.repairTable(session, entityName, null);
			session.commit();
			
			//检查表结构
			//列名和长度
			String[][] columns = {
					{"ID_", "20"},
					{"NAME_", "100"},
					{"PRICE_", "10"},
					{"PRODUCEAREA_", "100"},
					{"INTRODUCTION_", "200"},
					{"SALEDAY_", null},
					{"BTYPE_", "1"},
					{"PARENT_", "20"},
					{"UPID0", "20"},
					{"UPID1", "20"},
					{"UPID2", "20"},
					{"UPID3", "20"},
					{"UPID4", "20"},
					{"UPID5", "20"},
					{"UPID6", "20"},
					{"UPID7", "20"},
					{"FROMDATE_", null},
					{"TODATE_", null},
			};
			
			Connection conn = session.getConnection();
			DatabaseMetaData metaData = conn.getMetaData();
			ResultSet rs = metaData.getColumns(null, null, "ES_FRUIT", null);
			int i = 0;
			for (i = 0; i < columns.length; i++) {
				assertTrue(rs.next());
				assertEquals(columns[i][0], rs.getString("COLUMN_NAME"));
				assertEquals(columns[i][1], rs.getString("COLUMN_SIZE"));
				
				if (columns[i][0].compareTo("UPID4") == 0) {
					assertTrue(rs.getBoolean("IS_NULLABLE"));
				}
				
				if (columns[i][0].compareTo("UPID5") == 0) {
					assertEquals(Types.VARCHAR, rs.getInt("DATA_TYPE"));
				}
			}
			assertFalse(rs.next());
			
			rs.close();
			conn.close();	
			
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			session.rollback();
			fail();
		} finally {
			session.close();
		}
		
		try {
			EntityInfoManager entityManager = EntityInfoManagerFactory.buildFromFile(filePath2);

			session = TestFunc.buildSessionFactory(entityManager);
			session.beginTransaction();
						
			//表已经存在，结构发生变化
			EntityAdvFunc.repairTable(session, entityName, null);
			session.commit();
			
			//检查表结构
			//列名和长度
			String[][] columns = {
					{"ID_", "20"},
					{"NAME_", "100"},
					{"PRICE_", "10"},
					{"PRODUCEAREA_", "100"},
					{"INTRODUCTION_", "200"},
					{"SALEDAY_", null},
//					{"BTYPE_", "1"},
//					{"PARENT_", "20"},
//					{"UPID0", "20"},
//					{"UPID1", "20"},
//					{"UPID2", "20"},
					{"UPID3", "180"},
					{"UPID4", "20"},
					{"UPID5", "20"},
					{"UPID6", "20"},
					{"UPID7", "20"},
					{"FROMDATE_", null},
					{"TODATE_", null},
					{"ADD1_", "20"},
					{"ADD2_", "20"},
			};
			//检查是否是新结构
			Connection conn = session.getConnection();
			DatabaseMetaData metaData = conn.getMetaData();
			ResultSet rs = metaData.getColumns(null, null, "ES_FRUIT", null);
			int i = 0;
			for (i = 0; i < columns.length; i++) {
				assertTrue(rs.next());
				assertEquals(columns[i][0], rs.getString("COLUMN_NAME"));
				assertEquals(columns[i][1], rs.getString("COLUMN_SIZE"));
				if (columns[i][0].compareTo("UPID4") == 0) {
					assertTrue(rs.getBoolean("IS_NULLABLE"));
				}
				
				if (columns[i][0].compareTo("UPID5") == 0) {
					int type = rs.getInt("DATA_TYPE");
					assertTrue(Types.DECIMAL == type
								|| Types.NUMERIC == type);
				}
			}
			assertFalse(rs.next());
			rs.close();
			//conn.close();	
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			session.rollback();
			fail();
		} finally {
			session.close();
		}
	}
	
	public void testUpgradeTable() {
		init();
		
		//创建表
		try {
			EntityInfoManager entityManager = EntityInfoManagerFactory.buildFromFile(filePath);
			EntityInfo entityInfo = entityManager.getEntity(entityName);
			
			session = TestFunc.buildSessionFactory(entityManager);			
			session.beginTransaction();
			EntityAdvFunc.createTable(session, entityInfo);
			session.commit();
			
			//插入部分数据，以便检查字段名改变后数据是否丢失
			Fruit obj = new Fruit("10001", "苹果", 10, "武汉", "introduction", new Date());
			Fruit obj2 = new Fruit("10002", "梨子", 8.5, "烟台", "introductionintroduction", new Date());
			session.add(entityName, obj);
			session.add(entityName, obj2);
			session.commit();
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			session.rollback();
			fail();
		} finally {
			session.close();
		}
		
		//升级表结构
		try {
			EntityInfoManager entityManager = EntityInfoManagerFactory.buildFromFile(filePath, filePath3);
			EntityInfo newEntityInfo = entityManager.getEntity("orm.FruitModify");
			
			session = TestFunc.buildSessionFactory(entityManager);
			
			Map<String, String> nameMapping = new HashMap<String, String>();
			nameMapping.put("name", "nameNew");
			nameMapping.put("produceArea", "area");
			
			session.beginTransaction();
			EntityAdvFunc.upgradeTable(session, entityName, newEntityInfo, nameMapping, null);
			session.commit();
			
			//检查修改后的表结构和数据
			//检查表结构
			//列名和长度
			String[][] columns = {
					{"ID_", "20"},
					{"NAME_", "400"},
					{"PRICE_", "20"},
					{"AREA_", "100"},
					{"SALEDAY_", null},
					{"ADDPROP_", "20"},
			};
			//检查是否是新结构
			Connection conn = session.getConnection();
			DatabaseMetaData metaData = conn.getMetaData();
			
			//旧表不存在
			ResultSet rs = metaData.getColumns(null, null, "ES_FRUIT", null);
			assertFalse(rs.next());
			rs.close();
			
			//新表的信息
			rs = metaData.getColumns(null, null, "ES_FRUIT_MODIFY", null);
			int i = 0;
			for (i = 0; i < columns.length; i++) {
				assertTrue(rs.next());
				assertEquals(columns[i][0], rs.getString("COLUMN_NAME"));
				if (rs.getInt("DATA_TYPE") == Types.VARCHAR) {
					assertEquals(columns[i][1], rs.getString("COLUMN_SIZE"));
				}
				if (columns[i][0].compareTo("ID_") == 0) {
					int type = rs.getInt("DATA_TYPE");
					assertTrue(Types.INTEGER == type
							|| Types.DECIMAL == type
							|| Types.NUMERIC == type);
				}
				if (columns[i][0].compareTo("PRICE_") == 0) {
					assertEquals(Types.VARCHAR, rs.getInt("DATA_TYPE"));
				}
			}
			assertFalse(rs.next());
			rs.close();
			conn.close();	
		} catch (Exception sqlex) {
			sqlex.printStackTrace();
			session.rollback();
			fail();
		} finally {
			session.close();
		}
	}

}
