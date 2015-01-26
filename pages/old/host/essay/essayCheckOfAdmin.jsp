<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>

<%@ page import="sys.dao.*" %>
<%@ page import="sys.dao.impl.*" %>
<%@ page import="sys.dao.proxy.*" %>
<%@ page import="sys.factory.*" %>
<%@ page import="sys.dbc.*" %>
<%@ page import="util.*" %>



<html>
  <head>
  	<title>
  	   	Essay Check By Admin!'
  	</title>
  	<link rel="stylesheet" type="text/css" href="/sys/css/a.css" />
  	
  </head>
  
  <body>
  		
		<center>
		<table width="700px" style="font-size:11px" border="0">
			<tr>				
				<td width="100%" align="center" height="30">
					<form action="essayCheckOfAdmin.jsp" method="post">
					<strong><font size="3">稿件名：</font></strong>
					<input type="text" name="essayName"/>&nbsp;&nbsp;&nbsp;
					<strong><font size="3">专家名：</font></strong>
					<input type="text" name="expertpName"/>&nbsp;&nbsp;&nbsp;
					<input type="submit" value="查询"/>
					</form>
				</td>
			</tr>
			
			<tr>
				<td width="600px" align="center">
				
					<table width="700px" style="font-size:11px" border="0">
	  	  				<tr>
	  	  					<td align="center" width="20%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" color="blue">稿件名</font>    						
	  	  					</td>
	  	  					<td align="center" width="10%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" color="blue">栏目</font>	 	  						
	  	  					</td>
	  	  					<td align="center" width="10%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" color="blue">专家名</font>	    						
	  	  					</td>
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" color="blue">专家审核结果</font> 	  						
	  	  					</td>
	  	  					<td align="center" width="20%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" color="blue">专家审稿日期</font>	 	  						
	  	  					</td>
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" color="blue">下载稿件</font>	  	  						
	  	  					</td>	  	  					
	  	  					<td align="center" width="10%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" color="blue">审核</font> 	  						
	  	  					</td>	  	  						  	  					
	  	  				</tr>	  	  				
	  	  				
	  	  				 <%			
	  	  				 	 String essayName = request.getParameter("essayName");
	  	  				 	 String expertpName = request.getParameter("expertpName");
							 Map<Integer, List<String>> value = PryFactory.getPryQueryAllDao().getEssayCheckedByExpert(essayName, expertpName);
	  	  				 
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
						 							 	
						 	if(listValue.isEmpty() || value.isEmpty())
	  	  				 	{
	  	  				 		//out.println("没有稿件需要审核");
	  	  				 		response.sendRedirect("/sys/error/adminCheckError.jsp");
	  	  				 		return;
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
	  								out.println(value.get(x).get(3));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(4));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
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
	  								out.println(value.get(x).get(1));
	  								out.println("</font>");
	  								
	  								out.println("</td>");	  								  								
	  									  
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/download.jsp?filePath="+ value.get(x).get(5)+"/" + "&disName=" + value.get(x).get(3) + ".txt" +  ">下载</a>");
	  								out.println("</font>");
	  								
	  								out.println("</td>");	
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/host/essay/essayCheckOfAdminTo.jsp?essayId=" + x + "&essayName=" + value.get(x).get(3) + ">审稿</a>");
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
	  								out.println(listValue.get(i).get(3));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(4));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
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
	  								out.println("<font size='3'");
	  								out.println(listValue.get(i).get(1));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/download.jsp?filePath="+ listValue.get(i).get(5) + "/" + "&disName=" + listValue.get(i).get(3) + ".txt" +">下载</a>");
	  								out.println("</font>");
	  								
	  								out.println("</td>");	
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/host/essay/essayCheckOfAdminTo.jsp?essayId=" + listKey.get(i) + "&essayName=" + listValue.get(i).get(3) + ">审稿</a>");
	  								out.println("</font>");
	  								
	  								out.println("</td>");	  								
	  									  								
	  								out.println("</tr>"); 
						  	 } 
					  	 }						  	  	 
  					
	  	  				%>   	  	  				
	  	  				
	  	  				<td colspan = "7" align="center" height="30" bgcolor="lemonchiffon">
	  	  					<font size='3'>
	  	  					<%
								//上一页
								if(pageNow!=1)
								{
									out.println("<a href=essayCheckOfAdmin.jsp?pageNow="+(pageNow-1)+">上一页</a>");
								}
								//显示超链接
								for(int i=1;i<=pageCount;i++)
								{
									out.println("<a href=essayCheckOfAdmin.jsp?pageNow="+i+">["+i+"]</a>");
								}
								//下一页
								if(pageNow!=pageCount)
								{
									out.println("<a href=essayCheckOfAdmin.jsp?pageNow="+(pageNow+1)+">下一页</a>");
								}
							%>
	  	  				</font>
	  	  				</td>	  	  							  	  				
	  	  		</table>				
				</td>
			</tr>			
	  	  	</table>				
			</td>
			</tr>
		</table>
		</center>
  </body>
</html>

