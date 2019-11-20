package org.codeyn.util.progress;

import org.codeyn.util.GUID;
import org.codeyn.util.UnionInputStream;
import org.codeyn.util.exception.ExceptionHandler;
import org.codeyn.util.file.FileUtil;
import org.codeyn.util.i18n.I18N;
import org.codeyn.util.yn.StrUtil;

import java.io.*;
import java.util.ArrayList;

public class ProgressDefaultFile extends ProgressDefault {
    private static final String NEWLINE = "\r\n";
    private File dir;
    private File pgfile;
    private ArrayList list;
    private RandomAccessFile raf;
    private int maxsize = 20;
    private boolean printLog;

    public ProgressDefaultFile(String nm, File dir) {
        super(nm);
        this.dir = dir;
        try {
            FileUtil.ensureExists(dir, true, true);
            String filepath = FileUtil.createTempFile(dir.getAbsolutePath(),
                    "progress_" + (new GUID().toString()) + ".txt", false, true);
            if (filepath == null)
                //throw new RuntimeException("创建日志文件没有成功!");
                throw new RuntimeException(I18N.getString("com.esen.util.progressdefaultfile.exp", "创建日志文件没有成功!"));
            pgfile = new File(filepath);
            raf = new RandomAccessFile(pgfile, "rw");
        } catch (Throwable e) {
            ExceptionHandler.rethrowRuntimeException(e);
        }
        list = new ArrayList(maxsize);
    }

    public void setPrintLog(boolean printLog) {
        this.printLog = printLog;
    }

    public synchronized void addLog(String log) {
        list.add(log);
        int size = list.size();
        for (int i = 0; i < size - maxsize; i++) {
            String str = (String) list.remove(0);
            write(str);
        }
        if (printLog) {
            System.out.println(log);
        }
    }

    public synchronized String getLastLog() {
        int size = list.size();
        if (size == 0)
            return null;
        return (String) list.get(size - 1);
    }

    public synchronized void setLastLog(String log) {
        int size = list.size();
        if (size > 0) {
            list.set(size - 1, log);
        } else {
            list.add(log);
        }
        if (printLog) {
            System.out.println(log);
        }
    }

    public synchronized String getLogs() {
        int size = list.size();
        if (size == 0)
            return null;
        StringBuffer sb = new StringBuffer(size * 40);
        for (int i = 0; i < size; i++) {
            sb.append((String) list.get(i));
            sb.append(NEWLINE);
        }
        return sb.toString();
    }

    public synchronized int getLogCount() {
        return list.size();
    }

    public synchronized String getLog(int i) {
        return (String) list.get(i);
    }

    public synchronized InputStream getLogStm() {
        ProgressFileInputStream filein = null;
        InputStream login = null;
        try {
            filein = new ProgressFileInputStream(this.pgfile);
            filein.setLength(raf.length());
            login = this.getCacheLogStm();
            return new UnionInputStream(new InputStream[]{
                    filein, login});
        } catch (Throwable e) {
            try {
                if (filein != null)
                    filein.close();
            } catch (Exception e1) {
            }
            ExceptionHandler.rethrowRuntimeException(e);
        }
        return null;
    }

    private InputStream getCacheLogStm() {
        String logs = this.getLogs();
        if (StrUtil.isNull(logs))
            return null;
        try {
            //TODO 2.1修改为Reader和Writer
            return new ByteArrayInputStream(logs.getBytes(StrUtil.UTF8));
        } catch (UnsupportedEncodingException e) {
            ExceptionHandler.rethrowRuntimeException(e);
        }
        return null;
    }

    private synchronized void close() {
        try {
            raf.close();
        } catch (Throwable e) {
            ExceptionHandler.rethrowRuntimeException(e);
        }
    }

    private void write(String log) {
        write(log, true);
    }

    private void writeNewLine() throws IOException {
        raf.write(NEWLINE.getBytes());
    }

    private void write(String log, boolean newline) {
        try {
            if (!StrUtil.isNull(log)) {
                raf.write(log.getBytes());
            }
            if (newline) {
                writeNewLine();
            }
        } catch (Throwable e) {
            ExceptionHandler.rethrowRuntimeException(e);
        }
    }

    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}

class ProgressFileInputStream extends FileInputStream {
    private long length;
    private long pos;

    public ProgressFileInputStream(File file) throws FileNotFoundException {
        super(file);
    }

    public void setLength(long length) {
        this.length = length;
    }

    public synchronized int read() throws IOException {
        if (pos < length) {
            pos++;
            return super.read();
        }
        return -1;
    }

    public int read(byte[] b) throws IOException {
        return readBytes(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return readBytes(b, off, len);
    }

    private synchronized int readBytes(byte[] b, int off, int len)
            throws IOException {
        if (pos < length) {
            long count = length - pos;
            if (len > count)
                len = (int) count;
            pos += len;
            return super.read(b, off, len);
        }
        return -1;
    }

    public synchronized long skip(long n) throws IOException {
        if (pos < length) {
            long count = length - pos;
            if (n > count)
                n = count;
            long len = super.skip(n);
            pos += len;
            return len;
        }
        return 0;
    }
}
