<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<%@ include file="../../layer/scripts.jsp" %>

<meta charset="ISO-8859-1">
<title>Editar usuario</title>

<script type="text/javascript">
// Used in edit-user-if-null.jsp page to make an ajax request,
// get the searched user from the database and show it in a 
// dynamic table.
$(document).ready(function() {
	$('#userListContainer').hide();
	var data = [];

	$("#updateProfilePictureButton").click(function() {
		$("#updateProfilePictureForm").submit();
	});
	
	$('#editUserIfNullForm input').bind('keyup search', function() {
		data = $('#editUserIfNullForm').serializeArray();
		sendSearchRequest(data);
		$('.searchResultTableRow').remove();
	});
	$('#editUserIfNullForm #searchSelect').on('change', function() {
		data = $('#editUserIfNullForm').serializeArray();
		sendSearchRequest(data);
		$('.searchResultTableRow').remove();
	});
	
});

function sendSearchRequest(data) {
	$.ajax({
		url: '/users/search/ajax',
		type: 'get',
		dataType: 'json',
		data: data,
		
		success: function(response) {
			showTableResult(response);
		}
	});
}

function showTableResult(response) {
	if($("#inputSearchId").val().length === 0) {
		$('#userListContainer').hide();
	}
	if(!response || response.length === 0) {
		$('#userListContainer').hide();
	} else {
		$('#userListContainer').show();
	
		$.each(response, function(index, user) {
			$('#searchResultTable > tbody').append(
				'<tr class="searchResultTableRow">' +
					'<th scope="row">' + user.username + '</th>' +
					'<td>' + user.email + '</td>' +
					'<td>' + user.firstName + ' ' +  user.firstLastName + ' ' + user.secondLastName + '</td>' +
					'<td><a href="/admin/users/edit?user_id=' + user.id + '" class="btn btn-secondary">Editar</a></td>' +
				'</tr>'
			);
		});
	}
}
</script>
</head>
<body>

<%@ include file="../../layer/navbar.jsp" %>
	
	<div class="container">
	
	<%@ include file="admin-users-layer/admin-users-navbar.jsp" %>
	
		<div class="row mt-2">
			<div class="col-md-3 text-center">
				<%@ include file="../admin-layer/admin-sidebar.jsp" %>
			</div>
			<div class="col-md-9">
				
				<h3 class="h3 text-center mt-3">Editar Usuario</h3>
				
				<c:if test="${user eq null }">
					<%@ include file="./admin-users-layer/edit-user-if-null.jsp" %>
				</c:if>
				<c:if test="${!(user eq null) }">
					<%@ include file="./admin-users-layer/edit-user-form.jsp" %>
				</c:if>
			</div>
		</div>
	</div>

</body>
</html>