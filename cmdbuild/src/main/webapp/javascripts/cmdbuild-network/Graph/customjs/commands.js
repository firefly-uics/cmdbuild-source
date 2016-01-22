(function($) {
	var commands = {
		test: function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			console.log("Test", param, paramActualized);
		},
		switchOnSelected: function(param) {
			var check = $.Cmdbuild.utilities.getHtmlFieldValue("#"
					+ param.check);
			$.Cmdbuild.standard.commands.tab({
				form: param.form,
				activeTab: (check) ? 1 : 0
			});
		},
		initOptions: function(param) {
			$("#nodeTooltipEnabled").prop("checked",
					$.Cmdbuild.custom.configuration.nodeTooltipEnabled);
			$.Cmdbuild.customvariables.options["nodeTooltipEnabled"] = $.Cmdbuild.custom.configuration.nodeTooltipEnabled;
			$("#edgeTooltipEnabled").prop("checked",
					$.Cmdbuild.custom.configuration.edgeTooltipEnabled);
			$.Cmdbuild.customvariables.options["edgeTooltipEnabled"] = $.Cmdbuild.custom.configuration.edgeTooltipEnabled;
			$.Cmdbuild.customvariables.options["displayLabel"] = $.Cmdbuild.custom.configuration.displayLabel;
			var backendFn = $.Cmdbuild.utilities.getBackend(param.backend);
			setTimeout(function() {
				$("#clusteringThreshold").spinner("value",
						$.Cmdbuild.custom.configuration.clusteringThreshold);
			}, 100);
		},
		navigateOnNode: function(param) {
			var selected = $.Cmdbuild.customvariables.selected.getCards(0, 1);
			if (selected.total <= 0) {
				return;
			}
			var classId = selected.rows[0].className;
			var cardId = selected.rows[0].id;
			$.Cmdbuild.customvariables.viewer.clearSelection();
			$.Cmdbuild.customvariables.model.erase();
			$.Cmdbuild.customvariables.viewer.refresh(true);
			var init = new $.Cmdbuild.g3d.commands.init_explode(
					$.Cmdbuild.customvariables.model, {
						classId: classId,
						cardId: cardId
					});
			$.Cmdbuild.customvariables.commandsManager.execute(init, {},
					function(response) {
						$.Cmdbuild.customvariables.selected.erase();
						$.Cmdbuild.customvariables.selected.select(cardId);
						var me = this;
						setTimeout(function() {
							me.centerOnViewer();
						}, 500);
					}, this);
		},
		selectAll: function() {
			$.Cmdbuild.customvariables.selected.erase();
			var nodes = $.Cmdbuild.customvariables.model.getNodes();
			for (var i = 0; i < nodes.length; i++) {
				$.Cmdbuild.customvariables.selected.select(nodes[i].id(), true);
			}
			$.Cmdbuild.customvariables.selected.changed();
		},
		centerOnViewer: function() {
			var box = $.Cmdbuild.customvariables.viewer.boundingBox();
			$.Cmdbuild.customvariables.viewer.zoomAll(box);

		},
		openSelection: function(param) {
			var selected = $.Cmdbuild.customvariables.selected.getData();
			var levels = $.Cmdbuild.customvariables.options["explosionLevels"];
			var arCommands = getExplodeCommands(selected, levels);
			var macroCommand = new $.Cmdbuild.g3d.commands.macroCommand(
					$.Cmdbuild.customvariables.model, arCommands);
			$.Cmdbuild.customvariables.commandsManager
					.execute(macroCommand, {},
							function() {
								var nodes = $.Cmdbuild.customvariables.model
										.getNodes();
								$.Cmdbuild.g3d.Model.removeGraphData(nodes,
										"exploded_children");
							}, $.Cmdbuild.customvariables.viewer);

		},
		boolean: function(param) {
			var value = (param.type === "displayLabel") ?
				param.value : $.Cmdbuild.utilities
				.getHtmlFieldValue("#" + param.type);
			$.Cmdbuild.customvariables.options[param.type] = value;
			$.Cmdbuild.customvariables.options.changed();
		},
		selectNode: function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			if (param.addSelection === false) {
				$.Cmdbuild.customvariables.selected.erase();
				$.Cmdbuild.customvariables.selected
						.select(paramActualized.node);
			} else {
				if ($.Cmdbuild.customvariables.selected
						.isSelect(paramActualized.node)) {
					$.Cmdbuild.customvariables.selected
							.unSelect(paramActualized.node);
				} else {
					$.Cmdbuild.customvariables.selected
							.select(paramActualized.node);
				}
			}
			var form2Hook = $.Cmdbuild.dataModel.forms[paramActualized.id];
			form2Hook.selectRows($.Cmdbuild.customvariables.selected.getData());
		},
		initialize: function(callback) {
			$.Cmdbuild.customvariables.model = new $.Cmdbuild.g3d.Model();
			$.Cmdbuild.customvariables.selected = new $.Cmdbuild.g3d.Selected($.Cmdbuild.customvariables.model);
//			$.Cmdbuild.g3d.Options.loadConfiguration(CONFIGURATION_FILE, function(response) {
//				$.Cmdbuild.custom.configuration = response;
				callback.apply(this, []);
//			}, this);
		},
		doLayout: function(param) {
			$.Cmdbuild.customvariables.model.doLayout();
		},
		undo: function(param) {
			$.Cmdbuild.customvariables.commandsManager.undo();
		},
		stopCommands: function(param) {
			$.Cmdbuild.customvariables.commandsManager.stopped = true;
		},
		deleteSelection: function(param) {
			var deleteCards = new $.Cmdbuild.g3d.commands.deleteCards(
					$.Cmdbuild.customvariables.model,
					$.Cmdbuild.customvariables.selected, param.selected);
			$.Cmdbuild.customvariables.commandsManager.execute(deleteCards, {});
		},
		dijkstra: function(param) {
			new $.Cmdbuild.g3d.algorithms.dijkstra(
					$.Cmdbuild.customvariables.model,
					$.Cmdbuild.customvariables.selected);
		},
		connect: function(param) {
			new $.Cmdbuild.g3d.algorithms.connect(
					$.Cmdbuild.customvariables.model,
					$.Cmdbuild.customvariables.selected);
		},
		zoomOn: function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			$.Cmdbuild.customvariables.camera.zoomOn(paramActualized.node);
		}

	};
	$.Cmdbuild.custom.commands = commands;

	function getExplodeCommands(selected, levels) {
		var arCommands = [];
		for ( var key in selected) {
			arCommands.push({
				command: "explode_levels",
				id: key,
				levels: levels
			});
		}
		return arCommands;
	}
})(jQuery);
