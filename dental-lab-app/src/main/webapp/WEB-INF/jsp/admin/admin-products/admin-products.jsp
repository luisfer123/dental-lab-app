<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>

<html>
<head>

<%@ include file="../../layer/scripts.jsp" %>

<meta charset="ISO-8859-1">
<title>Administrar Productos</title>
</head>
<body>

	<%@ include file="../../layer/navbar.jsp" %>
	
	<div class="container">
	
	<div class="row mt-2">
		<div class="col-md-3 mt-5 text-center">
			<%@ include file="../admin-layer/admin-sidebar.jsp" %>
		</div>
		<div class="col-md-9">
			<h3 class="h3 mt-5 text-primary text-center">Administrar products</h3>
			
			<%@ include file="./admin-products-layer/admin-products-navbar.jsp" %>
		</div>
	</div>
	
</div>

</body>
</html>