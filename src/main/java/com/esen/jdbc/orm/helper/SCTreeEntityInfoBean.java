package com.esen.jdbc.orm.helper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 带缓慢增长属性的树形实体
 *
 * @author wangshzh
 */
public class SCTreeEntityInfoBean extends TreeEntityInfoBean implements SCTreeEntityInfo {

	/**
	 * 数据开始有效期
	 */
	private String fromDatePropertyName;

	/**
	 * 数据结束有效期
	 */
	private String toDatePropertyName;

	/**  
	 * 设置数据开始有效期  
	 * @param fromDatePropertyName 数据开始有效期  
	 */
	protected void setFromDatePropertyName(String fromDatePropertyName) {
		this.fromDatePropertyName = fromDatePropertyName;
	}

	/**  
	 * 设置数据结束有效期  
	 * @param toDatePropertyName 数据结束有效期  
	 */
	protected void setToDatePropertyName(String toDatePropertyName) {
		this.toDatePropertyName = toDatePropertyName;
	}

	/**
	 * {@inheritDoc}   
	 */
	public String getFromDatePropertyName() {
		return this.fromDatePropertyName;
	}

	/**
	 * {@inheritDoc}   
	 */
	public String getToDatePropertyName() {
		return this.toDatePropertyName;
	}

	/**
	 * {@inheritDoc}   
	 */
	public String getIdPropertyName() {
		return super.getPrimaryKey().getName();
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void addExtendedNode(Document doc, Element entity) {
		super.addExtendedNode(doc, entity);
		
		Element scinfo = addNode(doc, entity, SCTreeEntityInfoParser.TAG_SLOWLYCHANGE);		
		scinfo.setAttribute(SCTreeEntityInfoParser.ATTRIBUTE_FROMDATE, this.fromDatePropertyName);
		scinfo.setAttribute(SCTreeEntityInfoParser.ATTRIBUTE_TODATE, this.toDatePropertyName);
	}
}
