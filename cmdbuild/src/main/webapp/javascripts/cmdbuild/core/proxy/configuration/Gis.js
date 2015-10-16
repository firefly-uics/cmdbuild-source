(function() {

	Ext.define('CMDBuild.core.proxy.configuration.Gis', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.configurationBuilders.Gis',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;
			parameters.params = Ext.isEmpty(parameters.params) ? {} : parameters.params;
			parameters.params[CMDBuild.core.constants.Proxy.NAME] = 'gis';

			parameters.success = Ext.Function.createInterceptor(parameters.success, function(response, options, decodedResponse) {
				if (!CMDBuild.core.configurationBuilders.Gis.isValid())
					CMDBuild.core.configurationBuilders.Gis.build(decodedResponse); // Refresh CMDBuild configuration object
			}, this);

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.configuration.read });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.CONFIGURATION, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;
			parameters.params = Ext.isEmpty(parameters.params) ? {} : parameters.params;
			parameters.params[CMDBuild.core.constants.Proxy.NAME] = 'gis';

			CMDBuild.core.configurationBuilders.Gis.invalid(); // Invalidate CMDBuild configuration object

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.configuration.update });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.CONFIGURATION, parameters, true);
		}
	});

})();