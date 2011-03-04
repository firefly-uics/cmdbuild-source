CMDBuild.Management.CardTabPanel = Ext.extend(CMDBuild.TabPanel, {
	initComponent : function() {
		CMDBuild.Management.CardTabPanel.superclass.initComponent.apply(this, arguments);
		this.subscribe('cmdb-select-class', this.onSelectClass, this);
	},	
	
	onSelectClass: function(eventParams) {
		if (eventParams.tabToOpen) {
			this.activateTabByAttr('cmdbName', eventParams.tabToOpen || CMDBuild.Constants.tabNames.card);
		}
	}
});
Ext.reg('cardtabpanel', CMDBuild.Management.CardTabPanel);
