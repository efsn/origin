package com.esen.jdbc.dialect.impl.greenplum;

import java.sql.Types;

import com.esen.jdbc.dialect.impl.TableColumnMetaDataProvider;
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;

public class GreenplumTableColumnMetaData extends TableColumnMetaDataProvider {

	public GreenplumTableColumnMetaData(TableMetaDataImpl meta, String name) {
	    super(meta, name);
    }
	public int getType() {
		switch(sqltype){
	      case -9:
	        return Types.VARCHAR;
	    }
	    return this.sqltype;
	}
}
