package com.esen.jdbc.orm.helper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.esen.jdbc.orm.EntityInfoManager;
import com.esen.jdbc.orm.impl.EntityInfoManagerImpl;

import junit.framework.TestCase;

/**
 * XmlWriter测试类
 *
 * @author wangshzh
 */
public class TestXmlWriter extends TestCase {
	public void testSaveToDbdfinerXml(){
		XmlWriter xmlWriter=new XmlWriter();
		EntityInfoManager entityInfoManager=new EntityInfoManagerImpl();
		try {
			xmlWriter.saveToDbdfinerXml(xmlWriter.entityToDbdfinerDoc(entityInfoManager.loadEntityFrom(new FileInputStream("test/com/esen/jdbc/orm/fruit_mapping.xml"))), new FileOutputStream("test/com/esen/jdbc/orm/fruit_dbdefiner.xml"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
