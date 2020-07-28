<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>

<%@ include file="../../layer/scripts.jsp" %>

<meta charset="ISO-8859-1">
<title>Editar producto</title>
</head>
<body>


<%@ include file="../../layer/navbar.jsp" %>
	
	<div class="container">
	
		<%@ include file="admin-products-layer/admin-products-navbar.jsp" %>
	
		<div class="row mt-2">
			<div class="col-md-3 text-center">
				<%@ include file="../admin-layer/admin-sidebar.jsp" %>
			</div>
			<div class="col-md-9">
				
				<h3 class="h3 text-center mt-3">Editar Producto</h3>
				
				<div class="row mt-5">
					<div class="col-md-4">
						<img class="img-thumbnail" src="data:image/jpeg;base64,${productImage }" alt="Image was not found" />
					</div>
					<div class="col-md-8 pl-0">
						<div class="card">
							<div class="card-body">
								<h5 class="card-title">${product.name }</h5>
								
								<div class="row my-2">
									<div class="col-8">
										Precio: ${currentPrice.price }
									</div>
									<div class="col-4">
										<a href="#" class="badge badge-primary">Cambiar</a>
									</div>
								</div>
								<p>Descripción</p>
								<p class="card-text">${product.description }</p>
								
								<a href="#" class="card-link">Card link</a>
								<a href="#" class="card-link">Another link</a>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

</body>
</html>