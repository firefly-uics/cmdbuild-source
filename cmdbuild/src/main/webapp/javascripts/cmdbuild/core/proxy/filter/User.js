(function() {

	/**
	 * Proxy for filters created by users
	 */
	Ext.define('CMDBuild.core.proxy.filter.User', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.filter.user.read });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.FILTER, parameters);
		},
		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {}
	});

})();