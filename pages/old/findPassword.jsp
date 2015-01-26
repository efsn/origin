<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>


<html>
<head>
	<title>Get PWD!'</title>
	<link rel="stylesheet" type="text/css" href="/sys/css/a.css" />
	<script type="text/javascript">		
  	
  		function isValid()
  		{
  			var name = document.getElementsByName("pname")[0];
  			
  			
  			if("" == name.value || null == name.value)
  			{
  				alert("用户名不能为空!");
  				return false;
  			}  			  			
  			 			
  			return true;	
  			
  		} 	
  	</script>
	
</head>
<body>
	<table width="100%" height="10%">
  		<tr><td>
  		<div style="border-top: 1px solid blue;border-bottom: 1px solid blue; width: 100%; height:100%">
  		
  			<table width="100%">
  			<tr>
  			<td width="40%">
			<img src="/sys/image/background/tg-logo.jpg"></img>
		    </td>
  			<td align="right"><a href ="/sys/index.jsp">返回首页</a></td></tr>		
  		</table> 			
  		</div>
  		</td></tr>
  		</table>
		
		<table width="100%">
			<tr>
				<td>
					<table width="200px">
						<tr><td align="right"><font size="6" face="黑体" color="red">密</font></td></tr>
						<tr><td align="right"><font size="6" face="黑体" color="red">码</font></td></tr>
						<tr><td align="right"><font size="6" face="黑体" color="red">找</font></td></tr>
						<tr><td align="right"><font size="6" face="黑体" color="red">回</font></td></tr>
					</table>
				</td>
				<td>
					<form onsubmit="return isValid()" action="/sys/FindPWDServlet" method="post">
				<table height="120px" width="300px" style="font-size:11px" border="0" borderColor="#ffffff">
							<col bgcolor="#dcdcdc"/><col bgcolor="#dcdcdc"/>
					<tr>
						<td align="center" width="30%">
						<strong><font size="3" face="楷体">用户名:</font></strong>		
						</td>
						<td>
						 	<input type="text" name="pname" size="30%"/>	
						</td>
					</tr>
					<tr>
		    			<td align="center">
		    				<strong><font size="3" face="楷体">登陆类型:</font></strong>		
		    			</td>	
		    			<td>
		    				<select name="user">
		    					<option value="author" selected="selected">投稿人</option>
		    					<option value="editor">编辑</option>
		    					<option value="expert">专家</option>
		    					<option value="admin">主编</option>
		    				</select>	
		    			</td>
		    		</tr>
		    		<tr>
		    			<td/>
		    			<td>
		    				<input type="submit" value="确认"/>
		    				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		    				<input type="reset" value="取消"/>
		    			</td>
		    		</tr>
				</table>
			</form>
				</td>
			</tr>				
		</table>
		
		<center>
			
			
				<%
					if(null != request.getAttribute("list") && null!= request.getAttribute("user"))
					{
					    out.println("<table height='120px' width='400px' style='font-size:11px' border='0' borderColor='#ffffff'> " +
									"<col bgcolor='#dcdcdc'/><col bgcolor='#dcdcdc'/>" +					
									"<tr><td align='center'><strong><font size='3' face='楷体'>登陆类型:</font></strong></td><td>");
					    out.println("<font size='3' color='blue'>");
					    out.println((String)request.getAttribute("user"));
					    out.println("</font>");
					    out.println("</td></tr><tr>" +
					    	"<td align='center' width='30%'><strong> <font size='3' face='楷体'> 用户名:</font></strong></td><td>");
					    out.println("<font size='3' color='blue'>");
					    out.println(((List<String>)request.getAttribute("list")).get(0));
					    out.println("</font>");
					    out.println("</td></tr>	<tr><td align='center' width='30%'><strong><font size='3' face='楷体'>密码:</font></strong></td><td>");
					    out.println("<font size='3' color='blue'>");
					    out.println(((List<String>)request.getAttribute("list")).get(1));
					    out.println("</font>");
					    out.println("</td></tr></table>");
					}
				%>	
	</center>
</body>
</html>