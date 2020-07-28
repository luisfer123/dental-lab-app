$(document).ready(function() {
	submitLogoutForm();
	activeNavbarTab();
});
	
function submitLogoutForm() {
	$("#logout-link").click(function(e) {
		e.preventDefault();
		$("#logout-form").submit();
	});
}
	
function activeNavbarTab() {
	$('.navbar-nav .nav-item').click(function() {
	    $('.navbar-nav .nav-item').removeClass('active');
	    $(this).addClass('active');
	})
}

