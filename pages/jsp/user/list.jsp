<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>List all users</title>
	</head>
	<body>
		<p>List all users</p>
		<table>
			<tr>
				<th>Username</th>
				<th>Password</th>
				<th>Email</th>
			</tr>
			
			<c:forEach items="${map}" var="entry">
				<tr>
					<td>${entry.value.username}</td>
					<td>${entry.value.password}</td>
					<td>${entry.value.email}</td>
				</tr>
			</c:forEach>
			
		</table>
	</body>
</html>