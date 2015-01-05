package org.codeyn.util.yn;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.jsp.JspWriter;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.codeyn.util.exception.ExceptionHandler;
import org.codeyn.util.file.FileYn;

/**
 * 此类提供一些服务器端Web相关的Servlet和Jsp开发过程中常用的通用函数，但不包括Struts相关的
 * 1.处理异常，错误信息
 * 2.处理HTTP协议中的相关数据
 * 3.包装一些JS输出到客户端
 * 
 */
public abstract class ServletYn {
    /**
     * ISSUE:BI-4354  在上下两栏的workspace中进行删除主题域的操作后，该选项卡没有自动消失  
     * 原因：closeCurrentWindow由于没有指定参数，使得该方法在上下两栏时无法正确找到所在的页，所以closeCurrentWindow方法必须指定参数，这里传递的是第二个参数
     * --20110317
     */
    public static final String SCRIPT_CLOSE_WINDOW = "<script>var _ws=getWorkspace();\n_ws?_ws.closeCurrentPage(null,window):autoCloseBrowser();</script>";

    public static final String SCRIPT_REFRESH_LEFT_TREE = "<script>refreshLeftTree();</script>";

    /**
     * 返回客户端环境
     * 返回的形式类似：
     * CPU类型 浏览器类型
     * (x86) Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)
     * @param req
     * @return
     */
    public static final String getUserAgent(HttpServletRequest req) {
        return "(" + StrYn.null2default(req.getHeader("UA-CPU"), "Other") + ") " + SecurityYn.checkXSSParam(req.getHeader("User-Agent"));
    }

    /**
     * weblogic10.3在windows上好像有个bug，导致bi报错：
     * java.lang.IllegalArgumentException: Bad date header: 'Wed, 03 Jun 2009 11:03:48 GMT'
     *   at weblogic.servlet.internal.ServletRequestImpl.getDateHeader(ServletRequestImpl.java:983)
     *   at com.esen.irpt.jsp.filter.Filter_js_css.forwardSysjs(Filter_js_css.java:116)
     *   at com.esen.irpt.jsp.filter.Filter_js_css.filter_js_css(Filter_js_css.java:61)
     *   at com.esen.irpt.jsp.filter.Filter_js_css.doFilter(Filter_js_css.java:45)
     *   at weblogic.servlet.internal.FilterChainImpl.doFilter(FilterChainImpl.java:42)
     *   Truncated. see log file for complete stacktrace
     * 将语言环境设置为en_US可以避免这个问题，设置方法就是在启动java是带上环境变量：-Duser.language=ll,
     * 但是设置之后无法用jdbc连接oracle了，oracle报ORA-12705: Cannot access NLS data files....，
     * 所以这里将从HttpServletRequest类获取日期信息的函数包装一下，如果出现异常了，那么自己从字符串分析出日期信息
     */
    public static final long getDateHeader(HttpServletRequest req, String s) {
        try {
            return req.getDateHeader(s);
        }
        catch (IllegalArgumentException e) {
            String header = req.getHeader(s);
            if (header == null || header.length() == 0) {
                return -1;
            }
            try {
                SimpleDateFormat df = new SimpleDateFormat("EEE, DD MMM yyyy HH:mm:ss zzz", Locale.US);
                Date date = df.parse(header);
                return date.getTime();
            }
            catch (ParseException ex) {
                throw e;
            }
        }
    }

    /**
     * 在页面中打印输出指定模板的内容，该模板只能够是非Jsp的文件。
     * 在jsp中可以通过<%@ include file="" %>方法引用，具体用那种方式看各自的使用习愦
     * 
     * @param req
     * @param out
     * @param templet 模板文件名
     * @throws Exception
     */
    public static final void getTemplet(HttpServletRequest req, JspWriter out, String templet) throws Exception {
        out.println(getTemplet(req, templet));
    }

    /**
     * 返回指定模板的内容
     * 
     * @param req
     * @param templet
     * @return
     * @throws Exception
     */
    public static final String getTemplet(HttpServletRequest req, String templet) throws Exception {
        InputStream in = req.getSession().getServletContext().getResourceAsStream(templet);
        if (in == null)
            return null;
        String rs = null;
        try {
            rs = StmYn.stm2Str(in, "UTF-8");
        }
        finally {
            in.close();
        }
        return rs;
    }

    /**
     * 获取一个Html页面的输出流
     * @param req
     * @param res
     * @param html
     * @param defjsheader  通过参数defjsheader来控制是否缺省将公共的脚本引入，因为有时候只是非常简单的
     *                     页面输出，不必引入脚本，引用后反而是增加了浏览器的负担
     * @return
     * @throws Exception
     */
    public static PrintWriter getHtmlOutputStream(HttpServletRequest req, HttpServletResponse res, String html,
            boolean defjsheader) throws Exception {
        res.setContentType("text/html; charset=UTF-8");
        PrintWriter out = res.getWriter();
        out.print("<html><head>");
        out.print("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
        out.print("<link href=\"xui/xui.css\" rel=\"stylesheet\" type=\"text/css\"/>");
        out.print("</head><body>" + StrYn.null2blank(html) + "</body>");
        /*通过参数defjsheader来控制是否缺省将公共的脚本引入，因为有时候只是非常简单的页面输出，不必引入脚本，引用后反而是增加了浏览器的负担*/
        if (defjsheader) {
            out.println("<script src=\"xui/sys.js\"></script>");
            out.println("<script src=\"xui/util.js\"></script>");
            out.println("<script src=\"xui/uibase.js\"></script>");
        }
        out.println("</html>");
        return out;
    }

    /**
     * 获取一个Html页面的输出流
     * @param req
     * @param res
     * @param html
     * @return
     * @throws Exception
     */
    public static PrintWriter getHtmlOutputStream(HttpServletRequest req, HttpServletResponse res, String html)
            throws Exception {
        return getHtmlOutputStream(req, res, html, true);
    }

    public static PrintWriter getHtmlOutputStream(HttpServletRequest req, HttpServletResponse res) throws Exception {
        return getHtmlOutputStream(req, res, null);
    }

    //输出指定的Javascript脚本
    public static void outJavascript(PrintWriter out, String js) throws Exception {
        if (StrYn.isNull(js))
            return;
        out.println("<script>");
        out.println(js);
        out.println("</script>");
    }

    //关闭workspace里的当前页面
    public static void closeWindowScript(HttpServletRequest req) {
        req.setAttribute("closeWindowScript", SCRIPT_CLOSE_WINDOW);
    }

    public static String getCloseWindowScript(HttpServletRequest req) {
        String script = (String) req.getAttribute("closeWindowScript");
        return (StrYn.isNull(script) ? "" : script);
    }

    public static void reloadTopWindowScript(HttpServletRequest req) {
        reloadScript(req, "top");
    }

    public static void reloadParentWindowScript(HttpServletRequest req) {
        reloadScript(req, "parent");
    }

    public static void reloadWindowScript(HttpServletRequest req) {
        reloadScript(req, null);
    }

    //重新载入页面,istop表示是否重新载入顶部页面
    public static void reloadScript(HttpServletRequest req, String object) {
        StringBuffer js = new StringBuffer("<script>\n");
        //object如果没有指定，则表示为当前window对象
        js.append("var _java_isWindow_ = ").append(StrYn.isNull(object)).append(";\n");
        js.append("var _java_wnd_ = ").append(StrYn.null2default(object, "window")).append(";\n");
        js.append("if (_java_isWindow_ || _java_wnd_ != window) {\n");
        js.append("_java_wnd_.location.reload();}\n");
        js.append("</script>\n");
        req.setAttribute("reloadWindowScript", js.toString());
    }

    public static String getReloadScript(HttpServletRequest req) {
        String script = (String) req.getAttribute("reloadWindowScript");
        return (StrYn.isNull(script) ? "" : script);
    }

    //重新载入页面,istop表示是否重新载入顶部页面
    public static void refreshLeftTreeScript(HttpServletRequest req) {
        req.setAttribute("refreshLeftTreeScript", SCRIPT_REFRESH_LEFT_TREE);
    }

    public static String getRefreshLeftTreeScript(HttpServletRequest req) {
        String script = (String) req.getAttribute("refreshLeftTreeScript");
        return (StrYn.isNull(script) ? "" : script);
    }

    public static final String GLOBAL_EXCEPTION = "ESENSOFT.Global_Exception";

    public static final String GLOBAL_MESSAGE = "ESENSOFT.Global_Message";

    public static final void setMessage(HttpServletRequest request, String msg) {
        request.setAttribute(GLOBAL_MESSAGE, msg);
    }

    public static final String getMessage(HttpServletRequest request) {
        return (String) request.getAttribute(GLOBAL_MESSAGE);
    }

    public static final boolean hasMessage(HttpServletRequest request) {
        return getMessage(request) != null;
    }

    public static final void setException(HttpServletRequest request, Throwable ex) {
        request.setAttribute(GLOBAL_EXCEPTION, ex);
    }

    public static final Exception getException(HttpServletRequest request) {
        return (Exception) request.getAttribute(GLOBAL_EXCEPTION);
    }

    public static final String getExceptionMessage(HttpServletRequest request) {
        Exception ex = getException(request);
        if (ex == null)
            return "";
        /*
         * [安全改造]系统信息泄露，add by shenzhy 2013.5.7
         */
        String msg = SecurityYn.filter(ex.getMessage());
        return StrYn.isNull(msg) ? "" : msg.replaceAll("\r\n", "<br>");
    }

    public static final String exception2str(HttpServletRequest request) {
        Exception ex = getException(request);
        return ex == null ? "" : StrYn.null2blank(StrYn.exception2str(ex));
    }

    public static final boolean hasException(HttpServletRequest request) {
        return getException(request) != null;
    }

    /**
     * 向前端显示一个简单的错误提示内容，该方法适用于向隐藏框架中执行操作时出现异常后给前端进行提示
     * @param req
     * @param res
     * @param e
     * @throws Exception
     */
    public static final void showError(HttpServletRequest req, HttpServletResponse res, Throwable e) throws Exception {
        getHtmlOutputStream(req, res, "<script>alert(\"" + StrYn.exceptionMsg2str(e) + "\");</script>", false);
        res.flushBuffer();
    }

    /**
     * 获得客户端的实际ip地址,如果是127.0.0.1,也要转换为实际的地址
     * 主要用在日志中,记录日志的生成机器
     */
    public static String getRemoteAddress(HttpServletRequest req) {
        // 当通过apache反向代理访问bi时，所有的RemoteAddress都是反向代理服务器的IP，我们此时只能种http头中的x-forwarded-for获取到客户端真正的IP
        //    host = 192.168.1.30:8080
        //    accept = */*
        //    accept-language = zh-cn
        //    ua-cpu = x86
        //    accept-encoding = gzip, deflate
        //    user-agent = Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; InfoPath.1)
        //    cookie = JSESSIONID=DB38FFC34FEDD3B360247DAC638ABD9E; com.esen.irpt.web.login.user=demo002
        //    via = 1.1 delsvr
        //    x-forwarded-for = 58.49.52.114
        //    x-forwarded-host = 58.49.52.114
        //    x-forwarded-server = delsvr
        //    connection = Keep-Alive

        String ip = req.getHeader("x-forwarded-for");
        if (StrYn.isNull(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("Proxy-Client-IP");
        }
        if (StrYn.isNull(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("WL-Proxy-Client-IP");
        }
        if (StrYn.isNull(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
            /*
             * 2011-03-28 by hejzh：将"127.0.0.1".equalsIgnoreCase(ip)改为"127.0.0.1".equals(ip)，实际上字符串
             *                  "127.0.0.1"并不包含存在大小写的字符，因此没必要ignoreCase，改为equals性能好些；
             *                  "0:0:0:0:0:0:0:1".equals(ip)同理
             */
            if ("127.0.0.1".equals(ip) || "localhost".equalsIgnoreCase(ip)
                    || "0:0:0:0:0:0:0:1".equals(ip)) {//windows7上，还可能是0:0:0:0:0:0:0:1
                try {
                    /*edit by xujw 2013.5.2 安全改造： Often Misused: Authentication*/
                    ip=SecurityYn.getAddress();
                }
                catch (Exception e) {
                    ExceptionHandler.rethrowRuntimeException(e);
                }
            }
        }
        return ip;
    }

    /**
     * 获取指定的参数，如果参数中有escape=true，则将指定的参数进行解码
     * @param req
     * @param nm
     * @return
     */
    public static final String getParameter(HttpServletRequest req, String nm) {
        //edit by zqj  2013.5.6 安全改造：Log Forging
        String rs = SecurityYn.checkLogDesc(req,nm);
        if (!StrYn.isNull(rs) && StrYn.parseBoolean(req.getParameter("escape"), false)) {
            rs = StrYn.unescape(rs);
        }
        return rs;
    }

    public static final String[] getParameterValues(HttpServletRequest req, String nm) {
        String[] rs = req.getParameterValues(nm);
        /**
         * 为空则直接返回空 20130121 by kangx
         * */
        if(rs == null){
            return null;
        }
        if (rs != null && rs.length > 0 && StrYn.parseBoolean(req.getParameter("escape"), false)) {
            for (int i = 0; i < rs.length; i++) {
                rs[i] = StrYn.unescape(rs[i]);
            }
        }
        return rs;
    }

    /**
     * 返回web请求req对应的参数，如果没有参数，那么返回null
     * 否则返回形如:?a=1&b=2 这样的字符串
     */
    public static final String getQueryString(HttpServletRequest req) {
        String r = req.getQueryString();
        if (r != null && r.length() > 0) {
            return "?" + r;
        }
        Enumeration a = req.getParameterNames();
        StringBuffer params = null;
        //取出POST的请求参数
        while (a.hasMoreElements()) {
            if (params == null) {
                params = new StringBuffer(256);
                params.append('?');
            }
            String key = a.nextElement().toString();
            String val = req.getParameter(key);
            if (params.length() > 2) {
                params.append('&');
            }
            params.append(key).append('=').append(val);
        }
        if (params == null || params.length() <= 0) {
            return null;
        }
        else {
            return params.toString();
        }
    }

    /**
     * 发送重定向请求，url可以是绝对地址，也可以是相对地址，此函数会自能判断，判断规则如下：
     * 如果url是http或www.开头的话，那么直接sendRedirect
     * 如果是/开头的话也直接sendRedirect
     * 其他请假加上contextPath后再sendRedirect
     */
    public static final void sendRedirect(HttpServletRequest req, HttpServletResponse res, String url)
            throws IOException {
        String lwurl = url.toLowerCase();
        //edit by zqj 2013.5.6 安全改造：Header Manipulation
        if (lwurl.startsWith("http://") || lwurl.startsWith("https://"))
            res.sendRedirect(SecurityYn.checkHttpHeader(null,url));
        else if (lwurl.charAt(0) == '/')
            res.sendRedirect(SecurityYn.checkHttpHeader(null,url));
        else if (lwurl.startsWith("www."))
            res.sendRedirect(SecurityYn.checkHttpHeader(null,"http://" + url));
        else if (lwurl.indexOf("<script") > -1) {
            //支持直接个人首页那里写javascript脚本，支持吉大正元网关集成。
            lwurl = SecurityYn.filter(lwurl);
            res.getWriter().write(lwurl);
        }
        else
            res.sendRedirect(SecurityYn.checkHttpHeader(null,req.getContextPath() + "/" + url));//edit by zqj 2013.5.6 安全改造：Header Manipulation
    }

    /**
     * 判断上传的文件是否是zip文件
     * 判断方法:如果上传文件的后缀是以".zip"结尾,则是zip文件
     */
    public static boolean isZipFile(FileItem item) {
        return ".zip".equalsIgnoreCase(FileYn.extractFileExt(item.getName()));
    }

    /**
     * 设置下载文件的HTTP头
     * contentType默认为application/x-download
     * charset指定编码,如果没有指定,则不设置
     * contentDisposition默认为attachment; filename=xxx,需要传入文件名,否则使用默认的下载文件名download
     *   如果设置了此参数,则filename无效
     * 设置传输编码Content-transfer-Encoding=binary
     */
    public static final void setDownloadHeader(HttpServletResponse res, String contentType, String charset,
            String contentDisposition, String filename) {
        /**
         * 去掉不需要的http头，包括cache设置，下载文件不需要这些信息
         */
        ServletYn.resetResponse(res);

        if (StrYn.isNull(contentType))
            contentType = "application/x-download";
        if (StrYn.isNull(contentDisposition)) {
            if (StrYn.isNull(filename))
                filename = "download";

            /**
             * 20090711 浏览器对下载的文件名有很多限制，光进行ISO8859-1编码不行，还要处理特殊字符。
             *          之前用encodeISO8859_1，改为用formatDownloadFileName。
             */
            contentDisposition = "attachment; filename=" + StrYn.formatDownloadFileName(filename);
        }
        if (!StrYn.isNull(charset)) {
            contentType += "; charset=" + charset;
        }

        /**
         * 部分web容器不支持setCharacterEncoding方式设置编码,所以直接将编码加入到ContentType中
         * 如:在weblogic 8.1 sunos 5.9上res.setCharacterEncoding("UTF-8")会出现异常
         */
        res.setContentType(contentType);
        /**
         * Content-Disposition参数是为了在客户端另存文件时提供一个建议的文件名,但是考虑到安全的原因，就从规范中去掉了这个参数,这个不是HTTP/1.1的标准参数
         * 有了Content-Disposition，这样才能使IE使用外部程序打开文件，否则如果是word文件，那么用户选择“打开”而不是“保存”，那么其实IE是在IE的窗口中打开文件的。
         */
        res.addHeader("Content-Disposition", contentDisposition);
        /**
         * 内容传输编码（Content-Transfer-Encoding），这个区域可以指定ASCII以外的字符编码方式
         * Content-Transfer-Encoding: [mechanism]
         * 其中，mechanism的值可以指定为"7bit"，"8bit"，"binary"，"quoted-printable"，"base64"。
         * 7bit:7字节的ASCII编码方式。
         * 8bit:8比特ASCII码.
         * quoted-printable:因为欧洲的一些文字和ASCII字符集中的某些字符有部分相同。
         *   如果邮件消息使用的是这些语言的话，于ASCII重叠的那些字符可以原样使用，ASCII字符集中不存在的字符采用形如“=??”的方法编码。
         *   这里“??”需要用将字符编码后的16进制数字来指定。采用quoted-printable编码的消息，长度不会变得太长，而且大部分都是ASCII中的字符，
         *   即使不通过解码也大致可以读懂消息的内容。
         * base64:是一种将二进制的01序列转化成ASCII字符的编码方法。
         *   编码后的文本或者二进制消息，就可以运用SMTP等只支持ASCII字符的协议传送了。
         *   Base64一般被认为会平均增加33%的报文长度，而且，经过编码的消息对于人类来说是不可读的。
         */
        res.addHeader("Content-transfer-Encoding", "binary");
    }

    /**
     * 执行HttpServletResponse的reset方法，其实只调用res.reset()即可，但是websphere上有bug，调用reset并不能清空header，
     * 所以此处用这个方法封装了一下。
     * websphere的bug参考：http://www-01.ibm.com/support/docview.wss?rs=180&uid=swg1PK29451
     *                   PK29451: A CALL TO HTTPSERVLETRESPONSE:RESET() DOES NOT CAUSE PREVIOUSLY ADDED HEADERS TO BE REMOVED
     */
    public static final void resetResponse(HttpServletResponse res) {
        res.setHeader("P3P", null);
        res.setHeader("Pragma", null);
        res.setHeader("Cache-Control", null);
        res.setHeader("Expires", null);
        res.setHeader("Content-Type", null);
        res.setHeader("Date", null);

        res.reset();
    }

    /**
     * 返回request的URI，不含参数（即？后面的内容），不含contextpath，以/开头
     */
    public static String getRequestURI_withoutContextPath(HttpServletRequest httpRequest) {
        String r = httpRequest.getRequestURI();
        //update by zhuzht 20130506 安全改造File Disclosure
        r = SecurityYn.filterUrl(r);
        if (r != null && r.indexOf("//") > -1) {
            /**
             * BIDEV-827
             * 在IE中访问这个地址：http://192.168.1.200:8080/bi2.1//editrpt.do?action=edit&taskid=LKU5BCLYNUU6S5MKLVWCC8LUJ1KTUXS2&rptid=LKU5BCLYNUU6S5MKLVWCC8LUJ1KTUXS2$1$ZYL4K39I8PL1I4LYCTBUU97KMIYDMTLL
             * 浏览器总是报错。特点是bi2.1后面有2个斜线。
             * 所以这里处理了一下，把//换成/ 
             */
            r = r.replaceAll("//+", "/");
        }
        String cp = httpRequest.getContextPath();
        if (cp != null && cp.length() > 1) {
            return r.substring(cp.length());
        }
        return r;
    }

    /**
     * 取得服务器的web应用目录(contextpath)，返回的值总是前后有/,如果contextpath为空,则返回/。
     */
    public static String getContextPath(HttpServletRequest httpRequest) {
        String ctxpath = httpRequest.getContextPath();
        if (ctxpath == null || ctxpath.length() == 0) {
            return "/";
        }
        if (ctxpath.charAt(0) != '/') {
            ctxpath = '/' + ctxpath;
        }
        if (ctxpath.charAt(ctxpath.length() - 1) != '/')
            ctxpath += '/';

        return ctxpath;
    }
    
    /**
     * HttpServletRequest的getParameterMap方法获取的map中value都是字符串数组对象，大部分情况下其实value都是一个单个的字符串
     * 此函数将HttpServletRequest的getParameterMap方法返回的map中的中的value参数转换为String对象，如果value原来是长度大于1的
     * 数组，那么转换为逗号连接的字符串
     * @param httpRequest
     * @return
     */
    public static Map conventRequestToMap(HttpServletRequest httpRequest) {
        //request中获取的参数为Map<String,String[]>
        Map params = httpRequest.getParameterMap();
        Map map = new HashMap(params.size());
        for (Iterator iter = params.keySet().iterator(); iter.hasNext();) {
            Object name = iter.next();
            String[] values = (String[]) params.get(name);
            map.put(name, ArrYn.array2Str(values, ","));
        }
        return map;
    }
    
    /**
     * 对目录进行编码，忽略/，例如：public/portals/中文/
     * @param url
     * @param enc
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String encodeURL(String url, String enc) throws UnsupportedEncodingException {
        if (url == null || url.length() == 0)
            return url;
        StringBuffer rs = new StringBuffer((int) (url.length() * 1.3) + 5);
        int j = 0;
        int i = url.indexOf('/');
        while (i > -1) {
            rs.append(URLEncoder.encode(url.substring(j, i), enc));
            rs.append('/');
            j = i + 1;
            i = url.indexOf('/', j);
        }
        rs.append(URLEncoder.encode(url.substring(j), enc));
        /*
         * ISSUE:BI-8526,add by shenzhy at 2013.05.30
         * 此问题的真正原因是：在vfs中如果门户具有上级目录，并且目录的名字中含有" "，那么将会导致后面的逻辑失效。
         * 比如门户的路径是"public/protals/门户用例整理/01 测试用例贴/010 报表参数默认值.portal"。
         * 这是jdk的BUG，URLEncoding会把" "encode为"+"，导致url被decode时，"+"没有被转成" "，仍然是"+"。
         * 解决的办法是在encode时，将"+"替换成"%20"，在这里替换时不用担心会将本来就应该存在的"+"被替换掉，
         * 因为此时的"+"已经被encode成"%2B"了。 
         */
        //return rs.toString()
        return rs.toString().replace("+", "%20");
    }
    
    public static String encodeURL(String url) throws UnsupportedEncodingException {
        return encodeURL(url, "UTF-8");
    }
    
    /**
     * 当页面装入完成时,调用此方法,会隐藏页面中正在装入的提示信息
     * @param out
     * @deprecated 请使用标签库：jspwaitmsg，jspwaitmsg标签会自动隐藏等待信息的，如果非要手工隐藏可以设置autohide=false，并直接执行js函数hideJspWaitingDomMsg();
     * @param includeScriptTag
     * @throws IOException
     */
    public static final void printHideJspWaitingDom(JspWriter out, boolean includeScriptTag) throws IOException {
        if (includeScriptTag)
            out.println("<script>");
        out.println("hideJspWaitingDomMsg();");//来自TagLib_WaitMsg.printJspWaitingDom的输出
        if (includeScriptTag)
            out.println("</script>");
    }

    /**
     * 跳转到url指定的页面，执行跳转操作时应尽可能使用该方法。
     */
    public static String forwardPage(final HttpServletRequest req, final HttpServletResponse res, final String url)
            throws Exception {
        /*
         * 当Struts处理上传请求时，会将HttpServletRequest对象包装为MultipartRequestWrapper，
         * 但是当RequestDispatcher执行forward时，可能会抛出ClassCastException异常
         * （tomcat是没有该问题的，但是weblogic的RequestDispatcher实现类会出问题）。
         *  
         * 为了避免该问题，在执行forard的时候，在forward前，获取MultipartRequestWrapper保存的HttpServletRequest对象，对其进行forward操作。
         * 参考页面：https://issues.apache.org/jira/browse/STR-3156?page=com.atlassian.jira.plugin.system.issuetabpanels%3Achangehistory-tabpanel
         */
        HttpServletRequest newReq = req;
        RequestDispatcher dispatcher = newReq.getRequestDispatcher(url);
        dispatcher.forward(newReq, res);
        return null;
    }
    
    /**
     * 将jsp的结果或者html内容包含到指定区域内进行显示
     * @param req
     * @param res
     * @param src 要包含的页面，可以是jsp或者html
     * @param out
     * @throws ServletException
     * @throws IOException
     */
    public static void includeJspResult(ServletRequest req, ServletResponse res, String src, JspWriter out)
            throws ServletException, IOException {
        out.print("<!-- start[include ");
        out.print(src);
        out.print("] -->");
        req.getRequestDispatcher(src).include(req, new HttpServletResponseWrapper4includeJspResult((HttpServletResponse) res, out));
        out.print("<!-- end[include ");
        out.print(src);
        out.print("] -->");
    }
    
    /**
     * 将string的数组返回给客户端js使用
     * var hiddenparams = <%=hiddenparams%>;
     * @param array
     * @return
     */
    public static String array2json(String[] array) {
        StringBuffer json = new StringBuffer();
        json.append("[");
        if (array != null && array.length > 0) {
            for (int i = 0; i < array.length; i++) {
                String obj = array[i];
                json.append("\"");
                json.append(obj);
                json.append("\",");
            }
            json.setCharAt(json.length() - 1, ']');
        }
        else {
            json.append("]");
        }
        return json.toString();
    }

    /**
     * 将一个<String,String>的map返回给客户端js使用
     * var paramsmap = new Map(<%=paramsmap%>);
     * @param map
     * @return
     */
    public static String map2json(Map map) {
        StringBuffer json = new StringBuffer();
        json.append("\"");
        if (map != null && map.size() > 0) {
            Iterator iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                json.append(key);
                json.append("=");
                json.append(map.get(key));
                json.append(";");
            }
        }
        json.append("\"");
        return json.toString();
    }
    
    private static String[] mobileUserAgents = new String[] {
            "iphone",
            "ipad",
            "android"
    };

    /**
     * 判断是否是移动设备访问
     * @param req
     * @return
     * @throws Exception
     */
    public static boolean isMobileDevice(HttpServletRequest req) {
        String userAgent = req.getHeader("user-agent");
        if (!StrYn.isNull(userAgent)) {
            userAgent = userAgent.toLowerCase();
            for (int i = 0; i < mobileUserAgents.length; i++) {
                if (userAgent.contains(mobileUserAgents[i])) {
                    return true;
                }
            }
        }
        return false;
    }
    
    
    /**
     * 判断是否为ajax请求,根据header里面"X_REQUESTED_WITH"属性是否为"XMLHttpRequest"
     * @param req
     * @return
     */
    public static final boolean isAjaxRequest(HttpServletRequest req) {

        /**
         * X_REQUESTED_WITH: XMLHttpRequest
         */
        return "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X_REQUESTED_WITH"));
    }
    
    
    /**
     * 根据语言代码返回期望的Locale对象。不支持时，返回Locale.ENGLISH。
     * 匹配语言代码的顺序是：
     * 先从request中检查lang参数有没值，
     * 没有，则读取cookie "esensoft.user.language"的值，
     * 再没有，则读取request.getLocale()值(浏览器accept-language值)
     * 然后检查是否支持的语言，不支持时，返回Locale.ENGLISH.
     * 最后将locale写入cookie.
     * @param req
     * @return
     */
    public static Locale getPureposeLocale(HttpServletRequest req) {
        String langStr = req.getParameter("lang");
        if (StrYn.isNull(langStr)) {
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (int i = 0; i < cookies.length; i++) {
                    if ("esensoft.user.language".equalsIgnoreCase(cookies[i].getName())) {
                        langStr = cookies[i].getValue();
                        break;
                    }
                }
            }
            if (StrYn.isNull(langStr))
                langStr = req.getLocale().toString();
        }
        return StrYn.parseLocaleStr(langStr, Locale.SIMPLIFIED_CHINESE);
    }
}


/**
 * 该类扩展了HttpServletResponseWrapper类，主要是为了在自定义标签实现中通过include方法来引入的jsp或者html时，能够正确的显示在指定区域内
 */
class HttpServletResponseWrapper4includeJspResult extends HttpServletResponseWrapper {

    private PrintWriter out;

    public HttpServletResponseWrapper4includeJspResult(HttpServletResponse response) {
        super(response);
    }

    public HttpServletResponseWrapper4includeJspResult(HttpServletResponse response, JspWriter out) {
        super(response);
        this.out = new PrintWriter(out);
    }

    public PrintWriter getWriter() throws IOException {
        return out;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }
}