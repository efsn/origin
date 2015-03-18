package com.esen.jdbc.orm.helper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.esen.jdbc.orm.ORMException;

/**
 * 解析带有缓慢增长的树形属性的XML
 *
 * @author wangshzh
 */
public class SCTreeEntityInfoParser extends TreeEntityInfoParser {

	/**
	 * xml中的标签<slowlychange ....> 缓慢增长属性设置
	 */
	public static final String TAG_SLOWLYCHANGE = "slowlychange";

	/**
	 * xml中的属性<slowlychange fromdate= ...> 数据开始有效期
	 */
	public static final String ATTRIBUTE_FROMDATE = "fromdate";

	/**
	 * xml中的属性<slowlychange todate= ...> 数据结束有效期
	 */
	public static final String ATTRIBUTE_TODATE = "todate";

	/**
	 * 用在异常中的部分提示信息。
	 */
	//public static final String[] EXCEPTION_INFO_SLOWLYCHANGE = {"com.esen.jdbc.orm.helper.sctreeentityinfoparser.1","标签slowlychange"};
	

	/**
	 * 通过XML根节点构造SCTreeEntityInfoParser
	 * @param doc XML根节点
	 */
	public SCTreeEntityInfoParser(Document doc) {
		super(doc);
	}

	/**
	 * 创建实体对象Bean
	 * @return 实体对象Bean
	 */
	@Override
	protected SCTreeEntityInfoBean createEntityInfoBean() {
		return new SCTreeEntityInfoBean();
	}

	/**
	 * {@inheritDoc}   
	 */
	@Override
	protected SCTreeEntityInfoBean parseEntityElement(Element el) {
		SCTreeEntityInfoBean scTreeEntityInfoBean = (SCTreeEntityInfoBean) super.parseEntityElement(el);//父类会判空

		NodeList nodeList = el.getElementsByTagName(TAG_SLOWLYCHANGE);
		if (nodeList != null) {
			Element slowlyChange = (Element) nodeList.item(0);
			if (slowlyChange != null) {	
				NamedNodeMap attributes = slowlyChange.getAttributes();
				if (attributes == null) {//it is an <code>Element</code>) or <code>null</code> otherwise.
					throw new ORMException("com.esen.jdbc.orm.helper.sctreeentityinfoparser.2","解析{0}时出错，得到的{1}不能为空！",new Object[]{"Element(slowlyChange)","attributes"});
				}
				String[] taginfo = {"com.esen.jdbc.orm.helper.sctreeentityinfoparser.3", "标签slowlychange的{0}属性不能为空" };
				String fromDatePropertyName = parseNode(attributes, ATTRIBUTE_FROMDATE, true,taginfo, new Object[]{ATTRIBUTE_FROMDATE});
				scTreeEntityInfoBean.setFromDatePropertyName(fromDatePropertyName);
				checkPropertyIsExist(scTreeEntityInfoBean, fromDatePropertyName, TAG_SLOWLYCHANGE, ATTRIBUTE_FROMDATE);
		
				String toDatePropertyName = parseNode(attributes, ATTRIBUTE_TODATE, true, taginfo, new Object[]{ATTRIBUTE_TODATE});
				scTreeEntityInfoBean.setToDatePropertyName(toDatePropertyName);
				checkPropertyIsExist(scTreeEntityInfoBean, toDatePropertyName, TAG_SLOWLYCHANGE, ATTRIBUTE_TODATE);
			}
		}

		return scTreeEntityInfoBean;
	}

}
