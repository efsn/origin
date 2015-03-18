package com.esen.jdbc.orm.impl;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.EntityInfoManager;
import com.esen.jdbc.orm.EntityInfoParser;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.helper.SCEntityInfoParser;
import com.esen.jdbc.orm.helper.SCTreeEntityInfoBean;
import com.esen.jdbc.orm.helper.SCTreeEntityInfoParser;
import com.esen.jdbc.orm.helper.TreeEntityInfoBean;
import com.esen.jdbc.orm.helper.TreeEntityInfoParser;
import com.esen.util.XmlFunc;

/**
 * 实体对象的管理
 *
 * @author wangshzh
 */
public class EntityInfoManagerImpl implements EntityInfoManager {
	/**
	 * 实体类型
	 */
	private final int ENTITY_TYPE_DEFAULT = 0;	
	private final int ENTITY_TYPE_SC = 1;	
	private final int ENTITY_TYPE_TREE = 2;	
	private final int ENTITY_TYPE_SCTREE = 3;	

	/**
	 * 持有EntityInfo的集合，这个地方应该用HashMap,用entityname做键，EntityInfo做值
	 */
	private HashMap<String, EntityInfo> entityMap = new HashMap<String, EntityInfo>();

	/**
	 * {@inheritDoc}   
	 */
	public int size() {
		return entityMap.size();
	}

	/**
	 * {@inheritDoc}   
	 */
	public EntityInfo getEntity(String entityName) {
		return entityMap.get(entityName);
	}

	/**
	 * {@inheritDoc}   
	 */
	public Set<EntityInfo> getEntityByClass(Class cls) {
		if (cls == null) {
			return null;
		}
		Collection<EntityInfo> entities = this.entityMap.values();
		Set<EntityInfo> entitySet = new HashSet<EntityInfo>();
		for (EntityInfo entityInfo : entities) {
			if (entityInfo.getBean().equals(cls)) {
				entitySet.add(entityInfo);
			}
		}
		return entitySet;
	}

	/**
	 * {@inheritDoc}   
	 */
	public Set<String> getEntityNames() {
		return entityMap.keySet();
	}

	/**
	 * {@inheritDoc}   
	 */
	public EntityInfo loadEntityFrom(InputStream in) {
		Document doc = null;
		try {
			doc = XmlFunc.getDocument(in);
			
			switch (parseEntityType(doc)) {
			case ENTITY_TYPE_SCTREE:
				return new SCTreeEntityInfoParser(doc).parse();

			case ENTITY_TYPE_TREE:
				return new TreeEntityInfoParser(doc).parse();
				
			case ENTITY_TYPE_SC:
				return new SCEntityInfoParser(doc).parse();

			case ENTITY_TYPE_DEFAULT:
			default:
				return new EntityInfoParser(doc).parse();
			}			
		} catch (Exception e) {
			throw new ORMException("com.esen.jdbc.orm.impl.entityinfomanagerimpl.1","从 xml 文件流中得到 Document 对象时发生异常", e);
		}
	}

	/**
	 * 从xml流中解析带有树形属性的实体
	 * @param in xml流
	 * @return 带有树形属性的实体
	 */
	public TreeEntityInfoBean loadSCEntityFrom(InputStream in) {
		Document doc = null;
		TreeEntityInfoBean treeEntityInfo = null;
		try {
			doc = XmlFunc.getDocument(in);
			TreeEntityInfoParser treeEntityInfoParser = new TreeEntityInfoParser(doc);
			treeEntityInfo = (TreeEntityInfoBean) treeEntityInfoParser.parse();
		} catch (Exception e) {
			throw new ORMException("com.esen.jdbc.orm.impl.entityinfomanagerimpl.1","从 xml 文件流中得到 Document 对象时发生异常", e);
		}
		return treeEntityInfo;
	}

	/**
	 * 从xml流中解析带有缓慢增长的树形属性的实体
	 * @param in xml流
	 * @return 带有缓慢增长的树形属性的实体
	 */
	public SCTreeEntityInfoBean loadSCTreeEntityFrom(InputStream in) {
		Document doc = null;
		SCTreeEntityInfoBean scTreeEntityInfo = null;
		try {
			doc = XmlFunc.getDocument(in);
			SCTreeEntityInfoParser scTreeEntityInfoParser = new SCTreeEntityInfoParser(doc);
			scTreeEntityInfo = (SCTreeEntityInfoBean) scTreeEntityInfoParser.parse();
		} catch (Exception e) {
			throw new ORMException("com.esen.jdbc.orm.impl.entityinfomanagerimpl.1","从 xml 文件流中得到 Document 对象时发生异常", e);
		}
		return scTreeEntityInfo;
	}

	/**
	 * {@inheritDoc}   
	 */
	public void addEntity(EntityInfo entity) {
		if (entity == null || getEntity(entity.getEntityName()) != null) {
			return;
		} else {
			entityMap.put(entity.getEntityName(), entity);
		}
	}

	/**
	 * {@inheritDoc}   
	 */
	public EntityInfo remove(String entityName) {
		return entityMap.remove(entityName);
	}
	
	/**
	 * 分析实体类型
	 * 
	 * @param doc
	 * @return
	 */
	private int parseEntityType(Document doc) {
		NodeList entityList = doc.getElementsByTagName(EntityInfoParser.TAG_ENTITY);
		if (entityList == null) {
			return ENTITY_TYPE_DEFAULT;
		}
		
		Element entity = (Element) entityList.item(0);
		NodeList scList = entity.getElementsByTagName(SCEntityInfoParser.TAG_SLOWLYCHANGE);
		Element sc = null;
		if (scList != null) {
			sc = (Element) scList.item(0);
		}

		NodeList treeList = entity.getElementsByTagName(TreeEntityInfoParser.TAG_TREE);
		Element tree = null;
		if (treeList != null) {
			tree = (Element) treeList.item(0);
		}
		
		if (sc != null && tree != null) {
			return ENTITY_TYPE_SCTREE;
		}
		
		if (sc != null) {
			return ENTITY_TYPE_SC;
		}
		
		if (tree != null) {
			return ENTITY_TYPE_TREE;
		}
		
		return ENTITY_TYPE_DEFAULT;
	}
}
