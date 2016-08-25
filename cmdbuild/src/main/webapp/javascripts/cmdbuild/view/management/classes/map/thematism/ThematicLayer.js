(function() {
	Ext.define('CMDBuild.view.management.classes.map.thematism.ThematicLayer', {
		
		/**
		 * @property {Object} 
		 * @property {String} thematism.name 
		 * @property {ol.layer.Vector} thematism.layer 
		 * @property {Object} thematism.strategy 
		 * @property {String} thematism.strategy.description
		 * @property {Function} thematism.strategy.value 
		 * @property {Object} thematism.configuration (form fields CMDBuild.view.management.classes.map.thematism.ThematismMainWindow) 
		 * @property {Object} thematism.configuration.thematismConfiguration 
		 * @property {Object} thematism.configuration.functionConfiguration 
		 * @property {Object} thematism.configuration.layoutConfiguration 
		 */
		thematism : undefined,
		
		interactionDocument : undefined,

		/**
		 * @param {Object}
		 *            thematism
		 * @param {String}
		 *            thematism.name
		 * @param {ol.layer.Vector}
		 *            thematism.layer
		 * 
		 * @param
		 * {CMDBuild.view.management.classes.map.geoextension.InteractionDocument}
		 * interactionDocument
		 * 
		 * @returns {Object}
		 *          CMDBuild.view.management.classes.map.thematism.ThematicLayer
		 */
		constructor : function(thematism, interactionDocument) {
			this.callParent(arguments);
			this.thematism = thematism;
			this.interactionDocument = interactionDocument;
			this.layer = this.buildThematicLayer(this.thematism.name);
			this.layer.set("name", this.thematism.name);
			this.layer.name = this.thematism.name;
			this.charge(this.thematism.layer);
			this.interactionDocument.changed();
		},

		/**
		 * 
		 * @returns {ol.Layer}
		 */
		getLayer : function() {
			return this.layer;
		},

		/**
		 * 
		 * @returns {ol.Source}
		 */
		getSource : function() {
			return this.layer.getSource();
		},

		/**
		 * 
		 * @returns {Void}
		 */
		refreshFeatures : function() {
			this.charge(this.thematism.layer, this.thematism.strategy);
		},

		/**
		 * 
		 * @returns {Void}
		 */
		refresh : function() {
			this.layer.getSource().clear();
			this.refreshFeatures();
		},

		/**
		 * @param {String}
		 *            attributeName
		 * @param {ol.Map}
		 *            map
		 * 
		 * @returns {Void}
		 */
		buildThematicLayer : function(attributeName) {
			var me = this;
			var vectorSource = new ol.source.Vector({
				strategy : ol.loadingstrategy.bbox
			});
			var view = new ol.View({
				projection : "EPSG:900913"
			});

			var thematicLayer = new ol.layer.Vector({
				name : attributeName,
				source : vectorSource,
				view : view,
				adapter : this
			});
			thematicLayer.masterTableName = "_Thematism";

			return thematicLayer;
		},

		/**
		 * @param {ol.layer.Vector}
		 *            originalLayer
		 * @param {Thematic
		 *            Strategy} strategy
		 * 
		 * @returns {Void}
		 */
		charge : function(originalLayer, strategy) {
			var me = this;
			var visibles = [];
			originalLayer.getSource().forEachFeature(function(feature) {
				var cardId = feature.get("master_card");
				var justHereFeatures = me.getFeaturesByCardId(cardId, this.layer);
				visibles.push(cardId);
				if (justHereFeatures.getLength() === 0) {
					me.newFeature({
						master_card : feature.get("master_card"),
						master_className : feature.get("master_className"),
						master_class : feature.get("master_class"),
						geometry : feature.clone().getGeometry(),
						strategy : strategy
					});
				}
				else {
					
				}
			});
			this.layer.getSource().forEachFeature(function(feature) {
				var cardId = feature.get("master_card");
				if (visibles.indexOf(cardId) === -1) {
					me.layer.getSource().removeFeature(feature);
				}
			});
		},

		/**
		 * @param {ol.Feature}
		 *            originalFeature
		 * 
		 * @returns {Void}
		 */
		newFeature : function(originalFeature) {
			var feature = new ol.Feature({
				master_card : originalFeature.master_card,
				master_className : originalFeature.master_className,
				master_class : originalFeature.master_class,
			});
			feature.setGeometry(originalFeature.geometry);

			this.layer.getSource().addFeature(feature);
			this.loadCard(originalFeature.master_card, originalFeature.master_className, function(card) {
				this.getThematicColor(card, function(color) {
					var style = this.getStyle(feature.getGeometry().getType(), color);
					feature.setStyle(style);
				}, this);
			}, this);
		},

		/**
		 * @param {Integer}
		 *            cardId
		 * 
		 * @returns {Array[ol.feature]} features
		 */
		getFeaturesByCardId : function(cardId) {
			return this.interactionDocument.getFeaturesOnLayerByCardId(cardId, this.layer);
		},

		/**
		 * @param {Object}
		 *            card : complete card from server
		 * 
		 * @returns {Rgb} color
		 * 
		 */
		getThematicColor : function(card, callback, callbackScope) {
			var strategy = this.thematism.strategy
			var parameters = {
				card : card,
				strategy : strategy
			};
			strategy.value(parameters, function(value) {
				var configuration = this.thematism.configuration;
				if (configuration.thematismConfiguration.source === CMDBuild.gis.constants.layers.FUNCTION_SOURCE) {
					var field = configuration.layoutConfiguration.resultFieldName;
					value = value[field];
				}
				var thematicDocument = this.interactionDocument.getThematicDocument();
				var color = thematicDocument.getColor(value, configuration.layoutConfiguration.colorsTable);
				callback.apply(callbackScope, [ color ]);
			}, this);
		},

		/**
		 * @param {Integer}
		 *            id : card id
		 * @param {String}
		 *            className
		 * 
		 * @returns {Object} card
		 * 
		 */
		loadCard : function(id, className, callback, callbackScope) {
			if (!params) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CARD_ID] = id;
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = className;
			}

			CMDBuild.proxy.Card.read({
				params : params,
				loadMask : false,
				success : function(result, options, decodedResult) {
					var data = decodedResult.card;
					callback.apply(callbackScope, [ data ]);
				}
			});
		},
		getConfiguration : function() {
			return this.thematism.configuration;
		},
		getStyle : function(shape, color) {
			var configuration = this.getConfiguration().layoutConfiguration;
			switch (shape) {
			case 'LineString':
				return new ol.style.Style({
					stroke : new ol.style.Stroke({
						color : 'green',
						width : 1
					})
				});
			case 'Polygon':
				return new ol.style.Style({
					stroke : new ol.style.Stroke({
						color : 'blue',
						lineDash : [ 4 ],
						width : 3
					}),
					fill : new ol.style.Fill({
						color : color
					})
				});
			case 'Point':
			case 'Circle':
			default:
				return new ol.style.Style({
					fill : new ol.style.Fill({
						color : 'rgba(255, 100, 50, 0.3)'
					}),
					stroke : new ol.style.Stroke({
						width : 2,
						color : 'rgba(255, 100, 50, 0.8)'
					}),
					image : new ol.style.Circle({
						fill : new ol.style.Fill({
							color : color
						}),
						stroke : new ol.style.Stroke({
							width : 1,
							color : color
						}),
						radius : parseInt(configuration.firstValue)
					}),
				});
			}
		}
	});

})();
