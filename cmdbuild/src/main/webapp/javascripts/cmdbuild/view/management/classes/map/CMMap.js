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

		refresh : function() {
			var card = this.interactionDocument.getCurrentCard();
			var currentClassName = card.className;
			var currentCardId = card.cardId;
			var me = this;
			this.center(this.interactionDocument.getConfigurationMap());
			this.interactionDocument.getAllLayers(function(layers) {
				me.refreshAllLayers(layers, currentClassName, currentCardId);
				me.refreshThematicLayers(currentClassName, currentCardId);
			}, this);
		},
		refreshThematicLayers : function(layerscurrentClassName, currentCardId) {
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
					adapter.refresh(currentCardId);
				}
				visibleLayers.push(layer);
			}
			this.removeThematicsNotVisibleLayers(visibleLayers)
		},
		refreshAllLayers : function(layers, currentClassName, currentCardId) {
			if (!currentCardId) {
				return;
			}
			var visibleLayers = [];
			for (var i = 0; i < layers.length; i++) {
				var layer = layers[i];
				var visible = this.interactionDocument.isVisible(layer, currentClassName, currentCardId);
				var hide = this.interactionDocument.isHide(layer);
				if (!hide && visible) {
					var geoAttribute = {
						description : layer.description,
						masterTableName : layer.masterTableName,
						name : layer.name,
						type : layer.type
					};
					var geoLayer = this.getLayerByName(layer.name);
					if (!geoLayer) {
						geoLayer = this.makeLayer(geoAttribute, true);
					}
					var adapter = geoLayer.get("adapter");
					if (adapter && adapter.refresh) {
						adapter.refresh(currentCardId);
					}
					visibleLayers.push(layer);
				}
			}
			this.removeNotVisibleLayers(layers, visibleLayers);
		},
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
