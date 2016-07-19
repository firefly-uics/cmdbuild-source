(function() {

	Ext.require([ 'CMDBuild.proxy.gis.Gis' ]);

	Ext
			.define(
					"CMDBuild.controller.management.classes.map.CMMapController",
					{
						alternateClassName : "CMDBuild.controller.management.classes.CMMapController", // Legacy
						// class
						// name
						extend : "CMDBuild.controller.management.classes.CMCardDataProvider",

						mixins : {
							observable : "Ext.util.Observable",
							mapDelegate : "CMDBuild.view.management.map.CMMapPanelDelegate",
							editingWindowDelegate : "CMDBuild.controller.management.classes.map.CMMapEditingWindowDelegate",
							cardStateDelegate : "CMDBuild.state.CMCardModuleStateDelegate"
						},

						cmfgCatchedFunctions: [
						           			'onCardZoom'
						           		],

						cardDataName : "geoAttributes", // CMCardDataProvider
														// member, to say the
						// name to use for given data

						constructor : function(mapPanel, interactionDocument) {
							var me = this;
							this.interactionDocument = interactionDocument;
							if (mapPanel) {
								this.mapPanel = mapPanel;
								this.mapPanel.addDelegate(this);

								// set me as delegate of the OpenLayers.Map
								// (pimped in CMMap)
								this.map = mapPanel.getMap();
								this.map.delegate = this;

								this.cmIsInEditing = false;

								// init the map state
								this.mapState = new CMDBuild.state.CMMapState(
										this);
								_CMMapState = this.mapState;

								var cardbrowserPanel = this.mapPanel
										.getCardBrowserPanel();
								if (cardbrowserPanel) {
									new CMDBuild.controller.management.classes.CMCardBrowserTreeDataSource(
											cardbrowserPanel, this.mapState);
									cardbrowserPanel
											.addDelegate(new CMDBuild.controller.management.classes.map.CMCardBrowserDelegate(
													this));
								}

								// initialize editing control
								this.editingWindowDelegate = new CMDBuild.controller.management.classes.map.CMMapEditingWindowDelegate(
										this, this.interactionDocument);
								this.mapPanel.editingWindow
										.addDelegate(this.editingWindowDelegate);
								this.selectControl = new CMDBuild.Management.CMSelectFeatureController(
										[],
										{
											hover : false,
											renderIntent : "default",
											eventListeners : {
												featurehighlighted : function(e) {
													me
															.onFeatureSelect(e.feature);
												}
											}
										});

								this.map.addControl(this.selectControl);
								// MINE NB this.selectControl.activate();

								// build long press controller
								// MINE NB buildLongPressController(this);

								// add me to the CMCardModuleStateDelegates
								_CMCardModuleState.addDelegate(this);

								// MINE NB this.map.events.register("zoomend",
								// this, onZoomEnd);
							} else {
								throw new Error(
										"The map controller was instantiated without a map or the related form panel");
							}
						},

						onFeatureSelect : function(feature) {
							var prop = feature.attributes, layer = feature.layer;

							if (!layer.editLayer) {
								// the feature selected is not
								// in a cmdbLayer with an associated editLayer
								return;
							}

							_CMCardModuleState.setCard({
								Id : prop.master_card,
								IdClass : prop.master_class
							});
						},

						/*
						 * card could be either a String (the id of the card) or
						 * a Ext.model.Model
						 */
						onCardSelected : function(card) {
							this.interactionDocument.changed();
						},

						onAddCardButtonClick : function() {
							this.mapPanel.getMap().clearSelection();
//							this.currentCardId = undefined;
//							this.mapPanel.getMap().refreshStrategies();
						},
						onCardZoom : function() {
							console.log("-------------------------------");
						},
						centerMapOnFeature : function(params) {
							if (params == null) {
								return;
							}

							var me = this;

							function onSuccess(resp, req, feature) {
								// the card could have no feature
								if (feature.properties) {
									me.mapPanel.getMap().centerOnGeometry(
											feature);
								} else {
									me.mapPanel.getMap().clearSelection();
								}
							}
							;

							var entryTypeId = params.IdClass;
							var entryType = _CMCache
									.getEntryTypeById(entryTypeId);
							_CMCache
									.getLayersForEntryTypeName(
											entryType.getName(),
											function(layers) {
												// if (layers.length > 0) {
												// var layer = layers[0];
												// if (me.map.getZoom() <
												// layer.minZoom ) {
												// // change the zoom to the
												// minimum to show the feature
												// me.map.setCenter(me.map.getCenter(),
												// layer.minZoom);d
												// }
												CMDBuild.proxy.gis.Gis
														.getFeature({
															params : {
																"className" : _CMCache
																		.getEntryTypeNameById(params.IdClass),
																"cardId" : params.Id
															},
															loadMask : false,
															scope : this,
															success : onSuccess
														});
												// }
											});

						},

						editMode : function() {
							this.cmIsInEditing = true;

							if (this.mapPanel.cmVisible) {
								this.mapPanel.editMode();
								this.deactivateSelectControl();
							}
						},

						displayMode : function() {
							this.cmIsInEditing = false;

							if (this.mapPanel.cmVisible) {
								this.mapPanel.displayMode();
//								this.editingWindowDelegate
//										.deactivateEditControls();
								this.activateSelectControl();
							}
						},

						onCardSaved : function(c) {
							/*
							 * Normally after the save, the main controller say
							 * to the grid to reload it, and select the new
							 * card. If the map is visible on save, this could
							 * not be done, so say to this controller to refresh
							 * the features loaded, and set the new card as
							 * selected
							 */
							if (this.mapPanel.cmVisible) {
								var me = this;

								_CMCardModuleState.setCard({
									Id : c.Id,
									IdClass : c.IdClass
								}, function(card) {
									// me.mapPanel.getMap().clearSelection();
									// me.mapPanel.getMap().refreshStrategies();
								});
							}
						},

						deactivateSelectControl : function() {
							// this.selectControl.deactivate();
						},

						activateSelectControl : function() {
							// this.selectControl.activate();
						},

						selectFeature : function(feauture) {
							// this.selectControl.select(feauture);
						},

						onEntryTypeSelected : onEntryTypeSelected,
						getCardData : getCardData,

						/* As mapDelegate ******** */

						onLayerAdded : onLayerAdded,
						onLayerRemoved : onLayerRemoved,
						onMapPanelVisibilityChanged : onVisibilityChanged,

						/* As layerSwitcherDelegate ******** */

						onLayerCheckChange : function(node, checked) {
							var map = this.mapPanel.getMap();
						},

						/* As CMCardModuleStateDelegate ************** */

						onEntryTypeDidChange : function(state, entryType,
								danglingCard) {
							this.onEntryTypeSelected(entryType, danglingCard);
						},

						onCardDidChange : function(state, card) {
							if (card) {
								this.onCardSelected(card);
							}
						},

						/* As CMMap delegate *************** */

						featureWasAdded : function(feature) {
						},

	
						// As CMDBuild.state.CMMapStateDelegate

						geoAttributeUsageChanged : function(geoAttribute) {
						},

						geoAttributeZoomValidityChanged : function(geoAttribute) {
							if (!geoAttribute.isZoomValid()) {
								removeLayerForGeoAttribute(this.map,
										geoAttribute, this);
							} else {
								addLayerForGeoAttribute(this.map, geoAttribute,
										this);
							}
						},

						featureVisibilityChanged : function(className, cardId,
								visible) {
						},

						getCurrentCardId : function() {
							return this.currentCardId;
						},

						getCurrentMap : function() {
							return this.map;
						}
					});

	function getLayerVisibility(id, bindings, visibles) {
		for (var i = 0; i < bindings.length; i++) {
			if (Ext.Array.contains(visibles, bindings[i].className)) {
				if (bindings[i].idCard == id) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Executed after zoomEvent to update mapState object and manually redraw
	 * all map's layers
	 */
	function onZoomEnd() {
		var map = this.map;
		var zoom = map.getZoom();
		this.mapState.updateForZoom(zoom);
		var baseLayers = map.cmBaseLayers;
		var haveABaseLayer = false;

		// Manually force redraw of all layers to fix a problem with GoogleMaps
		Ext.Array.each(map.layers, function(item, index, allItems) {
			item.redraw();
		});

		for (var i = 0; i < baseLayers.length; ++i) {
			var layer = baseLayers[i];

			if (!layer || typeof layer.isInZoomRange != 'function')
				continue;

			if (layer.isInZoomRange(zoom)) {
				map.setBaseLayer(layer);
				haveABaseLayer = true;

				break;
			}
		}

		if (!haveABaseLayer)
			map.setBaseLayer(map.cmFakeBaseLayer);
	}

	function buildLongPressController(me) {
		var map = me.map;
		var longPressControl = new OpenLayers.Control.LongPress(
				{
					onLongPress : function(e) {
						var lonlat = map.getLonLatFromPixel(e.xy);
						var features = map.getFeaturesInLonLat(lonlat);

						// no features no window
						if (features.length == 0) {
							return;
						}

					}
				});

		map.addControl(longPressControl);
		longPressControl.activate();
	}

	function loadCardGridStore(gridController) {
		gridController.onCardGridShow();
	}

	function updateCardGridTitle(entryType, gridController) {
		var grid = gridController.getView();
		var prefix = CMDBuild.Translation.management.modcard.title;
		grid.setTitle(prefix + entryType.get("name"));
	}
	function getCardData(params) {
		if (params.cardId === -1) {
			return "";
		}
		var cardId =  _CMCardModuleState.card.get("Id");
		var className = _CMCardModuleState.card.get("className");
		return Ext.JSON.encode(this.mapPanel.getMap().getGeometries(cardId, className));
	}

	function onEntryTypeSelected(entryType, danglingCard) {
		if (!entryType || !this.mapPanel.cmVisible) {
			return;
		}

		var newEntryTypeId = entryType.get("id");
		if (this.currentClassId != newEntryTypeId) {
			this.currentClassId = newEntryTypeId;
//			this.updateMap(entryType);
			this.interactionDocument.changed();
//			var cardGridController = this.mapPanel.getCardGridController(); // <<<<----
			// MINE
//			loadCardGridStore(cardGridController);
//			updateCardGridTitle(et, cardGridController);
		}

		if (danglingCard) {
			_CMCardModuleState.setCard(danglingCard);
		} else {
			// check for card selected when update
			// the map on show
			var lastCard = _CMCardModuleState.card;
			if (lastCard) {
				this.onCardSelected(lastCard);
			} else {
				this.currentCardId = undefined;
			}
		}
	}

	function onLayerAdded(map, params) {
		var layer = params.layer;
		var me = this;

		if (layer == null) {
			return;
		}

		if (this.interactionDocument.isGeoServerLayer(layer)) {
			// layer.setVisibility(this.mapState.isGeoServerLayerVisible(layer.name));
		}

		else {
			this.editingWindowDelegate.buildEditControls(layer, "POINT");
			this.selectControl.addLayer(layer);

			layer.events.on({
				"beforefeatureadded" : beforefeatureadded,
				"scope" : me
			});
		}
	}

	function onLayerRemoved(map, params) {
		var layer = params.layer;
		var me = this;

		if (layer == null || !layer.CM_Layer) {
			return;
		}

		this.editingWindowDelegate.destroyEditControls(layer);
		this.selectControl.removeLayer(layer);

		layer.events.un({
			"beforefeatureadded" : beforefeatureadded,
			"scope" : me
		});
	}

	function beforefeatureadded(o) {
		var layer = o.object;

		if (layer.CM_EditLayer) {
			if (layer.features.length > 0) {
				var currentFeature = layer.features[0];
				if (o.feature.attributes.master_card) {
					// add a feature in edit layer
					// because was selected by the user
					if (currentFeature.attributes.master_card == o.feature.attributes.master_card) {
						return false; // forbid the add
					} else {
						layer.removeFeatures([ currentFeature ]);
					}
				} else {
					// is added in editing mode
					// and want only one feature
					layer.removeAllFeatures();
					return true;
				}
			}
		} else {
			var data = o.feature.data;

			if (CMDBuild.configuration.gis
					.get('cardBrowserByDomainConfiguration')['root']) { // TODO:
				// use
				// proxy
				// constants
				if (!this.mapState.isFeatureVisible(data.master_className,
						data.master_card)) { // could
					// be
					// also
					// null,
					// or
					// undefined
					layer.hideFeatureWithCardId(data.master_card, o.feature);
					return false;
				}
			}
		}

		return true;
	}

	function onCmdbLayerBeforeAdd(o) {
		var layer = o.object, feature = o.feature;

		if (this.currentCardId
				&& this.currentCardId == feature.data.master_card) {

			layer.selectFeature(feature);
		}
	}

	function onVisibilityChanged(map, visible) {
		if (visible) {
			var lastClass = _CMCardModuleState.entryType, lastCard = _CMCardModuleState.card;

			if (lastClass && this.currentClassId != lastClass.get("id")) {

				this.onEntryTypeSelected(lastClass);
			} else {
				if (lastCard
						&& (!this.currentCardId || this.currentCardId != lastCard
								.get("Id"))) {

					this.centerMapOnFeature(lastCard.data);
					this.onCardSelected(lastCard);
				}
			}

			if (this.cmIsInEditing) {
				this.editMode();
			} else {
				this.displayMode();
			}
		} else {
			if (this.cmIsInEditing) {
				this.mapPanel.displayMode();
			}
		}
	}

	// ////*****************************************************************
	// /////

	function removeLayerForGeoAttribute(geoMap, geoAttribute, me) {
		var map = geoMap;
		var l = getLayerByGeoAttribute(map, geoAttribute);
		if (l) {
			if (!geoAttribute.isUsed() && l.editLayer) {

				map.removeLayer(l.editLayer);
			}

			l.events.unregister("visibilitychanged", me,
					onLayerVisibilityChange);
			l.destroyStrategies();
			l.clearSelection();

			map.removeLayer(l);
		}
	}

	function addLayerForGeoAttribute(map, geoAttribute, me) {
		if (!geoAttribute.isZoomValid()) {
			return;
		}

		addLayerToMap(map, //
		map.makeLayer(_CMCardModuleState.entryType.get("id"), geoAttribute
				.getValues(), true));
	}

	function addLayerToMap(map, layer, me) {
		if (layer) {
			// MINE layer.events.register("visibilitychanged", me,
			// onLayerVisibilityChange);
			me.mapState.addLayer(layer, map.getZoom());
			map.addLayer(layer);

			if (typeof layer.cmdb_index != "undefined") {
				map.setLayerIndex(layer, layer.cmdb_index);
			}

			if (layer.editLayer) {
				var el = map.getLayerByName(layer.editLayer.name);
				if (!el) {
					map.addLayers([ layer.editLayer ]);
				}
			}
		}
	}
	

	function getLayerByGeoAttribute(map, geoAttribute) {
		var layers = map.getLayers();
		for (var i = 0; i < layers.getLength(); ++i) {
			var layer = layers.item(i);
			if (!layer.geoAttribute || layer.CM_EditLayer) {
				continue;
			} else if (CMDBuild.state.GeoAttributeState
					.getKey(layer.geoAttribute) == geoAttribute.getKey()) {
				return layer;
			}
		}
		return null;
	}

	function onLayerVisibilityChange(param) {
		var layer = param.object;
		this.mapState.updateLayerVisibility(layer, this.map.getZoom());

		var cardBrowserPanel = this.mapPanel.getCardBrowserPanel();
		if (layer.CM_geoserverLayer && cardBrowserPanel) {
			cardBrowserPanel.udpateCheckForLayer(layer);
		}
	}
	;

	function sortAttributesByIndex(geoAttributes) {
		var cmdbuildLayers = [];
		var geoserverLayers = [];
		for (var i = 0, l = geoAttributes.length; i < l; ++i) {
			var attr = geoAttributes[i];
			if (attr.masterTableId) {
				cmdbuildLayers[attr.index] = attr;
			} else {
				geoserverLayers[attr.index] = attr;
			}
		}

		return cmdbuildLayers.concat(geoserverLayers);
	}

})();
