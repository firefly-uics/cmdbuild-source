(function() {
		Ext.define("CMDBuild.view.administration.tasks.CMTasksPanelTabPanel", {
			extend: "Ext.tab.Panel",
			activeTab: 0,
	        numberOfTabs: 3,
			height: "100%",
			border: false,
			defaults:{
				bodyPadding: 10,
				layout: 'anchor'
		    },
		
		    items:[],
			initComponent: function() {
				this.callParent(arguments);
				this.getTabBar().setVisible(false);
			}
		});
		var tabbedPanel = new CMDBuild.view.administration.tasks.CMTasksPanelTabPanel();
		Ext.define("CMDBuild.view.administration.tasks.CMTasksWizard", {
			extend: "Ext.form.Panel",
			id: 'tabForm',
			width: 350,
			height: "100%",
			border: false,
			bodyBorder: false,
	        cls: 'x-panel-body-default-framed cmbordertop',
			bodyCls: 'cmgraypanel',
			fieldDefaults: {
			    labelWidth: 200,
			    msgTarget: 'side'
			},
			items: [tabbedPanel],
			tabbedPanel: tabbedPanel
		});
})();
