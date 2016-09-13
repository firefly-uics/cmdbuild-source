
 (function () {
		var EXTERNAL_LAYERS_FOLDER_NAME = "cm_external_layers_folder";
		var CMDBUILD_LAYERS_FOLDER_NAME = "cm_cmdbuild_layers_folder";
		var GEOSERVER_LAYERS_FOLDER_NAME = "cm_geoserver_layers_folder";

	 Ext.define('CMDBuild.proxy.gis.Layer', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.gis.Layer',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		getStore: function (parameters) {
			var pageSize = CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT);
			return Ext.create("Ext.data.TreeStore", {
				root : {
					expanded: true,
					children : [{
						text: CMDBuild.Translation.administration.modClass.tabs.geo_attributes,
						leaf: false,
//						expanded: true,
						layerName: CMDBUILD_LAYERS_FOLDER_NAME,
						checked: true
					}, {
						text: CMDBuild.Translation.administration.modcartography.external_services.title,
						leaf: false,
//						expanded: true,
						layerName: EXTERNAL_LAYERS_FOLDER_NAME,
						checked: true
					}]
				},
				rootVisible : false,
				autoLoad : true,
				model: 'CMDBuild.model.gis.Layer'
			});
		},
		readAll: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.gis.layer.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.GIS, parameters);
		},
	});

})();

