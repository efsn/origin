package org.efsn.web.controller.cache;

import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import net.sf.ehcache.constructs.blocking.LockTimeoutException;
import net.sf.ehcache.constructs.web.AlreadyCommittedException;
import net.sf.ehcache.constructs.web.AlreadyGzippedException;
import net.sf.ehcache.constructs.web.filter.FilterNonReentrantException;
import net.sf.ehcache.constructs.web.filter.SimplePageCachingFilter;

public class CachePageFilter extends SimplePageCachingFilter{

    private static final Logger log = Logger.getLogger(CachePageFilter.class);
    private static final String PATTERNS = "patterns";
    
    private String[] cacheUrls;
    
    @Override
    protected String getCacheName(){
        return "cachePageFilter";
    }
    
    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain)throws AlreadyGzippedException,
                                                     AlreadyCommittedException,
                                                     FilterNonReentrantException, 
                                                     LockTimeoutException, Exception{
        if(cacheUrls == null) init();
        String uri = request.getRequestURI();
        boolean flag = false;
        if(cacheUrls != null && cacheUrls.length > 0){
            for(String cacheUrl : cacheUrls){
                if(uri.contains(cacheUrl)){
                    flag = true;
                    break;
                }
            }
        }
        if(flag){
            //matches get from cache
            String query = request.getQueryString();
            if(query != null) query = "?" + query;
            log.info("Current request has been cached:" + uri + query);
            //cacheFilter.doFilter(buildPageInfo->calcKey->
            //cache.get(true->writeResponse else buildPage(chain.doFilter(forward to controller -> new PageInfo ->
            //writeResponse
            super.doFilter(request, response, chain);
        }else{
            chain.doFilter(request, response);
        }
    }
    
    /**
     * determine accept-encoding gzip, special for IE 6 or 7
     */
    @Override
    protected boolean acceptsGzipEncoding(HttpServletRequest request){
        return super.acceptsGzipEncoding(request) || acceptsGzipEncoding(request, "User-Agent", "MSIE 6.0") ||
                acceptsGzipEncoding(request, "User-Agent", "MSIE 7.0");
    }
    
    private void init(){
        String patterns = filterConfig.getInitParameter(PATTERNS);
        this.cacheUrls = patterns.split(",");
    }
    
    private boolean acceptsGzipEncoding(HttpServletRequest request, String header, String value){
        logRequestHeaders(request);
        Enumeration<String> itr = request.getHeaders(header);
        while(itr.hasMoreElements()){
            if(itr.nextElement().indexOf(value) > -1)
                return true;
        }
        return false;
    }
    
}
