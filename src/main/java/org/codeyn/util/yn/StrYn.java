package org.codeyn.util.yn;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codeyn.util.CnToSpell;
import org.codeyn.util.MathUtil;
import org.codeyn.util.exception.ExceptionHandler;
import org.codeyn.util.i18n.HanziFormat;
import org.codeyn.util.i18n.I18N;


public final class StrYn{

    public static final long G = 1024 * 1024 * 1024L;// GB 1073741824
    public static final long MB = 1024 * 1024L;// MB
    public static final String UTF8 = "UTF-8";
    public static final String GB2312 = "GB2312";
    public static final String GBK = "GBK";
    public static final String ISO8859_1 = "ISO-8859-1";
    public static final String CRLF = "\r\n";
    public static final String STR_WHITE_SPACE = " ";
    public static final String[] BLANK_STR_ARRAY = new String[] {};

    /**
     * 未知对象。这是一个标志对象，通常在延迟加载属性时判断“属性是否已经加载”。 比如： Object
     * drillcell=StrFunc.UNINITED_OBJECT; //初始设置为StrFunc.UNINITED_OBJECT
     * 
     * function Object getDrillCell(){ if(drillcell == StrFunc.UNINITED_OBJECT){
     * //判断是否已经加载 drillcell = xxxx; //开始加载 } return drillcell; }
     * 
     */
    public static final Object UNINITED_OBJECT = new Object();

    public static final Comparator comparator = new Comparator(){
        public int compare(final Object obj1, final Object obj2){
            return obj1.toString().compareTo(obj2.toString());
        }
    };

    /**
     * 构造函数为私有表示此类不能被实例化
     */
    private StrYn(){

    }

    /**
     * 将字串按GBK方式编码。(常用于输出到流时用到)
     * 
     * @param str
     *            要编码的字符串
     * @return gbk编码串
     * @throws UnsupportedEncodingException
     *             转码可能会抛异常
     */
    public static String encoding2GBK(final String str)
            throws UnsupportedEncodingException{
        if (str != null) {
            return new String(str.getBytes("GBK"));
        } else {
            return str;
        }
    }

    /**
     * 将字符串按GBK方式解码.(常用于从流输入时用到)
     * 
     * @deprecated 此函数有隐患，不推荐使用
     * @param str
     *            要编码的字符串
     * @return gbk编码串
     * @throws UnsupportedEncodingException
     *             转码可能会抛异常
     */
    public static String decoding2GBK(final String str)
            throws UnsupportedEncodingException{
        if (str != null) {
            return new String(str.getBytes(), "GBK");
        } else {
            return str;
        }
    }

    /**
     * 将字符串按UTF-8方式解码.(常用于从客户端使用get方式提交时解析参数)
     * 
     * @param str
     *            iso-8859-1编码方式的字符串
     * @return utf-8编码方式的字符串
     * @throws UnsupportedEncodingException
     *             转码可能会抛异常
     */
    public static String decoding2UTF8(final String str)
            throws UnsupportedEncodingException{
        return str == null ? null : new String(str.getBytes("ISO-8859-1"),
                "UTF-8");
    }

    /**
     * 与OldPassword相关的key
     */
    private static final byte[] key = "leafbellalyz".getBytes();

    /**
     * 加密一个byte数组，b是要加密的byte数组，i是加密种子，解密时必须传递和加密时同样的种子才能还原密文为明文
     * 返回一个加密后的数组，返回的数组的长度和传入的长度一致
     * 
     * @param b
     *            加密的byte数组
     * @param i
     *            i是加密种子
     * @return 加密后的byte数组
     */
    public static byte[] encryptBytes(final byte[] b, final int i){
        int startKey = i * 12345;
        int mulKey = startKey >>> 8;
        int addKey = startKey >>> 16;
        int j;
        for (j = 0; j < b.length; j++) {
            b[j] = (byte) (b[j] ^ (startKey >>> 8));
            startKey = (b[j] + startKey) * mulKey + addKey;
        }
        return b;
    }

    /**
     * 解密encryptBytes函数加密的结果，i是加密种子，必须传递和加密时同样的种子才能解密
     * 
     * @param b
     *            密文
     * @param i
     *            i是解密种子
     * @return 明文
     */
    public static byte[] decryptBytes(final byte[] b, final int i){
        byte temp;
        int startKey = i * 12345;
        int mulKey = startKey >>> 8;
        int addKey = startKey >>> 16;
        int j;
        for (j = 0; j < b.length; j++) {
            temp = b[j];
            b[j] = (byte) (b[j] ^ (startKey >>> 8));
            startKey = (temp + startKey) * mulKey + addKey;
        }
        return b;
    }

    /**
     * 用encryptPlainPassword方法加密的密文前缀
     */
    private static final String ENCRYPT_PLAIN_PASSWORD_PREFIX = "[$`.!encrypt:]";

    /**
     * 加密一个密码，与encryptPassword不同的是，此函数是可逆的，即密文可以解密为明文， 而且此函数的加密结果长度和输入的密码的长度是相关的
     * 传入的密文如果是null此函数自动将其转换为""
     * 
     * @param plainpw
     *            要加密的字串
     * @return 加密后的字串
     */
    public static String encryptPlainPassword(String plainpw){
        try {
            if (isEncryptedPlainPassword(plainpw)) return plainpw;
            if (plainpw == null) {
                plainpw = "";
            }
            byte[] b = plainpw.getBytes(StrYn.UTF8);
            b = encryptBytes(b, 123);
            return ENCRYPT_PLAIN_PASSWORD_PREFIX + StrYn.bytesToHexString(b);
        } catch (UnsupportedEncodingException ex) {
            return plainpw;
        }
    }

    /**
     * 判断是否为加密后的密码
     * 
     * @param pw
     *            需要判断的字串
     * @return 是加密后的密码返回true，反之false
     */
    public static boolean isEncryptedPlainPassword(final String pw){
        return pw != null
                && pw.length() >= ENCRYPT_PLAIN_PASSWORD_PREFIX.length()
                && pw.startsWith(ENCRYPT_PLAIN_PASSWORD_PREFIX);
    }

    /**
     * 加密一个密码，此函数是可逆的，即密文可以解密为明文
     * 
     * @param encryptedpw
     *            要加密的密码
     * @return 密文
     */
    public static String decryptPlainPassword(final String encryptedpw){
        if (encryptedpw == null) return "";// 参数有可能为null，此时直接返回空串。

        try {
            if (!isEncryptedPlainPassword(encryptedpw)) return encryptedpw;
            byte[] b = StrYn.hexStringToBytes(encryptedpw
                    .substring(ENCRYPT_PLAIN_PASSWORD_PREFIX.length()));
            if (b != null) {
                b = decryptBytes(b, 123);
                return new String(b, UTF8);
            } else {
                return "";
            }
        } catch (UnsupportedEncodingException ex) {
            return encryptedpw;
        }
    }

    /**
     * 
     static int ROUND_CEILING Rounding mode to round towards positive
     * infinity. 向正无穷方向舍入
     * 
     * static int ROUND_DOWN Rounding mode to round towards zero. 向零方向舍入
     * 
     * static int ROUND_FLOOR Rounding mode to round towards negative infinity.
     * 向负无穷方向舍入
     * 
     * static int ROUND_HALF_DOWN Rounding mode to round towards
     * "nearest neighbor" unless both neighbors are equidistant, in which case
     * round down. 向（距离）最近的一边舍入，除非两边（的距离）是相等,如果是这样，向下舍入, 例如1.55 保留一位小数结果为1.5
     * 
     * static int ROUND_HALF_EVEN Rounding mode to round towards the
     * "nearest neighbor" unless both neighbors are equidistant, in which case,
     * round towards the even neighbor.
     * 向（距离）最近的一边舍入，除非两边（的距离）是相等,如果是这样，如果保留位数是奇数，使用ROUND_HALF_UP
     * ，如果是偶数，使用ROUND_HALF_DOWN
     * 
     * static int ROUND_HALF_UP Rounding mode to round towards
     * "nearest neighbor" unless both neighbors are equidistant, in which case
     * round up. 向（距离）最近的一边舍入，除非两边（的距离）是相等,如果是这样，向上舍入, 1.55保留一位小数结果为1.6
     * 
     * static int ROUND_UNNECESSARYRounding mode to assert that the requested
     * operation has an exact result, hence no rounding is necessary.
     * 计算结果是精确的，不需要舍入模式
     * 
     * static int ROUND_UP Rounding mode to round away from zero. 向远离0的方向舍入
     * 
     * 将d转换成字符串表达的形式
     * 
     * @param d
     *            double
     * @param mindeclen
     *            int最小小数部分位数
     * @param maxdeclen
     *            int最大小数部分位数
     * @param groupnum
     *            boolean是否带千分符
     * @deprecated 不建议使用，请用double2str(double,int,int,boolean)方法代替
     * @return String
     */
    static final public String double2str3(final double d, final int mindeclen,
            final int maxdeclen, final boolean groupnum){

        int MAXLEN = 4;
        if (!(Double.isInfinite(d) || Double.isNaN(d))) {
            MAXLEN = (int) (1.333 * (Math.log(Math.max(1, Math.abs(d)))
                    / Math.log(10) + maxdeclen + 3));
        }
        char[] buf = new char[MAXLEN];
        int len = StrYn.double2str(d, mindeclen, maxdeclen, groupnum, buf);
        return new String(buf, 0, len);
    }

    /**
     * @see #double2str(double)
     * @deprecated
     */
    static final public String double2str2(final double d, final int mindeclen,
            final int maxdeclen, final boolean groupnum){
        if (Double.isNaN(d)) return "";
        if (d == Double.NEGATIVE_INFINITY) return "-∞";
        if (d == Double.POSITIVE_INFINITY) return "∞";
        // 很多情况下d为0，此时优化一下，避免创建很多对象
        if (Math.abs(d - 0.0) < 0.0000000001 && mindeclen < 5)
            return DOUBLE2STR_ZEROS[mindeclen];

        if (maxdeclen == 0 && maxdeclen == 0 && !groupnum) {// 不需要小数也不需要分组，那么可以优化一下
            return String.valueOf((long) (d + (d > 0 ? 0.5d : -0.5d)));
        }

        /**
         * 20100127 yk
         * 以前用String.valueOf(d)转换为字符串，再去构造BigDecimal，很耗费内存，也找不到为何非要先转换为字符串了
         * 现改为直接构造BigDecimal，有问题再说
         */
        BigDecimal bd = new BigDecimal(d);
        double newd = bd.setScale(maxdeclen, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.CHINA);
        nf.setGroupingUsed(groupnum);
        nf.setMaximumFractionDigits(maxdeclen);
        nf.setMinimumFractionDigits(mindeclen);
        return nf.format(newd);

        // int MAXLEN = (int) (1.333 * (Math.log(Math.max(1, Math.abs(d))) /
        // Math.log(10) + maxdeclen + 3));
        // char[] buf = new char[MAXLEN];
        // int len = StrFunc.double2str(d, mindeclen, maxdeclen, groupnum, buf);
        // return new String(buf, 0, len);
    }

    /**
     * 默认的转换函数，精度是10位小数，不分组。 比如1.2返回"1.2"，0.123123123123返回"0.1231231231"
     * 
     * 把数值转为字符串时，jdk默认是用科学计数法，这个展示在客户端不好。
     * 
     * @param d
     *            将要转换成String类型的double
     * @return 数值对应的String
     */
    static final public String double2str(final double d){
        return double2str(d, 0, 10, false);
    }

    /**
     * 将浮点数转换为字符数组存储与用于提供的字符数组中，并返回转换后的字符数组的长度。该方法能够处理的 浮点数的范围为[-
     * {@link Long#MAX_VALUE}, {@link Long#MAX_VALUE}]，超过该范围的数被截尾成long型
     * 然后再进行转换，结果可能不正确。
     * 
     * @param d
     *            需要进行转换的浮点数。
     * @param mindeclen
     *            转换时最少的小数位个数；该参数非负。
     * @param maxdeclen
     *            转换时最多的小数位个数；该参数非负并且不能比最少的小数位个数少。
     * @param groupnum
     *            表示在转换时是否插入千分符；true表示插入千分符，false表示不插入千分符。
     * @param buf
     *            提供的用于保存转换后字符的字符数组；该字符数组中的内容会被覆盖掉，同时为了保证
     *            转换的顺利进行，提供的字符数组长度必需要可以容纳转换后的所有字符。
     * @return 转换后的字符数组的长度。当传入的浮点数为NaN时，返回0；当传入的浮点数为正无穷时，返回2并且
     *         数组中前两个字符的内容为'+'和'∞'；当传入的浮点数为负无穷时，返回2并且 数组中前两个字符的内容为'-'和'∞'。
     * @deprecated 不建议使用 ，请用double2str(double,int,int,boolean)方法代替
     */
    public final static int double2str(final double d, int mindeclen,
            int maxdeclen, final boolean groupnum, char[] buf){

        if (Double.isNaN(d)) {
            return 0;
        }

        if (Double.isInfinite(d)) {
            if (d > 0) {
                // buf[0] = '+';
                buf[0] = '∞';
                return 1;
            } else {
                buf[0] = '-';
                buf[1] = '∞';
                return 2;
            }
        }

        /*
         * 对于超过10^15的浮点数, 转换为字符串时采用科学计数法.
         */
        if (Math.abs(d) > Math.pow(10, 15)) {
            String str = String.valueOf(d);
            str.getChars(0, str.length(), buf, 0);
            return str.length();
        }

        int maxl = 15 - (int) MathUtil.log10(Math.abs(d));

        if (maxdeclen > maxl) {
            maxdeclen = Math.max(0, maxl);
        }

        if (mindeclen > maxdeclen) {
            mindeclen = maxdeclen;
        }

        if (mindeclen < 0 || maxdeclen < 0 || maxdeclen < mindeclen) {
            throw new IllegalArgumentException("");
        }

        boolean positive = (d >= 0);
        double value = Math.abs(d);
        double maxBase = Math.pow(10.0d, maxdeclen);
        long intpart;
        long frapart;

        if (maxdeclen == 0) {
            intpart = Math.round(value);
            frapart = 0;
        } else {
            intpart = (long) ((long) (value * maxBase + 0.5) / maxBase);
            double frac = value - intpart;
            frapart = (long) (maxBase * frac);
            if (frac >= (frapart + 0.5) / maxBase) {
                frapart++;
            }
        }

        int MAX_LEN = buf.length;
        int pos = MAX_LEN - 1;

        int fraclen = maxdeclen;
        while (fraclen > mindeclen && frapart % 10 == 0) {
            frapart /= 10;
            fraclen--;
        }

        while (fraclen > 0 || frapart > 0) {
            int q = (int) (frapart % 10);
            buf[pos--] = (char) (q + '0');
            frapart /= 10;
            fraclen--;
        }

        if (pos < MAX_LEN - 1) {
            buf[pos--] = '.';
        }

        if (intpart == 0) {
            buf[pos--] = '0';
        } else {
            if (groupnum) {
                int count = 1;
                while (intpart > 0) {
                    int q = (int) (intpart % 10);
                    buf[pos--] = (char) (q + '0');
                    intpart /= 10;
                    if (count % 3 == 0 && intpart > 0) {
                        buf[pos--] = ',';
                    }
                    count++;
                }
            } else {
                while (intpart > 0) {
                    int q = (int) (intpart % 10);
                    buf[pos--] = (char) (q + '0');
                    intpart /= 10;
                }
            }
        }

        /**
         * BI-5082 如果四舍五入后的值是0,则不需要在加负号；
         */
        if (!positive && MathUtil.round(d, maxdeclen) != 0) {
            buf[pos--] = '-';
        }

        int len = MAX_LEN - pos - 1;
        for (int i = 0; i < len; i++) {
            buf[i] = buf[i + pos + 1];
        }
        return len;
    }

    /**
     * 下面的内容是以前写的注释，可能之前写过加密算法后来又删掉了，所以有decryptOldPassword @ 应该是以前的加密算法密文前后写的标注。
     * 
     * @param text
     * @param maxLen
     *            ,密码的最大长度
     * @return
     * @throws java.lang.Exception
     */
    private static final char ENCRYPTTAG = '@';

    /**
     * encryptPassword加密密码后对密文串前做的标记，方便判断字符串是否被加密过
     */
    private static final char ENCRYPTTAG_MD5_LEFT = '{';

    /**
     * encryptPassword加密密码后对密文串后做的标记，方便判断字符串是否被加密过
     */
    private static final char ENCRYPTTAG_MD5_RIGHT = '}';

    /**
     * 加密密码,加密后密码一个字符{开始,以}符号结束,加密算法为标准的md5算法，加密后密码的长度是固定的 如果密码已经被加密过，那么不会再次加密它，
     * 注意md5加密是不可逆的，即不能由密文解密为密码明文
     * 
     * @param text
     *            要加密的内容
     * @return 密文
     */
    public final static String encryptPassword(String text){
        if (isOldEncryptPassword(text)) {
            text = decryptOldPassword(text);
        }
        if (isMd5EncryptPassword(text)) {
            return text;
        }
        return ENCRYPTTAG_MD5_LEFT + MD5(text) + ENCRYPTTAG_MD5_RIGHT;
    }

    /**
     * 解密OldPassWord
     * 
     * @param text
     *            密文
     * @return 明文
     */
    final private static String decryptOldPassword(String text){
        if (text == null) {
            return "";
        }

        if (!isOldEncryptPassword(text)) {
            return text;
        }

        text = text.substring(1, text.length() - 1);
        byte[] tt = hexStringToBytes(text);
        byte[] ss = new byte[tt.length];
        for (int i = 0; i < tt.length; i++) {
            ss[i] = (byte) (tt[i] + key[i % key.length]);
        }
        return new String(ss).trim();
    }

    /**
     * 比较2个字符串的加密后的串是否一致（忽略大小写）
     * 
     * @param p1
     *            要比较的字符串
     * @param p2
     *            要比较的字符串
     * @return 如果2字符串加密后的串一致返回true，否则false
     */
    static final public boolean comparePassword(String p1, String p2){
        p1 = encryptPassword(p1);
        p2 = encryptPassword(p2);
        // System.out.println(p1+"   "+p2);
        return p1.compareToIgnoreCase(p2) == 0;
    }

    /**
     * 
     * @param p
     *            要判断的字串
     * @return 如果字串为null，长度小于2，或者前后不是由@括起来，返回false，否则返回true
     */
    static final private boolean isOldEncryptPassword(String p){
        if ((p != null) && (p.length() >= 2) && (p.charAt(0) == ENCRYPTTAG)
                && (p.charAt(p.length() - 1) == ENCRYPTTAG)) {
            return true;
        }
        return false;
    }

    /**
     * 是否是md5加密过的密码,左边是{右边是}
     * 
     * @param p
     *            要判断的字串
     * @return 若是md5加密过的密码,左边是{右边是},长度大于等于26则返回true，否则false 个人认为 这个地方p.length()
     *         >= 26改成p.length() == 32 会更好
     */
    static final private boolean isMd5EncryptPassword(String p){
        if ((p != null) && (p.length() >= 26)
                && (p.charAt(0) == ENCRYPTTAG_MD5_LEFT)
                && (p.charAt(p.length() - 1) == ENCRYPTTAG_MD5_RIGHT)) {
            return true;
        }
        return false;
    }

    /**
     * 16进制相关的字符，字母为小写 HEXCHAR 字母为大写
     */
    private static final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final String algorithm_md5 = "MD5";

    /**
     * byte0 >>> 4 & 0xf 为取字节的高4位,byte0 & 0xf 为取字节的低4位
     * 因为hexDigits[]只有16个字符,所以每4位就可以对应一个字符(2的4次方),一个字节可以获得两个字符
     * 所以一个对字符串进行加密后获得的byte[],转换为字符串后可以得到两倍length的字符串
     */
    private final static String _md5str(byte[] md){
        int j = md.length;
        char str[] = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; i++) {
            byte byte0 = md[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str);
    }

    /**
     * 取一个字符串s的md5串,s如果为null则和""一样
     * 
     * @param s
     *            要加密的字串
     * @return md5串
     */
    public final static String MD5(final String s){
        try {
            byte[] strTemp = (s != null) ? s.getBytes() : new byte[0];
            MessageDigest mdTemp = MessageDigest.getInstance(algorithm_md5);
            mdTemp.update(strTemp);
            return _md5str(mdTemp.digest());
        } catch (Exception e) {
            e.printStackTrace();// TODO 不加调试开关的打印请做处理
            return null;
        }
    }

    /**
     * 取一个字符串s的md5串,
     * 
     * @param ss
     *            要加密的字符串数组
     * @return md5串
     */
    public final static String MD5(final String[] ss){
        try {
            MessageDigest mdTemp = MessageDigest.getInstance(algorithm_md5);
            for (int i = 0; i < ss.length; i++) {
                mdTemp.update((StrYn.null2blank(ss[i]) + "\t").getBytes());
            }
            return _md5str(mdTemp.digest());
        } catch (Exception e) {
            e.printStackTrace();// TODO 不加调试开关的打印请做处理
            return null;
        }
    }

    static private final int BUF_SIZE = 1024 * 8;

    /**
     * 从流中的类容生成md5串
     * 
     * @param in
     *            要加密的流
     * @return md5串
     */
    public final static String MD5(final InputStream in){
        try {
            if (in == null) return MD5((String) null);
            MessageDigest mdTemp = MessageDigest.getInstance(algorithm_md5);
            byte[] buf = new byte[BUF_SIZE];
            int sz = BUF_SIZE;
            while (sz > 0) {
                sz = in.read(buf, 0, sz);
                if (sz > 0) mdTemp.update(buf, 0, sz);
            }
            return _md5str(mdTemp.digest());
        } catch (Exception e) {
            e.printStackTrace();// TODO 不加调试开关的打印请做处理
        }
        return null;
    }

    /**
     * 使用选定加密算法,对字符串s加密,encryptType可以为“md5”或者“sha1”
     * 
     * @param s
     *            要加密的字串
     * @param encryptType
     *            选定的加密算法 忽略大小写
     * @return 加密后的密文，如果加密算法传参错误会返回null。传参null时返回值与""处理相同
     */
    public final static String stringHash(final String s,
            final String encryptType){
        try {
            byte[] strTemp = (s != null) ? s.getBytes() : new byte[0];
            MessageDigest alga;
            if (encryptType.equalsIgnoreCase("MD5")) {
                alga = MessageDigest.getInstance("MD5");
            } else if (encryptType.equalsIgnoreCase("SHA1"))
                alga = MessageDigest.getInstance("SHA-1");
            else
                alga = MessageDigest.getInstance(encryptType);
            alga.update(strTemp);
            return _md5str(alga.digest());
        } catch (Exception ex) {
            ex.printStackTrace();// TODO 不加调试开关的打印请做处理
            return null;
        }
    }

    /**
     * 将s设置为l长,如果s原来比l长,则从s的左边截去多余的部分,如果s为null或者没有l长则从左边补字符c 并将结果返回
     * 
     * @param s
     *            要设置的字符串
     * @param l
     *            设置的长度
     * @param c
     *            补足字符
     * @return 设置后的字符串
     */
    static final public String setStrLengthL(final String s, final int l,
            final char c){
        if (l <= 0) {
            return null;
        }
        int rl = 0;
        StringBuffer sb;
        if (s != null) {
            byte[] bb = s.getBytes();
            rl = bb.length;
            if (rl > l) {
                return new String(bb, rl - l, l);
            }
            sb = new StringBuffer(s);
        } else {
            sb = new StringBuffer(l);
        }

        for (int i = rl; i < l; i++) {
            sb.insert(0, c);
        }
        return sb.toString();
    }

    /**
     * 将s设置为l长,如果s原来比l长,则从s的右边截去多余的部分,如果s为null或者没有l长则从右边补字符c 并将结果返回
     * 
     * @param s
     *            要设置的字符串
     * @param l
     *            设置的长度
     * @param c
     *            补足字符
     * @return 设置后的字符串
     */
    static final public String setStrLength(final String s, final int l,
            final char c){
        if (l <= 0) {
            return null;
        }
        int rl = 0;
        StringBuffer sb;
        if (s != null) {
            byte[] bb = s.getBytes();
            rl = bb.length;
            if (rl > l) {
                return new String(bb, 0, l);
            }
            sb = new StringBuffer(s);
        } else {
            sb = new StringBuffer(l);
        }

        for (int i = rl; i < l; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 按字节来切割字符串 将一个String 按指定字符集来截取，得出的结果通过str.getBytes（charsetName） <=
     * bytesize. 该方法不会破坏str以前的编码方式。
     * 
     * @param str
     *            需要截取的String
     * @param charsetName
     *            编码方式，为空的话设置为本地编码方式
     * @param bytesize
     *            设置按字节截取的长度
     * @return 切割后的字符串
     * @throws Exception
     *             如果传参是不存在的编码方式，会抛异常
     */
    public static final String chopString(final String str,
            final String charsetName, final int bytesize) throws Exception{
        if (bytesize < 1) return null;
        if (str == null || str.length() == 0) {
            return str;
        }

        boolean noncharset = charsetName == null || charsetName.length() == 0;
        byte[] encodebytes = noncharset ? str.getBytes() : str
                .getBytes(charsetName);
        if (encodebytes.length > bytesize) {
            char[] chars = str.toCharArray();
            int c = -1;
            int len = 0;
            int b = 0;
            while (c++ < chars.length) {
                len = noncharset ? String.valueOf(chars[c]).getBytes().length
                        : String.valueOf(chars[c]).getBytes(charsetName).length;
                if ((b += len) > bytesize) {
                    c--;
                    break;
                }
            }
            /*
             * 跑测试发现此异常，第二个参数为count,但变量c代表的是数组下标，正常结果应该是c+1 huling 2012.12.26
             */
            return new String(chars, 0, c + 1);
        }
        return str;
    }

    /**
     * @param ch
     *            字符
     * @return ch是数字，返回数字，ch是字母，返回字母序列加10
     * @throws Exception
     *             传入参数不为字母或数字会抛异常
     */
    private static final int getIdcCharVal(final char ch) throws Exception{
        if ((ch >= '0') && (ch <= '9')) {
            return (ch - '0');
        } else if ((ch >= 'a') && (ch <= 'z')) {
            return (ch - 'a') + 10;
        } else if ((ch >= 'A') && (ch <= 'Z')) {
            return (ch - 'A') + 10;
        } else {
            // throw new Exception("非法的IDC字符" + ch + "！");
            throw new Exception(I18N.getString("com.esen.util.StrFunc.1",
                    "非法的IDC字符{0}！", new String[] {String.valueOf(ch)}));

        }
    }

    /**
     * 产生一个重复n次某字符的串
     * 
     * @param cc
     *            组成字符串的字符
     * @param count
     *            重复多少次
     * @return 重复 count 次 cc 的串
     */
    public static final String strOfchar(final char cc, final int count){
        if (count <= 0) {
            return "";
        }
        char[] aa = new char[count];
        Arrays.fill(aa, cc);
        return new String(aa);
    }

    /**
     * 产生一个代码的IDC校验位字符
     * 
     * @param code
     *            传入的idc码
     * @return 校验位字符 // TODO 运算这一部分可以简化，可以参考isPdy(String),
     *         感觉idc和pdy唯一的区别在于pdy不支持小写字母，而idc可以
     */
    public static final String IDC(final String code) throws Exception{
        if ((code == null) || (code.length() != (8))) {
            // throw new Exception("代码串" + code + "不是一个8位的串！无法产生IDC码！");
            throw new Exception(I18N.getString("com.esen.util.StrFunc.2",
                    "代码串{0}不是一个8位的串！无法产生IDC码！", new String[] {code}));
        }
        int v = 11 - (getIdcCharVal(code.charAt(0)) * 3
                + getIdcCharVal(code.charAt(1)) * 7
                + getIdcCharVal(code.charAt(2)) * 9
                + getIdcCharVal(code.charAt(3)) * 10
                + getIdcCharVal(code.charAt(4)) * 5
                + getIdcCharVal(code.charAt(5)) * 8
                + getIdcCharVal(code.charAt(6)) * 4 + getIdcCharVal(code
                .charAt(7)) * 2) % 11;
        if (v == 10) {
            return "X";
        } else if (v == 11) {
            return "0";
        } else {
            return String.valueOf(v);
        }
    }

    /**
     * 判断一个字符串是否是一段rtf
     * 
     * @param s
     *            需要判断的字符串
     * @return 如果是rtf格式，返回TRUE，反之flase
     */
    public static final boolean isrtf(final String s){
        return s != null && s.length() > 10 && s.charAt(0) == '{'
                && s.startsWith("{\\rtf1");// rtf字串以{\\trf1开始而不是{\trf1
    }

    /**
     * IDC码的长度
     */
    public static final int IDCLEN = 9;

    /**
     * 判断一个串是否是idc码
     * 
     * @param code
     *            参数
     * @return 是idc码返回true，反之flase
     */
    public static final boolean isIDC(final String code) throws Exception{
        if ((code == null) || (code.length() != IDCLEN)) {
            return false;
        }
        return code.substring(IDCLEN - 1, IDCLEN).equals(
                IDC(code.substring(0, IDCLEN - 1)));
    }

    /**
     * 判断一个串是否在一个串数组中
     * 
     * @param array
     *            串数组
     * @param element
     *            要查找的字符串
     * @return 找到了为true，没找到为false
     */
    public static final boolean contains(final String[] array,
            final String element){
        if (array == null) {
            return false;
        }
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 与16进制相关的字符，字母为大写
     */
    public static final char[] HEXCHAR = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 返回16进制的值，如果传参不是16进制字符，返回0
     * 
     * @param c
     *            16进制
     * @return 10进制
     */
    public static final int hexValue(char c){
        if (c >= '0' && c <= '9') {
            return c - 0x30;
        }
        c = Character.toUpperCase(c);
        if (c >= 'A' && c <= 'F') {
            return c - 0x41 + 10;
        }
        return 0;
    }

    /**
     * 截取StringBuffer串从开头至subStr的位置，并返回String类型 考虑到效率问题，空传参没有进行处理，用的时候注意参数的控制
     * 
     * @param s
     *            要截取的串
     * @param subStr
     *            分割串
     * @return 如果subStr不存在于s，不截取，若存在，截取至subStr出现的位置之前
     */
    public static final String strCut(final StringBuffer s, final String subStr){
        String str = s.toString();
        int e = str.indexOf(subStr);
        if (e != -1) {
            s.delete(0, e + subStr.length());
            return str.substring(0, e);
        } else {
            s.delete(0, str.length());
            return str;
        }
    }

    /**
     * 判断是否需要进行转义，字符含空格以下字符及\\需要转义
     * 
     * @param s
     *            进行判断的字符串
     * @return 无需转义返回true
     */
    public static boolean isNotNeedStr2Text(final String s){
        if (s == null || s.length() == 0) {
            return true;
        }

        /**
         * 多数情况下，一个字符串是无需转义的，此处做一个判断，如果s中没有需要转义的字符，那么直接返回它
         */
        int slen = s.length();
        int fromi = 0;
        for (; fromi < slen; fromi++) {
            char c = s.charAt(fromi);
            if (c <= ' ' || c == '\\') {
                return false;
            }
        }

        return true;
    }

    /**
     * 将指定的字符串中的不可见字符转换为可见字符的表示 与text2Str是互逆的 //TODO 原注释写的是互逆的，但实际上有偏差，目前没有完全互逆
     * #9 -> \t #13-> \r #10-> \n '\'-> \\ #1..#8,#11,#12,#14..#32 -> \ uAA
     * “AA”为大写的十六进制 其他字符不转换
     * 
     * @param s
     *            要转换的字符串
     * @return String 可见字符
     */
    public static final String str2Text(final String s){
        if (s == null || s.length() == 0) {
            return s;
        }

        /**
         * 多数情况下，一个字符串是无需转义的，此处做一个判断，如果s中没有需要转义的字符，那么直接返回它
         */
        int slen = s.length();
        boolean dontneed2text = true;
        int fromi = 0;
        for (; fromi < slen; fromi++) {
            char c = s.charAt(fromi);
            if (c <= ' ' || c == '\\') {
                dontneed2text = false;
                break;
            }
        }
        if (dontneed2text) {// 如果s中没有一个需要转义的字符，那么可以直接返回它
            return s;
        }

        int i, k, l;
        k = fromi;
        l = slen * 4 / 3 + 8;// 原来是+4，现改为+8保证大多数情况下不必二次分配内存
        char[] result = new char[l];
        s.getChars(0, fromi, result, 0);// result.length肯定大于s的长度
        for (i = fromi; i < slen; i++) {
            if (l < k + 4) {
                l = (l + 1) * 2;// 原来的内存扩张速度是：l += 50;
                                // 在EIBC项目中，i有个地方是长度为2mb的斜杠，导致此函数很慢，故改变扩张速度和StringBuffer一致
                char[] old = result;
                result = new char[l];
                System.arraycopy(old, 0, result, 0, old.length);
            }
            char c = s.charAt(i);
            switch (c) {
                case '\t':
                    result[k++] = '\\';
                    result[k++] = 't';
                    break;
                case '\r':
                    result[k++] = '\\';
                    result[k++] = 'r';
                    break;
                case '\n':
                    result[k++] = '\\';
                    result[k++] = 'n';
                    break;
                case '\\':
                    result[k++] = '\\';
                    result[k++] = '\\';
                    break;
                default: {
                    /**
                     * BI-2336 由于s中含有(char 0)字符，应该转义为
                     * \\u00，而这里没有处理，在下载主题表内容时，主题表的内容是根据Report对象构造成 内容中含有char
                     * 0字符，导致传入客户端的内容被截断，故出现了上述错误
                     */
                    if ((c >= 0 && c <= 8) || (c >= 14 && c <= 32) || (c == 11)
                            || (c == 12)) {
                        result[k++] = '\\';
                        result[k++] = 'u';
                        result[k++] = HEXCHAR[c / 16];
                        result[k++] = HEXCHAR[c % 16];
                    } else {
                        result[k++] = c;
                    }
                }
            }
        }
        return new String(result, 0, k);
    }

    /**
     * 将指定的字符串中的可见字符转换为不可见字符的表示 将字符串txt中的\r,\n,\t等明文的转义写法，转成对应的字符
     * 
     * @param txt
     *            要转义的字符串
     * @return 转义成对应字符，如\\t转换为\t，\\u20转换为空格 当斜线后面的转义字符是无效的转义时保留斜线，例如\a\b 这里只转义r
     *         n t u ,(a b f 等都作为无效转义了)，这个地方的处理与str2Text没有对应上
     */
    public static final String text2Str(final String txt){
        int i;
        if ((txt == null) || (txt.length() < 2)
                || ((i = txt.indexOf('\\')) == -1)) {
            return txt;
        }
        int txtlen = txt.length();
        StringBuffer result = new StringBuffer(txtlen);
        int i1 = 0;
        while (i != -1) {
            if (i == txtlen - 1) {
                break;
            }
            result.append(txt.substring(i1, i));
            char c = txt.charAt(++i);
            switch (c) {
                case '\\':
                    result.append('\\');
                    break;
                case 'r':
                    result.append('\r');
                    break;
                case 'n':
                    result.append('\n');
                    break;
                case 't':
                    result.append('\t');
                    break;
                case 'u':
                    if (txtlen > i + 2) {
                        int u = hexValue(txt.charAt(i + 1)) * 16
                                + hexValue(txt.charAt(i + 2));
                        result.append((char) u);
                        i += 2;
                    }
                    break;
                default:
                    /**
                     * 20101115 当斜线后面的转义字符是无效的转义时要保留斜线，例如\a\b
                     * 注：看以前的注释，好像text2str函数是有includeUnknown参数的，但是没有实现这个参数。
                     * 实际情况需要总是保留斜线。
                     */
                    result.append("\\");
                    result.append(c);
            }
            i1 = i + 1;
            i = txt.indexOf('\\', i1);
        }
        result.append(txt.substring(i1));
        return result.toString();
    }

    private static final char URL_ZHANGYICHAR = '$';

    /**
     * 将urlparam串转成对应的String串
     * 
     * @param txt
     *            urlparam串
     * @return string字串
     */
    public static final String urlparam2str(final String txt){
        int i;
        if ((txt == null) || ((i = txt.indexOf(URL_ZHANGYICHAR)) == -1)) {
            return txt;
        }
        StringBuffer result = new StringBuffer();
        int i1 = 0;
        while (i != -1) {
            if (i == txt.length() - 1) {
                break;
            }
            result.append(txt.substring(i1, i));
            char c = txt.charAt(++i);
            switch (c) {
                case URL_ZHANGYICHAR:
                    result.append(URL_ZHANGYICHAR);
                    break;
                case 'r':
                    result.append('\r');
                    break;
                case 'n':
                    result.append('\n');
                    break;
                case 't':
                    result.append('\t');
                    break;
                case 'a':
                    if (txt.length() > i + 2) {
                        int u = hexValue(txt.charAt(i + 1)) * 16
                                + hexValue(txt.charAt(i + 2));
                        result.append((char) u);
                        i += 2;
                    }
                    break;
                case 'u':
                    if (txt.length() > i + 2) {
                        int u1 = hexValue(txt.charAt(i + 1)) * 16
                                + hexValue(txt.charAt(i + 2));
                        int u2 = hexValue(txt.charAt(i + 3)) * 16
                                + hexValue(txt.charAt(i + 4));
                        int u = (u1 << 8) | u2;
                        result.append((char) u);
                        i += 4;
                    }
                    break;
                default:
                    result.append(c);
            }
            i1 = i + 1;
            i = txt.indexOf(URL_ZHANGYICHAR, i1);
        }
        result.append(txt.substring(i1));
        return result.toString();
    }

    /**
     * 在使用String.replaceAll函数时，如果replacement中含有 \ $ 符号时，会自动进行转义
     * 故需要使用quoteReplacement函数进行处理一下，在jdk1.5 Matcher中含有该函数，为兼容1.4故提取在 这里 Returns
     * a literal replacement <code>String</code> for the specified
     * <code>String</code>.
     *
     * This method produces a <code>String</code> that will work use as a
     * literal replacement <code>s</code> in the <code>appendReplacement</code>
     * method of the {@link Matcher} class. The <code>String</code> produced
     * will match the sequence of characters in <code>s</code> treated as a
     * literal sequence. Slashes ('\') and dollar signs ('$') will be given no
     * special meaning.
     *
     * @param s
     *            The string to be literalized
     * @return A literal string replacement
     * @since 1.5
     */
    public static String quoteReplacement(final String s){
        if ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1)) return s;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                sb.append('\\');
                sb.append('\\');
            } else if (c == '$') {
                sb.append('\\');
                sb.append('$');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * String字串转成对应的urlparam串
     * 
     * @param s
     *            要转换的String串
     * @return urlparam串
     */
    public static final String str2urlparam(final String s){
        if (s == null || s.length() == 0) {
            return s;
        }
        int i, k, l;
        k = 0;
        l = s.length() + 7;
        char[] result = new char[l];
        for (i = 0; i < s.length(); i++) {
            if (l < k + 7) {
                l += 50;
                char[] old = result;
                result = new char[l];
                System.arraycopy(old, 0, result, 0, old.length);
            }
            char c = s.charAt(i);
            switch (c) {
                case '\t':
                    result[k++] = URL_ZHANGYICHAR;
                    result[k++] = 't';
                    break;
                case '\r':
                    result[k++] = URL_ZHANGYICHAR;
                    result[k++] = 'r';
                    break;
                case '\n':
                    result[k++] = URL_ZHANGYICHAR;
                    result[k++] = 'n';
                    break;
                case URL_ZHANGYICHAR:
                    result[k++] = URL_ZHANGYICHAR;
                    result[k++] = URL_ZHANGYICHAR;
                    break;
                default: {
                    if (isABC_xyz(c) || isDigit(c)) {
                        result[k++] = c;
                    } else {
                        if (c > 0xff) {
                            result[k++] = URL_ZHANGYICHAR;
                            result[k++] = 'u';
                            result[k++] = HEXCHAR[(c >> 8) / 16];
                            result[k++] = HEXCHAR[(c >> 8) % 16];
                            result[k++] = HEXCHAR[(c & 0xFF) / 16];
                            result[k++] = HEXCHAR[(c & 0xFF) % 16];
                        } else {
                            result[k++] = URL_ZHANGYICHAR;
                            result[k++] = 'a';
                            result[k++] = HEXCHAR[c / 16];
                            result[k++] = HEXCHAR[c % 16];
                        }
                    }
                }
            }
        }
        return new String(result, 0, k);
    }

    /**
     * 将指定的字符串用quote括起来，如果字符串内部有quote则用两个quote表示
     *
     * @param s
     *            要括的字串
     * @param quote
     *            用什么字符括字串
     * @return String 处理后的字串
     */
    public static final String quotedStr(final String s, char quote){
        /* 使用StringBuffer大约比使用String快两倍 */
        if (s == null) {
            return null;
        }
        int i = s.indexOf(quote);
        if (i == -1) {
            return quote + s + quote;
        }
        StringBuffer result = new StringBuffer(s.length() + 12);
        result.append(quote);
        int i1 = 0;
        while (i != -1) {
            result.append(s.substring(i1, i) + quote + quote);
            i1 = i + 1;
            i = s.indexOf(quote, i1);
        }
        result.append(s.substring(i1) + quote);
        return result.toString();
    }

    /**
     * ireport 还用这个函数
     * 
     * @param s
     *            字符串
     * @param fromindex
     *            从第几个开始找
     * @param quote
     *            一般是'"'
     * @param separator
     *            分隔符
     * @param newindex
     *            个人认为这个参数无用
     * @return 
     *         如果字符串第fromindex位置是quote,截取字符串至末，如果不是，分割字符串至separator，没有separator截取至末
     */
    public static final String getValue(final String s, int fromindex,
            char quote, char separator, int[] newindex){
        if (s.length() <= fromindex) {
            if (newindex != null) {
                newindex[0] = -1;
            }
            return "";
        }
        if (s.charAt(fromindex) == quote) {
            String v = extractQuotedStr(s, fromindex, quote, newindex);
            if (newindex != null) {
                if (newindex[0] != -1) {
                    newindex[0] = newindex[0] + 1;
                }
            }
            return v;
        }
        int i = s.indexOf(separator, fromindex);
        if (i == -1) {
            if (newindex != null) {
                newindex[0] = -1;
            }
            return s.substring(fromindex);
        } else {
            if (newindex != null) {
                newindex[0] = i + 1;
            }
            return s.substring(fromindex, i);
        }
    }

    /**
     * 从标记字符串长度-1开始截取第二个参数至倒数第2个位置，截取后的字串前后空格会被抛弃。
     * 考虑效率问题，没有对null，""，及第2个参数长度小于第一个参数长度的传参进行处理 使用时注意控制正确传参，否则会抛相应的异常
     * 
     * @param tag
     *            标记字符串
     * @param ln
     *            需要截取的字符串
     * @return 截取后的字串
     */
    static final public String getTagLineAttrStr(final String tag,
            final String ln){
        return ln.substring(tag.length() - 1, ln.length() - 1).trim();
    }

    /**
     * 对引号进行解码
     * 
     * @param s
     *            需要解码的字符串
     * @param quote
     *            一般为'"'
     * @return String 返回解码的字符串
     */
    public static final String extractQuotedStr(final String s, char quote){
        return extractQuotedStr(s, 0, quote, null);
        // TODO 这个函数根本就用不了，(VarInt)null，所以调用的时候会抛空指针异常
    }

    /**
     * 取得buf内的从index[0]开始到sep字符的值,并改变index[0]的值,为新的位置.
     * 同时，返回从index[0]位置至sep的byte数组转成的字串
     * 
     * @param buf
     *            要转成String的byte数组
     * @param sep
     *            搜索的字符
     * @param index
     *            开始搜索的位置 TODO 既然这个地方只使用index[0] ，应该把int[] 改为int
     * @return 从找到的字符位置开始至末byte数组 转成的字符串
     */
    public static final String strpcut(final byte[] buf, final char sep,
            final int[] index){
        int i = index[0];
        for (; i < buf.length; i++) {
            if (buf[i] == sep) {
                break;
            }
        }
        String s = new String(buf, index[0], i - index[0]);
        index[0] = i + 1;
        return s;
    }

    /**
     * s1和s2都不为null且内容一致或者都为null，返回true 其他情况返回false
     * 
     * @param s1
     *            String 要比较的字串
     * @param s2
     *            String 要比较的字串
     * @return boolean 内容一致返回true
     */
    static public final boolean strEquals(final String s1, final String s2){
        return (s1 != null & s2 != null && s1.equals(s2))
                || (s1 == null && s2 == null);
    }

    /**
     * 比较CharSequence的实现者(String，StringBuffer，CharBuffer，StringBuilder)的内容是否一致
     * 此函数主要是为了提高效率，避免判断过程中还要创建新的对象
     * 
     * @param s1
     *            要判断的CharSequence
     * @param s2
     *            要判断的CharSequence
     * @return 如果内容一致，返回true，反之false
     */
    public static boolean strCompare(final CharSequence s1,
            final CharSequence s2){
        if (s1 == s2) return true;
        int n = s1.length();
        if (n == s2.length()) {
            int i = 0;
            int j = 0;
            while (n-- != 0) {
                if (s1.charAt(i++) != s2.charAt(j++)) return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 比较2个字符串内容是否一致
     * 
     * @param s1
     *            要比较的字符串
     * @param s2
     *            要比较的字符串
     * @return s1<s2返回小于0的数,相等=0,大于0的数,兼容null,""相当于null, null相当于"".
     */
    public static int strCompare(final String s1, final String s2){
        if (s1 == null || s1.length() == 0)
            return (s2 == null || s2.length() == 0 ? 0 : -1);
        if (s2 == null || s2.length() == 0)
            return (s1 == null || s1.length() == 0 ? 0 : 1);
        return s1.compareTo(s2);
    }

    /**
     * 极端情况下可能频繁的根据一个字符构造字符串，此处cache一下解释内存
     * 
     * @param i
     *            要构造成字符串的字符
     * @return 字符串
     */
    public static final String strofascii(final char i){
        return STRINGS_CACHE_CHARS[i];
    }

    /**
     * 为避免重复的创建Integer类 所创建的整形数组，
     */
    static final Integer INTCACHE[] = new Integer[-(-128) + 127 + 1];
    static {
        for (int i = 0; i < INTCACHE.length; i++)
            INTCACHE[i] = new Integer(i - 128);
    }
    private final static String[] STRINGS_CACHE_CHARS = new String[127];
    static {
        char[] chars = new char[1];
        for (int i = 0; i < 127; i++) {
            chars[0] = (char) i;
            STRINGS_CACHE_CHARS[i] = new String(chars);
        }
    }

    /**
     * 1,10,及10的倍数,(至10的10倍)
     */
    final static public int[] TEN_POWERS = {1, 10, 100, 1000, 10000, 100000,
            1000000, 10000000, 100000000, 1000000000};

    /**
     * add by jzp 2012-08-07 此方法在别的地方也可能调用所以修改为公有的，并增加长度到支持8为小数，有些时候银行汇率等会达到8位小数
     */
    public static final String[] DOUBLE2STR_ZEROS = {"0", "0.0", "0.00",
            "0.000", "0.0000", "0.00000", "0.000000", "0.0000000", "0.00000000"};

    /**
     * 将字符串转换为int 如果数字字符串长度大于10，返回默认值 与重载方法str2int(final String s, int fromi,
     * int toi,final int def)不同的是 如果字符串为数字字符串且含小数点，返回值截取整数部分（不进行四舍五入）
     * 
     * @param s
     *            要转换的字符串
     * @param def
     *            转换失败返回的默认值
     * @return int 返回字符串转换的int值
     */
    static public final int str2int(final String s, final int def){
        if (s == null || s.length() == 0) {
            return def;
        }

        if (s.indexOf('.') != -1) return (int) str2double(s, def);

        int len = s.length();
        boolean fu = s.charAt(0) == '-';
        int i = fu ? 1 : 0;
        int r = 0;
        int w;
        for (; i < len; i++) {
            w = s.charAt(i) - '0';
            if (w > 9 || w < 0) {
                return def;
            }
            int p = len - i - 1;
            if (p >= 10 || p < 0) return def;// 当s是：12M_LOW_BAL时，会出现异常，见：BI-3075
            r += TEN_POWERS[p] * w;
        }
        return fu ? -r : r;
    }

    /**
     * 将字符串fromi至toi的内容转换为int 如果数字字符串长度大于10，返回默认值 与重载方法str2int(final String s,
     * final int def)不同的是 如果截取后的字符串为数字字符串且含小数点，返回默认值
     * 
     * @param s
     *            要转换的字符串
     * @param fromi
     *            从字符串地fromi位置开始转
     * @param toi
     *            到字符串toi位置结束
     * @param def
     *            转换失败返回的默认值
     * @return int 返回字符串转换的int值
     */
    static public final int str2int(final String s, int fromi, int toi,
            final int def){
        if (toi - fromi <= 0) {
            return def;
        }
        boolean fu = s.charAt(fromi) == '-';
        int i = fromi + (fu ? 1 : 0);
        int r = 0;
        int w;
        for (; i < toi; i++) {
            w = s.charAt(i) - '0';
            if (w > 9 || w < 0) {
                return def;
            }
            int p = toi - i - 1;
            if (p >= 10 || p < 0) return def;// 当s是：12M_LOW_BAL时，会出现异常，见：BI-3075
            r += TEN_POWERS[p] * w;
        }
        return fu ? -r : r;
    }

    /**
     * 将String类型转成long类型，如果转不成功，返回默认值
     * 
     * @param s
     *            要转的字串
     * @param def
     *            默认值
     * @return long类型数据
     */
    static public final long str2long(final String s, final long def){
        if (s == null || s.length() == 0) {
            return def;
        }
        try {
            return Long.parseLong(s);
        } catch (Exception ex) {
            return def;
        }
    }

    /**
     * 将String类型转成float类型，如果转不成功，返回默认值
     * 
     * @param s
     *            要转的字串
     * @param def
     *            默认值
     * @return long类型数据
     */
    static public final float str2float(final String s, final float def){
        if (s == null || s.length() == 0) {
            return def;
        }
        try {
            return Float.parseFloat(s);
        } catch (Exception ex) {
            return def;
        }
    }

    /**
     * 将传入的o转换为Int返回，如果转换不成功那么返回def
     * 
     * @param o
     *            可以是任意类型的Object，Double、Integer、Long、String等
     * @param def
     *            默认返回值
     * @return 返回int类型
     */
    static public final int parseInt(final Object o, final int def){
        if (o == null) {
            return def;
        }
        if (o instanceof Number) return ((Number) o).intValue();
        return str2int(o.toString(), def);
    }

    /**
     * 将传入的o转换为double返回，如果转换不成功那么返回def
     * o可以是任意类型的Object，Double、Integer、Long、String等
     */
    static public final double parseDouble(final Object o, final double def){
        if (o == null) {
            return def;
        }
        if (o instanceof Number) return ((Number) o).doubleValue();
        return str2double(o.toString(), def);
    }

    /**
     * String类型转成double类型
     * 
     * @param s
     *            要转的字串
     * @param def
     *            默认值
     * @return double类型数据
     */
    static public final double str2double(final String s, final double def){
        if (s == null || s.length() == 0) {
            return def;
        }
        try {
            return Double.parseDouble(s);
        } catch (Exception ex) {
            return def;
        }
    }

    /**
     * 判断对象是否可以成为double对象
     * 
     * @param o
     *            要判断的对象
     * @return 可以成为double返回true，反之false
     */
    static public final boolean isdouble(final Object o){
        if (o == null) {
            return false;
        }

        if (o instanceof Number) {
            return true;
        }

        String s = o.toString();
        if (s == null || s.length() == 0) return false;
        try {
            Double.parseDouble(s);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 判断对象是否可以成为Long对象
     * 
     * @param o
     *            要判断的对象
     * @return 可以成为Long返回true，反之false
     */
    static public final boolean isLong(final Object o){
        if (o == null) {
            return false;
        }

        if ((!(o instanceof Double))
                && (o instanceof Number)
                && (o instanceof Long || o instanceof Integer
                        || o instanceof Short || o instanceof Byte || o instanceof BigInteger)) {
            return true;
        }

        String s = o.toString();
        if (s == null || s.length() == 0) return false;
        try {
            Long.parseLong(s);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 将整数转为字符串，保证字符串的长度不小于len
     * 
     * @param l
     *            要转换的Long型数据
     * @param len
     *            字符串长度
     * @return 将整数转为字符串，保证字符串的长度不小于len，左边补0
     */
    static final public String long2str(final long l, final int len){
        String str = String.valueOf(l);
        if (str.length() < len) {
            return strOfchar('0', len - str.length()) + str;
        } else {
            return str;
        }
    }

    /**
     * 将一个对象转换为字符串的显示形式，如果是double的nan那么返回null，
     * 是日期对象(Calendar)则返回yyyy-mm-dd"格式的，如果是Number类型，精度为10 20081217
     * 把数值转为字符串时，jdk默认是用科学计数法。使用double2str。 20110902
     * 优化：最先判断String类型。最常见的是String类型，它要以最快速度返回。
     */
    public final static String object2str(final Object o){
        if (o instanceof String) return (String) o;
        if (o instanceof Number) {
            /**
             * BI-5339
             * 如果是BigDecimal或者BigInteger类型，那么就直接调用其对象的toString方法，而double2str
             * 方法是不支持转换20位以上数值转化成字符串的(会转换成科学计数法的情形)，
             */
            if (o instanceof BigDecimal || o instanceof BigInteger) {
                return o.toString();
            }
            double d = ((Number) o).doubleValue();
            return Double.isNaN(d) ? null : double2str(d);
        }
        if (o instanceof Calendar) {
            return date2str((Calendar) o, "yyyy-mm-dd");
        }
        return o != null ? o.toString() : null;
    }

    /**
     * 生成格式化字符串，比如"#,##0.00"
     * 
     * @param grouped
     *            是否进行分组
     * @param isFloat
     *            是否写成小数类型
     * @param declen
     *            小数位数 如果传参为负，自动转成0，
     * @return 规则对应的格式化字符串
     */
    static final public String getDataFormat(final boolean grouped,
            final boolean isFloat, int declen){
        DecimalFormat nf = new DecimalFormat();
        nf.setGroupingUsed(grouped);

        if (!isFloat) declen = 0;
        nf.setMaximumFractionDigits(declen);
        nf.setMinimumFractionDigits(declen);
        return nf.toPattern();
    }

    /**
     * 字符串转boolean类型，传参为true（不区分大小写），返回true，反之false
     * 
     * @param str
     *            要转的字符串
     * @return boolean类型
     */
    static public final boolean str2boolean(final String str){
        return Boolean.valueOf(str).booleanValue();
    }

    /**
     * hejzh：将字符串解析为boolean值。如果给定的字符串不是true/false、t/f、0/1、是/否其中之一，
     * 或为null，则返回给定的默认值
     *
     * @param str
     *            待解析的字符串
     * @param def
     *            给定默认值
     * @return 解析结果
     */
    static public final boolean parseBoolean(final String str, final boolean def){
        if (str == null || str.length() == 0) {
            return def;
        }
        if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("t")
                // || str.equalsIgnoreCase("1")||str.equals("是")) {
                || str.equalsIgnoreCase("1")
                || str.equals(I18N.getString("com.esen.util.StrFunc.4", "是"))) {

            return true;
        } else if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("f")
                // || str.equalsIgnoreCase("0")||str.equals("否")) {
                || str.equalsIgnoreCase("0")
                || str.equals(I18N.getString("com.esen.util.StrFunc.5", "否"))) {
            return false;
        } else {
            return def;
        }
    }

    /**
     * 将传入的o转换为boolean返回，如果转换不成功返回def
     * o可以是任意类型的Object，Double、Integer、Long、String、Boolean等
     */
    static public final boolean parseBoolean(final Object o, final boolean def){
        if (o == null) return def;
        if (o instanceof Boolean) return ((Boolean) o).booleanValue();
        if (o instanceof Number) return ((Number) o).doubleValue() > 0;
        return parseBoolean(o.toString(), def);
    }

    /**
     * str是要转换的字符串，fmt是描述str格式的一个串，如果fmt为空，则用parseCalendar返回
     * fmt的格式可以如下:YYYY-MM-DD fmt中不要含时分秒。
     * fmt的格式中可以含mmm，比如str2date("2006 Jun 4","yyyy mmm d")
     * 2006年1月12日返回Date(2006, 0, 12)
     * 
     * @param str
     *            要转换的字符串
     * @param fmt
     *            要转换的格式
     * @return 返回Calendar类
     */
    static public final Calendar str2date(String str, String fmt){
        if (str == null || fmt == null) {
            return parseCalendar(str, null);
        }

        /*
         * 作容错处理 使用1.4的方法；
         */
        fmt = fmt.replace('Y', 'y');
        fmt = fmt.replace('m', 'M');
        fmt = fmt.replace('D', 'd');

        // 处理mmm
        if (fmt.indexOf("MMM") != -1) {
            for (int i = 0; i < MONTH_EN.length; i++) {
                str = str.replaceAll(MONTH_EN[i], String.valueOf(i + 1));
            }
            fmt = fmt.replaceAll("MMM", "MM");
        }

        DateFormat df = new SimpleDateFormat(fmt);
        try {
            Date d = df.parse(str);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(d.getTime());
            return c;
        } catch (ParseException e) {
            ExceptionHandler.rethrowRuntimeException(e);
            return null;
        }
    }

    static final String hz2[] = {"一", "二", "三", "四", "五", "六", "七", "八", "九",
            "十", "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "二十一", "二十二", "二十三", "二十四", "二十五", "二十六", "二十七", "二十八", "二十九",
            "三十", "三十一"};

    /**
     * 汉字0~9
     */
    static final char hz[] = {'零', '一', '二', '三', '四', '五', '六', '七', '八', '九'};

    /**
     * 日期的月份英文简写
     */
    static final String[] MONTH_EN = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    /**
     * 将日期对象格式化为字符串,对象可传递Date,Timestamp,Calendar
     */
    static public final String date2str(Object dat, String fmt){
        if (dat == null) return null;
        Calendar cal = null;
        if (dat instanceof Calendar) {
            cal = (Calendar) dat;
        } else if (dat instanceof Date) {
            cal = Calendar.getInstance();
            cal.setTime((Date) dat);
        } else if (dat instanceof Timestamp) {
            cal = Calendar.getInstance();
            cal.setTimeInMillis(((Timestamp) dat).getTime());
        }
        return cal == null ? null : date2str(cal, fmt);
    }

    /**
     * fmt如果是"YYYY年m月" 则可能返回：“二零零二年1月” 1. y表示年，m表示月，d表示日 2.
     * y/m/d取值为阿拉伯数字（123），Y/M/D取值为汉字（一二三） 3.
     * 2006，对“yy年”返回“06年”；对“yyyy年”则返回“2006年” 4. 不处理时分秒 5. mmm表示用英文显示月份
     * 
     * 20091223 本函数和str2date是相对的函数，str2date当字符串不合法时会返回null，相应地本函数应该允许date为null，
     * 当date为null时返回null。 之前没有处理date为null的情况，导致空指针异常。 20100327 by yk
     * 支持季度：q、qq、Q、QQ，表示季度
     * 
     * @param dat
     *            要转换成字符串格式的Calendar
     * @param fmt
     *            转换的格式
     * @return 字符串形式的日期
     */
    static public final String date2str(final Calendar dat, String fmt){
        if (dat == null) return null;

        if (fmt == null || fmt.length() == 0) {
            fmt = "yyyy-mm-dd";
        }

        int y = dat.get(Calendar.YEAR);
        int m = dat.get(Calendar.MONTH);
        int q = dat.get(Calendar.MONTH) / 3 + 1;
        int d = dat.get(Calendar.DAY_OF_MONTH);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < fmt.length(); i++) {
            char ch = fmt.charAt(i);
            boolean upcase = false;
            boolean isM = false;
            boolean isD = false;
            int num;
            char ch2 = '*';
            switch (ch) {
                case 'y':
                    num = y;
                    ch2 = 'Y';
                    break;
                case 'Y':
                    num = y;
                    ch2 = 'y';
                    upcase = true;
                    break;
                case 'm':
                    num = m;
                    ch2 = 'M';
                    isM = true;
                    break;
                case 'M':
                    num = m;
                    ch2 = 'm';
                    upcase = true;
                    isM = true;
                    break;
                case 'd':
                    num = d;
                    ch2 = 'D';
                    isD = true;
                    break;
                case 'D':
                    num = d;
                    ch2 = 'd';
                    upcase = true;
                    isD = true;
                    break;
                case 'q':
                    num = q;
                    ch2 = 'Q';
                    isD = true;
                    break;
                case 'Q':
                    num = q;
                    ch2 = 'q';
                    upcase = true;
                    isD = true;
                    break;
                default:
                    num = -1;
                    sb.append(ch);
            }

            if (num >= 0) {
                int n = 1; // 数字显示的个数，比如2006当格式为"yy"时显示"06"
                while (i + 1 < fmt.length()
                        && (fmt.charAt(i + 1) == ch || fmt.charAt(i + 1) == ch2)) {
                    n++;
                    i++;
                }

                if (isM && n == 3) { // mmm -> Apr
                    sb.append(MONTH_EN[num]);
                    continue;
                }

                if (isM) {
                    num++;
                }

                String s = String.valueOf(num);
                if (s.length() > n) {
                    s = s.substring(s.length() - n);
                }
                if (upcase) {
                    if (isM || isD) {
                        /* 月、日 */
                        sb.append(hz2[num - 1]);
                    } else {
                        /* 年 */
                        for (int j = 0; j < s.length(); j++) {
                            sb.append(hz[s.charAt(j) - '0']);
                        }
                    }
                } else {
                    if (s.length() < n) {
                        s = strOfchar('0', n - s.length()) + s;
                    }

                    sb.append(s);
                }
            }
        }

        return sb.toString();
    }

    /**
     * 正则表达式要考虑到参数datestr中可能含有小时分钟。 没有考虑含小时分钟等情况，没有考虑错误日期情况如：20121390
     * 没有考虑字符串前方有空格，yyyymm00 yyyymm-- yyyy-mm yyyy---- 格式后方有空格情况 20060101 -->
     * yyyymmdd 2006-01-01 --> yyyy-mm-dd 200601-- --> yyyymm-- 200601 -->
     * yyyymm 2006-01 --> yyyy-mm 2006---- --> yyyy---- 2006 --> yyyy
     * 
     * @param datestr
     *            表示日期的数字字串
     * @return 支持的日期格式
     */
    static public String guessDateFormat(final String datestr){
        if (Pattern.matches("[0-9]{4}[0-9]{2}00", datestr)) {
            // 支持yyyymm00格式的月报
            return "yyyymm00";
        } else if (Pattern.matches("[0-9]{4}[0-9]{2}[0-9]{2}.*", datestr)) {
            return "yyyymmdd";
        } else if (Pattern.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}.*", datestr)) {
            return "yyyy-mm-dd";
        } else if (Pattern.matches("[0-9]{4}[0-9]{2}--", datestr)) {
            return "yyyymm--";
        } else if (Pattern.matches("[0-9]{4}[0-9]{2}\\s*", datestr)) {
            return "yyyymm";
        } else if (Pattern.matches("[0-9]{4}-[0-9]{2}", datestr)) {
            return "yyyy-mm";
        } else if (Pattern.matches("[0-9]{4}----", datestr)) {
            return "yyyy----";
        } else if (Pattern.matches("[0-9]{4}0000", datestr)) {
            // 支持yyyy0000格式的年报
            return "yyyy0000";
        } else if (Pattern.matches("[0-9]{4}\\s*", datestr)) {
            return "yyyy";
        }
        return null;
    }

    /**
     * 猜测年月的格式，不含日期 没有考虑字符串前方有空格情况
     * 
     * @param datestr
     *            表示日期的数字字串
     * @return 支持的年月日期格式
     */
    static public String guessMonthFormat(final String datestr){
        if (Pattern.matches("[0-9]{4}[0-9]{2}[0-9]{2}.*", datestr)) {
            return "yyyymm";
        } else if (Pattern.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}.*", datestr)) {
            return "yyyy-mm";
        } else if (Pattern.matches("[0-9]{4}[0-9]{2}--", datestr)) {
            return "yyyymm--";
        } else if (Pattern.matches("[0-9]{4}[0-9]{2}\\s*", datestr)) {
            // "200802  " 有可能后面有空格等不可见字符；
            return "yyyymm";
        } else if (Pattern.matches("[0-9]{4}-[0-9]{2}", datestr)) {
            return "yyyy-mm";
        } else if (Pattern.matches("[0-9]{4}----", datestr)) {
            return "yyyy----";
        } else if (Pattern.matches("[0-9]{4}\\s*", datestr)) {
            return "yyyy";
        }
        return null;
    }

    /**
     * 把datestr转为format的日期格式
     * 
     * @param datestr
     *            比如'2007-09-09'
     * @param format
     *            比如'yyyyMMdd'
     * @return format格式的字符串
     */
    static public String convertDateFormat(final String datestr,
            final String format){
        Calendar calendar = parseCalendar(datestr, null);
        if (calendar == null)
            calendar = str2date(datestr, isNull(datestr) ? null
                    : guessDateFormat(datestr));
        if (calendar == null) return datestr;
        return date2str(calendar, isNull(format) ? null : format.toLowerCase());
    }

    static final Pattern TIME_REGEX = Pattern
            .compile("(\\d\\d(:|：)){2}(\\d\\d)((\\s|\\.|:|：)(\\d)*)?");

    /**
     * parseCalendar方法要用到的正则表达式，主要用来判断字符串类型样式是否符合时间样式
     */
    static final Pattern[] DATE_REGEX = {
            Pattern.compile("[0-9]{2,4}(-|/|\\s)[0-9]{1,2}(-|/|\\s)[0-9]{1,2}"),
            Pattern.compile("[0-9]{8}"),
            Pattern.compile("[0-9]{2,4}(-|/|\\s)[0-9]{1,2}(-)*"),
            Pattern.compile("[0-9]{6}(-|0)*"),
            Pattern.compile("[0-9]{4}(-|0)*"),
            Pattern.compile("[0-9]{2}-[a-zA-Z]{3}-[0-9]{2,4}"),
            Pattern.compile("([0-9]{4})\\s*年\\s*([0-9]{1,2})\\s*月\\s*([0-9]{1,2})")};

    /**
     * 将对象转换为Calendar类
     * 
     * @param o
     *            要转换成Calendar类的对象
     * @param def
     *            转换失败返回的默认值
     * @return Calendar类
     */
    static public final Calendar parseCalendar(final Object o,
            final Calendar def){
        if (o == null) return def;
        if (o instanceof Calendar) return (Calendar) o;
        if (o instanceof Date) {
            Calendar r = Calendar.getInstance();
            r.setTime((Date) o);
            return r;
        }
        return parseCalendar(o.toString(), def);
    }

    static public final Date parseDate(final Object o, final Date def){
        if (o == null) return def;
        if (o instanceof Calendar) return ((Calendar) o).getTime();
        if (o instanceof Date) {
            return (Date) o;
        }
        Calendar cal = parseCalendar(o.toString(), null);
        return cal == null ? def : cal.getTime();
    }

    /**
     * 将时间字符串转换为Calendar类
     * 
     * @param str
     *            要转换的时间字串
     * @param def
     *            转换失败返回的默认值
     * @return Calendar类，str为null，返回null。
     */
    static public final Calendar parseCalendar(String str, final Calendar def){
        str = str == null ? null : str.replaceAll("(\\s){2,}", " ");
        if (str != null && str.length() > 2
                && str.charAt(0) == str.charAt(str.length() - 1)
                && "'\"#".indexOf(str.charAt(0)) >= 0)
            str = str.substring(1, str.length() - 1);
        if (str == null || str.equals(" ") || str.length() < 2
                || str.length() > 28) {
            return def;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 0);// 如果当前日期是31号，而要转换的月份没有31号，没有这行代码，会返回下个月份的日期；
        /**
         * 必须设置缺省值，否则对同样的字符串如“20010101” 两次parseCalendar返回的日期对象的小时、分秒等可能不相等
         */
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int splitIndex = str.indexOf(':') - 3;
        if (splitIndex > 0) {
            String timeStr = str.substring(splitIndex + 1);
            if (TIME_REGEX.matcher(timeStr).matches()) {
                parseTime(timeStr, cal);
                str = str.substring(0, splitIndex);
            }
        }
        if (DATE_REGEX[0].matcher(str).matches()) {// yyyy-mm-dd
            String[] _date = str.split("-|/|\\s");
            int year = getYear(Integer.parseInt(_date[0]));
            cal.set(year, Integer.parseInt(_date[1]) - 1,
                    Integer.parseInt(_date[2]));
            return cal;
        }

        if (DATE_REGEX[2].matcher(str).matches()) {
            String[] _date = str.split("-|/|\\s");
            int year = getYear(Integer.parseInt(_date[0]));
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, Integer.parseInt(_date[1]) - 1);
            return cal;
        }
        if (DATE_REGEX[3].matcher(str).matches()) {// 200201或200201-......
                                                   // 20020200
            cal.set(Calendar.YEAR, Integer.parseInt(str.substring(0, 4)));
            cal.set(Calendar.MONTH, Integer.parseInt(str.substring(4, 6)) - 1);
            return cal;
        }
        // 把这个放后面来是因为前面要处理形如：20020200 和这个正则表达式冲突；
        if (DATE_REGEX[1].matcher(str).matches()) {// 20020101
            cal.set(Integer.parseInt(str.substring(0, 4)),
                    Integer.parseInt(str.substring(4, 6)) - 1,
                    Integer.parseInt(str.substring(6, 8)));
            return cal;
        }
        if (DATE_REGEX[4].matcher(str).matches()) {// 2002或者2002-......
            cal.set(Calendar.YEAR, Integer.parseInt(str.substring(0, 4)));
            return cal;
        }
        if (DATE_REGEX[5].matcher(str).matches()) {// 15-Oct-06
            int month = getMonth(str.substring(3, 6));
            int year = getYear(Integer.parseInt(str.substring(7)));
            cal.set(year, month, Integer.parseInt(str.substring(0, 2)));
            return cal;
        }
        // 20079
        if (str.matches("[0-9]{5}")) {
            int m = Integer.parseInt(str.substring(4, 5)) - 1;
            if (m < 0) return null;
            cal.set(Calendar.YEAR, Integer.parseInt(str.substring(0, 4)));
            cal.set(Calendar.MONTH, m);
            return cal;
        }
        if (str.length() == 28) {// Tue Jun 19 09:28:19 CST 2007
            int month = getMonth(str.substring(4, 7));
            if (month != -1) {
                cal.set(Integer.parseInt(str.substring(24, 28)), month,
                        Integer.parseInt(str.substring(8, 10)),
                        Integer.parseInt(str.substring(11, 13)),
                        Integer.parseInt(str.substring(14, 16)),
                        Integer.parseInt(str.substring(17, 19)));
                return cal;
            }
        }
        Matcher matcher = DATE_REGEX[6].matcher(str);
        if (matcher.find()) {// 2012年2月29日
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int day = Integer.parseInt(matcher.group(3));
            cal.set(year, month - 1, day);
            return cal;
        }
        return def;
    }

    private static final int getYear(int year){
        return year >= 100 ? year : (year < 20 ? year + 2000 : year + 1900);
    }

    /**
     * 根据月份英文简写返回月份对应的 月份数-1
     * 
     * @param mon
     *            月份英文简写
     * @return 对应的月份数-1，(Jan->0)，如果没有对应上，返回-1
     */
    private static final int getMonth(final String mon){
        for (int i = 0; i < MONTH_EN.length; i++)
            if (MONTH_EN[i].equals(mon)) return i;
        return -1;
    }

    private static final Calendar parseTime(final String timeStr,
            final Calendar cal){
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStr.substring(0, 2)));
        cal.set(Calendar.MINUTE, Integer.parseInt(timeStr.substring(3, 5)));
        cal.set(Calendar.SECOND, Integer.parseInt(timeStr.substring(6, 8)));
        if (timeStr.length() > 9)
            cal.set(Calendar.MILLISECOND,
                    Integer.parseInt(timeStr.substring(9)));
        return cal;
    }

    /**
     * 格式化指定字符串的输出，类似于delphi的Format
     */
    static final Pattern ARG_REGEX = Pattern
            .compile("%(\\d+:)?([,，])?(-?\\d+)?(\\.\\d+)?([a-zA-Z])");

    /**
     * 格式化指定字符串的输出，类似于delphi的Format
     * 这个地方传参null会抛NUllPointerException，传参时注意控制参数的正确性 要转换的参数过大时，可能期望值不符，会出现偏差
     * 
     * @param s
     *            要格式化的字串
     * @param arguments
     *            要格式化的样式
     * @return 格式化后的字串
     */
    public static final String formatter(String s, final Object arguments[]){
        s = s.replaceAll("%%", "ㄩ");
        StringBuffer sb = new StringBuffer();
        Matcher m = ARG_REGEX.matcher(s);
        int i = 0;
        int argIndex = -1;
        while (i < s.length()) {
            if (m.find(i)) {
                if (m.start() != i) {
                    sb.append(s.substring(i, m.start()));
                }
                String[] sa = new String[4];
                for (int j = 2; j <= m.groupCount(); j++) {
                    sa[j - 2] = m.group(j);
                }
                argIndex = (m.group(1) == null ? argIndex + 1 : Integer
                        .parseInt(m.group(1).substring(0,
                                m.group(1).length() - 1)));
                sb.append(_argForamtter(sa, arguments[argIndex]));
                i = m.end();
            } else {
                sb.append(s.substring(i));
                break;
            }
        }
        return sb.toString().replaceAll("ㄩ", "%");
    }

    private static final String _argForamtter(final String sa[],
            final Object arg){
        if (arg == null) // 可能出现format('a%b',null)的情况
            return "";

        String res = _typeFormatter(sa[3], arg);
        if (sa[2] != null) {
            res = _precisionFormatter(sa[2], res);
        }
        if (sa[1] != null) {
            res = _widthFormatter(sa[1], res);
        }
        if (sa[0] != null) {
            res = _thousandSign(res);
        }
        return res;
    }

    /**
     * 参考：http://192.168.1.200/wiki/display/bimanual/format
     */
    private static final String _typeFormatter(final String s, final Object arg){
        if (arg instanceof String)
            return arg.toString();
        else if (arg instanceof Number) {
            Number num = (Number) arg;
            switch (s.charAt(0)) {
                case 'D':
                    return HanziFormat.toHanZiSi(num.intValue());
                case 'd':
                    return String.valueOf(Math.round(num.doubleValue()));
                case 'F':
                case 'f':
                    return double2str(num.doubleValue(), 1, 10, false);
                case 'S':
                case 's':
                    return double2str(num.doubleValue(), 0, 10, false);
                case 'T':
                case 't':
                    return formatTime(num.longValue() * 1000, 1000);
                case 'X':
                    return Long.toHexString(num.longValue()).toUpperCase();
                case 'x':
                    return Long.toHexString(num.longValue()).toLowerCase();
                case 'E':
                case 'e': {
                    DecimalFormat df = new DecimalFormat("##0.#####E0");
                    return df.format(num.doubleValue());
                }
            }
        }
        return (arg == null) ? "" : arg.toString();
    }

    /**
     * 将一个整数，转换为大写的形式，如“一”、“二十一”
     * 
     * @param i
     *            整数
     * @return 大写形式
     */
    public static String int2dx(int i){
        return hz2[i - 1];// 暂时的解决方法，待罗中优化
    }

    private static final String _thousandSign(final String arg){
        if (arg == null || arg.length() == 0) // 当有千分符时，如果arg等于空，那么下面的代码有异常
            return "";
        int index = arg.indexOf('.');
        String intValue = index == -1 ? arg : arg.substring(0, index);
        String decimalValue = index == -1 ? "" : arg.substring(index);
        return double2str(Double.parseDouble(intValue), 0, 10, true)
                + decimalValue;
    }

    private static final String _widthFormatter(final String s, final String arg){
        int width = s.charAt(0) == '-' ? Integer.parseInt(s.substring(1))
                : Integer.parseInt(s);
        int argLength = arg.indexOf('.') == -1 ? arg.length()
                : arg.length() - 1;
        if (argLength < width) {
            StringBuffer sb = new StringBuffer();
            if (s.charAt(0) == '-') {
                sb.append(arg);
                for (int i = argLength; i < width; i++)
                    sb.append("0");
            } else {
                for (int i = argLength; i < width; i++)
                    sb.append('0');
                sb.append(arg);
            }
            return sb.toString();
        }
        return arg;
    }

    private static final String _precisionFormatter(final String s,
            final String arg){
        int index = arg.indexOf('.');
        if (index != -1) {
            StringBuffer sb = new StringBuffer(arg);
            int num = Integer.parseInt(s.substring(1));
            String ss = arg.substring(index + 1);
            int len = ss.length();
            if (len < num) {
                for (int i = 0; i < num - len; i++)
                    sb.append('0');
                return sb.toString();
            } else if (len > num && num > 0)
                return sb.substring(0, sb.length() - len + num);
            else if (num == 0) return sb.substring(0, index);
        }
        return arg;
    }

    /**
     * 如果i大于max或者i小于min则返回def，否则返回i
     */
    static public final int limit(final int i, final int max, final int min,
            final int def){
        if (i > max || i < min) {
            return def;
        } else {
            return i;
        }
    }

    /**
     * 将一个byte数组内容转为hex串，约定一个字节对应两个hex字符
     * 
     * @param bb
     *            要转行的byte数组
     * @return 转行后的hex串
     */
    static public final String bytesToHexString(final byte[] bb){
        if (bb == null) return null;// 避免空指针异常
        StringBuffer result = new StringBuffer();
        String hex;
        int cc = 0;
        for (int i = 0; i < bb.length; i++) {
            cc = bb[i];
            if (cc < 0) {
                cc = 256 + cc;
            }
            hex = Integer.toHexString(cc);
            if (hex.length() < 2) {
                hex = "0" + hex;
            }
            result.append(hex);
        }
        return result.toString();
    }

    /**
     * 2004.06.04 hexToBytes检查字符数,如果是奇数,异常; 将16进制的串转为一个byte数组，时bytesToHex的逆函数
     * 约定两个16进制的字符转为一个byte
     * 
     * @param hex
     *            16进制的字符串
     * @return byte数组
     */
    static public final byte[] hexStringToBytes(final String hex){
        if ((hex == null) || (hex.length() < 2)) {
            return null;
        }
        if (hex.length() % 2 == 1) {
            throw new java.lang.RuntimeException("not a valid hex string!");
        }
        byte[] result = new byte[hex.length() / 2];
        int i = 0;
        String hh;
        int b;
        while (i < result.length) {
            hh = hex.substring(i * 2, i * 2 + 2);
            b = Integer.parseInt(hh, 16);
            if (b > 127) {
                b = b - 256;
            }
            result[i] = (byte) b;
            i++;
        }
        return result;
    }

    /**
     * ireport用此函数 对引号进行解码，fromindex位置是左引号，找到右引号，返回这两个引号之间的字符串。
     * 
     * @param s
     *            需要解密的字符串
     * @param fromindex
     *            从frominde开始解码，fromindex位置是左引号
     * @param quote
     *            代表引号，一般就是字符'"'
     * @param newindex
     *            newindex[0]记录右引号位置+1
     * @return 解密后的字符串
     */
    public static final String extractQuotedStr(final String s,
            final int fromindex, final char quote, final int[] newindex){
        int i = -1;
        if (newindex != null) i = 0;
        String r = extractQuotedStr(s, fromindex, quote, i);
        if (newindex != null) newindex[0] = i;
        return r;
    }

    /**
     * 对引号进行解码，fromindex位置是左引号，找到右引号，返回这两个引号之间的字符串。 特殊地：
     * 1、如果找不到右引号，则返回fromindex之后的字符串
     * 2、引号是特殊字符，用两个相邻的引号来对引号进行转义，这样就允许返回的字符串中间含有引号
     * 3、除了引号是特殊字符，其它的比如换行符、\t这些都不是特殊字符。
     * <p/>
     * 在实现时，要先求右引号的位置，确定返回字符串的长度，根据这个长度创建一个StringBuffer。
     * 这样创建的StringBuffer不会再自动扩展长度
     * ，对于很长的字符串，自动扩展长度可能导致“java.lang.OutOfMemoryError: Java heap space”
     * 
     * @param s
     *            要解码的字符串
     * @param fromindex
     *            从frominde开始解码，fromindex位置是左引号
     * @param quote
     *            代表引号，一般就是字符'"'
     * @param vi
     *            记录右引号的位置+1，如果没有右引号，则设置为字符串的长度
     * @return 解密后的字符串
     */
    public static final String extractQuotedStr(final String s,
            final int fromindex, final char quote, int vi){
        int length = s.length();

        /* 先找到右引号的位置 */
        int start = fromindex + 1;
        int quoteAt = s.indexOf(quote, start);
        boolean hasDoubleQuote = false;
        while (quoteAt != -1) {
            if ((length > quoteAt + 1) && (s.charAt(quoteAt + 1) == quote)) {
                quoteAt++;
                hasDoubleQuote = true;
            } else {
                break;
            }
            start = quoteAt + 1;
            quoteAt = s.indexOf(quote, start);
        }

        int end = quoteAt == -1 ? length : quoteAt;
        vi = end + 1;

        /* 当左右引号之间没有转义引号时可以直接返回字串，避免了创建StringBuffer */
        if (!hasDoubleQuote) {
            return s.substring(fromindex + 1, end);
        }

        start = fromindex + 1;
        quoteAt = s.indexOf(quote, start);
        StringBuffer result = new StringBuffer(end - start); // 创建大小合适的StringBuffer，避免下面调用append方法时重新分配内存
        while (quoteAt != -1) {
            if ((length > quoteAt + 1) && (s.charAt(quoteAt + 1) == quote)) {
                quoteAt++;
                result.append(s.substring(start, quoteAt));
            } else {
                result.append(s.substring(start, quoteAt));
                break;
            }
            start = quoteAt + 1;
            quoteAt = s.indexOf(quote, start);
        }

        return result.toString();
    }

    /**
     * 搜索字符在字符数组中是否存在
     * char数组传参null，fromIndex超出了char[]的长度，toIndex、fromIndex为负数会抛异常
     * 考虑到效率问题没有进行处理，传参的时候注意参数的正确性
     * 
     * @param chars
     *            字符数组
     * @param eq
     *            要查找的字符
     * @param fromIndex
     *            从第几个开始找
     * @param toIndex
     *            到第几个不找了
     * @return 找到了返回找到的位置，没找到返回-1
     */
    public static final int indexOf(final char[] chars, final char eq,
            final int fromIndex, final int toIndex){
        for (int i = fromIndex; i < toIndex; i++) {
            if (chars[i] == eq) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取一个汉字串对应的拼音首字母串； 如果字符串内容有不是汉字的，原串返回，有些日文字或中文繁体字返回_如：镕，
     * 对于某些多音中文字，只会根据编码习惯返回一种首字母，如：藏宝图，宝藏，藏 -> C
     * 有些中文字，没有认定为汉字，返回原串，且如果该字后面有其他汉字，与他相连的字也返回原串，如：諤
     * 
     * @param hzStr
     *            汉字串
     * @return 拼音首字母串
     */
    public static final String getPyString(final String hzStr){
        try {
            byte[] bb = hzStr.getBytes("GBK");
            int j = 0;
            StringBuffer py = new StringBuffer();
            for (int i = 0; i < hzStr.length(); i++) {
                char c = hzStr.charAt(i);
                // bb[j] if range 0--256,>0x81 && bb[j+1]>0x40
                if ((bb[j] < 0) && ((bb[j + 1] < 0) || (bb[j + 1] > 0x40))) {
                    c = getPyChar(c);
                    j++;
                }
                j++;
                py.append(c);
            }
            return py.toString();
        } catch (Exception ex) {
            ex.printStackTrace();// TODO 不加调试开关的打印请做处理
            return "";
        }
    }

    /**
     * 数据库表名不容许特殊字符，这里把除字母，下划线，数字外的字符替换掉 null传参会抛异常，传参时要注意
     * 
     * @param s
     *            需要替换的字符
     * @return 替换后的字符
     */
    public static final String formatTableName(final String s){
        StringBuffer sb = new StringBuffer(s.length());
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z') || ch == '_') {
                sb.append(ch);
            }
        }

        return sb.toString();
    }

    /**
     * 获取中文字符串的汉语拼音； 每个字的拼音用空格隔开；
     * 
     * @param str
     *            中文字符串
     * @return 对应的含有拼音
     */
    public static final String cnToSpell(final String str){
        if (str == null) return null;
        return CnToSpell.getFullSpell(str);
    }

    /**
     * 判断字符是否是中文字符 不太准确，认为非ascii字符都是中文字符
     * 
     * @param c
     *            需要判断的字符
     * @return 是中文字符返回true，反之false
     */
    public static final boolean isHz(char c){
        return c < 0 || c > 0x7f;
    }

    public static final String format2HtmlStr(final String s){
        return htmlFilter(s, false);
    }

    /**
     * Filter the specified message string for characters that are sensitive in
     * HTML. This avoids potential attacks caused by including JavaScript codes
     * in the request URL that is often reported in error messages.
     * 不在结果中增加双引号，不考虑回车换行与制表符进行转换,忽略空格，即空格不转换成&#xA0;，原样输出
     * 
     * @param s
     *            The message string to be filtered
     * @return 处理后的字符串
     */
    public static final String format2HtmlStr_ignoreBlank(final String s){
        return htmlFilter(s, true);
    }

    /**
     * Filter the specified message string for characters that are sensitive in
     * HTML. This avoids potential attacks caused by including JavaScript codes
     * in the request URL that is often reported in error messages.
     * 不在结果中增加双引号，不考虑回车换行与制表符进行转换
     * 
     * @param message
     *            The message string to be filtered
     * @param ignoreBlank
     *            是否忽略对空格的处理，对空格的处理是将其转换为&#xA0;字符输出，因为在Firefox中，当页面是XHTML时&nbsp;
     *            是无效的
     * @return 处理后的字符串
     */
    public static final String htmlFilter(final String message,
            final boolean ignoreBlank){
        return format2HtmlStr(message, ignoreBlank, false, false);
    }

    /**
     * Filter the specified message string for characters that are sensitive in
     * HTML. This avoids potential attacks caused by including JavaScript codes
     * in the request URL that is often reported in error messages.
     *
     * @param message
     *            The message string to be filtered
     * @param ignoreBlank
     *            是否忽略对空格的处理，对空格的处理是将其转换为&#xA0;字符输出，因为在Firefox中，当页面是XHTML时&nbsp;
     *            是无效的
     * @param quateit
     *            是否在返回的结果中增加双引号
     * @param toTextArea
     *            是否将结果输出到文本编辑框TextArea内，如果是则不需要对回车换行与制表符进行转换
     * @return 处理后的字符串
     */
    public static final String format2HtmlStr(final String message,
            final boolean ignoreBlank, final boolean quateit,
            final boolean toTextArea){
        if (message == null) {
            return (null);
        }

        int len = message.length();
        StringBuffer result = new StringBuffer(len + 50);
        if (quateit) result.append('"');
        char c;
        for (int i = 0; i < len; i++) {
            c = message.charAt(i);
            switch (c) {
            /**
             * 增加对回车换行与制表符的转义 回车换行<br/>
             * 中的<和>也需要转义,否则在firefox中用xhtml显示时,格式也是不合法的 如hint="a<br/>
             * b"必须转换为hint="a&lt;br/&gt;b"
             */
                case '\r':
                    if (!toTextArea) {
                        result.append("&lt;br/&gt;");
                        if (i + 1 < len && message.charAt(i + 1) == '\n') {
                            i++;
                        }
                    } else {
                        result.append(c);
                    }
                    break;
                case '\n':
                    if (!toTextArea) {
                        result.append("&lt;br/&gt;");
                    } else {
                        result.append(c);
                    }
                    break;
                case '\t':
                    if (!toTextArea) {
                        result.append("&#xA0;&#xA0;&#xA0;&#xA0;");
                    } else {
                        result.append(c);
                    }
                    break;
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                case '&':
                    result.append("&amp;");
                    break;
                case '"':
                    result.append("&quot;");
                    break;
                case ' ':
                    if (ignoreBlank)
                        result.append(c);
                    else
                        result.append("&#xA0;");
                    break;
                default:
                    result.append(c);
            }
        }
        if (quateit) result.append('"');
        return (result.toString());
    }

    public static final String format2HtmlStr(final String message,
            final boolean ignoreBlank, final boolean quateit){
        return format2HtmlStr(message, ignoreBlank, quateit, false);
    }

    /**
     * 此方法是将后台字段串，转换成HTML端能显示的字符串，比如JAVA换行转换成HTML换行
     * 
     * @param message
     * @param formatJs
     *            是否处理成JS字符串，比如引号加转义
     * @return
     */
    public static final String format2Html(final String message,
            boolean formatJs){
        if (message == null) {
            return (null);
        }

        int len = message.length();
        StringBuffer result = new StringBuffer(len + 50);
        char c;
        for (int i = 0; i < len; i++) {
            c = message.charAt(i);
            switch (c) {
                case '\r':
                    result.append("<br/>");
                    if (i + 1 < len && message.charAt(i + 1) == '\n') {
                        i++;
                    }
                    break;
                case '\n':
                    result.append("<br/>");
                    break;
                case '\t':
                    result.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                    break;

                default:
                    result.append(c);
            }
        }
        if (formatJs) {
            return StrYn.formatJsStr(result.toString());
        }
        return (result.toString());
    }

    /**
     * 获取一个汉字字符对应的拼音首字母；
     * 
     * @param hzchar
     *            有的汉字会返回_，如：難
     * @return 汉字字符对于首字母
     */
    public static final char getPyChar(final char hzchar){
        byte[] bb;
        try {
            bb = (new Character(hzchar)).toString().getBytes("GBK");
        } catch (Exception ex) {
            return '_';
        }
        int a = ((bb[0] & 0x7F) + 128) << 8;
        int b = bb[1];
        if (b < 0) { // byte取值<=127
            b = bb[1] & 0x7F + 128;
        }
        int hz = a | b;
        if ((hz >= 0xB0A1) && (hz <= 0XB0C4)) {
            return 'A';
        }
        if ((hz >= 0XB0C5) && (hz <= 0XB2C0)) {
            return 'B';
        }
        if ((hz >= 0xB2C1) && (hz <= 0XB4ED)) {
            return 'C';
        }
        if ((hz >= 0xB4EE) && (hz <= 0XB6E9)) {
            return 'D';
        }
        if ((hz >= 0xB6EA) && (hz <= 0XB7A1)) {
            return 'E';
        }
        if ((hz >= 0xB7A2) && (hz <= 0XB8C0)) {
            return 'F';
        }
        if ((hz >= 0xB8C1) && (hz <= 0XB9FD)) {
            return 'G';
        }
        if ((hz >= 0xB9FE) && (hz <= 0XBBF6)) {
            return 'H';
        }
        if ((hz >= 0xBBF7) && (hz <= 0XBFA5)) {
            return 'J';
        }
        if ((hz >= 0xBFA6) && (hz <= 0XC0AB)) {
            return 'K';
        }
        if ((hz >= 0xC0AC) && (hz <= 0XC2E7)) {
            return 'L';
        }
        if ((hz >= 0xC2E8) && (hz <= 0xC4C2)) {
            return 'M';
        }
        if ((hz >= 0xC4C3) && (hz <= 0xC5B5)) {
            return 'N';
        }
        if ((hz >= 0xC5B6) && (hz <= 0xC5BD)) {
            return 'O';
        }
        if ((hz >= 0xC5BE) && (hz <= 0xC6D9)) {
            return 'P';
        }
        if ((hz >= 0xC6DA) && (hz <= 0xC8BA)) {
            return 'Q';
        }
        if ((hz >= 0xC8BB) && (hz <= 0xC8F5)) {
            return 'R';
        }
        if ((hz >= 0xC8F6) && (hz <= 0xCBF9)) {
            return 'S';
        }
        if ((hz >= 0xCBFA) && (hz <= 0xCDD9)) {
            return 'T';
        }
        if ((hz >= 0xCDDA) && (hz <= 0xCEF3)) {
            return 'W';
        }
        if ((hz >= 0xCEF4) && (hz <= 0xD1B8)) {
            return 'X';
        }
        if ((hz >= 0xD1B9) && (hz <= 0xD4D0)) {
            return 'Y';
        }
        if ((hz >= 0xD4D1) && (hz <= 0xD7F9)) {
            return 'Z';
        }
        return '_';
    }

    /**
     * 将异常的堆栈调用信息转化为字符串
     * 
     * @param e
     *            异常
     * @return 异常的堆栈信息
     */
    public static final String exception2str(final Throwable e){
        return exception2str(e, null);
    }

    /**
     * 将异常的堆栈调用信息转化为字符串，会在返回值前面添加指定的msg
     * 
     * @param e
     *            异常
     * @return 异常的堆栈信息
     */
    public static final String exception2str(final Throwable e, String msg){
        if (e == null) return msg;
        StringWriter sw = new StringWriter(1024 * 4);// 一个普通的异常堆栈，一般4k
        PrintWriter pw = new PrintWriter(sw);
        if (msg != null && msg.length() > 0) {
            pw.print(msg);
            pw.print(' ');
        }
        e.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    /**
     * 返回异常信息。 当异常信息为空时，返回异常的类型信息。 对于sql异常，返回嵌套的所有异常的信息。
     * 
     * 20090204 修改参数的类型为Throwable，这样本函数更通用。
     * 
     * @param e
     *            异常
     * @return 当异常信息为空时，返回异常的类型信息。对于sql异常，返回嵌套的所有异常的信息。
     */
    public static final String exceptionMsg2str(final Throwable e){
        if (e == null) return null;
        String message = e.getMessage();
        if (message == null) message = e.getClass().getName();

        if (e instanceof SQLException) {
            SQLException nextException = ((SQLException) e).getNextException();
            if (nextException != null)
                message += "\r\n" + exceptionMsg2str(nextException);
        }
        return message;
    }

    /**
     * 同方法exceptionMsg2str，但返回当前线程语言的异常信息，主要是供前台显示调用
     * 
     * @param e
     * @return
     */
    public static final String exceptionMsg2LocalizedStr(final Throwable e){
        if (e == null) return null;
        String message = e.getLocalizedMessage();
        if (message == null) message = e.getClass().getName();

        if (e instanceof SQLException) {
            SQLException nextException = ((SQLException) e).getNextException();
            if (nextException != null)
                message += "\r\n" + exceptionMsg2str(nextException);
        }
        return message;
    }

    /**
     * 比较2个字符串是否一致 都为空返回true,都不为空且内容一样也返回true,否则返回false
     * 
     * @param str1
     *            要比较的字符串
     * @param str2
     *            要比较的字符串
     * @return 都为空返回true,都不为空且内容一样也返回true,反之false
     */
    public static final boolean compareStr(final String str1, final String str2){
        return ((str1 == null) && (str2 == null))
                || ((str1 != null) && (str2 != null) && (str1.equals(str2)));
    }

    /**
     * 比较字符串的大小，如果str1>str2，返回正整数；如果str1=str2，返回0；如果str1<str2，返回负整数。
     * 
     * @param str1
     *            比较的字符串
     * @param str2
     *            比较的字符串
     * @return 如果str1>str2，返回正整数；如果str1=str2，返回0；如果str1<str2，返回负整数。
     */
    public static final int compareStrInt(final String str1, final String str2){
        return str1 == null ? (str2 == null ? 0 : -1) : (str2 == null ? 1
                : str1.compareTo(str2));
    }

    /**
     * 比较字符串的大小，如果str1>str2，返回1；如果str1=str2，返回0；如果str1<str2，返回-1。 比较时忽略大小写
     * 
     * @param str1
     *            比较的字符串
     * @param str2
     *            比较的字符串
     * @return 如果str1>str2，返回正整数；如果str1=str2，返回0；如果str1<str2，返回负整数。
     */
    public static final int compareStrIntIgnoreCase(final String str1,
            final String str2){
        return str1 == null ? (str2 == null ? 0 : -1) : (str2 == null ? 1
                : str1.compareToIgnoreCase(str2));
    }

    /**
     * 比较字符串是否相等 比较时忽略""和null的区别
     * 
     * @param str1
     *            比较的字符串
     * @param str2
     *            比较的字符串
     * @return 字符串相等返回true，反之false
     */
    public static final boolean compareStrIgnoreBlank(final String str1,
            final String str2){
        return ((str1 == null || str1.length() == 0) && (str2 == null || str2
                .length() == 0))
                || ((str1 != null) && (str2 != null) && (str1.equals(str2)));
    }

    /**
     * 比较字符串是否相等 比较时忽略字母大小写
     * 
     * @param str1
     *            比较的字符串
     * @param str2
     *            比较的字符串
     * @return 字符串相等返回true，反之false
     */
    public static final boolean compareStrIgnoreCase(final String str1,
            final String str2){
        return ((str1 == null) && (str2 == null))
                || ((str1 != null) && (str2 != null) && (str1
                        .equalsIgnoreCase(str2)));
    }

    /**
     * //TODO 和compareText重复，可以删之一, 建议删掉compareStrIgnoreBlankAndCase方法，
     * 把compareText方法改名为compareStrIgnoreBlankAndCase 比较字符串是否相等
     * 比较时忽略""和null的区别，并且忽略字母大小写
     * 
     * @param str1
     *            比较的字符串
     * @param str2
     *            比较的字符串
     * @return 字符串相等返回true，反之false
     */
    public static final boolean compareStrIgnoreBlankAndCase(final String str1,
            final String str2){
        return ((str1 == null || str1.length() == 0) && (str2 == null || str2
                .length() == 0))
                || ((str1 != null) && (str2 != null) && (str1
                        .equalsIgnoreCase(str2)));
    }

    /**
     * 判断两个str数组内容是否一致；
     * 
     * @param s
     *            要进行判断的String数组
     * @param s2
     *            要进行判断的String数组
     * @return 2个String数组内容一致返回true，反之false
     */
    public static final boolean compareStrs(final String[] s, final String[] s2){
        if (s == null && s2 == null) return true;
        if (s == null || s2 == null) return false;
        if (s.length != s2.length) return false;
        for (int i = 0; i < s.length; i++) {
            if (!compareStr(s[i], s2[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 比较字符串是否相等 比较时忽略""和null的区别，并且忽略字母大小写
     * 
     * @param str1
     *            比较的字符串
     * @param str2
     *            比较的字符串
     * @return 字符串相等返回true，反之false
     */
    public static final boolean compareText(final String str1, final String str2){
        return ((str1 == str2)
                || ((str1 == null || str1.length() == 0) && (str2 == null || str2
                        .length() == 0)) || (str1 != null && str2 != null && str1
                .compareToIgnoreCase(str2) == 0));
    }

    /**
     * 字符串为null或左右2端去空格后长度为0，返回null
     * 
     * @param str
     *            需要格式化的字符串
     * @return 如果字符串为null或者左右2端去空格后长度为0，返回null，反之返回原串去除左右空格
     */
    public static final String formatNull(final String str){
        if (str == null || str.trim().length() == 0) return null;
        return str.trim();
    }

    /**
     * 如果参数为空，返回""，如果不为空，转成String类型返回
     * 
     * @param str
     *            需要转的对象
     * @return String类型值
     */
    public static final String null2blank(final Object str){
        /**
         * 这里为了兼容原有的使用方式，判断str是否为String的实例，并分别进行处理 如不这样则会造成很多页面或者类出现异常
         */
        return (str == null) ? "" : (str instanceof String ? (String) str : str
                .toString());
    }

    /**
     * 如果str为空时，就用def的值进行替代
     * 
     * @param str
     *            要转换的字符串
     * @param def
     *            代替空串的字符串
     * @return str为空时，就用def的值进行替代，反之返回原串
     */
    public static final String null2default(final String str, final String def){
        return (str == null || str.length() <= 0) ? def : str;
    }

    /**
     * 判断字符串是否为 null 或 ""
     * 
     * @param str
     *            需要判断的字符串
     * @return 为空返回true，反之false
     */
    public static final boolean isNull(final String str){
        return (str == null || str.length() <= 0);// 不要trim str，效率不好，也和函数的名称不匹配
    }

    /**
     * 判断一个字符串去掉左右两端的空白字符后是否为空
     * 
     * @param str
     *            需要判断的字符串
     * @return 去掉左右两端的空白字符后为空则返回true，反之false
     */
    public static final boolean isTrimedNull(final String str){
        return (str == null || isNull(str.trim()));
    }

    /**
     * 判断对象是否为"空"，这里“空”有特殊含义，指对象没有值。 一般为空的对象不用输出到npf中。
     * 
     * @param obj
     *            要判断的对象
     * @return 如果对象为空，返回false，反之true
     */
    public static final boolean isNotEmpty(final Object obj){
        if (obj == null) return false;
        if (obj instanceof String) return ((String) obj).length() > 0;
        if (obj instanceof Double) return !((Double) obj).isNaN();
        return true;
    }

    /**
     * 判断是否是大写字母
     * 
     * @param c
     *            需要判断在字符
     * @return 是大写字母返回true，反之flase
     */
    public final static boolean isABC_Z(final char c){
        return c >= 'A' && c <= 'Z';
    }

    /**
     * 判断是否是小写字母
     * 
     * @param c
     *            需要判断在字符
     * @return 是小写字母返回true，反之flase
     */
    public static final boolean isabc_z(final char c){
        return c >= 'a' && c <= 'z';
    }

    /**
     * 判断是否是字母
     * 
     * @param c
     *            需要判断在字符
     * @return 是字母返回true，反之flase
     */
    public static final boolean isABC_xyz(final char c){
        return isabc_z(c) || isABC_Z(c);
    }

    /**
     * 判断字符是否是数字
     * 
     * @param c
     *            传入的字符
     * @return 字符是数字，返回true，反之false
     */
    public static final boolean isDigit(final char c){
        return c >= '0' && c <= '9';
    }

    /**
     * 判断是否是字母、数字、下划线组成的字符串 考虑到效率问题，没有对null和""传参进行处理
     * 
     * @param str
     *            要判断的字符串
     * @return 是字母数字下划线组成的字符串返回true，反之false
     */
    public static final boolean isABC_xyz_123(final String str){
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z') || (c == '_')) == false) {
                return false;
            }
        }

        return true;
    }

    public static final String format(final String fmt, final String[] params){
        // to do
        return fmt;
    }

    /**
     * 将字符串以','作为分隔符，分隔成字符串数组
     * 
     * @param str
     *            要分隔的字符串
     * @return 分隔后的字符串数组
     */
    public static final String[] analyzeStr(final String str){ // 分析以','分割的字符串
        return analyzeStr(str, ',');
    }

    /**
     * 将字符串以separator作为分隔符，分隔成字符串数组
     * 
     * @param str
     *            要分隔的字符串
     * @param separator
     *            分隔符
     * @return 分隔后的字符串数组
     */
    public static final String[] analyzeStr(final String str,
            final char separator){
        if (str == null) return null;
        int len = str.length();
        int count = charCount(str, separator); // 取字符串中','出现的次数
        String result[] = new String[count + 1]; // 分配空间
        if (count == 0) {
            result[0] = str;
        }
        int strId = 0, o = 0, n = 0;
        while (++strId < len) {
            if (str.charAt(strId) == separator) { // 检测是否为','字符
                result[n] = str.substring(o, strId); // 提取至数组中
                o = strId + 1;
                n++;
                if (n == count) {
                    result[n] = str.substring(o, len);
                }
            }
        }
        return result;
    }

    /**
     * 将一数字变成格式化字符串。该函数类似于C语言中的printf函数。 例如： Format("%d",1234)="1234"
     * Format("%.8d",1234)="00001234" Format("%f",12.34)="12.34"
     * Format("%.1f",12.3412)="12.3"
     * 
     * @param ff
     * @param v
     * @return
     */
    private static final String _blank_zero = "00000000000000";

    private static final String _blank = "                 ";

    /**
     * 将double类型的数据v按照ff的格式进行格式化
     * 例如：format("%2d",7)=" 7"</br>format("%.2d",7)="07" 这个很像c里面的。x 16进制，o 8进制
     * ，e 科学计数法， u 汉字
     * 
     * @param ff
     *            要格式化的样式
     * @param v
     *            要格式化的数据
     * @return 格式化后的数据(String)
     */
    public static final String format(final String ff, final double v){
        if (ff == null || ff.length() == 0) {
            return ff;
        }
        int i = ff.indexOf('%');
        if (i == -1) {
            return ff;
        }
        int j;
        char c = 0;
        for (j = i + 1; j < ff.length(); j++) {
            c = ff.charAt(j);
            if (c == 'f' || c == 'd') {
                break;
            }
        }
        // Format("%x",31)="1F" 　将num格式化为16进制，num必须是一整数
        if (c == 'x') {
            return Long.toHexString(new Double(v).longValue());
        }
        // Format("%o",12)="14" 　将num格式化为8进制，num必须是一整数
        if (c == 'o') {
            return Long.toOctalString(new Double(v).longValue());
        }
        // Format("%e",1234.56)="1.23456000000000E003" 　用科学计数法表示
        if (c == 'e') {
            // DecimalFormat
            DecimalFormat df = new DecimalFormat("#.##############E0");
            return df.format(v);
        }
        // Format("%u",2005)=“二零零五”　将整数格式化为大写
        if (c == 'u') {
            DecimalFormat.getInstance(Locale.CHINA);
            String s = String.valueOf((long) v);
            StringBuffer buf = new StringBuffer();
            for (int a = 0; a < s.length(); a++) {
                if (s.charAt(a) == '-') {
                    buf.append("负");
                } else {
                    buf.append(hz[s.charAt(a) - '0']);
                }
            }
            return buf.toString();
        }
        if (j >= ff.length()) {
            return ff;
        }
        String prefix = ff.substring(0, i);
        String suffix = ff.substring(j + 1);

        String s = ff.substring(i + 1, j);
        String ss;
        if (c == 'd') {
            ss = String.valueOf((int) v);
        } else {
            ss = String.valueOf(v);
        }

        if (s == null || s.length() == 0) {
            return prefix + ss + suffix;
        }
        String blank;
        if (s.charAt(0) == '.') {
            s = s.substring(1);
            blank = _blank_zero;
        } else {
            blank = _blank;
        }
        int l = Integer.parseInt(s);
        if (c == 'd') {
            if (ss.length() < l) {
                ss = blank.substring(0, l - ss.length()) + ss;
            }
        } else if (c == 'f') {
            int d = ss.indexOf('.');
            if (d > 0 && ss.length() - d > l) {
                ss = ss.substring(0, d + l + 1);
            } else if (d > 0) {
                ss = ss + _blank_zero.substring(0, l - ss.length() + d + 1);
            } else if (d < 0) {
                ss = ss + ".000000000000000".substring(0, l + 2);
            }
        }
        return prefix + ss + suffix;
    }

    /**
     * 将浮点 数格式化为指定小数位置的字符串。 格式化前先按指定小数位置4舍5入(依excel算法)。
     * 
     * @param d
     *            要格式化的double数
     * @param decLen
     *            小数精确的位数及显示的位数
     * @param groupnum
     *            是否将这种格式应用分组
     * @return double类型的字符串显示
     */
    public static String formatDouble(double d, int decLen, boolean groupingUsed){
        return double2str(d, decLen, decLen, groupingUsed);
    }

    private static DecimalFormat decimalFormat = new DecimalFormat();

    /**
     * 此函数负责浮点数转成字符串用来显示。 由于double类型的限制，转换成字符串后的数值只能保证最大15或16位有效位数。
     * 
     * @param d
     *            要转换成串的浮点数
     * @param mindeclen
     *            设置小数部分允许的最小位数(小数部分显示的位数)
     * @param maxdeclen
     *            设置小数部分允许的最大位数(小数部分精确的位数)
     * @param groupnum
     *            是否将这种格式应用分组
     * @return
     */
    static final public String double2str(final double d, final int mindeclen,
            final int maxdeclen, final boolean groupnum){
        if (Double.isNaN(d)) return "";
        if (d == Double.NEGATIVE_INFINITY) return "-∞";
        if (d == Double.POSITIVE_INFINITY) return "∞";
        // 很多情况下d为0，此时优化一下，避免创建很多对象
        if (Math.abs(d - 0.0) < 0.0000000001 && mindeclen < 5)
            return DOUBLE2STR_ZEROS[mindeclen];

        decimalFormat.setGroupingUsed(groupnum);
        decimalFormat.setMaximumFractionDigits(maxdeclen);
        decimalFormat.setMinimumFractionDigits(mindeclen);
        return decimalFormat.format(MathUtil.round(d, maxdeclen));
    }

    /**
     * 获取当前时间，format指定时间的格式。 FULL_DATE 返回"yyyy-MM-dd HH:mm:ss"形式的完整格式。
     * SMALL_DATE 返回"yyyy-MM-dd"形式的精简格式。 DATA_DATE 返回"yyyyMMddHHmmss"形式的时间
     */
    public static final String FULL_DATE = "yyyy-MM-dd HH:mm:ss";
    public static final String SMALL_DATE = "yyyy-MM-dd";
    public static final String DATA_DATE = "yyyyMMddHHmmss";
    public static final String TIME_DATE = "HH:mm:ss";

    /**
     * 格式化当前时间
     * 
     * @param format
     *            显示的格式
     * @return 格式化后的时间字符串
     * @throws Exception
     */
    public static String formatDate(String format) throws Exception{
        java.text.DateFormat df = new java.text.SimpleDateFormat(format);
        return df.format(new java.util.Date());
    }

    /**
     * 格式化指定日期对象
     * 
     * @param format
     *            显示格式
     * @param dt
     *            需要格式的日期对象
     * @return 格式化后的时间字符串
     */
    public static final String formatDate(String format, java.util.Date dt)
            throws Exception{
        java.text.DateFormat df = new java.text.SimpleDateFormat(format);
        return df.format(dt);
    }

    public static final String DATETIMEFORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 根据指定的毫秒数初始化一个时间对象，以格林时间1970-01-01 00:00:00为起始时间
     * 
     * @param l
     *            毫秒数
     * @return 日期时间字符串 格式为yyyy-MM-dd HH:mm:ss
     */
    public static final String formatDateTime(final long l){
        return formatDateTime(new java.util.Date(l));
    }

    /**
     * 将日期格式格式化为yyyy-MM-dd HH:mm:ss String类型
     * 
     * @param d
     *            日期时间
     * @return yyyy-MM-dd HH:mm:ss格式的String
     */
    public static final String formatDateTime(final Date d){
        java.text.DateFormat df = new java.text.SimpleDateFormat(DATETIMEFORMAT);
        return df.format(d);
    }

    /**
     * 格式化日期字符串。 有别于date2str使用Bi自己的日期格式，这里的格式是jdk标准的格式。
     * 
     * @param l
     *            毫秒数
     * @param format
     *            格式字符串，比如"yyyy-MM-dd HH:mm:ss.SSS"
     * @return
     */
    public static final String formatDateTime(final long l, final String format){
        java.text.DateFormat df = new java.text.SimpleDateFormat(format);
        return df.format(new java.util.Date(l));
    }

    /**
     * 返回一个简短易懂的日期描述 如果l和当前日期的年月日都一样，那么只显示时分秒 如果l和当前日期的年月都一样，那么只显示日时分
     * 如果l和当前日期的年一样，那么只显示月日 否则只显示年月日
     * 
     * @param l
     *            毫秒数
     * @return 日期描述
     */
    public static final String formatShortDateTime(final long l){
        Calendar d = Calendar.getInstance();
        d.setTimeInMillis(l);
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(l);
        String format;

        if (d.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
            if (d.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
                if (d.get(Calendar.DAY_OF_MONTH) == now
                        .get(Calendar.DAY_OF_MONTH)) {
                    format = "HH:mm:ss";
                } else {
                    // format = "dd号HH:mm分";
                    format = I18N.getString("com.esen.util.StrFunc.8",
                            "dd号HH:mm分");
                }
            } else {
                // format = "MM月dd日";
                format = I18N.getString("com.esen.util.StrFunc.9", "MM月dd日");
            }
        } else {
            format = "yyyy-MM-dd";
        }

        java.text.DateFormat df = new java.text.SimpleDateFormat(format);
        return df.format(new java.util.Date(l));
    }

    /**
     * 格式化时间，参数l单位是毫秒
     * 
     * @param l
     *            需要格式化的毫秒数
     * @return 格式化后的日期字符串
     */
    public static String formatTime(final long l){
        return formatTime(l, 1);
    }

    /**
     * 格式化时间，参数l单位是毫秒
     * 
     * @param l
     *            需要格式化的毫秒数
     * @param unit
     *            精确的位数，unit大于3600000，只显示天数，大于60000小于等于3600000，显示天数和小时数因此类推
     * @return 格式化后的日期字符串
     */
    public static String formatTime(final long l, long unit){
        long ms = l % 1000;
        long s = (l / 1000) % 60;
        long m = l / (1000 * 60) % 60;
        long h = l / (1000 * 60 * 60) % 24;
        long d = l / (1000 * 60 * 60 * 24);
        // return ((d > 0) ? (d + "天") : "") +
        // ((unit<=1000 * 60 * 60 && h > 0) ? (h + "小时") : "") +
        // ((unit<=1000 * 60 && m > 0) ? (m + "分") : "") +
        // ((unit<=1000 && s > 0) ? (s + "秒") : "") +
        // ((unit==1 && ms > 0 || ms == l) ? (ms + "毫秒") : "");
        return ((d > 0) ? I18N.getString("com.esen.util.StrFunc.10", "{0}天",
                new String[] {d + ""}) : "")
                + ((unit <= 1000 * 60 * 60 && h > 0) ? I18N.getString(
                        "com.esen.util.StrFunc.11", "{0}小时", new String[] {h
                                + ""}) : "")
                + ((unit <= 1000 * 60 && m > 0) ? I18N.getString(
                        "com.esen.util.StrFunc.12", "{0}分", new String[] {m
                                + ""}) : "")
                + ((unit <= 1000 && s > 0) ? I18N.getString(
                        "com.esen.util.StrFunc.13", "{0}秒", new String[] {s
                                + ""}) : "")
                + ((unit == 1 && ms > 0 || ms == l) ? I18N.getString(
                        "com.esen.util.StrFunc.14", "{0}毫秒", new String[] {ms
                                + ""}) : "");
    }

    /**
     * 格式化当前时间对象
     * 
     * @return 日期时间字符串 格式为yyyy-MM-dd HH:mm:ss
     */
    public static final String formatNowDateTime(){
        return formatDateTime(System.currentTimeMillis());
    }

    /**
     * 将带有星号和问号的字符串转换成sql中的匹配字符
     * 
     * @param s
     *            要转换的字符串
     * @return 转换后的字串
     */
    static final public String convertLikeWords(String s){
        if (s == null || s.length() == 0) {
            return null;
        }
        s = s.replaceAll("%", "\\\\%");
        s = s.replaceAll("_", "\\\\_");
        s = s.replace('*', '%');
        s = s.replace('?', '_');
        return s;
    }

    /**
     * 将带有星号和问号的字符串转换成sql中的匹配字符 _,%前面加转义字符 $ ,以前用 \ 在mysql中有语法错误；
     * 
     * @param s
     *            要转换的字符串
     * @return 转换后的字串
     */
    static final public String convertLikeWords(String s, final char escapechar){
        if (s == null || s.length() == 0) {
            return null;
        }
        s = s.replaceAll("%", "\\" + escapechar + "%");
        s = s.replaceAll("_", "\\" + escapechar + "_");
        s = s.replace('*', '%');
        s = s.replace('?', '_');
        return s;
    }

    /**
     * 将带有星号和问号的字符串转换成转换正则表达式 "*"匹配0或多个任意字符，"?"匹配单个任意字符。而","号相当于或运算， 例如：w*d.do?
     * 可以匹配word.doc, world.dot等等。 *.jsp,*.java 可以匹配以jsp或java为扩展名的文件。
     * 
     * @param wildcard
     *            通配符字串
     * @return 正则表达式字串
     */
    static final public String convert2Regex(final String wildcard){
        if (wildcard == null || wildcard.length() == 0) {
            return null;
        }
        StringBuffer s = new StringBuffer(wildcard.length() + 5);
        String[] temp = wildcard.split(",");
        for (int x = 0; x < temp.length; x++) {
            String w = temp[x];
            if (x > 0) s.append("|");
            s.append('^');
            s.append(wildcard2Regex(w));
            s.append('$');
        }
        return (s.toString());
    }

    /**
     * 将输入的字符串转换为Pattern类，如果输入的字符串是形如js的正则表达式语法，如：/xxxx/mi，那么提取其中的xxxx作为正则表达式
     * 否则使用{@link #wildcard2Regex(String)}进行转换 没有考虑/g
     * 
     * @param text
     *            需要进行转换为Pattern类的字串
     * @return Pattern类
     */
    static final public Pattern wildcard2RegexPattern(final String text){
        if (text == null || text.length() == 0) {
            return null;
        }
        if (text.length() >= 2 && text.charAt(0) == '/') {
            int i = text.lastIndexOf('/');
            if (i >= 1) {
                String attributes = text.substring(i + 1);
                int flags = 0;
                if (attributes.indexOf('i') >= 0)
                    flags = flags | Pattern.CASE_INSENSITIVE;
                if (attributes.indexOf('m') >= 0)
                    flags = flags | Pattern.MULTILINE | Pattern.DOTALL;
                return Pattern.compile(text.substring(1, i), flags);
            }
        }

        return Pattern.compile(wildcard2Regex(text), Pattern.CASE_INSENSITIVE
                | Pattern.DOTALL | Pattern.MULTILINE);
    }

    /**
     * 将带有星号和问号的字符串转换成转换正则表达式 "*"匹配0或多个任意字符，"?"匹配单个任意字符。
     * 
     * @param wildcard
     *            需要转换的字符串， 如果传的参数本身就是正则表达式，它还是会转一道，所以要人为的控制传参内容
     * @return 正则表达式，或原串（不带* ?的字符串返回原串）
     */
    static final public String wildcard2Regex(final String wildcard){
        StringBuffer s = new StringBuffer((int) (wildcard.length() * 1.3));
        String w = wildcard;
        for (int i = 0, is = w.length(); i < is; i++) {
            char c = w.charAt(i);
            switch (c) {
                case '*':
                    s.append(".*");
                    break;
                case '?':
                    s.append(".");
                    break;
                case '(':
                case ')':
                case '[':
                case ']':
                case '$':
                case '^':
                case '.':
                case '{':
                case '}':
                case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        return s.toString();
    }

    /**
     * 格式标识符 去掉中间的不合法字符，只留下字母和数字,下划线。 字母打头
     * 
     * @param id
     *            要格式标识符的字串
     * @param maxlen
     *            返回字串限定的最大长度
     * @return 格式后的字串(传参null或""，返回unknown)
     */
    public static String formatIdentifier(final String id, final int maxlen){
        StringBuffer buf = new StringBuffer(id != null ? id : "");
        int i = 0;
        while (i < buf.length()) {
            char c = buf.charAt(i);
            if (StrYn.isABC_xyz(c) || (StrYn.isDigit(c) && i > 0)
                    || (c == '_' && i > 0)) {
                i++;
            } else {
                buf.deleteCharAt(i);
            }
        }

        if (buf.length() == 0) {
            return "unknown";
        }

        if (buf.length() > maxlen) {
            buf.setLength(maxlen);
        }
        return buf.toString();
    }

    /**
     * // TODO 这个方法，AND和括号之间最好空格隔开，sql语句末差个反括号，对于返回值的处理，(只要value不为空，就会返回true，
     * 个人认为没有多大意义) 生成sql语言的where语句 该方法根据参数生成sql的where语句，故所传的参数值内容要进行人为的把控，
     * 倘若传入的字符串是很随便的字符串，生成的where语句没有任何意义，且若作为sql语句进行数据库操作，肯定会出问题
     * 
     * @param sb
     *            存放生成的sql语句
     * @param fld
     *            表的属性(列名)
     * @param op
     *            操作，一般为 = < > <= >= 本函数没有考虑between，in等情况
     * @param value
     *            值
     * @return 成功生成sql语句返回true，反之false
     */
    public static final boolean makeSql(final StringBuffer sb,
            final String fld, final String op, final Boolean value){
        if (value == null) {
            return false;
        }
        if ((sb.length() > 0) && (sb.charAt(sb.length() - 1) == ')')) {
            sb.append("AND");
        }
        sb.append('(');
        sb.append(fld);
        sb.append(op);
        sb.append(value.booleanValue() ? "true" : "false");
        return true;
    }

    /**
     * // TODO 这个方法，AND和括号之间最好空格隔开，对于返回值的处理，(只要value不为空，就会返回true，个人认为没有多大意义)
     * 生成sql语言的where语句 该方法根据参数生成sql的where语句，故所传的参数值内容要进行人为的把控，
     * 倘若传入的字符串是很随便的字符串，生成的where语句没有任何意义，且若作为sql语句进行数据库操作，肯定会出问题
     * 
     * @param sb
     *            存放生成的sql语句
     * @param fld
     *            表的属性(列名)
     * @param op
     *            操作，一般为 = < > <= >= 本函数没有考虑between，in等情况
     * @param value
     *            值
     * @return 成功生成sql语句返回true，反之false
     */
    public static final boolean makeSql(final StringBuffer sb,
            final String fld, final String op, final String value){

        if (value == null) {
            return false;
        }
        if ((sb.length() > 0) && (sb.charAt(sb.length() - 1) == ')')) {
            sb.append("AND");
        }
        sb.append('(');
        sb.append(fld);
        sb.append(op);
        sb.append("'");
        sb.append(value);
        sb.append("')");
        return true;
    }

    /**
     * 将字符串翻译成整形数组
     * 
     * @param s
     *            由整形数及分隔符拼接的字符串
     * @param separator
     *            分隔符
     * @return 整形数组
     */
    public static final int[] str2intarray_old(final String s,
            final String separator){
        if (s == null || s.length() == 0) {
            return null;
        }
        String[] ss = s.split(separator);
        int[] result = new int[ss.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Integer.parseInt(ss[i]);
        }
        return result;
    }

    /**
     * 将字符串s按照separator进行分割成字符串数组，再将 数组中的string 参数分析为有符号的十进制 byte<br/>
     * 目前而言，该方法对传参格式要求很严， 如果分割后的字符串数组中存在非数字字符，字符串中数字超出byte范围的，会达不到想要的效果<br/>
     * 传参如：82,77,36,89, 返回数组为82,77,36,89<br/>
     * 传参如：,82,77,36,89 返回值为0,82,77,36,89
     * 
     * @param s
     *            要翻译成字节数组的字符串
     * @param separator
     *            分割串 不要以-作为分隔符
     * @return 字节数组
     */
    public static final byte[] str2Bytearray(final String s,
            final String separator){
        if (s == null || s.length() == 0) {
            return null;
        }
        String[] ss = s.split(separator);
        byte[] result = new byte[ss.length];
        for (int i = 0; i < result.length; i++) {
            try {
                result[i] = Byte.parseByte(ss[i]);
            } catch (Exception e) {
                System.err.println(s);
                e.printStackTrace();
                // TODO 不加调试开关的打印请做处理
            }
        }
        return result;
    }

    /**
     * 获得字符串的在byte级的长度一个汉字算两个长度，和字符串的length方法不同，length方法是返回unicode字符的长度一个汉字算一个长度
     * 。
     * 
     * @param s
     *            要测试长度的字符串
     * @return 字符串的长度(字符大于0x7F的均按2个长度算)
     */
    public static final int strByteLength(final String s){
        return strByteLength(s, 2);
    }

    /**
     * 20090810 返回 指定unicode字符byte长度的 字符串的byte长度；
     * 
     * @param s
     *            要测试长度的字符串
     * @param blen
     *            指定unicode字符的byte长度；
     * @return 字符串的长度
     */
    public static final int strByteLength(final String s, final int blen){
        if (s == null || s.length() == 0) return 0;
        int r = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 0x7F)
                r += blen;
            else
                r++;
        }
        return r;
    }

    /**
     * 将字符串s按照separator进行分割成字符串数组，再将 数组中的string 参数分析为有符号的十进制 short<br/>
     * 目前而言，该方法对传参格式要求很严， 如果分割后的字符串数组中存在非数字字符，字符串中数字超出short范围的，会达不到想要的效果<br/>
     * 传参如：82,77,36,89, 返回数组为82,77,36,89<br/>
     * 传参如：,82,77,36,89 返回值为0,82,77,36,89
     * 
     * @param s
     *            要翻译成字节数组的字符串
     * @param separator
     *            分割串
     * @return short数组
     */
    public static final short[] str2Shortarray(final String s,
            final String separator){
        if (s == null || s.length() == 0) {
            return null;
        }
        String[] ss = s.split(separator);
        short[] result = new short[ss.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Short.parseShort(ss[i]);
        }
        return result;
    }

    /**
     * 查找字符在字符串中出现的次数
     * 
     * @param s
     *            字符串
     * @param c
     *            要查找的字符
     * @return 没找到返回0，找到了返回出现的次数
     */
    public static final int charCount(final String s, final char c){
        int r = 0;
        for (int i = 0, len = s != null ? s.length() : 0; i < len; i++) {
            if (c == s.charAt(i)) {
                r++;
            }
        }
        return r;
    }

    /**
     * 将字符串翻译成整形数组
     * 
     * @param s
     *            由整形数及分隔符拼接的字符串
     * @param separator
     *            分隔符
     * @return 整形数组
     */
    public static final int[] str2intarray(final String s, final char separator){
        if (s == null || s.length() == 0) {
            return null;
        }
        int sl = s.length(), f = 0, k = -1, len = charCount(s, separator) + 1;
        if (len == 0) {
            len++;
        } else {
            if (s.charAt(sl - 1) == separator) {
                len--;
            }
        }
        int[] result = new int[len];
        int t = s.indexOf(separator, f);
        while (t != -1) {
            if (++k >= len) {
                len += len / 3 + 1;
                int[] temp = new int[len];
                System.arraycopy(result, 0, temp, 0, result.length);
                result = temp;
            }
            result[k] = str2int(s, f, t, 0);
            f = t + 1;
            t = s.indexOf(separator, f);
        }
        if (sl - f > 0) {
            if (++k >= len) {
                len++;
                int[] temp = new int[len];
                System.arraycopy(result, 0, temp, 0, result.length);
                result = temp;
            }
            result[k] = str2int(s, f, sl, 0);
        }
        if (len > ++k) {
            int[] temp = new int[k];
            System.arraycopy(result, 0, temp, 0, k);
            result = temp;
        }
        return result;
    }

    /**
     * 将字符串翻译成整形数组 如果传参，字符串除分隔符外还包含其他非数字字符，该段返回0 如：-90,80,0x134,88 ，分割符为,
     * ，返回{-90,80,0,88}
     * 
     * @param s
     *            由整形数及分隔符拼接的字符串
     * @param separator
     *            分隔符
     * @return 整形数组
     */
    public static final int[] str2intarray(final String s,
            final String separator){
        if (s == null || s.length() == 0) {
            return null;
        }
        int sepl = separator.length();
        if (sepl == 1) {
            return str2intarray(s, separator.charAt(0));
        }
        int sl = s.length(), f = 0, k = -1, len = s.length() / 5 + 2;
        int[] result = new int[len];
        int t = s.indexOf(separator, f);
        while (t != -1) {
            if (++k >= len) {
                len += len / 3;
                int[] temp = new int[len];
                System.arraycopy(result, 0, temp, 0, result.length);
                result = temp;
            }
            result[k] = str2int(s, f, t, 0);
            f = t + sepl;
            t = s.indexOf(separator, f);
        }
        if (sl - f > 0) {
            if (++k >= len) {
                len++;
                int[] temp = new int[len];
                System.arraycopy(result, 0, temp, 0, result.length);
                result = temp;
            }
            result[k] = str2int(s, f, sl, 0);
        }
        if (len > ++k) {
            int[] temp = new int[k];
            System.arraycopy(result, 0, temp, 0, k);
            result = temp;
        }
        return result;
    }

    private final static HashMap contents = new HashMap();

    private static void loadContents(InputStream is) throws IOException{
        BufferedReader bf = new BufferedReader(new InputStreamReader(is));
        String l = bf.readLine();
        while (l != null) {
            int index = l.indexOf(':');
            contents.put(l.substring(0, index), l.substring(index + 1));
            l = bf.readLine();
        }
    }

    static {
        try {
            InputStream is = StrYn.class
                    .getResourceAsStream("contents.properties");
            try {
                loadContents(is);
            } finally {
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断文件的类型，如二进制文件返回application/octet-stream
     * 如果文件名为null，也会返回application/octet-stream
     * 
     * @param ext
     *            要判断的文件名
     * @return 文件类型
     */
    public static final String getContentType(String ext){
        if (!StrYn.isNull(ext)) {
            int i = ext.lastIndexOf('.');
            if (i >= 0) {
                ext = ext.substring(i + 1);
            }
            /**
             * BUG:在BI资源管理器里上传后缀名包含大写字母(例如.DOC)的文件，系统会将这种资源的文件类型判断为application/
             * octet-stream。
             * 这是由于contents里面存放的都是全小写的后缀名导致的。为了避免这样的情况，在判断文件类型之前，将后缀名转化为小写。
             * 2010-1-20 by menghsh
             */
            ext = ext.toLowerCase();
        }
        String c = (String) contents.get(ext);
        if (c == null) c = "application/octet-stream";
        return c;
    }

    /**
     * 获得指定类型的content-type,如果编码不为空,则带上编码信息</br>
     * 如果文件名为null，也会返回application/octet-stream，不存在的文件应该没有编码信息，故使用时注意传参正确性
     * 
     * @param ext
     *            要判断的文件名
     * @param encoding
     *            编码信息如果错误传参不存在的编码信息，不会报错，所以注意控制正确的传参
     * @return 文件类型及编码信息
     */
    public static final String getContentType(final String ext,
            final String encoding){
        String ct = getContentType(ext);
        if (StrYn.isNull(encoding)) return ct;
        return ct + "; charset=" + encoding;
    }

    public final static String[] HEX_0_FF = {"00", "01", "02", "03", "04",
            "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F",
            "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1A",
            "1B", "1C", "1D", "1E", "1F", "20", "21", "22", "23", "24", "25",
            "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F", "30",
            "31", "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B",
            "3C", "3D", "3E", "3F", "40", "41", "42", "43", "44", "45", "46",
            "47", "48", "49", "4A", "4B", "4C", "4D", "4E", "4F", "50", "51",
            "52", "53", "54", "55", "56", "57", "58", "59", "5A", "5B", "5C",
            "5D", "5E", "5F", "60", "61", "62", "63", "64", "65", "66", "67",
            "68", "69", "6A", "6B", "6C", "6D", "6E", "6F", "70", "71", "72",
            "73", "74", "75", "76", "77", "78", "79", "7A", "7B", "7C", "7D",
            "7E", "7F", "80", "81", "82", "83", "84", "85", "86", "87", "88",
            "89", "8A", "8B", "8C", "8D", "8E", "8F", "90", "91", "92", "93",
            "94", "95", "96", "97", "98", "99", "9A", "9B", "9C", "9D", "9E",
            "9F", "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9",
            "AA", "AB", "AC", "AD", "AE", "AF", "B0", "B1", "B2", "B3", "B4",
            "B5", "B6", "B7", "B8", "B9", "BA", "BB", "BC", "BD", "BE", "BF",
            "C0", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "CA",
            "CB", "CC", "CD", "CE", "CF", "D0", "D1", "D2", "D3", "D4", "D5",
            "D6", "D7", "D8", "D9", "DA", "DB", "DC", "DD", "DE", "DF", "E0",
            "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9", "EA", "EB",
            "EC", "ED", "EE", "EF", "F0", "F1", "F2", "F3", "F4", "F5", "F6",
            "F7", "F8", "F9", "FA", "FB", "FC", "FD", "FE", "FF"};

    private final static byte[] val = {0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x00, 0x01,
            0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
            0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F};

    private static final String escapeStr = "+-@*/._";

    /**
     * 判断是否需要进行Escape 不会对 ASCII 字母和数字进行编码，也不会对下面这些 ASCII 标点符号进行编码： * @ - _ + . /
     * 其他所有的字符都会被转义序列替换。
     * 
     * @param c
     *            要判断的字符
     * @return 如果需要Escape，返回False，反之True
     */
    public static final boolean needEscape(final char c){
        return c < 0x7F
                && c > 0x20
                && ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                        || (c >= '0' && c <= '9') || (escapeStr.indexOf(c) >= 0));
    }

    /**
     * 类似客户端javascript代码中的escape函数
     * 
     * @param s
     *            要被转义或编码的字符串。
     * @return 转义的结果
     * @see StmFunc.escape
     */
    public static String escape(final String s){
        if (s == null || s.length() == 0) {
            return s;
        }
        int len = s.length();
        StringBuffer sbuf = new StringBuffer(len * 4 / 3);
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            if (needEscape(ch))
                sbuf.append((char) ch);
            else if (ch <= 0x007F) { // other ASCII : map to %XX
                sbuf.append('%');
                sbuf.append(HEX_0_FF[ch]);
            } else { // unicode : map to %uXXXX
                sbuf.append('%');
                sbuf.append('u');
                sbuf.append(HEX_0_FF[(ch >>> 8)]);
                sbuf.append(HEX_0_FF[(0x00FF & ch)]);
            }
        }
        return sbuf.toString();
    }

    /**
     * 对字符串进行解码
     * 
     * @param s
     *            需要解密的字符串
     * @return 解码后的结果
     */
    public static String unescape(final String s){
        if (s == null || s.length() == 0) {
            return s;
        }
        StringBuffer sbuf = new StringBuffer(s.length());
        int i = 0;
        int len = s.length();
        while (i < len) {
            char ch = s.charAt(i);
            if (needEscape(ch))
                sbuf.append((char) ch);
            else if (ch == '%') {
                int cint = 0;
                if ('u' != s.charAt(i + 1)) { // %XX : map to ascii(XX)
                    cint = (cint << 4) | val[s.charAt(i + 1)];
                    cint = (cint << 4) | val[s.charAt(i + 2)];
                    i += 2;
                } else { // %uXXXX : map to unicode(XXXX)
                    cint = (cint << 4) | val[s.charAt(i + 2)];
                    cint = (cint << 4) | val[s.charAt(i + 3)];
                    cint = (cint << 4) | val[s.charAt(i + 4)];
                    cint = (cint << 4) | val[s.charAt(i + 5)];
                    i += 5;
                }
                sbuf.append((char) cint);
            } else
                sbuf.append((char) ch);
            i++;
        }
        return sbuf.toString();
    }

    public static void main(String[] args) throws Exception{
        // System.out.println(getPyString("中文"));
        // testStr2Text();
        // String s = "你好好看看“戏”字";
        /*
         * String s = "123456789012345"; byte[] bb = s.getBytes();
         * System.out.println(s.length()+"length of byte[]:"+bb.length); int len
         * = 20; System.out.println(encryptPassword(s,len));
         * System.out.println(decryptPassword(encryptPassword(s,len)));
         */
        // String str = "asdf\\sd\\f";
        // System.out.println(str.replace('\\',java.io.File.separatorChar));
        /*
         * String s = "10100000"; String s1=IDC(s);
         * System.out.println("idc char of "+s+":"+s1); if (isIDC(s+"9"))
         * System.out.println("is idc code!"); else System.out.println("???");
         */
        /*
         * char[] cc=s.toCharArray(); byte[] b= s.getBytes();
         * System.out.println(
         * s+"s length:"+s.length()+" char[] length:"+cc.length+
         * " byte[] length:"+b.length); /* byte[] bb= new
         * String("12345").getBytes(); s = bytesToHexString(bb);
         * System.out.println(s); byte[] rr = hexStringToBytes(s);
         * System.out.println("hex to bytes:"+new String (rr)); try { throw new
         * Exception("asdfasdf"); } catch (Exception ex) {
         * System.out.println(exception2str(ex)); } /*long l =
         * System.currentTimeMillis(); for (int i=1; i<1000; i++){
         * quotedStr("\"sdf\"asdf\"dfgsdf\"",'\"'); }
         * System.out.println(System.currentTimeMillis()-l);
         * System.out.println(quotedStr("\"",'\"'));
         * System.out.println(quotedStr("",'\"'));
         * System.out.println(quotedStr(null,'\"'));
         */

        /*
         * System.out.println(extractQuotedStr(quotedStr("\"",'\"'),'\"'));
         * System.out.println(extractQuotedStr(quotedStr("",'\"'),'\"'));
         * System.
         * out.println(extractQuotedStr(quotdStr("\"sdfsd\"f",'\"'),'\"'));
         * System.out.println(extractQuotedStr("\"asd\"f",'\"'));
         */

        // System.out.println(getPyString("会统月text你Str("));
        /*
         * String s = "\"1\",\"2\"\"fgd,\",3,4,\"df,g\",sfg,"; int[] arr = new
         * int[1]; arr[0] = 0; while (arr[0] != -1) { String v = getValue(s,
         * arr[0], '\"', ',', arr); System.out.println("+" + v); }
         */
        // format("",new String[]{"",""});
        // System.out.println("s.df.tx.t".matches(convert2Regex("*.tx.t")));

        // double d = 0.0000001/3;
        // Double d2 = new Double(d);
        // System.out.println(d2);
        // System.out.println(d+"");
        //
        // DecimalFormat df = new DecimalFormat("#.#########################");
        // System.out.println(df.format(d));
        //
        // System.out.println(double2str(d, 0, 10, false));
        // System.out.println(double2str(0.2, 0, 10, false));
        // int i =1;
        // String s = i+"";
        // System.out.print(s);
        //
        // s = String.valueOf(i);

        // char pdyCode = getPdyCode("PDY60031");
        // System.out.println(pdyCode);
        // System.out.println(double2str(1071304931489.8049d, 2, 2, true));
        System.out.println("1071304931489.8049:       ->"
                + formatDouble(1071304931489.8049, 2, false));
        // System.out.println(formatDouble(round(1071304931489.8049d,2),2,true));
        System.out.println("999999995555555555555555: ->"
                + formatDouble(999999995555555555555555d, 2, false));
        System.out.println("457992222433786658323.89: ->"
                + new BigDecimal(457992222433786658323.89d).toString());
        System.out.println("49388999999999999889.13:  ->"
                + new BigDecimal(49388999999999999889.13d).toString());
        System.out.println(double2str(13.3000d, 0, 4, false));
    }

    /**
     * trim字符串中所有的空白字符 空白字符：包括空格、制表符、换页符等等。等价于 [ \f\n\r\t\v]。
     * 
     * @param str
     *            要处理的字串
     * @return 处理后的字串，传参null则返回null
     */
    public static final String trimAll(final String str){
        return str == null ? null : str.replaceAll("\\s", "");
    }

    /**
     * 将str中，open和close之间的字符串截取出来，
     * 
     * @param str
     *            要截取的字符串
     * @param open
     *            从第一次出现open后的地方开始截取
     * @param close
     *            截取到close第一次出现的地方前
     * @return 如果有1个参数为null返回null，若open或close不存在于str，也返回null，
     *         close串出现在第一次出现的open前，返回null。否则，返回open和close之间的字符串
     */
    public static final String substringBetween(final String str,
            final String open, final String close){
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    /**
     * 确保字符串以串start开头，如果r已经是start开头，则不改变
     * 
     * @param r
     *            原串
     * @param start
     *            串开始的内容
     * @return 以串start开头的r串
     */
    public static String ensureStartWith(final String r, final String start){
        if (r == null || r.length() == 0) {
            return start + r;
        }
        if (!r.startsWith(start)) return start + r;
        return r;
    }

    /**
     * 确保字符串以串end结尾，如果r已经是end结尾或end为null，则不改变
     * 
     * @param r
     *            原串
     * @param end
     *            串结尾的内容
     * @return 以串end结尾的r串
     */
    public static String ensureEndWith(final String r, final String end){
        if (r == null || r.length() == 0) {
            return r + end;
        }
        if (end != null && !r.endsWith(end)) return r + end;
        return r;
    }

    /**
     * 确保字符串以串start开头end结尾， 如果r已经是start开头或start为null，则不在开头添加start内容
     * 如果r已经是end结尾或end为null，则不在结尾添加end内容
     * 
     * @param r
     *            原串
     * @param start
     *            串开始的内容
     * @param end
     *            串结尾的内容
     * @return 以串start开头，串end结尾，中间内容为r串的字符串
     */
    public final static String ensureStartEndWith(final String s,
            final String start, final String end){
        if (s == null || s.length() == 0) {
            return start + s + end;
        }
        String r = s;
        if (start != null && !s.startsWith(start)) r = start + r;
        if (end != null && !s.endsWith(end)) r = r + end;
        return r;
    }

    /**
     * 确保字符串不以串start开头且不以end结尾，
     * 
     * @param r
     *            原串
     * @param start
     *            开头要截取的内容
     * @param end
     *            结尾要截取的内容
     * @return 不以串start开头且不以end结尾的串
     */
    public final static String ensureNotStartEndWith(final String s,
            final String start, final String end){
        if (s == null || s.length() == 0) {
            return s;
        }
        String r = s;
        if (start != null && r.startsWith(start))
            r = r.substring(start.length());
        if (end != null && r.endsWith(end))
            r = r.substring(0, r.length() - end.length());
        return r;
    }

    /**
     * 确保字符串不以串start开头且不以end结尾，
     * 
     * @param r
     *            原串
     * @param start
     *            开头要截取的内容
     * @param end
     *            结尾要截取的内容
     * @return 不以字符start开头且不以字符end结尾的串
     */
    public final static String ensureNotStartEndWith(final String s,
            final char start, final char end){
        if (s == null || s.length() == 0) {
            return s;
        }
        String r = s;
        if (r.charAt(0) == start) r = r.substring(1);
        if (r.length() > 0 && r.charAt(r.length() - 1) == end)
            r = r.substring(0, r.length() - 1);
        return r;
    }

    /**
     * 判断r是否以start开头，是否以end结尾
     * 
     * @param r
     *            需要判断的字符串
     * @param start
     *            起始字符串
     * @param end
     *            结束字符串
     * @return 如果r即以start开头也以end结尾，返回true，否则false
     */
    public final static boolean startEndWith(final String r,
            final String start, final String end){
        if (r == null || r.length() == 0) {
            return false;
        }
        return r.startsWith(start) && r.endsWith(end);
    }

    /**
     * 返回不以sep中的任何字符打头的字符串，如果sep为null，返回不以空白字符打头的字符串
     * 不同于delphi中的ensureNotStartWith
     * 
     * @param str
     *            要检查的字串
     * @param sep
     *            所有不能打头的字符存放在字串里
     * @return 不以sep中的任何字符打头的字符串
     */
    public static final String ensureNotStartWithChars(final String str,
            final String sep){
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        int start = 0;
        if (sep == null) {
            while ((start != strLen)
                    && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
        } else if (sep.length() == 0) {
            return str;
        } else {
            while ((start != strLen) && (sep.indexOf(str.charAt(start)) != -1)) {
                start++;
            }
        }
        return str.substring(start);
    }

    /**
     * 确保str不以start开头。
     * 
     * @param r
     *            原串
     * @param start
     *            要截去的开头
     * @return 没有start开头的r串
     */
    public static final String ensureNotStartWith(final String str,
            final String start){
        if (str == null || str.length() == 0 || start == null
                || start.length() == 0 || str.length() < start.length())
            return str;
        if (str.startsWith(start)) return str.substring(start.length());
        return str;
    }

    /**
     * 先保证字符串不以start1开头，在保证字符串不以start2开头
     * 
     * @param str
     *            要确保不以start1开头再不以start2开头的串
     * @param start1
     *            不以该串开头
     * @param start2
     *            确保字符串不以start1开头后，不以start2开头
     * @return
     */
    public static final String ensureNotStartWith(final String str,
            final String start1, final String start2){
        return ensureNotStartWith(ensureNotStartWith(str, start1), start2);
    }

    /**
     * 返回不以sep中的任何字符结尾的字符串，如果sep为null，返回不以空白字符结尾的字符串 不同于delphi中的ensureNotEndWith
     * 
     * @param str
     *            要检查的字串
     * @param sep
     *            所有不能结尾的字符存放在字串里
     * @return 不以sep中的任何字符结尾的字符串
     */
    public static final String ensureNotEndWith(final String str,
            final String sep){
        int end;
        if (str == null || (end = str.length()) == 0) {
            return str;
        }

        if (sep == null) {
            while ((end != 0) && Character.isWhitespace(str.charAt(end - 1))) {
                end--;
            }
        } else if (sep.length() == 0) {
            return str;
        } else {
            while ((end != 0) && (sep.indexOf(str.charAt(end - 1)) != -1)) {
                end--;
            }
        }
        return str.substring(0, end);
    }

    /**
     * 将RGB颜色转为windows颜色,例如#ff0000 -> red, #00ff00->green, #0000ff->blue
     * 如果转换不成功，返回默认颜色 如果传参字符串长度超过了7位，但前7位是正常的rgb颜色，返回前7位对应的windows颜色，后面的字符串忽略掉
     * 
     * @param cv
     *            rgb颜色字符串
     * @param defaultColor
     *            默认颜色
     * @return windows颜色
     */
    public static final Color str2Color(final String cv,
            final Color defaultColor){
        if (cv == null || !cv.startsWith("#") || cv.length() < 2) {
            return defaultColor;
        }
        int r = Integer.parseInt(cv.substring(1, 3), 16);
        int g = Integer.parseInt(cv.substring(3, 5), 16);
        int b = Integer.parseInt(cv.substring(5, 7), 16);
        return new Color(r, g, b);
    }

    /**
     * 将windows颜色转为rgb颜色，如red->#ff0000
     * 
     * @param color
     *            windows颜色
     * @return rgb颜色字串
     */
    public static final String color2Str(final Color color){
        int rgb = color.getRed() * 0x10000 + color.getGreen() * 0x100
                + color.getBlue();
        String hex = Integer.toHexString(rgb);
        String str0 = strOfchar('0', 6 - hex.length());
        return '#' + str0 + hex;
    }

    /**
     * 分析表元名，返回行列号。（从0开始计数） 如果不是合法的表元名，则行列值返回-1
     * 
     * @param name
     *            表元名
     * @return 返回int[2]数组，存放行列号
     */
    public static final int[] getRowColFromName(final String name){
        return getRowColFromName(name, new int[2]);
    }

    /**
     * 分析表元名，返回行列号。（从0开始计数） 如果不是合法的表元名，则返回-1(r[0],r[1]为-1)
     * 
     * @param name
     *            表元名
     * @param r
     *            用来存放行列号的int数组
     * @return r[0]为行号，r[1]为列号，其余数不变
     */
    public static final int[] getRowColFromName(final String name, final int[] r){
        int numIndex = 0;
        int col = -1;
        char c = name.charAt(numIndex++);
        int len = name.length();
        while (((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))
                && numIndex < len) {
            /**
             * 20090320 表元名忽略大小写。 20090323 之前将表元名转为大写，效率不高；这里通过字符判断来兼容大小写。
             */
            col = (col + 1) * 26 + (c >= 'a' ? c - 'a' : c - 'A');// 这样就可以兼容大小写了
            c = name.charAt(numIndex++);
        }

        int row = numIndex - 1 < len ? str2int(name, numIndex - 1,
                name.length(), 0/* 后面会再减1的 */) - 1 : -1;
        r[0] = row;
        r[1] = col;
        return r;
    }

    /**
     * 分析表元名，返回行号。（从0开始计数） 如果不是合法的表元名，则返回-1
     * 
     * @param name
     *            表元名
     * @return 表元名对应的行号
     */
    public static final int getRowFromName(final String name){
        return getRowColFromName(name)[0];
    }

    /**
     * 分析表元名，返回列号。（从0开始计数） 如果不是合法的表元名，则返回-1
     * 
     * @param name
     *            表元名
     * @return 表元名对应的列号
     */
    public static final int getColFromName(final String name){
        return getRowColFromName(name)[1];
    }

    /**
     * 使用utf-8编码机制将字符串转换为 application/x-www-form-urlencoded 格式。
     * 
     * @param s
     *            要转码的字符串
     * @return application/x-www-form-urlencoded 格式的字符串
     */
    public static final String encodeURIComponent(final String s){
        if (s == null || s.length() == 0) {
            return s;
        }
        try {
            return URLEncoder.encode(s, "utf-8");// StrFunc.UTF8
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 判断字串是否是数字类型字串，包括int,double 如果为null或长度为0,返回false
     * 
     * @param s
     *            需要判断的字符串
     * @return 是数字类型字符串返回true，反之false
     */
    public static final boolean isNumber(final String s){
        if (s == null || s.length() == 0) {
            return false;
        }
        /*
         * char c = s.charAt(0); boolean fu = c == '-'; if (fu && s.length() ==
         * 1) { return false; } int start = fu ? 1 : 0; for (int i = start; i <
         * s.length(); i++) { c = s.charAt(i); if (c > '9' || c < '0') { return
         * false; } } return true;
         */
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException ne) {
            return false;
        }
    }

    /**
     * 将对象v转换为js语法的常量形式，支持null、字符串、数字、布尔，
     * 如果传入的是一个StringBuffer，那么不把它当字符串处理，而是直接输出它的内容，不加引号，这样可以做到输出一段json字符串
     * 如果对象为String类型，返回值两端有"
     * 
     * @param v
     *            要转成js语法形式的对象
     * @return js语法常量的形式
     */
    public final static String formatJsValue(final Object v){
        if (v == null) {
            return "null";
        } else if (v instanceof String) {
            return StrYn.formatJsStr(v.toString(), true);
        } else {// Number和Boolean都可以这样
            return v.toString();
        }
        // set一个StringBuffer可以让输出json时没有引号,当v是StringBuffer时不要对它加引号，也不要formatJsStr
    }

    /**
     * 将一个服务器端的字符串，格式化成js端的常量，如：ab"c 格式化成ab\"c ，不给字符串两端加"
     * 
     * @param s
     *            服务器端的字符串
     * @return js端的常量
     */
    public final static String formatJsStr(final String s){
        return formatJsStr(s, false);
    }

    /**
     * 将一个服务器端的字符串，格式化成js端的常量，如：ab"c 格式化成ab\"c
     * 
     * @param s
     *            服务器端的字符串
     * @param quateit
     *            是否给字符串两端加上"
     * @return js端的常量
     */
    public final static String formatJsStr(final String s, final boolean quateit){
        if (s == null) return s;
        if (s.length() == 0) return quateit ? "\"\"" : s;
        char c;
        for (int i = 0, len = s.length(); i < len; i++) {
            c = s.charAt(i);
            if (c < ' ' || c == '\'' || c == '\\' || c == '"' || c == '/') {// 在ie中如果字符串常量中有</script>那么会出异常，所以把/也转义一下
                return _formatJsStr(s, quateit);
            }
        }
        return quateit ? '"' + s + '"' : s;
    }

    /**
     * 此函数的主要代码都copy自org.json.JSONObject类的quote函数
     * 
     * Produce a string in double quotes with backslash sequences in all the
     * right places. A backslash will be inserted within </, allowing JSON text
     * to be delivered in HTML. In JSON text, a string cannot contain a control
     * character or an unescaped quote or backslash.
     * 
     * @param string
     *            A String
     * @return A String correctly formatted for insertion in a JSON text.
     */
    private static String _formatJsStr(final String s, final boolean quateit){

        char b;
        char c = 0;
        int i;
        int len = s.length();
        StringBuffer sb = new StringBuffer(len + 8);
        String t;

        if (quateit) sb.append('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = s.charAt(i);
            switch (c) {
                case '\\':
                case '\'':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    if (b == '<') {
                        sb.append('\\');
                    }
                    sb.append(c);
                    break;
                case '\b':// Backspace
                    sb.append("\\b");
                    break;
                case '\t':// Tab
                    sb.append("\\t");
                    break;
                case '\n':// Line Feed
                    sb.append("\\n");
                    break;
                case '\f':// Form Feed
                    sb.append("\\f");
                    break;
                case '\r':// Carriage Return
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
                            || (c >= '\u2000' && c < '\u2100')) {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        if (quateit) sb.append('"');

        return sb.toString();
    }

    /**
     * 在导出报表为excel时，如果导出的文件名中有中文时，在客户端会是乱码，需用本函数转码
     * 
     * @param s
     *            要进行转码的字符串
     * @return 转码后的结果
     */
    public static String encodeISO8859_1(final String s){
        if (s == null) return s;
        try {
            return new String(s.getBytes("gb2312"), "iso8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return s;
        }
    }

    /**
     * 当用户下载一个文件时,浏览器对文件名有限制 与formatFileName方法不同的是这个地方会进行解码操作，以防中文乱码
     * 
     * @param s
     *            要处理的文件名
     * @return 处理后的字符串
     */
    public static String formatDownloadFileName(String s){
        if (s == null) return s;
        s = s.replace('—', '-');
        s = s.replace('/', '-');
        s = s.replace('\\', '-');
        s = s.replace('/', '-');

        /**
         * 20090512 用IE6导出报表时，文件名中含冒号会导致下载失败。IE7好像会自动处理冒号。 ISSUE:BIDEV-475
         * ExportReportAction中downloadfile方法在处理标题中包含英文冒号的报表导出时，导出的文件名不正确且无后缀名
         */
        s = s.replace(':', '-');
        return encodeISO8859_1(s);
    }

    /**
     * 当用户下载一个文件时,浏览器对文件名有限制 与formatDownloadFileName方法不同的是这个地方不会进行解码操作
     * 
     * @param s
     *            要处理的文件名
     * @return 处理后的字符串
     */
    public static String formatFileName(String s){
        if (s == null) return s;
        s = s.replace('—', '-');
        s = s.replace('/', '-');
        s = s.replace('\\', '-');
        s = s.replace('/', '-');
        s = s.replace(':', '-');

        return s;
    }

    /**
     * 返回小数点位数
     * 
     * @param strnum
     *            数字字符串 比如"1.23"
     * @return 最后一个小数点后面的字符串长度,没有小数点返回0
     */
    public static int getDecLen(final String strnum){
        if (strnum == null) return 0;
        int i = strnum.lastIndexOf('.');
        if (i == -1) return 0;
        return strnum.length() - i - 1;
    }

    /**
     * 如果s中包含cs中的任意一个字符，返回该字符的位置，从0开始查找,如果没有找到，返回-1
     * 
     * @param s
     *            被查找的串
     * @param cs
     *            要查找的字符所存放的位置
     * @return 找到了返回找到的位置，没找到返回-1
     */
    public static int indexOf(final String s, final char[] cs){
        if (isNull(s) || cs == null || cs.length == 0) {
            return -1;
        }
        int len = s.length();
        char c;
        for (int i = 0; i < len; i++) {
            c = s.charAt(i);
            for (int j = 0; j < cs.length; j++) {
                if (c == cs[j]) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 如果s中包含cs中的任意一个字符，返回该字符的位置，从最后一个字符开始查找,如果没有找到，返回-1
     * 
     * @param s
     *            被查的字符串
     * @param cs
     *            要查找的字符
     * @return 返回找到的位置，没找到返回-1
     */
    public static int lastIndexOf(final String s, final char[] cs){
        if (isNull(s) || cs == null || cs.length == 0) {
            return -1;
        }
        int len = s.length();
        char c;
        for (int i = len - 1; i >= 0; i--) {
            c = s.charAt(i);
            for (int j = 0; j < cs.length; j++) {
                if (c == cs[j]) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Get minimum of three values
     * 
     * @param a
     *            要判断最小数的其中一个数
     * @param b
     *            要判断最小数的其中一个数
     * @param c
     *            要判断最小数的其中一个数
     * @return 3个数中的最小数
     */
    private static final int Minimum(int a, int b, int c){
        int mi;

        mi = a;
        if (b < mi) {
            mi = b;
        }
        if (c < mi) {
            mi = c;
        }
        return mi;

    }

    /**
     * Compute Levenshtein distance 字符串相似度算法 </br>
     * 编辑距离就是用来计算从原串（s）转换到目标串(t)所需要的最少的插入，删除和替换的数目 </br> If s is "test" and t is
     * "test", then LD(s,t) = 0, because no transformations are needed. The
     * strings are already identical. </br> If s is "test" and t is "tent", then
     * LD(s,t) = 1, because one substitution (change "s" to "n") is sufficient
     * to transform s into t. </br>
     * 
     * @param s
     *            原串
     * @param t
     *            目标串
     * @return 编辑距离
     */
    public static final int LD(final String s, final String t){
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost

        // Step 1
        /*
         * Set n to be the length of s. Set m to be the length of t. If n = 0,
         * return m and exit. If m = 0, return n and exit. Construct a matrix
         * containing 0..m rows and 0..n columns.
         */
        n = s.length();
        m = t.length();
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n + 1][m + 1];

        // Step 2
        /*
         * Initialize the first row to 0..n. Initialize the first column to
         * 0..m.
         */

        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }

        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }

        // Step 3
        /*
         * Examine each character of s (i from 1 to n).
         */

        for (i = 1; i <= n; i++) {

            s_i = s.charAt(i - 1);

            // Step 4
            /*
             * Examine each character of t (j from 1 to m).
             */
            for (j = 1; j <= m; j++) {

                t_j = t.charAt(j - 1);

                // Step 5
                /*
                 * If s[i] equals t[j], the cost is 0. If s[i] doesn't equal
                 * t[j], the cost is 1.
                 */

                if (s_i == t_j) {
                    cost = 0;
                } else {
                    cost = 1;
                }

                // Step 6
                /*
                 * Set cell d[i,j] of the matrix equal to the minimum of: a. The
                 * cell immediately above plus 1: d[i-1,j] + 1. b. The cell
                 * immediately to the left plus 1: d[i,j-1] + 1. c. The cell
                 * diagonally above and to the left plus the cost: d[i-1,j-1] +
                 * cost.
                 */

                d[i][j] = Minimum(d[i - 1][j] + 1, d[i][j - 1] + 1,
                        d[i - 1][j - 1] + cost);

            }

        }

        // Step 7
        /*
         * After the iteration steps (3, 4, 5, 6) are complete, the distance is
         * found in cell d[n,m].
         */

        return d[n][m];

    }

    /**
     * LCS(Longest Common Subsequence) 就是求两个字符串最长公共子串的问题。 比如： String str1 = new
     * String("adbccadebbca"); String str2 = new String("edabccadece");
     * str1与str2的公共子串就是bccade.
     * 解法就是用一个矩阵来记录两个字符串中所有位置的两个字符之间的匹配情况，若是匹配则为1，否则为0。然后求出对角线最长的1序列
     * ，其对应的位置就是最长匹配子串的位置.
     * 下面是字符串21232523311324和字符串312123223445的匹配矩阵，前者为X方向的，后者为Y方向的
     * 。不难找到，红色部分是最长的匹配子串。通过查找位置我们得到最长的匹配子串为：21232 　　0 0 0 1 0 0 0 1 1 0 0 1 0 0
     * 0 　　0 1 0 0 0 0 0 0 0 1 1 0 0 0 0 　　1 0 1 0 1 0 1 0 0 0 0 0 1 0 0 　　0 1 0
     * 0 0 0 0 0 0 1 1 0 0 0 0 　　1 0 1 0 1 0 1 0 0 0 0 0 1 0 0 　　0 0 0 1 0 0 0 1
     * 1 0 0 1 0 0 0 　　1 0 1 0 1 0 1 0 0 0 0 0 1 0 0 　　1 0 1 0 1 0 1 0 0 0 0 0 1
     * 0 0 　　0 0 0 1 0 0 0 1 1 0 0 1 0 0 0 　　0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 　　0 0
     * 0 0 0 0 0 0 0 0 0 0 0 1 0 　　0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 　　0 0 0 0 0 0 0
     * 0 0 0 0 0 0 0 0
     * 但是在0和1的矩阵中找最长的1对角线序列又要花去一定的时间。通过改进矩阵的生成方式和设置标记变量，可以省去这部分时间。下面是新的矩阵生成方式：
     * 　　0 0 0 1 0 0 0 1 1 0 0 1 0 0 0 　　0 1 0 0 0 0 0 0 0 2 1 0 0 0 0 　　1 0 2 0
     * 1 0 1 0 0 0 0 0 1 0 0 　　0 2 0 0 0 0 0 0 0 1 1 0 0 0 0 　　1 0 3 0 1 0 1 0 0
     * 0 0 0 1 0 0 　　0 0 0 4 0 0 0 2 1 0 0 1 0 0 0 　　1 0 1 0 5 0 1 0 0 0 0 0 2 0
     * 0 　　1 0 1 0 1 0 1 0 0 0 0 0 1 0 0 　　0 0 0 2 0 0 0 2 1 0 0 1 0 0 0 　　0 0 0
     * 0 0 0 0 0 0 0 0 0 0 1 0 　　0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 　　0 0 0 0 0 1 0 0
     * 0 0 0 0 0 0 0 　　0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
     * 当字符匹配的时候，我们并不是简单的给相应元素赋上1，而是赋上其左上角元素的值加一
     * 。我们用两个标记变量来标记矩阵中值最大的元素的位置，在矩阵生成的过程中来判断当前生成的元素的值是不是最大的
     * ，据此来改变标记变量的值，那么到矩阵完成的时候，最长匹配子串的位置和长度就已经出来了。
     * 这样做速度比较快，但是花的空间太多。我们注意到在改进的矩阵生成方式当中，每生成一行，前面的那一行就已经没有用了。因此我们只需使用一维数组即可。
     * 
     * @param str1
     *            要求公共子串的串1
     * @param str2
     *            要求公共子串的串2
     * @return 没有公共子串返回null，有公共子串返回最长公共子串，若有多个长度一样的公共子串返回第一个出现在串2的公共子串
     */
    public static final String LCS(final String str1, final String str2){
        if (str1 == null || str2 == null) return null;
        int i, j;
        int len1, len2;
        len1 = str1.length();
        len2 = str2.length();
        if (len1 == 0 || len2 == 0) return null;
        int maxLen = len1 > len2 ? len1 : len2;
        int[] max = new int[maxLen];
        int[] maxIndex = new int[maxLen];
        int[] c = new int[maxLen];

        for (i = 0; i < len2; i++) {
            for (j = len1 - 1; j >= 0; j--) {
                if (str2.charAt(i) == str1.charAt(j)) {
                    if ((i == 0) || (j == 0))
                        c[j] = 1;
                    else
                        c[j] = c[j - 1] + 1;
                } else {
                    c[j] = 0;
                }

                if (c[j] > max[0]) { // 如果是大于那暂时只有一个是最长的,而且要把后面的清0;
                    max[0] = c[j];
                    maxIndex[0] = j;

                    for (int k = 1; k < maxLen; k++) {
                        max[k] = 0;
                        maxIndex[k] = 0;
                    }
                } else if (c[j] == max[0]) { // 有多个是相同长度的子串
                    for (int k = 1; k < maxLen; k++) {
                        if (max[k] == 0) {
                            max[k] = c[j];
                            maxIndex[k] = j;
                            break; // 在后面加一个就要退出循环了
                        }

                    }
                }
            }
        }

        for (j = 0; j < maxLen; j++) {
            if (max[j] > 0) {
                return str1
                        .substring(maxIndex[j] - max[j] + 1, maxIndex[j] + 1);
                // System.out.print("第" + (j + 1) + "个公共子串:");
                // System.out.println(str1.substring(maxIndex[j] - max[j] +
                // 1,maxIndex[j]+1));
                // for (i = maxIndex[j] - max[j] + 1; i <= maxIndex[j]; i++)
                // System.out.print(str1.charAt(i));
                // System.out.println(" ");
            }
        }
        return null;
    }

    /**
     * double比较的误差
     */
    private static final double ERROR_DOUBLE_COMPARE = 0.0000001;

    /**
     * 比较2个对象的大小，o1和o2如果o1>02返回大于0的值，o1==02返回0，o1<02返回小于0的值
     * 该方法，Double.NaN=null，String和StringBuffer，Number类型比较，值相等则认为对象大小一致
     * 而Calendar和Date对象比较一样的日期对象大小不一致
     * 
     * @param o1
     *            要比较的对象
     * @param o2
     *            要进行比较的对象
     * @return o1和o2如果o1>02返回大于0的值，o1==02返回0，o1<02返回小于0的值
     */
    public final static int compareObject(final Object o1, final Object o2){
        if (o1 == o2) return 0;

        if (o2 == null)
            return (o1 instanceof Double) ? compareDouble((Double) o1, o2) : 1; // 为了处理nan=null
        if (o1 == null)
            return (o2 instanceof Double) ? -compareDouble((Double) o2, o1)
                    : -1;

        if (o1 instanceof Long && o2 instanceof Long) // 大数值如果转换成double比较可能无法得出正确结果：(99999999999999999+1>99999999999999999)
            return ((Long) o1).compareTo((Long) o2);

        if (o1 instanceof Number && o2 instanceof Number) {
            double d1 = ((Number) o1).doubleValue();
            double d2 = ((Number) o2).doubleValue();
            return compareDouble(d1, d2);
        }

        if (o1 instanceof String && o2 instanceof String) {
            return ((String) o1).compareTo((String) o2);
        }

        if (o1 instanceof Calendar && o2 instanceof Calendar) {
            return compareDate((Calendar) o1, (Calendar) o2);
        }
        if (o1 instanceof Calendar || o2 instanceof Calendar) {
            if (o1 instanceof Calendar) {
                return compareDate((Calendar) o1, o2);
            } else {
                return -compareDate((Calendar) o2, o1);
            }
        }
        if (o1 instanceof Boolean && o2 instanceof Boolean) {
            return compareBoolean(((Boolean) o1).booleanValue(),
                    ((Boolean) o2).booleanValue());
        }

        if (o1 instanceof Comparable
                && o2.getClass().isAssignableFrom(o1.getClass())) {
            return ((Comparable) o1).compareTo(o2);
        }

        /**
         * if(D2>@max,'#FFFF00',if(D2<@min,'#ff00ff','#00ff00')) 参数@max给20，@min
         * 给10，D2是浮点数，那么上面这个表达式计算不正确，系统吧@min和@max当作字符串和d2做比较去了，用下面的表达式可以：
         * if(D2><#=@max#>,'#FFFF00',if(D2<<#=@min#>,'#ff00ff','#00ff00'))
         */
        if (o1 instanceof Number || o2 instanceof Number) {
            return compareDouble(parseDouble(o1, Double.NaN),
                    parseDouble(o2, Double.NaN));
        }

        String str1 = StrYn.object2str(o1);// object2str可能返回null
        String str2 = StrYn.object2str(o2);
        if (str1 == null) {
            return str2 == null ? 0 : -1;
        } else {
            return str2 == null ? 1 : str1.compareTo(str2);
        }
    }

    private static int compareDate(final Calendar o1, final Object o2){
        String datestr2 = o2.toString();
        String style = StrYn.guessDateFormat(datestr2);
        String datestr1 = StrYn.date2str(o1, style);
        return datestr1.compareTo(datestr2);
    }

    /**
     * 比较2个布尔类型的值是否相等
     * 
     * @param b1
     *            要比较的布尔类型值
     * @param b2
     *            要比较的布尔类型值
     * @return 相等返回0，不等，b1为true返回1，false返回-1
     */
    public final static int compareBoolean(final boolean b1, final boolean b2){
        if (b1 == b2) {
            return 0;
        }
        return b1 ? 1 : -1;
    }

    /**
     * 比较2个日期大小
     * 
     * @param c1
     *            要比较的日期
     * @param c2
     *            要比较的日期
     * @return 如果c1==c2，返回0，c1>c2返回1，c1<c2返回-1
     */
    public final static int compareDate(final Calendar c1, final Calendar c2){
        if (c1 == null && c2 == null) {
            return 0;
        }
        if (c1 == null) {
            return -1;
        }
        if (c2 == null) {
            return 1;
        }
        if (c1.equals(c2)) {
            return 0;
        } else if (c1.after(c2)) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * 将Double类型的o1与Object类型的o2进行比较 nan 和null 相等
     * 
     * @param o1
     *            要比较的double
     * @param o2
     *            要进行比较的Object
     * @return 相等返回0，o1>o2返回1，o1<o2返回-1
     */
    public final static int compareDouble(final Double o1, final Object o2){
        if (o2 instanceof Number)
            return compareDouble(o1.doubleValue(), ((Number) o2).doubleValue());
        if (o1.isNaN() && o2 == null) {
            return 0;
        }
        if (o2 == null) // 非nan的double大于null
            return 1;
        return o1.compareTo((Double) o2);
    }

    /**
     * 比较2个对象的大小
     * 
     * @param o1
     *            要比较的对象
     * @param o2
     *            要进行比较的对象
     * @param ignoreCase
     *            如果对象是字符串类型，是否忽略大小写
     * @return 相等返回0,o1<o2返回-1，o1>o2返回1
     */
    public final static int compareObject(final Object o1, final Object o2,
            final boolean ignoreCase){
        if (ignoreCase && o1 instanceof String && o2 instanceof String) {
            return o1.toString().compareToIgnoreCase(o2.toString());
        }
        return compareObject(o1, o2);
    }

    /**
     * 对2个Double类型的数进行比较
     * 
     * @param d1
     *            要比较的数
     * @param d2
     *            要进行比较的数
     * @return 相等返回0，d1>d2返回1，d1小于d2返回-1
     */
    public final static int compareDouble(final double d1, final double d2){
        if (d1 == d2) {
            return 0;
        }
        if (Double.isNaN(d1) && Double.isNaN(d2)) {
            return 0;
        }
        if (Double.isNaN(d1) || Double.isNaN(d2)) {// 非nan大于nan
            return Double.isNaN(d2) ? 1 : -1;
        }
        double cc = d1 - d2;
        if (cc > ERROR_DOUBLE_COMPARE) {
            return 1;
        } else if (cc < -ERROR_DOUBLE_COMPARE) {
            return -1;
        } else {
            return 0;
        }
    }

    final static Pattern PATTERN_SANLIB = Pattern
            .compile("^com\\.(sanlink|esen|esensoft)\\.");

    /**
     * 判断是否是com.sanlink,com.esen,com.esensoft下的类 仅仅通过名称来判断，不管类是否真的存在
     * 如果传参com.esen.util.SC，也会返回true(SC这个类不存在) 故传参的时候注意参数的正确性
     * 
     * @param className
     *            类名
     * @return 一个类名是com.sanlink,com.esen,com.esensoft下的类返回true，否则false
     */
    public static final boolean isSanLibClass(final String className){
        return PATTERN_SANLIB.matcher(className).find();
    }

    /**
     * 找到com.sanlink,com.esen,com.esensoft下的类的所抛出的异常
     * 
     * @param stacks异常堆栈信息
     * @return 根据一组堆栈信息找到
     */
    public static StackTraceElement[] findSabLibStacks(
            final StackTraceElement stacks[]){
        if (stacks == null || stacks.length == 0) return null;
        List l = new ArrayList();
        for (int i = 0; i < stacks.length; i++) {
            if (isSanLibClass(stacks[i].getClassName())) {
                l.add(stacks[i]);
            }
        }
        StackTraceElement[] rs = new StackTraceElement[l.size()];
        l.toArray(rs);
        return rs;
    }

    /**
     * @param bts
     *            要转换的long类型大小
     * @return 将一个File或内存的long 转化为 ..MB..KB..BYTES
     */
    public static final String formatBytes(final long bts){
        long bt = bts % 1024;
        long kb = (bts / 1024) % 1024;
        long mb = bts / (1024 * 1024);
        return ((mb > 0) ? (mb + "MB,") : "") + ((kb > 0) ? (kb + "KB,") : "")
                + ((bt > 0) ? (bt + "BYTES") : "");
    }

    /**
     * @param bts
     *            bts>=0
     * @return 把一个以byte为单位的数量bts转化为 一种比较易读的格式，e.g., 1K 234M 2.1G 当 bts>=1T
     *         返回：xx.xT 当 bts>=1G 返回：xx.xG 当 bts>=1Mb 返回：xx.xM 当 bts>=1Kb
     *         返回：xx.xK 当 bts>=1b 返回：xxb
     *         当舍弃小数部分时要四舍五入，如果没有小数就不要显示小数，例如“2.0G”应该显示为“2G”
     */
    public static final String formatSize(final double bts){
        if (bts == 0) return "0b";
        String format = null;
        int pow = 0;
        if (bts >= (long) Math.pow(2, 40)) { // 如果bts>=1T
            pow = 40;
            format = "0.#T";
        } else if (bts >= (long) Math.pow(2, 30)) { // 如果bts>=1G
            pow = 30;
            format = "0.#G";
        } else if (bts >= (long) Math.pow(2, 20)) { // 如果bts>=1M
            pow = 20;
            format = "0.#M";
        } else if (bts >= 1024) {// 如果bts>=1K
            pow = 10;
            format = "0.#K";
        } else {
            // 不用再做if (bts > 0) { // 如果bts>0B
            // 避免当bts<=0时format为null导致后面的代码出现异常
            pow = 0;
            format = "0.#b";
        }
        DecimalFormat df = new DecimalFormat(format);
        return df.format(bts * 1.0 / Math.pow(2, pow));
    }

    /**
     * 用分割符分割字符串
     * 
     * @param s
     *            要分割出int的字符串
     * @param c
     *            分割符
     * @return 分割后的字符串数组
     */
    public static final String[] splitByChar(final String s, final char c){
        if (s == null) {
            return null;
        }
        int cnt = charCount(s, c);
        if (cnt == 0) {
            return new String[] {s};
        }

        String[] r = new String[cnt + 1];
        int f = 0;
        int k = 0;
        for (int i = 0, len = s.length(); i < len; i++) {
            if (c == s.charAt(i)) {
                r[k++] = s.substring(f, i);
                f = i + 1;
            }
        }
        r[k++] = s.substring(f);
        return r;
    }

    /**
     * 返回分隔符c截取的字符串后的第index个串
     * 
     * @param s
     *            要分割出int的字符串
     * @param c
     *            分割符
     * @param index
     *            选取分割的第几位
     * @return 分割后的字符串
     */
    public static final String splitByChar(final String s, char c, int index){
        if (s == null) {
            return null;
        }
        int k = 0;
        int f = 0;
        for (int i = 0, len = s.length(); i < len; i++) {
            if (c == s.charAt(i)) {
                if (k++ == index) {
                    return s.substring(f, i);
                }
                f = i + 1;
            }
        }
        return index == k ? s.substring(f) : null;
    }

    /**
     * 返回分隔符c截取的字符串所转的数字，若转换失败，返回默认值
     * 
     * @param s
     *            要分割出int的字符串
     * @param c
     *            分割符
     * @param index
     *            选取分割的第几位进行转换
     * @param def
     *            默认值
     * @return int数
     */
    public static final int splitIntByChar(final String s, char c, int index,
            int def){
        if (s == null) {
            return def;
        }
        int k = 0;
        int f = 0;
        for (int i = 0, len = s.length(); i < len; i++) {
            if (c == s.charAt(i)) {
                if (k++ == index) {
                    return str2int(s, f, i, def);
                }
                f = i + 1;
            }
        }
        return index == k ? str2int(s, f, s.length(), def) : def;
    }

    /**
     * 对指定的文本串进行编码操作
     * 
     * @param str
     *            需要编码的字符串
     * @return 编码后结果
     * @throws UnsupportedEncodingException
     *             编码可能会抛异常
     */
    public static final String escapeURIComponent(final String str)
            throws UnsupportedEncodingException{
        return isNull(str) ? null : URLEncoder.encode(escape(str), "UTF-8");
    }

    /**
     * 对指定的文本串进行解码操作
     * 
     * @param str
     *            需要解码的密文
     * @return 解密后的串
     * @throws Exception
     *             字符转换可能会抛异常
     */
    public static final String unescapeURIComponent(final String str)
            throws Exception{
        return isNull(str) ? null : unescape(URLDecoder.decode(str, "UTF-8"));
    }

    /**
     * 全国组织机构代码由八位数字（或大写拉丁字母）本体代码和一位数字（或大写拉丁字母）校验码组成。 本体代码采用系列（即分区段）顺序编码方法。
     * 校验码按下列公式计算：C9=11－MOD(∑Ci×Wi,11) MOD-表示求余函数；i-表示代码字符从左至右位置序号；
     * Ci-表示第i位置上的代码字符的值，采用附录A“代码字符集”所列字符； C9-表示校验码； Wi-表示第i位置上的加权因子，其数值如下表： I 1
     * 2 3 4 5 6 7 8 Wi 3 7 9 10 5 8 4 2
     * 当MOD函数值为1（即C9=10）时，校验码应用大写拉丁字母X表示；当MOD函数值为0（即C9=11）时，校验码仍用0表示。
     * 
     */
    private static final int PDYW[] = {3, 7, 9, 10, 5, 8, 4, 2};

    /**
     * 获得组织机构代码的校验位
     * 
     * @param pdy
     *            组织机构代码的前8位
     * @return 对应的校验位
     * @throws Exception
     *             抛出的自定义异常，长度小于8位及代码包含非数字和大写字母的其他字符会抛异常
     */
    public static final char getPdyCode(final String pdy) throws Exception{
        if (pdy == null || pdy.length() != 8)
        // throw new Exception("不是合法的组织机构代码，代码长度不得小于8位!");
            throw new Exception(I18N.getString("com.esen.util.StrFunc.5", "否"));
        int cw = 0;
        char ch;
        int c;
        for (int i = 0; i < 8; i++) {
            ch = pdy.charAt(i);
            if ((ch >= '0') && (ch <= '9'))
                c = ch - '0';
            else if ((ch >= 'A') && (ch <= 'Z'))
                c = ch - 'A' + 10;
            else
                // throw new
                // Exception("不是合法的组织机构代码，代码只允许0~9,A~Z，不允许小写字母和其他字符!");
                throw new Exception(I18N.getString("com.esen.util.StrFunc.6",
                        "不是合法的组织机构代码，代码长度不得小于8位!"));
            cw += PDYW[i] * c;
        }
        c = 11 - (cw % 11);
        if (c == 10) return 'X';
        return (char) (c == 11 ? '0' : ('0' + c));
    }

    /**
     * pdy码长度
     */
    public static final int PDYLEN = 9;

    /**
     * 是否是组织机构代码
     * 
     * @param pdy
     *            代码字符串
     * @return 是组织机构代码返回true，反之false
     */
    public static boolean isPdy(final String pdy){
        if ((pdy == null) || pdy.length() != PDYLEN) return false;
        int cw = 0;
        char ch;
        int c;
        for (int i = 0; i < 8; i++) {
            ch = pdy.charAt(i);
            if ((ch >= '0') && (ch <= '9'))
                c = ch - '0';
            else if ((ch >= 'A') && (ch <= 'Z'))
                c = ch - 'A' + 10;
            else
                return false;
            cw += PDYW[i] * c;
        }
        c = 11 - (cw % 11);
        ch = pdy.charAt(8);
        if (c == 10) return ch == 'X';
        return c == 11 ? ch == '0' : ch == ('0' + c);
    }

    private static final byte[] PDY = {'P', 'D', 'Y', '0', '0', '0', '0', '0',
            'X'};

    /**
     * 由5位数字产生PDY编码 传参大于5位数的0，会默认转为5个0</br>
     * 为满足各系统管理上的特殊需要，规定本体代码PDY00001至PDY99999为自定义区，供个系统编制内部组织机构代码使用。
     * 
     * @param i
     *            5位数
     * @return i为负数或大于5位数返回null，
     */
    public static String makePdyCode(final int i){
        if ((i < 0) || (i > 99999)) return null;
        byte s[] = (byte[]) PDY.clone();
        String v = String.valueOf(i);
        int len = v.length();
        for (int j = 0; j < len; j++)
            s[8 - len + j] = (byte) v.charAt(j);
        // PDY
        // int cw = 25 * 3 + 13 * 7 + 34 * 9;
        int cw = 472;
        for (int j = 3; j < 8; j++)
            cw += PDYW[j] * (s[j] - '0');
        int c = 11 - (cw % 11);
        if (c == 10)
            s[8] = 'X';
        else
            s[8] = (byte) (c == 11 ? '0' : ('0' + c));
        return new String(s);
    }

    public static final int[] IDCARD_WI = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9,
            10, 5, 8, 4, 2}; // 身份证第i位置上的加权因子

    public static final char[] IDCARD_YI = {'1', '0', 'X', '9', '8', '7', '6',
            '5', '4', '3', '2'}; // Sum(Ai * Wi)%11后对应的校验码

    /**
     * 校验方法： （1）十七位数字本体码加权求和公式 S = Sum(Ai * Wi), i = 0, ... , 16 ，先对前17位数字的权求和
     * Ai:表示第i位置上的身份证号码数字值 Wi:表示第i位置上的加权因子 Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4
     * 2
     * 
     * （2）计算模 Y = mod(S, 11)
     * 
     * （3）通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0 X 9 8 7 6 5 4 3 2
     * 
     * @param idcard
     *            新版身份证前17位 如果传参是17位字符串，包含字母或其他非数字字符不会抛异常，当做正常传参进行处理，
     *            故这个地方注意传参正确性
     * @return 根据前17位生成最后一位加权码
     * @throws Exception
     *             传参为null或长度不是17会抛异常
     */
    public static final char getIdCardCode(final String idcard)
            throws Exception{
        if (StrYn.isNull(idcard) || idcard.length() != 17)
        // throw new Exception("不是合法的身份证号码,必须小于17位.");
            throw new Exception(I18N.getString("com.esen.util.StrFunc.7",
                    "不是合法的身份证号码,必须小于17位."));
        char pszSrc[] = idcard.toCharArray();
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (int) (pszSrc[i] - '0') * IDCARD_WI[i];
        }
        int ymod = sum % 11;
        return IDCARD_YI[ymod];
    }

    /**
     * 二代身份证长度常量
     */
    public static final int PIDLEN = 18;

    /**
     * 判断是否是合法的新身份证ID
     * 
     * @param idcard
     *            需要判断的字符串
     * @return 是合法的id返回true，反之false
     */
    public static final boolean isIdCard(String idcard) throws Exception{
        if (StrYn.isNull(idcard) || idcard.length() != PIDLEN) return false;
        char c = getIdCardCode(idcard.substring(0, PIDLEN - 1));
        return idcard.charAt(PIDLEN - 1) == c;
    }

    /**
     * 合法电话号码字符串的正则
     */
    public static final Pattern PATTERN_ALL_PHONE = Pattern
            .compile("^(\\+[\\d]{1,2})?(1([3|5|8]{1}[\\d]{9})|((106)?(((0[\\d]{2})?[1-9]{1}[\\d]{7})|((0[\\d]{3})?[1-9]{1}[\\d]{6}))))$");

    /**
     * 检查有时候否一个合法的电话号码,可以为+8601012345678,01012345678,12345678,9647292,+
     * 8613912345678,+8615912345678,10602783560000</br>
     * 然而，对于一些特殊的号码，也会返回false，如：110，80086等
     * 
     * @param number
     *            需要判断的数字串
     * @return 是合法电话号码返回true，反之false
     */
    public static final boolean isPhoneNumber(final String number){
        if (StrYn.isNull(number)) return false;
        return PATTERN_ALL_PHONE.matcher(number.trim()).find();
    }

    /**
     * 判断是否是firefox浏览器 传参时注意正确传入浏览器ua字串 以字符串是否包含Firefox来判断是否是firefox浏览器
     * 
     * @param useragent
     *            声明了浏览器用于 HTTP 请求的用户代理头的值的字串
     * @return 是firefox浏览器的useragent返回true，反之false
     */
    public static boolean isFirefox(final String useragent){
        return useragent != null && useragent.indexOf("Firefox") != -1;
    }

    /**
     * 判断是否是IE浏览器 传参时注意正确传入浏览器ua字串 以字符串是否包含MSIE来判断是否是IE浏览器
     * 
     * @param useragent
     *            声明了浏览器用于 HTTP 请求的用户代理头的值的字串
     * @return 是IE浏览器的useragent返回true，否则false
     */
    public static boolean isIE(final String useragent){
        return useragent != null && useragent.indexOf("MSIE") != -1;
    }

    /**
     * 20121220 by kangx 判断是否是Chrome浏览器 传参时注意正确传入浏览器ua字串
     * 以字符串是否包含Chrome来判断是否是Chrome浏览器
     * 
     * @param useragent
     *            声明了浏览器用于 HTTP 请求的用户代理头的值的字串
     * @return 是Chrome浏览器的useragent返回true，否则false
     */
    public static boolean isChrome(final String useragent){
        return useragent != null && useragent.indexOf("Chrome") != -1;
    }

    /**
     * 20121220 by kangx 判断是否是Safari浏览器 传参时注意正确传入浏览器ua字串
     * 以字符串是否包含Safari来判断是否是Safari浏览器
     * 
     * @param useragent
     *            声明了浏览器用于 HTTP 请求的用户代理头的值的字串
     * @return 是Safari浏览器的useragent返回true，否则false
     */
    public static boolean isSafari(final String useragent){
        return useragent != null && useragent.indexOf("Safari") != -1;
    }

    /**
     * 判断是否为Ascii字符
     * 
     * @param ch
     *            字符的isAscii码
     * @return 是ascii码返回true，反之false
     */
    public static final boolean isAscii(final int ch){
        return ((ch & 0xFFFFFF80) == 0);
    }

    /**
     * 获得一个Integer对象，此类避免了重复的创建Integer类，其实jdk1.5已经提供了相关方法
     * 
     * @param i
     *            Integer对象的值
     * @return Integer对象
     */
    public static final Integer intobj(final int i){
        // return Integer.valueOf(type);//如果是jdk1.5可以这样！
        final int offset = 128;
        if (i >= -128 && i <= 127) { // must cache
            return INTCACHE[i + offset];
        }
        return new Integer(i);
    }

    /**
     * 返回a最接近的整数值
     * 
     * @param a
     *            要转换成long类型的数据
     * @return a最接近的整数值
     */
    public static long round(double a){
        return (long) StrictMath.floor(a + 0.500000001d);
    }

    private static final double[] DIV = {1.0, 10.0, 100.0, 1000.0, 10000.0,
            100000.0, 1000000.0, 10000000.0};

    /**
     * 保留declen位小数位数
     * 
     * @param d
     *            要处理的浮点数
     * @param declen
     *            要保留的小数位数
     * @return
     */
    public final static double round(double d, int declen){
        // /* TODO: 长度大于8的暂时不解决 */
        // if (declen >= 8)
        // return d;
        // if (Double.isInfinite(d) || Double.isNaN(d)) {
        // return d;
        // }
        // //参照excel标准，round(-0.5,0)=-1
        // if(d<0){
        // if(declen<0)
        // return -round(-d / DIV[-declen]) * DIV[-declen];
        // return -round(-d * DIV[declen]) / DIV[declen];
        // }else{
        // if(declen<0)
        // return round(d / DIV[-declen]) * DIV[-declen];
        // return round(d * DIV[declen]) / DIV[declen];
        // }
        return MathUtil.round(d, declen);
    }

    public static Locale parseLocaleStr(String langStr, Locale defaultLocale){

        for (Locale locale : I18N.LOCALES) {
            if (StrYn.compareStr(langStr, locale.toString())) {
                return locale;
            }
        }

        String[] parts = langStr.split("[_-]");
        String lang = parts[0];
        String country = parts.length == 2 ? parts[1] : "";
        Locale locale = null;
        if (parts.length == 2 && lang.equalsIgnoreCase("zh")) {
            if (country.equalsIgnoreCase("CN")) {
                locale = Locale.SIMPLIFIED_CHINESE;
            } else if (country.equalsIgnoreCase("TW")) {
                locale = Locale.TRADITIONAL_CHINESE;
            } else {
                locale = Locale.SIMPLIFIED_CHINESE;
            }
        } else if (lang.equalsIgnoreCase("en")) {
            locale = Locale.ENGLISH;
        } else if (lang.equalsIgnoreCase("ja")) {
            locale = Locale.JAPANESE;
        } else if (lang.equalsIgnoreCase("zh")) {
            locale = Locale.SIMPLIFIED_CHINESE;
        } else {
            locale = defaultLocale;
        }
        return locale;
    }

}