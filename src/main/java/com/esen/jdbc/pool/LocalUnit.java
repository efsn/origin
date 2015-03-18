package com.esen.jdbc.pool;

/**
 * 配合ThreadLocal使用，用于将ThreadLocal中用于同步的对象删除；
 * 原因是ThreadLocal的jdk1.4版本没有提供remove方法；
 * 
 * 用法：ThreadLocal总保存LocalUnit对象，使用LocalUnit对象保存需要同步的对象；
 * @author dw
 */
public class LocalUnit {
	private Object obj;
	public final void set(Object o){
		obj = o;
	}
	
	public final Object get(){
		return obj;
	}
	
	public final Object remove(){
		Object o = obj;
		obj = null;
		return o;
	}
}
