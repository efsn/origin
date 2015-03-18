package com.esen.jdbc.dialect.impl.sybase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataProvider;
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;

/**
 * SybaseAse的表结构实现类；
 * 主要处理字段的默认值；
 * @author dw
 *
 */
public class SybaseTableMetaData extends TableMetaDataImpl {

  public SybaseTableMetaData(DbMetaDataImpl owner, String tablename) {
    super(owner, tablename);
  }
  /**
   * 默认值读出来
   * 字符型，有'', 这里将''去掉；
   */
  protected void initColumns() throws Exception {
	  boolean f = true;
	  
	  Connection con = this.owner.getConnection();
	    try {
	    	/*
	    	 * BUG:ESENFACE-1081: modify by liujin 2014.06.19
	    	 * Sybase 在 md.getColumns(...) 方法中也会有事务内不能执行  create Table 的错误
	    	 */
	    	Dialect dl = SqlFunc.createDialect(con);
	    	f = dl.supportCreateTableInTransaction();

	    	if (!f) {
			  con.commit();
			  con.setAutoCommit(true);
	    	}

	      DatabaseMetaData md = con.getMetaData();
	      String[] tbs = DbDef.getTableNameForDefaultSchema(getTableName(), owner.getDataBaseInfo());
	      ResultSet rs = md.getColumns(null,  tbs[0], tbs[1], null);
	      try {
	        while (rs.next()) {
	          String tbname = rs.getString("TABLE_NAME");
	          /**
	           * 20090218
	           * 可能会有重复的表，比如t_hy将会把tahy表（如果存在）的字段读进来；
	           */
	          if(!tbname.equalsIgnoreCase(tbs[1])){
	            continue;
	          }
	          String colname = rs.getString("COLUMN_NAME");
	          /**
	           * Sybase的系统字段，这里过滤掉；
	           */
	          if(colname.equals("SYB_IDENTITY_COL")){
	            continue;
	          }
	          int tp = rs.getInt("DATA_TYPE");
	          int len = rs.getInt("COLUMN_SIZE");
	          int dec = rs.getInt("DECIMAL_DIGITS");
	          String isnullable = rs.getString("IS_NULLABLE");
	          boolean nullable = isnullable == null || !isnullable.trim().equals("NO");
	          String defvalue = rs.getString("COLUMN_DEF");
	          String desc = rs.getString("REMARKS");
	          TableColumnMetaDataProvider column = new TableColumnMetaDataProvider(this,colname);
	          column.setLable(colname);
	          column.setType(tp);
	          /**
	           * IRPT-5306
	           * 如果字段类型是double,float，获取其精度：
	           * 这里通过meta.getPrecision(i)的值是15,meta.getScale(i)的值是0 明显是不对的，变成整形，造成进度丢失；
	           * 现在改为其精度为-1,表示不知道其精度；
	           * 20110224
	           */
	          if (tp == Types.FLOAT || tp == Types.DOUBLE) {
	            column.setLength(-1);
	          }
	          else {
	            column.setLength(len);
	            column.setScale(dec);
	          }
	          column.setNullable(nullable);
	          column.setDefaultValue(getDefaultValue(defvalue));
	          column.setDesc(desc);
	          addColumn(column);
	        }
	      }
	      finally {
	        rs.close();
	      }
	    }
	    finally {
		  if (!f) {
			  con.setAutoCommit(false);
		  }

	      this.owner.closeConnection(con);
	    }
    for(int i=0;i<columnList.size();i++){
      TableColumnMetaDataImpl col = (TableColumnMetaDataImpl)columnList.get(i);
      String def = col.getDefaultValue();
      if(def!=null){
        /*if(def.startsWith("(")&&def.endsWith(")")){
          def = def.substring(1,def.length()-1);
          col.setDefaultValue(def);
        }*/
        def = def.trim();
        /**
         * 20090806
         * 默认值可能是空串，这里增加长度的判断，否则可能出现数组越界异常；
         */
        if(def.length()>0&&def.charAt(0)=='\''&&def.endsWith("'")){
          def = def.substring(1,def.length()-1);
          col.setDefaultValue(def);
        }
      }
    }
  }

}
