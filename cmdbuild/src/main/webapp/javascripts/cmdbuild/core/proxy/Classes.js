(function() {

	Ext.define('CMDBuild.core.proxy.Classes', {
		alternateClassName: 'CMDBuild.ServiceProxy.classes', // Legacy class name

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * TODO: waiting for refactor (crud)
		 */
		create: function(parameters) {},

		/**
		 * @param {Object} parameters
		 *
		 * TODO: waiting for refactor (crud)
		 */
		read: function(parameters) {},

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.classes.readAll });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.CLASS, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.classes.remove });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.CLASS, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * TODO: waiting for refactor (crud), rename as update
		 */
		save: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.classes.update });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.CLASS, parameters, true);
		}
	});

})();