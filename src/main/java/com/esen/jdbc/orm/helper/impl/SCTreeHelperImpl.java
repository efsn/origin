package com.esen.jdbc.orm.helper.impl;

import java.sql.Timestamp;
import java.util.List;

import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.Query;
import com.esen.jdbc.orm.QueryResult;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.helper.HelperFactory;
import com.esen.jdbc.orm.helper.SCEntityInfo;
import com.esen.jdbc.orm.helper.SCTreeEntityInfo;
import com.esen.jdbc.orm.helper.SCTreeHelper;
import com.esen.jdbc.orm.helper.TreeEntityInfo;
import com.esen.jdbc.orm.helper.TreeHelper;
import com.esen.util.StrFunc;
import com.esen.util.exp.Expression;
import com.esen.util.reflect.DynaBean;

public class SCTreeHelperImpl<T> extends SCHelperImpl implements SCTreeHelper<T> {

	/**
	 * 缓慢变化树实体信息
	 */
	private SCTreeEntityInfo scTreeEntityInfo;
	
	/**
	 * sql 语句中用到的判断时间有效性的子句
	 */
	private String validDateClause;

	/**
	 * 构造方法
	 * 
	 * @param session
	 * @param scTreeEntityInfo
	 */
//	public SCTreeHelperImpl(Session session, SCTreeEntityInfo scTreeEntityInfo) {
//		if (scTreeEntityInfo == null) {
//			throw new ORMException("参数 scEntityInfo 不能为空！");
//		}
//
//		if (session == null) {
//			throw new ORMException("参数 session 不能为空！");
//		}
//
//		this.session = session;
//		this.scTreeEntityInfo = scTreeEntityInfo;
//	}

	public SCTreeHelperImpl(Session session, SCEntityInfo scEntityInfo) {
		super(session, scEntityInfo);
		this.scTreeEntityInfo = (SCTreeEntityInfo)scEntityInfo;
		this.validDateClause = scTreeEntityInfo.getFromDatePropertyName() + " <= ? and "
				+ scTreeEntityInfo.getToDatePropertyName() + " >= ? ";
	}

	/**
	 * {@inheritDoc}   
	 */
	public QueryResult<T> listChildren(Object idValue, boolean recursive, long date) {
		return listChildren(idValue, recursive, date, null);
	}
	
	/**
	 * {@inheritDoc}   
	 */
	public QueryResult<T> listChildren(Object idValue, boolean recursive, long date, String orderByProperties) {
		ringCheck();
		Query queryExecuter = session.createQuery(scTreeEntityInfo.getClass(), scTreeEntityInfo.getEntityName());
		
		Timestamp validDate = new Timestamp(date);
		
		if (!recursive) {
			return queryExecuter.query(new Expression(scTreeEntityInfo.getUpIdPropertyName()
					+ " = ? and " + validDateClause), orderByProperties, null, idValue, validDate, validDate);
		}
		
		//如果递归，idValue 存在于父节点链中。
		//idValue不是 rootUpid 的情况
		if (!idValue.equals(scTreeEntityInfo.getRootUpid())) {		
			StringBuilder sql = new StringBuilder();
			int i = 0;
			String[] upids = StrFunc.analyzeStr(this.scTreeEntityInfo.getUpidsPropertyName());
			Object[] params = new Object[upids.length + 2];
			sql.append("(");
			for (String str : upids) {
				if (i != 0) {
					sql.append(" OR ");
				}
				sql.append(str + " = ? ");
				params[i] = idValue;
				i++;
			}		
			sql.append(") and ");
			sql.append(validDateClause);
			params[i] = validDate;
			params[i + 1] = validDate;
	
			return queryExecuter.query(new Expression(sql.toString()), orderByProperties, null, params);
		}
		
		//idValue 是 rootUpid 的情况
		StringBuilder sql = new StringBuilder();
		String[] upids = StrFunc.analyzeStr(this.scTreeEntityInfo.getUpidsPropertyName());
		Object[] params = new Object[2];
		params[0] = validDate;
		params[1] = validDate;
		
		sql.append(this.scTreeEntityInfo.getUpIdPropertyName() 
				+ " = '" + scTreeEntityInfo.getRootUpid() + "' and " + validDateClause); 
		QueryResult rs = queryExecuter.query(new Expression(sql.toString()), null, params);
		if (rs == null) {
			return null;
		}
		
		int rows = rs.calcTotalCount();
		if (upids == null || upids.length <= 0 || rows <= 0) {
			return rs;
		}
		
		List list = rs.list(-1, -1);
		params = new Object[rows + 2];
		
		sql.setLength(0);
		sql.append(validDateClause + " and ((" + this.scTreeEntityInfo.getUpIdPropertyName() 
				+ " = '" + scTreeEntityInfo.getRootUpid() + "') "); 
		params[0] = validDate;
		params[1] = validDate;
		try {
			for (int i = 0; i < rows; i++) {
				Object object = list.get(i);
				DynaBean<?> bean = DynaBean.getDynaBean(object);
				Object idValueTmp = bean.getValue(this.scTreeEntityInfo.getIdPropertyName());
				sql.append(" OR (" + upids[0] + "= ? ) ");
				params[i + 2] = idValueTmp;
			}
		} catch (Exception e) {
			throw new ORMException("com.esen.jdbc.orm.helper.impl.sctreehelperimpl.1","获取 idValue 失败", e);
		}
		sql.append(")");

		return queryExecuter.query(new Expression(sql.toString()), orderByProperties, null, params);	
	}


	/**
	 * 是否形成环
	 * 暂时未实现，由调用者自行确定不会存在环
	 */
	protected void ringCheck() {

	}

	/**
	 * {@inheritDoc}   
	 */
	public QueryResult<T> getRoots(long date) {
		Query queryExecuter = session.createQuery(scTreeEntityInfo.getClass(), scTreeEntityInfo.getEntityName());
		
		Timestamp validDate = new Timestamp(date);
		
		//upid 为 null 的情况
		String rootUpid = scTreeEntityInfo.getRootUpid();
		if (StrFunc.isNull(rootUpid)) {
			return queryExecuter.query(new Expression(scTreeEntityInfo.getUpIdPropertyName()
					+ " is null and " + validDateClause), null, validDate, validDate);
		}
		
		//upid 不为 null 的情况
		return queryExecuter.query(new Expression(scTreeEntityInfo.getUpIdPropertyName()
				+ " = ? and " + validDateClause), null, rootUpid, validDate, validDate);
	}

	/**
	 * {@inheritDoc}   
	 */
	public void update(Object idValue, Object object) {
		throw new ORMException("com.esen.jdbc.orm.helper.impl.sctreehelperimpl.2","缓慢变化树结构不支持修改 id");
	}
		
	/**
	 * {@inheritDoc}
	 */
	public void delete(Object idValue) {
		delete(idValue, false);
	}
		
	/**
	 * {@inheritDoc}
	 */
	public void delete(Object idValue, boolean recursive) {
		TreeHelper treeHelper = HelperFactory.getTreeHelper(session, (TreeEntityInfo)scTreeEntityInfo);
		if (treeHelper == null) {
			throw new ORMException("com.esen.jdbc.orm.helper.impl.sctreehelperimpl.3","删除结点出现异常");
		}
		treeHelper.delete(idValue, recursive);
	}

//	/**
//	 * {@inheritDoc}   
//	 */
//	public void update(Object idValue, T object, long date) {
//		Date validDate = new Date(date);
//		super.setDate(object);
//		
//		Class<T> clazz = scEntityInfo.getBean();
//		DynaBean<T> bean = DynaBeanMapCache.getInstance().getDynaBean(clazz);
//
//		try {
//			Expression exp = new Expression(scTreeEntityInfo.getIdPropertyName() + " = ? and " + validDateClause);
//			
//			//更新原数据，将 ToDate 修改为指定时间
//			Update update = this.session.createUpdate(this.scTreeEntityInfo.getBean(), this.scTreeEntityInfo.getEntityName());
//			update.setCondition(exp, idValue, validDate, validDate);
//			update.setPropertyValue(scEntityInfo.getToDatePropertyName(),
//					bean.getBeanProperty(object, scEntityInfo.getFromDatePropertyName()));
//			
//			update.executeUpdate();
//			
//			//插入新数据
//			this.session.add(scEntityInfo.getEntityName(), object);
//			
//			updateUpids(idValue, bean.getBeanProperty(object, scEntityInfo.getIdPropertyName()), date);
//			this.ringCheck();
//		} catch (Exception e) {
//			throw new ORMException(e);
//		}	
//	}
//	
	/**
	 * 修改其子节点的上级结点
	 * 目前暂时不支持修改上级节点（递归修改各级节点时，可能影响性能）
	 * 
	 * @param idValue
	 * @param object
	 * @param date
	 */
	private void updateUpids(Object oldIdValue, Object newIdValue, long date) {	
// 		String[] upids = StrFunc.analyzeStr(scTreeEntityInfo.getUpidsPropertyName());
//		if (upids == null || upids.length <= 0) {
//			return;
//		}
//		
//		try {
//			Date validDate = new Date(date);
//			DynaBean<T> bean = DynaBeanMapCache.getInstance().getDynaBean(scEntityInfo.getBean());
//			for (int i = 0; i < upids.length; i++) {
//				//查找需要修改的数据
//				Expression exp = new Expression(upids[i] + " = ? and " + validDateClause);
//				
//				QueryExecuter<T> queryExe = new QueryExecuter<T>(session, scTreeEntityInfo);
//				QueryResult res = queryExe.query(exp, null, oldIdValue, validDate, validDate);
//				if (res == null) { //没有满足条件的数据节点。
//					return;
//				}
//				
//				List oldObjList = res.list(-1, -1);
//				if (oldObjList == null || oldObjList.size() <= 0) {
//					//当前级别已经没有满足条件的数据节点，再下一级不可能还有结点，直接返回。
//					return;
//				}
//				
//				//更新旧数据，将 ToDate 修改为指定时间
//				Update exec = this.session.createUpdate(scTreeEntityInfo.getBean(), scTreeEntityInfo.getEntityName());
//				exec = exec.setCondition(new Expression(upids[i] + "= ?"), oldIdValue);
//				exec = exec.setCondition(new Expression(this.validDateClause), validDate, validDate);
//				exec = exec.setPropertyValue(upids[i], newIdValue);
//				exec.executeUpdate();
//					
//				//依次插入新数据
//				//新数据是原旧数据基础上修改上级结点，修改FromDate
//				int size = oldObjList.size();
//				for (int j = 0; j < size; j++) {
//					T oldObj = (T) oldObjList.get(j);
//					bean.setBeanProperty(oldObj, upids[i], newIdValue);
//					bean.setBeanProperty(oldObj, scTreeEntityInfo.getFromDatePropertyName(), validDate);
//					this.session.add(scEntityInfo.getEntityName(), oldObj);
//				}
//			}
//		} catch (Exception e) {
//			throw new ORMException(e);
//		}
	}

}
