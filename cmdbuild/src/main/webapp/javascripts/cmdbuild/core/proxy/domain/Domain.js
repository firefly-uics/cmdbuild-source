(function() {

	Ext.define('CMDBuild.core.proxy.domain.Domain', {

		requires: [
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

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DOMAINS, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		getList: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.domain.getDomainList });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DOMAINS, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.domain.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DOMAINS, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.domain.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DOMAINS, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.domain.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DOMAINS, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.domain.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DOMAINS, parameters, true);
		}
	});

})();
