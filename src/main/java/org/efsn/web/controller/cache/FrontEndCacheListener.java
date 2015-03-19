package org.efsn.web.controller.cache;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class FrontEndCacheListener implements ServletContextListener{

    public void contextInitialized(ServletContextEvent event){
//        CacheManager cacheMgr = CacheManager.create();
//        event.getServletContext().setAttribute("cacheMgr", cacheMgr);
    }

    public void contextDestroyed(ServletContextEvent event){
//        CacheManager cacheMgr = (CacheManager) event.getServletContext().getAttribute("cacheMgr");
//        cacheMgr.shutdown();
    }

}
