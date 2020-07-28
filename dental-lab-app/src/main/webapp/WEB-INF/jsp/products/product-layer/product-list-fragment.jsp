<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<!-- List group-->
<ul class="list-group shadow">
	<c:forEach items="${products }" var="product">
		<!-- list group item-->
		<li class="list-group-item">
			<!-- Custom content-->
			<div class="media align-items-lg-center flex-column flex-lg-row p-3">
				<div class="media-body order-2 order-lg-1">
					<h5 class="mt-0 font-weight-bold mb-2">
						<a href="<c:url value="/products/${product.id }/details" />">
							${product.name }
						</a>
					</h5>
					<p class="font-italic text-muted mb-0 small">${product.description }</p>
					<div class="d-flex align-items-center justify-content-between mt-1">
						<h6 class="font-weight-bold my-2">$ ${product.currentPrice }</h6>
						<ul class="list-inline small">
							<li class="list-inline-item m-0"><i
								class="fa fa-star text-success"></i></li>
							<li class="list-inline-item m-0"><i
								class="fa fa-star text-success"></i></li>
							<li class="list-inline-item m-0"><i
								class="fa fa-star text-success"></i></li>
							<li class="list-inline-item m-0"><i
								class="fa fa-star text-success"></i></li>
							<li class="list-inline-item m-0"><i
								class="fa fa-star-o text-gray"></i></li>
						</ul>
					</div>
				</div>
				<img src="data:image/jpeg;base64,${product.productImage }"
					alt="Generic placeholder image" width="200"
					class="ml-lg-5 order-1 order-lg-2">
			</div> <!-- End medias -->
		</li> <!-- End list-group-item -->
	</c:forEach>
</ul>