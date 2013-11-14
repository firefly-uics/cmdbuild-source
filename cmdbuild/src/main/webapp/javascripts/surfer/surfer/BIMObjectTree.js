(function() {

	var CONSTANT_SCROLL_ERROR = 50;
	var TREE_PANEL_ID = "bim-object-tree";
	var TREE_PANEL_HEADER_ID = TREE_PANEL_ID + "-header";
	var TREE_CONTAINER_ID = TREE_PANEL_ID + "-wrapper";
	var TREE_NODE_DESCRIPTION = TREE_PANEL_ID + "-node-description";

	var TEMPLATE = 	"<div id='" + TREE_PANEL_ID + "'>" +
						"<h2 id='" + TREE_PANEL_HEADER_ID + "' class='bim-accordion-header bim-plus'>Objects</h2>" +
						"<div id='" + TREE_CONTAINER_ID + "'></div>" +
					"<div>";

	BIMObjectTree = function(sceneManager, containerId) {
		this.sceneManager = sceneManager;

		/*
		 * used to manage scrolling of
		 * control panel when select item
		 * from scene
		 */
		this.scrolEnabled = true;

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
	BIMObjectTree.prototype.sceneLoaded = function(sceneManager, scene) {
		_BIM_LOGGER.log("BIM OBJECT TREE:sceneLoaded", sceneManager, scene);

		$("#" + TREE_CONTAINER_ID).empty();

		var sceneData = scene.data();
		var treeHtml = "";
		var relations = sceneData.relationships;
		for (var i = 0, l = relations.length; i<l; i++) {
			var project = relations[i];
			treeHtml += ifcObjectDescription(project, 0);
		}

		$("#" + TREE_CONTAINER_ID).append(treeHtml);
		bindTreeEvents(this);
	};

	BIMObjectTree.prototype.objectSelected = function(sceneManager, objectId) {
		// clear current selection
		this.selectionCleaned();

		// select the tree item
		var checkBoxId = "#" + TREE_PANEL_ID + "-node-" + objectId;
		var checkBox = $(checkBoxId);
		checkBox.parent().addClass("bim-object-tree-item-selected");

		if (this.scrolEnabled) {
			// expand the parents
			var node = checkBox;
			while (node.length != 0) {

				if (node.hasClass("bim-object-tree-relations")) {
					node = node.siblings(".bim-object-tree-selector");
				}

				if (node.hasClass("bim-object-tree-selector")) {
					node.find(".bim-object-tree-row-expander-plus").trigger("click");
				}

				node = node.parent();
			}

			/*
			 * the check could have no
			 * offset if the container panel
			 * is hidden, so don't scroll
			 */
			if (checkBox.offset()) {
				// reset the scroll to 
				// have right offset
				$('#controls-wrapper').animate({
					scrollTop: 0
				}, 0);
				// scroll the controller panel to the new selection
				$('#controls-wrapper').animate({
					scrollTop: (checkBox.offset().top - CONSTANT_SCROLL_ERROR)
				}, 0);
			}
		} else {
			this.scrolEnabled = true;
		}
	};

	BIMObjectTree.prototype.selectionCleaned = function() {
		$(".bim-object-tree-item-selected").removeClass("bim-object-tree-item-selected");
	};

	function render(containerId) {
		$("#" + containerId).append(TEMPLATE);
		$("#" + TREE_CONTAINER_ID).toggle();
	}

	function bindTreeEvents(me) {

		$(".bim-object-tree-row-expander").click(function() {

			var expander = $(this);
			var elemenToToggle = expander.parent().siblings(".bim-object-tree-relations");

			elemenToToggle.toggle();

			if (elemenToToggle.css("display") == "none") {
				expander.removeClass("bim-object-tree-row-expander-minus");
				expander.addClass("bim-object-tree-row-expander-plus");
			} else {
				expander.removeClass("bim-object-tree-row-expander-plus");
				expander.addClass("bim-object-tree-row-expander-minus");
			}

			return false;
		});

		$('#' + TREE_PANEL_ID + ' input').on('change', function(event) {

			window._BIM_LOGGER.log(event);
			var checkBox = $(event.target);
			if (!checkBox) {
				return;
			}

			var id = extractNodeIdFromElement(checkBox);

			var show = checkBox.attr("checked") == "checked";
			if (show) {
				me.sceneManager.showObject(id);
			} else {
				me.sceneManager.hideObject(id);
			}

			return false;
		});

		var SELECTOR_CLASS = "bim-object-tree-selector";
		$("." + SELECTOR_CLASS).click(function() {

			var element = $(this);
			var id = extractNodeIdFromElement(element.find("input"));

			me.scrolEnabled = false; 	// prevent the scroll of tree 
										// when the selection
										// come from interface

			me.sceneManager.selectObject(id);
		}).dblclick(function() {
			$(this).children(".bim-object-tree-row-expander").trigger("click");
		});

		// start with all the node collapsed
		$(".bim-object-tree-relations").toggle();
	}

	function bindEvents(me) {
		$("#" + TREE_PANEL_HEADER_ID).click(function() {
			var c = $("#" + TREE_CONTAINER_ID);
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

	function ifcObjectDescription(obj, indent) {
		var name = obj.name || "---";
		var relations = (ifcRelationships(obj.decomposedBy, indent)) 
						+ (ifcRelationships(obj.definedBy, indent))
						+ (ifcRelationships(obj.contains, indent));

		var descriptionClass = "";
		var expanderClass = "bim-object-tree-row-expander";

		if (relations) {
			descriptionClass = TREE_NODE_DESCRIPTION;
			expanderClass += " bim-object-tree-row-expander-plus";
			indent += 1;
		}

		var expander = "<div class='" + expanderClass + "'></div>";
		var indentElement = "<div class='bim-indent bim-indent" + indent + "'></div>";
		var out = "<div class='bim-object-tree-item'>" +
						"<div class='bim-object-tree-selector'>" +
							indentElement +
							expander +

							"<input id='" + TREE_PANEL_ID + "-node-" + obj.id + "' type='checkbox' checked='checked'> " +
							"<label class='" + descriptionClass + "'>" + name;
	
							if (obj.type) {
								out += ' <span>(' + obj.type + ')</span>';
							}
		
							out += '</label>' +
	
						"</div>" +
						relations +

				"</div>";

		return out;
	};

	function ifcRelationships (rel, indent) {
		var html = "";
		var obj;

		if ((rel != null) && rel.length > 0) {

			indent = Math.min(indent + 1, 7);
			html = '<div class="' + TREE_PANEL_ID + '-relations">';

			for (var _i = 0, _len = rel.length; _i < _len; _i++) {
				obj = rel[_i];
				html += ifcObjectDescription(obj, indent);
			}

			return html += "</div>";
		}

		return html;
	};

	function extractNodeIdFromElement(element) {
		var id = String(element.attr("id"));
		var lastMinusIndex = id.lastIndexOf("-");
		var out = false;
		if (lastMinusIndex >= 0) {
			out = id.substring(lastMinusIndex + 1).trim();
		}

		return out;
	}

})();