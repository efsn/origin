<%@ page language="java" import="java.util.*"  contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="sys.dao.*" %>
<%@ page import="sys.dao.impl.*" %>
<%@ page import="sys.dao.proxy.*" %>
<%@ page import="sys.factory.*" %>
<%@ page import="sys.dbc.*" %> 


<html>
<head>
	<title>Message From Expert!'</title>
	
	<style type="text/css">
			textarea{resize: none;}
	</style>
	
	<script type="text/javascript">	
	  	function isValid()
	  		{
	  			var message = document.getElementsByName("messagee")[0];
	  			
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
		<center>		
		<table height="100px" width="650px" style="font-size:11px" border="0">	
			<tr>
				<td width="100">
					<font size="3" color="blue">编辑部的消息</font>
				</td>
				<td>
					<!-- 显示消息 -->
					<font size="3" face="楷体">
					<%
						String message = PryFactory.getPryQueryDao().getMessage((String)session.getAttribute("user"), "expert").get("message");
						if(null == message || "".equals(message))
						{
							out.println("没有编辑部消息");
						}
						else
						{
							out.println(message);
						}
					%>
					</font>
				</td>
			</tr>
		
		
		
		
		<form onsubmit="return isValid()" action="/sys/MessageFromExpertServlet" method="post">	
		<table height="210px" width="650px" style="font-size:11px" border="0">									
			<tr>
				<td>
					&nbsp;<font size="3" color="blue">发送消息给编辑部</font>
														
				</td>
			</tr>																
			<tr>
				<td>
					<font size="3" color="green">消息：</font>
					<textarea rows="5" cols="43" name="messagee"></textarea>&nbsp;<font color="red">（300字以内）</font>
				</td>			
			</tr>
			<tr>
				<td>
					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					<input type="submit" value="发送"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					<input  type="reset" value="取消"/>
				</td>
			</tr>
		</form>
	</center>
    		
    		</div></td>
    	</tr>
    </table>





	</body>
</html>