(function() {

	Ext.define('CMDBuild.core.proxy.common.tabs.history.Classes', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.model.common.tabs.history.CardRecord'
		],

		singleton: true,

		/**
		 * @property {Object} params
		 */
		get: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.card.getCardHistory,
				headers: parameters.headers,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @property {Object} params
		 */
		getHistoric: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.card.getHistoricCard,
				headers: parameters.headers,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || true,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @property {Object} params
		 */
		getRelations: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.card.getRelationsHistory,
				headers: parameters.headers,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @property {Object} params
		 */
		getRelationHistoric: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.card.getHistoricRelation,
				headers: parameters.headers,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || true,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.common.tabs.history.CardRecord',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.card.getCardHistory,
					reader: {
						type: 'json',
						root: 'response.elements'
					}
				},
				sorters: [ // Setup sorters, also if server returns ordered collection, to use addSorted store function
					{ property: CMDBuild.core.proxy.CMProxyConstants.BEGIN_DATE, direction: 'DESC' }
				]
			});
		}
	});

})();