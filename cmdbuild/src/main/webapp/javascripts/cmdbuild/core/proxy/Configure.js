(function() {

	Ext.define('CMDBuild.core.proxy.Configure', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		apply: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getConfigurationSetup(), // Get report timeout from configuration
				url: CMDBuild.core.proxy.Index.configuration.apply
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.CONFIGURATION, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		dbConnectionCheck: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.configuration.connectionTest });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.CONFIGURATION, parameters);
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreDbTypes: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: [
					[CMDBuild.core.constants.Proxy.EMPTY, CMDBuild.Translation.empty],
					[CMDBuild.core.constants.Proxy.DEMO, CMDBuild.Translation.demo],
					[CMDBuild.core.constants.Proxy.EXISTING, CMDBuild.Translation.existing]
				]
			});
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreUserType: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: [
					['superuser', CMDBuild.Translation.superUser],
					['limuser', CMDBuild.Translation.limitedUser],
					['new_limuser', CMDBuild.Translation.createLimitedUser]
				]
			});
		}
	});

})();