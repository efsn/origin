package org.codeyn.util.oservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.codeyn.util.exception.ExceptionHandler;


/**
 * 此类是ObjectFactoryBuilder的默认实现者，提供了加载属性列表的功能，默认的此类从系统的
 * src目录下的objfactory.properties和objfactory1.properties加载属性
 * 使用者也可以在系统初始化时调用loadPropertiesFile函数加载自己的配置文件
 * 
 * @see ObjectFactoryBuilder
 * @author yukun
 */
public class ObjectFactoryBuilderDefault implements ObjectFactoryBuilder {

	public ObjectFactoryBuilderDefault() {
		super();
		loadPropertiesFile("objfactory.properties");
		loadPropertiesFile("objfactory1.properties");
	}

	private static final ObjectFactoryBuilderDefault instance = new ObjectFactoryBuilderDefault();

	public static ObjectFactoryBuilderDefault getInstance() {
		return instance;
	}

	/**
	 * 名字对列表
	 */
	private Properties properties = new Properties();

	/**
	 * 创建指定的对象工厂，根据传入的objfactoryName在内部的属性列表中找到对应的实现者类名，并实例化它返回。
	 * 如果不存在对应的属性，那么return null
	 */
	public ObjectFactory createObjectFactory(String objfactoryName, String defFactory) {
		try {
			String fctcls = (String) getProperty(objfactoryName);
			if (fctcls == null || fctcls.length() == 0) {
				fctcls = defFactory;
			}
			if (fctcls == null || fctcls.length() == 0) {
				return null;
			}

			Class cls = Class.forName(fctcls);
			ObjectFactory factory = (ObjectFactory) cls.newInstance();
			Object initparams = getProperty(objfactoryName + ".init-params");
			if (initparams != null) {
				factory.setParam("init-params", initparams);
			}
			return factory;
		}
		catch (Exception ex) {
			ExceptionHandler.rethrowRuntimeException(ex);
			return null;
		}
	}

	/**
	 * 此函数是一个对函数createObjectFactory的简化调用，相当于：
	 * @param objfactoryName
	 * @return
	 */
	public Object createObject(String objfactoryName) {
		ObjectFactory f = createObjectFactory(objfactoryName, null);
		return f != null ? f.createObject() : null;
	}

	public synchronized Object getProperty(String key) {
		return properties.get(key);
	}

	public synchronized Object setProperty(String key, Object value) {
		return properties.put(key, value);
	}

	public synchronized void loadProperties(Properties p) {
		properties.putAll(p);
	}

	/**
	 * 加载src目录下指定的properties文件，如果文件不存在，则直接return
	 * @param fn 
	 */
	public void loadPropertiesFile(String fn) {
		try {
			InputStream in = ObjectFactoryBuilderDefault.class.getClassLoader().getResourceAsStream(fn);
			if (in != null) {
				try {
					properties.load(in);
				}
				finally {
					in.close();
				}
			}
		}
		catch (IOException e) {
			ExceptionHandler.rethrowRuntimeException(e);
		}
	}

}
