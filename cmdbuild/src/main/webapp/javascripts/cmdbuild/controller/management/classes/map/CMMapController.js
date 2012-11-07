(function() {
	Ext.define("CMDBuild.controller.management.classes.CMMapController", {

		extend: "CMDBuild.controller.management.classes.CMCardDataProvider",

		mixins: {
			observable: "Ext.util.Observable",
			mapDelegate: "CMDBuild.view.management.map.CMMapPanelDelegate",
			editingWindowDelegate: "CMDBuild.view.management.map.CMMapEditingToolsWindowDelegate",
			layerSwitcherDelegate: "CMDBuild.view.management.map.CMMapLayerSwitcherDelegate",
			cardBrowserDelegate: "CMDBuild.view.management.CMCardBrowserTreeDelegate",
			cardStateDelegate: "CMDBuild.state.CMCardModuleStateDelegate",
			miniCardGridDelegate: "CMDBuild.view.management.CMMiniCardGridDelegate"
		},

		cardDataName: "geoAttributes", // CMCardDataProvider member, to say the name to use for given data

		constructor: function(mapPanel) {
			var me = this;

			if (mapPanel) {
				this.mapPanel = mapPanel;
				this.mapPanel.addDelegate(this);
				this.mapPanel.editingWindow.addDelegate(this);

				// set me as delegate of the OpenLayers.Map (pimped in CMMap)
				this.map = mapPanel.getMap();
				this.map.delegate = this;

				this.cmIsInEditing = false;

				// set the switcher controller as a map delegate
				var layerSwitcher = this.mapPanel.getLayerSwitcherPanel();
				this.mapPanel.addDelegate(
						new CMDBuild.controller.management.classes
							.CMMapLayerSwitcherController(layerSwitcher, this.map));

				// set me as a delegate of the switcher
				layerSwitcher.addDelegate(this);

				// set me as a delegate of the cardBrowser
				var cardbrowserPanel = this.mapPanel.getCardBrowserPanel();
				if (cardbrowserPanel) {
					cardbrowserPanel.addDelegate(this);
				}

				// set me as delegate of the mini card grid
				this.mapPanel.getMiniCardGrid().addDelegate(this);

				// init the miniCardGridWindowController
				this.miniCardGridWindowController = new CMDBuild.controller
					.management.CMMiniCardGridWindowFeaturesController();

				// initialize editing control
				this.editingControls = {};
				this.selectControl = new CMDBuild.Management.CMSelectFeatureController([], {
					hover: false,
					renderIntent: "default",
					eventListeners: {
						featurehighlighted: function(e) {
							me.onFeatureSelect(e.feature);
						}
					}
				});

				this.map.addControl(this.selectControl);
				this.selectControl.activate();

				// build long press controller
				buildLongPressController(this);

				// add me to the CMCardModuleStateDelegates
				_CMCardModuleState.addDelegate(this);

				this.mapState = new CMDBuild.state.CMMapState(this);
				this.map.events.register("zoomend", this, onZoomEnd);
			} else {
				throw new Error("The map controller was instantiated without a map or the related form panel");
			}
		},

		updateMap: function(entryType) {
			// at first clear the panel calling the updateMap method;
			this.mapPanel.updateMap(entryType);

			// then do something build new layers
			var geoAttributes = entryType.getGeoAttrs() || [];
			// TODO the sorting does not work
			var orderedAttrs = sortAttributesByIndex(geoAttributes);
			this.mapState.update(orderedAttrs, this.map.getZoom());
			this.map.activateStrategies(true);

		},

		onFeatureSelect: function(feature) {
			var prop = feature.attributes,
				layer = feature.layer;

			if (!layer.editLayer) {
				// the feature selected is not
				// in a cmdbLayer with an associated editLayer
				return;
			}

			_CMCardModuleState.setCard({
				Id: prop.master_card,
				IdClass: prop.master_class
			});
		},

		/*
		 * card could be either a String (the id of the card) or a Ext.model.Model
		 */
		onCardSelected: function(card) {
			if (!this.mapPanel.cmVisible) {
				return;
				// the selection is deferred when the map is shown
			}

			var id = card;
			if (card && typeof card.get == "function") {
				id = card.get("Id");
			}

			if (id != this.currentCardId) {
				this.currentCardId = id;
				var layers = this.mapPanel.getMap().getCmdbLayers();

				for (var i=0, l=layers.length; i<l; ++i) {
					layers[i].clearSelection();
					layers[i].selectFeatureByMasterCard(this.currentCardId);
				}
			}

			// to sync the cardBrowserPanelSelection
			if (this.mapPanel.getCardBrowserPanel()) {
				this.mapPanel.getCardBrowserPanel().selectCardSilently(card);
			}

			// to sync the miniCardGrid
			// TODO ensure that the grid is on the right page
			this.mapPanel.getMiniCardGrid().selectCardSilently(card);
		},

		onAddCardButtonClick: function() {
			this.mapPanel.getMap().clearSelection();
			this.currentCardId = undefined;
			this.mapPanel.getMap().refreshStrategies();
		},

		centerMapOnFeature: function(params) {
			var me = this;

			function onSuccess(resp, req, feature) {
				// the card could have no feature
				if (feature.properties) {
					me.mapPanel.getMap().centerOnGeometry(feature);
				} else {
					me.mapPanel.getMap().clearSelection();
				}
			};

			CMDBuild.ServiceProxy.getFeature(params.IdClass, params.Id, onSuccess);
		},

		activateTransformConrol: function(layerId) {
			activateControl.call(this, layerId, "transform");
		},

		editMode: function() {
			this.cmIsInEditing = true;

			if (this.mapPanel.cmVisible) {
				this.mapPanel.editMode();
				this.deactivateSelectControl();
			}
		},

		displayMode: function() {
			this.cmIsInEditing = false;

			if (this.mapPanel.cmVisible) {
				this.mapPanel.displayMode();
				deactivateEditControls.call(this);
				this.activateSelectControl();
			}
		},

		onCardSaved: function(c) {
			/*
			 * Normally after the save, the main controller
			 * say to the grid to reload it, and select the
			 * new card. If the map is visible on save, this
			 * could not be done, so say to this controller
			 * to refresh the features loaded, and set the
			 * new card as selected
			 */
			if (this.mapPanel.cmVisible) {
				var me = this;

				_CMCardModuleState.setCard({
					Id: c.Id,
					IdClass: c.IdClass
				}, function(card) {
					me.mapPanel.getMap().clearSelection();
					me.mapPanel.getMap().refreshStrategies();
				});
			}
		},

		deactivateSelectControl: function() {
			this.selectControl.deactivate();
		},

		activateSelectControl: function() {
			this.selectControl.activate();
		},

		selectFeature: function(feauture) {
			this.selectControl.select(feauture);
		},

		onEntryTypeSelected: onEntryTypeSelected,
		getCardData: getCardData,

		/* As mapDelegate *********/

		onLayerAdded: onLayerAdded,
		onLayerRemoved: onLayerRemoved,
		onMapPanelVisibilityChanged: onVisibilityChanged,

		/* As editingWindowDelegate *********/

		addFeatureButtonHasBeenToggled: onAddFeatureButtonToggle,
		removeFeatureButtonHasBeenClicked: onRemoveFeatureButtonClick,
		geoAttributeMenuItemHasBeenClicked: activateEditControls,

		/* As layerSwitcherDelegate *********/

		onLayerCheckChange: function(node, checked) {
			var map = this.mapPanel.getMap();
			if (map) {
				var layer = map.getLayersBy("id", node.layerId);
				if (layer.length > 0) {
					layer[0].setVisibility(checked);
				}
			}
		},

		/* As cardBrowserDelegate *********/

		// Hide or show the feature[s] for the node
		// from the map.
		// the action has effect over all the branch that start with the
		// passed node.
		// So, if the node was never opened,
		// there aren't the info to show/hide the features.
		// For this reason, act like an expand, loading the
		// branch at all, and then show/hide the features.

		onCardBrowserTreeCheckChange: function(tree, node, checked) {
			var forceChildren = true;
			setFeatureVisibilityForAllBranch(tree, this.mapPanel.getMap(), node, checked, forceChildren);
		},

		onCardBrowserTreeItemExpand: function(tree, node) {
			tree.dataSource.loadChildren(node);
		},

		onCardBrowserTreeCardSelected: function(cardBaseInfo) {
			_CMMainViewportController.openCard(cardBaseInfo);
		},

		onCardBrowserTreeItemAdded: function(tree, targetNode, newNode) {
			var card = _CMCardModuleState.card;
			if (this.mapPanel.getCardBrowserPanel() 
					&& newNode.isBindingCard(card)) {
				this.mapPanel.getCardBrowserPanel().selectNodeSilently(newNode);
			}
		},

		onCardBrowserTreeActivate: function(cardBrowserTree, activationCount) {
			// init the cardBrowserDataSource
			if (activationCount == 1) {
				new CMDBuild.controller.management.classes.CMCardBrowserTreeDataSource(cardBrowserTree);
			}
		},

		/* As CMCardModuleStateDelegate ***************/

		onEntryTypeDidChange: function(state, entryType, danglingCard) {
			this.onEntryTypeSelected(entryType, danglingCard);
		},

		onCardDidChange: function(state, card) {
			this.onCardSelected(card);
		},

		/* As CMMap delegate ****************/

		featureWasAdded: function(feature) {
			if (feature.data) {
				var data = feature.data;
				var currentClassId = _CMCardModuleState.entryType ? _CMCardModuleState.entryType.getId() : null;
				var currentCardId = _CMCardModuleState.card ? _CMCardModuleState.card.get("Id") : null;

				if (data.master_card == currentCardId
						&& data.master_class == currentClassId) {

					feature.layer.selectFeature(feature);
				}
			}
		},

		/* As miniCardGridDelegate ************/

		miniCardGridDidActivate: loadMiniCardGridStore,
		miniCardGridWantOpenCard: function(grid, card) {
			_CMCardModuleState.setCard(card);
		},

		// As CMDBuild.state.CMMapStateDelegate

		geoAttributeUsageChanged: function(geoAttribute) {
			if (!geoAttribute.isUsed()) {
				removeLayerForGeoAttribute(this.map, geoAttribute, this);
			} else {
				addLayerForGeoAttribute(this.map, geoAttribute, this);
			}
		},

		geoAttributeZoomValidityChanged: function(geoAttribute) {
			if (!geoAttribute.isZoomValid()) {
				removeLayerForGeoAttribute(this.map, geoAttribute, this);
			} else {
				addLayerForGeoAttribute(this.map, geoAttribute, this);
			}
		}
	});

	function onZoomEnd() {
		this.mapState.updateForZoom(this.map.getZoom());
	};

	function buildLongPressController(me) {
		var map = me.map;
		var longPressControl = new OpenLayers.Control.LongPress({
			onLongPress: function(e) {
				var lonlat = map.getLonLatFromPixel(e.xy);
				var features = map.getFeaturesInLonLat(lonlat);

				// no features no window
				if (features.length == 0) {
					return;
				}

				me.miniCardGridWindowController.setFeatures(features);
				if (me.miniCardGridWindow) {
					me.miniCardGridWindow.close();
				}

				me.miniCardGridWindow = new CMDBuild.view.management.CMMiniCardGridWindow({
					width: me.mapPanel.getWidth() / 100 * 40,
					height: me.mapPanel.getHeight() / 100 * 80,
					x: e.xy.x,
					y: e.xy.y,
					dataSource: me.miniCardGridWindowController.getDataSource()
				});

				me.miniCardGridWindowController.bindMiniCardGridWindow(me.miniCardGridWindow);
				me.miniCardGridWindow.show();
			}
		});

		map.addControl(longPressControl);
		longPressControl.activate();
	}

	function loadMiniCardGridStore(grid) {
		if (!grid.isVisible()) {
			return;
		}

		var ds = grid.getDataSource();
		var currentIdClass = _CMCardModuleState.entryType.getId();

		if (!ds || ds.getLastEntryTypeIdLoaded() == currentIdClass) {
			return;
		}

		ds.loadStoreForEntryTypeId(currentIdClass, //
			function(records, operation, success) {
				var currentCard = _CMCardModuleState.card;
				if (!currentCard) {
					return;
				}

				for (var i=0, r=null; i<records.length; ++i) {
					r = records[i];
					if (r && r.get("Id") == currentCard.get("Id")
							&& r.get("IdClass") == currentCard.get("IdClass")) {

						grid.selectRecordSilently(r);
					}
				}
			}
		);
	}

	function updateMiniCardGridTitle(entryType, grid) {
		var prefix = CMDBuild.Translation.management.modcard.title;
		grid.setTitle(prefix + entryType.get("name"));
	}

	function setFeatureVisibilityForAllBranch(tree, map, node, checked, forceChildren) {
		setCardFeaturesVisibility(map, node, checked);

		if (forceChildren || !node.isExpanded()) {
			if (node.didChildrenLoaded()) {
				var children = node.childNodes || [];
				setChildrenFeaturesVisibility(tree, map, checked, children, true);
			} else {
				tree.dataSource.loadChildren(node, function(children) {
					setChildrenFeaturesVisibility(tree, map, checked, children, true);
				});
			}
		}
	}

	function setCardFeaturesVisibility(map, node, visibility) {
		var className = node.getCMDBuildClassName();
		var cardId = node.getCardId();
		var layers = map.getLayersByTargetClassName(className);

		for (var i=0, layer=null; i<layers.length; ++i) {
			layer = layers[i];
			if (visibility) {
				if (typeof layer.showFeatureWithCardId == "function") {
					layer.showFeatureWithCardId(cardId);
				}
			} else {
				if (typeof layer.hideFeatureWithCardId == "function") {
					layer.hideFeatureWithCardId(cardId);
				}
			}
		}
	}

	function setChildrenFeaturesVisibility(tree, map, checked, children, forceChildren) {
		for (var i=0, child=null; i<children.length; ++i) {
			child = children[i];
			child.set("checked", checked);
			setFeatureVisibilityForAllBranch(tree, map, child, checked, forceChildren);
		}
	}

	function getCardData() {
		return Ext.JSON.encode(this.mapPanel.getMap().getEditedGeometries());
	};

	function onEntryTypeSelected(et, danglingCard) {
		if (!et 
				|| !this.mapPanel.cmVisible) {
			return;
		}

		var newEntryTypeId = et.get("id");
		if (this.currentClassId != newEntryTypeId) {
			this.currentClassId = newEntryTypeId;
			this.updateMap(et);

			var miniCardGrid = this.mapPanel.getMiniCardGrid();
			loadMiniCardGridStore(miniCardGrid);
			updateMiniCardGridTitle(et, miniCardGrid);
		}

		if (danglingCard) {
			_CMCardModuleState.setCard(danglingCard);
		} else {
			// check for card selected when update
			// the map on show
			var lastCard = _CMCardModuleState.card;
			if (lastCard) {
				this.centerMapOnFeature(lastCard.data);
			} else {
				this.currentCardId = undefined;
			}
		}
	}

	function onLayerAdded(map, params) {
		var layer = params.layer;

		if (layer == null || !layer.CM_Layer) {
			return;
		}

		buildEditControls.call(this, layer);
		this.selectControl.addLayer(layer);
	}

	function onLayerRemoved(map, params) {
		var layer = params.layer;

		if (layer == null || !layer.CM_Layer) {
			return;
		}

		destroyEditControls.call(this, layer);
		this.selectControl.removeLayer(layer);
	}

	function onEditableLayerBeforeAdd(o) {
		var layer = o.object;

		if (layer.features.length > 0) {
			var currentFeature = layer.features[0];
			if (o.feature.attributes.master_card) {
				// add a feature in edit layer
				// because was selected by the user
				if (currentFeature.attributes.master_card == o.feature.attributes.master_card) {
					return false; // forbid the add
				} else {
					layer.removeFeatures([currentFeature]);
				}
			} else {
				// is added in editing mode
				// and want only one feature
				layer.removeAllFeatures();
				return true;
			}
		}
		return true;
	}

	function activateEditControls(editLayer) {
		deactivateEditControls.call(this);

		this.currentEditLayer = editLayer;
		this.activateTransformConrol(editLayer.name);

		var editFeature = editLayer.features[0];

		if (editFeature) {
			setTransformControlFeature.call(this, editLayer.name, editFeature);
			editLayer.drawFeature(editFeature, "select");
		}
	}

	function deactivateEditControls() {
		for (var layer in this.editingControls) {
			for (var control in this.editingControls[layer]) {
				this.editingControls[layer][control].deactivate();
			}
		}
	};

	function onCmdbLayerBeforeAdd(o) {
		var layer = o.object,
			feature = o.feature;

		if (this.currentCardId 
				&& this.currentCardId == feature.data.master_card) {

			layer.selectFeature(feature);
		}
	}

	function onAddFeatureButtonToggle(toggled) {
		if (toggled) {
			activateControl.call(this, this.currentEditLayer.id, "creation");
			deactivateControl.call(this, this.currentEditLayer.id, "transform");
		} else {
			deactivateControl.call(this, this.currentEditLayer.id, "creation");
			activateControl.call(this, this.currentEditLayer.id, "transform");
		}
	}

	function onRemoveFeatureButtonClick() {
		if (this.currentEditLayer) {
			this.currentEditLayer.removeAllFeatures();
		}
	}

	function onVisibilityChanged(map, visible) {
		if (visible) {
			var lastClass = _CMCardModuleState.entryType,
				lastCard = _CMCardModuleState.card;

			if (lastClass 
				&& this.currentClassId != lastClass.get("id")) {

				this.onEntryTypeSelected(lastClass);
			} else {
				if (lastCard 
						&& (!this.currentCardId || this.currentCardId != lastCard.get("Id"))) {

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

	function buildEditControls(layer) {
		if (layer.editLayer) {
			if (this.editingControls[layer.editLayer.name]) {
				return;
			}

			_debug("BUILD EDIT CONTROL", layer.editLayer.id, layer.editLayer.name);

			var geoAttribute = layer.geoAttribute,
				creation = buildCreationControl(geoAttribute.type, layer.editLayer),
				transform = buildTransformControl(layer.editLayer);

			this.map.addControls([creation, transform]);
			this.editingControls[layer.editLayer.name] = {
				creation: creation,
				transform: transform
			};

			this.mapPanel.addLayerToEditingWindow(layer);
		}
	}

	function destroyEditControls(layer) {
		if (layer.editLayer) {
			if (this.mapState.isAUsedGeoAttribute(layer.geoAttribute)) {
				return;
			}

			var name = layer.editLayer.name;
			for (var control in this.editingControls[name]) {
				this.mapPanel.getMap().removeControl(this.editingControls[name][control]);
				delete this.editingControls[name][control];
			}

			delete this.editingControls[name];
		}
	};

	function activateControl(layerId, controlName) {
		var l = this.editingControls[layerId];
		if (l[controlName]) {
			l[controlName].activate();
		}
	};

	function setTransformControlFeature(layerId, feature) {
		if (feature) {
			var l = this.editingControls[layerId];
			if (l["transform"]) {
				l["transform"].selectFeature(feature);
			}
		}
	};

	function deactivateControl(layerId, controlName) {
		_debug("DEACTIVATE", layerId, controlName);
		var l = this.editingControls[layerId];
		if (l[controlName]) {
			l[controlName].deactivate();
		}
	};

	function buildTransformControl(layer) { 
		var c = new OpenLayers.Control.ModifyFeature(layer);
		c.mode = OpenLayers.Control.ModifyFeature.DRAG
		|= OpenLayers.Control.ModifyFeature.ROTATE
		|= OpenLayers.Control.ModifyFeature.RESIZE;
		return c;
	};

	function buildCreationControl(type, layer) {
		var controlBuilders = {
			POINT: function(layer) {
				return new OpenLayers.Control.DrawFeature(layer, OpenLayers.Handler.Point);
			},
			POLYGON: function(layer) {
				return new OpenLayers.Control.DrawFeature(layer, OpenLayers.Handler.Polygon);
			},
			LINESTRING: function(layer) {
				return new OpenLayers.Control.DrawFeature(layer, OpenLayers.Handler.Path);
			}
		};
		return controlBuilders[type](layer);
	};

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

				me.miniCardGridWindowController.setFeatures(features);
				if (me.miniCardGridWindow) {
					me.miniCardGridWindow.close();
				}

				me.miniCardGridWindow = new CMDBuild.view.management.CMMiniCardGridWindow({
					width : me.mapPanel.getWidth() / 100 * 40,
					height : me.mapPanel.getHeight() / 100 * 80,
					x : e.xy.x,
					y : e.xy.y,
					dataSource : me.miniCardGridWindowController.getDataSource()
				});

				me.miniCardGridWindowController.bindMiniCardGridWindow(me.miniCardGridWindow);
				me.miniCardGridWindow.show();
			}
		});

		map.addControl(longPressControl);
		longPressControl.activate();
	}

	//////***************************************************************** /////

	function removeLayerForGeoAttribute(map, geoAttribute, me) {
		var l = getLayerByGeoAttribute(map, geoAttribute);
		if (l) {
			if (!geoAttribute.isUsed() 
					&& l.editLayer) {

				map.removeLayer(l.editLayer);
			}

			l.events.unregister("visibilitychanged", me, onLayerVisibilityChange);
			l.destroyStrategies();
			map.removeLayer(l);
		}
	}

	function addLayerForGeoAttribute(map, geoAttribute, me) {
		if (!geoAttribute.isZoomValid()) {
			return;
		}

		addLayerToMap(map, // 
			CMDBuild.Management.CMMap.LayerBuilder.buildLayer({
				classId : _CMCardModuleState.entryType.get("id"),
				geoAttribute : geoAttribute.getValues(),
				withEditLayer : true
			}, map), //
			me
		);
	}

	function addLayerToMap(map, layer, me) {
		if (layer) {
			layer.events.register("visibilitychanged", me, onLayerVisibilityChange);
			me.mapState.addLayer(layer, map.getZoom());
			map.addLayers([layer]);

			if (layer.editLayer) {
				var el = map.getLayerByName(layer.editLayer.name);
				if (!el) {
					map.addLayers([layer.editLayer]);
				}
			}
		}
	};

	function getLayerByGeoAttribute(me, geoAttribute) {
		for (var i=0, l=me.layers.length; i<l; ++i) {
			var layer = me.layers[i];
			if (!layer.geoAttribute 
					|| layer.CM_EditLayer) {
				continue;
			} else if (CMDBuild.state.GeoAttributeState.getKey(layer.geoAttribute)
					== geoAttribute.getKey()) {
				return layer;
			}
		}

		return null;
	}

	function onLayerVisibilityChange(param) {
		var layer = param.object;
		this.mapState.updateLayerVisibility(layer, this.map.getZoom());
	};

	function sortAttributesByIndex(geoAttributes) {
		var out = [];
		for (var i=0, l=geoAttributes.length; i<l; ++i) {
			var attr = geoAttributes[i];
			out[attr.index] = attr;
		}

		return out;
	};
})();