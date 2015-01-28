<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>

<%@ page import="org.blue.sys.dao.*" %>
<%@ page import="org.blue.sys.dao.impl.*" %>
<%@ page import="org.blue.sys.dao.proxy.*" %>
<%@ page import="org.blue.sys.factory.*" %>



<html>
  <head>
  	<title>
  	   	Type Admin!'
  	</title>
  	<link rel="stylesheet" type="text/css" href="/sys/css/a.css" />
  	<script type="text/javascript">		
  	
  		function isValid()
  		{
  			var typeName = document.getElementsByName("typeName")[0];		  			
  			
  			if("" == typeName.value || null == typeName.value)
  			{
  				alert("栏目名不能为空!");
  				return false;
  			}
	  			
  			return true;	
  			
  		}
  	
  	
  	</script>
  	
  </head>
  
  <body>
  			 <center>		
	  	  		<table width="600px" style="font-size:11px" border="0">					
	  	  				<tr>
	  	  					<td align="center" width="20%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" color="blue">栏目编号</font> 
	  	  					</td>
	  	  					<td align="center" width="40%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" color="blue">栏目名称</font>
	  	  					</td>
	  	  					<td align="center" width="20%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" color="blue">截止日期</font>
	  	  					</td>
	  	  					<td align="center" width="20%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" color="blue">删除</font>
	  	  					</td>
	  	  				</tr>
	  	  				
	  	  				 <%
  							 Map<Integer, String[]> value = PryFactory.getPryTypeDao().getAllType();
	  	  				 		
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
						  	 List<String[]> listValue = new ArrayList<String[]>();
						  
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
						  	 
						  	 for(String[] str : value.values())
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
	  								out.println(x);
	  								out.println("</font>");
	  								out.println("</td>");
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x)[0]);
	  								out.println("</font>");  								
	  								out.println("</td>");	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x)[1]);
	  								out.println("</font>");		  								
	  								out.println("</td>");
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/host/delete/deleteType.jsp?typeId="+ x + ">删除</a>");
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
	  								out.println(listKey.get(i));
	  								out.println("</font>");	  								
	  								out.println("</td>");
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i)[0]);
	  								out.println("</font>");	  								
	  								out.println("</td>");
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i)[1]);
	  								out.println("</font>");	  								
	  								out.println("</td>");
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println("<a href=" + "/sys/host/delete/deleteType.jsp?typeId=" + listKey.get(i) + ">删除</a>");
	  								out.println("</font>");	  								
	  								out.println("</td>");
	  								out.println("</tr>");	 
							  	 } 
						  	 }						  	  	 
  					
	  	  				%>   	  	  				
	  	  				
	  	  				<td colspan = "4" align="center" bgcolor="lemonchiffon">
	  	  				
	  	  				<font size='3'>	  									  								
	  	  					<%
								//上一页
								if(pageNow!=1)
								{
									out.println("<a href=typeAdmin.jsp?pageNow="+(pageNow-1)+">上一页</a>");
								}
								//显示超链接
								for(int i=1;i<=pageCount;i++)
								{
									out.println("<a href=typeAdmin.jsp?pageNow="+i+">["+i+"]</a>");
								}
								//下一页
								if(pageNow!=pageCount)
								{
									out.println("<a href=typeAdmin.jsp?pageNow="+(pageNow+1)+">下一页</a>");
								}
							%>
	  	  					</font>	
	  	  				</td>	  	  							  	  				
	  	  		</table>
  		 						
  		<br/>
	
  		
  		
	  	  	<form onsubmit="return isValid()" action="/sys/TypeServlet" method="post"> 
	  	  		<table height="80px" width="600px" style="font-size:11px" border="0">
	  	  				<tr>
	  	  					<td align="center" width="15%" bgcolor="lemonchiffon">
	  	  						<font size='3' color="blue">栏目名称</font>
	  	  					</td>
	  	  					<td>
	  	  						<input type="text" name="typeName" size="30"/>  	  						
	  	  					</td>
	  	  				</tr>	  	  			 	  				  	  				
  	  				<tr>  	  					 	  						
  	  					<td colspan = "2">
  	  						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  	  						&nbsp;&nbsp;&nbsp;
  	  						<input type="submit" value="添加"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	
  	  						<input  type="reset" value="取消"/>	
  	  					</td>
  	  				</tr>
	  	  		</table>
  			</form>
	</center>
</body>
  
</html>

