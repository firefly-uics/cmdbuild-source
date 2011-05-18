(function() {
	Ext.ns("CMDBuild.core");
	
	CMDBuild.core.CMBaseController = function(conf) {
		if (conf && conf.view) {
			this.view = conf.view;
		} else {
			throw CMDBuild.core.error.controller.NO_VIEW(CMDBuild.core.CMBaseController.NAME);
		}
		this.listeners = this.listeners || {};
			
		for (var listener in conf.listeners) {
			this[listener] = conf.listeners[listener];
		}
	};

	CMDBuild.core.CMBaseController.NAME = "CMDBuild.core.CMBaseController";
})();