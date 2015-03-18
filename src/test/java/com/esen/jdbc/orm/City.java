package com.esen.jdbc.orm;

public class City {
	private String cityId;
	
	private String name;

	//树形结构
	private boolean btype;
	
	private String parent;
	
//	private String upid0;
//	private String upid1;
//	private String upid2;
	
	private Object[] upids;
	
	public City() {
	}
	
	public City(String cityId, String name, String parent, boolean btype) {
		this.setCityId(cityId);
		this.setName(name);
		this.setParent(parent);
		this.setBtype(btype);
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isBtype() {
		return btype;
	}

	public void setBtype(boolean btype) {
		this.btype = btype;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

//	public String getUpid0() {
//		return upid0;
//	}
//
//	public void setUpid0(String upid0) {
//		this.upid0 = upid0;
//	}
//
//	public String getUpid1() {
//		return upid1;
//	}
//
//	public void setUpid1(String upid1) {
//		this.upid1 = upid1;
//	}
//
//	public String getUpid2() {
//		return upid2;
//	}
//
//	public void setUpid2(String upid2) {
//		this.upid2 = upid2;
//	}
	
	public void setUpids(Object... values) {
		this.upids = values;
	}
	
	public Object[] getUpids() {
		return upids;
	}
}
