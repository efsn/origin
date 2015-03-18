package com.esen.jdbc.sql.parser.token;

public class SqlTokenizerImplArray implements SqlTokenizer {

  private SqlTokenItem items[];

  private int index = 0;

  public SqlTokenizerImplArray(SqlTokenItem[] items) {
    this.items = items;
  }

  public boolean hasMoreTokens() {
    return (items.length > 0) && (index < items.length);
  }

  public SqlTokenItem nextToken() throws SqlSyntaxError {
    return hasMoreTokens() ? items[index++] : null;
  }

  public boolean nextTokenIs(String item) {
    return hasMoreTokens() ? items[index].isItem(item) : false;
  }

}
