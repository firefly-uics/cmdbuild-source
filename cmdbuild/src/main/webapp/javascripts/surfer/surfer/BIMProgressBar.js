BIMProgressBar = function() {
	$("body").append("<div class='loadingdiv initialhide'><div class='text'>Loading</div></div>");
};

BIMProgressBar.prototype = {
	setProgress: function(progress) {
		$(".loadingdiv .progress .bar").css("width", progress + "%");
	},

	beginProcessing: function() {
		$(".loadingdiv .progress").addClass("progress-striped").addClass("active");
	},

	show: function() {
		$(".loadingdiv").fadeIn(500);
	},

	hide: function() {
		$(".loadingdiv").fadeOut(800);
	},

	reset: function() {
		$(".loadingdiv .progress").remove();
		$(".loadingdiv").append("<div class=\"progress\"><div class=\"bar\" style=\"width: 0%\"></div></div>");
	},

	setText: function(text) {
		$(".loadingdiv .text").html(text);
	}
};