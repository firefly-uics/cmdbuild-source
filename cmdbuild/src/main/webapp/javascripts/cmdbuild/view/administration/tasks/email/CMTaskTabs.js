(function() {

	Ext.define("CMDBuild.view.administration.tasks.email.CMTaskTabs", {

		constructor: function() {
			this.step1 = Ext.create('CMDBuild.view.administration.tasks.email.CMStep1');
			this.step2 = Ext.create('CMDBuild.view.administration.tasks.email.CMStep2');
			this.step3 = Ext.create('CMDBuild.view.administration.tasks.email.CMStep3');
		},

		getTabs: function() {
			return [this.step1, this.step2, this.step3];
		}
	});

})();