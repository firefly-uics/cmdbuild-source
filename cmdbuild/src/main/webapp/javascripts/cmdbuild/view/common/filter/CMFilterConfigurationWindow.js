Ext.define("CMDBuild.view.common.filter.CMFilterConfigurationWindow", {
	extend: "CMDBuild.view.management.common.filter.CMFilterWindow",

	// override
	buildButtons: function() {
		var me = this;
		this.buttonAlign = "center";
		this.buttons = [{
			text: CMDBuild.Translation.common.btns.save,
			handler: function() {
				me.callDelegates("onCMFilterWindowSaveButtonClick", [me, me.getFilter()]);
			}
		},{
			text: CMDBuild.Translation.common.buttons.abort,
			handler: function() {
				me.callDelegates("onCMFilterWindowAbortButtonClick", [me]);
			}
		}];
	}
});