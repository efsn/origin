<%@ page language="java" import="java.util.*" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ page import="sys.dao.*" %>
<%@ page import="sys.dao.impl.*" %>
<%@ page import="sys.dao.proxy.*" %>
<%@ page import="sys.factory.*" %>
<%@ page import="sys.dbc.*" %>
<%@ page import="util.*" %>



<html>
<head>
<title>Read News!'</title>  	<link rel="stylesheet" type="text/css" href="/sys/css/a.css" />
</head>
<body>		
		<center>
			<table width="700px" style="font-size:11px" border="0">
				<tr>
					<td align="center" width="20px">
						<strong><font size="5" color="red">稿件征集</font></strong>
					</td>
					<td>
						<table width="700px" style="font-size:11px" border="0">
	  	  				<tr>	  	  					
	  	  					<td align="center" width="30%" height="30" bgcolor="lemonchiffon">
	  	  					<p><font size="3" face="Courier" color="blue">栏目</font></p>	  	  						
	  	  					</td>	  	  					
	  	  					<td align="center" width="30%" height="30" bgcolor="lemonchiffon">
	  	  					<p><font size="3" face="Courier" color="blue">截止日期</font></p>	  	  						
	  	  					</td>	  	  						  	  					
	  	  				</tr>
	  	  				
	  	  				 <%

							 Map<Integer, List<String>> value = PryFactory.getPryQueryAllDao().getEssayType();						
	  	  				 			  	  				 		  	 
	  	  					
	  	  				 	if(value.keySet().isEmpty())
	  	  				 	{
	  	  				 		response.sendRedirect("/sys/error/typeError.jsp");
	  	  				 	}
	  	  				 
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
	  								out.println(value.get(x).get(1));
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
	  								out.println(listValue.get(i).get(1));
	  								out.println("</font>");
	  								
	  								out.println("</td>");	  								  									  								
	  									  								
	  								out.println("</tr>"); 
						  	 } 
					  	 }						  	  	 
  					
	  	  				%>   	  	  				
	  	  				<tr>
	  	  				<td colspan = "2" align="center" height="30" bgcolor="lemonchiffon">
	  	  					<font size='3'>
	  	  					<%
								//上一页
								if(pageNow!=1)
								{
									out.println("<a href=readNews.jsp?pageNow="+(pageNow-1)+">上一页</a>");
								}
								//显示超链接
								for(int i=1;i<=pageCount;i++)
								{
									out.println("<a href=readNews.jsp?pageNow="+i+">["+i+"]</a>");
								}
								//下一页
								if(pageNow!=pageCount)
								{
									out.println("<a href=readNews.jsp?pageNow="+(pageNow+1)+">下一页</a>");
								}
							%>	
							</font>  	  				
	  	  				</td>
	  	  				</tr>	  	  							  	  				
	  	  		</table>
					</td>
				</tr>
				<tr>
					<td align="center" width="20px">
						<strong><font size="5" color="red">稿件发表</font></strong>
					</td>
					<td>
						<table width="700px" style="font-size:11px" border="0">
	  	  				<tr>	  	  					
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" face="Courier" color="blue">稿件名</font>		
	  	  					</td>
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" face="Courier" color="blue">栏目</font>		
	  	  					</td>
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" face="Courier" color="blue">作者</font>  	  						
	  	  					</td>
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" face="Courier" color="blue">发表期次</font>  
	  	  					</td>
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" face="Courier" color="blue">下载</font>
	  	  					</td>
	  	  					
	  	  						  	  						  	  						  	  						  	  					
	  	  				</tr>
	  	  				
	  	  				 <%

							 Map<Integer, List<String>> value1 = PryFactory.getPryQueryAllDao().getEssayCheckedByAdmin();
	  	  				 		
	  	  					 int param1 = 0;
	  	  				 	 if(null != request.getParameter("pageNow1"))
	  	  				 	 {
	  	  				 		param1 = Integer.parseInt(request.getParameter("pageNow1"));
	  	  				 	 } 
						  	 int pageSize1 = 5;
						  	 int pageNow1 = 1;
						  	 int pageCount1;//total pages
						  	 int rowCount1 = value1.keySet().size();//total rows
						  	 List<Integer> listKey1 = new ArrayList<Integer>();
						  	 List<List<String>> listValue1 = new ArrayList<List<String>>();
						  
						  	 if(0 == rowCount1%pageSize1)
						  	 {
						  		pageCount1 = rowCount1/pageSize1; 
						  	 }
						  	 else
						  	 {
						  		 pageCount1 = rowCount1/pageSize1 + 1 ;
						  	 } 
						  	 
						  	 for(Integer key1 : value1.keySet())
						  	 {
						  		 listKey1.add(key1);
						  	 }
						  	 
						  	 for(List<String> str1 : value1.values())
						  	 {
							 	listValue1.add(str1);
						 	 }
						  	 
						 	if(0 != param1)
	  	  					 {
						 		pageNow1 = param1; 
	  	  					 }							  	 						  	 						  	 
						  	 
						 	if(value.isEmpty() || listValue.isEmpty())
						 	{
						 	    response.sendRedirect("/sys/NoNews.jsp");
						 	    return;
						 	}
						 	
						  	 if(pageNow1 == pageCount1)
						  	 {
						  		 for(int j = 0; j < (pageCount1-1)*pageSize1; j++)
						  		 {
						  			 value1.remove(listKey1.get(j));
						  		 }
						  		 
						  		 for(Integer x1 : value1.keySet())
						  		 {
									out.println("<tr>");
									
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value1.get(x1).get(0));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value1.get(x1).get(5));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value1.get(x1).get(1));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value1.get(x1).get(2));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/download.jsp?filePath="+ value1.get(x1).get(1)+"/" + "&disName=" + value1.get(x1).get(0) + ".txt" +  ">下载</a>");
	  								out.println("</font>");
	  								
	  								out.println("</td>");		  								
	  									  								
	  								out.println("</tr>"); 
						  		 }
						  	 }
						  	 else
						  	 {
						  		for(int i = (pageNow1-1)*pageSize1; i < pageNow1*pageSize1; i++)
							  	 {
						  			out.println("<tr>");								  		
						  			
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue1.get(i).get(0));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue1.get(i).get(5));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue1.get(i).get(1));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue1.get(i).get(2));
	  								out.println("</font>");
	  								
	  								out.println("</td>");	  									  								
	  									 
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/download.jsp?filePath="+ value.get(i).get(1)+"/" + "&disName=" + value.get(i).get(0) + ".txt" +  ">下载</a>");
	  								out.println("</font>");
	  								
	  								out.println("</td>");	
	  								
	  								out.println("</tr>"); 
						  	 } 
					  	 }						  	  	 
  					
	  	  				%>   	  	  				
	  	  				
	  	  				<td colspan = "5" align="center" height="30" bgcolor="lemonchiffon">
	  	  					<font size='3'>
	  	  					<%
								//上一页
								if(pageNow1!=1)
								{
									out.println("<a href=readNews.jsp?pageNow1="+(pageNow1-1)+">上一页</a>");
								}
								//显示超链接
								for(int i=1;i<=pageCount1;i++)
								{
									out.println("<a href=readNews.jsp?pageNow1="+i+">["+i+"]</a>");
								}
								//下一页
								if(pageNow1!=pageCount1)
								{
									out.println("<a href=readNews.jsp?pageNow1="+(pageNow1+1)+">下一页</a>");
								}
							%>
	  	  				</font>
	  	  				</td>	  	  							  	  				
	  	  		</table>
					</td>
				</tr>			
			</table>
	  	  	</center>	

	</body>
</html>