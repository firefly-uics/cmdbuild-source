(function() {

	Ext.define('CMDBuild.core.proxy.common.tabs.history.Classes', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.common.tabs.history.classes.CardRecord'
		],

		singleton: true,

		/**
		 * @property {Object} parameters
		 */
		get: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.history.classes.getCardHistory,
				headers: parameters.headers,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @property {Object} parameters
		 */
		getHistoric: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.history.classes.getHistoricCard,
				headers: parameters.headers,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @property {Object} parameters
		 */
		getRelations: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.history.classes.getRelationsHistory,
				headers: parameters.headers,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @property {Object} parameters
		 */
		getRelationHistoric: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.history.classes.getHistoricRelation,
				headers: parameters.headers,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
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
				model: 'CMDBuild.model.common.tabs.history.classes.CardRecord',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.history.classes.getCardHistory,
					reader: {
						type: 'json',
						root: 'response.elements'
					}
				},
				sorters: [ // Setup sorters, also if server returns ordered collection
					{ property: CMDBuild.core.constants.Proxy.BEGIN_DATE, direction: 'DESC' }
				]
			});
		}
	});

})();