(function() {
	Ext.define('CMDBuild.Management.CMMap', {
		extend : 'CMDBuild.view.management.classes.map.geoextension.Map',
		requires : [ 'CMDBuild.view.management.classes.map.geoextension.Map' ],

		interactionDocument : undefined,

		/**
		 * @returns {Void}
		 * 
		 * @override
		 */
		configure : function() {
			this.interactionDocument.setConfigurationMap(this);
			this.interactionDocument.observe(this);
		},

		/**
		 * @returns {Void}
		 * 
		 * @override
		 */
		refresh : function() {
			var card = this.interactionDocument.getCurrentCard();
			var currentClassName = card.className;
			var currentCardId = card.cardId;
			var me = this;
			this.interactionDocument.getAllLayers(function(layers) {
				me.clearSelections();//Adding has a different refresh on gis layer
				me.refreshAllLayers(layers, currentClassName, currentCardId);
				me.refreshThematicLayers(currentClassName, currentCardId);
				me.selectCard({
					cardId : currentCardId,
					className : currentClassName
				});
//				me.setSelections();
				me.refreshLegend();
			}, this);
		},

		/**
		 * @param {String}
		 *            currentClassName
		 * @param {Integer}
		 *            currentCardId
		 * 
		 * @returns {Void}
		 */
		refreshThematicLayers : function(currentClassName, currentCardId) {
			var thematicLayers = this.interactionDocument.getThematicLayers();
			var visibleLayers = [];
			for (var i = 0; i < thematicLayers.length; i++) {
				var layer = thematicLayers[i];
				var mapThematicLayer = this.getLayerByClassAndName(layer.masterTableName, layer.name);
				var hide = !this.interactionDocument.getLayerVisibility(layer);
				if (hide) {
					continue;
				}
				if (!mapThematicLayer) {
					this.map.addLayer(layer);
					mapThematicLayer = layer;
				}
				var adapter = mapThematicLayer.get("adapter");
				if (adapter && adapter.refresh) {
					var thematism = adapter.getThematism();
					var originalLayer = thematism.configuration.originalLayer;
					var className = originalLayer.className;
					var name = originalLayer.name;
					thematism.layer = this.getLayerByClassAndName(className, name);
					adapter.refresh({
						cardId : currentCardId,
						className : currentClassName
					});
				}
				visibleLayers.push(layer);
			}
			this.removeThematicsNotVisibleLayers(visibleLayers);
		},

		/**
		 * @param {Array} :
		 *            layer layers from _CMCACHE
		 * @param {Array} :
		 *            layer.visibility
		 * @param {Array} :
		 *            layer.cardBinding
		 * @param {Integer} :
		 *            layer.maxZoom
		 * @param {Integer} :
		 *            layer.minZoom
		 * @param {String} :
		 *            layer.name
		 * @param {String} :
		 *            layer.description
		 * @param {String} :
		 *            layer.masterTableName // className
		 * 
		 * @param {String}
		 *            currentClassName
		 * @param {Integer}
		 *            currentCardId
		 * 
		 * @returns {Void}
		 */
		refreshAllLayers : function(layers, currentClassName, currentCardId) {
			layers.sort(sortLayers);
			var zoom = this.getMap().getView().getZoom();
			var visibleLayers = [];
			for (var i = 0; i < layers.length; i++) {
				var layer = layers[i];
				if (zoom < layer.minZoom || zoom > layer.maxZoom) {
					continue;
				}
				var visible = this.interactionDocument.isVisible(layer, currentClassName, currentCardId);
				var hide = !this.interactionDocument.getLayerVisibility(layer);
				var navigable = this.interactionDocument.isANavigableLayer(layer);
				if ((hide || !navigable) && visible) {
					this.clearHideLayer(layer.masterTableName, layer.name);
				} else if (visible && navigable) {
					this.showLayer(layer, currentClassName, currentCardId);
					visibleLayers.push(layer);
				}
			}
			this.removeNotVisibleLayers(layers, visibleLayers);
		},
		selectCard : function(card) {
			var geoLayers = this.getLayers();
			geoLayers.forEach(function(geoLayer) {
				var adapter = geoLayer.get("adapter");
				if (adapter && adapter.selectCard) {
					adapter.selectCard(card);
				}
			});
		},
		showLayer : function(layer, currentClassName, currentCardId) {
			var style = Ext.decode(layer.style);
			var geoAttribute = {
				description : layer.description,
				masterTableName : layer.masterTableName,
				name : layer.name,
				type : layer.type,
				iconUrl : style.externalGraphic
			};
			var geoLayer = this.getLayerByClassAndName(layer.masterTableName, layer.name);
			if (!geoLayer) {
				geoLayer = this.makeLayer(geoAttribute, true);
			}
			var adapter = geoLayer.get("adapter");
			if (adapter && adapter.refresh) {
				adapter.refresh();
			}
		},
		clearHideLayer : function(className, nameLayer) {
			var geoLayer = this.getLayerByClassAndName(className, nameLayer);
			if (geoLayer) {
				var adapter = geoLayer.get("adapter");
				if (adapter && adapter.clearAllFeatures) {
					adapter.clearAllFeatures();
				}
				this.map.removeLayer(geoLayer);
			}
		},
		setSelections : function() {
			var card = this.interactionDocument.getCurrentCard();
			this.map.getLayers().forEach(function(layer) {
				var adapter = layer.get("adapter");
				if (adapter && adapter.setStatus) {
					var geoAttribute = layer.get("geoAttribute");
					adapter.setStatus((geoAttribute && geoAttribute.masterTableName === card.className ) ? "Select" : "None");
				}
			});

		},
		clearSelections : function() {
			var gisLayers = this.interactionDocument.getGisAdapters();
			for ( var key in gisLayers) {
				var namedAdapters = gisLayers[key];
				for (var i = 0; i < namedAdapters.length; i++) {
					namedAdapters[i].adapter.refresh();
				}
			}

		},
		/**
		 * @param {Array} :
		 *            allLayers : layers from _CMCACHE
		 * @param {Array}
		 *            visibles : layers from _CMCACHE
		 * 
		 * @returns {Void}
		 */
		removeNotVisibleLayers : function(allLayers, visibles) {
			var allLayers = [];
			var mapLayers = this.map.getLayers();
			mapLayers.forEach(function(mapLayer) {
				allLayers.push(mapLayer);
			});

			var me = this;
			var index = 0;
			for(var i = 0; i < allLayers.length; i++) {
				var mapLayer = allLayers[i];
				var geoAttribute = mapLayer.get("geoAttribute");
				if (geoAttribute) {
					me.remove4GeoAttribute(mapLayer, geoAttribute, visibles);
				}
			}
		},

		/**
		 * @param {Object}
		 *            geoAttribute
		 * @param {Array}
		 *            visibles : layers from _CMCACHE
		 * 
		 * @returns {Void}
		 */
		remove4GeoAttribute : function(mapLayer, geoAttribute, visibles) {
			var mapLayerName = geoAttribute.name;
			var mapClassName = geoAttribute.masterTableName;
			function compare(layer) {
				return (layer.name === mapLayerName && layer.masterTableName === mapClassName);

			}
			var zoom = this.getMap().getView().getZoom();
			if (!visibles.find(compare)) {
				var adapter = mapLayer.get("adapter");
				if (adapter) {
					adapter.refresh();

				}
				this.clearHideLayer(mapClassName, mapLayerName);
				this.map.removeLayer(mapLayer);
			}
		},

		/**
		 * @param {Array}
		 *            visibles : layers from _CMCACHE
		 * 
		 * @returns {Void}
		 */
		removeThematicsNotVisibleLayers : function(visibles) {
			var allLayers = this.interactionDocument.getAllThematicLayers();
			var me = this;
			allLayers.forEach(function(layer) {
				if (!isVisible(visibles, layer)) {
					me.removeLayerByName(layer.masterTableName, layer.name);

				}
			});
		}
	});
	function isVisible(visibles, layer) {
		for (var i = 0; i < visibles.length; i++) {
			if (visibles[i].name === layer.name) {
				return true;
			}
		}
		return false;
	}
	function sortLayers(l1, l2) {
		return parseInt(l1.index) - parseInt(l2.index);
	}

})();
