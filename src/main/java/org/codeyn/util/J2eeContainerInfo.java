package org.codeyn.util;

import java.io.File;

/**
 * 访问web服务器的抽象类，要根据不同的服务器类型提供不同的实现。
 */

public abstract class J2eeContainerInfo {

    /**
     *
     */
    public J2eeContainerInfo() {
        super();
    }

    public static J2eeContainerInfo getInstance() {
        return null;
    }

    /**
     * 重新启动这个服务器
     */
    public abstract void restart();

    /**
     * 获得web服务器的日志文件列表。
     */
    public abstract File[] getLogFiles();

    /**
     * 返回tomcat weblogic websphere等串。
     */
    public abstract String getType();

    /**
     * 返回5.0 5.5 7.0等串。
     */
    public abstract String getVersion();
}
