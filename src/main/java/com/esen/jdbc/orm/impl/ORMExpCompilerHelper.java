package com.esen.jdbc.orm.impl;

import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.Property;
import com.esen.util.StrFunc;
import com.esen.util.exp.ExpCompilerHelper;
import com.esen.util.exp.ExpException;
import com.esen.util.exp.ExpFuncOp;
import com.esen.util.exp.ExpUtil;
import com.esen.util.exp.ExpVar;
import com.esen.util.exp.ExpVarImpl;

public class ORMExpCompilerHelper implements ExpCompilerHelper {

	public static final ExpVar PROPERTY_PARAMETER_VAR = new ExpVarImpl("?", ExpUtil.TOOBJ);

	private EntityInfo entity;

	public ExpVar getExpVar(String var) throws ExpException {
		if (StrFunc.compareStr(PROPERTY_PARAMETER_VAR.getName(), var)) {
			return PROPERTY_PARAMETER_VAR;
		}
		if (entity != null) {
			Property property = entity.getPropertyIgoreCase(var);
			if (property != null) {
				return new ExpVarImpl(property.getFieldName(), property.getType());
			}
		}
		return null;
	}

	public ORMExpCompilerHelper(EntityInfo entity) {
		this.entity = entity;
	}

	public ExpFuncOp getFuncInfo(String funcName) throws ExpException {
		return null;
	}

}
