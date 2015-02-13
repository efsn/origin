<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="f"%>


<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Error</title>
	</head>
	<body>
<%-- 		<form action="/origin/ee.do"> --%>
			<f:form commandName="command">
				<f:errors path="*" cssStyle="color:red"/>
			bool:<f:input path="bool"/><br/>
			phoneNumber:<f:input path="phoneNumber"/><br/>
			date:<f:input path="date"/><br/>
			<input type="submit" value="Submit"/>
			</f:form><br/>
	</body>
</html>