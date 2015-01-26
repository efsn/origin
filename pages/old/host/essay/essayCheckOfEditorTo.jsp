<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>

<%@ page import="sys.dao.*" %>
<%@ page import="sys.dao.impl.*" %>
<%@ page import="sys.dao.proxy.*" %>
<%@ page import="sys.factory.*" %>
<%@ page import="sys.dbc.*" %>



<html>
  <head>
  	<title>
  	   	Essay Check Of Editor To !'
  	</title>
  	
  	<style type="text/css">
			textarea{resize: none;}
	</style>
  	
  </head>
  
 <body>					
		<center>			
			<form  action="/sys/EssayCheckServlet" method="post">
				<table height="210px" width="650px" style="font-size:11px" border="0">
					<tr>
						<td align="center" width="18%">
						<p><font size="3" face="楷体" color="blue">稿件名:</font></p>	 							
						</td>
						<td>
							<font size="3">
							<%= request.getParameter("essayName") %>
							<% session.setAttribute("essay_name", request.getParameter("essayName")); %>
							</font>
						</td>
										
					</tr>										
					
					<tr>
						<td align="center">
						<p><font size="3" face="楷体" color="blue">处理结果:</font></p>			
						</td>
						<td>
							<select name="checkTo">
								<option value="not" selected="selected">未通过</option>
								<option value="expert">专家审稿</option>
							</select>
						</td>
					</tr>	
						
						<tr>
						<td align="center">
						<p><font size="3" face="楷体" color="blue">审稿意见:</font></p>	 							
						</td>
						<td>
							<textarea rows = "3" cols="43" name="checkMark"></textarea>&nbsp;<font size="2">（如果通过，必填）</font>
						</td>
										
					</tr>
						
						
					<tr>
						<td/>
						<td>
							<input type="submit" value="确定" size="60"/> &nbsp;<input type="reset" value="取消"/>	
						</td>
					</tr>				
				</table>				
			</form>
		</center> 				
    		
    		</div></td>
    	</tr>
    </table>
	</body>
</html>

