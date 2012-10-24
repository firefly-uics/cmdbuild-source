CMDBuild.Management.CMSelectFeatureController = OpenLayers.Class(OpenLayers.Control.SelectFeature, {
	initialize: function(layers, options) {
		layers = layers || [];
		options = options || {};

		OpenLayers.Control.SelectFeature.prototype.initialize.apply(this, [layers, options]);

			// TAKEN FROM OPENLAYERS TO SAY THAT I DONT WANNA
			// STOP THE CLICK EVENT

			if (this.scope === null) {
				this.scope = this;
			}

			this.initLayer(layers);
			var callbacks = {
				click : this.clickFeature,
				clickout : this.clickoutFeature
			};
			if (this.hover) {
				callbacks.over = this.overFeature;
				callbacks.out = this.outFeature;
			}

			this.callbacks = OpenLayers.Util.extend(callbacks,
					this.callbacks);

			this.handlers = {
				feature : new OpenLayers.Handler.Feature(this, this.layer,
					this.callbacks, {
						geometryTypes: this.geometryTypes,
						stopDown: false, // <===
						stopClick: false // <===
					})
			};

			if (this.box) {
				this.handlers.box = new OpenLayers.Handler.Box(this, {
					done : this.selectBox
				}, {
					boxDivClassName : "olHandlerBoxSelectFeature"
				});
			}
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