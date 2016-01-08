(function() {

	Ext.define('CMDBuild.core.proxy.domain.Domain', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.domain.create });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.DOMAINS, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		getList: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.domain.getDomainList });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.DOMAINS, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.domain.read });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.DOMAINS, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.domain.readAll });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.DOMAINS, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.domain.remove });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.DOMAINS, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.domain.update });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.DOMAINS, parameters, true);
		}
	});

})();