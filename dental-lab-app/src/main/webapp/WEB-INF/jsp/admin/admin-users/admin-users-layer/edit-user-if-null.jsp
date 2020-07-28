<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<p class="mx-3 mt-5">
	Ningún usuario ha sido seleccionado. Puedes usar el formulario de abajo
	para buscar el usuario que desees editar o puedes ver una lista
	completa de todos los usuarios registrados <a
		href="<c:url value="/admin/users/list" />">aquí</a>
</p>
<div class="container">
	<br />
	<div class="row justify-content-center">
		<div class="col-12 col-md-10 col-lg-8">
			<form class="card card-sm" id="editUserIfNullForm">
				<h3 class="h3 ml-3 mt-3 text-secondary">Buscar usuario</h3>

				<div class="card-body row no-gutters align-items-center">
					<div class="input-group mb-3">
						<div class="input-group-prepend">
							<label class="input-group-text" for="searchSelect">Buscar por</label>
						</div>
						<select name="searchBy" class="custom-select" id="searchSelect">
							<option value="username" selected>Nombre de usuario</option>
							<option value="email">Correo electronico</option>
							<option value="name">Nombre</option>
						</select>
					</div>
					<div class="col-auto">
						<i class="fas fa-search h4 text-body"></i>
					</div>
					<!--end of col-->
					<div class="col">
						<input name="searchKeyword"
							id="inputSearchId"
							class="form-control form-control-lg form-control-borderless"
							type="search">
					</div>

					<sec:csrfInput />
					
					<!--end of col-->
					<div class="col-auto">
						<button class="btn btn-lg btn-secondary" type="button">Buscar</button>
					</div>
					<!--end of col-->
				</div>
			</form>
		</div>
		<!--end of col-->
	</div>
</div>
<div class="container" id="userListContainer">
	<table class="table mt-3 ml-3" id="searchResultTable">
		<thead>
			<tr>
				<th scope="col" class="text-center">Nombre de usuario</th>
				<th scope="col">Correo electronico</th>
				<th scope="col">Nombre</th>
				<th></th>
			</tr>
		</thead>
		<tbody>
		</tbody>
	</table>
</div>