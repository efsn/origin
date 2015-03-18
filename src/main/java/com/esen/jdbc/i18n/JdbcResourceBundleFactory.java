package com.esen.jdbc.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

import com.esen.util.i18n.LocaleContext;
import com.esen.util.i18n.ResourceBundleFactory;

public class JdbcResourceBundleFactory implements ResourceBundleFactory {

	public Object createObject() {
		return createResourceBundle();
	}

	public Object createObject(Object createParams) {
		return createResourceBundle((Locale)createParams);
	}

	public void setParam(String name, Object value) {
		// TODO Auto-generated method stub

	}

	public ResourceBundle createResourceBundle() {
		return createResourceBundle(LocaleContext.getLocaleContext().getLocale());
	}

	public ResourceBundle createResourceBundle(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle("com.esen.jdbc.i18n.res.resource", locale);
		return res;
	}

}
