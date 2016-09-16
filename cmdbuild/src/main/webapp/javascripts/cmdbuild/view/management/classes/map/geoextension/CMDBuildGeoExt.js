(function() {

	Ext.define("CMDBuild.view.management.classes.map.geoextension.CMDBuildGeoExt", {
		baseLayer : undefined,
		constructor : function () {
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
			if (CMDBuild.configuration.gis.get([CMDBuild.gis.constants.MAP_OSM, CMDBuild.gis.constants.ENABLED])) {
				var osm_source = new ol.source.OSM(
						{
							url : 'http://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png'
						});
				baseLayer = new ol.layer.Tile({
					source : osm_source
				});
			} else if (CMDBuild.configuration.gis.get([CMDBuild.gis.constants.MAP_GOOGLE, CMDBuild.gis.constants.ENABLED])) {
				var layer= new ol.layer.Tile({

				    source: new ol.source.OSM({
			            url : 'http://mt{0-3}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}',
			            attributions: [
			                new ol.Attribution({ html: '© Google' }),
			                new ol.Attribution({ html: '<a href="https://developers.google.com/maps/terms">Terms of Use.</a>' })
			            ]
			        })
				});
				baseLayer = layer;
				baseLayer =  new ol.layer.Tile({/*NB!!!!!!!!!*/
                    source: new ol.source.Stamen({
                        layer: 'watercolor'
                    })
                });
			} else if (CMDBuild.configuration.gis.get([CMDBuild.gis.constants.MAP_YAHOO, CMDBuild.gis.constants.ENABLED])) {
			
			}
			this.baseLayer = baseLayer;
		}
	});

})();
