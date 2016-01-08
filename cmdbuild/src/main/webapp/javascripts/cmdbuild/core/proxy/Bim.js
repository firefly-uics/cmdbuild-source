(function() {

	Ext.define('CMDBuild.core.proxy.Bim', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		readRootLayer: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.bim.readRootLayer });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.BIM, parameters);
		}
	});

})();