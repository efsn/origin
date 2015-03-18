package com.esen.jdbc.orm.helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.esen.jdbc.orm.Entity;
import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.EntityInfoManager;
import com.esen.jdbc.orm.EntityInfoManagerFactory;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.TestFunc;
import com.esen.util.FileFunc;
import com.esen.util.exp.Expression;

import junit.framework.TestCase;

public class TestEntityAdvFunc2 extends TestCase {
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
				
	public TestEntityAdvFunc2() {

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
	 * 测试导出数据
	 */
	public void testExportData() {
		initData();
		
		session = TestFunc.buildSessionFactory(entityManager);
		File file;
		try {
			file = FileFunc.createTempFileObj(null, "aaa.csv", false, true);
			FileWriter out = new FileWriter(file);
			ExportDataHelper helper = new ExportDataHelper();
			helper.setExp(new Expression("id < ? and saleDay is not null"));
			helper.setOrderbyProperties("id=true,name=false");
			helper.setParams(new Object[]{10});
			
			EntityDataManager.exportDataToCSV(session, entityInfo, out, helper);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		session.close();
	    
		
	}
	
	/**
	 * 测试导入数据
	 */
	public void testImportData() {
		initData();
		
		
		try {
			session = TestFunc.buildSessionFactory(entityManager);
			
			
			InputStream in = getClass().getResourceAsStream("aaa.csv");
			try {
	//			EntityAdvFunc.importDataFromCSV(session, entityInfo, new InputStreamReader(in), EntityAdvFunc.OPT_CLEARTABLE);
				ImportDataHelper imhelper = new ImportDataHelper();
				Map map = new HashMap();
				map.put("ID_", 0);
				map.put("NAME_", 1);
				imhelper.setNameMapping(map);
				
				imhelper.setOption(ImportDataHelper.OPT_UPDATE);
				
				imhelper.addDefaultValue("NAME_", "XXXXXX");
				imhelper.addDefaultValue("PRICE_", "2");
				
				//imhelper = null;
				
				EntityDataManager.importDataFromCSV(session, entityInfo, imhelper, new InputStreamReader(in));
			} finally {
			
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} finally {
			session.close();
		}
	}
}
