(function() {

	Ext.define('CMDBuild.core.proxy.common.field.filter.advanced.window.Functions', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.common.field.filter.advanced.window.Function'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.common.field.filter.advanced.window.Function',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.functions.readAll,
					reader: {
						type: 'json',
						root: 'response'
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' }
				]
			});
		}
	});

})();