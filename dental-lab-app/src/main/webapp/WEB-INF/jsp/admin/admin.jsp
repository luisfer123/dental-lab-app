<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>

<style type="text/css">
	#list-tab {
		text-align: center;
	}
</style>

<%@ include file="../layer/scripts.jsp" %>

<meta charset="ISO-8859-1">
<title>Panel de administración</title>
</head>
<body>

<%@ include file="../layer/navbar.jsp" %>

<div class="container">
	
	<div class="row mt-2">
		<div class="col-md-3 mt-5 text-center">
			<%@ include file="admin-layer/admin-sidebar.jsp" %>
		</div>
		<div class="col-md-9">
			<h3 class="h3 mt-5 text-primary text-center">Panel de administración</h3>
		</div>
	</div>
	
</div>

</body>
</html>