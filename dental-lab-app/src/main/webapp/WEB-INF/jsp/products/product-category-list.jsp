<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>

<%@ include file="../layer/scripts.jsp" %>

<meta charset="ISO-8859-1">
<title>Lista de productos</title>

<style type="text/css">

	.text-gray {
	    color: #aaa
	}
	
	img {
	    height: 170px;
	    width: 140px
	}
</style>
</head>
<body>

<%@ include file="../layer/navbar.jsp" %>

<div class="container my-5">

	<h1 class="h1 my-5 p-3 border round text-center custom-background">
		<a href="<c:url value="/products/category-list" />">Lista de Productos</a>
	</h1>

	<div class="row mx-auto">
		<div class="col-md-4 text-center">
			<%@ include file="./product-layer/product-sidebar.jsp" %>
		</div>
		<div class="col-md-8">
			<%@ include file="./product-layer/product-list-fragment.jsp" %>
		</div>
	</div>

	
</div>

</body>
</html>