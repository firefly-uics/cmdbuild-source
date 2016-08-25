(function() {
	CMDBuild.gis = {
		constants : {
			MAP_DIV : 'cmdbBaseBuildMap',
			MAP_OSM : 'osm',
			MAP_YAHOO : 'yahoo',
			MAP_GOOGLE : 'google',
			ENABLED : 'enabled',
			ZOOM_INITIAL_LEVEL : 'zoomInitialLevel',
			CENTER_LONGITUDE : 'centerLongitude',
			CENTER_LATITUDE : 'centerLatitude',

			layers : {
				PUNTUAL_ANALYSIS : "puntual_analysis",
				RANGES_ANALYSIS : "ranges_analysis",
				DENSITY_ANALYSIS : "density_analysis",
				TABLE_SOURCE : "table_source",
				FUNCTION_SOURCE : "function_source"
			},
			shapes : {
				CIRCLE : "shape_circle",
				RECTANGLE : "shape_rectangle",
				STAR : "shape_star"
			}
		}
	};
	Ext.define('CMDBuild.view.management.classes.map.geoextension.Map', {
		extend : 'Ext.panel.Panel',

		requires : [ 'CMDBuild.core.constants.Proxy', 'CMDBuild.view.management.classes.map.geoextension.Layer',
				'CMDBuild.view.management.classes.map.geoextension.GisLayer' ],
		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.user.User}
		 */
		delegate : undefined,

		baseTitle : CMDBuild.Translation.users,
		bodyCls : 'cmdb-gray-panel-no-padding',
		region : "center",
		cls : "cmdb-border-right",
		border : false,
		frame : false,
		layout : 'border',
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
		initComponent : function() {
			this.configure();
			var configuration = this.interactionDocument.getConfigurationMap();
			Ext.apply(this, {
				items : [ Ext.create('Ext.container.Container', {
					region : 'center',
					html : "<div id='" + configuration.mapDivId + "'></div>"
				}) ]
			});
			this.geoExtension.setMap(this);
			this.callParent(arguments);
		},
		getGeometries : function(cardId, className) {
			var layers = this.getLayers();
			var geoAttributes = {};
			this.getLayers().forEach(function(layer) {
				var adapter = layer.get("adapter");
				if (adapter && adapter.getGeometries) {
					var layerName = layer.get("name");
					geoAttributes[layerName] = adapter.getGeometries(cardId, className);
				}
			});
			return geoAttributes;
		},

		/**
		 * @returns {Void}
		 */
		configure : function() {
		},

		listeners : {
			afterrender : function() {
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
				var size = [ document.getElementById(this.id + "-body").offsetWidth,
						document.getElementById(this.id + "-body").offsetHeight ];
				  var divContainerControl =  document.createElement('div');
				  divContainerControl.innerHTML = "<div style='position: absolute;'><h1>pippo</h1><input type='button' onClick='alert(1)'>pippo</input></div>";
				this.map.setSize(size);
				  document.getElementById(this.id + "-body").appendChild(divContainerControl); 
			},
			resize : function() {
				var size = [ document.getElementById(this.id + "-body").offsetWidth,
						document.getElementById(this.id + "-body").offsetHeight ];
				this.map.setSize(size);
			},
			show : function(panel, eOpts) {
				this.delegate.cmfg('onUserAndGroupUserShow');
			}
		},
		// interface
		getLayerByName : function(name) {
			var retLayer = undefined
			this.map.getLayers().forEach(function(layer) {
				if (layer.get("name") === name) {
					retLayer = layer;
				}
			});
			return retLayer;
		},
		clearSelection : function() {
			this.map.getLayers().forEach(function(layer) {
				var adapter = layer.get("adapter");
				if (adapter && adapter.clearFeatures) {
					adapter.clearFeatures();
				}
			});
		},
		removeLayerByName : function(layerName) {
			var layer = this.getLayerByName(layerName);
			this.map.removeLayer(layer);
		},
		center : function(configuration) {
			this.view.setCenter(configuration.center);
			this.map.renderSync();
		},

		/**
		 * 
		 * @returns {ol.Map}
		 * 
		 */
		getMap : function() {
			return this.map;
		},
		getLayers : function() {
			return this.map.getLayers();
		},
		setLayerIndex : function(layer, index) {
			this.map.addLayer(layer);

			this.map.getLayers().insertAt(layer, 0);
		},
		makeLayer : function(geoValues, withEditWindow) {
			var layer;
			if (geoValues.type === "SHAPE") {
				layer = Ext.create('CMDBuild.view.management.classes.map.geoextension.Layer', geoValues,
						withEditWindow, this.interactionDocument);
			} else {
				layer = Ext.create('CMDBuild.view.management.classes.map.geoextension.GisLayer', geoValues,
						withEditWindow, this.interactionDocument);

			}
			var geoLayer = layer.getLayer();
			this.map.addLayer(geoLayer);
			return geoLayer;
		},
		changeFeatureOnLayers : function(newId) {
			this.map.getLayers().forEach(function(layer) {
				var adapter = layer.get("adapter");
				if (adapter && adapter.changeFeaturegetMap)
					adapter.changeFeature(newId);
			});
		}
	});
})();
