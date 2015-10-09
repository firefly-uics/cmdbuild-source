(function() {

	Ext.define('CMDBuild.core.proxy.dataView.Sql', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.core.Utils'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 * @param {Array} parameters.fields
		 *
		 * @return {Ext.data.Store}
		 */
		getStore: function(parameters) {
			parameters = parameters || {};

			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				fields: parameters.fields || [],
				pageSize: CMDBuild.core.Utils.getPageSize(),
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.card.getSqlCardList,
					reader: {
						type: 'json',
						root: 'cards',
						totalProperty: 'results'
					}
				}
			});
		}
	});

})();