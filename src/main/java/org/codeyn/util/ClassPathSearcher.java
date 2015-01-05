package org.codeyn.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ClassPathSearcher {

	/**
	 * 在classpath中查找指定的资源。
	 * @param filter 过滤器。*或?通配符格式。
	 */
	public static Set<URL> findResources(String filter) throws IOException {
		PathMatchingResourcePatternResolver pathMathing = new PathMatchingResourcePatternResolver();

		Resource[] resources = pathMathing.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + filter);
		Set<URL> collectedURLs = new HashSet<URL>();
		for( Resource res : resources ){
			collectedURLs.add(res.getURL());
		}
		return collectedURLs;
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		Set<URL> foundUrls = ClassPathSearcher.findResources("/com/esen/**/i18n-*-bundle.properties");
		Iterator<URL> it = foundUrls.iterator();
		while (it.hasNext()) {
			System.out.println(it.next().getFile());
		}
	}

}

