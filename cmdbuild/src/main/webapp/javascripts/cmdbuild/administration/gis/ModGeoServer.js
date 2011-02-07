(function() {
	var tr = CMDBuild.Translation.administration.modcartography.geoserver;
	
	CMDBuild.Administration.ModGeoServer = Ext.extend(CMDBuild.ModPanel, {
		modtype: "gis-geoserver",
		title: tr.title,
		
		initComponent : function() {
			var layersGrid = new CMDBuild.Administration.GeoServerLayerGrid({
				region: "center",				
				enableDragDrop: true,
				cls: "cmdbuild_border_bottom_gray"
			});
			
			var form = new CMDBuild.Administration.GeoServerForm({
				region: "south",
				split: true
			});
			
			var addLayerButton = new Ext.Action({
	        	iconCls: 'add',
		        text: tr.add_layer,
		        handler: function() {
					form.onAddLayer();
					layersGrid.clearSelection();
		        },
		        scope: this
	    	});
			
			layersGrid.getSelectionModel().on('rowselect', form.onRowSelect , form);
			
			this.layout = "border";
			this.tbar = [addLayerButton];
			this.items = [layersGrid, form];
			
			this.afterBringToFront = function() {
				if (CMDBuild.Config.gis.geoserver && CMDBuild.Config.gis.geoserver == "on") {
					return true;
				} else {
					var msg = String.format(tr.service_not_available, CMDBuild.Translation.administration.modcartography.title +
								"/" + CMDBuild.Translation.administration.modcartography.external_services.title);
					this.publish('cmdb-select-notconfiguredpanel',{msg: msg});
					return false;
				}
			};
			
			CMDBuild.Administration.ModGeoServer.superclass.initComponent.call(this, arguments);
			this.on("show", function() {
				layersGrid.onModShow();
		    }, this);
		}
	});
})();