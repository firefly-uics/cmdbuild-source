(function () {
	Ext.define('CMDBuild.Management.CMMap', {
		extend: 'CMDBuild.view.management.classes.map.geoextension.Map',
		requires: ['CMDBuild.view.management.classes.map.geoextension.Map'],
		
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
			var currentClassId = (Ext.isEmpty(_CMCardModuleState.entryType)) ?
					undefined : _CMCardModuleState.entryType.getId();
			if (! currentClassId) {
				return;
			}
			var currentClassName = (Ext.isEmpty(_CMCardModuleState.entryType)) ?
					undefined : _CMCardModuleState.entryType.getName();
			var currentCardId = (Ext.isEmpty(_CMCardModuleState.card)) ?
					undefined : _CMCardModuleState.card.raw.Id;//getId();
			var me = this;
			this.interactionDocument.getAllLayers(function(layers) {
				me.refreshAllLayers(layers, currentClassId, currentClassName, currentCardId);
			}, this);
		},
		refreshAllLayers : function(layers, currentClassId, currentClassName, currentCardId) {
			if (! currentCardId) {
				return;
			}
			var visibleLayers = [];
			for (var i = 0; i < layers.length; i++) {
				var layer = layers[i];
				var visible = this.interactionDocument.isVisible(layer, currentClassName, currentCardId);
				var hide = this.interactionDocument.isHide(layer);
				if (! hide && visible) {
					var geoAttribute = {
							description : layer.description,	
							masterTableName : "_Geoserver",	
							name : layer.name,
							type : layer.type
					};
					var geoLayer = this.getLayerByName(layer.name);
					if (! geoLayer) {
						geoLayer = this.makeLayer(currentClassId, geoAttribute, true);
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
		removeNotVisibleLayers: function(all, visibles) {
			for (var i = 0; i < all.length; i++) {
				if (! visibles.find(function(l) {
					return l.name === all[i].name;
				})) {
					if (this.getLayerByName(all[i].name)) {
						this.removeLayerByName(all[i].name);
					}
				}
			}
		}
	});
})();
						
