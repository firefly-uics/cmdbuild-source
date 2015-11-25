(function() {

	Ext.define('CMDBuild.core.proxy.configuration.Configuration', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				params:{ names: Ext.JSON.encode(['bim', 'cmdbuild', 'dms', 'gis', 'graph', 'workflow']) },
				url: CMDBuild.core.proxy.Index.configuration.readAll
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.CONFIGURATION, parameters);
		}
	});

})();