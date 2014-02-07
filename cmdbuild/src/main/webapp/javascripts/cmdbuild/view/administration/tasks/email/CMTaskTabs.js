(function() {

	Ext.define("CMDBuild.view.administration.tasks.email.CMTaskTabs", {

		constructor: function() {
			this.step1 = new CMDBuild.view.administration.tasks.email.CMStep1();
			this.step2 = new CMDBuild.view.administration.tasks.email.CMStep2();
			this.step3 = new CMDBuild.view.administration.tasks.email.CMStep3();
		},

		getTabs: function() {
			return [this.step1, this.step2, this.step3];
		}
	});

})();