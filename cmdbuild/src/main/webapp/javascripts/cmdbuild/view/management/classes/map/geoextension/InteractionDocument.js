(function() {
	Ext.define('CMDBuild.view.management.classes.map.geoextension.InteractionDocument', {
		observers : [],
		layerObservers : [],
		featureObservers : [],
		navigablesObservers : [],
		editLayer : undefined,
		feature : undefined,
		currentCard : undefined,
		classesControlledByNavigation : undefined,
		gisAdapters : {},
		/**
		 * @property {Boolean} wait if there is a navigation tree
		 */
		started : true,
		visible : false,

		/**
		 * @property {Object}
		 * 
		 */
		navigable : {},

		/**
		 * @property {Boolean}
		 * 
		 */
		inEditing : false,
		noZoom : false,

		configurationMap : {
			center : [ CMDBuild.configuration.gis.get(CMDBuild.gis.constants.CENTER_LONGITUDE) || 0,
					CMDBuild.configuration.gis.get(CMDBuild.gis.constants.CENTER_LATITUDE) || 0 ],
			zoom : CMDBuild.configuration.gis.get(CMDBuild.core.constants.Proxy.INITIAL_ZOOM_LEVEL) || 0,
			mapDivId : CMDBuild.gis.constants.MAP_DIV || 0
		},
		constructor : function(thematicDocument) {
			this.thematicDocument = thematicDocument;
			this.navigable = Ext.create('CMDBuild.view.management.classes.map.geoextension.Navigable', this);

			this.callParent(arguments);
		},
		setConfigurationMap : function(mapPanel) {
			this.configurationMap.mapPanel = mapPanel;

		},

		resetZoom : function() {
			this.getMapPanel().resetZoom();
		},

		setStarted : function(started) {
			this.started = started;
		},

		getStarted : function() {
			return this.started;
		},

		setVisible : function(visible) {
			this.visible = visible;
		},

		getVisible : function() {
			return this.visible;
		},

		/**
		 * @param {Array}
		 *            arrayNavigables Ext.data.TreeModel
		 */
		prepareNavigables : function() {
			this.navigable.prepareNavigables();
		},
		setNavigables : function(arrayNavigables) {
			this.navigable.setNavigables(arrayNavigables);
			this.changed();
		},
		setEditing : function(inEditing) {
			this.inEditing = inEditing;
		},
		getEditing : function(inEditing) {
			return this.inEditing;
		},
		isANavigableClass : function(className) {
			return this.navigable.isANavigableClass(className) || className === CMDBuild.gis.constants.GEOSERVER_LAYER;
		},
		getNavigableNode : function(card) {
			return this.navigable.getNavigableNode(card);
		},
		getNavigable : function(card) {
			return this.navigable.getNavigable(card);
		},
		getNavigablesToChange : function() {
			return this.navigable.getNavigablesToChange();
		},
		isANavigableCard : function(card) {
			return this.navigable.isANavigableCard(card);
		},
		isANavigableLayer : function(layer) {
			if (!this.isANavigableClass(layer.masterTableName)) {
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
		getStrategiesManager : function() {
			return this.thematicDocument.getStrategiesManager();
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
		observeLayers : function(view) {
			if (this.layerObservers.indexOf(view) === -1) {
				this.layerObservers.push(view);
			}
		},
		observeNavigables : function(view) {
			if (this.navigablesObservers.indexOf(view) === -1) {
				this.navigablesObservers.push(view);
			}
		},
		getGisAdapters : function() {
			return this.gisAdapters;
		},
		pushGisLayerAdapter : function(name, className, adapterGisLayer) {
			if (this.gisAdapters[className]) {
				var newAdapter = true;
				for (var i = 0; i < this.gisAdapters[className].length; i++) {
					var namedAdapter = this.gisAdapters[className][i];
					if (namedAdapter.name === name) {
						namedAdapter.adapter = adapterGisLayer;
						newAdapter = false;
						break;
					}
				}
			} else {
				this.gisAdapters[className] = [ {
					name : name,
					adapter : adapterGisLayer
				} ];
			}
		},
		changed : function() {
			for (var i = 0; i < this.observers.length; i++) {
				this.observers[i].refresh();
			}
		},
		changedNavigables : function() {
			for (var i = 0; i < this.navigablesObservers.length; i++) {
				this.navigablesObservers[i].refreshNavigables();
			}
		},
		changedLayers : function() {
			for (var i = 0; i < this.layerObservers.length; i++) {
				this.layerObservers[i].refreshLayers();
			}
		},
		observeFeatures : function(view) {
			if (this.featureObservers.indexOf(view) === -1) {
				this.featureObservers.push(view);
			}
		},
		onLoadedfeatures : function(layerName, features) {
			if (this.thematicDocument) {
				this.thematicDocument.refreshFeatures(layerName, features);
			}
			this.getMapPanel().selectCard(this.getCurrentCard());
		},
		changedFeature : function() {
			for (var i = 0; i < this.featureObservers.length; i++) {
				this.featureObservers[i].refreshCurrentFeature();
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
			return layer.masterTableName === CMDBuild.gis.constants.layers.GEOSERVER_LAYER;
		},
		centerOnLayer : function(card, callback, callbackScope) {
			var map = this.getMap();
			this.getPosition(card, function(center) {
				if (center) {
					callback.apply(callbackScope, [ center ])
				} else {
					callback.apply(callbackScope, [ undefined ])
				}
			}, this);
		},
		/**
		 * 
		 * @returns {Object} (x,y)
		 * 
		 */
		getPosition : function(card, callback, callbackScope) {
			var me = this;

			function onSuccess(resp, req, feature) {
				// the card could have no feature
				if (!feature || !feature.geometry || !feature.geometry.coordinates) {
					callback.apply(callbackScope, [ undefined ]);
					return;
				}
				var center = getCenter(feature.geometry);
				callback.apply(callbackScope, [ center ]);
			}
			var cardId = card.cardId;
			var className = card.className;
			CMDBuild.proxy.gis.Gis.getFeature({
				params : {
					"className" : className,
					"cardId" : cardId
				},
				loadMask : false,
				scope : this,
				success : onSuccess
			});
		},
		centerOnCard : function(card, callback, callbackScope) {
			var map = this.getMap();
			var me = this;
				var mapPanel = me.getMapPanel();
				me.centerOnLayer(card, function(center) {
					if (center) {
						me.configurationMap.center = center;
					}
					mapPanel.center(me.configurationMap);
					callback.apply(callbackScope, [ center ]);
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
			this.currentCard = card;
			this.thematicDocument.setCurrentCard(card);
		},
		getCurrentCard : function() {
			return this.currentCard;
		},
		setNoZoom : function(noZoom) {
			this.noZoom = noZoom;
		},
		getNoZoom : function() {
			return this.noZoom;
		},
		getZoom : function() {
			var mapPanel = this.getMapPanel();
			return mapPanel.getZoom();
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
			var me = this;
			function checkClass(visibility) {
				return me.sameClass(currentClassName, visibility);
			}
			function checkCard(binding) {
				// because an id can be a string or an integer have to be ==
				return (binding.idCard == currentCardId && this.sameClass(binding.className, currentClassName));
			}
			if (layer.visibility.find(checkClass) !== undefined) {
				return true;
			}
			if (layer.cardBinding.find(checkCard) !== undefined) {
				return true;
			}
			return false;
		},
		isCardOnMap : function(currentCard) {
			var layers = this.getMap().getLayers();
			var found = false;
			layers.forEach(function(layer) {
				var geoAttribute = layer.get("geoAttribute");
				if (geoAttribute && geoAttribute.masterTableName === currentCard.className) {
					found = true;
				}
			});
			return found;
		},
		setCurrentFeature : function(name, geoType, operation) {
			this.feature = {
				nameAttribute : name,
				geoType : geoType,
				operation : operation
			};
		},
		sameClass : function(classNameOne, classNameTwo) {
			return this.isDescendant(classNameOne, classNameTwo) || this.isDescendant(classNameTwo, classNameOne);
		},

		isDescendant : function(subClassName, superClassName) {
			var etSub = _CMCache.getEntryTypeByName(subClassName)
			var etSuper = _CMCache.getEntryTypeByName(superClassName)
			if (!(etSub && etSuper)) {
				return false;
			}
			var idSub = etSub.get("id");
			var idSuper = etSuper.get("id");
			var classes = _CMCache.getClasses();
			while (true) {
				if (!idSub) {
					return false;
				}
				if (idSub === idSuper) {
					return true;
				}
				var type = classes[idSub];
				idSub = type.get("parent");
			}
		}
	});

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

	function getCenterOfExtent(extent) {
		var x = extent[0] + (extent[2] - extent[0]) / 2;
		var y = extent[1] + (extent[3] - extent[1]) / 2;
		return [ x, y ];
	}
	function getPointCenter(geometry) {
		return geometry.coordinates;
	}
	function getLineCenter(geometry) {
		return getGenericPolygonCenter(geometry.coordinates);
	}
	function getPolygonCenter(geometry) {
		return getGenericPolygonCenter(geometry.coordinates[0]);
	}
	function getGenericPolygonCenter(coordinates) {
		var minX = Number.MAX_VALUE;
		var minY = Number.MAX_VALUE;
		var maxX = Number.MIN_VALUE;
		var maxY = Number.MIN_VALUE;
		for (var i = 0; i < coordinates.length; i++) {
			var coordinate = coordinates[i];
			minX = Math.min(minX, coordinate[0]);
			maxX = Math.max(maxX, coordinate[0]);
			minY = Math.min(minY, coordinate[1]);
			maxY = Math.max(maxY, coordinate[1]);
		}
		return getCenterOfExtent([ minX, minY, maxX, maxY ]);
	}
	function getCenter(geometry) {
		switch (geometry.type) {
		case "LINESTRING":
			return getLineCenter(geometry);
		case "POLYGON":
			return getPolygonCenter(geometry);
		case "POINT":
			return getPointCenter(geometry);
		}

	}

})();
