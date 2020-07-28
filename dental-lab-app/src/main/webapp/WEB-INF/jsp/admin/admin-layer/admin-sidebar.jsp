<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<ul class="nav flex-column mt-3 py-5 border round custom-background">
	<li class="nav-item">
		<h3 class="h3 text-primary">Opciones</h3>
	</li>
	<li class="nav-item">
		<a class="nav-link active" href="<c:url value="/admin" />">General</a>
	</li>
	<li class="nav-item">
		<a class="nav-link" href="<c:url value="/admin/users/panel" />">Usuarios</a>
	</li>
	<li class="nav-item">
		<a class="nav-link" href="<c:url value="/admin/products/panel" />">Productos</a>
	</li>
	<li class="nav-item">
		<a class="nav-link disabled" href="#">Disabled</a>
	</li>
</ul>

