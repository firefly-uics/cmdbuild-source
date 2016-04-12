(function () {

	Ext.define('CMDBuild.core.proxy.localization.Localization', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.index.Json',
			'CMDBuild.model.localization.Localization'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreLanguages: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.LOCALIZATION, {
				autoLoad: true,
				model: 'CMDBuild.model.localization.Localization',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.index.Json.utils.readAllAvailableTranslations,
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
		getStoreSections: function () {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.DESCRIPTION, CMDBuild.core.constants.Proxy.NAME],
				data: [
					[CMDBuild.Translation.all, CMDBuild.core.constants.Proxy.ALL],
					[CMDBuild.Translation.classes, CMDBuild.core.constants.Proxy.CLASS],
					[CMDBuild.Translation.processes, CMDBuild.core.constants.Proxy.PROCESS],
					[CMDBuild.Translation.domains, CMDBuild.core.constants.Proxy.DOMAIN],
					[CMDBuild.Translation.views, CMDBuild.core.constants.Proxy.VIEW],
					[CMDBuild.Translation.searchFilters, CMDBuild.core.constants.Proxy.FILTER],
					[CMDBuild.Translation.lookupTypes, CMDBuild.core.constants.Proxy.LOOKUP],
					[CMDBuild.Translation.report, CMDBuild.core.constants.Proxy.REPORT],
					[CMDBuild.Translation.menu, CMDBuild.core.constants.Proxy.MENU]
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		read: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.localization.translation.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOCALIZATION, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAll: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.localization.translation.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOCALIZATION, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.localization.translation.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOCALIZATION, parameters, true);
		}
	});

})();
