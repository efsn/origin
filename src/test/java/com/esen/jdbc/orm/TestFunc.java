package com.esen.jdbc.orm;

import com.esen.jdbc.ConnectFactoryManagerImpl;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

/**
 * 
 *
 * @author wang
 */
public class TestFunc {
	public static Session buildSessionFactory(EntityInfoManager entityManager) {
//		EntityInfoManager entityManager = EntityInfoManagerFactory.buildFromFile("src/com/esen/jdbc/orm/Example/fruit_mapping.xml");
//		String driver = "oracle.jdbc.driver.OracleDriver";
//		String url = "jdbc:oracle:thin:@172.21.1.6:1521:WSZDB";
//		String user = "wsz";
//		String pwd = "wszbi";
				
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://127.0.0.1:3306/test_orm?useUnicode=true&characterEncoding=UTF8";
		String user = "root";
		String pwd = "abcd";

		SimpleConnectionFactory fct = new SimpleConnectionFactory(driver, url, user, pwd, "fatal");
		
		ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
		fm.setConnectionFactory("defaultDataSource", fct);
		DefaultConnectionFactory.set(fm);
		
		SessionFactory sessionFactory = SessionFactoryBuilder.build("defaultDataSource", entityManager);
		return sessionFactory.openSession();		
	}
	
	
	public static Session buildSessionFactory() {
		EntityInfoManager entityManager = EntityInfoManagerFactory.buildFromFile("src/com/esen/jdbc/orm/Example/fruit_mapping.xml");
		return buildSessionFactory(entityManager);
	}
	
	public static void main(String[] args){
		if(buildSessionFactory()==buildSessionFactory())
			System.out.println("==");
	}
}
