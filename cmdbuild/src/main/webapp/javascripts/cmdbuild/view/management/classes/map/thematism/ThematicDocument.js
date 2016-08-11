(function() {
	Ext.define('CMDBuild.view.management.classes.map.thematism.ThematicDocument', {
		thematisms : [],
		strategiesManager : undefined,
		setInteractionDocument : function(interactionDocument) {
			this.interactionDocument = interactionDocument;
		},
		configureStrategiesManager : function(strategiesManager) {
			this.strategiesManager = strategiesManager
		},
		getFieldStrategies : function(callback, callbackScope) {
			this.strategiesManager.getFieldStrategies(function(strategies) {
				callback.apply(callbackScope, [strategies]);
			}, this);
		},
		getFunctionStrategies : function(callback, callbackScope) {
			this.strategiesManager.getFunctionStrategies(function(strategies) {
				callback.apply(callbackScope, [strategies]);
			}, this);
		},
		getLayers : function() {
			var layers = [];
			for (var i = 0; i < this.thematisms.length; i++) {
				var thematism = this.thematisms[i];
				layers.push(thematism.thematicLayer.layer);
			}
			return layers;
		},
		getThematicLayersBySourceName : function(name) {
			var thematicLayers = [];
			for (var i = 0; i < this.thematisms.length; i++) {
				var thematism = this.thematisms[i];
				if (name === thematism.layer.get("name")) {
					thematicLayers.push(thematism.thematicLayer);
				}
			}
			return thematicLayers;
		},
		getLayerByName : function(name) {
			for (var i = 0; i < this.thematisms.length; i++) {
				var thematism = this.thematisms[i];
				if (name === thematism.thematicLayer.layer.name) {
					return thematism.thematicLayer.layer;
				}
			}
			return null;
		},
		refreshFeatures : function(layerName, features) {
			var thematicLayers = this.getThematicLayersBySourceName(layerName);
			for (var i = 0; i < thematicLayers.length; i++) {
				thematicLayers[i].refreshFeatures(features);
			}
		},
		addThematism : function(thematism) {
			var thematicLayer = Ext.create('CMDBuild.view.management.classes.map.thematism.ThematicLayer', thematism,
					this.interactionDocument);
			thematism.thematicLayer = thematicLayer;
			this.thematisms.push(thematism);
			this.interactionDocument.changed();
		}
	});
})();
