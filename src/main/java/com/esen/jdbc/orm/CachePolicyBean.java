package com.esen.jdbc.orm;

import com.esen.util.StrFunc;

import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * 对象缓存策略
 *
 * @author liujin
 */
public class CachePolicyBean implements CachePolicy {
	/**
	 * 缓存策略名，用 java bean 的名称表示
	 */
	private String name;

	/**
	 * 缓存策略
	 */
	private String policy = MemoryStoreEvictionPolicy.LRU.toString();

	/**
	 * 是否开启SQL缓存
	 */
	private boolean enable;

	/**
	 * 缓存对象最大数量
	 */
	private int maxsize = ALLOW_MAXSIZE;

	/**
	 * 对象空闲时间 ，单位为秒
	 */
	private int idle;

	/**
	 * 对象生存时间，单位为 秒
	 */
	private int liveTime;
	
	/**
	 * 内存不够时是否写入磁盘
	 */
	private boolean overflowToDisk = false;

	/**
	 * 默认缓存对象最大数量,不能超过此数目
	 */
	public static final int ALLOW_MAXSIZE = 10000;

	/**
	 * 
	 */
	public static final int DEFAULT_IDLETIME = 120;
	
	/**
	 * 
	 */
	public static final int DEFAULT_LIVETIME = 120;

	/**
	 * {@inheritDoc}   
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 设置缓存策略名
	 * 
	 * @param name 缓存策略名
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * {@inheritDoc}   
	 */
	public int getIdleTime() {
		return idle;
	}

	/**
	 * 设置缓存对象空闲时间，单位为秒
	 * 
	 * @param idle 空闲时间
	 */
	public void setIdleTime(int idle) {
		this.idle = idle;
	}

	/**
	 * {@inheritDoc}   
	 */
	public int getMaxsize() {
		return maxsize;
	}

	/**
	 * 设置缓存对象最大个数
	 * 
	 * @param maxsize 最大个数
	 */
	public void setMaxsize(int maxsize) {
		this.maxsize = maxsize;
	}

	/**
	 * {@inheritDoc}   
	 */
	public boolean isEnable() {
		return enable;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isOverflowToDisk() {
		return this.overflowToDisk;
	}
	
	/**
	 * 设置是否存放到磁盘
	 * 
	 * @param overflowToDisk
	 */
	public void setOverflowToDisk(boolean overflowToDisk) {
		this.overflowToDisk = overflowToDisk;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getLiveTime() {
		return this.liveTime;
	}
	
	/**
	 * 设置生存时间，单位为秒
	 * 
	 * @param live 生存时间
	 */
	public void setLiveTime(int live) {
		this.liveTime = live;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPolicy() {
		return this.policy;
	}

	/**
	 * 设置缓存策略
	 * 
	 * @param name 缓存策略名
	 */
	public void setPolicy(String policy) {
		if (StrFunc.isNull(policy)) {
			return;
		}
		this.policy = MemoryStoreEvictionPolicy.fromString(policy).toString();
	}
}
