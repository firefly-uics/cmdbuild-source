(function() {
	Ext
			.define(
					'CMDBuild.Management.CMMap',
					{
						extend : 'CMDBuild.view.management.classes.map.geoextension.Map',
						requires : [ 'CMDBuild.view.management.classes.map.geoextension.Map' ],

						interactionDocument : undefined,

						iconCache : {},

						/**
						 * @returns {Void}
						 * 
						 * @override
						 */
						configure : function() {
							this.interactionDocument.setConfigurationMap(this);
							this.interactionDocument.observe(this);
							this.current = this.makePointSelect();
							this.current.setActive(false);
						},

						/**
						 * @returns {Void}
						 * 
						 * @override
						 */
						refresh : function() {
							// when the refresh is called more times it takes
							// the last
							var me = this;
							if (this.interactionDocument.getEditing()) {
								return;
							}
							if (this.operation) {
								clearTimeout(this.operation);
							}
							this.operation = setTimeout(function() {
								me._refresh();
								me.operation = undefined;
							}, 200);
						},
						/**
						 * @returns {Void}
						 * 
						 * @override
						 */
						_refresh : function() {
							var card = this.interactionDocument.getCurrentCard();
							if (!card) {
								return;
							}
							var currentClassName = card.className;
							var currentCardId = card.cardId;
							this.interactionDocument.prepareNavigables();
							this.interactionDocument.getAllLayers(function(layers) {
								this.completeStyle(layers, 0, function() {
									this.clearSelections();
									this.refreshAllLayers(layers, currentClassName, currentCardId);
									this.refreshThematicLayers(currentClassName, currentCardId);
									this.selectCard({
										cardId : currentCardId,
										className : currentClassName
									});
									this.showCurrent(card);
									this.refreshLegend();

								}, this);
							}, this);
						},

						completeSingleLayerStyle : function(layer, callback, callbackScope) {
							var style = Ext.decode(layer.style);
							var geoAttribute = {
								description : layer.description,
								masterTableName : layer.masterTableName,
								name : layer.name,
								type : layer.type,
								style : style,
								iconSize : layer.iconSize
							};
							layer.geoAttribute = geoAttribute;
							if (!style.externalGraphic) {
								geoAttribute.iconSize = [ 0, 0 ];
								callback.apply(callbackScope, []);
								return;
							}
							if (this.iconCache[style.externalGraphic]) {
								var iconSize = this.iconCache[style.externalGraphic];
								geoAttribute.iconSize = iconSize;
								callback.apply(callbackScope, []);
								return;
							}
							var http = new XMLHttpRequest();
							http.open('HEAD', style.externalGraphic, false);
							http.send();
							var me = this;
							if (http.status === 404) {
								geoAttribute.iconSize = [ 0, 0 ];
								me.iconCache[style.externalGraphic] = geoAttribute.iconSize;
								callback.apply(callbackScope, []);
								return;

							}
							var img = new Image;
							img.onload = function() {
								geoAttribute.iconSize = [ this.width, this.height ];
								me.iconCache[style.externalGraphic] = geoAttribute.iconSize;
								callback.apply(callbackScope, []);
							}
							img.src = style.externalGraphic;

						},
						completeStyle : function(layers, index, callback, callbackScope) {
							if (index >= layers.length) {
								callback.apply(callbackScope, []);
								return;
							}
							var layer = layers[index++];
							this.completeSingleLayerStyle(layer, function() {
								this.completeStyle(layers, index, callback, callbackScope);
							}, this);
						},

						/**
						 * @param {String}
						 *            currentClassName
						 * @param {Integer}
						 *            currentCardId
						 * 
						 * @returns {Void}
						 */
						refreshThematicLayers : function(currentClassName, currentCardId) {
							var thematicLayers = this.interactionDocument.getThematicLayers();
							var visibleLayers = [];
							for (var i = 0; i < thematicLayers.length; i++) {
								var layer = thematicLayers[i];
								var mapThematicLayer = this.getLayerByClassAndName(layer.masterTableName, layer.name);
								var hide = !this.interactionDocument.getLayerVisibility(layer);
								if (hide) {
									continue;
								}
								if (!mapThematicLayer) {
									this.map.addLayer(layer);
									mapThematicLayer = layer;
								}
								mapThematicLayer.setZIndex(CMDBuild.gis.constants.layers.THEMATIC_MIN_ZINDEX + i);
								var adapter = mapThematicLayer.get("adapter");
								if (adapter && adapter.refresh) {
									var thematism = adapter.getThematism();
									var originalLayer = thematism.configuration.originalLayer;
									var className = originalLayer.className;
									var name = originalLayer.name;
									thematism.layer = this.getLayerByClassAndName(className, name);
									adapter.refresh({
										cardId : currentCardId,
										className : currentClassName
									});
								}
								visibleLayers.push(layer);
							}
							this.removeThematicsNotVisibleLayers(visibleLayers);
						},

						/**
						 * @param {Array} :
						 *            layer layers from _CMCACHE
						 * @param {Array} :
						 *            layer.visibility
						 * @param {Array} :
						 *            layer.cardBinding
						 * @param {Integer} :
						 *            layer.maxZoom
						 * @param {Integer} :
						 *            layer.minZoom
						 * @param {String} :
						 *            layer.name
						 * @param {String} :
						 *            layer.description
						 * @param {String} :
						 *            layer.masterTableName // className
						 * 
						 * @param {String}
						 *            currentClassName
						 * @param {Integer}
						 *            currentCardId
						 * 
						 * @returns {Void}
						 */
						refreshAllLayers : function(layers, currentClassName, currentCardId) {
							var visibleLayers = [];
							for (var i = 0; i < layers.length; i++) {
								var layer = layers[i];
								var zoom = this.interactionDocument.getZoom();
								if (zoom < layer.minZoom || zoom > layer.maxZoom) {
									continue;
								}
								var visible = this.interactionDocument
										.isVisible(layer, currentClassName, currentCardId);
								var hide = !this.interactionDocument.getLayerVisibility(layer);
								var navigable = this.interactionDocument.isANavigableLayer(layer);
								if ((hide || !navigable) && visible) {
									this.clearHideLayer(layer.masterTableName, layer.name);
								} else if (visible && navigable) {
									this.showLayer(layer, currentClassName, currentCardId);
									visibleLayers.push(layer);
								}
							}
							this.removeNotVisibleLayers(layers, visibleLayers);
						},
						selectCard : function(card) {
							var geoLayers = this.getLayers();
							geoLayers.forEach(function(geoLayer) {
								var adapter = geoLayer.get("adapter");
								if (adapter && adapter.selectCard) {
									adapter.selectCard(card);
								}
							});
						},
						showLayer : function(layer, currentClassName, currentCardId) {
							var geoLayer = this.getLayerByClassAndName(layer.masterTableName, layer.name);
							if (!geoLayer) {
								geoLayer = this.makeLayer(layer.geoAttribute, true);
							}
							var index = CMDBuild.gis.constants.layers.GIS_MIN_ZINDEX + layer.index;
							if (layer.geoAttribute.type === "SHAPE") {
								index = CMDBuild.gis.constants.layers.GEO_MIN_ZINDEX;
							}
							geoLayer.setZIndex(index);
							var adapter = geoLayer.get("adapter");
							if (adapter && adapter.refresh) {
								adapter.refresh();
							}

						},
						clearHideLayer : function(className, nameLayer) {
							var geoLayer = this.getLayerByClassAndName(className, nameLayer);
							if (geoLayer) {
								var adapter = geoLayer.get("adapter");
								if (adapter && adapter.clearAllFeatures) {
									adapter.clearAllFeatures();
								}
								this.map.removeLayer(geoLayer);
							}
						},
						setSelections : function() {
							var card = this.interactionDocument.getCurrentCard();
							this.map
									.getLayers()
									.forEach(
											function(layer) {
												var adapter = layer.get("adapter");
												if (adapter && adapter.setStatus) {
													var geoAttribute = layer.get("geoAttribute");
													adapter
															.setStatus((geoAttribute && geoAttribute.masterTableName === card.className) ? "Select"
																	: "None");
												}
											});

						},
						clearSelections : function() {
							var gisLayers = this.interactionDocument.getGisAdapters();
							for ( var key in gisLayers) {
								var namedAdapters = gisLayers[key];
								for (var i = 0; i < namedAdapters.length; i++) {
									namedAdapters[i].adapter.refresh();
								}
							}

						},
						/**
						 * @param {Array} :
						 *            allLayers : layers from _CMCACHE
						 * @param {Array}
						 *            visibles : layers from _CMCACHE
						 * 
						 * @returns {Void}
						 */
						removeNotVisibleLayers : function(allLayers, visibles) {
							var allLayers = [];
							var mapLayers = this.map.getLayers();
							mapLayers.forEach(function(mapLayer) {
								allLayers.push(mapLayer);
							});

							var me = this;
							var index = 0;
							for (var i = 0; i < allLayers.length; i++) {
								var mapLayer = allLayers[i];
								var geoAttribute = mapLayer.get("geoAttribute");
								if (geoAttribute) {
									me.remove4GeoAttribute(mapLayer, geoAttribute, visibles);
								}
							}
						},

						/**
						 * @param {Object}
						 *            geoAttribute
						 * @param {Array}
						 *            visibles : layers from _CMCACHE
						 * 
						 * @returns {Void}
						 */
						remove4GeoAttribute : function(mapLayer, geoAttribute, visibles) {
							var mapLayerName = geoAttribute.name;
							var mapClassName = geoAttribute.masterTableName;
							function compare(layer) {
								return (layer.name === mapLayerName && layer.masterTableName === mapClassName);

							}
							if (!visibles.find(compare)) {
								var adapter = mapLayer.get("adapter");
								if (adapter) {
									adapter.refresh();

								}
								this.clearHideLayer(mapClassName, mapLayerName);
								this.map.removeLayer(mapLayer);
							}
						},

						/**
						 * @param {Array}
						 *            visibles : layers from _CMCACHE
						 * 
						 * @returns {Void}
						 */
						removeThematicsNotVisibleLayers : function(visibles) {
							var allLayers = this.interactionDocument.getAllThematicLayers();
							var me = this;
							allLayers.forEach(function(layer) {
								if (!isVisible(visibles, layer)) {
									me.removeLayerByName(layer.masterTableName, layer.name);

								}
							});
						},
						showCurrent : function(card) {
							if (this.interactionDocument.isCardOnMap(card)) {
								var features = this.current.getFeatures();
								features.clear();
							} else {
								this.interactionDocument.getPosition(card, function(center) {
									if (!center) {
										return;
									}
									var thing = new ol.Feature({
										geometry : new ol.geom.Point(center)
									});
									var features = this.current.getFeatures();
									features.clear();
									this.map.addInteraction(this.current);
									features.push(thing);

								}, this);
							}
						},
						makePointSelect : function() {
							var selectPoints = new ol.style.Style({
								image : new ol.style.Circle({
									fill : new ol.style.Fill({
										color : 'orange'
									}),
									stroke : new ol.style.Stroke({
										color : 'yellow'
									}),
									radius : CMDBuild.gis.constants.layers.DEFAULT_RADIUS
								})
							});
							var me = this;
							return new ol.interaction.Select({
								style : [ selectPoints ],
								wrapX : false
							})
						},
					});
	function isVisible(visibles, layer) {
		for (var i = 0; i < visibles.length; i++) {
			if (visibles[i].name === layer.name) {
				return true;
			}
		}
		return false;
	}

})();
