package org.codeyn.util.yn;

import org.apache.tomcat.dbcp.dbcp.ConnectionFactory;
import org.codeyn.util.DoubleArray;
import org.codeyn.util.MiniProperties;
import org.codeyn.util.exception.RuntimeException4I18N;
import org.codeyn.util.i18n.I18N;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svip.db.definer.DbDefiner;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 此类定义一些关于JDBC数据库的通用的函数
 */

public class SqlYn {

    public static final String[] KEYWORDS = {"ABORT", "ABS", "ABSOLUTE",
            "ACCESS", "ACTION", "ADA", "ADD", "ADMIN", "AFTER", "AGGREGATE",
            "ALIAS", "ALL", "ALLOCATE", "ALTER", "ANALYSE", "ANALYZE", "AND",
            "ANY", "ARE", "ARRAY", "AS", "ASC", "ASENSITIVE", "ASSERTION",
            "ASSIGNMENT", "ASYMMETRIC", "AT", "ATOMIC", "AUTHORIZATION", "AVG",
            "BACKUP", "BACKWARD", "BEFORE", "BEGIN", "BETWEEN", "BINARY",
            "BIT", "BITVAR", "BIT_LENGTH", "BLOB", "BOOLEAN", "BOTH",
            "BREADTH", "BREAK", "BROWSE", "BULK", "BY", "C", "CACHE", "CALL",
            "CALLED", "CARDINALITY", "CASCADE", "CASCADED", "CASE", "CAST",
            "CATALOG", "CATALOG_NAME", "CHAIN", "CHAR", "CHARACTER",
            "CHARACTERISTICS", "CHARACTER_LENGTH", "CHARACTER_SET_CATALOG",
            "CHARACTER_SET_NAME", "CHARACTER_SET_SCHEMA", "CHAR_LENGTH",
            "CHECK", "CHECKED", "CHECKPOINT", "CLASS", "CLASS_ORIGIN", "CLOB",
            "CLOSE", "CLUSTER", "CLUSTERED", "COALESCE", "COBOL", "COLLATE",
            "COLLATION", "COLLATION_CATALOG", "COLLATION_NAME",
            "COLLATION_SCHEMA", "COLUMN", "COLUMN_NAME", "COMMAND_FUNCTION",
            "COMMAND_FUNCTION_CODE", "COMMENT", "COMMIT", "COMMITTED",
            "COMPLETION", "COMPUTE", "CONDITION_NUMBER", "CONFIRM", "CONNECT",
            "CONNECTION", "CONNECTION_NAME", "CONSTRAINT", "CONSTRAINTS",
            "CONSTRAINT_CATALOG", "CONSTRAINT_NAME", "CONSTRAINT_SCHEMA",
            "CONSTRUCTOR", "CONTAINS", "CONTAINSTABLE", "CONTINUE",
            "CONTROLROW", "CONVERT", "COPY", "CORRESPONDING", "COUNT",
            "CREATE", "CREATEDB", "CREATEUSER", "CROSS", "CUBE", "CURRENT",
            "CURRENT_DATE", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_TIME",
            "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "CURSOR_NAME",
            "CYCLE", "DATA", "DATABASE", "DATE", "DATETIME_INTERVAL_CODE",
            "DATETIME_INTERVAL_PRECISION", "DAY", "DBCC", "DEALLOCATE", "DEC",
            "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED",
            "DEFINED", "DEFINER", "DELETE", "DELIMITERS", "DENY", "DEPTH",
            "DEREF", "DESC", "DESCRIBE", "DESCRIPTOR", "DESTROY", "DESTRUCTOR",
            "DETERMINISTIC", "DIAGNOSTICS", "DICTIONARY", "DISCONNECT", "DISK",
            "DISPATCH", "DISTINCT", "DISTRIBUTED", "DO", "DOMAIN", "DOUBLE",
            "DROP", "DUMMY", "DUMP", "DYNAMIC", "DYNAMIC_FUNCTION",
            "DYNAMIC_FUNCTION_CODE", "EACH", "ELSE", "ENABLED", "ENCODING",
            "ENCRYPTED", "END", "END-EXEC", "EQUALS", "ERRLVL", "ERROREXIT",
            "ESCAPE", "EVERY", "EXCEPT", "EXCEPTION", "EXCLUSIVE", "EXEC",
            "EXECUTE", "EXISTING", "EXISTS", "EXIT", "EXPLAIN", "EXTERNAL",
            "EXTRACT", "FALSE", "FETCH", "FILE", "FILLFACTOR", "FINAL",
            "FIRST", "FLOAT", "FLOPPY", "FOR", "FORCE", "FOREIGN", "FORTRAN",
            "FORWARD", "FOUND", "FREE", "FREETEXT", "FREETEXTTABLE", "FREEZE",
            "FROM", "FULL", "FUNCTION", "G", "GENERAL", "GENERATED", "GET",
            "GLOBAL", "GO", "GOTO", "GRANT", "GRANTED", "GROUP", "GROUPING",
            "HANDLER", "HAVING", "HIERARCHY", "HOLD", "HOLDLOCK", "HOST",
            "HOUR", "IDENTITY", "IDENTITYCOL", "IDENTITY_INSERT", "IF",
            "IGNORE", "ILIKE", "IMMEDIATE", "IMPLEMENTATION", "IN",
            "INCREMENT", "INDEX", "INDICATOR", "INFIX", "INHERITS",
            "INITIALIZE", "INITIALLY", "INNER", "INOUT", "INPUT",
            "INSENSITIVE", "INSERT", "INSTANCE", "INSTANTIABLE", "INSTEAD",
            "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "INVOKER", "IS",
            "ISNULL", "ISOLATION", "ITERATE", "JOIN", "K", "KEY", "KEY_MEMBER",
            "KEY_TYPE", "KILL", "LANCOMPILER", "LANGUAGE", "LARGE", "LAST",
            "LATERAL", "LEADING", "LEFT", "LENGTH", "LESS", "LEVEL", "LIKE",
            "LIMIT", "LINENO", "LISTEN", "LOAD", "LOCAL", "LOCALTIME",
            "LOCALTIMESTAMP", "LOCATION", "LOCATOR", "LOCK", "LOWER", "M",
            "MAP", "MATCH", "MAX", "MAXVALUE", "MESSAGE_LENGTH",
            "MESSAGE_OCTET_LENGTH", "MESSAGE_TEXT", "METHOD", "MIN", "MINUTE",
            "MINVALUE", "MIRROREXIT", "MOD", "MODE", "MODIFIES", "MODIFY",
            "MODULE", "MONTH", "MORE", "MOVE", "MUMPS", "NAME", "NAMES",
            "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NEXT", "NO",
            "NOCHECK", "NOCREATEDB", "NOCREATEUSER", "NONCLUSTERED", "NONE",
            "NOT", "NOTHING", "NOTIFY", "NOTNULL", "NULL", "NULLABLE",
            "NULLIF", "NUMBER", "NUMERIC", "OBJECT", "OCTET_LENGTH", "OF",
            "OFF", "OFFSET", "OFFSETS", "OIDS", "OLD", "ON", "ONCE", "ONLY",
            "OPEN", "OPENDATASOURCE", "OPENQUERY", "OPENROWSET", "OPERATION",
            "OPERATOR", "OPTION", "OPTIONS", "OR", "ORDER", "ORDINALITY",
            "OUT", "OUTER", "OUTPUT", "OVER", "OVERLAPS", "OVERLAY",
            "OVERRIDING", "OWNER", "PAD", "PARAMETER", "PARAMETERS",
            "PARAMETER_MODE", "PARAMETER_NAME", "PARAMETER_ORDINAL_POSITION",
            "PARAMETER_SPECIFIC_CATALOG", "PARAMETER_SPECIFIC_NAME",
            "PARAMETER_SPECIFIC_SCHEMA", "PARTIAL", "PASCAL", "PASSWORD",
            "PATH", "PENDANT", "PERCENT", "PERM", "PERMANENT", "PIPE", "PLAN",
            "PLI", "POSITION", "POSTFIX", "PRECISION", "PREFIX", "PREORDER",
            "PREPARE", "PRESERVE", "PRIMARY", "PRINT", "PRIOR", "PRIVILEGES",
            "PROC", "PROCEDURAL", "PROCEDURE", "PROCESSEXIT", "PUBLIC",
            "RAISERROR", "READ", "READS", "READTEXT", "REAL", "RECONFIGURE",
            "RECURSIVE", "REF", "REFERENCES", "REFERENCING", "REINDEX",
            "RELATIVE", "RENAME", "REPEATABLE", "REPLACE", "REPLICATION",
            "RESET", "RESTORE", "RESTRICT", "RESULT", "RETURN",
            "RETURNED_LENGTH", "RETURNED_OCTET_LENGTH", "RETURNED_SQLSTATE",
            "RETURNS", "REVOKE", "RIGHT", "ROLE", "ROLLBACK", "ROLLUP",
            "ROUTINE", "ROUTINE_CATALOG", "ROUTINE_NAME", "ROUTINE_SCHEMA",
            "ROW", "ROWCOUNT", "ROWGUIDCOL", "ROWS", "ROW_COUNT", "RULE",
            "SAVE", "SAVEPOINT", "SCALE", "SCHEMA", "SCHEMA_NAME", "SCOPE",
            "SCROLL", "SEARCH", "SECOND", "SECTION", "SECURITY", "SELECT",
            "SELF", "SENSITIVE", "SEQUENCE", "SERIALIZABLE", "SERVER_NAME",
            "SESSION", "SESSION_USER", "SET", "SETOF", "SETS", "SETUSER",
            "SHARE", "SHOW", "SHUTDOWN", "SIMILAR", "SIMPLE", "SIZE",
            "SMALLINT", "SOME", "SOURCE", "SPACE", "SPECIFIC", "SPECIFICTYPE",
            "SPECIFIC_NAME", "SQL", "SQLCODE", "SQLERROR", "SQLEXCEPTION",
            "SQLSTATE", "SQLWARNING", "START", "STATE", "STATEMENT", "STATIC",
            "STATISTICS", "STDIN", "STDOUT", "STRUCTURE", "STYLE",
            "SUBCLASS_ORIGIN", "SUBLIST", "SUBSTRING", "SUM", "SYMMETRIC",
            "SYSID", "SYSTEM", "SYSTEM_USER", "TABLE", "TABLE_NAME", "TAPE",
            "TEMP", "TEMPLATE", "TEMPORARY", "TERMINATE", "TEXTSIZE", "THAN",
            "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE",
            "TITLE", "TO", "TOAST", "TOP", "TRAILING", "TRAN", "TRANSACTION",
            "TRANSACTIONS_COMMITTED", "TRANSACTIONS_ROLLED_BACK",
            "TRANSACTION_ACTIVE", "TRANSFORM", "TRANSFORMS", "TRANSLATE",
            "TRANSLATION", "TREAT", "TRIGGER", "TRIGGER_CATALOG",
            "TRIGGER_NAME", "TRIGGER_SCHEMA", "TRIM", "TRUE", "TRUNCATE",
            "TRUSTED", "TSEQUAL", "TYPE", "UNCOMMITTED", "UNDER",
            "UNENCRYPTED", "UNION", "UNIQUE", "UNKNOWN", "UNLISTEN", "UNNAMED",
            "UNNEST", "UNTIL", "UPDATE", "UPDATETEXT", "UPPER", "USAGE", "USE",
            "USER", "USER_DEFINED_TYPE_CATALOG", "USER_DEFINED_TYPE_NAME",
            "USER_DEFINED_TYPE_SCHEMA", "USING", "VACUUM", "VALID", "VALUE",
            "VALUES", "VARCHAR", "VARIABLE", "VARYING", "VERBOSE", "VERSION",
            "VIEW", "WAITFOR", "WHEN", "WHENEVER", "WHERE", "WHILE", "WITH",
            "WITHOUT", "WORK", "WRITE", "WRITETEXT", "YEAR", "ZONE"};
    public static final HashMap KEYWORDMAP;
    private static final Logger log = LoggerFactory.getLogger(SqlYn.class);
    // 匹配形如:insert into tbname values(?,?)的sql
    /*
     * private static final Pattern INSERT_ALLFIELD_REGEX = Pattern .compile(
     * "^[\\s]*insert[\\s]+into[\\s]+([^\\s,\\(\\)]+)[\\s]+values[\\s]*\\([\\s]*\\?([\\s]*,[\\s]*\\?)*[\\s]*\\)[\\s]*$"
     * , Pattern.CASE_INSENSITIVE);
     */
    private static final Pattern INSERT_ALLFIELD_REGEX = Pattern
            .compile(
                    "^[\\s]*insert[\\s]+into[\\s]+([^\\s,\\(\\)]+)[\\s]+values[\\s]*\\(",
                    Pattern.CASE_INSENSITIVE);
    // 匹配形如:insert into tbname (field1,field2)values(?,?)的sql
    /*
     * private static final Pattern INSERT_SOMEFIELD_REGEX = Pattern .compile(
     * "^[\\s]*insert[\\s]+into[\\s]+([^\\s,\\(\\)]+)[\\s]*\\([\\s]*([^\\s,]+([\\s]*,[\\s]*[^\\s,]+[\\s]*)*)\\)[\\s]*values[\\s]*\\([\\s]*\\?([\\s]*,[\\s]*\\?)*[\\s]*\\)[\\s]*$"
     * , Pattern.CASE_INSENSITIVE);
     */
    private static final Pattern INSERT_SOMEFIELD_REGEX = Pattern
            .compile(
                    "^[\\s]*insert[\\s]+into[\\s]+([^\\s,\\(\\)]+)[\\s]*\\([\\s]*([^\\(\\)]+)\\)[\\s]*values[\\s]*\\(",
                    Pattern.CASE_INSENSITIVE);
    // 匹配形如:update tbname set ... 的sql
    private static final Pattern UPDATE_SQL_REGEX = Pattern.compile(
            "^[\\s]*update[\\s]+([\\S]+)[\\s]+set([\\s]+[\\S].+)",
            Pattern.CASE_INSENSITIVE);
    /**
     * 在匹配字段时需要注意字段两端有括号,如select Field1 from table where
     * ((field2)=?,field3=?),这里获得字段时了需要从((field2)=?,field3=?)中取出字段field2,field3
     */
    private static final Pattern UPDATE_FIELD = Pattern
            .compile(
                    "[\\s,][\\(\\s]*([^\\s>=<,\\)]+)[\\s\\)]*((([\\s]*(>|=|<|(>=)|(<=)|(<>))[\\s]*)|([\\s]+like[\\s]+)))[\\?]",
                    Pattern.CASE_INSENSITIVE);

    static {
        KEYWORDMAP = new HashMap();
        for (int i = 0; i < KEYWORDS.length; i++) {
            KEYWORDMAP.put(KEYWORDS[i], KEYWORDS[i]);
        }
    }

    public static final boolean isKeyWord(String key) {
        return KEYWORDMAP.containsKey(key.toUpperCase());
    }

    /**
     * 如果字段名是数据库关键字，则用 " 括起来； mysql 使用 `
     *
     * @param cname
     * @return
     */
    public static final String getColumnName(Dialect dl, String cn) {
        if (cn == null) return null;
        if (dl.getDataBaseInfo().getDbtype() == SqlConst.DB_TYPE_GREENPLUM) {
            return cn;
        }
        if (SqlYn.isKeyWord(cn)) {
            DataBaseInfo db = dl.getDataBaseInfo();
            if (db.isMysql()) return "`" + cn + "`";
            return "\"" + cn + "\"";
        }
        return cn;
    }

    /**
     * 返回一个在数据库中不存在的表名，以tableNamePreFix为前缀，tableNamePreFix可以为null
     */
    public static final String generateTableName(DbDefiner db, Connection con,
                                                 String tableNamePreFix) throws Exception {
        if (tableNamePreFix != null) {
            tableNamePreFix = tableNamePreFix.trim();
        }
        if (tableNamePreFix == null || tableNamePreFix.length() == 0) {
            tableNamePreFix = "ES_UNKNOWN_";
        }
        int i = 0;
        String table = tableNamePreFix;
        // 最大表名长度 是有限制的
        int tblen = table.length();
        int maxlen = db.getMaxTableLength();
        if (tblen >= maxlen) {
            /*
             * ISSUE:BI-7806,BI-8315 add by jzp 2013.4.17
             * 表名长度会超过数据库最大长度限制，以下改法为截取部分表名。目前只截掉一位，那么表明不能重复10次以上。
             */
            table = table.substring(0, maxlen - 1);
        }
        while (db.tableExists(con, null, table)) {
            i++;
            table = tableNamePreFix + i;
        }
        return table;
    }

    public static final char getType(int sqlType, boolean throwException) {
        char r = getType(sqlType);
        if (r == 0 && throwException)
            // throw new RuntimeException("不支持的字段类型 " + sqlType);
            throw new RuntimeException(I18N.getString(
                    "com.esen.jdbc.sqlfunc.unabletype", "不支持的字段类型 {0}"));
        return r;
    }

    /**
     * 将表达式变量类型转换成sql类型； 支持字符，数值，日期，CLOB，BLOB类型；
     *
     * @param returnType
     * @return
     */
    public static int expType2SqlType(char returnType) {
        switch (returnType) {
            case ExpUtil.TOSTR:
                return Types.VARCHAR;
            case ExpUtil.TOFLT:
                return Types.FLOAT;
            case ExpUtil.TOINT:
                return Types.INTEGER;
            case ExpUtil.TODAT:
                return Types.DATE;
            /** ISSEU:BI-7732 存在clob字段的表，用csv方式导入数据库报错 */
            case ExpUtil.TOBLOB:
                return Types.BLOB;
            case ExpUtil.TOCLOB:
                return Types.CLOB;
        }
        /**
         * 20090217 原来的程序返回字符类型，当处理形如：nvl(a.field,0)>0.1 时，
         * 会认为nvl(a.field,0)是字符类型，生成sql：nvl(a.field,0)>'0.1'，这是错误的；
         * 原因是nvl是oracle函数，系统识别为变体函数； 所以这里不能返回字符，应该直接返回变体函数类型；
         */
        return returnType;
    }

    public static final char getType(int sqlType) {
        /*
         * TYPE_STR = 'C'; TYPE_FLOAT = 'N'; TYPE_INT = 'I'; TYPE_LOGIC = 'L';
         * TYPE_DATE = 'D'; TYPE_MEMO = 'M'; TYPE_OLE = 'X';
         */
        switch (sqlType) {
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.BIGINT:
                return DbDefiner.FIELD_TYPE_INT;
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.BIT:
            case Types.REAL:
            case Types.DECIMAL:
                return DbDefiner.FIELD_TYPE_FLOAT;
            case Types.VARCHAR:
            case Types.CHAR:
                return DbDefiner.FIELD_TYPE_STR;
            case Types.TIMESTAMP:
                /**
                 * 20090608
                 * SybaseIQ中，对date,time,datetime,timestamp类型返回值与其他主流数据库不一致；
                 * date=9;time=10;datetime=timestamp=11
                 */
                // return DbDefiner.FIELD_TYPE_TIMESTAMP;
            case 11:
                // return DbDefiner.FIELD_TYPE_TIMESTAMP;
            case Types.TIME:
                // return DbDefiner.FIELD_TYPE_TIME;
            case 10:
                // return DbDefiner.FIELD_TYPE_TIME;
            case Types.DATE:
            case 9:
                return DbDefiner.FIELD_TYPE_DATE;
            case Types.BLOB:
            case Types.VARBINARY:
            case Types.BINARY:
            case Types.LONGVARBINARY:// sybase
                return DbDefiner.FIELD_TYPE_BINARY;
            case Types.LONGVARCHAR: // DB2,Mssql,Oracle
            case Types.CLOB: // Mysql,Sybase
                return DbDefiner.FIELD_TYPE_CLOB;
            case Types.BOOLEAN:
                return DbDefiner.FIELD_TYPE_LOGIC;
            case Types.ARRAY:
            case Types.DATALINK:
            case Types.JAVA_OBJECT:
            case Types.NULL:
            case Types.OTHER:
            case Types.REF:
            case Types.STRUCT:
            default: {
                return DbDefiner.FIELD_TYPE_OTHER;
            }
        }
    }

    /**
     * 此函数区分date，time，timestamp
     *
     * @param sqlType
     * @return
     */
    public static final char getSubsectionType(int sqlType) {

        switch (sqlType) {
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.BIGINT:
                return DbDefiner.FIELD_TYPE_INT;
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.BIT:
            case Types.REAL:
            case Types.DECIMAL:
                return DbDefiner.FIELD_TYPE_FLOAT;
            case Types.VARCHAR:
            case Types.CHAR:
                return DbDefiner.FIELD_TYPE_STR;
            /**
             * 20090608 同getType方法的修改；
             */
            case Types.TIMESTAMP:
            case 11:
                return DbDefiner.FIELD_TYPE_TIMESTAMP;
            case Types.TIME:
            case 10:
                return DbDefiner.FIELD_TYPE_TIME;
            case Types.DATE:
            case 9:
                return DbDefiner.FIELD_TYPE_DATE;
            case Types.BLOB:
            case Types.VARBINARY:
            case Types.BINARY:
            case Types.LONGVARBINARY:// sybase
                return DbDefiner.FIELD_TYPE_BINARY;
            case Types.LONGVARCHAR: // DB2,Mssql,Oracle
            case Types.CLOB: // Mysql,Sybase
                return DbDefiner.FIELD_TYPE_CLOB;
            case Types.BOOLEAN:
                return DbDefiner.FIELD_TYPE_LOGIC;
            case Types.ARRAY:
            case Types.DATALINK:
            case Types.JAVA_OBJECT:
            case Types.NULL:
            case Types.OTHER:
            case Types.REF:
            case Types.STRUCT:
            default:
                return DbDefiner.FIELD_TYPE_OTHER;
        }
    }

    public static String[] readStrArray(ResultSet rs, int i)
            throws SQLException {
        ArrayList dr = new ArrayList(1024 * 5);
        while (rs.next())
            dr.add(rs.getString(i));
        String[] r = new String[dr.size()];
        dr.toArray(r);
        return r;
    }

    public static int[] readIntArray(ResultSet rs, int i) throws SQLException {
        DoubleArray dr = new DoubleArray(1024 * 5);
        while (rs.next())
            dr.add(rs.getDouble(i));
        int[] r = new int[dr.size()];
        for (int j = 0; j < dr.size(); j++) {
            r[j] = (int) dr.get(j);
        }
        return r;
    }

    public static double[] readDoubleArray(ResultSet rs, int i)
            throws SQLException {
        DoubleArray dr = new DoubleArray(1024 * 5);
        while (rs.next())
            dr.add(rs.getDouble(i));
        return dr.toArray();
    }

    /**
     * 如果只有一列，那么返回一个对应类型的数组 如果有多列，那么返回一个object[]数组，每个元素对应一列
     */
    public static Object readArrayFromRS(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmt = rs.getMetaData();
        if (rsmt.getColumnCount() == 1) {// 为了效率 特殊处理字段是1的情况
            char coltype = SqlYn.getType(rsmt.getColumnType(1));
            switch (coltype) {
                case 'N':
                    return SqlYn.readDoubleArray(rs, 1);
                case 'I':
                    return SqlYn.readIntArray(rs, 1);
                case 'C':
                    return SqlYn.readStrArray(rs, 1);
                default:
                    // throw new
                    // UnsupportedOperationException("不支持数值和字符型以外的其他类型的数组");
                    throw new UnsupportedOperationException(I18N.getString(
                            "com.esen.jdbc.sqlfunc.unableother",
                            "不支持数值和字符型以外的其他类型的数组"));
            }
        } else {
            return SqlYn.readMultiDimArray(rs);// 读取多维数组。
        }
    }

    public static Object readMultiDimArray(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmt = rs.getMetaData();
        int columnCount = rsmt.getColumnCount();
        Object[] r = new Object[columnCount];
        char[] types = new char[columnCount];
        int capacity = 1025 * 5;
        for (int i = 1; i <= types.length; i++) {
            char t = getType(rsmt.getColumnType(i));
            types[i - 1] = t;
            switch (t) {
                case 'I': {
                    r[i - 1] = new int[capacity];
                    break;
                }
                case 'N': {
                    r[i - 1] = new double[capacity];
                    break;
                }
                case 'C': {
                    r[i - 1] = new String[capacity];
                    break;
                }
                case 'D': {
                    r[i - 1] = new Date[capacity];
                    break;
                }
                default:
                    // throw new UnsupportedOperationException("不支持的类型：" + t);
                    throw new UnsupportedOperationException(I18N.getString(
                            "com.esen.jdbc.sqlfunc.unsupportkind",
                            "不支持的类型：{0}", String.valueOf(t)));
            }
        }

        int count = 0;
        while (rs.next()) {
            if (count + 1 >= capacity) {
                for (int i = 0; i < columnCount; i++) {
                    r[i] = ArrayFunc.ensureCapacity(r[i], count + 1);
                }
            }
            for (int i = 0; i < columnCount; i++) {
                char t = types[i];
                switch (t) {
                    case 'N': {
                        ((double[]) (r[i]))[count] = rs.getDouble(i + 1);
                        break;
                    }
                    case 'C': {
                        ((String[]) (r[i]))[count] = rs.getString(i + 1);
                        break;
                    }
                    case 'I': {
                        ((int[]) (r[i]))[count] = rs.getInt(i + 1);
                        break;
                    }
                    case 'D': {
                        ((Date[]) (r[i]))[count] = rs.getDate(i + 1);
                        break;
                    }
                }
            }
            count++;
        }

        for (int i = 0; i < columnCount; i++) {
            r[i] = ArrayFunc.setCapacity(r[i], count);
        }

        return r;
    }

    /**
     * 根据一个链接获得dialect
     */
    public final static Dialect createDialect(Connection con) {
        return DialectFactory.createDialect(con);
    }

    /**
     * 判断是不是一个调用存储过程的sql
     *
     * @param sql
     * @return
     */
    public final static boolean isCallalbe(String sql) {
        if (sql == null || sql.length() == 0) {
            return false;
        }
        Pattern p = Pattern.compile(
                "\\s*((\\?|\\$\\w*)\\s*=)?\\s*\\{\\s*call\\s+.*\\}\\s*",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sql);
        return m.matches();
    }

    /**
     * 判断输入的sql 是不是形如： select * from tbname 的形式，如果是，解析出tbname 如果有where条件将返回null;
     * 这个方法主用用于DataCopy中判断是否需要整表复制；
     * <p>
     * 20100330 支持dbo.tablename, user.dbo.tablename 形式的表名；
     *
     * @param sql
     * @return
     */
    public final static String getTablename(String sql) {
        /*
         * Pattern p = Pattern.compile(
         * "^\\s*+select\\s+\\*\\s+from\\s+([a-z]+([\\.]?[a-z_0-9\\$]+)*)\\s*$",
         * Pattern.CASE_INSENSITIVE);
         */

        /**
         * 原始的reg:^\s*select\s*\*\s*from\s*([a-zA-Z\$]+[a-zA-Z0-9\$]*\.)*([a-zA-
         * Z\$]+[a-zA-Z0-9\$]*)*\s*$ 修改一处BUG,以前正则表达式在某些JVM上匹配
         * "select * from T1E1648G802D1271P461A1496C1743 where isfile_ = 1"
         * 这条SQL语句会卡死
         */
        Pattern p = Pattern
                .compile(
                        "^\\s*select\\s*\\*\\s*from\\s*([a-zA-Z\\$]+[a-zA-Z0-9\\$]*\\.)*([a-zA-Z\\$]+[a-zA-Z0-9\\$]*)*\\s*$",
                        Pattern.CASE_INSENSITIVE);

        Matcher m = p.matcher(sql);
        if (m.find()) {
            return m.group(2);
        }
        return null;
    }

    /**
     * 判断是否是一个合法的变量名，表名，或者字段名 20100330 支持dbo.tablename, user.dbo.tablename
     * 形式的表名；
     */
    public final static boolean isValidSymbol(String s) {
        if (s == null || s.length() == 0 || s.length() > 100) return false;
        Pattern p = Pattern.compile("[a-z]+([\\.]?[a-z_0-9\\$]+)*",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(s);
        return m.matches();
    }

    /**
     * 判断一个sql是否是update xxx set xxx
     */
    public final static boolean isUpdate(String sql) {
        if (sql == null || sql.length() < 12) return false;
        /**
         * BI-5779 20110919 dw update语句中的表面可能带有schema前缀，原来的代码不能匹配这种表名，
         * 造成写入数据调用setObject()，由于无法知道字段类型，写入null值出现异常；
         */
        Pattern p = Pattern.compile("\\s*update\\s+([\\S]+)\\s+set\\s+.+",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(sql);
        return m.matches();
    }

    /**
     * 判断一个sql是否是insert into
     */
    public final static boolean isInsertInto(String sql) {
        if (sql == null || sql.length() < 12) return false;
        Pattern p = Pattern.compile("\\s*insert\\s+into\\s+\\w+.*",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(sql);
        return m.matches();
    }

    /**
     * 判断一个sql是否是delete from
     */
    public final static boolean isDelete(String sql) {
        if (sql == null || sql.length() < 12) return false;
        Pattern p = Pattern.compile("\\s*delete\\s+from\\s+\\w+.*",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(sql);
        return m.matches();
    }

    /**
     * 判断一个sql是否是alter table
     */
    public final static boolean isAlter(String sql) {
        if (sql == null || sql.length() < 12) return false;
        Pattern p = Pattern.compile("\\s*alter\\s+table\\s+\\w+.*",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(sql);
        return m.matches();
    }

    /**
     * 判断一个sql是否是select XXXX from XX
     */
    public final static boolean isSelect(String sql) {
        if (sql == null || sql.length() < 15) return false;
        Pattern p = Pattern.compile("\\s*\\(*\\s*select\\s+.*\\s+from.+",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(sql);
        return m.matches();
    }

    /**
     * 判断一个sql是否是create xxxx 这里只是简单的判断是否以create开始,以后如果需要再完善
     */
    public final static boolean isCreate(String sql) {
        if (sql == null || sql.length() < 7) return false;
        Pattern p = Pattern.compile("\\s*create\\s+.*",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        return p.matcher(sql).matches();
    }

    /**
     * 判断一个sql是否是drop xxx 这里只是简单的判断是否以drop开始，以后如果需要在完善
     *
     * @param sql
     * @return
     */
    public final static boolean isDrop(String sql) {
        if (sql == null || sql.length() < 5) return false;
        Pattern p = Pattern.compile("\\s*drop\\s+.*", Pattern.CASE_INSENSITIVE
                | Pattern.DOTALL);
        return p.matcher(sql).matches();
    }

    /**
     * 判断一个sql是否是rename xxx 这里只是简单的判断是否以drop开始，以后如果需要在完善
     *
     * @param sql
     * @return
     */
    public final static boolean isRename(String sql) {
        if (sql == null || sql.length() < 5) return false;
        Pattern p = Pattern.compile("\\s*rename\\s+.*",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        return p.matcher(sql).matches();
    }

    /**
     * 将任意类型的对象转换成java.sql.Timestamp
     *
     * @param x
     * @return
     */
    public final static Timestamp toSqlTimeStamp(Object x) {
        if (x == null || x instanceof Timestamp) return (Timestamp) x;
        if (x instanceof Date) {
            Date d = (Date) x;
            return new Timestamp(d.getTime());
        }
        Calendar cc = StrUtil.parseCalendar(x, null);
        if (cc == null) return null;

        return new Timestamp(cc.getTimeInMillis());
    }

    public final static Time toSqlTime(Object x) {
        if (x == null || x instanceof Time) return (Time) x;
        Calendar cc = StrUtil.parseCalendar(x, null);
        if (cc == null) return null;

        return new Time(cc.getTimeInMillis());
    }

    /**
     * 将任意类型转换成java.sql.Date
     *
     * @param x
     * @return
     */
    public final static Date toSqlDate(Object x) {
        if (x == null || x instanceof Date) return (Date) x;
        if (x instanceof Timestamp) {
            Timestamp tp = (Timestamp) x;
            new Date(tp.getTime());
        }
        Calendar cc = StrUtil.parseCalendar(x, null);
        if (cc == null) return null;
        return new Date(cc.getTimeInMillis());
    }

    /**
     * 将任意的类型转换成字符串类型； 包括：数字，字符，日期，clob,reader 其他：bolb,inputsteam 返回null
     *
     * @param x
     * @return
     * @throws SQLException
     */
    public final static String toSqlVarchar(Object x) throws SQLException {
        if (x == null || x instanceof String) return (String) x;
        if (x instanceof Integer || x instanceof Long || x instanceof Float
                || x instanceof Double || x instanceof Date
                || x instanceof java.util.Date || x instanceof Time
                || x instanceof Timestamp) return x.toString();
        if (x instanceof Reader) {
            Reader r = (Reader) x;
            return reader2str(r);
        }
        if (x instanceof Clob) {
            Clob clob = (Clob) x;
            Reader r = clob.getCharacterStream();
            if (r != null) return reader2str(r);
        }
        return null;
    }

    // 匹配形如:field1=?的sql(符号包括>,=,<,>=,<=,like)

    public static String reader2str(Reader rr) throws SQLException {
        if (rr == null) return null;
        /*
         * StringBuffer sb; char[] cc = new char[1024 * 8]; sb = new
         * StringBuffer(); int ll, len = 0; try{ while ( (ll = rr.read(cc)) !=
         * -1) { len += ll; if (ll > 0) { sb.append(cc, 0, ll); } }
         * }catch(IOException ie){ SQLException se = new
         * SQLException(ie.getMessage()); se.setStackTrace(ie.getStackTrace());
         * throw se; } return sb.toString();
         */
        try {
            return StmFunc.reader2str(rr);
        } catch (IOException e) {
            SQLException se = new SQLException(e.getMessage());
            se.setStackTrace(e.getStackTrace());
            throw se;
        }
    }

    /**
     * 根据带?号的insert或者update语句,获得select语句(查询出带?号的字段的值) eg:insert into tbname
     * (field1,field2)values(?,?) --> select field1,field2 from tbname insert
     * into tbname values(?,?) --> select * from tbname update tbname set
     * field1=?,field2=?,field6=field7 where field3=? and field4=? and
     * field5>0--> select field1,field2,field3,field4 from tbname update tbname
     * set field1=?,field2=? --> select field1,field2 from tbname
     *
     * @param sql (带?号的sql,只能为insert和update语句)
     * @return ?号对应的字段的select语句
     */
    public static final String getSelectSql(String sql) {
        /*
         * ESENBI-2425: modify by liujin 2014.11.07 将 sql 语句中的换行改为空格，方便 SQL
         * 语句处理。
         */
        String sql_tmp = sql.replace('\n', ' ');
        StringBuffer sb = new StringBuffer("select ");
        /*
         * Matcher insertAllField = INSERT_ALLFIELD_REGEX.matcher(sql); if
         * (insertAllField.matches()) { String tableName =
         * insertAllField.group(1); sb.append("* from "); sb.append(tableName);
         * return sb.toString(); }
         */
        Matcher insertAllField = INSERT_ALLFIELD_REGEX.matcher(sql_tmp);
        if (insertAllField.find()) {
            String tableName = insertAllField.group(1);
            sb.append("* from ");
            sb.append(tableName);
            return sb.toString();
        }
        /*
         * Matcher insertSomeField = INSERT_SOMEFIELD_REGEX.matcher(sql); if
         * (insertSomeField.matches()) { String tableName =
         * insertSomeField.group(1); String fieldsName =
         * insertSomeField.group(2); sb.append(fieldsName); sb.append(" from ");
         * sb.append(tableName); return sb.toString(); }
         */
        Matcher insertSomeField = INSERT_SOMEFIELD_REGEX.matcher(sql_tmp);
        if (insertSomeField.find()) {
            String tableName = insertSomeField.group(1);
            String fieldsName = insertSomeField.group(2);
            sb.append(fieldsName);
            sb.append(" from ");
            sb.append(tableName);
            return sb.toString();
        }
        Matcher updateSql = UPDATE_SQL_REGEX.matcher(sql_tmp);
        if (updateSql.find()) {
            String tableName = updateSql.group(1);
            String srcStr = updateSql.group(2);
            Matcher updateField = UPDATE_FIELD.matcher(srcStr);
            if (updateField.find()) {
                String field = updateField.group(1);
                sb.append(field);
            }
            while (updateField.find()) {
                String field = updateField.group(1);
                sb.append(",");
                sb.append(field);
            }
            sb.append(" from ");
            sb.append(tableName);
            return sb.toString();
        }
        return null;
    }

    /**
     * jdbc:postgresql://127.0.0.1:5432/orcdb
     * jdbc:sybase:Tds:127.0.0.1:2638/orcdb
     * jdbc:informix-sqli://127.0.0.1:port/orcdb:informixserver=SERVERNAME
     * jdbc:db2://127.0.0.1:50000/orcdb
     * jdbc:jtds:sqlserver://127.0.0.1:1433/orcdb
     * ;TDS=8.0;SendStringParametersAsUnicode=true jdbc:mysql://127.0.0.1/orcdb
     * jdbc:oracle:thin:@127.0.0.1:1521:orcdb
     */
    public final static String getIpAddressFromUrl(String url) {
        Pattern p = Pattern
                .compile("([1234567890]{1,3}\\.){3}[1234567890]{1,3}");
        Matcher mt = p.matcher(url);
        if (mt.find()) {
            return mt.group();
        }

        p = Pattern.compile("(\\:\\/\\/)([\\w|\\-|\\_]+)(\\:|\\/)",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(url);
        if (m.find()) {
            return m.group(2);
        }

        p = Pattern.compile("jdbc\\:sybase\\:Tds\\:([\\w|\\-|\\_]+)(\\:|\\/)",
                Pattern.CASE_INSENSITIVE);
        m = p.matcher(url);
        if (m.find()) {
            return m.group(1);
        }

        p = Pattern.compile(
                "jdbc\\:oracle\\:thin\\:\\@([\\w|\\-|\\_]+)(\\:|\\/)",
                Pattern.CASE_INSENSITIVE);
        m = p.matcher(url);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * 20091221 检查jdbc的配置文件； 1)url参数中是否指定了编码参数； 2)日志级别是否设置过低；
     *
     * @param props
     * @return
     */
    public static String checkJdbc(MiniProperties props) {
        String ds3str = props.getProperty(PoolPropName.PROP_OTHERDATASOURCE);
        if (StrUtil.isNull(ds3str)) {
            ds3str = props.getProperty(PoolPropName.PROP_OTHERDATASOURCE3);
        }
        StringBuffer sb = new StringBuffer(32);
        /**
         * 如果没有第三方连接池，则检查连接池的参数； 主要是url中是否有指定编码、日志级别是否太低；
         */
        if (StrUtil.isNull(ds3str)) {
            String driverclass = props
                    .getProperty(PoolPropName.PROP_DRIVERCLASSNAME);
            String url = props.getProperty(PoolPropName.PROP_URL);
            String levstr = props.getProperty(PoolPropName.PROP_LOGLEVER);

            if (driverclass.indexOf("mysql") > 0) {
                if (url.indexOf("characterEncoding") < 0) {
                    // sb.append("Mysql连接池url没有指定编码参数characterEncoding;\n");
                    sb.append(I18N.getString(
                            "com.esen.jdbc.sqlfunc.mysqlmischarset",
                            "Mysql连接池url没有指定编码参数characterEncoding;\n"));
                }
            }
            if (driverclass.indexOf("sybase") > 0) {
                /**
                 * 20100205 连接SybaseAse 时driverClassName最好使用：
                 * com.sybase.jdbc3.jdbc.SybDriver
                 * 而不是使用：com.sybase.jdbc2.jdbc.SybDriver
                 * 原因是：com.sybase.jdbc2.jdbc
                 * .SybDriver会导致写入varchar类型最大长度是256，如果超过会截取，却不报错；
                 * com.sybase.jdbc3.jdbc.SybDriver没有这个问题；
                 */
                if (driverclass.equals("com.sybase.jdbc2.jdbc.SybDriver")) {
                    // sb.append("Sybase请使用driverClassName=com.sybase.jdbc3.jdbc.SybDriver;\n");
                    // sb.append("原因是：com.sybase.jdbc2.jdbc.SybDriver会导致写入varchar类型最大长度是256，如果超过会截取，却不报错；\n");
                    sb.append(I18N
                            .getString(
                                    "com.esen.jdbc.sqlfunc.sybasedriver",
                                    "Sybase请使用driverClassName=com.sybase.jdbc3.jdbc.SybDriver;\n原因是：com.sybase.jdbc2.jdbc.SybDriver会导致写入varchar类型最大长度是256，如果超过会截取，却不报错；\n"));
                }
                if (url.indexOf("charset") < 0) {
                    // sb.append("Sybase连接池url没有指定编码参数charset;\n");
                    sb.append(I18N.getString(
                            "com.esen.jdbc.sqlfunc.sybasemischarset",
                            "Sybase连接池url没有指定编码参数charset;\n"));
                }
            }

            if (!StrUtil.isNull(levstr) && levstr.equalsIgnoreCase("debug")) {
                sb.append(I18N.getString("com.esen.jdbc.sqlfunc.leveladvise",
                        "连接池日志级别现在是debug级，建议改成error级别；\n"));
            }
        }
        if (sb.length() > 0) {
            return sb.toString();
        }
        return null;
    }

    /**
     * 根据驱动类名来返回数据库的类型
     *
     * @param driverClassName
     * @return
     */
    public static final String getDbType(String driverClassName) {
        if (!StrUtil.isNull(driverClassName)) {
            if (driverClassName.startsWith("com.ibm.db2")) {
                return SqlConst.DB_DB2;
            }
            if (driverClassName.startsWith("com.mysql")) {
                return SqlConst.DB_MYSQL;
            }
            if (driverClassName.startsWith("oracle")) {
                return SqlConst.DB_ORACLE;
            }
            if (driverClassName.startsWith("com.sybase.jdbc3")) {
                return SqlConst.DB_SYBASE_IQ;
            }
            if (driverClassName.startsWith("essbase")) {
                return SqlConst.DB_ESSBASE;
            }
            if (driverClassName.startsWith("com.sybase.jdbc2")) {
                return SqlConst.DB_SYBASE;
            }
            if (driverClassName.startsWith("com.microsoft.sqlserver")
                    || driverClassName.startsWith("net.sourceforge.jtds")) {
                return SqlConst.DB_MSSQL;
            }
            if (driverClassName.startsWith("org.netezza.Driver")) {
                return SqlConst.DB_NETEZZA;
            }
            if (driverClassName.startsWith("com.gbase")) {
                return SqlConst.DB_GBASE;
            }
            if (driverClassName.startsWith("org.postgresql.Driver")) {
                return SqlConst.DB_GREENPLUM;
            }
            if (driverClassName.startsWith("com.teradata.jdbc.TeraDriver")) {
                return SqlConst.DB_TERADATA;
            }
            if (driverClassName.startsWith("com.vertica.jdbc.Driver")) {
                return SqlConst.DB_VERTICA;
            }

        }

        return SqlConst.DB_OTHER;
    }

    /**
     * 通过连接池对象判断是否是mdx服务器。 此方法不需要启动连接池，即使mdx服务器无法访问，也可以保证很快的判断此连接池是不是mdx服务器；
     *
     * @param ConnectionFactory conf 此参数如果为空，会出异常；
     * @return
     */
    public static final boolean isMdx(ConnectionFactory conf) {
        if (conf == null) {
            // throw new RuntimeException("连接池对像为空，无法判断是否是mdx服务器；");
            throw new RuntimeException(I18N.getString(
                    "com.esen.jdbc.sqlfunc.unknownmdx",
                    "连接池对像为空，无法判断是否是mdx服务器；"));
        }
        /*
         * add by jzp 2013.4.25当不是poolconnectionFactory时 直接返回false;
         */
        if (!(conf instanceof PoolConnectionFactory)) return false;
        PoolConnectionFactory pconf = (PoolConnectionFactory) conf;
        String url = pconf.getDataSource().getUrl();
        return isMdx(url);
    }

    /**
     * 通过连接池参数url判断是否是mdx服务器。
     *
     * @param url
     * @return
     */
    public static final boolean isMdx(String url) {
        return url != null && url.startsWith("jdbc:olap2j");
    }

    /**
     * 通过连接池对象判断是否是Greenplum数据库。
     * 此方法不需要启动连接池，即使Greenplum服务器无法访问，也可以保证很快的判断此连接池是不是Greenplum服务器；
     *
     * @param ConnectionFactory conf 此参数如果为空，会出异常；
     * @return
     */
    public static final boolean isGreenplum(ConnectionFactory conf) {
        if (conf == null) {
            // throw new RuntimeException("连接池对像为空，无法判断是否是Greenplum服务器；");
            throw new RuntimeException(I18N.getString(
                    "com.esen.jdbc.sqlfunc.unknownGreenplum",
                    "连接池对像为空，无法判断是否是Greenplum服务器；"));
        }
        /*
         * add by jzp 2013.4.25当不是poolconnectionFactory时 直接返回false;
         */
        if (!(conf instanceof PoolConnectionFactory)) return false;
        PoolConnectionFactory pconf = (PoolConnectionFactory) conf;
        String url = pconf.getDataSource().getUrl();
        return isGreenplum(url);
    }

    /**
     * 通过连接池url判断是否是Greenplum数据库。 可能驱动是 jdbc:postgresql 不只一种 此处可能有点误差
     *
     * @param url
     * @return
     */
    public static final boolean isGreenplum(String url) {
        return url != null && url.startsWith("jdbc:postgresql");
    }

    /**
     * 通过连接池对象判断是否是Netezza数据库。
     * 此方法不需要启动连接池，即使Netezza服务器无法访问，也可以保证很快的判断此连接池是不是Netezza服务器；
     *
     * @param ConnectionFactory conf 此参数如果为空，会出异常；
     * @return
     */
    public static final boolean isNetezza(ConnectionFactory conf) {
        if (conf == null) {
            // throw new RuntimeException("连接池对像为空，无法判断是否是Netezza服务器；");
            throw new RuntimeException(I18N.getString(
                    "com.esen.jdbc.sqlfunc.unknownNetezza",
                    "连接池对像为空，无法判断是否是Netezza服务器；"));
        }
        /*
         * add by jzp 2013.4.25当不是poolconnectionFactory时 直接返回false;
         */
        if (!(conf instanceof PoolConnectionFactory)) return false;
        PoolConnectionFactory pconf = (PoolConnectionFactory) conf;
        String url = pconf.getDataSource().getUrl();
        return isNetezza(url);
    }

    /**
     * 通过连接池url判断是否是Netezza数据库。
     *
     * @param url
     * @return
     */
    public static final boolean isNetezza(String url) {
        return url != null && url.startsWith("jdbc:netezza");
    }

    /**
     * 通过连接池对象判断是否是Gbase数据库。
     * 此方法不需要启动连接池，即使Gbase服务器无法访问，也可以保证很快的判断此连接池是不是Gbase服务器；
     *
     * @param ConnectionFactory conf 此参数如果为空，会出异常；
     * @return
     */
    public static final boolean isGBase(ConnectionFactory conf) {
        if (conf == null) {
            // throw new RuntimeException("连接池对像为空，无法判断是否是Netezza服务器；");
            throw new RuntimeException(I18N.getString(
                    "com.esen.jdbc.sqlfunc.unknownGBase",
                    "连接池对像为空，无法判断是否是GBase服务器；"));
        }
        /*
         * add by jzp 2013.4.25当不是poolconnectionFactory时 直接返回false;
         */
        if (!(conf instanceof PoolConnectionFactory)) return false;
        PoolConnectionFactory pconf = (PoolConnectionFactory) conf;
        String url = pconf.getDataSource().getUrl();
        return isGBase(url);
    }

    /**
     * 通过连接池对象判断是否是Gbase数据库。
     *
     * @param url
     * @return
     */
    public static final boolean isGBase(String url) {
        return url != null && url.startsWith("jdbc:gbase");
    }

    /**
     * 判断两个连接是不是来自同一个数据库的同一个用户；
     *
     * @param srccon
     * @param destcon
     * @return
     */
    public static boolean compareConnection(Connection srccon,
                                            Connection destcon) {
        DataBaseInfo db1 = DataBaseInfo.createInstance(srccon);
        DataBaseInfo db2 = DataBaseInfo.createInstance(destcon);
        return db1.getJdbcurl().equals(db2.getJdbcurl())
                && db1.getUserName().equals(db2.getUserName());
    }

    /**
     * 返回连接池是否能够连接到数据库,可以返回true,否则返回false
     *
     * @param fct
     * @return
     */
    public static final boolean canConnect(ConnectionFactory fct) {
        if (fct == null) return false;
        try {
            testConnect(fct);
            return true;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 判断连接池是否能够连接到数据库,如果无法获取连接抛出异常
     *
     * @param fct
     * @return
     */
    public static final void testConnect(ConnectionFactory fct)
            throws Exception {
        if (fct == null)
            // throw new Exception("ConnectionFactory为空,无法获取连接。");
            throw new Exception(I18N.getString(
                    "com.esen.jdbc.sqlfunc.nullconnf",
                    "ConnectionFactory为空,无法获取连接。"));
        Connection con = fct.getConnection();
        try {
            if (con == null) {
                // throw new Exception("无法获取连接。");
                throw new Exception(I18N.getString(
                        "com.esen.jdbc.sqlfunc.unablegetconn", "无法获取连接。"));
            }
        } finally {
            if (con != null) con.close();
        }
    }

    /**
     * 判断表或视图是否存在
     *
     * @param fct
     * @param table
     * @param throwException 如果出现异常是否抛出
     * @return
     */
    public static final boolean tableOrViewExist(ConnectionFactory fct,
                                                 String table, boolean throwException) {
        if (StrUtil.isNull(table)) return false;
        try {
            Connection con = fct.getConnection();
            try {
                return tableOrViewExist(con, fct.getDbDefiner(), table,
                        throwException);
            } finally {
                con.close();
            }
        } catch (Exception e) {
            if (throwException) {
                throw new RuntimeException(e);
            } else {
                log.warn(e.getMessage(), e);
                return false;
            }
        }
    }

    public static final boolean tableOrViewExist(Connection con, DbDefiner dd,
                                                 String table, boolean throwException) {
        if (StrUtil.isNull(table)) return false;
        try {
            return dd.tableOrViewExists(con, table);
        } catch (Exception e) {
            if (throwException) {
                throw new RuntimeException(e);
            } else {
                log.warn(e.getMessage(), e);
                return false;
            }
        }
    }

    /**
     * 根据xml文件的配置创建一个数据库表,如果表已经存在,则修复之
     *
     * @param fct
     * @param table
     * @param c
     * @param xml
     * @return
     * @throws Exception
     */
    public static final String createTable(ConnectionFactory fct, String table,
                                           Class c, String xml) throws Exception {
        if (StrUtil.isNull(table) || StrUtil.isNull(xml)) return null;
        DbDefiner dd = fct.getDbDefiner();
        Connection con = fct.getConnection();
        try {
            return createTable(con, dd, table, c, xml);
        } finally {
            con.close();
        }
    }

    public static final String createTable(Connection con, DbDefiner dd,
                                           String table, Class c, String xml) throws Exception {
        if (StrUtil.isNull(table) || StrUtil.isNull(xml)) return null;
        Document doc = null;
        InputStream in = c.getResourceAsStream(xml);
        if (in == null) return null;
        try {
            doc = XmlFunc.getDocument(in);
        } finally {
            in.close();
        }
        if (dd.tableOrViewExists(con, table)) {
            return dd.repairTable(con, doc, table, true);
        } else {
            return dd.createTable(con, doc, table, false, true);
        }
    }

    public static final String createTable(ConnectionFactory fct, String table,
                                           String xml) throws Exception {
        return createTable(fct, table, SqlYn.class, xml);
    }

    public static final String createTable(Connection con, DbDefiner dd,
                                           String table, String xml) throws Exception {
        return createTable(con, dd, table, SqlYn.class, xml);
    }

    /**
     * 删除表或视图,此函数会判断表或视图是否存在，如果存在，才调用drop方法。
     *
     * @param fct   连接池
     * @param table 表名或视图名
     * @throws Exception
     */
    public static final void dropTable(ConnectionFactory fct, String table)
            throws Exception {
        // 如果表为空或无效的表名,则不会作任何处理
        if (StrUtil.isNull(table) || !SqlYn.isValidSymbol(table)) {
            return;
        }
        DbDefiner dd = fct.getDbDefiner();
        Connection con = fct.getConnection();
        try {
            dropTable(con, dd, table);
        } finally {
            con.close();
        }
    }

    /**
     * 此函数会判断表或视图是否存在，如果存在，才调用drop方法。
     *
     * @param con
     * @param dd
     * @param table
     * @throws Exception
     */
    public static void dropTable(Connection con, DbDefiner dd, String table)
            throws Exception {
        /**
         * 可能表或视图不存在或被前面的代码已经删除了，此时应该忽略他们。
         */
        if (dd.tableExists(con, null, table)) {
            // log.info("删除数据库表{}", table);
            log.info(I18N.getString("com.esen.jdbc.SqlFunc.java.comp1",
                    "删除数据库表{}"), table);
            dd.dropTable(con, null, table);
        } else if (dd.viewExists(con, table)) {
            // log.info("删除视图{}", table);
            log.info(I18N.getString("com.esen.jdbc.SqlFunc.java.comp2",
                    "删除视图{}"), table);
            dd.dropView(con, table);
        } else {
            // log.info("表或视图{}不存在，忽略它", table);
            log.info(I18N.getString("com.esen.jdbc.SqlFunc.java.comp3",
                    "表或视图{}不存在，忽略它"), table);
        }
    }

    /**
     * 测试数据库连接 允许指定超时时间 这里的数据库连接超时不是在jdbc层面实现的，而是通过的Future的get方法来指定超时的时间
     *
     * @param props   数据库连接池的相关属性
     * @param pool    线程池对象
     * @param millsec 超时时间 秒
     * @throws Exception
     */
    public static final void testDatasourceWithTimeout(
            final MiniProperties props, ThreadPool pool, long sec)
            throws Exception {
        Future f = pool.execute(new Callable<Exception>() {
            public Exception call() throws Exception {
                try {
                    SqlYn.testDataSource(props);
                    return null;
                } catch (Exception e) {
                    return e;
                }
            }
        });
        Exception e = null;
        try {
            e = (Exception) f.get(sec * 1000, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            throw new RuntimeException4I18N("com.esen.jdbc.sqlfunc.1", "连接超时",
                    ex);
        }
        if (e != null) {
            throw e;
        }
    }

    /**
     * 测试数据库连接，如果不成功则抛出异常。
     *
     * @throws Exception
     */
    public static final void testDataSource(MiniProperties props)
            throws Exception {
        if (props == null) {
            // throw new RuntimeException("测试失败,缺少JDBC连接属性.");
            throw new RuntimeException(I18N.getString(
                    "com.esen.jdbc.sqlfunc.testerr1", "测试失败,缺少JDBC连接属性."));
        }
        String url = props.getString(PoolPropName.PROP_URL);
        String catalog = props.getString(PoolPropName.PROP_CATALOG);
        String driver = props.getString(PoolPropName.PROP_DRIVERCLASSNAME);
        String username = props.getString(PoolPropName.PROP_USERNAME, "");
        String password = StrFunc.decryptPlainPassword(props.getString(
                PoolPropName.PROP_PASSWORD, ""));
        if (StrFunc.isNull(url)) {
            // throw new RuntimeException("测试失败,缺少参数(" + PoolPropName.PROP_URL +
            // ").");
            throw new RuntimeException(I18N.getString(
                    "com.esen.jdbc.sqlfunc.testerr2", "测试失败,缺少参数({0}).",
                    new Object[]{PoolPropName.PROP_URL}));
        }
        if (StrFunc.isNull(driver)) {
            // throw new RuntimeException("测试失败,缺少参数(" +
            // PoolPropName.PROP_DRIVERCLASSNAME + ").");
            throw new RuntimeException(I18N.getString(
                    "com.esen.jdbc.sqlfunc.testerr2", "测试失败,缺少参数({0}).",
                    new Object[]{PoolPropName.PROP_DRIVERCLASSNAME}));
        }
        try {
            Class.forName(driver);
        } catch (Exception ex) {
            // throw new RuntimeException("测试失败,找不到驱动类库(" + driver + ").");
            throw new RuntimeException(I18N.getString(
                    "com.esen.jdbc.sqlfunc.testerr3", "测试失败,找不到驱动类库({0}).",
                    driver), ex);
        }
        url = DefaultConnectionFactory.convertDbUrl(url);
        java.sql.Connection _conn = null;
        try {
            _conn = java.sql.DriverManager.getConnection(url, username,
                    password);
        } catch (Exception e) {
            // throw new
            // RuntimeException("测试失败,无法获得数据库链接.\r\n"+e.toString()+"\r\n"+e.getMessage());
            throw new RuntimeException(I18N.getString(
                    "com.esen.jdbc.sqlfunc.testerr4",
                    "测试失败,无法获得数据库链接.\r\n{0}\r\n{1}", e.toString(), e.getMessage()), e);
        }
        if (_conn == null) {
            // throw new RuntimeException("测试失败,无法获得数据库链接.");
            throw new RuntimeException(I18N.getString(
                    "com.esen.jdbc.sqlfunc.testerr5", "测试失败,无法获得数据库链接."));
        }
        try {
            if (!StrFunc.isNull(catalog)) _conn.setCatalog(catalog);
        } finally {
            _conn.close();
            _conn = null;
        }
    }

    /**
     * 删除存储过程
     *
     * @param conn
     * @param Name
     * @return
     * @throws Exception
     */
    public static boolean dropProcedure(Connection conn, String Name)
            throws Exception {
        if (conn == null) {
            // 无法获取连接
            throw new Exception(I18N.getString(
                    "com.esen.jdbc.sqlfunc.unablegetconn", "无法获取连接。"));
        }
        String sql = " DROP PROCEDURE " + Name;

        PreparedStatement stmt = conn.prepareStatement(sql);
        try {
            stmt.execute();
        } finally {
            stmt.close();
        }
        return true;
    }

    /**
     * 创建存储过程，用于执行比较复杂的sql，数据库连接需要自己释放
     *
     * @param conn     数据库连接
     * @param Name     存储过程名称
     * @param sql      过程内容
     * @param progress 进度
     * @return
     * @throws Exception
     */
    public static boolean createProcedure(Connection conn, String Name,
                                          String sql) throws Exception {
        sql = " CREATE OR REPLACE PROCEDURE " + Name + " AS\n BEGIN\n"
                + sql.replaceAll("\r\n", "\n") + "\nEND " + Name + " ; ";
        conn.setAutoCommit(false);
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.execute();
            conn.commit();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        return true;
    }

    /**
     * 执行指定的存储过程（无参数/已拼接为字符串）
     *
     * @param conn      数据库连接
     * @param procedure 存储过程"{call name}"
     * @return count 影响的数据条数
     * @throws Exception
     */
    public static int execute_procedure(Connection conn, String procedure)
            throws Exception {
        conn.setAutoCommit(false);
        CallableStatement cstmt = null;
        try {
            cstmt = conn.prepareCall(procedure);
            cstmt.execute();
            conn.commit();
            return cstmt.getUpdateCount();
        } finally {
            if (cstmt != null) {
                cstmt.close();
            }
        }
    }

}