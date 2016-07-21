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
			}, this);
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
				if (!visible || hide) {
					var pippo = 1;
				}
				if (!hide && visible) {
					var geoAttribute = {
						description : layer.description,
						masterTableName : "_Geoserver",
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
			this.removeNotVisibleLayers(layers, visibleLayers)
		},
		removeNotVisibleLayers : function(all, visibles) {
			for (var i = 0; i < all.length; i++) {
				if (!visibles.find(function(l) {
					return l.name === all[i].name;
				})) {
					if (this.getLayerByName(all[i].name)) {
						this.removeLayerByName(all[i].name);
					}
				}
			}
		},
	});
})();
