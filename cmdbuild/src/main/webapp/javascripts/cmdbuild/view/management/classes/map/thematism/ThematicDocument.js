(function() {
	Ext.define('CMDBuild.view.management.classes.map.thematism.ThematicDocument', {
		thematisms : [],
		strategiesManager : undefined,

		/**
		 * CMDBuild.core.buttons.gis.Thematism
		 */
		thematismButton : undefined,

		setInteractionDocument : function(interactionDocument) {
			this.interactionDocument = interactionDocument;
		},
		setThematismButton : function(thematismButton) {
			this.thematismButton = thematismButton;
		},
		configureStrategiesManager : function(strategiesManager) {
			this.strategiesManager = strategiesManager
		},
		getStrategyByDescription : function(description) {
			return this.strategiesManager.getStrategyByDescription(description);
		},
		getFieldStrategies : function(callback, callbackScope) {
			this.strategiesManager.getFieldStrategies(function(strategies) {
				callback.apply(callbackScope, [ strategies ]);
			}, this);
		},
		getFunctionStrategies : function(callback, callbackScope) {
			this.strategiesManager.getFunctionStrategies(function(strategies) {
				callback.apply(callbackScope, [ strategies ]);
			}, this);
		},
		getDefaultThematismConfiguration : function() {
			return clone(defaultConfiguration);
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
		removeThematism : function(thematism) {
			var index = -1;
			for (var i = 0; i < this.thematisms.length; i++) {
				if (thematism.name === this.thematisms[i].name) {
					index = i;
					break;
				}
			}
			var map = this.interactionDocument.getMap();
			map.removeLayer(this.thematisms[i].thematicLayer.layer);
			if (index !== -1) {
				this.thematisms.splice(index, 1);
			}
		},
		modifyThematism : function(thematism) {
			this.removeThematism(thematism);
			this.addThematism(thematism, true);
		},
		addThematism : function(thematism, bModify) {
			var thematicLayer = Ext.create('CMDBuild.view.management.classes.map.thematism.ThematicLayer', thematism,
					this.interactionDocument);
			if (!(bModify === true)) {
				this.thematismButton.add([ thematism.name ]);
			}
			thematism.thematicLayer = thematicLayer;
			this.thematisms.push(thematism);
			this.interactionDocument.changed();
		}
	});

	var defaultConfiguration = {
		name : "",
		layer : "",
		strategy : "",
		configuration : {
			thematismConfiguration : {},
			functionConfiguration : {},
			layoutConfiguration : {},
		}
	};
	function clone(obj) {
		return JSON.parse(JSON.stringify(obj));
	}

})();
