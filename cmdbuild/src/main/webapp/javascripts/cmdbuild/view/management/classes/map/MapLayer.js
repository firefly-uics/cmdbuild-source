(function() {
	var DEFAULT_MIN_ZOOM = 0;
	var DEFAULT_MAX_ZOOM = 25;

/**
 * @class CMDBuild.Management.CMDBuildMap.MapLayer
 */

CMDBuild.Management.CMMap.MapLayer = OpenLayers.Class(OpenLayers.Layer.Vector, {

	initialize: function(name, options) {
		// Set the google projection
		this.projection = new OpenLayers.Projection("EPSG:900913"),

		// CMDBuild stuff
		this.editLayer = undefined,
		this.geoAttribute = undefined,
		this.cmdb_minZoom = options.geoAttribute.minZoom || DEFAULT_MIN_ZOOM;
		this.cmdb_maxZoom = options.geoAttribute.maxZoom || DEFAULT_MAX_ZOOM;
		this.hiddenFeature = {};

		this.styleMap = new OpenLayers.StyleMap({
			"default": Ext.decode(options.geoAttribute.style),
			"select": new OpenLayers.Style(OpenLayers.Feature.Vector.style["default"]),
			"temporary": Ext.decode(options.geoAttribute.style)
		});

		this.protocol = new OpenLayers.Protocol.HTTP({
			url: 'services/json/gis/getgeocardlist',
			params: {
				idClass: options.targetClassId,
				attribute: options.geoAttribute.name
			},
			format: new OpenLayers.Format.GeoJSON()
		});

		this.strategies = [
			new OpenLayers.Strategy.BBOX({
				autoActivate: true
			}),
			new OpenLayers.Strategy.Refresh({
				autoActivate: true
			})
		];

		OpenLayers.Layer.Vector.prototype.initialize.apply(this, arguments);
	},

	activateStrategies: function(activate) {
		for (var i=0, strategy=null; i < this.strategies.length; ++i) {
			strategy = this.strategies[i];

			if (activate) {
				strategy.activate();
				if (typeof strategy.refresh == "function") {
					strategy.refresh();
				}
			} else {
				strategy.deactivate();
			}
		}
	},

	refreshStrategies: function() {
		for (var i=0, strategy=null; i<this.strategies.length; ++i) {
			strategy = this.strategies[i];
			if (strategy.refresh) {
				strategy.force = true;
				strategy.refresh();
				strategy.force = false;
			}
		}
	},

	selectFeatureByMasterCard: function(masterCardId) {
		var f = this.getFeatureByMasterCard(masterCardId);
		this.selectFeature(f);
	},

	selectFeature: function(f) {
		if (f) {
			if (this.editLayer) {
				this.lastSelection = f.clone();
				this.removeFeatures([f]);

				this.editLayer.removeAllFeatures();
				this.editLayer.addFeatures(f.clone());
			}
		}
	},

	clearSelection: function() {
		if (this.lastSelection) {
			// restore the feature that was selected
			this.addFeatures( [this.lastSelection.clone()]);
			this.lastSelection = undefined;
		}

		if (this.editLayer) {
			this.editLayer.removeAllFeatures();
		}
	},

	getFeatureByMasterCard: function(masterCardId) {
		var features = this.features;
		for (var i=0, l = features.length; i < l; ++i) {
			var f = features[i];
			if (f.attributes.master_card == masterCardId) {
				return f;
			}
		}
		return null;
	},

	hideFeatureWithCardId: function(masterCardId) {
		var f = this.getFeatureByMasterCard(masterCardId);
		if (f) {
			this.hiddenFeature[masterCardId] = f.clone();
			this.removeFeatures([f]);
		}
	},

	showFeatureWithCardId: function(masterCardId) {
		var f = this.hiddenFeature[masterCardId];
		if (f) {
			this.addFeatures([f.clone()]);

			delete this.hiddenFeature[masterCardId];
		}
	},

	getEditedGeometry: function() {
		try {
			var f = this.editLayer.features;
			return f[0].geometry;
		} catch (Error) {
			return null;
		}
	}
});

})();