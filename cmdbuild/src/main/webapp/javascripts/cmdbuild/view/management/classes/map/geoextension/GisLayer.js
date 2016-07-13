(function() {
	Ext
			.define(
					'CMDBuild.view.management.classes.map.geoextension.GisLayer',
					{
						status : undefined,

						constructor : function(classId, geoAttribute,
								withEditWindow, interactionDocument) {
							var options = {
								geoAttribute : geoAttribute,
								targetClassName : "Building"
							};
							this.interactionDocument = interactionDocument;
							var map = this.interactionDocument.getMap();
							this.layer = this.buildGisLayer(options, map);
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
						buildGisLayer : function(options, map) {
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
															loadFeatures(data,
																	vectorSource);
														}
													});
										},

										strategy : ol.loadingstrategy.bbox
									});
							var view = new ol.View({
								projection : "EPSG:900913"
							});

							var styleFunction = function(feature) {
								return styles[feature.getGeometry().getType()];
							};
							this.createControls(map, vectorSource);
							var gisLayer = new ol.layer.Vector({
								name : options.geoAttribute.name,
								source : vectorSource,
								view : view,
								style : styleFunction,
								geoAttribute : options.geoAttribute,
								adapter : this,
								CM_EditLayer : true,
								CM_Layer : true
							});
							this.interactionDocument.setCurrentFeature(
									options.geoAttribute.name, "", "Select");
							this.interactionDocument.changedFeature();
							return gisLayer;
						},
						refreshCurrentFeature : function() {
							var feature = this.interactionDocument.getCurrentFeature();
							var nameLayer = this.getLayer().get("name");
							var nameAttribute = feature.nameAttribute;
							if (nameAttribute !== nameLayer) {
								this.setStatus("Select")
								return;
							}
							if (! feature.operation === "Select") {
								var pippo = 1;
							}
							switch (feature.operation) {
							case "Modify":
								var selected = this.selectFeaturesByCardId(_CMCardModuleState.card.raw.Id);
								if (selected.length > 0) {
									this.setStatus("Modify")
									break;
									
								}
								//no break because Draw if and only if is new
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
											return feature.get("master_card") == _CMCardModuleState.card.raw.Id
													|| this.status !== "Modify";
										},
										wrapX : false
									});
							this.modify = new ol.interaction.Modify({
								features : this.select.getFeatures()
							});
							this.draw = new ol.interaction.Draw({
								source : vectorSource,
								type : ('Point')
							});
							this.draw.on('drawend', function(event) {
								var newFeature = event.feature;
								newFeature.set("master_card", _CMCardModuleState.card.raw.Id);
								newFeature.set("master_class", _CMCardModuleState.card.raw.IdClass);
								newFeature.set("master_className", _CMCardModuleState.card.raw.className);
								me.layer.getSource().addFeature(newFeature);
								me.interactionDocument.setCurrentFeature(
										me.layer.get("name"), "", "Modify");
								me.interactionDocument.changedFeature();
							});
							this.select.on('select', function(event) {
								if (me.status !== "Select") {
									return true;
								}
								var selectedId = event.selected[0]
										.get("master_card");
								var currentId = _CMCardModuleState.card.raw.Id;
								if (selectedId == currentId) {
									return true;
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
							map.addInteraction(this.draw);
							this.draw.setActive(false);
							this.select.setActive(true);
							this.modify.setActive(false);
						},
						setStatus : function(status) {
							this.draw.setActive(status === "Draw");
							this.select.setActive(status === "Select" || status === "Modify");
							this.modify.setActive(status === "Modify");
							if (! status === "Select") {
								var pippo = 1;
							}
							this.status = status;
						},
						selectFeaturesByCardId : function(cardId) {
							var retFeatures = [];
							var features = this.select.getFeatures();// ById(cardId);
							features.clear();// -> removes the selected items
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
						clearFeatures : function() {
							var features = this.select.getFeatures();// ById(cardId);
							features.clear();// -> removes the selected items
						},
						refresh : function(cardId) {
							this.selectFeaturesByCardId(cardId);
							// this.setStatus("Select");
						}
					});
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
	function loadFeatures(data, vectorSource) {
		var geoJSONFormat = new ol.format.GeoJSON();
		if (data.features.length) {
			for (var i = 0; i < data.features.length; i++) {
				data.features[i].geometry.type = changeType(data.features[i].geometry.type);
			}
			try {
				var features = geoJSONFormat.readFeatures(data);
				vectorSource.addFeatures(features);

			} catch (e) {
				console.log("data ", data);
			}
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
	var styles = {
		'Point' : new ol.style.Style({
			image : image
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
