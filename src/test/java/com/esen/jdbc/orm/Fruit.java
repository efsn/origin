package com.esen.jdbc.orm;

import java.util.Date;

import com.esen.util.reflect.ExtProperties;

public class Fruit implements ExtProperties {
	private String id;//primarykey

	private String name;

	private double price;
	
	public Fruit(String id, String name, double price, String produceArea, String introduction, Date saleDay) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.produceArea = produceArea;
		this.introduction = introduction;
		this.saleDay = saleDay;
	}

	private String produceArea;

	private String introduction;

	private Date saleDay;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getProduceArea() {
		return produceArea;
	}

	public void setProduceArea(String produceArea) {
		this.produceArea = produceArea;
	}

	public String getIntroduction() {
		return introduction;
	}

	public void setIntroduction(String introduction) {
		this.introduction = introduction;
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
}

