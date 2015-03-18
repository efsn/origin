package com.esen.jdbc.data.impl;

import java.util.ArrayList;
import java.util.List;

import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.i18n.I18N;

/**
 * 只选取目的表的部分字段，并按指定顺序排列字段的表结构；
 * @author dw
 *
 */
public class AbstractMetaDataForFields implements AbstractMetaData {
	private List cols;

	private AbstractMetaData md;

	public AbstractMetaDataForFields(AbstractMetaData md, String[] fields) {
		this.md = md;
		cols = new ArrayList();
		for (int i = 0; i < fields.length; i++) {
			int pos = getFieldsPos(fields[i]);
			cols.add(new Integer(pos));
		}
	}

	private int getFieldsPos(String fd) {
		for (int i = 0; i < md.getColumnCount(); i++) {
			String cname = md.getColumnName(i);
			if (cname.equalsIgnoreCase(fd))
				return i;
		}
		if (md instanceof TableMetaData) {
			TableMetaData tmd = (TableMetaData) md;
			throw new RuntimeException(I18N.getString(
					"com.esen.jdbc.data.impl.abstractmetadataforfields.nofield4table",
					"表{0}不存在字段：{1}", new Object[] { tmd.getTableName(), fd }));
		}
		return -1;
	}

	public int getColumnCount() {
		return cols.size();
	}

	public String getColumnDescription(int i) {
		int pos = getPos(i);
		return md.getColumnDescription(pos);
	}

	private int getPos(int i) {
		Integer p = (Integer) cols.get(i);
		return p.intValue();
	}

	public String getColumnLabel(int i) {
		int pos = getPos(i);
		return md.getColumnLabel(pos);
	}

	public int getColumnLength(int i) {
		int pos = getPos(i);
		return md.getColumnLength(pos);
	}

	public String getColumnName(int i) {
		int pos = getPos(i);
		return md.getColumnName(pos);
	}

	public int getColumnScale(int i) {
		int pos = getPos(i);
		return md.getColumnScale(pos);
	}

	public int getColumnType(int i) {
		int pos = getPos(i);
		return md.getColumnType(pos);
	}

	public int isNullable(int i) {
		return md.isNullable(i);
	}

	public int isUnique(int i) {
		return md.isUnique(i);
	}

}
