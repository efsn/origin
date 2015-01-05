package org.codeyn.util.oservice;


/**
 * 系统在项目实施的过程中往往需要和第三方的应用集成，需要使用外部的代码实现去定制内部的某些逻辑
 * 此时我们可以定义一个接口，由外部应用实现，然后通过某种配置可以在系统内部获取到外部实现的类的
 * 实例，而此类就是为此目的，此接口的唯一目的就是构造一个ObjectFactory的实例
 * 
 * 此接口有一个默认的实现者：ObjectFactoryBuilderDefault，请使用它而不是自己实现一个此接口。
 * 
 * @see ObjectFactoryBuilderDefault
 */
public interface ObjectFactoryBuilder {
	/**
	 * factoryName属性名，通过此属性名找到具体的实现类的类名，然后实例化它并返回，如果不存在此属性，那么返回null
	 **/
	public ObjectFactory createObjectFactory(String factoryName, String defFactory);
}
