package com.esen.jdbc.sql.parser.token;

import java.util.ArrayList;
import java.util.HashMap;

import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * <p>
 * Title: BI@Report
 * </p>
 * <p>
 * Description: 网络报表在线分析系统
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company: 武汉新连线科技有限公司
 * </p>
 * 
 * @author daqun
 * @version 5.0
 */

public class SqlTokenizerImpl implements SqlTokenizer {
  private static final String ops = "+-*/()'=<>,";

  private String sql;

  private int pos;

  private int maxLength;

  private static final HashMap keywords = new HashMap();
  static {
    keywords.put(TAG_SELECT, TAG_SELECT);
    keywords.put(TAG_FROM, TAG_FROM);
    keywords.put(TAG_WHERE, TAG_WHERE);
    keywords.put(TAG_ORDER, TAG_ORDER);
    keywords.put(TAG_GROUP, TAG_GROUP);
    keywords.put(TAG_HAVING, TAG_HAVING);
    keywords.put(TAG_INTERSECT, TAG_INTERSECT);

    keywords.put(TAG_AS, TAG_AS);
    keywords.put(TAG_BY, TAG_BY);
    keywords.put(TAG_UNION, TAG_UNION);
    keywords.put(TAG_BETWEEN, TAG_BETWEEN);
    keywords.put(TAG_AND, TAG_AND);
    keywords.put(TAG_OR, TAG_OR);
    keywords.put(TAG_NOT, TAG_NOT);
    keywords.put(TAG_LIKE, TAG_LIKE);
    keywords.put(TAG_IN, TAG_IN);
    keywords.put(TAG_DISTINCT, TAG_DISTINCT);
    keywords.put(TAG_ASC, TAG_ASC);
    keywords.put(TAG_DESC, TAG_DESC);
    keywords.put(TAG_LEFT, TAG_LEFT);
    keywords.put(TAG_RIGHT, TAG_RIGHT);
    keywords.put(TAG_JOIN, TAG_JOIN);
    keywords.put(TAG_INNER, TAG_INNER);
    keywords.put(TAG_OUTER, TAG_OUTER);
    keywords.put(TAG_FULL, TAG_FULL);
    keywords.put(TAG_ALL, TAG_ALL);
    keywords.put(TAG_MINUSSECT, TAG_MINUSSECT);
  }

  private static final boolean isKeyWord(String key) {
    return keywords.get(key.toUpperCase()) != null;
  }

  private final Object getKeyWordObject(String key) {
    //    if (StrFunc.compareText(key, TAG_SELECT))
    //      return new SelectFieldsStateMent();
    //    else if (StrFunc.compareText(key, TAG_FROM))
    //      return new FromStateMent();
    //    else if (StrFunc.compareText(key, TAG_WHERE))
    //      return new ConditionStatetMent();
    //    else if (StrFunc.compareText(key, TAG_GROUP))
    //      return new FieldsStateMent();
    //    else if (StrFunc.compareText(key, TAG_ORDER))
    //      return new FieldsStateMent();
    //    else if (StrFunc.compareText(key, TAG_HAVING))
    //      return new ConditionStatetMent();
    //    else
    return key.toUpperCase();
  }

  public SqlTokenizerImpl(String sql) {
    this.sql = sql;
    this.pos = 0;
    this.maxLength = sql.length();
  }

  private boolean isNum(int startpos, int endpos) {
    boolean dot = false;
    for (int i = startpos; i <= endpos; i++) {
      char ch = sql.charAt(i);
      if (ch == '.') {
        if (dot)
          return false;
        dot = true;
      }
      else if ((ch < '0') || (ch > '9'))
        return false;
    }
    return true;
  }

  private boolean isConst(int startpos, int endpos) {
    return ((sql.charAt(startpos) == '\'') && (sql.charAt(endpos) == '\''))
        || (isNum(startpos, endpos));
  }

  /**
   * 略过空白符号
   * 
   * @param startPos
   * @return
   */
  private int skipDelimiters(int startPos) {
    int i = startPos;
    while (i < maxLength) {
      if (sql.charAt(i) > 32)
        return i;
      i++;
    }
    return i;
  }

  /* （非 Javadoc）
   * @see com.esen.jdbc.sql.parser.SqlTokenizer#hasMoreTokens()
   */
  public boolean hasMoreTokens() {
    pos = skipDelimiters(pos);
    return pos < maxLength;
  }

  /**
   * 从startPos开始扫描,直到遇到ch为止,若breakOnWord为true,则遇到非空字符也停止
   * 
   * @param startPos
   * @param ch
   * @param breakOnWord
   * @return
   */
  private int tokenUntilChar(int startPos, char ch, boolean breakOnWord) {
    int i = startPos;
    while (i < maxLength) {
      char c = sql.charAt(i);
      if (ch == c)
        return i;
      if (breakOnWord && (c > 32))
        return i - 1;
      i++;
    }
    return -1;
  }

  private boolean tokenIsOp;

  /**
   * 从startPos开始扫描,遇到处理以下情况返回 1.空白符号 2.常量表达式,' 遇到'必须扫描到下一个'才返回,没有结束的抛异常
   * 3.数值型表达式 200,300等 扫描到下一个非数字或者.为止 4.运算符号+-/\*()<>= >= <=是一个符号
   * 
   * @param startPos
   * @return
   * @throws SqlSyntaxError
   */
  private int scanToken(int startPos) throws SqlSyntaxError {
    tokenIsOp = false;
    int i = startPos;
    while (i < maxLength) {
      char ch = sql.charAt(i);
      if (ch <= 32)
        return i - 1;
      if (ops.indexOf(ch) != -1) {
        if (i > startPos)
          // 遇到操作符，返回
          return i - 1;
        // 操作符开头
        if (ch == '\'') {
          // 字符串常量表达式开头
          int j = tokenUntilChar(i + 1, '\'', false);
          if (j > i)
            return j;
//          throw new SqlSyntaxError("常量表达式没有正常结束!");
          throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.token.sqltokenizerimpl.wrongend", "常量表达式没有正常结束!"));
        }
        tokenIsOp = true;
        if ((ch == '<') || (ch == '>')) {
          // 检索<=,>=
          int j = tokenUntilChar(i + 1, '=', true);
          return j > i ? j : startPos;
        }
        else
          return startPos;
      }
      i++;
    }
    return i;
  }

  private String isOpWord(int startpos, int endpos) {
    if (tokenIsOp) {
      if (endpos > startpos)
        return sql.charAt(startpos) == '>' ? TAG_NOTLESS : this.TAG_NOTGREATE;
      else
        return sql.substring(startpos, startpos + 1);
    }
    else
      return null;
  }

  /**判断下一个token是否是指定的项目
   * @param item
   * @param ignorcase
   * @return
   */
  public boolean nextTokenIs(String item) {
    int p = this.pos;
    try {
      SqlTokenItem token = nextToken();
      if ((token != null) && (token.isItem(item)))
        return true;
    }
    catch (SqlSyntaxError e) {
    }
    finally {
      this.pos = p;
    }
    return false;
  }

  /* （非 Javadoc）
   * @see com.esen.jdbc.sql.parser.SqlTokenizer#nextToken()
   */
  public SqlTokenItem nextToken() throws SqlSyntaxError {
    if (!hasMoreTokens())
      return null;
    int startpos = pos;
    pos = scanToken(pos);
    int endpos = pos < maxLength ? pos : maxLength - 1;
    pos++;
    String element = sql.substring(startpos, endpos + 1);
    if (isConst(startpos, endpos))
      // 常量,数字，字符串
      return SqlTokenItem.getConstObj(element);
    else {
      String op = isOpWord(startpos, endpos);
      if (op != null) {
        // 是操作符号
        if (op.equals(TAG_LEFTQ)) {
          // 左括号,扫描右括号
          endpos = tokenQuoted(startpos + 1);
          if (endpos == -1)
//            throw new SqlSyntaxError(sql + "第" + startpos
//                + "个字符处的左括号没有正常结束,找不到匹配的右括号!");
        	  throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.token.sqltokenizerimpl.noykh", "{0}第{1}个字符处的左括号没有正常结束,找不到匹配的右括号!", new Object[]{sql,String.valueOf(startpos)}));
        }
        return SqlTokenItem.getOpObj(op);
      }
      // select from where and or not 等 关键字
      if (isKeyWord(element))
        return SqlTokenItem.getKeyObj(element);
      return SqlTokenItem.getVarObj(element);
    }
  }

  /**token到对应的右括号为止,若includeRIGHTQ则找到后添加右括号到末尾，否则不加
   * @param tokens
   * @param includeRIGHTQ
   * @throws SqlSyntaxError 
   * @throws TokenNotFound 
   */
  private void tokenUntilQuoted(ArrayList tokens, boolean includeRIGHTQ)
      throws SqlSyntaxError, TokenNotFound {
    while (hasMoreTokens()) {
      SqlTokenItem token = nextToken();
      if (token.isItem(this.TAG_RIGHTQ)) {
        // find
        if (includeRIGHTQ)
          tokens.add(token);
        return;
      }
      if (token.isItem(this.TAG_LEFTQ)) {
        //遇到左括号，找相应的右括号
        tokenUntilQuoted(tokens, true);
        continue;
      }
      tokens.add(token);
    }
    throw new TokenNotFound();
  }

  // 扫描)匹配,返回最近的一个)匹配,若没有发现或者没有匹配，返回-1
  private int tokenQuoted(int startpos) {
    int i = startpos;
    while (i < maxLength) {
      char ch = sql.charAt(i);
      if (ch == ')')
        return i;
      if (ch == '(') {
        int j = tokenQuoted(i + 1);
        if (j <= i)
          return -1;
        i = j;
      }
      i++;
    }
    return -1;
  }

}
