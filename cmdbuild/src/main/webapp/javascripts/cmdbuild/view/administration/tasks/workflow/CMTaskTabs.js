(function() {

	Ext.define("CMDBuild.view.administration.tasks.workflow.CMTaskTabs", {

		constructor: function() {
			this.step1 = new CMDBuild.view.administration.tasks.workflow.CMStep1();
			this.step2 = new CMDBuild.view.administration.tasks.workflow.CMStep2();
		},

		getTabs: function() {
			return [this.step1, this.step2];
		}
	});

	Ext.define("CMDBuild.view.administration.tasks.workflow.CMStep2", {
		extend: "Ext.panel.Panel",

		title:'Personal Details',
		defaultType: 'textfield',
		border: false,
		bodyCls: 'cmgraypanel',
		height: '100%',
		defaults: {
			anchor: '100%'
		},
		items: [{
			fieldLabel: 'Pluto',
			name: 'pluto',
		}]
	});

})();