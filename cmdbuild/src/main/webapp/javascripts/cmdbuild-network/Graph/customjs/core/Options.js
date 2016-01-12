(function($) {
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
			for (var i = 0; i < this.observers.length; i++) {
				this.observers[i].refreshOptions(params);
			}
		};
		this.init();
	};
	$.Cmdbuild.g3d.Options = Options;
	$.Cmdbuild.g3d.Options.initFields = function() {
		$("#explosionLevels").spinner("value",
				$.Cmdbuild.custom.configuration.explosionLevels);

	};
	$.Cmdbuild.g3d.Options.loadConfiguration = function(configurationFile,
			callback, callbackScope) {
		$.Cmdbuild.g3d.Options
				.loadJsonConfiguration(
						configurationFile,
						function(configuration) {
							$.Cmdbuild.utilities.proxy
									.getGraphConfiguration(
											function(graphConfiguration) {
												// CONFIGURATION:
												// here the mixing between the
												// Json properties and those
												// defined
												// in administration/setup
												configuration.explosionLevels = graphConfiguration.baseLevel;
												callback.apply(callbackScope,
														[configuration]);
											}, this);
						}, this);
	};
	$.Cmdbuild.g3d.Options.loadJsonConfiguration = function(configurationFile,
			callback, callbackScope) {
		var fileName = $.Cmdbuild.appConfigUrl + configurationFile;
		$.getJSON(fileName, {}, function(configuration) {
			$.Cmdbuild.g3d.Options.includeFiles(configuration, function() {
				callback.apply(callbackScope, [configuration]);
			}, this);
		}, this);
	};
	$.Cmdbuild.g3d.Options.includeFiles = function(configuration, callback,
			callbackScope) {
		var filesToInclude = [];
		for ( var key in configuration) {
			if (configuration[key][INCLUDED_FILE]) {
				filesToInclude.push({
					key: key,
					fileName: configuration[key][INCLUDED_FILE]
				});
			}
		}
		$.Cmdbuild.g3d.Options.loadFiles(configuration, filesToInclude,
				function() {
					callback.apply(callbackScope, [configuration]);
				}), this;
	};
	$.Cmdbuild.g3d.Options.loadFiles = function(configuration, filesToInclude,
			callback, callbackScope) {
		if (filesToInclude.length == 0) {
			callback.apply(callbackScope, [configuration]);
			return;
		}
		var file = filesToInclude[0];
		filesToInclude.splice(0, 1);
		$.Cmdbuild.g3d.Options.loadJsonConfiguration(file.fileName, function(
				response) {
			configuration[file.key] = response;
			$.Cmdbuild.g3d.Options.loadFiles(configuration, filesToInclude,
					function() {
						callback.apply(callbackScope, [configuration]);
					}), this;
		}, this);
	};
})(jQuery);