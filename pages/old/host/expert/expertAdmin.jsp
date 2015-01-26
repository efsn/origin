<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>

<%@ page import="sys.dao.*" %>
<%@ page import="sys.dao.impl.*" %>
<%@ page import="sys.dao.proxy.*" %>
<%@ page import="sys.factory.*" %>
<%@ page import="sys.dbc.*" %>



<html>
  <head>
  	<title>
  	   	Expert Admin!'
  	</title>
  	<link rel="stylesheet" type="text/css" href="/sys/css/a.css" />
  	
  </head>
  
 <body>
		
		<center>
		<table height="400px" width="700px" style="font-size:11px" border="0">
			<tr>				
				<td width="100%" align="center">
					<form action="expertAdmin.jsp" method="post">
					<font face="Courier" size="2">登陆名：</font>
					<input type="text" name="expertpName"/>&nbsp;
					<font face="Courier" size="2">专家姓名：</font>
					<input type="text" name="expertName"/>&nbsp;
					<font face="Courier" size="2">管理栏目：</font><select name="type_id">
							<%
								Map<Integer, List<String>> valuec = PryFactory.getPryQueryAllDao().getEssayType();
							
								for(int id : valuec.keySet())
								{
									out.println("<option value='" + id +"'>");
									out.println(valuec.get(id).get(0));
									out.println("</option>");
								}							
							%>
							</select>&nbsp;&nbsp;&nbsp;
							<input type="submit" value="查询"/>
					</form>
				</td>
			</tr>
			
			<tr>
				<td width="600px" align="center">
				
					<table width="700px" style="font-size:11px" border="0">
	  	  				<tr>
	  	  					<td align="center" width="10%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" face="Courier" color="blue">用户名</font>
	  	  					</td>
	  	  					<td align="center" width="10%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" face="Courier" color="blue">姓名</font>
	  	  					</td>
	  	  					<td align="center" width="10%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" face="Courier" color="blue">职称</font>
	  	  					</td>
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" face="Courier" color="blue">管理栏目</font>
	  	  					</td>	  	  					
	  	  					<td align="center" width="25%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" face="Courier" color="blue">电子邮件</font>
	  	  					</td>
	  	  					<td align="center" width="20%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" face="Courier" color="blue">联系电话</font>
	  	  					</td>
	  	  					<td align="center" width="10%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" face="Courier" color="blue">删除</font>
	  	  					</td>
	  	  					
	  	  				</tr>
	  	  				
	  	  				 <%

							 Map<Integer, List<String>> value = PryFactory.getPryQueryAllDao().getExpertAll();
	  	  				 		
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
	  								out.println(value.get(x).get(3));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(7));
	  								out.println("</font>");
	  								
	  								out.println("</td>");	  								
	  									  
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(5));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(6));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/host/delete/deleteExpert.jsp?expertId=" + x + ">删除</a>");
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
	  								out.println(listValue.get(i).get(3));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(7));
	  								out.println("</font>");
	  								
	  								out.println("</td>");	  								
	  									  
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(5));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(6));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/host/delete/deleteExpert.jsp?expertId=" + listKey.get(i) + ">删除</a>");
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
									out.println("<a href=expertAdmin.jsp?pageNow="+(pageNow-1)+">上一页</a>");
								}
								//显示超链接
								for(int i=1;i<=pageCount;i++)
								{
									out.println("<a href=expertAdmin.jsp?pageNow="+i+">["+i+"]</a>");
								}
								//下一页
								if(pageNow!=pageCount)
								{
									out.println("<a href=expertAdmin.jsp?pageNow="+(pageNow+1)+">下一页</a>");
								}
							%>
	  	  				</font>
	  	  				</td>	  	  							  	  				
	  	  		</table>				
				</td>
			</tr>
			<tr>
				<td align="center">
				 
					<form action="/sys/Servlet" method="post"> 
	  	  		<table height="130px" width="700px" style="font-size:11px" border="0">
	  	  				<tr>
	  	  					<td align="center" width="15%" bgcolor="lemonchiffon">
	  	  						<font size="3" face="Courier" color="blue">用户名</font>
	  	  					</td>
	  	  					<td>
	  	  						<input type="text" name="expertpName" size="30"/>  	  						
	  	  					</td>
	  	  				</tr>
	  	  				<tr>
	  	  					<td align="center" width="15%" bgcolor="lemonchiffon">
	  	  						<font size="3" face="Courier" color="blue">姓名</font>
	  	  					</td>
	  	  					<td>
	  	  						<input type="text" name="expertName" size="30"/>  	  						
	  	  					</td>
	  	  				</tr>
	  	  				<tr>
	  	  					<td align="center" width="15%" bgcolor="lemonchiffon">
	  	  						<font size="3" face="Courier" color="blue">管理栏目</font>
	  	  					</td>
	  	  					<td>
	  	  						<select name="type_id">
							<%
								Map<Integer, List<String>> valuex = PryFactory.getPryQueryAllDao().getEssayType();
							
								for(int id : valuex.keySet())
								{
									out.println("<option value='" + id +"'>");
									out.println(valuex.get(id).get(0));
									out.println("</option>");
								}							
							%>
							</select>						
	  	  					</td>
	  	  				</tr>		  	  			 	  				  	  				
  	  				<tr>  	  											
  	  					<td colspan="2">
  	  					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  	  						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  	  						<input type="submit" value="添加"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	
  	  						<input  type="reset" value="取消"/>	
  	  					</td>
  	  				</tr>
	  	  		</table>
  			</form>
				
			</td>
			</tr>
		</table>
		</center>
    		
    		</div></td>
    	</tr>
    </table>
	
</body>
</html>

