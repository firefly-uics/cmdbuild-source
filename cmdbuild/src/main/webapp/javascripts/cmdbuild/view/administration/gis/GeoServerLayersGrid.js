(function() {
	var tr = CMDBuild.Translation.administration.modClass.attributeProperties;
	var tr_geo = CMDBuild.Translation.administration.modClass.geo_attributes;
	
	Ext.define("CMDBuild.Administration.GeoServerLayerGrid", {
		extend: "Ext.grid.Panel",
		
		region: 'center',
		frame: false,
		border: false,
		loadMask: true,
		store: CMDBuild.ServiceProxy.geoServer.getGeoServerLayerStore(),
		sm: new Ext.selection.RowModel(),

		initComponent: function() {
			this.columns = [{
				header: tr.name,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "name",
				flex: 1
			},{
				header: tr.description,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "description",
				flex: 1
			},{
				header: tr.type,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "type",
				flex: 1
			},{
				header: tr_geo.min_zoom,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "minZoom",
				flex: 1
			},{
				header: tr_geo.max_zoom,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "maxZoom",
				flex: 1
			}];
			
			this.callParent(arguments);
		},

		clearSelection: function() {
			this.getSelectionModel().deselectAll();
		},

		onModShow: function(firstLoad) {
			this.store.load();
		},

		loadStoreAndSelectLayerWithName: function(name) {
			this.store.load({
				scope : this,
				callback: function(records, operation, success) {
					var toSelect = this.store.find("name", name);
					if (toSelect >= 0) {
						this.getSelectionModel().select(toSelect);
					} else if (records.length > 0) {
						this.getSelectionModel().select(0);
					}
				}
			});
		}
	});

})();