package com.esen.jdbc.data.impl;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataImpl;
import com.esen.util.StrFunc;

public class TableColumnMetaDataForWriter extends TableColumnMetaDataImpl {
	public TableColumnMetaDataForWriter(){
		super("");
	}

	public TableColumnMetaDataForWriter(String str) {
		super(str);
		if (!StrFunc.isNull(str)) {
			String name = str;
			int type = DataWriterToDbFromCSV.DEFAULT_TYPE;
			int length = DataWriterToDbFromCSV.DEFAULT_LENGTH;
			int scale = 0;
			int i1 = str.indexOf('(');
			if (i1 > 0) {
				name = str.substring(0, i1);
				char t = str.charAt(i1 + 1);
				type = com.esen.jdbc.SqlFunc.expType2SqlType(t);
				int i2 = str.indexOf('|');
				int i3 = str.indexOf(')');
				if (i2 > i1 && i3 > i2) {
					String lens = str.substring(i2 + 1, i3);
					int i4 = lens.indexOf('|');
					if (i4>0) {
						length = StrFunc.str2int(lens.substring(0, i4), DataWriterToDbFromCSV.DEFAULT_LENGTH);
						scale = StrFunc.str2int(lens.substring(i4+1), 0);
					}else{
						length = StrFunc.str2int(lens, DataWriterToDbFromCSV.DEFAULT_LENGTH);
					}
				}
			}
			setName(name);
			setType(type);
			setLen(length);
			setScale(scale);
		}
	}

	public TableColumnMetaDataForWriter(String name, String lable, int type, int length, int scale, String defaultValue,
			String desc) {
		super(name, lable, type, length, scale, defaultValue, desc);
	}

	public TableColumnMetaDataForWriter(String name, String label, int type, int length, int scale) {
		this(name, label, type, length, scale, null, null);
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(getName()).append('(').append(SqlFunc.getType(getType())).append('|').append(getLen());
		if(getScale()!=0){
			sb.append('|').append(getScale());
		}
		sb.append(')');
		return sb.toString();
	}
}
