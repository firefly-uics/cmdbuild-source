(function() {

	Ext.define("CMDBuild.view.administration.tasks.workflow.CMTaskTabs", {

		constructor: function() {
			this.step1 = new CMDBuild.view.administration.tasks.workflow.CMStep1();
		},

		getTabs: function() {
			return [this.step1];
		}
	});

})();