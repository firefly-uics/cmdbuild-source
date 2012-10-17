(function() {

	/**
	 * @class CMDBuild.Management.CMDBuildMap
	 */
	CMDBuild.Management.CMMap = OpenLayers.Class(OpenLayers.Map, {
		cmdbLayers: [], //array with the layers added

		update: function(entryType, withEditLayer) {
			removeCmdbLayers(this);
			addCmdbLayers(this, entryType, withEditLayer);
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

		refreshStrategies: function() {
			var layers = this.cmdbLayers;
			for (var i=0, l=layers.length; i<l; ++i) {
				var layer = layers[i];
				if (typeof layer.refreshStrategies == "function") {
					layer.refreshStrategies();
				}
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

		getLayersByTargetClassName: function(targetClassName) {
			var layers = [];
			for (var i=0, layer=null; i<this.cmdbLayers.length; ++i) {
				layer = this.cmdbLayers[i];
				
				if (layer.geoAttribute 
						// TODO or an ancestor?
						&& layer.geoAttribute.masterTableName == targetClassName) {

					layers.push(layer);
				}
			}

			return layers;
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

		// called by the layers when a feature is added

		featureWasAdded: function(feature) {
			if (this.delegate) {
				this.delegate.featureWasAdded(feature);
			}
		}
	});

	// subroutine of the update method
	function removeCmdbLayers(me) {
		if (me.cmdbLayers) {
			var layer = me.cmdbLayers.pop();
			while (layer) {
				me.removeLayer(layer);
				if (layer.editLayer) {
					me.removeLayer(layer.editLayer);
				}
				delete layer;
				layer = me.cmdbLayers.pop();
			}
		}
		me.cmdbLayers = [];
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
	function addCmdbLayers(me, entryType, withEditLayer) {
		var geoAttributes = entryType.getGeoAttrs() || [];
		var orderedAttrs = orderAttributesByIndex(geoAttributes);

		for (var i = orderedAttrs.length; i>=0; i--) {
			// add the related layer to the map
			addLayerToMap.call(me, // 
					CMDBuild.Management.CMMap.LayerBuilder.buildLayer({
						classId : entryType.getId(),
						geoAttribute : orderedAttrs[i],
						withEditLayer : withEditLayer
					}, me)
				);
		}

		// add the editable layers to the map after
		// the cmdb layers to see them over all
		for (var i=0, l=me.cmdbLayers.length; i<l; ++i) {
			var layer = me.cmdbLayers[i];
			if (layer.editLayer) {
				me.addLayers([layer.editLayer]);
			}
		}
	};

	function addLayerToMap(layer) {
		if (layer) {
			this.cmdbLayers.push(layer);
			layer.setVisibilityByZoom(this.getZoom());
			this.addLayers([layer]);
		}
	};
	
})();