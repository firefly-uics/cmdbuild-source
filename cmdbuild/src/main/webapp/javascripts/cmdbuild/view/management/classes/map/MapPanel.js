Ext.define("CMDBuild.Management.MapPanel", {
	extend: "Ext.panel.Panel",

	initComponent : function() {
//		var zoomSlider = new GeoExt.ZoomSlider({
//	        map: map,
//	        vertical: true,
//            height: 100,
//            x: 10,
//            y: 20,
//	        aggressive: false,                                                                                                                                                   
//	        plugins: new GeoExt.ZoomSliderTip({
//	            template: "<div>Zoom Level: {zoom}</div>"
//	        })
//	    });

//		var mapPanel = new GeoExt.MapPanel({
//			map: map,
//			center: center.transform(new OpenLayers.Projection("EPSG:4326"),map.getProjectionObject()),
//			zoom: CMDBuild.Config.gis.initialZoomLevel,
//			region: 'center',
//			border: false
//		});

//		var layersTree = new Ext.tree.TreePanel({
//			collapsible: true,
//			collapsed: false,
//			margins: '0 0 0 0',
//			cmargins: '5 5 0 5',
//			autoScroll: true,		    
//			region: 'east',			
//			split: true,
//			width: 200,
//			loader: new Ext.tree.TreeLoader({
//	            // applyLoader has to be set to false to not interfer with loaders
//	            // of nodes further down the tree hierarchy
//	            applyLoader: false
//	        }),
//	        root: {
//	            expanded: true,
//	            children: [{
//	    	        nodeType: "gx_baselayercontainer",
//	    	        expanded: true,
//	    	        layerStore: mapPanel.layers,
//	    	        iconCls: "cmdbuild-tree-folder-icon"
//	    	    },{
//	    	        nodeType: "gx_overlaylayercontainer",
//	    	        expanded: true,
//	    	        layerStore: mapPanel.layers,
//	    	        iconCls: "cmdbuild-tree-folder-icon"
//	    	    }]
//	        },
//	        rootVisible: false,
//		    border: false,
//		    style: {
//				"border-left": "1px " + CMDBuild.Constants.colors.blue.border + " solid", 
//				"border-bottom": "1px " +CMDBuild.Constants.colors.blue.border+ " solid"
//			}
//		});

//		map.controller = new CMDBuild.Management.MapController({
//			map: map,
//			container: this
//		});;

//		this.getController = function() {
//			return map.controller;
//		};

		Ext.apply(this, {
			hideMode: "offsets"
		});

		this.callParent(arguments);

//		this.on('show', function() {
//			//TODO find a solution for the problem with the slider synchronization
//			//Ugly ugly hack to force the update of the zoom slider
//			map.zoomIn();
//			map.zoomOut();
//		}, this);
	},

	// override
	onRender: function() {
		this.callParent(arguments);

		var map = CMDBuild.Management.MapBuilder.buildMap(this.body.dom.id);
//		var lon = CMDBuild.Config.gis['center.lon'];
//		var lat = CMDBuild.Config.gis['center.lat'];
//
//		var center = new OpenLayers.LonLat(lon,lat);

		this.getMap = function() {
			return map;
		};

	},

	onBack: function() {
		var c = this.getController();
		if (c) {
			c.onBack();
		}
	},
	
	onFront: function() {
		var c = this.getController();
		if (c) {
			c.onFront();
		}
	},
	
	enableModify: function() {
		if (this.editingWindow) {
			this.editingWindow.show();
		}
	},
	
	disableModify: function() {
		if (this.editingWindow) {
			this.editingWindow.hide();
		}
	},
	
	updateMap: function(params) {
		this.currentClassName = params.className;
		this.getMap().update(params, withEditLayer=true);
//		this.editingWindow.update();
	}
});