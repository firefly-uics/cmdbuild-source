(function() {

	Ext.define('CMDBuild.core.configurationBuilders.Bim', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Configuration'
		],

		constructor: function() {
			Ext.ns('CMDBuild.configuration');

			CMDBuild.configuration[CMDBuild.core.constants.Proxy.BIM] = Ext.create('CMDBuild.model.configuration.Bim'); // BIM configuration object

			CMDBuild.core.proxy.Configuration.readBimConfiguration({
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					CMDBuild.configuration[CMDBuild.core.constants.Proxy.BIM] = Ext.create('CMDBuild.model.configuration.Bim', decodedResponse);
				}
			});
		}
	});

})();