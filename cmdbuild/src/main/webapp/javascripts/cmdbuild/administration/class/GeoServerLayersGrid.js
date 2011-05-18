(function() {
	var tr = CMDBuild.Translation.administration.modClass.attributeProperties;
	var tr_geo = CMDBuild.Translation.administration.modClass.geo_attributes;
	
	CMDBuild.Administration.GeoServerLayerGrid = Ext.extend(Ext.grid.GridPanel, {
		region: 'center',
		frame: false,
		border: false,
		loadMask: true,
		store: CMDBuild.ServiceProxy.geoServer.getGeoServerLayerStore(),
		viewConfig: {
			forceFit: true
		},
		sm: new Ext.grid.RowSelectionModel({singleSelect:true}),
		initComponent: function() {
			var columns = [{
				header: tr.name,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "name"
			},{
				header: tr.description,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "description"
			},{
				header: tr.type,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "type"
			},{
				header: tr_geo.min_zoom,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "minZoom"
			},{
				header: tr_geo.max_zoom,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "maxZoom"
			}];
			this.colModel = new Ext.grid.ColumnModel( {
			defaults: {
				width: 120,
				sortable: true
			},
			columns: columns
			});
			
			CMDBuild.Administration.GeoServerLayerGrid.superclass.initComponent.call(this, arguments);
			// select the first row or the modified
			this.store.on("load", function(store) {
				var recIndex = 0;
				if (store.nameToSelect) {
					recIndex = store.findExact("name", store.nameToSelect);
				}
				var sm = this.getSelectionModel();
				sm.selectRow(recIndex);
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

	
	function onModShow() {
		var sm = this.getSelectionModel();
		if (sm.hasSelection()) {
			return;
		} else {
			sm.selectRow(0);
		}
	}

})();