<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="f" %>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Create User</title>
	</head>
	<body>
	<p>Create user</p>
		<f:form commandName="command" action="create.do" method="POST">
			<input type="hidden" name="action" value="doCreate"/>
			Name:<f:input path="username"/>
			<f:errors path="username" cssStyle="color:red"/><br/>
			Password:<f:input path="password"/><br/>
			Email:<f:input path="email"/>
			<input type="submit" name="submit" value="Create"/>
		</f:form>
		<a href="${pageContext.request.contextPath}/jsp/user/list.do"/>List</a>
	</body>
</html>