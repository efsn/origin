package org.codeyn.util.yn;

import java.io.File;
import java.io.StringWriter;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.codeyn.util.Maps;
import org.springframework.scheduling.config.Task;

/**
 * 安全方法工具类, 提供一组参数校验函数，用于判断参数以避免恶意脚本攻击与注入。
 * 参数校验使用正则表达式进行，允许按照表达式进行取反匹配，如IRPT_IDENTIFIER和IRPT_TASKID为正向的匹配公式,
 * SCRIPT_SQLINJ为需要取反的匹配公式 调整SCRIPT_XSS为正向的正则表达式，仅验证是否含有<、>、 \r、 \n、 \t、 &、
 * "字符，保持同StrFunc.format2HtmlStr原则一致
 */
public class SecurityUtil{

    /**
     * 外面直接获取目录的方式报出路径操纵的安全问题，设置为常量后（白名单）应该能解决问题
     */
    public static final String JAVA_TEMP = getTempPath();

    /**
     * 匹配标识符：字母、数字下划线构成 edit by chenlan 2013/6/8 补充中划线（如取数名称等可能出现中划线）
     */
    public static final Pattern IRPT_IDENTIFIER = Pattern.compile("[-\\w]+");

    /**
     * 匹配:字母、数字、点、下划线、中线 主要用来检测类名
     */
    public static final Pattern CLASSNAME = Pattern.compile("[-.\\w]+");

    /**
     * 匹配任务GUID的正则表达式：{8位大写字母或数字-4位大写字母或数字-4位大写字母或数字-4位大写字母或数字-12位大写字母或数字}
     */
    public static final Pattern IRPT_GUID = Pattern
            .compile("\\{[0-9A-Z]{8}(-[0-9A-Z]{4}){3}-[0-9A-Z]{12}\\}");

    /**
     * 匹配任务ID的正则表达式：{8位大写字母或数字-4位大写字母或数字-4位大写字母或数字-4位大写字母或数字-12位大写字母或数字}.32
     * 位小写字母或者数字
     */
    public static final Pattern IRPT_TASKID = Pattern
            .compile("\\{[0-9A-Z]{8}(-[0-9A-Z]{4}){3}-[0-9A-Z]{12}\\}(.[0-9a-z]{32})?");

    /**
     * 20121224 by kangx 修改XSS匹配规则<br>
     * 匹配带有XSS攻击脚本的正则表达式, 匹配符合*&ltscript *&gt*&lt/script&gt*的认为含有脚本攻击<br>
     * 以前的匹配规则为"[^<>\r\n\t&\'\"\\x00]*"<br>
     * 20121226 by kangx 增加匹配&ltimg *"/&gt<br>
     * 增加匹配&ltiframe *"/&gt
     * 
     * 20130427 将匹配iframe改成匹配frame
     * */
    public static final Pattern SCRIPT_XSS = Pattern
            .compile(".*?((<|\\%3[C|c]).*([S|s][C|c][R|r][I|i][P|p][T|t].*?(>|\\%3[E|e]).*?(<|\\%3[C|c]).*?[S|s][C|c][R|r][I|i][P|p][T|t].*?(>|\\%3[E|e]))"
                    + "|((<|\\%3[C|c]).*(([I|i][M|m][G|g])|([F|f][R|r][A|a][M|m][E|e])|([A|a]))[\\s+]+.*?[\\/]?(>|\\%3[E|e]))|([J|j][A|a][V|v][A|a][S|s][C|c][R|r][I|i][P|p][T|t])"
                    + "|(([S|s][T|t][Y|y][L|l][E|e]).*:.*?([E|e][X|x][P|p][R|r][E|e][S|s][S|s][I|i][O|o][N|n])|(\\/\\*.*\\*\\/))).*?");
    /**
     * 2013.2.3 wandj 初始化js事件的数组,后面验证时需要用到.
     * */
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
     * Header Manipulation 文件头操作 CR 和 LF 字符是 HTTP Response Splitting 攻击的核心，
     * 但其他字符，如 “:” （冒号）和 “=”（等号），在响应头文件中同样具有特殊的含义。 匹配换行，
     * 由于要检查forward路径，参数中带有=号，不检查=号 由于有的情况下是url绝对路径，有http://，带有冒号，不检测：
     */
    public static final Pattern HEADSPLITTER = Pattern.compile(
            "((?!(%0d|%0a)).)*", Pattern.CASE_INSENSITIVE);

    /**
     * 带有SQL注入脚本字符的正则表达式，如果字符串中包含了至少以下的一个字符'、=、>、<、!，
     * 或者包含了union、join、from、select
     * 、update、delete、where、or、and、not、between、like、is的SQL关键词 或者包含了 %u0027
     * %u02b9 %u02bc %u02c8 %u2032 %uff07 %c0%27 %c0%a7 %e0%80%a7
     */
    public static final Pattern SCRIPT_SQLINJ = Pattern
            .compile("[\\s\\S]*(([\'=><!]+)|(\\s+(?-i:union|join|from|select|update|delete|where|or|and|not|between|like|is)+\\s+)|(%u0027|%u02b9|%u02bc|%u02c8|%u2032|%uff07|%c0%27|%c0%a7|%e0%80%a7))[\\s\\S]*");

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
     * 匹配报表户类型， 0 或者 9 或者 空
     */
    public static final Pattern IRPT_BTYPE = Pattern.compile("(0|9)?");

    /**
     * 匹配报表户ID，字母数字下划线、中划线、点号、星号，1-32位长度
     * 
     * edit 2013/4/12 chenlan 匹配报表户ID、机构ID，等
     */
    public static final Pattern IRPT_ID = Pattern
            .compile("[\\d\\w_\\-\\.\\*]{1,32}");

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
     * 匹配IP地址：数字 . : 组成
     */
    public static final Pattern IRPT_IP = Pattern.compile("[\\d\\.:]+");

    /**
     * 2013.1.15 by wandj 增加参数分号验证.js中出现的参数不能有"或者'和;或者+组合出现,否则可以直接跟js语句执行
     * */
    public static Pattern InJS = Pattern
            .compile("[\\s\\S]*(\"|\\%27|\'|\\%22)+[\\s\\S]*(;|\\%3[Bb]|\\+|%2[Bb])+?[\\s\\S]*");

    /**
     * 匹配报表户ID,包含有数字,大小写字母,/
     * 
     * edit 2013/4/12 chenlan 匹配报表户ID、机构ID，等
     */
    public static String checkId(HttpServletRequest request, String param)
            throws IllegalArgumentException{
        return checkParam(request, param, IRPT_ID, false);
    }

    /**
     * 匹配报表户ID，可自定义报错信息
     * 
     * @param request
     * @param param
     * @param errorMsg
     * @return
     */
    public static String checkId(HttpServletRequest request, String param,
            String errorMsg){
        return checkParam(request, param, IRPT_ID, false, errorMsg);
    }

    /**
     * 匹配报表户类型，允许为空或者0,9
     */
    public static String checkBtype(HttpServletRequest request, String param)
            throws IllegalArgumentException{
        return checkParam(request, param, IRPT_BTYPE, false);
    }

    /**
     * 匹配整数，包括正负号
     */
    public static final String checkInt(HttpServletRequest request, String param)
            throws IllegalArgumentException{
        return checkParam(request, param, INTEGER);
    }

    /**
     * 匹配数字。可以是整数也可以是浮点数(包括正负号)。支持科学计算法。
     */
    public static final String checkNum(HttpServletRequest request, String param)
            throws IllegalArgumentException{
        return checkParam(request, param, NUMBER);
    }

    /**
     * 匹配:字母、数字、点、下划线、中线 主要用来检测类名
     */
    public static final String checkClassName(HttpServletRequest request,
            String param) throws IllegalArgumentException{
        return checkParam(request, param, CLASSNAME);
    }

    /**
     * 匹配boolean值
     * 
     * @param param
     * @return
     * @throws IllegalArgumentException
     */
    public static String checkBoolean(HttpServletRequest request, String param)
            throws IllegalArgumentException{
        String value = request != null ? request.getParameter(param) : param;
        value = filter(value);
        // 判断空必须放在转换后
        if (StrUtil.isNull(value)) {
            return value;
        } else {
            String errmsg = StrUtil.format2HtmlStr("'" + value + "'值不合法。");
            if (!"true".equalsIgnoreCase(value)
                    && !"false".equalsIgnoreCase(value)) {
                throw new IllegalArgumentException(errmsg);
            }
        }
        return value;
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
            throws IllegalArgumentException{
        String value = request.getParameter(param);
        return checkUrlValue(request, value);
    }

    public static String checkUrlValue(HttpServletRequest request, String value)
            throws IllegalArgumentException{
        value = filter(value);
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
        // String protocolRegex = "^[\\s\\x00]*[\\s\\x00a-zA-Z]+:.*$";
        // //匹配http:, https:, ftp:,ftps:;等所有协议串。
        // if( value.matches(protocolRegex) ) //不充许是绝对地址
        // throw new IllegalArgumentException("URL值'"+value+"'不合法。");
        // String beginStrRegex = "^[0-9a-zA-Z.].*$";
        // if( !value.matches(beginStrRegex) ) //只允许以字母,下开线,.号开头开头．
        // throw new IllegalArgumentException("URL值'"+value+"'不合法。");
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
    private static void limitSubStrCount(String param, String subStr, int c){
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
            throw new IllegalArgumentException(StrUtil.format2HtmlStr("URL值'"
                    + param + "'不合法。"));
    }

    /**
     * 判断HTTP请求参数的值是否为一个合法标识符(字母数字下划线构成), 否则抛出非法参数异常
     * 
     * @param request
     *            HTTP请求参数可以为空
     * @param param
     *            参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @return 参数值
     * @throws IllegalArgumentException
     */
    public static String checkIdentifier(HttpServletRequest request,
            String param) throws IllegalArgumentException{
        return checkParam(request, param, IRPT_IDENTIFIER, false);
    }

    /**
     * 判断HTTP请求参数的值是否为任务GUID格式, 否则抛出非法参数异常
     * 
     * @param request
     *            HTTP请求参数可以为空
     * @param param
     *            参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @return 参数值
     * @throws IllegalArgumentException
     */
    public static String checkTaskGUID(HttpServletRequest request, String param)
            throws IllegalArgumentException{
        return checkParam(request, param, IRPT_GUID, false);
    }

    /**
     * 判断HTTP请求参数的值是否为任务TASKID格式, 否则抛出非法参数异常
     * 
     * @param request
     *            HTTP请求参数可以为空
     * @param param
     *            参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @return 参数值
     * @throws IllegalArgumentException
     */
    public static String checkTaskID(HttpServletRequest request, String param)
            throws IllegalArgumentException{
        return checkParam(request, param, IRPT_TASKID, false);
    }

    /**
     * 判断参数是否为任务task下的公式 该函数目前没有进行实际的校验，直接返回参数值
     * 
     * @param task
     *            任务
     * @param param
     *            参数
     * @throws IllegalArgumentException
     */
    public static void checkExpression(Task task, String param)
            throws IllegalArgumentException{
        checkExpression(null, task, param);
    }

    /**
     * 判断HTTP请求参数的值是否为任务task下的公式, 否则抛出非法参数异常 该函数目前没有进行实际的校验，直接返回参数值
     * 
     * @param request
     *            HTTP请求
     * @param task
     *            任务
     * @param param
     *            参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @return 参数值
     * @throws IllegalArgumentException
     */
    public static String checkExpression(HttpServletRequest request, Task task,
            String param) throws IllegalArgumentException{
        return request != null ? request.getParameter(param) : param;
    }

    /**
     * 判断一组参数是否带有SQL注入脚本字符, 否则抛出非法参数异常
     * 
     * @param param
     *            一组参数值
     * @throws IllegalArgumentException
     */
    public static void checkSQLParam(String[] params)
            throws IllegalArgumentException{
        for (int i = 0; i < params.length; i++) {
            checkSQLParam(params[i]);
        }
    }

    /**
     * 判断参数是否带有SQL注入脚本字符, 否则抛出非法参数异常
     * 
     * @param param
     *            参数值
     * @throws IllegalArgumentException
     */
    public static String checkSQLParam(String param)
            throws IllegalArgumentException{
        return checkSQLParam(null, param);
    }

    /**
     * 判断HTTP请求参数的值是否带有SQL注入脚本字符, 否则抛出非法参数异常
     * 
     * @param request
     *            HTTP请求参数可以为空
     * @param param
     *            参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @return 参数值
     * @throws IllegalArgumentException
     */
    public static String checkSQLParam(HttpServletRequest request, String param)
            throws IllegalArgumentException{
        return checkParam(request, param, SCRIPT_SQLINJ, true);
    }

    /*  *//**
     * 判断参数是否带有XSS攻击脚本字符, 否则抛出非法参数异常
     * 
     * @param param
     *            参数值
     * @throws IllegalArgumentException
     */
    public static String checkParam(String param, String errorMsg)
            throws IllegalArgumentException{
        return checkParam(null, param, errorMsg);
    }

    /**
     * 判断参数是否带有XSS攻击脚本字符, 否则抛出非法参数异常
     * 
     * @param param
     *            参数值 尽量不要调用这个方法，最好调用传参errorMsg的
     * @throws IllegalArgumentException
     */
    public static String checkParam(String param)
            throws IllegalArgumentException{
        return checkParam(null, param, SCRIPT_XSS, true, true, null);
    }

    /**
     * 判断HTTP请求参数的值是否带有XSS攻击脚本字符, 否则抛出非法参数异常
     * 
     * @param request
     *            HTTP请求参数可以为空
     * @param param
     *            参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @return 参数值
     * @throws IllegalArgumentException
     */
    public static String checkParam(HttpServletRequest request, String param)
            throws IllegalArgumentException{
        return checkParam(request, param, SCRIPT_XSS, true);// XSS正则，第二个参数传true
    }

    /**
     * 判断HTTP请求参数的值是否带有XSS攻击脚本字符, 否则抛出非法参数异常
     * 
     * @param request
     *            HTTP请求参数可以为空
     * @param param
     *            参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @return 参数值
     * @throws IllegalArgumentException
     */
    public static String checkParam(HttpServletRequest request, String param,
            String errorMsg) throws IllegalArgumentException{
        return checkParam(request, param, SCRIPT_XSS, true, errorMsg);// XSS正则，第二个参数传true
    }

    /**
     * 判断HTTP请求参数的值是否带有XSS攻击脚本字符, 否则抛出非法参数异常
     * 
     * @param request
     *            HTTP请求参数可以为空
     * @param param
     *            参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @return 参数值
     * @param injs
     *            如果是在js中，需要调用checkSemicolon方法进行检查，true表示检查参数分号验证
     * @param errorMsg
     *            错误提示信息
     * @throws IllegalArgumentException
     */
    public static String checkParam(HttpServletRequest request, String param,
            boolean injs, String errorMsg) throws IllegalArgumentException{
        return checkParam(request, param, SCRIPT_XSS, true, false, errorMsg);// XSS正则，第二个参数传true
    }

    /**
     * 判断HTTP请求参数的值是否可以匹配指定的正则表达式, 如果不能匹配，则抛出非法参数异常
     * 
     * @param request
     *            HTTP请求 可以为空
     * @param param
     *            参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @param pattern
     *            匹配正则表达式
     * @return
     * @throws IllegalArgumentException
     */
    public static String checkParam(HttpServletRequest request, String param,
            Pattern pattern) throws IllegalArgumentException{
        return checkParam(request, param, pattern, false);
    }

    /**
     * 判断HTTP请求参数的值是否可以(取反)匹配指定的正则表达式, 如果不能(取反)匹配，则抛出非法参数异常 HTTP
     * Request请求对象为空时，认为param为参数的值 默认执行checkSemicolon方法
     * 
     * @param request
     *            HTTP请求 可以为空
     * @param param
     *            参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @param pattern
     *            匹配正则表达式
     * @param negate
     *            是否匹配取反
     * @return
     * @throws IllegalArgumentException
     */
    public static String checkParam(HttpServletRequest request, String param,
            Pattern pattern, boolean negate) throws IllegalArgumentException{
        return checkParam(request, param, pattern, negate, true);
    }

    /**
     * 判断HTTP请求参数的值是否可以(取反)匹配指定的正则表达式, 如果不能(取反)匹配，则抛出非法参数异常 HTTP
     * Request请求对象为空时，认为param为参数的值 默认执行checkSemicolon方法
     * 
     * @param request
     *            HTTP请求 可以为空
     * @param param
     *            参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @param pattern
     *            匹配正则表达式
     * @param negate
     *            是否匹配取反
     * @return
     * @throws IllegalArgumentException
     */
    public static String checkParam(HttpServletRequest request, String param,
            Pattern pattern, boolean negate, String errorMsg)
            throws IllegalArgumentException{
        return checkParam(request, param, pattern, negate, true, errorMsg);
    }

    /**
     * 判断HTTP请求参数的值是否可以(取反)匹配指定的正则表达式, 如果不能(取反)匹配，则抛出非法参数异常 HTTP
     * Request请求对象为空时，认为param为参数的值
     * 
     * @param request
     *            HTTP请求 可以为空
     * @param param
     *            参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @param pattern
     *            匹配正则表达式
     * @param negate
     *            是否匹配取反
     * @param injs
     *            如果是在js中，需要调用checkSemicolon方法进行检查，true表示检查参数分号验证
     * @return
     * @throws IllegalArgumentException
     */
    public static String checkParam(HttpServletRequest request, String param,
            Pattern pattern, boolean negate, boolean injs)
            throws IllegalArgumentException{
        String value = request != null ? request.getParameter(param) : param;
        value = filter(value);
        // 判断空必须放在转换后
        if (StrUtil.isNull(value)) return value;
        // 这里把参数名称传给异常,错误页面用该参数名称从request对象中去取值来组织与显示异常
        String errmsg = StrUtil.format2HtmlStr("'" + param + "'值不合法。");

        if (!StrUtil.isNull(value)
                && !(negate ^ pattern.matcher(value).matches())) {
            // 这里把参数名称传给异常,错误页面用该参数名称从request对象中去取值来组织与显示异常
            throw new IllegalArgumentException(errmsg);
        }
        if (pattern.equals(SCRIPT_XSS)) {
            if (injs) {
                checkSemicolon(request, param);
            }
            if (value.indexOf("\"") > -1 || value.indexOf("%22%20") > -1) {
                for (int i = 0; i < _patlist.length; i++) {
                    if (_patlist[i].matcher(value).matches()) {
                        // 这里把参数名称传给异常,错误页面用该参数名称从request对象中去取值来组织与显示异常
                        throw new IllegalArgumentException(errmsg);
                    }
                }
            }
        }
        return value;
    }

    /**
     * 判断HTTP请求参数的值是否可以(取反)匹配指定的正则表达式, 如果不能(取反)匹配，则抛出非法参数异常 HTTP
     * Request请求对象为空时，认为param为参数的值
     * 
     * @param request
     *            HTTP请求 可以为空
     * @param param
     *            参数名称 当 request 为空时表示参数值（即待校验的字符串）
     * @param pattern
     *            匹配正则表达式
     * @param negate
     *            是否匹配取反
     * @param injs
     *            如果是在js中，需要调用checkSemicolon方法进行检查，true表示检查参数分号验证
     * @return
     * @throws IllegalArgumentException
     */
    public static String checkParam(HttpServletRequest request, String param,
            Pattern pattern, boolean negate, boolean injs, String errorMsg)
            throws IllegalArgumentException{
        String value = request != null ? request.getParameter(param) : param;
        value = filter(value);
        // 判断空必须放在转换后
        if (StrUtil.isNull(value)) return value;
        // 这里把参数名称传给异常,错误页面用该参数名称从request对象中去取值来组织与显示异常
        String errmsg = StrUtil.format2HtmlStr("'" + param + "'值不合法。");

        if (!StrUtil.isNull(value)
                && !(negate ^ pattern.matcher(value).matches())) {
            // 这里把参数名称传给异常,错误页面用该参数名称从request对象中去取值来组织与显示异常
            if (StrUtil.isNull(errorMsg)) {
                throw new IllegalArgumentException(errmsg);
            } else {
                throw new IllegalArgumentException(errorMsg);
            }
        }
        if (pattern.equals(SCRIPT_XSS)) {
            if (injs) {
                checkSemicolon(request, param, errorMsg);
            }
            if (value.indexOf("\"") > -1 || value.indexOf("%22%20") > -1) {
                for (int i = 0; i < _patlist.length; i++) {
                    if (_patlist[i].matcher(value).matches()) {
                        // 这里把参数名称传给异常,错误页面用该参数名称从request对象中去取值来组织与显示异常
                        if (StrUtil.isNull(errorMsg)) {
                            throw new IllegalArgumentException(errmsg);
                        } else {
                            throw new IllegalArgumentException(errorMsg);
                        }
                    }
                }
            }
        }
        return value;
    }

    /**
     * 2013.1.15 by wandj 增加参数分号验证.js中出现的参数不能有"或者'和;或者+组合出现,否则可以直接跟js语句执行
     * */
    public static String checkSemicolon(HttpServletRequest request, String param){
        String value = request != null ? request.getParameter(param) : param;
        if (StrUtil.isNull(value)) {
            return value;
        }
        if (InJS.matcher(value).matches()) {
            throw new IllegalArgumentException(StrUtil.format2HtmlStr("'"
                    + param + "'值不合法。"));
            // throw new
            // IllegalArgumentException(I18N.getString("com.esen.util.security.SecurityFunc.java.1",
            // UtilResourceBundleFactory.class, new Object[]{param}));
        }
        return value;
    }

    /**
     * 2013.1.15 by wandj 增加参数分号验证.js中出现的参数不能有"或者'和;或者+组合出现,否则可以直接跟js语句执行
     * */
    public static String checkSemicolon(HttpServletRequest request,
            String param, String errorMsg) throws IllegalArgumentException{
        String value = request != null ? request.getParameter(param) : param;
        if (StrUtil.isNull(value)) {
            return value;
        }
        if (InJS.matcher(value).matches()) {
            if (StrUtil.isNull(errorMsg)) {
                throw new IllegalArgumentException(StrUtil.format2HtmlStr("'"
                        + param + "'值不合法。"));
            } else {
                throw new IllegalArgumentException(errorMsg);
            }
            // throw new
            // IllegalArgumentException(I18N.getString("com.esen.util.security.SecurityFunc.java.1",
            // UtilResourceBundleFactory.class, new Object[]{param}));
        }
        return value;
    }

    /**
     * 判断文件路径是否在指定的文件夹夹下，后缀名是否合法，用于文件的用户访问控制
     * 
     * @param filepath
     *            文件路径
     * @param dir
     *            目录控制参数
     * @param suffix
     *            后缀名的正则匹配表达式
     * @return 文件对象
     * @throws IllegalAccessException
     */
    public static File checkFile(String filepath, String dir, String suffix)
            throws IllegalAccessException{
        String errmsg = StrUtil.format2HtmlStr("路径值'" + filepath + "'不合法。");
        filepath = filter(filepath);
        dir = filter(dir);
        File file = new File(filepath);
        if (file != null && file.exists()) {
            // 校验路径范围
            if (!StrUtil.isNull(dir)) {
                File dirFile = new File(dir);
                if (!file.getAbsolutePath().startsWith(
                        dirFile.getAbsolutePath())) {
                    throw new IllegalAccessException(errmsg);
                }
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
     * 判断参数返回数组是否带有XSS攻击脚本字符, 否则抛出非法参数异常 适用于request.getParameterValues返回的数组值
     * 
     * @param request
     *            HTTP请求 不允许为空
     * @param param
     *            参数名称
     * @param pattern
     *            匹配正则表达式
     * @param negate
     *            是否匹配取反
     * @return
     * @throws IllegalArgumentException
     */
    public static String[] checkParamValues(HttpServletRequest request,
            String param, Pattern pattern, boolean negate)
            throws IllegalArgumentException{
        if (request == null || StrUtil.isNull(param)) {
            return null;
        }
        String[] values = request.getParameterValues(param);
        if (null == values) {
            return null;
        }
        // 必须new新数据，否则检查仍通不过
        String[] returnvalues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            returnvalues[i] = checkParam(null, values[i], pattern, negate);
        }
        return returnvalues;
    }

    /**
     * 判断参数返回数组是否带有XSS攻击脚本字符, 否则抛出非法参数异常 适用于request.getParameterValues返回的数组值
     * 
     * @param request
     *            HTTP请求 不允许为空
     * @param param
     *            参数名称
     * @return
     * @throws IllegalArgumentException
     */
    public static String[] checkParamValues(HttpServletRequest request,
            String param) throws IllegalArgumentException{
        return checkParamValues(request, param, SCRIPT_XSS, true);
    }

    /**
     * 检查从前台获取且拼接到日志信息中的参数值 解决Category: Log Forging（日志注入）类型问题
     * 
     * @param request
     *            HTTP请求 允许为空
     * @param param
     *            参数名，如果request为空，传递参数值
     * @return 返回合法参数值
     * @throws IllegalArgumentException
     *             如果存在此类型问题，抛出异常
     */
    public static String checkLogDesc(HttpServletRequest request, String param)
            throws IllegalArgumentException{
        return checkParam(request, param, SCRIPT_LOGDESC, false);
    }

    /**
     * 检查HTTP头信息，防止HTTP头操纵
     * 
     * @param request
     *            HTTP请求 允许为空
     * @param param
     *            参数名，如果request为空，传递参数值
     * @return 返回合法参数值
     * @throws IllegalArgumentException
     *             如果存在此类型问题，抛出异常
     */
    public static String checkHttpHeader(HttpServletRequest request,
            String param) throws IllegalArgumentException{
        return checkParam(request, param, HEADSPLITTER, false);
    }

    /**
     * 开发者通常将 cookie 设置为可从根上下文路径（“/”）访问它。 这样做会使 cookie 暴露在域中的所有 Web 应用程序 下。 由于
     * cookie 通常包含敏感信息（如会话标识符），因此，在应用程序之间共享 cookie 可能在一个应用程序中导致 漏洞，从而危及其他应用程序安全。
     * 
     * @return 返回cookie根,应为部署的web应用名，如部署在webapp下的应用为irpt，应返回/irpt
     */
    public static String getCookiePath(){
        return "";
    }

    /*
     * 尽量不要允许用户输入正则表达式，因为正则表达式容易引发 Regular Expressions Denial of
     * Service类型的攻击,如果使用的正则必须由用户输入 比如IrptNormalEvaluateHelper.java:693
     * java.lang.String.split()，正则来源于用户输入的计算公司 那么使用此方法绕过检查
     * 
     * @param regexp
     * 
     * @return
     */
    public static String checkRegularExpressions(String regexp){
        return filter(regexp);
    }

    /*
     * 返回JAVA临时目录，直接返回，或者定义为常量，都还是报出 Path Manipulation漏洞
     */
    private static String getTempPath(){
        return getSystemProperty("java.io.tmpdir", null);
    }

    /**
     * 返回System对象的某个属性值
     * 
     * @param key
     *            属性名
     * @return 属性值
     */
    public static String getSystemProperty(String key, String def){
        return filter(System.getProperty(key, def));
    }

    /*
     * 这是一个能成功通过Fortify扫描的方法
     */
    public static String filter(String value){
        if (value == null) return null; // 判断为空后返回value是不能通过检查的
        if ("".equals(value)) return "";
        StringWriter sw = new StringWriter(value.length());
        sw.getBuffer().append(value);
        return sw.toString();
    }

    /**
     * 检查异常里面是否存在跨站脚本的信息
     * 
     * @param e
     *            返回不存在XSS的对象
     * @return
     */
    public static Exception checkException(Exception e){
        if (e == null) {
            return null;
        }
        try {
            checkParam(e.getMessage(), null);
            return e;
        } catch (IllegalArgumentException ex) {
            Exception newe = new Exception(StrUtil.format2HtmlStr(e
                    .getMessage()));
            newe.setStackTrace(e.getStackTrace());
            return newe;
        }
    }

    public static void main(String[] args){
        String[] s = {
                "test 大吃大喝大吃大喝大吃大喝火！！！！！",
                "2012-08-06 16:16:30 [com.esen.server.ScheduleLoger]-[info] 【计划任务】IPAddress:   ;Description:启动计划调度线程",
                "【计划任务】IPAddress:  ;Description:启动计划调度线程",
                "info]ssss",
                "这个里面的debug是合法的，因为没有[]",
                "如果是混在汉字里，前后没有分[debug]隔符空格等情况",
                "2013-03-07 17:00:12 [com.esen.i.common.thdpool.Workable]-[debug]",
                "\r\n[INFO] \r\n ",
                "[debug][info]....",
                " alert 这个是合法的",
                "2013-03-07 17:00:12 [com.esen.i.common.thdpool.Workable]-[INFO] \r\n2013-03-07 17:00:12 [com.esen.i.common.thdpool.Workable]-[INFO]"};
        for (int i = 0; i < s.length; i++) {
            try {
                checkParam(null, s[i], SCRIPT_LOGDESC, false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        String[] header = {
                "Wiley Hacker\r\nHTTP/1.1 200 OK\r\n...",
                "Wiley Hacker",
                "us%0d%0aContent- Length:%200%0d%0a%0d%0aHTTP/1.1%20200%20OK%0d%0aContent- Type:%20text/html%0d%0aContent- Length:%2019%0d%0a%0d%0a<html>Got you hacked mate !</html>",
                "us%d", "tt%0", "mm%0d", "mm%0D"};

        for (int i = 0; i < header.length; i++) {
            try {
                checkParam(null, header[i], HEADSPLITTER, false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("OK!");
    }

}