package com.esen.jdbc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * 格式化输入的存储过程sql语句；
 * 分析出输入，输出参数；
 * <pre>
 * $n={call proc_name('aa',123,#20100203#,$c)}
 *   --> ?={call proc_name(?,?,?,?)}
 *       第一个?是输出参数，整型；
 *       第二个?是输入参数，字符，'aa';
 *       第三个?是输入参数，整型，123;
 *       第四个?是输入参数，日期型，20100203；
 *       第五个?是输出参数，字符型；
 *       
 * {call proc_name2('aa',$cursor)}
 *   --> {call proc_name2(?,?)}
 *       第一个?是输入参数，字符，'aa'
 *       第二个?是输出参数，结果集，主要用于Oracle；
 * 
 * {call proc_name3($'aa')}
 *   --> {call proc_name3(?)}
 *       第一个?是输入输出参数，字符型，输入值是'aa'
 * 
 * </pre>
 * 
 * @author dw
 *
 */
public class FormatCallableSql {
	private StringBuffer callableSql;

	private CallableParam[] cps;

	private String inputsql;

	public FormatCallableSql(String inputsql) {
		this.inputsql = inputsql;
		formatsql();
	}

	private void formatsql() {
		callableSql = new StringBuffer(32);
		List pms = new ArrayList();
		int len = inputsql.length();
		StringBuffer tt = new StringBuffer(32);
		int startCall = -1; //语句中{call ...} 第一个'{'的位置；
		int startParam = -1;//语句中{call p_name(..)} 第一个'('的位置；
		for (int i = 0; i < len; i++) {
			char c = inputsql.charAt(i);
			if (startCall < 0 && c == '{') {
				startCall = i;
			}
			if (startCall < 0) {//读取 {call ...} 前面可能出现的返回参数；
				if (c == '$') {
					tt.setLength(0);
					do {
						tt.append(c);
					}
					while (StrFunc.isABC_xyz(c = inputsql.charAt(++i)));
					CallableParam cp = getOutCallableParam(tt.toString());
					pms.add(cp);
					callableSql.append('?');
				}
			}
			else {
				if (startParam < 0 && c == '(') {
					startParam = i;
				}
				if (startParam < 0) {
					callableSql.append(c);
					continue;
				}
				if (c == '\'') {//字符类型输入参数 'abc'
					i = formatInCharParam(pms, len, tt, i);
					callableSql.append('?');
					continue;
				}
				else if (c == '$') {
					char nc = inputsql.charAt(i + 1);
					if (StrFunc.isABC_xyz(nc)) {//输出参数 $c,$n,$d,$cursor
						i = formatOutParam(pms, len, tt, i);
						callableSql.append('?');
						continue;
					}
					else if (nc == '\'') {//字符类型输入输出参数 $'abc'
						i = formatInOutCharParam(pms, len, tt, i);
						callableSql.append('?');
						continue;
					}
					else if (StrFunc.isDigit(nc) || nc == '-') {//数值型输入输出参数 $12.3
						i = formatInOutNumParam(pms, len, tt, i);
						callableSql.append('?');
						continue;
					}
					else if (nc == '#') {//日期型输入输出参数 $#20100201#
						i = formatInOutDateParam(pms, len, tt, i);
						callableSql.append('?');
						continue;
					}
				}
				else if (StrFunc.isDigit(c) || c == '-') {//数值型输入参数
					i = formatInNumParam(pms, len, tt, i);
					callableSql.append('?');
					continue;
				}
				else if (c == '#') {//日期型输入参数
					i = formatInDateParam(pms, len, tt, i);
					callableSql.append('?');
					continue;
				}
			}
			callableSql.append(c);
		}
		if (pms.size() > 0) {
			cps = new CallableParam[pms.size()];
			pms.toArray(cps);
		}
	}

	private int formatInOutNumParam(List pms, int len, StringBuffer tt, int i) {
		tt.setLength(0);
		i++;
		for (; i < len; i++) {
			char c = inputsql.charAt(i);
			if (StrFunc.isDigit(c) || c == '-' || c == '.') {
				tt.append(c);
			}
			else {
				break;
			}
		}
		i--;
		Double d = new Double(tt.toString());
		CallableParam cp = new CallableParam(CallableParam.CALLABLE_TYPE_INOUT, CallableParam.SQLTYPE_NUMBER, d);
		pms.add(cp);
		return i;
	}

	private int formatInDateParam(List pms, int len, StringBuffer tt, int i) {
		tt.setLength(0);
		i++;
		for (; i < len; i++) {
			char c = inputsql.charAt(i);
			if (c != '#') {
				tt.append(c);
			}
			else {
				break;
			}
		}
		Calendar cal = StrFunc.str2date(tt.toString(), null);
		CallableParam cp = new CallableParam(CallableParam.CALLABLE_TYPE_IN, CallableParam.SQLTYPE_DATE, cal);
		pms.add(cp);
		return i;
	}

	private int formatInOutDateParam(List pms, int len, StringBuffer tt, int i) {
		tt.setLength(0);
		i++;
		i++;
		for (; i < len; i++) {
			char c = inputsql.charAt(i);
			if (c != '#') {
				tt.append(c);
			}
			else {
				break;
			}
		}
		Calendar cal = StrFunc.str2date(tt.toString(), null);
		CallableParam cp = new CallableParam(CallableParam.CALLABLE_TYPE_INOUT, CallableParam.SQLTYPE_DATE, cal);
		pms.add(cp);
		return i;
	}

	private int formatInNumParam(List pms, int len, StringBuffer tt, int i) {
		tt.setLength(0);
		for (; i < len; i++) {
			char c = inputsql.charAt(i);
			if (StrFunc.isDigit(c) || c == '-' || c == '.') {
				tt.append(c);
			}
			else {
				break;
			}
		}
		i--;//...,123.4,... 读取到4后面的,才能知道数值参数读取完毕，这里i--是为了返回数值参数的末位位置，外面循环会读取下一个字符',';
		Double d = new Double(tt.toString());
		CallableParam cp = new CallableParam(CallableParam.CALLABLE_TYPE_IN, CallableParam.SQLTYPE_NUMBER, d);
		pms.add(cp);
		return i;
	}

	/**
	 * 字符类型输入输出参数 $'abc'
	 * 里如果有'使用''转义，比如：$'Tom''s'
	 * @param pms
	 * @param len
	 * @param tt
	 * @param i
	 * @return
	 */
	private int formatInOutCharParam(List pms, int len, StringBuffer tt, int i) {
		tt.setLength(0);
		char c = inputsql.charAt(i);
		i++;
		i++;
		for (; i < len; i++) {
			c = inputsql.charAt(i);
			if (c == '\'') {
				char nc = inputsql.charAt(i + 1);
				if (nc == '\'') {
					tt.append(c);
					i++;
				}
				else {
					break;
				}
			}
			else {
				tt.append(c);
			}
		}
		CallableParam cp = new CallableParam(CallableParam.CALLABLE_TYPE_INOUT, CallableParam.SQLTYPE_CHAE,
				tt.toString());
		pms.add(cp);
		return i;
	}

	/**
	 * 输出参数 $c,$n,$d,$cursor
	 * @param pms
	 * @param len
	 * @param tt
	 * @param i
	 * @return
	 */
	private int formatOutParam(List pms, int len, StringBuffer tt, int i) {
		tt.setLength(0);
		char c = inputsql.charAt(i);
		tt.append(c);
		i++;
		for (; i < len; i++) {
			c = inputsql.charAt(i);
			if (StrFunc.isABC_xyz(c)) {
				tt.append(c);
			}
			else {
				break;
			}
		}
		i--;//同formatInNumParam() 方法中的注释；
		CallableParam cp = getOutCallableParam(tt.toString());
		pms.add(cp);
		return i;
	}

	private CallableParam getOutCallableParam(String p) {
		String p2 = p.substring(1).trim();
		if (p2.length() == 1) {
			char c = p2.charAt(0);
			if (c == 'c' || c == 'C') {
				return new CallableParam(CallableParam.CALLABLE_TYPE_OUT, CallableParam.SQLTYPE_CHAE, null);
			}
			else if (c == 'd' || c == 'D') {
				return new CallableParam(CallableParam.CALLABLE_TYPE_OUT, CallableParam.SQLTYPE_DATE, null);
			}
			else if (c == 'n' || c == 'N') {
				return new CallableParam(CallableParam.CALLABLE_TYPE_OUT, CallableParam.SQLTYPE_NUMBER, null);
			}
		}
		else if (p2.equalsIgnoreCase("cursor")) {
			return new CallableParam(CallableParam.CALLABLE_TYPE_OUT, CallableParam.SQLTYPE_RESULTSET, null);
		}
		//    throw new RuntimeException("错误的输入参数格式："+p);
		throw new RuntimeException(I18N.getString("com.esen.jdbc.formatcallablesql.wrongargs",
				"错误的输入参数格式：{0}", new Object[] { p }));
	}

	/**
	 * 字符类型输入参数 'abc'
	 * 里如果有'使用''转义，比如：'Tom''s'
	 * @param pms
	 * @param len
	 * @param tt
	 * @param i
	 * @return
	 */
	private int formatInCharParam(List pms, int len, StringBuffer tt, int i) {
		tt.setLength(0);
		char c = inputsql.charAt(i);
		i++;
		for (; i < len; i++) {
			c = inputsql.charAt(i);
			if (c == '\'') {
				char nc = inputsql.charAt(i + 1);
				if (nc == '\'') {
					tt.append(c);
					i++;
				}
				else {
					break;
				}
			}
			else {
				tt.append(c);
			}
		}
		CallableParam cp = new CallableParam(CallableParam.CALLABLE_TYPE_IN, CallableParam.SQLTYPE_CHAE, tt.toString());
		pms.add(cp);
		return i;
	}

	/**
	 * 返回供执行的存储过程语句，形如：
	 * ?={call proc_name(?,?,?,?)}
	 * {call proc_name2(?,?)}
	 * @return
	 */
	public String getCallableSql() {
		return callableSql.toString();
	}

	/**
	 * 顺序获取存储过程参数；
	 * 可能为空，表示没有参数；
	 * @return
	 */
	public CallableParam[] getParams() {
		return cps;
	}
}
