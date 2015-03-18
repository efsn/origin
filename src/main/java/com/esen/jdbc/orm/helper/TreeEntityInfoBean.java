package com.esen.jdbc.orm.helper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.esen.jdbc.orm.EntityInfoBean;

/**
 * 定义树形
 *
 * @author wangshzh
 */
public class TreeEntityInfoBean extends EntityInfoBean implements TreeEntityInfo {

	/**
	 * 根节点的upid
	 */
	private String rootUpid;

	/**
	 * id字段的属性名
	 */
	private String idPropertyName;

	/**
	 * upid字段的属性名
	 */
	private String upIdPropertyName;

	/**
	 * 是否叶子节点的标记，一般0-非叶子节点，1-叶子节点 
	 */
	private String btypePropertyName;

	/**
	 * UPID的冗余字段
	 */
	private String upidsPropertyName;

	/**
	 * {@inheritDoc}   
	 */
	public String getRootUpid() {
		return rootUpid;
	}

	/**  
	 * 设置根节点的upid  
	 * @param rootUpid 根节点的upid  
	 */
	protected void setRootUpid(String rootUpid) {
		this.rootUpid = rootUpid;
	}

	/**
	 * {@inheritDoc}   
	 */
	public String getIdPropertyName() {
		return idPropertyName;
	}

	/**  
	 * 设置id字段的属性名    
	 * @param idPropertyName id字段的属性名    
	 */
	protected void setIdPropertyName(String idPropertyName) {
		this.idPropertyName = idPropertyName;
	}

	/**
	 * {@inheritDoc}   
	 */
	public String getUpIdPropertyName() {
		return upIdPropertyName;
	}

	/**  
	 * 设置upid字段的属性名  
	 * @param upIdPropertyName upid字段的属性名  
	 */
	protected void setUpIdPropertyName(String upIdPropertyName) {
		this.upIdPropertyName = upIdPropertyName;
	}

	/**
	 * {@inheritDoc}   
	 */
	public String getBtypePropertyName() {
		return btypePropertyName;
	}

	/**  
	 * 设置是否叶子节点的标记，一般0-非叶子节点，1-叶子节点  
	 * @param btypePropertyName 是否叶子节点的标记  
	 */
	protected void setBtypePropertyName(String btypePropertyName) {
		this.btypePropertyName = btypePropertyName;
	}

	/**
	 * {@inheritDoc}   
	 */
	public String getUpidsPropertyName() {
		return upidsPropertyName;
	}

	/**  
	 * 设置UPID的冗余字段 
	 * @param upidsPropertyName UPID的冗余字段 
	 */
	protected void setUpidsPropertyName(String upidsPropertyName) {
		this.upidsPropertyName = upidsPropertyName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void addExtendedNode(Document doc, Element entity) {
		Element treeinfo = addNode(doc, entity, TreeEntityInfoParser.TAG_TREE);
		
		treeinfo.setAttribute(TreeEntityInfoParser.ATTRIBUTE_ROOT, this.rootUpid);
		treeinfo.setAttribute(TreeEntityInfoParser.ATTRIBUTE_ID, this.idPropertyName);
		treeinfo.setAttribute(TreeEntityInfoParser.ATTRIBUTE_UPID, this.upIdPropertyName);
		treeinfo.setAttribute(TreeEntityInfoParser.ATTRIBUTE_BTYPE, this.btypePropertyName);
		treeinfo.setAttribute(TreeEntityInfoParser.ATTRIBUTE_UPIDS, this.upidsPropertyName);
	}
}
