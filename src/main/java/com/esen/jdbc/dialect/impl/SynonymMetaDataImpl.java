package com.esen.jdbc.dialect.impl;

import com.esen.jdbc.dialect.SynonymMetaData;

public class SynonymMetaDataImpl implements SynonymMetaData {

	private String name;

	private String tablename;

	private String owner;

	public String getName() {
		return name;
	}

	public String getTableName() {
		return tablename;
	}

	public String getTableOwner() {
		return owner;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTableName(String tablename) {
		this.tablename = tablename;
	}

	public void setTableOwner(String owner) {
		this.owner = owner;
	}
}
