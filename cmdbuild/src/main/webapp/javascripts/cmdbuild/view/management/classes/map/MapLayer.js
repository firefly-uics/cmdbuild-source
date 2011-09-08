(function() {
	var DEFAULT_MIN_ZOOM = 0;
	var DEFAULT_MAX_ZOOM = 25;

/**
 * @class CMDBuild.Management.CMDBuildMap.MapLayer
 */
CMDBuild.Management.CMMap.MapLayer = OpenLayers.Class(OpenLayers.Layer.Vector, {
	initialize: function(name, options) {

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

		this.refreshFeatures = function() {
			var bboxStrategie = this.strategies[0];
			if (bboxStrategie.invalidBounds()) {
				bboxStrategie.calculateBounds();
			}
			bboxStrategie.triggerRead();
		};

		this.cmdb_minZoom = options.geoAttribute.minZoom || DEFAULT_MIN_ZOOM;
		this.cmdb_maxZoom = options.geoAttribute.maxZoom || DEFAULT_MAX_ZOOM;

		OpenLayers.Layer.Vector.prototype.initialize.apply(this, arguments);
	},
	projection: new OpenLayers.Projection("EPSG:900913"),

	// CMDBuild stuff
	editLayer: undefined,
	geoAttribute: undefined,

	activateStrategies: function(activate) {
		for (var strategy in this.strategies) {
			strategy = this.strategies[strategy];

			if (activate) {
				strategy.activate();
				if (strategy.refresh) {
					strategy.refresh();
				}
			} else {
				strategy.deactivate();
			}
		}
	},

	refreshStrategies: function() {
		for (var strategy in this.strategies) {
			strategy = this.strategies[strategy];
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
			var dolly = this.lastSelection.clone();
			dolly.cmForceAdd = true; // see onCmdbLayerBeforeAdd in CMMapController

			this.addFeatures( [ dolly ]);
			this.lastSelection = undefined;
		}

		if (this.editLayer) {
			this.editLayer.removeAllFeatures();
		}
	},

	getFeatureByMasterCard: function(masterCard) {
		var features = this.features;
		for ( var i = 0, l = features.length; i < l; ++i) {
			var f = features[i];
			if (f.attributes.master_card == masterCard) {
				return f;
			}
		}
		return null;
	},

	getEditedGeometry: function() {
		try {
			var f = this.editLayer.features;
			return f[0].geometry;
		} catch (Error) {
			return null;
		}
	},

	reselectLastSelection: function() {
		if (this.lastSelection) {
			this.selectFeature(this.lastSelection);
		}
	}
});

})();