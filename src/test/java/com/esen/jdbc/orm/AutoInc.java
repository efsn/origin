package com.esen.jdbc.orm;

public class AutoInc {
	
	private int id; //自增列
	
	private String value;

	public AutoInc() {
	}

	public AutoInc(String value) {
		setValue(value);
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
