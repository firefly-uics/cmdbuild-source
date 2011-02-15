(function() {
	CMDBuild.Management.LinkCardsMapController = function(map, ownerController, model) {
		this.map = map;
		this.ownerController = ownerController;
		this.model = model;
		
		map.controller = this;
		
		this.layers = [];		
		this.selectControl = addSelectControl(map, multiple=!ownerController.singleSelect);
		
		map.events.on({
		    "addlayer": function(params) {
				var layer = params.layer;
				if (layer == null || !layer.CMDBuildLayer) {
					return;
				}
				this.layers.push(layer);
				this.selectControl.setLayer(this.layers);
				
				layer.events.on({
					"featureselected": onFeatureSelected,
					"featureunselected": onFeatureUnselected,
					"featureadded": onFeatureAdded,
					scope: this
				});
			},
		    "removelayer": function(params) {
				var layer = params.layer
				if (layer == null || !layer.CMDBuildLayer) {
					return;
				}
				this.layers.remove(layer);
				this.selectControl.setLayer(this.layers);
				
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
			_debug("Link cards - mapController: model select", selection);
			this.selectByCardId(selection);
		}, this);
		
		model.on("deselect", function(selection) {
			_debug("Link cards - mapController: model deselect", selection);
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
				this.selectControl.select(feature);
			}
		},
		
		deselectByCardId: function(cardId) {
			var feature = getFeatureByMasterCard.call(this, cardId);
			
			if (feature != null) {
				this.selectControl.unselect(feature);
			}
		} 
	};
	
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
	}
	
	function onFeatureUnselected(params) {
		var cardId = params.feature.attributes.master_card;
		this.model.deselect(String(cardId));
	}
	
	function onFeatureAdded(p) {
		var master_card = p.feature.attributes.master_card;
		if (master_card && this.model.isSelected(master_card)) {
			this.selectControl.select(p.feature);
		}
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
})();