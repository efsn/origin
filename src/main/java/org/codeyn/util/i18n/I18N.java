package org.codeyn.util.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.codeyn.util.ClassPathSearcher;
import org.codeyn.util.exception.ExceptionHandler;
import org.codeyn.util.yn.ArrYn;
import org.codeyn.util.yn.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 这是国际化的工具类。需要国际化的字符串通过调用此类返回国际化的语言串。 例如：
 * 
 * <pre>
 * I18N.getString(&quot;com.esen.mykey&quot;, &quot;请输入数据&quot;);
 * </pre>
 * 
 *
 */
public final class I18N{

    private static final Logger logger = LoggerFactory.getLogger(I18N.class);

    private static final String I18N_BUNDLE_PATTERN = "/com/esen/**/i18n-*-bundle.properties";

    // 记录支持语言的配置文件
    private static final String SUPPORTLANG = "supportlang.properties";

    private static Set<String> bundleBaseNames = new HashSet<String>();

    public final static String[] LANGUAGE_CODES = {"zh_CN", "zh_TW", "en"};

    public final static String[] LANGUAGE_NAMES = new String[] {"简体中文", "繁體中文", "English"};

    /**
     * 支持的Locale
     */
    public final static Locale[] LOCALES = new Locale[] {
            Locale.SIMPLIFIED_CHINESE, Locale.TRADITIONAL_CHINESE,
            Locale.ENGLISH};

    /*
     * 从supportlang.properties中读出的支持的语言
     */
    private static Locale[] supports = LOCALES;

    /**
     * 取得系统默认的locale信息。 当只支持一种语言时，默认为所支持的那种语言
     */
    private static Locale defaultLocale = null;

    static {
        initSupportLang();
        initResourceBundleNames();
    }

    public static String getLanguageName(String langCode){
        return LANGUAGE_NAMES[ArrYn.find(LANGUAGE_CODES, langCode)];
    }

    /**
     * 根据键名获取国际化资源串。
     * 当objfactory.properties中配置了ResourceBundleFactory时可以采用此方法。否则不要使用。
     * 
     * @param key
     *            资源键
     * @return
     */
    public static String getString(String key){
        return getString(key, (String) null);
    }

    public static String getString(String key, String defValue){
        return getString(key, defValue, new Object[0]);
    }

    public static String getString(String key, String defValue,
            Object... params){
        return getString(key, defValue, null, params);
    }

    public static void setDefaultLocale(Locale l){
        defaultLocale = l;
    }

    public static synchronized Locale getDefaultLocale(){
        if (defaultLocale == null) {
            defaultLocale = getSupportLocale(Locale.getDefault(), supports[0]);
        }
        return defaultLocale;
    }

    /**
     * 根据键名获取国际化资源串。
     * 
     * @param key
     *            资源键
     * @param defFactoryClass
     *            缺省的ResourceBundleFactory类。当objfactory.
     *            properties中没有指定ResourceBundleFactory时，就采用默认的。
     * @param locale
     *            语言环境
     * @param params
     *            格式化用的参数。如资源串中出现的{0}, {1}...，通过此参数来格式化。
     * @return
     */
    public static String getString(String key, String defaultValue,
            Locale locale, Object[] params){
        if (locale == null)
            locale = LocaleContext.getLocaleContext().getLocale();
        String value = null;
        boolean unfindedkey = false;// 是否在资源文件里面不存在此key
        for (String basename : bundleBaseNames) {
            ResourceBundle res = getResourceBundle(basename, locale);
            try {
                unfindedkey = false;
                value = res.getString(key);
                break;// 如果在资源文件找到此key，退出循环
            } catch (MissingResourceException ex) {
                unfindedkey = true;
                continue;// 如果不含此资源继续找
            }
        }
        if (unfindedkey) {
            logger.debug("the key {} is undefined", key);
            value = StrUtil.null2blank(defaultValue);
        }
        if (params == null || params.length == 0) {
            return value;
        } else {
            MessageFormat format = new MessageFormat(value, locale);
            return format.format(params);
        }
    }

    public static ResourceBundle getResourceBundle(String basename,
            Locale locale){
        return ResourceBundle.getBundle(basename, locale);
    }

    /*
     * 初如化支持的语言
     */
    private static void initSupportLang(){
        InputStream in = I18N.class.getResourceAsStream(SUPPORTLANG);
        if (in == null) {
            return;
        }
        try {
            Properties props = new Properties();
            props.load(in);
            String lang = (String) props.get("lang");
            if ("all".equals(lang)) {
                return;
            }
            supports = new Locale[] {LOCALES[ArrYn.find(LANGUAGE_CODES, lang)]};
        } catch (IOException e) {
            ExceptionHandler.handleException(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private static void initResourceBundleNames(){
        try {
            Set<URL> foundResources = ClassPathSearcher
                    .findResources(I18N_BUNDLE_PATTERN);
            if (foundResources == null || foundResources.size() == 0) {
                throw new RuntimeException("没找到任何i18n-*-bundle.properties文件！");
            }
            for (URL res : foundResources) {
                logger.info("找到i18n boundle配置文件 :" + res.toString());
                Properties props = new Properties();
                InputStream in = res.openStream();
                try {
                    props.load(in);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                String basename = props.getProperty("bundle");
                bundleBaseNames.add(basename);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (bundleBaseNames.size() == 0) {
            throw new RuntimeException("没找到任何i18n-*-bundle.properties文件！");
        }
    }

    /**
     * 根据传递的"语言"判断是否支持,如果不支持返回"defaultLocale"
     * 
     * @param locale
     * @param defaultLocale
     * @return
     */
    public static Locale getSupportLocale(Locale locale, Locale defaultLocale){
        if (locale == null) {
            return defaultLocale;
        }
        for (Locale support : supports) {
            if (support.equals(locale)) return support;
        }
        return defaultLocale;
    }

    /**
     * 是否支持语言切换功能 界面可根据此判断是否显示语言切换菜单
     * 
     * @return
     */
    public static boolean supportSwitchLocale(){
        return supports.length > 1;
    }

    /**
     * 返回系统已加载的所有ResourceBundle。
     * 
     * @param locale
     * @return
     */
    public static ResourceBundle[] getResourceBundles(Locale locale){
        if (bundleBaseNames == null || bundleBaseNames.size() == 0) {
            return null;
        }
        ResourceBundle[] bundles = new ResourceBundle[bundleBaseNames.size()];
        int i = 0;
        for (String res : bundleBaseNames) {
            bundles[i] = getResourceBundle(res, locale);
            i++;
        }
        return bundles;
    }

}
