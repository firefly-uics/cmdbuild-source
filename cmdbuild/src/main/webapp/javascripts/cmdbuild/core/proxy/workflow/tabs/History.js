(function () {

	Ext.define('CMDBuild.core.proxy.workflow.tabs.History', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.index.Json',
			'CMDBuild.model.workflow.tabs.history.CardRecord'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.HISTORY, {
				autoLoad: false,
				model: 'CMDBuild.model.workflow.tabs.history.CardRecord',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.index.Json.history.workflow.activity.read,
					reader: {
						type: 'json',
						root: 'response.elements'
					}
				},
				sorters: [ // Setup sorters, also if server returns ordered collection
					{ property: CMDBuild.core.constants.Proxy.BEGIN_DATE, direction: 'DESC' }
				]
			});
		},

		/**
		 * @property {Object} parameters
		 *
		 * @returns {Void}
		 */
		readHistoric: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.history.workflow.activity.readHistoric });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.HISTORY, parameters);
		},

		/**
		 * @property {Object} parameters
		 *
		 * @returns {Void}
		 */
		readHistoricRelation: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.history.workflow.activity.readHistoricRelation });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.HISTORY, parameters);
		},

		/**
		 * @property {Object} parameters
		 *
		 * @returns {Void}
		 */
		readRelations: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.history.workflow.activity.readRelations });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.HISTORY, parameters);
		}
	});

})();
