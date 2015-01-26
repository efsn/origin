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
  	   	Layout Assortment!'
  	</title>
  	<link rel="stylesheet" type="text/css" href="/sys/css/a.css" />
  	<script type="text/javascript">		
  	
  		function isValid()
  		{
  			var essayId = document.getElementsByName("essayId")[0];	
  			var publish_time = document.getElementsByName("publish_time")[0];	
  			var publish_money = document.getElementsByName("publish_money")[0];  			
  			
  			if("" == essayId.value || null == essayId.value)
  			{
  				alert("序号不能为空!");
  				return false;
  			}
  			if("" == publish_time.value || null == publish_time.value)
  			{
  				alert("发表期次不能为空!");
  				return false;
  			}
  			if("" == publish_money.value || null == publish_money.value)
  			{
  				alert("版面费用不能为空!");
  				return false;
  			}
  			
  			return true;	
  			
  		}
  	
  	
  	</script>

  </head>
  
  <body>
		
		<center>
		<table width="700px" style="font-size:11px" border="0">
			<tr>				
				<td width="100%" align="center">
					<form action="" method="post">
					<font size="3" face="Courier">稿件名：</font>
					<input type="text" name="expertpName"/>&nbsp;&nbsp;&nbsp;
					<font size="3" face="Courier">作者：</font>
					<input type="text" name="expertName"/>&nbsp;&nbsp;&nbsp;
					&nbsp;&nbsp;&nbsp;
					<input type="submit" value="查询"/>
					</form>
				</td>
			</tr>
			
			<tr>
				<td width="600px" align="center">
				
					<table width="700px" style="font-size:11px" border="0">
	  	  				<tr>
	  	  					<td align="center" width="8%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" face="Courier" color="blue">序号</font>	
	  	  					</td>
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" face="Courier" color="blue">稿件名</font>		
	  	  					</td>
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" face="Courier" color="blue">栏目</font>		
	  	  					</td>
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  					<font size="3" face="Courier" color="blue">作者</font> 						
	  	  					</td>
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" face="Courier" color="blue">发表期次</font>  
	  	  					</td>
	  	  					<td align="center" width="17%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" face="Courier" color="blue">版面费用</font>
	  	  					</td>
	  	  					<td align="center" width="15%" height="30" bgcolor="lemonchiffon">
	  	  						<font size="3" face="Courier" color="blue">缴费状态</font>
	  	  					</td>	  	  						  	  						  	  						  	  					
	  	  				</tr>
	  	  				
	  	  				 <%

							 Map<Integer, List<String>> value = PryFactory.getPryQueryAllDao().getEssayCheckedByAdmin();
	  	  				 		
	  	  					 int param = 0;
	  	  				 	 if(null != request.getParameter("pageNow"))
	  	  				 	 {
	  	  				 		param = Integer.parseInt(request.getParameter("pageNow"));
	  	  				 	 } 
						  	 int pageSize = 5;
						  	 int pageNow = 1;
						  	 int pageCount;//total pages
						  	 int rowCount = value.keySet().size();//total rows
						  	 List<Integer> listKey = new ArrayList<Integer>();
						  	 List<List<String>> listValue = new ArrayList<List<String>>();
						  
						  	 if(0 == rowCount%pageSize)
						  	 {
						  		pageCount = rowCount/pageSize; 
						  	 }
						  	 else
						  	 {
						  		 pageCount = rowCount/pageSize + 1 ;
						  	 } 
						  	 
						  	 for(Integer key : value.keySet())
						  	 {
						  		 listKey.add(key);
						  	 }
						  	 
						  	 for(List<String> str : value.values())
						  	 {
							 	listValue.add(str);
						 	 }
						  	 
						 	if(0 != param)
	  	  					 {
						 		pageNow = param; 
	  	  					 }							  	 						  	 						  	 
						  	 
						 	if(value.isEmpty() || listValue.isEmpty())
						 	{
						 	    response.sendRedirect("/sys/error/noSys.jsp");
						 	    return;
						 	}
						 	
						  	 if(pageNow == pageCount)
						  	 {
						  		 for(int j = 0; j < (pageCount-1)*pageSize; j++)
						  		 {
						  			 value.remove(listKey.get(j));
						  		 }
						  		 
						  		 for(Integer x : value.keySet())
						  		 {
									out.println("<tr>");
							  					
									out.println("<td height=" + "30 align=" + "center" + ">");
									out.println("<font size='3'>");
	  								out.println(x);
	  								out.println("</font>");
	  								
	  								out.println("</td>");
									
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(0));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(5));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(1));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(2));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(3));
	  								out.println("</font>");
	  								
	  								out.println("</td>");	  								
	  									  
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(value.get(x).get(4));
	  								out.println("</font>");
	  								
	  								out.println("</td>");	  									  									  								
	  									  								
	  								out.println("</tr>"); 
						  		 }
						  	 }
						  	 else
						  	 {
						  		for(int i = (pageNow-1)*pageSize; i < pageNow*pageSize; i++)
							  	 {
						  			out.println("<tr>");
								  		
						  			out.println("<td height=" + "30 align=" + "center" + ">");
						  			out.println("<font size='3'>");
						  			out.println(listKey.get(i));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
						  			
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(0));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(5));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(1));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(2));
	  								out.println("</font>");
	  								
	  								out.println("</td>");
	  								
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(3));
	  								out.println("</font>");
	  								
	  								out.println("</td>");	  								
	  									  
	  								out.println("<td height=" + "30 align=" + "center" + ">");
	  								out.println("<font size='3'>");
	  								out.println(listValue.get(i).get(4));
	  								out.println("</font>");
	  								
	  								out.println("</td>");	  									  								
	  									  								
	  								out.println("</tr>"); 
						  	 } 
					  	 }						  	  	 
  					
	  	  				%>   	  	  				
	  	  				
	  	  				<td colspan = "7" align="center" height="30" bgcolor="lemonchiffon">
	  	  				<font size='3'>
	  	  					<%
								//上一页
								if(pageNow!=1)
								{
									out.println("<a href=layoutSet.jsp?pageNow="+(pageNow-1)+">上一页</a>");
								}
								//显示超链接
								for(int i=1;i<=pageCount;i++)
								{
									out.println("<a href=layoutSet.jsp?pageNow="+i+">["+i+"]</a>");
								}
								//下一页
								if(pageNow!=pageCount)
								{
									out.println("<a href=layoutSet.jsp?pageNow="+(pageNow+1)+">下一页</a>");
								}
							%>
	  	  				</font>
	  	  				</td>	  	  							  	  				
	  	  		</table>				
				</td>
			</tr>
			<tr>
				<td align="center">
				 
					<form onsubmit="return isValid()" action="/sys/LayoutSetServlet" method="post"> 
	  	  		<table height="170px" width="700px" style="font-size:11px" border="0">					
	  	  				<tr>
	  	  					<td align="center" width="15%" bgcolor="lemonchiffon">
	  	  						<font size='3' color='blue'>序号</font>
	  	  					</td>
	  	  					<td>
	  	  						<input type="text" name="essayId" size="30"/>  	  						
	  	  					</td>
	  	  				</tr>
	  	  				<tr>
	  	  					<td align="center" width="15%" bgcolor="lemonchiffon">
	  	  						<font size='3' color='blue'>发表期次</font>
	  	  					</td>
	  	  					<td>
	  	  						<input type="text" name="publish_time" size="30"/>  	  						
	  	  					</td>
	  	  				</tr>
	  	  			<tr>
	  	  				<td align="center" width="15%" bgcolor="lemonchiffon">
	  	  						<font size='3' color='blue'>版面费用</font>
	  	  				</td>
	  	  				<td>
	  	  					<input type="text" name="publish_money" size="30"/>&nbsp;<font size="2">(元)</font>  	  						
	  	  				</td>
	  	  			</tr>
	  	  			<tr>
	  	  				<td align="center" width="15%" bgcolor="lemonchiffon">
	  	  						<font size='3' color='blue'>缴费状态</font>
	  	  					</td>
	  	  					<td> 
	  	  						<select name="ispay">
	  	  							<option selected="selected" value="未缴费">未缴费</option>
	  	  							<option value="已缴费">已缴费</option>
	  	  						</select>	  						
	  	  					</td>
	  	  				</tr>	  	  			 	  				  	  				
  	  				<tr>  	  					 	  						
  	  					<td colspan='2'>
  	  						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  	  						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  	  						<input type="submit" value="添加"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	
  	  						<input  type="reset" value="取消"/>	
  	  					</td>
  	  				</tr>
	  	  		</table>
  			</form>
				
			</td>
			</tr>
		</table>
		</center>
    		
    		</div></td>
    	</tr>
    </table>
	</body>
</html>