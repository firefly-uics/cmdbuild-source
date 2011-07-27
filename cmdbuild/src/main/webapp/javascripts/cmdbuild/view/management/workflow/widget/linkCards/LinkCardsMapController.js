(function() {
	CMDBuild.Management.LinkCardsMapController = function(map, ownerController, model) {
		this.map = map;
		this.ownerController = ownerController;
		this.model = model;

		map.controller = this;
		
		this.layers = [];		
		
		this.selectControl = addSelectControl(map, multiple=true);
		this.popupControl = addPopupControl(map);
		
		this.lastSelection = null;
		
		map.events.on({
		    "addlayer": function(params) {
				var layer = params.layer;
				if (layer == null || !layer.CMDBuildLayer) {
					return;
				}
				this.layers.push(layer);
				this.popupControl.setLayer(this.layers.concat([])); // to use a different handler,  setLayer of the control set to null the current layers
				this.selectControl.setLayer(this.layers.concat([]));
				
				layer.events.on({
					"featureselected": onFeatureSelected,
					"featureunselected": onFeatureUnselected,
					"featureadded": onFeatureAdded,
					scope: this
				});
			},
		    "removelayer": function(params) {
				var layer = params.layer;
				if (layer == null || !layer.CMDBuildLayer) {
					return;
				}
				this.layers.remove(layer);
				this.selectControl.setLayer(this.layers.concat([]));
				this.popupControl.setLayer(this.layers.concat([]));
				
				layer.events.un({
					"featureselected": onFeatureSelected,
					"featureunselected": onFeatureUnselected,
					"featureadded": onFeatureAdded,
					scope: this
				});		
			},
		    scope: this
		});
		
		model.on("select", function(selection) {
			this.selectByCardId(selection);
		}, this);
		
		model.on("deselect", function(selection) {
			this.deselectByCardId(selection);
		}, this);
		
		
	};
	
	CMDBuild.Management.LinkCardsMapController.prototype = {
		buildEditControls: function() {
			_debug("Build edit controls");
		},
		
		setSelectableLayers: function() {
			_debug("setSelectableLayers");
		},
		
		selectByCardId: function(cardId) {
			var feature = getFeatureByMasterCard.call(this, cardId);
			
			if (feature != null) {
				if (featureInLayerSelection(feature)) {
					return;
				} else {
					this.selectControl.select(feature);
					// the map has already the related feature, use it to center the map
					centerMapOnLoadedFeature.call(this, feature);
				}
			} else {
				// the map has not the feature but it's possible that the feature
				// exists in another bounding box, ask to the server if exists the
				// feature, and use it to center the map
				centerMapOnCardId.call(this, cardId);
			}
		},
		
		deselectByCardId: function(cardId) {
			var feature = getFeatureByMasterCard.call(this, cardId);
			
			if (feature != null) {
				this.selectControl.unselect(feature);
			}
		},
		
		getLastSelection: function() {
			return this.lastSelection;
		}
	};
	
	function addPopupControl(map) {
		var popupControl = new CMDBuild.Management.CMDBuildMap.PopupController();
		map.addControl(popupControl);
		popupControl.activate();
		
		return popupControl;
	}
	
	function addSelectControl(map, multiple) {
		var selectControl = new OpenLayers.Control.SelectFeature([], {
			hover: false,
			toggle: true,
			clickout: false,
			multiple: multiple,
			multipleKey: multiple
		});
		
		map.addControl(selectControl);
		selectControl.activate();
		
		return selectControl;
	}
	
	function onFeatureSelected(params) {
		var cardId = params.feature.attributes.master_card;
		this.model.select(String(cardId));
		this.lastSelection = cardId;
	}
	
	function onFeatureUnselected(params) {
		var cardId = params.feature.attributes.master_card;
		this.model.deselect(String(cardId));
		if (this.lastSelection == cardId) {
			this.lastSelection = null;
		}
	}
	
	function onFeatureAdded(p) {
		var master_card = p.feature.attributes.master_card;
		if (master_card && this.model.isSelected(master_card)) {
			this.selectControl.select(p.feature);
			centerMapOnLoadedFeature.call(this, p.feature);
		}		
	}
	
	function centerMapOnLoadedFeature(feature) {
		var center = feature.geometry.getCentroid();
		var lonLat = new OpenLayers.LonLat(center.x, center.y);
		this.map.setCenter(lonLat);
	}
	
	function centerMapOnCardId(cardId) {
		function onSuccess(resp, req, feature) {
			// the card could have no feature
			if (feature.geometry) {
				this.map.centerOnGeometry(feature);
			}
		};
		
		var classId = this.ownerController.extAttrDef.ClassId;
		CMDBuild.ServiceProxy.getFeature(classId, cardId,
				onSuccess.createDelegate(this));
	}
	
	function getFeatureByMasterCard(id) {
		var layers = this.layers;
		for (var i=0, l=this.layers.length; i<l; ++i) {
			var layer = layers[i];
			if (layer) {
				var feature = layer.getFeatureByMasterCard(id);
				if (feature) {
					return feature;
				}
			}
		}
		return null;
	}
	
	function featureInLayerSelection(feature) {
		var layer = feature.layer;
		var selections = layer.selectedFeatures;
		
		for (var i=0, l=selections.length; i<l; ++i) {
			var f = selections[i];
			if (f.attributes.master_card == feature.attributes.master_card) {
				return true;
			}
		}
		return false;
	}
})();