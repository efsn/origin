<%@ page language="java" import="java.util.*" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ page import="sys.dao.*" %>
<%@ page import="sys.dao.impl.*" %>
<%@ page import="sys.dao.proxy.*" %>
<%@ page import="sys.factory.*" %>
<%@ page import="sys.dbc.*" %>    
    
    
<html>
	<head>
		<title>
			Essay Info Insert!'
		</title>
		<style type="text/css">
			textarea{resize: none;}
		</style>
		
		<script type="text/javascript">		
  	
  		function isValid()
  		{
  			var name = document.getElementsByName("essay_pname")[0];
  			var keywords = document.getElementsByName("essay_keywords")[0];
  			var num = document.getElementsByName("essay_num")[0];
  			var content2 = document.getElementsByName("essay_content2")[0];
  			var authorInfo = document.getElementsByName("authorInfo")[0];
  			var typeId = document.getElementsByName("type_id")[0];
  			
  			
  			if("" == name.value || null == name.value)
  			{
  				alert("稿件名不能为空!");
  				return false;
  			}  			
  			
  			if("" == keywords.value || null == keywords.value)
  			{
  				alert("关键字不能为空!");
  				return false;
  			}  			  			  			 			
  			
  			if("" == num.value || null == num.value)
  			{
  				alert("字数不能为空!");
  				return false;
  			}		  		  			  			
  			
  			if("" == content2.value || null == content2.value)
  			{
  				alert("摘要不能为空!");
  				return false;
  			}
  			
  			if("" == authorInfo.value || null == authorInfo.value)
  			{
  				alert("作者信息不能为空!");
  				return false;
  			}
  			
  			if("" == typeId.value || null == typeId.value)
  			{
  				alert("栏目不能为空!");
  				return false;
  			}
  			
  			return true;	
  			
  		}
  	
  	
  	</script>				
		
	</head>
	
	<body>				 		
    	<!-- 在EssayInsertServlet之前得到session -->
		<%
			request.getSession();
		%>						
					
		<center>
			<form onsubmit="return isValid()" action="/sys/EssayInsertServlet" method="post">
				<table width="700px" style="font-size:11px" border="0">
					<tr>
						<td align="center">
							<font size="3" color="blue" face="楷体">英文标题</font>
						</td>
						<td>
							<input type="text" name="essay_pname" size="60"/>&nbsp;<font size="2">（必填，不超过10个字）</font>
						</td>
										
					</tr>
					
					<tr>
						<td align="center">
							<font size="3" color="blue" face="楷体">关键字</font>
						</td>
						<td>
							<input type="text" name="essay_keywords" size="60"/>&nbsp;<font size="2">（必填，不超过100字）</font>
						</td>
					</tr>
					
					<tr>
						<td align="center">
							<font size="3" color="blue" face="楷体">稿件字数</font>
						</td>
						<td>
							<input type="text" name="essay_num" size="60"/>&nbsp;<font size="2">（必填）</font>
						</td>
					</tr>
					
						<tr>
						<td align="center">
							<font size="3" color="blue" face="楷体">摘要</font>
						</td>
						<td>
							<textarea rows = "5" cols="43" name="essay_content2"></textarea>&nbsp;<font size="2">（必填，不超过300字）</font>
						</td>
					</tr>
						
						
					<tr>
						<td align="center">
							<font size="3" color="blue" face="楷体">作者信息</font>
						</td>
						<td>
							<textarea rows="5" cols="43" name="authorInfo"></textarea>&nbsp;<font size="2">（作者、稿件等相关信息）</font>
						</td>
					</tr>
						
					<tr>
						<td align="center">
							<font size="3" color="blue" face="楷体">稿件栏目</font>
						</td>
						<td>
							<select name="type_id">							
							<%
								Map<Integer, List<String>> value = PryFactory.getPryQueryAllDao().getEssayType();
							
								for(int id : value.keySet())
								{
									out.println("<option value='" + id +"'>");
									out.println(value.get(id).get(0));
									out.println("</option>");
								}							
							%>
							</select>&nbsp;<font size="2">（必填）</font>
						</td>
					</tr>	
						
					<tr>
						<td/>
						<td>
							<input type="submit" value="下一步" size="60"/> &nbsp;<input type="reset" value="取消"/>	
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