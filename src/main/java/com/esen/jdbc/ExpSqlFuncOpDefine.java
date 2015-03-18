/**
 * 
 */
package com.esen.jdbc;

import java.util.Calendar;

import com.esen.util.exp.ExpUtil;
import com.esen.util.exp.ExpEvaluateHelper;
import com.esen.util.exp.ExpressionNode;
import com.esen.util.exp.ExpFuncOp;
import com.esen.util.exp.ExpVarArray;
import com.esen.util.exp.impl.funcs.ExpFuncAbstract;
import com.esen.util.i18n.I18N;

/**
 * @author yukun
 *
 */
public class ExpSqlFuncOpDefine extends ExpFuncAbstract implements ExpFuncOp {

	private String funcname;

	public ExpSqlFuncOpDefine(String funcName) {
		this.funcname = funcName;
	}

	public int getPriority() {
		return 0;
	}

	public int getIndex() {
		return -1;
	}

	public String getName() {
		return funcname;
	}

	public boolean isFunc() {
		return true;
	}

	public int getParamCount() {
		return 0;
	}

	public char checkOpParams(ExpressionNode v) {
		return ExpUtil.TOVAR;
	}

	public char checkOpParams(ExpressionNode v1, ExpressionNode v2) {
		return ExpUtil.TOVAR;
	}

	public char checkParams(ExpressionNode[] nodes, int len) {
		return ExpUtil.TOVAR;
	}

	public long evaluateInt(ExpressionNode node, ExpEvaluateHelper h) {
		//    throw new RuntimeException("ExpSqlFuncOpDefine无法计算："+node);
		throw new RuntimeException(I18N.getString("com.esen.jdbc.expsqlfuncopdefine.unablecal",
				"ExpSqlFuncOpDefine无法计算：{0}", new Object[] { node }));
	}

	public String evaluateStr(ExpressionNode node, ExpEvaluateHelper h) {
		//    throw new RuntimeException("ExpSqlFuncOpDefine无法计算："+node);
		throw new RuntimeException(I18N.getString("com.esen.jdbc.expsqlfuncopdefine.unablecal",
				"ExpSqlFuncOpDefine无法计算：{0}", new Object[] { node }));
	}

	public double evaluateDouble(ExpressionNode node, ExpEvaluateHelper h) {
		//    throw new RuntimeException("ExpSqlFuncOpDefine无法计算："+node);
		throw new RuntimeException(I18N.getString("com.esen.jdbc.expsqlfuncopdefine.unablecal",
				"ExpSqlFuncOpDefine无法计算：{0}", new Object[] { node }));
	}

	public boolean evaluateBool(ExpressionNode node, ExpEvaluateHelper h) {
		//    throw new RuntimeException("ExpSqlFuncOpDefine无法计算："+node);
		throw new RuntimeException(I18N.getString("com.esen.jdbc.expsqlfuncopdefine.unablecal",
				"ExpSqlFuncOpDefine无法计算：{0}", new Object[] { node }));
	}

	public Calendar evaluateDate(ExpressionNode node, ExpEvaluateHelper h) {
		//    throw new RuntimeException("ExpSqlFuncOpDefine无法计算："+node);
		throw new RuntimeException(I18N.getString("com.esen.jdbc.expsqlfuncopdefine.unablecal",
				"ExpSqlFuncOpDefine无法计算：{0}", new Object[] { node }));
	}

	public ExpVarArray evaluateArray(ExpressionNode node, ExpEvaluateHelper h) {
		//    throw new RuntimeException("ExpSqlFuncOpDefine无法计算："+node);
		throw new RuntimeException(I18N.getString("com.esen.jdbc.expsqlfuncopdefine.unablecal",
				"ExpSqlFuncOpDefine无法计算：{0}", new Object[] { node }));
	}

}
