<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>

<html>
  <head>
    <title>
    	AuthorRegister!`
    </title>
	<link rel="stylesheet" type="text/css" href="/sys/css/a.css" />
	<script type="text/javascript">		
  	
  		function isValid()
  		{
  			var name = document.getElementsByName("userName")[0];
  			var pass = document.getElementsByName("authorPW1")[0];
  			var repass = document.getElementsByName("authorPW2")[0];
  			var phone = document.getElementsByName("phone")[0];
  			var address = document.getElementsByName("address")[0];
  			var email = document.getElementsByName("email")[0];
  			var tname = document.getElementsByName("authorName")[0];
  			
  			
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
  				alert("用户名不能和密码相同");
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
  			
  			if("" == address.value || null == address.value)
  			{
  				alert("地址不能为空!");
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
						<tr><td align="right"><font size="6" face="黑体" color="red">作</font></td></tr>
						<tr><td align="right"><font size="6" face="黑体" color="red">者</font></td></tr>
						<tr><td align="right"><font size="6" face="黑体" color="red">注</font></td></tr>
						<tr><td align="right"><font size="6" face="黑体" color="red">册</font></td></tr>
					</table>
				</td>
				<td>
					<center>
  		<table height="60px" width="600px">
  			<tr><td><p>
				<font size="4" face="Courier" color="blue">账号基本信息（必填）</font>
			</p></td></tr>		
  		</table>
  	  	<form onsubmit="return isValid()" action="authorRegister" method="post"> 
  	  		<table width="700px" style="font-size:11px" border="0" borderColor="#ffffff">
				<col bgcolor="#dcdcdc"/><col bgcolor="#dcdcdc"/>
  	  				<tr>
  	  					<td align="center">
  	  						<font size="3" face="楷体">用户名</font>
  	  					</td>
  	  					<td>
  	  						<input type="text" name="userName" size="30"/>&nbsp;
  	  						<font size="2">用户名由5-16字母、数字、汉字或者下划线组成</font>
  	  						<a href = "CheckName.jsp"><font size="2">检查用户名</font></a>	
  	  					</td>
  	  				</tr>
  	  				<tr>
  	  					<td align="center">
  	  						<font size="3" face="楷体">登陆密码</font>
  	  					</td>
  	  					<td>
  	  						<input type="password" name="authorPW1" size="30"/>&nbsp;
  	  						<font size="2">密码6-20个字符，请不要和登陆名相同</font>
  	  					</td>
  	  				</tr>
  	  				<tr>
  	  					<td align="center">
  	  						<font size="3" face="楷体">确认密码</font>
  	  					</td>
  	  					<td>
  	  						<input type="password" name="authorPW2" size="30"/>
  	  					</td>	
  	  				</tr>
  	  		</table>	
  	  						
  	  		<table height="60px" width="600px"> 	
  	  			<tr><td><p>
				<font size="4" face="Courier" color="blue">联系信息</font>
				</p></td></tr>  			
  	  		</table>				
  	  		
  	  		<table width="700px" style="font-size:11px" border="0" borderColor="#ffffff">
				<col bgcolor="#dcdcdc"/><col bgcolor="#dcdcdc"/>
  	  				<tr>
  	  					<td align="center">
  	  						<font size="3" face="楷体">真实姓名</font>
  	  					</td>
  	  					<td>
  	  						<input type="text" name="authorName" size="30"/>&nbsp;		
  	  						<font size="2">请输入您的真实姓名（必填）</font>
  	  					</td>
  	  				</tr>  	  				
  	  				<tr>
  	  					<td align="center">
  	  						<font size="3" face="楷体">通讯地址</font>
  	  					</td>
  	  					<td>
  	  						<input type="text" name="address" size="30"/>&nbsp;		
  	  						<font size="2">请填写市级以下的包括“区、县、街道、门牌”等详细地址</font>
  	  					</td>
  	  				</tr>
  	  				<tr>
  	  					<td align="center">
  	  						<font size="3" face="楷体">联系电话</font>
  	  					</td>
  	  					<td>
  	  						<input type="text" name="phone" size="30"/>&nbsp;		
  	  							<font size="2">请填写手机号码</font>
  	  					</td>
  	  				</tr>
  	  				<tr>
  	  					<td align="center">
  	  						<font size="3">Email</font>
  	  					</td>
  	  					<td>
  	  						<input type="text" name="email" size="30"/>&nbsp;		
  	  					</td>
  	  				</tr>	
  	  				<tr>
  	  					<td/>
  	  					<td>
  	  						<input type="submit" value="注册"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	
  	  						<input  type="reset" value="重置"/>	
  	  					</td>
  	  				</tr>	 	  					 	  					
  	  		</table>			
		</form>
	</center>
				</td>
			</tr>				
		</table>

  </body>
</html>
