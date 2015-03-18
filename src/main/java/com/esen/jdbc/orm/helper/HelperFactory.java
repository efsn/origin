package com.esen.jdbc.orm.helper;

import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.helper.impl.SCHelperImpl;
import com.esen.jdbc.orm.helper.impl.SCTreeHelperImpl;
import com.esen.jdbc.orm.helper.impl.TreeHelperImpl;

/**
 * TreeHelper SCHelper的工厂类。
 * 
 * @author wangshzh
 */
public class HelperFactory {

	/**
	 * 获取 TreeHelper 对象
	 * @param session
	 * @param entityInfo
	 * @return
	 */
	public static <T> TreeHelper<T> getTreeHelper(Session session, TreeEntityInfo entityInfo) {
		return new TreeHelperImpl(session, entityInfo);
	}

	/**
	 * 获取 SCHelper 对象
	 * @param session
	 * @param scEntityInfo
	 * @return
	 */
	public static <T> SCHelper<T> getSCHelper(Session session, SCEntityInfo scEntityInfo) {
		return new SCHelperImpl(session, scEntityInfo);
	}
	
	/**
	 * 获取 SCTreeHelper 对象
	 * @param session
	 * @param scEntityInfo
	 * @return
	 */
	public static <T> SCTreeHelper<T> getSCTreeHelper(Session session, SCTreeEntityInfo scEntityInfo) {
		return new SCTreeHelperImpl(session, scEntityInfo);
	}	
}
