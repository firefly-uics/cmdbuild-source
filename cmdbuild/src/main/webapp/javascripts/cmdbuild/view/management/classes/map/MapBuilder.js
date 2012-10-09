CMDBuild.Management.MapBuilder = (function() {

	var bounds = new OpenLayers.Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34),
		geojson_format = new OpenLayers.Format.GeoJSON(),
		projection = new OpenLayers.Projection("EPSG:900913"),
		displayProjection = new OpenLayers.Projection("EPSG:4326");

	function buildMap(divId) {
		var options = {
			projection: projection,
			displayProjection: displayProjection,
			units: "m",
			numZoomLevels: 25,
			maxResolution: 156543.0339,
			maxExtent: bounds,
			div: divId,
			initBaseLayers: initBaseLayers,
			eventListeners: {
				"zoomend": function(event) {
					/*
					 * Manage the visibility of
					 * the features
					 */
					var map = event.object;
					var layers = map.layers;
					var currentZoom = map.getZoom();
					for (var i=0, l=layers.length; i<l; ++i) {
						var layer = layers[i];
						if (layer.setVisibilityByZoom) {
							layer.setVisibilityByZoom(currentZoom);
						}
					}
				}
			}
		};

		var map = new CMDBuild.Management.CMMap(options);

//		map.addControl(new OpenLayers.Control.LayerSwitcher());

		map.addControl(new CMDBuild.Management.CMZoomAndMousePositionControl({
			zoomLabel : CMDBuild.Translation.management.modcard.gis.zoom,
			positionLabel : CMDBuild.Translation.management.modcard.gis.position
		}));

		addFakeLayer(map);

		return map;
	};

	function initBaseLayers() {
		var DEFAULT_MIN_ZOOM = 0,
			DEFAULT_MAX_ZOOM = 18,
			gisConfig = CMDBuild.Config.gis,
			map = this;

		if (gisConfig.osm && gisConfig.osm == "on") {
			var osm = new OpenLayers.Layer.OSM("Open Street Map", null, {
				numZoomLevels: 25,
				cmdb_minZoom: gisConfig.osm_minzoom || DEFAULT_MIN_ZOOM,
				cmdb_maxZoom: gisConfig.osm_maxzoom || DEFAULT_MAX_ZOOM,
				setVisibilityByZoom: function(zoom) {
					var max = this.cmdb_maxZoom <= DEFAULT_MAX_ZOOM ? this.cmdb_maxZoom : DEFAULT_MAX_ZOOM;
					var isInRange = (zoom >= this.cmdb_minZoom && zoom <= max);

					this.setVisibility(isInRange);
				}
			});

			osm.CMDBuildLayer = true;
			map.addLayers([osm]);
			map.setBaseLayer(osm);
		}

		if (gisConfig.google && gisConfig.google == "on") {
			var googleLayer = new OpenLayers.Layer.Google(
				"Google",
				{
					sphericalMercator: true
				}
			);

			googleLayer.cmdb_minZoom = gisConfig.google_minzoom || DEFAULT_MIN_ZOOM;
			googleLayer.cmdb_maxZoom = gisConfig.google_maxzoom || DEFAULT_MAX_ZOOM;
			googleLayer.setVisibilityByZoom = function(zoom) {
				var max = this.cmdb_maxZoom <= DEFAULT_MAX_ZOOM ? this.cmdb_maxZoom : DEFAULT_MAX_ZOOM;
				var isInRange = (zoom >= this.cmdb_minZoom && zoom <= max);

				this.setVisibility(isInRange);
			};
			googleLayer.CMDBuildLayer = true;
			map.addLayers([googleLayer]);
			map.setBaseLayer(googleLayer);
		}

		if (gisConfig.yahoo && gisConfig.yahoo == "on") {
			var yahooLayer = new OpenLayers.Layer.Yahoo(
				"Yahoo",
				{
					sphericalMercator: true
				}
			);
			yahooLayer.cmdb_minZoom = gisConfig.yahoo_minzoom || DEFAULT_MIN_ZOOM;
			yahooLayer.cmdb_maxZoom = gisConfig.yahoo_maxzoom || DEFAULT_MAX_ZOOM;
			yahooLayer.setVisibilityByZoom = function(zoom) {
				var max = this.cmdb_maxZoom <= DEFAULT_MAX_ZOOM ? this.cmdb_maxZoom : DEFAULT_MAX_ZOOM;
				var isInRange = (zoom >= this.cmdb_minZoom && zoom <= max);

				this.setVisibility(isInRange);
			};
			yahooLayer.CMDBuildLayer = true;
			map.addLayers([yahooLayer]);
			map.setBaseLayer(yahooLayer);
		}

		// could not build a map without a base layer
		// if there are no layers in the configuration
		// ad a fake one.
		if (map.layers.length == 0) {
			addFakeLayer(map);
		}
	};

	function addFakeLayer(map) {
		var fakeBaseLayer = new OpenLayers.Layer.Vector("", {
			displayInLayerSwitcher: false,
			isBaseLayer: true
		});

		map.addLayers([fakeBaseLayer]);
	}

	return {
		buildMap: buildMap
	};
})();