<%@ page language="java" import="java.util.*" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="sys.dao.*" %>
<%@ page import="sys.dao.impl.*" %>
<%@ page import="sys.dao.proxy.*" %>
<%@ page import="sys.factory.*" %>
<%@ page import="sys.dbc.*" %>

<html>
<head>
	<title>Essay Delete</title>
</head>
<body>
	<%
		int essayId = Integer.parseInt(request.getParameter("essayId"));
	
		if(PryFactory.getPryDeleteDao().deleteEssay(essayId))
		{
		    response.sendRedirect("/sys/host/essay/essayAuthor.jsp");
		}
		else
		{
		    out.println("删除失败");
		    out.println("<a href='/sys/host/author/authorHost.jsp'>返回主页</a>");
		}				
	
	%>
</body>
</html>