(function() {
	Ext.define('CMDBuild.view.management.classes.map.geoextension.InteractionDocument', {
		observers : [],
		featuresObserver : [],
		editLayer : undefined,
		feature: undefined,
		configurationMap : {
			center : [CMDBuild.configuration.gis.get(CMDBuild.gis.constants.CENTER_LONGITUDE) || 0, 
			          CMDBuild.configuration.gis.get(CMDBuild.gis.constants.CENTER_LATITUDE) || 0],
			zoom : CMDBuild.configuration.gis.get(CMDBuild.gis.constants.ZOOM_INITIAL_LEVEL) || 0,
			mapDivId :  CMDBuild.gis.constants.MAP_DIV || 0
		},
		setConfigurationMap: function(mapPanel) {
			this.configurationMap.mapPanel = mapPanel;
			
		},
		getConfigurationMap: function() {
			return this.configurationMap;
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
		centerOnCard : function(card) {///????????
			this.getLayersForCard(card, function(layers) {
				if (layers.length <= 0) {
					return;
				}
				var map = this.configurationMap.mapPanel.getMap();
				me.getLayerByName(layer.name, function() {
					if (geoLayer) {
						var extent = geoLayer.source.getExtent();
						if (extent) {
							me.configurationMap.center = ol.extent.getCenter(extent);
							me.changed();
						}
					}
					
				}, this);
			}, this);
		},
		clearSelection : function() {
			var mapPanel = this.getMapPanel();
			mapPanel.clearSelection();
		},
		getLayersForCard : function(card, callback, callbackScope) {
			_CMCache.getAllLayers(function(layers) {
				callback.apply(callbackScope, [layers]);
			});
		},
		getAllLayers : function(callback, callbackScope) {
			_CMCache.getAllLayers(function(layers) {
				callback.apply(callbackScope, [layers]);
			});
		},
		getLayerByName : function(name, callback, callbackScope) {
			layerByName(name, callback, callbackScope);
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
					operation: operation
			};
		},
		getCurrentFeature : function() {
			return this.feature;
		}
	});

	function layersByCard(card, callback, callbackScope) {//?????????
		var retLayers = [];
		_CMCache.getAllLayers(function(layers) {
			for (var i = 0; i < layers.length; i++) {
				if (layer === card.cardId) {
					
				}
			}
			callback.apply(callbackScope, [retLayers]);
		});
	}

	function layerByName(name, callback, callbackScope) {
		function checkName(layer) {
			return (layer.name === name);
		}
		_CMCache.getAllLayers(function(layers) {
			var layer = layers.find(checkName);
			callback.apply(callbackScope, [layer]);
		});
	}

	function isVisible(layer, currentClassName, currentCardId) {
		function checkClass(visibility) {
			return (currentClassName === visibility);
		}
		function checkCard(binding) {
			// because an id can be a string or an integer have to be ==
			return (binding.idCard == currentCardId &&
					binding.className === currentClassName);
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
