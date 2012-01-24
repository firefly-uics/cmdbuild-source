(function() {
	Ext.define("CMDBuild.controller.management.common.CMCardDataProvider", {
		constructor: function(target, dataName) {
			if (target) {
				this.target = target;
			} else {
				throw "You have to pass aterget to the CMCardDataProvider";
			}

			var me = this.target;
			if (typeof dataName == "string") {
				me.getCardDataName = function() {
					return dataName;
				}
			} else {
				throw "You have to set a cmCardDataName for the CMCardDataProvider " + me.$className;
			}

			if (typeof me.getCardData != "function") {
				me.getCardData = function() {
					throw "You have to implement the getCardData method in " + me.$className;
				}
			}

			return target;
		}
	});

	Ext.define("CMDBuild.controller.management.classes.CMMapController", {

		mixins: {
			observable: "Ext.util.Observable"
		},

		constructor: function(mapPanel, ownerController) {
			var me = this;

			if (mapPanel && ownerController) {
				this.mapPanel = mapPanel;

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

				return new CMDBuild.controller.management.common.CMCardDataProvider(this, "geoAttributes");
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

			this.ownerController.onCardSelected(card = {
				Id: prop.master_card,
				IdClass: prop.master_class
			});

			this.onCardSelected(prop.master_card);
		},

		onCardSelected: function(card) {
			if (!this.mapPanel.cmVisible) {
				return;
				// the selection is defered when the map is shown
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
				this.mapPanel.getMap().refreshStrategies();
				if (typeof this.currentCardId == "undefined") {
					// the card is new, alert the owner to buble the selection event
					this.ownerController.onCardSelected(card = {
						Id: c.Id,
						IdClass: c.IdClass
					});
				};

				this.onCardSelected(c.Id);
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

		// FIXME: for compatibility
		onEntryTypeSelect: onEntryTypeSelected,
		getValues: getCardData
	});

	function getCardData() {
		return Ext.JSON.encode(this.mapPanel.getMap().getEditedGeometries());
	};

	function onEntryTypeSelected(et) {
		if (!et 
				|| !this.mapPanel.cmVisible) {
			return;
		}

		this.currentClassId = et.get("id");
		// if update the map on show and there is a card selected
		var lastCard = this.ownerController.getCard();
		if (lastCard) {
			// this.currentCardId = lastCard.get("Id");
			this.onCardSelected(lastCard);
			this.centerMapOnFeature(lastCard.data);
		} else {
			this.currentCardId = undefined;
		}

		this.updateMap(et);
	}

	function registerMapEventListeners() {
		this.mon(this.mapPanel, "addlayer", onLayerAdded, this);
		this.mon(this.mapPanel, "removelayer",onLayerRemoved,this);
		this.mon(this.mapPanel, "addFeatureButtonToogle", onAddFeatureButtonToggle, this);
		this.mon(this.mapPanel, "onRemoveFeatureButtonClick", onRemoveFeatureButtonClick, this);
		this.mon(this.mapPanel, "cmGeoAttrMenuClicked", this.activateEditControls, this);
		this.mon(this.mapPanel, "cmVisible", onCmVisible, this);

		// TODO remove the reference to the relatedFormPanel and listen the event in super controller
		// this.relatedFormPanel.mon(this.relatedFormPanel, "cmeditmode", this.editMode, this);
		// this.relatedFormPanel.mon(this.relatedFormPanel, "cmdisplaymode", this.displayMode, this);
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
			var lastClass = this.ownerController.getEntryType(),
				lastCard = this.ownerController.getCard();

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