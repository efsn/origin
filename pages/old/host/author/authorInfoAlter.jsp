<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>    


<html>
<head>

<title>Author Info Alter</title>
</head>
<body>					
    			<center>
    				<table width="50%" style="font-size:11px" border="0">
		    				<tr >
		    					<td width="100%" align="center" colspan="2">		    						
		    						<p><font size="5" face="楷体" color="blue">注册信息</font></p>
		    					</td>
		    				</tr>
		    				
		    				<tr>
		    					<td width="25%" align="center">		    						
		    						<p><font size="3" color="blue">姓名</font></p>
		    					</td>
		    					<td width="60%" align="center">		    						
		    					<p><font size="3">
								<%= ((ArrayList<String>)session.getAttribute("list")).get(0) %>
								</font></p>
		    					</td>
		    				</tr>
		    				<tr>
		    					<td width="25%" align="center">		    						
		    						<p><font size="3" color="blue">联系地址</font></p>
		    					</td>
		    					<td width="60%" align="center">		    						
		    					<p><font size="3">
								<%= ((ArrayList<String>)session.getAttribute("list")).get(1) %>
								</font></p>
		    					</td>
		    				</tr>
		    				<tr>
		    					<td width="25%" align="center">		    						
		    						<p><font size="3" color="blue">Email</font></p>
		    					</td>
		    					<td width="60%" align="center">		    						
		    					<p><font size="3">
								<%= ((ArrayList<String>)session.getAttribute("list")).get(2) %>
								</font></p>
		    					</td>
		    				</tr>
		    				
		    				<tr>
		    					<td width="25%" align="center">		    						
		    						<p><font size="3" color="blue">电话</font></p>
		    					</td>
		    					<td width="60%" align="center">		    						
		    						<p><font size="3">
		    						<%= ((ArrayList<String>)session.getAttribute("list")).get(3) %>
		    						</font></p>
		    					</td>
		    				</tr>			
		    				<tr>
		    					<td width="25%" align="center">		    						
		    						<p><font size="3" face="Courier" color="blue">备注</font></p>
		    					</td>
		    					<td width="60%" align="center">		    						
		    					<p><font size="3">
		    					<%
		    						if(null != ((ArrayList<String>)session.getAttribute("list")).get(4))
		    						{
		    						    out.println(((ArrayList<String>)session.getAttribute("list")).get(4));
		    						}
		    						else
		    						{
		    						    out.println("没有备注");
		    						}
		    					%>
								</font></p>
		    					</td>
		    				</tr>					
		    			</table>
		    			</center>
						<br/>
    		<!-- 分割线 -->	
    
    			<form action="/sys/InfoUpdateServlet" method="post">	
				<center>
				<table width="50%" style="font-size:11px" border="0">
		    				<tr >
		    					<td width="100%" align="center" colspan="3">		    						
		    						<p><font size="5" face="楷体" color="blue">信息修改</font></p>
		    					</td>
		    				</tr>
		    				
		    				<tr>
		    					<td width="100" align="center">		    						
		    						<p><font size="3" color="blue">姓名</font></p>
		    					</td>
		    					<td>		    						
		    						<input type="text" name="userName" size="35"/>
		    					</td>
		    					
		    					<td>
		    						<input type="checkbox" name="checkUserName" value="checkUserName"/>
		    					</td>
		    					
		    				</tr>
		    				<tr>
		    					<td width="30" align="center">		    						
		    						<p><font size="3" color="blue">联系地址</font></p>
		    					</td>
		    					<td>		    								    					
								<input type="text" name="address" size="35"/>								
		    					</td>
		    					
		    					<td>
		    						<input type="checkbox" name="checkAddress" value="checkAddress"/>
		    					</td>
		    					
		    				</tr>
		    				<tr>
		    					<td align="center">		    						
		    						<p><font size="3" color="blue">Email</font></p>
		    					</td>
		    					<td>		    						
								<input type="text" name="email" size="35"/>&nbsp;	
		    					</td>
		    					
		    					<td>
		    						<input type="checkbox" name="checkEmail" value="checkEmail"/>
		    					</td>
		    					
		    					
		    				</tr>
		    				
		    				<tr>
		    					<td align="center">		    						
		    						<p><font size="3" color="blue">电话</font></p>
		    					</td>
		    					<td>		    						
		    						<input type="text" name="phone" size="35"/>&nbsp;
		    					</td>
		    					
		    					<td>
		    						<input type="checkbox" name="checkPhone" value="checkPhone"/>
		    					</td>
		    					
		    				</tr>			
		    				<tr>
		    					<td align="center">		    						
		    						<p><font size="3" color="blue">备注</font></p>
		    					</td>
		    					<td>		    						
		    					<input type="text" name="mark" size="35"/>
		    					</td>		    					
		    					<td>
		    						<input type="checkbox" name="checkMark" value="checkMark"/>
		    					</td>		    					
		    				</tr>
		    				
		    				<tr>
		    					<td colspan="3" align="center">
		    						<input type="submit" value="修改"/>
									&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		    						<input type="reset" value="取消"/>
		    					</td>
		    				</tr>				
		    			</table> 
    				</center>
    			</form>				
	</body>
</html>