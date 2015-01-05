package org.codeyn.util.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

import org.codeyn.util.oservice.ObjectFactory;

/**
 * 创建ResourceBundle的工厂类。
 * 每个项目工程模块都必须实现一个ResourceBundleFactory工厂类。否则国际化无效。
 *
 */
public interface ResourceBundleFactory extends ObjectFactory {

	public ResourceBundle createResourceBundle();
	public ResourceBundle createResourceBundle(Locale locale);

}
