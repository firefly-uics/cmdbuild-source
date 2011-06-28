(function() {
	
	Ext.define("CMDBuild.Administration.ModLayerOrder", {
		extend: "Ext.panel.Panel",

		cmName: "gis-layers-order",
		title: CMDBuild.Translation.administration.modcartography.layermanager.title,
		
		initComponent : function() {
			var layersGrid = new CMDBuild.Administration.LayerGrid({
				region: "center",
				enableDragDrop: true,

				beforeRowMove: function(objThis, oldIndex, newIndex, records) {
					var data = records[0].data;
					CMDBuild.LoadMask.get().show();
					CMDBuild.ServiceProxy.saveLayerOrder({
						oldIndex: oldIndex,
						newIndex: newIndex,
						failure: function() {
							layersGrid.getStore().reload();
						},
						callback: function() {
							CMDBuild.LoadMask.get().hide();
						}
					});
					return true;
				}
			});

			this.items = [layersGrid];
			this.layout = "border";
			this.callParent(arguments);
		}
	});
})();