package com.esen.jdbc.dialect.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.ProcedureMetaData;
import com.esen.jdbc.dialect.SynonymMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.TriggerMetaData;
import com.esen.util.ArrayFunc;
import com.esen.util.StrFunc;

/**
 * 此数据库结构实现类，总是实时获取信息；
 *
 * @author dw
 */
public  class DbMetaDataImpl implements DbMetaData {
  
  protected ConnectionFactory connectionFactory;
  
  protected Connection con;

  public DbMetaDataImpl(ConnectionFactory dbf) {
    this.connectionFactory = dbf;
  }

  public DbMetaDataImpl(Connection con) {
    this.con = con;
  }

  public final List getTableNames() {
      return selectAllTableNames();
  }
  
  public final List getViewNames(){
	  return selectAllViewNames();
  }
  
  public DataBaseInfo getDataBaseInfo(){
    if (connectionFactory!=null){
      return connectionFactory.getDbType();
    }
    return DataBaseInfo.createInstance(con);
  }
  
  public Connection getConnection() throws SQLException{
    if (con!=null)
      return con;
    return connectionFactory.getConnection();
  }
  
  public void closeConnection(Connection con) throws SQLException{
    if (con!=this.con)
      con.close();
  }
  
  /**
   * 20090814
   * 返回框架schema名；
   * db2,Oracle一般就是用户名大写；
   * 其他数据库就是用户名；
   * sqlserver数据库，如果是sa用户，schema=dbo
   * @param username
   * @return
   */
  protected String getSchemaName(){
    return getDataBaseInfo().getDefaultSchema();
  }
  
  protected ArrayList getAllObjectName(String[] objs){
    try {
      Connection conn = getConnection();
      try {
        DatabaseMetaData _dmd = conn.getMetaData();
        ResultSet _rs = _dmd.getTables(null, getSchemaName(), null, objs);
        ArrayList l = new ArrayList(32);
        try {
          while (_rs.next()) {
            String tbName = _rs.getString(3);
            l.add(tbName);
          }
        }
        finally {
          if (_rs != null)
            _rs.close();
        }
        return l;
      }
      finally {
        closeConnection(conn);
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected ArrayList selectAllTableNames() {
    return getAllObjectName(new String[]{"TABLE"});
  }

  protected ArrayList selectAllViewNames() {
    return getAllObjectName(new String[]{"VIEW"});
  }

  public TableMetaData getTableMetaData(String tableName)  {
    return createTableMetaData(tableName);
  }
 
  protected TableMetaData createTableMetaData(String tablename){
    return new TableMetaDataImpl(this,tablename);
  }

	public ProcedureMetaData[] getProcedureMetaData() {
		ProcedureMetaData[] procedures = getAllProcedureMetaData();
		if (procedures == null) {
			procedures = new ProcedureMetaData[0];
		}
		return procedures;
	}

	public SynonymMetaData[] getSynonymMetaData() {
		SynonymMetaData[] synonyms = getAllSynonymMetaData();
		if (synonyms == null) {
			synonyms = new SynonymMetaData[0];
		}
		return synonyms;
	}

	public TriggerMetaData[] getTriggerMetaData() {
		TriggerMetaData[] triggers = getAllTriggerMetaData();
		if (triggers == null) {
			triggers = new TriggerMetaData[0];
		}
		return triggers;
	}

	protected ProcedureMetaData[] getAllProcedureMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

	protected SynonymMetaData[] getAllSynonymMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

	protected TriggerMetaData[] getAllTriggerMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

	protected ProcedureMetaData[] getProcedureMetaData(ResultSet rs) throws Exception {
		ArrayList list = null;
		while (rs.next()) {
			String name = rs.getString(1);
			boolean isvalid = StrFunc.parseBoolean(rs.getString(2), false);
			if (list == null) {
				list = new ArrayList();
			}
			ProcedureMetaDataImpl o = new ProcedureMetaDataImpl();
			o.setName(name);
			o.setValid(isvalid);
			list.add(o);
		}
		return (ProcedureMetaData[]) ArrayFunc.list2array(list);
	}

	protected SynonymMetaData[] getSynonymMetaData(ResultSet rs) throws Exception {
		ArrayList list = null;
		while (rs.next()) {
			String name = rs.getString(1);
			String owner = rs.getString(2);
			String table = rs.getString(3);
			if (list == null) {
				list = new ArrayList();
			}
			SynonymMetaDataImpl o = new SynonymMetaDataImpl();
			o.setName(name);
			o.setTableName(table);
			o.setTableOwner(owner);
			list.add(o);
		}
		//TODO 考虑表不在此数据库中,而是在其它机器上的服务器中
		return (SynonymMetaData[]) ArrayFunc.list2array(list);
	}

	protected TriggerMetaData[] getTriggerMetaData(ResultSet rs) throws Exception {
		ArrayList list = null;
		while (rs.next()) {
			String name = rs.getString(1);
			String table = rs.getString(2);
			if (list == null) {
				list = new ArrayList();
			}
			TriggerMetaDataImpl o = new TriggerMetaDataImpl();
			o.setName(name);
			o.setAffectTable(table);
			list.add(o);
		}
		return (TriggerMetaData[]) ArrayFunc.list2array(list);
	}

	public void reset() {
		
	}
}
