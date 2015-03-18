package com.esen.jdbc.orm.helper.impl;

import java.util.List;

import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.Property;
import com.esen.jdbc.orm.Query;
import com.esen.jdbc.orm.QueryResult;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.Update;
import com.esen.jdbc.orm.helper.TreeEntityInfo;
import com.esen.jdbc.orm.helper.TreeHelper;
import com.esen.util.StrFunc;
import com.esen.util.exp.Expression;
import com.esen.util.reflect.DynaBean;

/**
 * 树形操作控件
 * @param <T>
 *
 * @author wangshzh
 */
public class TreeHelperImpl<T> implements TreeHelper<T> {

	/**
	 * 树形实体
	 */
	private TreeEntityInfo treeEntityInfo;

	/**
	 * 会话
	 */
	protected Session session;

	/**
	 * @param treeEntityInfo 树形实体
	 * @param session 会话
	 */
	public TreeHelperImpl(Session session, TreeEntityInfo treeEntityInfo) {
		if (treeEntityInfo == null) {
			throw new ORMException("com.esen.jdbc.orm.helper.impl.treehelperimpl.1","javabean对应的实体类不能为空！");
		}
		
		if (session == null) {
			throw new ORMException("com.esen.jdbc.orm.helper.impl.treehelperimpl.2","session不能为空！");
		}
		
		this.treeEntityInfo = treeEntityInfo;
		this.session = session;
	}

	/**
	 * {@inheritDoc}   
	 */
	public QueryResult<T> getRoots() {
		Query queryExecuter = session.createQuery(treeEntityInfo.getClass(), treeEntityInfo.getEntityName());
		
		//upid 为 null 的情况
		String rootUpid = treeEntityInfo.getRootUpid();
		if (StrFunc.isNull(rootUpid)) {
			return queryExecuter.query(new Expression(treeEntityInfo.getUpIdPropertyName()
					+ " is null "), null);
		}
		
		//upid 不为 null 的情况
		return queryExecuter.query(new Expression(treeEntityInfo.getUpIdPropertyName()
				+ " = ? "), null, rootUpid);
	}

	/**
	 * {@inheritDoc}   
	 */
	public QueryResult<T> listChildren(Object idValue, boolean recursive) {
		return listChildren(idValue, recursive, null);
	}

	/**
	 * {@inheritDoc}   
	 */
	public QueryResult<T> listChildren(Object idValue, boolean recursive, String orderByProperties) {
		Query queryExecuter = session.createQuery(treeEntityInfo.getClass(), treeEntityInfo.getEntityName());
		
		if (!recursive) {
			return queryExecuter.query(new Expression(treeEntityInfo.getUpIdPropertyName()
					+ " = ? "), orderByProperties, null, idValue);
		}
		
		//如果递归，idValue存在于父节点链中。
		if (!treeEntityInfo.getRootUpid().equals(idValue)) {
			StringBuilder sql = new StringBuilder();
			int i = 0;
			String[] upids = StrFunc.analyzeStr(this.treeEntityInfo.getUpidsPropertyName());
			Object[] params = new Object[upids.length];
			for (String str : upids) {
				if (i != 0) {
					sql.append(" OR ");
				}
				sql.append(str + " = ? ");
				params[i] = idValue;
				i++;
			}
			return queryExecuter.query(new Expression(sql.toString()), orderByProperties, null, params);
		}
		
		//idValue 是 rootUpid 的情况
		StringBuilder sql = new StringBuilder();
		String[] upids = StrFunc.analyzeStr(this.treeEntityInfo.getUpidsPropertyName());
		
		sql.append(this.treeEntityInfo.getUpIdPropertyName() 
				+ " = '" + treeEntityInfo.getRootUpid() + "'"); 
		QueryResult rs = queryExecuter.query(new Expression(sql.toString()), null);

		if (rs == null) {
			return null;
		}
		
		int rows = rs.calcTotalCount();
		if (upids == null || upids.length <= 0 || rows <= 0) {
			return rs;
		}
		
		List list = rs.list(-1, -1);
		Object[] params = new Object[rows];
		
		try {
			for (int i = 0; i < rows; i++) {
				T object = (T) list.get(i);
				DynaBean<T> bean = DynaBean.getDynaBean(object);
				Object idValueTmp = bean.getValue(this.treeEntityInfo.getIdPropertyName());
				sql.append(" OR (" + upids[0] + "=? ) ");
				params[i] = idValueTmp;
			}
		} catch (Exception e) {
			throw new ORMException("com.esen.jdbc.orm.helper.impl.treehelperimpl.3","获取 idValue 失败", e);
		}

		return queryExecuter.query(new Expression(sql.toString()), orderByProperties, null, params);	
	}

	/**
	 * {@inheritDoc}   
	 */
	public void update(Object idValue, T object) {
		// 更新树形节点数据， 允许修改节点ID。
		// 如果节点ID发生变化，除了更新当前节点信息以外，下级节点的upids也将随之做出相应的调整 
		this.session.update(this.treeEntityInfo.getEntityName(), idValue, object);
		updateUpids(idValue, object);
	}

	/**
	 * {@inheritDoc}   
	 */
	public void update(Object idValue, T object, String... properties) {
		/* 
		 * 更新树形节点数据，允许修改节点ID
		 * 如果节点ID发生变化，除了更新当前节点信息以外，下级节点的 upids 也将随之做出相应的调整 
		 */
		this.session.update(this.treeEntityInfo.getEntityName(), idValue, object, properties);
		updateUpids(idValue, object);
	}

	protected void updateUpids(Object idValue, T object) {
		//idValue为Object对象，无法比较是否有改变。故均更新下级upids的值。
		DynaBean<T> dynaBean = DynaBean.getDynaBean(object);
		
		Object newIdValue = null;
		try {
			newIdValue = dynaBean.getValue(this.treeEntityInfo.getIdPropertyName());
		} catch (Exception e) {
			throw new ORMException("com.esen.jdbc.orm.helper.impl.treehelperimpl.3","获取 idValue 失败", e);
		}
		
		//id没有改变时，不需更新下级节点的上级 id
		if (idValue.equals(newIdValue)) {
			return;
		}
		
		String[] upids = StrFunc.analyzeStr(this.treeEntityInfo.getUpIdPropertyName() + "," + this.treeEntityInfo.getUpidsPropertyName());
		for (String str : upids) {
			Update update = session.createUpdate(treeEntityInfo.getClass(), treeEntityInfo.getEntityName());
			update.setCondition(new Expression(str + " = ? "), idValue);
			update.setPropertyValue(str, newIdValue);
			update.executeUpdate();
		}
		
		ringCheck();
	}

	/**
	 * {@inheritDoc}   
	 */
	public void delete(Object idValue) {
		if (idValue == null) {
			return;
		}

		Query query = this.session.createQuery(this.treeEntityInfo.getBean(), this.treeEntityInfo.getEntityName());
		Object object = query.get(idValue, treeEntityInfo.getUpIdPropertyName(), treeEntityInfo.getBtypePropertyName());
		if (object == null) { //该节点不存在
			return;
		}

		DynaBean<?> dynaBean = DynaBean.getDynaBean(object);
		Object btypeValue = dynaBean.getValue(treeEntityInfo.getBtypePropertyName());
		Object upid = dynaBean.getValue(treeEntityInfo.getUpIdPropertyName());
		if (!Boolean.parseBoolean(btypeValue.toString())) { //非叶子节点
			throw new ORMException("com.esen.jdbc.orm.helper.impl.treehelperimpl.5","不能删除非叶子节点");
		}
		//删除叶子结点
		this.session.delete(treeEntityInfo.getEntityName(), new Expression(getPrimaryKey().getName() + " = ? "), idValue);

		//更新其上级结点的 btype 信息
		updateBtype(dynaBean, upid);
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete(Object idValue, boolean recursive) {
		if (idValue == null) {
			return;
		}
		
		if (!recursive) {
			delete(idValue);
			return;
		}
		
		//获取需要删除的结点的 upid
		Query query = this.session.createQuery(this.treeEntityInfo.getBean(), this.treeEntityInfo.getEntityName());
		Object object = query.get(idValue, treeEntityInfo.getUpIdPropertyName(), treeEntityInfo.getBtypePropertyName());
		if (object == null) { //该节点不存在
			return;
		}
		
		DynaBean<T> dynaBean = (DynaBean<T>) DynaBean.getDynaBean(object);
		Object upid = dynaBean.getValue(treeEntityInfo.getUpIdPropertyName());
			
		//先删除节点本身，再删除所有子节点。		
		this.session.delete(this.treeEntityInfo.getEntityName(), new Expression(getPrimaryKey().getName() + " = ? "), idValue);
		String[] upids = StrFunc.analyzeStr(this.treeEntityInfo.getUpidsPropertyName());
		this.session.delete(this.treeEntityInfo.getEntityName(), new Expression(
				this.treeEntityInfo.getUpIdPropertyName() + " = ? "), idValue);		
		for (String str : upids) {
			this.session.delete(this.treeEntityInfo.getEntityName(), new Expression(
					str + " = ? "), idValue);
		}
		
		//更新上级节点的 btype
		updateBtype(dynaBean, upid);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void add(T object) {
		if (object == null) {
			return;
		}
		DynaBean<T> dynaBean = DynaBean.getDynaBean(object);
		Object upid;
		Object btype;
		try {
			upid = dynaBean.getValue(treeEntityInfo.getUpIdPropertyName());
			btype = dynaBean.getValue(treeEntityInfo.getBtypePropertyName());
			if (btype == null) {
				dynaBean.setValue(treeEntityInfo.getBtypePropertyName() , Boolean.TRUE);
			}
		} catch (Exception e1) {
			throw new ORMException(e1);
		}
		
		//增加该节点
		this.session.add(treeEntityInfo.getEntityName(), object);

		//检查其上级节点的 btype 是否需要修改
		//没有实际存在的上级节点
		if (upid == null || upid.toString().equalsIgnoreCase(treeEntityInfo.getRootUpid())) {
			return;
		}
		
		//如果存在，修改上级节点的 btype
		updateBtype(dynaBean, upid);	
	}

	protected Property getPrimaryKey() {
		Property primaryKey = this.treeEntityInfo.getPrimaryKey();
		if (primaryKey == null) {
			throw new ORMException("com.esen.jdbc.orm.helper.impl.treehelperimpl.6","没有找到主键属性！");
		}
		return primaryKey;
	}

	/**
	 * 更新指定 id 的节点的  btype 信息
	 * 	如果该节点有子节点，则  btype 设置为 false
	 *  如果该节点为叶子节点，则  btype 设置为 true
	 * 
	 * @param idValue
	 */
	private void updateBtype(DynaBean<?> dynaBean,Object idValue) {
		boolean btype = true;
		//查询是否存在该节点的子节点
		String exp = treeEntityInfo.getUpIdPropertyName() + "= ? ";
		Query query = this.session.createQuery(treeEntityInfo.getBean(), this.treeEntityInfo.getEntityName());
		QueryResult res = query.query(new Expression(exp), null, idValue);
		if (res.calcTotalCount() > 0) { //存在子节点
			btype = false;
		}
		
		//修改该节点信息
		//DynaBean<T> dynaBean = DynaBean.getDynaBean(treeEntityInfo.getBean());
		try {
			dynaBean.setValue(treeEntityInfo.getBtypePropertyName(), btype);
		} catch (Exception e) {
			throw new ORMException(e);
		}
		this.session.update(treeEntityInfo.getEntityName(), idValue, dynaBean.getBean(), treeEntityInfo.getBtypePropertyName());
	}

	/**
	 * 是否形成环
	 * 暂时未实现，由调用者自行确定不会存在环
	 */
	protected void ringCheck() {

	}
}
