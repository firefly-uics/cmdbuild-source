(function() {
	Ext
			.define(
					'CMDBuild.view.management.classes.map.geoextension.GisLayer',
					{
						status : "Select",

						constructor : function(classId, geoAttribute,
								withEditWindow, interactionDocument) {
							var options = {
								geoAttribute : geoAttribute,
								targetClassName : "Building"
							};
							this.interactionDocument = interactionDocument;
							var map = this.interactionDocument.getMap();
							this.layer = this.buildGisLayer(geoAttribute.name,
									options, map);
							this.layer.set("name", geoAttribute.name);
							this.interactionDocument.observeFeatures(this);
							this.callParent(arguments);
						},
						getLayer : function() {
							return this.layer;
						},
						getSource : function() {
							return this.layer.getSource();
						},
						getFeaturesByCardId : function(cardId) {
							var source = this.getSource();
							var features = (source) ? source.getFeatures() : [];
							var retFeatures = [];
							for (var i = 0; i < features.length; i++) {
								// always == on ids
								if (features[i].get("master_card") == cardId) {
									retFeatures.push(features[i]);
								}
							}
							return retFeatures;
						},
						buildGisLayer : function(attributeName, options, map) {
							var me = this;
							var geoJSONFormat = new ol.format.GeoJSON();
							var vectorSource = new ol.source.Vector(
									{
										loader : function(extent, resolution,
												projection) {
											$
													.ajax({
														url : getGeoUrl(),
														data : {
															className : options.targetClassName,
															attribute : options.geoAttribute.name,
															bbox : extent
																	.join(",")
														},
														type : "post",
														success : function(data) {
															vectorSource
																	.getFeatures()
																	.forEach(
																			function(
																					feature) {
																				vectorSource
																						.removeFeature(feature);
																			});
															for (var i = 0; i < data.features.length; i++) {
																data.features[i].geometry.type = changeType(data.features[i].geometry.type);
															}
															var features = geoJSONFormat
																	.readFeatures(data);
															vectorSource
																	.addFeatures(features);
														},

													});
										},

										strategy : ol.loadingstrategy.bbox
									});
							var view = new ol.View({
								projection : "EPSG:900913"
							});

							var styleFunction = function(feature) {
								console.log("styleFunction", styles[feature.getGeometry().getType()]);
								return styles[feature.getGeometry().getType()];
							};
							this.createControls(map, vectorSource);
							var gisLayer = new ol.layer.Vector({
								name : options.geoAttribute.name,
								source : vectorSource,
								view : view,
								style : styleFunction,
								geoAttribute : options.geoAttribute,
								adapter : this
							});
							CMDBuild.proxy.gis.Gis
							.getFeature({
								params : {
									"className" : "Buiding",
									"cardId" : -1 //taking icons. there is one for all cards in a class
								},
								loadMask : false,
								scope : this,
								success : function(param) {
									console.log("----->", param);
								}
							});
							this.interactionDocument.setCurrentFeature(
									options.geoAttribute.name, "", "Select");
							this.interactionDocument.changedFeature();
							return gisLayer;
						},
						refreshCurrentFeature : function() {
							var feature = this.interactionDocument
									.getCurrentFeature();
							var nameLayer = this.getLayer().get("name");
							var nameAttribute = feature.nameAttribute;
							if (nameAttribute && nameAttribute !== nameLayer) {
								this.setStatus("None");
								this.clearFeatures();
								return;
							}
							switch (feature.operation) {
							case "Modify":
								var selected = this
										.selectFeaturesByCardId(_CMCardModuleState.card.raw.Id);
								if (selected.length > 0) {
									this.setStatus("Modify")
									break;

								}
								// no break because enters in Draw if and only
								// if is new
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
						createControls : function(map, vectorSource) {
							var me = this;
							this.select = new ol.interaction.Select(
									{
										filter : function(feature, layer) {
											if (!layer) {
												return false;
											}
											var nameLayer = layer.get("name");
											var thisName = me.getLayer().get(
													"name");
											return (me.status === "Select")
											return (feature.get("master_card") == _CMCardModuleState.card.raw.Id && nameLayer === thisName)
													|| (me.status !== "Modify" && me.status !== "None");
										},
										wrapX : false
									});
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
								var selectedId = event.selected[0]
										.get("master_card");
								var currentId = _CMCardModuleState.card.raw.Id;
								if (selectedId == currentId) {
									return false;
								}
								var card = {
									Id : event.selected[0].get("master_card"),
									IdClass : event.selected[0]
											.get("master_class")
								};
								CMDBuild.global.controller.MainViewport.cmfg(
										'mainViewportCardSelect', card);
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
						newFeature : function(feature) {
							feature.set("master_card",
									_CMCardModuleState.card.raw.Id);
							feature.set("master_class",
									_CMCardModuleState.card.raw.IdClass);
							feature.set("master_className",
									_CMCardModuleState.card.raw.className);
							this.layer.getSource().addFeature(feature);
							this.interactionDocument.setCurrentFeature(
									this.layer.get("name"), "", "Modify");
							this.interactionDocument.changedFeature();
						},
						getPosition : function(card) {
							var extent = this.layer.getSource().getExtent();
							var x = extent[0] + (extent[2] - extent[0]) / 2;
							var y = extent[1] + (extent[3] - extent[1]) / 2;
							return [ x, y ];
						},
						setStatus : function(status) {
							var map = this.interactionDocument.getMap();
							var feature = this.interactionDocument
									.getCurrentFeature();
							var geoType = feature.geoType;
							this.drawPoint.setActive(status === "Draw"
									&& geoType === "POINT");
							this.drawPolygon.setActive(status === "Draw"
									&& geoType === "POLYGON");
							this.drawLine.setActive(status === "Draw"
									&& geoType === "LINESTRING");
							this.select.setActive(status === "Select");
							this.modify.setActive(status === "Modify");
							this.status = status;
						},
						selectFeaturesByCardId : function(cardId) {
							var retFeatures = [];
							var features = this.select.getFeatures();
							features.clear();
							var featuresOnLayer = this
									.getFeaturesByCardId(cardId);
							for (var i = 0; i < featuresOnLayer.length; i++) {
								var feature = featuresOnLayer[i];
								if (feature.get("master_card") == cardId) {
									features.push(feature);
									retFeatures.push(feature);

								}
							}
							return retFeatures;
						},
						featureOnThisLayer : function() {
							var feature = this.interactionDocument
									.getCurrentFeature();
							var nameAttribute = feature.nameAttribute;
							var nameLayer = this.getLayer().get("name");
							return nameAttribute === nameLayer;
						},
						getGeometries : function(cardId, className) {
							var featuresOnLayer = this
									.getFeaturesByCardId(cardId);
							for (var i = 0; i < featuresOnLayer.length; i++) {
								var feature = featuresOnLayer[i];
								var geojson = new ol.format.GeoJSON();
								var json = geojson.writeFeature(feature);
								var translation = translate2CMDBuild(feature);
								return translation;// only one attribute on
								// this layer for this card
							}
							return undefined;
						},
						clearFeatures : function() {
							var features = this.select.getFeatures();
							features.clear();// -> removes the selected items
						},
						refresh : function(cardId) {
							this.selectFeaturesByCardId(cardId);
						}
					});
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
	function getGeoUrl() {
		return CMDBuild.proxy.index.Json.gis.getGeoCardList
				+ '?'
				+ CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY
				+ '='
				+ Ext.util.Cookies
						.get(CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY); // FIXME:
	}
	var image = new ol.style.Circle({
		radius : 100,
		fill : null,
		stroke : new ol.style.Stroke({
			color : 'red',
			width : 1
		})
	});
    var imageb = new ol.style.Icon({
           src: 'upload/images/gis/puf.png'
       });
	var styles = {
		'Point' : new ol.style.Style({
			image : imageb
		}),
		'LineString' : new ol.style.Style({
			stroke : new ol.style.Stroke({
				color : 'green',
				width : 1
			})
		}),
		'Polygon' : new ol.style.Style({
			stroke : new ol.style.Stroke({
				color : 'blue',
				lineDash : [ 4 ],
				width : 3
			}),
			fill : new ol.style.Fill({
				color : 'rgba(0, 0, 255, 0.1)'
			})
		}),
		'Circle' : new ol.style.Style({
			stroke : new ol.style.Stroke({
				color : 'red',
				width : 2
			}),
			fill : new ol.style.Fill({
				color : 'rgba(255,0,0,0.2)'
			})
		})
	};
})();
