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
		 *            layers from _CMCACHE
		 * @param {Array} :
		 *            layers[n].visibility
		 * @param {Array} :
		 *            layers[n].cardBinding
		 * @param {Integer} :
		 *            layers[n].maxZoom
		 * @param {Integer} :
		 *            layers[n].minZoom
		 * @param {String} :
		 *            layers[n].name
		 * @param {String} :
		 *            layers[n].description
		 * @param {String} :
		 *            layers[n].masterTableName // className
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
		selectCard: function(card) {
			var geoLayers = this.getLayers();
			geoLayers.forEach(function(geoLayer) {
				var adapter = geoLayer.get("adapter");
				if (adapter && adapter.selectCard) {
					adapter.selectCard(card);
				}
			});			
		},
		showLayer : function(layer, currentClassName, currentCardId)  {
			var style = Ext.decode(layer.style);
			var geoAttribute = {
					description : layer.description,
					masterTableName : layer.masterTableName,
					name : layer.name,
					type : layer.type,
					iconUrl :style.externalGraphic
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
			for (var i = 0; i < allLayers.length; i++) {
				if (!visibles.find(function(layer) {
					return layer.name === allLayers[i].name;
				})) {
					if (this.getLayerByName(allLayers[i].name)) {
						this.removeLayerByName(allLayers[i].name);
					}
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
			var allLayers = this.interactionDocument.getThematicLayers();
			var me = this;
			allLayers.forEach(function(layer) {
				if (!visibles.find(function(l) {
					return layer.name === l.name;
				})) {
					if (me.getLayerByName(layer.name)) {
						me.removeLayerByName(layer.name);
					}
				}
			});
		}
	});
})();
