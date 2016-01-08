(function() {

	Ext.define('CMDBuild.core.proxy.localization.Localization', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.localization.Localization'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		getLanguages: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.utils.listAvailableTranslations });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.LOCALIZATION, parameters);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreLanguages: function() {
			return CMDBuild.core.cache.Cache.requestAsStore(CMDBuild.core.constants.Proxy.LOCALIZATION, {
				autoLoad: true,
				model: 'CMDBuild.model.localization.Localization',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.utils.listAvailableTranslations,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.TRANSLATIONS
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreSections: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.DESCRIPTION, CMDBuild.core.constants.Proxy.NAME],
				data: [
					['@@ All', CMDBuild.core.constants.Proxy.ALL],
					['@@ Classes', CMDBuild.core.constants.Proxy.CLASS],
					['@@ Processes', CMDBuild.core.constants.Proxy.PROCESS],
					['@@ Domains', CMDBuild.core.constants.Proxy.DOMAIN],
					['@@ Views', CMDBuild.core.constants.Proxy.VIEW],
					['@@ Search filters', CMDBuild.core.constants.Proxy.FILTER],
					['@@ Lookup types', CMDBuild.core.constants.Proxy.LOOKUP],
					['@@ Report', CMDBuild.core.constants.Proxy.REPORT],
					['@@ Menu', CMDBuild.core.constants.Proxy.MENU]
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.localizations.translation.read });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.LOCALIZATION, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.localizations.translation.readAll });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.LOCALIZATION, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.localizations.translation.update });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.LOCALIZATION, parameters, true);
		}
	});

})();