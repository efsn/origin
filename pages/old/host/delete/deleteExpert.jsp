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
<title>Delete Expert!'</title>
</head>
<body>

		<%
			String expertId = request.getParameter("expertId");
			TB_Expert expert = new TB_Expert();
			if(null != expertId)
			{
				expert.setExpert_id(Integer.parseInt(expertId));
			}
						
			if(PryFactory.getPryDeleteDao().deleteExpert(expert))
			{
				response.sendRedirect("/sys/host/expert/expertAdmin.jsp");
			}
		%>


</body>
</html>