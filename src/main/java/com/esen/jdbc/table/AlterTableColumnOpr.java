package com.esen.jdbc.table;

/**
 * 记录对表做了哪些修改
 *
 * @author zhuchx
 */
public class AlterTableColumnOpr {
	/**
	 * 对表的操作 
	 */
	public int opr;

	/**
	 * 对应的字段
	 */
	public TableColumnMetaDataChange column;

	public AlterTableColumnOpr(int opr, TableColumnMetaDataChange column) {
		this.opr = opr;
		this.column = column;
	}

	public int getOper() {
		return opr;
	}

	public TableColumnMetaDataChange getColumnChange() {
		return column;
	}
}
