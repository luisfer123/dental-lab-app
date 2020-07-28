<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>


<nav class="navbar navbar-expand-lg navbar-light bg-light">
	<a class="navbar-brand" href="<c:url value='/home' />">Navbar</a>
	<button class="navbar-toggler" type="button" data-toggle="collapse"
		data-target="#navbarNav" aria-controls="navbarNav"
		aria-expanded="false" aria-label="Toggle navigation">
		<span class="navbar-toggler-icon"></span>
	</button>
	
	<div class="collapse navbar-collapse" id="navbarNav">
		<ul class="navbar-nav">
			<sec:authorize access="hasRole('ROLE_ADMIN')" >
				<li class="nav-item active">
					<a class="nav-link" href="<c:url value="/admin" />">Administrar<span class="sr-only">(current)</span></a>
				</li>
			</sec:authorize>
			<sec:authorize access="hasRole('USER')">
				<li class="nav-item">
					<a class="nav-link" href="<c:url value="/products/category-list" />">Productos</a>
				</li>
			</sec:authorize>
			<li class="nav-item">
				<a class="nav-link" href="#">Contacto</a>
			</li>
			<li class="nav-item">
				<a class="nav-link" href="#">Conócenos</a>
			</li>
		</ul>
		<ul class="navbar-nav ml-auto">
			<sec:authorize access="isAuthenticated()">
				<li class="nav-item">
					<a class="nav-link" id="logout-link" href="#">Logout</a>
					<form method="post" id="logout-form" action="<c:url value='/logout' />">
						<sec:csrfInput/>
					</form>
				</li>
			</sec:authorize>
			<sec:authorize access="!isAuthenticated()">
				<li class="nav-item">
					<a href="<c:url value="/users/register" />" class="nav-link">Registrarse</a>
				</li>
				<li class="nav-item">
					<a class="nav-link" href="<c:url value='/login' />">Identificarse</a>
				</li>
			</sec:authorize>
		</ul>
	</div>
</nav>