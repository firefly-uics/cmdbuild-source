(function() {

	Ext.define('CMDBuild.core.configurationBuilders.Gis', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Configuration'
		],

		constructor: function() {
			Ext.ns('CMDBuild.configuration');

			CMDBuild.configuration[CMDBuild.core.constants.Proxy.GIS] = Ext.create('CMDBuild.model.configuration.gis.Gis'); // GIS configuration object

			CMDBuild.core.proxy.Configuration.readGisConfiguration({
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					CMDBuild.configuration[CMDBuild.core.constants.Proxy.GIS] = Ext.create('CMDBuild.model.configuration.gis.Gis', decodedResponse);
				}
			});
		}
	});

})();