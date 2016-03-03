(function($) {
	function configureFromServer(configuration, graphConfiguration) {// graph,
		// server
		configuration.nodeTooltipEnabled = graphConfiguration.nodeTooltipEnabled;
		configuration.edgeTooltipEnabled = graphConfiguration.edgeTooltipEnabled;
		configuration.stepRadius = graphConfiguration.stepRadius;
		configuration.spriteDimension = graphConfiguration.spriteDimension;
		configuration.edgeColor = graphConfiguration.edgeColor;
		configuration.displayLabel = graphConfiguration.displayLabel;
		configuration.explosionLevels = graphConfiguration.baseLevel;
		configuration.clusteringThreshold = graphConfiguration.clusteringThreshold;
		configuration.expandingThreshold = graphConfiguration.expandingThreshold;

		configuration.viewPointDistance = $.Cmdbuild.g3d.constants.RANGE_VIEWPOINTDISTANCE
				/ 2 - graphConfiguration.viewPointDistance;
		// configuration.camera.position.z = graphConfiguration.viewPointHeight;
	}
	var INCLUDED_FILE = "NetworkConfigurationFile";
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	var Options = function() {
		this.observers = [];
		this.init = function(name) {
			return this[name];
		};

		if (!$.Cmdbuild.customvariables.options) {
			$.Cmdbuild.customvariables.options = {};
		}
		for ( var key in $.Cmdbuild.custom.configuration) {
			this[key] = $.Cmdbuild.custom.configuration[key];
		}
		this.data = function(name) {
			return this[name];
		};
		this.observe = function(observer) {
			this.observers.push(observer);
		};
		this.changed = function(params) {
			// for (var i = 0; i < this.observers.length; i++) {
			// this.observers[i].refreshOptions(params);
			// }
		};
		this.init();
	};
	$.Cmdbuild.g3d.Options = Options;
	$.Cmdbuild.g3d.Options.initVariables = function() {// from saved to session
		$.Cmdbuild.customvariables.options.nodeTooltipEnabled = $.Cmdbuild.custom.configuration.nodeTooltipEnabled;
		$.Cmdbuild.customvariables.options.edgeTooltipEnabled = $.Cmdbuild.custom.configuration.edgeTooltipEnabled;
		$.Cmdbuild.customvariables.options.displayLabel = $.Cmdbuild.custom.configuration.displayLabel;
		$.Cmdbuild.customvariables.options.clusteringThreshold = $.Cmdbuild.custom.configuration.clusteringThreshold;
		$.Cmdbuild.customvariables.options.spriteDimension = $.Cmdbuild.custom.configuration.spriteDimension;
		$.Cmdbuild.customvariables.options.stepRadius = $.Cmdbuild.custom.configuration.stepRadius;
		$.Cmdbuild.customvariables.options.explosionLevels = $.Cmdbuild.custom.configuration.explosionLevels;
	};
	$.Cmdbuild.g3d.Options.initFields = function() {
		// $("#explosionLevels").spinner("value",
		// $.Cmdbuild.custom.configuration.explosionLevels);

	};
	$.Cmdbuild.g3d.Options.loadConfiguration = function(callback, callbackScope) {
		$.Cmdbuild.g3d.proxy.getGraphConfiguration(function(
				graphConfiguration) {
			// CONFIGURATION:
			// here the mixing between the
			// Json properties and those
			// defined
			// in administration/setup
			console.log("graphConfiguration ", graphConfiguration);
//			configureFromServer(configuration, graphConfiguration);
			callback.apply(callbackScope, [ graphConfiguration ]);
		}, this);
	};
	$.Cmdbuild.g3d.Options.loadJsonConfiguration = function(configurationFile,
			callback, callbackScope) {
		var fileName = $.Cmdbuild.appConfigUrl + configurationFile;
		$.getJSON(fileName, {}, function(configuration) {
			$.Cmdbuild.g3d.Options.includeFiles(configuration, function() {
				callback.apply(callbackScope, [ configuration ]);
			}, this);
		}, this);
	};
	$.Cmdbuild.g3d.Options.includeFiles = function(configuration, callback,
			callbackScope) {
		var filesToInclude = [];
		for ( var key in configuration) {
			if (configuration[key][INCLUDED_FILE]) {
				filesToInclude.push({
					key : key,
					fileName : configuration[key][INCLUDED_FILE]
				});
			}
		}
		$.Cmdbuild.g3d.Options.loadFiles(configuration, filesToInclude,
				function() {
					callback.apply(callbackScope, [ configuration ]);
				}, this);
	};
	$.Cmdbuild.g3d.Options.loadFiles = function(configuration, filesToInclude,
			callback, callbackScope) {
		if (filesToInclude.length === 0) {
			callback.apply(callbackScope, [ configuration ]);
			return;
		}
		var file = filesToInclude[0];
		filesToInclude.splice(0, 1);
		$.Cmdbuild.g3d.Options.loadJsonConfiguration(file.fileName, function(
				response) {
			configuration[file.key] = response;
			$.Cmdbuild.g3d.Options.loadFiles(configuration, filesToInclude,
					function() {
						callback.apply(callbackScope, [ configuration ]);
					}, this);
		}, this);
	};
})(jQuery);