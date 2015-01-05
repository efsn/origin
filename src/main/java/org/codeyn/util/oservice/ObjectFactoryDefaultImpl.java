package org.codeyn.util.oservice;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.codeyn.util.exception.ExceptionHandler;
import org.codeyn.util.i18n.I18N;

/**
 * 这是ObjectFactory的一个默认的实现类，它利用反射机制在需要创建对象的地方构建对象
 * 
 * @author yukun
 *
 */
public class ObjectFactoryDefaultImpl implements ObjectFactory, Serializable {

	private Object[] initParams;

	private Class objcls;

	private Constructor constructor;

	/**
	 * 构建一个此对象。
	 * 
	 * @param objcls 表示此ObjectFactory产生的对象的类型，此类的createObject方法将利用反射机制构造其返回值
	 * @param initParams 表示构造objcls对象实例时所传递的构造参数，可以为null，表示无构造参数
	 */
	public ObjectFactoryDefaultImpl(Class objcls, Object initParams) {
		super();
		this.objcls = objcls;
		this.initParams = initParams != null ? new Object[] { initParams } : null;
		this.constructor = findConstructor(objcls, initParams);
	}

	private Constructor findConstructor(Class objcls, Object initParams) {
		Constructor[] cons = objcls.getConstructors();
		for (int i = 0; i < cons.length; i++) {
			Class[] paramdefs = cons[i].getParameterTypes();
			if (initParams == null && paramdefs.length == 0) {
				return cons[i];
			}
			else if (initParams != null && paramdefs.length == 1 && paramdefs[0].isInstance(initParams)) {
				return cons[i];
			}
			//TODO 当initParams是数组时寻找匹配的构造器
		}

		//throw new UnsupportedOperationException("没有找到合适的构造方法:" + objcls);
		throw new UnsupportedOperationException(I18N.getString("com.esen.util.objectfactorydefaultimpl.exp", "没有找到合适的构造方法:{0}",new Object[]{objcls}));
	}

	public Object createObject() {
		try {
			return createInstance(constructor, initParams);
		}
		catch (Exception e) {
			ExceptionHandler.rethrowRuntimeException(e);
			return null;
		}
	}

	/**
	 * 根据传入的参数构造返回的对象，此函数会尽量寻找与所传参数相匹配的构造器，找不到合适的构造器时抛出异常
	 */
	public Object createObject(Object createParams) {
		try {
			if (createParams == null) {
				return createInstance(findConstructor(objcls, createParams), null);
			}

			if (createParams instanceof Object[]) {
				return createInstance(findConstructor(objcls, createParams), (Object[]) createParams);
			}

			return createInstance(findConstructor(objcls, createParams), new Object[] { createParams });
		}
		catch (Exception e) {
			ExceptionHandler.rethrowRuntimeException(e);
			return null;
		}
	}
	
	protected Object createInstance(Constructor con, Object[] initargs) throws IllegalArgumentException,
			InstantiationException, IllegalAccessException, InvocationTargetException {
		return con.newInstance(initargs);
	}

	public void setParam(String name, Object value) {
		throw new UnsupportedOperationException("setParam");
	}

}
