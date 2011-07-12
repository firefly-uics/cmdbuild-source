(function() {
	var tr = CMDBuild.Translation.administration.modcartography.geoserver;
	
	Ext.define("CMDBuild.view.administration.gis.CMModGeoServer", {
		extend: "Ext.panel.Panel",
		
		cmName: "gis-geoserver",
		firstShow: true,
		initComponent : function() {
			this.addLayerButton = new Ext.button.Button({
				iconCls: 'add',
				text: tr.add_layer
			});

			this.layersGrid = new CMDBuild.Administration.GeoServerLayerGrid({
				title: tr.title,
				region: "center",
				enableDragDrop: true,
				frame: false,
				border: true,
				tbar: [this.addLayerButton]
			});

			this.form = new CMDBuild.Administration.GeoServerForm({
				height: "50%",
				autoScroll: true,
				frame: true,
				border: true,
				region: "south",
				split: true
			});

			Ext.apply(this, {
				layout: "border",
				items: [this.layersGrid, this.form],
				border: false
			});

			this.callParent(arguments);
		}
	});
})();