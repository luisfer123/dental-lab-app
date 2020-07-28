<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>

<%@ include file="../layer/scripts.jsp" %>

<meta charset="ISO-8859-1">
<title>Productos</title>

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