(function() {
	var DEFAULT_RADIUS = 20;
	Ext.define('CMDBuild.view.management.classes.map.thematism.ThematicLayer', {

		/**
		 * @property {Object}
		 * @property {String} thematism.name
		 * @property {ol.layer.Vector} thematism.layer
		 * @property {Object} thematism.strategy
		 * @property {String} thematism.strategy.description
		 * @property {Function} thematism.strategy.value
		 * @property {Object} thematism.configuration (form fields
		 *           CMDBuild.view.management.classes.map.thematism.ThematismMainWindow)
		 * @property {Object} thematism.configuration.thematismConfiguration
		 * @property {Object} thematism.configuration.functionConfiguration
		 * @property {Object} thematism.configuration.layoutConfiguration
		 */
		thematism : undefined,

		/**
		 * @property {boolean} beDirty (a thematic layer is dirty when a feature
		 *           is modified)
		 * 
		 * 
		 */
		beDirty : undefined,

		/**
		 * @property {CMDBuild.view.management.classes.map.geoextension.InteractionDocument}
		 *           interactionDocument
		 * 
		 * 
		 */
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
		 * @returns {ol.Source}
		 */
		getThematism : function() {
			return this.thematism;
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
			if (this.beDirty === true) {
				this.layer.getSource().clear();
				this.beDirty = false;
			}
			this.refreshFeatures();
		},

		/**
		 * 
		 * @returns {Void}
		 */
		setDirty : function() {
			this.beDirty = true;
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
			if (originalLayer) {
				originalLayer.getSource().forEachFeature(function(feature) {
					var cardId = feature.get("master_card");
					var justHereFeatures = me.getFeaturesByCardId(cardId);
					visibles.push(cardId);
					if (justHereFeatures.getLength() === 0) {
						me.newFeature({
							master_card : feature.get("master_card"),
							master_className : feature.get("master_className"),
							master_class : feature.get("master_class"),
							geometry : feature.clone().getGeometry(),
							strategy : strategy
						});
					} else {

					}
				});
			}
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

			this.loadCard(originalFeature.master_card, originalFeature.master_className, function(rowColor, card) {
				var style = this.getStyle(feature.getGeometry().getType(), rowColor.color);
				feature.setStyle(style);
				this.layer.getSource().addFeature(feature);
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
		 * @param {Integer}
		 *            id : card id
		 * @param {String}
		 *            className
		 * 
		 * @returns {Object} card
		 * 
		 */
		loadCard : function(id, className, callback, callbackScope) {
			var colorsTable = this.thematism.configuration.layoutConfiguration.colorsTable
			for (var i = 0; i < colorsTable.length; i++) {
				for (var j = 0; j < colorsTable[i].cards.length; j++) {
					if (id === colorsTable[i].cards[j].Id) {// Id! no constants
															// for me
						callback.apply(callbackScope, [ colorsTable[i], colorsTable[i].cards[j] ]);
						return;
					}
				}
			}
			callback.apply(callbackScope, []);

		},

		/**
		 * 
		 * @returns {Thematic Table}
		 * 
		 */
		getColorsTable : function() {
			var colorsTable = this.thematism.configuration.layoutConfiguration.colorsTable
			return colorsTable;
		},

		/**
		 * 
		 * @returns {Object} configuration
		 * @property {Object} configuration.thematismConfiguration
		 * @property {Object} configuration.functionConfiguration
		 * @property {Object} configuration.layoutConfiguration
		 * 
		 */
		getConfiguration : function() {
			return this.thematism.configuration;
		},

		/**
		 * @param {String}
		 *            shape
		 * @param {String}
		 *            color
		 * 
		 * @returns {ol.style.Style}
		 * 
		 */
		getStyle : function(shape, color) {
			var configuration = this.getConfiguration().layoutConfiguration;
			switch (shape) {
			case 'LineString':
				return line(color);
			case 'Polygon':
				return polygon(color);
			case 'Point':
			case 'Circle':
			default:
				var radius = (configuration.firstValue) ? parseInt(configuration.firstValue) : DEFAULT_RADIUS;
				return point(color, radius);
			}
		}
	});

	function line(color) {
		var line = new ol.style.Style({
			stroke : new ol.style.Stroke({
				color : 'green',
				width : 1
			})
		});
		return line;
	}
	function polygon(color) {
		var polygon = new ol.style.Style({
			stroke : new ol.style.Stroke({
				color : 'blue',
				width : 3
			}),
			fill : new ol.style.Fill({
				color : color
			})
		});
		return polygon;
	}
	function point(color, radius) {
		var point = new ol.style.Style({
			fill : new ol.style.Fill({
				color : 'rgba(255, 100, 50, 0.3)'
			}),
			stroke : new ol.style.Stroke({
				width : 2,
				color : 'rgba(255, 100, 50, 0.8)'
			}),
			image : new ol.style.Circle({
				fill : new ol.style.Fill({
					color : color,
					 opacity: 0.5,
				}),
				stroke : new ol.style.Stroke({
					width : 1,
					color : color,
					 opacity: 0.5,
				}),
				radius : radius

			}),
		});
		return point;
	}

})();
