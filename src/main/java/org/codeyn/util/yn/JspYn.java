package org.codeyn.util.yn;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import org.codeyn.util.exception.ExceptionHandler;
import org.codeyn.util.file.FileYn;
import org.springframework.scheduling.config.Task;

public class JspYn{

    static final char[] HEXCHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static final String getDataType(ResultSetMetaData rmd, int index)
            throws Exception{
        switch (rmd.getColumnType(index)) {
            case Types.ARRAY:
                return "ARRAY";
            case Types.BIGINT:
                return "BIGINT";
            case Types.BINARY:
                return "BINARY";
            case Types.BIT:
                return "BIT";
            case Types.BLOB:
                return "BLOB";
            case Types.BOOLEAN:
                return "BOOLEAN";
            case Types.CHAR:
                return "CHAR";
            case Types.CLOB:
                return "CLOB";
            case Types.DATALINK:
                return "DATALINK";
            case Types.DATE:
                return "DATE";
            case Types.DECIMAL:
                return "DECIMAL";
            case Types.DISTINCT:
                return "DISTINCT";
            case Types.DOUBLE:
                return "DOUBLE";
            case Types.FLOAT:
                return "FLOAT";
            case Types.INTEGER:
                return "INTEGER";
            case Types.JAVA_OBJECT:
                return "JAVA_OBJECT";
            case Types.LONGVARBINARY:
                return "LONGVARBINARY";
            case Types.LONGVARCHAR:
                return "LONGVARCHAR";
            case Types.NULL:
                return "NULL";
            case Types.NUMERIC:
                return "NUMERIC";
            case Types.OTHER:
                return "OTHER";
            case Types.REAL:
                return "REAL";
            case Types.REF:
                return "REF";
            case Types.SMALLINT:
                return "SMALLINT";
            case Types.STRUCT:
                return "STRUCT";
            case Types.TIME:
                return "TIME";
            case Types.TIMESTAMP:
                return "TIMESTAMP";
            case Types.TINYINT:
                return "TINYINT";
            case Types.VARBINARY:
                return "VARBINARY";
            case Types.VARCHAR:
                return "VARCHAR";
            default:
                return "";
        }
    }

    public static String encode(String s){
        if (s == null || s.length() == 0) {
            return "";
        }
        StringBuffer result = new StringBuffer("");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (((c >= '0') && (c <= '9')) || ((c >= 'a') && (c <= 'z'))
                    || ((c >= 'A') && (c <= 'Z'))) {
                result.append(c);
            } else {
                int j;
                System.out.println((int) c);
                if (c > 0xFF) {
                    j = (int) c >> 8;
                    result.append('%');
                    result.append(HEXCHAR[j / 16]);
                    result.append(HEXCHAR[j % 16]);
                }
                j = c & 0xFF;
                result.append('%');
                result.append(HEXCHAR[j / 16]);
                result.append(HEXCHAR[j % 16]);
            }
        }
        return result.toString();
    }

    public static String myDecodeURL(String p){
        if (p == null || p.length() == 0) {
            return p;
        }
        try {
            byte[] b = StrYn.hexStringToBytes(p);
            return new String(b);
        } catch (Exception ex) {
            return p;
        }
    }

    public static String myEncodeURL(String p){
        if (p == null || p.length() == 0) {
            return p;
        }
        byte[] b = p.getBytes();
        return StrYn.bytesToHexString(b);
    }

    public static String encodeURL(String p){
        try {
            p = java.net.URLEncoder.encode(p, "gb2312");
            return p.replaceAll("%2F", "/");
        } catch (Exception ex) {
            return p;
        }

    }

    public static String encodeURL(String jspFile, String[] postName,
            String[] postValue){
        StringBuffer result = new StringBuffer(jspFile);
        boolean b = false;
        int j;
        try {
            for (j = 0; j < postName.length; j++) {
                if (j >= postValue.length) {
                    break;
                }
                if (postName[j] != null && postName[j].length() > 0
                        && postValue[j] != null && postValue[j].length() > 0) {
                    if (!b) {
                        b = true;
                        result.append('?');
                    } else {
                        result.append('&');
                    }
                    result.append(postName[j]);
                    result.append('=');
                    result.append(java.net.URLEncoder.encode(postValue[j],
                            "gb2312"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result.toString();
    }

    public static String chinese(String cn) throws Exception{
        if (cn == null) {
            return null;
        } else {
            return new String(cn.getBytes("8859_1"));
        }
    }

    public static String base64Encode(String enStr) throws Exception{
        if (enStr == null) {
            return null;
        }
        return (new sun.misc.BASE64Encoder().encode(enStr.getBytes()));
    }

    public static String base64Decode(String deStr) throws Exception{
        if (deStr == null) {
            return null;
        }
        sun.misc.BASE64Decoder decodeStr = new sun.misc.BASE64Decoder();
        byte[] deBuf = decodeStr.decodeBuffer(deStr);
        return new String(deBuf);
    }

    /**
     * 获取当前时间，format指定时间的格式。 DateTime 返回"yyyy-MM-dd HH:mm:ss"形式的时间。 Date
     * 返回"yyyy-MM-dd"形式的时间。 FileDT 返回"yyyyMMdd_HHmmss"形式的时间 DT
     * 返回"yyyyMMddHHmmss"形式的时间
     * */
    public static final String DateTime = "yyyy-MM-dd HH:mm:ss";

    public static final String Date = "yyyy-MM-dd";

    public static final String FileDT = "yyyyMMdd_HHmmss";

    public static final String DT = "yyyyMMddHHmmss";

    public static String getDateFormat(String format) throws Exception{
        java.text.DateFormat df = new java.text.SimpleDateFormat(format);
        return df.format(new java.util.Date());
    }

    public static String getDateFormat(String format, java.util.Date dt)
            throws Exception{
        java.text.DateFormat df = new java.text.SimpleDateFormat(format);
        return df.format(dt);
    }

    public static String getDateFormat(String format, long currentTime)
            throws Exception{
        java.text.DateFormat df = new java.text.SimpleDateFormat(format);
        return df.format(new java.util.Date(currentTime));
    }

    public static String getContentType(String type){
        if (type.equalsIgnoreCase(".gif")) {
            return "image/gif";
        }
        if (type.equalsIgnoreCase(".jpg") || type.equalsIgnoreCase(".jpe")
                || type.equalsIgnoreCase(".jpeg")) {
            return "image/jpeg";
        }
        if (type.equalsIgnoreCase(".png")) {
            return "image/png";
        }
        if (type.equalsIgnoreCase(".bmp") || type.equalsIgnoreCase(".rle")
                || type.equalsIgnoreCase(".dib")) {
            return "image/bmp";
        }
        if (type.equalsIgnoreCase(".htm") || type.equalsIgnoreCase(".html")) {
            return "text/html";
        }
        if (type.equalsIgnoreCase(".zip")) {
            return "application/zip";
        }
        if (type.equalsIgnoreCase(".doc")) {
            return "application/msword";
        }
        if (type.equalsIgnoreCase(".xls")) {
            return "application/vnd.ms-excel";
        }
        if (type.equalsIgnoreCase(".txt")) {
            return "text/plain";
        }
        return "application/octet-stream";
    }

    public static String getFileICON(String fileExt, String path){
        if (StrYn.isNull(fileExt)) {
            return StrYn.isNull(path) ? "images/none.gif" : path
                    + "images/none.gif";
        }
        if (fileExt.equalsIgnoreCase(".gif")) {
            return StrYn.isNull(path) ? "images/gif.gif" : path
                    + "images/gif.gif";
        }
        if (fileExt.equalsIgnoreCase(".bmp")) {
            return StrYn.isNull(path) ? "images/bmp.gif" : path
                    + "images/bmp.gif";
        }
        if (fileExt.equalsIgnoreCase(".htm")
                || fileExt.equalsIgnoreCase(".html")) {
            return StrYn.isNull(path) ? "images/htm.gif" : path
                    + "images/htm.gif";
        }
        if (fileExt.equalsIgnoreCase(".jpg")
                || fileExt.equalsIgnoreCase(".jpeg")) {
            return StrYn.isNull(path) ? "images/jpg.gif" : path
                    + "images/jpg.gif";
        }
        if (fileExt.equalsIgnoreCase(".png")) {
            return StrYn.isNull(path) ? "images/png.gif" : path
                    + "images/png.gif";
        }
        if (fileExt.equalsIgnoreCase(".doc")) {
            return StrYn.isNull(path) ? "images/word.gif" : path
                    + "images/word.gif";
        }
        if (fileExt.equalsIgnoreCase(".xls")) {
            return StrYn.isNull(path) ? "images/xls.gif" : path
                    + "images/xls.gif";
        }
        if (fileExt.equalsIgnoreCase(".txt")) {
            return StrYn.isNull(path) ? "images/txt.gif" : path
                    + "images/txt.gif";
        }
        if (fileExt.equalsIgnoreCase(".dll")
                || fileExt.equalsIgnoreCase(".ocx")) {
            return StrYn.isNull(path) ? "images/dll.gif" : path
                    + "images/dll.gif";
        }
        if (fileExt.equalsIgnoreCase(".bat")
                || fileExt.equalsIgnoreCase(".cmd")) {
            return StrYn.isNull(path) ? "images/bat.gif" : path
                    + "images/bat.gif";
        }
        if (fileExt.equalsIgnoreCase(".exe")
                || fileExt.equalsIgnoreCase(".com")) {
            return StrYn.isNull(path) ? "images/exe.gif" : path
                    + "images/exe.gif";
        }
        if (fileExt.equalsIgnoreCase(".pdf")) {
            return StrYn.isNull(path) ? "images/pdf.gif" : path
                    + "images/pdf.gif";
        }
        if (fileExt.equalsIgnoreCase(".rar")
                || fileExt.equalsIgnoreCase(".zip")
                || fileExt.equalsIgnoreCase(".tar")
                || fileExt.equalsIgnoreCase(".cab")
                || fileExt.equalsIgnoreCase(".arj")
                || fileExt.equalsIgnoreCase(".lzh")
                || fileExt.equalsIgnoreCase(".ace")
                || fileExt.equalsIgnoreCase(".gzip")) {
            return StrYn.isNull(path) ? "images/rar.gif" : path
                    + "images/rar.gif";
        }
        return StrYn.isNull(path) ? "images/none.gif" : path
                + "images/none.gif";
    }

    public static String getFileType(File file){
        if (file.isDirectory()) return "文件夹";
        String fileExt = FileYn.extractFileExt(file.getAbsolutePath());
        if (StrYn.isNull(fileExt)) {
            return "未知文件";
        }
        if (fileExt.equalsIgnoreCase(".gif")) {
            return "GIF图片";
        }
        if (fileExt.equalsIgnoreCase(".bmp")) {
            return "BMP图片";
        }
        if (fileExt.equalsIgnoreCase(".htm")
                || fileExt.equalsIgnoreCase(".html")) {
            return "HTML网页";
        }
        if (fileExt.equalsIgnoreCase(".jpg")
                || fileExt.equalsIgnoreCase(".jpeg")) {
            return "JPEG图片";
        }
        if (fileExt.equalsIgnoreCase(".png")) {
            return "PNG图片";
        }
        if (fileExt.equalsIgnoreCase(".doc")) {
            return "Word文档";
        }
        if (fileExt.equalsIgnoreCase(".xls")) {
            return "Excel表格";
        }
        if (fileExt.equalsIgnoreCase(".txt")) {
            return "TXT文本";
        }
        if (fileExt.equalsIgnoreCase(".dll")
                || fileExt.equalsIgnoreCase(".ocx")) {
            return "DLL动态链接库";
        }
        if (fileExt.equalsIgnoreCase(".bat")
                || fileExt.equalsIgnoreCase(".cmd")) {
            return "BAT批处理文件";
        }
        if (fileExt.equalsIgnoreCase(".exe")
                || fileExt.equalsIgnoreCase(".com")) {
            return "EXE可执行文件";
        }
        if (fileExt.equalsIgnoreCase(".pdf")) {
            return "PDF文档";
        }
        if (fileExt.equalsIgnoreCase(".class")) {
            return "Java类文件";
        }
        if (fileExt.equalsIgnoreCase(".jsp")) {
            return "Java Sever Page动态网页";
        }
        if (fileExt.equalsIgnoreCase(".xml")) {
            return "XML文件";
        }
        if (fileExt.equalsIgnoreCase(".js")) {
            return "JavaScript文件";
        }
        if (fileExt.equalsIgnoreCase(".css")) {
            return "级联样式表";
        }
        if (fileExt.equalsIgnoreCase(".rar")
                || fileExt.equalsIgnoreCase(".zip")
                || fileExt.equalsIgnoreCase(".tar")
                || fileExt.equalsIgnoreCase(".cab")
                || fileExt.equalsIgnoreCase(".arj")
                || fileExt.equalsIgnoreCase(".lzh")
                || fileExt.equalsIgnoreCase(".ace")
                || fileExt.equalsIgnoreCase(".gzip")) {
            return fileExt + "压缩文件";
        }
        return fileExt + "文件";
    }

    public static void printError(Writer out, String path, String error)
            throws Exception{
        printErrorMsg(out, path, new String[] {error});
    }

    public static void printTrasError(JspWriter out, String path, String title,
            String error) throws Exception{
        String imgPath = null;
        if (path != null && path.length() > 0) {
            imgPath = path;
        } else {
            imgPath = "";
        }
        out.println("<table width=100% height=100% border=0 cellspacing=0 cellpadding=0 style='border:0px;background-color:#FFFFFF' >");
        out.println("<tr><td height=25 align=left valign=middle nowrap ><table width=100% border=0 cellspacing=0 cellpadding=0><tr>");
        out.println("<td class=ltborder><img src="
                + imgPath
                + "tras/images/null.gif width=14 height=1></td><td width=99% valign=middle class=borderbg>");
        out.println(title);
        out.println("</td><td class=rtborder><img src=tras/images/null.gif width=5 height=1></td></tr></table></td></tr> ");

        out.print("<tr><td align=center valign=top bgColor=#ffffff class=bordermain>");
        out.print("<table width=100% border=0 cellPadding=0 cellSpacing=4 ><tr valign=top><td class=L1pt9><img src="
                + imgPath + "tras/images/error.gif></td><td>" + error + "</td>");
        out.print("</tr><tr align=center valign=middle><td colspan=2><hr class=cutBg size=1 noshade></td></tr>");
        out.print("<tr align=center valign=middle><td height=25 colspan=2 nowrap class=L1pt9>");
        out.print("<a href=\"javascript:top.window.close()\" class=boxLink><img src="
                + imgPath + "images/cancel.gif border=0>关 闭</a></td>");
        out.print("</tr></table></td></tr></table></td></tr></table>");
    }

    public static void printInfo(Writer out, String path, String info)
            throws Exception{
        printInfoMsg(out, path, new String[] {info});
    }

    // 输出提示信息，比如:操作成功之类的信息。如要输出出错信息，请用printErrorMsg()方法。
    public static void printInfoMsg(Writer out, String path, String[] msgs)
            throws Exception{
        if (path == null) path = "";
        // 用此类可通过安全检查，实际包了一个StringBuffer,此流不需要关闭
        StringWriter sw = new StringWriter();
        sw.write("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\r\n");
        sw.write("  <tr>\r\n");
        sw.write("    <td width=\"1%\" valign=\"top\" nowrap><img src=\""
                + path
                + "images/success.gif\" width=\"77\" height=\"91\" hspace=\"4\" vspace=\"4\"></td>\r\n");
        sw.write("    <td valign=\"top\" style=\"padding:5px;line-height:18px;\">\r\n");
        for (int i = 0; i < msgs.length; i++) {
            // 信息已增加换行，原信息如果有换行，通过format2HtmlStr会转换成&lt;这样的字符
            sw.write(formatHtmlMsg(msgs[i]) + "<br>\r\n");
        }
        sw.write("    </td>\r\n");
        sw.write("  </tr>\r\n");
        sw.write("</table>\r\n");
        sw.write("<script>\r\n");
        sw.write("document.createStyleSheet('" + path
                + "theme/blue/css/main.css');\r\n");
        sw.write("document.createStyleSheet('" + path
                + "theme/blue/css/global.css');\r\n");
        sw.write("</script>\r\n");
        out.write(sw.toString());

    }

    public static void printException(Writer out, String path, Exception error)
            throws Exception{
        String msg = error.getMessage();
        if (error instanceof NullPointerException || error.getMessage() == null
                || error.getMessage().length() == 0) msg = "空指针异常!";
        printErrorMsg(out, path, new String[] {msg}, error);
//        IReportServer.handleException(error);
        throw error;
    }

    public static void printError(Writer out, String path, String caption,
            String error) throws Exception{
        printErrorMsg(out, path, new String[] {caption, error});
    }

    public static void printError(Writer out, String path, String returnValue,
            String caption, String error) throws Exception{
        printErrorMsg(out, path, new String[] {caption, error});
    }

    public static void print2textarea(JspWriter out, String path,
            String caption, String error) throws Exception{
        String imgPath = null, resultValue = null;
        if (path != null && path.length() > 0) {
            imgPath = path;
        } else {
            imgPath = "";
        }
        out.print("<table width=100% height=100% border=0 align=center cellpadding=0 cellspacing=2 class=mainBg>");
        out.print("<tr><td height=30 align=left valign=middle class=t9b>"
                + caption + "</td></tr>");
        out.print("<tr><td align=center valign=top class=formBg>");
        out.print("<table width=100% border=0 cellPadding=0 cellSpacing=4><tr valign=top><td colspan=2 class=L1pt9>");
        out.print("<textarea name=textarea readonly cols=76 rows=12 style=font-size:9pt;border-width:1px;background-color:#eeeee2>"
                + error + "</textarea>");
        out.print("</td></tr><tr align=center valign=middle><td colspan=2><hr class=cutBg size=1 noshade></td></tr>");
        out.print("<tr align=center valign=middle><td height=25 colspan=2 nowrap class=L1pt9>");
        out.print("<a href=\"javascript:top.window.close()\" class=boxLink><img src="
                + imgPath + "images/open.gif border=0>关 闭</a></td>");
        out.print("</tr></table></td></tr></table>");
    }

    public static void exception2textarea(JspWriter out, String path,
            String caption, Exception error) throws Exception{
        String imgPath = null;
        if (path != null && path.length() > 0) {
            imgPath = path;
        } else {
            imgPath = "";
        }
        StringWriter sw = new StringWriter();
        sw.write("<table width=100% height=100% border=0 align=center cellpadding=0 cellspacing=2 class=mainBg>");
        sw.write("<tr><td height=30 align=left valign=middle class=t9b>"
                + caption + "</td></tr>");
        sw.write("<tr><td align=center valign=top class=formBg>");
        sw.write("<table width=100% border=0 cellPadding=0 cellSpacing=4><tr valign=top><td colspan=2 class=L1pt9>");
        sw.write("<textarea name=textarea readonly cols=76 rows=12 style=font-size:9pt;border-width:1px;background-color:#eeeee2>"
                + error.getMessage() + "</textarea>");
        sw.write("</td></tr><tr align=center valign=middle><td colspan=2><hr class=cutBg size=1 noshade></td></tr>");
        sw.write("<tr align=center valign=middle><td height=25 colspan=2 nowrap class=L1pt9>");
        sw.write("<a href=\"javascript:top.window.close()\" class=boxLink><img src="
                + imgPath + "images/open.gif border=0>关 闭</a></td>");
        sw.write("</tr></table></td></tr></table>");
        out.print(sw.toString());
//        IReportServer.handleException(error);
        throw error;
    }

    /**
     * 设置Cookie，name Cookie名，value Cookie值，time Cookie存活周期（秒为单位）
     * 
     * @param response
     *            HttpServletResponse
     * @param name
     *            String
     * @param value
     *            String
     * @param time
     *            int
     * @throws Exception
     */
    public final static int ONE_Y = 60 * 60 * 24 * 365; // 一年

    public final static int ONE_M = 60 * 60 * 24 * 31; // 一月

    public final static int ONE_W = 60 * 60 * 24 * 7; // 一周

    public final static int ONE_D = 60 * 60 * 24; // 一天

    public static void setCookie(HttpServletResponse response, String name,
            String value, int time) throws Exception{
        Cookie _cookie = new Cookie(name, value);
        _cookie.setMaxAge(time);
        response.addCookie(_cookie);
    }

    /**
     * 取Cookie值
     * 
     * @param request
     *            HttpServletRequest
     * @param name
     *            String
     * @throws Exception
     * @return String
     */
    public static String getCookie(HttpServletRequest request, String name)
            throws Exception{
        Cookie cookies[] = request.getCookies();
        Cookie sCookie = null;
        String sname = null;
        String cookieId = null, cookieTaskId = null;
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                sCookie = cookies[i];
                sname = sCookie.getName();
                if (sname.equals(name)) {
                    String result = sCookie.getValue();
                    return StrYn.isNull(result) ? "" : result;
                }
            }
        }
        return "";
    }

    /**
     * 打印调用堆栈
     * 
     * @return
     */
    public static final String printCallStack(){
        String result = ArrYn.array2Str(new Throwable().getStackTrace(),
                "\r\n");
        System.out.println("调试信息:\r\n" + result);
        System.out.println();
        return result;
    }

    public static void printExceptionStackTrace(Writer out, Exception e)
            throws java.io.IOException{
        out.write(e.toString());
        out.write("<br>");
        StackTraceElement[] trace = e.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            out.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + trace[i]);
            out.write("<br>");
        }

        /*
         * Throwable ourCause = e.getCause(); if (ourCause != null)
         * ourCause.printStackTraceAsCause(s, trace);
         */
    }

    public static void setProperty(HttpServletRequest req, Login login,
            String valName, String propertyName, String defaultVal)
            throws Exception{
        String _type = req.getParameter(valName);
        if (StrYn.isNull(_type)) {
            _type = (String) login.getProperty(propertyName);
            _type = StrYn.isNull(_type) ? defaultVal : _type;
        }
        login.setProperty(propertyName, _type);
    }

    public static String getProperty(Login login, String propertyName,
            String defaultVal) throws Exception{
        String result = (String) login.getProperty(propertyName);
        return StrYn.isNull(result) ? defaultVal : result;
    }

    /**
     * \r\n,\t\n,\r,\n转换成<br>
     * 
     * @param str
     *            String
     * @throws Exception
     * @return String
     */
    public static final String conversion2br(String str) throws Exception{
        if (StrYn.isNull(str)) return "";
        String swap = str.replaceAll("\r\n", "<br>");
        return swap.replaceAll("\r", "<br>").replaceAll("\n", "<br>")
                .replaceAll("\t\n", "<br>");
    }

    /**
     * 格式化文件大小的输出
     * 
     * @param fs
     *            long
     * @throws Exception
     * @return String
     */
    public static final String formatFileSize(long fs) throws Exception{
        return fs > 1024 ? (fs > 1024 * 1024 ? (fs > 1024 * 1024 * 1024 ? fs
                / (1024 * 1024 * 1024) + "GB" : fs / (1024 * 1024) + " MB")
                : (fs / 1024) + " KB") : fs + " B ";
    }

    public static final void getFile4Database(HttpServletResponse response,
            JspWriter out, String name, byte[] bytes) throws Exception{
        if (StrYn.isNull(name)) {
            throw new Exception("未指定文件名.");
        }
        out.clearBuffer();
        response.reset();
        response.addHeader("Content-Disposition", "attachment;filename=\""
                + java.net.URLEncoder.encode(name, "UTF-8") + "\"");
        response.addHeader("Content-transfer-Encoding", "binary");
        response.setContentType("application/octet-stream");
        OutputStream o = response.getOutputStream();
        o.write(bytes);
        o.flush();
        o.close();
    }

    public static final void getFile(HttpServletResponse response,
            JspWriter out, String fn) throws Exception{
        if (StrYn.isNull(fn)) {
            throw new Exception("未指定文件.");
        }
        out.clearBuffer();
        response.reset();
        // 流可能未被关闭，被安全工具检出，故修改
        FileInputStream fi = new FileInputStream(fn);
        try {
            int _fiSize = fi.available();

            response.setContentType(JspYn.getContentType(FileYn
                    .extractFileExt(fn)));
            response.setContentLength(_fiSize);
            String path = "filename=\"" + java.net.URLEncoder.encode(fn, "UTF-8") + "\"";
            path = SecurityYn.checkHttpHeader(null, path);
            response.setHeader("Content-Disposition", path);
            OutputStream ostr = response.getOutputStream();
            StmYn.stmCopyFrom(fi, ostr, _fiSize);
            ostr.flush();
        } finally {
            fi.close();
        }
    }

    public static Login getLogin(HttpServletRequest req){
        return getLogin(req, true);
    }

    public static Login getLogin(HttpServletRequest req, boolean createit){
        com.esen.platform.login.Login login = ActionFunc.getLogin(req);
        Login eiLogin = login.getProperty("EI.LOGIN", Login.class);
        if (eiLogin == null) {
            synchronized (login) {
                eiLogin = new Login(login);
                login.setProperty("EI.LOGIN", eiLogin);
            }
        }
        return eiLogin;
    }

    public static Login getLogin(HttpSession session){
        if (session == null) return null;
        synchronized (session) {
            return (Login) session.getAttribute("report_login");
        }
    }

    /**
     * ip校验
     * 
     * @param s
     * @return
     */
    final static String regex = "(((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))";

    final static Pattern ipRegex = Pattern.compile(regex);

    public static boolean isIpAddress(String s){
        Matcher m = ipRegex.matcher(s);
        return m.matches();
    }

    /**
     * 获得客户端的实际ip地址,如果是127.0.0.1,也要转换为实际的地址 主要用在日志中,记录日志的生成机器
     */
    public static String getRemoteAddress(HttpServletRequest req){
        // 当通过apache反向代理访问bi时，所有的RemoteAddress都是反向代理服务器的IP，我们此时只能种http头中的x-forwarded-for获取到客户端真正的IP
        // host = 192.168.1.30:8080
        // accept = */*
        // accept-language = zh-cn
        // ua-cpu = x86
        // accept-encoding = gzip, deflate
        // user-agent = Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET
        // CLR 2.0.50727; InfoPath.1)
        // cookie = JSESSIONID=DB38FFC34FEDD3B360247DAC638ABD9E;
        // com.esen.irpt.web.login.user=demo002
        // via = 1.1 delsvr
        // x-forwarded-for = 58.49.52.114
        // x-forwarded-host = 58.49.52.114
        // x-forwarded-server = delsvr
        // connection = Keep-Alive

        String ip = req.getHeader("x-forwarded-for");
        if (StrYn.isNull(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("Proxy-Client-IP");
        }
        if (StrYn.isNull(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("WL-Proxy-Client-IP");
        }
        if (StrYn.isNull(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
            if ("127.0.0.1".equalsIgnoreCase(ip)
                    || "localhost".equalsIgnoreCase(ip)
                    || "0:0:0:0:0:0:0:1".equalsIgnoreCase(ip)) {// windows7上，还可能是0:0:0:0:0:0:0:1
                try {
                    // edit by chenlan 2013.5.3 安全改造，Often Misused:
                    // Authentication
                    ip = SecurityUtil.filter(InetAddress.getLocalHost()
                            .getHostAddress());
                } catch (Exception e) {
                    ExceptionHandler.rethrowRuntimeException(e);
                }
            }
        }
        // 如果是多级代理的话，可能X-Forwarded-For：192.168.1.110， 192.168.1.120，
        // 192.168.1.130， 192.168.1.100，要取其中第１个
        if (!StrYn.isNull(ip)) {
            String[] ips = ip.split(",");
            if (ips.length > 1) ip = ips[0].trim();
        }
        ip = SecurityUtil.checkParam(null, ip, SecurityUtil.IRPT_IP, false);
        return ip;
    }

    // 获取contextPath
    public static String getContextPath(HttpServletRequest request){
        String contextPath = request.getContextPath();
        contextPath = StrYn.isNull(contextPath) ? "" : (contextPath
                .charAt(contextPath.length() - 1) == '/' ? contextPath
                : contextPath + "/");
        return contextPath;
    }

    public static String getRealPath(HttpServletRequest request){
        String iRealPath = getContextPath(request) + "i/";
        iRealPath = StrYn.isNull(iRealPath) ? "/" : iRealPath;
        if (iRealPath != null && !iRealPath.startsWith("/")) {
            iRealPath = "/" + iRealPath;
        }
        return iRealPath;
    }

    public static final String getServerUrlPath(String serverpath,
            String urlpath, String defpath){
        if (StrYn.isNull(urlpath)) {
            return getUrlPath(serverpath, defpath);
        }
        return getUrlPath(serverpath, urlpath);
    }

    private static String getUrlPath(String serverpath, String path){
        if (path.length() > 8) {
            String prefix = path.substring(0, 8).toLowerCase();
            if (prefix.indexOf("://") == -1) return serverpath + path;
            return path;
        }
        return serverpath + path;
    }

    /**
     * 根据当前时间，登录者，生成问候信息。
     */
    public static String getHelloMsg(Login login){
        if (login.isLogined()) {
            return (login.getLoginName());
        } else {
            return ("");
        }

    }

    /**
     * 显示出错信息。
     * 
     * @param out
     * @param path
     *            图片相对路径。
     * @param msgs
     *            要显示的信息。每条显示在一行。 信息的格式中支持超链接。格式是: new String[]{ "你没有查看任务组的权限。",
     *            "如要开通相关权限，请联系<a href='#' onclick='msgto(admin)'>管理员</a>开通" }
     *            由于功能上支持超链接，故不能进行XSS检查，调用者请慎用，要确保信息来源安全。（异常信息可能会被XSS攻击）
     */
    public static void printErrorMsg(Writer out, String path, String[] msgs,
            Exception ex) throws Exception{
        if (path == null) path = "";
        ex = SecurityUtil.checkException(ex);

        String exDetail = ex != null ? com.esen.util.StrFunc
                .exception2str((Exception) ex)
                .replaceAll("\r\n", "<br>")
                .replaceAll("\n", "<br>")
                .replaceAll("\t",
                        "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                .replace('\"', '\'') : null;
        StringWriter sw = new StringWriter();// 用此对象可通过安全检查，此流不需要关闭
        sw.write("<table width=\"100%\" height=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\r\n");
        sw.write("  <tr height=1%>\r\n");
        sw.write("    <td width=\"1%\" valign=\"top\" nowrap><img src=\""
                + path
                + "images/error.gif\" width=\"77\" height=\"91\" hspace=\"4\" vspace=\"4\"></td>\r\n");
        sw.write("    <td valign=\"top\" style=\"padding:5px;line-height:18px;\">\r\n");
        for (int i = 0; i < msgs.length; i++) {
            // 由于功能需求上，需要支持超链接格式，故这个地方不能进行XSS检测。
            out.write(msgs[i] + "<br>\r\n");
        }
        sw.write("    </td>\r\n");
        sw.write("  </tr>\r\n");
        if (!StrYn.isNull(exDetail)) {
            sw.write("<tr height=1%>"
                    + "<td colspan=2>"
                    + "<div style=\"cursor:pointer\" onclick=\"if(errDiv.style.display=='none'){errDiv.style.display='block';arrowImg.src='"
                    + path
                    + "tras/images/arrow_up.gif'}else{errDiv.style.display='none';arrowImg.src='"
                    + path
                    + "tras/images/arrow_down.gif'}\">"
                    + "<img id=\"arrowImg\" src=\""
                    + path
                    + "tras/images/arrow_down.gif\">显示错误详情</div>"
                    + "</td>"
                    + "</tr>"
                    + "<tr height=98%>"
                    + "<td colspan=2>"
                    + "<div id=\"errDiv\" style=\"display:none;padding:10px;position: relative; width: 100%; height: 100%; overflow: hidden;\">"
                    + "<div style=\"border:1px solid gray;position: absolute;background-color:white; width: 98%; height: 95%; overflow: auto;\" class=t9b>"
                    + exDetail + "</div>" + "</div>" + "</td>" + "</tr>");
        }

        sw.write("</table>\r\n");
        sw.write("<script>\r\n");
        sw.write("document.createStyleSheet('" + path
                + "theme/blue/css/main.css');\r\n");
        sw.write("document.createStyleSheet('" + path
                + "theme/blue/css/global.css');\r\n");
        sw.write("</script>\r\n");
        out.write(sw.toString());
    }

    /*
     * 传换成前台显示的信息字符串，要避免XSS攻击 需要把字符串转成HTML
     */
    private static String formatHtmlMsg(String msg){
        if (msg == null) return "";
        try {
            msg = SecurityUtil.checkParam(msg, "返回信息不合法");
        } catch (IllegalArgumentException ex) {
            // 如果存在XSS攻击的，转成HTML
            msg = StrYn.format2HtmlStr(msg);
        }
        return msg;
    }

    public static void printErrorMsg(Writer out, String path, String[] msgs)
            throws Exception{
        printErrorMsg(out, path, msgs, null);
    }

    /**
     * 获取指定ID的对应的级次的字段信息.
     * 如果id和btype与Login对象内的CurrBbh相当同,则使用的login.getCurrBbh(),反之则从数据库中查找该户
     * 
     * @param id
     *            报表户信息,不能为空
     * @param btype
     *            报表户类型,不能为空
     * @param group
     *            任务组名,不能为空
     * @param login
     *            Login对象,可以为空
     * @param conn
     *            数据库连接,可以为空
     * @return 可能返回null
     * @throws Exception
     * @throws Exception
     */
    public static final String genUpIdFields(String id, String btype,
            String bbhlistname, Login login, Connection conn) throws Exception{
        if (StrYn.isNull(id) || StrYn.isNull(bbhlistname)
                || StrYn.isNull(btype)) return null;

        BbhList bbhlist = BbhList.getBbhList(bbhlistname);

        Bbh bbh = bbhlist.getBbh(conn, id, btype);

        if (bbh == null) return null;

        return genUpIdFields(bbh);
    }

    /**
     * 获取指定ID的对应的级次的字段信息.
     * 如果id和btype与Login对象内的CurrBbh相当同,则使用的login.getCurrBbh(),反之则从数据库中查找该户
     * 
     * @param id
     *            报表户信息,不能为空
     * @param btype
     *            报表户类型,不能为空
     * @param group
     *            任务组名,不能为空
     * @param login
     *            Login对象,可以为空
     * @param conn
     *            数据库连接,可以为空
     * @return 可能返回null
     * @throws Exception
     */
    public static final String genUpIdFields(Bbh bbh){
        if (bbh == null) return null;
        String[] fileds = BbhListFields.getUpidFields();
        if (fileds == null || fileds.length == 0) return null;
        int level = bbh.getLevel();
        return level < fileds.length && level > -1 ? fileds[level] : null;
    }

    public static void main(String[] args){
        System.out.println(formatFileName("c:\\ww \\ww \\ww"));
        System.out.println(formatFileName("c:" + File.separator + " r"
                + File.separator));
    }

    /**
     * @param path
     * @return 格式话文件路径,将 C:\Documents and Settings\Administrator\rootdir\ 转化为
     *         C:\\Documents and Settings\\Administrator\\rootdir\\ 不处理空格.
     */
    public static String formatFileName(String path){
        if (StrYn.isNull(path)) return path;
        char sr = (File.separatorChar == '\\') ? '/' : '\\';
        path = path.replace(sr, File.separatorChar);
        char[] cs = path.toCharArray();
        char[] result = new char[cs.length * 2];
        int p = 0;
        for (int i = 0; i < cs.length; i++) {
            result[p++] = cs[i];
            if (cs[i] == '\\') {
                result[p++] = '\\';
                if (i < cs.length - 1 && cs[i + 1] == '\\') {
                    i++;
                }
            }
        }
        return new String(result, 0, p);
    }

    /**
     * 在session 中获取界面选择的报表户
     * 
     * @param login
     * @param tsk
     * @param conn
     * @return
     * @throws Exception
     */
    public static final Bbh[] getSelectedBbhs(Login login, Task tsk,
            Connection conn) throws Exception{
        if (login == null || tsk == null) return null;
        BbhBookMarks marks = BbhBookMarkUtil.getBbhBookMarks(login,
                tsk.getServerId());
        if (marks == null) return null;
        List bbhs = new ArrayList();
        BbhInfoIterator it = marks.getIterator(conn);
        try {
            while (it.hasNext()) {
                IBbhInfo info = it.next();
                if (info != null) bbhs.add(info.getBbh());
            }
        } finally {
            it.close();
        }

        return (Bbh[]) (bbhs.size() == 0 ? null : bbhs.toArray(new Bbh[0]));
    }

    public static final Bbh[] getSelectedUserInfos(Login login, Task tsk,
            Connection conn) throws Exception{
        if (login == null || tsk == null) return null;
        String[] selectBbhs = BbhBookMarkUtil.getSelBbhs(login,
                tsk.getServerId());
        if (selectBbhs == null || selectBbhs.length == 0) return null;
        List bbhs = new ArrayList();
        BbhList bbhlist = tsk.getBbhList();
        boolean needclose = false;
        if (conn == null) {
            conn = IReportServer.getConnection();
            needclose = true;
        }
        try {
            for (int i = 0; i < selectBbhs.length; i++) {
                if (StrYn.isNull(selectBbhs[i])) continue;
                String idtype[] = selectBbhs[i].split(",");
                String userid = idtype[0];
                String btype = idtype.length == 2 ? idtype[1]
                        : Bbh.BBH_TYPE_JCH;
                Bbh bbh = bbhlist.getBbh(conn, userid, btype);
                if (bbh != null) bbhs.add(bbh);
            }
        } finally {
            if (needclose) conn.close();
        }
        if (bbhs.size() == 0) return null;
        Bbh[] bbhsa = new Bbh[bbhs.size()];
        bbhs.toArray(bbhsa);
        return bbhsa;
    }

    /**
     * 根据BbhBrowser获取报表户对象
     * 
     * @param bb
     *            BbhBrowser实例
     * @return 报表户对象
     * @throws Exception
     */
    public static final Bbh getBrowBbh(IBbhInfo info) throws Exception{
        return info.getBbh();
    }

    /**
     * 根据upid使用bbhBrowse获取已经上报的下级报表户
     * 
     * @param brow
     *            bbhBrowse实例
     * @param bbq
     *            报表期
     * @param upid
     *            上级ID
     * @param rec
     *            是否递归
     * @param bbhList
     *            报表户列表容器
     * @throws Exception
     */
    private static void collectionBrowBbh(BbhBrowser brow, String bbq,
            String upid, boolean rec, List bbhList) throws Exception{
        BbhBrowserParam param = brow.getBbhBrowserParam();
        param.setBbq(bbq).setUpid(upid).setRec(rec);
        BbhBrowserResult result = brow.browse(0, -1);
        try {
            while (result.hasNext()) {
                bbhList.add(getBrowBbh(result.next()));
            }
        } finally {
            result.close();
        }
    }

    public static void pageWaiting(JspWriter out, String serverPath)
            throws IOException{
        // 页面加载前的等待画面，此段代码放在<body>标签之前。所有<script>标签要在此段代码之后
        out.println("<div id=\"pageWaitDiv\" style=\"position:absolute;width:100%;height:100%;\">");
        out.println("<table border=\"0\" style=\"position:absolute;width:100%;height:100%;\">");
        out.println("<tr><td align=\"center\" vAlign=\"middle\"><table border=\"0\"><tr><td><span>页面加载中...</span></td></tr>");
        out.println("<tr><td align=\"center\" vAlign=\"middle\"><img src=\""
                + (StrYn.isNull(serverPath) ? "" : serverPath)
                + "images/loading.gif\"></td></tr>");
        out.println("</table></td></tr></table>");
        out.println("</div>");
        out.flush();
        out.println("<script>");
        out.println("window.attachEvent(\"onload\",function(){try{document.getElementById(\"pageWaitDiv\").parentNode.removeChild(document.getElementById(\"pageWaitDiv\"));}catch(e){}});");
        out.println("</script>");
        out.flush();
    }

    /**
     * 在页面中引用定制的js文件。
     * 
     * @param out
     * @param relPath
     *            oemJsPath相对于当前页面路径的相对路径。
     * @param jsFile
     *            含相对路径的js文件名(相对于oemJsPath下js文件夹的路径)
     * @throws IOException
     */
    public static void includeOemJs(JspWriter out, String relPath, String jsFile)
            throws IOException{
        String oemJsPath = ReportProperties.getProperty("oemJsPath", "");
        relPath = StrYn.ensureEndWith(relPath, "/");
        oemJsPath = StrYn.ensureEndWith(oemJsPath, "/");
        String oemJsFile = relPath + oemJsPath + "js/" + jsFile;
        // if( new File(oemJsFile).exists() ){//无法检测是否存在，因为不是绝对路径。
        out.println("<!-- BEGIN: 页面定制js函数，扩展页面功能-->");
        out.println("<script src=\"" + oemJsFile + "\"></script>");
        out.println("<!-- END -->");
        // }
    }

    public static Login makeAdminLogin(){
        return new Login(com.esen.platform.login.Login.makeAdminUserLogin());
    }

    public static Login makeUserLogin(String userid){
        return new Login(com.esen.platform.login.Login.makeUserLogin(userid));
    }

    /**
     * 根据用户名密码创建一个LOGIN,当正确的时候会返回一个Login,它会覆盖你当前会话的Login</br>
     * 因此你只需要创建一次即可,后续的获取可以通过{@link JspYn#getLogin(HttpServletRequest)} 来获取</br>
     * 
     * 如果错误则会抛出以下异常</br> <li>登录失败 用户名或者密码错误</li> <li>登录失败 机构被禁用</li> <li>登录失败
     * 用户被禁用</li> <li>登录失败 用户过期</li>
     * 
     * @param request
     * @param userid
     * @param password
     * @return
     */
    public static Login makeUserLogin(HttpServletRequest request,
            String userid, String password){
        com.esen.platform.login.Login login = ActionFunc.getLogin(request);
        int type = login.loginServer(request, userid, password);
        if (type == com.esen.platform.login.Login.STATE_SUC) {
            Login eiLogin = login.getProperty("EI.LOGIN", Login.class);
            if (eiLogin == null || !userid.equals(eiLogin.getLoginId())) {
                synchronized (login) {
                    eiLogin = new Login(login);
                    login.setProperty("EI.LOGIN", eiLogin);
                }
            }
            return eiLogin;
        }
        throw LoginFailedException.createUserLoginException(type);
    }
}