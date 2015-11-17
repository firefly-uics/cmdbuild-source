(function() {

	Ext.define('CMDBuild.core.proxy.widgets.Grid', {

		requires: [
			'CMDBuild.core.interfaces.Ajax',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * Validates presets function name
		 *
		 * @param {Object} parameters
		 */
		getFunctions: function(parameters) {
			CMDBuild.core.interfaces.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.functions.readAll,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @return {Ext.data.Store}
		 */
		getStoreFromFunction: function(parameters) {
			// Avoid to send limit, page and start parameters in server calls
			parameters.extraParams.limitParam = undefined;
			parameters.extraParams.pageParam = undefined;
			parameters.extraParams.startParam = undefined;

			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				fields: parameters.fields,
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.widgets.grid.getSqlCardList,
					reader: {
						root: 'cards',
						type: 'json',
						totalProperty: 'results',
					},
					extraParams: parameters.extraParams
				}
			});
		}
	});

})();