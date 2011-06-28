(function() {
	var tr = CMDBuild.Translation.administration.modcartography.geoserver;
	
	Ext.define("CMDBuild.controller.administration.gis.CMModGeoServerController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);
			
			this.view.on("show", function() {
				this.layersGrid.onModShow(this.firstShow);
				this.firstShow = false;
			}, this.view);

			this.view.layersGrid.getSelectionModel().on("rowselect"
					, this.view.form.onRowSelect 
					, this.view.form);

			this.view.addLayerButton.on("click", function() {
				this.form.onAddLayer();
				this.layersGrid.clearSelection();
			}, this.view);

		},
		onViewOnFront: function() {
			if (!geoserverIsEnabled()) {
				var msg = Ext.String.format(tr.service_not_available
						, CMDBuild.Translation.administration.modcartography.title +
							"/" + CMDBuild.Translation.administration.modcartography.external_services.title);
				
				_CMMainViewportController.bringTofrontPanelByCmName("notconfiguredpanel", msg);
				return false;
			}
		}
	});
	
	function geoserverIsEnabled() {
		return CMDBuild.Config.gis.geoserver && CMDBuild.Config.gis.geoserver == "on";
	}
})();