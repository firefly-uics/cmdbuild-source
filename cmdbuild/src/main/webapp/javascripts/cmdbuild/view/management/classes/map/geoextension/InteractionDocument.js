(function() {
	Ext.define('CMDBuild.view.management.classes.map.geoextension.InteractionDocument', {
		observers : [],
		featuresObserver : [],
		editLayer : undefined,
		feature : undefined,
		currentCard : undefined,
		configurationMap : {
			center : [ CMDBuild.configuration.gis.get(CMDBuild.gis.constants.CENTER_LONGITUDE) || 0,
					CMDBuild.configuration.gis.get(CMDBuild.gis.constants.CENTER_LATITUDE) || 0 ],
			zoom : CMDBuild.configuration.gis.get(CMDBuild.gis.constants.ZOOM_INITIAL_LEVEL) || 0,
			mapDivId : CMDBuild.gis.constants.MAP_DIV || 0
		},
		constructor : function(thematicDocument) {
			this.thematicDocument = thematicDocument;
			
			this.callParent(arguments);
		},
		setConfigurationMap : function(mapPanel) {
			this.configurationMap.mapPanel = mapPanel;

		},
		getFieldStrategies : function(callback, callbackScope) {
			this.thematicDocument.getFieldStrategies(function(strategies) {
				callback.apply(callbackScope, [strategies]);
			}, this);
		},
		getFunctionStrategies : function(callback, callbackScope) {
			this.thematicDocument.getFunctionStrategies(function(strategies) {
				callback.apply(callbackScope, [strategies]);
			}, this);
		},
		getConfigurationMap : function() {
			return this.configurationMap;
		},
		setThematicDocument : function(thematicDocument) {
			this.thematicDocument = thematicDocument;
		},
		getThematicDocument : function() {
			return this.thematicDocument;
		},
		getThematicLayers : function() {
			if (! this.thematicDocument) {
				return [];
			}
			return this.thematicDocument.getLayers();
		},
		observe : function(view) {
			if (this.observers.indexOf(view) === -1) {
				this.observers.push(view);
			}
		},
		changed : function() {
			for (var i = 0; i < this.observers.length; i++) {
				this.observers[i].refresh();
			}
		},
		observeFeatures : function(view) {
			if (this.featuresObserver.indexOf(view) === -1) {
				this.featuresObserver.push(view);
			}
		},
		onLoadedfeatures : function(layerName, features) {
			if (this.thematicDocument) {
				this.thematicDocument.refreshFeatures(layerName, features);
			}
		},
		changedFeature : function() {
			for (var i = 0; i < this.featuresObserver.length; i++) {
				this.featuresObserver[i].refreshCurrentFeature();
			}
		},
		setLayerVisibility : function(layer, checked) {
			if (layer) {
				layer.unChecked = !checked;
			}
		},
		isGeoServerLayer : function(layer) {
			return layer.type === "SHAPE";
		},
		centerOnLayer : function(card, layers, index, callback, callbackScope) {
			var map = this.getMap();
			if (index >= layers.length) {
				callback.apply(callbackScope, [undefined])
				return;
			}
			var geoLayer = this.getGeoLayerByName(layers[index].name);
			if (geoLayer && geoLayer.get("adapter") && geoLayer.get("adapter").getPosition) {
				geoLayer.get("adapter").getPosition(card, function(center) {
					if (center) {
						callback.apply(callbackScope, [center])
					}
					else {
						this.centerOnLayer(card, layers, index + 1, callback, callbackScope)
					}
				}, this);
			}
		},
		centerOnCard : function(card) {
			var map = this.getMap();
			var me = this;
			this.getLayersForCard(card, function(layers) {
				me.centerOnLayer(card, layers, 0, function(center) {
					if (center) {
						me.configurationMap.center = center;
						me.changed();
					}
				}, this);
			}, this);
		},
		setCurrentCard : function(card) {
			this.currentCard = card;
		},
		getCurrentCard : function() {
			return this.currentCard;
		},
		clearSelection : function() {
			var mapPanel = this.getMapPanel();
			mapPanel.clearSelection();
		},
		getLayersForCard : function(card, callback, callbackScope) {
			_CMCache.getAllLayers(function(layers) {
				var retLayers = [];
				for (var i = 0; i < layers.length; i++) {
					var layer = layers[i];
					if (layer.masterTableName === card.className)  {
						retLayers.push(layer);
					}
				}
				callback.apply(callbackScope, [ retLayers ]);
			});
		},
		getAllLayers : function(callback, callbackScope) {
			_CMCache.getAllLayers(function(layers) {
				callback.apply(callbackScope, [ layers ]);
			});
		},
		getLayerByName : function(name, callback, callbackScope) {
			layerByName(name, callback, callbackScope);
		},
		getThematicLayerByName : function(name) {
			if (! this.thematicDocument) {
				return null;
			}
			return this.thematicDocument.getLayerByName(name);
		},
		getGeoLayerByName : function(name) {
			var map = this.getMap();
			return (! map) ? null : geoLayerByName(name, map);
		},
		isVisible : function(layer, currentClassName, currentCardId) {
			return isVisible(layer, currentClassName, currentCardId);
		},
		isHide : function(layer) {
			return (layer.unChecked === true);
		},
		getMap : function() {
			var map = this.configurationMap.mapPanel.getMap();
			return map;
		},
		getMapPanel : function() {
			var mapPanel = this.configurationMap.mapPanel;
			return mapPanel;
		},
		setCurrentFeature : function(name, geoType, operation) {
			this.feature = {
				nameAttribute : name,
				geoType : geoType,
				operation : operation
			};
		},
		getCurrentFeature : function() {
			return this.feature;
		}
	});

	function layersByCard(card, callback, callbackScope) {// ?????????
		var retLayers = [];
		_CMCache.getAllLayers(function(layers) {
			for (var i = 0; i < layers.length; i++) {
				if (layer === card.cardId) {

				}
			}
			callback.apply(callbackScope, [ retLayers ]);
		});
	}

	function geoLayerByName(name, map) {
		var retLayer = null;
		var layers = map.getLayers();
		layers.forEach(function(layer) {
			if (name === layer.get("name")) {
				retLayer = layer;
			}
		});
		return retLayer;
	}

	function layerByName(name, callback, callbackScope) {
		function checkName(layer) {
			return (layer.name === name);
		}
		_CMCache.getAllLayers(function(layers) {
			var layer = layers.find(checkName);
			callback.apply(callbackScope, [ layer ]);
		});
	}

	function isVisible(layer, currentClassName, currentCardId) {
		function checkClass(visibility) {
			return (currentClassName === visibility);
		}
		function checkCard(binding) {
			// because an id can be a string or an integer have to be ==
			return (binding.idCard == currentCardId && binding.className === currentClassName);
		}
		if (layer.visibility.find(checkClass) !== undefined) {
			return true;
		}
		if (layer.cardBinding.find(checkCard) !== undefined) {
			return true;
		}
		return false;
	}
})();
