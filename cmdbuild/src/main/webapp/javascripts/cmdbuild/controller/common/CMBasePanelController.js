(function() {

	Ext.define("CMDBuild.controller.CMBasePanelController", {
		constructor: function(view) {
			this.view = view;
			
			this.view.on("CM_iamtofront", this.onViewOnFront, this);
		},

		onViewOnFront: function(p) {
			CMDBuild.log.info("onPanelActivate " + this.view.title, this, p);
		}
	});

})();