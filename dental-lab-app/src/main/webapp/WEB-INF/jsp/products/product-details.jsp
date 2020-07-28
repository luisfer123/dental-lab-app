<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>

<%@ include file="../layer/scripts.jsp" %>

<meta charset="ISO-8859-1">
<title>Detalles de Producto</title>
</head>
<body>

<%@ include file="../layer/navbar.jsp" %>


<div class="container my-3 ">

	
	<nav aria-label="breadcrumb">
		<ol class="breadcrumb">
			<c:forEach items="${productCategoriesPath }" var="category">
				<li class="breadcrumb-item active" aria-current="page">
					${category.name }
				</li>
			</c:forEach>
		</ol>
	</nav>
	

</div>

</body>
</html>