(function() {

	Ext.define('CMDBuild.core.proxy.configuration.RelationGraph', {

		requires: [
			'CMDBuild.core.cache.Cache',
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
			parameters.params[CMDBuild.core.constants.Proxy.NAME] = 'graph';

			parameters.success = Ext.Function.createInterceptor(parameters.success, function(response, options, decodedResponse) {
				if (!CMDBuild.core.configurationBuilders.RelationGraph.isValid())
					CMDBuild.core.configurationBuilders.RelationGraph.build(decodedResponse); // Refresh configuration object
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
			parameters.params[CMDBuild.core.constants.Proxy.NAME] = 'graph';

			CMDBuild.core.configurationBuilders.RelationGraph.invalid(); // Invalidate configuration object

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.configuration.update });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.CONFIGURATION, parameters, true);
		}
	});

})();