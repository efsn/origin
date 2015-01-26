<%@ page language="java" import="java.util.*"  contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="sys.dao.*" %>
<%@ page import="sys.dao.impl.*" %>
<%@ page import="sys.dao.proxy.*" %>
<%@ page import="sys.factory.*" %>
<%@ page import="sys.dbc.*" %> 


<html>
<head>
	<title>Message From Editor</title>
	<link rel="stylesheet" type="text/css" href="/sys/css/a.css" />
	<style type="text/css">
			textarea{resize: none;}
	</style>
	
	<script type="text/javascript">	
	  	function isValid()
	  		{
	  			var message = document.getElementsByName("message")[0];
	  			
	  			if("" == message.value || null == message.value)
	  			{
	  				alert("消息不能为空!");
	  				return false;
	  			}	  			
	  			
	  			return true;	
	  			
	  		}
	  	
	  	
	  	</script>
	
	
</head>
<body>	
		<p>
			<font size="6" face="楷体" color="red">消息发送</font>
		</p>	
		<center>
		<form onsubmit="return isValid()" action="/sys/MessageFromEditorServlet" method="post">	
		<table height="210px" width="650px" style="font-size:11px" border="0">
			<tr>
				<td>
					<font size="3" face="楷体" color="green">发给：</font>
					<select name="person">
						<option value="author" selected="selected">作者</option>
						<option value="expert">专家</option>
						<option value="admin">主编</option>
					</select>&nbsp;<font color="red" size="2">（必选）</font>
														
				</td>
			</tr>
			
			<tr>
				<td>
				&nbsp;<font color="blue" size="2">（作者必选）</font>
					<select name="author_pname">	
				
							<%
								Map<String, String> value = PryFactory.getPryQueryDao().getAuthor();
							
								for(String pname : value.keySet())
								{
									out.println("<option value='" + pname +"'>");
									out.println(pname);
									out.println("</option>");
								}							
							%>
							
					</select>
						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					
					&nbsp;<font color="blue" size="2">（专家必选）</font>
					<select name="expert_pname">	
				
							<%
								Map<String, String> valuese = PryFactory.getPryQueryDao().getExpert();
							
								for(String pname : valuese.keySet())
								{
									out.println("<option value='" + pname +"'>");
									out.println(pname);
									out.println("</option>");
								}							
							%>
							
					</select>
						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					
					&nbsp;<font color="blue" size="2">（主编必选）</font>
					<select name="admin_pname">	
				
							<%
								Map<String, String> valuesa = PryFactory.getPryQueryDao().getAdmin();
							
								for(String pname : valuesa.keySet())
								{
									out.println("<option value='" + pname +"'>");
									out.println(pname);
									out.println("</option>");
								}							
							%>							
					</select>									
				</td>			
			</tr>
			<tr>
				<td>
					<font size="3" face="楷体" color="green">消息：</font>
					<textarea rows="5" cols="43" name="message"></textarea>&nbsp;<font color="red" size="2">（300字以内）</font>
				</td>			
			</tr>
			<tr>
				<td>
					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					<input type="submit" value="发送"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					<input  type="reset" value="取消"/>
				</td>
			</tr>
		</table>	
		</form>
	</center>
	<p><font size="6" face="楷体" color="red">消息接受</font></p>
		<center>
		<table width="650px" style="font-size:11px" border="0">
			<tr>
				<td>
					<table width="650px" style="font-size:11px" border="0">
	  	  				<tr>
	  	  					<td align="center" width="25%" height="30" bgcolor="lemonchiffon">
	  	  					<p><font size="3" face="Courier" color="blue">部门/用户名</font></p>	    						
	  	  					</td>
	  	  					<td align="center" width="75%" height="30" bgcolor="lemonchiffon">
	  	  					<p><font size="3" face="Courier" color="blue">消息</font></p>	 	  						
	  	  					</td>
	  	  				</tr>	  	  				
	  	  				
	  	  				 <%
							String user = "author";
	  	  				 	String user1 = null;
	  	  				 	String other = request.getParameter("user");
	  	  				 
	  	  				 	if(null != other)
	  	  				 	{
	  	  				 		if(!user.equals(other))
	  	  				 		{
	  	  				 			user = other;
	  	  				 		}
	  	  				 	}	  	  				 		
	  	  				 			  	  			
							 Map<Integer, List<String>> values = PryFactory.getPryQueryAllDao().getMessageToEditor(user);
							 
							 if("author".equals(user))
	  	  				 	{
	  	  				 	    user1 = "投稿人";
	  	  				 	}
		  	  				if("expert".equals(user))
	  	  				 	{
	  	  				 	    user1 = "专家";
	  	  				 	}
			  	  			if("admin".equals(user))
	  	  				 	{
	  	  				 	    user1 = "主编";
	  	  				 	}
	  	  				 			  	  				 		  
							if(values.isEmpty())
	  	  				 	{
	  	  				 		response.sendRedirect("/sys/error/messageIsEmpty.jsp");
	  	  				 	}
	  	  				 
	  	  					 int param = 0;
	  	  				 	 if(null != request.getParameter("pageNow"))
	  	  				 	 {
	  	  				 		param = Integer.parseInt(request.getParameter("pageNow"));
	  	  				 	 } 
						  	 int pageSize = 3;
						  	 int pageNow = 1;
						  	 int pageCount;//total pages
						  	 int rowCount = values.keySet().size();//total rows
						  	 List<Integer> listKey = new ArrayList<Integer>();
						  	 List<List<String>> listvalues = new ArrayList<List<String>>();
						  
						  	 if(0 == rowCount%pageSize)
						  	 {
						  		pageCount = rowCount/pageSize; 
						  	 }
						  	 else
						  	 {
						  		 pageCount = rowCount/pageSize + 1 ;
						  	 } 
						  	 
						  	 for(Integer key : values.keySet())
						  	 {
						  		 listKey.add(key);
						  	 }
						  	 
						  	 for(List<String> str : values.values())
						  	 {
							 	listvalues.add(str);
						 	 }
						  	 
						 	if(0 != param)
	  	  					 {
						 		pageNow = param; 
	  	  					 }							  	 						  	 						  	 						  	 						 							 							 	
						 	
						  	 if(pageNow == pageCount)
						  	 {
						  		 for(int j = 0; j < (pageCount-1)*pageSize; j++)
						  		 {
						  			 values.remove(listKey.get(j));
						  		 }
						  		 
						  		 for(Integer x : values.keySet())
						  		 {
									out.println("<tr>");
							  									  		
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(user1 + "/" + values.get(x).get(0));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(values.get(x).get(1));
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
	  								out.println(user1 + "/" + listvalues.get(i).get(0));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listvalues.get(i).get(1));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  												
	  								out.println("</tr>"); 
						  	 } 
					  	 }						  	  	 
  					
	  	  				%>   	  	  				
	  	  				
	  	  				<td colspan = "3" align="center" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3">
	  	  					<%
								//上一页
								if(pageNow!=1)
								{
									if("author".equals(user))
									{
										out.println("<a href=messageFromEditor.jsp?user=author&pageNow="+(pageNow-1)+">上一页</a>");
									}
									if("expert".equals(user))
									{
										out.println("<a href=messageFromEditor.jsp?user=expert&pageNow="+(pageNow-1)+">上一页</a>");
									}
									if("admin".equals(user))
									{
										out.println("<a href=messageFromEditor.jsp?user=admin&pageNow="+(pageNow-1)+">上一页</a>");
									}
								}
								//显示超链接
								for(int i=1;i<=pageCount;i++)
								{
									if("author".equals(user))
									{
										out.println("<a href=messageFromEditor.jsp?user=author&pageNow="+i+">["+i+"]</a>");
									}
									if("expert".equals(user))
									{
										out.println("<a href=messageFromEditor.jsp?user=expert&pageNow="+i+">["+i+"]</a>");
									}
									if("admin".equals(user))
									{
										out.println("<a href=messageFromEditor.jsp?user=admin&pageNow="+i+">["+i+"]</a>");
									}
								}
								//下一页
								if(pageNow!=pageCount)
								{
									if("author".equals(user))
									{
										out.println("<a href=messageFromEditor.jsp?user=author&pageNow="+(pageNow+1)+">下一页</a>");
									}
									if("expert".equals(user))
									{
										out.println("<a href=messageFromEditor.jsp?user=expert&pageNow="+(pageNow+1)+">下一页</a>");
									}
									if("admin".equals(user))
									{									    
										out.println("<a href=messageFromEditor.jsp?user=admin&pageNow="+(pageNow+1)+">下一页</a>");
									}
								}
							%>
	  	  				</font>
	  	  				</td>	  	  							  	  				
	  	  		</table>
				</td>
			</tr>
			<tr>
				<td align="center" bgcolor="lemonchiffon">
					<font size="3">
					<a href="messageFromEditor.jsp?user=author">作者消息</a>
					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					<a href="messageFromEditor.jsp?user=expert">专家消息</a>
					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					<a href="messageFromEditor.jsp?user=admin">主编消息</a>
					</font>
				</td>
			</tr>
	</table>
	</center>			
	</body>
</html>