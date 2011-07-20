CMDBuild.ModPanel = Ext.extend(Ext.Panel, {
	basetitle: '',
	hideMode: "offsets",
	plugins: new Ext.ux.plugins.HeaderButtons(),
	afterBringToFront: function() {
		return true;
	},
	/**
	 * 
	 * @param {} params (optional) contains the selected class id and name
	 */
	selectPanel: function(eventParams) {
		if (eventParams && eventParams.itemName) {
			this.setTitle(this.basetitle + ' ' + eventParams.itemName);
		}
		var activateThePanel = this.afterBringToFront(eventParams);
		if (activateThePanel) {
			this.ownerCt.layout.setActiveItem(this.id);
		}
		this.fireEvent("select", eventParams);
	},

	initComponent: function() {
		CMDBuild.ModPanel.superclass.initComponent.apply(this, arguments);
//		this.subscribe('cmdb-select-'+this.modtype, this.selectPanel, this);
	}
});
