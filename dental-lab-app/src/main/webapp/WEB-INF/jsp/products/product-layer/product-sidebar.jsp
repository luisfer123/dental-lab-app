<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

	<nav aria-label="breadcrumb">
		<ol class="breadcrumb">
			<c:forEach items="${categoryPath }" var="category">
				<li class="breadcrumb-item active" aria-current="page">
					<a href="<c:url value="/products/${category.id }/list" />">${category.name }</a>				
				</li>
			</c:forEach>
		</ol>
	</nav>

<c:if test="${!empty category.children }">
	<ul class="nav flex-column mt-3 py-5 border round custom-background">
	
		<li class="nav-item">
			<h3 class="h3 text-primary">Categorias</h3>
		</li>
	
		<c:forEach items="${category.children }" var="ct">
			<li class="nav-item">
				<a href="<c:url value="/products/${ct.id }/list" />" class="nav-link">${ct.name }</a>
			</li>
		</c:forEach>
	</ul>
</c:if>