package com.esen.jdbc.dialect.impl;

import com.esen.jdbc.dialect.TriggerMetaData;

public class TriggerMetaDataImpl implements TriggerMetaData {

	private String name;

	private String affecttable;

	public String getAffectTable() {
		return affecttable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAffectTable(String table) {
		this.affecttable = table;
	}
}
