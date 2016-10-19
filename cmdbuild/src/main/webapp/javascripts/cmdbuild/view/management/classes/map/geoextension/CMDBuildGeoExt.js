(function() {

	Ext.define("CMDBuild.view.management.classes.map.geoextension.CMDBuildGeoExt", {
		baseLayer : undefined,
		constructor : function() {
			this.initBaseLayer();
			this.callParent(arguments);
		},
		setMap : function(mapPanel) {
			this.mapPanel = mapPanel;
		},
		getBaseLayer : function() {
			return this.baseLayer;
		},
		initBaseLayer : function() {
			var baseLayer = undefined;
			if (CMDBuild.configuration.gis.get([ CMDBuild.gis.constants.MAP_OSM, CMDBuild.gis.constants.ENABLED ])) {
				var osm_source = new ol.source.OSM({
					url : 'http://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png'
				});
				baseLayer = new ol.layer.Tile({
					source : osm_source
				});
			} else if (CMDBuild.configuration.gis.get([ CMDBuild.gis.constants.MAP_GOOGLE,
					CMDBuild.gis.constants.ENABLED ])) {
				var osm_source = new ol.source.OSM({
					url : 'http://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png'
				});
				baseLayer = new ol.layer.Tile({
					source : osm_source
				});
			} else if (CMDBuild.configuration.gis.get([ CMDBuild.gis.constants.MAP_YAHOO,
					CMDBuild.gis.constants.ENABLED ])) {
				var osm_source = new ol.source.OSM({
					url : 'http://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png'
				});
				baseLayer = new ol.layer.Tile({
					source : osm_source
				});

			}
			this.baseLayer = baseLayer;
		}
	});

})();
