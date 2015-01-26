<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>

<%@ page import="sys.dao.*" %>
<%@ page import="sys.dao.impl.*" %>
<%@ page import="sys.dao.proxy.*" %>
<%@ page import="sys.factory.*" %>
<%@ page import="sys.dbc.*" %>
<%@ page import="sys.vo.*" %>



<html>
  <head>
  	<title>
  	   	Essay Admin!'
  	</title>
  	<link rel="stylesheet" type="text/css" href="/sys/css/a.css" />
  	
  </head>
  
 <body>				
		<center>
		<table width="700px" style="font-size:11px" border="0">
			<tr>
				<td align="center">
				
					<table  width="700px" style="font-size:11px" border="0">
	  	  				<tr>
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" color="blue">稿件名</font>		
	  	  					</td>
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" color="blue">栏目</font>		
	  	  					</td>
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" color="blue">发表期次</font>  
	  	  					</td>
	  	  					<td align="center" width="10%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" color="blue">版面费用</font>
	  	  					</td>
	  	  					<td align="center" width="10%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" color="blue">缴费状态</font>
	  	  					</td>	
	  	  					<td align="center" width="10%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" color="blue">修改</font>
	  	  					</td>
	  	  					<td align="center" width="10%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" color="blue">删除</font>
	  	  					</td>  	  						  	  						  	  						  	  					
	  	  				</tr>
	  	  				
	  	  				 <%

	  	  					 String author = (String)session.getAttribute("user");		  	  				 
							 Map<Integer, List<String>> value = PryFactory.getPryQueryAllDao().getEssayToAuthor(author);	  	  				 		
	  	  					 
							 TB_Essay essay = new TB_Essay();
 		  	  			 	 essay.setAuthor_pname(author);
 							 session.setAttribute("essay", essay);  
	  	  				 			 
 							 int n = 0;
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
	  								out.println(value.get(x).get(4));
	  								out.println("</font>");
	  								 		  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								if(null != value.get(x).get(1))
	  								{
	  								  	out.println("<font size='3'>");
	  									out.println(value.get(x).get(1));
		  								out.println("</font>");
	  								  
	  								}
	  								else
		  							{
	  								    out.println("<font size='3'>");
	  								    out.println("未发表");
		  								out.println("</font>");
	  								   
	  								}
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								if(null != value.get(x).get(2))
	  								{
	  								  	out.println("<font size='3'>");
	  									out.println(value.get(x).get(2));
		  								out.println("</font>");
	  								 
	  								}
	  								else
	  								{
	  								  	out.println("<font size='3'>");
	  									out.println("无版费"); 
		  								out.println("</font>");
	  								  
	  								}  								
	  								out.println("</td>");	  								
	  									  
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(3));
	  								out.println("</font>");
	  								 		  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								if("未缴费".equals(value.get(x).get(3)))
	  								{	 
	  								  out.println("<font size='3'>");  
	  								  n++;
	  								  String name = "" + n;
	  								  session.setAttribute(name, value.get(x).get(0));	  								  
	  								  out.println("<a href=" + "/sys/host/essay/essayUpload.jsp?essayName=" + n + ">修改</a>");
	  									out.println("</font>");
	  								}
	  								else
	  								{
	  								  	out.println("<font size='3'>");
	  									out.println("不能修改");
		  								out.println("</font>");
	  								    
	  								}
	  								out.println("</td>");		  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								if("未缴费".equals(value.get(x).get(3)))
	  								{	
	  								    out.println("<font size='3'>");
	  									out.println("<a href=" + "/sys/host/delete/essayDelete.jsp?essayId=" + x + ">删除</a>");
	  									out.println("</font>");
	  									
	  								}
	  								else
	  								{
	  								    out.println("<font size='3'>");
	  								  	out.println("不能删除");
  										out.println("</font>");
	  								   ;
	  								}
	  					
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
	  								
	  								
	  								essay.setEssay_name(listValue.get(i).get(0));
	  								session.setAttribute("essay", essay);
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(4));
									out.println("</font>");
	  	 							
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								if(null != listValue.get(i).get(1))
	  								{
	  								  	out.println("<font size='3'>");
	  									out.println(listValue.get(i).get(1));
										out.println("</font>");
	  								  
	  								}
	  								else
	  								{
	  								  	out.println("<font size='3'>");
	  									out.println("未发表");
										out.println("</font>");
	  								   
	  								}  	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								if(null != listValue.get(i).get(2))
	  								{
	  								  	out.println("<font size='3'>");
	  									out.println(listValue.get(i).get(2));
										out.println("</font>");
	  								  
	  								}
	  								else
	  								{
	  								  	out.println("<font size='3'>");
	  									out.println("无版费"); 
										out.println("</font>");
	  								 
	  								} 	  								
	  								out.println("</td>");	  									  									  
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(3)); 
									out.println("</font>");
	  	 							
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								if("未缴费".equals(listValue.get(i).get(3)))
	  								{	  	
	  								  out.println("<font size='3'>");
	  								  n++;
	  								  String name = "" + n;
	  								  session.setAttribute(name, listValue.get(i).get(0));	  								  
	  								  out.println("<a href=" + "/sys/host/essay/essayUpload.jsp?essayName=" + n + ">修改</a>"); 
	  								  out.println("</font>");
	  								}
	  								else
	  								{
	  								  out.println("<font size='3'>");
	  								  out.println("不能修改"); 
	  								  out.println("</font>");
	  								}
	  								out.println("</td>");		  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								if("未缴费".equals(listValue.get(i).get(3)))
	  								{
	  								  out.println("<font size='3'>");
	  								  out.println("<a href=" + "/sys/host/delete/essayDelete.jsp?essayId=" + listKey.get(i) + ">删除</a>"); 
	  								  out.println("</font>");
	  								  
	  								}
	  								else
	  								{
	  								  out.println("<font size='3'>");
	  								  out.println("不能删除"); 
	  								  out.println("</font>");
	  								   
	  								}	  								
	  								out.println("</td>");	
	  									  								
	  								out.println("</tr>"); 
						  	 } 
					  	 }						  	  	 
  					
	  	  				%>   	  	  				
	  	  				
	  	  				<td colspan = "7" align="center" bgcolor="lemonchiffon">
	  	  					<font size="3">
	  	  					<%
								//上一页
								if(pageNow!=1)
								{
									out.println("<a href=essayAuthor.jsp?pageNow="+(pageNow-1)+">上一页</a>");
								}
								//显示超链接
								for(int i=1;i<=pageCount;i++)
								{
									out.println("<a href=essayAuthor.jsp?pageNow="+i+">["+i+"]</a>");
								}
								//下一页
								if(pageNow!=pageCount)
								{
									out.println("<a href=essayAuthor.jsp?pageNow="+(pageNow+1)+">下一页</a>");
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

