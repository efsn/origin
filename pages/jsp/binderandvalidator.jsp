<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="f"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	phoneNumber<spring:bind path="dataBinder.phoneNumber">${status.value}</spring:bind><br/>
	<!-- 
		if not configurate ConversionServiceExposingInterceptor will become exception
	 -->
	phoneNumber<spring:eval expression="dataBinder.phoneNumber"></spring:eval><br/>
	<f:form commandName="dataBinder">
		Date<f:input path="date"/>
		phoneNumber<f:input path="phoneNumber"/>
	</f:form>
</body>
</html>