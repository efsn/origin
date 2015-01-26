<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>

<html>
  <head>
    <title>
    	Login!`
    </title>
	<link rel="stylesheet" type="text/css" href="/sys/css/a.css" />
	<script type="text/javascript">		
  	
  		function isValid()
  		{
  			var name = document.getElementsByName("username")[0];
  			var pass = document.getElementsByName("password")[0];
  			var user = document.getElementsByName("user")[0];
  			var code = document.getElementsByName("code")[0];
  			var x = document.getElementsByName("x")[0];
  			
  			
  			if("" == name.value || null == name.value)
  			{
  				alert("用户名不能为空!");
  				return false;
  			}
  			
  			if(name.value.length < 5 || name.value.length > 17)
  			{
  				alert("用户名的长度必须在5到16之间!");
  				return false;
  			}
  			
  			if("" == pass.value || null == pass.value)
  			{
  				alert("密码不能为空!");
  				return false;
  			}
  			
  			if(pass.value.length < 5 || pass.value.length > 17)
  			{
  				alert("密码的长度必须在5到16之间!");
  				return false;
  			}
  			
  			if("" == user.value || null == user.value)
  			{
  				alert("用户类型不能为空!");
  				return false;
  			}
  			
  			if(code.value != x.value)
  			{
  				alert("验证码错误");
  				return false;
  			}
  			 			
  			return true;	
  			
  		} 	
  	</script>

  </head>
  
  <body bgcolor="lightblue">  
  
  
  	<div style="float: left;width: 25%;height: 95%" >
  	
  		<table width="100%" height="100%">
  			<tr>
  				<td align="center">
  					<table >
  			<tr>
  				<td align="center">
  					<font color="green" size="7" face="黑体">电</font>
  				</td>
  			</tr>
  			<tr>
  				<td align="center">
  					<font color="green" size="7" face="黑体">子</font>
  				</td>
  			</tr>
  			<tr>
  				<td align="center">
  					<font color="green" size="7" face="黑体">投</font>
  				</td>
  			</tr>
  			<tr>
  				<td align="center">
  					<font color="green" size="7" face="黑体">稿</font>
  				</td>
  			</tr>
  			<tr>
  				<td align="center">
  					<font color="green" size="7" face="黑体">系</font>
  				</td>
  			</tr>
  			<tr>
  				<td align="center">
  					<font color="green" size="7" face="黑体">统</font>
  				</td>
  			</tr> 		
  		</table>
  				</td>
  			</tr>
  		</table>
  	
  		  	
  	
  	</div>
  
  	<div style="float: left;width: 75%;height: 95%;">
  	
  		<table width="100%" height="100%">
  			<tr>
  				<td align="center">
  					<form onsubmit="return isValid()" action="LoginServlet" method="post">
    	
    	<table height="160px" width="100%" style="font-size:11px" border="0" borderColor="#ffffff">
				<col bgcolor="#dcdcdc"/><col bgcolor="#dcdcdc"/>
			<tr>				
    			<td align="center">
	    			<p><font size="3">
						如果您是第一次使用，请先<a href = "authorRegister.jsp"><font size="3" color="red">注册</font></a>，忘记密码，
						点击此处，<a href = "/sys/findPassword.jsp"><font size="3" color="red">找回密码</font></a></font>
					</p>
    			</td>
    			<tr/>
    	</table>
    
    	<table height="180px" width="600px" style="font-size:11px" border="0" borderColor="#ffffff">
				<col bgcolor="#dcdcdc"/><col bgcolor="#dcdcdc"/>
    		
    		<tr>
    			<td align="center" width="">
    				<font size="3" face="楷体">用户名</font>
    			</td>
    			<td>
    				<input type="text" name="username" size="30"/>		
    			</td>
    		</tr>
    		<tr>
    			<td align="center">
    				<font size="3" face="楷体">密	码</font>
    			</td>
    			<td>
    				<input type="password" name="password" size="30"/>		
    			</td>
    		</tr>
    		<tr>
    			<td align="center">
    				<font size="3" face="楷体">登陆类型</font>
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
    			<td align="center">
    			<!-- 验证码，通过session动态实现-->
    				<font size="3" face="楷体">验证码</font>	
    			</td>
    			<td>
    				<input type="text" name="code" size="15"/>	
    				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    				<%
						int x = (int)(Math.random()*10000);
					%>
					<input type="hidden" value=<%= x %> name="x"/>
					<font size="4" color="red">
						<%= x %>
					</font>
    			</td>
    		</tr>	   	
    		<tr>
    			<td/>
    			<td>
    				<input type="submit" value="登陆"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					<input type="reset" value="取消"/>
    			</td>
    		</tr>
    			
    	</table>
    						
		</form>
  				</td>
  			</tr>
  		</table>

  	
  	</div>
  
  
  



  </body>
</html>

