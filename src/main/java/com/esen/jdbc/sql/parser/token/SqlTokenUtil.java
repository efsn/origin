package com.esen.jdbc.sql.parser.token;

import java.util.ArrayList;
import java.util.HashMap;

import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.i18n.I18N;

public class SqlTokenUtil implements SqlTokenizer {
  private SqlTokenizer tokennizer;

  public SqlTokenUtil(SqlTokenizer tokennizer) {
    this.tokennizer = tokennizer;
  }

  public boolean hasMoreTokens() {
    return tokennizer.hasMoreTokens();
  }

  public SqlTokenItem nextToken() throws SqlSyntaxError {
    return tokennizer.nextToken();
  }

  /**
   * 扫描直到遇到指定关键字为止,结果中不包含关键字,找不到就抛出TokenNotFound异常
   * 
   * @param token
   * @return
   * @throws SqlSyntaxError
   */
  public SqlTokenItem[] tokenUntil(String key) throws SqlSyntaxError,
      TokenNotFound {
    ArrayList tokens = new ArrayList();
    while (hasMoreTokens()) {
      SqlTokenItem token = nextToken();
      if (token.isItem(key)) {
        // find
        SqlTokenItem result[] = new SqlTokenItem[tokens.size()];
        tokens.toArray(result);
        return result;
      }
      tokens.add(token);
    }
    throw new TokenNotFound();
  }

  /**
   * token到对应的右括号为止,若includeRIGHTQ则找到后添加右括号到末尾，否则不加
   * 
   * @param tokens
   * @param includeRIGHTQ
   * @throws SqlSyntaxError
   * @throws TokenNotFound
   */
  private void tokenUntilQuoted(ArrayList tokens, boolean includeQ)
      throws SqlSyntaxError, TokenNotFound {
    while (hasMoreTokens()) {
      SqlTokenItem token = nextToken();
      if (token.isItem(SqlTokenizer.TAG_RIGHTQ)) {
        // find
        if (includeQ)
          tokens.add(token);
        return;
      }
      tokens.add(token);
      if (token.isItem(SqlTokenizer.TAG_LEFTQ)) {
        // 遇到左括号，找相应的右括号
        tokenUntilQuoted(tokens, true);
        continue;
      }
    }
    throw new TokenNotFound();
  }

  public SqlTokenItem[] tokenUntilQuoted() throws SqlSyntaxError, TokenNotFound {
    ArrayList tokens = new ArrayList();
    tokenUntilQuoted(tokens, false);
    SqlTokenItem result[] = new SqlTokenItem[tokens.size()];
    tokens.toArray(result);
    return result;
  }

  /**
   * token 直到遇到关键字为止,若没找到抛出TokenNotFound异常,map中放着关键字的列表
   * 找到则返回token的内容,结果的最后一个项目中为关键字
   * 
   * @return
   * @throws SqlSyntaxError
   * @throws TokenNotFound
   */
  public SqlTokenItem[] tokenUntil(HashMap map) throws SqlSyntaxError,
      TokenNotFound {
    ArrayList tokens = new ArrayList();
    while (hasMoreTokens()) {
      SqlTokenItem token = nextToken();
      tokens.add(token);
      if (map.get(token.getItem()) != null) {
        // find
        SqlTokenItem result[] = new SqlTokenItem[tokens.size()];
        tokens.toArray(result);
        return result;
      }
    }
    throw new TokenNotFound();
  }

  /**
   * 下一个表达式必须是指定的key,如果不是就抛出异常
   * 
   * @param key
   * @throws SqlSyntaxError
   */
  public SqlTokenItem nextTokenMust(String key) throws SqlSyntaxError {
    SqlTokenItem token = null;
    if (hasMoreTokens())
      token = nextToken();
    if ((token == null) || !token.isItem(key))
//      throw new SqlSyntaxError("不是" + key + "表达式!");
    	 throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.token.sqltokenutil.notmatch", "不是{0}表达式!", new Object[]{key}));
    return token;
  }

  public boolean nextTokenIs(String item) {
    return tokennizer.nextTokenIs(item);
  }
}
