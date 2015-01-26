<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<html>
	<head>
		<title>
			Welcome to here!`
		</title>
		<link rel="stylesheet" type="text/css" href="/sys/css/a.css" />
			<script type="text/javascript">
			function displaysys()
			{
				var type = document.getElementById("type");
				var pwd = document.getElementById("pwd");
				if(type.style.display == "none")
				{
					type.style.display="table-row"
				}
				else
				{
					type.style.display="none"
				}
				if(pwd.style.display == "none")
				{
					pwd.style.display="table-row"
				}
				else
				{
					pwd.style.display="none"
				}
				return false;
			}
			function displayem()
			{
				var em1 = document.getElementById("em1");
				var em2 = document.getElementById("em2");
				var em3 = document.getElementById("em3");
				var em4 = document.getElementById("em4");
				var em5 = document.getElementById("em5");
				if(em1.style.display == "none")
				{
					em1.style.display="table-row";
					em2.style.display="table-row";
					em3.style.display="table-row";
					em4.style.display="table-row";
					em5.style.display="table-row";
				}
				else
				{
					em1.style.display="none";
					em2.style.display="none";
					em3.style.display="none";
					em4.style.display="none";
					em5.style.display="none";
				}
				return false;
			}
			</script>
	</head>


	<body>
		
		<table border="0" width="100%" height="100%">
    	<tr>
    		<td colspan="2" height="10%"><div style="border-top: 1px solid blue;border-bottom: 1px solid blue; width: 100%; height:100%">
    		   			     			
    			<table height="100%" width="100%" style="font-size:11px" border="0" borderColor="#ffffff">
							<col bgcolor="#dcdcdc"/><col bgcolor="#dcdcdc"/>
		    				<tr>
		    				<td>
							<img src="/sys/image/background/tg-logo.jpg"></img>
		    				</td>		    							    						
		    					<td width="30%" align="right">
		    						<p><font size="4" face="楷体" color="blue">
		    							主编首页,&nbsp;
		    						</font>
		    						<font size="3" face="Courier" color="red">
		    						<%= session.getAttribute("user")%>
		    						</font></p>
		    					</td>
		    				</tr>   					
    			</table>
    		</div>
    		</td>
    	</tr>
    	<tr>
    		<td width="20%"><div style="border-right: 1px solid blue; width: 100%; height:100%">

				<table width="100%" style="font-size:11px" border="0" >
		    				<tr>
		    					<td align="center">
		    						<a href = "###" target="main" onclick="return displaysys(this)">
		    						<p><font size="3" face="Courier">系统设置</font></p>
		    						</a>
		    					</td>
		    				</tr>
		    				<tr style="display:none" id="type">
		    					<td align="center">
		    						<a href = "/sys/host/typeAdmin.jsp" target="main">
		    						<font size="2" face="Courier" color="blue">栏目管理</font>
		    						</a>
		    					</td>
		    				</tr>
		    				<tr style="display:none" id="pwd">
		    					<td align="center">
		    						<a href = "/sys/host/admin/adminPwdAlter.jsp" target="main">
		    						<font size="2" face="Courier" color="blue">密码修改</font>
		    						</a>
		    					</td>
		    				</tr>
		    				<tr>
		    					<td align="center">
		    						<a href="/sys/host/layoutSet.jsp" target="main">
		    						<p><font size="3" face="Courier">版面安排</font></p>
		    						</a>
		    					</td>
		    				</tr>				
		    				<tr>
		    					<td align="center">
		    						<a href="/sys/host/essay/essayCheckOfAdmin.jsp" target="main">
		    						<p><font size="3" face="Courier">稿件审核</font></p>
		    						</a>
		    					</td>
		    				</tr>
		    				<tr>
		    					<td align="center">
		    						<a href="###" target="main" onclick="return displayem(this)">
		    						<p><font size="3" face="Courier">人员管理</font></p>
		    						</a>
		    					</td>
		    				</tr>
		    				
		    				<tr style="display:none" id="em1">
		    					<td align="center">
		    						<a href="/sys/host/expert/expertRegister.jsp"  target="main">
		    						<font size="2" face="Courier" color="blue">
		    						专家注册
		    						</font></a>
		    					</td>
		    				</tr>
		    				
		    				<tr style="display:none" id="em2">
		    					<td align="center">
		    						<a href="/sys/host/expert/expertAdmin.jsp" target="main">
		    						<font size="2" face="Courier" color="blue">
		    						专家查看
		    						</font></a>
		    					</td>
		    				</tr>
		    				
		    				<tr style="display:none" id="em3">
		    					<td align="center">
		    						<a href="/sys/host/editor/editorRegister.jsp" target="main">
		    						<font size="2" face="Courier" color="blue">
		    						编辑注册
		    						</font></a>
		    					</td>
		    				</tr>
		    				
		    				<tr style="display:none" id="em4">
		    					<td align="center">
		    						<a href="/sys/host/editor/editorAdmin.jsp" target="main">
		    						<font size="2" face="Courier" color="blue">
		    						编辑查看
		    						</font>
		    						</a>
		    					</td>
		    				</tr>
		    				
		    				<tr style="display:none" id="em5">
		    					<td align="center">								
									<a href="/sys/host/author/authorAdmin.jsp" target="main">
		    						<font size="2" face="Courier" color="blue">
		    						作者管理
		    						</font></a>
								</td>
		    				</tr>
		    				
		    				<tr>
		    					<td align="center">
		    						<a href="/sys/host/message/messageFromAdmin.jsp" target="main">
		    						<p><font size="3" face="Courier">消息处理</font></p>
		    						</a>		
		    					</td>
		    				</tr>
		    				<tr>
		    					<td align="center">
		    						<a href="/sys/index.jsp">
		    						<p><font size="3" face="Courier">退出</font></p>
		    						</a>		
		    					</td>
		    				</tr>				
		    			</table> 
				</div>
    		</td>
    		<td>   		   		    			
    		<iframe name="main" frameborder="no" height="100%" width="100%" ></iframe>
    		</td>
    	</tr>
    </table>


	</body>
</html>