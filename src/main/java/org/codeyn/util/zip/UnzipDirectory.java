package org.codeyn.util.zip;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;

public class UnzipDirectory {

    /*文件过滤器。满足过滤器的文件，不对其进行解压*/
    private FileFilter fileFilter;

    public UnzipDirectory() {
        this.fileFilter = null;
    }

    /**
     * 解压zip文件，如果包括子目录，同时也解压子目录结构和内容。
     *
     * @throws Exception
     */
    public void unzip(ZipInputStream zipInput, File destDir) throws Exception {
        ZipEntry entry = null;
        while ((entry = zipInput.getNextEntry()) != null) {
            String entryName = entry.getName();
            File newFile = new File(destDir, entryName);
            // 符合过滤器的文件将不进行解压
            if (newFile.isFile() && this.fileFilter != null && this.fileFilter.accept(newFile))
                continue;
            if (entry.isDirectory() || entryName.endsWith(File.separator)) {
                if (!newFile.exists())
                    newFile.mkdirs();
            } else if (newFile.isDirectory()) {
                if (!newFile.exists())
                    newFile.mkdirs();
            } else {
                if (!newFile.getParentFile().exists())
                    newFile.getParentFile().mkdirs();
                byte[] zipData = new byte[2048];
                FileOutputStream output = new FileOutputStream(newFile);
                try {
                    int len = 0;
                    while ((len = zipInput.read(zipData)) != -1)
                        output.write(zipData, 0, len);
                } finally {
                    output.close();
                }
            }
            zipInput.closeEntry();
        }

    }

    public final FileFilter getFileFilter() {
        return fileFilter;
    }

    public final void setFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

}
