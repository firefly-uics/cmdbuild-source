(function() {

	Ext.define("CMDBuild.view.administration.tasks.mail.CMTasksMailTabs", {

		constructor: function() {
			this.step1 = new CMDBuild.view.administration.tasks.mail.CMMailStep1();
			this.step2 = new CMDBuild.view.administration.tasks.mail.CMMailStep2();
			this.step3 = new CMDBuild.view.administration.tasks.mail.CMMailStep3();
		},

		getTabs: function() {
			return [this.step1, this.step2, this.step3];
		}
	});

})();