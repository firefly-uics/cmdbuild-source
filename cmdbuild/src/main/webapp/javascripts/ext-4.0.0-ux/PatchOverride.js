Ext.define("Ext.grid.Panel", {
	extend: "Ext.grid.Panel",
	
	reconfigure: function() {
		this.callParent(arguments);

		var v = this.getView();
		v.getSelectionModel().bindComponent(v);
	}
});