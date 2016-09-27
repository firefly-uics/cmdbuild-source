(function() {
	CMDBuild.gis = {
		constants : {
			MAP_DIV : 'cmdbBaseBuildMap',
			MAP_OSM : 'osm',
			MAP_YAHOO : 'yahoo',
			MAP_GOOGLE : 'google',
			ENABLED : 'enabled',
			CENTER_LONGITUDE : 'centerLongitude',
			CENTER_LATITUDE : 'centerLatitude',
			ICON_SIZE : .7,

			layers : {
				PUNTUAL_ANALYSIS : "puntual_analysis",
				RANGES_ANALYSIS : "ranges_analysis",
				DENSITY_ANALYSIS : "density_analysis",
				TABLE_SOURCE : "table_source",
				FUNCTION_SOURCE : "function_source",
				DEFAULT_RADIUS : 8,
				ICON_SCALE : 1.5,
				GEO_MIN_ZINDEX : 1000,
				GIS_MIN_ZINDEX : 10000,
				THEMATIC_MIN_ZINDEX : 100000

			},
			shapes : {
				CIRCLE : "shape_circle",
				RECTANGLE : "shape_rectangle",
				STAR : "shape_star"
			},
			metadata : {
				TAGS : "system.entrytype.tags",
				MASTERTABLE : "system.entrytype.mastertable",
				THEMATICFUNCTION : "ThematicFunction"
			},
			colors : {
				POINT_FILL :  '#FFCC00',
				POINT_LINE:'#FF9966',
				POLYGON_FILL :  'rgba(0, 0, 255, 0.1)',
				POLYGON_LINE:'blue',
				LINE_LINE:'green',
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

		/**
		 * @property {"CMDBuild.view.management.classes.map.thematism.Legend"}
		 * 
		 */
		legend : undefined,
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
			configuration.center = ol.proj.transform(configuration.center, 'EPSG:4326', 'EPSG:3857')
			Ext.apply(this, {
				items : [ Ext.create('Ext.container.Container', {
					region : 'center',
					html : "<div id='" + configuration.mapDivId + "'></div>"
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

		listeners : {
			afterrender : function() {
				var configuration = this.interactionDocument.getConfigurationMap();
				var extent = ol.proj.get("EPSG:900913").getExtent();
				var center = ol.proj.transform(configuration.center, 'EPSG:3857', 'EPSG:4326')
				this.view = new ol.View({
					center : center,
					zoom : configuration.zoom,
					maxZoom: 50,
					minZoom: 1,
					extent:extent

				});
				this.map = new ol.Map({
					target : configuration.mapDivId,
					renderer : 'canvas',
					layers : [ this.geoExtension.getBaseLayer() ],
					view : this.view
				});
				me = this;
				this.map.getView().on('propertychange', function(e) {
					switch (e.key) {
					case 'resolution':
						me.refresh();
						break;
					}
				});
				var size = [ document.getElementById(this.id + "-body").offsetWidth,
						document.getElementById(this.id + "-body").offsetHeight ];
				this.map.setSize(size);
				this.createLegend();

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

		/**
		 * 
		 * @returns {Void}
		 * 
		 */
		clearSelection : function() {
			this.map.getLayers().forEach(function(layer) {
				var adapter = layer.get("adapter");
				if (adapter && adapter.clearSelections) {
					adapter.clearSelections();
				}
			});
		},
		/**
		 * 
		 * @returns {Void}
		 * 
		 */
		clearSource : function() {
			this.map.getLayers().forEach(function(layer) {
				var adapter = layer.get("adapter");
				if (adapter && adapter.clearSource) {
					adapter.clearSource();
				}
			});
		},

		createLegend : function() {
			var divContainerControl = document.createElement('div');
			document.getElementById(this.id + "-body").appendChild(divContainerControl);
			this.legend = Ext.create("CMDBuild.view.management.classes.map.thematism.Legend", {
				parentDiv : divContainerControl
			});
			this.legend.compose();
		},
		refreshLegend : function() {
			var arrLayers = this.interactionDocument.getThematicLayers();
			var visibles = [];
			for (var i = 0; i < arrLayers.length; i++)  {
				var layer = arrLayers[i];
				var visible = this.interactionDocument.getLayerVisibility(layer);
				if (visible) {
					visibles.push(layer);
				}
			}
			if (visibles.length > 0) {
				this.legend.refreshResults(visibles);
			} else {
				this.legend.hide();
			}
		},

		/**
		 * @param {String}
		 *            name
		 * 
		 * @returns {ol.Layer}
		 * 
		 */
		getLayerByClassAndName : function(className, name) {
			var retLayer = undefined
			var currentCard = this.interactionDocument.getCurrentCard();
			className = (className) ? className : currentCard.className;
			this.map.getLayers().forEach(function(layer) {
				var geoAttribute = layer.get("geoAttribute");
				var masterTableName = (geoAttribute) ? geoAttribute.masterTableName : layer.masterTableName;
				if (layer.get("name") === name && className === masterTableName) {
					retLayer = layer;
				}
			});
			return retLayer;
		},

		/**
		 * @param {Integer}
		 *            cardId
		 * @param {String}
		 *            className
		 * 
		 * @returns {Array} ol.Feature pay attention there is a translation of
		 *          the feature's type (Point -> POINT)
		 * 
		 */
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
		 * @param {String}
		 *            layerName
		 * 
		 * @returns {Void}
		 * 
		 */
		removeLayerByName : function(className, layerName) {
			var layer = this.getLayerByClassAndName(className, layerName);

			this.map.removeLayer(layer);
		},

		/**
		 * @param {Object}
		 *            configuration
		 * @param {Array}
		 *            configuration.center
		 * 
		 * @returns {Void}
		 * 
		 */
		center : function(configuration) {
			this.view.setCenter(configuration.center);
			this.clearSource();
			this.map.renderSync();
		},
		resetZoom : function() {
			var configuration = this.interactionDocument.getConfigurationMap();
			this.view.setZoom(configuration.zoom);
		},

		/**
		 * 
		 * @returns {ol.Map}
		 * 
		 */
		getMap : function() {
			return this.map;
		},

		/**
		 * 
		 * @returns {Array} ol.Layer
		 * 
		 */
		getLayers : function() {
			return this.map.getLayers();
		},

		/**
		 * @param {ol.Layer}
		 *            layer
		 * @param {Integer}
		 *            index
		 * 
		 * @returns {Void}
		 * 
		 */
		setLayerIndex : function(layer, index) {
			this.map.addLayer(layer);

			this.map.getLayers().insertAt(layer, 0);
		},

		/**
		 * @param {Object}
		 *            geoValues
		 * @param {String}
		 *            geoValues.type
		 * @param {String}
		 *            geoValues.name
		 * @param {String}
		 *            geoValues.description
		 * @param {String}
		 *            geoValues.masterTableName // class name
		 * @param {String}
		 *            geoValues.externalGraphic //icon url
		 * @param {Boolean}
		 *            withEditWindow
		 * 
		 * @returns {ol.Layer}
		 * 
		 */
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
			// if (geoValues.type !== "SHAPE") {
			this.map.addLayer(geoLayer);
			// }
			return geoLayer;
		},

		/**
		 * @param {Integer}
		 *            newId
		 * 
		 * @returns {Void}
		 * 
		 */
		changeFeatureOnLayers : function(newId) {
			this.map.getLayers().forEach(function(layer) {
				var adapter = layer.get("adapter");
				if (adapter && adapter.changeFeature)
					adapter.changeFeature(newId);
			});
		}
	});
})();
