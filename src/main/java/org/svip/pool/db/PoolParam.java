package org.svip.pool.db;

import org.dom4j.Element;
import org.svip.util.StrUtil;

import java.util.Properties;

/**
 * @author Blues
 * @version 1.0
 * Created on 2014/8/24
 */
public class PoolParam {

    public static final int MINI_NUM = 3;
    public static final int MAXI_NUM = 15;
    public static final int TIMEOUT = 0;
    public static final int WAIT_TIME = 60 * 1000;

    private static final String DRIVER = "driver";
    private static final String URL = "url";
    private static final String USE_UNICODE = "useUnicode";
    private static final String CHARACTER_ENCODING = "characterEncoding";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String MINI_NUM_Str = "miniNum";
    private static final String MAXI_NUM_Str = "maxiNum";
    private static final String TIMEOUT_Str = "timeout";
    private static final String WAIT_TIME_Str = "waitTime";

    private String driver;
    private String url;
    private String useUnicode;
    private String encoding;
    private String username;
    private String password;

    private int miniNum;
    private int maxiNum;
    private int timeout;
    private int waitTime;

    public PoolParam(Properties pro) {
        init(pro);
    }

    public PoolParam(Element element) {
        init(element);
    }

    public PoolParam(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public PoolParam(String driver, String url, String username, String password, int miniNum, int maxiNum, int timeout, int waitTime) {
        this(driver, url, username, password);
        this.maxiNum = maxiNum;
        this.miniNum = miniNum;
        this.timeout = timeout;
        this.waitTime = waitTime;
    }

    @Override
    public boolean equals(Object pool) {
        if (this == pool) {
            return true;
        } else if (pool instanceof PoolParam) {
            return StrUtil.equals(url, ((PoolParam) pool).getDriver()) &&
                    StrUtil.equals(username, ((PoolParam) pool).getDriver());
        } else {
            return false;
        }
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        int len = url.length();
        StringBuffer sb = new StringBuffer(url);
        if (!StrUtil.isNull(useUnicode))
            sb.append(sb.length() > len ? useUnicode : "?").append(useUnicode);
        if (!StrUtil.isNull(encoding))
            sb.append(sb.length() > len ? encoding : "?").append(encoding);
        return sb.toString();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMiniNum() {
        return miniNum;
    }

    public void setMiniNum(int miniNum) {
        this.miniNum = miniNum;
    }

    public int getMaxiNum() {
        return maxiNum;
    }

    public void setMaxiNum(int maxiNum) {
        this.maxiNum = maxiNum;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    private void init(Element element) {
        this.driver = element.elementText(DRIVER);
        this.url = element.elementText(URL);
        this.username = element.elementText(USERNAME);
        this.password = element.elementText(PASSWORD);
        String mini = element.elementText(MINI_NUM_Str);
        this.miniNum = StrUtil.isNull(mini) ? MINI_NUM : Integer.parseInt(mini);
        String maxi = element.elementText(MAXI_NUM_Str);
        this.maxiNum = StrUtil.isNull(maxi) ? MAXI_NUM : Integer.parseInt(maxi);
        String to = element.elementText(TIMEOUT_Str);
        this.timeout = StrUtil.isNull(to) ? TIMEOUT : Integer.parseInt(to);
        String wt = element.elementText(WAIT_TIME_Str);
        this.waitTime = StrUtil.isNull(wt) ? WAIT_TIME : Integer.parseInt(wt);
    }

    private void init(Properties pro) {
        this.driver = pro.getProperty(DRIVER);
        this.url = pro.getProperty(URL);
        this.useUnicode = pro.getProperty(USE_UNICODE);
        this.encoding = pro.getProperty(CHARACTER_ENCODING);
        this.username = pro.getProperty(USERNAME);
        this.password = pro.getProperty(PASSWORD);
        String mini = pro.getProperty(MINI_NUM_Str);
        this.miniNum = StrUtil.isNull(mini) ? MINI_NUM : Integer.parseInt(mini);
        String maxi = pro.getProperty(MAXI_NUM_Str);
        this.maxiNum = StrUtil.isNull(maxi) ? MAXI_NUM : Integer.parseInt(maxi);
        String to = pro.getProperty(TIMEOUT_Str);
        this.timeout = StrUtil.isNull(to) ? TIMEOUT : Integer.parseInt(to);
        String wt = pro.getProperty(WAIT_TIME_Str);
        this.waitTime = StrUtil.isNull(wt) ? WAIT_TIME : Integer.parseInt(wt);
    }
}
