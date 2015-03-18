package com.esen.db.sql;

import com.esen.util.exp.ExpVar;
import com.esen.util.exp.ExpressionNode;
import com.esen.util.exp.IFormatZz;

/**
 * 用于格式化Oracle 的LeftJoin条件
 * 
 *
 * @author dw
 */
public class IFormatZzForOracleLeftJoinExp implements IFormatZz {
	private String tableAlias;
	public IFormatZzForOracleLeftJoinExp(String tableAlias){
		this.tableAlias = tableAlias;
	}
	public String formatNode(ExpressionNode pnode, ExpressionNode nd) {
		return null;
	}

	public String formatNodeItSelf(ExpressionNode nd) {
		return null;
	}

	public String formatZz(ExpVar var) {
		String field = var.getName();
		int index = field.indexOf(".");
		if(index>0){
			String alias = field.substring(0,index);
			if(alias.equalsIgnoreCase(tableAlias)){
				return field+"(+)";
			}
		}
		return null;
	}

}
