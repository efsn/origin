<%@ page language="java" import="java.util.*" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="sys.dao.*" %>
<%@ page import="sys.dao.impl.*" %>
<%@ page import="sys.dao.proxy.*" %>
<%@ page import="sys.factory.*" %>
<%@ page import="sys.dbc.*" %>


<html>
<head>
	<title>Author Detail!'</title>
</head>
<body background="/sys/image/bg.jpg">
	
	<table border="0" width="100%" height="100%">
    	<tr>
    		<td colspan="2" height="10%"><div style="border: 1px solid; width: 100%; height:100%">
		
    			<table height="100%" width="100%" style="font-size:11px" border="0" borderColor="#ffffff">
							<col bgcolor="#dcdcdc"/><col bgcolor="#dcdcdc"/>
		    				<tr>
		    					<td width="70%" align="center">
		    						<p><font size="6" face="Courier" color="green">
		    							作者信息
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
    		<td><div style="border: 1px solid; width: 100%; height:100%">
    		
    				<table height="60px" width="100%">
  			<tr>  			
			<td align="right">
			<font size="3">
				<a href="/sys/host/author/authorAdmin.jsp">返回</a>
				</font>
			</td>
			</tr>	
				
  		</table>
		<center>
	  	  	<%!
	  	  		List<String> value = null;
	  	  	
	  	  	%>
	  	  	
	  	  	<%
			  	  value = PryFactory.getPryQueryAllDao().getAuthorAll().get(
			  				Integer.parseInt(request.getParameter("authorId")));
	  	  	%>
	  	  		<table width="600px" style="font-size:11px" border="1" borderColor="#ffffff">
					<col bgcolor="#dcdcdc"/><col bgcolor="#dcdcdc"/>
	  	  				<tr>
	  	  					<td align="center" width="25%">
	  	  					<p>
								<font size="3" face="Courier" color="blue">用户名</font>
							</p>	  	  						
	  	  					</td>
	  	  					<td>
	  	  						<font size="3" color="white"><%= value.get(0) %></font>
	  	  					</td>
	  	  				</tr>
	  	  				<tr>
	  	  					<td align="center">
	  	  					<p>
								<font size="3" face="Courier" color="blue">姓名</font>
							</p>	  	  						
	  	  					</td>
	  	  					<td>
	  	  						<font size="3" color="white"><%= value.get(2) %></font>
	  	  					</td>
	  	  				</tr>
	  	  				<tr>
	  	  					<td align="center">		
	  	  					<p>
								<font size="3" face="Courier" color="blue">联系地址</font>
							</p>
	  	  					</td>
	  	  					<td>
	  	  						<font size="3" color="white"><%= value.get(3) %></font>
	  	  					</td>	
	  	  				</tr>	  	  				
	  	  				<tr>
  	  					<td align="center">
  	  					<p>
							<font size="3" face="Courier" color="blue">电子邮件</font>
						</p>  	  						
  	  					</td>
  	  					<td>
  	  						<font size="3" color="white"><%= value.get(4) %></font>
  	  					</td>
  	  				</tr>
  	  				<tr>
  	  					<td align="center">
  	  					<p>
							<font size="3" face="Courier" color="blue">联系电话</font>
						</p> 		
  	  					</td>
  	  					<td>
  	  						<font size="3" color="white"><%= value.get(5) %></font>	
  	  					</td>
  	  				</tr>  				  	  				
  	  				<tr>
  	  					<td align="center">
  	  					<p>
							<font size="3" face="Courier" color="blue">备注</font>
						</p>	
  	  					</td>
  	  					<td>
  	  						<font size="3" color="white">
  	  						<%if(null != value.get(6)) 
  	  						{
  	  						%>
  	  						<%= value.get(6) %>
  	  						<%}
  	  						else
  	  						{%>
  	  						<%= "无备注" %>
  	  						<%} %>
  	  						</font>
  	  					</td>
  	  				</tr>  	  				
	  	  		</table>

  		</center>
    		
    		</div></td>
    	</tr>
    </table>
	
</body>
</html>