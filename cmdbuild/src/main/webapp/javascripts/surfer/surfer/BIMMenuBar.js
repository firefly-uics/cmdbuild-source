(function() {
	var	BIM_SERVER_IMPORT = "top-menu-import-bimserver",
		TEMPLATE =	"<div id='top-menu'>" +
						"<ul>" +
							"<li class='top-menu-left'>" +
								"<a>File</a>" +
								"<ul>" +
									"<li>" +
										"<a id='" + BIM_SERVER_IMPORT + "'>BIMserver Project</a>" +
									"</li>" +
								"</ul>" +
							"</li>" +
						"</ul>" +
					"</div>";

	BIMMenuBar = function(config) {
		config = config || {};
		render(config.containerElementId);
		bindEvents(this);
	};

	function render(containerElementId) {
		var containerElement = null;

		if (containerElementId) {
			containerElement = $("#" + containerElementId);
		} else {
			containerElement = $("body");
		}

		if (containerElement != null) {
			containerElement.prepend(TEMPLATE);
		}

		$(".top-menu-left").children("ul").toggle();
	}

	function bindEvents(me) {

		($('#' + BIM_SERVER_IMPORT)).click(function () {
			new BIMLoginAndImportWindow();
		});

		$(".top-menu-left").hover(function() {
			$(this).children("ul").toggle();
		});
	}

})();