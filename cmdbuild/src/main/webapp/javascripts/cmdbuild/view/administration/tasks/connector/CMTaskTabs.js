(function() {

	/**
	 * Task's wizard tabs index
	 */
	Ext.define('CMDBuild.view.administration.tasks.connector.CMTaskTabs', {

		constructor: function() {
			this.step1 = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep1');
			this.step2 = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep2');
			this.step3 = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep3');
			this.step4 = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep4');
			this.step5 = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep5');
		},

		getTabs: function() {
			return [this.step1, this.step2, this.step3, this.step4, this.step5];
		}
	});

})();