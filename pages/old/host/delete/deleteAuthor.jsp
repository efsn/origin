<%@ page language="java" import="java.util.*" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="sys.dao.*" %>
<%@ page import="sys.dao.impl.*" %>
<%@ page import="sys.dao.proxy.*" %>
<%@ page import="sys.factory.*" %>
<%@ page import="sys.dbc.*" %>
<%@ page import="sys.vo.*" %>


<html>
<head>
<title>Delete Author!'</title>
</head>
<body>

		<%
			String authorId = request.getParameter("authorId");
			TB_Author author = new TB_Author();
			if(null != authorId)
			{
				author.setAuthor_id(Integer.parseInt(authorId));
			}
						
			if(PryFactory.getPryDeleteDao().deleteAuthor(author))
			{
				response.sendRedirect("/sys/host/author/authorAdmin.jsp");
			}
		%>


</body>
</html>