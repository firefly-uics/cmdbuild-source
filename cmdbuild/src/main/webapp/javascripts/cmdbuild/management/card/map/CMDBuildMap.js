(function() {
	/**
	 * @class CMDBuild.Management.CMDBuildMap
	 */
	CMDBuild.Management.CMDBuildMap = OpenLayers.Class(OpenLayers.Map, {
		cmdbLayers: [], //array with the layers added		
		update: function(params, withEditLayer) {
			removeCmdbLayers.call(this);
			addCmdbLayers.call(this, params, withEditLayer);
			this.controller.setSelectableLayers(this.cmdbLayers);
		},
		
		getEditableLayers: function() {
			var layers = this.cmdbLayers;
			var editLayers = [];
			for (var i=0, l=layers.length; i<l; ++i) {
				var layer = layers[i];
				if (layer.editLayer) {
					editLayers.push(layer.editLayer);
				}
			}
			return editLayers;
		},
		
		activateStrategies: function(acvitate) {
			var layers = this.cmdbLayers;
			for (var i=0, l=layers.length; i<l; ++i) {
				var layer = layers[i];
				layer.activateStrategies(acvitate);
			}
		},
		
		centerOnGeometry: function(geometry) {
			try {
				var geom = CMDBuild.GeoUtils.readGeoJSON(geometry.geometry);
				var center = geom.getCentroid();
				var lonLat = new OpenLayers.LonLat(center.x, center.y);
				this.setCenter(lonLat);
				this.activateStrategies(true);
			} catch (Error) {
				_debug("Map: centerOnGeometry - Error");
				/*
				 * if the server doesn't return a feature
				 * the readGeoJSON methods throw an error.Rightly!!
				 */
			}			
		},
		
		getFeatureByMasterCard: function(id) {
			var layers = this.cmdbLayers;
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
		},
		
		clearSelection: function() {			
			var layers = this.cmdbLayers;
			for (var i=0, l=this.layers.length; i<l; ++i) {
				var layer = layers[i];
				if (layer) {
					layer.clearSelection();					
				}
			}
		},
		
		getLayerByName: function(name) {
			var l = this.getLayersByName(name);
			if (l.length > 0) {
				return l[0];
			} else {
				return null;
			}
		},
		
		getEditedGeometries: function() {
			var mapOfFeatures = {};
			var layers = this.cmdbLayers;
			for (var i=0, l=layers.length; i<l; ++i) {
				var layer = layers[i];
				if (layer.editLayer) {
					var geo = layer.getEditedGeometry();
					if (geo != null) {
						mapOfFeatures[layer.geoAttribute.name] = geo.toString();
					} else {
						mapOfFeatures[layer.geoAttribute.name] = "";
					}
				}
			}
			return mapOfFeatures;
		},
		
		onLoadCard: function(cardId) {
			var layers = this.cmdbLayers;
			for (var i=0, l=layers.length; i<l; ++i) {
				layers[i].clearSelection();
				layers[i].selectFeatureByMasterCard(cardId);				
			}
		},
		
		refreshFeatures: function() {
			var layers = this.cmdbLayers;
			for (var i=0, l=layers.length; i<l; ++i) {
				layers[i].refreshFeatures();			
			}
		},
		
		reselectLastSelection: function() {
			var layers = this.cmdbLayers;
			for (var i=0, l=layers.length; i<l; ++i) {
				layers[i].reselectLastSelection();
			}
		},
		
		removeAllPopups: function() {
			var popups = this.popups;
			for (var i=0, l=popups.length; i<l; i++) {
				this.removePopup(popups[i]);
			}
		}
	});
	
    // subroutine of the update method
    function removeCmdbLayers() {
		if (this.cmdbLayers) {
			var layer = this.cmdbLayers.pop();
			while (layer) {
				this.removeLayer(layer);
				if (layer.editLayer) {
					this.removeLayer(layer.editLayer);
				}
				layer = this.cmdbLayers.pop();
			}
		}
		this.cmdbLayers = [];		
	};
	
	function orderAttributesByIndex(geoAttributes) {
		var out = [];
		for (var i=0, l=geoAttributes.length; i<l; ++i) {
			var attr = geoAttributes[i];
			out[attr.index] = attr;
		}
		return out;
	};
	
	// subroutine of the update method
	function addCmdbLayers(params, withEditLayer) {
		var geoAttributes = params.geoAttributes || [];
		var orderedAttrs = orderAttributesByIndex(geoAttributes);
		
		for (var i = orderedAttrs.length; i>=0; i--) {
			// add the related layer to the map
			var attr = orderedAttrs[i];
			var newLayer = CMDBuild.Management.CMDBuildMap.LayerBuilder.buildLayer({
				classId: params.classId,
		    	geoAttribute: attr,
		    	withEditLayer: withEditLayer
			});
			addLayerToMap.call(this, newLayer);
		}
		
		// add the editable layers to the map after
		// the cmdb layers to see them over all
		for (var i=0, l=this.cmdbLayers.length; i<l; ++i) {
			var layer = this.cmdbLayers[i];		
			if (layer.editLayer) {
				this.addLayers([layer.editLayer]);
			}
		}
	};
	
	function addLayerToMap(layer) {
		if (layer) {
			this.cmdbLayers.push(layer);
			layer.setVisibilityByZoom(this.getZoom());
			this.addLayers([layer]);
			this.controller.buildEditControls(layer);
		}
	};
	
})();