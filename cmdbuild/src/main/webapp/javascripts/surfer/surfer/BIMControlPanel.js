(function() {
	var BASE_ID = "bim-control-panel",
		RESET_ID = BASE_ID + "-reset",
		FRONT_ID = BASE_ID + "-front",
		SIDE_ID = BASE_ID + "-side",
		TOP_ID = BASE_ID + "-top",
		PAN_ROTATE_ID = BASE_ID + "-panrotate",
		ZOOM_ID = BASE_ID + "-zoom",
		EXPOSE_ID = BASE_ID + "-expose",
		TRANSPARENT_ID = BASE_ID + "-transparent",
		SLIDER_CLASS = BASE_ID + "-slider",
		WRAPPER_ID = BASE_ID + "-wrapper",
		MIN_ZOOM = 0,
		MAX_ZOOM = 15,
		MIN_EXPOSE = 0,
		MAX_EXPOSE = 10000,
		TEMPLATE =	"<div id='" + BASE_ID + "'>" +
						"<h2 id='" + BASE_ID + "-header' class='bim-accordion-header bim-plus'>Controlli</h2>" +
						"<div id='" + WRAPPER_ID + "' class=''>" +
							"<div class='btn-group'>" +
								"<button class='btn' id='" + RESET_ID + "'>Reset</button>" +
								"<button class='btn' id='" + FRONT_ID + "'>Front</button>" +
								"<button class='btn' id='" + SIDE_ID + "'>Side</button>" +
								"<button class='btn' id='" + TOP_ID + "'>Top</button>" +
								"<button class='btn' id='" + PAN_ROTATE_ID + "'>Pan/Rotate</button>" +
							"</div>" +
							"<div id='" + BASE_ID + "-sliders'>" +
								"<div><p>Zoom:</p> <p id='" + ZOOM_ID + "' class='" + SLIDER_CLASS + "'></p> </div>" +
								"<div><h3> Current element </h3></div>" +
								"<div><p>Expose:</p> <p id='" + EXPOSE_ID + "' class='" + SLIDER_CLASS + "'></p> </div>" +
								"<div><p>Transparent:</p> <p id='" + TRANSPARENT_ID + "' class='" + SLIDER_CLASS + "'></p> </div>" +
							"</div>" +
						"</div>" +
					"</div>";

	BIMControlPanel = function(bimSceneManager, containerId) {
		render(containerId);
		bindEvents(this, bimSceneManager);
		bimSceneManager.addDelegate(this);

		this.currentObjectId = null;
	};

	BIMControlPanel.prototype.objectSelected = function(sceneManager, objectId) {
		this.currentObjectId = objectId;
	};

	BIMControlPanel.prototype.selectionCleaned = function(sceneManager) {
		this.currentObjectId = null;
	};
 
	function render(containerId) {

		$("#" + containerId).append(TEMPLATE);

		$("#" + WRAPPER_ID).toggle();

		$("#" + TRANSPARENT_ID).slider();

		$("#" + EXPOSE_ID).slider({
			min: MIN_EXPOSE,
			max: MAX_EXPOSE
		});

		$("#" + ZOOM_ID).slider({
			min: MIN_ZOOM,
			max: MAX_ZOOM
		});
	}

	function bindEvents(me, bimSceneManager) {
		$('#' + RESET_ID).click(function() {
			bimSceneManager.defaultView();
		});

		$('#' + FRONT_ID).click(function() {
			bimSceneManager.frontView();
		});

		$('#' + SIDE_ID).click(function() {
			bimSceneManager.sideView();
		});

		$('#' + TOP_ID).click(function() {
			bimSceneManager.topView();
		});

		$('#' + PAN_ROTATE_ID).click(function() {
			bimSceneManager.togglePanRotate();
		});

		$("#" + ZOOM_ID).on("slide", function(event, ui) {
			var zoom = MAX_ZOOM - ui.value;
			bimSceneManager.setZoomLevel(zoom);
		});

		$("#" + TRANSPARENT_ID).on("slide", function(event, ui) {
			if (me.currentObjectId) {
				var factor = 100 - ui.value;
				bimSceneManager.setNodeTransparentLevel(me.currentObjectId, factor);
			}
		});

		$("#" + EXPOSE_ID).on("slide", function(event, ui) {
			if (me.currentObjectId) {
				bimSceneManager.exposeNodeWithItsStorey(me.currentObjectId, ui.value);
			}
		});


		$("#" + BASE_ID + "-header").click(function() {
			var c = $("#" + WRAPPER_ID);
			c.toggle();

			if (c.css("display") == "none") {
				$(this).removeClass("bim-minus");
				$(this).addClass("bim-plus");
			} else {
				$(this).removeClass("bim-plus");
				$(this).addClass("bim-minus");
			}
		});
	}

})();