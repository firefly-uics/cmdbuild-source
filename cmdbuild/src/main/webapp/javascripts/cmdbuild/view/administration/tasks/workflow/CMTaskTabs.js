(function() {

	Ext.define("CMDBuild.view.administration.tasks.workflow.CMTaskTabs", {

		constructor: function() {
			this.step1 = Ext.create('CMDBuild.view.administration.tasks.workflow.CMStep1');
		},

		getTabs: function() {
			return [this.step1];
		}
	});

})();