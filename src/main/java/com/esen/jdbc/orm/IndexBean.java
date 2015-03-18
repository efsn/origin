package com.esen.jdbc.orm;

/**
 * 实体的索引对象
 *
 * @author wangshzh
 */
public class IndexBean implements Index {

	/**
	 * 索引名字
	 */
	private String indexName;

	/**
	 * 索引的字段名，多个字段用逗号分割
	 */
	private String indexFields;

	/**
	 * 索引是否唯一
	 */
	private boolean unique;

	/**  
	 * 设置索引名字  
	 * @param indexName 索引名字  
	 */
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	/**  
	 * 设置索引字段  
	 * @param indexFields 索引字段  
	 */
	public void setIndexFields(String indexFields) {
		this.indexFields = indexFields;
	}

	/**  
	 * 设置是否唯一  
	 * @param unique 是否唯一   
	 */
	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	/**
	 * {@inheritDoc}   
	 */
	public String getIndexName() {
		return this.indexName;
	}

	/**
	 * {@inheritDoc}   
	 */
	public String getIndexFields() {
		return this.indexFields;
	}

	/**
	 * {@inheritDoc}   
	 */
	public boolean isUnique() {
		return this.unique;
	}

}
