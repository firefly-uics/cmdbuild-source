(function() {

	var LAYERS_PANEL_ID = "bim-layers-panel";
	var LAYERS_PANEL_HEADER_ID = LAYERS_PANEL_ID + "-header";
	var LAYERS_CONTAINER_ID = LAYERS_PANEL_ID + "-wrapper";
	var TEMPLATE = 	"<div id='" + LAYERS_PANEL_ID + "'>" +
						"<h2 id='" + LAYERS_PANEL_HEADER_ID + "' class='bim-accordion-header bim-plus'>Layers</h2>" +
						"<ul id='" + LAYERS_CONTAINER_ID + "'></ul>" +
					"</div>";

	BIMLayersPanel = function(sceneManager, containerId) {
		this.sceneManager = sceneManager;
		sceneManager.addDelegate(this);

		render(containerId);
		bindEvents(this);
	};

	/**
	 * Method called from BIMSceneManager
	 * after scene loading ending
	 * 
	 * @param {Object} sceneManager
	 * @param {Object} scene
	 */
	BIMLayersPanel.prototype.sceneLoaded = function(sceneManager, scene) {

		// remove current layers
		$("#" + LAYERS_CONTAINER_ID).empty();

		var sceneData = scene.data();
		var ifcTypes = sceneData.ifcTypes;

		for (var i=0, l=ifcTypes.length; i<l; ++i) {
			var ifcType = ifcTypes[i];
			window._BIM_LOGGER.log(ifcType);
			$("#" + LAYERS_CONTAINER_ID).append("<li>" + getLayerCheckbox(ifcType) + "</li>");
		}

		bindCheckboxEvents(this);

//		othis.controlsPropertiesSelectObject();

	};

	function render(containerId) {
		$("#" + containerId).append(TEMPLATE);
		$("#" + LAYERS_CONTAINER_ID).toggle();
	}

	function bindEvents(me) {
		$("#" + LAYERS_PANEL_HEADER_ID).click(function() {
			var c = $("#" + LAYERS_CONTAINER_ID);
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

	function bindCheckboxEvents(me) {
		$('#' + LAYERS_PANEL_ID + ' input').on('change', function(event) {
			window._BIM_LOGGER.log(event);
			var checkBox = $(event.target);
			var layerName = checkBox.attr("className");
			var show = checkBox.attr("checked") == "checked";
			if (show) {
				me.sceneManager.showLayer(layerName);
			} else {
				me.sceneManager.hideLayer(layerName);
			}
		});
	}

	function getLayerCheckbox(ifcType) {

		if (!ifcType) {
			return "";
		}

		var label = ifcType.substring(3); // remove the ifc prefix from the label
		var id = "layer-" + ifcType.toLowerCase();
		return 	"<input id='" + id + "' className='" + ifcType + "' type='checkbox'> " +
				"<label for='" + id + "'>" + label + "</label>";
	}

})();