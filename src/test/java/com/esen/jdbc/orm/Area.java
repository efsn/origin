package com.esen.jdbc.orm;

import java.util.Date;

public class Area {
	private String areaId;
	
	private String name;

	//缓慢变化
	private Date createDate;
	private Date expiredDate;
	
	//树形结构
	private boolean btype;
	private String parent;	
//	private String upid0;
//	private String upid1;
//	private String upid2;
	
	private Object upids[];
	
	public Area() {
	}
	
	public Area(String areaId, String name, Date createDate, Date expiredDate, String parent, boolean btype) {
		this.setAreaId(areaId);
		this.setName(name);
		this.setCreateDate(createDate);
		this.setExpiredDate(expiredDate);
		this.setBtype(btype);
		this.setParent(parent);
	}
	
	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getExpiredDate() {
		return expiredDate;
	}

	public void setExpiredDate(Date expiredDate) {
		this.expiredDate = expiredDate;
	}

	public boolean getBtype() {
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

	public Object[] getUpids() {
		return upids;
	}

	public void setUpids(Object... upids) {
		this.upids = upids;
	}

}
