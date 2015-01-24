<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
	<body>
		<form action="wizard.do" method="POST">
			School:<input type="text" name="email" value="${email}"/>
			<input type="submit" name="_target1" value="Above"/>
			<input type="submit" name="_finish" value="Finish"/>
			<input type="submit" name="_cancel" value="Cancel"/>
		</form>
	</body>
</html>