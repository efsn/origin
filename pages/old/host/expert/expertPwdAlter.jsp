<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<html>
<head>
	<title>Author Pwd Alter!'</title>
	
	<script type="text/javascript">		
  	
  		function isValid()
  		{
  			var tpass = document.getElementsByName("expertPW0")[0];
  			var pass = document.getElementsByName("expertPW1")[0];
  			var repass = document.getElementsByName("expertPW2")[0]; 			  			
  			
  			if("" == tpass.value || null == tpass.value)
  			{
  				alert("原始密码不能为空!");
  				return false;
  			}
  			
  			if(tpass.value.length < 5 || tpass.value.length > 17)
  			{
  				alert("原始密码的长度必须在5到16之间!");
  				return false;
  			}
  			  			
  			if("" == pass.value || null == pass.value)
  			{
  				alert("新密码不能为空!");
  				return false;
  			}
  			
  			if(pass.value.length < 5 || pass.value.length > 17)
  			{
  				alert("新密码的长度必须在5到16之间!");
  				return false;
  			}
  			
  			if(pass.value == tpass.value)
  			{
  				alert("新密码不能与原始密码相同");
  				return false;
  			}
  			
  			if("" == repass.value || null == repass.value)
  			{
  				alert("第二次输入的密码不能为空!");
  				return false;
  			}		  		  			  			
  			
  			if(pass.value != repass.value)
  			{
  				alert("两次输入的密码不同!");
  				return false;
  			}
  			  			
  			return true;	
  			
  		}
  	
  	
  	</script>

</head>
<body>		
				<br/><br/><br/>
				<form onsubmit="return isValid()" action="/sys/PwdAlterServlet" method="post"> 
				<center>
	  	  		<table height="210px" width="600px" style="font-size:11px" border="0">																
	  	  				<tr>
	  	  					<td align="center">
	  	  						<font color="blue" face="楷体" size="3">原始密码</font>
	  	  					</td>
	  	  					<td>
	  	  						<input type="password" name="expertPW0" size="30"/>&nbsp;
	  	  					</td>
	  	  				</tr>
	  	  				<tr>
	  	  					<td align="center">
	  	  						<font color="blue" face="楷体" size="3">登陆密码</font>
	  	  					</td>
	  	  					<td>
	  	  						<input type="password" name="expertPW1" size="30"/>&nbsp;
	  	  						<font size="2">密码6-20个字符，请不要和登陆名相同</font>
	  	  					</td>
	  	  				</tr>
	  	  				<tr>
	  	  					<td align="center">
	  	  						<font color="blue" face="楷体" size="3">确认密码</font>
	  	  					</td>
	  	  					<td>
	  	  						<input type="password" name="expertPW2" size="30"/>
	  	  					</td>	
	  	  				</tr>
	  	  				<tr>	  	  					
	  	  					<td colspan="2">
	  	  					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	  	  					&nbsp;&nbsp;
	  	  						<input type="submit" value="确认"/>
	  	  						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	  	  						<input type="reset" value="重置">
	  	  					</td>
	  	  				</tr>			  	  				
	  	  			</table>
	  	  			</center>
	  	  			</form>	   		

</html>