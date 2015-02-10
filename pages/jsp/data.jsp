<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Show binder data</title>
	</head>
	<body>
		${data.username}<br/>
		${data.bl}<br/>
		${data.sInfo}<br/>
		${data.list}<br/>
		${data.map}<br/>
		${data.phoneNumber}<br/>
		${data.date}<br/>
		${data.state}<br/>
	</body>
</html>