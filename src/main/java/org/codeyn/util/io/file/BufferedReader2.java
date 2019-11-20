package org.codeyn.util.io.file;

import org.codeyn.util.yn.StrUtil;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * 此类负责快速的读取文本信息，支持读取npf格式文件的一些比较高效的函数
 * <p>
 * java已经有BufferedReader了，为何还需要此类呢？
 * BufferedReader不支持readLineUntil，readTag等函数，而这些函数正是高效的读取NPF格式文件的关键
 *
 * @author yk
 */
public class BufferedReader2 extends Reader {

    private static int defaultCharBufferSize = 8192;
    private static int defaultExpectedLineLength = 1024;
    private Reader in;
    private char[] cb;
    private int nChars, nextChar;
    /**
     * If the next character is a line feed, skip it
     */
    private boolean skipLF = false;
    /**
     * 在readLine时用于cache读取的内容
     */
    private StringBuffer readlsb;
    /**
     * 记录调用begin_parse时，当前buf的index值
     * 当它等于-1时表示使用readlsb标记已读取的内容
     */
    private int parse_start_index;

    /**
     * Create a buffering character-input stream that uses an input buffer of
     * the specified size.
     *
     * @param in A Reader
     * @param sz Input-buffer size
     * @throws IllegalArgumentException If sz is <= 0
     */
    public BufferedReader2(Reader in, int sz) {
        super(in);
        if (sz <= 0)
            throw new IllegalArgumentException("Buffer size <= 0");
        this.in = in;
        cb = new char[sz];
        nextChar = nChars = 0;
    }

    /**
     * Create a buffering character-input stream that uses a default-sized
     * input buffer.
     *
     * @param in A Reader
     */
    public BufferedReader2(Reader in) {
        this(in, defaultCharBufferSize);
    }

    /**
     * 判断一个字符串和一个StringBuffer或者是CharSequence的实现者的指定内容是否内容一致
     * 此函数主要是为了提高效率，避免判断过程中还要创建新的对象
     */
    public static boolean strCompare(final CharSequence s1, int s1pos, final CharSequence s2, int s2pos, int length) {
        if (s1 == s2) return true;
        int i = s1pos;
        int j = s2pos;
        int n = length;
        if (i + n <= s1.length() && j + n <= s2.length()) {
            while (n-- != 0) {
                if (s1.charAt(i++) != s2.charAt(j++))
                    return false;
            }
            return true;
        }
        return false;
    }

    public static boolean strCompare(final String s, final int spos, final char[] chars, final int offset, final int count) {
        int n = count;
        if (n <= s.length() - spos) {
            char[] v1 = chars;
            int i = offset;
            int j = spos;
            while (n-- != 0) {
                if (v1[i++] != s.charAt(j++))
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 判断字符串s中的内容是否和chars数组中指定的内容一致
     * 此函数主要是为了提高效率，避免判断过程中还要创建新的对象
     */
    public static boolean strCompare(final String s, final char[] chars, final int offset, final int count) {
        int n = count;
        if (n == s.length()) {
            char[] v1 = chars;
            int i = offset;
            int j = 0;
            while (n-- != 0) {
                if (v1[i++] != s.charAt(j++))
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Check to make sure that the stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (in == null)
            throw new IOException("Stream closed");
    }

    /**
     * Fill the input buffer, taking the mark into account if it is valid.
     */
    private void fill() throws IOException {
        int dst = 0;
        int n;
        do {
            n = in.read(cb, dst, cb.length - dst);
        }
        /**
         * modify by chenjianbo
         * 当cb.length为0时,这里会发生死循环
         */
        while ((n == 0) & (cb.length != 0));
        if (n > 0) {
            nChars = dst + n;
            nextChar = dst;
        }
    }

    /**
     * Read a single character.
     *
     * @return The character read, as an integer in the range
     * 0 to 65535 (<tt>0x00-0xffff</tt>), or -1 if the
     * end of the stream has been reached
     * @throws IOException If an I/O error occurs
     */
    public int read() throws IOException {
        ensureOpen();
        for (; ; ) {
            if (nextChar >= nChars) {
                fill();
                if (nextChar >= nChars)
                    return -1;
            }
            if (skipLF) {
                skipLF = false;
                if (cb[nextChar] == '\n') {
                    nextChar++;
                    continue;
                }
            }
            return cb[nextChar++];
        }
    }

    /**
     * Read characters into a portion of an array, reading from the underlying
     * stream if necessary.
     */
    private int read1(char[] cbuf, int off, int len) throws IOException {
        if (nextChar >= nChars) {
      /* If the requested length is at least as large as the buffer, and
         if there is no mark/reset activity, and if line feeds are not
         being skipped, do not bother to copy the characters into the
         local buffer.  In this way buffered streams will cascade
         harmlessly. */
            if (len >= cb.length && !skipLF) {
                return in.read(cbuf, off, len);
            }
            fill();
        }
        if (nextChar >= nChars)
            return -1;
        if (skipLF) {
            skipLF = false;
            if (cb[nextChar] == '\n') {
                nextChar++;
                if (nextChar >= nChars)
                    fill();
                if (nextChar >= nChars)
                    return -1;
            }
        }
        int n = Math.min(len, nChars - nextChar);
        System.arraycopy(cb, nextChar, cbuf, off, n);
        nextChar += n;
        return n;
    }

    /**
     * Read characters into a portion of an array.
     *
     * <p> This method implements the general contract of the corresponding
     * <code>{@link Reader#read(char[], int, int) read}</code> method of the
     * <code>{@link Reader}</code> class.  As an additional convenience, it
     * attempts to read as many characters as possible by repeatedly invoking
     * the <code>read</code> method of the underlying stream.  This iterated
     * <code>read</code> continues until one of the following conditions becomes
     * true: <ul>
     *
     * <li> The specified number of characters have been read,
     *
     * <li> The <code>read</code> method of the underlying stream returns
     * <code>-1</code>, indicating end-of-file, or
     *
     * <li> The <code>ready</code> method of the underlying stream
     * returns <code>false</code>, indicating that further input requests
     * would block.
     *
     * </ul> If the first <code>read</code> on the underlying stream returns
     * <code>-1</code> to indicate end-of-file then this method returns
     * <code>-1</code>.  Otherwise this method returns the number of characters
     * actually read.
     *
     * <p> Subclasses of this class are encouraged, but not required, to
     * attempt to read as many characters as possible in the same fashion.
     *
     * <p> Ordinarily this method takes characters from this stream's character
     * buffer, filling it from the underlying stream as necessary.  If,
     * however, the buffer is empty, the mark is not valid, and the requested
     * length is at least as large as the buffer, then this method will read
     * characters directly from the underlying stream into the given array.
     * Thus redundant <code>BufferedReader</code>s will not copy data
     * unnecessarily.
     *
     * @param cbuf Destination buffer
     * @param off  Offset at which to start storing characters
     * @param len  Maximum number of characters to read
     * @return The number of characters read, or -1 if the end of the
     * stream has been reached
     * @throws IOException If an I/O error occurs
     */
    public int read(char[] cbuf, int off, int len) throws IOException {

        ensureOpen();
        if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int n = read1(cbuf, off, len);
        if (n <= 0)
            return n;
        while ((n < len) && in.ready()) {
            int n1 = read1(cbuf, off + n, len - n);
            if (n1 <= 0)
                break;
            n += n1;
        }
        return n;
    }

    /**
     * 读取到byte数组中，将char强制转换为byte
     */
    public int read(byte[] buf, int off, int len) throws IOException {

        ensureOpen();
        if ((off < 0) || (off > buf.length) || (len < 0) || ((off + len) > buf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int haveread = 0;
        boolean omitLF = skipLF;

        ensureOpen();

        for (; ; ) {
            if (nextChar >= nChars)
                fill();
            if (nextChar >= nChars) { /* EOF */
                return haveread;
            }
            int i;

            /* Skip a leftover '\n', if necessary */
            if (omitLF && (cb[nextChar] == '\n'))
                nextChar++;
            skipLF = false;
            omitLF = false;

            int toChar = Math.min(nChars, nextChar + len - haveread);
            while (nextChar < toChar) {
                haveread++;
                buf[off++] = (byte) cb[nextChar++];
            }

            if (haveread >= len) {
                return haveread;
            }
        }
    }

    /**
     * Read a line of text.  A line is considered to be terminated by any one
     * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
     * followed immediately by a linefeed.
     *
     * @param ignoreLF If true, the next '\n' will be skipped
     * @return A String containing the contents of the line, not including
     * any line-termination characters, or null if the end of the
     * stream has been reached
     * @throws IOException If an I/O error occurs
     * @see java.io.LineNumberReader#readLine()
     */
    String readLine(boolean ignoreLF) throws IOException {
        boolean usesb = false;
        int startChar;
        boolean omitLF = ignoreLF || skipLF;

        ensureOpen();

        for (; ; ) {
            if (nextChar >= nChars)
                fill();
            if (nextChar >= nChars) { /* EOF */
                if (usesb && readlsb.length() > 0)
                    return readlsb.toString();
                else
                    return null;
            }
            boolean eol = false;
            char c = 0;
            int i;

            /* Skip a leftover '\n', if necessary */
            if (omitLF && (cb[nextChar] == '\n'))
                nextChar++;
            skipLF = false;
            omitLF = false;

            charLoop:
            for (i = nextChar; i < nChars; i++) {
                c = cb[i];
                if ((c == '\n') || (c == '\r')) {
                    eol = true;
                    break charLoop;
                }
            }

            startChar = nextChar;
            nextChar = i;

            if (eol) {
                String str;
                nextChar++;
                if (usesb) {
                    readlsb.append(cb, startChar, i - startChar);
                    str = readlsb.toString();
                } else {
                    str = new String(cb, startChar, i - startChar);
                }
                if (c == '\r') {
                    skipLF = true;
                }
                return str;
            }

            if (!usesb) {
                usesb = true;
                initReadLSB();
            }
            readlsb.append(cb, startChar, i - startChar);
        }
    }

    /**
     * Read a line of text.  A line is considered to be terminated by any one
     * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
     * followed immediately by a linefeed.
     *
     * @return A String containing the contents of the line, not including
     * any line-termination characters, or null if the end of the
     * stream has been reached
     * @throws IOException If an I/O error occurs
     */
    public String readLine() throws IOException {
        return readLine(false);
    }

    /**
     * 读取一个类似这样的字符串：<a v1=1 v2=2 v3=\"a b\">
     * 返回"<a"，其后的属性列表会set到map中去，遇到回车换行父后推出
     * 如果不函数有属性列表，例如：<a>，那么直接返回<a>
     */
    public String readTag(Map map) throws IOException {
        boolean usesb = false;
        int startChar;
        boolean omitLF = skipLF;

        ensureOpen();

        for (; ; ) {
            if (nextChar >= nChars)
                fill();
            if (nextChar >= nChars) { /* EOF */
                if (usesb && readlsb.length() > 0)
                    return readlsb.toString();
                else
                    return null;
            }
            boolean eol = false;
            char c = 0;
            int i;

            /* Skip a leftover '\n', if necessary */
            if (omitLF && (cb[nextChar] == '\n'))
                nextChar++;
            skipLF = false;
            omitLF = false;

            charLoop:
            for (i = nextChar; i < nChars; i++) {
                c = cb[i];
                if ((c == '\n') || (c == '\r') || (c == ' ')) {
                    eol = true;
                    break charLoop;
                }
            }

            startChar = nextChar;
            nextChar = i;

            if (eol) {
                String str;
                nextChar++;
                if (usesb) {
                    readlsb.append(cb, startChar, i - startChar);
                    str = readlsb.toString();
                } else {
                    str = new String(cb, startChar, i - startChar);
                }
                if (c == '\r') {
                    skipLF = true;
                } else if (c == ' ') {
                    readTagMap(map);
                }
                return str;
            }

            if (!usesb) {
                usesb = true;
                initReadLSB();
            }
            readlsb.append(cb, startChar, i - startChar);
        }
    }

    /**
     * 读取数据行，直到遇到指定的行until，返回读取的数据，但不包括until以及它前面的一个回车换行
     * 如果遇到eof，那么返回所有读取的数据
     * 此函数之所以写的这么麻烦，是为了避免在读取的过程中创建多余的对象，浪费内存
     */
    public String readLineUntil(final String until) throws IOException {
        boolean usesb = false;
        int startChar;
        boolean omitLF = skipLF;

        ensureOpen();

        if (nextChar >= nChars) {
            //也许此时nextChar刚好等于nChars，如果不fill一下，那么下面的bof赋值将错误
            fill();
        }

        int bol = nextChar;//记录在当前的buf中，一个正在读取的行的行起始字符，如果为-1，表示行只有一部分在buf中，另一部分在readlsb中
        int bol_sb = 0;//记录在readlsb中当前正在读取的行的行起始字符

        /*
         * 2011-4-11 标记缓存的字符数组是否刚好达到行末，到达行末时，需要重新对bol进行赋值。
         * 	尚未达到行末时，不需要重新对bol赋值，否则会无法处理字符串行有部分在buf中，部分在readlsb中的情形
         */
        boolean reachEndOfLine = false;

        for (; ; ) {
            if (nextChar >= nChars) {
                fill();
                /*
                 * 2011-4-8 ISSUE:BI-4523 计算一张表后，cpu占100%
                 * 原因：出现这种情况，是由于后台出现死循环所致。这里重新fill后，没有重新对bol赋值，导致一直将字符流
                 * 读取完都找不到参数指定的字符串行。将字符串读取完之后，readLine将返回null。导致外层调用出现了死循环，
                 * 不断地readLine都找不到匹配的字符串行。
                 */
                if (reachEndOfLine) {
                    bol = nextChar;
                    reachEndOfLine = false;
                }
            }
            if (nextChar >= nChars) { /* EOF */
                if (usesb && readlsb.length() > 0) {
                    if (strCompare(until, 0, readlsb, bol_sb, readlsb.length() - bol_sb))
                        readlsb.delete(bol_sb, readlsb.length());
                    return sb2str_skiptail_ln();
                }
                return null;
            }
            boolean eol = false;
            char c = 0;
            int i;

            /* Skip a leftover '\n', if necessary */
            if (omitLF && (cb[nextChar] == '\n')) {
                nextChar++;
                bol++;
                /*
                 * 由于nextChart加1后恰好大于或等于nChars的概率极低，绝大多数情况下没有重新fill，
                 * 所以ISSUE:BI-4523描述的情况以前没有发现。
                 */
                if (nextChar >= nChars) {
                    reachEndOfLine = true;
                    continue;//如果cb长度为1，那么此时需要fill一下
                }
            }
            skipLF = false;
            omitLF = false;

            //在当前的buf中查找需要的内容，如果能找到那就最好了，不必使用readlsb了
            charLoop:
            for (i = nextChar; i < nChars; i++) {
                c = cb[i];
                if ((c == '\n') || (c == '\r')) {
                    if (bol > -1) {//这一行的内容都在buf中
                        if (strCompare(until, cb, bol, i - bol)) {
                            eol = true;
                            break charLoop;
                        }
                    } else {//这行有部分在buf中，部分在readlsb中
                        if (readlsb.length() - bol_sb + i == until.length() &&
                                strCompare(until, 0, readlsb, bol_sb, readlsb.length() - bol_sb) &&
                                strCompare(until, readlsb.length() - bol_sb, cb, 0, i)) {
                            eol = true;
                            break charLoop;
                        }
                    }
                    bol = i + 1;
                }
            }

            startChar = nextChar;
            nextChar = i;

            if (eol) {//找到了需要找到的行，结束遍历
                String str;
                nextChar++;
                if (usesb) {
                    if (bol > -1) {//要找的那一行的内容都在buf中
                        readlsb.append(cb, startChar, bol - startChar);
                    } else {//要找的那一行有部分在buf中，部分在readlsb中
                        readlsb.delete(bol_sb, readlsb.length());
                    }
                    str = sb2str_skiptail_ln();
                } else {
                    int ee = bol - 1;
                    if (bol > startChar && ee >= 0 && cb[ee] == '\n') {
                        bol--;
                        ee--;
                    }
                    if (bol > startChar && ee >= 0 && cb[ee] == '\r') {
                        bol--;
                    }
                    str = new String(cb, startChar, bol - startChar);
                }
                if (c == '\r') {
                    skipLF = true;
                }
                return str;
            }

            if (!usesb) {
                usesb = true;
                initReadLSB();
                bol_sb = i - bol - 1;
            }

            if (bol > -1) {
                bol_sb = readlsb.length() + bol - startChar;
            }
            readlsb.append(cb, startChar, i - startChar);
            bol = -1;//在当前的buf中没有找到需要的行，bof已经无效了，置为-1，等待下次赋值
        }
    }

    /**
     * 返回readlsb中的字符串，忽略最后的回车换行符
     */
    private String sb2str_skiptail_ln() {
        int readlsbl = readlsb.length();
        if (readlsbl > 0 && readlsb.charAt(readlsbl - 1) == '\n') readlsb.deleteCharAt(readlsbl - 1);
        readlsbl = readlsb.length();
        if (readlsbl > 0 && readlsb.charAt(readlsbl - 1) == '\r') readlsb.deleteCharAt(readlsbl - 1);
        return readlsb.toString();
    }

    public void skipLineUntil(String until) throws IOException {
        readLineUntil(until);//TODO 待优化
    }

    private void begin_parse() throws IOException {
        if (readlsb != null) readlsb.setLength(0);

        if (nextChar >= nChars)
            fill();

        /* Skip a leftover '\n', if necessary */
        if (skipLF && nextChar < nChars && (cb[nextChar] == '\n')) {
            nextChar++;
            if (nextChar >= nChars)
                fill();
        }

        parse_start_index = nextChar;

        skipLF = false;
    }

    private int parse_a_char() throws IOException {
        if (nextChar >= nChars) {
            fill();
        }
        if (nextChar >= nChars) { /* EOF */
            return -1;//如果readlsb有内容，那么读取的内容就在readlsb中
        }
        char c = cb[nextChar++];
        if (nextChar >= nChars || (parse_start_index == -1)) {
            if (readlsb == null) {
                initReadLSB();
            }
            readlsb.append(c);
            parse_start_index = -1;
        }
        if (c == '\r') {
            skipLF = true;
        }
        return c;
    }

    /**
     * 读取内容，直到遇到enchar，并返回读取的内容，但不包含endchar
     */
    private int parse_chars(final char endchar, final boolean stopOnLF) throws IOException {
        int startChar;

        for (; ; ) {
            if (nextChar >= nChars) {
                fill();
            }
            if (nextChar >= nChars) { /* EOF */
                return -1;//如果readlsb有内容，那么读取的内容就在readlsb中
            }
            boolean eol = false;
            char c = 0;
            int i;

            charLoop:
            for (i = nextChar; i < nChars; i++) {
                c = cb[i];
                if ((c == endchar) || (stopOnLF && ((c == '\n') || (c == '\r')))) {
                    eol = true;
                    break charLoop;
                }
            }

            startChar = parse_start_index > -1 ? parse_start_index : nextChar;
            nextChar = i;

            if (eol) {
                nextChar++;
                if (parse_start_index == -1) {
                    readlsb.append(cb, startChar, i - startChar);
                }
                if (c == '\r') {
                    skipLF = true;
                }
                //如果readlsb有内容，那么读取的内容就在readlsb中，否则内容都在buf中，截止到nextChar-1
                return c;
            }

            if (readlsb == null) {
                initReadLSB();
            }
            readlsb.append(cb, startChar, i - startChar);
            parse_start_index = -1;
        }
    }

    private void initReadLSB() {
        if (readlsb == null) {
            readlsb = new StringBuffer(defaultExpectedLineLength);
        } else {
            readlsb.setLength(0);
        }
    }

    /**
     * 结果用end_parse函数获取
     */
    private int parse_quated_str() throws IOException {
        initReadLSB();//清空sb buf
        begin_parse();
        parse_start_index = -1;//总是使用readlsb
        int startChar = nextChar;
        int c;
        for (; ; ) {
            c = parse_chars('"', false);
            if (c == '"') {
                c = parse_a_char();
                if (c != '"') {
                    /**
                     * 最后总是多读取了一个字符，可能是-1表示到了文件结尾，也可能是回车换行等分隔字符，
                     * 这样做的前提是名字对后面一定有一个分隔字符
                     */
                    if (readlsb.length() > 0 && c != -1)
                        readlsb.deleteCharAt(readlsb.length() - 1);
                    return c;
                }
            } else if (c == -1) {
                return c;
            }
        }
    }

    /**
     * 忽略空白字符，和指定的一个skipchar，如果skipchar是\r或\n，那么\r和\n都被忽略
     */
    private void parse_skip_blank(char skipchar) throws IOException {
        for (; ; ) {
            if (nextChar >= nChars)
                fill();
            if (nextChar >= nChars) /* EOF */
                break;
            char c = cb[nextChar];
            if (c == ' ' || c == '\t' || c == skipchar || ((skipchar == '\r') && (c == '\n'))) {
                nextChar++;
            } else {
                break;
            }
        }
        skipLF = false;
    }

    /**
     * 从begin_parse开始到现在，所读取过的内容，是否和s相等
     */
    private boolean parse_content_equals(final String s) {
        if (parse_start_index == -1) {
            return StrUtil.strCompare(s, readlsb);
        }

        return strCompare(s, 0, cb, parse_start_index, s.length());
    }

    public String end_parse() {
        if (parse_start_index > -1) {
            int len = nextChar - 1 - parse_start_index;
            if (len == 1) {
                char c = cb[parse_start_index];
                if (c < 127)
                    return StrUtil.strofascii(c);
            }
            return new String(cb, parse_start_index, len);
        }

        if (readlsb != null && readlsb.length() > 0) {
            return readlsb.toString();
        }

        //例如解析a=""时，readlsb.length()==0，parse_start_index==-1
        return "";
    }

    /**
     * 不要值最后的>
     */
    public String end_parse_tagmap_value() {
        if (parse_start_index > -1) {
            int endindex = nextChar >= nChars ? nChars - 1 : nextChar - 1;
            if (cb[endindex] == '\r') endindex--;
            if (cb[endindex] == '\n') endindex--;
            if (cb[endindex] == '>') endindex--;
            int len = endindex + 1 - parse_start_index;
            if (len == 1) {
                char c = cb[parse_start_index];
                if (c < 127)
                    return StrUtil.strofascii(c);
            }
            return new String(cb, parse_start_index, len);
        }
        if (readlsb != null && readlsb.length() > 0) {
            if (readlsb.charAt(readlsb.length() - 1) == '\r') {
                readlsb.deleteCharAt(readlsb.length() - 1);
            }
            if (readlsb.charAt(readlsb.length() - 1) == '\n') {
                readlsb.deleteCharAt(readlsb.length() - 1);
            }
            if (readlsb.charAt(readlsb.length() - 1) == '>') {
                readlsb.deleteCharAt(readlsb.length() - 1);
            }
            return readlsb.toString();
        }
        return "";
    }

    public Map readTagMap(Map map) throws IOException {
        for (; ; ) {
            parse_skip_blank(' ');
            begin_parse();
            int c = parse_chars('=', true);
            if (c == '=') {
                String key = end_parse();
                begin_parse();
                c = parse_a_char();
                if (c == '"') {
                    c = parse_quated_str();
                    map.put(key, end_parse());
                } else if ((c == '\n') || (c == '\r') || (c == '>') || (c == ' ')) {
                    //map.put(key, "");节省内存不读出空值
                } else {
                    c = parse_chars(' ', true);
                    if ((c == '\n') || (c == '\r')) {
                        map.put(key, end_parse_tagmap_value());
                        return map;//tagmap 暂时只支持在一行的，遇到回车换行就结束读取
                    }
                    map.put(key, end_parse());
                }
            } else if ((c == '\n') || (c == '\r')) {
                return map;
            } else {
                return map;//可能遇到eof了
            }
        }
    }

    /**
     * 读取一个map信息，直到遇到until，如果until为null，那么一直读取到eof
     */
    public Map readMap(String until, char separator, char equal, Map map) throws IOException {
        for (; ; ) {
            parse_skip_blank(separator);
            begin_parse();
            int c = parse_chars(equal, true);
            if (c == equal) {
                String key = end_parse();
                begin_parse();
                c = parse_a_char();
                if (c == '"') {
                    parse_quated_str();
                    map.put(key, end_parse());
                } else if ((c == '\n') || (c == '\r')) {
                    //map.put(key, "");节省内存不读出空值
                } else {
                    parse_chars(separator, true);
                    map.put(key, end_parse());
                }
            } else if ((c == '\n') || (c == '\r')) {
                if (until != null && parse_content_equals(until)) {
                    return map;
                }
            } else {
                return map;//可能遇到eof了
            }
        }
    }

    public Map getContentMap(final String tagEnd) throws IOException {
        return readMap(tagEnd, '\n', '=', new HashMap());
    }

    /**
     * Skip characters.
     *
     * @param n The number of characters to skip
     * @return The number of characters actually skipped
     * @throws IllegalArgumentException If <code>n</code> is negative.
     * @throws IOException              If an I/O error occurs
     */
    public long skip(long n) throws IOException {
        if (n < 0L) {
            throw new IllegalArgumentException("skip value is negative");
        }

        ensureOpen();
        long r = n;
        while (r > 0) {
            if (nextChar >= nChars)
                fill();
            if (nextChar >= nChars) /* EOF */
                break;
            if (skipLF) {
                skipLF = false;
                if (cb[nextChar] == '\n') {
                    nextChar++;
                }
            }
            long d = nChars - nextChar;
            if (r <= d) {
                nextChar += r;
                r = 0;
                break;
            } else {
                r -= d;
                nextChar = nChars;
            }
        }
        return n - r;
    }

    /**
     * Tell whether this stream is ready to be read.  A buffered character
     * stream is ready if the buffer is not empty, or if the underlying
     * character stream is ready.
     *
     * @throws IOException If an I/O error occurs
     */
    public boolean ready() throws IOException {

        ensureOpen();

        /*
         * If newline needs to be skipped and the next char to be read
         * is a newline character, then just skip it right away.
         */
        if (skipLF) {
            /* Note that in.ready() will return true if and only if the next
             * read on the stream will not block.
             */
            if (nextChar >= nChars && in.ready()) {
                fill();
            }
            if (nextChar < nChars) {
                if (cb[nextChar] == '\n')
                    nextChar++;
                skipLF = false;
            }
        }

        if (nextChar < nChars)
            return true;

        fill();
        //此处不能使用in的ready来判断，因为StringReader的ready方法总返回true
        return (nextChar < nChars);
    }

    /**
     * Tell whether this stream supports the mark() operation, which it does.
     */
    public boolean markSupported() {
        return false;
    }

    public void mark(int readAheadLimit) throws IOException {
        throw new UnsupportedOperationException("mark");
    }

    public void reset() throws IOException {
        throw new UnsupportedOperationException("mark");
    }

    /**
     * Close the stream.
     *
     * @throws IOException If an I/O error occurs
     */
    public void close() throws IOException {

        if (in == null)
            return;
        in.close();
        in = null;
        cb = null;
    }

}
