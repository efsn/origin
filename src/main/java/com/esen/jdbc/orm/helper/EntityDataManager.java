package com.esen.jdbc.orm.helper;

import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.esen.jdbc.DataCopy;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.Executer;
import com.esen.jdbc.orm.Iterator;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.ORMSQLException;
import com.esen.jdbc.orm.Property;
import com.esen.jdbc.orm.QueryResult;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.helper.impl.ImportDataImpl;
import com.esen.jdbc.orm.impl.ExecuterProxy;
import com.esen.jdbc.orm.impl.ORMUtil;
import com.esen.jdbc.orm.impl.SessionImpl;
import com.esen.util.StrFunc;
import com.esen.util.exp.Expression;
import com.esen.util.reflect.DynaBean;

public class EntityDataManager {

	/**
	 * 修复树结构的数据
	 * 
	 * 1.检查是否叶子节点这个字段的值，如果不正确，主动修改
	 * 2.完善 upids
	 * 3.如果是缓慢变化树，修复当前时间点的数据。
	 */
	public static void repairData(Session session, EntityInfo entityInfo) {
		repairData(session, entityInfo, 0);
	}
	
	/**
	 * 修复缓慢变化树结构指定的时间点的数据
	 * 
	 * @param session
	 * @param entityInfo
	 * @param time
	 */
	public static void repairData(Session session, EntityInfo entityInfo, long time) {
		if (session == null || entityInfo == null) {
			throw new ORMException("com.esen.jdbc.orm.helper.entitydatamanager.1","参数不能为空");
		}
		
		if (!(entityInfo instanceof TreeEntityInfo)) {
			return;
		}

		Property btypeProp = entityInfo.getProperty(((TreeEntityInfo)entityInfo).getBtypePropertyName());
		Property upidProp = entityInfo.getProperty(((TreeEntityInfo)entityInfo).getUpIdPropertyName());
		Property idProp = entityInfo.getProperty(((TreeEntityInfo)entityInfo).getIdPropertyName());
		if (btypeProp == null
				|| upidProp == null
				|| idProp == null) {
			return;
		}			

		//如果是缓慢变化树，更新节点信息时，需要根据指定时间处理
		Property fromDate = null;
		Property toDate = null;
		boolean isSCTree = false;
		Timestamp ts = null;
		if (entityInfo instanceof SCTreeEntityInfo) {
			fromDate = entityInfo.getProperty(((SCTreeEntityInfo)entityInfo).getFromDatePropertyName());
			toDate = entityInfo.getProperty(((SCTreeEntityInfo)entityInfo).getToDatePropertyName());
			if (fromDate != null && toDate != null) {
				isSCTree = true;
				if (time == 0) {
					time = System.currentTimeMillis();
				}
				ts = new Timestamp(time);
			}
		}

		Dialect dl = ((SessionImpl)session).getDialect();
		String tsStr = "";
		if (ts != null) {
			tsStr = dl.funcToDateTime(StrFunc.formatDateTime((Date)ts));
		}
		String tsCondition = "";
		String whereCondition = "";
		String andCondition = "";
		if (isSCTree) {
			tsCondition = "(" + fromDate.getFieldName() + " <= " + tsStr
					+ " and " + toDate.getFieldName() + " >= " + tsStr + ")";
			whereCondition = " where " + tsCondition;
			andCondition = " and " + tsCondition;
		}
		
		StringBuilder selectSql = new StringBuilder();
		selectSql.append("select " + idProp.getFieldName() + ", 1");
		selectSql.append(" from " + entityInfo.getTable());
		selectSql.append(" where " + idProp.getFieldName());
		selectSql.append(" not in(select " + upidProp.getFieldName());
		selectSql.append(" from " + entityInfo.getTable() + whereCondition + ")");
		selectSql.append(" and (" + btypeProp.getFieldName() + " <> '1' or " + btypeProp.getFieldName() + " is null)");
		selectSql.append(andCondition);
		
		selectSql.append(" union ");
		selectSql.append("select " + idProp.getFieldName() + ", 0");
		selectSql.append(" from " + entityInfo.getTable());
		selectSql.append(" where " + idProp.getFieldName());
		selectSql.append(" in(select " + upidProp.getFieldName());
		selectSql.append(" from " + entityInfo.getTable() + whereCondition + ")");
		selectSql.append(" and (" + btypeProp.getFieldName() + " <> '0' or " + btypeProp.getFieldName() + " is null)");
		selectSql.append(andCondition);
		
		try {
			Connection conn = session.getConnection();
			try {			
				//修复 btype
				Statement stmt = conn.createStatement();
				try {
					ResultSet rs = stmt.executeQuery(selectSql.toString());
					try {
						String sql = "update " + entityInfo.getTable()
								+ " set " + btypeProp.getFieldName() + " = ?"
								+ " where " + idProp.getFieldName() + " = ?"
								+ andCondition;

						PreparedStatement pstmt = conn.prepareStatement(sql);
						try {
							while (rs.next()) {
								pstmt.setString(1, rs.getString(2));
								pstmt.setString(2, rs.getString(1));
								pstmt.addBatch();
							}
							pstmt.executeBatch();
						} finally {
							pstmt.close();
						}
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
				
				//修复 upids
				repairUpids(session, entityInfo, isSCTree, tsCondition, time);
				
				//清除垃圾数据
				/*
				 * delete from tablename 
				 * where upid0 is null
				 * and fromdate...
				 */
				String[] upids = StrFunc.splitByChar(((TreeEntityInfo)entityInfo).getUpidsPropertyName(), ',');
				Property upid0 = entityInfo.getProperty(upids[0]);
				
				StringBuilder deleteSql = new StringBuilder();
				deleteSql.append("delete from ");
				deleteSql.append(entityInfo.getTable());
				deleteSql.append(" where (");
				deleteSql.append(upid0.getFieldName() + " is NULL ");
				deleteSql.append(" or " + upid0.getFieldName() + "='')");
				deleteSql.append(andCondition);
				
				Statement stmt2 = conn.createStatement();
				try {
					stmt2.execute(deleteSql.toString());
				} finally {
					stmt2.close();
				}
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			throw new ORMSQLException("com.esen.jdbc.orm.helper.entitydatamanager.2","修复数据出错", e);
		}
	}
	
	/**
	 * 修复树形结构的  upids
	 * 
	 * @param session
	 * @param entityInfo
	 * @throws Exception
	 */
	private static void repairUpids(Session session, EntityInfo entityInfo, boolean isSCTree, String tsCondition, long time) throws SQLException {
		String[] upids = StrFunc.splitByChar(((TreeEntityInfo)entityInfo).getUpidsPropertyName(), ',');
		
		if (upids == null || upids.length <= 0) {
			return;
		}
		
		int level = upids.length;
		Property[] upidProps = new Property[level];
		String[] upidsValue = new String[level];
		for (int i = 0; i < level; i++) {
			upidProps[i] = entityInfo.getProperty(upids[i]);
			if (upidProps[i] == null) {
				throw new ORMException("com.esen.jdbc.orm.helper.entitydatamanager.3","实体{0}没有属性{1}",new Object[]{entityInfo.getEntityName(),upids[i]});
			}
			
			upidsValue[i] = TreeEntityInfo.UPID_NULL;
		}
		
		Property upidProp = entityInfo.getProperty(((TreeEntityInfo)entityInfo).getUpIdPropertyName());
		String parent = ((TreeEntityInfo)entityInfo).getRootUpid();
		
		StringBuilder sql = new StringBuilder("update " + entityInfo.getTable() + " SET ");
		for (int i = 0; i < level; i++) {
			sql.append(upidProps[i].getFieldName() + " = ?");
			if (i != level - 1) {
				sql.append(", ");
			}
		}
		sql.append(" where ").append(upidProp.getFieldName()).append("=?");
		if (isSCTree) {
			sql.append(" and " + tsCondition);
		}

		Connection conn = session.getConnection();
		try {
			String updateSql = "update " + entityInfo.getTable() 
					+ " SET " + upidProps[0].getFieldName() + "=''";
			if (isSCTree) { //缓慢变化树结构
				updateSql = updateSql + " where " + tsCondition;
			}
			
			//将所有的 upid0 置为空
			//在增量导入数据时，历史数据会存在 upid0 不为空的情况，清空垃圾数据时不满足条件，无法删除
			Statement stmt = conn.createStatement();
			try {
				stmt.execute(updateSql);
			}
			finally {
				stmt.close();
			}
			
			PreparedStatement pstmt = conn.prepareStatement(sql.toString());
			try {
				if (!isSCTree) { //树结构
					TreeHelper treeHelper = HelperFactory.getTreeHelper(session, (TreeEntityInfo) entityInfo);
					_repairUpids(entityInfo, treeHelper, null, isSCTree, pstmt, parent, upidsValue, time);
				} else {//缓慢变化树结构
					SCTreeHelper sctreeHelper = HelperFactory.getSCTreeHelper(session, (SCTreeEntityInfo) entityInfo);
					_repairUpids(entityInfo, null, sctreeHelper, isSCTree, pstmt, parent, upidsValue, time);
				}
			}
			finally {
				pstmt.close();
			}
		} finally {
			conn.close();
		}
	}

	/**
	 * 递归修复节点信息
	 * 
	 * @param entityInfo 实体信息
	 * @param treeHelper 
	 * @param ps
	 * @param idValue
	 * @param upids
	 * @throws Exception
	 */
	private static void _repairUpids(EntityInfo entityInfo, TreeHelper treeHelper, SCTreeHelper scTreeHelper, boolean isSCTree, PreparedStatement pstmt, Object idValue, Object[] upids, long time) throws SQLException {
		int index = 0;
		for (index = 0; index < upids.length; index++) {
			pstmt.setObject(index + 1, upids[index]);
		}
		pstmt.setObject(index + 1, idValue);
		pstmt.execute();

		Property btypeProp = entityInfo.getProperty(((TreeEntityInfo)entityInfo).getBtypePropertyName());
		Property idProp = entityInfo.getProperty(((TreeEntityInfo)entityInfo).getIdPropertyName());

		QueryResult rs = null;
		if (isSCTree) {
			rs = scTreeHelper.listChildren(idValue, false, time);
		} else {
			rs = treeHelper.listChildren(idValue, false);
		}

		Iterator it = rs.iterator(-1, -1);
		try {
			while (it.hasNext()) {
				Object obj = it.next();
				if (obj == null) {
					return;
				}
				DynaBean<?> bean = DynaBean.getDynaBean(obj);	
				Object[] tupids = (Object[]) ORMUtil.getPropertyValue(entityInfo, bean, TreeEntityInfo.UpidsPropName);
	
				Object newIdValue = null;			
				for (int i = 0; i < tupids.length; i++) {
					if (StrFunc.isNull((String) tupids[i]) 
							|| StrFunc.compareStr((String) tupids[i], TreeEntityInfo.UPID_NULL)) {
						tupids[i] = ORMUtil.getPropertyValue(entityInfo, bean, idProp.getName());
						newIdValue = tupids[i];
						break;
					}
				}
				
				boolean btypeValue = (Boolean) ORMUtil.getPropertyValue(entityInfo, bean, btypeProp.getName());
				if (!btypeValue && newIdValue != null) { //非叶子结点
					_repairUpids(entityInfo, treeHelper, scTreeHelper, isSCTree, pstmt, newIdValue, tupids, time);
				}
			}
		} finally {
			it.close();	
		}
	}
	
	/**
	 * 导出数据
	 * 
	 * @param session 会话
	 * @param entityInfo 实体信息
	 * @param out 
	 */
	public static void exportDataToCSV(Session session, EntityInfo entityInfo, Writer out) {
		exportDataToCSV(session, entityInfo, out, new ExportDataHelper());
	}

	/**
	 * 导出数据
	 * 
	 * @param session 会话
	 * @param entityInfo 实体信息
	 * @param out 
	 * @param helper 导出的数据的配置参数
	 */
	public static void exportDataToCSV(Session session, EntityInfo entityInfo, Writer out, ExportDataHelper helper) {
		if (session == null 
				|| entityInfo == null
				|| out == null
				|| helper == null) {
			throw new ORMException("com.esen.jdbc.orm.helper.entitydatamanager.1","参数不能为空");
		}
		
		String[] str = getExportInfo(session, entityInfo, helper);		
		try {
			Connection conn = session.getConnection();
			try {
				out.write(str[0], 0, str[0].length());
				DataCopy.createInstance().exportDataToCSV(conn, str[1], out, helper.getSeparator(), '"');
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			throw new ORMException(e);
		}
	}
	  
	/**
	 * 导入数据
	 * 
	 * @param session 会话
	 * @param entityInfo 实体信息
	 * @param in 
	 * @param opt 导入数据的参数
	 */
	public static void importDataFromCSV(Session session, EntityInfo entityInfo, ImportDataHelper imhelper, Reader in) {
		ImportDataImpl importData = new ImportDataImpl(session, entityInfo, imhelper, in);
		
		try  {
			boolean isAutoCommit = ((SessionImpl)session).getAutoCommit();
			
			session.beginTransaction();
			try {
				importData.writeData();
				repairData(session, entityInfo);
				
				session.commit();
			} finally {
				if (isAutoCommit) {
					((SessionImpl)session).setAutoCommit(isAutoCommit);
				}
			}
		} catch (SQLException e) {
			session.rollback();
			throw new ORMSQLException(e);
		}
	}

	/**
	 * 获取导出文件头的实体名 和 查询全表数据的 sql 语句
	 * 
	 * @param session
	 * @param entityInfo
	 * @param exp
	 * @param orderbyProperties
	 * @param separator
	 * @param params
	 * @return
	 */
	private static String[] getExportInfo(Session session, EntityInfo entityInfo, ExportDataHelper helper) {	
		String[] ret = new String[2];
		
		List<Property> props = null;
		String[] propertyNames = helper.getPropertyNames();
		if (propertyNames == null || propertyNames.length <= 0) {
			props = entityInfo.getProperties();
		} else {
			props = new ArrayList<Property>();
			for (int i = 0; i < propertyNames.length; i++) {
				Property prop = entityInfo.getProperty(propertyNames[i]);
				if (prop == null) {
					throw new ORMException("com.esen.jdbc.orm.helper.entitydatamanager.4","属性{0}不存在",new Object[]{propertyNames[i]});
				}
				props.add(prop);
			}
		}
		
		StringBuilder str = new StringBuilder();
		StringBuilder headinfo = new StringBuilder();
		
		char separator = helper.getSeparator();
		boolean isShowCaption = helper.isShowCaption();
		str.append("select ");
		for (int i = 0; i < props.size(); i++) {
			Property prop = props.get(i);
			
			if (i != 0) {
				str.append(", ");
				headinfo.append(separator);
			}
			
			str.append(prop.getFieldName());
			
			String caption = null;
			if (isShowCaption) {
				caption = prop.getCaption();
			}
			if (StrFunc.isNull(caption)) {
				headinfo.append(prop.getName());
			} else {
				headinfo.append(caption);
			}
		}
		ret[0] = "#" + headinfo.toString() + "\r\n";
		
		str.append(" from " + entityInfo.getTable());

		Expression exp = helper.getExp();
		Executer exe = session.createExecuter(entityInfo.getClass(), entityInfo.getEntityName());
		if (exp != null) {	
			String condition = ((ExecuterProxy)exe).getSql(exp);
			if (!StrFunc.isNull(condition)) {
				str.append(" where ");
				str.append(condition);
			}
		}

		String orderby = ((ExecuterProxy)exe).getSql(helper.getOrderbyProperties());
		if (!StrFunc.isNull(orderby)) {
			str.append(" order by ");
			str.append(orderby);
		}
		
		Object[] params = helper.getParams();
		if (params == null) {
			ret[1] = str.toString();
			return ret;
		}
		
		//将 sql 语句中的  ? 替换为 param
		StringBuilder sql = new StringBuilder();

		Dialect dl = ((SessionImpl)session).getDialect();
		int i = 0;
		int head = 0;
		int tail = str.indexOf("?", head);
		while (tail > 0 && i < params.length) {
			sql.append(str.substring(head, tail));
			if (params[i] instanceof Calendar) {
				Timestamp ts = ORMUtil.calendar2Timestamp((Calendar)params[i]);
				sql.append(dl.funcToDateTime(StrFunc.formatDateTime((Date)ts)));
			} else if (params[i] instanceof Date) {
				sql.append(dl.funcToDateTime(StrFunc.formatDateTime((Date)params[i])));
			} else {
				sql.append("'" + dl.formatConstStr(params[i].toString()) + "'");
			}
			
			head = tail + 1;
			i++;
			tail = str.indexOf("?", head);
		}
		sql.append(str.substring(head));
		
		ret[1] = sql.toString();
		return ret;
	}
}
