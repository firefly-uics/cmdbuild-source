(function() {
	Ext.define("CMDBuild.controller.management.classes.CMMapController", {

		extend: "CMDBuild.controller.management.classes.CMCardDataProvider",

		mixins: {
			observable: "Ext.util.Observable",
			mapDelegate: "CMDBuild.view.management.map.CMMapPanelDelegate",
			editingWindowDelegate: "CMDBuild.view.management.map.CMMapEditingToolsWindow",
			layerSwitcherDelegate: "CMDBuild.view.management.map.CMMapLayerSwitcherDelegate",
			cardBrowserDelegate: "CMDBuild.view.management.CMCardBrowserTreeDelegate",
			cardStateDelegate: "CMDBuild.state.CMCardModuleStateDelegate"
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
							.CMMapLayerSwitcherController(layerSwitcher,
								this.mapPanel.getMap()));

				// set me as a delegate of the switcher
				layerSwitcher.addDelegate(this);

				// set me as a delegate of the cardBrowser
				var cardBrowser = this.mapPanel.getCardBrowserPanel();
				cardBrowser.addDelegate(this);

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

				this.mapPanel.getMap().addControl(this.selectControl);
				this.selectControl.activate();

				// add me to the CMCardModuleStateDelegates
				_CMCardModuleState.addDelegate(this);
			} else {
				throw new Error("The map controller was instantiated without a map or the related form panel");
			}
		},

		updateMap: function(entryType) {
			this.mapPanel.updateMap(entryType);
			this.mapPanel.getMap().activateStrategies(true);
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
				var layers = this.mapPanel.getMap().cmdbLayers;

				for (var i=0, l=layers.length; i<l; ++i) {
					layers[i].clearSelection();
					layers[i].selectFeatureByMasterCard(this.currentCardId);
				}
			}

			// to sync the cardBrowserPanelSelection
			this.mapPanel.getCardBrowserPanel().selectCardSilently(card);
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
		// If the node is a folder and is expanded, the action
		// is targeted only over the node. Otherwise, do the
		// action over all the branch that start with the
		// passed node. So, if the node was never opened,
		// there aren't the info to show/hide the features.
		// For this reason, act like an expand, loading the
		// branch at all, and then show/hide the features.

		onCardBrowserTreeCheckChange: function(tree, node, checked) {
			setFeatureVisibilityForAllBranch(tree, this.mapPanel.getMap(), node, checked, false);
		},

		onCardBrowserTreeItemExpand: function(tree, node) {
			tree.dataSource.loadChildren(node);
		},

		onCardBrowserTreeCardSelected: function(cardBaseInfo) {
			_CMMainViewportController.openCard(cardBaseInfo);
		},

		onCardBrowserTreeItemAdded: function(tree, targetNode, newNode) {
			var card = _CMCardModuleState.card;
			if (newNode.isBindingCard(card)) {
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
		}
	});

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
				layer.showFeatureWithCardId(cardId);
			} else {
				layer.hideFeatureWithCardId(cardId);
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
		this.activateTransformConrol(editLayer.id);

		var editFeature = editLayer.features[0];

		if (editFeature) {
			setTransformControlFeature.call(this, editLayer.id, editFeature);
			editLayer.drawFeature(editFeature, "select");
		}
	}

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
					this.onCardSelected(lastCard.get("Id"));
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
			var geoAttribute = layer.geoAttribute,
				creation = buildCreationControl(geoAttribute.type, layer.editLayer),
				transform = buildTransformControl(layer.editLayer);

			this.mapPanel.getMap().addControls([creation, transform]);
			this.editingControls[layer.editLayer.id] = {
				creation: creation,
				transform: transform
			};
		}
	}

	function destroyEditControls(layer) {
		if (layer.editLayer) {
			var id = layer.editLayer.id;
			for (var control in this.editingControls[id]) {
				this.mapPanel.getMap().removeControl(this.editingControls[id][control]);
				delete this.editingControls[id][control];
			}

			delete this.editingControls[id];
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
		var l = this.editingControls[layerId];
		if (l[controlName]) {
			l[controlName].deactivate();
		}
	};

	function deactivateEditControls() {
		for (var layer in this.editingControls) {
			for (var control in this.editingControls[layer]) {
				this.editingControls[layer][control].deactivate();
			}
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
})();