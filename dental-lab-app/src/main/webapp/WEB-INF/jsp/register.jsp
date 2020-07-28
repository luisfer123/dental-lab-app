<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<!DOCTYPE html>

<html>
<head>

<%@ include file="./layer/scripts.jsp" %>

<meta charset="ISO-8859-1">
<title>Registro nuevo usuario</title>
</head>
<body>

<%@ include file="./layer/navbar.jsp" %>

<div class="container">

	<h3 class="cart-title my-3 text-center">Formulario de registro</h3>

	<div class="card mt-5 mx-auto" style="width: 50em;">
		<div class="card-body p-5">
							
			<form method="post" action="<c:url value='/users/register' />">
				<div class="form-group">
					<label for="username">Nombre de usuario:</label> 
					<input type="text" name="username" class="form-control" id="username" placeholder="Elige un nombre de usuario">
				</div>
				
				<div class="form-group">
					<label for="password">Contraseña</label> 
					<input type="password" name="password" class="form-control" id="password" placeholder="Elige una contraseña">
				</div>
				
				<div class="form-group">
					<label for="confirmPassword">Confirmar contraseña</label> 
					<input type="password" name="confirmPassword" class="form-control" id="confirmPassword" placeholder="Vuelve a introducir la contraseña">
				</div>
				
				<div class="form-group">
					<label for="email">Email</label> 
					<input type="email" name="email" class="form-control" id="email" placeholder="Introduce una dirección de correo">
				</div>
				
				<div class="form-group">
					<label for="firstName">Nombre:</label> 
					<input type="text" name="firstName" class="form-control" id="firstName">
				</div>
				
				<div class="form-group">
					<label for="firstLastName">Apellido paterno:</label> 
					<input type="text" name="firstLastName" class="form-control" id="firstLastName">
				</div>
				
				<div class="form-group">
					<label for="secondLastName">Apellido materno:</label> 
					<input type="text" name="secondLastName" class="form-control" id="secondLastName">
				</div>
				
				<sec:csrfInput/>
				
				<button type="submit" class="btn btn-primary">Enviar formulario</button>
			</form>
		</div>
	</div>

</div>

</body>
</html>