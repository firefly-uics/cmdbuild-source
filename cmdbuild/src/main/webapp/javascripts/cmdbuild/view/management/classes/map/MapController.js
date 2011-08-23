(function() {
	CMDBuild.Management.MapController = function(o) {
		if (o.map) {
			this.map = o.map;
			this.map.controller = this;
			
			this.map.events.on({
			    "addlayer": function(params) {
					var layer = params.layer;
					if (layer == null || !layer.CM_Layer) {
						return;
					}
					
					if (layer.CM_EditLayer) {
						layer.events.on({
							"beforefeatureadded": onEditableLayerBeforeAdd
						});
					} else {
						layer.events.on({
							"beforefeatureadded": onCmdbLayerBeforeAdd							
						});
					}					
				},
			    "removelayer": function(params) {
					var layer = params.layer;
					if (layer == null || !layer.CM_Layer) {
						return;
					}
					if (layer.CM_EditLayer) {
						layer.events.un({
							"beforefeatureadded": onEditableLayerBeforeAdd
						});
					} else {
						layer.events.un({
							"beforefeatureadded": onCmdbLayerBeforeAdd
						});
					}		
				},
			    scope: this
			});
			
			
			
		} else {
			throw new Error("The map controller was instantiated without a map");
		}
		
		this.classNotLoadedParams = undefined; //used to load a layer only when the map is shown
		this.editMode = false; //used to know if the panel is in editing mode or not
		this.container = o.container;
		this.silent = true; // the map is hidden at the begin
		
		buildSelectionControl.call(this);
		
		// the keys of the map are the
		// names of the layers
		this.editingControls = {};
			
		//events handling
		this.subscribe('cmdb-init-class', this.onSelectClass, this);
		this.subscribe('cmdb-load-card', this.onLoadCard, this);
		this.subscribe("cmdb-reload-card", this.onReloadCard, this);
		this.subscribe('cmdb-enablemodify-card', this.onEnableModify, this);
		this.subscribe('cmdbuild-card-disablemodify', this.onDisableModify, this);
		
		this.container.on("show", function() {
			if (this.editMode) {
				this.container.enableModify();
			}
		}, this);
	};
		
	Ext.extend(CMDBuild.Management.MapController, Ext.util.Observable, {
		updateMap: function(params) {
			removeEditControls.call(this);
			this.container.updateMap(params);
			if (params.classId &&  params.cardId) {
				this.centerMapOnFeature({
					IdClass: params.classId,
					Id: params.cardId
				});
			} else {
				this.map.activateStrategies(true);
			}
		},
		
		onFeatureSelect: function(feature) {
			var layer = feature.layer;
			layer.map.removeAllPopups();
			
			if (!layer.editLayer) {
				// the feature selected is not
				// in a cmdbLayer with an associated editLayer
				return;
			}
			
			if (feature.CM_card) {
				this.publish("cmdb-load-card", {
					record: new Ext.data.Record(feature.CM_card),
					publisher: this
				});		
			} else {
				var success = (function(response, options, decoded) {
					feature.CM_card = decoded.card;
					this.publish("cmdb-load-card", {
						record: new Ext.data.Record(feature.CM_card),
						publisher: this
					});				
				}).createDelegate(this);
				var prop = feature.attributes;
				
				CMDBuild.ServiceProxy.getCard(prop.master_class, prop.master_card, success);
			}
		},
		
		onSelectClass: function(params) {
			if (!params) {
				return;
			}			
			//add to the params the geoAttributes
			if (this.silent) {
			/*
			 * do nothing!
			 * on show, check if the
			 * current selected is different from the
			 * last selected in the CMDBuild.state.
			 */
			} else {			
				var p = Ext.apply({}, params);
				p.geoAttributes = CMDBuild.GeoUtils.getGeoAttributes(p.classId);
				this.currentClassId = p.classId;
				this.updateMap(p);
			}
		},
		
		onLoadCard: function(params) {
			if (params.publisher.id == this.id) {
				this.map.onLoadCard(params.record.data.Id);
			}
		},
		
		onReloadCard: function(params) {
			if (!this.silent) {
				this.map.clearSelection();
				this.map.refreshFeatures();
			}
		},
		
		centerMapOnFeature: function(params) {
			var onSuccess = function(resp, req, feature) {
				// the card could have no feature
				if (feature.properties) {
					this.map.centerOnGeometry(feature);
					var featureOnTheMap = this.map.getFeatureByMasterCard(feature.properties.master_card);
					if (featureOnTheMap) {		
						this.selectFeature(featureOnTheMap);
					}
				} else {
					this.map.clearSelection();
				}
			};

			CMDBuild.ServiceProxy.getFeature(params.IdClass, params.Id,
					onSuccess.createDelegate(this));			
		},
		
		onFront: function() {
			this.silent = false;
			var lastClassId = CMDBuild.State.getLastClassSelectedId();
			if (this.currentClassId != lastClassId) {
				this.onSelectClass(CMDBuild.State.getLastClassSelected());
			} else {
				var lastCardId = CMDBuild.State.getLastCardSelectedId();
				if (!this.currentFeatureId || this.currentFeatureId != lastCardId) {
					var lastCard = CMDBuild.State.getLastCardSelected();
					if (lastCard) {
						this.centerMapOnFeature(lastCard.record.data);
					}
				}
			}
		},
		
		onBack: function() {
			this.silent = true;
			if (this.editMode) {
				this.container.disableModify();
			}
		},
		
		onEnableModify: function(p) {
			if (typeof p == "undefined") {
				return;
			}
			this.editMode = true;
			this.deactivateSelectControl();
			if (p.newCard) {
				this.map.clearSelection();
			}
			this.container.enableModify();
		},
		
		onDisableModify: function() {
			this.editMode = false;
			this.activateSelectControl();
			deactivateEditControls.call(this);
			this.container.disableModify();
			this.map.reselectLastSelection();
		},
		
		buildEditControls: function(layer) {
			if (layer.editLayer) {
				var geoAttribute = layer.geoAttribute;
				
				var creation = buildCreationControl(geoAttribute.type, layer.editLayer);		
				var transform = buildTransformControl(layer.editLayer);
				
				this.map.addControls([creation, transform]);
				this.editingControls[layer.editLayer.id] = {
					creation: creation,
					transform: transform
				};
			}
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
		
		activateCreationControl: function(activate) {			
			if (activate) {
				activateControl.call(this, this.currentEditLayer.id, "creation");
				deactivateControl.call(this, this.currentEditLayer.id, "transform");
			} else {
				deactivateControl.call(this, this.currentEditLayer.id, "creation");
				activateControl.call(this, this.currentEditLayer.id, "transform");
			}
		},
		
		removeCurrentEditFeature: function() {			
			if (this.currentEditLayer) {
				this.currentEditLayer.removeAllFeatures();
			}			
		},
		// TODO inherit from CardExtensionProvider
		getValues: function() {
			return this.map.getEditedGeometries();
		},
		extensionName: "geoAttributes",
		getExtensionName: function() {
			return this.extensionName;
		}
	});
	
	function onEditableLayerBeforeAdd(o) {
		var layer = o.object;
		
		if (layer.features.length > 0) {
			var currentFeature = layer.features[0];
			if (o.feature.attributes.master_card) {
				// we are added a feature in edit layer
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
		var layer = o.object;
		if (layer.editLayer 
				&& o.feature.attributes.master_card == CMDBuild.State.getLastCardSelectedId()) {
			layer.editLayer.addFeatures([o.feature]);
			layer.lastSelection = o.feature.clone();
			return false;
		}
		return true;
	}
	
	// build the controls to manage the selection
	// of the card via map
	function buildSelectionControl() {
		var selectControl = new OpenLayers.Control.SelectFeature([], {
			hover: false,
		    renderIntent: "default",
		    eventListeners: {
		        featurehighlighted: (function(e) {
					this.onFeatureSelect(e.feature);
				}).createDelegate(this)
		    }
		});
		this.map.addControl(selectControl);
		selectControl.activate();
		
		var popupControl = new CMDBuild.Management.CMDBuildMap.PopupController();
		this.map.addControl(popupControl);
		popupControl.activate();
		
		// called by the map, after the add of new layers
		this.setSelectableLayers = function(layers) {
			var layerForPopupController = [];
			var layerForSelectController = [];
			for (var i=0, l=layers.length; i<l; ++i) {
				var layer = layers[i];
				if (layer.CM_Layer) {
					layerForPopupController.push(layer);
				}
				if (layer.editLayer) {
					layerForSelectController.push(layer.editLayer);
				}
			}
			popupControl.setLayer(layerForPopupController);
			selectControl.setLayer(layerForPopupController.concat(layerForSelectController));
		};
		
		this.deactivateSelectControl = function() {
			selectControl.deactivate();
			popupControl.deactivate();
		};
		
		this.activateSelectControl = function() {
			selectControl.activate();
			popupControl.activate();
		};
		
		this.selectFeature = function(feauture) {
			selectControl.select(feauture);
		};
	};
	
	// delete the controls and 
	// removes them from the map
	function removeEditControls() {
		for (var layer in this.editingControls) {
			for (var control in this.editingControls[layer]) {
				this.map.removeControl(this.editingControls[layer][control]);
				delete this.editingControls[layer][control];
			}
			delete this.editingControls[layer];
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