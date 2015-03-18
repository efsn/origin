package com.esen.jdbc.dialect.impl;

import com.esen.jdbc.dialect.ProcedureMetaData;

public class ProcedureMetaDataImpl implements ProcedureMetaData {

	private String name;

	private boolean isvalid;

	public String getName() {
		return name;
	}

	public boolean isValid() {
		return isvalid;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValid(boolean isvalid) {
		this.isvalid = isvalid;
	}
}
