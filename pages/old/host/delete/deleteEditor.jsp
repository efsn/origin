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
<title>Delete Editor!'</title>
</head>
<body>

		<%
			String editorId = request.getParameter("editorId");
			TB_Editor editor = new TB_Editor();
			if(null != editorId)
			{
				editor.setEditor_id(Integer.parseInt(editorId));
			}
						
			if(PryFactory.getPryDeleteDao().deleteEditor(editor))
			{
				response.sendRedirect("/sys/host/editor/editorAdmin.jsp");
			}
		%>


</body>
</html>