<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>

<html>
  <head>
  	<title>
  	   	EditorRegister
  	</title>
  	<script type="text/javascript">	
	  	function isValid()
	  		{
	  			var name = document.getElementsByName("userName")[0];
	  			var pass = document.getElementsByName("editorPW1")[0];
	  			var repass = document.getElementsByName("editorPW2")[0];
	  			var phone = document.getElementsByName("phone")[0];
	  			var email = document.getElementsByName("email")[0];
	  			var tname = document.getElementsByName("name")[0];
	  			
	  			
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
	  			
	  			if(pass.value == name.value)
	  			{
	  				alert("用户名不能和密码相同!");
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
	  			
	  			if("" == tname.value || null == tname.value)
	  			{
	  				alert("真实姓名不能为空!");
	  				return false;
	  			}
	  			
	  			if("" == phone.value || null == phone.value)
	  			{
	  				alert("联系电话不能为空!");
	  				return false;
	  			}
	  			
	  			if("" == email.value || null == email.value)
	  			{
	  				alert("邮箱不能为空!");
	  				return false;
	  			}
	  			
	  			return true;	
	  			
	  		}
	  	
	  	
	  	</script>
  	
  </head>
  
 <body>
	
  		<center>
	  	  	<form onsubmit="return isValid()" action="/sys/EditorInsertServlet" method="post"> 
	  	  		<table height="230px" width="600px" style="font-size:11px" border="0">
	  	  				<tr>
	  	  					<td align="center">
	  	  						<font size="3" face="楷体" color="blue">用户名</font>
	  	  					</td>
	  	  					<td>
	  	  						<input type="text" name="userName" size="30"/>&nbsp;
	  	  						<font size="2">用户名由5-16字母、数字、汉字或者下划线组成</font>
	  	  					</td>
	  	  				</tr>
	  	  				<tr>
	  	  					<td align="center">
	  	  						<font size="3" face="楷体" color="blue">登陆密码</font>
	  	  					</td>
	  	  					<td>
	  	  						<input type="password" name="editorPW1" size="30"/>&nbsp;
	  	  						<font size="2">密码6-20个字符，请不要和登陆名相同</font>
	  	  					</td>
	  	  				</tr>
	  	  				<tr>
	  	  					<td align="center">
	  	  						<font size="3" face="楷体" color="blue">确认密码</font>
	  	  					</td>
	  	  					<td>
	  	  						<input type="password" name="editorPW2" size="30"/>
	  	  					</td>	
	  	  				</tr>	  	  				
	  	  				<tr>
  	  					<td align="center">
  	  						<font size="3" face="楷体" color="blue">真实姓名</font>
  	  					</td>
  	  					<td>
  	  						<input type="text" name="name" size="30"/>&nbsp;		
  	  						<font size="2">请输入您的真实姓名（必填）</font>
  	  					</td>
  	  				</tr>  	  				
  	  				<tr>
  	  					<td align="center">
  	  						<font size="3" face="楷体" color="blue">联系电话</font>
  	  					</td>
  	  					<td>
  	  						<input type="text" name="phone" size="30"/>&nbsp;		
  	  							<font size="2">请填写手机号码</font>
  	  					</td>
  	  				</tr>
  	  				<tr>
  	  					<td align="center">
  	  						<font size="3" face="楷体" color="blue">Email</font>
  	  					</td>
  	  					<td>
  	  						<input type="text" name="email" size="30"/>&nbsp;		
  	  					</td>
  	  				</tr>	
  	  				<tr>
  	  					<td/>
  	  					<td>
  	  						<input type="submit" value="注册"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	
  	  						<input  type="reset" value="取消"/>	
  	  					</td>
  	  				</tr>
	  	  		</table>
  			</form>
  		</center>	
	
</body>
</html>

