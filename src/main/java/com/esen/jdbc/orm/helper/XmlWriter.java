package com.esen.jdbc.orm.helper;

import java.io.OutputStream;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.Index;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.Property;
import com.esen.util.StrFunc;
import com.esen.util.XmlFunc;

/**
 * 将 ORM 里的实体信息写入到 DbDefiner 规定格式的 xml 文件中
 *
 * @author wangshzh
 */
public class XmlWriter {
	/**
	 * xml中的标签<tablemeta ....>
	 */
	public static final String TAG_TABLEMETA = "tablemeta";

	/**
	 * xml中的属性<tablemeta tablename= ...>
	 */
	public static final String ATTRIBUTE_TABLENAME = "tablename";

	/**
	 * xml中的属性<tablemeta primarykey= ...>
	 */
	public static final String ATTRIBUTE_PRIMARYKEY = "primarykey";

	/**
	 * xml中的标签<fields >
	 */
	public static final String TAG_FIELDS = "fields";

	/**
	 * xml中的标签<field ....>
	 */
	public static final String TAG_FIELD = "field";

	/**
	 * xml中的属性<field fieldname= ...>
	 */
	public static final String ATTRIBUTE_FIELDNAME = "fieldname";

	/**
	 * xml中的属性<field fielddesc= ...>
	 */
	public static final String ATTRIBUTE_FIELDDESC = "fielddesc";

	/**
	 * xml中的属性<field sqltype= ...>
	 */
	public static final String ATTRIBUTE_SQLTYPE = "sqltype";

	/**
	 * xml中的属性<field len= ...>
	 */
	public static final String ATTRIBUTE_LEN = "len";

	/**
	 * xml中的属性<field scale= ...>
	 */
	public static final String ATTRIBUTE_SCALE = "scale";

	/**
	 * xml中的属性<field autoinc= ...>
	 */
	public static final String ATTRIBUTE_AUTOINC = "autoinc";

	/**
	 * xml中的属性<field nullalbe= ...>
	 */
	public static final String ATTRIBUTE_NULLABLE = "nullable";

	/**
	 * xml中的属性<field unique= ...>
	 */
	public static final String ATTRIBUTE_UNIQUE = "unique";

	/**
	 * xml中的属性<field defaultvalue= ...>
	 */
	public static final String ATTRIBUTE_DEFAULTVALUE = "defaultvalue";

	/**
	 * xml中的属性<field newfieldname= ...>
	 */
	public static final String ATTRIBUTE_NEWFIELDNAME = "newfieldname";

	/**
	 * xml中的标签<indexes ....>
	 */
	public static final String TAG_INDEXES = "indexes";

	/**
	 * xml中的标签<index ....>
	 */
	public static final String TAG_INDEX = "index";

	/**
	 * xml中的属性<index indexname= ...>
	 */
	public static final String ATTRIBUTE_INDEXNAME = "indexname";

	/**
	 * xml中的属性<index fields= ...>
	 */
	public static final String ATTRIBUTE_INDEX_FIELDS = "fields";

	/**
	 * xml中的属性<index unique= ...>
	 */
	public static final String ATTRIBUTE_INDEX_UNIQUE = "unique";
	
	/**
	 * 将实体信息保存到Dbdefiner规定格式的xml的Document中。
	 * @param entity 实体
	 * @return 规定格式的xml的Document
	 */
	public static Document entityToDbdfinerDoc(EntityInfo entity) {
		Document doc = null;

		try {
			doc = XmlFunc.createDocument(TAG_TABLEMETA);
		} catch (Exception e) {
			throw new ORMException("com.esen.jdbc.orm.helper.xmlwriter.1","创建 Document 失败", e);
		}

		Element root = doc.getDocumentElement();
		root.setAttribute(ATTRIBUTE_TABLENAME, entity.getTable());

		Property primaryKeyPro = entity.getPrimaryKey();
		//缓慢变化定义时的主键不能当做真正的主键
		if (entity instanceof SCEntityInfo
				&& !StrFunc.isNull(((SCEntityInfo)entity).getFromDatePropertyName())) {
			;
		} else {
			root.setAttribute(ATTRIBUTE_PRIMARYKEY, (primaryKeyPro == null ? "" : primaryKeyPro.getFieldName()));
		}

		Element fields = addNode(doc, root, TAG_FIELDS);
		List<Property> allProperties = entity.getProperties();
		for (Property tempProperty : allProperties) {
			Element field = addNode(doc, fields, TAG_FIELD);

			field.setAttribute(ATTRIBUTE_FIELDNAME, tempProperty.getFieldName());

			if (!StrFunc.isNull(tempProperty.getCaption())) {
				field.setAttribute(ATTRIBUTE_FIELDDESC, tempProperty.getCaption());
			}

			if (tempProperty.isUnique()) {
				if (primaryKeyPro != null && primaryKeyPro.getName().compareTo(tempProperty.getName()) == 0) {
					field.setAttribute(ATTRIBUTE_UNIQUE, "0");
				} else {
					field.setAttribute(ATTRIBUTE_UNIQUE, tempProperty.isUnique()? "1" : "0");
				}
			}

			if (!tempProperty.isNullable()) {
				field.setAttribute(ATTRIBUTE_NULLABLE, tempProperty.isNullable()? "1" : "0");
			}

			char typeValue = tempProperty.getType();
			field.setAttribute(ATTRIBUTE_SQLTYPE, String.valueOf(tempProperty.getType()));
			if (typeValue == Property.FIELD_TYPE_INT || typeValue == Property.FIELD_TYPE_STR
					|| typeValue == Property.FIELD_TYPE_FLOAT) {
				field.setAttribute(ATTRIBUTE_LEN, Integer.toString(tempProperty.length()));
			}

			if (typeValue == Property.FIELD_TYPE_INT && tempProperty.isAutoInc()) {
				field.setAttribute(ATTRIBUTE_AUTOINC, "1");
			}

			if (typeValue == Property.FIELD_TYPE_FLOAT) {
				field.setAttribute(ATTRIBUTE_SCALE, Integer.toString(tempProperty.getScale()));
			}
		}

		/* 索引
		 * <indexes>
		 *     <index indexname="index_name" unique="1" fields="userid_,bbq_,btype_"/>
		 *     <!--索引名，是否唯一索引(0,1)，索引字段逗号分割…-->
		 * </indexes>
		 */
		List<Index> allIndexes = entity.listIndexes();
		if (allIndexes != null && allIndexes.size() > 0) {
			Element indexes = addNode(doc, root, TAG_INDEXES);
			
			for (Index tempIndex : allIndexes) {
				Element index = addNode(doc, indexes, TAG_INDEX);
	
				index.setAttribute(ATTRIBUTE_INDEXNAME, tempIndex.getIndexName());
				index.setAttribute(ATTRIBUTE_INDEX_FIELDS, tempIndex.getIndexFields());
				if (tempIndex.isUnique()) {
					index.setAttribute(ATTRIBUTE_INDEX_UNIQUE, "1");
				} else {
					index.setAttribute(ATTRIBUTE_INDEX_UNIQUE, "0");
				}
			}
		}

		return doc;
	}

	/**
	 * 将Document保存到xml文件流
	 * @param doc 要保存的Document
	 * @param out 保存到的文件
	 */
	public static void saveToDbdfinerXml(Document doc, OutputStream out) {
		try {
			XmlFunc.saveDocument(doc, out, "utf-8");
		} catch (Exception e) {
			throw new ORMException("com.esen.jdbc.orm.helper.xmlwriter.2","将 Document 保存到输出流时失败！", e);
		}
	}

	/**
	 * 添加子节点
	 * @param doc xml的Document对象
	 * @param node 父节点
	 * @param name 子节点名称
	 * @return 子节点
	 */
	private static Element addNode(Document doc, Node node, String name) {
		if (doc != null && node != null && name != null) {
			Element e = doc.createElement(name);
			e.appendChild(doc.createTextNode(""));
			node.appendChild(e);
			return e;
		} else {
			throw new ORMException("com.esen.jdbc.orm.helper.xmlwriter.3","添加节点{0}时失败！",new Object[]{name});
		}
	}

}
