<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<!DOCTYPE html>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<html>
<head>

<%@ include file="./layer/scripts.jsp" %>

<meta charset="ISO-8859-1">
<title>Login</title>
</head>
<body>

<%@ include file="./layer/navbar.jsp" %>

<div class="container mt-3 login">

	<div class="card mt-5 mx-auto" style="width: 18rem;">
		<div class="card-body">
			<h5 class="cart-title mb-5 text-center">Identificarse</h5>
							
			<form method="post" action="<c:url value='/login' />">
				<c:if test="${param.error eq true }">
					<div class="alert alert-danger" role="alert">
		  				Username or Password are incorrect! Please try again.
					</div>
				</c:if>
				<div class="form-group">
					<label for="username">Nombre de usuario:</label> 
					<input type="text" name="username" class="form-control" id="username" placeholder="Enter your username">
				</div>
				
				<div class="form-group">
					<label for="password">Contraceña:</label> 
					<input type="password" name="password" class="form-control" id="password" placeholder="Enter your password">
				</div>
				
				<sec:csrfInput/>
				<div class="container text-center mt-5">
					<button type="submit" class="btn btn-primary">Enviar</button>
				</div>
			</form>
		</div>
	</div>
</div>

</body>
</html>