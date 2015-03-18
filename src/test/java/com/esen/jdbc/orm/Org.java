package com.esen.jdbc.orm;

import java.util.Calendar;

public class Org {
	private String orgId;
	
	private String name;

	private Calendar createDate;
	
	//缓慢变化
	private Calendar fromDate;
	private Calendar toDate;
	
	public Org() {
	}
	
	public Org(String orgId, String name, Calendar createDate) {
		this.setOrgId(orgId);
		this.setName(name);
		this.setCreateDate(createDate);
	}
	
	public Org(String orgId, String name, Calendar createDate, Calendar fromDate, Calendar toDate) {
		this.setOrgId(orgId);
		this.setName(name);
		this.setCreateDate(createDate);
		this.setFromDate(fromDate);
		this.setToDate(toDate);
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Calendar getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Calendar createDate) {
		this.createDate = createDate;
	}

	public Calendar getFromDate() {
		return fromDate;
	}

	public void setFromDate(Calendar fromDate) {
		this.fromDate = fromDate;
	}

	public Calendar getToDate() {
		return toDate;
	}

	public void setToDate(Calendar toDate) {
		this.toDate = toDate;
	}
}
