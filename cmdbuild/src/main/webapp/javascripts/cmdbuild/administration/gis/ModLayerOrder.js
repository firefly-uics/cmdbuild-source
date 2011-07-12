(function() {
	
	Ext.define("CMDBuild.Administration.ModLayerOrder", {
		extend: "CMDBuild.Administration.LayerGrid",

		cmName: "gis-layers-order",
		title: CMDBuild.Translation.administration.modcartography.layermanager.title,
		initComponent: function() {
			Ext.apply(this, {
				border: true
			});
			
			this.callParent(arguments);
		},

		enableDragDrop: true,
		
		beforeRowMove: function(node, data, dropRec, dropPosition) {
			// TODO 3 to 4 adapt for the new signature and move to the controller
			// old signature function(objThis, oldIndex, newIndex, records) {
			
//			var data = records[0].data;
//			CMDBuild.LoadMask.get().show();
//			CMDBuild.ServiceProxy.saveLayerOrder({
//				oldIndex: oldIndex,
//				newIndex: newIndex,
//				failure: function() {
//					layersGrid.getStore().reload();
//				},
//				callback: function() {
//					CMDBuild.LoadMask.get().hide();
//				}
//			});
//			return true;
		}
	});
})();