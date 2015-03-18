package com.esen.jdbc.orm.helper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.EntityInfoParser;
import com.esen.jdbc.orm.ORMException;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * 解析带有树形属性的XML
 *
 * @author wangshzh
 */
public class TreeEntityInfoParser extends EntityInfoParser {

	/**
	 * xml中的标签<tree ....> 树形的属性设置
	 */
	public static final String TAG_TREE = "tree";

	/**
	 * xml中的属性<tree root= ...> 根节点的upid
	 */
	public static final String ATTRIBUTE_ROOT = "root";

	/**
	 * xml中的属性<tree id= ...> id字段的属性名
	 */
	public static final String ATTRIBUTE_ID = "id";

	/**
	 * xml中的属性<tree upid= ...> upid字段的属性名
	 */
	public static final String ATTRIBUTE_UPID = "upid";

	/**
	 * xml中的属性<tree btype= ...> 是否叶子节点的标记
	 */
	public static final String ATTRIBUTE_BTYPE = "btype";

	/**
	 * xml中的属性<tree upids= ...> UPID的冗余字段
	 */
	public static final String ATTRIBUTE_UPIDS = "upids";

	/**
	 * 用在异常中的部分提示信息。
	 */
	//public static final String[] EXCEPTION_INFO_TREE = {"com.esen.jdbc.orm.helper.treeentityinfoparser.1","标签tree"};
	
	
	/**
	 * 通过XML根节点构造ScTreeEntityInfoParser
	 * @param doc XML根节点
	 */
	public TreeEntityInfoParser(Document doc) {
		super(doc);
	}

	/**
	 * 创建包含树形属性的实体对象Bean
	 * @return 包含树形属性的实体对象Bean
	 */
	@Override
	protected TreeEntityInfoBean createEntityInfoBean() {
		return new TreeEntityInfoBean();
	}

	/**
	 * {@inheritDoc}   
	 */
	@Override
	protected TreeEntityInfoBean parseEntityElement(Element el) {
		TreeEntityInfoBean treeEntityInfoBean = (TreeEntityInfoBean) super.parseEntityElement(el);

		NodeList nodeList = el.getElementsByTagName(TAG_TREE);
		if (nodeList != null) {		
			Element tree = (Element) nodeList.item(0);
			if (tree != null) {
				NamedNodeMap attributes = tree.getAttributes();
				if (attributes == null) {//it is an <code>Element</code>) or <code>null</code> otherwise.
					throw new ORMException("com.esen.jdbc.orm.helper.treeentityinfoparser.2","解析{0}时出错，得到的{1}不能为空！",new Object[]{"Element(tree)","attributes"});
				}
				String[] taginfo = {"com.esen.jdbc.orm.helper.treeentityinfoparser.4", "标签tree的{0}属性不能为空" };
				String rootValue = parseNode(attributes, ATTRIBUTE_ROOT, true, taginfo, new Object[]{ATTRIBUTE_ROOT});
				treeEntityInfoBean.setRootUpid(rootValue);
	
				String idValue = parseNode(attributes, ATTRIBUTE_ID, true, taginfo, new Object[]{ATTRIBUTE_ID});
				treeEntityInfoBean.setIdPropertyName(idValue);
				checkPropertyIsExist(treeEntityInfoBean, idValue, TAG_TREE, ATTRIBUTE_ID);
		
				String upidValue = parseNode(attributes, ATTRIBUTE_UPID, true, taginfo, new Object[]{ATTRIBUTE_UPID});
				treeEntityInfoBean.setUpIdPropertyName(upidValue);
				checkPropertyIsExist(treeEntityInfoBean, upidValue, TAG_TREE, ATTRIBUTE_UPID);
		
				String btypeValue = parseNode(attributes, ATTRIBUTE_BTYPE, false, null,null);
				if (!StrFunc.isNull(btypeValue)) {//没设这个值，就不管，设了这个值，就得能找到对应属性
					treeEntityInfoBean.setBtypePropertyName(btypeValue);
					checkPropertyIsExist(treeEntityInfoBean, btypeValue, TAG_TREE, ATTRIBUTE_BTYPE);
				}
		
				String upidsValue = parseNode(attributes, ATTRIBUTE_UPIDS, false, null,null);
				treeEntityInfoBean.setUpidsPropertyName(upidsValue);
				if (!StrFunc.isNull(upidsValue)) {//没设这个值，就不管，设了这个值，就得能找到对应属性
					String[] upids = upidsValue.split(",");
					for (String s : upids) {
						checkPropertyIsExist(treeEntityInfoBean, s, TAG_TREE, I18N.getString("com.esen.jdbc.orm.helper.treeentityinfoparser.3","{0}中{1}",new Object[]{ATTRIBUTE_UPIDS,s}));
					}
				}
			}
		}

		return treeEntityInfoBean;
	}

	protected void checkPropertyIsExist(EntityInfo entity, String propertyName, String tagName, String attributeName) {
		if (entity.getProperty(propertyName) == null) {
			throw new ORMException("com.esen.jdbc.orm.helper.treeentityinfoparser.5","{0}的{1}属性值没有对应的 property",new Object[]{tagName,attributeName});
		}
	}

}
