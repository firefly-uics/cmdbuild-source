(function() {
	var tr = CMDBuild.Translation.administration.modcartography.geoserver;
	
	Ext.define("CMDBuild.view.administration.gis.CMModGeoServer", {
		extend: "Ext.panel.Panel",
		
		cmName: "gis-geoserver",
		title: tr.title,
		firstShow: true,
		initComponent : function() {
			
			this.form = new CMDBuild.Administration.GeoServerForm({
				region: "south",
				split: true
			});
			
			this.addLayerButton = new Ext.button.Button({
				iconCls: 'add',
				text: tr.add_layer
	    	});
			
			this.layersGrid = new CMDBuild.Administration.GeoServerLayerGrid({
				region: "center",
				enableDragDrop: true,
				frame: true,
				tbar: [this.addLayerButton]
			});
			
			this.layout = "border";
			this.items = [this.layersGrid, this.form];
			this.frame = true;
			
			this.callParent(arguments);
		}
	});
})();