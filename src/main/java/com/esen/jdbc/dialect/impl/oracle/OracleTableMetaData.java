package com.esen.jdbc.dialect.impl.oracle;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.TableSequenceMetaData;
import com.esen.jdbc.dialect.TableTriggerMetaData;
import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableIndexMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;

/**
 * Oracle9i级以上版本的数据库表结构实现类；
 * 
 * @author dw
 *
 */
public class OracleTableMetaData extends TableMetaDataImpl {

	/**
	 * 20090916  dw
	 * 解析触发器的body,找出用于自动增长的字段名,这个正则表达式,需要忽略大小写;
	 */
	private static final Pattern TRIGGERPATTERN = Pattern.compile(".+\\.nextval\\s+into\\s+:new\\.(\\S+)\\s+",
	    Pattern.CASE_INSENSITIVE);

	private boolean isinittrigger;

	private boolean isinitsequence;

	private TableTriggerMetaData[] triggers;

	private TableSequenceMetaData[] sequences;

	public OracleTableMetaData(DbMetaDataImpl owner, String tablename) {
		super(owner, tablename);
	}

	/**
	 * 返回触发器
	 * @return
	 * @throws Exception
	 */
	public TableTriggerMetaData[] getTriggers() {
		if (!isTable) {//如果是视图，不需要获取触发器；
			return null;
		}
		if (!isinittrigger) {
			try {
				this.initTriggers();
				this.isinittrigger = true;
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return triggers;
	}

	//无用的方法
	/*  public TableSequenceMetaData[] getSequences() {
	    if (!isinitsequence) {
	      try {
	        this.initSequences();
	        isinitsequence = true;
	      }
	      catch (Exception e) {
	        throw new RuntimeException(e);
	      }
	    }
	    return sequences;
	  }*/
	//重载方法；以前的重复了
	//改用jdbc系统函数
	/*  protected void initPrimaryKey() {
	    String sql = "select index_name from user_constraints where constraint_type='P' and table_name=\'"
	        + this.getTableName() + "\'";
	    String indexname = null;
	    try {
	      Connection con = this.owner.getConnection();
	      try {
	        Statement sm = con.createStatement();
	        try {
	          ResultSet rs = sm.executeQuery(sql.toUpperCase());
	          try {
	            if (rs.next()) {
	              indexname = rs.getString(1);
	            }
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
	        this.owner.closeConnection(con);
	      }
	    }
	    catch (Exception e) {
	      throw new RuntimeException(e);
	    }
	    if (indexname != null) {
	      TableIndexMetaData[] idx = getIndexes();
	      if (idx != null && idx.length != 0) {
	        for (int i = 0; i < idx.length; i++) {
	          if (idx[i].getName().equalsIgnoreCase(indexname)) {
	            primarykey = idx[i].getColumns();
	            break;
	          }
	        }
	      }
	    }
	  }*/

	/**
	 * 初始化列
	 * @throws Exception
	 */
	protected void initColumns() throws Exception {
		/*
		 * imp by RX 2014.09.10
		 * 为分析组件增加此处理：加上了try/catch处理
		 * 分析组件处理SQL数据源时，如果作为主题表计算，会动态分析表结构、字段信息等
		 * 此时tablename为括号包含起来的SQL语句，如果SQL为复杂SQL，可能含有多个嵌套子查询，
		 * 导致md.getColumns直接抛异常
		 * 如果不加此try/catch处理，会导致直接抛异常无法支持，其实此时用后面的analyseMeta
		 * 动态分析临时结果表即可
		 */
		try {
			Connection con = this.owner.getConnection();
			try {
				DatabaseMetaData md = con.getMetaData();
				String[] tbs = DbDef.getTableNameForDefaultSchema(getTableName(), owner.getDataBaseInfo());
				
				ResultSet rs = md.getColumns(null, tbs[0], tbs[1].toUpperCase(), null);
				try {
					while (rs.next()) {
						String tbname = rs.getString("TABLE_NAME");
						/**
						 * 20090218
						 * 可能会有重复的表，比如t_hy将会把tahy表（如果存在）的字段读进来；
						 */
						if (!tbname.equals(tbs[1].toUpperCase())) {
							continue;
						}
						String colname = rs.getString("COLUMN_NAME");
						int tp = rs.getInt("DATA_TYPE");
						int len = rs.getInt("COLUMN_SIZE");
						int dec = rs.getInt("DECIMAL_DIGITS");
						String isnullable = rs.getString("IS_NULLABLE");
						boolean nullable = isnullable == null || !isnullable.equals("NO");
						//String defvalue = rs.getString("COLUMN_DEF");oracle读这个值是个流
						//String desc = rs.getString("REMARKS");oracle这个值读不到字段注释
						OracleTableColumnMetaData column = new OracleTableColumnMetaData(this, colname);
						column.setLable(colname);
						//IRPT-9102: DECIMAL_DIGITS 为 SQL NULL 时，dec 为 0，增加 dec 是否为 null 的判断
						setType(column, tp, len, dec, rs.getObject("DECIMAL_DIGITS") == null? true : false);
						procNumberLength(tp, len, dec, column, rs.getObject("DECIMAL_DIGITS") == null? true : false);
						column.setNullable(nullable);
						//column.setDefaultValue(defvalue);
						//column.setDesc(desc);
						addColumn(column);
					}
				}
				finally {
					rs.close();
				}
			}
			finally {
				this.owner.closeConnection(con);
			}
		}catch (Exception ex) {
			/*
			 * 直接根据表名读取字段列表时出现异常时，先不抛出，动态分析字段列表
			 * 以支持括号包含起来的SQL语句字段列表分析
			 */
		}
		/**
		 * 20090508
		 * 如果不是数据表，且无法通过getColumns()获取字段信息；
		 * 则使用下面的方法分析其字段结构；
		 * 增加这个方法，是由于如果是同义词，无法获取字段信息；
		 */
		analyseMeta();
	}

	/*
	 * IRPT-9102：
	 * Oracle jdbc 驱动获取表的列元信息数值类型精度为 22，标度为 null 时，认为此字段没有定义精度和标度， 精度和标度都置为  0
	 * 使用 getInt 方法获取标度时，会将  SQL NULL 当做 0，增加相关判断。
	 * 获取结果集元信息时，对于没有定义标度和精度的  NUMBER 类型，获取到的精度为 0，标度为 -127，增加相关处理。
	 */
	private void procNumberLength(int tp, int len, int dec, OracleTableColumnMetaData column, boolean isDecNull) {
		char tc = SqlFunc.getType(tp);
		if (tc == 'N' || tc == 'I') {
			if (len == 0 && dec == -127)
				isDecNull = true;
			if (isDecNull) {
				len = 0;
				dec = 0;
			}
		}

		column.setLength(len);
		column.setScale(dec);
	}

	private void analyseMeta() throws Exception {
		if (columnList != null && columnList.size() > 0) {
			return;
		}
		Connection con = this.owner.getConnection();
		try {
			Statement sm = con.createStatement();
			try {
				ResultSet rs = sm.executeQuery("select * from " + tablename + " where 1>2");
				try {
					ResultSetMetaData md = rs.getMetaData();
					for (int i = 1; i <= md.getColumnCount(); i++) {
						String colname = md.getColumnName(i);
						int tp = md.getColumnType(i);
						int nullable = md.isNullable(i);
						OracleTableColumnMetaData column = new OracleTableColumnMetaData(this, colname);
						column.setLable(colname);
						setType(column, tp, md.getPrecision(i), md.getScale(i), false);
						char tc = SqlFunc.getType(tp);
						if (tc == 'N' || tc == 'I' || tc == 'C') {
							/**
							 * 只有在字符类型和数值类型，才需要读取长度；
							 * oracle9，bolb类型读取md.getPrecision(i)会有异常；
							 */
							procNumberLength(tp, md.getPrecision(i), md.getScale(i), column, false);
						}
						column.setNullable(nullable == 1);
						addColumn(column);
					}
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
			this.owner.closeConnection(con);
		}
	}

	/**
	 * 由于Oracle 的date类型可以存放时间，获取date字段类型是总返回timestamp类型；
	 * 这是为了解决从Oracle拷贝数据到mysql,db2时date类型时间丢失问题；
	 * 注：mysql，db2，sybase日期date类型不包含时间
	 * sqlserver没有date类型，它用datetime，包含时间；
	 * @param column
	 * @param tp
	* @param dec 
	* @param len 
	 */
	private void setType(OracleTableColumnMetaData column, int tp, int len, int dec, boolean isDecNull) {
		if (tp == Types.DATE) {
			tp = Types.TIMESTAMP;
		}
		/**
		 * BI-5876
		 * 由于oracle没有整形，定义的整形，读取类型却是浮点型，这对BI分析造成影响。
		 * 所以这里对读取的浮点类型的小数位数进行判断，如果是0，则返回整形。
		 * --20111122 dw
		 */
		if (SqlFunc.getType(tp) == 'N') {
			/*
			 * IRPT-9102：
			 * 在Oracle，对于定义为 number，却没有指定精度的字段，jdbc 驱动读取列元信息中，字段长度为22，标度为 null，当做数值时标度为 0；
			 * 当根据 number(22,0) 这个结构生成新表copy数据，会造成小数位数值丢失的问题。
			 * 这里进行判断，出现这种情况时，则认为是浮点类型。
			 * 结果集元信息中，number 不指定精度时，精度为 0，标度为 -127，同样当做浮点类型。
			 */
			if (len == 0 && dec == -127)
				isDecNull = true;
			
			if (len > 0 && dec == 0 && isDecNull == false) {
				tp = Types.BIGINT;
			}
		}
		column.setType(tp);
	}

	protected void initDesc() throws Exception {

		String[] tbs = DbDef.getTableNameForDefaultSchema(getTableName(), owner.getDataBaseInfo());
		String sql2 = "select column_name,comments from user_col_comments Where TABLE_NAME=\'" + tbs[1].toUpperCase()
		    + "\'";
		Connection con = this.owner.getConnection();
		try {
			Statement sm = con.createStatement();
			try {
				ResultSet rs = sm.executeQuery(sql2.toUpperCase());
				try {
					while (rs.next()) {
						String name = rs.getString(1);
						String comment = rs.getString(2);
						TableColumnMetaDataImpl column = (TableColumnMetaDataImpl) this.getColumn(name);
						if (column != null) {
							column.setDesc(comment);
						}
					}
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
			this.owner.closeConnection(con);
		}
	}

	/**
	 * 初始化索引
	 * 改用jdbc系统函数获取
	 * @throws Exception
	 */
	/* protected void initIndexes() throws Exception {
	   HashMap idxs = new HashMap();
	   String sql = "select t.index_name,t.uniqueness,c.column_name "
	       + "from user_indexes t,user_ind_columns c "
	       + "where t.index_name=c.index_name and t.table_name=\'"
	       + this.getTableName() + "\' "
	       + "order by t.index_name,c.column_position";
	   Connection con = this.owner.getConnection();
	   try {
	     Statement sm = con.createStatement();
	     try {
	       ResultSet rs = sm.executeQuery(sql.toUpperCase());
	       try {
	         String name;
	         TableIndexMetaDataImpl index = null;
	         while (rs.next()) {
	           name = rs.getString(1);
	           index = (TableIndexMetaDataImpl) idxs.get(name);
	           if (index == null) {
	             index = new TableIndexMetaDataImpl(name);
	             index.setIsUnique("UNIQUE".equalsIgnoreCase(rs.getString(2)));
	             index.setColumns(rs.getString(3));
	             idxs.put(name, index);
	           }
	           else {
	             index.setColumns(ArrayFunc.array2Str(index.getColumns(), ',')
	                 + "," + rs.getString(3));
	           }
	         }
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
	     this.owner.closeConnection(con);
	   }

	   if (idxs.size() != 0) {
	     this.indexes = new TableIndexMetaDataImpl[idxs.size()];
	     idxs.values().toArray(this.indexes);
	   }
	 }*/

	/**
	 * 初始化触发器
	 * @throws Exception
	 */
	protected void initTriggers() throws Exception {
		HashMap trgs = new HashMap();
		String[] tbs = DbDef.getTableNameForDefaultSchema(getTableName(), owner.getDataBaseInfo());
		String sql = "select trigger_name,description,trigger_body from user_triggers" + " where table_name=\'" + tbs[1]
		    + "\'";
		TableTriggerMetaData trigger;
		Connection con = this.owner.getConnection();
		try {
			Statement sm = con.createStatement();
			try {
				ResultSet rs = sm.executeQuery(sql.toUpperCase());
				try {
					while (rs.next()) {
						trigger = new TableTriggerMetaData(rs.getString(1));
						trigger.setDesc(rs.getString(2));
						String body = rs.getString(3);
						trigger.setBody(body);
						String[] column = getTriggerColumns(body);
						trigger.setColumn(column);
						trgs.put(trigger.getName(), trigger);
					}
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
			this.owner.closeConnection(con);
		}

		if (trgs.size() != 0) {
			this.triggers = new TableTriggerMetaData[trgs.size()];
			trgs.values().toArray(this.triggers);
		}
	}

	/**
	 * 通过触发器的body解析出影响的字段
	 * @param body
	 * @return
	 */
	private String[] getTriggerColumns(String body) {
		Matcher m = TRIGGERPATTERN.matcher(body);
		if (m.find() && m.groupCount() > 0) {
			String group = m.group(1);
			return new String[] { group };
		}
		return null;
	}

	/*
	  protected void initSequences() throws Exception {
	    HashMap seqs = new HashMap();
	    String sql = "select sequence_name,min_value,max_value,increment_by from user_sequences "
	        + "where sequence_name in "
	        + "(select distinct referenced_name from user_dependencies where name in ("
	        + "select distinct name from user_dependencies where referenced_name=\'"
	        + getUpcaseTableName()
	        + "\') "
	        + "and referenced_type='SEQUENCE')";
	    Connection con = this.owner.getConnection();
	    try {
	      Statement sm = con.createStatement();
	      try {
	        ResultSet rs = sm.executeQuery(sql.toUpperCase());
	        try {
	          TableSequenceMetaData sequence;
	          String name;
	          while (rs.next()) {
	            name = rs.getString(1);
	            sequence = new TableSequenceMetaData(name);
	            sequence.setMinValue(rs.getInt(2));
	            sequence.setMaxValue(rs.getDouble(3));
	            sequence.setStep(rs.getInt(4));
	            seqs.put(name, sequence);
	          }
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
	      this.owner.closeConnection(con);
	    }

	    if (seqs.size() != 0) {
	      this.sequences = new TableSequenceMetaData[seqs.size()];
	      seqs.values().toArray(this.sequences);
	    }
	  }
	*/
	public void initDefaultValue() throws Exception {
		if (!isTable) {//如果是视图，不需要获取默认值；
			return;
		}
		String[] tbs = DbDef.getTableNameForDefaultSchema(getTableName(), owner.getDataBaseInfo());
		String sql2 = "select column_name,default_length,data_default from USER_tab_columns Where TABLE_NAME=\'" + tbs[1]
		    + "\'";
		Connection con = this.owner.getConnection();
		try {
			Statement sm = con.createStatement();
			try {
				ResultSet rs = sm.executeQuery(sql2.toUpperCase());
				try {
					while (rs.next()) {
						String name = rs.getString(1);
						int len = rs.getInt(2);
						TableColumnMetaDataImpl column = (TableColumnMetaDataImpl) this.getColumn(name);
						if (len > 0) {
							String defaultv = rs.getString(3);
							column.setDefaultValue(getDefaultValue(defaultv));
						}
					}
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
			this.owner.closeConnection(con);
		}

	}

	/**
	 * ORACLE数据库直接使用JDBC查询表的索引结构信息, 当表数据量很大的时候, 非常慢
	 * 理论上, 使用直接的SQL语句进行查询，和采用ORACLE提供的JDBC接口应该是一致的
	 * 但是测试的效果使用直接的SQL语句查询瞬间就可以返回，猜想可能的原因是ORACLE提供的JDBC接口
	 * 方法中不仅仅是索引信息查询，还极有可能在查询之前进行了索引结构信息的验证、刷新等
	 * 这个原因就可以很好的解释为何很多大表下的查询慢，小表快，
	 * 因为大表一般索引信息和索引占用的存储也较多，验证和刷新起来也较慢
	 * 
	 * 参见 http://192.168.1.200/bbs/viewthread.php?tid=917&page=1
	 */
	protected void initIndexes() throws Exception {
		if (columnList == null || columnList.size() == 0) {
			try {
				this.initColumns();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		HashMap pvs = new HashMap();
		Connection con = owner.getConnection();
		try {
			String[] tbs = DbDef.getTableNameForDefaultSchema(getTableName(), owner.getDataBaseInfo());
			PreparedStatement psmt = con.prepareStatement("SELECT A.INDEX_NAME, B.UNIQUENESS, A.COLUMN_NAME from ALL_IND_COLUMNS A LEFT JOIN ALL_INDEXES B ON A.INDEX_NAME = B.INDEX_NAME AND A.TABLE_OWNER=B.TABLE_OWNER where A.TABLE_OWNER=? AND A.TABLE_NAME=?");
			try {
				psmt.setString(1, tbs[0]);
				psmt.setString(2, tbs[1]);
				ResultSet rs = psmt.executeQuery();
				try {
					while (rs.next()) {
						String[] pv = new String[3];
						pv[0] = rs.getString(1);
						if (pv[0] == null)
							continue;
						pv[1] = rs.getString(2);
						pv[2] = rs.getString(3);
						if (!checkIndexField(pv[2]))
							continue;
						Object o = pvs.get(pv[0]);
						if (o == null) {
							List l = new ArrayList();
							l.add(pv);
							pvs.put(pv[0], l);
						}
						else {
							List l = (List) o;
							l.add(pv);
						}
					}
				}
				finally {
					if (rs != null)
						rs.close();
				}
			}
			finally {
				if (psmt != null)
					psmt.close();
			}
			Set keys = pvs.keySet();
			Iterator it = keys.iterator();
			while (it.hasNext()) {
				String indexname = (String) it.next();
				List l = (List) pvs.get(indexname);
				String cols[] = new String[l.size()];
				boolean unique = false;
				for (int i = 0; i < l.size(); i++) {
					String[] pv = (String[]) l.get(i);
					if (i == 0) {
						unique  = pv[1].equals("UNIQUE");
					}
					cols[i] = pv[2];
				}
				TableIndexMetaDataImpl imd = new TableIndexMetaDataImpl(indexname, cols, unique);
				this.addIndexMeta(imd);
			}
		}
		finally {
			owner.closeConnection(con);
		}
	}

}