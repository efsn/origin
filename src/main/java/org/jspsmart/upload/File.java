package org.jspsmart.upload;

import org.codeyn.util.yn.StrUtil;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;

public class File {

    private SmartUpload sUpload;
    private int startData;
    private int endData;
    private int size;
    private String fieldName;
    private String filenNme;
    private String fileExt;
    private String filePathName;
    private String contentType;
    private String contentDisp;
    private String typeMime;
    private String subTypeMime;
    //    private String contentString;
    private boolean isMissing = true;

    public void saveAs(String path) throws Exception {
        saveAs(path, 0);
    }

    public void saveAs(String path, int offset) throws Exception {
        String str = sUpload.getPhysicalPath(path, offset);
        if (str == null) {
            throw new IllegalArgumentException("There is no specified destination file");
        }
        FileOutputStream out = null;
        try {
            java.io.File file = new java.io.File(str);
            out = new FileOutputStream(file);
            out.write(sUpload.getBinaryArray(), startData, size);
        } catch (IOException localIOException) {
            throw new SmartUploadException("File can't be saved");
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public void fileToField(ResultSet rs, String paramString) throws Exception {
        int i = 0x10000;
        int k = startData;
        if (rs == null)
            throw new IllegalArgumentException("The resultSet cannot be null");
        if (StrUtil.isNull(paramString))
            throw new IllegalArgumentException("The columnName cannot be null or empty");
        long l = BigInteger.valueOf(size).divide(BigInteger.valueOf(i)).longValue();
        int j = BigInteger.valueOf(size).mod(BigInteger.valueOf(i)).intValue();
        try {
            for (int m = 1; m < l; m++) {
                rs.updateBinaryStream(paramString, new ByteArrayInputStream(sUpload.getBinaryArray(), k, i), i);
                k = k == 0 ? 1 : k;
                k = m * i + startData;
            }
            if (j > 0) {
                rs.updateBinaryStream(paramString, new ByteArrayInputStream(sUpload.getBinaryArray(), k, j), j);
            }
        } catch (SQLException localSQLException) {
            byte[] arrayOfByte = new byte[size];
            System.arraycopy(sUpload.getBinaryArray(), startData, arrayOfByte, 0, size);
            rs.updateBytes(paramString, arrayOfByte);
        } catch (Exception localException) {
            throw new SmartUploadException("Unable to save file in the DataBase");
        }
    }

    public boolean isMissing() {
        return isMissing;
    }

    public String getFieldName() {
        return fieldName;
    }

    protected void setFieldName(String paramString) {
        fieldName = paramString;
    }

    public String getFileName() {
        return filenNme;
    }

    protected void setFileName(String paramString) {
        filenNme = paramString;
    }

    public String getFilePathName() {
        return filePathName;
    }

    protected void setFilePathName(String paramString) {
        filePathName = paramString;
    }

    public String getFileExt() {
        return fileExt;
    }

    protected void setFileExt(String paramString) {
        fileExt = paramString;
    }

    public String getContentType() {
        return contentType;
    }

    protected void setContentType(String paramString) {
        contentType = paramString;
    }

    public String getContentDisp() {
        return contentDisp;
    }

    protected void setContentDisp(String paramString) {
        contentDisp = paramString;
    }

    public String getContentString() {
        return new String(sUpload.getBinaryArray(), startData, size);
    }

    public String getTypeMIME() throws IOException {
        return typeMime;
    }

    protected void setTypeMIME(String paramString) {
        typeMime = paramString;
    }

    public String getSubTypeMIME() {
        return subTypeMime;
    }

    protected void setSubTypeMIME(String paramString) {
        subTypeMime = paramString;
    }

    public int getSize() {
        return size;
    }

    protected void setSize(int paramInt) {
        size = paramInt;
    }

    protected int getStartData() {
        return startData;
    }

    protected void setStartData(int paramInt) {
        startData = paramInt;
    }

    protected int getEndData() {
        return endData;
    }

    protected void setEndData(int paramInt) {
        endData = paramInt;
    }

    protected void setParent(SmartUpload paramSmartUpload) {
        sUpload = paramSmartUpload;
    }

    protected void setIsMissing(boolean paramBoolean) {
        isMissing = paramBoolean;
    }

    public byte getBinaryData(int paramInt) {
        if (startData + paramInt > endData) {
            throw new ArrayIndexOutOfBoundsException("Index Out of range");
        }
        if (startData + paramInt <= endData)
            return sUpload.getBinaryArray()[(startData + paramInt)];
        return 0;
    }
}
