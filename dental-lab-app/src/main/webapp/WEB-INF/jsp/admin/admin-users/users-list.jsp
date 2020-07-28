<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<%@ include file="../../layer/scripts.jsp" %>

<meta charset="ISO-8859-1">
<title>Lista de usuarios</title>
<style type="text/css">
	
</style>
</head>
<body>

	<%@ include file="../../layer/navbar.jsp" %>
	
	<div class="container" id="body-container">
	
	<%@ include file="admin-users-layer/admin-users-navbar.jsp" %>
	
		<div class="row mt-2">
			<div class="col-md-3 text-center">
				<%@ include file="../admin-layer/admin-sidebar.jsp" %>
			</div>
			<div class="col-md-9">

				<div class="container pt-3" id="user-list-container">
				
					<h3 class="h3 mt-3 text-primary text-center pb-3">Lista de usuarios</h3>
					
					<table class="table mt-3">
						<thead>
							<tr>
								<th scope="col">Nombre de usuario</th>
								<th scope="col">Correo electronico</th>
								<th scope="col">Nombre</th>
								<th></th>
							</tr>
						</thead>
						<tbody>
							<c:forEach items="${users }" var="user">
								<tr>
									<th scope="row">${user.username }</th>
									<td>${user.email }</td>
									<td>${user.firstLastName } ${user.secondLastName } ${user.firstName }</td>
									<td><a href="<c:url value="/admin/users/edit?user_id=${user.id }" />" class="btn btn-secondary">Editar</a></td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</div>
			</div>
		</div>
	</div>

</body>
</html>