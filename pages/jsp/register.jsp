<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
<body>
	<form action="form.do" method="POST">
		Username:<input type="text" name="username" value="${command.username}"/>
		Password:<input type="password" name="password"/></br>
		Address:<select name="address">
					<c:forEach items="${address}" var="item">
						<option value="${item}">${item}</option>
					</c:forEach>
				</select>
		<br/>
		<input type="submit" value="Submit"/>
		<input type="submit" name="_cancel" value="Cancel"/>
	</form>
</body>
</html>