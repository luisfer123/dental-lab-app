<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<%@ include file="../../layer/scripts.jsp" %>

<meta charset="ISO-8859-1">
<title>Lista de productos</title>
<style type="text/css">
	
</style>
</head>
<body>

	<%@ include file="../../layer/navbar.jsp" %>
	
	<div class="container" id="body-container">
	
	<%@ include file="admin-products-layer/admin-products-navbar.jsp" %>
	
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
								<th scope="col">Producto</th>
								<th></th>
							</tr>
						</thead>
						<tbody>
							<c:forEach items="${products }" var="product">
								<tr>
									<th scope="row">${product.name }</th>
									<td><a href="<c:url value="/admin/products/${product.id }/edit" />" class="btn btn-secondary">Editar</a></td>
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