package org.codeyn.util.oservice;


/**
 * 一个通用的工厂类，请参考ObjectFactoryBuilder的说明
 *
 * @author yk
 * @see ObjectFactoryBuilder
 */
public interface ObjectFactory {

    /**
     * 创建一个对象的实例。
     */
    Object createObject();

    /**
     * 创建一个对象的实例。便于创建某些对象时需要构造参数
     *
     * @param createParams
     * @return
     */
    Object createObject(Object createParams);

    /**
     * 在创建实例前可以先调用此函数初始化某些创建实例时需要用到的信息。
     */
    void setParam(String name, Object value);

}
