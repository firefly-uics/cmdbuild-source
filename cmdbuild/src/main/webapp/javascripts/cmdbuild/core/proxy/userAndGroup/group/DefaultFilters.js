(function () {

	Ext.define('CMDBuild.core.proxy.userAndGroup.group.DefaultFilters', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.index.Json',
			'CMDBuild.model.userAndGroup.group.defaultFilters.Filter'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getClassFiltersStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.FILTER, {
				autoLoad: false,
				model: 'CMDBuild.model.userAndGroup.group.defaultFilters.Filter',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.index.Json.group.defaultFilters.readAllGroupFilters,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.FILTERS
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
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

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.group.defaultFilters.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DEFAULT_FILTER, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.group.defaultFilters.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DEFAULT_FILTER, parameters, true);
		}
	});

})();
