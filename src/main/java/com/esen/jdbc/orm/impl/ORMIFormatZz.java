package com.esen.jdbc.orm.impl;

import com.esen.jdbc.FormatExpToSqlExp;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.Property;
import com.esen.util.exp.ExpVar;
import com.esen.util.exp.ExpressionNode;
import com.esen.util.exp.IFormatZz;

/**
 * ORM模块中将条件表达式翻译为SQL表达式的对象
 *
 * @author wang
 */
public class ORMIFormatZz implements IFormatZz {
	private FormatExpToSqlExp formatUtil;

	private EntityInfo entity;

	public ORMIFormatZz(EntityInfo entity, Dialect dialect) {
		this.entity = entity;
		this.formatUtil = new FormatExpToSqlExp(dialect);
	}

	public String formatZz(ExpVar var) {
		if (var.getImplType() == ExpVarProperty.ORM_PROPERTY_TYPE) {
			//不将“字段名”放在ExpVarProperty里面只要是因为Expression对象经常是缓存里面的，而字段名不固定
			Property property = entity.getProperty(var.getName());
			if (property == null) {
				throw new ORMException("com.esen.jdbc.orm.impl.ormiformatzz.1","属性名“{0}”不存在!",new Object[]{var.getName()});
			}
			return property.getFieldName();
		}
		return formatUtil.formatZz(var);
	}

	public String formatNode(ExpressionNode pnode, ExpressionNode nd) {
		return formatUtil.formatNode(pnode, nd);
	}

	public String formatNodeItSelf(ExpressionNode nd) {
		return formatUtil.formatNodeItSelf(nd);
	}

}
