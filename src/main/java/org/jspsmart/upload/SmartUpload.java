package org.jspsmart.upload;

import org.codeyn.util.yn.StrUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.util.Vector;

public class SmartUpload {

    protected byte[] binaryArray;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected ServletContext application;

    private int totalBytes;
    private int currentIndex;
    private int startData;
    private int endData;
    private long totalMaxFileSize;
    private long maxFileSize;
    private String boundary;
    private Vector<String> deniedFilesList;
    private Vector<String> allowedFilesList;
    private boolean denyPhysicalPath;
    private boolean forcePhysicalPath;
    private String contentDisposition;
    private Files files;

    private Request formRequest = new Request();

    public final void initialize(ServletConfig paramServletConfig,
                                 HttpServletRequest paramHttpServletRequest,
                                 HttpServletResponse paramHttpServletResponse)
            throws ServletException {
        application = paramServletConfig.getServletContext();
        request = paramHttpServletRequest;
        response = paramHttpServletResponse;
    }

    public final void initialize(PageContext paramPageContext) throws ServletException {
        application = paramPageContext.getServletContext();
        request = ((HttpServletRequest) paramPageContext.getRequest());
        response = ((HttpServletResponse) paramPageContext.getResponse());
    }

    public void upload() throws Exception {
        int i = 0, j = 0, k = 0;
        long l = 0L;
        totalBytes = request.getContentLength();
        binaryArray = new byte[totalBytes];
        while (i < totalBytes) {
            try {
                request.getInputStream();
                j = request.getInputStream().read(binaryArray, i, totalBytes - i);
            } catch (Exception localException) {
                throw new SmartUploadException("Unable to upload");
            }
            i += j;
        }
        while ((k == 0) && (currentIndex < totalBytes)) {
            if (binaryArray[currentIndex] == 13)
                k = 1;
            else
                boundary += (char) binaryArray[currentIndex];
            currentIndex += 1;
        }
        if (currentIndex == 1) {
            return;
        }
        currentIndex += 1;
        while (currentIndex < totalBytes) {
            String head = this.getDataHeader();
            currentIndex += 2;
            int m = head.indexOf("filename") > 0 ? 1 : 0;
            String fieldName = this.getDataFieldValue(head, "name");
            if (m != 0) {
                String path = this.getDataFieldValue(head, "filename");
                String fileName = this.getFileName(path);
                String fileExt = this.getFileExt(fileName);
                String type = this.getContentType(head);
                String disp = this.getContentDisp(head);
                String mime = this.getTypeMIME(type);
                String subTypeMemi = this.getSubTypeMIME(type);
                this.getDataSection();
                if (fileName.length() > 0) {
                    if (deniedFilesList.contains(fileExt) == true) {
                        throw new SecurityException("The extension of the file is denied to be uploaded");
                    }
                    if ((!allowedFilesList.isEmpty()) && (!allowedFilesList.contains(fileExt))) {
                        throw new SecurityException("The extension of the file is not allowed to be uploaded");
                    }
                    if ((maxFileSize > 0L) && (endData - startData + 1 > maxFileSize)) {
                        throw new SecurityException("Size exceeded for this file : " + fileName);
                    }
                    l += endData - startData + 1;
                    if ((totalMaxFileSize > 0L) && (l > totalMaxFileSize)) {
                        throw new SecurityException("Total File Size exceeded");
                    }
                }
                File file = new File();
                file.setParent(this);
                file.setFieldName(fieldName);
                file.setFileName(fileName);
                file.setFileExt(fileExt);
                file.setFilePathName(path);
                file.setIsMissing(path.length() == 0);
                file.setContentType(type);
                file.setContentDisp(disp);
                file.setTypeMIME(mime);
                file.setSubTypeMIME(subTypeMemi);
                if (type.indexOf("application/x-macbinary") > 0) {
                    startData += 128;
                }
                file.setSize(endData - startData + 1);
                file.setStartData(startData);
                file.setEndData(endData);
                files.addFile(file);
            } else {
                formRequest.putParameter(fieldName, new String(binaryArray, startData, endData - startData + 1));
            }
            if ((char) binaryArray[(currentIndex + 1)] == '-') {
                break;
            }
            currentIndex += 2;
        }
    }

    public int save(String paramString) throws Exception {
        return save(paramString, 0);
    }

    public int save(String paramString, int paramInt) throws Exception {
        int i = 0;

        if (paramString == null) {
            paramString = application.getRealPath("/");
        }

        if (paramString.indexOf("/") != -1) {
            if (paramString.charAt(paramString.length() - 1) != '/')
                paramString = paramString + "/";
        } else if (paramString.charAt(paramString.length() - 1) != '\\') {
            paramString = paramString + "\\";
        }
        for (int j = 0; j < files.getCount(); j++) {
            if (!files.getFile(j).isMissing()) {
                files.getFile(j).saveAs(
                        paramString + files.getFile(j).getFileName(),
                        paramInt);

                i++;
            }
        }
        return i;
    }

    public int getSize() {
        return totalBytes;
    }

    public byte getBinaryData(int paramInt) {
        return binaryArray[paramInt];
    }

    public byte[] getBinaryArray() {
        return binaryArray;
    }

    public Files getFiles() {
        return files;
    }

    public Request getRequest() {
        return formRequest;
    }

    public void downloadFile(String path) throws Exception {
        downloadFile(path, null, null);
    }

    public void downloadFile(String path, String type) throws Exception {
        downloadFile(path, type, null);
    }

    public void downloadFile(String path, String type, String disposition) throws Exception {
        downloadFile(path, type, disposition, 0xFDE8);
    }

    public void downloadFile(String path,
                             String type,
                             String disposition,
                             int offset) throws Exception {
        if (StrUtil.isNull(path))
            throw new IllegalArgumentException("File '" + path + "' not found");
        if ((!isVirtual(path)) && (denyPhysicalPath)) {
            throw new SecurityException("Physical path is denied");
        }
        if (isVirtual(path)) path = application.getRealPath(path);
        java.io.File file = new java.io.File(path);
        FileInputStream in = new FileInputStream(file);
        long len = file.length();
        byte[] arrayOfByte = new byte[offset];
        if (StrUtil.isNull(type))
            response.setContentType("application/x-msdownload");
        else {
            response.setContentType(type);
        }
        response.setContentLength((int) len);
        contentDisposition = (contentDisposition == null ? "attachment;" : contentDisposition);
        if (disposition == null)
            response.setHeader("Content-Disposition", contentDisposition + " filename=" + getFileName(path));
        else if (disposition.length() == 0)
            response.setHeader("Content-Disposition", contentDisposition);
        else {
            response.setHeader("Content-Disposition", contentDisposition + " filename=" + URLEncoder.encode(disposition, "UTF-8"));
        }
        for (int i = 0; i < len; ) {
            int count = in.read(arrayOfByte, 0, offset);
            response.getOutputStream().write(arrayOfByte, 0, count);
            i += count;
        }
        in.close();
    }

    public void downloadField(ResultSet resultSet,
                              String path,
                              String type,
                              String disposition) throws Exception {
        if (resultSet == null)
            throw new IllegalArgumentException("The resultSet cannot be null");
        if (StrUtil.isNull(path))
            throw new IllegalArgumentException("The columnName cannot be null or empty");
        byte[] arrayOfByte = resultSet.getBytes(path);
        if (StrUtil.isNull(type))
            response.setContentType("application/x-msdownload");
        else {
            response.setContentType(type);
        }
        response.setContentLength(arrayOfByte.length);
        if (StrUtil.isNull(disposition))
            response.setHeader("Content-Disposition", "attachment;");
        else {
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(disposition, "UTF-8"));
        }
        response.getOutputStream().write(arrayOfByte, 0, arrayOfByte.length);
    }

    public void fieldToFile(ResultSet resultSet, String field, String path) throws Exception {
        InputStream in = null;
        FileOutputStream out = null;
        try {
            if (application.getRealPath(path) != null) {
                path = application.getRealPath(path);
            }
            in = resultSet.getBinaryStream(field);
            out = new FileOutputStream(path);
            int i = -1;
            while ((i = in.read()) != -1) out.write(i);
        } catch (Exception e) {
            throw new SmartUploadException("Unable to save file from the DataBase");
        } finally {
            if (in != null) in.close();
            if (out != null) out.close();
        }
    }

    private String getDataFieldValue(String head, String field) {
        String str = field + "=" + '"';
        int i = head.indexOf(str);
        if (i > 0) {
            int j = i + str.length();
            str = "\"";
            int m = head.indexOf(str, j);
            if ((j > 0) && (m > 0)) return head.substring(j, m);
        }
        return "";
    }

    private String getFileExt(String fileName) {
        if (StrUtil.isNull(fileName)) {
            return fileName;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    private String getContentType(String type) {
        if (StrUtil.isNull(type)) {
            return type;
        }
        String ct = "Content-Type:";
        int i = type.indexOf(ct) + ct.length();
        if (i > -1) {
            return type.substring(i);
        }
        return "";
    }

    private String getTypeMIME(String mime) {
        if (StrUtil.isNull(mime)) {
            return mime;
        }
        int i = mime.indexOf("/");
        if (i > 1) {
            return mime.substring(1, i);
        }
        return mime;
    }

    private String getSubTypeMIME(String mime) {
        if (StrUtil.isNull(mime)) {
            return mime;
        }
        int i = mime.indexOf("/") + 1;
        if (i > 0) {
            return mime.substring(i);
        }
        return mime;
    }

    private String getContentDisp(String content) {
        return content.substring(content.indexOf(":") + 1, content.indexOf(";"));
    }

    private void getDataSection() {
        int m = boundary.length();
        startData = currentIndex;
        for (int i = currentIndex; i < totalBytes; i++) {
            int k = 0;
            if (binaryArray[i] == (byte) boundary.charAt(k)) {
                if (k == m - 1) {
                    endData = i - m - 1;
                    break;
                }
                k++;
            } else {
                k = 0;
            }
        }
        currentIndex = endData + m + 3;
    }

    private String getDataHeader() {
        int i = currentIndex, j = 0, m = 0;
        while (m == 0) {
            if ((binaryArray[currentIndex] == 13) && (binaryArray[currentIndex + 2] == 13)) {
                m = 1;
                j = currentIndex - 1;
                currentIndex += 2;
            } else {
                currentIndex += 1;
            }
        }
        return new String(binaryArray, i, j - i + 1);
    }

    public void setDeniedFilesList(String path) throws Exception {
        this.setFilesList(path, deniedFilesList);
    }

    public void setAllowedFilesList(String path) {
        this.setFilesList(path, allowedFilesList);
    }

    public void setDenyPhysicalPath(boolean denyPhysicalPath) {
        this.denyPhysicalPath = denyPhysicalPath;
    }

    public void setForcePhysicalPath(boolean forcePhysicalPath) {
        this.forcePhysicalPath = forcePhysicalPath;
    }

    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    public void setTotalMaxFileSize(long totalMaxFileSize) {
        this.totalMaxFileSize = totalMaxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    protected String getPhysicalPath(String path, int offset) throws IOException {
        String parent = null;
        String name = null;
        String separator = System.getProperty("file.separator");
        int i = 0;
        if (StrUtil.isNull(path))
            throw new IllegalArgumentException("There is no specified destination file");
        if (path.lastIndexOf("\\") >= 0) {
            parent = path.substring(0, path.lastIndexOf("\\"));
            name = path.substring(path.lastIndexOf("\\") + 1);
        }
        if (path.lastIndexOf("/") >= 0) {
            parent = path.substring(0, path.lastIndexOf("/"));
            name = path.substring(path.lastIndexOf("/") + 1);
        }
        parent = parent.length() == 0 ? "/" : parent;
        java.io.File localFile = new java.io.File(parent);
        if (localFile.exists()) i = 1;
        if (offset == 0) {
            if (isVirtual(parent)) {
                parent = application.getRealPath(parent);
                if (parent.endsWith(separator))
                    parent = parent + name;
                else {
                    parent = parent + separator + name;
                }
                return parent;
            }
            if (i != 0) {
                if (denyPhysicalPath) {
                    throw new IllegalArgumentException("Physical path is denied");
                }
                return path;
            }
            throw new IllegalArgumentException("This path does not exist");
        }

        if (offset == 1) {
            if (isVirtual(parent)) {
                parent = application.getRealPath(parent);
                if (parent.endsWith(separator))
                    parent = parent + name;
                else {
                    parent = parent + separator + name;
                }
                return parent;
            }
            if (i != 0) {
                throw new IllegalArgumentException("The path is not a virtual path");
            }
            throw new IllegalArgumentException("This path does not exist");
        }

        if (offset == 2) {
            if (i != 0) {
                if (denyPhysicalPath) {
                    throw new IllegalArgumentException("Physical path is denied");
                }
                return path;
            }
            if (isVirtual(parent)) {
                throw new IllegalArgumentException("The path is not a physical path");
            }
            throw new IllegalArgumentException("This path does not exist");
        }
        return null;
    }

    public void uploadInFile(String path) throws Exception {
        if (StrUtil.isNull(path))
            throw new IllegalArgumentException("There is no specified destination file");
        if ((!isVirtual(path)) && (denyPhysicalPath)) {
            throw new SecurityException("Physical path is denied");
        }
        int i = request.getContentLength();
        binaryArray = new byte[i];
        int j = 0, k = 0;
        while (j < i) {
            try {
                k = request.getInputStream().read(binaryArray, j, i - j);
            } catch (Exception e) {
                throw new SmartUploadException("Unable to upload");
            }
            j += k;
        }
        if (isVirtual(path))
            path = application.getRealPath(path);
        try {
            java.io.File localFile = new java.io.File(path);
            FileOutputStream out = new FileOutputStream(localFile);
            out.write(binaryArray);
            out.close();
        } catch (Exception e) {
            throw new SmartUploadException("The Form cannot be saved in the specified file");
        }
    }

    private void setFilesList(String path, Vector<String> list) {
        if (StrUtil.isNull(path)) {
            list = null;
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < path.length(); i++) {
                if (path.charAt(i) == ',') {
                    if (!list.contains(sb.toString())) {
                        list.addElement(sb.toString());
                    }
                    sb.delete(0, sb.length());
                } else {
                    sb.append(path.charAt(i));
                }
            }
            list.addElement(sb.toString());
        }
    }

    private boolean isVirtual(String path) {
        if (application.getRealPath(path) != null) {
            return new java.io.File(application.getRealPath(path)).exists();
        }
        return false;
    }

    private String getFileName(String name) {
        if (StrUtil.isNull(name))
            return null;
        int i = name.lastIndexOf('/');
        if (i != -1) return name.substring(i + 1);
        i = name.lastIndexOf('\\');
        if (i != -1) {
            return name.substring(i + 1);
        }
        return name;
    }
}
