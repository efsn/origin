package org.codeyn.util.io.file;

import org.codeyn.codec.Base64;
import org.codeyn.util.yn.StrYn;

import java.io.*;
import java.util.Map;

public class TxtLoader {
    protected StringMap tagAttr = new StringMap(" ", "=");
    protected StringMap contentMap = new StringMap();
    private BufferedReader2 in;

    public TxtLoader() {
    }

    /**
     * 抛出异常
     *
     * @param tag
     * @throws Exception
     */
    public static void throwNoEndTagException(String tag) throws Exception {
        throw new Exception("没有匹配到结束标签:" + tag);
    }

    /**
     * 抛出异常
     *
     * @param tag
     * @throws Exception
     */
    public static void throwUnidentificationException(String tag)
            throws Exception {
        throw new Exception("未识别的标签:" + tag);
    }

    public final boolean hasLine() throws IOException {
        return in.ready();
    }

    public final String readLine() throws IOException {
        return in.readLine();
    }

    public void prepareRead(String s) {
        this.in = new BufferedReader2(new StringReader(s), 1024);
    }

    public void prepareRead(byte[] bytes, int size, String charsetName)
            throws UnsupportedEncodingException {
        prepareRead(new MyByteArrayInputStream(bytes, 0, size), charsetName);
    }

    public void prepareRead(InputStream stm)
            throws UnsupportedEncodingException {
        prepareRead(stm, null);
    }

    public void prepareRead(InputStream stm, String charsetName)
            throws UnsupportedEncodingException {
        this.in = new BufferedReader2(
                StrYn.isNull(charsetName) ? new InputStreamReader(stm)
                        : new InputStreamReader(stm, charsetName));
    }

    /**
     * 20100414 BI-3489 yk
     * 此处的size是基于byte的，但是在2.2之后TxtLoader改为使用Reader实现了，一个汉字算一个长度，故此函数需要修改
     */
    public final String readFixString(int size) throws IOException {
        // char[] r = new char[size];
        // int count = in.read(r);
        // return new String(r, 0, count);
        StringBuffer sb = new StringBuffer(size);
        while (size > 0) {
            int k = in.read();
            if (k == -1) {
                break;
            }
            char c = (char) k;
            size -= (c > 0xFF) ? 2 : 1;
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 解码，format可能是base64或空
     */
    public final String decode(String s, String format) {
        if (format == null || format.length() == 0 || s == null
                || s.length() == 0) {
            return s;
        }
        if (format.equalsIgnoreCase("base64")) {
            return new String(Base64.decodeBase64(s.getBytes()));
        }
        return s;
    }

    /**
     * 解码，format可能是base64或空
     */
    public final byte[] decode2bytes(String s, String format) {
        if (format == null || format.length() == 0 || s == null
                || s.length() == 0) {
            return null;
        }
        if (format.equalsIgnoreCase("base64")) {
            return Base64.decodeBase64(s.getBytes());
        }
        return s.getBytes();
    }

    public final String readLineUtil(final String ln) throws IOException {
        return in.readLineUntil(ln);
    }

    /**
     * 读取所有行内容直到碰到与tagName匹配的结束标签 会移动读流指针位置，且将结束标签行也读完
     * 与带一个参数的readLineUtil不同的是，本函数的第一个参数tagName为标签名，而不是标签结束符
     *
     * @param tagName   标签名，不是标签结束符
     * @param recursive 是否递归查找，为true时同括号匹配规则，为false时找到第一个结束标签
     * @return 读取过程中的所有行组成的字符串，行间用CRLF连接
     * @throws IOException 读写异常
     */
    public final String readTagContent(String tagName, boolean recursive)
            throws IOException {
        String tagStart = "<" + tagName;
        String tagEnd = "</" + tagName + ">";
        if (!recursive) {
            return readLineUtil(tagEnd);
        }
        int tagCnt = 1;
        StringBuffer buf = new StringBuffer(32);
        String aline = this.readLine();
        while (null != aline) {
            if (aline.startsWith(tagStart)) {
                tagCnt++;
            } else if (StrYn.compareStr(tagEnd, aline)) {
                tagCnt--;
                if (tagCnt < 1) {
                    break;
                }
            }
            buf.append(aline).append(StrYn.CRLF);
            aline = readLine();
        }
        int len = buf.length();
        if (len > 1) {
            buf.delete(len - 2, len);
        }
        return buf.toString();
    }

    public final void skipLineUtil(final String ln) throws IOException {
        in.skipLineUntil(ln);
    }

    protected final String getTagEnd(String s) {
        int i = s.indexOf(' ');
        if (i >= 0) {
            return "</" + s.substring(1, i) + '>';
        } else {
            return "</" + s.substring(1);
        }
    }

    public final String getTagName(final String s) {
        if ((s == null) || (s.length() == 0) || (s.charAt(0) != '<'))
            return null;
        int i = s.indexOf(' ');
        if (i >= 0)
            return s.substring(1, i);
        else {
            // 使用readtag读出的字符串 形式有 <aaaa> <aaaa <aaaa/> ,所以以下代码有问题 update by jzp
            // 2011-11-08
            i = s.length() - (s.endsWith("/>") ? 2 : s.endsWith(">") ? 1 : 0);
            return s.substring(1, i);
            // 返回值多了一位
            // return s.substring(1, i + 1);
        }
    }

    public final String text2str(String s) {
        return StrYn.text2Str(s);
    }

    /**
     * 判断一行文本是否是指定标签
     *
     * @param s   原串，形如"<widths count=6>"
     * @param tag 标签名，形如："<widths "或"<widths>"，通常为readTag的返回值
     *            如果s为"<widths count=6>"，则第二个参数必须传"<widths "，才会返回true
     *            即s与tag必须格式对应，不能一个含有标签头属性而另一个以">"结束
     * @return boolean
     */
    public final boolean isTag(final String s, final String tag) {
        if (tag.equals(s)) return true;

        return (s != null)
                && s.startsWith(tag)
                && (s.length() > tag.length())
                && (s.charAt(tag.length()) == ' ' || s.charAt(tag.length()) == '>');
    }

    protected final boolean isTagEnd(final String tag) {
        return tag.endsWith("/>");
    }

    protected final boolean isTagEnd(final String line, final String tag) {
        return line.endsWith(tag + "/>");
    }

    /**
     * 读取一个map对象，直到遇到行tagEnd，注意此函数返回的结果是一个共享的，调用者不能长期持有这个结果
     */
    public final StringMap getContentMap(final String tagEnd)
            throws IOException {
        /**
         * BUG: 20110824
         * contentMap必须清空，否则读取当前文档时，如果contentMap里面有当前文档没有的属性值，可能会造成紊乱；
         * 这时在检查BI-5569时发现的，维的topn属性，第一个维设置了topn，读取第二个维，却没有设置topn，
         * 但是会将第一个的topn复制到第二个维；
         */
        contentMap.clear();
        return (StringMap) in.readMap(tagEnd, '\n', '=', contentMap);
    }

    /**
     * getContentMap 这个方法 返回是该对象内部的一个map， 外面使用的时候容易导致获得结果混乱
     *
     * @param tagEnd
     * @return
     * @throws IOException
     */
    public final StringMap getContentMap2(final String tagEnd)
            throws IOException {
        return (StringMap) in.readMap(tagEnd, '\n', '=', new StringMap());
    }

    public final Map getContentMap(final String tagEnd, Map map)
            throws IOException {
        return in.readMap(tagEnd, '\n', '=', map);
    }

    /**
     * 读取一行文本，如果此行文本为标签 ，那么将标签属性读取到tagAtr中
     *
     * @return
     * @throws IOException
     */
    public String readTag() throws IOException {
        tagAttr.clear();
        return in.readTag(tagAttr);
    }

    /**
     * 为了内存效率 tagAttr 共用一个对象，所以此对象不能给外部直接持有此对象 必须拷贝一份使用
     *
     * @return
     */
    public StringMap getTagMap() {
        return tagAttr;
    }

    /**
     * 根据一行 获得其中的tag 属性 如 <item a=ff b=ff />
     *
     * @param line
     * @return
     */
    public StringMap getTagMap(final String line) {
        int i = line.indexOf(' ');
        if (i > 0) {
            String attr = line.substring(i + 1, line.length()
                    - (isTagEnd(line) ? 2 : 1));
            return new StringMap(attr.trim(), " ", "=");
        }
        return null;
    }

    public void reset() {
    }
}
