package org.codeyn.util.i18n;

import java.io.Serializable;
import java.util.Locale;

/**
 * 线程独立的Locale信息。可以在线程各处使用。
 * <p>
 * LocaleContext应当在线程创建后调用其setLocaleContext方法为当前线程缓存相关的locale信息：
 * <ul>
 * <li>(1)对于action或servlet，通过RequestLocaleFilter在每次请求时调用。</li>
 * <li>(2)对于程序内部自已创建的线程池，应该在创建时调用setLocaleContext方法。</li>
 * </ul>
 * <p>
 * 线程内获取LocaleContext的方法：
 * 
 * <pre>
 * LocaleContext ctx = LocaleContext.getLocaleContext();
 * Locale locale = ctx.getLocale();
 * System.out.println(locale);
 * </pre>
 *
 * @author chxb
 */
public class LocaleContext implements Serializable{

    private static final long serialVersionUID = 7817352765639990968L;

    // 此变量所有子线程共享
    private static InheritableThreadLocal localeContext = new InheritableThreadLocal();

    private Locale locale;

    public Locale getLocale(){
        if (locale == null) {
            return I18N.getDefaultLocale();// 返回环境默认值
        } else {
            return locale;
        }
    }

    public void setLocale(Locale locale){
        this.locale = locale;
        setLocaleContext(this);
    }

    /**
     * 取得当前线程环境下的LocaleContext对象。
     * 
     * @return
     */
    public static LocaleContext getLocaleContext(){
        LocaleContext s = (LocaleContext) localeContext.get();
        if (s == null) s = new LocaleContext();
        return s;
    }

    /**
     * 设置当前线程的LocaleContext对象。应该在每个线程内部调用。
     * 
     * @param ctx
     */
    public static void setLocaleContext(LocaleContext ctx){
        localeContext.set(ctx);
    }

}
