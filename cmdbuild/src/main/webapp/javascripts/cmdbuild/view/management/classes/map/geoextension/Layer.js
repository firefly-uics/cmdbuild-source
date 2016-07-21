(function() {
	var WMS_IMAGE_FORMAT = 'image/png';
	var GOESERVER_SERVICE_TYPE = "wms";
	var GEOSERVER = "_Geoserver";
	var DEFAULT_MIN_ZOOM = 0;
	var DEFAULT_MAX_ZOOM = 25;
	Ext.define('CMDBuild.view.management.classes.map.geoextension.Layer', {
		constructor : function(geoAttribute, withEditWindow, interactionDocument) {
			this.interactionDocument = interactionDocument;
			this.layer = buildGeoserverLayer(geoAttribute);
			this.layer.set("name",  geoAttribute.name);
			this.callParent(arguments);
		},
		getLayer : function() {
			return this.layer;
		},
		getSource : function() {
			return this.layer.getSource();
		}
	});
	function buildGeoserverLayer(geoAttribute) {
			var geoserver_ws = CMDBuild.configuration.gis.get([
					CMDBuild.core.constants.Proxy.GEO_SERVER, 'workspace' ]); 
			var geoserver_url = CMDBuild.configuration.gis.get([
					CMDBuild.core.constants.Proxy.GEO_SERVER, 'url' ]); 
			var source = new ol.source.TileWMS({
				url : geoserver_url + "/" + GOESERVER_SERVICE_TYPE,
				params : {
					layers : geoserver_ws + ":" + geoAttribute.name,
					format : WMS_IMAGE_FORMAT,
					transparent : true
				}
			});
				
			var layer = new ol.layer.Tile({
				title : geoAttribute.description,
				source : source
			});
			layer.cmdb_minZoom = geoAttribute.minZoom || DEFAULT_MIN_ZOOM;
			layer.cmdb_maxZoom = geoAttribute.maxZoom || DEFAULT_MAX_ZOOM;
			layer.geoAttribute = geoAttribute;
			layer.editLayer = undefined;
			layer.strategies = [new OpenLayers.Strategy.BBOX()];
			layer.cmdb_index = geoAttribute.cmdb_index;
			layer.set("name",  geoAttribute.name);

			layer.CMDBuildLayer = false;
			layer.CM_Layer = false;
			layer.CM_geoserverLayer = true;
			return layer;
		}

})();
