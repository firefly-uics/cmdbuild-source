(function() {
	Ext.require([ 'CMDBuild.core.constants.Proxy', 'CMDBuild.proxy.gis.Icon' ]);
	Ext.define('CMDBuild.view.management.classes.map.geoextension.GisLayer', {

		/**
		 * @property {ol.style.Icon}
		 * 
		 */
		classBitmap : undefined,

		/**
		 * 
		 * @property {String}
		 * 
		 */
		attributeType : undefined,

		/**
		 * 
		 * @property {String}
		 * 
		 */
		status : undefined,

		/**
		 * @param {String}
		 *            attributeName
		 * @param {Object}
		 *            options
		 * @param {ol.Map}
		 *            map
		 * 
		 * @returns {ol.layer.Vector}
		 * 
		 */
		buildGisLayer : function(attributeName, options, map) {
			var me = this;
			this.attributeType = options.geoAttribute.type;
			var vectorSource = this.getVectorSource(options);
			var view = new ol.View({
				projection : "EPSG:900913"
			});

			var styleFunction = function(feature) {
				return me.getStyle(feature.getGeometry().getType());
			};
			this.createControls(map, vectorSource);
			var gisLayer = new ol.layer.Vector({
				name : options.geoAttribute.name,
				source : vectorSource,
				view : view,
				// style : styleFunction,
				geoAttribute : options.geoAttribute,
				adapter : this
			});
			gisLayer.setStyle(styleFunction);

			CMDBuild.proxy.gis.Gis.getFeature({
				params : {
					"className" : options.targetClassName,
					"cardId" : -1
				// taking icons. there is one for all cards in a
				// class
				},
				loadMask : false,
				scope : this,
				success : function(param) {
				}
			});
			this.status = "Select";
			this.interactionDocument.setCurrentFeature(options.geoAttribute.name, "", "Select");
			this.interactionDocument.changedFeature();
			return gisLayer;
		},

		constructor : function(geoAttribute, withEditWindow, interactionDocument) {
			var options = {
				geoAttribute : geoAttribute,
				targetClassName : geoAttribute.masterTableName,
				iconUrl : geoAttribute.iconUrl
			};
			this.classBitmap = this.loadIcon(options.iconUrl);
			this.interactionDocument = interactionDocument;
			var map = this.interactionDocument.getMap();
			this.layer = this.buildGisLayer(geoAttribute.name, options, map);
			this.layer.set("name", geoAttribute.name);
			this.interactionDocument.observeFeatures(this);
			this.callParent(arguments);
		},
		createControls : function(map, vectorSource) {
			var me = this;
			this.makeSelect();
			this.modify = new ol.interaction.Modify({
				features : this.select.getFeatures()
			});
			this.drawPoint = new ol.interaction.Draw({
				source : vectorSource,
				type : ('Point')
			});
			this.drawPolygon = new ol.interaction.Draw({
				source : vectorSource,
				type : ('Polygon')
			});
			this.drawLine = new ol.interaction.Draw({
				source : vectorSource,
				type : ('LineString')
			});
			this.drawPoint.on('drawend', function(event) {
				me.newFeature(event.feature);
			});
			this.drawPolygon.on('drawend', function(event) {
				me.newFeature(event.feature);
			});
			this.drawLine.on('drawend', function(event) {
				me.newFeature(event.feature);
			});
			this.select.on('select', function(event) {
				if (me.status !== "Select") {
					return false;
				}
				if (event.selected.length === 0) {
					return false;
				}
				me.clearSelections();
				var selectedId = event.selected[0].get("master_card");
				var currentId = (_CMCardModuleState.card) ? _CMCardModuleState.card.get("Id") : -1;
				if (selectedId == currentId) {
					return false;
				}
				var card = {
					Id : event.selected[0].get("master_card"),
					IdClass : event.selected[0].get("master_class")
				};
				CMDBuild.global.controller.MainViewport.cmfg('mainViewportCardSelect', card);
			});
			map.addInteraction(this.select);
			map.addInteraction(this.modify);
			map.addInteraction(this.drawPoint);
			map.addInteraction(this.drawLine);
			map.addInteraction(this.drawPolygon);
			this.drawPoint.setActive(false);
			this.drawLine.setActive(false);
			this.drawPolygon.setActive(false);
			this.select.setActive(true);
			this.modify.setActive(false);
		},

		/**
		 * 
		 * @returns {String}
		 * 
		 */
		getAttributeType : function() {
			return this.attributeType;
		},

		/**
		 * 
		 * @returns {Array} ids
		 * 
		 */
		getCardsOnLayer : function() {
			var source = this.getSource();
			var features = (source) ? source.getFeatures() : new ol.Collection();
			var cards = [];
			features.forEach(function(feature) {
				cards.push(feature.get("master_card"));
			});
			return cards;
		},

		/**
		 * @param {Integer}
		 *            cardId
		 * 
		 * @returns {Array[ol.Feature]} features
		 * 
		 */
		getFeaturesByCardId : function(cardId) {
			return this.interactionDocument.getFeaturesOnLayerByCardId(cardId, this.layer);
		},

		/**
		 * 
		 * @returns {ol.layer.Vector}
		 * 
		 */
		getLayer : function() {
			return this.layer;
		},

		/**
		 * 
		 * @returns {ol.Source}
		 * 
		 */
		getSource : function() {
			return this.layer.getSource();
		},
		onlyToAddFeatures : function(visibleFeatures) {
			var features = [];
			var layerFeatures = this.layer.getSource().getFeatures();
			for (var i = 0; i < visibleFeatures.length; i++) {
				var visibleFeature = visibleFeatures[i];
				if (!inLayerFeatures(visibleFeature, layerFeatures)) {
					features.push(visibleFeature);
				}
			}
			return features;

		},
		removeNotVisibleFeatures : function(visibleFeatures) {
			var layerFeatures = this.layer.getSource().getFeatures();
			for (var i = 0; i < layerFeatures.length; i++) {
				var layerFeature = layerFeatures[i];
				if (!inVisibleFeatures(layerFeature, visibleFeatures)) {
					this.layer.getSource().removeFeature(layerFeature);
				}
			}
		},
		getVectorSource : function(options) {
			var me = this;
			var geoJSONFormat = new ol.format.GeoJSON();
			var vectorSource = new ol.source.Vector({
				loader : function(extent, resolution, projection) {
					$.ajax({
						url : getGeoUrl(),
						data : {
							className : options.targetClassName,
							attribute : options.geoAttribute.name,
							bbox : extent.join(",")
						},
						type : "post",
						success : function(data) {
							// clearVectorSource(vectorSource);
							var visibleFeatures = me.onlyVisibleFeatures(data.features);
							data.features = me.onlyToAddFeatures(visibleFeatures);
							var jsonFeatures = geoJSONFormat.readFeatures(data);
							vectorSource.addFeatures(jsonFeatures);
							me.interactionDocument.onLoadedfeatures(me.layer.get("name"), jsonFeatures);
							me.removeNotVisibleFeatures(visibleFeatures);
						},

					});
				},

				strategy : ol.loadingstrategy.bbox
			});
			return vectorSource;
		},

		loadIcon : function(url) {
			var icon = undefined;
			try {
				icon = new ol.style.Icon({
					src : url
				});

			} catch (e) {
				icon = new ol.style.Icon({
					src : "upload/images/gis/Edificio.png"
				});
			}
			return icon;
		},

		onlyVisibleFeatures : function(featuresOnLayer) {
			var features = [];
			var geoAttribute = this.layer.get("geoAttribute");
			var bControlledByNavigation = this.interactionDocument
					.isControlledByNavigation(geoAttribute.masterTableName);
			for (var i = 0; i < featuresOnLayer.length; i++) {
				var feature = featuresOnLayer[i];
				feature.geometry.type = changeType(feature.geometry.type);
				if (!bControlledByNavigation) {
					features.push(feature);

				} else {
					var card = {
						className : feature.properties.master_className,
						cardId : feature.properties.master_card,
					};
					if (this.interactionDocument.isANavigableCard(card)) {
						features.push(feature);
					}

				}
			}
			return features;
		},

		/**
		 * 
		 * @returns {Void}
		 * 
		 */
		refresh : function() {
			this.getSource().clear();
//			var featuresOnLayer = this.getSource().getFeatures();
//			var features = [];
//			for (var i = 0; i < featuresOnLayer.length; i++) {
//				var feature = featuresOnLayer[i];
//				var card = {
//					className : feature.get("master_className"),
//					cardId : feature.get("master_card"),
//				};
//				if (this.interactionDocument.isANavigableCard(card)) {
//					features.push(feature);
//				}
//
//			}
//			for (var i = 0; i < featuresOnLayer.length; i++) {
//				var feature = featuresOnLayer[i];
//				if (searchFeature(feature, features) === -1) {
//					this.layer.getSource().removeFeature(feature);
//				}
//			}
		},

		/**
		 * 
		 * @param {String}
		 *            status
		 * 
		 */
		setStatus : function(status) {
			var map = this.interactionDocument.getMap();
			var feature = this.interactionDocument.getCurrentFeature();
			var geoType = feature.geoType;
			this.drawPoint.setActive(status === "Draw" && geoType === "POINT");
			this.drawPolygon.setActive(status === "Draw" && geoType === "POLYGON");
			this.drawLine.setActive(status === "Draw" && geoType === "LINESTRING");
			this.select.setActive(status === "Select");
			this.modify.setActive(status === "Modify");
			this.status = status;
		},

		/**
		 * 
		 * @returns {Void}
		 * 
		 */
		refreshCurrentFeature : function() {
			var feature = this.interactionDocument.getCurrentFeature();
			var nameLayer = this.getLayer().get("name");
			var nameAttribute = feature.nameAttribute;
			if (nameAttribute && nameAttribute !== nameLayer) {
				this.setStatus("None");
				this.clearSelections();
				return;
			}
			switch (feature.operation) {
			case "Modify":
				var card = this.interactionDocument.getCurrentCard();
				var selected = this.selectFeaturesByCardId(card);
				if (selected.length > 0) {
					this.setStatus("Modify")
					break;

				}
				// no break because enters in Draw if and only if is new
			case "Draw":
				this.setStatus("Draw")
				break;
			case "Select":
				this.setStatus("Select")
				break;
			default:
				;
			}
		},

		/**
		 * 
		 * @returns {Void}
		 * 
		 */
		newFeature : function(feature) {
			var currentCard = this.interactionDocument.getCurrentCard();
			feature.set("master_card", currentCard.cardId);
			feature.set("master_className", currentCard.className);
			var cl = _CMCache.getEntryTypeByName(currentCard.className);
			feature.set("master_class", cl.get("id"));
			this.getSource().addFeature(feature);
			this.interactionDocument.setCurrentFeature(this.layer.get("name"), "", "Modify");
			this.interactionDocument.changedFeature();
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

		removeById : function(id) {

		},
		changeFeature : function(newId) {
			var newFeatures = [];
			var features = this.getFeaturesByCardId(-1); // can be an insert
			if (features.getLength() === 0) { // or a modify
				features = this.getFeaturesByCardId(newId);
			}
			features.forEach(function(feature) {
				newFeatures.push(feature);
			});
			features.forEach(function(feature) {
				features.remove(feature);
			});
			for (var i = 0; i < newFeatures.length; i++) {
				newFeatures[i].set("master_card", newId);
				features.push(newFeatures[i]);
			}
			this.interactionDocument.forceRefreshThematism();
			this.setStatus("Select");
		},
		makeSelect : function() {
			var me = this;
			this.select = new ol.interaction.Select({
				filter : function(feature, layer) {
					if (!layer) {
						return false;
					}
					return (me.status === "Select");
				},
				wrapX : false
			});

		},
		selectFeaturesByCardId : function(card) {
			var retFeatures = [];
			var features = this.select.getFeatures();
			try {
				features.clear();
			} catch (e) {
				console.log("Error on selectFeaturesByCardId ", card);
			}
			var featuresOnLayer = this.getFeaturesByCardId(card.cardId);
			featuresOnLayer.forEach(function(feature) {
				if (feature.get("master_card") == card.cardId) {
					features.push(feature);
					retFeatures.push(feature);
				}
			});
			return retFeatures;
		},
		featureOnThisLayer : function() {
			var feature = this.interactionDocument.getCurrentFeature();
			var nameAttribute = feature.nameAttribute;
			var nameLayer = this.getLayer().get("name");
			return nameAttribute === nameLayer;
		},
		getGeometries : function(cardId, className) {
			var featuresOnLayer = this.getFeaturesByCardId(cardId);
			var translation = undefined;
			featuresOnLayer.forEach(function(feature) { // first found is good
				// <<<----NB!!
				var geojson = new ol.format.GeoJSON();
				var json = geojson.writeFeature(feature);
				translation = translate2CMDBuild(feature);
			});
			return translation;
		},
		clearSelections : function() {
			var features = this.select.getFeatures();
			if (features) {
				features.forEach(function(feature) {
					features.remove(feature);
				});
			}
		},
		clearAllFeatures : function() {
			this.clearSelections();
			this.layer.getSource().clear();
		},
		selectCard : function(card) {
			this.selectFeaturesByCardId(card);
		},
		getStyle : function(shape) {
			switch (shape) {
			case 'Point':
				return new ol.style.Style({
					image : this.classBitmap
				});
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
						color : 'rgba(0, 0, 255, 0.1)'
					})
				});
			case 'Circle':
			default:
				return new ol.style.Style({
					stroke : new ol.style.Stroke({
						color : 'red',
						width : 2
					}),
					fill : new ol.style.Fill({
						color : 'rgba(255,0,0,0.2)'
					})
				});
			}
		}
	});
	function clearVectorSource(vectorSource) {
		try {
			var features = vectorSource.getFeatures();
			if (features) {
				features.forEach(function(feature) {
					vectorSource.removeFeature(feature);
				});
			}
		} catch (e) {
			console.log("Errore");
		}
	}
	function translate2CMDBuild(feature) {
		var geometry = feature.getGeometry();
		var str = "";
		var coordinates = geometry.getCoordinates();
		var type = geometry.getType();
		switch (type) {
		case "Point":
			str = "POINT(";
			str += coordinates[0] + " " + coordinates[1];
			str += ")";
			break;
		case "Polygon":
			coordinates = coordinates[0];
			str = "POLYGON((";
			for (var i = 0; i < coordinates.length; i++) {
				str += coordinates[i][0] + " " + coordinates[i][1];
				str += (i < coordinates.length - 1) ? "," : "";
			}
			str += "))";
			break;
		case "LineString":
			str = "LINESTRING(";
			for (var i = 0; i < coordinates.length; i++) {
				str += coordinates[i][0] + " " + coordinates[i][1];
				str += (i < coordinates.length - 1) ? "," : "";
			}
			str += ")";
			break;
		default:
			str = "not implemented";
		}
		return str;
	}
	function changeType(type) {
		switch (type) {
		case "POINT":
			return "Point";
		case "POLYGON":
			return "Polygon";
		case "LINESTRING":
			return "LineString";
		default:
			return type;
		}
	}
	function searchFeature(feature, featuresArray) {
		for (var i = 0; i < featuresArray.length; i++) {
			var className = featuresArray[i].get("master_className");
			var cardId = featuresArray[i].get("master_card");
			if (className === feature.get("master_className") && cardId === feature.get("master_card")) {
				return i;
			}
		}
		console.log(feature.get("master_className") +"&&"+ feature.get("master_card"));
		return -1;
	}
	function getGeoUrl() {
		return CMDBuild.proxy.index.Json.gis.getGeoCardList + '?'
				+ CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY + '='
				+ Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY); // FIXME:
	}
	function getCenterOfExtent(extent) {
		var x = extent[0] + (extent[2] - extent[0]) / 2;
		var y = extent[1] + (extent[3] - extent[1]) / 2;
		return [ x, y ];
	}
	function getPointCenter(geometry) {
		return geometry.coordinates;
	}
	function getPolygonCenter(geometry) {
		var minX = Number.MAX_VALUE;
		var minY = Number.MAX_VALUE;
		var maxX = Number.MIN_VALUE;
		var maxY = Number.MIN_VALUE;
		var coordinates = geometry.coordinates[0];
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
		case "POLYGON":
			return getPolygonCenter(geometry);
		case "POINT":
			return getPointCenter(geometry);
		}

	}
	function inLayerFeatures(feature, features) {
		for (var i = 0; i < features.length; i++) {
			if (features[i].get("master_card") === feature.properties.master_card) {
				return true;
			}
		}
		return false;
	}
	function inVisibleFeatures(feature, features) {
		for (var i = 0; i < features.length; i++) {
			if (feature.get("master_card") === features[i].properties.master_card) {
				return true;
			}
		}
		return false;
	}
})();
