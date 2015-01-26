<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="sys.vo.*" %> 

<html>
<head>
<title>Essay Info Upload</title>
</head>
<body>
	<table height="60px" width="600px">
  			<tr><td><p>
				<font size="6" face="楷体" color="red">稿件上传</font>
			</p></td></tr>		
  	</table>	

	<center>
		<form action="/sys/UploadServlet" method="post" enctype="multipart/form-data">
		<table height="180px" width="600px" style="font-size:11px" border="0" borderColor="#ffffff">
				<col bgcolor="#dcdcdc"/><col bgcolor="#dcdcdc"/>
					<tr>
						<td align="center" height="40px">
							<font size="3" color="blue">作者名：</font>
						</td>
						<td>
						<font size="3">
							<%= ((TB_Essay)session.getAttribute("essay")).getAuthor_pname() %>
							</font>
						</td>
					</tr>
					<tr>
						<td align="center" height="40px">
							<font size="3" color="blue">稿件名：</font>
						</td>
						<td> 
							<font size="3">
							<%
								if(null != ((TB_Essay)session.getAttribute("essay")).getEssay_name())
								{
								    out.println(((TB_Essay)session.getAttribute("essay")).getEssay_name());
								}
								else
								{
								    ((TB_Essay)session.getAttribute("essay")).setEssay_name(((String)session.getAttribute(request.getParameter("essayName"))));
								    out.println(((TB_Essay)session.getAttribute("essay")).getEssay_name());								    
								}
							%>
							</font>
						</td>
					</tr>
					<tr>
						<td/>
						<td height="30px">
							<input type="file" name="essay"/>&nbsp;<font color="white" size="2">（上传稿件,最多3000字,只能是.txt格式）</font>		
						</td>
					</tr>
					<tr>
						<td/>
						<td height="30px">
							<input type="submit" value="上传"/>
							&nbsp;&nbsp;&nbsp;&nbsp;
							<input type="reset" value="取消"/>
						</td>
					</tr>
				</table>
			</form>
	</center>



						
		
</body>
</html>