(function($) {
	if (! $.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (! $.Cmdbuild.g3d.commands) {
		$.Cmdbuild.g3d.commands = {};
	}
	var init = function(model, params) {
		this.model = model;
		this.params = params;
		var backend = new $.Cmdbuild.g3d.backend.CmdbuildTest();
		this.execute = function(callback, callbackScope) {
			backend.getInitModel(params, function(elements) {
				this.model.pushElements(elements);
				this.model.changed();
				callback.apply(callbackScope, []);
			}, this);
		};
		this.undo = function() {
//			console.log("UNDO THE INIT?");
//			this.model.erase();
//			this.model.changed();
		};
	};
	$.Cmdbuild.g3d.commands.init = init;
})(jQuery);
