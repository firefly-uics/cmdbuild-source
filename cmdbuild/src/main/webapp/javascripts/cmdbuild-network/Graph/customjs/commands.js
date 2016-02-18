(function($) {
	var commands = {
		test: function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			console.log("Test", param, paramActualized);
		},
		filterDomains: function(param) {
			var classDescription = $.Cmdbuild.utilities.getHtmlFieldValue("#"
					+ param.id);
			$.Cmdbuild.standard.commands.navigate({
				form: param.navigationForm,
				container: param.navigationContainer,
				classDescription: (classDescription) ? classDescription : "-1"
			});
		},
		applyFilters: function(param) {
			var formObject = $.Cmdbuild.dataModel.forms[param.filterByClass];
			var configuration = $.Cmdbuild.custom.configuration;
			configuration.filterClasses = [];
			if (formObject) {
				$.Cmdbuild.customvariables.selected.erase();
				for ( var key in formObject.checked) {
					if (formObject.checked[key] == false) {
						$.Cmdbuild.customvariables.selected.selectByClassName(
								key, true);
						configuration.filterClasses.push(key);
					}
				}
			}
			$.Cmdbuild.custom.commands.deleteSelection({
				selected: "true"
			});
			var formObject = $.Cmdbuild.dataModel.forms[param.filterByDomain];
			if (formObject) {
				$.Cmdbuild.customvariables.selected.erase();
				configuration.filterClassesDomains = [];
				for ( var key in formObject.checked) {
					$.Cmdbuild.customvariables.cacheDomains.setActive(key, formObject.checked[key]);
					if (formObject.checked[key] == false) {
						var domain = $.Cmdbuild.customvariables.cacheDomains.getDomain(key);
						if (! configuration.filterClassesDomains[domain.sourceId]) {
							configuration.filterClassesDomains[domain.sourceId] = [];
						}
						if (! configuration.filterClassesDomains[domain.destinationId]) {
							configuration.filterClassesDomains[domain.destinationId] = [];
						}
						configuration.filterClassesDomains[domain.sourceId].push({
							_id: key,
							description: key
						});
						configuration.filterClassesDomains[domain.destinationId].push({
							_id: key,
							description: key
						});
						console.log("removeEdge for ", key);
						$.Cmdbuild.customvariables.model.removeEdge({
							domainId: key
						});
					}
				}
			}
			$.Cmdbuild.standard.commands.dialogClose(param);
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
					$.Cmdbuild.customvariables.options.nodeTooltipEnabled);
			$("#edgeTooltipEnabled").prop("checked",
					$.Cmdbuild.customvariables.options.edgeTooltipEnabled);
			setTimeout(
					function() {
						$("#clusteringThreshold")
								.spinner(
										"value",
										$.Cmdbuild.customvariables.options.clusteringThreshold);
						$("#spriteDimension")
								.spinner(
										"value",
										$.Cmdbuild.customvariables.options.spriteDimension);
						$("#stepRadius").spinner("value",
								$.Cmdbuild.customvariables.options.stepRadius);
					}, 100);
		},
		navigateOnNode: function(param) {
			var selected = $.Cmdbuild.customvariables.selected.getCards(0, 1);
			if (selected.total <= 0) {
				return;
			}
			var classId = selected.rows[0].classId;
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
		optionsOk: function(param) {
			$.Cmdbuild.customvariables.viewer.refresh(true);
			$.Cmdbuild.standard.commands.dialogClose(param);
		},
		optionsPreview: function(param) {
			$.Cmdbuild.customvariables.viewer.refresh(true);
		},
		optionsCancel: function(param) {
			$.Cmdbuild.customvariables.viewer.refresh(true);
			$.Cmdbuild.standard.commands.dialogClose(param);
		},
		optionsReset: function(param) {
			$.Cmdbuild.g3d.Options.initVariables();
			$.Cmdbuild.customvariables.viewer.refresh(true);
			$.Cmdbuild.standard.commands.dialogClose(param);
		},
		boolean: function(param) {
			var value = (param.type === "displayLabel")
					? param.value
					: $.Cmdbuild.utilities.getHtmlFieldValue("#" + param.type);
			$.Cmdbuild.customvariables.options[param.type] = value;
			$.Cmdbuild.customvariables.options.changed();
			$.Cmdbuild.customvariables.viewer.refresh();
		},
		selectClass: function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			$.Cmdbuild.customvariables.selected.selectByClassName(
					paramActualized.node, param.addSelection);
			var form2Hook = $.Cmdbuild.dataModel.forms[paramActualized.id];
			form2Hook
					.selectRows($.Cmdbuild.custom.classesGrid.getAllSelected());
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
			$.Cmdbuild.customvariables.selected = new $.Cmdbuild.g3d.Selected(
					$.Cmdbuild.customvariables.model);
			new $.Cmdbuild.g3d.cache();
			// $.Cmdbuild.g3d.Options.loadConfiguration(CONFIGURATION_FILE,
			// function(response) {
			// $.Cmdbuild.custom.configuration = response;
			callback.apply(this, []);
			// }, this);
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
