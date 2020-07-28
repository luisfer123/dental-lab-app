<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<ul class="nav border round justify-content-center mt-3 custom-background">
	<li class="nav-item">
		<a class="nav-link active" href="<c:url value="/admin/users/list" />">Lista de usuarios</a>
	</li>
	<li class="nav-item">
		<a class="nav-link" href="<c:url value="/admin/users/edit" />">Editar usuario</a>
	</li>
	<li class="nav-item">
		<a class="nav-link" href="#">Agregar usuario</a>
	</li>
	<li class="nav-item"><a class="nav-link disabled" href="#">Disabled</a>
	</li>
	
</ul>