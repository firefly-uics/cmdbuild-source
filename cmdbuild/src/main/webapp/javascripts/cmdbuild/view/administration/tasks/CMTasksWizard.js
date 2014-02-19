(function() {

	Ext.define("CMDBuild.view.administration.tasks.CMTasksWizard", {
		extend: "Ext.tab.Panel",

		activeTab: 0,
		numberOfTabs: 0,
		width: '100%',
		height: '100%',
		border: false,
		defaults: {
			bodyPadding: 10,
			layout: 'anchor'
		},
		items: [],

		initComponent: function() {
			this.callParent(arguments);
			this.getTabBar().setVisible(false);
		}
	});

})();
