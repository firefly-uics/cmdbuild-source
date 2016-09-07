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
			if (currentCardId !== -1) {
				this.center(this.interactionDocument.getConfigurationMap());
			}
			this.interactionDocument.getAllLayers(function(layers) {
				me.refreshAllLayers(layers, currentClassName, currentCardId);
				me.refreshThematicLayers(currentClassName, currentCardId);
				me.selectCard({
					cardId : currentCardId,
					className : currentClassName
				});
				this.map.render();
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
				var geoLayer = this.getLayerByClassAndName(layer.masterTableName, layer.name);
				var hide = ! this.interactionDocument.getLayerVisibility(layer);
				if (hide) {
					continue;
				}
				if (!geoLayer) {
					this.map.addLayer(layer);
					geoLayer = layer;
				}
				var adapter = geoLayer.get("adapter");
				if (adapter && adapter.refresh) {
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
			if (!currentCardId) {
				return;
			}
			var zoom = this.getMap().getView().getZoom();
			var visibleLayers = [];
			for (var i = 0; i < layers.length; i++) {
				var layer = layers[i];
				if (layer.minZoom > zoom || zoom > layer.maxZoom) {
					continue;
				}
				var visible = this.interactionDocument.isVisible(layer, currentClassName, currentCardId);
				var hide = ! this.interactionDocument.getLayerVisibility(layer);
				var navigable = this.interactionDocument.isANavigableLayer(layer);
				if ((hide || ! navigable) && visible) {
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
				adapter.refresh({
					cardId : currentCardId,
					className : currentClassName
				});
			}
		},
		clearHideLayer : function(className, nameLayer) {
			var geoLayer = this.getLayerByClassAndName(className, nameLayer);
			if (geoLayer) {
				var adapter = geoLayer.get("adapter");
				if (adapter && adapter.clearFeatures) {
					adapter.clearFeatures();
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
			var allLayers = this.map.getLayers();
			var me = this;
			allLayers.forEach(function(mapLayer) {
				var geoAttribute = mapLayer.get("geoAttribute");
				if (geoAttribute) {
					me.remove4GeoAttribute(geoAttribute, visibles);
				}
			});
		},

		/**
		 * @param {Object} geoAttribute
		 * @param {Array}
		 *            visibles : layers from _CMCACHE
		 * 
		 * @returns {Void}
		 */
		remove4GeoAttribute : function(geoAttribute, visibles) {
			var mapLayerName = geoAttribute.name;
			var mapClassName = geoAttribute.masterTableName;
			if (!visibles.find(function(layer) {
				return (layer.name === mapLayerName && layer.masterTableName === mapClassName);
			})) {
				if (this.getLayerByClassAndName(mapClassName, mapLayerName)) {
					this.removeLayerByName(mapClassName, mapLayerName);
				}
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

})();
