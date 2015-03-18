package com.esen.jdbc.orm;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.esen.util.OrderedMap;
import com.esen.util.StrFunc;
import com.esen.util.XmlFunc;

/**
 * 定义一个实体对象
 *
 * @author wangshzh
 */
public class EntityInfoBean implements EntityInfo, Serializable {

	/**
	 * 实体对象的ID
	 */
	private String entityName;

	/**
	 * 数据库表名
	 */
	private String table;

	/**
	 * 主键属性
	 */
	private String primaryKeyName;

	/**
	 * 实体对象对应的javabean对象
	 */
	private Class bean;
	
	/**
	 * 实体对象对应的创建对象的类和方法
	 */
	private BeanBuilder beanBuilder;

	/**
	 * 存放所有的属性对象
	 * 因为后面有方法返回
	 */
	private OrderedMap properties = new OrderedMap();

	/**
	 * 存放所有的索引对象
	 */
	private List<Index> indexes = new ArrayList<Index>();

	/**
	 * 存放缓存策略
	 */
	private CachePolicy cachePolicy = null;

	/**
	 * 为真常量字符串
	 */
	public static final String BOOLEAN_TRUE = "true";

	/**
	 * 为假常量字符串
	 */
	public static final String BOOLEAN_FALSE = "false";

	/**
	 * 对象更新的监听器集合
	 */
	private ArrayList<DataChangeEventListener> dataChaEvtListeners = new ArrayList<DataChangeEventListener>();

	/**
	 * 添加对象更新的监听器
	 * @param dataChangeEventListener 对象更新的监听器
	 */
	public void addDataChangeEventListener(DataChangeEventListener dataChangeEventListener) {
		this.dataChaEvtListeners.add(dataChangeEventListener);
	}

	/**
	 * 移除对象更新的监听器
	 * @param dataChangeEventListener 对象更新的监听器
	 */
	public void removeDataChangeEventListener(DataChangeEventListener dataChangeEventListener) {
		this.dataChaEvtListeners.remove(dataChangeEventListener);
	}

	/**
	 * 触发对象更新事件
	 * @param factory Session创建工厂
	 */
	public void notifyDataChaEvtListeners(SessionFactory factory) {
		for (DataChangeEventListener dataChaEvtListener : dataChaEvtListeners) {
			dataChaEvtListener.onEntityChangedEvent(factory, this.entityName);
		}
	}

	/**
	 * 实体对象名
	 * @param entityName 实体对象名
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	/**
	 * {@inheritDoc}   
	 */
	public String getEntityName() {
		return this.entityName;
	}

	/**  
	 * 设置数据库表名  
	 * @param table 数据库表名 
	 */
	public void setTable(String table) {
		this.table = table;
	}

	/**
	 * {@inheritDoc}   
	 */
	public String getTable() {
		return this.table;
	}

	/**  
	 * 设置主键属性  
	 * @param primaryKeyName 主键属性  
	 */
	public void setPrimaryKey(String primaryKeyName) {
		this.primaryKeyName = primaryKeyName;
	}

	/**
	 * {@inheritDoc}   
	 */
	public Property getPrimaryKey() {
		return this.getProperty(this.primaryKeyName);
	}

	/**  
	 * 设置实体对象对应的javabean对象  
	 * @param bean 实体对象对应的javabean对象  
	 */
	public void setBean(Class bean) {
		this.bean = bean;
	}

	/**
	 * {@inheritDoc}   
	 */
	public Class getBean() {
		return this.bean;
	}
	
	/**
	 * 设置实体对象对应的创建对象的类和方法
	 */
	public void setBeanBuilder(BeanBuilder beanBuilder) {
		this.beanBuilder = beanBuilder;
	}
	
	/**
	 * {@inheritDoc}   
	 */
	public BeanBuilder getBeanBuilder() {
		return this.beanBuilder;
	}

	/**
	 * {@inheritDoc}   
	 */
	public Property getProperty(String propertyName) {
		return (Property) this.properties.get(propertyName);
	}
	
	/**
	 * {@inheritDoc}   
	 */
	public Property getPropertyIgoreCase(String propertyName) {
		int size = this.properties.size();
		for (int i = 0; i < size; i++) {
			Property prop = (Property)this.properties.get(i);
			if (prop != null) {
				String name = prop.getName();
				if (!StrFunc.isNull(name) && name.compareToIgnoreCase(propertyName) == 0) {
					return prop;
				}
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}   
	 */
	public Property getPropertyByField(String fieldName) {
		Collection<Property> allProperty = this.properties.values();
		for (Property tempProperty : allProperty) {
			if (tempProperty.getFieldName().equalsIgnoreCase(fieldName)) {
				return tempProperty;
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}   
	 */
	public List<Property> getProperties() {
		Collection<Property> tempAllPro = this.properties.values();
		if (tempAllPro.size() == 0) {
			return null;
		}
		
		List<Property> allProperties = new ArrayList<Property>();
		allProperties.addAll(tempAllPro);
		return allProperties;
	}

	/**
	 * {@inheritDoc}   
	 */
	public List<Index> listIndexes() {
		//不能直接把this.indexes返回，这样用户改的时候会影响到this.indexes
		if (this.indexes == null || this.indexes.size() == 0) {
			return null;
		}
		
		List<Index> allIndexs = new ArrayList<Index>();
		allIndexs.addAll(this.indexes);
		return allIndexs;
	}

	/**
	 * 根据索引名获取索引对象
	 * 
	 * @param indexName 索引名
	 * @return 索引对象
	 */
	public Index getIndex(String indexName) {
		for (Index tempIndex : this.indexes) {
			if (tempIndex.getIndexName().equals(indexName)) {
				return tempIndex;
			}
		}
		
		return null;
	}

	/**
	 * 增加索引   
	 * @param index 索引
	 */
	public void addIndex(Index index) {
		//已经存在了，就不加。
		if (index == null 
				|| getIndex(index.getIndexName()) != null) {
			return;
		} 
		
		this.indexes.add(index);
	}

	/**
	 * 将实体对象转换为 document
	 * 
	 * @return Document
	 */
	protected Document saveToDocument() {
		Document doc = null;
		try {
			doc = XmlFunc.createDocument(EntityInfoParser.TAG_ROOT);
		} catch (Exception e) {
			throw new ORMException("com.esen.jdbc.orm.entityinfobean.1","创建Document失败",e);
		}
		
		Element root = doc.getDocumentElement();
		Element entity = addNode(doc, root, EntityInfoParser.TAG_ENTITY);
		entity.setAttribute(EntityInfoParser.ATTRIBUTE_NAME, this.getEntityName());
		entity.setAttribute(EntityInfoParser.ATTRIBUTE_TABLE, this.getTable());
		entity.setAttribute(EntityInfoParser.ATTRIBUTE_BEAN, this.getBean().getName());

		Element caches = addNode(doc, entity, EntityInfoParser.TAG_CACHES);
		if (this.cachePolicy != null) {
			Element cache = addNode(doc, caches, EntityInfoParser.TAG_CACHE);
			cache.setAttribute(EntityInfoParser.ATTRIBUTE_CACHE_POLICY, cachePolicy.getPolicy().toString());
			cache.setAttribute(EntityInfoParser.ATTRIBUTE_CACHE_ENABLE, Boolean.toString(cachePolicy.isEnable()));
			cache.setAttribute(EntityInfoParser.ATTRIBUTE_CACHE_MAXSIZE, Integer.toString(cachePolicy.getMaxsize()));
			cache.setAttribute(EntityInfoParser.ATTRIBUTE_CACHE_IDLE, Integer.toString(cachePolicy.getIdleTime()));
			cache.setAttribute(EntityInfoParser.ATTRIBUTE_CACHE_LIVE, Integer.toString(cachePolicy.getLiveTime()));
			cache.setAttribute(EntityInfoParser.ATTRIBUTE_CACHE_OVERFLOWTODISK, Boolean.toString(cachePolicy.isOverflowToDisk()));
		}
		
		Element properties = addNode(doc, entity, EntityInfoParser.TAG_PROPERTIES);
		if (this.properties != null) {
			Collection<Property> allProperties = this.properties.values();
			
			for (Property tempProperty : allProperties) {
				Element property = addNode(doc, properties, EntityInfoParser.TAG_PROPERTY);
				
				property.setAttribute(EntityInfoParser.ATTRIBUTE_NAME, tempProperty.getName());
				if (!StrFunc.isNull(tempProperty.getFieldName())) {//getFieldName()直接返回的，基本无开销，不用暂存。
					property.setAttribute(EntityInfoParser.ATTRIBUTE_FIELD, tempProperty.getFieldName());
				}
				
				if (!StrFunc.isNull(tempProperty.getCaption())) {
					property.setAttribute(EntityInfoParser.ATTRIBUTE_CAPTION, tempProperty.getCaption());
				}
				
				if (tempProperty.equals(this.getPrimaryKey())) {
					property.setAttribute(EntityInfoParser.ATTRIBUTE_PK, BOOLEAN_TRUE);
				} else {
					if (tempProperty.isUnique()) {
						property.setAttribute(EntityInfoParser.ATTRIBUTE_UNIQUE, Boolean.toString(tempProperty.isUnique()));
					}
					if (!tempProperty.isNullable()) {
						property.setAttribute(EntityInfoParser.ATTRIBUTE_NULLABLE, Boolean.toString(tempProperty.isNullable()));
					}
				}
				
				char typeValue = tempProperty.getType();
				property.setAttribute(EntityInfoParser.ATTRIBUTE_TYPE, String.valueOf(typeValue));
				if (typeValue == Property.FIELD_TYPE_INT 
						|| typeValue == Property.FIELD_TYPE_STR
						|| typeValue == Property.FIELD_TYPE_FLOAT) {
					property.setAttribute(EntityInfoParser.ATTRIBUTE_LENGTH, Integer.toString(tempProperty.length()));
				}
				
				if (typeValue == Property.FIELD_TYPE_INT && tempProperty.isAutoInc()) {
					property.setAttribute(EntityInfoParser.ATTRIBUTE_AUTOINC, BOOLEAN_TRUE);
				}
	
				if (typeValue == Property.FIELD_TYPE_FLOAT) {
					property.setAttribute(EntityInfoParser.ATTRIBUTE_SCALE, Integer.toString(tempProperty.getScale()));
				}
			}
		}

		Element indexes = addNode(doc, entity, EntityInfoParser.TAG_INDEXES);
		for (Index tempIndex : this.indexes) {
			Element index = addNode(doc, indexes, EntityInfoParser.TAG_INDEX);
			index.setAttribute(EntityInfoParser.ATTRIBUTE_NAME, tempIndex.getIndexName());
			index.setAttribute(EntityInfoParser.ATTRIBUTE_FIELDS, tempIndex.getIndexFields());
			index.setAttribute(EntityInfoParser.ATTRIBUTE_UNIQUE, Boolean.toString(tempIndex.isUnique()));
		}

		addExtendedNode(doc, entity);
		
		return doc;
	}
	
	/**
	 * {@inheritDoc}   
	 */
	public void saveTo(OutputStream out) {
		if (out == null) {
			throw new ORMException("com.esen.jdbc.orm.entityinfobean.2","传入的 OutputStream 不能为空！");
		}

		Document doc = saveToDocument();
		
		try {
			XmlFunc.saveDocument(doc, out, "utf-8");
		} catch (Exception e) {
			throw new ORMException("com.esen.jdbc.orm.entityinfobean.3","将 Document 保存到输出流时失败！",e);
		}
	}
	
	/**
	 * 添加扩展的结点信息
	 */
	protected void addExtendedNode(Document doc, Element entity) {
		return;
	}

	/**
	 * 添加子节点
	 * 
	 * @param doc xml的Document对象
	 * @param node 父节点
	 * @param name 子节点名称
	 * @return 子节点
	 */
	protected Element addNode(Document doc, Node node, String name) {
		if (doc != null && node != null && name != null) {
			Element e = doc.createElement(name);
			e.appendChild(doc.createTextNode(""));
			node.appendChild(e);
			return e;
		} else {
			throw new ORMException("com.esen.jdbc.orm.entityinfobean.4","添加节点{0}时失败！",new Object[]{name});
		}
	}

	/**
	 * 设置缓存策略
	 * 
	 * @param cachePolicy 缓存策略
	 */
	public void setCachePolicy(CachePolicy cachePolicy) {
		if (cachePolicy.isEnable() && !hasRequiredInterface()) {
			throw new ORMException("com.esen.jdbc.orm.entityinfobean.5","使用缓存的实体对象必须实现 Serializable 和 Cloneable 接口");
		}

		this.cachePolicy = cachePolicy;
	}

	/**
	 * 增加属性
	 * 
	 * @param prop 属性
	 */
	public void addProperty(Property prop) {
		if (prop == null || getProperty(prop.getName()) != null) {
			return;
		} 
		
		this.properties.put(prop.getName(), prop);
	}
	
	/**
	 * 增加多个属性
	 * 
	 * @param props 属性
	 */
	public void addProperty(List<Property> props) {
		if (props == null) {
			return;
		} 
		
		for (Property prop : props) {
			this.properties.put(prop.getName(), prop);
		}
	}
	
	/**
	 * 删除属性
	 * @param propname 属性名
	 */
	public void removeProperty(String propname) {
		if (StrFunc.isNull(propname)) {
			return;
		} 
		
		if (getProperty(propname) == null) {
			throw new ORMException("com.esen.jdbc.orm.entityinfobean.6","属性{0}不存在！",new Object[]{propname});
		}
		
		this.properties.remove(propname);
	}
	
	/**
	 * 删除所有属性
	 */
	public void removeAllProperty() {
		this.properties.clear();
	}

	/**
	 * {@inheritDoc}   
	 */
	public CachePolicy getCachePolicy() {
		return this.cachePolicy;
	}
	
	/**
	 * 判断该实体是否实现了 Serializable 和 Cloneable 接口
	 * @return
	 */
	private boolean hasRequiredInterface() {
		boolean hasSerializable = false;
		boolean hasCloneable = false;
		Class[] interfaces = this.getBean().getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			if (interfaces[i] == Serializable.class) {
				hasSerializable = true;
			}
			
			if (interfaces[i] == Cloneable.class) {
				hasCloneable = true;
			}
		}
		
		if (hasSerializable && hasCloneable) {
			return true;
		}
		return false;
	}
}
