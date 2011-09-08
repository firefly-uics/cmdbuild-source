(function() {
	Ext.define("CMDBuild.controller.management.classes.CMMapController", {
		constructor: function(mapPanel, relatedFormPanel, ownerController) {
			var me = this;

			if (mapPanel && relatedFormPanel && ownerController) {
				this.mapPanel = mapPanel;
				this.relatedFormPanel = relatedFormPanel;
				this.ownerController = ownerController;
				this.cmIsInEditing = false;
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

				this.popupControl = new CMDBuild.Management.PopupController();
				this.mapPanel.getMap().addControl(this.popupControl);
				this.popupControl.activate();

				registerMapEventListeners.call(this);

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
				layer = feature.layer,
				me = this;

			layer.map.removeAllPopups();

			if (!layer.editLayer) {
				// the feature selected is not
				// in a cmdbLayer with an associated editLayer
				return;
			}

			this.ownerController.onCardSelected(selectionModel = null, card = {
				Id: prop.master_card,
				IdClass: prop.master_class
			});

			this.onCardSelected(prop.master_card);
		},

		onCardSelected: function(cardId) {
			this.currentCardId = cardId;
			var layers = this.mapPanel.getMap().cmdbLayers;

			for (var i=0, l=layers.length; i<l; ++i) {
				layers[i].clearSelection();
				layers[i].selectFeatureByMasterCard(cardId);
			}
		},

		onEntryTypeSelect: function(et) {
			if (!et) {
				return;
			}

			if (this.mapPanel.cmVisible) {
				this.currentClassId = et.get("id");
				// if update the map on show and there is a card selected
				var lastCard = this.ownerController.currentCard;
				if (lastCard) {
					this.currentCardId = lastCard.get("Id");
					this.centerMapOnFeature(lastCard.data);
				} else {
					this.currentCardId = undefined;
				}

				this.updateMap(et);
			} else {
				/*
				 * do nothing!
				 * on show, check if the
				 * current selected is different from the
				 * last selected in the CMDBuild.state.
				 */
			}
		},

		onAddCardButtonClick: function() {
			this.mapPanel.getMap().clearSelection();
			this.currentCardId = undefined;
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

		activateEditControls: function(editLayer) {
			deactivateEditControls.call(this);

			this.currentEditLayer = editLayer;
			this.activateTransformConrol(editLayer.id);

			var editFeature = editLayer.features[0];

			if (editFeature) {
				setTransformControlFeature.call(this, editLayer.id, editFeature);
				editLayer.drawFeature(editFeature, "select");
			}
		},

		activateTransformConrol: function(layerId) {
			activateControl.call(this, layerId, "transform");
		},

		getValues: function() {
			return Ext.JSON.encode(this.mapPanel.getMap().getEditedGeometries());
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
			if (this.mapPanel.cmVisible) {
				this.mapPanel.getMap().clearSelection();
				this.mapPanel.getMap().refreshStrategies();

				this.displayMode();

				onCmVisible.call(this);
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
		}
	});

	function registerMapEventListeners() {
		this.mapPanel.mon(this.mapPanel, "addlayer", onLayerAdded, this);
		this.mapPanel.mon(this.mapPanel, "removelayer",onLayerRemoved,this);
		this.mapPanel.mon(this.mapPanel, "addFeatureButtonToogle", onAddFeatureButtonToggle, this);
		this.mapPanel.mon(this.mapPanel, "onRemoveFeatureButtonClick", onRemoveFeatureButtonClick, this);
		this.mapPanel.mon(this.mapPanel, "cmGeoAttrMenuClicked", this.activateEditControls, this);
		this.mapPanel.mon(this.mapPanel, "cmVisible", onCmVisible, this);

		this.relatedFormPanel.mon(this.relatedFormPanel, "cmeditmode", this.editMode, this);
		this.relatedFormPanel.mon(this.relatedFormPanel, "cmdisplaymode", this.displayMode, this);
	}

	function onLayerAdded(params) {
		var layer = params.layer,
			me = this;

		if (layer == null || !layer.CM_Layer) {
			return;
		}

		buildEditControls.call(this, layer);

		if (layer.CM_EditLayer) {
			layer.events.on({
				"beforefeatureadded": onEditableLayerBeforeAdd,
				"scope": me
			});
		} else {
			this.popupControl.addLayer(layer);
			layer.events.on({
				"featureadded": onCmdbLayerBeforeAdd,
				"scope": me
			});
		}

		this.selectControl.addLayer(layer);
	}

	function onLayerRemoved(params) {
		var layer = params.layer,
			me = this;

		if (layer == null || !layer.CM_Layer) {
			return;
		}

		destroyEditControls.call(this, layer);

		if (layer.CM_EditLayer) {
			layer.events.un({
				"beforefeatureadded": onEditableLayerBeforeAdd,
				"scope": me
			});
		} else {
			this.popupControl.addLayer(layer);
			layer.events.un({
				"featureadded": onCmdbLayerBeforeAdd,
				"scope": me
			});
		}
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

	function onCmVisible(visible) {
		if (visible) {
			var lastClassId = this.ownerController.currentEntryId,
				lastCard = this.ownerController.currentCard;

			if (this.currentClassId != lastClassId) {
				this.onEntryTypeSelect(this.ownerController.currentEntry);
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