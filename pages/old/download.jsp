<%@ page contentType="text/html; charset=UTF-8" 
import="com.jspsmart.upload.*" %><%
	
	//获取路径 
	String filePath = request.getParameter("filePath");
	String dizName = request.getParameter("disName");
	String path = "E:/EclipseWorkspace/CTBTSystem/essayFiles/";
	
	//new a SmartUpload
	SmartUpload down = new SmartUpload();
	//init
	down.initialize(pageContext);
	//set contentDisposition to null 以禁止browser自动打开文件。
	//保证点击链接后是下载文件。若不设定，则下载的是文件扩展名为doc or pdf(acrobat) 时，浏览器将自动打开word
	down.setContentDisposition(null);
	//download files
	
	down.downloadFile(path + filePath + dizName);
	
%>