package com.esen.jdbc.orm;

import java.io.InputStream;
import java.net.URL;

import org.w3c.dom.Document;

import com.esen.jdbc.orm.impl.EntityInfoManagerImpl;

/**
 * 实体对象管理器的工厂类
 *
 * @author wangshzh
 */
public abstract class EntityInfoManagerFactory {
	/**
	 * 根据mapping.xml文件创建一个EntityInfoManager
	 * 
	 * @param files
	 *            mapping文件路经,允许一个或多个
	 * @return EntityInfoManager对象
	 */
	public static EntityInfoManager buildFromFile(String... files) {
		if (files == null || files.length <= 0) {
			return null;
		}

		EntityInfoManager entityInfoManager = new EntityInfoManagerImpl();
		for (String str : files) {
			try {
				InputStream in = EntityInfoManager.class.getResourceAsStream(str);
				try {
					entityInfoManager.addEntity(entityInfoManager.loadEntityFrom(in));
				} finally {
					in.close();
				}
			} catch (Exception e) {
				throw new ORMException("com.esen.jdbc.orm.entityinfomanagerfactory.1","读取文件： {0} 出现异常！",new Object[]{str}, e);
			}
		}
		return entityInfoManager;
	}
	

	/**
	 * 根据定义的属性document对象创建EntityInfoManager
	 * 
	 * @param docs
	 *            document对象,允许一个或多个
	 * @return EntityInfoManager对象
	 */
	public static EntityInfoManager buildFromDoc(Document... docs) {
		if (docs == null || docs.length <= 0) {
			return null;
		}

		EntityInfoManager entityInfoManager = new EntityInfoManagerImpl();
		for (Document doc : docs) {
			EntityInfoParser entityInfoParser = new EntityInfoParser(doc);
			entityInfoManager.addEntity(entityInfoParser.parse());
		}
		return entityInfoManager;
	}

	/**
	 * 根据 Entity 集合来创建 EntityInfoManager
	 * 
	 * @param eneities
	 *            Eneity集合
	 * @return SessionFactory对象
	 */
	public static EntityInfoManager build(EntityInfo... entities) {
		if (entities == null || entities.length <= 0) {
			return null;
		}

		EntityInfoManager entityInfoManager = new EntityInfoManagerImpl();
		for (EntityInfo entity : entities) {
			entityInfoManager.addEntity(entity);
		}
		return entityInfoManager;
	}
	
	public static final EntityInfoManager buildFromURL(URL... urls) {
		if (urls == null || urls.length <= 0) {
			return null;
		}
		EntityInfoManager entityInfoManager = new EntityInfoManagerImpl();
		for (URL url : urls) {
			try {
				InputStream in = url.openStream();
				try {
					EntityInfo entity = entityInfoManager.loadEntityFrom(in);
					entityInfoManager.addEntity(entity);
				} finally {
					in.close();
				}
			} catch (Exception ex) {
				throw new ORMException("com.esen.jdbc.orm.entityinfomanagerfactory.2", "读取url：{0} 出现异常！", new Object[] { url }, ex);
			}
		}
		return entityInfoManager;
	}

 
}
