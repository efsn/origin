package org.codeyn.util.exception;

import java.util.concurrent.TimeoutException;

import org.codeyn.util.i18n.I18N;

/**
 * 集群里面用到了此类型异常，增加国际化支持
 *
 */
public class TimeoutException4I18N extends TimeoutException{

    private static final long serialVersionUID = 6020596120335612371L;

    private String messageKey;
    private String defaultValue;
    private Object[] params;

    public TimeoutException4I18N(String messageKey, String defaultValue,
            Object[] params){
        this.messageKey = messageKey;
        this.defaultValue = defaultValue;
        this.params = params;
    }

    public TimeoutException4I18N(String messageKey, String defaultValue){
        this(messageKey, defaultValue, (Object[]) null);
    }

    @Override
    public String getLocalizedMessage(){
        return I18N.getString(messageKey, defaultValue, params);
    }

    @Override
    public String getMessage(){
        return I18N.getString(messageKey, defaultValue,
                I18N.getDefaultLocale(), params);
    }

}
