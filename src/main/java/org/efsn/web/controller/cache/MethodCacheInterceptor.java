package org.efsn.web.controller.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.codeyn.util.GUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class MethodCacheInterceptor implements MethodInterceptor, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(MethodCacheInterceptor.class);

    private Cache cache;

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info(cache + " A cache is required");
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String target = invocation.getThis().getClass().getName();
        String method = invocation.getMethod().getName();
        Object[] arguments = invocation.getArguments();
        Object value;
        String key = getCacheKey(target, method, arguments);
        Element element;
        synchronized (this) {
            element = cache.get(key);
            if (element == null) {
                log.info(key + " add to cache: " + cache.getName());
                value = invocation.proceed();
                element = new Element(key, value);
                cache.put(element);
            } else {
                log.info(key + " get from cache: " + cache.getName());
            }
        }
        return element.getValue();
    }

    private String getCacheKey(String target, String method, Object... arguments) {
        StringBuffer sb = new StringBuffer(target).append(method);
        for (Object arg : arguments) {
            sb.append(arg);
        }
        return GUID.makeGuid(sb.toString());
    }

}
