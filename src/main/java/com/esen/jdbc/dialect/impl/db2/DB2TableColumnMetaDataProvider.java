package com.esen.jdbc.dialect.impl.db2;

import com.esen.jdbc.dialect.impl.TableColumnMetaDataProvider;
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;

public class DB2TableColumnMetaDataProvider extends TableColumnMetaDataProvider {

	public DB2TableColumnMetaDataProvider(TableMetaDataImpl meta, String name) {
		super(meta, name);
	}
	public boolean isAutoInc() {
	    /**
	     * DB2在初始化获取字段信息时，已经通过查系统表获得了是否为自增长属性值；
	     * 这里不需要在查数据库从rsmeta.isAutoIncrement(i) 获取了；
	     */
	    isInitAutlInc = true;
	    return super.isAutoInc();
	  }
}
