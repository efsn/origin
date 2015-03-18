package com.esen.jdbc.sql.parser;

import java.util.ArrayList;
import java.util.HashMap;

import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.jdbc.sql.parser.token.*;
import com.esen.util.i18n.I18N;

/**
 * <p>Title: BI@Report</p>
 * <p>Description: 网络报表在线分析系统</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 武汉新连线科技有限公司</p>
 * @author daqun
 * @version 5.0
 */

public class SQlCompiler {
  private String sql;

  private SqlTokenUtil tokenizer;

  public SQlCompiler(String sql) {
    this.sql = sql;
    tokenizer = new SqlTokenUtil(new SqlTokenizerImpl(sql));
  }

  public SQlCompiler(SqlTokenUtil tokenizer) {
    this.tokenizer = tokenizer;
  }

  protected SqlTokenItem mustTokenNext(SqlTokenUtil tokenizer)
      throws SqlSyntaxError {
    if (!tokenizer.hasMoreTokens())
//      throw new SqlSyntaxError("SQL语句不合法!");
    	 throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.illegalsql", "SQL语句不合法!"));
    SqlTokenItem token = tokenizer.nextToken();
    return token;
  }

  protected SelectExpression tokenSelect(SelectStateMent selectStateMent)
      throws SqlSyntaxError {
    //后面可以接上where或者union
    if (selectStateMent.select != null)
//      throw new SqlSyntaxError("select 语句已存在!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.selectexist", "select 语句已存在!"));
    SqlTokenItem token = mustTokenNext(tokenizer);
    SelectExpression select = new SelectExpression();
    selectStateMent.select = select;
    if (token.isItem(SqlTokenizer.TAG_DISTINCT)) {
      //select DISTINCT ...
      select.distinct = true;
      token = mustTokenNext(tokenizer);
    }
    if (token.isItem(SqlTokenizer.TAG_STAR)) {
      //select * from
      select.allFields = true;
      token = mustTokenNext(tokenizer);
      tokenNext(selectStateMent, token);
      return select;
    }

    //字段列表
    while (token != null) {
      if (token.isKey()) {
        if (select.isEmpty())
//          throw new SqlSyntaxError("Select 之后不可识别的关键字:" + token.toString());
        	 throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.unknownkey", "Select 之后不可识别的关键字:{0}", new Object[]{token.toString()}));
        tokenNext(selectStateMent, token);
        return select;
      }
      //字段
      try {
        FieldExpression field = new FieldExpression();
        try {
          tokenField(field, token, tokenizer);

        }
        finally {
          select.add(field);
        }
        token = tokenizer.nextToken();
      }
      catch (UnKownTokenItem e) {
        token = e.getToken();
      }
    }
    return select;
  }

  /**扫描到遇到右括号为止,找不到抛出异常
   * @return
   * @throws SqlSyntaxError
   */
  private SqlTokenItem[] tokenUntilQuoted(SqlTokenUtil tokenizer)
      throws SqlSyntaxError {
    SqlTokenItem[] tokens;
    try {
      tokens = tokenizer.tokenUntilQuoted();
    }
    catch (TokenNotFound e) {
//      throw new SqlSyntaxError("左右括号不匹配!");
    	 throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.unmatchrlkh", "左右括号不匹配!"));
    }
    return tokens;
  }

  /**解析一个字段表达式,遇到逗号就返回,遇到左括号判断是否是函数或者子查询
   *   遇到关键字抛出UnkownNextTokenField异常
   * @param token
   * @param tokenizer
   * @param allowAlias
   * @return
   * @throws SqlSyntaxError
   */
  protected void tokenField(FieldExpression field, SqlTokenItem token,
      SqlTokenUtil tokenizer) throws SqlSyntaxError, UnKownTokenItem {
    try {
      tokenExp(field, token, tokenizer);
    }
    catch (UnKownTokenItem e) {
      token = e.getToken();
      if (token.isItem(SqlTokenizer.TAG_AS)) {
        //as 表达式,后面跟的是别名
        token = mustTokenNext(tokenizer);
        if (!token.isVar())
//          throw new SqlSyntaxError("字段别名定义不合法!");
        	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.illegalalias", "字段别名定义不合法!"));
        field.setAlias(token.getItem());
        if (tokenizer.hasMoreTokens()) {
          token = tokenizer.nextToken();
          if (token.isItem(SqlTokenizer.TAG_COMAR))
            return;
          else if (token.isKey())
            throw new UnKownTokenItem(token);
        }
      }
      else
        throw e;
    }
  }

  protected void tokenExp(SqlCommonExpression exp, SqlTokenItem token,
      SqlTokenUtil tokenizer) throws SqlSyntaxError, UnKownTokenItem {
    tokenExp(exp, token, tokenizer, null);
  }

  protected InExpression tokenIn(SqlTokenUtil tokenizer) throws SqlSyntaxError {
    InExpression in = new InExpression();
    while (tokenizer.hasMoreTokens()) {
      SqlTokenItem token = tokenizer.nextToken();
      SqlCommonExpression exp = new SqlCommonExpression();
      try {
        tokenExp(exp, token, tokenizer);
      }
      catch (UnKownTokenItem e) {
//        throw new SqlSyntaxError("In表达式,括号内不可识别的标示符:" + e.getToken().getItem());
    	  throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.unknownid", "In表达式,括号内不可识别的标示符:{0}",new Object[]{e.getToken().getItem()}));
      }
      in.add(exp);
    }
    return in;
  }

  /**解析表达式,可以为如下形式
   * 1.(字段列表) c3,c4,(b0.c3+b0.c4)*0.17  
   * 3.(子查询)(c3>=100) and c4>90 and id in (select id from xxb where bbq='200512--')
   * 3.(函数,函数嵌套也可以) sum(c3)+func(count(...))
   * 4.以上3种基本形式的组合
   * 
   *遇到左括号判断是否是函数或者子查询,分别解析为函数和子查询或者子句
   * 遇到关键字且不在allowedKeys之中的关键字抛出UnKownTokenItem异常
   * 遇到逗号抛出UnKownTokenItem异常
   * 遇到常量，变量,运算符号(逗号除外)统统记录到exp表达式中
   * 
   * @param exp
   * @param token
   * @param tokenizer
   * @throws SqlSyntaxError 
   */
  protected void tokenExp(SqlCommonExpression exp, SqlTokenItem token,
      SqlTokenUtil tokenizer, HashMap allowedKeys) throws SqlSyntaxError,
      UnKownTokenItem {
    SqlTokenItem last = null;
    while (token != null) {
      if (token.isItem(SqlTokenizer.TAG_LEFTQ)) {
        //左括号
        SqlTokenItem tokens[] = tokenUntilQuoted(tokenizer);
        SqlTokenUtil util = new SqlTokenUtil(new SqlTokenizerImplArray(tokens));
        boolean isFunc = (last != null) && (last.isVar());
        if (isFunc) {
          //是函数
          exp.pop();
          exp.addItem(tokenFunc(last, util));
        }
        else {
          //是子查询
          boolean isSubQuery = (tokens != null) && (tokens.length > 1)
              && (tokens[0].isItem(SqlTokenizer.TAG_SELECT));
          if (isSubQuery)
            exp.addItem(tokenSubQuery(util));
          else if ((last != null) && last.isItem(SqlTokenizer.TAG_IN)) {
            //in 表达式 in(v1,v2,v3,v4,v5,v6),必须是常量表达式
            exp.pop();
            exp.addItem(tokenIn(util));
          }
          else {
            //普通括号表达式
            if (util.hasMoreTokens()) {   
              SqlCommonExpression sub = new SqlCommonExpression();
              sub.quoted = true;
              try {
                tokenExp(sub, util.nextToken(), util, allowedKeys);
              }
              catch (UnKownTokenItem e) {
//                throw new SqlSyntaxError("括号内不可识别的标示符:"
//                    + e.getToken().getItem());
            	  throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.unknownid2", "括号内不可识别的标示符:{0}",new Object[]{e.getToken().getItem()}));
              }
              exp.addItem(sub);
            }
          }
        }
      }
      else if (token.isKey()) {
        //遇到关键字
        if ((allowedKeys != null) && (allowedKeys.get(token.getItem()) != null)) {
          exp.addItem(token);
        }
        else
          throw new UnKownTokenItem(token);
      }
      else if (token.isItem(SqlTokenizer.TAG_COMAR)) {
        //逗号
        return;
        //throw new UnKownTokenItem(token);
      }
      else
        exp.addItem(token);
      last = token;
      token = tokenizer.nextToken();
    }
  }

  /**解析函数
   * @param fun
   * @param tokenizer
   * @return
   * @throws SqlSyntaxError
   */
  protected SqlFunc tokenFunc(SqlTokenItem fun, SqlTokenUtil tokenizer)
      throws SqlSyntaxError {
    SqlFunc func = new SqlFunc(fun.getItem());
    while (tokenizer.hasMoreTokens()) {
      try {
        SqlCommonExpression param = new SqlCommonExpression();
        try {
          tokenExp(param, tokenizer.nextToken(), tokenizer);
        }
        finally {
          func.addParam(param);
        }
      }
      catch (UnKownTokenItem e) {
//        throw new SqlSyntaxError("函数定义非法!");
    	  throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.illegalfunc", "函数定义非法!"));
      }
    }
    return func;
  }

  /**解析子查询
   * @param tokenizer
   * @return
   * @throws SqlSyntaxError
   */
  protected SelectStateMent tokenSubQuery(SqlTokenUtil tokenizer)
      throws SqlSyntaxError {
    SQlCompiler compiler = new SQlCompiler(tokenizer);
    SelectStateMent select = compiler.compile();
    select.quoted = true;
    return select;
  }

  private static final HashMap mainkeys = new HashMap();
  static {
    mainkeys.put(SqlTokenizer.TAG_SELECT, SqlTokenizer.TAG_SELECT);
    mainkeys.put(SqlTokenizer.TAG_FROM, SqlTokenizer.TAG_FROM);
    mainkeys.put(SqlTokenizer.TAG_WHERE, SqlTokenizer.TAG_WHERE);
    mainkeys.put(SqlTokenizer.TAG_ORDER, SqlTokenizer.TAG_ORDER);
    mainkeys.put(SqlTokenizer.TAG_GROUP, SqlTokenizer.TAG_GROUP);
    mainkeys.put(SqlTokenizer.TAG_HAVING, SqlTokenizer.TAG_HAVING);
    mainkeys.put(SqlTokenizer.TAG_UNION, SqlTokenizer.TAG_UNION);
    mainkeys.put(SqlTokenizer.TAG_MINUSSECT, SqlTokenizer.TAG_MINUSSECT);
    mainkeys.put(SqlTokenizer.TAG_INTERSECT, SqlTokenizer.TAG_INTERSECT);
    mainkeys.put(SqlTokenizer.TAG_INNER, SqlTokenizer.TAG_INNER);
    mainkeys.put(SqlTokenizer.TAG_LEFT, SqlTokenizer.TAG_LEFT);
    mainkeys.put(SqlTokenizer.TAG_RIGHT, SqlTokenizer.TAG_RIGHT);

  }

  protected boolean isMainKey(SqlTokenItem token) {
    return mainkeys.get(token.getItem()) != null;
  }

  private static final HashMap joineys = new HashMap();
  static {
    joineys.put(SqlTokenizer.TAG_INNER, SqlTokenizer.TAG_INNER);
    joineys.put(SqlTokenizer.TAG_LEFT, SqlTokenizer.TAG_LEFT);
    joineys.put(SqlTokenizer.TAG_RIGHT, SqlTokenizer.TAG_RIGHT);
  }

  protected boolean isJoinKey(SqlTokenItem token) {
    return joineys.get(token.getItem()) != null;
  }

  protected void tokenNext(SelectStateMent selectStateMent, SqlTokenItem token)
      throws SqlSyntaxError {
    if (token.isItem(SqlTokenizer.TAG_SELECT))
      selectStateMent.select = tokenSelect(selectStateMent);
    else if (token.isItem(SqlTokenizer.TAG_FROM))
      selectStateMent.from = tokenFrom(selectStateMent);
    else if (token.isItem(SqlTokenizer.TAG_WHERE))
      selectStateMent.where = tokenWhere(selectStateMent);
    else if (token.isItem(SqlTokenizer.TAG_ORDER))
      selectStateMent.orderBy = tokenOrderBy(selectStateMent);
    else if (token.isItem(SqlTokenizer.TAG_GROUP))
      selectStateMent.groupBy = tokenGroupBy(selectStateMent);
    else if (token.isItem(SqlTokenizer.TAG_HAVING))
      selectStateMent.having = tokenHaving(selectStateMent);
    else if (token.isItem(SqlTokenizer.TAG_UNION))
      selectStateMent.addUnion(tokenUnion(selectStateMent));
    else if (token.isItem(SqlTokenizer.TAG_INTERSECT))
      selectStateMent.nextInterSelect = tokenIntersect(selectStateMent);
    else if (token.isItem(SqlTokenizer.TAG_MINUSSECT))
      selectStateMent.nextMinus = tokenMinussect(selectStateMent);
    else if (isJoinKey(token))
      selectStateMent.join = tokenJoin(selectStateMent, token);
    else
//      throw new SqlSyntaxError("SQL语法错误,不可识别的标示符号:" + token.getItem());
    	 throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.wrongsql", "SQL语法错误,不可识别的标示符号:{0}",new Object[]{token.getItem()}));
  }

  protected void tokenTable(TableStateMent table, SqlTokenItem token,
      SqlTokenUtil tokenizer) throws SqlSyntaxError, UnKownTokenItem {
    if (token.isItem(SqlTokenizer.TAG_LEFTQ)) {
      //左括号
      SqlTokenItem tokens[] = tokenUntilQuoted(tokenizer);
      boolean isSubQuery = (tokens != null) && (tokens.length > 1)
          && (tokens[0].isItem(SqlTokenizer.TAG_SELECT));
      if (isSubQuery) {
        //子查询
        SqlTokenUtil util = new SqlTokenUtil(new SqlTokenizerImplArray(tokens));
        table.setQuery(tokenSubQuery(util));
        //子查询后不一定带上别名
        //token = mustTokenNext(tokenizer);
        token = tokenizer.nextToken();
        if (token != null) {
          if (token.isVar())
            table.setAlias(token.getItem());
          else
            //throw new SqlSyntaxError("子查询表达式必须要有别名!");
            throw new UnKownTokenItem(token);
        }
        return;
      }
      else {
        //简单表达式
        if (tokens.length == 1)
          table = new TableStateMent(tokens[0]);
        else if (tokens.length == 2) {
          //简单别名表达式
          table = new TableStateMent(tokens[0]);
          table.setAlias(tokens[1]);
        }
        else
          //超过二个参数的不合法
//          throw new SqlSyntaxError("SQL语法错误,from中报表表达式定义不合法!");
        	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.wrongfrom", "SQL语法错误,from中报表表达式定义不合法!"));
      }
    }
    else
      table.setTableName(token.getItem());
    while (tokenizer.hasMoreTokens()) {
      token = tokenizer.nextToken();
      //遇到逗号,表示一个表定义完毕
      if (token.isItem(SqlTokenizer.TAG_COMAR))
        return;
      if (token.isVar()) {
        //别名
        if (table.getAlias() != null)
//          throw new SqlSyntaxError("table 的别名定义重复!");
        	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.wrongtableali", "table 的别名定义重复!"));
        table.setAlias(token);
        //继续探索到逗号为止
      }
      else if (token.isKey())
        throw new UnKownTokenItem(token);
    }
  }

  /**INNER (LEFt,RIGHT) JOIN Orders ON xxx..
   * @param selectStateMent
   * @param token
   * @return
   * @throws SqlSyntaxError 
   */
  protected JoinExpression tokenJoin(SelectStateMent selectStateMent,
      SqlTokenItem token) throws SqlSyntaxError {
    if (selectStateMent.join != null)
//      throw new SqlSyntaxError("join表达式已存在!");
    	 throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.joinexist", "join表达式已存在!"));
    if (selectStateMent.select == null)
//      throw new SqlSyntaxError("join表达式不合法,找不到Select 表达式!!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.noselect4join", "join表达式不合法,找不到Select 表达式!!"));
    if (selectStateMent.from == null)
//      throw new SqlSyntaxError("join表达式不合法,找不到FROM 表达式!!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.nofrom4join", "join表达式不合法,找不到FROM 表达式!!"));
    JoinExpression join = new JoinExpression(token);
    selectStateMent.join = join;
    tokenizer.nextTokenMust(SqlTokenizer.TAG_JOIN);
    //连接的表名
    token = mustTokenNext(tokenizer);
    join.setTable(token);
    if (tokenizer.hasMoreTokens()) {
      token = tokenizer.nextToken();
      if (token.isItem(SqlTokenizer.TAG_ON)) {
        SqlCommonExpression exp = new SqlCommonExpression();
        try {
          try {
            tokenExp(exp, tokenizer.nextToken(), tokenizer);
          }
          finally {
            join.setExp(exp);
          }
        }
        catch (UnKownTokenItem e) {
          tokenNext(selectStateMent, e.getToken());
        }
      }
      else
        tokenNext(selectStateMent, token);
    }
    return join;
  }

  /**1.from xxb,b1
   * 2.inner join ,left join,right join
   * 
   * @param selectStateMent
   * @return
   * @throws SqlSyntaxError
   */
  protected FromExpression tokenFrom(SelectStateMent selectStateMent)
      throws SqlSyntaxError {
    if ((selectStateMent.select == null) || (selectStateMent.select.isEmpty()))
//      throw new SqlSyntaxError("SQL语法错误,from找不到select!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.noselect4from", "SQL语法错误,from找不到select!"));
    SqlTokenItem token = mustTokenNext(tokenizer);
    FromExpression from = new FromExpression();
    selectStateMent.from = from;
    while (token != null) {
      if (token.isKey()) {
        if (isMainKey(token))
          this.tokenNext(selectStateMent, token);
        else
//          throw new SqlSyntaxError("FROM表达式不合法不可识别的关键字:" + token.getItem());
        	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.errkey4from", "FROM表达式不合法不可识别的关键字:{0}",new Object[]{token.getItem()}));
        return from;
      }
      TableStateMent table = new TableStateMent();
      try {
        try {
          tokenTable(table, token, tokenizer);
          token = tokenizer.nextToken();
        }
        finally {
          table.validate();
          from.add(table);
        }
      }
      catch (UnKownTokenItem e) {
        token = e.getToken();
      }
    }
    if (from.isEmpty())
//      throw new SqlSyntaxError("SQL语法错误,FROM中没有表!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.notable4from", "SQL语法错误,FROM中没有表!"));
    return from;
  }

  private static final HashMap expKeys = new HashMap();
  static {
    expKeys.put(SqlTokenizer.TAG_BETWEEN, SqlTokenizer.TAG_BETWEEN);
    expKeys.put(SqlTokenizer.TAG_AND, SqlTokenizer.TAG_AND);
    expKeys.put(SqlTokenizer.TAG_OR, SqlTokenizer.TAG_OR);
    expKeys.put(SqlTokenizer.TAG_NOT, SqlTokenizer.TAG_NOT);
    expKeys.put(SqlTokenizer.TAG_LIKE, SqlTokenizer.TAG_LIKE);
    expKeys.put(SqlTokenizer.TAG_IN, SqlTokenizer.TAG_IN);
  }

  protected WhereExpression tokenWhere(SelectStateMent selectStateMent)
      throws SqlSyntaxError {
    if ((selectStateMent.select == null) || (selectStateMent.select.isEmpty()))
//      throw new SqlSyntaxError("SQL语法错误,WHERE找不到SELECT!");
    	 throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.nowehere4select", "SQL语法错误,WHERE找不到SELECT!"));
    if ((selectStateMent.from == null) || (selectStateMent.from.isEmpty()))
//      throw new SqlSyntaxError("SQL语法错误,WHERE找不到FROM!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.nofrom4where", "SQL语法错误,WHERE找不到FROM!"));
    if (selectStateMent.where != null)
//      throw new SqlSyntaxError("WHERE重复定义!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.multiwehere", "WHERE重复定义!"));
    SqlTokenItem token = mustTokenNext(tokenizer);
    WhereExpression where = new WhereExpression();
    selectStateMent.where = where;
    try {
      tokenExp(where, token, tokenizer, expKeys);
    }
    catch (UnKownTokenItem e) {
      token = e.getToken();
      if (isMainKey(token)) {
        tokenNext(selectStateMent, token);
      }
      else
//        throw new SqlSyntaxError("WHERE表达式中不可识别的标示符:" + token.getItem());
    	  throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.unknownwhere", "WHERE表达式中不可识别的标示符:{0}", new Object[]{token.getItem()}));
    }
    return where;
  }

  private void tokenSortedField(SortedFieldExpression field,
      SqlTokenItem token, SqlTokenUtil tokenizer) throws SqlSyntaxError,
      UnKownTokenItem {
    try {
      tokenExp(field, token, tokenizer);
    }
    catch (UnKownTokenItem e) {
      token = e.getToken();
      boolean asc = token.isItem(SqlTokenizer.TAG_ASC);
      boolean desc = token.isItem(SqlTokenizer.TAG_DESC);
      if (asc || desc) {
        field.setDesc(desc);
        if (tokenizer.hasMoreTokens()) {
          token = tokenizer.nextToken();
          if (token.isItem(SqlTokenizer.TAG_COMAR))
            return;
          else if (token.isKey())
            throw new UnKownTokenItem(token);
          else
//            throw new SqlSyntaxError("不可识别的标示符:" + token.getItem());
        	  throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.unknownide", "不可识别的标示符:{0}",new Object[]{token.getItem()}));
        }
      }
      else
        throw e;
    }
  }

  protected OrderByExpression tokenOrderBy(SelectStateMent selectStateMent)
      throws SqlSyntaxError {
    if ((selectStateMent.select == null) || (selectStateMent.select.isEmpty()))
//      throw new SqlSyntaxError("SQL语法错误,ORDERBY找不到SELECT!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.noselect4orderby", "SQL语法错误,ORDERBY找不到SELECT!"));
    SelectExpression select = selectStateMent.select;
    if (selectStateMent.orderBy != null)
//      throw new SqlSyntaxError("order by 重复定义!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.multiorder", "order by 重复定义!"));
    tokenizer.nextTokenMust(SqlTokenizer.TAG_BY);
    SqlTokenItem token = this.mustTokenNext(tokenizer);
    OrderByExpression orderBy = new OrderByExpression();
    selectStateMent.orderBy = orderBy;
    while (token != null) {
      if (token.isKey()) {
        tokenNext(selectStateMent, token);
        break;
      }
      SortedFieldExpression field = new SortedFieldExpression();
      try {
        try {
          tokenSortedField(field, token, tokenizer);
        }
        finally {
          orderBy.add(field);
        }
        token = tokenizer.nextToken();
      }
      catch (UnKownTokenItem e) {
        token = e.getToken();
      }
    }
    if (orderBy.isEmpty())
//      throw new SqlSyntaxError("order by 中没有可用字段!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.norargs", "order by 中没有可用字段!"));
    return orderBy;
  }

  /**
   * @param selectStateMent
   * @return
   * @throws SqlSyntaxError
   */
  protected GroupByExpression tokenGroupBy(SelectStateMent selectStateMent)
      throws SqlSyntaxError {
    if ((selectStateMent.select == null) || (selectStateMent.select.isEmpty()))
//      throw new SqlSyntaxError("SQL语法错误,GROUP 找不到SELECT!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.noselect4group", "SQL语法错误,GROUP 找不到SELECT!"));
    SelectExpression select = selectStateMent.select;
    if (selectStateMent.groupBy != null)
//      throw new SqlSyntaxError("GROUP BY 重复定义!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.multigroup", "GROUP BY 重复定义!"));
    tokenizer.nextTokenMust(SqlTokenizer.TAG_BY);
    GroupByExpression groupBy = new GroupByExpression();
    selectStateMent.groupBy = groupBy;
    //group by 一定是select 中的表达式或者别名
    while (tokenizer.hasMoreTokens()) {
      SqlTokenItem token = tokenizer.nextToken();
      FieldExpression field = new FieldExpression();
      try {
        tokenExp(field, token, tokenizer);
      }
      catch (UnKownTokenItem e) {
        if (tokenizer.hasMoreTokens()) {
          token = tokenizer.nextToken();
          if (token.isKey()) {
            tokenNext(selectStateMent, token);
            break;
          }
          else if (!token.isItem(SqlTokenizer.TAG_COMAR))
//            throw new SqlSyntaxError("GROUP BY 解析不合法,不可识别的标示符:"
//                + token.getItem());
        	  throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.illegalgroup", "GROUP BY 解析不合法,不可识别的标示符:{0}", new Object[]{token.getItem()}));
        }
      }
      finally {
        groupBy.add(field);
      }
      /*      FieldExpression field = select.find(token.getItem());
       if (field == null)
       throw new SqlSyntaxError("group by 中不可识别的字段:" + token.getItem());
       */

    }
    return groupBy;
  }

  protected HavingExpression tokenHaving(SelectStateMent selectStateMent)
      throws SqlSyntaxError {
    if ((selectStateMent.select == null) || (selectStateMent.select.isEmpty()))
//      throw new SqlSyntaxError("SQL语法错误,HAVING找不到SELECT!");
    	 throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.noselect4having", "SQL语法错误,HAVING找不到SELECT!"));
    if ((selectStateMent.from == null) || (selectStateMent.from.isEmpty()))
//      throw new SqlSyntaxError("SQL语法错误,HAVING找不到FROM!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.nofrom4having", "SQL语法错误,HAVING找不到FROM!"));
    if (selectStateMent.having != null)
//      throw new SqlSyntaxError("HAVING重复定义!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.multihaving", "HAVING重复定义!"));
    SqlTokenItem token = mustTokenNext(tokenizer);
    HavingExpression having = new HavingExpression();
    selectStateMent.having = having;
    try {
      tokenExp(having, token, tokenizer, expKeys);
    }
    catch (UnKownTokenItem e) {
      token = e.getToken();
      if (isMainKey(token)) {
        tokenNext(selectStateMent, token);
      }
      else
//        throw new SqlSyntaxError("WHERE表达式中不可识别的标示符:" + token.getItem());
    	  throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.unknownid4having", "WHERE表达式中不可识别的标示符:{0}",new Object[]{token.getItem()}));
    }
    return having;
  }

  protected UnionExpression tokenUnion(SelectStateMent selectStateMent)
      throws SqlSyntaxError {
    if ((selectStateMent.select == null) || (selectStateMent.select.isEmpty()))
//      throw new SqlSyntaxError("SQL语法错误,UNION找不到SELECT!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.noselect4union", "SQL语法错误,UNION找不到SELECT!"));
    UnionExpression union = new UnionExpression();
    //支持union All ...
    if (tokenizer.nextTokenIs(SqlTokenizer.TAG_ALL)) {
      union.setAll(true);
      this.mustTokenNext(tokenizer);
    }
    SelectStateMent select = tokenSubQuery(tokenizer);
    select.quoted = false;
    union.setSelect(select);
    return union;
  }

  protected SelectStateMent tokenMinussect(SelectStateMent selectStateMent)
      throws SqlSyntaxError {
    if ((selectStateMent.select == null) || (selectStateMent.select.isEmpty()))
//      throw new SqlSyntaxError("SQL语法错误,MINUS找不到SELECT!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.noselect4minus", "SQL语法错误,MINUS找不到SELECT!"));
    return tokenSubQuery(tokenizer);
  }

  protected SelectStateMent tokenIntersect(SelectStateMent selectStateMent)
      throws SqlSyntaxError {
    if ((selectStateMent.select == null) || (selectStateMent.select.isEmpty()))
//      throw new SqlSyntaxError("SQL语法错误,INTERSECT找不到SELECT!");
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.noselect4intersect", "SQL语法错误,INTERSECT找不到SELECT!"));
    return tokenSubQuery(tokenizer);
  }

  public SelectStateMent compile() throws SqlSyntaxError {
    if (!tokenizer.hasMoreTokens())
//      throw new SqlSyntaxError("不是合法的Sql语句:" + sql);
    	 throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.illsqlargs", "不是合法的Sql语句:{0}", new Object[]{sql}));
    //必须是select语句 
    SelectStateMent select;
    SqlTokenItem token = tokenizer.nextToken();
    if (token.isItem(SqlTokenizer.TAG_LEFTQ)) {
      SqlTokenItem tokens[] = tokenUntilQuoted(tokenizer);
      if (tokenizer.hasMoreTokens())
//        throw new SqlSyntaxError("SQL语句没有正常结束!");
    	  throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.wrongend4sql", "SQL语句没有正常结束!"));
      SqlTokenUtil util = new SqlTokenUtil(new SqlTokenizerImplArray(tokens));
      select = tokenSubQuery(util);
      select.quoted = true;
      return select;
    }
    if (!token.isItem(SqlTokenizer.TAG_SELECT))
      //throw new SqlSyntaxError("不是合法的查询语句:" + sql);
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.illsqlargs", "不是合法的Sql语句:{0}", new Object[]{sql}));
    select = new SelectStateMent();
    select.select = tokenSelect(select);
    if (tokenizer.hasMoreTokens())
      //throw new SqlSyntaxError("SQL语句没有正常结束!");
    	 throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.wrongend4sql", "SQL语句没有正常结束!"));
    if (select.getFrom() == null)
//      throw new SqlSyntaxError("SELECT语句没有正常结束,找不到From 表达式!");
    	 throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.nofrom4select", "SELECT语句没有正常结束,找不到From 表达式!"));
    return select;
  }

  public SqlCommonExpression compileExp(SqlCommonExpression exp)
      throws SqlSyntaxError {
    if (!tokenizer.hasMoreTokens())
      //throw new SqlSyntaxError("不是合法的Sql语句:" + sql);
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.illsqlargs", "不是合法的Sql语句:{0}", new Object[]{sql}));
    SqlTokenItem token = mustTokenNext(tokenizer);
    try {
      tokenExp(exp, token, tokenizer, expKeys);
    }
    catch (UnKownTokenItem e) {
//      throw new SqlSyntaxError("WHERE表达式中不可识别的标示符:" + e.getToken().getItem());
    	throw new SqlSyntaxError(I18N.getString("com.esen.jdbc.sql.parser.sqlcompiler.unknownwhere", "WHERE表达式中不可识别的标示符:{0}", new Object[]{e.getToken().getItem()}));
    }
    return exp;
  }

  public void mergeCondition(SqlConditionExpression con) throws SqlSyntaxError {
    SqlConditionExpression exp = (SqlConditionExpression) compileExp(new SqlConditionExpression());
    con.mergeCondition(exp, SqlTokenItem.getKeyObj(SqlTokenizer.TAG_AND));
  }
}
