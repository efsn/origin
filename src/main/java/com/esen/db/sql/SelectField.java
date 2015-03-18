package com.esen.db.sql;

import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.i18n.I18N;

/**
 * 把一个子查询作为字段；
 * 列：select tt.*,(select id from table1 t where t.id=tt.id) as id from (select * from table2) tt
 * 这种方法我们的系统主要用来olap取单位名称，速度很快
 * @author user
 *
 */
public class SelectField extends Field {
	private SelectTable st;

	public SelectField(SelectTable st, String tag) {
		this.st = st;
		if (tag == null || tag.length() == 0)
			throw new RuntimeException(I18N.getString("com.esen.db.sql.selectfield.1",
					"别名不能为空；"));
		this.tag = tag;
		this.type = Field.FTYPE_SELECTFIELD;
	}

	public String toString(Dialect dl) {
		return "(" + st.getSql(dl) + ") as " + tag;
	}

	public String toString() {
		return toString(null);
	}
	//  //国际化测试
	//  public static void main(String[] args){
	//	  System.out.println(I18N.getString("com.esen.db.sql.selectfield.notnull4tag", ""));
	//  }
}
