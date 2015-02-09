<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Update user</title>
	</head>
	<body>
	<p>Create user</p>
		<form action="update.do" method="POST">
			<input type="hidden" name="action" value="doUpdate"/>
			Name:<input type="text" name="username" value="${command.username}"/>
			Password:<input type="password" name="password" value="${command.password}"/>
			Email:<input type="text" name="email" value="${command.email}"/>
			<input type="submit" name="submit" value="Update"/>
		</form>
		<a href="${pageContext.request.contextPath}/jsp/user/list.do"/>List</a>
	</body>
</html>