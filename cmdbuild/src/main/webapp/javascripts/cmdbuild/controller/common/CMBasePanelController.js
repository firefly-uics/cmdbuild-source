(function() {

	Ext.define("CMDBuild.controller.CMBasePanelController", {
		constructor: function(view) {
			this.view = view;
			this.view.on("CM_iamtofront", this.onViewOnFront, this);
		},

		onViewOnFront: function(p) {
			CMDBuild.log.info("onPanelActivate " + this.view.title, this, p);
		},

		callMethodForAllSubcontrollers: function(method, args) {
			if (this.subcontrollers) {
				for (var i=0, l=this.subcontrollers.length; i<l; ++i) {
					var c = this.subcontrollers[i];
					if (c && typeof c[method] == "function") {
						c[method].apply(c, args);
					}
				}
			}
		}
	});

})();