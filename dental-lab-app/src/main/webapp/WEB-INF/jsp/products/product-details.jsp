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
	
	<div class="row">
		<div class="col-md-3 text-center">
			<nav aria-label="breadcrumb">
				<ol class="breadcrumb">
					<c:forEach items="${productCategoriesPath }" var="category">
						<li class="breadcrumb-item active" aria-current="page">
							<a href="<c:url value="/products/${category.id }/list" />">${category.name }</a>
						</li>
					</c:forEach>
				</ol>
			</nav>
			<ul class="nav flex-column mt-3 py-5 border round custom-background">
				<li class="nav-item">
					<h3 class="h3 text-primary">Menú</h3>
				</li>
				<li class="nav-item">
					<a href="<c:url value="#" />" class="nav-link">Some link</a>
				</li>
				<li class="nav-item">
					<a href="<c:url value="#" />" class="nav-link">Some link</a>
				</li>
				<li class="nav-item">
					<a href="<c:url value="#" />" class="nav-link">Some link</a>
				</li>
			</ul>
		</div>
		<div class="col-md-9">
			
			<div class="card">
				<div class="card-header text-center">
					<h3 class="h3">${product.name }</h3>
				</div>
				<div class="row">
					<div class="col-md-4">
						<img 
							class="img-thumbnail text-md-center m-3" 
							src="data:image/jpeg;base64,${product.productImage }" 
							alt="Image was not found" />
					</div>
					<div class="col-md mr-0 pr-0">
						<div class="card-body">
						<h5 class="card-title">Precio ${product.currentPrice }$</h5>
						<p class="card-text">${product.description }</p>
						<a href="#" class="btn btn-primary">Go somewhere</a>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>

</body>
</html>