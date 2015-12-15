(function($) {
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
	$.Cmdbuild.g3d.Options.chargeConfiguration = function(callback, callbackScope) {
		var nameFile = $.Cmdbuild.appConfigUrl + "configuration.json";
		$.getJSON(nameFile, {}, function(response) {
			console.log("response = ", response);
			callback.apply(callbackScope, [response]);
		}, this);
	};
})(jQuery);