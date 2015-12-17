(function($) {
	var INCLUDED_FILE = "NetworkConfigurationFile";
	if (! $.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	var Options = function() {
		this.observers = [];
		this.init = function(name) {
			return this[name];
		};
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
	//statics
	$.Cmdbuild.g3d.Options.chargeConfiguration = function(configurationFile, callback, callbackScope) {
		var fileName = $.Cmdbuild.appConfigUrl + configurationFile;
		$.getJSON(fileName, {}, function(configuration) {
			$.Cmdbuild.g3d.Options.includeFiles(configuration, function() {
				console.log("configuration = ", configuration);
				callback.apply(callbackScope, [configuration]);
			}, this);
		}, this);
	};
	$.Cmdbuild.g3d.Options.includeFiles = function(configuration, callback, callbackScope) {
		console.log("include = ", configuration);
		var filesToInclude = [];
		for (var key in configuration) {
			if (configuration[key][INCLUDED_FILE]) {
				console.log("found = ", configuration[key][INCLUDED_FILE] );

				filesToInclude.push({
					key: key,
					fileName: configuration[key][INCLUDED_FILE] 
				});
			}
		}
		$.Cmdbuild.g3d.Options.chargeFiles(configuration, filesToInclude, function() {
			callback.apply(callbackScope, [configuration]);
		}), this;
	};
	$.Cmdbuild.g3d.Options.chargeFiles = function(configuration, filesToInclude, callback, callbackScope) {
		if (filesToInclude.length == 0) {
			callback.apply(callbackScope, [configuration]);
			return;
		}
		var file = filesToInclude[0];
		filesToInclude.splice(0, 1);
		$.Cmdbuild.g3d.Options.chargeConfiguration(file.fileName, function(response) {
			configuration[file.key] = response;
			$.Cmdbuild.g3d.Options.chargeFiles(configuration, filesToInclude, function() {
				callback.apply(callbackScope, [configuration]);
			}), this;
		}, this);
	};
})(jQuery);