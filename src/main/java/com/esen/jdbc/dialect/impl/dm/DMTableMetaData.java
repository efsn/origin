package com.esen.jdbc.dialect.impl.dm;

import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;

public class DMTableMetaData extends TableMetaDataImpl {

	public DMTableMetaData(DbMetaDataImpl owner, String tablename) {
		super(owner, tablename);
	}

	  /**
	   * 默认值读出来
	   * 字符型，有'', 这里将''去掉；
	   */
	  protected void initColumns() throws Exception {
	    super.initColumns();
	    for(int i=0;i<columnList.size();i++){
	      TableColumnMetaDataImpl col = (TableColumnMetaDataImpl)columnList.get(i);
	      String def = col.getDefaultValue();
	      if(def!=null){
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
