<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>


<div class="container mt-5">

	<div class="e-profile">
		<div class="row">
			<div class="col-12 col-sm-auto mb-3">
				<div class="mx-auto" style="width: 140px;">
					<div
						class="d-flex justify-content-center align-items-center rounded"
						style="height: 140px; max-height: 140px; background-color: rgb(233, 236, 239);">
						<img class="img-thumbnail" src="data:image/jpeg;base64,${profilePicture }" alt="Image was not found" />
					</div>
				</div>
			</div>
			<div class="col d-flex flex-column flex-sm-row justify-content-between mb-3">
				<div class="text-center text-sm-left mb-2 mb-sm-0">
					<h4 class="pt-sm-2 pb-1 mb-0 text-nowrap">${user.firstName } ${user.firstLastName }</h4>
					<p class="mb-0">${user.username }</p>
					
					<div class="mt-2">
						<button id="updateProfilePictureButton" class="btn btn-primary" type="button">
							<i class="fa fa-fw fa-camera"></i> <span>Actualizar foto</span>
						</button>
					</div>
				</div>
				
			</div>
		</div>
		<form id="updateProfilePictureForm" enctype="multipart/form-data" method="post" action="<c:url value="/admin/users/update/profilePicture?user_id=${user.id }" />">
			<div class="row">
				<div class="col-sm-auto mb-4">
					<div class="custom-file">
						<input 
							type="file" 
							class="custom-file-input" 
							name="newProfilePicture"
							id="newProfilePicture">
						<label id="newProfilePictureLabel" class="custom-file-label" for="newProfilePicture">Elige una foto</label>
					</div>
					<script type="application/javascript">
					    $('#newProfilePicture').change(function(e){
					        var fileName = e.target.files[0].name;
					        $('#newProfilePictureLabel').html(fileName);
					    });
					</script>
					<sec:csrfInput/>
				</div>
			</div>
		</form>
		
		<ul class="nav nav-tabs" id="myTab" role="tablist">
			<li class="nav-item">
				<a class="nav-link active" id="general-tab" data-toggle="tab" href="#general" role="tab" aria-controls="general" aria-selected="true">General</a>
			</li>
			<li class="nav-item">
				<a class="nav-link" id="password-tab" data-toggle="tab" href="#passwordTab" role="tab" aria-controls="passwordTab" aria-selected="false">Contraseña</a>
			</li>
			<li class="nav-item">
				<a class="nav-link" id="roles-tab" data-toggle="tab" href="#rolesTab" role="tab" aria-controls="rolesTab" aria-selected="false">Roles</a>
			</li>
		</ul>
		<div class="tab-content" id="myTabContent">
			
			<div class="tab-pane fade show active" id="general" role="tabpanel" aria-labelledby="general-tab">
				<h4 class="h4 my-5 text-center">Editar información general</h4>
				<form class="form mb-5" method="post"
					action="<c:url value="/admin/users/${user.id }/update_general_info" />">
					<div class="row">
						<div class="col">
							<div class="row">
								<div class="col">
									<div class="form-group">
										<label for="firstName">Nombre</label> 
										<input 
											class="form-control"
											type="text" 
											name="firstName" 
											placeholder="John Smith"
											id="firstName" 
											value="${user.firstName }">
									</div>
								</div>
								<div class="col">
									<div class="form-group">
										<label for="firstLastName">Apellido paterno</label> 
										<input 
											class="form-control" 
											id="firstLastName"
											type="text" 
											name="firstLastName" 
											value="${user.firstLastName }">
									</div>
								</div>
								<div class="col">
									<div class="form-group">
										<label for="secondLastName">Apellido materno</label> 
										<input 
											class="form-control" 
											id="secondLastName"
											type="text" 
											name="secondLastName" 
											value="${user.secondLastName }">
									</div>
								</div>
							</div>
							
							<div class="row">
								<div class="col">
									<div class="form-group">
										<label for="username">Nombre de usuario</label> 
										<input 
											class="form-control" 
											type="text"
											name="username"
											id="username"
											value="${user.username }">
									</div>
								</div>
								<div class="col">
									<div class="form-group">
										<label for="email">Correo electronico</label> 
										<input 
											class="form-control" 
											type="email"
											name="email"
											id="email"
											value="${user.email }">
									</div>
								</div>
								
							</div>
							<sec:csrfInput/>
							<div class="row">
								<div class="col d-flex justify-content-end">
									<button class="btn btn-primary mr-5 mt-5" type="submit">Guardar cambios</button>
								</div>
							</div>
						</div>
					</div>
				</form>
			</div>
			<div class="tab-pane fade" id="passwordTab" role="tabpanel" aria-labelledby="password-tab">
			
				<h4 class="h4 mt-5 text-center">Cambiar contraseña</h4>
				<form class="form my-5"
					action="<c:url value="/admin/users/${user.id }/change_password" />" method="post">
					
					<div class="row d-flex justify-content-center">
						<div class="col-12 col-sm-6 mb-3">
							<div class="row">
								<div class="col">
									<div class="form-group">
										<label for="newPassword">Nueva contraseña</label> 
										<input 
											class="form-control"
											type="password" 
											name="newPassword"
											id="newPassword"
											placeholder=".......">
									</div>
								</div>
							</div>
							<div class="row">
								<div class="col">
									<div class="form-group">
										<label for="confirmNewPassword">Confirmar nueva contraseña</label>
										<input 
											class="form-control" 
											type="password" 
											name="confirmNewPassword"
											id="confirmNewPassword"
											placeholder=".......">
									</div>
								</div>
							</div>
						</div>
					</div>
					<sec:csrfInput/>
					<div class="row">
						<div class="col d-flex justify-content-end">
							<button class="btn btn-primary mr-5 mt-5" type="submit">Guardar</button>
						</div>
					</div>
				</form>
			</div>
			<div class="tab-pane fade" id="rolesTab" role="tabpanel" aria-labelledby="roles-tab">
				<h4 class="h4 my-5 text-center">Editar roles de usuario</h4>
				<p>
					Los roles de usuario determinan a que páginas e información cada usuario
					tiene acceso. Dependiendo de los roles de usuario otorgados cada usuario
					podra ver, administrar y modificar diferente información en la página.
				</p>
				
				<div class="container mt-5 mb-3">
					<div class="row">
						<div class="col-md-6">
							<h5 class="h5 mb-3">Roles de este usuario:</h5>
							<c:forEach items="${userAuthorities }" var="authority">
								<form class="form-inline" method="post"
									action="<c:url value="/admin/users/${user.id }/delete_role" />">
									<label class="sr-only" for="inlineFormInputName2">Name</label>
									<input 
										type="text" 
										class="form-control mb-2 mr-sm-2" 
										id="inlineFormInputName2"
										value="${authority == 'ROLE_USER' ? 'Rol Usuario': '' or 
												authority == 'ROLE_CLIENT' ? 'Rol cliente': '' or
												authority == 'ROLE_TECHNICIAN' ? 'Rol tecnico': '' or
												authority == 'ROLE_ADMIN' ? 'Rol administrador': '' }" 
										readonly >
									<input type="hidden" value="${authority }" name="authorityToDelete" />
									<sec:csrfInput/>
									<c:if test="${!(authority eq 'ROLE_USER') }">
										<button type="submit" class="btn btn-primary mb-2">Eliminar</button>
									</c:if>
									<c:if test="${authority eq 'ROLE_USER' }">
										<button type="button" class="btn btn-secondary mb-2">Eliminar</button>
									</c:if>
								</form>
							</c:forEach>
						</div>
						<div class="col-md-6">
							<h5 class="h5 mb-3">Agregar rol</h5>
							<c:if test="${!(empty userAuthoritiesComplement) }">
								<form class="form-inline" method="post" action="<c:url value="/admin/users/${user.id }/add_role" />">
									<select class="custom-select"  name="authorityToAdd">
										<option selected>Selecciona un rol</option>
										<c:forEach items="${userAuthoritiesComplement }" var="authority">
											<option value="${authority }">
												${authority == 'ROLE_USER' ? 'Rol Usuario': '' or 
												authority == 'ROLE_CLIENT' ? 'Rol cliente': '' or
												authority == 'ROLE_TECHNICIAN' ? 'Rol tecnico': '' or
												authority == 'ROLE_ADMIN' ? 'Rol administrador': '' }</option>
										</c:forEach>
									</select>
									<sec:csrfInput/>
									<button type="submit" class="btn btn-primary ml-2">Agregar</button>
								</form>
							</c:if>
							<c:if test="${empty userAuthoritiesComplement }">
								<p>Este usuarion ya tiene todos los roles definidos en la aplicación</p>
							</c:if>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>