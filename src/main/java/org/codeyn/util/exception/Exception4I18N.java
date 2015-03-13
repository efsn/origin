package org.codeyn.util.exception;

import org.codeyn.util.yn.StrUtil;
import org.codeyn.util.i18n.I18N;

/**
 * 既需要前台显示给用户，又需要记录到日志的异常使用此异常类
 * @author wakeup
 *
 */
public class Exception4I18N extends Exception{
	
	protected String messageKey;
	
	protected String defaultValue;
	
	protected Object[] params ;
	
	protected Exception4I18N(Throwable cause){
		super(cause);
	}
	
	public Exception4I18N(String messageKey,String defaultValue, Object[] params){
		this.messageKey = messageKey;
		this.defaultValue = defaultValue;
		this.params = params;
	}
	
	public Exception4I18N(String messageKey,String defaultValue, Object[] params,Throwable cause){
		super(cause);
		this.messageKey = messageKey;
		this.defaultValue = defaultValue;
		this.params = params;
	}
	
	public Exception4I18N(String messageKey,String defaultValue){
		this(messageKey,defaultValue,(Object[])null);
	}
	
	public Exception4I18N(String messageKey,String defaultValue,Throwable cause){
		this(messageKey,defaultValue,null,cause);
	}

	@Override
	public String getLocalizedMessage() {
		if(StrUtil.isNull(messageKey)){
			return super.getLocalizedMessage();
		}
		return I18N.getString(messageKey, defaultValue, params);
	}

	@Override
	public String getMessage() {
		if(StrUtil.isNull(messageKey)){
			return super.getMessage();
		}
		return I18N.getString(messageKey, defaultValue, I18N.getDefaultLocale(), params);
	}

	public String getMessageKey() {
		return messageKey;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public Object[] getParams() {
		return params;
	}
}
