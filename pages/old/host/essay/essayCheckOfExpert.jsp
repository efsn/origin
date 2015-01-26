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
  	   	Essay Check Of Editor!'
  	</title>
  	<link rel="stylesheet" type="text/css" href="/sys/css/a.css" />
  	
  </head>
  
 <body>
		<center>
		<table width="700px" style="font-size:11px" border="0">
			<tr>				
				<td width="100%" align="center" height="30">
					<form action="/sys/host/essay/essayCheckOfExpert.jsp" method="post">
					<strong><font size="3">稿件名：</font></strong>
					<input type="text" name="essayName"/>&nbsp;&nbsp;&nbsp;&nbsp;
					<input type="submit" value="查询"/>
					</form>
				</td>
			</tr>
			
			<tr>
				<td width="600px" align="center">
				
					<table width="700px" style="font-size:11px" border="0" >
					<col bgcolor="#dcdcdc"/><col bgcolor="#dcdcdc"/>
	  	  				<tr>
	  	  					<td align="center" width="20%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" face="Courier" color="blue">稿件名</font>	  	  						
	  	  					</td>
	  	  					<td align="center" width="10%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" face="Courier" color="blue">栏目</font>	  	  						
	  	  					</td>
	  	  					<td align="center" width="20%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" face="Courier" color="blue">编辑审核结果</font>	  	  						
	  	  					</td>
	  	  					<td align="center" width="10%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" face="Courier" color="blue">下载稿件</font>	  	  						
	  	  					</td>	  	  					
	  	  					<td align="center" width="10%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" face="Courier" color="blue">审核</font>	  	  						
	  	  					</td>	  	  						  	  					
	  	  				</tr>
	  	  					  	  				
	  	  				 <%
							 String essayName = request.getParameter("essayName");
	  	  				 	 String expertName = (String)session.getAttribute("user");
	  	  				 	 
							 Map<Integer, List<String>> value = PryFactory.getPryQueryAllDao().getEssayCheckedByEditor(expertName, essayName);
	  	  				 									 
	  	  				  // 此处过滤编辑没有审核和通过的
					//		 value = RecordFilter.EditorCheckFilter(value);
	 
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
						 	   response.sendRedirect("/sys/error/expertCheckError.jsp");
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
	  								out.println(value.get(x).get(0));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(1));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(2));
	  								out.println("</font>");
	  								
	  								out.println("</td>");	  								  								
	  									  
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/download.jsp?filePath="+ value.get(x).get(3)+"/" + "&disName=" + value.get(x).get(0) + ".txt" +  ">下载</a>");
	  								out.println("</font>");
	  								
	  								out.println("</td>");	
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/host/essay/essayCheckOfExpertTo.jsp?essayId=" + x + "&essayName=" + value.get(x).get(0) +">审稿</a>");
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
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(2));
	  								out.println("</font>");
	  								
	  								out.println("</td>");	  								  								
	  									  
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/download.jsp?filePath="+ listValue.get(i).get(3) + "/" + "&disName=" + listValue.get(i).get(0) + ".txt" +">下载</a>");
	  								out.println("</font>");
	  								
	  								out.println("</td>");	
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/host/essay/essayCheckOfExpertTo.jsp?essayId=" + listKey.get(i) + "&essayName=" + listValue.get(i).get(0) + ">审稿</a>");
	  								out.println("</font>");
	  								
	  								out.println("</td>");	  								
	  									  								
	  								out.println("</tr>"); 
						  	 } 
					  	 }						  	  	 
  					
	  	  				%>   	  	  				
	  	  				<tr>
	  	  				<td colspan = "5" align="center" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3">
	  	  					<%
								//上一页
								if(pageNow!=1)
								{
									out.println("<a href=essayCheckOfExpert.jsp?pageNow="+(pageNow-1)+">上一页</a>");
								}
								//显示超链接
								for(int i=1;i<=pageCount;i++)
								{
									out.println("<a href=essayCheckOfExpert.jsp?pageNow="+i+">["+i+"]</a>");
								}
								//下一页
								if(pageNow!=pageCount)
								{
									out.println("<a href=essayCheckOfExpert.jsp?pageNow="+(pageNow+1)+">下一页</a>");
								}
							%>	
							</font>  	  				
	  	  				</td>
	  	  				</tr>	  	  							  	  				
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

