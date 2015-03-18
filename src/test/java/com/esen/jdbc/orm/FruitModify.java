package com.esen.jdbc.orm;

import java.util.Date;

import com.esen.util.reflect.ExtProperties;

public class FruitModify implements ExtProperties {
	private int id;//primarykey

	private String nameNew;

	private String price;
	
	public FruitModify(int id, String name, String price, String produceArea, Date saleDay) {
		this.id = id;
		this.nameNew = name;
		this.price = price;
		this.area = produceArea;
		this.saleDay = saleDay;
	}

	private String area;

	private Date saleDay;

	private String addProp;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNameNew() {
		return nameNew;
	}

	public void setNameNew(String name) {
		this.nameNew = name;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public Date getSaleDay() {
		return saleDay;
	}

	public void setSaleDay(Date saleDay) {
		this.saleDay = saleDay;
	}

	public void setExtValue(String propertyName, Object value) {

	}

	public Object getExtValue(String propertyName) {
		return null;
	}

	public String getAddProp() {
		return addProp;
	}

	public void setAddProp(String addProp) {
		this.addProp = addProp;
	}
}

