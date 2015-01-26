<%@ page language="java" import="java.util.*" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="sys.dao.*" %>
<%@ page import="sys.dao.impl.*" %>
<%@ page import="sys.dao.proxy.*" %>
<%@ page import="sys.factory.*" %>
<%@ page import="sys.dbc.*" %>



<html>
<head>

	<title>Author Admin!'</title>
	<link rel="stylesheet" type="text/css" href="/sys/css/a.css" />
</head>
<body>		
  			<center> 		
	  	  		<table width="700px" style="font-size:11px" border="0">
	  	  				<tr>
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" color="blue">用户名</font>
	  	  					</td>
	  	  					<td align="center" width="20%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" color="blue">姓名</font>
	  	  					</td>
	  	  					<td align="center" width="25%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" color="blue">电子邮件</font>
	  	  					</td>
	  	  					<td align="center" width="20%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" color="blue">联系电话</font>
	  	  					</td>
	  	  					<td align="center" width="10%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" color="blue">删除</font>
	  	  					</td>
	  	  					<td align="center" width="10%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" color="blue">详细</font>
	  	  					</td>
	  	  					
	  	  					
	  	  				</tr>
	  	  				
	  	  				 <%
  							 Map<Integer, List<String>> value = PryFactory.getPryQueryAllDao().getAuthorAll();
	  	  				 		
	  	  					 int param = 0;
	  	  				 	 if(null != request.getParameter("pageNow"))
	  	  				 	 {
	  	  				 		param = Integer.parseInt(request.getParameter("pageNow"));
	  	  				 	 } 
						  	 int pageSize = 5;
						  	 int pageNow = 1;
						  	 int pageCount;//total pages
						  	 int rowCount = value.keySet().size();//total rows
						  	 List<Integer> listKey = new ArrayList<Integer>();
						  	 List<List<String>> listValue = new ArrayList<List<String>>();
						  
						  	 if(0 == rowCount%pageSize)
						  	 {
						  		pageCount = rowCount/pageSize; 
						  	 }
						  	 else
						  	 {
						  		 pageCount = rowCount/pageSize + 1 ;
						  	 } 
						  	 
						  	 for(Integer key : value.keySet())
						  	 {
						  		 listKey.add(key);
						  	 }
						  	 
						  	 for(List<String> str : value.values())
						  	 {
							 	listValue.add(str);
						 	 }
						  	 
						 	if(0 != param)
	  	  					 {
						 		pageNow = param; 
	  	  					 }							  	 						  	 						  	 
						  	 
						  	 if(pageNow == pageCount)
						  	 {
						  		 for(int j = 0; j < (pageCount-1)*pageSize; j++)
						  		 {
						  			 value.remove(listKey.get(j));
						  		 }
						  		 
						  		 for(Integer x : value.keySet())
						  		 {
									out.println("<tr>");
							  									  		
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(0));
	  								out.println("</font>");	
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(2));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(4));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(5));
	  								out.println("</font>");
	  								
	  								out.println("</td>");	  								
	  									  						
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/host/delete/deleteAuthor.jsp?authorId=" + x + ">删除</a>");
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/host/queryAuthor.jsp?authorId=" + x + ">详细</a>");
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  									  								
	  								out.println("</tr>"); 
						  		 }
						  	 }
						  	 else
						  	 {
						  		for(int i = (pageNow-1)*pageSize; i < pageNow*pageSize; i++)
							  	 {
							  		out.println("<tr>");
							  		
							  		
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(0));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(2));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(4));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(5));
	  								out.println("</font>");
	  								
	  								out.println("</td>");	  								
	  									  						
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/host/delete/deleteAuthor.jsp?authorId=" + listKey.get(i) + ">删除</a>");
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/host/queryAuthor.jsp?authorId=" + listKey.get(i) + ">详细</a>");
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  									  								
	  								out.println("</tr>");	 
							  	 } 
						  	 }						  	  	 
  					
	  	  				%>   	  	  				
	  	  				
	  	  				<td colspan = "6" align="center" bgcolor="lemonchiffon">
	  	  				<font size="3">
	  	  					<%
								//上一页
								if(pageNow!=1)
								{
									out.println("<a href=authorAdmin.jsp?pageNow="+(pageNow-1)+">上一页</a>");
								}
								//显示超链接
								for(int i=1;i<=pageCount;i++)
								{
									out.println("<a href=authorAdmin.jsp?pageNow="+i+">["+i+"]</a>");
								}
								//下一页
								if(pageNow!=pageCount)
								{
									out.println("<a href=authorAdmin.jsp?pageNow="+(pageNow+1)+">下一页</a>");
								}
							%>
	  	  				</font>
	  	  				</td>	  	  							  	  				
	  	  		</table>
  		</center>
	
</body>
</html>