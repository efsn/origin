package com.esen.jdbc.orm.impl;

import com.esen.jdbc.orm.Property;
import com.esen.util.exp.ExpVarImpl;

public class ExpVarProperty extends ExpVarImpl {

	public static final int ORM_PROPERTY_TYPE = 0xFF;

	public ExpVarProperty(Property property) {
		super(property.getName(), property.getType());
	}

	public int getImplType() {
		//覆盖父类方法，让formatZz判断时方便判断出该对象
		return ORM_PROPERTY_TYPE;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 242859251320365741L;

}
