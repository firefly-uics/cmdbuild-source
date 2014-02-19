(function() {

	Ext.define("CMDBuild.view.administration.tasks.null.CMTaskTabs", {

		constructor: function() {
			this.step1 = Ext.create('CMDBuild.view.administration.tasks.null.CMStep1');
		},

		getTabs: function() {
			return [this.step1];
		}
	});

	Ext.define("CMDBuild.view.administration.tasks.null.CMStep1", {
		extend: "Ext.panel.Panel",

		border: false,
		bodyCls: 'cmgraypanel',
		height: '100%',

		defaults: {
			anchor: '100%'
		},

		items: []
	});

})();