package com.esen.jdbc.orm;

import java.io.FileInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;
import com.esen.util.XmlFunc;

/**
 * xml解析单元测试
 *
 * @author wangshzh
 */
public class TestEntityInfoParser extends TestCase {

	public void testParse() throws ClassNotFoundException {
		//完全正确的xml格式
		EntityInfo<?> entityInfo = makeTestInstance("test/com/esen/jdbc/orm/fruit_mapping_right.xml");
		assertEquals("orm.Fruit", entityInfo.getEntityName());
		assertEquals("ES_FRUIT", entityInfo.getTable());
		assertEquals(Class.forName("com.esen.jdbc.orm.Fruit"), entityInfo.getBean());

		CachePolicy cachePolicy = entityInfo.getCachePolicy();
		assertEquals("cache", cachePolicy.getName());
		assertEquals(true, cachePolicy.isEnable());
		assertEquals(1000, cachePolicy.getMaxsize());
		assertEquals(100, cachePolicy.getIdleTime());

		Index index = entityInfo.listIndexes().get(0);
		assertEquals("INDEX_ID_NAME", index.getIndexName());
		assertEquals("ID_,NAME_", index.getIndexFields());
		assertEquals(true, index.isUnique());

		Property property = entityInfo.getProperties().get(0);
		assertEquals("id", property.getName());//要求property有序存储，故用get(0)
		//assertEquals("id", entityInfo.getProperty("id").getName());
		assertEquals(false, property.isNullable());
		assertEquals("ID_", property.getFieldName());
		assertEquals(true, property.isUnique());
		assertEquals(20, property.length());
		assertEquals(true, property.isPrimaryKey());
		assertEquals(false, property.isAutoInc());
		assertEquals('C', property.getType());
		assertEquals("", property.getCaption());
		assertEquals(2, entityInfo.getProperties().get(2).getScale());

		//有错误的fruit_erro_onemore_pk.xml：配置了两主键 
		try {
			entityInfo = makeTestInstance("test/com/esen/jdbc/orm/fruit_erro_onemore_pk.xml");
			fail();
		} catch (Exception e) {
			assertEquals("entity(\"" + entityInfo.getEntityName() + "\")不能有多个主键", e.getMessage());
		}

		//有错误的fruit_erro_no_entityname.xml：实体entity 的 name 属性为空		
		try {
			entityInfo = makeTestInstance("test/com/esen/jdbc/orm/fruit_erro_no_entityname.xml");
			fail();
		} catch (Exception e) {
			assertEquals("实体entity 的 name 属性不能为空", e.getMessage());
		}

		//有错误的fruit_erro_no_tablename.xml：实体entity 的 table 属性为空	
		try {
			entityInfo = makeTestInstance("test/com/esen/jdbc/orm/fruit_erro_no_tablename.xml");
			fail();
		} catch (Exception e) {
			assertEquals("实体(\"orm.Fruit\") 的 table 属性不能为空", e.getMessage());
		}

		//有错误的fruit_erro_wrongbeanname.xml：实体entity 的 beanname写错了	
		try {
			entityInfo = makeTestInstance("test/com/esen/jdbc/orm/fruit_erro_wrongbeanname.xml");
			fail();
		} catch (Exception e) {
			if (e.getMessage().indexOf("得到 Class 时出现异常") < 0) {
				fail();
			}
		}

	}

	public EntityInfo makeTestInstance(String filePath) {
		Document doc = null;
		EntityInfoParser entityInfoParser = null;
		try {
			doc = XmlFunc.getDocument(new FileInputStream(filePath));
		} catch (Exception e) {
			throw new ORMException(e);
		}
		entityInfoParser = new EntityInfoParser(doc);
		return entityInfoParser.parse();
	}

	private EntityInfoParser getEntityInfoParser() {
		Document doc = null;
		try {
			doc = XmlFunc.getDocument(new FileInputStream(
					"test/com/esen/jdbc/orm/fruit_mapping_right.xml"));
		} catch (Exception e) {
			fail();
		}

		return new EntityInfoParser(doc);
	}
	
	 public void testClassForName() throws Exception {
		EntityInfoParser entityInfoParser = getEntityInfoParser();
		Class getFruitCls = entityInfoParser.classForName("com.esen.jdbc.orm.Fruit");
		//junit比较两个object的时候，不是简单的比较内存地址。应该是用了反射获取object的真实类型。
		assertEquals(Fruit.class, getFruitCls);
	}

	public void testParseNode() {
		EntityInfoParser entityInfoParser = getEntityInfoParser();
		String[] tagInfo = {"com.esen.jdbc.orm.testentityinfoparser.1","实体entity的{0}属性不能为空"};
		String name = entityInfoParser.parseNode(getNamedNodeMap(EntityInfoParser.TAG_ENTITY, 0),EntityInfoParser.ATTRIBUTE_NAME, true,tagInfo,new Object[]{EntityInfoParser.ATTRIBUTE_NAME});
		assertEquals("orm.Fruit", name);
	}


	public void testParseIndexElement() {
		EntityInfoParser entityInfoParser = getEntityInfoParser();
		
		NamedNodeMap attributes = getNamedNodeMap(EntityInfoParser.TAG_INDEX, 0);
		String name = entityInfoParser.parseNode(attributes, EntityInfoParser.ATTRIBUTE_NAME, false, null,null);
		assertEquals("INDEX_ID_NAME", name);
		String fields = entityInfoParser.parseNode(attributes, EntityInfoParser.ATTRIBUTE_FIELDS, false, null,null);
		assertEquals("ID_,NAME_", fields);
		String unique = entityInfoParser.parseNode(attributes, EntityInfoParser.ATTRIBUTE_UNIQUE, false, null,null);
		assertEquals("true", unique);
	}

	public void testParseCacheElement() {
		EntityInfoParser entityInfoParser = getEntityInfoParser();
		
		NamedNodeMap attributes = getNamedNodeMap(EntityInfoParser.TAG_CACHE, 0);
		String name = entityInfoParser.parseNode(attributes, EntityInfoParser.ATTRIBUTE_CACHE_POLICY, false, null,null);
		assertEquals("cache", name);
		String fields = entityInfoParser.parseNode(attributes, EntityInfoParser.ATTRIBUTE_CACHE_ENABLE, false, null,null);
		assertEquals("true", fields);
	}

	public void testParsePropertyElement() {
		EntityInfoParser entityInfoParser = getEntityInfoParser();
		
		NamedNodeMap attributes = getNamedNodeMap(EntityInfoParser.TAG_PROPERTY, 0);
		String name = entityInfoParser.parseNode(attributes, EntityInfoParser.ATTRIBUTE_NAME, true,EntityInfoParser.EXCEPTION_INFO_PROPERTY,null);
		assertEquals("id", name);
		String field = entityInfoParser.parseNode(attributes, EntityInfoParser.ATTRIBUTE_FIELD, true,EntityInfoParser.EXCEPTION_INFO_PROPERTY,null);
		assertEquals("ID_", field);
		String nullale = entityInfoParser.parseNode(attributes, EntityInfoParser.ATTRIBUTE_NULLABLE, true,EntityInfoParser.EXCEPTION_INFO_PROPERTY,null);
		assertEquals("false", nullale);
		String unique = entityInfoParser.parseNode(attributes, EntityInfoParser.ATTRIBUTE_UNIQUE, true,EntityInfoParser.EXCEPTION_INFO_PROPERTY,null);
		assertEquals("true", unique);
		String length = entityInfoParser.parseNode(attributes, EntityInfoParser.ATTRIBUTE_LENGTH, true,EntityInfoParser.EXCEPTION_INFO_PROPERTY,null);
		assertEquals("20", length);
		String pk = entityInfoParser.parseNode(attributes, EntityInfoParser.ATTRIBUTE_PK, true,EntityInfoParser.EXCEPTION_INFO_PROPERTY,null);
		assertEquals("true", pk);
	}

	public void testParseEntityElement() {
		EntityInfoParser entityInfoParser = getEntityInfoParser();
		NamedNodeMap attributes = getNamedNodeMap(EntityInfoParser.TAG_ENTITY, 0);
		String name = entityInfoParser.parseNode(attributes, EntityInfoParser.ATTRIBUTE_NAME, true, EntityInfoParser.EXCEPTION_INFO_ENTITY, null);
		assertEquals("orm.Fruit", name);
		String table = entityInfoParser.parseNode(attributes, EntityInfoParser.ATTRIBUTE_TABLE, true, EntityInfoParser.EXCEPTION_INFO_ENTITY, null);
		assertEquals("ES_FRUIT", table);
		String bean = entityInfoParser.parseNode(attributes, EntityInfoParser.ATTRIBUTE_BEAN, true, EntityInfoParser.EXCEPTION_INFO_ENTITY, null);
		assertEquals("com.esen.jdbc.orm.Fruit", bean);
	}

	private NamedNodeMap getNamedNodeMap(String tag, int item) {
		Document doc = null;
		try {
			doc = XmlFunc.getDocument(new FileInputStream(
					"test/com/esen/jdbc/orm/fruit_mapping_right.xml"));
		} catch (Exception e) {
			fail();
	}

		NodeList elementsByTagName = doc.getElementsByTagName(tag);
		if (elementsByTagName == null) {
			throw new ORMException("com.esen.jdbc.orm.testentityinfoparser.3","没有找到名为“{0}”的标签！",new Object[]{tag});
		}
		Element el = (Element) elementsByTagName.item(item);
		if (el == null) {
			throw new ORMException("com.esen.jdbc.orm.testentityinfoparser.4","参数{0}不能为空！",new Object[]{"Element（el）"});
		}
		return el.getAttributes();
	}

}
