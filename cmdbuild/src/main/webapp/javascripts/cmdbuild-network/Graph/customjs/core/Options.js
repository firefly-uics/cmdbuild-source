(function($) {
	if (! $.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	var defaultOptions = {
		nodesTooltip: true,
		edgesTooltip: true,
		layoutType: "Hierarchical",
		projectionType: "Projection",
		explosionLevels: 1,
		labels: false
	};
	var Options = function() {
		this.observers = [];
		this.init = function(name) {
			return this[name];
		};
		$.extend(this, defaultOptions);
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
})(jQuery);