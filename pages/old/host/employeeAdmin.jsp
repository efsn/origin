<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<html>
<head>
	<title>Employee Admin!'</title>
</head>
<body>
	
	<table border="0" width="100%" height="100%">
    	<tr>
    		<td colspan="2" height="10%"><div style="border: 1px solid; width: 100%; height:100%">
		
    			<table height="100%" width="100%" style="font-size:11px" border="0" borderColor="#ffffff">
							<col bgcolor="#dcdcdc"/><col bgcolor="#dcdcdc"/>
		    				<tr>
		    					<td width="70%" align="center">
		    						<p><font size="6" face="Courier" color="green">
		    							人员管理
		    						</font></p>
		    					</td>
		    						
		    					<td width="30%" align="right">
		    						<p><font size="3" face="Courier" color="white">
		    							Welcome,&nbsp;
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
    		<td width="20%"><div style="border: 1px solid; width: 100%; height:100%">

				<table height="25%" width="100%" style="font-size:11px" border="0" borderColor="#ffffff">
							<col bgcolor="#dcdcdc"/><col bgcolor="#dcdcdc"/>
		    				<tr>
		    					<td rowspan="2" width="15%" align="center">	    						
		    						<p><font size="5" face="Courier" color="green">专家管理</font></p>
		    					</td>
		    					<td align="center" width="60%">
		    						<a href="/sys/host/expert/expertRegister.jsp">
		    						<font size="3" face="Courier" color="blue">
		    						专家注册
		    						</font></a>
		    					</td>
		    				</tr>
		    				<tr>
		    					<td align="center" width="60%">
		    						<a href="/sys/host/expert/expertAdmin.jsp">
		    						<font size="3" face="Courier" color="blue">
		    						专家查看
		    						</font></a>
		    					</td>
		    				</tr>				
				
		    			</table></br>
		    			
		    			<table height="25%" width="100%" style="font-size:11px" border="0" borderColor="#ffffff">
							<col bgcolor="#dcdcdc"/><col bgcolor="#dcdcdc"/>
		    				<tr>
		    					<td rowspan="2" width="15%" align="center">	    						
		    						<p><font size="5" face="Courier" color="green">编辑管理</font></p>
		    					</td>
		    					<td align="center" width="60%">
		    						<a href="/sys/host/editor/editorRegister.jsp">
		    						<font size="3" face="Courier" color="blue">
		    						编辑注册
		    						</font></a>
		    					</td>
		    				</tr>
		    				<tr>
		    					<td align="center" width="60%">
		    						<a href="/sys/host/editor/editorAdmin.jsp">
		    						<font size="3" face="Courier" color="blue">
		    						编辑查看
		    						</font></a>
		    					</td>
		    				</tr>				
				
		    			</table></br>

						<table height="20%" width="100%" style="font-size:11px" border="0" borderColor="#ffffff">
							<col bgcolor="#dcdcdc"/><col bgcolor="#dcdcdc"/>
							<tr>
								<td align="center">								
									<a href="/sys/host/author/authorAdmin.jsp">
		    						<font size="3" face="Courier" color="blue">
		    						作者管理
		    						</font></a>
								</td>	
							</tr>		
						</table>
				</div>
    		</td>
    		<td  background="/sys/image/index.jpg"><div style="border: 1px solid; width: 100%; height:100%">
    		
    				
    		
    		</div></td>
    	</tr>
    </table>
	
</body>
</html>