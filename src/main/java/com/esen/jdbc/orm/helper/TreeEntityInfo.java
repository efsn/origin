package com.esen.jdbc.orm.helper;

import com.esen.jdbc.orm.EntityInfo;

/**
 * 树形实体定义接口
 *   定义树形的属性
		  root(*): 根节点的upid
		    id(*)： id字段的属性名
	    upid(*)： upid字段的属性名
	      btype： 是否叶子节点的标记，一般0-非叶子节点，1-叶子节点
	      upids：UPID的冗余字段
		-->
		<tree root="-" id="upid" upid="upid" btype="btype" upids="upid0,upid1,upid2,upid3,upid4,upid5,upid6,upid7" />
 * @author wang
 */
public interface TreeEntityInfo extends EntityInfo {
	/**
	 * 机构  ID 为空的值
	 */
	public final static String UPID_NULL = "-";
	
	/**
	 * upids 属性名
	 */
	public final static String UpidsPropName = "upids";

	/**
	 * @return root节点的upid
	 */
	public String getRootUpid();

	/**
	 * @return ID字段的属性名
	 */
	public String getIdPropertyName();

	/**
	 * @return UPID的属性名
	 */
	public String getUpIdPropertyName();

	/**
	 * @return btype字段的属性名
	 */
	public String getBtypePropertyName();

	/**
	 * @return 冗余upids属性名，多个字段用逗号分割，upid0,upid1,upid2...
	 */
	public String getUpidsPropertyName();

}
