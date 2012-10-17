CMDBuild.Management.CMSelectFeatureController = OpenLayers.Class(OpenLayers.Control.SelectFeature, {
	initialize: function(layers, options) {
		layers = layers || [];
		options = options || {};

		OpenLayers.Control.SelectFeature.prototype.initialize.apply(this, [layers, options]);
	},

	addLayer: function(layer) {
		if (!layer) {
			return;
		}

		var layers = this.layers;

		layers.push(layer);
		this.setLayer(layers);
	},

	removeLayer: function(layer) {
		if (!layer) {
			return;
		}

		var layers = [];
		for (var i=0, l=this.layers.length; i<l; ++i) {
			var currentLayer = this.layers[i];
			if (layer.id != currentLayer.id) {
				layers.push(currentLayer);
			}
		}

		this.setLayer(layers);
	},

	selectFeaturesByCardId: function(cardId) { _deprecated();
		for (var i=0, l=this.map.cmdbLayers.length; i<l; i++) {
			var layer = this.map.cmdbLayers[i];
			if (layer) {
				var f = layer.getFeatureByMasterCard(cardId);
				if (f) {
					this.select(f);
				}
			}
		}
	}
});