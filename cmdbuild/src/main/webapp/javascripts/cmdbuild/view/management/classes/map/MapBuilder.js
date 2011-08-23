CMDBuild.Management.MapBuilder = (function() {
	var bounds = new OpenLayers.Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34);
	var geojson_format = new OpenLayers.Format.GeoJSON();
	var projection = new OpenLayers.Projection("EPSG:900913");
	var displayProjection = new OpenLayers.Projection("EPSG:4326");	
	
	function buildMap(divId) {
		var navControl = new OpenLayers.Control.Navigation();
		var mouseControl = new OpenLayers.Control.MousePosition();

		var options = {
	        projection: projection,
	        displayProjection: displayProjection,
	        units: "m",
	        numZoomLevels: 25,
	        maxResolution: 156543.0339,
	        maxExtent: bounds,
//	        controls: [navControl, mouseControl],
	        div: divId,
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
		var map = new CMDBuild.Management.CMDBuildMap(options);
		addBaseLayers(map);
		return map;
	};

	function addBaseLayers(map) {
		var DEFAULT_MIN_ZOOM = 0;
		var DEFAULT_MAX_ZOOM = 24;
		var gisConfig = CMDBuild.Config.gis;
		
		if (gisConfig.osm && gisConfig.osm == "on") {
            var osm = new OpenLayers.Layer.OSM("Open Street Map", null, {
                numZoomLevels: 25
            });
			osm.cmdbuildMinZoom = gisConfig.osm_minzoom || DEFAULT_MIN_ZOOM;
			osm.cmdbuildMaxZoom = gisConfig.osm_maxzoom || DEFAULT_MAX_ZOOM;
			map.addLayers([osm]);
		}
		
		if (gisConfig.google && gisConfig.google == "on") {
			var googleLayer = new OpenLayers.Layer.Google(
		        "Google",
		        {
		        	sphericalMercator: true
		        }
		    );
			googleLayer.cmdbuildMinZoom = gisConfig.google_minzoom || DEFAULT_MIN_ZOOM;
			googleLayer.cmdbuildMaxZoom = gisConfig.google_maxzoom || DEFAULT_MAX_ZOOM;
			map.addLayers([googleLayer]);
		}
		
		if (gisConfig.yahoo && gisConfig.yahoo == "on") {
			var yahooLayer = new OpenLayers.Layer.Yahoo(
                "Yahoo",
                {
                	sphericalMercator: true
                }
            );
			yahooLayer.cmdbuildMinZoom = gisConfig.yahoo_minzoom || DEFAULT_MIN_ZOOM;
			yahooLayer.cmdbuildMaxZoom = gisConfig.yahoo_maxzoom || DEFAULT_MAX_ZOOM;
			map.addLayers([yahooLayer]);
		}
		
		if (map.layers.length == 0) {
            var fakeBaseLayer = new OpenLayers.Layer.Vector("", {
            	displayInLayerSwitcher: false,
            	isBaseLayer: true
            });
            map.addLayers([fakeBaseLayer]);
		}
	};
	
	return {
		buildMap: buildMap
	};
})();