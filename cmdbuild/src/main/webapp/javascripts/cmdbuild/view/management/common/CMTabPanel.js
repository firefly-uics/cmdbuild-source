Ext.define("CMDBuild.view.management.common.CMTabPanel", {
	extend: "Ext.tab.Panel",
	plain: true,

	initComponent: function() {
		this.tabPosition = CMDBuild.Config.cmdbuild.card_tab_position || "top",
		this.callParent(arguments);
		if (this.items.getCount() == 1) {
			this.getTabBar().hide();
		}
	},

	activateFirst: function() {
		this.setActiveTab(0);
	},

	editMode: function() {
		this.items.each(function(item) {
			if (typeof item.editMode == "function") {
				item.editMode();
			}
		});
	},

	displayMode: function() {
		this.items.each(function(item) {
			if (typeof item.displayMode == "function") {
				item.displayMode();
			}
		});
	}
});