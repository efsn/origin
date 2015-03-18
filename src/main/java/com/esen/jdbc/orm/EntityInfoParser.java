package com.esen.jdbc.orm;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.esen.jdbc.orm.impl.ORMUtil;
import com.esen.util.ExceptionHandler;
import com.esen.util.StrFunc;
import com.esen.util.reflect.DynaBean;

/**
 * XML解析器
 *
 * @author wangshzh
 */
public class EntityInfoParser {

	/**
	 * XML根节点
	 */
	protected Document doc;

	/**
	 * xml中的标签<mapping ....>
	 */
	public static final String TAG_ROOT = "mapping";

	/**
	 * xml中的标签<entity ....>
	 */
	public static final String TAG_ENTITY = "entity";

	/**
	 * xml中的属性<entity name= ...>,<property name= ...>,<index name= ...>
	 */
	public static final String ATTRIBUTE_NAME = "name";

	/**
	 * xml中的属性<entity table= ...>
	 */
	public static final String ATTRIBUTE_TABLE = "table";

	/**
	 * xml中的属性
	 * <entity bean= ...>
	 * <beanBuilder bean= ...>
	 */
	public static final String ATTRIBUTE_BEAN = "bean";

	/**
	 * xml中的标签<caches>
	 */
	public static final String TAG_BEANBUILDER = "beanBuilder";
	
	/**
	 * xml中的属性<beanBuilder method= ...>
	 */
	public static final String ATTRIBUTE_METHOD = "method";

	/**
	 * xml中的标签<argument>
	 */
	public static final String TAG_ARGUMENT = "argument";
	
	/**
	 * xml中的属性<argument properties= ...>
	 */
	public static final String ATTRIBUTE_PROPERTIES = "properties";

	/**
	 * xml中的标签<properties>
	 */
	public static final String TAG_PROPERTIES = "properties";

	/**
	 * xml中的标签<property  ...>
	 */
	public static final String TAG_PROPERTY = "property";

	/**
	 * xml中的属性<property  field= ...>
	 */
	public static final String ATTRIBUTE_FIELD = "field";

	/**
	 * xml中的属性<property pk= ...>
	 */
	public static final String ATTRIBUTE_PK = "pk";

	/**
	 * xml中的属性<property unique= ...>，<index unique= ...>
	 */
	public static final String ATTRIBUTE_UNIQUE = "unique";

	/**
	 * xml中的属性<property nullable= ...>
	 */
	public static final String ATTRIBUTE_NULLABLE = "nullable";

	/**
	 * xml中的属性<property caption= ...>
	 */
	public static final String ATTRIBUTE_CAPTION = "caption";

	/**
	 * xml中的属性
	 * <property type= ...>
	 * <argument type= ...>
	 */
	public static final String ATTRIBUTE_TYPE = "type";

	/**
	 * xml中的属性<property length= ...>
	 */
	public static final String ATTRIBUTE_LENGTH = "length";

	/**
	 * xml中的属性<property autoinc= ...>
	 */
	public static final String ATTRIBUTE_AUTOINC = "autoinc";

	/**
	 * xml中的属性<property scale= ...>
	 */
	public static final String ATTRIBUTE_SCALE = "scale";

	/**
	 * xml中的标签<caches>
	 */
	public static final String TAG_CACHES = "caches";

	/**
	 * xml中的标签<cache>
	 */
	public static final String TAG_CACHE = "cache";

	/**
	 * xml中的属性<cache enable= ...>
	 */
	public static final String ATTRIBUTE_CACHE_ENABLE = "enable";

	/**
	 * xml中的属性<cache maxsize= ...>
	 */
	public static final String ATTRIBUTE_CACHE_MAXSIZE = "maxsize";

	/**
	 * xml中的属性<cache idletime= ...>
	 */
	public static final String ATTRIBUTE_CACHE_IDLE = "idletime";

	/**
	 * xml中的属性<cache livetime= ...>
	 */
	public static final String ATTRIBUTE_CACHE_LIVE = "livetime";
	
	/**
	 * xml中的属性<cache policy= ...>
	 */
	public static final String ATTRIBUTE_CACHE_POLICY = "policy";
	
	/**
	 * xml中的属性<cache policy= ...>
	 */
	public static final String ATTRIBUTE_CACHE_OVERFLOWTODISK = "overflowtodisk";

	/**
	 * xml中的标签<indexs>
	 */
	public static final String TAG_INDEXES = "indexes";

	/**
	 * xml中的标签<index ...>
	 */
	public static final String TAG_INDEX = "index";

	/**
	 * xml中的属性<index fields= ...>
	 */
	public static final String ATTRIBUTE_FIELDS = "fields";

	/**
	 * 用在异常中的部分提示信息。
	 */
	public static final String[] EXCEPTION_INFO_ENTITY = {"com.esen.jdbc.orm.entityinfoparser.entity","标签 entity 不存在"};

	/**
	 * 用在异常中的部分提示信息。
	 */
	public static final String[] EXCEPTION_INFO_NAME = {"com.esen.jdbc.orm.entityinfoparser.name","标签  name 不存在"};

	/**
	 * 用在异常中的部分提示信息。
	 */
	public static final String[] EXCEPTION_INFO_PROPERTY = {"com.esen.jdbc.orm.entityinfoparser.property","标签 property 不存在"};
	
	/**
	 * 用在异常中的部分提示信息。
	 */
	public static final String[] EXCEPTION_INFO_PROPERTIES = {"com.esen.jdbc.orm.entityinfoparser.properties","标签 properties 不存在"};
	
	/**
	 * 用在异常中的部分提示信息。
	 */
	public static final String[] EXCEPTION_INFO_ATTRIBUTES = {"com.esen.jdbc.orm.entityinfoparser.attributes","标签 attributes 不存在"};

	/**
	 * 用在异常中的部分提示信息。
	 */
	public static final String[] EXCEPTION_INFO_BEANBUILDER = {"com.esen.jdbc.orm.entityinfoparser.beanbuilder", "标签 beanBuilder 的属性不存在"};

	/**
	 * 用在异常中的部分提示信息。
	 */
	public static final String[] EXCEPTION_INFO_GETDYNABEAN = {"com.esen.jdbc.orm.entityinfoparser.dynabean","根据 Class 对象得到 DynaBean 出现异常"};
	
	/**
	 * 通过 XML 根节点构造 EntityInfoParser
	 * @param doc XML 根节点
	 */
	public EntityInfoParser(Document doc) {
		this.doc = doc;
	}

	/**
	 * 解析 XML
	 * @return 一个实体对象
	 */
	public EntityInfo parse() {
		if (doc == null) {
			return null;
		}
		
		NodeList elementsByTagName = doc.getElementsByTagName(TAG_ENTITY);
		if (elementsByTagName == null) {
			throw new ORMException(EXCEPTION_INFO_ENTITY[0],EXCEPTION_INFO_ENTITY[1]);
		}
		
		Element el = (Element) elementsByTagName.item(0);
		return parseEntityElement(el);
	}

	/**
	 * 解析得到EntityInfo
	 * @param el XML的mapping节点下的第一个Element
	 * @return 解析得到EntityInfo
	 */
	protected EntityInfoBean parseEntityElement(Element el) {
		if (el == null) {
			return null;
		}
		
		EntityInfoBean entity = createEntityInfoBean();
		NamedNodeMap attributes = el.getAttributes();
		if (attributes == null) {
			throw new ORMException(EXCEPTION_INFO_ATTRIBUTES[0],EXCEPTION_INFO_ATTRIBUTES[1]);
		}
		String[] tagInfo = {"com.esen.jdbc.orm.entityinfoparser.2","实体entity的{0}属性不能为空"};

		String name = parseNode(attributes, ATTRIBUTE_NAME, true, tagInfo, new Object[]{ATTRIBUTE_NAME});
		entity.setEntityName(name);

	    tagInfo = new String[]{"com.esen.jdbc.orm.entityinfoparser.3","实体(\"{0}\")的 {1}属性不能为空"};

		String table = parseNode(attributes, ATTRIBUTE_TABLE, true, tagInfo,new Object[]{name, ATTRIBUTE_TABLE});
		entity.setTable(table);

		String bean = parseNode(attributes, ATTRIBUTE_BEAN, true, tagInfo,new Object[]{name,ATTRIBUTE_BEAN});
		entity.setBean(classForName(bean));

		NodeList nodeList = el.getElementsByTagName(TAG_PROPERTIES);
		if (nodeList == null) {
			throw new ORMException(EXCEPTION_INFO_PROPERTIES[0],EXCEPTION_INFO_PROPERTIES[1]);
		}
		
		Element propertiesList = (Element) nodeList.item(0);
		if (propertiesList == null) {
			throw new ORMException(EXCEPTION_INFO_PROPERTIES[0],EXCEPTION_INFO_PROPERTIES[1]);
		}
		
		NodeList propNodeList = propertiesList.getElementsByTagName(TAG_PROPERTY);
		if (propNodeList == null) {
			throw new ORMException(EXCEPTION_INFO_PROPERTY[0],EXCEPTION_INFO_PROPERTY[1]);
		}
		
		//反射操作做一次就好，缓存好对象，便于后面使用
		DynaBean dynaBean = null;
		try {
			dynaBean = DynaBean.getDynaBean(entity.getBean().newInstance());
		} catch (Exception e) {
			ExceptionHandler.rethrowRuntimeException(e);
		}

		for (int i = 0; i < propNodeList.getLength(); i++) {
			Element propElement = (Element) propNodeList.item(i);
			Property property = parsePropertyElement(propElement, entity, dynaBean);
			entity.addProperty(property);
		}
		
//		if (entity.getPrimaryKey() == null) {
//			throw new ORMException("标签entity（“" + name + "”）没有设置主键！");
//		}
		
		//解析 BeanBuilder
		NodeList beanBuilderNodes = el.getElementsByTagName(TAG_BEANBUILDER);
		if (beanBuilderNodes != null) {
			Element beanBuilderElement = (Element) beanBuilderNodes.item(0);
			if (beanBuilderElement != null) {
				BeanBuilderBean beanBuilder = new BeanBuilderBean();
				
				NamedNodeMap beanBuilderAtt = beanBuilderElement.getAttributes();
				if (beanBuilderAtt == null) {
					throw new ORMException(EXCEPTION_INFO_BEANBUILDER[0], EXCEPTION_INFO_BEANBUILDER[1]);
				}
				
				String[] builderTagInfo = {"com.esen.jdbc.orm.entityinfoparser.14","标签 beanBuilder 的 {0} 属性不能为空"};
				String beanBuilderBean = parseNode(beanBuilderAtt, ATTRIBUTE_BEAN, true, builderTagInfo, new Object[]{ATTRIBUTE_BEAN});
				beanBuilder.setBuilder(classForName(beanBuilderBean));
				
				String beanBuilderMethod = parseNode(beanBuilderAtt, ATTRIBUTE_METHOD, true, builderTagInfo, new Object[]{ATTRIBUTE_METHOD});
				beanBuilder.setMethod(beanBuilderMethod);
				
				NodeList arguments = beanBuilderElement.getElementsByTagName(TAG_ARGUMENT);
				if (arguments != null) {
					Element argumentElement = (Element)arguments.item(0);
					if (argumentElement != null) {
						NamedNodeMap argumentAttr = argumentElement.getAttributes();
						String argumentString = parseNode(argumentAttr, ATTRIBUTE_PROPERTIES, false, null, null);
						beanBuilder.setArguments(argumentString == null ? null : argumentString.split(","));
					}
				}
				
				//检查 argument 与 properties 配置是否一致
				checkArgument(beanBuilder.getArguments(), entity);
				entity.setBeanBuilder(beanBuilder);
			}			
		}

		//解析caches
		NodeList cachesNodes = el.getElementsByTagName(TAG_CACHES);
		if (cachesNodes != null) {
			//cache
			Element cachesElement = (Element) cachesNodes.item(0);
			if (cachesElement != null) {
				NodeList cacheNodeList = cachesElement.getElementsByTagName(TAG_CACHE);
				if (cacheNodeList != null) {
					for (int i = 0; i < cacheNodeList.getLength(); i++) {
						Element cacheElement = (Element) cacheNodeList.item(i);
						
						CachePolicy cachePolicy = parseCachePolicyElement(cacheElement);
						((CachePolicyBean)cachePolicy).setName(entity.getBean().getName());
						entity.setCachePolicy(cachePolicy);
					}
				}
			}
		}

		//Indexes
		NodeList indexesNodes = el.getElementsByTagName(TAG_INDEXES);
		if (indexesNodes != null) {
			//index
			Element indexesElement = (Element) indexesNodes.item(0);
			if (indexesElement != null) {
				NodeList indexNodeList = indexesElement.getElementsByTagName(TAG_INDEX);
				if (indexNodeList != null) {
					for (int i = 0; i < indexNodeList.getLength(); i++) {
						Element indexElement = (Element) indexNodeList.item(i);
						Index index = parseIndexElement(indexElement);
						entity.addIndex(index);
					}
				}
			}
		}

		return entity;
	}
	
	/**
	 * 检查 beanBuilder 的属性 argument 中配置的属性名是否与实体的属性一致
	 * @param arguments
	 * @param entity
	 */
	private void checkArgument(String[] arguments, EntityInfo entity) {
		if (arguments == null || arguments.length == 0) {
			return;
		}
		
		for (int i = 0; i < arguments.length; i++) {
			if (entity.getProperty(arguments[i]) == null) {
				throw new ORMException("com.esen.jdbc.orm.entityinfoparser.15", "标签  argument 的属性  properties 的值  {0} 在实体的属性中不存在", new Object[]{arguments[i]});
			}
		}
		
	}

	/**
	 * 通过xml相应元素构建实体属性对象
	 * @param el xml相应元素
	 * @return 实体属性对象 
	 */
	protected PropertyBean parsePropertyElement(Element el, EntityInfoBean entityInfo, DynaBean dynaBean) {
		if (el == null) {
			throw new ORMException("JDBC.COMMON.SCANNOTBEEMPTY","参数 {0}不能为空！",new Object[]{"el"});
		}
		
		if (entityInfo == null) {
			throw new ORMException("JDBC.COMMON.SCANNOTBEEMPTY","参数  {0} 不能为空！",new Object[]{"entityInfo"});
		}
		
		if (dynaBean == null) {
			throw new ORMException("JDBC.COMMON.SCANNOTBEEMPTY","参数 {0} 不能为空！",new Object[]{"dynaBean"});
		}
		
		PropertyBean property = createPropertyBean();
		NamedNodeMap attributes = el.getAttributes();
		if (attributes == null) {
			throw new ORMException(EXCEPTION_INFO_ATTRIBUTES[0],EXCEPTION_INFO_ATTRIBUTES[1]);
		}

		String name = parseNode(attributes, ATTRIBUTE_NAME, true, EXCEPTION_INFO_PROPERTY,null);
		property.setName(name);
		String field = parseNode(attributes, ATTRIBUTE_FIELD, false, null,null);
		property.setFieldName(StrFunc.isNull(field) ? name : field);

		String pkValue = parseNode(attributes, ATTRIBUTE_PK, false, null,null);
		boolean isPk = StrFunc.parseBoolean(pkValue, false);
		if (isPk && (entityInfo.getPrimaryKey() != null)) {
			throw new ORMException("com.esen.jdbc.orm.entityinfoparser.6","entity(\"{0}\")不能有多个主键",new Object[]{entityInfo.getEntityName()});
		}

		//设置了主键，那么"unique" 自动设置为 true，"nullable" 为 false
		if (isPk) {
			property.setUnique(true);
			property.setNullable(false);
			property.setPrimaryKey(true);
			entityInfo.setPrimaryKey(name);
		} else {
			String unique = parseNode(attributes, ATTRIBUTE_UNIQUE, false, null,null);
			property.setUnique(StrFunc.parseBoolean(unique, false));

			String nullale = parseNode(attributes, ATTRIBUTE_NULLABLE, false, null,null);
			property.setNullable(StrFunc.parseBoolean(nullale, true));
		}

		String caption = parseNode(attributes, ATTRIBUTE_CAPTION, false, null,null);
		property.setCaption(caption);


		/*
		 * 非扩展字段用javabean中得到的值，有冲突时忽略xml中type设置；
		 * 扩展字段必须设type
		 */
		Class<?> typeClass = dynaBean.getFiledType(name);//注意 javabean 中的方法要求是public的，否则会找不到该属性。
		String typeInXml = parseNode(attributes, ATTRIBUTE_TYPE, false, null,null);
		if (typeClass == null) {
			if (StrFunc.isNull(typeInXml)) {
				throw new ORMException("com.esen.jdbc.orm.entityinfoparser.7","标签 Property(\"{0}\")的{1}属性不能为空",new Object[]{name,ATTRIBUTE_TYPE});
			}
			property.setType(typeInXml.trim().charAt(0));
		} else {
			/*
			 * 对于 int ,Integer 可以为 I，也可以为 L
			 * 对于 String，StringBuilder,StringBuffer 可以为 C，也可以为 M
			 * 对于 Document，可以为 M，也可以为 X
			 * 如果 xml 中有设置，以 xml为准；如果没有，以 javabean 为准
			 */
			char typeInBean = ORMUtil.getPropertyDefType(typeClass);
			if (StrFunc.isNull(typeInXml)) {
				property.setType(typeInBean);
			} else {
				char type = typeInXml.trim().charAt(0);
				if ((ORMUtil.isLogicTypeClass(typeClass) && type == Property.FIELD_TYPE_LOGIC)
					|| (ORMUtil.isClobTypeClass(typeClass) && type == Property.FIELD_TYPE_CLOB)
					|| (ORMUtil.isBinaryTypeClass(typeClass) && type == Property.FIELD_TYPE_BINARY)) {
					property.setType(type);;
				} else {
					property.setType(typeInBean);
				}					
			}
		}
		String[] tagInfo = {"com.esen.jdbc.orm.entityinfoparser.7","标签 Property(\"{0}\")的{1}属性不能为空"};
		char typeValue = property.getType();
		if (typeValue == Property.FIELD_TYPE_STR
				|| typeValue == Property.FIELD_TYPE_FLOAT) {
			String length = parseNode(attributes, ATTRIBUTE_LENGTH, true, tagInfo, new Object[]{name, ATTRIBUTE_LENGTH});
			property.setLength(Integer.parseInt(length));//length无默认值,故不用StrFunc中的方法
		}
		
		if (typeValue == Property.FIELD_TYPE_INT) {
			String length = parseNode(attributes, ATTRIBUTE_LENGTH, false, null,null);
			/*
			 * BUG:ESENFACE-1067: modify by liujin 2014.06.18
			 * 整型不指定长度时，需要根据 java bean 中的类型确定长度
			 * 如果都当做 integer 处理，数据库中数据范围小于 java bean 中数据范围时，会出错
			 */
			property.setLength(StrFunc.parseInt(length, ORMUtil.getITypeLength(typeClass)));
			
			String autoinc = parseNode(attributes, ATTRIBUTE_AUTOINC, false, null,null);
			property.setAutoInc(StrFunc.parseBoolean(autoinc, false));
		}

		if (typeValue == Property.FIELD_TYPE_FLOAT) {
			String scale = parseNode(attributes, ATTRIBUTE_SCALE, false, null,null);
			property.setScale(StrFunc.parseInt(scale, 0));
		}
		
		return property;
	}

	/**
	 * 通过xml相应元素构建缓存策略对象
	 * @param el xml相应元素
	 * @return 缓存策略对象
	 */
	protected CachePolicyBean parseCachePolicyElement(Element el) {
		if (el == null) {
			throw new ORMException("JDBC.COMMON.SCANNOTBEEMPTY","参数 {0} 不能为空！",new Object[]{el});
		}
		
		CachePolicyBean cachePolicy = new CachePolicyBean();
		NamedNodeMap attributes = el.getAttributes();
		if (attributes == null) {//it is an <code>Element</code>) or <code>null</code> otherwise.
			throw new ORMException(EXCEPTION_INFO_ATTRIBUTES[0],EXCEPTION_INFO_ATTRIBUTES[1]);
		}

		//缓存策略名，默认为 LRU
		String name = parseNode(attributes, ATTRIBUTE_CACHE_POLICY, false, null, null);
		cachePolicy.setPolicy(name);
		//String[] tagInfo = {"com.esen.jdbc.orm.entityinfoparser.15","标签 cache(\"" + name + "\")"};

		String enable = parseNode(attributes, ATTRIBUTE_CACHE_ENABLE, false, null,null);
		cachePolicy.setEnable(StrFunc.parseBoolean(enable, false));

		String maxsize = parseNode(attributes, ATTRIBUTE_CACHE_MAXSIZE, false, null,null);
		cachePolicy.setMaxsize(StrFunc.parseInt(maxsize, CachePolicyBean.ALLOW_MAXSIZE));

		String idle = parseNode(attributes, ATTRIBUTE_CACHE_IDLE, false, null,null);
		cachePolicy.setIdleTime(StrFunc.parseInt(idle, CachePolicyBean.DEFAULT_IDLETIME));

		String live = parseNode(attributes, ATTRIBUTE_CACHE_LIVE, false, null,null);
		cachePolicy.setLiveTime(StrFunc.parseInt(live, CachePolicyBean.DEFAULT_LIVETIME));

		return cachePolicy;
	}

	/**
	 * 根据xml的Element构建实体的索引对象
	 * @param el 包含Indexs信息的一个Element
	 * @return 实体的索引对象
	 */
	protected IndexBean parseIndexElement(Element el) {
		if (el == null) {
			throw new ORMException("JDBC.COMMON.SCANNOTBEEMPTY","参数 {0} 不能为空！",new Object[]{"el"});
		}
		
		IndexBean index = new IndexBean();
		NamedNodeMap attributes = el.getAttributes();
		if (attributes == null) {
			throw new ORMException(EXCEPTION_INFO_ATTRIBUTES[0],EXCEPTION_INFO_ATTRIBUTES[1]);
		}

		String name = parseNode(attributes, ATTRIBUTE_NAME, true, EXCEPTION_INFO_NAME,null);
		index.setIndexName(name);
		
		String[] tagInfo = {"com.esen.jdbc.orm.entityinfoparser.10","标签 index(\"{0}\")的{1}属性不能为空"};

		String indexFields = parseNode(attributes, ATTRIBUTE_FIELDS, true, tagInfo,new Object[]{name, ATTRIBUTE_FIELDS});
		index.setIndexFields(indexFields);

		String unique = parseNode(attributes, ATTRIBUTE_UNIQUE, false, null,null);
		index.setUnique(StrFunc.parseBoolean(unique, false));
		
		return index;
	}

	/**
	 * 获取标签的一个属性
	 * @param attributes NamedNodeMap
	 * @param namedItem 属性名
	 * @param mustBeSet 是否必须设置
	 * @param tagName 标签信息
	 * @return mustBeSet为true情况下，不可能返回null和""；
	 */
	protected String parseNode(NamedNodeMap attributes, String namedItem, boolean mustBeSet, String[] exInfo,Object[] exparams) {
		Node node = attributes.getNamedItem(namedItem);
		
		if (node == null || StrFunc.isNull(node.getNodeValue())) {
			if (mustBeSet) {
				throw new ORMException(exInfo[0],exInfo[1],exparams);
			}
			return "";//对于一个非必须设置的值，返回""比null更好，避免别的地方用的时候抛空指针。
		}
		return node.getNodeValue();
	}

	/**
	 * 通过clsName得到Class，继承此类的子类可覆盖此方法
	 * @param clsName
	 * @return Class
	 */
	protected Class classForName(String clsName) {
		if (StrFunc.isNull(clsName)) {
			throw new ORMException("com.esen.jdbc.orm.entityinfoparser.12","参数 clsName 不能为空！");
		}
		
		Class clazz = null;
		try {
			clazz = Class.forName(clsName);
		} catch (ClassNotFoundException e) {
			throw new ORMException("com.esen.jdbc.orm.entityinfoparser.13","通过 clsName :{0}得到 Class 时出现异常",new Object[]{clsName},e);
		}
		return clazz;
	}

	/**
	 * 创建实体对象Bean
	 * @return 实体对象Bean
	 */
	protected EntityInfoBean createEntityInfoBean() {
		return new EntityInfoBean();
	}
	
	/**
	 * 创建实体对象的一个属性 Bean
	 * @return 实体对象的一个属性 Bean
	 */
	protected PropertyBean createPropertyBean() {
		return new PropertyBean();
	}
	
	/**
	 * 创建实体的索引对象Bean
	 * @return 实体的索引对象Bean
	 */
	protected IndexBean createIndexBean() {
		return new IndexBean();
	}

	/**
	 * 创建对象缓存策略Bean
	 * @return 对象缓存策略Bean
	 */
	protected CachePolicyBean createCachePolicyBean() {
		return new CachePolicyBean();
	}
}
