CMDBuild.Management.DomainCardListWindow = Ext.extend(CMDBuild.Management.CardListWindow, {

	initComponent: function() {
		CMDBuild.Management.DomainCardListWindow.superclass.initComponent.apply(this);
	},
	
	onCancel: function() {
		this.close();
	}
});
