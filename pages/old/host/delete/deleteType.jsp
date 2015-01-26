<%@ page language="java" import="java.util.*" contentType="text/html;  charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="sys.dao.*" %>
<%@ page import="sys.dao.impl.*" %>
<%@ page import="sys.dao.proxy.*" %>
<%@ page import="sys.factory.*" %>
<%@ page import="sys.dbc.*" %>
<%@ page import="sys.vo.*" %>



<html>
<head>
	<title>Essay Type Delete!'</title>
</head>
<body>
		<%
			String typeS = request.getParameter("typeId");
			TB_EssayType type = new TB_EssayType();
			type.setType_id(Integer.parseInt(typeS));
			
			if(PryFactory.getPryTypeDao().typeDelete(type))
			{
				response.sendRedirect("/sys/host/typeAdmin.jsp");
			}
		%>
</body>
</html>