(function() {
	Ext.define('CMDBuild.view.management.classes.map.geoextension.InteractionDocument', {
		observers : [],
		featuresObserver : [],
		editLayer : undefined,
		feature : undefined,
		currentCard : undefined,
		classesControlledByNavigation : undefined,
		/**
		 * @property {Object}
		 * 
		 */
		navigables : {},

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

		setClassesControlledByNavigation : function(classes) {
			this.classesControlledByNavigation = classes;
		},

		isControlledByNavigation : function(className) {
			if (className === "_Geoserver") {
				return true;
			}
			return (!this.classesControlledByNavigation) ? false : this.classesControlledByNavigation
					.indexOf(className) != -1;
		},

		/**
		 * @param {Array}
		 *            arrayNavigables Ext.data.TreeModel
		 */
		setNavigables : function(arrayNavigables) {
			this.navigables = {};
			for (var i = 0; i < arrayNavigables.length; i++) {
				var navigable = arrayNavigables[i];
				var cardId = navigable.get("cardId");
				var className = navigable.get("className");
				if (!this.navigables[className]) {
					this.navigables[className] = [];
				}
				this.navigables[className].push(parseInt(cardId));
			}
			this.changed();
		},
		isANavigableClass : function(className) {
			return this.navigables[className];
		},
		isANavigableCard : function(card) {
			if (! this.isControlledByNavigation(card.className)) {
				return true;
			}
			var id = parseInt(card.cardId);
			return (!this.navigables[card.className]) ? false : this.navigables[card.className].indexOf(id) != -1;
		},
		isANavigableLayer : function(layer) {
			if (!this.isControlledByNavigation(layer.masterTableName)) {
				return true;
			}
			if (layer.cardBinding.length > 0) {
				for (var i = 0; i < layer.cardBinding.length; i++) {
					var binding = layer.cardBinding[i];
					var card = {
						cardId : binding.idCard,
						className : binding.className
					};
					if (this.isANavigableClass(binding.className) && this.isANavigableCard(card)) {
						return true;
					}
				}
				return false;
			} else if (!this.isANavigableClass(layer.masterTableName)) {
				return false;
			}
			return true;
		},
		getConfigurationMap : function() {
			return this.configurationMap;
		},
		getFieldStrategies : function(callback, callbackScope) {
			this.thematicDocument.getFieldStrategies(function(strategies) {
				callback.apply(callbackScope, [ strategies ]);
			}, this);
		},
		getFunctionStrategies : function(callback, callbackScope) {
			this.thematicDocument.getFunctionStrategies(function(strategies) {
				callback.apply(callbackScope, [ strategies ]);
			}, this);
		},
		getStrategyByDescription : function(description) {
			return this.thematicDocument.getStrategyByDescription(description);
		},
		setThematicDocument : function(thematicDocument) {
			this.thematicDocument = thematicDocument;
		},
		getAllLayers : function(callback, callbackScope) {
			_CMCache.getAllLayers(function(layers) {
				callback.apply(callbackScope, [ layers ]);
			});
		},
		getAllThematicLayers : function() {
			if (!this.thematicDocument) {
				return [];
			}
			return this.thematicDocument.getAllLayers();
		},
		getThematicDocument : function() {
			return this.thematicDocument;
		},
		getThematicLayers : function() {
			if (!this.thematicDocument) {
				return [];
			}
			return this.thematicDocument.getLayers();
		},
		forceRefreshThematism : function() {
			this.thematicDocument.forceRefreshThematism();
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
			this.getMapPanel().selectCard(this.getCurrentCard());
		},
		changedFeature : function() {
			for (var i = 0; i < this.featuresObserver.length; i++) {
				this.featuresObserver[i].refreshCurrentFeature();
			}
		},
		getLayerVisibility : function(layer) {
			return !(layer.unChecked === true);
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
				callback.apply(callbackScope, [ undefined ])
				return;
			}
			var geoLayer = this.getGeoLayerByName(layers[index].name);
			if (geoLayer && geoLayer.get("adapter") && geoLayer.get("adapter").getPosition) {
				geoLayer.get("adapter").getPosition(card, function(center) {
					if (center) {
						callback.apply(callbackScope, [ center ])
					} else {
						this.centerOnLayer(card, layers, index + 1, callback, callbackScope)
					}
				}, this);
			}
			else {
				this.centerOnLayer(card, layers, index + 1, callback, callbackScope)
				
			}
		},
		centerOnCard : function(card, callback, callbackScope) {
			var map = this.getMap();
			var me = this;
			this.getLayersForCard(card, function(layers) {
				me.centerOnLayer(card, layers, 0, function(center) {
					if (center) {
						me.configurationMap.center = center;
						// me.changed();
					}
					callback.apply(callbackScope, []);
				}, this);
			}, this);
		},
		removeAllGisLayers : function() {
			var me = this;
			var map = this.getMap();
			_CMCache.getAllLayers(function(layers) {
				for (var i = 0; i < layers.length; i++) {
					var layer = layers[i];
					var geoLayer = me.getGeoLayerByName(layer.name);
					if (geoLayer) {
						var debug = map.removeLayer(geoLayer);
					}
				}
			}, this);
		},
		setCurrentCard : function(card) {
			if (this.currentCard && card.className !== this.currentCard.className) {
				this.removeAllGisLayers();
			}
			this.currentCard = card;
			this.thematicDocument.setCurrentCard(card);
		},
		getCurrentCard : function() {
			return this.currentCard;
		},
		clearSelection : function() {
			var mapPanel = this.getMapPanel();
			mapPanel.clearSelection();
		},
		getFeaturesOnLayerByCardId : function(cardId, layer) {
			var source = layer.getSource();
			var features = (source) ? source.getFeatures() : new ol.Collection();
			var retFeatures = new ol.Collection();
			features.forEach(function(feature) {
				// always == on ids
				if (feature.get("master_card") == cardId) {
					retFeatures.push(feature);
				}
			});
			return retFeatures;
		},
		getLayersForCard : function(card, callback, callbackScope) {
			_CMCache.getAllLayers(function(layers) {
				var retLayers = [];
				for (var i = 0; i < layers.length; i++) {
					var layer = layers[i];
					if (layer.masterTableName === card.className) {
						retLayers.push(layer);
					}
				}
				callback.apply(callbackScope, [ retLayers ]);
			});
		},
		getCurrentFeature : function() {
			return this.feature;
		},
		getGeoLayerByName : function(name) {
			var map = this.getMap();
			var currentCard = this.getCurrentCard();

			return (!map) ? null : geoLayerByName(name, map, currentCard);
		},
		getLayerByName : function(name, callback, callbackScope) {
			layerByName(name, callback, callbackScope);
		},
		getLayerByClassAndName : function(className, name, callback, callbackScope) {
			layerByClassAndName(className, name, callback, callbackScope);
		},
		getMap : function() {
			var map = this.configurationMap.mapPanel.getMap();
			return map;
		},
		getMapPanel : function() {
			var mapPanel = this.configurationMap.mapPanel;
			return mapPanel;
		},
		getThematicLayerByName : function(name) {
			if (!this.thematicDocument) {
				return null;
			}
			return this.thematicDocument.getLayerByName(name);
		},
		isVisible : function(layer, currentClassName, currentCardId) {
			return isVisible(layer, currentClassName, currentCardId);
		},
		setCurrentFeature : function(name, geoType, operation) {
			this.feature = {
				nameAttribute : name,
				geoType : geoType,
				operation : operation
			};
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

	function geoLayerByName(name, map, currentCard) {
		var retLayer = null;
		var layers = map.getLayers();
		layers.forEach(function(layer) {
			var geoAttribute = layer.get("geoAttribute");
			if (geoAttribute && name === layer.get("name") && geoAttribute.masterTableName === currentCard.className) {
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
	function layerByClassAndName(className, name, callback, callbackScope) {
		function checkName(layer) {
			return (layer.name === name && layer.masterTableName === className);
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
