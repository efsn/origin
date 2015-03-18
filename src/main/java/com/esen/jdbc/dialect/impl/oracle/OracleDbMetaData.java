package com.esen.jdbc.dialect.impl.oracle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.ProcedureMetaData;
import com.esen.jdbc.dialect.SynonymMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.TriggerMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.util.ExceptionHandler;

/*
 * SELECT * FROM USER_TAB_COLUMNS WHERE table_name = 'ZDSYJKYBB4_B1';
 * select * from USER_tables
 */

public class OracleDbMetaData extends DbMetaDataImpl {
  /**
   * oracle10中获得表名时会有BIN$XXXXX==$0这样的表,在获得表时去掉
   */
  private final static String SQL_TABLE10g = "select TABLE_NAME from USER_TABLES where DROPPED='NO'";
  private final static String SQL_TABLE = "select TABLE_NAME from USER_TABLES";
  /**
   * 同义词当视图返回
   */
  private final static String SQL_VIEW = "select VIEW_NAME from User_Views";
  
  private final static String SQL_PROCEDURE = "select OBJECT_NAME,'1' from user_procedures where object_type='PROCEDURE'";
  
  private final static String SQL_TRIGGER = "select TRIGGER_NAME,TABLE_NAME from user_triggers";
  
  private final static String SQL_SYNONYM = "select SYNONYM_NAME,TABLE_OWNER,TABLE_NAME from user_synonyms";

  public OracleDbMetaData(ConnectionFactory dbf) {
    super(dbf);
  }

  public OracleDbMetaData(Connection con) {
    super(con);
  }

  private ArrayList querySql(String sql) {
    try {
      Connection con = this.getConnection();
      try {
        Statement stm = con.createStatement();
        try {
          ResultSet rst = stm.executeQuery(sql);
          ArrayList tabs = new ArrayList();
          try {
            while (rst.next()) {
              tabs.add(rst.getString(1));
            }
            return tabs;
          }
          finally {
            rst.close();
          }
        }
        finally {
          stm.close();
        }
      }
      finally {
        closeConnection(con);
      }
    }
    catch (Exception e) {
      ExceptionHandler.rethrowRuntimeException(e);
      return null;
    }
  }

  protected ArrayList selectAllTableNames() {
    if(getDataBaseInfo().getDatabaseMajorVersion()>=10)
      return querySql(SQL_TABLE10g);
    else return querySql(SQL_TABLE);
  }

  protected TableMetaData createTableMetaData(String tablename){
    if (getDataBaseInfo().isOracle8i()) {
      return new Oracle8iTableMetaData(this, tablename);
    }
    return new OracleTableMetaData(this, tablename);
  }
  protected ArrayList selectAllViewNames() {
    return querySql(SQL_VIEW);
  }

	protected ProcedureMetaData[] getAllProcedureMetaData() {
		try {
			Connection con = getConnection();
			try {
				Statement sm = con.createStatement();
				try {
					ResultSet rs = sm.executeQuery(SQL_PROCEDURE);
					try {
						return super.getProcedureMetaData(rs);
					}
					finally {
						rs.close();
					}
				}
				finally {
					sm.close();
				}
			}
			finally {
				con.close();
			}
		}
		catch (Exception e) {
			ExceptionHandler.rethrowRuntimeException(e);
			return null;
		}
	}

	protected SynonymMetaData[] getAllSynonymMetaData() {
		try {
			Connection con = getConnection();
			try {
				Statement sm = con.createStatement();
				try {
					ResultSet rs = sm.executeQuery(SQL_SYNONYM);
					try {
						return super.getSynonymMetaData(rs);
					}
					finally {
						rs.close();
					}
				}
				finally {
					sm.close();
				}
			}
			finally {
				con.close();
			}
		}
		catch (Exception e) {
			ExceptionHandler.rethrowRuntimeException(e);
			return null;
		}
	}

	protected TriggerMetaData[] getAllTriggerMetaData() {
		try {
			Connection con = getConnection();
			try {
				Statement sm = con.createStatement();
				try {
					ResultSet rs = sm.executeQuery(SQL_TRIGGER);
					try {
						return super.getTriggerMetaData(rs);
					}
					finally {
						rs.close();
					}
				}
				finally {
					sm.close();
				}
			}
			finally {
				con.close();
			}
		}
		catch (Exception e) {
			ExceptionHandler.rethrowRuntimeException(e);
			return null;
		}
	}
}
