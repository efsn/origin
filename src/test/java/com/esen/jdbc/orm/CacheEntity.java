package com.esen.jdbc.orm;

import java.io.Serializable;
import java.util.Date;

import com.esen.util.reflect.ExtFieldNameIgnoreCase;

public class CacheEntity extends ExtFieldNameIgnoreCase implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//primary key
	//整型
	private int id;

	//字符类型
	private String name;

	//浮点型
	private double price;
	
	//日期类型
	private Date saleDay;

	public CacheEntity() {
	}
	
	public CacheEntity(int id, String name, double price, Date saleDay) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.saleDay = saleDay;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
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

	public Date getSaleDay() {
		return saleDay;
	}

	public void setSaleDay(Date saleDay) {
		this.saleDay = saleDay;
	}
}
