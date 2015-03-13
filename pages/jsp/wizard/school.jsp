<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<html>
	<body>
		<form action="wizard.do" method="POST">
			School:<input type="text" name="mobilePhone" value="${mobilePhone}"/>
			<input type="hidden" name="_target" value="2"/>
			<input type="submit" name="above" value="Above"/>
			<input type="submit" name="next" value="Next"/>
		</form>
	</body>
</html>