package com.esen.jdbc.data.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

import com.esen.db.sql.analyse.SQLAnalyse;
import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.data.DataReader;
import com.esen.jdbc.dialect.Dialect;

/**
 * 读取指定数据表或者sql的数据；
 * 用于从数据库读取指定的表或者sql的数据，实现DataReader接口；
 * @author dw
 *
 */
public class DataReaderFromDb implements DataReader {
  private Connection conn;
  private String srcsql;
  private Dialect dl;
  private AbstractMetaData meta;
  
  private boolean isTable;
  private Statement stat;
  private ResultSet rs;
  private int totalcount;
  /**
   * 读取指定数据表的数据，可以忽略指定的字段，可以给定过滤条件；
   * @param conn
   * @param tbname
   *          可以是数据库表名，也可是一个sql语句；
   * @param ignorefields
   *          忽略的字段；
   * @param wheresql
   *          过滤条件
   */
  public DataReaderFromDb(Connection conn, String tbname){
    this.conn = conn;
    this.srcsql = tbname;
    this.isTable = SqlFunc.isValidSymbol(srcsql);
    if(!isTable){
      /**
       * 不是表名，判断是不是select * from tbname的形式；
       * 如果是，当表名处理，并解析出表名；
       * 做这个判断是因为 构造函数的tbname参数可以是表名也可以是sql，两种类型处理方式不一样;
       * 如果是表名，复制表时会把字段的是否唯一，是否可空等属性复制过去；
       * 如果是sql，将只是根据字段名和类型创建表，无法获得其他属性；
       */
      String tbn = SqlFunc.getTablename(srcsql);
      if(tbn!=null){
        isTable = true;
        srcsql = tbn;
      }
    }
    this.dl = SqlFunc.createDialect(conn);
    totalcount = -1;//表示还没有计算行数
  }

	private void initTableMeta() throws Exception {
		if (meta == null) {
			if (isTable) {
				meta = dl.createDbMetaData().getTableMetaData(srcsql);
			}
			else {
				meta = dl.getQueryResultMetaData(srcsql);
			}
		}

	}

	private void initQurery() throws Exception {
		initTableMeta();
		if (rs == null) {
			stat = conn.createStatement();
			String sql = getQureySQL();
			rs = stat.executeQuery(sql);
		}
	}
  
  private String getQureySQL() throws Exception {
    if(isTable){
      StringBuffer buf = new StringBuffer(256);
      buf.append("select ");
      buf.append(getSaveFieldStr());
      buf.append(" from ");
      buf.append(srcsql);
      return buf.toString();
    }else
      return srcsql;
  }
  private String getSaveFieldStr() throws Exception {
    int len = meta.getColumnCount();
    StringBuffer buf = new StringBuffer(len*12);
    for (int i = 0; i < len; i++) {
      if (buf.length() != 0) {
        buf.append(",");
      }
      buf.append(getColumnName(meta,i));
    }
    return buf.toString();
  }
  private String getColumnName(AbstractMetaData md, int i) throws Exception{
    String fn = md.getColumnLabel(i);
    if(fn==null||fn.length()==0)
      fn = md.getColumnName(i);
    return SqlFunc.getColumnName(dl,fn);
  }


	/**
	 * 获取表结构，不需要执行获取总行数的sql，也不需要执行查询结果的sql；
	 */
	public AbstractMetaData getMeta() throws Exception {
		initTableMeta();
		return meta;
	}

	/**
	 * 需要获取总行数，才执行相关sql；
	 */
	public int getRecordCount() throws Exception {
		if (totalcount == -1) {
			totalcount = getRecordCount(conn);
		}
		return totalcount;
	}
	
	private String getCountSql() throws Exception {
		if (isTable) {
			return "select count(*) from " + srcsql;
		}
		SQLAnalyse sqla = new SQLAnalyse(srcsql);
		return "select count(*) from (" + sqla.getNoOrderBySQl() + ") count_";
	}
	
  /**
   * 返回表的记录总数
   * @return
   * @throws Exception 
   */
  private int getRecordCount(Connection con) throws Exception {
    String sql = getCountSql();
    Statement sm = con.createStatement();
    try {
      ResultSet rs = sm.executeQuery(sql);
      try {
        if (rs.next()) {
          return rs.getInt(1);
        }
        return 0;
      }
      finally {
        rs.close();
      }
    }
    finally {
      sm.close();
    }
  }
  public Object getValue(int i) throws Exception {
	  initQurery();
    switch (getMeta().getColumnType(i)) {
      case Types.DATE:
        return rs.getDate(i+1);
      case Types.TIME:
        //统一标准，time和timestamp分开处理；
        //db2原来将time类型定义成timestamp， 在setTime时报类型不一致异常；
        //改成定义time类型后，读取该字段不能再用getTimestamp()
        return rs.getTime(i+1);
      case Types.TIMESTAMP:
        return rs.getTimestamp(i+1);
      case Types.BLOB:
      case Types.LONGVARBINARY:
      case Types.VARBINARY:
      case Types.BINARY:
        return rs.getBinaryStream(i+1);
      case Types.CLOB:
      case Types.LONGVARCHAR:
        return rs.getCharacterStream(i+1);
      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.BIGINT:
      case Types.INTEGER: {
        long v = rs.getLong(i+1);
        if (rs.wasNull())
          return null;
        return new Long(v);
      }
      case Types.FLOAT:
      case Types.DECIMAL:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.REAL:{
        //使用Double有精度损失，例：210005000004723196D
        String v = rs.getString(i+1);
        if(rs.wasNull())
          return null;
        return v;
      }
      default:
        return rs.getObject(i+1);
    }
  }
  /**
   * 遍历结果集，第一次调用执行查询sql
   */
  public boolean next() throws Exception {
	  initQurery();
    return rs.next();
  }
  public void close() throws Exception {
    if (rs != null)
      rs.close();
    if (stat != null)
      stat.close();
/*    if (conn != null)
      conn.close();*/
  }
}
