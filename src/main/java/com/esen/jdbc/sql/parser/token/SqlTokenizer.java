package com.esen.jdbc.sql.parser.token;

public interface SqlTokenizer {

  public static final String TAG_AS = "AS";

  public static final String TAG_ASC = "ASC";

  public static final String TAG_DESC = "DESC";

  public static final String TAG_SELECT = "SELECT";

  public static final String TAG_FROM = "FROM";

  public static final String TAG_WHERE = "WHERE";

  public static final String TAG_HAVING = "HAVINF";

  public static final String TAG_ORDER = "ORDER";

  public static final String TAG_GROUP = "GROUP";

  public static final String TAG_GROUPBY = "GROUP BY";

  public static final String TAG_BY = "BY";

  public static final String TAG_ORDERBY = "ORDER BY";

  public static final String TAG_UNION = "UNION";

  public static final String TAG_INTERSECT = "INTERSECT";

  public static final String TAG_MINUSSECT = "MINUS";

  public static final String TAG_BETWEEN = "BETWEEN";

  public static final String TAG_AND = "AND";

  public static final String TAG_ALL = "ALL";

  public static final String TAG_OR = "OR";

  public static final String TAG_NOT = "NOT";

  public static final String TAG_LIKE = "LIKE";

  public static final String TAG_IN = "IN";

  public static final String TAG_TRUE = "TRUE";

  public static final String TAG_FALSE = "FALSE";

  public static final String TAG_DISTINCT = "DISTINCT";

  public static final String TAG_INNER = "INNER";

  public static final String TAG_LEFT = "LEFT";

  public static final String TAG_RIGHT = "RIGHT";

  public static final String TAG_JOIN = "JOIN";

  public static final String TAG_ON = "ON";

  public static final String TAG_OUTER = "OUTER";

  public static final String TAG_FULL = "FULL";

  public static final String TAG_LESS = "<";

  public static final String TAG_NOTLESS = ">=";

  public static final String TAG_CREATE = ">";

  public static final String TAG_NOTGREATE = "<=";

  public static final String TAG_EQUAL = "=";

  public static final String TAG_NOTEQUAL = "<>";

  public static final String TAG_PLUS = "+";

  public static final String TAG_MINUS = "-";

  public static final String TAG_MULTI = "*";

  public static final String TAG_STAR = "*";

  public static final String TAG_DIV = "/";

  public static final String TAG_LEFTQ = "(";

  public static final String TAG_RIGHTQ = ")";

  public static final String TAG_COMAR = ",";

  public static final String TAG_STR = "'";

  public static final String TAG_CRLF = "\r\n";

  public static final String TAG_BLANK = " ";

  public abstract boolean hasMoreTokens();

  public abstract SqlTokenItem nextToken() throws SqlSyntaxError;

  public boolean nextTokenIs(String item);
}