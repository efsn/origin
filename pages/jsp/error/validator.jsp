<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="f"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Data Binder Model Validate</title>
	</head>
	<body>
		<f:form commandName="command">
			<f:errors path="*" cssStyle="color:red"/><br/>
			Username:<f:input path="username"/>
			<f:errors path="username" cssStyle="color:red"/><br/>
			Password:<f:input path="password"/>
			<f:errors path="password" cssStyle="color:red"/><br/>
			<input type="submit" value="Validate"/>
		</f:form>
	</body>
</html>