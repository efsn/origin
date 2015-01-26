<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<html>
	<head>
		<title>
			Welcome to here!`
		</title>
		<link rel="stylesheet" type="text/css" href="/sys/css/a.css" />
	</head>


	<body>
		<table border="0" width="100%" height="100%">
    	<tr>
    		<td colspan="2" height="10%"><div style="border-top: 1px solid blue;border-bottom: 1px solid blue; width: 100%; height:100%">
    		
    			 
    			
    			<table height="100%" width="100%" style="font-size:11px" border="0">
		    				<tr>
		    				<td>
							<img src="/sys/image/background/tg-logo.jpg"></img>
		    				</td>		    							    						
		    					<td width="30%" align="right">
		    						<p><font size="3" face="楷体">
		    							欢迎进入<font color="blue">专家首页</font>,&nbsp;
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

				<table height="50%" width="100%" style="font-size:11px" border="0" borderColor="#ffffff">
							<col bgcolor="#dcdcdc"/><col bgcolor="#dcdcdc"/>
		    				<tr>
		    					<td width="15%" align="center">
		    						<a href = "/sys/host/expert/expertInfoAlter.jsp" target="main">
		    						<p><font size="3" face="Courier" color="blue">查看或修改个人信息</font></p>
		    						</a>
		    					</td>
		    				</tr>		    							
		    				<tr>
		    					<td align="center">
		    						<a href="/sys/host/essay/essayCheckOfExpert.jsp" target="main">
		    						<p><font size="3" face="Courier" color="blue">稿件审核</font></p>
		    						</a>
		    					</td>
		    				</tr>
		    				<tr>
		    					<td align="center">
		    						<a href="/sys/host/message/messageFromExpert.jsp" target="main">
		    						<p><font size="3" face="Courier" color="blue">消息处理</font></p>
		    						</a>
		    					</td>
		    				</tr>
		    				<tr>
		    					<td align="center">
		    						<a href="/sys/host/readNews.jsp" target="main">
		    						<p><font size="3" face="Courier" color="blue">浏览新闻</font></p>
		    						</a>		
		    					</td>
		    				</tr>	
		    				
		    				<tr>
		    					<td align="center">
		    						<a href="/sys/host/expert/expertPwdAlter.jsp" target="main">
		    						<p><font size="3" face="Courier" color="blue">修改密码</font></p>
		    						</a>		
		    					</td>
		    				</tr>		    				
		    					
		    					<tr>
		    					<td align="center">
		    						<a href="/sys/index.jsp">
		    						<p><font size="3" face="Courier" color="blue">退出</font></p>
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