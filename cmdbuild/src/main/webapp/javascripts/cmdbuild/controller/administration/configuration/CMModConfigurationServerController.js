(function() {
	Ext.define("CMDBuild.controller.administration.configuration.CMModConfigurationServerController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		
		constructor: function(view) {
			this.callParent([view]);
			
			this.view.clearCacheButton.on("click", function() {
				CMDBuild.Ajax.request( {
					url : 'services/json/utils/clearcache',
					loadMask : true,
					success : CMDBuild.Msg.success
				});
			});
			
			this.view.clearProcesses.on("click", function() {
				CMDBuild.Ajax.request( {
					url : 'services/json/schema/modworkflow/removeallinconsistentprocesses',
					loadMask : true,
					success : CMDBuild.Msg.success
				});
			});
		}
	});
})();