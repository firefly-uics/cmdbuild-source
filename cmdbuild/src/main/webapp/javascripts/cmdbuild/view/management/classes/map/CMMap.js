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
				var geoLayer = this.getLayerByName(layer.name);
				var hide = this.interactionDocument.isHide(layer);
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
			var visibleLayers = [];
			for (var i = 0; i < layers.length; i++) {
				var layer = layers[i];
				var visible = this.interactionDocument.isVisible(layer, currentClassName, currentCardId);
				var hide = this.interactionDocument.isHide(layer);
				if (hide && visible) {
					this.clearHideLayer(layer.name);
				} else if (visible) {
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
			var geoLayer = this.getLayerByName(layer.name);
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
		clearHideLayer : function(nameLayer) {
			var geoLayer = this.getLayerByName(nameLayer);
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
		remove4GeoAttribute : function(geoAttribute, visibles) {
			var mapLayerName = geoAttribute.name;
			var mapClassName = geoAttribute.masterTableName;
			if (!visibles.find(function(layer) {
				return (layer.name === mapLayerName && layer.masterTableName === mapClassName);
			})) {
				if (this.getLayerByName(mapLayerName, mapClassName)) {
					this.removeLayerByName(mapLayerName, mapClassName);
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
					me.removeLayerByName(layer.name);

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
