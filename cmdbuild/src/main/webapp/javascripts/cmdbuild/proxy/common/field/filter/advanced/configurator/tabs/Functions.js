(function () {

	Ext.define('CMDBuild.proxy.common.field.filter.advanced.configurator.tabs.Functions', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.common.field.filter.advanced.configurator.tabs.functions.Function',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.FUNCTION, {
				autoLoad: true,
				model: 'CMDBuild.model.common.field.filter.advanced.configurator.tabs.functions.Function',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.functions.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.RESPONSE
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' }
				]
			});
		}
	});

})();
