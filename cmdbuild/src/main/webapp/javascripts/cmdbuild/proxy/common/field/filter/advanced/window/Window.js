(function () {

	Ext.define('CMDBuild.proxy.common.field.filter.advanced.window.Window', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.common.field.filter.advanced.Filter',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * Returns a store with the filters for a given group
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreGroup: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.WORKFLOW, {
				autoLoad: false,
				model: 'CMDBuild.model.common.field.filter.advanced.Filter',
				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT),
				proxy: {
					url: CMDBuild.proxy.index.Json.filter.group.readAll,
					type: 'ajax',
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.FILTERS,
						totalProperty: CMDBuild.core.constants.Proxy.COUNT
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
		readFilterUser: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.filter.user.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.FILTER, parameters);
		}
	});

})();
