<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="f"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<f:form commandName="dataBinder">
<%-- 		<f:errors style="color:red"> --%>
			Date<f:input path="date"/>
			phoneNumber<f:input path="phoneNumber"/>
		<spring:bind path="phoneNumber">${phoneNumber.value}</spring:bind><br/>
		<spring:eval expression="dataBinder.date"></spring:eval>
<%-- 		</f:errors><br/> --%>
		<input type="submit" value="Submit">
	</f:form>
</body>
</html>