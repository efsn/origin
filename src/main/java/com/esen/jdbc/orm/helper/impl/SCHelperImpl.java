package com.esen.jdbc.orm.helper.impl;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.Property;
import com.esen.jdbc.orm.Query;
import com.esen.jdbc.orm.QueryResult;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.Update;
import com.esen.jdbc.orm.helper.SCEntityInfo;
import com.esen.jdbc.orm.helper.SCHelper;
import com.esen.jdbc.orm.impl.ORMUtil;
import com.esen.util.StrFunc;
import com.esen.util.exp.Expression;
import com.esen.util.reflect.DynaBean;

/**
 * 缓慢变化对象操作类,所有的事务交给调用者处理，本类中的方法不commit也不close
 * @param <T>
 *
 * @author wangshzh
 */
public class SCHelperImpl<T> implements SCHelper<T> {

	/**
	 * 无穷大时间的表示
	 */
	public static long MAX_DATE = 0;

	/**
	 * 会话
	 */
	protected Session session;

	/**
	 * 实体对象
	 */
	protected SCEntityInfo scEntityInfo;

	/**
	 * @param session 会话
	 * @param scEntityInfo 实体对象
	 */
	public SCHelperImpl(Session session, SCEntityInfo scEntityInfo) {
		if (scEntityInfo == null) {
			throw new ORMException("com.esen.jdbc.orm.helper.impl.schelperimpl.1","参数 scEntityInfo 不能为空！");
		}

		if (session == null) {
			throw new ORMException("com.esen.jdbc.orm.helper.impl.schelperimpl.2","参数 session 不能为空！");
		}

		this.session = session;
		this.scEntityInfo = scEntityInfo;
	}

	/**
	 * {@inheritDoc}   
	 */
	public T get(Object idValue, long date) {
		checkIdProperty();

		Timestamp dat = new Timestamp(date);
		Query queryExecuter = session.createQuery(scEntityInfo.getClass(), scEntityInfo.getEntityName());
		QueryResult<T> queryResult = queryExecuter.query(
				new Expression(scEntityInfo.getIdPropertyName() + " = ? and "
						+ scEntityInfo.getFromDatePropertyName() + " <= ? and "
						+ scEntityInfo.getToDatePropertyName() + " >= ? "), null, idValue, dat, dat);
		List<T> list = queryResult.list(-1, 1);
		if (list == null || list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	/**
	 * {@inheritDoc}   
	 */
	public QueryResult<T> getAll(long date) {
		Timestamp dat = new Timestamp(date);
		
		Query queryExecuter = session.createQuery(scEntityInfo.getClass(), scEntityInfo.getEntityName());
		QueryResult<T> queryResult = queryExecuter.query(
				new Expression(scEntityInfo.getFromDatePropertyName() + " <= ? and "
						+ scEntityInfo.getToDatePropertyName() + " >= ? "), null, dat, dat);
		return queryResult;
	}

	/**
	 * {@inheritDoc}   
	 */
	public void add(Object obj) {
		if (obj == null) {
			return;
		}
		DynaBean<?> bean = DynaBean.getDynaBean(obj);
		if (!StrFunc.isNull(scEntityInfo.getIdPropertyName())) {
			if (isDataExist(bean)) {
				throw new ORMException("com.esen.jdbc.orm.helper.impl.schelperimpl.3","该 id 的数据已存在，不能重复添加");
			}
		}

		//添加新记录
		setDate(bean);
		this.session.add(scEntityInfo.getEntityName(), obj);
	}

	/**
	 * {@inheritDoc}   
	 */
	public void update(Object object) {
		checkIdProperty();
		DynaBean<?> dynaBeanNew = DynaBean.getDynaBean(object);
		setDate(dynaBeanNew);
		Object idValue = dynaBeanNew.getValue(scEntityInfo.getIdPropertyName());
		Object fromDate = dynaBeanNew.getValue(scEntityInfo.getFromDatePropertyName());
		Object toDate = dynaBeanNew.getValue(scEntityInfo.getToDatePropertyName());

		//查询该 Id 的所有截止时间大于新纪录的开始时间的数据，根据开始时间升序排列
		Query queryExecuter = session.createQuery(scEntityInfo.getClass(), scEntityInfo.getEntityName());
		QueryResult<T> queryResult = queryExecuter.query(new Expression(scEntityInfo.getIdPropertyName() + " = ? and "
				+ scEntityInfo.getToDatePropertyName() + " >= ? and " + scEntityInfo.getFromDatePropertyName() + " <= ?"),
				scEntityInfo.getFromDatePropertyName() + "= true", null, idValue, fromDate, toDate);
		List resultList = queryResult.list(-1, -1);

		String entityName = scEntityInfo.getEntityName();
		if (resultList == null || resultList.isEmpty()) {
			//没有满足条件的数据，直接添加新数据
			this.session.add(scEntityInfo.getEntityName(), object);
			return;
		}

		int size = resultList.size();
		try {
			Object result;
			for (int i = 0; i < size; i++) {
				result = resultList.get(i);
				DynaBean<?> dynaBeanOld = DynaBean.getDynaBean(result);
				Object fromDateTmp = dynaBeanOld.getValue(scEntityInfo.getFromDatePropertyName());
				Object toDateTmp = dynaBeanOld.getValue(scEntityInfo.getToDatePropertyName());

				if (i == 0 && !fromDate.equals(fromDateTmp)) {
					dynaBeanOld.setValue(scEntityInfo.getToDatePropertyName(), addSecond(fromDate, -1)); // -1 秒
					this.session.add(entityName, result);
				}

				this.session.delete(entityName,
						new Expression(scEntityInfo.getIdPropertyName() + " = ? and " + scEntityInfo.getFromDatePropertyName()
								+ " = ? and " + scEntityInfo.getToDatePropertyName() + " = ?"), idValue, fromDateTmp, toDateTmp);

				if (i == size - 1 && !toDate.equals(toDateTmp)) {
					dynaBeanOld.setValue(scEntityInfo.getFromDatePropertyName(), addSecond(toDate, 1)); // +1 秒
					dynaBeanOld.setValue(scEntityInfo.getToDatePropertyName(), toDateTmp);
					this.session.add(entityName, result);
				}
			}

			//对于不同的时间，存在数据库中的 fromDate 和  toDate 一致的情况。
			//比如，带毫秒的时间类型只是毫秒不一样的情况，在数据库中如果没有毫秒，数据就一致了。
			this.session.delete(entityName,
					new Expression(scEntityInfo.getIdPropertyName() + " = ? and " + scEntityInfo.getFromDatePropertyName()
							+ " >= " + scEntityInfo.getToDatePropertyName()), idValue);

			this.session.add(entityName, object);
		} catch (Exception e1) {
			throw new ORMException(e1);
		}
	}

	/**
	 * {@inheritDoc}   
	 */
	public void update(Object oldIdValue, Object obj) {
		checkIdProperty();

		Update update = session.createUpdate(scEntityInfo.getClass(), scEntityInfo.getEntityName());
		DynaBean<?> dynaBean = DynaBean.getDynaBean(obj);
		setDate(dynaBean);
		Object newIdValue = dynaBean.getValue(scEntityInfo.getIdPropertyName());
		dynaBean.setValue(scEntityInfo.getIdPropertyName(), oldIdValue);
		update(obj);

		//将该数据的 id 修改为新的 id
		update.setCondition(
				new Expression(scEntityInfo.getIdPropertyName() + " = ? and " + scEntityInfo.getFromDatePropertyName()
						+ " = ? and " + scEntityInfo.getToDatePropertyName() + " = ?"), oldIdValue,
				dynaBean.getValue(scEntityInfo.getFromDatePropertyName()),
				dynaBean.getValue(scEntityInfo.getToDatePropertyName()));
		update.setPropertyValue(scEntityInfo.getIdPropertyName(), newIdValue);

		update.executeUpdate();
	}

	/**
	 * {@inheritDoc}   
	 */
	public void delete(Object idValue, boolean stretchUp) {
		checkIdProperty();

//		//把最新的一条数据的toDate设为当前时间，让有效期结束
//		UpdateImpl<T> update = new UpdateImpl(session, scEntityInfo);
//
//		update.setCondition(new Expression(scEntityInfo.getIdPropertyName() + " = ? and "
//				+ scEntityInfo.getToDatePropertyName() + " > now()"), idValue);
//		update.setPropertyValue(scEntityInfo.getToDatePropertyName(), new Date());
//		update.executeUpdate();
		this.session.delete(scEntityInfo.getEntityName(), idValue);
	}

	/**
	 * 根据对象，删除一条记录
	 * 
	 * @param obj
	 */
	public void delete(Object idValue) {
		delete(idValue, false);
	}

	protected Property getProperty(String propertyName) {
		Property property = scEntityInfo.getProperty(propertyName);
		if (property == null) {
			throw new ORMException("com.esen.jdbc.orm.helper.impl.schelperimpl.4","没有找到{0}属性！",new Object[]{propertyName});
		}
		return property;
	}
	
	/**
	 * 起止时间缺失时设置起止时间
	 * 
	 * @param instance
	 */
	protected void setDate(DynaBean<?> bean) {
		if (MAX_DATE == 0) {
			Calendar cal = Calendar.getInstance();
			cal.set(2099, 11, 31, 23, 59, 59);
			MAX_DATE = cal.getTimeInMillis();
		}
		
		try {
			String fromDate = scEntityInfo.getFromDatePropertyName();
			Object fromDateValue = null;
			if (!StrFunc.isNull(fromDate)) {
				fromDateValue = bean.getValue(fromDate);
				if (fromDateValue == null) {
					fromDateValue = new Timestamp(Calendar.getInstance().getTimeInMillis());
					bean.setValue(fromDate, fromDateValue);
				}
			}
			
			String toDate = scEntityInfo.getToDatePropertyName();
			Object toDateValue = null;
			if (!StrFunc.isNull(toDate)) {
				toDateValue = bean.getValue( toDate);
				if (toDateValue == null) {
					toDateValue = new Timestamp(MAX_DATE);
					bean.setValue(toDate, toDateValue);
				}
			}	
			
			//判断开始时间是否小于截止时间
			long t1 = 0; 
			long t2 = 0;
			if (fromDateValue instanceof Calendar) {
				t1 = ((Calendar) fromDateValue).getTimeInMillis();
			} else {
				t1 = ORMUtil.date2Calendar(fromDateValue).getTimeInMillis();
			}
			if (toDateValue instanceof Calendar) {
				t2 = ((Calendar) toDateValue).getTimeInMillis();
			} else {
				t2 = ORMUtil.date2Calendar(toDateValue).getTimeInMillis();
			}
			if (t1 >= t2) {
				throw new ORMException("com.esen.jdbc.orm.helper.impl.schelperimpl.5","开始时间大于截止时间");
			}
		} catch (Exception e) {
			throw new ORMException(e);
		}
	}
	
	/**
	 * 检查是否设置了主键
	 */
	private void checkIdProperty() {
		if (StrFunc.isNull(scEntityInfo.getIdPropertyName())) {
			throw new ORMException("com.esen.jdbc.orm.helper.impl.schelperimpl.6","实体对象({0})主键没有设置",new Object[]{scEntityInfo.getEntityName()});
		}
	}
	
	/**
	 * 检查该对象是否已存在
	 * 
	 * @param obj
	 * @return
	 */
	private boolean isDataExist(DynaBean<?> bean) {
		if (StrFunc.isNull(scEntityInfo.getIdPropertyName())) {
			return false;
		}
		Object idValue = bean.getValue(scEntityInfo.getIdPropertyName());
		if (getRows(idValue) > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 查询该 id 的数据行数
	 * 
	 * @param idValue
	 * @return
	 */
	private int getRows(Object idValue) {
		QueryResult rs = get(idValue);
		if (rs == null) {
			return 0;
		}

		return rs.calcTotalCount();
	}
	
	/**
	 * {@inheritDoc}   
	 */
	public QueryResult<T> get(Object idValue) {
		Query queryExecuter = session.createQuery(scEntityInfo.getClass(), scEntityInfo.getEntityName());
		QueryResult<T> queryResult = queryExecuter.query(
				new Expression(scEntityInfo.getIdPropertyName() + " = ?"),  
				scEntityInfo.getFromDatePropertyName() + "=false", null, idValue);
		return queryResult;
	}
	
	private Object addSecond(Object obj, int second) {
		if (obj == null) {
			return null;
		}
		
		if (obj instanceof Calendar) {
			Calendar tmp = Calendar.getInstance();
			tmp.setTimeInMillis(((Calendar)obj).getTimeInMillis() + second * 1000);
			return tmp;
		}
		if (obj instanceof java.util.Date) {
			return new Timestamp(((java.util.Date)obj).getTime() + second * 1000);
		}
		
		throw new ORMException("com.esen.jdbc.orm.helper.impl.schelperimpl.7","日期类型不正确");
	}
}
