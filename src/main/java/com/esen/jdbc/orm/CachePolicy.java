package com.esen.jdbc.orm;

import java.io.Serializable;

/**
 * 对象缓存策略
 *
 * @author wang
 */
public interface CachePolicy extends Serializable {

	/**
	 * 获取缓存策略名
	 * 
	 * @return 缓存策略名
	 */
	String getName();
	
	/**
	 * 是否开启SQL缓存，true 表示是， false 表示否
	 * 
	 * @return 是否开启SQL缓存
	 */
	boolean isEnable();
	
	/**
	 * 缓存对象最大数量
	 * 
	 * @return 缓存对象最大数量
	 */
	int getMaxsize();
	
	/**
	 * 对象空闲时间 ，单位为秒
	 * 
	 * @return 对象空闲时间
	 */
	int getIdleTime();
	
	/**
	 * 内存不够时是否写入磁盘
	 * 	
	 * @return 是否写入磁盘
	 */
	boolean isOverflowToDisk();
	
	/**
	 * 缓存对象生存时间，单位为秒
	 * 
	 * @return 生存时间
	 */
	int getLiveTime();	
	
	/**
	 * 获取缓存策略的类型名字，默认为 LRU
	 */
	String getPolicy();
}
