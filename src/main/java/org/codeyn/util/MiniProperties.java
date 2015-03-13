package org.codeyn.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codeyn.util.yn.StrmUtil;
import org.codeyn.util.yn.StrUtil;

/**
 * 此类和Properties不同的地方是，提供一些更易用的get方法和set方法 存储的格式不编码unicode的，存储时带有编码信息
 */
public class MiniProperties extends Properties{
    /**
	 * 
	 */
    private static final long serialVersionUID = 8556292079479092975L;

    private static final String HEADER_CHARSET = "#charset ";

    public MiniProperties(){
    }

    /**
     * 根据key值返回对应的value值
     * 
     * @param key
     *            指定的key值
     * @return 返回value
     */
    public String getString(String key){
        return getProperty(key);
    }

    /**
     * 根据指定key值返回对应的value值，当value值为null或""时，返回缺省值defaultValue
     * 
     * @param key
     *            指定的key值
     * @param defaultValue
     *            缺省值
     * @return 返回value或defaultValue
     */
    public String getString(String key, String defaultValue){
        return getProperty(key, defaultValue);
    }

    /**
     * 根据指定的key返回对应的value值的long类型，如果value不能转换为long类型，默认返回0
     * 
     * @param key
     *            指定的key值
     * @return 返回value的long类型
     */
    public long getLong(String key){
        return StrUtil.str2long(getProperty(key), 0);
    }

    /**
     * 根据指定的key返回对应的value值的long类型，如果value不能转换为long类型，则返回默认值defaultValue
     * 
     * @param key
     *            指定的key值
     * @param defaultValue
     *            缺省值
     * @return 返回value的long类型或缺省值
     */
    public long getLong(String key, long defaultValue){
        return StrUtil.str2long(getProperty(key), defaultValue);
    }

    /**
     * 根据指定的key返回对应的value值的int类型，如果value不能转换为int类型，默认返回0
     * 
     * @param key
     *            指定的key值
     * @return 返回value的int类型或0
     */
    public int getInt(String key){
        return StrUtil.str2int(getProperty(key), 0);
    }

    /**
     * 根据指定的key返回对应的value值的int类型，如果value不能转换为int类型，则返回默认值defaultValue
     * 
     * @param key
     *            指定的key值
     * @param defaultValue
     *            缺省值
     * @return 返回value的int类型或defaultValue
     */
    public int getInt(String key, int defaultValue){
        return StrUtil.str2int(getProperty(key), defaultValue);
    }

    /**
     * 根据指定的key返回对应的value值的float类型，如果value不能转换为float类型，则返回默认值0
     * 
     * @param key
     *            指定的key值
     * @return 返回value的float类型或0
     */
    public float getFloat(String key){
        return StrUtil.str2float(getProperty(key), 0);
    }

    /**
     * 根据指定的key返回对应的value值的float类型，如果value不能转换为float类型，则返回默认值defaultValue
     * 
     * @param key
     *            指定的key值
     * @param defaultValue
     * @return 返回value的float类型或defaultValue
     */
    public float getFloat(String key, float defaultValue){
        return StrUtil.str2float(getProperty(key), defaultValue);
    }

    /**
     * 根据指定的key返回对应的value值的double类型，如果value不能转换为double类型，则返回默认值0
     * 
     * @param key
     *            指定的key值
     * @return 返回value的double类型或0
     */
    public double getDouble(String key){
        return StrUtil.str2double(getProperty(key), 0);
    }

    /**
     * 根据指定的key返回对应的value值的double类型，如果value不能转换为double类型，则返回默认值defaultValue
     * 
     * @param key
     *            指定的key值
     * @param defaultValue
     *            缺省值
     * @return
     */
    public double getDouble(String key, double defaultValue){
        return StrUtil.str2double(getProperty(key), defaultValue);
    }

    /**
     * 根据指定的key返回对应的value值的boolean类型
     * 
     * @param key
     *            指定的key值
     * @return 返回value的boolean类型
     */
    public boolean getBoolean(String key){
        return StrUtil.str2boolean(getProperty(key));
    }

    /**
     * 根据指定的key返回对应的value值的boolean类型,如果value值为null或"",则返回缺省值defaultValue
     * 
     * @param key
     *            指定key值
     * @param defaultValue
     *            缺省值
     * @return
     */
    public boolean getBoolean(String key, boolean defaultValue){
        return StrUtil
                .str2boolean(getProperty(key, String.valueOf(defaultValue)));
    }

    /**
     * 设置MiniProperties的值，key为键值，value为对应的值
     * 
     * @param key
     *            指定键值
     * @param v
     *            对应的value值
     * @param ifnullOrBlankThenRemoveIt
     *            如果为true则不插入value值为null或""的键值对，反之插入
     */
    public void setString(String key, String v,
            boolean ifnullOrBlankThenRemoveIt){
        if (ifnullOrBlankThenRemoveIt && (v == null || v.length() == 0)) {
            this.remove(key);
        } else {
            this.setString(key, v);
        }
    }

    /**
     * 增加键值对，当value为空时，插入""
     * 
     * @param key
     *            要插入的键值
     * @param value
     *            要插入的value值
     * @return 返回输入键值的旧值，如果没有值，则为 null。
     */
    public String setString(String key, String value){
        if (value == null) {
            return (String) setProperty(key, "");
        }
        return (String) setProperty(key, value);
    }

    /**
     * 插入键值对，value为int类型
     * 
     * @param key
     *            要插入的key值
     * @param value
     *            要插入的value值
     * @return 返回value值
     */
    public int setInt(String key, int value){
        setProperty(key, String.valueOf(value));
        return value;
    }

    /**
     * 插入键值对，value为float类型
     * 
     * @param key
     *            要插入的key值
     * @param value
     *            要插入的value值
     * @return 返回value值
     */
    public float setFloat(String key, float value){
        setProperty(key, String.valueOf(value));
        return value;
    }

    /**
     * 插入键值对，value为double类型
     * 
     * @param key
     *            要插入的key值
     * @param value
     *            要插入的value值
     * @return 返回value值
     */
    public double setDouble(String key, double value){
        setProperty(key, String.valueOf(value));
        return value;
    }

    /**
     * 插入键值对，value为boolean类型
     * 
     * @param key
     *            要插入的key值
     * @param value
     *            要插入的value值
     * @return 返回value值
     */
    public boolean setBoolean(String key, boolean value){
        setProperty(key, String.valueOf(value));
        return value;
    }

    /**
     * 插入键值对，value为long类型
     * 
     * @param key
     *            要插入的key值
     * @param value
     *            要插入的value值
     * @return 返回value值
     */
    public long setLong(String key, long value){
        setProperty(key, String.valueOf(value));
        return value;
    }

    /**
     * 从文件导入
     * 
     * @param fn
     * @throws IOException
     */
    public synchronized void loadIfExists(String fn) throws IOException{
        File fobj = new File(fn);
        if (fobj.exists() && fobj.isFile()) {
            FileInputStream fin = new FileInputStream(fobj);
            try {
                load(fin);
            } finally {
                fin.close();
            }
        }
    }

    /**
     * 保存到文件
     * 
     * @param fn
     * @throws IOException
     */
    public synchronized void saveToFile(String fn) throws IOException{
        saveToFile(fn, null);
    }

    /**
     * 将MiniProperties保存到文件中，header为文件头注释
     * 
     * @param fn
     *            文件路径
     * @param header
     *            文件头注释
     * @throws IOException
     */
    public synchronized void saveToFile(String fn, String header)
            throws IOException{
        saveToFile(fn, header, null);
    }

    /**
     * 将MiniProperties以指定编码保存到文件中，header为文件头注释
     * 
     * @param fn
     *            文件路径
     * @param header
     *            文件头注释
     * @param charset
     *            编码
     * @throws IOException
     */
    public synchronized void saveToFile(String fn, String header, String charset)
            throws IOException{
        FileOutputStream fout = new FileOutputStream(fn);
        try {
            File fobj = new File(fn);
            fobj.getParentFile().mkdirs();
            store(fout, header, charset);
        } finally {
            fout.close();
        }
    }

    /**
     * 将MiniProperties保存到指定的流中
     * 
     * @throws IOException
     */
    public synchronized void store(OutputStream out) throws IOException{
        this.store(out, null, null);
    }

    /**
     * 将MiniProperties保存到指定的流中
     * 
     * @param fn
     *            文件路径
     * @param header
     *            文件头注释
     */
    public synchronized void store(OutputStream out, String header)
            throws IOException{
        this.store(out, header, null);
    }

    /**
     * 如果指定了编码，那么按指定的编码保存，并且在第一行保存编码信息，例如： #charset UTF-8
     */
    public synchronized void store(OutputStream out, String header,
            String encoding) throws IOException{
        BufferedWriter awriter;
        if (StrUtil.isNull(encoding)) {
            awriter = new BufferedWriter(new OutputStreamWriter(out));
        } else {
            awriter = new BufferedWriter(new OutputStreamWriter(out, encoding));
            writeln(awriter, HEADER_CHARSET + encoding);
        }
        if (header != null) {
            String[] s = header.split("\r\n|\r|\n");
            for (int i = 0; i < s.length; i++) {
                writeln(awriter, "#" + s[i]);
            }
        }
        writeln(awriter, "#" + new Date().toString());
        for (Enumeration e = keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            String val = (String) get(key);
            // 空值不保存
            if (val == null) continue;

            key = saveConvert(key, true);
            val = saveConvert(val, false);
            writeln(awriter, key + "=" + val);
        }
        awriter.flush();
    }

    /**
     * 编码字串
     */
    private String saveConvert(String theString, boolean escapeSpace){
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len * 2);

        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            switch (aChar) {
                case ' ':
                    if (x == 0 || escapeSpace) {
                        outBuffer.append('\\');
                    }
                    outBuffer.append(' ');
                    break;
                case '\t':
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;
                case '\n':
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;
                case '\r':
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;
                case '\f':
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;
                default:
                    if (specialSaveChars.indexOf(aChar) != -1) {
                        outBuffer.append('\\');
                    }
                    outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }

    /**
     * 解析字串
     * 
     * @param s
     * @return
     */
    private String loadConvert(String s){
        int len = s.length();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < len; i++) {
            char achar = s.charAt(i);
            if (achar == '\\') {
                if (i + 1 < len) {
                    i++;
                    achar = s.charAt(i);
                    switch (achar) {
                        case 't':
                            buf.append('\t');
                            break;
                        case 'r':
                            buf.append('\r');
                            break;
                        case 'n':
                            buf.append('\n');
                            break;
                        case 'f':
                            buf.append('\f');
                            break;
                        case 'u':
                            if (i + 4 < len) {
                                int value = 0;
                                for (int t = 0; t < 4; t++) {
                                    achar = s.charAt(i + 1 + t);
                                    if ((achar >= '0') && (achar <= '9')) {
                                        value = (value << 4) + (achar - '0');
                                    } else if ((achar >= 'a') && (achar <= 'z')) {
                                        value = (value << 4) + (achar - 'a')
                                                + 10;
                                    } else if ((achar >= 'A') && (achar <= 'Z')) {
                                        value = (value << 4) + (achar - 'A')
                                                + 10;
                                    } else {
                                        return null;
                                    }
                                }
                                i += 4;
                                buf.append((char) value);
                                break;
                            } else {
                                return null;// 格式不正确，\\uxxxx
                            }
                        case ' ':
                        case '=':
                        case ':':
                        case '\\':
                        case '#':
                        case '!':
                        default:
                            buf.append(achar);
                    }
                } else {// 格式不正确\\与" =:\\trnf#!"总是一起出现
                    return null;
                }
            } else {
                buf.append(achar);
            }
        }
        return buf.toString();
    }

    /**
     * 将流中的内容导入到MiniProperties中
     * 
     * @param InputStream
     *            输入的流
     */
    public synchronized void load(InputStream in) throws IOException{
        load(in, null);
    }

    /**
     * <p>
     * 覆盖父类的同名方法，当获取的对应值为null或""时，返回第二个参数指定的默认值 <br />
     * 注：父类的同名方法仅当获取的对应值为null时，才返回指定的默认值
     * </p>
     * 2011-2-21 ISSUE:BI-4203 保存主题集属性的时候报"java.lang.NullPointerException" <br />
     * MiniProperties的setString方法对null进行了特殊处理，当设置的值对象为null时，会将该对象转换为""，
     * 但是getString(String key, String defaultValue)方法的处理当键的对应值为null时，才返回默认值。 <br />
     * 由于该原因，主题集属性页面没有选中“分母为零时的处理”的单选框组合中的单选框，导致在进行保存时，由于服务器端
     * 接收到的参数为null，进而导致抛出了空指针异常。
     * 
     * @param in
     * @param charset
     */
    public String getProperty(String key, String defaultValue){
        String val = getProperty(key);
        return (StrUtil.isNull(val)) ? defaultValue : val;
    }

    /**
     * 支持传入一个编码， 如果流中带有编码信息，比如下面的格式
     * 
     * #charset UTF-8
     * 
     * 那么自动使用流中的编码设置。
     * 
     * fixme:在websphere上，如果没有指定明确的charset且in中有中文就算是注释，也会有问题
     * 
     * @param in
     * @param charset
     * @throws IOException
     */
    public synchronized void load(InputStream in, String charset)
            throws IOException{
        if (in == null) return;
        if (!in.markSupported()) in = new BufferedInputStream(in);
        in.mark(50);
        String ss = StrmUtil.readFix(in, HEADER_CHARSET.length());
        if (HEADER_CHARSET.equalsIgnoreCase(ss)) {
            charset = StrmUtil.readLine(in);
            if (charset != null) charset = charset.trim();
        }
        in.reset();

        Reader rd;
        if (StrUtil.isNull(charset)) {
            rd = new InputStreamReader(in);
        } else {
            rd = new InputStreamReader(in, charset);
        }
        load(rd);
    }

    /**
     * 将字符串中的内容导入MiniProperties中
     * 
     * @param in
     *            输入的流
     * @throws IOException
     */
    public synchronized void load(String in) throws IOException{
        if (in == null || in.length() == 0) return;
        load(new StringReader(in));
    }

    /**
     * 将流in中的内容导入到MiniProperties中
     * 
     * @param in
     *            输入的流
     * @throws IOException
     */
    public synchronized void load(Reader in) throws IOException{
        if (in == null) {
            return;
        }
        String s;
        String key;
        String value;
        BufferedReader reader = in instanceof BufferedReader ? (BufferedReader) in
                : new BufferedReader(in);
        try {
            while ((s = reader.readLine()) != null) {
                if (StrUtil.isNull(s)) {
                    continue;
                }
                if (commentChars.indexOf(s.charAt(0)) != -1) {
                    continue;
                }
                int p = this.getEqualsPos(s);
                if (p < 0) {// 没有=或:
                    continue;
                }
                key = loadConvert(s.substring(0, p));
                value = loadConvert(s.substring(p + 1, s.length()));
                if (!StrUtil.isNull(key) && !StrUtil.isNull(value)) {
                    put(key, value);
                }
            }
        } finally {
            reader.close();
        }
    }

    /**
     * 返回等号（=或:）的位置
     * 
     * @param s
     * @return
     */
    private int getEqualsPos(String s){
        Pattern p = Pattern
                .compile("(^(\\\\\\\\)*[=:])|([^\\\\](\\\\\\\\)*[=:])");
        Matcher m = p.matcher(s);
        if (m.find()) {
            return m.end() - 1;
        }
        return -1;
    }

    /**
     * Convert a nibble to a hex character
     * 
     * @param nibble
     *            the nibble to convert.
     */
    private static char toHex(int nibble){
        return hexDigit[(nibble & 0xF)];
    }

    private static final String commentChars = "#!";// 注释符号

    private static final String specialLoadChars = " =:\\trnf#!";

    /** A table of hex digits */
    private static final char[] hexDigit = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final String specialSaveChars = "=:\\\t\r\n\f#!";// =,:,\,\t,\t,\n,\f,＃，!

    private static void writeln(BufferedWriter bw, String s) throws IOException{
        bw.write(s);
        bw.newLine();
    }
}
