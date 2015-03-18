package com.esen.jdbc.orm.impl;

import com.esen.util.exp.ExpException;
import com.esen.util.exp.ExpFuncOp;
import com.esen.util.exp.ExpressionCompiler;

public class OrmExpressionCompiler extends ExpressionCompiler {

	@Override
	protected ExpFuncOp getOpInfo(String token) throws ExpException {
		if(token.equals(ORMExpCompilerHelper.PROPERTY_PARAMETER_VAR.getName())){
			return null;
		}
		return super.getOpInfo(token);
	}

}
