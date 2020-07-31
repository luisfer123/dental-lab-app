<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>

<%@ include file="../../layer/scripts.jsp" %>

<meta charset="ISO-8859-1">
<title>Editar producto</title>
</head>
<body>


<%@ include file="../../layer/navbar.jsp" %>
	
	<div class="container">
	
		<%@ include file="admin-products-layer/admin-products-navbar.jsp" %>
	
		<div class="row mt-2">
			<div class="col-md-3 text-center">
				<%@ include file="../admin-layer/admin-sidebar.jsp" %>
			</div>
			<div class="col-md-9">
				
				<h3 class="h3 text-center mt-3">Editar Producto</h3>
				
				<div class="row mt-5">
					<div class="col-md-4">
						<img 
							class="img-thumbnail text-md-center" 
							src="data:image/jpeg;base64,${productImage }" 
							alt="Image was not found" />
						
						<form 
							id="updateProductPictureForm" 
							enctype="multipart/form-data" 
							method="post" 
							action="<c:url value="/admin/products/${product.id }/updateImage" />">
							<div class="custom-file mt-3">
								<input 
									type="file" 
									class="custom-file-input" 
									name="imageToUpdate"
									id="newProductPicture">
								<label id="newProductPictureLabel" class="custom-file-label" for="newProductPicture">Elige una foto</label>
							</div>
							<script type="application/javascript">
							    $('#newProductPicture').change(function(e){
							        var fileName = e.target.files[0].name;
							        $('#newProductPictureLabel').html(fileName);
							    });
							</script>
							<sec:csrfInput/>
							<div class="flex-box mt-3">
								<button type="submit" class="btn btn-primary">Actualizar</button>
							</div>
						</form>
					</div>
					<div class="col-md-8 pl-0">
						<form
							class="form mb-5 px-2" 
							method="post"
							action="<c:url value="/admin/products/${product.id }/updateProduct" />">
							<div class="row">
								<div class="col">
									<div class="row">
										<div class="col">
											<div class="form-group">
												<label for="name">Nombre:</label> 
												<input 
													class="form-control"
													type="text" 
													name="name" 
													id="name" 
													value="${product.name }">
											</div>
										</div>
									</div>
									
									<div class="row mt-3">
										<div class="col">
											<label for="price">Precio</label>
										</div>
										<div class="col">
											<div class="form-group">
												<input 
													class="form-control" 
													type="number"
													name="newPrice"
													id="price"
													value="${currentPrice.price }">
											</div>
										</div>
									</div>
									<div class="row mt-3">
										<div class="col">
											<div class="form-group">
												<label for="description">Descripción</label>
												<textarea 
													name="description" 
													class="form-control" 
													id="description" 
													rows="3">${product.description }</textarea>
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
							</div> <!-- End of row -->
						</form>
					</div> <!-- End of col -->
				</div> <!-- End of row -->
			</div>
		</div>
	</div>

</body>
</html>