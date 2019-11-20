package org.codeyn.util;

import java.io.*;

/**
 * jdk已经有类似的类了ByteArrayOutputStream，此类和jdk提供的类功能大致相同，
 * 但她不是同步的，并且提供了getbuf,ensureCapacity和tryCopyFrom等方法
 * 这可以在某些情况下避免多次复制内存块，提高效率
 *
 * @author yk
 */

public final class YnByteArrayOutStream extends OutputStream {

    /**
     * The buffer where data is stored.
     */
    protected byte[] buf;

    /**
     * The number of valid bytes in the buffer.
     */
    protected int count;

    /**
     * Creates a new byte array output stream. The buffer capacity is
     * initially 32 bytes, though its size increases if necessary.
     */
    public YnByteArrayOutStream() {
        this(32);
    }

    /**
     * Creates a new byte array output stream, with a buffer capacity of
     * the specified size, in bytes.
     *
     * @param size the initial size.
     * @throws IllegalArgumentException if size is negative.
     */
    public YnByteArrayOutStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
        buf = new byte[size];
    }

    /**
     * Writes the specified byte to this byte array output stream.
     *
     * @param b the byte to be written.
     */
    public void write(int b) {
        int newcount = count + 1;
        ensureCapacity(newcount);
        buf[count] = (byte) b;
        count = newcount;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this byte array output stream.
     *
     * @param b   the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     */
    public void write(byte[] b, int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length)
                || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }

        int newcount = count + len;
        ensureCapacity(newcount);

        System.arraycopy(b, off, buf, count, len);
        count = newcount;
    }

    /**
     * Writes the complete contents of this byte array output stream to
     * the specified output stream argument, as if by calling the output
     * stream's write method using <code>out.write(buf, 0, count)</code>.
     *
     * @param out the output stream to which to write the data.
     * @throws IOException if an I/O error occurs.
     */
    public void writeTo(OutputStream out) throws IOException {
        out.write(buf, 0, count);
    }

    /**
     * Resets the <code>count</code> field of this byte array output
     * stream to zero, so that all currently accumulated output in the
     * output stream is discarded. The output stream can be used again,
     * reusing the already allocated buffer space.
     *
     * @see java.io.ByteArrayInputStream#count
     */
    public void reset() {
        count = 0;
    }

    /**
     * Creates a newly allocated byte array. Its size is the current
     * size of this output stream and the valid contents of the buffer
     * have been copied into it.
     *
     * @return the current contents of this output stream, as a byte array.
     * @see java.io.ByteArrayOutputStream#size()
     */
    public byte[] toByteArray() {
        byte[] newbuf = new byte[count];
        System.arraycopy(buf, 0, newbuf, 0, count);
        return newbuf;
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return the value of the <code>count</code> field, which is the number
     * of valid bytes in this output stream.
     * @see java.io.ByteArrayOutputStream#count
     */
    public int size() {
        return count;
    }

    /**
     * Converts the buffer's contents into a string, translating bytes into
     * characters according to the platform's default character encoding.
     *
     * @return String translated from the buffer's contents.
     * @since JDK1.1
     */
    public String toString() {
        return new String(buf, 0, count);
    }

    /**
     * Converts the buffer's contents into a string, translating bytes into
     * characters according to the specified character encoding.
     *
     * @param enc a character-encoding name.
     * @return String translated from the buffer's contents.
     * @throws UnsupportedEncodingException If the named encoding is not supported.
     * @since JDK1.1
     */
    public String toString(String enc) throws UnsupportedEncodingException {
        return new String(buf, 0, count, enc);
    }

    /**
     * Closing a <tt>ByteArrayOutputStream</tt> has no effect. The methods in
     * this class can be called after the stream has been closed without
     * generating an <tt>IOException</tt>.
     * <p>
     */
    public void close() throws IOException {
    }

    public void ensureCapacity(int newcapacity) {
        if (newcapacity > buf.length) {
            byte[] newbuf = new byte[Math.max(buf.length << 1, newcapacity)];
            System.arraycopy(buf, 0, newbuf, 0, count);
            buf = newbuf;
        }
    }

    /**
     * 确保此对象的buf不占用太多的内存
     */
    public void ensureMaxCapacity(int maxcapacity) {
        if (buf.length > maxcapacity && this.count < maxcapacity) {
            byte[] newbuf = new byte[maxcapacity];
            System.arraycopy(buf, 0, newbuf, 0, count);
            buf = newbuf;
        }
    }

    public byte[] getBuf() {
        return buf;
    }

    public final int tryCopyFrom(InputStream in)
            throws IOException {
        try {
            ensureCapacity(in.available());
        } catch (Exception ex) {
        }
        int oldcount = count;
        int r;
        while ((r = in.read(buf, count, buf.length - count)) != -1) {
            count += r;
            if (buf.length - count < 1) {
                ensureCapacity(count + 10 * 1024);
            }
        }
        return count - oldcount;
    }

    public final InputStream asInputStream() {
        return new ByteArrayInputStream(buf, 0, count);
    }

    public void trimToSize() {
        byte[] buf = new byte[count];
        System.arraycopy(this.buf, 0, buf, 0, count);
        this.buf = buf;
    }

}