<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<body>
	<p>${address.toString()}</p>
	<form action="form.do" method="POST">
		Username:<input type="text" name="username" value="${command.username}"/>
		Password:<input type="password" name="password"/>
		<br/>
		<input type="submit" value="Submit"/>
		<input type="submit" name="_cancel" value="Cancel"/>
	</form>
</body>
</html>