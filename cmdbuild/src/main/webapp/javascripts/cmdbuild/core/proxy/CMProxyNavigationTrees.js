(function() {

	Ext.define('CMDBuild.core.proxy.NavigationTree', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.navigationTrees.create });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.NAVIGATION_TREE, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.navigationTrees.read });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.NAVIGATION_TREE, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.navigationTrees.readAll });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.NAVIGATION_TREE, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters, success) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.navigationTrees.remove });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.NAVIGATION_TREE, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.navigationTrees.update });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.NAVIGATION_TREE, parameters, true);
		}
	});

})();