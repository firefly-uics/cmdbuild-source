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
			// select the first row or the modified
			this.store.on("load", function(store) {
				var recIndex = 0;
				if (store.nameToSelect) {
					recIndex = store.findExact("name", store.nameToSelect);
				}
				selectFirst.call(this);
			}, this);
		},

		clearSelection: function() {
			this.getSelectionModel().clearSelections();
		},

		onModShow: function(firstLoad) {
			if (firstLoad) {
				this.store.load({
					callback: onModShow,
					scope: this
				});
			} else {
				onModShow.call(this);
			}
		}
	});

	function selectFirst() {
		try {
			var sm = this.getSelectionModel();
			sm.select(recIndex);
		} catch (e) {
			_debug("GEOServerLayerGrid, Cannot select the row", e);
		}
	}
	
	function onModShow() {
		var sm = this.getSelectionModel();
		if (sm.hasSelection()) {
			return;
		} else {
			selectFirst.call(this);
		}
	}

})();