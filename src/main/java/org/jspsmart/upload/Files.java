package org.jspsmart.upload;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

public class Files {

    private Hashtable<Integer, File> files = new Hashtable<Integer, File>();
    private int count;

    protected void addFile(File paramFile) {
        if (paramFile == null) {
            throw new IllegalArgumentException("newFile cannot be null");
        }
        files.put(count, paramFile);
        count += 1;
    }

    public File getFile(int paramInt) {
        if (paramInt < 0) {
            throw new IllegalArgumentException("File's index cannot be a negative value");
        }
        File file = files.get(paramInt);
        if (file == null) {
            throw new IllegalArgumentException("Files' name is invalid or does not exist");
        }
        return file;
    }

    public int getCount() {
        return count;
    }

    public long getSize() throws IOException {
        long l = 0L;
        for (int i = 0; i < this.count; i++) {
            l += getFile(i).getSize();
        }
        return l;
    }

    public Collection<File> getCollection() {
        return files.values();
    }

    public Enumeration<File> getEnumeration() {
        return files.elements();
    }
}
