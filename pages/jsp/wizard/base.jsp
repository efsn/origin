<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<body>
	<p>${address.toString()}</p>
	<form action="wizard.do" method="POST">
		Username:<input type="text" name="username" value="${user.username}"/>
		Password:<input type="password" name="password" value="${user.password}"/>
		<br/>
		<input type="submit" name="_cancel" value="Cancel"/>
		<input type="submit" name="_target1" value="Next"/>
	</form>
</body>
</html>