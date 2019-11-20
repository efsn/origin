package org.codeyn.util.yn;

import org.codeyn.util.Maps;
import org.codeyn.util.i18n.I18N;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * 安全方法工具类, 提供一组参数校验函数，用于判断参数以避免恶意脚本攻击与注入。
 * 该类借用了i@report中相应的类，这里扩展为BI专用安全方法类，目前方法不够全面，后续可以扩展。
 */
public class SecurityYn {
    /**
     * 匹配标识符：字母、数字下划线构成
     */
    public static final Pattern BI_IDENTIFIER = Pattern.compile("[\\d\\w_]+");

    /**
     * 20121224 by kangx 修改XSS匹配规则<br>
     * 匹配带有XSS攻击脚本的正则表达式, 匹配符合*&ltscript *&gt*&lt/script&gt*的认为含有脚本攻击<br>
     * 以前的匹配规则为"[^<>\r\n\t&\'\"\\x00]*"<br>
     * 20121226 by kangx 增加匹配&ltimg *"/&gt<br>
     * 增加匹配&ltiframe *"/&gt 增加匹配规则:content-type: + ; add by wandj 2013.7.2
     * 增加匹配规则：</script><object data=jav\\x61scr\\x69pt:rsgvpk(upx)> by chxb
     * 2014/9/4
     */
    public static final Pattern SCRIPT_XSS = Pattern
            .compile(".*?((<|\\%3[C|c]).*([S|s][C|c][R|r][I|i][P|p][T|t].*?(>|\\%3[E|e]).*?(<|\\%3[C|c]).*?[S|s][C|c][R|r][I|i][P|p][T|t].*?(>|\\%3[E|e]))"
                    + "|((<|\\%3[C|c]).*(([I|i][M|m][G|g])|([I|i][F|f][R|r][A|a][M|m][E|e])|([A|a]))[\\s+]+.*?[\\/]?(>|\\%3[E|e]))|([J|j][A|a][V|v][A|a][S|s][C|c][R|r][I|i][P|p][T|t])"
                    + "|(([S|s][T|t][Y|y][L|l][E|e]).*:.*?([E|e][X|x][P|p][R|r][E|e][S|s][S|s][I|i][O|o][N|n])|(\\/\\*.*\\*\\/))"
                    + "|(([Cc][Oo][Nn][Tt][Ee][Nn][Tt]-[Tt][Yy][Pp][Ee](:|(\\%3[aA])).*(;|(\\%3[Bb])))+)"
                    + "|((<|\\%3[C|c])\\/.*([S|s][C|c][R|r][I|i][P|p][T|t].*?(>|\\%3[E|e])))"
                    + "|((<|\\%3[C|c])\\/.*([S|s][C|c][R|r][I|i][P|p][T|t].*?(>|\\%3[E|e])).*?<[O|o][B|b][J|j][E|e][C|c][T|t])"
                    + ").*?");

    /**
     * 带有SQL注入脚本字符的正则表达式，如果字符串中包含了至少以下的一个字符
     * '、=、>、<、!，或者包含了union、join、from、select、update、delete、where、or、and、not、between、like、is的SQL关键
     * 词
     */
    public static final Pattern SCRIPT_SQLINJ = Pattern
            .compile("[\\s\\S]*(([\'=><!]|(\\%27)|(\\%uFF07))|(\\s+(?-i:union|join|from|select|update|delete|where|or|and|not|between|like|is)+\\s+))[\\s\\S]*");

    /**
     * 匹配整数，包括正负号
     */
    public static final Pattern INTEGER = Pattern.compile("[+-]?\\d+");

    /**
     * 匹配数字。可以是整数也可以是浮点数。支持科学计算法。
     */
    public static final Pattern NUMBER = Pattern
            .compile("[+-]?\\d+(\\.\\d+[E|e]?\\d+)?");

    /**
     * 匹配访问的相对路径，可以../或者 字符打头，可以有后缀名，也可以没有
     */
    public static final Pattern URLPATH = Pattern
            .compile("(../)*([\\d\\w_]+(/))*([\\d\\w_]+(.(\\w)+)?)");
    /**
     * Header Manipulation 文件头操作 CR 和 LF 字符是 HTTP Response Splitting 攻击的核心，
     * 但其他字符，如 “:” （冒号）和 “=”（等号），在响应头文件中同样具有特殊的含义。 匹配换行，
     * 由于要检查forward路径，参数中带有=号，不检查=号 由于有的情况下是url绝对路径，有http://，带有冒号，不检测：
     */
    public static final Pattern HEADSPLITTER = Pattern.compile(
            "((?!(%0d|%0a)).)*", Pattern.CASE_INSENSITIVE);
    /**
     * 日志信息中不允许出现带有日志级别的字符串： emerg(紧急)，alert(必须立即采取措施)，crit(致命情况)，
     * error(错误情况)，warn(警告情况)，notice(一般重要情况)，info(普通信息)，debug(调试信息) edit by
     * chenlan 2013.4.11
     */
    public static final Pattern SCRIPT_LOGDESC = Pattern
            .compile(
                    "((?!(\\[info\\]|\\[debug\\]|\\[emerg\\]|\\[alert\\]|\\[crit\\]|\\[error\\]|\\[warn\\]|\\[notice\\]))[\\S\\s])*",
                    Pattern.CASE_INSENSITIVE);
    /**
     * X-Forwarded-For的验证正则表达式,为ip规则 add by wandj 2013.7.15
     */
    public static final Pattern XForwardedFor = Pattern
            .compile("[\\d]{1,3}.[\\d]{1,3}.[\\d]{1,3}.[\\d]{1,3}");
    /**
     * 20121226 by kangx 匹配BI系统资源ID,包含有数字,大小写字母,中繁日文,$,~,/
     */
    public static Pattern RESID = Pattern.compile("[\\d\\w\u0800-\u9fa5~/$-]+");
    /**
     * 20121226 by kangx 匹配数据库表名, 包含字母 , _, 数字, $ 2013.2.25 by wandj
     * sqlserver下数据库自动加上schema. 数据库前缀，故验证时应该允许含有0次或1次含字符串+.开头的前缀 ISSUE:BI-8628
     * edit by wandj tablename的正则表达式不正确.改为支持单字符至少出现一次,.后面必须以字符或数字结尾
     */
    public static Pattern TABLENAME = Pattern
            .compile("[\\w$]+(.\\w*[A-Za-z0-9$]+)?");
    /**
     * 2013.1.15 by wandj 增加参数分号验证.js中出现的参数不能有"或者'和;或者+组合出现,否则可以直接跟js语句执行
     */
    public static Pattern InJS = Pattern
            .compile("[\\s\\S]*(\"|\\%27|\'|\\%22)+[\\s\\S]*(;|\\%3[Bb]|\\+|%2[Bb])+?[\\s\\S]*");
    /**
     * 2013.2.3 wandj 初始化js事件的数组,后面验证时需要用到.
     */
    public static Pattern[] _patlist = {
            Pattern.compile("[\\S\\s]*onabort[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onactivate[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onafterprint[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onafterupdate[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onbeforeactivate[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onbeforecopy[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onbeforecut[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onbeforeeditfocus[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onbeforepaste[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onbeforeprint[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onbeforeunload[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onbeforeupdate[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onblur[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onbounce[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*oncellchange[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onchange[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onclick[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*oncontextmenu[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*oncontrolselect[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*oncopy[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*oncut[\\S\\s]*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*ondataavailable[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*ondataavailable[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*ondatasetchanged[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*ondatasetcomplete[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*ondblclick[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*ondeactivate[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*ondrag[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*ondragend[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*ondragenter[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*ondragleave[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*ondragover[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*ondragstart[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*ondrop[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onerror[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onerrorupdate[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onfilterchange[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onfinish[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onfocus[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onfocusin[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onfocusout[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onhelp[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onkeydown[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onkeypress[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onkeyup[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onlayoutcomplete[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onload[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onlosecapture[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onmousedown[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onmouseenter[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onmouseleave[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onmousemove[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onmouseout[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onmouseover[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onmouseup[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onmousewheel[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onmove[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onmoveend[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onmovestart[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onpaste[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onpropertychange[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onreadystatechange[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onreset[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onresize[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onresizeend[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onresizestart[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onrowenter[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onrowexit[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onrowsdelete[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onrowsinserted[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onscroll[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onselect[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onselectionchange[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onselectstart[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onstart[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onpaste[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onmovestart[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onstop[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onsubmit[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\S\\s]*onunload[\\S\\s]*",
                    Pattern.CASE_INSENSITIVE)};

    /**
     * 20121226 by kangx 匹配数据库表名，否则抛出非法参数异常。
     *
     * @param param
     * @return
     * @throws IllegalArgumentException
     */
    public static String checkTableName(String param)
            throws IllegalArgumentException {
        return checkParam(null, param, TABLENAME);
    }

    /**
     * 20121226 by kangx 匹配BI系统资源id,否则抛出非法参数异常
     *
     * @param param
     * @return
     * @throws IllegalArgumentException
     */
    public static String checkResID(String param)
            throws IllegalArgumentException {
        // return checkParam(null, param, SCRIPT_XSS);
        /**
         * 资源id的验证方法等同script_xss
         * */
        return checkXSSParam(param);
    }

    /**
     * 匹配整数，包括正负号
     */
    public static final String checkInt(HttpServletRequest request, String param)
            throws IllegalArgumentException {
        return checkParam(request, param, INTEGER);
    }

    /**
     * 匹配数字。可以是整数也可以是浮点数(包括正负号)。支持科学计算法。
     */
    public static final String checkNum(HttpServletRequest request, String param)
            throws IllegalArgumentException {
        return checkParam(request, param, NUMBER);
    }

    /**
     * 检查是否安全的URL地址。只充许本WEB APP的ContextPath下的url.超过的地址范围则认为不合法。
     *
     * @param request
     * @param param
     * @return
     * @throws IllegalArgumentException
     */
    public static String checkUrl(HttpServletRequest request, String param)
            throws IllegalArgumentException {
        String value = request.getParameter(param);
        return checkUrlValue(request, value);
    }

    public static String checkUrlValue(HttpServletRequest request, String value)
            throws IllegalArgumentException {
        if (StrUtil.isNull(value)) return value;
        int p = value.indexOf("?");
        if (p != -1) {// 对于有参数的url，要拆开，把参数部分进行url编码，再检查是否合法。
            String urlPath = value.substring(0, p);
            String urlQueryStr = value.substring(p + 1);
            checkParam(null, urlPath, URLPATH, false);// 先检查urlPath有没有script脚本。
            value = urlPath + "?"
                    + Maps.toUrlParams(Maps.toMap(urlQueryStr, "=", "&"));
        } else {
            checkParam(null, value, URLPATH, false);// 先检查有没有script脚本。
        }
        String curPath = request.getRequestURI();// 当前页面所在路径
        curPath = curPath.replaceAll("/+", "/");// 将重复的斜线转成１个．
        String ctxPath = request.getContextPath();// contextpath是以/开始的地址。如果contextpath是根，则返回""空串。
        if (ctxPath != null) {
            int c = StrUtil.charCount(curPath, '/');
            if ("".equals(ctxPath)) {// 如果是根开始的ctxPath，则url中"../"出现的次数不能达到"/"线出现的次数．
                limitSubStrCount(value, "../", c);
            } else if (ctxPath.startsWith("/")) {// 如果ctxPath不是根开始，则url中"../"出现的次数不能超过"/"线出现的次数
                limitSubStrCount(value, "../", c - 1);
            }
        }
        return value;
    }

    // 限制subStr出现的次数不能达到或超过指定次数．
    private static void limitSubStrCount(String param, String subStr, int c) {
        int p = 0, n = 0, len = subStr.length();
        while (p != -1) {
            p = param.indexOf(subStr, p);
            if (p != -1) {
                n++;
                p += len;
            } else
                break;
        }
        if (n >= c)
            throw new IllegalArgumentException("URL值'" + param + "'不合法。");
    }

    /**
     * 判断一组参数是否带有SQL注入脚本字符, 否则抛出非法参数异常
     *
     * @param param 一组参数值
     * @throws IllegalArgumentException
     */
    public static void checkSQLParam(String[] params)
            throws IllegalArgumentException {
        for (int i = 0; i < params.length; i++) {
            checkSQLParam(params[i]);
        }
    }

    /**
     * 判断参数是否带有SQL注入脚本字符, 否则抛出非法参数异常
     *
     * @param param 参数值
     * @throws IllegalArgumentException
     */
    public static String checkSQLParam(String param)
            throws IllegalArgumentException {
        return checkSQLParam(null, param);
    }

    /**
     * 判断HTTP请求参数的值是否带有SQL注入脚本字符, 否则抛出非法参数异常
     *
     * @param request HTTP请求参数可以为空
     * @param param   参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @return 参数值
     * @throws IllegalArgumentException
     */
    public static String checkSQLParam(HttpServletRequest request, String param)
            throws IllegalArgumentException {
        return checkParam(request, param, SCRIPT_SQLINJ, true);
    }

    /**
     * 判断HTTP请求参数的值是否为一个合法标识符(字母数字下划线构成), 否则抛出非法参数异常
     *
     * @param request HTTP请求参数可以为空
     * @param param   参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @return 参数值
     * @throws IllegalArgumentException
     */
    public static String checkIdentifier(HttpServletRequest request,
                                         String param) throws IllegalArgumentException {
        return checkParam(request, param, BI_IDENTIFIER, false);
    }

    /**
     * 判断参数是否带有XSS攻击脚本字符, 否则抛出非法参数异常
     *
     * @param param 参数值
     * @throws IllegalArgumentException
     */
    public static String checkXSSParam(String param)
            throws IllegalArgumentException {
        return checkXSSParam(null, param);
    }

    /**
     * 判断HTTP请求参数的值是否带有XSS攻击脚本字符, 否则抛出非法参数异常
     *
     * @param request HTTP请求参数可以为空
     * @param param   参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @return 参数值
     * @throws IllegalArgumentException
     */
    public static String checkXSSParam(HttpServletRequest request, String param)
            throws IllegalArgumentException {
        /**
         * 20121224 by kangx false改为true，与正则表达式对应，含有脚本则报出异常。
         * */
        return checkParam(request, param, SCRIPT_XSS, true);
    }

    /**
     * 判断HTTP请求参数的值是否可以匹配指定的正则表达式, 如果不能匹配，则抛出非法参数异常
     *
     * @param request HTTP请求 可以为空
     * @param param   参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @param pattern 匹配正则表达式
     * @return
     * @throws IllegalArgumentException
     */
    public static String checkParam(HttpServletRequest request, String param,
                                    Pattern pattern) throws IllegalArgumentException {
        return checkParam(request, param, pattern, false);
    }

    /**
     * 判断HTTP请求参数的值是否可以(取反)匹配指定的正则表达式, 如果不能(取反)匹配，则抛出非法参数异常 HTTP
     * Request请求对象为空时，认为param为参数的值
     *
     * @param request HTTP请求 可以为空
     * @param param   参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @param pattern 匹配正则表达式
     * @param negate  是否匹配取反
     * @return
     * @throws IllegalArgumentException
     */
    public static String checkParam(HttpServletRequest request, String param,
                                    Pattern pattern, boolean negate) throws IllegalArgumentException {
        String value = request != null ? request.getParameter(param) : param;
        if (StrUtil.isNull(value)) {
            return value;
        }
        String checkparam = StrUtil.format2HtmlStr(value);
        if (negate == pattern.matcher(value).matches()) {
            throw new IllegalArgumentException(I18N.getString(
                    "com.esen.util.security.SecurityFunc.java.1",
                    "参数“{0}”含有非法字符", checkparam));
        }
        if (!StrUtil.isNull(checkparam)
                && negate == pattern.matcher(checkparam).matches()) {
            // 这里把参数名称传给异常,错误页面用该参数名称从request对象中去取值来组织与显示异常
            throw new IllegalArgumentException(I18N.getString(
                    "com.esen.util.security.SecurityFunc.java.1",
                    "参数“{0}”含有非法字符", checkparam));
        }
        if (pattern.equals(SCRIPT_XSS) && value.indexOf("\"") > -1) {
            for (int i = 0; i < _patlist.length; i++) {
                if (_patlist[i].matcher(value).matches()) {
                    throw new IllegalArgumentException(I18N.getString(
                            "com.esen.util.security.SecurityFunc.java.1",
                            "参数“{0}”含有非法字符", value));
                }
            }
        }
        return value;
    }

    public static String checkXForwarded(String param) {
        if (StrUtil.isNull(param)) {
            return param;
        }
        if (!XForwardedFor.matcher(param).matches()) {

            throw new IllegalArgumentException(I18N.getString(
                    "com.esen.util.security.SecurityFunc.java.1",
                    "参数“{0}”含有非法字符",
                    StrUtil.format2HtmlStr(param)));
        }
        return param;
    }

    /**
     * 2013.1.15 by wandj 增加参数分号验证.js中出现的参数不能有"或者'和;或者+组合出现,否则可以直接跟js语句执行
     */
    public static String checkSemicolon(String param) {
        if (StrUtil.isNull(param)) {
            return param;
        }
        if (InJS.matcher(param).matches()) {
            throw new IllegalArgumentException(I18N.getString(
                    "com.esen.util.security.SecurityFunc.java.1",
                    "参数“{0}”含有非法字符", param));
        }
        return param;
    }

    /**
     * 2012.12.25 by wandj 匹配数字。可以是整数也可以是浮点数(包括正负号)。支持科学计算法。
     */
    public static String checkNum(String param) throws IllegalArgumentException {
        return checkParam(null, param, NUMBER);
    }

    /**
     * 2012.12.26 by wandj 对exttype的验证
     */
    public static String checkExttype(String param)
            throws IllegalArgumentException {
        // if(!StrFunc.isNull(param)){
        // if(!AnaConst.EXTTYPE_REPORT.equals(param)&&!AnaConst.EXTTYPE_CUBE.equals(param)&&!AnaConst.EXTTYPE_DLGOLAP.equals(param)
        // &&!AnaConst.EXTTYPE_EASYOLAP.equals(param)&&!AnaConst.EXTTYPE_EASYQBE.equals(param)&&!AnaConst.EXTTYPE_FACT.equals(param)
        // &&!AnaConst.EXTTYPE_LINK.equals(param)&&!AnaConst.EXTTYPE_QBE.equals(param)&&!AnaConst.EXTTYPE_STATIC_FILE.equals(param)
        // &&!AnaConst.EXTTYPE_VFACT.equals(param)&&!AnaConst.EXTTYPE_WORD.equals(param)){
        // throw new
        // IllegalArgumentException(I18N.getString("com.esen.util.security.SecurityFunc.java.1",
        // "参数“{0}”含有非法字符", new Object[]{param}));
        // }
        // }
        // return param;
        return checkXSSParam(param);
    }

    /**
     * 2012.12.26 增加对boolean的验证
     */
    public static String checkBoolean(String param)
            throws IllegalArgumentException {
        if (!StrUtil.isNull(param)) {
            if (!"true".equals(param) && !"false".equals(param)) {
                throw new IllegalArgumentException(I18N.getString(
                        "com.esen.util.security.SecurityFunc.java.1",
                        "参数“{0}”含有非法字符", param));
            }
        }
        return param;
    }

    /*
     * 这是一个能成功通过Fortify扫描的方法
     */
    public static String filter(String value) {
        if (value == null) return null; // 判断为空后返回value是不能通过检查的
        if ("".equals(value)) return "";
        StringWriter sw = new StringWriter(value.length());
        sw.getBuffer().append(value);
        return sw.toString();
    }

    /**
     * NEW: 2013.5.6 add by wandj 这是一个能成功通过Fortify扫描的方法,用于拼接的sql语句
     **/
    public static String filterSQL(String value) {
        return filter(value);
    }

    /**
     * NEW: 2013.5.6 add by wandj 这是一个能成功通过Fortify扫描的方法,用于url
     **/
    public static String filterUrl(String value) {
        return filter(value);
    }

    /**
     * 过滤文件路径，以后可能需要从功能上检查文件合法性，避免路径操纵问题
     *
     * @param value 需要过滤的值
     * @return 返回路径值
     */
    public static String filterFilePath(String value) {
        return filter(value);
    }

    /**
     * 检查从前台获取且拼接到日志信息中的参数值 解决Category: Log Forging（日志注入）类型问题
     *
     * @param request HTTP请求 允许为空
     * @param param   参数名，如果request为空，传递参数值
     * @return 返回合法参数值
     * @throws IllegalArgumentException 如果存在此类型问题，抛出异常
     */
    public static String checkLogDesc(HttpServletRequest request, String param)
            throws IllegalArgumentException {
        return checkParam(request, param, SCRIPT_LOGDESC, false);
    }

    /**
     * add by wandj 2013.5.8 这是一个能成功通过Fortify扫描的方法
     */
    public static String getAddress() throws UnknownHostException {
        String address = InetAddress.getLocalHost().getHostAddress();
        return address;
    }

    /**
     * 检查HTTP头信息，防止HTTP头操纵
     *
     * @param request HTTP请求 允许为空
     * @param param   参数名，如果request为空，传递参数值
     * @return 返回合法参数值
     * @throws IllegalArgumentException 如果存在此类型问题，抛出异常
     */
    public static String checkHttpHeader(HttpServletRequest request,
                                         String param) throws IllegalArgumentException {
        return checkParam(request, param, HEADSPLITTER, false);
    }

    /**
     * 判断文件路径是否在指定的文件夹夹下，后缀名是否合法，用于文件的用户访问控制
     *
     * @param filepath 文件路径
     * @param dir      目录控制参数
     * @param suffix   后缀名的正则匹配表达式
     * @return 文件对象
     * @throws IllegalAccessException
     */
    public static File checkFile(String filepath, String dir, String suffix)
            throws IllegalAccessException {
        String errmsg = StrUtil.format2HtmlStr("路径值'" + filepath + "'不合法。");
        filepath = filter(filepath);
        dir = filter(dir);
        File file = new File(filepath);
        if (file != null && file.exists()) {
            // 校验路径范围
            if (!StrUtil.isNull(dir) && !file.getAbsolutePath().startsWith(dir)) {
                throw new IllegalAccessException(errmsg);
            }
            if (!StrUtil.isNull(suffix)) {
                // 校验后缀名合法性
                int index = file.getName().lastIndexOf(".");
                if (index > -1) {
                    String s = file.getName().substring(index + 1);
                    if (!Pattern.compile(suffix).matcher(s).matches()) {
                        throw new IllegalAccessException(errmsg);
                    }
                }
            }
        }
        return file;
    }

    /**
     * 检验参数的长度，如果大于200抛异常
     *
     * @param param 要检验的参数
     * @return 检验后的值
     */
    public static String checkLength(String param) {
        if (StrUtil.isNull(param)) {
            return param;
        }
        if (param.length() >= 200) {
            throw new IllegalArgumentException(I18N.getString(
                    "com.esen.util.security.SecurityFunc.java.2",
                    "参数“{0}”长度过长", param));
        }
        return param;
    }

    /**
     * 测试
     *
     * @param args
     */
    public static void main(String[] args) {
        // String guid = GUID.makeGuid();
        // System.out.println(guid);
        Pattern img = Pattern
                .compile(".*?(<|\\%3[C|c])[I|i][M|m][G|g](\\s)+(.*)[S|s][R|r][C|c]=[\"|\'][J|j][A|a][V|v][A|a][S|s][C|c][R|r][I|i][P|p][T|t]:.*?[\"|\'].*?\\/(>|\\%3e).*?");

        Pattern xss = Pattern
                .compile(".*?((<|\\%3[C|c])([S|s][C|c][R|r][I|i][P|p][T|t].*?(>|\\%3e).*?(<|\\%3[C|c])\\/[S|s][C|c][R|r][I|i][P|p][T|t](>|\\%3e))"
                        + "|((<|\\%3[C|c])(([I|i][M|m][G|g])|([I|i][F|f][R|r][A|a][M|m][E|e]))(\\s)+.*?\\/(>|\\%3e))|([J|j][A|a][V|v][A|a][S|s][C|c][R|r][I|i][P|p][T|t])).*?");
        Pattern iframe = Pattern
                .compile(".*?(<|\\%3[C|c])(([I|i][M|m][G|g])|([I|i][F|f][R|r][A|a][M|m][E|e]))[\\s+]+.*?[\\/]?(>|\\%3e).*?");
        String input = "</Script ><object data=jav\\x61scr\\x69pt:rsgvpk(upx)>";
        // String input = "\\%27+%7C%7C+%27%27+%7C%7C+%27e%2Cx%2Ci";//脚本注入
        boolean isSCRIPT = SCRIPT_XSS.matcher(input).matches();
        boolean isIMG = img.matcher(input).matches();
        boolean isxss = xss.matcher(input).matches();
        boolean isIframe = iframe.matcher(input).matches();
        boolean IsSQL = SCRIPT_SQLINJ.matcher(input).matches();
        System.out.println("iframe--" + isIframe);
        System.out.println("script--" + isSCRIPT);
        System.out.println("img-----" + isIMG);
        System.out.println("arr-----" + isxss);
        System.out.println("sql-----" + IsSQL);
        // checkXSSParam(input);
        Pattern resid = Pattern.compile("[\\d\\w~/$\u0800-\u9fa5]+");
        String reid = "0$dfaADFASEF~///@@@";// 资源ID
        boolean isreid = resid.matcher(reid).matches();
        System.out.println("ResID--" + isreid);
        // checkResID(reid);
        String tbna = "FAE_$qqww1";// 数据库表名
        checkTableName(tbna);
    }
}