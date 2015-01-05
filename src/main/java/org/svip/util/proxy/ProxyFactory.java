package org.svip.util.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Blues
 * @version 1.0
 * Created on 2014/8/24
 */
public class ProxyFactory{
    public static Object getProxy(final Object target, final Advice advice){
        return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), new InvocationHandler(){
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable{
                advice.before();
                Object obj = method.invoke(target, args);
                advice.after();
                return obj;
            }
        });
    }
}
