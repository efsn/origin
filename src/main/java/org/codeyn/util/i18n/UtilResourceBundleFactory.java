package org.codeyn.util.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public class UtilResourceBundleFactory implements ResourceBundleFactory {

	public Object createObject() {
		return createResourceBundle();
	}

	public Object createObject(Object createParams) {
		return createResourceBundle((Locale)createParams);
	}

	public void setParam(String name, Object value) {

	}

	public ResourceBundle createResourceBundle() {
		return createResourceBundle(LocaleContext.getLocaleContext().getLocale());
	}

	public ResourceBundle createResourceBundle(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle("com.esen.util.i18n.res.resource", locale);
		return res;
	}

}
