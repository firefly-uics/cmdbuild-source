LoadingMask = function() {
	var active = true;

	$("body").ajaxStart(function() {
		if (active) {
			$("body").append('<div id="loading-mask"> <p id="loading-mask-message">Loading...</p> </div>');
		}
	}).ajaxStop(function() {
		$("#loading-mask").fadeOut(700).remove();
	});

	this.activate = function() {
		active = true;
	};

	this.deactivate = function() {
		active = false;
	};
};
