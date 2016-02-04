(function() {

	Ext.define('CMDBuild.core.proxy.Gis', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		readTreeNavigation: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.gis.readTreeNavigation });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.GIS, parameters);
		}
	});

})();