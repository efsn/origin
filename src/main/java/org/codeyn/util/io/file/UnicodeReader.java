package org.codeyn.util.io.file;

import java.io.*;

/**
 * Since Win2000, notepad store utf8 file will add BOM(Byte Order Mark, U+FEFF), due to utf8 and ASCII is compatible.
 * Avoid forgetting what save with, lead to utf8 use ASCII come about messy code, so add BOM for different. However,
 * when read utf8 may have BOM(U+FEFF) lead to messy code.
 * <p>When read utf8 may use three byte such as "EF BB BF" to mark, of course could not. Maybe read three byte due to the problem.
 * This is the jdk bug
 * Bug ID:4508058 http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4508058
 * Since jdk1.6 solved read BOM fail, unable to distinguish whether contain BOM which will not solve.
 * Solve whether contain BOM by appliction program
 * <p>Original pseudocode   : Thomas Weidenfeller
 * Implementation tweaked: Aki Nieminen
 * http://www.unicode.org/unicode/faq/utf_bom.html
 * <pre>BOMs:
 * 00 00 FE FF    = UTF-32, big-endian
 * FF FE 00 00    = UTF-32, little-endian
 * FE FF          = UTF-16, big-endian
 * FF FE          = UTF-16, little-endian
 * EF BB BF       = UTF-8</pre>
 * <p>Win2k Notepad:
 * Unicode format = UTF-16LE
 * <p>Generic unicode text reader, which will use BOM mark to identify the encoding
 * to be used.
 *
 * @author Codeyn
 * @version 1.0
 */
public class UnicodeReader extends Reader {

    private static final int BOM_SIZE = 4;
    private PushbackInputStream pbIn;
    private InputStreamReader reader = null;
    private String defaultEncoding;

    /**
     * Default encoding is used only if BOM is not found. If default encoding is
     * <code>NULL</code> then system default is used.
     */
    public UnicodeReader(InputStream in, String defaultEncding) {
        pbIn = new PushbackInputStream(in, BOM_SIZE);
        this.defaultEncoding = defaultEncding;
    }

    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    public String getEncoding() {
        if (reader == null) return null;
        return reader.getEncoding();
    }

    /**
     * Read-ahead four bytes and check for BOM marks. Extra bytes are unread
     * back to the stream, only BOM bytes are skipped.
     */
    protected void init() throws IOException {
        if (reader != null) return;
        String encoding;
        byte[] bom = new byte[BOM_SIZE];
        int n, unread;
        n = pbIn.read(bom, 0, bom.length);
        if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF)) {
            encoding = "UTF-8";
            unread = n - 3;
        } else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
            encoding = "UTF-16BE";
            unread = n - 2;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
            encoding = "UTF-16LE";
            unread = n - 2;
        } else if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00)
                && (bom[2] == (byte) 0xFE) && (bom[3] == (byte) 0xFF)) {
            encoding = "UTF-32BE";
            unread = n - 4;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)
                && (bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00)) {
            encoding = "UTF-32LE";
            unread = n - 4;
        } else {
            // Unicode BOM mark not found, unread all bytes
            encoding = defaultEncoding;
            unread = n;
        }
        if (unread > 0)
            pbIn.unread(bom, (n - unread), unread);
        else if (unread < -1) pbIn.unread(bom, 0, 0);

        // Use given encoding
        if (encoding == null) {
            reader = new InputStreamReader(pbIn);
        } else {
            reader = new InputStreamReader(pbIn, encoding);
        }
    }

    public void close() throws IOException {
        init();
        reader.close();
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        init();
        return reader.read(cbuf, off, len);
    }

}
