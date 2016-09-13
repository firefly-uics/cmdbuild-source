(function() {
	Ext.define('CMDBuild.view.management.classes.map.thematism.ThematicDocument', {
		thematisms4Classes : [],
		thematisms : [],

		/**
		 * 
		 * @property {CMDBuild.view.management.classes.map.thematism.ThematicStrategiesManager}
		 * 
		 */
		strategiesManager : undefined,

		/**
		 * 
		 * @property {CMDBuild.core.buttons.gis.Thematism}
		 * 
		 */
		thematismButton : undefined,

		/**
		 * 
		 * @property {CMDBuild.view.management.classes.map.geoextension.InteractionDocument}
		 * 
		 */
		interactionDocument : undefined,

		/**
		 * 
		 * @property {CMDBuild.view.management.classes.map.thematism.ThematicColors}
		 * 
		 */
		thematicColors : undefined,

		/**
		 * 
		 * @property {String}
		 * 
		 */
		currentClassName : undefined,

		/**
		 * 
		 * @returns {Void}
		 */
		addThematism : function(thematism, bModify) {
			var thematicLayer = Ext.create('CMDBuild.view.management.classes.map.thematism.ThematicLayer', thematism,
					this.interactionDocument);
			if (!(bModify === true)) {
				this.thematismButton.add([ thematism.name ]);
			}
			thematism.thematicLayer = thematicLayer;
			this.thematisms.push(thematism);
			this.interactionDocument.changed();
		},

		/**
		 * @param
		 * {CMDBuild.view.management.classes.map.thematism.ThematicStrategiesManager}
		 * strategiesManager
		 * 
		 * @returns {Void}
		 */
		configureStrategiesManager : function(strategiesManager) {
			this.strategiesManager = strategiesManager;
		},

		/**
		 * 
		 * @returns
		 * {CMDBuild.view.management.classes.map.thematism.ThematicStrategiesManager}
		 * strategiesManager
		 * 
		 */
		getStrategiesManager : function() {
			return this.strategiesManager;
		},

		/**
		 * 
		 * @returns {Void}
		 */
		forceRefreshThematism : function() {
			for (var i = 0; i < this.thematisms.length; i++) {
				var thematism = this.thematisms[i];
				thematism.thematicLayer.setDirty();
			}
		},
		getAllLayers : function() {
			var layers = [];
			var map = this.interactionDocument.getMap();
			var mapLayers = map.getLayers();
			mapLayers.forEach(function(layer) {
				var geoAttribute = layer.get("geoAttribute");
				if (layer.masterTableName === "_Thematism") {
					layers.push(layer);
				}
			});
			return layers;
		},
		getColor : function(value, colorsTable, index) {
			return this.thematicColors.getColor(value, colorsTable, index);
		},
		getDefaultThematismConfiguration : function() {
			return clone(defaultConfiguration);
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
		getLayerByName : function(name) {
			for (var i = 0; i < this.thematisms.length; i++) {
				var thematism = this.thematisms[i];
				if (name === thematism.thematicLayer.layer.name) {
					return thematism.thematicLayer.layer;
				}
			}
			return null;
		},

		/**
		 * 
		 * @returns {Array} ol.Layer
		 * 
		 */
		getLayers : function() {
			var layers = [];
			for (var i = 0; i < this.thematisms.length; i++) {
				var thematism = this.thematisms[i];
				layers.push(thematism.thematicLayer.layer);
			}
			return layers;
		},

		/**
		 * @param {String}
		 *            description
		 * 
		 * @returns {Object} strategy
		 */
		getStrategyByDescription : function(description) {
			return this.strategiesManager.getStrategyByDescription(description);
		},

		/**
		 * @param {String}
		 *            name
		 * 
		 * @returns {Array} ol.Layer
		 */
		getThematicLayersBySourceName : function(name) {
			var thematicLayers = [];
			for (var i = 0; i < this.thematisms.length; i++) {
				var thematism = this.thematisms[i];
				if (thematism.layer && name === thematism.layer.get("name")) {
					thematicLayers.push(thematism.thematicLayer);
				}
			}
			return thematicLayers;
		},

		/**
		 * @param
		 * {CMDBuild.view.management.classes.map.geoextension.InteractionDocument}
		 * interactionDocument
		 * @param {Object}
		 *            thematicColors
		 * @returns {Void}
		 */
		init : function(interactionDocument, thematicColors) {
			this.interactionDocument = interactionDocument;
			this.thematicColors = thematicColors;
		},

		/**
		 * 
		 * @returns {Void}
		 */
		modifyThematism : function(thematism) {
			this.removeThematism(thematism);
			this.addThematism(thematism, true);
		},

		/**
		 * 
		 * @returns {Void}
		 */
		refreshFeatures : function(layerName, features) {
			var thematicLayers = this.getThematicLayersBySourceName(layerName);
			for (var i = 0; i < thematicLayers.length; i++) {
				thematicLayers[i].refreshFeatures(features);
			}
		},

		/**
		 * 
		 * @returns {Void}
		 */
		removeAllThematicLayers : function() {
			var map = this.interactionDocument.getMap();
			var layers = map.getLayers();
			layers.forEach(function(layer) {
				var geoAttribute = layer.get("geoAttribute");
				if (layer.masterTable === "_Thematism"
						|| (geoAttribute && geoAttribute.masterTableName === "_Thematism")) {
					var debug = map.removeLayer(layer);
				}
			});
		},

		/**
		 * 
		 * @returns {Void}
		 */
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

		/**
		 * 
		 * @returns {Void}
		 */
		recover : function(thematisms4Class) {
			for (var i = 0; i < thematisms4Class.length; i++) {
				this.addThematism(thematisms4Class[i], true);
			}
		},

		/**
		 * 
		 * @returns {Void}
		 */
		refreshLayerButton : function() {
			this.thematismButton.removeAll();
			var names = [];
			for (var i = 0; i < this.thematisms.length; i++) {
				var thematism = this.thematisms[i];
				names.push(thematism.name);
			}
			this.thematismButton.add(names);
		},

		/**
		 * @param {Object}
		 *            card
		 * @param {Integer}
		 *            card.cardId
		 * @param {String}
		 *            card.className
		 * 
		 * @returns {Void}
		 */
		setCurrentCard : function(card) {
			if (this.currentClassName && card.className !== this.currentClassName) {
				this.removeAllThematicLayers();
				this.thematisms4Classes[this.currentClassName] = this.thematisms;
				this.thematisms = [];
				if (this.thematisms4Classes[card.className]) {
					this.recover(this.thematisms4Classes[card.className]);
				}
			}
			this.refreshLayerButton();
			this.currentClassName = card.className;
		},

		/**
		 * @param {CMDBuild.core.buttons.gis.Thematism}
		 *            thematismButton
		 * @returns {Void}
		 */
		setThematismButton : function(thematismButton) {
			this.thematismButton = thematismButton;
		},
		groupData : function(field, strategy, sourceType, cardsArray, attributeName) {
			return groupData(field, strategy, sourceType, cardsArray, attributeName);
		},
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
	function groupData(field, strategy, sourceType, cardsArray, attributeName) {
		var groups = {};
		for (var i = 0; i < cardsArray.length; i++) {
			var card = cardsArray[i];
			chargeCardByCard(field, strategy, sourceType, groups, card, attributeName);
		}
		return groups;
	}
	function chargeCardByCard(field, strategy, sourceType, groups, card, attributeName) {
		var params = {
			card : card,
			strategy : strategy,
			attributeName : attributeName
		}
		strategy.value(params, function(value) {
			if (sourceType === CMDBuild.gis.constants.layers.FUNCTION_SOURCE) {
				value = value[field];
			}
			if (groups[value]) {
				// can be different from cards count?
				groups[value].count++;
				groups[value].cards.push(card);
			} else {
				groups[value] = {
					count : 1,
					cards : [ card ]
				};
			}
		}, this);
	}

})();
