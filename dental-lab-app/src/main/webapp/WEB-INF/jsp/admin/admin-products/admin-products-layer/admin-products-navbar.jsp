<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<ul class="nav border round justify-content-center mt-3 custom-background">
	<li class="nav-item">
		<a class="nav-link active" href="<c:url value="/admin/products/products-list" />">Lista de productos</a>
	</li>
	<li class="nav-item">
		<a class="nav-link" href="#">Agregar producto</a>
	</li>
	
</ul>