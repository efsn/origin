package com.esen.jdbc.dialect.impl.db2;

import com.esen.jdbc.dialect.Dialect;

public class DB29Def extends DB2Def {

  public DB29Def(Dialect dl) {
    super(dl);
  }

  /**
   * 20090824
   * db2 9 支持部分直接修改字段属性，但不支持类型的转换，且不支持修改字段名；
   * 例：
create table testdb (field2 varchar(10))

select * from testdb

alter table testdb add field varchar(10)

alter table testdb drop column field

alter table testdb alter column field SET DATA TYPE varchar(16)

alter table testdb add fieldnum DECIMAL(10,2)

alter table testdb alter column fieldnum SET DATA TYPE DECIMAL(6,2)
--ALTER TABLE "JLL.TESTDB" 为列 "FIELDNUM" 指定的属性与现有列不兼容。不支持改小

alter table testdb alter column fieldnum SET DATA TYPE DECIMAL(16,2)

alter table testdb alter column fieldnum SET DATA TYPE DECIMAL(17,3)

--增大DECIMAL范围，支持

alter table testdb alter column field  set not null

alter table testdb alter column field  drop not null

alter table testdb alter column field  set default '123'

alter table testdb alter column field  drop default

ALTER TABLE testdb 
alter column fieldnum SET DATA TYPE DECIMAL(18,4)
alter column field  set not null
DROP COLUMN field2
-- 批量执行


--不支持修改字段名；

alter table testdb alter column field SET DATA TYPE varchar(18) alter column field SET not null
--扩容同时设置not null, 出异常："FIELD" 是一个重复的名称。

alter table testdb alter column field SET DATA TYPE DECIMAL(16,2)
--字符改数值，异常：ALTER TABLE "JLL.TESTDB" 为列 "FIELD" 指定的属性与现有列不兼容。

alter table testdb alter column fieldnum SET DATA TYPE varchar(20)
--数值改字符，异常：ALTER TABLE "JLL.TESTDB" 为列 "FIELDNUM" 指定的属性与现有列不兼容。

   */
  /*public void modifyColumn2(Dialect dl,Connection conn, String tablename, String col,
      String new_col, char coltype, int len, int dec, String defaultvalue,
      boolean unique) throws Exception {
    if (col == null || col.length() == 0) {
      throw new Exception("修改列名不能为空！");
    }
    StringBuffer ddl = new StringBuffer(64).append("ALTER TABLE ");
    ddl.append(tablename);
    ddl.append(" ALTER COLUMN ");
    StringBuffer renddl = null;
    if (new_col != null && new_col.length() > 0 && !col.equals(new_col)) {
      renddl = new StringBuffer(64).append("RENAME COLUMN ");
      renddl.append(col).append(" TO ").append(new_col);
      ddl.append(getFldDdl(new FieldInfo(new_col + " SET DATA TYPE ", coltype,
          len, dec, defaultvalue, true, unique)));
    }
    else {
      ddl.append(getFldDdl(new FieldInfo(col + " SET DATA TYPE ", coltype, len,
          dec, defaultvalue, true, unique)));
    }
    Statement stmt = conn.createStatement();
    try {
      if (renddl != null) {
        stmt.execute(renddl.toString());
      }
      //System.out.println(ddl.toString());
      stmt.execute(ddl.toString());
    }
    finally {
      stmt.close();
    }
  }*/

  /**
   * 20090824
   * DB2 9 可以支持直接删除字段；
   * 
   * 20090825
   * DB2 9 对表字段的删除，在实际使用过程中有如下问题：
   * 删除tbname的field1字段后，做查询：selelct field2 from tbname 等都出异常；
   * 解决：考虑不使用DB2 9 的直接删除字段功能，即alter table tbname drop column field1
   * 而使用创建新表，copy数据，在更名的方式；
   * 下面代码注释掉，使用父类DB2 8 的删除字段方法；
   */
  /*public void dropColumn(Connection conn, String tablename, String col)
      throws Exception {
    StringBuffer ddl = new StringBuffer(64).append("ALTER TABLE ");
    ddl.append(tablename);
    ddl.append(" DROP COLUMN ").append(col);
    Statement stmt = conn.createStatement();
    try {
      stmt.execute(ddl.toString());
    }
    finally {
      stmt.close();
    }
  }*/
}
