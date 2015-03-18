package com.esen.jdbc.orm;

import com.esen.exception.RuntimeException4I18N;

/**
 * ORM异常类
 * 增加对国际化支持
 *
 * @author wang
 */
public class ORMException extends RuntimeException4I18N {

	/**
	 * create by wang,2013-05-10 16:23:35
	 */
	private static final long serialVersionUID = -4501579467339072540L;


	public ORMException(Throwable cause){
		super(cause);
	}
	
	public ORMException(String key,String defaultValue, Throwable cause) {
		super(key,defaultValue, cause);
	}
	
		
	public ORMException(String key,String defaultValue) {
		super(key,defaultValue);
	}
	
	public ORMException(String key,String defaultValue,Object[] params, Throwable cause) {
		super(key,defaultValue,params, cause);
	}
	
		
	public ORMException(String key,String defaultValue,Object[] params) {
		super(key,defaultValue,params);
	}


}
