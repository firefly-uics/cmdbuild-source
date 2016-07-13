(function () {
	CMDBuild.gis = {
		constants : {
			MAP_DIV :'cmdbBaseBuildMap',
			MAP_OSM : 'osm',
			MAP_YAHOO : 'yahoo',
			MAP_GOOGLE : 'google',
			ENABLED : 'enabled',
			ZOOM_INITIAL_LEVEL :'zoomInitialLevel', 
			CENTER_LONGITUDE :'centerLongitude', 
			CENTER_LATITUDE :'centerLatitude'
		}
	};
	Ext.define('CMDBuild.view.management.classes.map.geoextension.Map', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.view.management.classes.map.geoextension.Layer',
			'CMDBuild.view.management.classes.map.geoextension.GisLayer'
			],
		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.user.User}
		 */
		delegate: undefined,

		baseTitle: CMDBuild.Translation.users,
		bodyCls: 'cmdb-gray-panel-no-padding',
		region: "center",
		cls: "cmdb-border-right",
		border: false,
		frame: false,
		layout: 'border',
		baseLayer : undefined,
		configuration : {
			center : undefined,
			zoom : undefined
		},
		constructor : function(options) {
			this.geoExtension = options.geoExtension;
			this.callParent(arguments);
		},
		
		/**
		 * @returns {Void}
		 * 
		 * @override
		 */
		initComponent: function () {
			this.configure();
			var configuration = this.interactionDocument.getConfigurationMap();			
			Ext.apply(this, {
				items : [ Ext.create('Ext.container.Container', {
					region : 'center',
					html : "<div id='"+ configuration.mapDivId + "'></div>"
				}) ]
			});
			this.geoExtension.setMap(this);
			this.callParent(arguments);
		},

		/**
		 * @returns {Void}
		 */
		configure : function() {
		},
		
 		listeners: {
			afterrender: function () {
				var configuration = this.interactionDocument.getConfigurationMap();			
				this.view = new ol.View({
					center : configuration.center,
					zoom : configuration.zoom
				});
				this.map = new ol.Map({
					target : configuration.mapDivId,
					renderer : 'canvas',
					layers : [ this.geoExtension.getBaseLayer() ],
					view : this.view
				});
                var size = [document.getElementById(this.id + "-body").offsetWidth, document.getElementById(this.id + "-body").offsetHeight];
                this.map.setSize(size);
			},
            resize: function () {
                var size = [document.getElementById(this.id + "-body").offsetWidth, document.getElementById(this.id + "-body").offsetHeight];
                this.map.setSize(size);
            },
			show: function (panel, eOpts) {
				this.delegate.cmfg('onUserAndGroupUserShow');
			}
		},
		// interface
		getLayerByName : function(name) {
			var retLayer = undefined
			this.map.getLayers().forEach(function (layer) { 
				if (layer.get("name") === name)  {
					retLayer = layer; 
				}
			});
			return retLayer;
		},
		clearSelection : function() {
			this.map.getLayers().forEach(function (layer) { 
				var adapter = layer.get("adapter");
				if (adapter && adapter.clearFeatures)  {
					adapter.clearFeatures(); 
				}
			});
		},
		removeLayerByName : function(layerName) {
			var layer = this.getLayerByName(layerName);
			this.map.removeLayer(layer);
		},
		center: function(configuration) {
	        var size = /** @type {ol.Size} */ (this.map.getSize());
	        this.view.setCenter(configuration.center);
		},
		
		getMap: function() {
			return this.map;
		},
		getLayers: function() {
			return this.map.getLayers();
		},
		setLayerIndex: function(layer, index) {
			this.map.addLayer(layer); 

			this.map.getLayers().insertAt(layer, 0); 
		},
		makeLayer : function(classId, geoValues, withEditWindow) {
			var layer;
			if (geoValues.type === "SHAPE") {
				layer =  Ext.create('CMDBuild.view.management.classes.map.geoextension.Layer', 
						classId, geoValues, withEditWindow, this.interactionDocument);
			}
			else {
				layer =  Ext.create('CMDBuild.view.management.classes.map.geoextension.GisLayer', 
						classId, geoValues, withEditWindow, this.interactionDocument);
				
			}
			var geoLayer = layer.getLayer();
			this.map.addLayer(geoLayer);
			return geoLayer;
		},
		// end interface

		// compatibility
		addLayer : function(layer) {
			return this.map.addLayer(layer);
		},
		getLayers : function() {
			return (this.map) ? this.map.getLayers() : [];
		},
		getGeoServerLayerByName : function() {
			console.log("getGeoServerLayerByName ");
		},
		getProjection : function() {
			console.log("getProjection ");
		},
		addControl : function() {
			console.log("addControl ");
		},

		getCmdbLayers : function() {
			var out = [];
			return out;
		}
		// end compatibility
	});

	
})();
						
