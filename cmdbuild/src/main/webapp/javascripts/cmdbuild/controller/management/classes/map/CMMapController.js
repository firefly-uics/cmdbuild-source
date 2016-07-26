(function() {

	Ext.require([ 'CMDBuild.proxy.gis.Gis' ]);

	Ext.define("CMDBuild.controller.management.classes.map.CMMapController", {
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

		cmfgCatchedFunctions : [ 'onCardZoom' ],

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
				this.mapState = new CMDBuild.state.CMMapState(this);
				_CMMapState = this.mapState;

				var cardbrowserPanel = this.mapPanel.getCardBrowserPanel();
				if (cardbrowserPanel) {
					new CMDBuild.controller.management.classes.CMCardBrowserTreeDataSource(cardbrowserPanel,
							this.mapState);
					cardbrowserPanel.addDelegate(new CMDBuild.controller.management.classes.map.CMCardBrowserDelegate(
							this));
				}

				// initialize editing control
				this.editingWindowDelegate = new CMDBuild.controller.management.classes.map.CMMapEditingWindowDelegate(
						this, this.interactionDocument);
				this.mapPanel.editingWindow.addDelegate(this.editingWindowDelegate);

				this.map.addControl(this.selectControl);
				// MINE NB this.selectControl.activate();

				// build long press controller
				// MINE NB buildLongPressController(this);

				// add me to the CMCardModuleStateDelegates
				_CMCardModuleState.addDelegate(this);

				// MINE NB this.map.events.register("zoomend",
				// this, onZoomEnd);
			} else {
				throw new Error("The map controller was instantiated without a map or the related form panel");
			}
		},

		/*
		 * card could be either a String (the id of the card) or a
		 * Ext.model.Model
		 */
		onCardSelected : function(card) {
			var cardId = card.cardId;
			var className = card.className;
			var type = _CMCache.getEntryTypeByName(className);
			_CMCardModuleState.setCard({
				Id : cardId,
				className : className,
				IdClass : type.get("id")
			});
			this.interactionDocument.setCurrentCard({
				cardId : cardId,
				className : className
			});
			this.interactionDocument.centerOnCard({
				className : className, 
				cardId : cardId
			});
			this.interactionDocument.changed();
		},

		onAddCardButtonClick : function() {
			this.mapPanel.getMap().clearSelection();
			// this.currentCardId = undefined;
			// this.mapPanel.getMap().refreshStrategies();
		},
		onCardZoom : function() {
			console.log("-------------------------------");
		},
		centerMapOnFeature : function(params) {
			var className = _CMCache.getEntryTypeNameById(params.IdClass);
			this.interactionDocument.centerOnCard({
				className : className, 
				cardId : params.Id
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
				this.activateSelectControl();
			}
		},

		onCardSaved : function(c) {
			/*
			 * Normally after the save, the main controller say to the grid to
			 * reload it, and select the new card. If the map is visible on
			 * save, this could not be done, so say to this controller to
			 * refresh the features loaded, and set the new card as selected
			 */
			if (this.mapPanel.cmVisible) {
				var me = this;

				_CMCardModuleState.setCard({
					Id : c.Id,
					IdClass : c.IdClass
				}, function(card) {
					me.mapPanel.getMap().changeIdOnLayers(-1, c.Id);
					var type = _CMCache.getEntryTypeById(c.IdClass);
					me.interactionDocument.setCurrentCard({
						cardId : c.Id,
						className : type.get("name")
					});
					me.interactionDocument.changed();
					me.interactionDocument.changedFeature();
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

		onEntryTypeDidChange : function(state, entryType, danglingCard) {
			this.onEntryTypeSelected(entryType, danglingCard);
		},

		onCardDidChange : function(state, card) {
		},

		/* As CMMap delegate *************** */

		featureWasAdded : function(feature) {
		},

		// As CMDBuild.state.CMMapStateDelegate

		geoAttributeUsageChanged : function(geoAttribute) {
		},

		geoAttributeZoomValidityChanged : function(geoAttribute) {
			if (!geoAttribute.isZoomValid()) {
				removeLayerForGeoAttribute(this.map, geoAttribute, this);
			} else {
				addLayerForGeoAttribute(this.map, geoAttribute, this);
			}
		},

		featureVisibilityChanged : function(className, cardId, visible) {
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
		var longPressControl = new OpenLayers.Control.LongPress({
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
		var cardId = params.cardId;
		var className = params.cardIdclassName;
		var geo = this.mapPanel.getMap().getGeometries(cardId, className);
		return Ext.JSON.encode(geo);
	}

	function onEntryTypeSelected(entryType, danglingCard) {
		if (!entryType || !this.mapPanel.cmVisible) {
			return;
		}

		var newEntryTypeId = entryType.get("id");
		var lastCard = _CMCardModuleState.card;
		if (this.currentClassId != newEntryTypeId) {
			this.currentClassId = newEntryTypeId;
			lastCard = undefined;
		}

		if (danglingCard) {
			this.onCardSelected({
				cardId : danglingCard.Id,
				className : entryType.get("name")
			});
		} 
		else {
			this.onCardSelected({
				cardId : -1,
				className : entryType.get("name")
			});
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

		layer.events.un({
			"beforefeatureadded" : beforefeatureadded,
			"scope" : me
		});
	}

	function beforefeatureadded(o) {
		return true;
	}

	function onVisibilityChanged(map, visible) {
		if (visible) {
			var lastClass = _CMCardModuleState.entryType, lastCard = _CMCardModuleState.card;

			if (lastClass && this.currentClassId != lastClass.get("id")) {

				this.onEntryTypeSelected(lastClass);
			} else {
				if (lastCard && (!this.currentCardId || this.currentCardId != lastCard.get("Id"))) {

					this.centerMapOnFeature(lastCard.data);
					this.onCardSelected({
						cardId : lastCard.get("Id"), 
						className : lastCard.get("className")
					});
				}
			}

//			if (this.cmIsInEditing) {
//				this.editMode();
//			} else {
//				this.displayMode();
//			}
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

			l.events.unregister("visibilitychanged", me, onLayerVisibilityChange);
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
		map.makeLayer(_CMCardModuleState.entryType.get("id"), geoAttribute.getValues(), true));
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
			} else if (CMDBuild.state.GeoAttributeState.getKey(layer.geoAttribute) == geoAttribute.getKey()) {
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
