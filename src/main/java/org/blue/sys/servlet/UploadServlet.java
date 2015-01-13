package org.blue.sys.servlet;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.blue.sys.vo.Essay;

public class UploadServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) {
        HttpSession session = request.getSession();
        String user = ((Essay) session.getAttribute("essay")).getAuthorInfo();
        String essayName = ((Essay) session.getAttribute("essay")).getEssayName();
        int MAX_SIZE = 102400 * 102400;
        String rootPath;
        DataInputStream in = null;
        FileOutputStream fileOut = null;
        String remoteAddr = request.getRemoteAddr();
        String serverName = request.getServerName();

        String realPath = request.getRealPath(serverName);
        realPath = realPath.substring(0, realPath.lastIndexOf("\\"));
        rootPath = realPath + "\\upload\\";
        PrintWriter out = null;
        try{
            out = response.getWriter();
        } catch(IOException e1){
            e1.printStackTrace();
        }

        String contentType = request.getContentType();
        try{
            if(contentType.indexOf("multipart/form-data") >= 0){
                in = new DataInputStream(request.getInputStream());
                int formDataLength = request.getContentLength();
                if(formDataLength > MAX_SIZE){
                    out.println("<P>�ϴ����ļ��ֽ�����Գ���" + MAX_SIZE + "</p>");
                    return;
                }
                byte dataBytes[] = new byte[formDataLength];
                int byteRead = 0;
                int totalBytesRead = 0;
                while(totalBytesRead < formDataLength){
                    byteRead = in.read(dataBytes, totalBytesRead,
                            formDataLength);
                    totalBytesRead += byteRead;
                }
                String file = new String(dataBytes);
                // out.println(file);
                String saveFile = file
                        .substring(file.indexOf("filename=\"") + 10);
                saveFile = saveFile.substring(0, saveFile.indexOf("\n"));
                saveFile = saveFile.substring(saveFile.lastIndexOf("\\") + 1,
                        saveFile.indexOf("\""));
                int lastIndex = contentType.lastIndexOf("=");
                String boundary = contentType.substring(lastIndex + 1,
                        contentType.length());
                String fileName = "E:\\Workspace\\Contents\\essayFiles\\"
                        + user + "\\" + essayName + ".txt";
                // out.print(fileName);
                int pos;
                pos = file.indexOf("filename=\"");
                pos = file.indexOf("\n", pos) + 1;
                pos = file.indexOf("\n", pos) + 1;
                pos = file.indexOf("\n", pos) + 1;
                int boundaryLocation = file.indexOf(boundary, pos) - 4;
                // out.println(boundaryLocation);
                int startPos = ((file.substring(0, pos)).getBytes()).length;
                // out.println(startPos);
                int endPos = ((file.substring(0, boundaryLocation)).getBytes()).length;
                // out.println(endPos);
                File checkFile = new File(fileName);
                if(checkFile.exists()){
                    out.println("<p>" + saveFile + "�ļ��Ѿ�����.</p>");
                }
                File fileDir = new File(rootPath);
                if(!fileDir.exists()){
                    fileDir.mkdirs();
                }
                fileOut = new FileOutputStream(fileName);
                fileOut.write(dataBytes, startPos, (endPos - startPos));
                fileOut.close();

                response.sendRedirect("/sys/success/uploadSuccess.jsp");
            }
            else{
                out.println("<p>�ϴ���������Ͳ���multipart/form-data</p>");
            }
        } catch(Exception ex){
            try{
                throw new ServletException(ex.getMessage());
            } catch(ServletException e){
                e.printStackTrace();
            }
        }
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) {
        doGet(request, response);
    }
}
