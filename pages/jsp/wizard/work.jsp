<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
	<body>
		<form action="wizard.do" method="POST">
			Work:<input type="text" name="email" value="${email}"/>
			<input type="hidden" name="_target" value="3"/>
			<input type="submit" name="above" value="Above"/>
			<input type="submit" name="_finish" value="Finish"/>
			<input type="submit" name="_cancel" value="Cancel"/>
		</form>
	</body>
</html>